package iwb.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

//import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter.FilterExceptFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.dao.metadata.MetadataLoader;
import iwb.dao.metadata.rdbms.PostgreSQLWriter;
import iwb.dao.rdbms_impl.PostgreSQL;
import iwb.domain.db.Log5VcsAction;
import iwb.domain.db.W5Customization;
import iwb.domain.db.W5Project;
import iwb.domain.db.W5Query;
import iwb.domain.db.W5QueryField;
import iwb.domain.db.W5Table;
import iwb.domain.db.W5TableChild;
import iwb.domain.db.W5TableField;
import iwb.domain.db.W5VcsCommit;
import iwb.domain.db.W5VcsObject;
import iwb.domain.helper.W5TableRecordHelper;
import iwb.domain.result.W5QueryResult;
import iwb.exception.IWBException;
import iwb.util.DBUtil;
import iwb.util.GenericUtil;
import iwb.util.HttpUtil;
import iwb.util.LogUtil;
import iwb.util.UserUtil;

@Service
@Transactional
public class VcsService {
	@Autowired
	private PostgreSQL dao;
	

	@Lazy
	@Autowired
	private MetadataLoader metadataLoader;
	

	@Lazy
	@Autowired
	private PostgreSQLWriter metadataWriter;
	

	synchronized public Map vcsClientObjectPull(Map<String, Object> scd, int tableId, int tablePk, boolean force) {
		if(FrameworkSetting.vcsServer && !FrameworkSetting.vcsServerClient)
			throw new IWBException("vcs","vcsClientObjectPull",0,null, "VCS Server not allowed to vcsClientObjectPull", null);
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		
		W5Project po = FrameworkCache.getProject(projectUuid);

		W5Table t = FrameworkCache.getTable(projectUuid, tableId);
		if(t.getVcsFlag()==0){
			throw new IWBException("vcs","vcsClientObjectPull", t.getTableId(), po.getProjectUuid()+"!="+projectUuid, "Not VCS Table2", null);
		}
		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&t="+tableId+"&k="+tablePk+"&r="+po.getProjectUuid();
		
		List lv = dao.find("from W5VcsObject t where t.tableId=?0 AND t.tablePk=?1 AND t.customizationId=?2 AND t.projectUuid=?3", tableId, tablePk, customizationId, projectUuid);
		W5VcsObject vo = null;
		Map result = new HashMap();
		result.put("success", true);
		if(!lv.isEmpty()){
			vo = (W5VcsObject)lv.get(0);
			if(vo.getVcsObjectStatusTip()==2 && !force){
			//	throw new PromisException("vcs","vcsClientObjectPull", vo.getVcsCommitId(), null, "Object is New. Cannot be Pulled2", null);
				result.put("error", "force");
				result.put("error_msg", "Object is New. Cannot be Pulled2");
				return result;
			}
			urlParameters+="&o="+vo.getVcsCommitId();
		} else {
			vo = new W5VcsObject(scd, tableId, tablePk);
			vo.setTableId(tableId);
			vo.setTablePk(tablePk);
			vo.setProjectUuid(projectUuid);
			vo.setCustomizationId(customizationId);
		}

		
		if(vo.getVcsObjectStatusTip()==1 && !force){ //conflicts: edited but wants to pull
			result.put("error", "force");
			result.put("error_msg", "Conflicts");
			return result;
		}
		
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSObjectPull";
		String s = HttpUtil.send(url, urlParameters);
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			try {
				json = new JSONObject(s);
				if(json.get("success").toString().equals("true")){
//					String sql =json.getString("sql");
					int action = json.getInt("action");
					JSONObject jo =action == 3 ? null:json.getJSONObject("object");
					int srvVcsCommitId = json.getInt("commit_id");
					int srvCommitUserId = json.getInt("user_id");
					
					metadataWriter.saveVcsObject(scd, tableId, tablePk, vo.getVcsObjectStatusTip()==3 ? 2:action, jo);

					vo.setVcsObjectStatusTip((short)9);
					vo.setVcsCommitRecordHash(metadataWriter.getObjectVcsHash(scd, tableId, tablePk));
					
					if(vo.getVcsObjectId()==0){
						vo.setVcsCommitId(srvVcsCommitId);
						vo.setInsertUserId(srvCommitUserId);
						dao.saveObject(vo);
					} else {
						vo.setVersionNo((short)(vo.getVersionNo()+1));
						vo.setVersionUserId(srvCommitUserId);
						vo.setVcsCommitId(srvVcsCommitId);
						vo.setVersionDttm(new Timestamp(new Date().getTime()));
						dao.updateObject(vo);
					}
					if(FrameworkSetting.log2tsdb)Log4Crud(po.getRdbmsSchema(), t, action, srvVcsCommitId, tablePk, jo);
				} else
					throw new IWBException("vcs","vcsClientObjectPull:server Error Response", t.getTableId(), s, json.has("error") ? json.getString("error"): json.toString(), null);
			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientObjectPull:JSONException", t.getTableId(), s, "Error", e);
			}
		}
		return result;
	}

	
	synchronized public Map vcsClientObjectPullMulti(Map<String, Object> scd, String tableKeys, boolean force) {
		if(FrameworkSetting.vcsServer && !FrameworkSetting.vcsServerClient)
			throw new IWBException("vcs","vcsClientObjectPullMulti",0,null, "VCS Server not allowed to vcsClientObjectPullMulti", null);
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		
		W5Project po = FrameworkCache.getProject(projectUuid);
		
		String[] arTableKeys = tableKeys.split(",");
		
		
//		List lo = new ArrayList();
		JSONArray jlo = new JSONArray();
		Map<String, W5VcsObject> voMap = new HashMap();
		Map result = new HashMap();
		result.put("success", true);
		StringBuilder keyz = new StringBuilder();// + "&k="+tableKeys
		int counter=0;
		for(String k:arTableKeys){
			String[] tableKey = k.replace('.', ',').split(",");
			int tableId=GenericUtil.uInt(tableKey[0]);
			W5Table t = FrameworkCache.getTable(projectUuid, tableId);
			if(t.getVcsFlag()==0){
				continue;
			}
			keyz.append(",").append(k);
			int tablePk = GenericUtil.uInt(tableKey[1]);
			List lv = dao.find("from W5VcsObject t where t.tableId=?0 AND t.tablePk=?1 AND t.customizationId=?2 AND t.projectUuid=?3", tableId, tablePk, customizationId, projectUuid);
			W5VcsObject vo = null;
			if(!lv.isEmpty()){
				vo = (W5VcsObject)lv.get(0);
				if(vo.getVcsObjectStatusTip()==2 && !force){
//					if(continueOnFail)continue;
					//	throw new PromisException("vcs","vcsClientObjectPull", vo.getVcsCommitId(), null, "Object is New. Cannot be Pulled2", null);
					result.put("error", "force");
					result.put("error_msg", "Object is New. Cannot be Pulled2");
					return result;
				}
				keyz.append(".").append(vo.getVcsCommitId());
				vo.setVersionDttm(new Timestamp(new Date().getTime() + counter++));
			} else {
			
				vo = new W5VcsObject(scd, tableId, tablePk);
				vo.setTableId(tableId);
				vo.setTablePk(tablePk);
				vo.setCustomizationId(customizationId);
			}
			voMap.put(vo.getTableId()+"." + vo.getTablePk(), vo);
			
		}
		
		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&r="+po.getProjectUuid()+"&k="+keyz.substring(1);

		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSObjectPullMulti";
		String s = HttpUtil.send(url, urlParameters);
		

		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			try {
				json = new JSONObject(s);
				if(json.get("success").toString().equals("true")){
					JSONArray ja = json.getJSONArray("data");
					for(int qi=0;qi<ja.length();qi++){
						JSONObject o = ja.getJSONObject(qi);
						
						int action = o.getInt("action");
						JSONObject jo =action == 3 ? null:o.getJSONObject("object");
						int srvVcsCommitId = o.getInt("commit_id");
						int srvCommitUserId = o.getInt("user_id");
						int tableId = o.getInt("table_id");
						int tablePk = o.getInt("table_pk");

						metadataWriter.saveVcsObject(scd, tableId, tablePk, action, jo);
						W5VcsObject vo = voMap.get(tableId + "." + tablePk);
						vo.setVcsObjectStatusTip((short)9);
						vo.setVcsCommitRecordHash(metadataWriter.getObjectVcsHash(scd, tableId, tablePk));
						
						if(vo.getVcsObjectId()==0){
							vo.setVcsCommitId(srvVcsCommitId);
							vo.setInsertUserId(srvCommitUserId);
							dao.saveObject(vo);
						} else {
							vo.setVersionNo((short)(vo.getVersionNo()+1));
							vo.setVersionUserId(srvCommitUserId);
							vo.setVcsCommitId(srvVcsCommitId);
							dao.updateObject(vo);
						}
					}
					if(FrameworkSetting.log2tsdb)for(int qi=0;qi<ja.length();qi++){
						JSONObject o = ja.getJSONObject(qi);
						
						int action = o.getInt("action");
						JSONObject jo =action == 3 ? null:o.getJSONObject("object");
						int srvVcsCommitId = o.getInt("commit_id");
						int tableId = o.getInt("table_id");
						int tablePk = o.getInt("table_pk");
						W5Table t= FrameworkCache.getTable(scd, tableId);
						Log4Crud(po.getRdbmsSchema(), t, action, srvVcsCommitId, tablePk, jo);
					}

				} else {
					result.put("success", false);
					result.put("error", s);
					return result;
					
				}
			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientObjectPullMulti:JSONException", 0, s, "Error", e);
			}
		}
		return result;
	}
	private void Log4Crud(String schema, W5Table t, int action, int srvVcsCommitId, int tablePk, JSONObject jo)
			throws JSONException {
		StringBuilder ql= new StringBuilder();
		ql.append(schema).append("_").append(t.getDsc().replace('.', '_')).append(",").append(t.get_tableParamList().get(0).getExpressionDsc()).append("=").append(tablePk);
		Map m = jo==null ? new HashMap() : GenericUtil.fromJSONObjectToMap(jo);
		m.put("_action", action);
		m.put("_vcs_commit_id", srvVcsCommitId);
		ql.append(" ").append(GenericUtil.fromMapToInfluxFields(m));
		LogUtil.logCrud(ql.toString());
	}
	

	public Map vcsServerObjectPullMulti(String userName, String passWord, int customizationId, String projectId, String tableKeys, boolean continueOnFail, String clientIP) {
		if(!FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsServerObjectPullMulti",0,null, "Not a VCS Server to vcsServerObjectPullMulti", null);
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, projectId);
		W5Project po = FrameworkCache.getProject(projectId);
		
		Map result = new HashMap();
		result.put("success", true);
		List data = new ArrayList();
		String[] arTableKeys = tableKeys.split(",");
		for(String k:arTableKeys){
			String[] tableKey = k.replace('.', ',').split(",");
			int tableId=GenericUtil.uInt(tableKey[0]);
			W5Table t = FrameworkCache.getTable(projectId, tableId);
			if(t.getVcsFlag()==0){
				if(continueOnFail)continue;
				else throw new IWBException("vcs","vcsServerObjectPullMulti", t.getTableId(), "Not VCS Table", "Not VCS Table2", null);
			}
			int vcsCommitId = tableKey.length>2 ? GenericUtil.uInt(tableKey[2]) : 0;
		//	List<W5VcsCommit> lc = dao.find("from W5VcsCommit t where t.vcsCommitId>? order by t.vcsCommitId", vcsCommitId);

			
			Map m = new HashMap();
				
			int tablePk=GenericUtil.uInt(tableKey[1]);
			List l = dao.find("from W5VcsObject t where t.tableId=?0 AND t.tablePk=?1 AND t.customizationId=?2 AND t.projectUuid=?3", 
					tableId, tablePk, customizationId, projectId);
			if(GenericUtil.isEmpty(l))
				throw new IWBException("vcs","vcsServerObjectPull", t.getTableId(), "Not Found", "Not VCS Object2", null);
	
			W5VcsObject o = (W5VcsObject)l.get(0);
			if(false && o.getVcsCommitId()<=vcsCommitId){ //TODO
				if(continueOnFail)continue;
				else throw new IWBException("vcs","vcsServerObjectPull", t.getTableId(), "Not Found", "No Update for Specified Object2", null);
			}
			m.put("commit_id", o.getVcsCommitId());
			m.put("user_id", o.getVersionUserId());
			m.put("table_id", tableId);
			m.put("table_pk", tablePk);
			if(o.getVcsObjectStatusTip()==8)m.put("action", 3);
			else {
				StringBuilder s = new StringBuilder();
				s.append("select x.* from ").append(t.getDsc()).append(" x where x.").append(t.get_tableParamList().get(0).getExpressionDsc()).append("=?");
				s.append(DBUtil.includeTenantProjectPostSQL(scd, t, "x"));
				List p= new ArrayList();p.add(tablePk);
				List<Map> lm =dao.executeSQLQuery2Map(s.toString(), p);
				if(!GenericUtil.isEmpty(lm)) {
					Map mo =(Map)lm.get(0);
					m.put("object", mo);
				} else {
					//m.put("object", new HashMap());
					continue;
				}
				m.put("action", vcsCommitId==0 ? 2:1);
			}
		
		/*	List sqls = new ArrayList();
			for(W5VcsCommit vc:lc)if(!GenericUtil.isEmpty(vc.getExtraSql())){
				Map m2 = new HashMap();
				m2.put("commitId", vc.getVcsCommitId());
				m2.put("sql", vc.getExtraSql());
				sqls.add(m2);
			}
			if(!GenericUtil.isEmpty(sqls))
				m.put("sqls", sqls); */
			data.add(m);
			
		}
		result.put("data", data);
		dao.saveObject(new Log5VcsAction(scd, (short) 14,clientIP));

		return result;
	}
	

	public Map vcsServerObjectPull(String userName, String passWord, int customizationId, String projectId, int tableId, int tablePk, int vcsCommitId) {
		if(!FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsServerObjectPull",0,null, "Not a VCS Server to vcsServerObjectPull", null);
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, projectId);
		W5Project po = FrameworkCache.getProject(projectId);

		W5Table t = FrameworkCache.getTable(projectId, tableId);
		if(t.getVcsFlag()==0){
			throw new IWBException("vcs","vcsServerObjectPull", t.getTableId(), "Not VCS Table", "Not VCS Table2", null);
		}
		
		List<W5VcsCommit> lc = dao.find("from W5VcsCommit t where t.projectUuid=?0 AND t.vcsCommitId>?1 order by t.vcsCommitId", projectId, vcsCommitId);
		if(false && GenericUtil.isEmpty(lc)){ //TODO
			throw new IWBException("vcs","No Update for Specified Commit",vcsCommitId, "Not Found", "tralala", null);
		}
		
		Map m = new HashMap();
			
		List l = dao.find("from W5VcsObject t where t.tableId=?0 AND t.tablePk=?1 AND t.projectUuid=?2 AND t.customizationId=?3", 
				tableId, tablePk, projectId, customizationId);
		if(GenericUtil.isEmpty(l))
			throw new IWBException("vcs","vcsServerObjectPull", t.getTableId(), "Not Found", "Not VCS Object2", null);

		W5VcsObject o = (W5VcsObject)l.get(0);
		if(false && o.getVcsCommitId()<=vcsCommitId) //TODO
			throw new IWBException("vcs","vcsServerObjectPull", t.getTableId(), "Not Found", "No Update for Specified Object2", null);
		m.put("commit_id", o.getVcsCommitId());
		m.put("user_id", o.getVersionUserId());
		if(o.getVcsObjectStatusTip()==8)m.put("action", 3);
		else {
			StringBuilder s = new StringBuilder();
			s.append("select x.* from ").append(t.getDsc()).append(" x where x.").append(t.get_tableParamList().get(0).getExpressionDsc()).append("=?");
			s.append(DBUtil.includeTenantProjectPostSQL(scd, t, "x"));
			List p= new ArrayList();p.add(tablePk);
			Map mo =(Map)dao.executeSQLQuery2Map(s.toString(), p).get(0);
			
			m.put("object", mo);
			m.put("action", vcsCommitId==0 ? 2:1);
		}
	
		
		List sqls = new ArrayList();
		for(W5VcsCommit vc:lc)if(!GenericUtil.isEmpty(vc.getExtraSql())){
			Map m2 = new HashMap();
			m2.put("commitId", vc.getVcsCommitId());
			m2.put("sql", vc.getExtraSql());
			sqls.add(m2);
		}
		if(!GenericUtil.isEmpty(sqls))
			m.put("sqls", sqls);
		
		dao.saveObject(new Log5VcsAction(scd, (short) 14, null));

		return m;
	}


	public W5QueryResult vcsClientObjectsAll(Map<String, Object> scd) {
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		
		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&r="+po.getProjectUuid();
		
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSObjectsAll";
		String s = HttpUtil.send(url, urlParameters);
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			try {
				json = new JSONObject(s);
				if(json.get("success").toString().equals("true")){
					JSONObject srvTables =json.getJSONObject("list");
					List<W5VcsObject> lclObjects = null;
					lclObjects = dao.find("from W5VcsObject t where t.projectUuid=?0 AND t.customizationId=?1 order by t.tableId, t.tablePk", projectUuid, customizationId) ;
					Map<String, W5VcsObject> srcMap = new HashMap();
					for(W5VcsObject ox:lclObjects){
						srcMap.put(ox.getTableId()+"."+ox.getTablePk(), ox);
					}
					
					W5QueryResult qr = metadataLoader.getQueryResult(scd, 148);
					qr.setErrorMap(new HashMap());qr.setNewQueryFields(new ArrayList(qr.getQuery().get_queryFields().size()));qr.getNewQueryFields().addAll(qr.getQuery().get_queryFields());
					List<Object[]> data = new ArrayList();
					Iterator keyz = srvTables.keys();
					W5Table t = null;
					StringBuilder sql = null;
					List summaryParams = null;
					String ssql=null;
					while(keyz.hasNext()){
						int srvTableId = GenericUtil.uInt(keyz.next());
						if(t==null || t.getTableId()!=srvTableId){
							t = FrameworkCache.getTable(projectUuid, srvTableId);
							if(t==null)continue;
							sql = new StringBuilder();
							sql.append("select (").append(t.getSummaryRecordSql()).append(") qqq from ").append(t.getDsc()).append(" x where x.").append(t.get_tableParamList().get(0).getExpressionDsc()).append("=?");
							sql.append(DBUtil.includeTenantProjectPostSQL(scd, t, "x"));
							Object[] res = DBUtil.filterExt4SQL(sql.toString(), scd, new HashMap(), new HashMap());
							summaryParams = (List)res[1];summaryParams.add(0);
							ssql=((StringBuilder)res[0]).toString();
						}
						if(t.getVcsFlag()==0)continue;
						JSONArray srvObjects = srvTables.getJSONArray(srvTableId+"");
						for(int qi=0;qi<srvObjects.length();qi++){
							JSONObject o = srvObjects.getJSONObject(qi);
							
							int srvPk = GenericUtil.uInt(o.keys().next());
							int srvCommitId = o.getInt(srvPk+"");
							Object[] od = new Object[14];
							String pk = t.getTableId()+"."+srvPk;
							od[0]=pk;
							od[1]=t.getTableId();//tableId
							od[2]=t.getDsc();//table Desc
							od[3]=srvPk;//server vcsCommitId
							od[4]=srvCommitId;//server vcsCommitId
							W5VcsObject lclObj = srcMap.get(pk);
							if(lclObj!=null){//server'da ve localde var
								if(lclObj.getVcsObjectStatusTip()==0) {
									srcMap.remove(pk);
									continue; // ignored object
								}
								if(srvCommitId<0){ //server'da silinmis, localde hala var
									if(lclObj.getVcsObjectStatusTip()==8){ //localde de silinmis, atla
										srcMap.remove(pk);
										continue;
									}
									//od[1]=0;//server vcsCommitId (-,+)
									od[5]=lclObj.getVcsCommitId();//local vcsCommitId
									summaryParams.set(summaryParams.size()-1, srvPk);
									List ll=dao.executeSQLQuery2(ssql, summaryParams);
									if(GenericUtil.isEmpty(ll)){//boyle birsey olmamasi lazim normalde ama varsa, duzeltmek lazim
										lclObj.setVcsObjectStatusTip((short)8);
										dao.updateObject(lclObj);
										srcMap.remove(pk);
										continue;
									}
									od[6]=ll.get(0);//recordSummary
									od[7]=lclObj.getVcsObjectStatusTip()==1 ? 3:1;//edit edildiyse, conflict, aksi halde pull(delete)				
								} else if(lclObj.getVcsObjectStatusTip()==3){ //localde silinmis, server'da var
//									od[1]=srvCommitId;//server vcsCommitId (+,-)
									od[5]=-lclObj.getVcsCommitId();//local vcsCommitId
									od[6]=lclObj.getVcsCommitRecordHash();//recordSummary: cheat
									od[7]=lclObj.getVcsCommitId()==srvCommitId ? 2:3;//push:conflict
									
								} else if(lclObj.getVcsObjectStatusTip()==1){ //localde edit edilmis
									od[4]=-srvCommitId;
									od[5]=-lclObj.getVcsCommitId();//local vcsCommitId(-,-)
									summaryParams.set(summaryParams.size()-1, srvPk);
									List ll = dao.executeSQLQuery2(ssql, summaryParams);
									od[6]=GenericUtil.isEmpty(ll) ? "Record Not Found!!" : ll.get(0);//recordSummary
									od[7]=lclObj.getVcsCommitId()==srvCommitId ? 2:3;//push:conflict
								} else if(lclObj.getVcsObjectStatusTip()==9){ //localde synched, serverda edit edilmis
									if(srvCommitId==lclObj.getVcsCommitId()){
										srcMap.remove(pk);
										continue; //normalde olmasi lazim
									}
//									od[4]=srvCommitId;//karsi taraf degismis(+,+)
									od[5]=lclObj.getVcsCommitId();//local vcsCommitId: karsi tarfta yeniyse NULL
									summaryParams.set(summaryParams.size()-1, srvPk);
									List ll = dao.executeSQLQuery2(ssql, summaryParams);
									od[6]=GenericUtil.isEmpty(ll) ? "Record Not Found!!" : ll.get(0);//recordSummary
									od[7]=1;//pull				
								} else {
									od[6]="Error: Probably ID Conflicts";//recordSummary
									od[7]=3;//conflict	
								}
							} else { //server'da var, localde yok
								if(srvCommitId<0){ //localde hic yokmus, atla
									srcMap.remove(pk);
									continue;
								}
								od[5]=0;//local vcsCommitId: burda karsiligi yok, eklenmesi lazim (+,0)
								od[7]=1;//pull				
							}
							
							data.add(od);
							srcMap.remove(pk);
						}

					}
					if(!srcMap.isEmpty())for(String k:srcMap.keySet()) {
						W5VcsObject lclObj = srcMap.get(k);
						if(lclObj.getVcsObjectStatusTip()!=8 && lclObj.getVcsObjectStatusTip()!=0){ //localde yeni eklenmis, server'da yok TODO
							Object[] od = new Object[14];
							od[0]=k;
							String[] kx = k.replace('.', ',').split(",");
							int tableId = GenericUtil.uInt(kx[0]);
							od[1]=tableId;
							W5Table tx = FrameworkCache.getTable(projectUuid, tableId); 
							od[2]=tx!=null?tx.getDsc():"Table Not Found";
							od[3]=GenericUtil.uInt(kx[1]);
							od[4]=0;//server vcsCommitId (0,+)
							od[5]=lclObj.getVcsCommitId();//local vcsCommitId
							//summaryParams.set(summaryParams.size()-1, k);
							od[6]=tx!=null?dao.getTableRecordSummary(scd, tableId, (Integer)od[3], 0):"";
							od[7]=2;//push
							data.add(od);
							
						}
					}

					qr.setData(data);
					qr.setFetchRowCount(data.size());
					qr.setResultRowCount(data.size());
					
					
					return qr;
//					return qr;
				} else
					throw new IWBException("vcs","vcsClientObjectsAll:server Error Response", 0, s, json.has("error") ? json.getString("error"): json.toString(), null);
			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientObjectsAll:JSONException", 0, url, "Error", e);
				
			}
		}
		throw new IWBException("vcs","vcsClientObjectsAll", 0, url, "No Response from VCS Server2", null);
		
	}
	
	public W5QueryResult vcsClientObjectsAllTree(Map<String, Object> scd, String schema, int userId, String dtStart, String dtEnd) {
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		
		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&r="+po.getProjectUuid();
		if(!GenericUtil.isEmpty(schema))urlParameters+="&s="+schema;
		if(userId>0)urlParameters+="&_u="+userId;
		if(!GenericUtil.isEmpty(dtStart) && dtStart.length()==10)urlParameters+="&_ds="+dtStart;
		if(!GenericUtil.isEmpty(dtEnd) && dtEnd.length()==10)urlParameters+="&_de="+dtEnd;
		
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSObjectsAll";
		String s = HttpUtil.send(url, urlParameters);
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			try {
				json = new JSONObject(s);
				if(json.get("success").toString().equals("true")){
					JSONObject srvTables =json.getJSONObject("list");
					List<W5VcsObject> lclObjects = null;
					if(GenericUtil.isEmpty(schema)){
						lclObjects = dao.find("from W5VcsObject t where t.projectUuid=?0 AND t.customizationId=?1 order by t.tableId, t.tablePk", projectUuid, customizationId) ;
					} else {
						if(!schema.endsWith("%"))schema+="%";
						lclObjects = dao.find("from W5VcsObject t where t.projectUuid=?0 AND t.customizationId=?1 and exists(select 1 from W5Table q where q.projectUuid=t.projectUuid AND q.tableId=t.tableId AND q.dsc like ?) order by t.tableId, t.tablePk", projectUuid, customizationId,schema) ;
					}
					Map<String, W5VcsObject> srcMap = new HashMap();
					for(W5VcsObject ox:lclObjects){
						srcMap.put(ox.getTableId()+"."+ox.getTablePk(), ox);
					}
					
					W5QueryResult qr = metadataLoader.getQueryResult(scd, 148);
					qr.setErrorMap(new HashMap());qr.setNewQueryFields(new ArrayList(qr.getQuery().get_queryFields().size()));qr.getNewQueryFields().addAll(qr.getQuery().get_queryFields());
					List<Object[]> data = new ArrayList();
					Iterator keyz = srvTables.keys();
					W5Table t = null;
					StringBuilder sql = null;
					List summaryParams = null;
					String ssql=null;
					while(keyz.hasNext()){
						int srvTableId = GenericUtil.uInt(keyz.next());
						if(t==null || t.getTableId()!=srvTableId){
							t = FrameworkCache.getTable(projectUuid, srvTableId);
							if(t==null)continue;
							sql = new StringBuilder();
							sql.append("select (").append(t.getSummaryRecordSql()).append(") qqq from ").append(t.getDsc()).append(" x where x.").append(t.get_tableParamList().get(0).getExpressionDsc()).append("=?");
							sql.append(DBUtil.includeTenantProjectPostSQL(scd, t, "x"));
							Object[] res = DBUtil.filterExt4SQL(sql.toString(), scd, new HashMap(), new HashMap());
							summaryParams = (List)res[1];summaryParams.add(0);
							ssql=((StringBuilder)res[0]).toString();
						}
						if(t.getVcsFlag()==0)continue;
						JSONArray srvObjects = srvTables.getJSONArray(srvTableId+"");
						for(int qi=0;qi<srvObjects.length();qi++){
							JSONObject o = srvObjects.getJSONObject(qi);
							
							int srvPk = GenericUtil.uInt(o.keys().next());
							int srvCommitId = o.getInt(srvPk+"");
							Object[] od = new Object[14];
							String pk = t.getTableId()+"."+srvPk;
							od[0]=pk;
							od[1]=t.getTableId();//tableId
							od[2]=t.getDsc();//table Desc
							od[3]=srvPk;//server vcsCommitId
							od[4]=srvCommitId;//server vcsCommitId
							W5VcsObject lclObj = srcMap.get(pk);
							if(lclObj!=null){//server'da ve localde var
								if(lclObj.getVcsObjectStatusTip()==0) {
									srcMap.remove(pk);
									continue; // ignored object
								}
								if(srvCommitId<0){ //server'da silinmis, localde hala var
									if(lclObj.getVcsObjectStatusTip()==8){ //localde de silinmis, atla
										srcMap.remove(pk);
										continue;
									}
									//od[1]=0;//server vcsCommitId (-,+)
									od[5]=lclObj.getVcsCommitId();//local vcsCommitId
									summaryParams.set(summaryParams.size()-1, srvPk);
									List ll=dao.executeSQLQuery2(ssql, summaryParams);
									if(GenericUtil.isEmpty(ll)){//boyle birsey olmamasi lazim normalde ama varsa, duzeltmek lazim
										lclObj.setVcsObjectStatusTip((short)8);
										dao.updateObject(lclObj);
										srcMap.remove(pk);
										continue;
									}
									od[6]=ll.get(0);//recordSummary
									od[7]=lclObj.getVcsObjectStatusTip()==1 ? 3:1;//edit edildiyse, conflict, aksi halde pull(delete)				
								} else if(lclObj.getVcsObjectStatusTip()==3){ //localde silinmis, server'da var
//									od[1]=srvCommitId;//server vcsCommitId (+,-)
									od[5]=-lclObj.getVcsCommitId();//local vcsCommitId
									od[6]=lclObj.getVcsCommitRecordHash();//recordSummary: cheat
									od[7]=lclObj.getVcsCommitId()==srvCommitId ? 2:3;//push:conflict
									
								} else if(lclObj.getVcsObjectStatusTip()==1){ //localde edit edilmis
									od[4]=-srvCommitId;
									od[5]=-lclObj.getVcsCommitId();//local vcsCommitId(-,-)
									summaryParams.set(summaryParams.size()-1, srvPk);
									List ll = dao.executeSQLQuery2(ssql, summaryParams);
									od[6]=GenericUtil.isEmpty(ll) ? "Record Not Found!!" : ll.get(0);//recordSummary
									od[7]=lclObj.getVcsCommitId()==srvCommitId ? 2:3;//push:conflict
								} else if(lclObj.getVcsObjectStatusTip()==9){ //localde synched, serverda edit edilmis
									if(srvCommitId==lclObj.getVcsCommitId()){
										srcMap.remove(pk);
										continue; //normalde olmasi lazim
									}
//									od[4]=srvCommitId;//karsi taraf degismis(+,+)
									od[5]=lclObj.getVcsCommitId();//local vcsCommitId: karsi tarfta yeniyse NULL
									summaryParams.set(summaryParams.size()-1, srvPk);
									List ll = dao.executeSQLQuery2(ssql, summaryParams);
									od[6]=GenericUtil.isEmpty(ll) ? "Record Not Found!!" : ll.get(0);//recordSummary
									od[7]=1;//pull				
								} else {
									od[6]="Error: Probably ID Conflicts";//recordSummary
									od[7]=3;//conflict	
								}
							} else { //server'da var, localde yok
								if(srvCommitId<0){ //localde hic yokmus, atla
									srcMap.remove(pk);
									continue;
								}
								od[5]=0;//local vcsCommitId: burda karsiligi yok, eklenmesi lazim (+,0)
								od[7]=1;//pull				
							}
							
							data.add(od);
							srcMap.remove(pk);
						}

					}
					if(!srcMap.isEmpty())for(String k:srcMap.keySet()) {
						W5VcsObject lclObj = srcMap.get(k);
						if(lclObj.getVcsObjectStatusTip()!=8 && lclObj.getVcsObjectStatusTip()!=0 && lclObj.getVcsObjectStatusTip()!=3){ //localde yeni eklenmis, server'da yok TODO
							Object[] od = new Object[14];
							od[0]=k;
							String[] kx = k.replace('.', ',').split(",");
							int tableId = GenericUtil.uInt(kx[0]);
							od[1]=tableId;
							W5Table tx = FrameworkCache.getTable(projectUuid, tableId); 
							od[2]=tx!=null?tx.getDsc():"Table Not Found";
							od[3]=GenericUtil.uInt(kx[1]);
							od[4]=0;//server vcsCommitId (0,+)
							od[5]=lclObj.getVcsCommitId();//local vcsCommitId
							//summaryParams.set(summaryParams.size()-1, k);
							od[6]=tx!=null?dao.getTableRecordSummary(scd, tableId, (Integer)od[3], 0):"";
							od[7]=2;//push
							data.add(od);
							
						}
					}

					qr.setData(data);
					qr.setFetchRowCount(data.size());
					qr.setResultRowCount(data.size());
					
					
					return convertFromStraight2Tree(po, qr, userId, dtStart, dtEnd);
//					return qr;
				} else
					throw new IWBException("vcs","vcsClientObjectsAll:server Error Response", 0, s, json.has("error") ? json.getString("error"): json.toString(), null);
			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientObjectsAll:JSONException", 0, url, "Error", e);
				
			}
		}
		throw new IWBException("vcs","vcsClientObjectsAll", 0, url, "No Response from VCS Server2", null);
		
	}

	
	private W5QueryResult convertFromStraight2Tree(W5Project po, W5QueryResult qr, int userId, String dtStart, String dtEnd) {
		JSONArray ar = new JSONArray();
		Map<String, List> pm = new HashMap<String, List>();
		Map<String, Object[]> wpm = new HashMap<String, Object[]>();
		List<Object[]> data2 = null;
		if(userId>0 || (!GenericUtil.isEmpty(dtStart) && dtStart.length()==10) || (!GenericUtil.isEmpty(dtEnd) && dtEnd.length()==10)){
			data2 = new ArrayList();
		}
		for(Object[] od:qr.getData()){
			wpm.put(od[0].toString(), od);
			int tableId = (Integer)od[1];
			W5Table t = FrameworkCache.getTable(0, tableId);
			od[8]=od[0];
			od[10]=1;
			if(t!=null && t.getTableTip()!=0 && !GenericUtil.isEmpty(t.get_tableParentList())){ // detail ise
				List<W5TableRecordHelper> pr = dao.findRecordParentRecords(qr.getScd(), tableId, GenericUtil.uInt(od[3]), 2, false);
//				if(tableId==64){
//					tableId = 8*8;
//				}
				if(!GenericUtil.isEmpty(pr) && pr.size()>1){
					W5TableRecordHelper th = pr.get(1);
					String key = th.getTableId() + "." + th.getTablePk();
					od[9]=key;
					od[10]=1;
					List l = pm.get(key);
					if(l==null){
						pm.put(key, l = new ArrayList());
					}
					l.add(od);					
				} else {
					int masterTableId =t.get_tableParentList().get(0).getTableId(); 
					String key = masterTableId + ".-666";
					W5Table mt = FrameworkCache.getTable(po, masterTableId);
					od[9]=key;//"Missing Parent: " + (t==null ? ""+masterTableId:mt.getDsc()+" ("+masterTableId+")");
					od[10]=1;
					List l = pm.get(key);
					if(l==null){
						pm.put(key, l = new ArrayList());
					}
					l.add(od);		
				}
			} else{ 
				od[10]=1;
			}
			if((Integer)od[7]==1 || (Integer)od[7]==3){ //pull ve conflict ise, gerekli bilgiyi karsidan cek
				ar.put(tableId+"."+od[3]);
			}
		}
		
		if(ar.length()>0)try {
			JSONObject params = new JSONObject(); 
			params.put("u", po.getVcsUserName());
			params.put("p", po.getVcsPassword());params.put("c", po.getCustomizationId());params.put("r", po.getProjectUuid());
			params.put("objects", ar);
			if(userId>0)params.put("_u", userId);
			Date startDt = null, endDt = null;
			if(!GenericUtil.isEmpty(dtStart) && dtStart.length()==10)try{
				startDt = GenericUtil.uDate(dtStart);
				if(startDt!=null)params.put("_ds", dtStart);
			} catch(Exception ee){}
			if(!GenericUtil.isEmpty(dtEnd) && dtEnd.length()==10)try{
				endDt = GenericUtil.uDate(dtEnd);
				if(endDt!=null)params.put("_de", dtEnd);
			} catch(Exception ee){}
			String url=po.getVcsUrl();//"http://localhost:8080/q1/app/";//
			if(!url.endsWith("/"))url+="/";
			url+="serverVCSObjectsDetail";
			String s = HttpUtil.sendJson(url, params);
			if(!GenericUtil.isEmpty(s)){
				JSONObject json;
				try {
					json = new JSONObject(s);
					if(json.get("success").toString().equals("true")){
						JSONArray data = json.getJSONArray("data");
						for(int qi=0;qi<data.length();qi++){
							JSONObject jo = data.getJSONObject(qi);
							String id = jo.getString("id");
//							if(id.substring(0,3).equals("64.")){
//								id = id.substring(0);
//							}
							Object[] od = wpm.get(id);
							if(od[9]==null && jo.has("parent")){ // kendisnin parent'i yoksa
								String parent = jo.getString("parent");
								od[9] = parent;
								List l = pm.get(parent);
								if(l==null){
									pm.put(parent, l = new ArrayList());
								}
								l.add(od);	
							}
							if(jo.has("dsc")){ // description
								String dsc = jo.getString("dsc");
								if((Integer)od[7]==3){//conflict
									if(!GenericUtil.safeEquals(od[6], dsc))
										od[6] = od[6] + " (Remote: " + dsc +")";
								} else od[6]=dsc;
							}
							boolean bx = false;
							if(jo.has("user_id")){ // userId
								od[11] = jo.getInt("user_id");
								if(jo.has("user_dsc"))od[12] = jo.getString("user_dsc");
								if(userId!=0 && userId==(Integer)od[11]){
									bx = true;
								}
							}
							if(jo.has("commit_dttm")){ // commitDttm
								od[13] = jo.getString("commit_dttm");
								if(startDt!=null || endDt!=null)try{
									Date dt = GenericUtil.uDate(jo.getString("commit_dttm"));
									if(startDt!=null && startDt.before(dt))bx = false;
									if(endDt!=null && startDt.after(dt))bx = false;
								} catch(Exception ee){}
							}
							if(bx)data2.add(od);
						}
					} else
						throw new IWBException("vcs","convertFromStraight2Tree:server Error Response", 0, s, json.has("error") ? json.getString("error"): json.toString(), null);
				} catch (JSONException e){
					throw new IWBException("vcs","convertFromStraight2Tree:JSONException", 0, s, "Error", e);
				}

			}
		} catch (Exception e) {
			if(FrameworkSetting.debug)e.printStackTrace();
		}
		
		for(Object[] od:qr.getData()){
			int tableId = (Integer)od[1];
//			W5Table t = FrameworkCache.getTable(0, tableId);
			if(pm.containsKey(od[0].toString())){ // master ve altinda eleman var ise
				od[10]=0;
				od[2]="("+pm.get(od[0].toString()).size()+") " +  od[2];
				pm.remove(od[0].toString());
			}
		}
		
		if(data2!=null)qr.setData(data2);
		for(String key:pm.keySet()){
			String[] k = key.replace('.', ',').split(",");
			int tableId = GenericUtil.uInt(k[0]);
			W5Table t = FrameworkCache.getTable(0, tableId);
//			if(t==null){ continue; }
			int tablePk = GenericUtil.uInt(k[1]);
			if(t== null || tablePk == -666){
				Object[] od = new Object[14]; 
				od[0]=key;
				od[8]=key;
				od[1]=tableId;//table Desc
				od[2]="("+pm.get(key).size()+") " +  (t==null ? "(table not found: "+tableId+")":t.getDsc());//table Desc
				od[6]="Missing Parent";//server vcsCommitId
				od[7]=0;//server vcsCommitId
				od[10]=0;
				qr.getData().add(od);
			} else {
				List<W5TableRecordHelper> pr = dao.findRecordParentRecords(qr.getScd(), tableId, tablePk, 1, false);
				if(!GenericUtil.isEmpty(pr)){
					Object[] od = new Object[14]; 
					od[0]=key;
					od[8]=key;
					od[1]=t.getTableId();//table Desc
					od[2]="("+pm.get(key).size()+") " +  t.getDsc();//table Desc
					od[6]=pr.get(0).getRecordDsc();//server vcsCommitId
					od[7]=0;//server vcsCommitId
					od[10]=0;
					qr.getData().add(od);
				} else {
					Object[] od = new Object[14]; 
					od[0]=key;
					od[1]=t.getTableId();//table Desc
					od[8]=key;
					od[2]="("+pm.get(key).size()+") " +  t.getDsc();//table Desc
					od[6]="(parent not found)";//server vcsCommitId
					od[7]=0;//server vcsCommitId
					od[10]=0;
					qr.getData().add(od);
				}
			}
		}

		return qr;
	}
	
	public boolean vcsClientMove2AnotherProject(Map<String, Object> scd, String newProjectUiid, int tableId, int tablePk) throws JSONException {
		if(FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsClientMove2AnotherProject",0,null, "VCS Server not allowed to vcsClientMove2AnotherProject", null);
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		if(projectUuid.equals(newProjectUiid))return false;
		W5Project po = FrameworkCache.getProject(projectUuid);
		W5Project npo = FrameworkCache.getProject(newProjectUiid);

		W5Table mt = FrameworkCache.getTable(projectUuid, tableId);
		
		
		StringBuilder sql = new StringBuilder();List<Object> params = new ArrayList();

		//master check
		sql.append("select v.vcs_object_status_tip from iwb.w5_vcs_object v where v.customization_id=? AND v.table_id=? AND v.table_pk=? AND exists(select 1 from ").append(mt.getDsc())
		.append(" m where m.project_uuid=? AND m.").append(mt.get_tableParamList().get(0).getExpressionDsc()).append("=v.table_pk");
		params.add(customizationId);params.add(tableId);params.add(tablePk);params.add(projectUuid);
		if(mt.get_tableParamList().size()>1){
			sql.append(" AND m.customization_id=?");
			params.add(customizationId);
		}
		sql.append(")");
		List lr = dao.executeSQLQuery2(sql.toString(), params);
		if(GenericUtil.isEmpty(lr))return false;
		boolean masterInsert = GenericUtil.uInt(lr.get(0))==2;
	
	
		if(masterInsert){
			sql.setLength(0);params.clear();
			sql.append("update ").append(mt.getDsc()).append(" m set project_uuid=? where exists(select 1 from iwb.w5_vcs_object v where v.vcs_object_status_tip=2 AND v.customization_id=? AND v.table_id=? AND v.table_pk=m.")
			.append(mt.get_tableParamList().get(0).getExpressionDsc()).append(") AND m.").append(mt.get_tableParamList().get(0).getExpressionDsc()).append("=?");
			params.add(newProjectUiid);params.add(customizationId);params.add(tableId);params.add(tablePk);
			if(mt.get_tableParamList().size()>1){
				sql.append(" AND m.customization_id=?");
				params.add(customizationId);
			}
			dao.executeUpdateSQLQuery(sql.toString(), params);
			
			sql.setLength(0);params.clear();
			sql.append("update iwb.w5_vcs_object v set project_uuid=? where v.customization_id=? AND v.table_id=? AND v.table_pk=? AND v.vcs_object_status_tip=2 AND exists(select 1 from ").append(mt.getDsc())
				.append(" m where m.").append(mt.get_tableParamList().get(0).getExpressionDsc()).append("=v.table_pk");
			params.add(newProjectUiid);params.add(customizationId);params.add(tableId);params.add(tablePk);
			if(mt.get_tableParamList().size()>1){
				sql.append(" AND m.customization_id=?");
				params.add(customizationId);
			}
			sql.append(")");
			dao.executeUpdateSQLQuery(sql.toString(), params);
		}
		
		if(!GenericUtil.isEmpty(mt.get_tableChildList()))for(W5TableChild tc:mt.get_tableChildList()){
			W5Table dt = FrameworkCache.getTable(projectUuid, tc.getRelatedTableId());
			if(dt.getTableTip()==0)continue;
			sql.setLength(0);params.clear();
			
			sql.append("update ").append(dt.getDsc()).append(" d set project_uuid=? where exists(select 1 from iwb.w5_vcs_object v where v.vcs_object_status_tip=2 AND v.customization_id=? AND v.table_id=? AND v.table_pk=d.")
				.append(dt.get_tableParamList().get(0).getExpressionDsc()).append(") AND exists(select 1 from ").append(mt.getDsc())
				.append(" m where m.").append(mt.get_tableFieldMap().get(tc.getTableFieldId()).getDsc()).append("=d.").append(dt.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc())
				.append(" AND m.").append(mt.get_tableParamList().get(0).getExpressionDsc()).append("=?");
			params.add(newProjectUiid);params.add(customizationId);params.add(tc.getRelatedTableId());params.add(tablePk);
			if(mt.get_tableParamList().size()>1){
				sql.append(" AND m.customization_id=?");
				params.add(customizationId);
			}
			sql.append(")");
			if(tc.getRelatedStaticTableFieldId()!=0){
				sql.append(" AND d.").append(dt.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc()).append("=").append(tc.getRelatedStaticTableFieldVal());
				
			}
			if(dt.get_tableParamList().size()>1){
				sql.append(" AND d.customization_id=?");
				params.add(customizationId);
			}
			
//			List rl = dao.executeSQLQuery2("select "+tc.getRelatedTableId()+" tid, "+dt.get_tableParamList().get(0).getExpressionDsc()+" tpk from "+dt.getDsc()+" d " + sql.toString(), params);
			dao.executeUpdateSQLQuery(sql.toString(), params);

			sql.setLength(0);params.clear();
			sql.append("update iwb.w5_vcs_object v set project_uuid=? where v.customization_id=? AND v.table_id=? AND v.vcs_object_status_tip=2 AND exists(select 1 from ").append(dt.getDsc())
				.append(" d, ").append(mt.getDsc()).append(" m where d.").append(dt.get_tableParamList().get(0).getExpressionDsc()).append("=v.table_pk AND m.").append(mt.get_tableFieldMap().get(tc.getTableFieldId()).getDsc()).append("=d.").append(dt.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc())
				.append(" AND m.").append(mt.get_tableParamList().get(0).getExpressionDsc()).append("=?");
			if(tc.getRelatedStaticTableFieldId()!=0){
				sql.append(" AND d.").append(dt.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc()).append("=").append(tc.getRelatedStaticTableFieldVal());
			}
			params.add(newProjectUiid);params.add(customizationId);params.add(tc.getRelatedTableId());params.add(tablePk);
			if(dt.get_tableParamList().size()>1){
				sql.append(" AND d.customization_id=?");
				params.add(customizationId);
			}
			if(mt.get_tableParamList().size()>1){
				sql.append(" AND m.customization_id=?");
				params.add(customizationId);
			}
			sql.append(")");
			dao.executeUpdateSQLQuery(sql.toString(), params);
		}

		if(!masterInsert){
			sql.setLength(0);params.clear();
			sql.append("update ").append(mt.getDsc()).append(" m set project_uuid=? where m.project_uuid=? AND exists(select 1 from iwb.w5_vcs_object v where v.vcs_object_status_tip in (1,9) AND v.customization_id=? AND v.table_id=? AND v.table_pk=m.")
			.append(mt.get_tableParamList().get(0).getExpressionDsc()).append(") AND m.").append(mt.get_tableParamList().get(0).getExpressionDsc()).append("=?");
			params.add(newProjectUiid);params.add(projectUuid);params.add(customizationId);params.add(tableId);params.add(tablePk);
			if(mt.get_tableParamList().size()>1){
				sql.append(" AND m.customization_id=?");
				params.add(customizationId);
			}
	//		dao.executeUpdateSQLQuery(sql.toString(), params);
			sql.append(";\nupdate iwb.w5_vcs_object v set project_uuid=? where v.customization_id=? AND v.table_id=? AND v.table_pk=? AND v.vcs_object_status_tip in (1,9) AND exists(select 1 from ").append(mt.getDsc())
				.append(" m where m.").append(mt.get_tableParamList().get(0).getExpressionDsc()).append("=v.table_pk");
			params.add(newProjectUiid);params.add(customizationId);params.add(tableId);params.add(tablePk);
			if(mt.get_tableParamList().size()>1){
				sql.append(" AND m.customization_id=?");
				params.add(customizationId);
			}
			sql.append(");");
			
			if(!GenericUtil.isEmpty(mt.get_tableChildList()))for(W5TableChild tc:mt.get_tableChildList()){
				W5Table dt = FrameworkCache.getTable(projectUuid, tc.getRelatedTableId());
				if(dt.getTableTip()==0)continue;
				
				sql.append("\n update ").append(dt.getDsc()).append(" d set project_uuid=? where exists(select 1 from iwb.w5_vcs_object v where v.vcs_object_status_tip in (1,9) AND v.customization_id=? AND v.table_id=? AND v.table_pk=d.")
					.append(dt.get_tableParamList().get(0).getExpressionDsc()).append(") AND exists(select 1 from ").append(mt.getDsc())
					.append(" m where m.").append(mt.get_tableFieldMap().get(tc.getTableFieldId()).getDsc()).append("=d.").append(dt.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc())
					.append(" AND m.").append(mt.get_tableParamList().get(0).getExpressionDsc()).append("=?");
				params.add(newProjectUiid);params.add(customizationId);params.add(tc.getRelatedTableId());params.add(tablePk);
				if(mt.get_tableParamList().size()>1){
					sql.append(" AND m.customization_id=?");
					params.add(customizationId);
				}
				sql.append(")");
				if(tc.getRelatedStaticTableFieldId()!=0){
					sql.append(" AND d.").append(dt.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc()).append("=").append(tc.getRelatedStaticTableFieldVal());
					
				}
				if(dt.get_tableParamList().size()>1){
					sql.append(" AND d.customization_id=?");
					params.add(customizationId);
				}
				
		//		List rl = dao.executeSQLQuery2("select "+tc.getRelatedTableId()+" tid, "+dt.get_tableParamList().get(0).getExpressionDsc()+" tpk from "+dt.getDsc()+" d " + sql.toString(), params);
				sql.append(";\n update iwb.w5_vcs_object v set project_uuid=? where v.customization_id=? AND v.table_id=? AND v.vcs_object_status_tip in (1,9) AND exists(select 1 from ").append(dt.getDsc())
					.append(" d, ").append(mt.getDsc()).append(" m where d.").append(dt.get_tableParamList().get(0).getExpressionDsc()).append("=v.table_pk AND m.").append(mt.get_tableFieldMap().get(tc.getTableFieldId()).getDsc()).append("=d.").append(dt.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc())
					.append(" AND m.").append(mt.get_tableParamList().get(0).getExpressionDsc()).append("=?");
				if(tc.getRelatedStaticTableFieldId()!=0){
					sql.append(" AND d.").append(dt.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc()).append("=").append(tc.getRelatedStaticTableFieldVal());
				}
				params.add(newProjectUiid);params.add(customizationId);params.add(tc.getRelatedTableId());params.add(tablePk);
				if(dt.get_tableParamList().size()>1){
					sql.append(" AND d.customization_id=?");
					params.add(customizationId);
				}
				if(mt.get_tableParamList().size()>1){
					sql.append(" AND m.customization_id=?");
					params.add(customizationId);
				}
				sql.append(");");
			}
			
			dao.executeUpdateSQLQuery(sql.toString(), params);
			vcsClientPushSqlRT(scd, GenericUtil.replaceSql(sql.toString(), params), mt.getDsc() + " move to another project (" + newProjectUiid);
		}

		
		return true;
	}
	
	public W5QueryResult vcsClientXRay(Map<String, Object> scd) {
//		if(FrameworkSetting.vcsServer)throw new PromisException("vcs","vcsClientReorg",0,null, "VCS Server not allowed to vcsClientReorg", null);
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);

		W5QueryResult qr = metadataLoader.getQueryResult(scd, 161);
		qr.setErrorMap(new HashMap());qr.setNewQueryFields(new ArrayList(qr.getQuery().get_queryFields().size()));qr.getNewQueryFields().addAll(qr.getQuery().get_queryFields());
		List<Object[]> data = new ArrayList();qr.setData(data);
		int id = 0;
		List<Integer> ps = dao.executeSQLQuery("select q.table_id from iwb.w5_table q where ((q.customization_id=? AND q.project_uuid=?) OR (q.customization_id=0 AND q.project_uuid = '067e6162-3b6f-4ae2-a221-2470b63dff00')) AND q.vcs_flag=1 order by q.table_id", customizationId, projectUuid); //sadece master tablolar
		if(ps!=null)for(Integer tid:ps){
			W5Table mt = FrameworkCache.getTable(projectUuid, tid);
			if(mt!=null && !GenericUtil.isEmpty(mt.get_tableParamList())){
				List params = new ArrayList();
				StringBuilder sql = new StringBuilder();
				sql.append("select count(1) from iwb.w5_vcs_object v where v.project_uuid=? AND v.customization_id=? AND v.table_id=? AND v.vcs_object_status_tip in (1,2,9) AND not exists(select 1 from  ").append(mt.getDsc()).append(" m where m.project_uuid=? AND v.table_pk=m.")//m.customization_id=? AND 
					.append(mt.get_tableParamList().get(0).getExpressionDsc()).append(")");
				params.add(projectUuid);params.add(customizationId);params.add(tid);params.add(projectUuid);//params.add(customizationId);
				int cnt = GenericUtil.uInt(dao.executeSQLQuery2(sql.toString(), params).get(0));
				
				if(cnt>0){
					Object[] o= new Object[5];
					o[0] = ++id;
					o[1] = tid;
					o[2] = mt.getDsc();
					o[3] = cnt;
					o[4] = 1;
					data.add(o);
				}
			}
			
		}
		
		if(ps!=null)for(Integer tid:ps){
			W5Table mt = FrameworkCache.getTable(projectUuid, tid);
			if(mt==null || GenericUtil.isEmpty(mt.get_tableParamList()))continue;
			List params = new ArrayList();
			StringBuilder sql = new StringBuilder();
			sql.append("select count(1) from ").append(mt.getDsc())
				.append(" m where m.project_uuid=? AND exists(select 1 from iwb.w5_vcs_object v where v.project_uuid!=? AND v.vcs_object_status_tip=2 AND v.customization_id=? AND v.table_id=? AND v.table_pk=m.")
				.append(mt.get_tableParamList().get(0).getExpressionDsc()).append(")");
			params.add(projectUuid);params.add(projectUuid);params.add(customizationId);params.add(tid);
		
			int cnt = GenericUtil.uInt(dao.executeSQLQuery2(sql.toString(), params).get(0));
			
			if(cnt>0){
				Object[] o= new Object[5];
				o[0] = ++id;
				o[1] = tid;
				o[2] = mt.getDsc();
				o[3] = cnt;
				o[4] = 2;
				data.add(o);
			}
			
		}
		
		if(ps!=null)for(Integer tid:ps){
			W5Table mt = FrameworkCache.getTable(projectUuid, tid);
			if(mt==null || GenericUtil.isEmpty(mt.get_tableParamList()))continue;
			List params = new ArrayList();
			StringBuilder sql = new StringBuilder();
			sql.append("select count(1) from ").append(mt.getDsc())
				.append(" m where m.project_uuid=? AND exists(select 1 from iwb.w5_vcs_object v where v.project_uuid!=? AND v.vcs_object_status_tip in (1,9) AND v.customization_id=? AND v.table_id=? AND v.table_pk=m.")
				.append(mt.get_tableParamList().get(0).getExpressionDsc()).append(")");
			params.add(projectUuid);params.add(projectUuid);params.add(customizationId);params.add(tid);
		
			int cnt = GenericUtil.uInt(dao.executeSQLQuery2(sql.toString(), params).get(0));
			
			if(cnt>0){
				Object[] o= new Object[5];
				o[0] = ++id;
				o[1] = tid;
				o[2] = mt.getDsc();
				o[3] = cnt;
				o[4] = 3;
				data.add(o);
			}
			
		}
		
		if(ps!=null)for(Integer tid:ps){
			W5Table mt = FrameworkCache.getTable(projectUuid, tid);
			if(mt.getTableTip()!=0)continue;
			if(GenericUtil.isEmpty(mt.get_tableParamList()) || GenericUtil.isEmpty(mt.get_tableFieldList()))continue;
			if(!GenericUtil.isEmpty(mt.get_tableChildList()))for(W5TableChild tc:mt.get_tableChildList()){
				W5Table dt = FrameworkCache.getTable(projectUuid, tc.getRelatedTableId());
				if(dt==null || dt.getTableTip()==0 || dt.getVcsFlag()==0)continue;
				if(mt.get_tableFieldMap().get(tc.getTableFieldId())==null || dt.get_tableFieldMap().get(tc.getRelatedTableFieldId())==null 
						|| (tc.getRelatedStaticTableFieldId()!=0 && dt.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId())==null))continue;
				List params = new ArrayList();
				StringBuilder sql = new StringBuilder();
				sql.append("where d.project_uuid!=? AND exists(select 1 from iwb.w5_vcs_object v where v.vcs_object_status_tip=2 AND v.customization_id=? AND v.table_id=? AND v.table_pk=d.")
					.append(dt.get_tableParamList().get(0).getExpressionDsc()).append(") AND exists(select 1 from ").append(mt.getDsc())
					.append(" m where m.project_uuid=? AND m.").append(mt.get_tableFieldMap().get(tc.getTableFieldId()).getDsc()).append("=d.").append(dt.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc());
				params.add(projectUuid);params.add(customizationId);params.add(tc.getRelatedTableId());params.add(projectUuid);
				if(mt.get_tableParamList().size()>1 && mt.get_tableParamList().get(1).getDsc().startsWith("custo")){
					sql.append(" AND m.customization_id=?");
					params.add(customizationId);
				}
				sql.append(")");
				if(tc.getRelatedStaticTableFieldId()!=0){
					sql.append(" AND d.").append(dt.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc()).append("=").append(tc.getRelatedStaticTableFieldVal());
					
				}
				if(dt.get_tableParamList().size()>1 && dt.get_tableParamList().get(1).getDsc().startsWith("custo")){
					sql.append(" AND d.customization_id=?");
					params.add(customizationId);
				}
			
				int cnt = GenericUtil.uInt(dao.executeSQLQuery2("select count(1) from "+dt.getDsc()+" d " + sql.toString(), params).get(0));
				
				if(cnt>0){
					Object[] o= new Object[5];
					o[0] = ++id;
					o[1] = tid;
					o[2] = mt.getDsc() + " -> " + dt.getDsc();
					o[3] = cnt;
					o[4] = 4;
					data.add(o);
				}
			}
			
		}

		if(ps!=null)for(Integer tid:ps){
			W5Table mt = FrameworkCache.getTable(projectUuid, tid);
			if(mt.getTableTip()!=0)continue;
			if(!GenericUtil.isEmpty(mt.get_tableChildList()))for(W5TableChild tc:mt.get_tableChildList()){
				W5Table dt = FrameworkCache.getTable(projectUuid, tc.getRelatedTableId());
				if(dt==null || dt.getTableTip()==0 || dt.getVcsFlag()==0)continue;
				if(mt.get_tableFieldMap().get(tc.getTableFieldId())==null || dt.get_tableFieldMap().get(tc.getRelatedTableFieldId())==null 
						|| (tc.getRelatedStaticTableFieldId()!=0 && dt.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId())==null))continue;

				List params = new ArrayList();
				StringBuilder sql = new StringBuilder();
				sql.append("where d.project_uuid!=? AND exists(select 1 from iwb.w5_vcs_object v where v.vcs_object_status_tip in (1,9) AND v.customization_id=? AND v.table_id=? AND v.table_pk=d.")
					.append(dt.get_tableParamList().get(0).getExpressionDsc()).append(") AND exists(select 1 from ").append(mt.getDsc())
					.append(" m where m.project_uuid=? AND m.").append(mt.get_tableFieldMap().get(tc.getTableFieldId()).getDsc()).append("=d.").append(dt.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc());
				params.add(projectUuid);params.add(customizationId);params.add(tc.getRelatedTableId());params.add(projectUuid);
				if(mt.get_tableParamList().size()>1 && mt.get_tableParamList().get(1).getDsc().startsWith("custo")){
					sql.append(" AND m.customization_id=?");
					params.add(customizationId);
				}
				sql.append(")");
				if(tc.getRelatedStaticTableFieldId()!=0){
					sql.append(" AND d.").append(dt.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc()).append("=").append(tc.getRelatedStaticTableFieldVal());
					
				}
				if(dt.get_tableParamList().size()>1 && dt.get_tableParamList().get(1).getDsc().startsWith("custo")){
					sql.append(" AND d.customization_id=?");
					params.add(customizationId);
				}
			
				int cnt = GenericUtil.uInt(dao.executeSQLQuery2("select count(1) from "+dt.getDsc()+" d " + sql.toString(), params).get(0));
				
				if(cnt>0){
					Object[] o= new Object[5];
					o[0] = ++id;
					o[1] = tid;
					o[2] = mt.getDsc() + " -> " + dt.getDsc();
					o[3] = cnt;
					o[4] = 5;
					data.add(o);
				}
			}
			
		}

		return qr;
	}

	public W5QueryResult vcsClientObjectsList(Map<String, Object> scd, int tableId, int tableMasterId, int tableMasterPk) {
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		W5Table t = FrameworkCache.getTable(projectUuid, tableId);
		if(t.getVcsFlag()==0){
			throw new IWBException("vcs","vcsClientObjectsList", t.getTableId(), t.getDsc(), "Not VCS Table2", null);
		}
		
		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&t="+tableId+"&r="+po.getProjectUuid();
		if(tableMasterId>0)urlParameters+="&m="+tableMasterId+"&k="+tableMasterPk;
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		JSONArray ar = new JSONArray();
		url+="serverVCSObjectsList";
		String s = HttpUtil.send(url, urlParameters);
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			try {
				json = new JSONObject(s);
				if(json.get("success").toString().equals("true")){
					JSONArray srvObjects =json.getJSONArray("list");
					List<W5VcsObject> lclObjects = dao.find("from W5VcsObject t where t.projectUuid=?0 AND t.customizationId=?1 AND t.tableId=?2 order by t.tablePk", projectUuid, customizationId, tableId) ;
					Map<Integer, W5VcsObject> srcMap = new HashMap();
					for(W5VcsObject ox:lclObjects){
						srcMap.put(ox.getTablePk(), ox);
					}
					StringBuilder sql = new StringBuilder();
					sql.append("select (").append(t.getSummaryRecordSql()).append(") qqq from ").append(t.getDsc()).append(" x where x.").append(t.get_tableParamList().get(0).getExpressionDsc()).append("=?");
					sql.append(DBUtil.includeTenantProjectPostSQL(scd, t, "x"));
					Object[] res = DBUtil.filterExt4SQL(sql.toString(), scd, new HashMap(), new HashMap());
					List summaryParams = (List)res[1];summaryParams.add(0);
					String ssql=((StringBuilder)res[0]).toString();
					
					W5QueryResult qr = metadataLoader.getQueryResult(scd, 2766);
					qr.setErrorMap(new HashMap());qr.setNewQueryFields(new ArrayList(qr.getQuery().get_queryFields().size()));qr.getNewQueryFields().addAll(qr.getQuery().get_queryFields());
					List<Object[]> data = new ArrayList();
					for(int qi=0;qi<srvObjects.length();qi++){
						JSONObject o = srvObjects.getJSONObject(qi);
						int srvPk = GenericUtil.uInt(o.keys().next());
						int srvCommitId = GenericUtil.uInt(o.get(srvPk+""));
						Object[] od = new Object[8];
						od[0]=srvPk;
						od[1]=srvCommitId;//server vcsCommitId
						W5VcsObject lclObj = srcMap.get(srvPk);
						if(lclObj!=null){//server'da ve localde var
							if(lclObj.getVcsObjectStatusTip()==0) {
								srcMap.remove(srvPk);
								continue; // ignored object
							}
							if(srvCommitId<0){ //server'da silinmis, localde hala var
								if(lclObj.getVcsObjectStatusTip()==8){ //localde de silinmis, atla
									srcMap.remove(srvPk);
									continue;
								}
								//od[1]=0;//server vcsCommitId (-,+)
								od[2]=lclObj.getVcsCommitId();//local vcsCommitId
								summaryParams.set(summaryParams.size()-1, srvPk);
								List ll=dao.executeSQLQuery2(ssql, summaryParams);
								if(GenericUtil.isEmpty(ll)){//boyle birsey olmamasi lazim normalde ama varsa, duzeltmek lazim
									lclObj.setVcsObjectStatusTip((short)8);
									dao.updateObject(lclObj);
									srcMap.remove(srvPk);
									continue;
								}
								od[3]=ll.get(0);//recordSummary
								od[4]=lclObj.getVcsObjectStatusTip()==1 ? 3:1;//edit edildiyse, conflict, aksi halde pull(delete)				
							} else if(lclObj.getVcsObjectStatusTip()==3){ //localde silinmis, server'da var
//								od[1]=srvCommitId;//server vcsCommitId (+,-)
								od[2]=-lclObj.getVcsCommitId();//local vcsCommitId
								od[3]=lclObj.getVcsCommitRecordHash();//recordSummary: cheat
								od[4]=lclObj.getVcsCommitId()==srvCommitId ? 2:3;//push:conflict
								
							} else if(lclObj.getVcsObjectStatusTip()==1){ //localde edit edilmis
								od[1]=-srvCommitId;
								od[2]=-lclObj.getVcsCommitId();//local vcsCommitId(-,-)
								summaryParams.set(summaryParams.size()-1, srvPk);
								od[3]=dao.executeSQLQuery2(ssql, summaryParams).get(0);//recordSummary
								od[4]=lclObj.getVcsCommitId()==srvCommitId ? 2:3;//push:conflict
							} else if(lclObj.getVcsObjectStatusTip()==9){ //localde synched, serverda edit edilmis
								if(srvCommitId==lclObj.getVcsCommitId()){
									srcMap.remove(srvPk);
									continue; //normalde olmasi lazim
								}
//								od[1]=srvCommitId;//karsi taraf degismis(+,+)
								od[2]=lclObj.getVcsCommitId();//local vcsCommitId: karsi tarfta yeniyse NULL
								summaryParams.set(summaryParams.size()-1, srvPk);
								od[3]=dao.executeSQLQuery2(ssql, summaryParams).get(0);//recordSummary
								od[4]=1;//pull				
							} else {
								od[3]="Error: Probably ID Conflicts";//recordSummary
								od[4]=3;//conflict	
							}
						} else { //server'da var, localde yok
							if(srvCommitId<0){ //localde hic yokmus, atla
								srcMap.remove(srvPk);
								continue;
							}
							od[2]=0;//local vcsCommitId: burda karsiligi yok, eklenmesi lazim (+,0)
							od[4]=1;//pull				
						}
						data.add(od);
						srcMap.remove(srvPk);
					}
					if(!srcMap.isEmpty())for(Integer k:srcMap.keySet()){
						W5VcsObject lclObj = srcMap.get(k);
						if(lclObj.getVcsObjectStatusTip()!=8 && lclObj.getVcsObjectStatusTip()!=0){ //localde yeni eklenmis, server'da yok //localde yeni eklenmis, server'da yok
							Object[] od = new Object[8];
							od[0]=k;
							od[1]=0;//server vcsCommitId (0,+)
							od[2]=lclObj.getVcsCommitId();//local vcsCommitId
							summaryParams.set(summaryParams.size()-1, k);
							List tl = dao.executeSQLQuery2(ssql, summaryParams);
							if(!GenericUtil.isEmpty(tl)){
								od[3]=tl.get(0);//recordSummary
								od[4]=2;//push
								data.add(od);
							}
						}						
					}
					
					Map<String, Object[]> wpm = new HashMap<String, Object[]>();
					for(Object[] od:data)if((Integer)od[4]==1 || (Integer)od[4]==3){ //pull || conflict
						ar.put(tableId+"."+od[0]);
						wpm.put(tableId+"."+od[0], od);

					}

					if(ar.length()>0)try {
						JSONObject params = new JSONObject(); 
						params.put("u", po.getVcsUserName());
						params.put("p", po.getVcsPassword());params.put("c", po.getCustomizationId());params.put("r", po.getProjectUuid());
						params.put("objects", ar);
						String url2=po.getVcsUrl();//"http://localhost:8080/q1/app/";//
						if(!url2.endsWith("/"))url2+="/";
						url2+="serverVCSObjectsDetail";
						String s2 = HttpUtil.sendJson(url2, params);
						if(!GenericUtil.isEmpty(s2)){
							JSONObject json2;
							try {
								json2 = new JSONObject(s2);
								if(json2.get("success").toString().equals("true")){
									JSONArray data2 = json2.getJSONArray("data");
									for(int qi=0;qi<data2.length();qi++){
										JSONObject jo = data2.getJSONObject(qi);
										String id = jo.get("id").toString();
//										if(id.substring(0,3).equals("64.")){
//											id = id.substring(0);
//										}
										Object[] od = wpm.get(id);
										
										if(jo.has("dsc")){ // description
											od[3] = jo.getString("dsc");
										}
										if(jo.has("user_id")){ // userId
											od[5] = jo.getInt("user_id");
											if(jo.has("user_dsc"))od[6] = jo.getString("user_dsc");
										}
										if(jo.has("commit_dttm")){ // commitDttm
											od[7] = jo.getString("commit_dttm");
										}
									}
								} else
									throw new IWBException("vcs","vcsClientObjectsList:server Error Response", 0, s, json.has("error") ? json.getString("error"): json.toString(), null);
							} catch (JSONException e){
								throw new IWBException("vcs","vcsClientObjectsList:JSONException", 0, s, "Error", e);
							}

						}
					} catch (JSONException e) {
						if(FrameworkSetting.debug)e.printStackTrace();
					}

					qr.setData(data);
					qr.setFetchRowCount(data.size());
					qr.setResultRowCount(data.size());
					return qr;
				} else
					throw new IWBException("vcs","vcsClientObjectsList:Server Error Response", 0, s, json.has("error")? "Server: " +json.getString("error"):url, null);
					
			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientObjectsList:JSONException", 0, s, "Error", e);
				
			}
		}
		throw new IWBException("vcs","vcsClientObjectsList", 0, url, "No Response from VCS Server2", null);
	}

	

	public Map vcsServerObjectsList(String userName, String passWord, int customizationId, String projectId, int tableId) {
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, null);
		W5Project po = FrameworkCache.getProject(projectId);
		if(tableId>0){
			W5Table t = FrameworkCache.getTable(projectId, tableId);
			if(t.getVcsFlag()==0){
				throw new IWBException("vcs","vcsServerObjectsList", t.getTableId(), t.getDsc(), "Not VCS Table2", null);
			}
		}
		scd.put("projectId", projectId);
		scd.put("oprojectId", projectId);

		List<W5VcsObject> l = tableId == 0 ?
				dao.find("from W5VcsObject t where t.projectUuid=?0 AND t.customizationId=?1 order by t.tableId, t.tablePk", projectId, customizationId) :

					dao.find("from W5VcsObject t where t.projectUuid=?0 AND t.customizationId=?1 AND t.tableId=?2 order by t.tablePk", projectId, customizationId, tableId);
		Map m = new HashMap();
		m.put("success", true);
		List<Map> l2= new ArrayList();
		for(W5VcsObject ox:l){
			Map m2 = new HashMap();
			m2.put(ox.getTablePk(), ox.getVcsObjectStatusTip()==8 ? -ox.getVcsCommitId() : ox.getVcsCommitId());
			l2.add(m2);
		}
		m.put("list", l2);
		dao.saveObject(new Log5VcsAction(scd, (short) 6,null));

		return m;		
	}


	public int vcsClientObjectPush(Map<String, Object> scd, int tableId, int tablePk, boolean force, boolean recursive) throws JSONException {
		if(FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsClientObjectPush",0,null, "VCS Server not allowed to vcsClientObjectPush", null);
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		
		
		W5Table t = FrameworkCache.getTable(projectUuid, tableId);
		if(t.getVcsFlag()==0){
			throw new IWBException("vcs","Not VCS Table", tableId, null, "Not VCS Table2", null);
		}
		
		
		W5VcsObject vo = (W5VcsObject)dao.find("from W5VcsObject t where t.tableId=?0 AND t.tablePk=?1 AND t.customizationId=?2 AND t.projectUuid=?3", tableId, tablePk, customizationId, projectUuid).get(0);
		int action=0;
		switch(vo.getVcsObjectStatusTip()){
		case 0:case	9:case	8://synch durumdaysa Push'a gerek yok (9:active synched, 8:deleted synched)
			if(!force)throw new IWBException("vcs","vcsClientObjectPush", tablePk, "vcsObjectStatusTip = " + vo.getVcsObjectStatusTip(), "Object Already Synched("+t.getDsc()+")", null);
		default:
			action = vo.getVcsObjectStatusTip();
		}

		
		Map o = action!=3 ? dao.getTableRecordJson(scd, t.getTableId(), vo.getTablePk(), 0) : new HashMap();

		
//		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&t="+vo.getTableId()+"&k="+vo.getTablePk()+"&r="+vo.getProjectUuid()+"&o="+vo.getVcsCommitId()+"&a="+action;
		JSONObject params = new JSONObject(); 
		params.put("u", po.getVcsUserName());params.put("p", po.getVcsPassword());params.put("c", customizationId);params.put("t", tableId);params.put("k", tablePk);
		params.put("r", vo.getProjectUuid());params.put("o", vo.getVcsCommitId());params.put("a", action);
//		if(force)urlParameters+="&f=1";
		if(force)params.put("f", 1);
		if(action!=3){
//			urlParameters+="&object="+GenericUtil.uUrlEncode(GenericUtil.fromMapToJsonString2(o));
			params.put("object", GenericUtil.fromMapToJSONObject(o));
		}
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSObjectPush";
		String s = HttpUtil.sendJson(url, params);
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			try {
				json = new JSONObject(s);
				if(json.get("success").toString().equals("true")){
					int srvVcsCommitId =json.getInt("commit_id");
					vo.setVersionNo((short)(vo.getVersionNo()+1));
					vo.setVersionUserId((Integer)scd.get("userId"));
					vo.setVcsCommitId(srvVcsCommitId);					
					vo.setVcsObjectStatusTip((short)(vo.getVcsObjectStatusTip()==3 ? 8:9));//8:synched deleted, 9:synched updated/inserted
					vo.setVcsCommitRecordHash(metadataWriter.getObjectVcsHash(scd, t.getTableId(), vo.getTablePk()));
					dao.updateObject(vo);
					return srvVcsCommitId; 
				} else
					throw new IWBException("vcs","vcsClientObjectPush: serverVCSObjectPush response", t.getTableId(), s, json.has("error") ? json.getString("error") : json.toString().substring(0,300), null);
			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientObjectPush:JSON Exception", t.getTableId(), s, "Error", e);
			}
		}
		throw new IWBException("vcs","vcsClientObjectPush", t.getTableId(), null, "VCS Server not responded", null);
	}

	synchronized public int vcsServerObjectPush(String userName, String passWord, int customizationId, String projectId, int tableId, int tablePk, int vcsCommitId, int action, boolean force, JSONObject jo, String comment) {
		if(!FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsServerObjectPush",0,null, "Not a VCS Server to vcsServerObjectPush", null);
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, projectId);
		W5Project po = FrameworkCache.getProject(projectId);

		W5Table t = FrameworkCache.getTable(projectId, tableId);
		if(t.getVcsFlag()==0){
			throw new IWBException("vcs","vcsServerObjectPush", t.getTableId(), "Not VCS Table", "Not VCS Table2", null);
		}
		
			
		List l = dao.find("from W5VcsObject t where t.tableId=?0 AND t.tablePk=?1 AND t.projectUuid=?2 AND t.customizationId=?3", 
				tableId, tablePk, projectId, customizationId);
		if(action == 1 && GenericUtil.isEmpty(l))
			throw new IWBException("vcs","vcsServerObjectPush", t.getTableId(), "Not Found", "Not VCS Object2", null);
		W5VcsObject o;
		
		if(action!=2)
			o = (W5VcsObject)l.get(0);
		else {
			o = new W5VcsObject(scd, t.getTableId(), tablePk);
		}
		if(!force && o.getVcsCommitId()>vcsCommitId) 
			throw new IWBException("vcs","vcsServerObjectPush", t.getTableId(), "Conflicts", "Conflicts for Specified Object2", null);
		
		o.setVcsObjectStatusTip((short)(action==3 ? 8:9));
		List lm = dao.find("select max(t.vcsCommitId) from W5VcsCommit t where t.projectUuid=?0", projectId);
		W5VcsCommit commit = new W5VcsCommit();
		if(lm.isEmpty() || lm.get(0)==null)commit.setVcsCommitId(1);
		else commit.setVcsCommitId((Integer)lm.get(0)+1);
		commit.setProjectUuid(projectId);commit.setComment(comment);
		dao.saveObject(commit);
		if(jo==null){
			jo=new JSONObject();
			o.setVcsObjectStatusTip((short)8);
		} else 
			o.setVcsObjectStatusTip((short)9);
		metadataWriter.saveVcsObject(scd, tableId, tablePk, action, jo);
		o.setVcsCommitId(commit.getVcsCommitId());
		if(o.getVcsObjectId()==0)
			dao.saveObject(o);
		else
			dao.updateObject(o);
		if(FrameworkSetting.log2tsdb)try {Log4Crud(po.getRdbmsSchema(), t, action, commit.getVcsCommitId(), tablePk, jo);
		} catch (JSONException e) {			}

		return commit.getVcsCommitId();
	}

	private	Map vcsServerAuthenticate(String userName, String passWord, int customizationId, String projectUuid){//TODO:
		List<Object[]> l = dao.executeSQLQuery("select x.user_id,(select r.user_role_id from iwb.w5_user_role r where r.customization_id=x.customization_id AND r.user_id=x.user_id AND r.role_id=0) user_role_id from iwb.w5_user x where x.customization_id=?::integer AND x.user_name=?::text AND x.pass_word=?::text", customizationId, userName, GenericUtil.getMd5Hash(userName+passWord));
		if(GenericUtil.isEmpty(l))
			throw new IWBException("vcs","vcsServerAuthenticate", 0, null, "NoUser or Wrong Password", null);
		Map scd = new HashMap();
		scd.put("userId", l.get(0)[0]);
		scd.put("userRoleId", l.get(0)[1]);
		scd.put("roleId", 0);
		scd.put("customizationId", customizationId);
		scd.put("ocustomizationId", customizationId);
		if(projectUuid!=null)scd.put("projectId", projectUuid);
		return scd;
	}
	
	public boolean vcsClientExportProject(Map<String, Object> scd, int startCommitId) throws JSONException { //TODO
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		
		JSONObject params = new JSONObject(); 
		params.put("u", po.getVcsUserName());params.put("p", po.getVcsPassword());params.put("c", customizationId);

		List lp = new ArrayList();lp.add(projectUuid);lp.add(customizationId);
		Map mo = (Map)dao.executeSQLQuery2Map("select * from iwb.w5_project t where t.project_uuid=? AND t.customization_id=?", lp).get(0);
		params.put("object", GenericUtil.fromMapToJSONObject(mo));
		
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSExportProject";
	/*	String s = HttpUtil.send(url, urlParameters);
		if(!GenericUtil.isEmpty(s)){
			
		}*/
		
		String s = HttpUtil.sendJson(url, params);
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			json = new JSONObject(s);
			if(json.get("success").toString().equals("true")){
				return true;
			} else
				throw new IWBException("vcs","vcsClientExportProject:server Error Response", 0, s, json.has("error") ? json.getString("error"): json.toString(), null);
		}
		return false;
	}

	public int vcsClientObjectPushMulti(Map<String, Object> scd, int tableId, String tablePks, boolean force, boolean recursive, boolean onSynchErrorThrow) throws JSONException {
		if(FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsClientObjectPushMulti",0,null, "VCS Server not allowed to vcsClientObjectPushMulti", null);
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		
		W5Table t = FrameworkCache.getTable(projectUuid, tableId);
		if(t.getVcsFlag()==0){
			throw new IWBException("vcs","vcsClientObjectPushMulti", tableId, null, "Not VCS Table", null);
		}
		
//		StringBuilder urlParameters = new StringBuilder();
//		urlParameters.append("u=").append(po.getVcsUserName()).append("&p=").append(po.getVcsPassword()).append("&t=").append(tableId).append("&c=").append(customizationId).append("&r=").append(projectUuid);
		JSONObject params = new JSONObject(); 
		params.put("u", po.getVcsUserName());params.put("p", po.getVcsPassword());params.put("c", customizationId);params.put("t", tableId);params.put("r", projectUuid);

//		if(force)urlParameters.append("&f=1");
		if(force)params.put("f", 1);

		String[] arTablePks = tablePks.split(",");
		
		
		Map<Integer, W5VcsObject> mv = new HashMap();
//		List lo = new ArrayList();
		JSONArray jlo = new JSONArray();
		for(String k:arTablePks){
			int tablePk = GenericUtil.uInt(k);
			W5VcsObject vo = (W5VcsObject)dao.find("from W5VcsObject t where t.tableId=?0 AND t.tablePk=?1 AND t.customizationId=?2 AND t.projectUuid=?3", tableId, tablePk, customizationId, projectUuid).get(0);
			if(vo.getVcsObjectStatusTip()==9 || vo.getVcsObjectStatusTip()==8 || vo.getVcsObjectStatusTip()==0){//synch durumdaysa Push'a gerek yok
				if(onSynchErrorThrow)throw new IWBException("vcs","vcsClientObjectPushMulti", vo.getVcsObjectId(), null, "Object Already Synched2", null);
				else continue;
			}
			int action= vo.getVcsObjectStatusTip();

			Map o = new HashMap();
			if(action!=3)o.put("o", dao.getTableRecordJson(scd, t.getTableId(), vo.getTablePk(), 0));
			o.put("a", action);
			o.put("k", tablePk);
			if(action!=2)o.put("c", vo.getVcsCommitId());
			mv.put(tablePk, vo);
			jlo.put(GenericUtil.fromMapToJSONObject(o));
			
		}
		if(jlo.length()==0)
			throw new IWBException("vcs","vcsClientObjectPushMulti", t.getTableId(), null, "No Record to Push", null);

		
		params.put("objects", jlo);

		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSObjectPushMulti";
		String s = HttpUtil.sendJson(url, params);
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			try {
				json = new JSONObject(s);
				if(json.get("success").toString().equals("true")){
					int srvVcsCommitId =json.getInt("commit_id");
					for(Integer k:mv.keySet()){
						W5VcsObject vo = mv.get(k);
						vo.setVersionNo((short)(vo.getVersionNo()+1));
						vo.setVersionUserId((Integer)scd.get("userId"));
						vo.setVcsCommitId(srvVcsCommitId);					
						vo.setVcsObjectStatusTip((short)(vo.getVcsObjectStatusTip()==3 ? 8:9));//8:synched deleted, 9:synched updated/inserted
						vo.setVcsCommitRecordHash(metadataWriter.getObjectVcsHash(scd, t.getTableId(), vo.getTablePk()));
						dao.updateObject(vo);	
					}
					return srvVcsCommitId;
				} else
					throw new IWBException("vcs","vcsClientObjectPushMulti:server Error Response", 0, s, json.has("error") ? json.getString("error"): json.toString(), null);
			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientObjectPushMulti:JSONException", t.getTableId(), s, "Error", e);
			}
		}
		return 0;
	}

	synchronized public int vcsServerObjectPushMulti(String userName, String passWord, int customizationId, String projectId, int tableId, boolean force, JSONArray ja, String comment) {
		if(!FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsServerObjectPushMulti",0,null, "Not a VCS Server to vcsServerObjectPushMulti", null);
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, projectId);
		W5Project po = FrameworkCache.getProject(projectId);

		W5Table t = FrameworkCache.getTable(projectId, tableId);
		if(t.getVcsFlag()==0){
			throw new IWBException("vcs","vcsServerObjectPushMulti", t.getTableId(), null, "Not VCS Table2", null);
		}
		
		List lm = dao.find("select max(t.vcsCommitId) from W5VcsCommit t where t.projectUuid=?0", projectId);
		W5VcsCommit commit = new W5VcsCommit();
		if(lm.isEmpty() || lm.get(0)==null)commit.setVcsCommitId(1);
		else commit.setVcsCommitId((Integer)lm.get(0)+1);
		commit.setProjectUuid(projectId);commit.setComment(comment);
		dao.saveObject(commit);

		try {
			for(int qi=0;qi<ja.length();qi++){
				JSONObject jo = ja.getJSONObject(qi);
				
				int action = jo.getInt("a");
				if(action!=3 && !jo.has("o"))continue;
				
				int tablePk = jo.getInt("k");
				List l = action!=2 ? dao.find("from W5VcsObject t where t.tableId=?0 AND t.tablePk=?1 AND t.projectUuid=?2 AND t.customizationId=?3", 
						tableId, tablePk, projectId, customizationId) : null;
				if(action != 2 && GenericUtil.isEmpty(l))
					throw new IWBException("vcs","vcsServerObjectPushMulti", t.getTableId(), "Not Found", "Not VCS Object2", null);
				W5VcsObject o;
				
				if(action!=2)
					o = (W5VcsObject)l.get(0);
				else {
					o = new W5VcsObject(scd, t.getTableId(), tablePk);
				}
				if(!force && action!=2 && o.getVcsCommitId()>jo.getInt("c")) 
					throw new IWBException("vcs","vcsServerObjectPushMulti", t.getTableId(), "Conflicts", "Conflicts for Specified Object2", null);
				
				o.setVcsObjectStatusTip((short)(action==3 ? 8:9));
				if(action==3)
					o.setVcsCommitRecordHash(dao.getTableRecordSummary(scd, tableId, tablePk, 32));
				metadataWriter.saveVcsObject(scd, tableId, tablePk, action, action==3 ? null : jo.getJSONObject("o"));
				if(action!=3)
					o.setVcsCommitRecordHash(metadataWriter.getObjectVcsHash(scd, tableId, tablePk));
				o.setVcsCommitId(commit.getVcsCommitId());
				if(o.getVcsObjectId()==0)
					dao.saveObject(o);
				else
					dao.updateObject(o);

			}
			if(FrameworkSetting.log2tsdb)for(int qi=0;qi<ja.length();qi++){
				JSONObject o = ja.getJSONObject(qi);				
				int action = o.getInt("a");
				JSONObject jo =action == 3 ? null:o.getJSONObject("o");
				int tablePk = o.getInt("k");
				Log4Crud(po.getRdbmsSchema(), t, action, commit.getVcsCommitId(), tablePk, jo);
			}

		} catch (JSONException e) {
			throw new IWBException("vcs","vcsServerObjectPushMulti", t.getTableId(), "JSONException", "Error", e);
		}
		
		return commit.getVcsCommitId();
	}
	
	synchronized public int vcsServerObjectPushAll(String userName, String passWord, int customizationId, String projectId, boolean force, JSONArray ja, String comment) {
		if(!FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsServerObjectPushAll",0,null, "Not a VCS Server to vcsServerObjectPushAll", null);
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, projectId);
		W5Project po = FrameworkCache.getProject(projectId);
		
		List lm = dao.find("select max(t.vcsCommitId) from W5VcsCommit t where t.projectUuid=?0", projectId);
		W5VcsCommit commit = new W5VcsCommit();
		if(lm.isEmpty() || lm.get(0)==null)commit.setVcsCommitId(1);
		else commit.setVcsCommitId((Integer)lm.get(0)+1);
		commit.setProjectUuid(projectId);commit.setComment(comment);
		dao.saveObject(commit);

		try {
			for(int qi=0;qi<ja.length();qi++){
				JSONObject jo = ja.getJSONObject(qi);
				
				int action = jo.getInt("a");
				if(action!=3 && !jo.has("o"))continue;

				int tablePk = jo.getInt("k");
				int tableId = jo.getInt("t");
				List l = action!=2 ? dao.find("from W5VcsObject t where t.tableId=?0 AND t.tablePk=?1 AND t.projectUuid=?2 AND t.customizationId=?3", 
						tableId, tablePk, projectId, customizationId) : null;
				if(action != 2 && GenericUtil.isEmpty(l))
					throw new IWBException("vcs","vcsServerObjectPushAll", tableId, "Not Found", "Not VCS Object2", null);
				W5VcsObject o;
				
				if(action!=2)
					o = (W5VcsObject)l.get(0);
				else {
					o = new W5VcsObject(scd, tableId, tablePk);
				}
				if(!force && action!=2 && o.getVcsCommitId()>jo.getInt("c")) 
					throw new IWBException("vcs","vcsServerObjectPushAll", tableId, "Conflicts", "Conflicts for Specified Object2", null);
				
				o.setVcsObjectStatusTip((short)(action==3 ? 8:9));
				if(action==3)
					o.setVcsCommitRecordHash(dao.getTableRecordSummary(scd, tableId, tablePk, 32));
				metadataWriter.saveVcsObject(scd, tableId, tablePk, action, action==3 ? null : jo.getJSONObject("o"));
				if(action!=3)
					o.setVcsCommitRecordHash(metadataWriter.getObjectVcsHash(scd, tableId, tablePk));
				o.setVcsCommitId(commit.getVcsCommitId());
				if(o.getVcsObjectId()==0)
					dao.saveObject(o);
				else
					dao.updateObject(o);

			}
			
			if(FrameworkSetting.log2tsdb && po!=null)for(int qi=0;qi<ja.length();qi++){
				JSONObject o = ja.getJSONObject(qi);				
				int action = o.getInt("a");
				int tableId = o.getInt("t");
				int tablePk = o.getInt("k");
				JSONObject jo =action == 3 ? null:o.getJSONObject("o");
				W5Table t= FrameworkCache.getTable(scd, tableId);
				Log4Crud(po.getRdbmsSchema(), t, action, commit.getVcsCommitId(), tablePk, jo);
			}

		} catch (JSONException e) {
			throw new IWBException("vcs","vcsServerObjectPushAll", 0, "JSONException", "Error", e);
		}
		
		return commit.getVcsCommitId();
	}

	public Map vcsServerObjectsDetail(String userName, String passWord, int customizationId, String projectId, JSONArray ja) {
//		if(!FrameworkSetting.vcsServer)throw new PromisException("vcs","vcsServerObjectsDetail",0,null, "Not a VCS Server to vcsServerObjectsDetail", null);
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, projectId);
		W5Project po = FrameworkCache.getProject(projectId);

		Map m = new HashMap();
		m.put("success", true);
		try {
			List l = new ArrayList();
			m.put("data", l);
			for(int qi=0;qi<ja.length();qi++){
				String[] pk = ja.getString(qi).replace('.', ',').split(",");
				
				int tableId = GenericUtil.uInt(pk[0]);
				int tablePk = GenericUtil.uInt(pk[1]);
				W5Table t = FrameworkCache.getTable(0, tableId);
				if(t.getVcsFlag()!=0){ //master olanlar haric
					List<W5VcsObject> l2 = dao.find("from W5VcsObject t where t.tableId=?0 AND t.tablePk=?1 AND t.projectUuid=?2", 
							tableId, tablePk, projectId);
					if(!l2.isEmpty()){
						Map m2 = new HashMap();
						m2.put("id", tableId + "." + tablePk);
						W5VcsObject vo = l2.get(0);
						m2.put("user_id", vo.getVersionUserId());
						String userDsc = UserUtil.getUserName(vo.getVersionUserId());
						if(!GenericUtil.isEmpty(userDsc))m2.put("user_dsc", userDsc);
						m2.put("commit_dttm", GenericUtil.uFormatDateTime(vo.getVersionDttm()));
						l.add(m2);
						if(t.getTableTip()!=0){ // master olanlar haric
							List<W5TableRecordHelper> pr = dao.findRecordParentRecords(scd, tableId, tablePk, 2, false);
							if(!GenericUtil.isEmpty(pr)){
								m2.put("dsc", pr.get(0).getRecordDsc());
								if(pr.size()>1)m2.put("parent", pr.get(1).getTableId() + "." + pr.get(1).getTablePk());
							}
						} else {
							List<W5TableRecordHelper> pr = dao.findRecordParentRecords(scd, tableId, tablePk, 1, false);
							if(!GenericUtil.isEmpty(pr)){
								m2.put("dsc", pr.get(0).getRecordDsc());
							}
							
						}
					}
				}
			}
		} catch (JSONException e) {
			throw new IWBException("vcs","vcsServerObjectsDetail", 0, "JSONException", "Error", e);
		}
		
		return m;
	}


	public Map vcsServerObjectsAll(String userName, String passWord, int customizationId, String projectId, String schema) {
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, null);
		W5Project po = FrameworkCache.getProject(projectId);

		List<W5VcsObject> l = null;
		if(GenericUtil.isEmpty(schema)){
			l = dao.find("from W5VcsObject t where t.projectUuid=?0 AND t.customizationId=?1 order by t.tableId, t.vcsCommitId, t.tablePk", projectId, customizationId);
		} else {
			if(!schema.endsWith("%"))schema+="%";
			l = dao.find("from W5VcsObject t where t.projectUuid=?0 AND t.customizationId=?1 and exists(select 1 from W5Table q where q.customizationId=t.customizationId AND q.tableId=t.tableId AND q.dsc like ?2) order by t.tableId, t.vcsCommitId, t.tablePk", projectId, customizationId,schema) ;
		}
		Map m = new HashMap();
		m.put("success", true);
		Map<Integer, List> mall = new HashMap();
		W5Table lastTable = null;
		List<Map> l2=null;
		for(W5VcsObject ox:l){
			if(lastTable == null || lastTable.getTableId()!=ox.getTableId()){
				lastTable = FrameworkCache.getTable(projectId, ox.getTableId());
				if(lastTable!=null && lastTable.getVcsFlag()!=0){
					l2= new ArrayList();
					mall.put(ox.getTableId(), l2);
				}
			}
			if(lastTable!=null && lastTable.getVcsFlag()!=0){
				Map m2 = new HashMap();
				m2.put(ox.getTablePk(), ox.getVcsObjectStatusTip()==8 ? -ox.getVcsCommitId() : ox.getVcsCommitId());
				l2.add(m2);
			}
		}
		m.put("list", mall);
		return m;		
	}
	
	public int vcsClientCleanVCSObjects(Map<String, Object> scd, int tableId) throws JSONException {
		if(FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsClientCleanVCSObjects",0,null, "VCS Server not allowed to vcsClientCleanVCSObjects", null);
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);

		int count =0;
		if(tableId==-1){
			List<Integer> ps = dao.executeSQLQuery("select q.table_id from iwb.w5_table q where q.vcs_flag=1 AND q.project_uuid in (?,'067e6162-3b6f-4ae2-a221-2470b63dff00')", projectUuid);
			if(ps!=null)for(Integer tid:ps){
				W5Table t = FrameworkCache.getTable(projectUuid, tid);
				String sql ="from iwb.w5_vcs_object where vcs_object_status_tip in (1,2,9) "
						+ "and table_id=?::integer and customization_id=? AND table_pk not in (select q."+t.get_tableParamList().get(0).getExpressionDsc()+" from "+t.getDsc()+" q where q.customization_id=?)";
				int newCount = GenericUtil.uInt(dao.executeSQLQuery("select count(1) " +sql, tableId, customizationId, customizationId).get(0));
				if(newCount>0){
					count += newCount;
					dao.executeUpdateSQLQuery("delete " + sql, tableId, customizationId, customizationId);
				}
			}
		} else {
			W5Table t = FrameworkCache.getTable(projectUuid, tableId);
			if(t.getVcsFlag()==0){
				throw new IWBException("vcs","vcsClientObjectsList", t.getTableId(), t.getDsc(), "Not VCS Table2", null);
			}
			String sql ="from iwb.w5_vcs_object where vcs_object_status_tip in (1,2,9) "
					+ "and table_id=? and project_uuid=? AND table_pk not in (select q."+t.get_tableParamList().get(0).getExpressionDsc()+" from "+t.getDsc()+" q where q.project_uuid=?)";
			count = GenericUtil.uInt(dao.executeSQLQuery("select count(1) " +sql, tableId, projectUuid, projectUuid).get(0));
			if(count>0)dao.executeUpdateSQLQuery("delete " + sql, tableId, projectUuid, projectUuid);

			count = GenericUtil.uInt(dao.executeSQLQuery("select count(1) " +sql, tableId, projectUuid, projectUuid).get(0));

		}
		return count;
	}

	public int vcsClientObjectPushAll(Map<String, Object> scd, String tableKeys, boolean force, boolean recursive, boolean onSynchErrorThrow) throws JSONException {
		if(FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsClientObjectPushAll",0,null, "VCS Server not allowed to vcsClientObjectPushAll", null);
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);

		
		
//		StringBuilder urlParameters = new StringBuilder();
//		urlParameters.append("u=").append(po.getVcsUserName()).append("&p=").append(po.getVcsPassword()).append("&c=").append(customizationId).append("&r=").append(projectUuid);
		JSONObject params = new JSONObject(); 
		params.put("u", po.getVcsUserName());params.put("p", po.getVcsPassword());params.put("c", customizationId);params.put("r", projectUuid);

//		if(force)urlParameters.append("&f=1");
		if(force)params.put("f", 1);
		String[] arTableKeys = tableKeys.split(",");
		
		
		Map<String, W5VcsObject> mv = new HashMap();
//		List lo = new ArrayList();
		JSONArray jlo = new JSONArray();
		for(String k:arTableKeys){
			String[] tableKey = k.replace('.', ',').split(",");
			int tableId=GenericUtil.uInt(tableKey[0]);
			if(FrameworkCache.getTable(projectUuid, tableId).getVcsFlag()==0){
				if(onSynchErrorThrow)
					throw new IWBException("vcs","vcsClientObjectPushAll", 0, projectUuid, "Not VCS Table", null);
				continue;
			}
			int tablePk=GenericUtil.uInt(tableKey[1]);
			W5VcsObject vo = (W5VcsObject)dao.find("from W5VcsObject t where t.tableId=?0 AND t.tablePk=?1 AND t.customizationId=?2 AND t.projectUuid=?3", tableId, tablePk, customizationId, projectUuid).get(0);
			if(vo.getVcsObjectStatusTip()==9 || vo.getVcsObjectStatusTip()==8 || vo.getVcsObjectStatusTip()==0){//synch durumdaysa Push'a gerek yok
				if(onSynchErrorThrow)throw new IWBException("vcs","vcsClientObjectPushAll", vo.getVcsObjectId(), null, "Object Already Synched2", null);	else 
					continue;
			}
			int action= vo.getVcsObjectStatusTip();

			Map o = new HashMap();
			if(action!=3)o.put("o", dao.getTableRecordJson(scd, tableId, vo.getTablePk(), 0));
			o.put("a", action);
			o.put("t", tableId);
			o.put("k", tablePk);
			if(action!=2)o.put("c", vo.getVcsCommitId());
			mv.put(k, vo);
			jlo.put(GenericUtil.fromMapToJSONObject(o));			
		}
		if(jlo.length()==0)
			throw new IWBException("vcs","vcsClientObjectPushAll", 0, null, "No Record to Push", null);

		
		params.put("objects", jlo);
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSObjectPushAll";
		String s = HttpUtil.sendJson(url, params);
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			try {
				json = new JSONObject(s);
				if(json.get("success").toString().equals("true")){
					int srvVcsCommitId =json.getInt("commit_id");
					for(String k:mv.keySet()){
						W5VcsObject vo = mv.get(k);
						vo.setVersionNo((short)(vo.getVersionNo()+1));
						vo.setVersionUserId((Integer)scd.get("userId"));
						vo.setVcsCommitId(srvVcsCommitId);					
						vo.setVcsObjectStatusTip((short)(vo.getVcsObjectStatusTip()==3 ? 8:9));//8:synched deleted, 9:synched updated/inserted
						vo.setVcsCommitRecordHash(metadataWriter.getObjectVcsHash(scd, vo.getTableId(), vo.getTablePk()));
						dao.updateObject(vo);	
					}
					return srvVcsCommitId;
				} else
					throw new IWBException("vcs","vcsClientObjectPushAll:server Error Response", 0, s, json.has("error") ? json.getString("error"): json.toString(), null);
			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientObjectPushAll:JSONException", 0, s, "Error", e);
			}
		}
		return 0;
	}
	public W5QueryResult vcsClientDBObjectList(Map<String, Object> scd) {
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		
		StringBuilder urlParameters = new StringBuilder();
		urlParameters.append("u=").append(po.getVcsUserName()).append("&p=").append(po.getVcsPassword()).append("&c=").append(customizationId).append("&r=").append(projectUuid);
		
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverDBObjectAll";
		
		String s = HttpUtil.send(url, urlParameters.toString());
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			try {
				
				json = new JSONObject(s);
				if(json.get("success").toString().equals("true")){
					JSONArray ar =json.getJSONArray("list");
					
					List ps=new ArrayList();ps.add(po.getRdbmsSchema());
					List<Map> ltables = dao.executeSQLQuery2Map("select x.table_name dsc, iwb.md5hash((select string_agg(y.column_name||'.'||y.is_nullable||'.'||y.data_type||'.'||coalesce(y.character_maximum_length,0)::text,',' order by ordinal_position) src_md5hash from information_schema.columns y "
					+ " where y.table_schema=x.table_schema "
					+ " AND y.table_name=x.table_name "
					+ ")) src_md5hash from information_schema.tables x "
					+ " where x.table_schema=? order by table_name", ps);
			
					List<Map> ldb_funcs = dao.executeSQLQuery2Map("select x.proname||replace(replace(coalesce(x.proargnames::text,''),'{','('),'}',')') dsc, iwb.md5hash(coalesce(x.proargnames::text,'')||coalesce(x.proargtypes::text,'') ||x.prosrc) src_md5hash from pg_proc x where x.pronamespace=(select q.oid from pg_namespace q where q.nspname=?) order by dsc"
					, ps);
					
					Map<String, String> mlc = new HashMap();
					if(!GenericUtil.isEmpty(ltables))for(Map o:ltables) {
						mlc.put("1."+o.get("dsc"), (String)o.get("src_md5hash"));
					}
					if(!GenericUtil.isEmpty(ldb_funcs))for(Map o:ldb_funcs) {
						mlc.put("2."+o.get("dsc"), (String)o.get("src_md5hash"));
					}
					
					W5QueryResult qr = metadataLoader.getQueryResult(scd, 2768);
					qr.setErrorMap(new HashMap());qr.setNewQueryFields(new ArrayList(qr.getQuery().get_queryFields().size()));qr.getNewQueryFields().addAll(qr.getQuery().get_queryFields());
					List data = new ArrayList();
					for(int qi=0;qi<ar.length();qi++) {
						JSONObject jo = ar.getJSONObject(qi);
						String key = jo.getInt("tip")+"."+jo.getString("dsc");
						String lclHash = (String)mlc.get(key);
						Object[] ox = new Object[5];
						ox[0] = jo.getInt("tip");
						ox[1] = jo.getString("dsc");
						if(lclHash==null) { //romte'da var local de yok
							ox[2] = 1; //push
						} else if(!lclHash.equals(jo.getString("src_md5hash"))){
							ox[2] =  3; //conflict
						} else {
							mlc.remove(key);
							continue;
						}
						mlc.remove(key);
						data.add(ox);
					}
					
					for(String k:mlc.keySet()) {
						Object[] ox = new Object[5];
						ox[0] = k.substring(0,1);
						ox[1] = k.substring(2);
						ox[2] = 2; //push
						data.add(ox);
					}
					
					qr.setData(data);
					return qr;
				} else
					throw new IWBException("vcs","vcsClientDBObjectList:server Error Response", 0, s, json.has("error") ? json.getString("error"): json.toString(), null);
			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientDBObjectList:JSONException", 0, s, "Error", e);
			}
		}
		
		
		return null;
	}
		
		
	public Map serverDBObjectAll(String userName, String passWord, int customizationId, String projectId) {
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, projectId);
		W5Project po = FrameworkCache.getProject(projectId);
	

		Map m = new HashMap();

		List ps=new ArrayList();ps.add(po.getRdbmsSchema());
		List<Map> ltables = dao.executeSQLQuery2Map("select 1 tip, x.table_name dsc, iwb.md5hash((select string_agg(y.column_name||'.'||y.is_nullable||'.'||y.data_type||'.'||coalesce(y.character_maximum_length,0)::text,',' order by ordinal_position) src_md5hash from information_schema.columns y "
			+ " where y.table_schema=x.table_schema "
			+ " AND y.table_name=x.table_name "
			+ ")) src_md5hash from information_schema.tables x "
			+ " where x.table_schema=? order by table_name", ps);
		List<Map> ldb_funcs = dao.executeSQLQuery2Map("select 2 tip, x.proname||replace(replace(coalesce(x.proargnames::text,''),'{','('),'}',')') dsc, iwb.md5hash(coalesce(x.proargnames::text,'')||coalesce(x.proargtypes::text,'') ||x.prosrc) src_md5hash from pg_proc x where x.pronamespace=(select q.oid from pg_namespace q where q.nspname=?) order by dsc"
				, ps);
		
		
		List all = new ArrayList();
		if(!GenericUtil.isEmpty(ltables))all.addAll(ltables);
		if(!GenericUtil.isEmpty(ldb_funcs))all.addAll(ldb_funcs);
		m.put("success", true);
		m.put("list", all);
		return m;
	}

	
	synchronized public Map vcsServerSQLCommit(String userName, String passWord, int customizationId, String projectId, String sql, String comment) {
		if(!FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsServerSQLCommit",0,null, "Not VCS Server to vcsServerSQLCommit", null);
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, projectId);
		W5Project po = FrameworkCache.getProject(projectId);
		
		//TODO, biraz inceleme gerekebilir
		if(po.getCustomizationId()==0){ //only for customizationId
			dao.executeUpdateSQLQuery(sql);
		}
		
		List lm = dao.find("select max(t.vcsCommitId) from W5VcsCommit t where t.projectUuid=?0", projectId);
		W5VcsCommit commit = new W5VcsCommit();
		if(lm.isEmpty() || lm.get(0)==null)commit.setVcsCommitId(1);
		else commit.setVcsCommitId((Integer)lm.get(0)+1);
		commit.setProjectUuid(projectId);commit.setComment(comment);
		commit.setExtraSql(sql);
		dao.saveObject(commit);
		Map m = new HashMap();
		m.put("success", true);
		m.put("cnt", 1);
		return m;
	}

	public String vcsClientSqlCommitList(Map<String, Object> scd) {
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);

		List lm = dao.find("select max(t.vcsCommitId) from W5VcsCommit t where t.projectUuid=?0", po.getProjectUuid());
		int lastCommitId = 0;
		if(lm.isEmpty() || lm.get(0)==null)lastCommitId = 0;
		else lastCommitId = (Integer)lm.get(0);
		
		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&r="+po.getProjectUuid()+"&q=2770&l="+lastCommitId;
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSQueryResult";
		return HttpUtil.send(url, urlParameters);
	}


	public W5QueryResult vcsServerQueryResult(String userName, String passWord, int customizationId, String projectId,
			int queryId, Map<String,String>  requestParams) {
		if(!FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsServerQueryResult",0,null, "Not VCS Server to vcsServerQueryResult", null);
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, projectId);
		return runQuery(scd, queryId, requestParams);
	}

	public W5QueryResult runQuery(Map scd, int queryId, Map<String,String>  requestParams) {
		W5QueryResult queryResult = metadataLoader.getQueryResult(scd,queryId);

/*		StringBuilder tmpx = new StringBuilder("ali baba ${obj.dsc} ve 40 haramiler ${lnk.pk_query_field_id.dsc} olmus");
		dao.interprateTemplate(scd, 5,1294, tmpx, true); */
		queryResult.setErrorMap(new HashMap());
		queryResult.setRequestParams(requestParams);
		
//		queryResult.setOrderBy(PromisUtil.uStrNvl(requestParams.get(PromisUtil.uStrNvl(PromisSetting.appSettings.get("sql_sort"),"sort")), queryResult.getQuery().getSqlOrderby()));
		queryResult.setOrderBy(queryResult.getQuery().getSqlOrderby());		
		switch(queryResult.getQuery().getQueryTip()){
		case	9:case	10:queryResult.prepareTreeQuery(null);break;
		case	15:queryResult.prepareDataViewQuery(null);break;
		default:queryResult.prepareQuery(null);
		}
		if(queryResult.getErrorMap().isEmpty()){
	        queryResult.setFetchRowCount(GenericUtil.uIntNvl(requestParams, "limit", GenericUtil.uInt(requestParams,"firstLimit")));
	        queryResult.setStartRowNumber(GenericUtil.uInt(requestParams,"start"));
        	dao.runQuery(queryResult);
        	if(queryResult.getQuery().getShowParentRecordFlag()!=0 && queryResult.getData()!=null){
        		for(Object[] oz:queryResult.getData()){
        			int tableId = GenericUtil.uInt(oz[queryResult.getQuery().get_tableIdTabOrder()-1]);
        			int tablePk = GenericUtil.uInt(oz[queryResult.getQuery().get_tablePkTabOrder()-1]);
        			if(tableId!=0 && tablePk!=0)oz[oz.length-1]=dao.findRecordParentRecords(scd, tableId, tablePk, 0, true);
        		}
        	}
		}
		return queryResult;
	}

	synchronized public int vcsClientSqlCommitsFirstSkip(Map<String, Object> scd) {
		if(FrameworkSetting.vcsServer && !FrameworkSetting.vcsServerClient)
			throw new IWBException("vcs","vcsClientSqlCommitsFetchAndRun",0,null, "VCS Server not allowed to vcsClientSqlCommitsFetchAndRun", null);
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		if(po==null) po = metadataLoader.loadProject(projectUuid);

		List lm = dao.find("select max(t.vcsCommitId) from W5VcsCommit t where t.projectUuid=?0", projectUuid);
		int lastCommitId = 0;
		if(lm.isEmpty() || lm.get(0)==null)lastCommitId = 0;
		else lastCommitId = (Integer)lm.get(0);
		
		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&r="+po.getProjectUuid()+"&q=2770&l="+lastCommitId;
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSQueryResult";
		String s = HttpUtil.send(url, urlParameters);
		
		int result = 0;
		
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			try {
				json = new JSONObject(s);
				if(json.get("success").toString().equals("true")){
					JSONArray ar = json.getJSONArray("data");
					for(int qi=0;qi<1;qi++){
						JSONObject o = ar.getJSONObject(ar.length()-1-qi);

						dao.saveObject(new W5VcsCommit(o));
					}
				} else
					throw new IWBException("vcs","vcsClientSqlCommitsFetchAndRun:server Error Response", 0, s, json.has("error") ? json.getString("error"): json.toString(), null);
			} catch (IWBException e){
				throw e;
			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientSqlCommitsFetchAndRun:JSONException", 0, s, "Error", e);
			}
		}
		return result;	}

	synchronized public int vcsClientSqlCommitsFetchAndRun(Map<String, Object> scd, int maxCount) {
		if(FrameworkSetting.vcsServer && !FrameworkSetting.vcsServerClient)
			throw new IWBException("vcs","vcsClientSqlCommitsFetchAndRun",0,null, "VCS Server not allowed to vcsClientSqlCommitsFetchAndRun", null);
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		if(po==null) po = metadataLoader.loadProject(projectUuid);

		List lm = dao.find("select max(t.vcsCommitId) from W5VcsCommit t where t.projectUuid=?0", projectUuid);
		int lastCommitId = 0;
		if(lm.isEmpty() || lm.get(0)==null)lastCommitId = 0;
		else lastCommitId = (Integer)lm.get(0);
		
		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&r="+po.getProjectUuid()+"&q=2770&l="+lastCommitId;
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSQueryResult";
		String s = HttpUtil.send(url, urlParameters);
		
		int result = 0;
		
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			try {
				json = new JSONObject(s);
				if(json.get("success").toString().equals("true")){
					JSONArray ar = json.getJSONArray("data");
					for(int qi=0;qi<ar.length() && (maxCount==0 || qi<maxCount);qi++){
						JSONObject o = ar.getJSONObject(ar.length()-1-qi);
						if(o.has("extra_sql")){
							String extraSql = o.getString("extra_sql");
							if(!GenericUtil.isEmpty(extraSql)){
								dao.executeUpdateSQLQuery("set search_path="+po.getRdbmsSchema());
								if(FrameworkSetting.debug)System.out.println("Executing SQL: " +o.getInt("vcs_commit_id") + " : " + extraSql);
								result+=dao.executeUpdateSQLQuery(extraSql);
							}
						}
						dao.saveObject(new W5VcsCommit(o));
					}
				} else
					throw new IWBException("vcs","vcsClientSqlCommitsFetchAndRun:server Error Response", 0, s, json.has("error") ? json.getString("error"): json.toString(), null);
			} catch (IWBException e){
				throw e;
			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientSqlCommitsFetchAndRun:JSONException", 0, s, "Error", e);
			}
		}
		return result;
	}
	
	synchronized public int vcsServerAddSQL(String userName, String passWord, int customizationId, String projectId,
			String sql, String comment) {
		if(!FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsServerAddSQL",0,null, "Not VCS Server to vcsServerAddSQL", null);
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, null);
		W5Project po = FrameworkCache.getProject(projectId);

		dao.executeUpdateSQLQuery("set search_path="+po.getRdbmsSchema());
		
		if(customizationId==0 || FrameworkCache.getAppSettingIntValue(0, "run_global_sql")!=0){// security:only for customization 0
			dao.executeUpdateSQLQuery(sql); 
		}
		
		W5VcsCommit commit = new W5VcsCommit();
		commit.setCommitTip((short)2);
		commit.setExtraSql(sql);
		commit.setProjectUuid(projectId);
		commit.setOprojectUuid(projectId);
		commit.setComment(comment);
		commit.setCommitUserId((Integer)scd.get("userId"));
		commit.setRunLocalFlag((short)1);

		List lm = dao.find("select max(t.vcsCommitId) from W5VcsCommit t where t.projectUuid=?0", po.getProjectUuid());
		int lastCommitId = 0;
		if(lm.isEmpty() || lm.get(0)==null)lastCommitId = 0;
		else lastCommitId = (Integer)lm.get(0);

		commit.setVcsCommitId(lastCommitId+1);
		dao.saveObject(commit);
		
		return commit.getVcsCommitId();
	}


	public int vcsClientPushSql(Map<String, Object> scd, int commitId) throws JSONException {
		if(FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsClientPushSql",0,null, "VCS Server not allowed to vcsClientAddSql", null);
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);

		String serverQR = vcsClientSqlCommitList(scd);
		if(!GenericUtil.isEmpty(serverQR))try {
			JSONObject json2 = new JSONObject(serverQR);
			JSONArray ar2 = json2.getJSONArray("data");
			if(ar2.length()>0){
				throw new IWBException("vcs","vcsClientPushSql", 0, serverQR, "You have to Fetch And Run Server SQLs First!!!", null);
			}
		} catch (JSONException e){
			throw new IWBException("vcs","vcsClientPushSql:JSONException", 0, serverQR, "Error", e);
		}
		
//		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&r="+po.getProjectUuid()+"&s="+GenericUtil.uUrlEncode(sql)+"&comment="+GenericUtil.uUrlEncode(comment);

		W5VcsCommit co = (W5VcsCommit)dao.find("from W5VcsCommit c where c.projectUuid=?0 AND c.vcsCommitId=?1", projectUuid, commitId).get(0);

		JSONObject params = new JSONObject(); 
		params.put("u", po.getVcsUserName());params.put("p", po.getVcsPassword());params.put("c", customizationId);
		params.put("r", po.getProjectUuid());params.put("or", co.getOprojectUuid());params.put("s", co.getExtraSql());params.put("comment", co.getComment());

		
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSAddSql";
		String s = HttpUtil.sendJson(url, params);
		
		int result = 0;
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			try {
				json = new JSONObject(s);
				if(json.get("success").toString().equals("true")){
					dao.executeUpdateSQLQuery("update iwb.w5_vcs_commit set vcs_commit_id=?, commit_user_id=?, commit_dttm=current_timestamp where vcs_commit_id=? AND project_uuid=?", json.getInt("cnt"), (Integer)scd.get("userId"), commitId, po.getProjectUuid());
					return json.getInt("cnt");
				} else
					throw new IWBException("vcs","vcsClientPushSql:server Error Response", 0, s, json.has("error") ? json.getString("error"): json.toString(), null);
			} catch (IWBException e){
				throw e;
			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientPushSql:JSONException", 0, s, "Error", e);
			}
		}
		return 0;
	}


	public int vcsClientPushSqlRT(Map<String, Object> scd, String sql, String comment) throws JSONException {
		if(FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsClientPushSqlRT",0,null, "VCS Server not allowed to vcsClientPushSqlRT", null);
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);

		String serverQR = vcsClientSqlCommitList(scd);
		if(!GenericUtil.isEmpty(serverQR))try {
			JSONObject json2 = new JSONObject(serverQR);
			JSONArray ar2 = json2.getJSONArray("data");
			if(ar2.length()>0){
				throw new IWBException("vcs","vcsClientPushSqlRT", 0, serverQR, "You have to Fetch And Run Server SQLs First!!!", null);
			}
		} catch (JSONException e){
			throw new IWBException("vcs","vcsClientPushSqlRT:JSONException", 0, serverQR, "Error", e);
		}
		
//		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&r="+po.getProjectUuid()+"&s="+GenericUtil.uUrlEncode(sql)+"&comment="+GenericUtil.uUrlEncode(comment);


		JSONObject params = new JSONObject(); 
		params.put("u", po.getVcsUserName());params.put("p", po.getVcsPassword());params.put("c", customizationId);
		params.put("r", po.getProjectUuid());params.put("s", sql);params.put("comment", comment);

		
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSAddSql";
		String s = HttpUtil.sendJson(url, params);
		
		int result = 0;
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			try {
				json = new JSONObject(s);
				if(json.get("success").toString().equals("true")){
					dao.executeUpdateSQLQuery("INSERT INTO iwb.w5_vcs_commit(vcs_commit_id, project_uuid, comment, commit_user_id, commit_ip, extra_sql, commit_tip) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?)", json.getInt("cnt"), projectUuid, comment, (Integer)scd.get("userId"), "localhost", sql, 2);
					return json.getInt("cnt");
				} else
					throw new IWBException("vcs","vcsClientPushSqlRT:server Error Response", 0, s, json.has("error") ? json.getString("error"): json.toString(), null);
			} catch (IWBException e){
				throw e;
			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientPushSqlRT:JSONException", 0, s, "Error", e);
			}
		}
		return 0;
	}


	public int vcsClientObjectAction(Map<String, Object> scd, int tableId, int tablePk, int action) {
		if(FrameworkSetting.vcsServer && !FrameworkSetting.vcsServerClient)
			throw new IWBException("vcs","vcsClientObjectAction",0,null, "VCS Server not allowed to vcsClientObjectAction", null);
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);

		W5Table t = FrameworkCache.getTable(projectUuid, tableId);
		if(customizationId>0 && t ==null)t = FrameworkCache.getTable(0, tableId);
		if(t==null)return 0;
		if(t.getVcsFlag()==0){
			throw new IWBException("vcs","vcsClientObjectAction", t.getTableId(), po.getProjectUuid()+"!="+projectUuid, "Not VCS Table2", null);
		}
		switch(action){
		case	3://ignore
//			return dao.executeUpdateSQLQuery("delete from iwb.w5_vcs_object where table_id=? AND table_pk=? AND customization_id=? AND project_uuid=?", tableId, tablePk, customizationId, projectUuid);
			List<Object[]> ll = dao.executeSQLQuery("select count(1) xxx, sum(case when vcs_object_status_tip=0 then 1 else 0 end) yyy from iwb.w5_vcs_object where table_id=? AND table_pk=? AND customization_id=? AND project_uuid=?", tableId, tablePk, customizationId, projectUuid);
			int cnt = GenericUtil.uInt(ll.get(0)[0]);
			if(cnt==0) {
				W5VcsObject vo = new W5VcsObject(scd, tableId, tablePk);
				vo.setVcsObjectStatusTip((short)0);
				dao.saveObject(vo);
				return 1;
			} else	if(GenericUtil.uInt(ll.get(0)[1])==0)	
				return dao.executeUpdateSQLQuery("update iwb.w5_vcs_object set vcs_object_status_tip=0 where table_id=? AND table_pk=? AND customization_id=? AND project_uuid=?", tableId, tablePk, customizationId, projectUuid);
			else
				return dao.executeUpdateSQLQuery("delete from iwb.w5_vcs_object where table_id=? AND table_pk=? AND customization_id=? AND project_uuid=?", tableId, tablePk, customizationId, projectUuid);
			
		case	2://insert
			dao.saveObject(new W5VcsObject(scd, tableId, tablePk));
			return 1;		
		}

		return 0;
	}


	public W5QueryResult vcsClientTableConflicts(Map<String, Object> scd, String tableName) throws JSONException {
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		
		
		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&r="+po.getProjectUuid();
		urlParameters+="&q=141&xtable_name="+tableName+"&xowner="+po.getRdbmsSchema();
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSQueryResult";
		String s = HttpUtil.send(url, urlParameters);
		Map rm = new HashMap(); rm.put("xtable_name", tableName);rm.put("xowner", po.getRdbmsSchema());
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			json = new JSONObject(s);
			if(json.get("success").toString().equals("true")){
				W5QueryResult local = runQuery(scd, 141, rm);
				JSONArray data = json.getJSONArray("data");
				Map<String, JSONObject> remoteColumns = new HashMap();
				for(int qi=0;qi<data.length();qi++){
					remoteColumns.put(data.getJSONObject(qi).getString("column_name"), data.getJSONObject(qi));
				}
				
				for(int qi=0;qi<local.getData().size();qi++){
					Object[] localColumn = local.getData().get(qi);
					localColumn[31]=0;
					JSONObject remoteColumn = remoteColumns.get((String)localColumn[3]); //column_name
					if(remoteColumn==null){ // remote'da yok, local'de var
						localColumn[31]=2;
					} else {
						for(W5QueryField f:local.getQuery().get_queryFields())if(GenericUtil.hasPartInside2("6,7,8,9,11,12,13,14", f.getTabOrder())){
							Object o = remoteColumn.get(f.getDsc());
							if(!GenericUtil.safeEquals2(localColumn[f.getTabOrder()-1],o)){
								localColumn[31]=1;
								localColumn[f.getTabOrder()-1]="!"+localColumn[f.getTabOrder()-1]+"!"+o;
							}
						}
						remoteColumn.remove((String)localColumn[3]);
					}
				}
				if(!remoteColumns.isEmpty())for(String k:remoteColumns.keySet()){
					Object[] newRec = new  Object[45];
					JSONObject remoteColumn = remoteColumns.get(k);
					for(W5QueryField f:local.getQuery().get_queryFields()){
						newRec[f.getTabOrder()-1] = remoteColumn.get(f.getDsc());
					}
					newRec[31] = 3; //remote'da var, locale'de yok
				}
				return local;
				
			}
		}
		return null;
	}
	
	
	public Map vcsClientDBFuncDetail(Map<String, Object> scd, String dbFuncName) throws JSONException {
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		
		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&r="+po.getProjectUuid();
		urlParameters+="&q=301&f="+dbFuncName+"&s="+po.getRdbmsSchema();
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSQueryResult";
		String s = HttpUtil.send(url, urlParameters);
		Map result = new HashMap();
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			json = new JSONObject(s);
			if(json.get("success").toString().equals("true")){
				if(json.has("data")){
					JSONArray rx= json.getJSONArray("data");
					if(rx.length()>0)
						result.put("rmt", rx.getJSONObject(0).getString("src"));
				}
				
			}
		}
		
		Map rm = new HashMap(); rm.put("f", dbFuncName);rm.put("s", po.getRdbmsSchema());
		W5QueryResult local = runQuery(scd, 301, rm);
		if(local.getData().size()>0){
			result.put("lcl", local.getData().get(0)[0]);
		}
		return result;
	}


	public boolean vcsFix(Map<String, Object> scd, int tid, int action) throws JSONException {
		if(action==1){
			vcsClientCleanVCSObjects(scd, tid);
			return true;
		}
		

		if(action!=3 && action!=5 && FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsClientFix",0,null, "VCS Server not allowed to vcsClientFix", null);
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);

		StringBuilder gsql = new StringBuilder();List<Object> gparams = new ArrayList();
		W5Table mt = FrameworkCache.getTable(projectUuid, tid);
		if(mt.getVcsFlag()==0){
			throw new IWBException("vcs","vcsClientFix", mt.getTableId(), po.getProjectUuid()+"!="+projectUuid, "Not VCS Table2", null);
		}
		List params = new ArrayList();
		StringBuilder sql = new StringBuilder();
		switch(action){
		case	2://REC.projectUiid!=VCS_OBJECT .projectUiid for INSERT
			sql.append("update iwb.w5_vcs_object v set project_uuid=? where v.customization_id=? AND v.project_uuid!=? AND v.table_id=? AND v.vcs_object_status_tip=2 AND exists(select 1 from ").append(mt.getDsc())
				.append(" d where d.project_uuid=? AND d.").append(mt.get_tableParamList().get(0).getExpressionDsc()).append("=v.table_pk");
			params.add(projectUuid);params.add(customizationId);params.add(projectUuid);params.add(tid);params.add(projectUuid);
			if(mt.get_tableParamList().size()>1){
				sql.append(" AND d.customization_id=?");
				params.add(customizationId);
			}
			sql.append(")");
			dao.executeUpdateSQLQuery(sql.toString(), params);
			return true;
		case	3://REC.projectUiid!=VCS_OBJECT .projectUiid for SYNC
			sql.append("select count(1) from iwb.w5_vcs_object v where v.customization_id=? AND v.project_uuid!=? AND v.table_id=? AND v.vcs_object_status_tip in (1,9) AND exists(select 1 from ").append(mt.getDsc())
				.append(" d where d.project_uuid=? AND d.").append(mt.get_tableParamList().get(0).getExpressionDsc()).append("=v.table_pk");
			params.add(customizationId);params.add(projectUuid);params.add(tid);params.add(projectUuid);
			if(mt.get_tableParamList().size()>1){
				sql.append(" AND d.customization_id=?");
				params.add(customizationId);
			}
			sql.append(")");
			
			int newCount = GenericUtil.uInt(dao.executeSQLQuery2(sql.toString(), params).get(0));
			if(newCount>0){
				sql.setLength(0);params.clear();
				sql.append("update iwb.w5_vcs_object v set project_uuid=? where v.customization_id=? AND v.project_uuid!=? AND v.table_id=? AND v.vcs_object_status_tip in (1,9) AND exists(select 1 from ").append(mt.getDsc())
				.append(" d where d.project_uuid=? AND d.").append(mt.get_tableParamList().get(0).getExpressionDsc()).append("=v.table_pk");
				params.add(projectUuid);params.add(customizationId);params.add(projectUuid);params.add(tid);params.add(projectUuid);
				if(mt.get_tableParamList().size()>1){
					sql.append(" AND d.customization_id=?");
					params.add(customizationId);
				}
				sql.append(")");
				dao.executeUpdateSQLQuery(sql.toString(), params);

				vcsClientPushSqlRT(scd, GenericUtil.replaceSql(sql.toString(), params), mt.getDsc() + " (REC.UUID != VCS.UUID 4 SYNC)");
			}
			return true;
		case	4://REC.projectUiid!=DETAIL_REC.projectUiid for INSERT
			if(!GenericUtil.isEmpty(mt.get_tableChildList()))for(W5TableChild tc:mt.get_tableChildList()){
				W5Table dt = FrameworkCache.getTable(projectUuid, tc.getRelatedTableId());
				if(dt.getTableTip()==0)continue;
				
				sql.setLength(0);params.clear();
				sql.append("update ").append(dt.getDsc()).append(" d set project_uuid=? where d.project_uuid!=? AND exists(select 1 from iwb.w5_vcs_object v where v.vcs_object_status_tip=2 AND v.customization_id=? AND v.table_id=? AND v.table_pk=d.")
					.append(dt.get_tableParamList().get(0).getExpressionDsc()).append(") AND exists(select 1 from ").append(mt.getDsc())
					.append(" m where m.project_uuid=? AND m.").append(mt.get_tableFieldMap().get(tc.getTableFieldId()).getDsc()).append("=d.").append(dt.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc());
				params.add(projectUuid);params.add(projectUuid);params.add(customizationId);params.add(tc.getRelatedTableId());params.add(projectUuid);
				if(mt.get_tableParamList().size()>1){
					sql.append(" AND m.customization_id=?");
					params.add(customizationId);
				}
				sql.append(")");
				if(tc.getRelatedStaticTableFieldId()!=0){
					sql.append(" AND d.").append(dt.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc()).append("=").append(tc.getRelatedStaticTableFieldVal());
					
				}
				if(dt.get_tableParamList().size()>1){
					sql.append(" AND d.customization_id=?");
					params.add(customizationId);
				}
				
//				List rl = dao.executeSQLQuery2("select "+tc.getRelatedTableId()+" tid, "+dt.get_tableParamList().get(0).getExpressionDsc()+" tpk from "+dt.getDsc()+" d " + sql.toString(), params);
				dao.executeUpdateSQLQuery(sql.toString(), params);

				sql.setLength(0);params.clear();
				sql.append("update iwb.w5_vcs_object v set project_uuid=? where v.customization_id=? AND v.project_uuid!=? AND v.table_id=? AND v.vcs_object_status_tip=2 AND exists(select 1 from ").append(dt.getDsc())
					.append(" d where d.project_uuid=? AND d.").append(dt.get_tableParamList().get(0).getExpressionDsc()).append("=v.table_pk");
				params.add(projectUuid);params.add(projectUuid);params.add(customizationId);params.add(tc.getRelatedTableId());params.add(projectUuid);
				if(dt.get_tableParamList().size()>1){
					sql.append(" AND d.customization_id=?");
					params.add(customizationId);
				}
				sql.append(")");
				dao.executeUpdateSQLQuery(sql.toString(), params);
			}
			return true;
			
		case	5://REC.projectUiid!=DETAIL_REC.projectUiid for SYNC
			if(!GenericUtil.isEmpty(mt.get_tableChildList())){
				for(W5TableChild tc:mt.get_tableChildList()){
					W5Table dt = FrameworkCache.getTable(projectUuid, tc.getRelatedTableId());
					if(dt.getTableTip()==0)continue;
					
					sql.append("\n update ").append(dt.getDsc()).append(" d set project_uuid=? where d.project_uuid!=? AND exists(select 1 from iwb.w5_vcs_object v where v.vcs_object_status_tip=2 AND v.customization_id=? AND v.table_id=? AND v.table_pk=d.")
						.append(dt.get_tableParamList().get(0).getExpressionDsc()).append(") AND exists(select 1 from ").append(mt.getDsc())
						.append(" m where m.project_uuid=? AND m.").append(mt.get_tableFieldMap().get(tc.getTableFieldId()).getDsc()).append("=d.").append(dt.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc());
					params.add(projectUuid);params.add(projectUuid);params.add(customizationId);params.add(tc.getRelatedTableId());params.add(projectUuid);
					if(mt.get_tableParamList().size()>1){
						sql.append(" AND m.customization_id=?");
						params.add(customizationId);
					}
					sql.append(")");
					if(tc.getRelatedStaticTableFieldId()!=0){
						sql.append(" AND d.").append(dt.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc()).append("=").append(tc.getRelatedStaticTableFieldVal());
						
					}
					if(dt.get_tableParamList().size()>1){
						sql.append(" AND d.customization_id=?");
						params.add(customizationId);
					}
					
	//				List rl = dao.executeSQLQuery2("select "+tc.getRelatedTableId()+" tid, "+dt.get_tableParamList().get(0).getExpressionDsc()+" tpk from "+dt.getDsc()+" d " + sql.toString(), params);
	//				dao.executeUpdateSQLQuery(sql.toString(), params);
	
					//params = new ArrayList(); sql = new StringBuilder();
					sql.append(";\n update iwb.w5_vcs_object v set project_uuid=? where v.customization_id=? AND v.project_uuid!=? AND v.table_id=? AND v.vcs_object_status_tip=2 AND exists(select 1 from ").append(dt.getDsc())
						.append(" d where d.project_uuid=? AND d.").append(dt.get_tableParamList().get(0).getExpressionDsc()).append("=v.table_pk");
					params.add(projectUuid);params.add(projectUuid);params.add(customizationId);params.add(tc.getRelatedTableId());params.add(projectUuid);
					if(dt.get_tableParamList().size()>1){
						sql.append(" AND d.customization_id=?");
						params.add(customizationId);
					}
					sql.append(");");
					
				}
				dao.executeUpdateSQLQuery(sql.toString(), params);
				
				vcsClientPushSqlRT(scd, GenericUtil.replaceSql(sql.toString(), params), mt.getDsc() + " (REC.UUID != DETAIL_REC.UUID 4 SYNC)");
			}
			return true;
			
			
		}
		
		return false;		
	}

	public Map vcsClientObjectPullAll(Map<String, Object> scd, int forceStrategy, boolean commitOnException/*0:normal, 1:override conflicts, 2:override all*/) throws JSONException {
		
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);

		List lm = dao.find("select max(t.vcsCommitId) from W5VcsCommit t where t.projectUuid=?0", po.getProjectUuid());
		int lastCommitId = 0;
		W5VcsCommit commit = new W5VcsCommit();
		if(lm.isEmpty() || lm.get(0)==null)lastCommitId = 0;
		else lastCommitId = (Integer)lm.get(0);
		
		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&r="+po.getProjectUuid()+"&q=2770&l="+lastCommitId;
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSQueryResult";
		String dbS = HttpUtil.send(url, urlParameters);
		Map<Integer, JSONObject> dbPullMap = new HashMap();
		int startDbCommitId = -1;
		
		if(!GenericUtil.isEmpty(dbS)){
			JSONObject json;
			try {
				json = new JSONObject(dbS);
				if(json.get("success").toString().equals("true")){
					JSONArray ar = json.getJSONArray("data");
					for(int qi=0;qi<ar.length();qi++){
						JSONObject o = ar.getJSONObject(ar.length()-1-qi);
						if(o.has("extra_sql")){
							String extraSql = o.getString("extra_sql");
							if(!GenericUtil.isEmpty(extraSql)){
								//result+=dao.executeUpdateSQLQuery(extraSql);
								dbPullMap.put(o.getInt("vcs_commit_id"), o);
								if(startDbCommitId==-1)startDbCommitId = o.getInt("vcs_commit_id");
	//							continue;
							}
						}
//						dao.saveObject(new W5VcsCommit(o));
					}
				}	else
					throw new IWBException("vcs","vcsClientPullAll(DB):server Error Response", 0, dbS, json.has("error") ? json.getString("error"): json.toString(), null);

			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientPullAll(DB):JSONException", 0, url, "Error", e);
				
			}
		}
		
		
		urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&r="+po.getProjectUuid();
		url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSObjectsAll";
		String s = HttpUtil.send(url, urlParameters);
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			try {
				json = new JSONObject(s);
				if(json.get("success").toString().equals("true")){
					JSONObject srvTables =json.getJSONObject("list");
					List<W5VcsObject> lclObjects = dao.find("from W5VcsObject t where t.projectUuid=?0 AND t.customizationId=?1 order by t.tableId, t.tablePk", projectUuid, customizationId) ;
					Map<String, W5VcsObject> srcMap = new HashMap();
					for(W5VcsObject ox:lclObjects){
						srcMap.put(ox.getTableId()+"."+ox.getTablePk(), ox);
					}
					
					Iterator keyz = srvTables.keys();
					W5Table t = null;
					String ssql=null;
					while(keyz.hasNext()){
						int srvTableId = GenericUtil.uInt(keyz.next());
						if(t==null || t.getTableId()!=srvTableId){
							t = FrameworkCache.getTable(projectUuid, srvTableId);
//							if(GenericUtil.hasCustomization(t.get_tableParamList()))sql.append(" AND x.customization_id=").append(customizationId);
						}
						if(t.getVcsFlag()==0)continue;
						JSONArray srvObjects = srvTables.getJSONArray(srvTableId+"");
						for(int qi=0;qi<srvObjects.length();qi++){
							JSONObject o = srvObjects.getJSONObject(qi);
							int vcsAction = 0;
							int srvPk = GenericUtil.uInt(o.keys().next());
							int srvCommitId = GenericUtil.uInt(o.getString(srvPk+""));
							String pk = t.getTableId()+"."+srvPk;
							W5VcsObject lclObj = srcMap.get(pk);
							if(lclObj!=null){//server'da ve localde var
								if(lclObj.getVcsObjectStatusTip()==0) {
									srcMap.remove(pk);
									continue; // ignored object
								}
								if(srvCommitId<0){ //server'da silinmis, localde hala var
									if(lclObj.getVcsObjectStatusTip()==8){ //localde de silinmis, atla
										srcMap.remove(pk);
										continue;
									}

/*									if(GenericUtil.isEmpty(ll)){//boyle birsey olmamasi lazim normalde ama varsa, duzeltmek lazim
										lclObj.setVcsObjectStatusTip((short)8);
										dao.updateObject(lclObj);
										srcMap.remove(pk);
										continue;
									} */
									vcsAction = lclObj.getVcsObjectStatusTip()==1 ? 3:1;//edit edildiyse, conflict, aksi halde pull(delete)
								} else if(lclObj.getVcsObjectStatusTip()==3){ //localde silinmis, server'da var
//									od[1]=srvCommitId;//server vcsCommitId (+,-)
									vcsAction = lclObj.getVcsCommitId()==srvCommitId ? 2:3;//push:conflict
								} else if(lclObj.getVcsObjectStatusTip()==1){ //localde edit edilmis
									vcsAction = lclObj.getVcsCommitId()==srvCommitId ? 2:3;//push:conflict
								} else if(lclObj.getVcsObjectStatusTip()==9){ //localde synched, serverda edit edilmis
									if(srvCommitId==lclObj.getVcsCommitId()){
										srcMap.remove(pk);
										continue; //normalde olmasi lazim
									}
									vcsAction = 1;//pull				
								} else {
									vcsAction = 3;//conflict	
								}
							} else { //server'da var, localde yok
								if(srvCommitId<0){ //localde hic yokmus, atla
									srcMap.remove(pk);
									continue;
								}
								vcsAction = 1;//pull				
							}
							srcMap.remove(pk);
							
							if(forceStrategy>0 && vcsAction==3)vcsAction=1;
							else if(forceStrategy==2)vcsAction=1;
							
							if(vcsAction==1){
								if(!dbPullMap.isEmpty() && startDbCommitId!=-1 && srvCommitId>startDbCommitId){
									for(int zi=startDbCommitId;zi<srvCommitId;zi++){
										JSONObject oo = dbPullMap.get(zi);
										if(oo!=null){
											String extraSql = oo.getString("extra_sql");
											dao.executeUpdateSQLQuery(extraSql);
											dao.saveObject(new W5VcsCommit(oo));
										}
										startDbCommitId = zi;
									}
								}
								
								Map resM = vcsClientObjectPull(scd, srvTableId, srvPk, true);
								startDbCommitId = srvCommitId;
							}

						}

					}
					Map m = new HashMap();
					m.put("success", true);
					return m;
				} else
					throw new IWBException("vcs","vcsClientPullAll:server Error Response", 0, s, json.has("error") ? json.getString("error"): json.toString(), null);
			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientPullAll:JSONException", 0, url, "Error", e);
				
			}
		}
		throw new IWBException("vcs","vcsClientPullAll", 0, url, "No Response from VCS Server2", null);
		
	}

	public Map vcsClientLocaleMsgSynch(Map<String, Object> scd) throws JSONException {
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);

		List lvlmd = dao.executeSQLQuery("select x.val from iwb.w5_app_setting x where x.customization_id=? AND x.dsc='vcs_locale_msg_dttm'", customizationId);
		String vlmd = "20170101";
		if(!GenericUtil.isEmpty(lvlmd)){
			vlmd = (String)lvlmd.get(0);
			dao.executeUpdateSQLQuery("update iwb.w5_app_setting set val=to_char(current_date,'yyyymmdd'), version_no=version_no+1, version_dttm=current_timestamp, version_user_id=? where customization_id=? AND dsc='vcs_locale_msg_dttm'", scd.get("userId"), customizationId);
		} else {
			dao.executeUpdateSQLQuery("INSERT INTO iwb.w5_app_setting(dsc, customization_id, setting_tip, locale_msg_key, val, control_tip, not_null_flag, tab_order, control_width) "
					+ "VALUES ('vcs_locale_msg_dttm', ?, 2, 'vcs_locale_msg_dttm', to_char(current_date,'yyyymmdd'), 1, 1, 55, 150)", customizationId);
		}
		
		Map m = new HashMap();
		m.put("success", true);
		
		String urlParameters = "q=2800&u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&r="+po.getProjectUuid()+"&l="+vlmd;
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSQueryResult";
		String dbS = HttpUtil.send(url, urlParameters);
		List<Object[]> toPush = new ArrayList();
		
		if(!GenericUtil.isEmpty(dbS)){
			JSONObject json;
			try {
				json = new JSONObject(dbS);
				if(json.get("success").toString().equals("true")){
					Map requestParams = new HashMap(); requestParams.put("l", vlmd);
					W5QueryResult qr = dao.executeQuery(scd, 2800, requestParams);
					Map<String, Object[]> lclMap = new HashMap();
					if(!GenericUtil.isEmpty(qr.getData()))for(Object[] o:qr.getData()){
						lclMap.put((String)o[0], o);
					}

					JSONArray ar = json.getJSONArray("data");
					for(int qi=0;qi<ar.length();qi++){
						JSONObject o = ar.getJSONObject(qi);
						String lmk = o.getString("k");
						Object[] lo = lclMap.get(lmk);
						List<String> vx = null;
						if(lo!=null || (vx=dao.executeSQLQuery("select x.dsc from iwb.w5_locale_msg x where x.customization_id=? AND project_uuid=? AND x.locale=? AND x.locale_msg_key=?", customizationId, projectUuid, lmk.substring(0,2), lmk.substring(2)))!=null){ //demek iki yerde de var
							String lmv = o.getString("v");
							if(!lmv.equals(vx==null ? (String)lo[1] : vx.get(0))){ //farkli ise
								String lmt = o.getString("t");
								if(vx!=null || lmt.compareTo((String)lo[2])>0){
									String lcl = lmk.substring(0,2);
									String key = lmk.substring(2);
									dao.executeUpdateSQLQuery("update iwb.w5_locale_msg set dsc=?, version_no=version_no+1, version_dttm=to_timestamp(?,'yymmddhh24miss'), version_user_id=? where customization_id=? AND project_uuid=? AND locale=? AND locale_msg_key=?", lmv, lmt, o.getInt("u"), customizationId, projectUuid, lcl, key);
								} else {
									lo[3]=1;
									toPush.add(lo);
								}
							}
							lclMap.remove(lmk);
						} else { //insert edilecek local
							String lcl = lmk.substring(0,2);
							String key = lmk.substring(2);
							dao.executeUpdateSQLQuery("INSERT INTO iwb.w5_locale_msg(locale, locale_msg_key, dsc, version_no, version_user_id, version_dttm, insert_user_id, insert_dttm, publish_flag, customization_id, project_uuid) "
									+ " VALUES (?, ?, ?, 1, ?, current_timestamp, ?, to_timestamp(?,'yymmddhh24miss'), 0, ?, ?)", lcl, key, o.get("v"), o.getInt("u"), o.getInt("u"), o.getString("t"), customizationId, projectUuid);
						}
						
						
//						dao.saveObject(new W5VcsCommit(o));
					}
					
					for(Object[] lo:lclMap.values()){
						lo[3]=2;
						toPush.add(lo);
					}
					
					if(!toPush.isEmpty()){
						JSONArray data = new JSONArray();
						for(Object[] o:toPush){
							JSONObject jo = new JSONObject();
							jo.put("l", ((String)o[0]).substring(0, 2));
							jo.put("k", ((String)o[0]).substring(2));
							jo.put("v", o[1]);
							jo.put("t", o[2]);
							jo.put("a", o[3]);
							data.put(jo);
						}
						JSONObject params = new JSONObject(); 
						
						params.put("u", po.getVcsUserName());
						params.put("p", po.getVcsPassword());params.put("c", customizationId);params.put("r", po.getProjectUuid());
						params.put("objects", data);
						url=po.getVcsUrl();//"http://localhost:8080/q1/app/";//
						if(!url.endsWith("/"))url+="/";
						url+="serverVCSLocaleMsgPushAll";
						String s = HttpUtil.sendJson(url, params);
						if(!GenericUtil.isEmpty(s)){
							JSONObject json2;
							try {
								json2 = new JSONObject(s);
								if(!json2.get("success").toString().equals("true")){
								//	throw new PromisException("vcs","vcsClientLocaleMsgSynch:server Error Response", 0, s, json2.getString("error"), null);
									m.put("outMsg", "SERVER ERROR: "+json2.getString("error"));

								}
							} catch (JSONException e){
								throw new IWBException("vcs","vcsClientLocaleMsgSynch:JSONException", 0, s, "Error", e);
							}
						}
						else
							throw new IWBException("vcs","vcsClientLocaleMsgSynch:Server no Response", 0, s, "Server no Response", null);

					}
								
/*					
					String lcl = lmk.substring(0,2);
					String key = lmk.substring(2);
*/
				}	else
					throw new IWBException("vcs","vcsClientLocaleMsgSynch:server Error Response", 0, dbS, json.has("error") ? json.getString("error"): json.toString(), null);

			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientLocaleMsgSynch:JSONException", 0, url, "Error", e);
				
			}
		}

		return m;
		
	}
	synchronized public int vcsServerLocaleMsgPushAll(String userName, String passWord, int customizationId, String projectId, JSONArray ja) throws JSONException {
		if(!FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsServerLocaleMsgPushAll",0,null, "Not a VCS Server to vcsServerLocaleMsgPushAll", null);
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, projectId);
		W5Project po = FrameworkCache.getProject(projectId);

		int userId = (Integer)scd.get("userId");
		for(int qi=0;qi<ja.length();qi++){
			JSONObject o = ja.getJSONObject(qi);
			String lcl = o.getString("l");
			String key = o.getString("k");
			String val = o.getString("v");
			String tm = o.getString("t");
			if(GenericUtil.uInt(dao.executeSQLQuery("select count(1) from iwb.w5_locale_msg where locale=? AND locale_msg_key=? AND customization_id=? AND project_uuid=?", lcl, key, customizationId, projectId).get(0))==0)
				dao.executeUpdateSQLQuery("INSERT INTO iwb.w5_locale_msg(locale, locale_msg_key, dsc, version_no, version_user_id, version_dttm, insert_user_id, insert_dttm, publish_flag, customization_id, project_uuid) "
						+ " VALUES (?, ?, ?, 1, ?, current_timestamp, ?, to_timestamp(?,'yymmddhh24miss'), 0, ?, ?)", lcl, key, val, userId, userId, tm, customizationId, projectId);
			else
				dao.executeUpdateSQLQuery("update iwb.w5_locale_msg set dsc=?, version_no=version_no+1, version_dttm=to_timestamp(?,'yymmddhh24miss'), version_user_id=? where customization_id=? AND project_uuid=? AND locale=? AND locale_msg_key=?", val, tm, userId, customizationId, projectId, lcl, key);
		}

		return ja.length();
	}


	public Map vcsServerTenantCheck(int socialCon, String email, String nickName, String socialNet) {
		if(!FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsServerObjectPull",0,null, "Not a VCS Server to vcsServerObjectPull", null);
		List<Object[]> list = dao.executeSQLQuery("select u.customization_id, u.user_id from iwb.w5_user u  where u.email=? AND u.lkp_auth_external_source=?",email,socialCon);
		Map map = new HashMap();
		if(!GenericUtil.isEmpty(list)){
			Object[] obj = list.get(0);
			int cusId = GenericUtil.uInt(obj[0]);
			
			map.put("customizationId", cusId);
			map.put("userId", obj[1]);
			//List eparams = new ArrayList(); eparams.add(email);
			//List<Map> userList = dao.executeSQLQuery2Map("select t.* from iwb.w5_user t where t.email=?", eparams);
			//map.put("userList",userList);
			List params = new ArrayList(); params.add(cusId);
			List<Map> projectList = dao.executeSQLQuery2Map("select p.* from iwb.w5_project p where p.customization_id=?", params);
			map.put("projects", projectList);
			List<Map> tList = dao.executeSQLQuery2Map("select t.* from iwb.w5_user_tip t where t.customization_id=?", params);
			map.put("userTips", tList);
		} else {
			String projectId = UUID.randomUUID().toString();
			int cusId = GenericUtil.getGlobalNextval("iwb.seq_customization", projectId, 0, 0);
			dao.executeUpdateSQLQuery("insert into iwb.w5_customization(customization_id, dsc, sub_domain) values (?,?,?)", cusId, socialNet, nickName);
			
			String schema = "c"+GenericUtil.lPad(cusId+"", 5, '0')+"_"+projectId.replace('-', '_');
			int userId = GenericUtil.getGlobalNextval("iwb.seq_user", projectId, 0, cusId);

			String vcsUrl = FrameworkCache.getAppSettingStringValue(0, "vcs_url_new_project");
			dao.executeUpdateSQLQuery("insert into iwb.w5_project(project_uuid, customization_id, dsc, access_users, rdbms_schema, vcs_url, vcs_user_name, vcs_password, oproject_uuid)"
					+ " values (?,?,?, ?, ?, ?,?,?,?)", projectId, cusId, "Project Name #1", ""+userId,schema,vcsUrl,nickName, GenericUtil.getMd5Hash(nickName+1), projectId);
			dao.executeUpdateSQLQuery("create schema "+schema + " AUTHORIZATION iwb");
			int userTip = GenericUtil.getGlobalNextval("iwb.seq_user_tip", projectId, 0, cusId);
			dao.executeUpdateSQLQuery("insert into iwb.w5_user_tip(user_tip, dsc, customization_id, project_uuid, oproject_uuid, web_frontend_tip, default_main_template_id) values (?,?,?, ?, ?, 1, 1145)", userTip, "Role Group #1", cusId, projectId, projectId);
			Map newScd = new HashMap();newScd.put("projectId", projectId);newScd.put("customizationId", cusId);newScd.put("userId", userId);
			W5VcsObject vo = new W5VcsObject(newScd, 369, userTip);
			vo.setVcsObjectStatusTip((short)9);
			dao.saveObject(vo);

//			dao.executeUpdateSQLQuery("insert into iwb.w5_role(role_id, customization_id, dsc, user_tip, project_uuid) values (0,?,?,?,?)", cusId, "Role 1", userTip, projectId);
			
			dao.executeUpdateSQLQuery("insert into iwb.w5_user(user_id, customization_id, user_name, email, pass_word, user_status, dsc,login_rule_id, lkp_auth_external_source, auth_external_id, project_uuid) values (?,?,?,?,?,?,?,?,?,?,?)", 

					userId, cusId, nickName, email, GenericUtil.getMd5Hash(nickName+1), 1, nickName, 1 , socialCon, email,projectId);
//			int userRoleId = GenericUtil.getGlobalNextval("iwb.seq_user_role", projectId, 0, cusId);
//			dao.executeUpdateSQLQuery("insert into iwb.w5_user_role(user_role_id, user_id, role_id, customization_id,unit_id, project_uuid) values(?, ?, 0, ?,?, ?)",userRoleId, userId, cusId,0,projectId);

			
			map.put("projectList", false);
			map.put("customizationId", cusId);
			map.put("userId", userId);
			List params = new ArrayList(); params.add(cusId);
			List<Map> projectList = dao.executeSQLQuery2Map("select p.* from iwb.w5_project p where p.customization_id=?", params);
			map.put("projects", projectList);
			List<Map> tList = dao.executeSQLQuery2Map("select t.* from iwb.w5_user_tip t where t.customization_id=?", params);
			map.put("userTips", tList);
			
			FrameworkCache.wCustomizationMap.put(cusId, (W5Customization)dao.find("from W5Customization t where t.customizationId=?0", cusId).get(0));
			metadataLoader.addProject2Cache(projectId);
			FrameworkSetting.projectSystemStatus.put(projectId, 0);
			//Map cache = FrameworkCache.reloadCacheQueue();
		}
		return map;
	}

	public boolean vcsClientPublish2AppStore(Map<String, Object> scd) {
		if(FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsClientPublish2AppStore",0,null, "VCS Server not allowed to vcsClientPublish2AppStore", null);
		// request: previously published on VCS Server?
		// response: true, with lastCommitId
		//           false, not published yet
		
		// send: commit all SQL and Metadata, AND LocaleMsgs to VCS Server
		// response, newProjectUuid, response
		
		int customizationId = (Integer)scd.get("ocustomizationId");
		String projectId = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectId);
		if(customizationId!=po.getCustomizationId())
			throw new IWBException("vcs","vcsClientPublish2AppStore",0,null, "Only your projects", null);
		List<Object[]> cnts = dao.executeSQLQuery("select (select count(1) from iwb.w5_vcs_commit x where x.project_uuid=? AND x.vcs_commit_id<0),(select count(1) from iwb.w5_vcs_object x where x.project_uuid=? AND x.vcs_object_status_tip<5),(select x.project_uuid from iwb.w5_project x where x.oproject_uuid=? limit 1)", projectId, projectId, projectId);
		if(GenericUtil.uInt(cnts.get(0)[0])+GenericUtil.uInt(cnts.get(0)[1])>0)
			throw new IWBException("vcs","vcsClientPublish2AppStore",0,null, "The project must be synchronized before publishing to App Space (Metadata AND DB Scripts)", null);
		String newProjectId = null;
		if(GenericUtil.isEmpty(cnts.get(0)[2])) {
//			throw new IWBException("vcs","vcsClientPublish2AppStore",0,null, "The project already published", null);
			newProjectId = UUID.randomUUID().toString();
			String schema = "c"+GenericUtil.lPad("1", 5, '0')+"_"+newProjectId.replace('-', '_');
			dao.executeUpdateSQLQuery("insert into iwb.w5_project(project_uuid, customization_id, dsc, project_status_tip, rdbms_schema, vcs_url, vcs_user_name, vcs_password, oproject_uuid, "
					+ " ui_web_frontend_tip, ui_main_template_id, session_query_id, authentication_func_id, ui_login_template_id, insert_user_id)"
					+ " values (?,1,?, ?, ?,?,?,?, ?,"
					+ "?, ?, ?, ?, ?, ?)", newProjectId, po.getDsc(), 0, schema, "http://34.68.231.169/app/","app.store", "1", projectId
					, po.getUiWebFrontendTip(), po.getUiMainTemplateId(), po.getSessionQueryId(), po.getAuthenticationFuncId(), po.getUiLoginTemplateId(), scd.get("userId"));
			dao.executeUpdateSQLQuery("create schema "+schema + " AUTHORIZATION iwb");
		} else { 
			newProjectId = cnts.get(0)[2].toString();
			dao.executeUpdateSQLQuery("update iwb.w5_project"
					+ " set dsc=?,ui_web_frontend_tip=?, ui_main_template_id=?, session_query_id=?, authentication_func_id=?, ui_login_template_id=?, project_status_tip=0"
					+ " where project_uuid=?"
					, po.getDsc(), po.getUiWebFrontendTip(), po.getUiMainTemplateId(), po.getSessionQueryId(), po.getAuthenticationFuncId(), po.getUiLoginTemplateId(),
					newProjectId);
		}
		metadataWriter.copyProject(scd, newProjectId, 1);
		metadataLoader.addProject2Cache(newProjectId);
		FrameworkSetting.projectSystemStatus.put(newProjectId, 0);
		return true;
/*
		List<Object[]> ll = dao.executeSQLQuery("select (select max(x.vcs_commit_id) from iwb.w5_vcs_commit x where x.project_uuid=?) cmt_id1, (select min(x.vcs_commit_id) from iwb.w5_vcs_commit x where x.project_uuid=?) cmt_id2, (select max(x.vcs_commit_id) from iwb.w5_vcs_object x where x.vcs_object_status_tip>5 AND x.project_uuid=? and x.customization_id=?) cmt_id3, (select count(x.vcs_commit_id) from iwb.w5_vcs_object x where x.vcs_object_status_tip<5 AND x.project_uuid=? and x.customization_id=?) cmt_id4"
				, po.getProjectUuid(), po.getProjectUuid(), po.getProjectUuid(), customizationId, po.getProjectUuid(), customizationId);
		int maxSqlCommit = GenericUtil.uInt(ll.get(0)[0]), minSqlCommit = GenericUtil.uInt(ll.get(0)[1])
				, maxObjCommit = GenericUtil.uInt(ll.get(0)[2]),  cntObjToCommit = GenericUtil.uInt(ll.get(0)[3]);
		if(maxSqlCommit<=0 || maxObjCommit<=0)
			throw new IWBException("vcs","vcsClientPublish2AppStore", 0, null, "You have not pushed anything to VCSServer yet", null);
		if(minSqlCommit<=0 || cntObjToCommit>0)
			throw new IWBException("vcs","vcsClientPublish2AppStore", 0, null, "You have objects OR SQLs to push", null);
		
		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&r="+po.getProjectUuid();
		
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverPublish2AppStore";
		String s = HttpUtil.send(url, urlParameters+"&cmt1="+maxSqlCommit+"&cmt2="+maxObjCommit);	
		if(!GenericUtil.isEmpty(s))try{
			JSONObject json;
			json = new JSONObject(s);
			if(json.get("success").toString().equals("true")){
				return true;
			}
		} catch (Exception e){
			throw new IWBException("vcs","vcsClientPublish2AppStore:serverError", 0, s, "VCS Server Error", e);
			
		}		*/
	}
	/*
	public Map copyProject(Map scd, String projectName, boolean appStore){
		String projectId = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectId), npo = null;
		int customizationId = (Integer)scd.get("customizationId");
		int dstCustomizationId = appStore ? 1:customizationId;
		int smaxSqlCommit = 0;
		Map result = new HashMap();
		result.put("success", true);
		String newProjectId = UUID.randomUUID().toString();
		String vcsUrl = FrameworkCache.getAppSettingStringValue(0, "vcs_url_new_project");
		String schema = "c"+GenericUtil.lPad(dstCustomizationId+"", 5, '0')+"_"+newProjectId.replace('-', '_');
		dao.executeUpdateSQLQuery("insert into iwb.w5_project(project_uuid, customization_id, dsc, project_status_tip, rdbms_schema, vcs_url, vcs_user_name, vcs_password, oproject_uuid)"
				+ " values (?,?,?, ?, ?,?,?,?, ?)", newProjectId, dstCustomizationId, projectName, 1, schema, appStore?vcsUrl:po.getVcsUrl(),appStore?"app.store":po.getVcsUserName(), appStore?"1":po.getVcsPassword(), projectId);
		if(!appStore){
			dao.executeUpdateSQLQuery("create schema "+schema + " AUTHORIZATION iwb");
			dao.executeUpdateSQLQuery("set search_path="+schema);
		}
		npo =  (W5Project)dao.find("from W5Project w where w.projectUuid=?", newProjectId).get(0);
		
		List<W5VcsCommit> sqlCommits = dao.find("from W5VcsCommit t where t.commitTip=2 AND t.projectUuid=? AND t.vcsCommitId>? order by t.vcsCommitId", projectId, smaxSqlCommit);
		for(W5VcsCommit o:sqlCommits){
			dao.saveObject(o.newInstance(newProjectId));
			if(!appStore)dao.executeUpdateSQLQuery(o.getExtraSql());
		}
		
		dao.executeUpdateSQLQuery("delete from iwb.w5_vcs_object x where x.project_uuid=? AND x.customization_id=? AND not exists(select 1 from iwb.w5_vcs_object z where z.project_uuid=? AND z.customization_id=? AND z.table_id=x.table_id AND z.table_pk=x.table_pk AND z.vcs_commit_id=x.vcs_commit_id)"
				, newProjectId, dstCustomizationId, projectId, customizationId);
		
		dao.executeUpdateSQLQuery("INSERT INTO iwb.w5_vcs_object(vcs_object_id, table_id, table_pk, customization_id, project_uuid, vcs_commit_id, vcs_commit_record_hash, vcs_object_status_tip) "
				+ "select nextval('iwb.seq_vcs_object'), x.table_id, x.table_pk, ?, ?, x.vcs_commit_id, x.vcs_commit_record_hash, x.vcs_object_status_tip from iwb.w5_vcs_object x where x.project_uuid=? AND x.customization_id=? AND not exists(select 1 from iwb.w5_vcs_object z where z.project_uuid=? AND z.customization_id=? AND z.table_id=x.table_id AND z.table_pk=x.table_pk AND z.vcs_commit_id=x.vcs_commit_id)"
				, dstCustomizationId, newProjectId, projectId, customizationId, newProjectId, dstCustomizationId);
		
		List<Object> ll3 = dao.executeSQLQuery("select x.table_id from iwb.w5_vcs_object x where x.project_uuid=? group by x.table_id order by x.table_id", newProjectId);
		if(ll3!=null)for(Object o:ll3){
			StringBuilder sql = new StringBuilder();
			W5Table t = FrameworkCache.getTable(0, GenericUtil.uInt(o));
			String pkField = t.get_tableParamList().get(0).getExpressionDsc();
			sql.append("delete from ").append(t.getDsc()).append(" x where x.customization_id=? AND x.project_uuid=? AND not exists(select 1 from ").append(t.getDsc())
				.append(" z where z.project_uuid=? AND z.customization_id=? AND z.").append(pkField).append("=x.").append(pkField);
			for(W5TableField tf:t.get_tableFieldList())if(tf.getTabOrder()>1 && !GenericUtil.hasPartInside2("customization_id,project_uuid,oproject_uuid,version_no,insert_user_id,version_user_id,insert_dttm,version_dttm", tf.getDsc())){
				sql.append(" AND x.").append(tf.getDsc()).append("=z.").append(tf.getDsc());
			}
			sql.append(")");
			dao.executeUpdateSQLQuery(sql.toString(), dstCustomizationId, newProjectId, projectId, customizationId);
			
			sql.setLength(0);
			StringBuilder sql2 = new StringBuilder();
			sql.append("insert into ").append(t.getDsc()).append("(");
			for(W5TableField tf:t.get_tableFieldList())if(!GenericUtil.hasPartInside2("customization_id,project_uuid,oproject_uuid,version_no,insert_user_id,version_user_id,insert_dttm,version_dttm", tf.getDsc())){
				sql.append(tf.getDsc()).append(",");
				sql2.append(tf.getDsc()).append(",");
			}
			sql.append("customization_id,project_uuid,oproject_uuid)");
			sql2.append("?,?,?");
			sql.append(" select ").append(sql2).append(" from ").append(t.getDsc()).append(" x where x.customization_id=? AND x.project_uuid=? AND not exists(select 1 from ").append(t.getDsc())
				.append(" z where z.customization_id=? AND z.project_uuid=? AND z.").append(pkField).append("=x.").append(pkField).append(")");
			dao.executeUpdateSQLQuery(sql.toString(), dstCustomizationId, newProjectId, projectId, customizationId, projectId, dstCustomizationId, newProjectId);
		}
		
		FrameworkCache.addProject(npo);
		FrameworkSetting.projectSystemStatus.put(npo.getProjectUuid(), 0);

		return result;
		
	}
	*/
	public Map vcsServerPublish2AppStore(String userName, String passWord, int customizationId, String projectId, int maxSqlCommit, int maxObjCommit) {
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, null);
		W5Project po = FrameworkCache.getProject(projectId), npo = null;

		List<Object[]> ll = dao.executeSQLQuery("select (select max(x.vcs_commit_id) from iwb.w5_vcs_commit x where x.project_uuid=?) cmt_id1, (select max(x.vcs_commit_id) from iwb.w5_vcs_object x where x.vcs_object_status_tip>5 AND x.project_uuid=? and x.customization_id=?) cmt_id2", po.getProjectUuid(), po.getProjectUuid(), customizationId);
		int smaxSqlCommit = GenericUtil.uInt(ll.get(0)[0]), smaxObjCommit = GenericUtil.uInt(ll.get(0)[1]);
		if(smaxSqlCommit!=maxSqlCommit || smaxObjCommit!=maxObjCommit)
			throw new IWBException("vcs","serverPublish2AppStore", 0, projectId, "Not synched Project. Synch SQL and Metadata first", null);
		
		Map result = new HashMap();
		result.put("success", true);
		List<Object[]> ll2 = dao.executeSQLQuery("select x.project_uuid, x.dsc, x.project_status_tip  from iwb.w5_project x where x.customization_id=1 AND x.oproject_uuid=?", projectId);
		String newProjectId = null;
		if(GenericUtil.isEmpty(ll2)){ // not defined
			newProjectId = UUID.randomUUID().toString();
			String vcsUrl = FrameworkCache.getAppSettingStringValue(0, "vcs_url_new_project");
			String schema = "c"+GenericUtil.lPad("1", 5, '0')+"_"+newProjectId.replace('-', '_');
			dao.executeUpdateSQLQuery("insert into iwb.w5_project(project_uuid, customization_id, dsc, project_status_tip, rdbms_schema, vcs_url, vcs_user_name, vcs_password, oproject_uuid)"
					+ " values (?,?,?, ?, ?,?,?,?, ?)", newProjectId, 1, "App on Store", schema, vcsUrl,"app.store", "1", projectId);
			//dao.executeUpdateSQLQuery("create schema "+schema + " AUTHORIZATION iwb");
			npo =  (W5Project)dao.find("from W5Project w where w.projectUuid=?0", newProjectId).get(0);
		} else {
			Object[] oo = ll2.get(0);
			int projectStatus= GenericUtil.uInt(oo[2]);
			if(projectStatus!=1){
				throw new IWBException("vcs","serverPublish2AppStore", 0, projectId, "Project Status Error", null);
			}
			newProjectId = oo[0].toString();
			npo = FrameworkCache.getProject(newProjectId);
		}
		if(npo==null)
			throw new IWBException("vcs","serverPublish2AppStore", 0, projectId, "Project Not Found", null);
		
		List<W5VcsCommit> sqlCommits = dao.find("from W5VcsCommit t where t.commitTip=2 AND t.projectUuid=?0 AND t.vcsCommitId>?1 order by t.vcsCommitId", projectId, smaxSqlCommit);
		for(W5VcsCommit o:sqlCommits)dao.saveObject(o.newInstance(newProjectId));
		
		dao.executeUpdateSQLQuery("delete from iwb.w5_vcs_object x where x.project_uuid=? AND x.customization_id=1 AND not exists(select 1 from iwb.w5_vcs_object z where z.project_uuid=? AND z.customization_id=? AND z.table_id=x.table_id AND z.table_pk=x.table_pk AND z.vcs_commit_id=x.vcs_commit_id)", newProjectId, projectId, customizationId);
		
		dao.executeUpdateSQLQuery("INSERT INTO iwb.w5_vcs_object(vcs_object_id, table_id, table_pk, customization_id, project_uuid, vcs_commit_id, vcs_commit_record_hash, vcs_object_status_tip) "
				+ "select nextval('iwb.seq_vcs_object'), x.table_id, x.table_pk, 1, ?, x.vcs_commit_id, x.vcs_commit_record_hash, x.vcs_object_status_tip from iwb.w5_vcs_object x where x.project_uuid=? AND x.customization_id=? AND not exists(select 1 from iwb.w5_vcs_object z where z.project_uuid=? AND z.customization_id=1 AND z.table_id=x.table_id AND z.table_pk=x.table_pk AND z.vcs_commit_id=x.vcs_commit_id)"
				, newProjectId, projectId, customizationId, newProjectId);
		
		List<Object> ll3 = dao.executeSQLQuery("select x.table_id from iwb.w5_vcs_object x where x.project_uuid=? group by x.table_id order by x.table_id", newProjectId);
		if(ll3!=null)for(Object o:ll3){
			StringBuilder sql = new StringBuilder();
			W5Table t = FrameworkCache.getTable(0, GenericUtil.uInt(o));
			String pkField = t.get_tableParamList().get(0).getExpressionDsc();
			sql.append("delete from ").append(t.getDsc()).append(" x where x.customization_id=1 AND x.project_uuid=? AND not exists(select 1 from ").append(t.getDsc())
				.append(" z where z.project_uuid=? AND z.customization_id=? AND z.").append(pkField).append("=x.").append(pkField);
			for(W5TableField tf:t.get_tableFieldList())if(tf.getTabOrder()>1 && !GenericUtil.hasPartInside2("customization_id,project_uuid,oproject_uuid,version_no,insert_user_id,version_user_id,insert_dttm,version_dttm", tf.getDsc())){
				sql.append(" AND x.").append(tf.getDsc()).append("=z.").append(tf.getDsc());
			}
			sql.append(")");
			dao.executeUpdateSQLQuery(sql.toString(), newProjectId, projectId, customizationId);
			
			sql.setLength(0);
			StringBuilder sql2 = new StringBuilder();sql2.append(" select ");
			sql.append("insert into ").append(t.getDsc()).append("(");
			for(W5TableField tf:t.get_tableFieldList())if(!GenericUtil.hasPartInside2("customization_id,project_uuid,oproject_uuid,version_no,insert_user_id,version_user_id,insert_dttm,version_dttm", tf.getDsc())){
				sql.append(tf.getDsc()).append(",");
				sql2.append(tf.getDsc()).append(",");
			}
			sql.append("customization_id,project_uuid,oproject_uuid)");
			sql2.append("1,?,?");
			sql.append(" select ").append(sql2).append(" from ").append(t.getDsc()).append(" x where x.customization_id=? AND x.project_uuid=? AND not exists(select 1 from ").append(t.getDsc())
				.append(" z where z.project_uuid=? AND z.customization_id=? AND z.").append(pkField).append("=x.").append(pkField).append(")");
			dao.executeUpdateSQLQuery(sql.toString(), newProjectId, projectId, customizationId);
		}

/*
		List<W5VcsObject> vcsObjects = dao.find("from W5VcsObject t where t.project_uuid=? AND t.vcsCommitId>? order by t.vcsCommitId, t.tableId, t.tablePk", projectId, smaxObjCommit);
		Set<Integer> tableIds = new HashSet<>();
		for(W5VcsObject o:vcsObjects){
			if(!tableIds.contains(o.getTableId())){// delete, update, insert
				StringBuilder sql = new StringBuilder();
				W5Table t = FrameworkCache.getTable(0, o.getTableId());
				String pkField = t.get_tableParamList().get(0).getExpressionDsc();
				sql.append("delete from ").append(t.getDsc()).append(" t where t.customization_id=1 AND t.projectUuid=? AND not exists(select 1 from ").append(t.getDsc())
					.append(" q where t.projectUuid=? AND q.customization_id=? AND q.").append(pkField).append("=t.").append(pkField);
				dao.executeUpdateSQLQuery(sql.toString(), newProjectId, projectId, customizationId);

				sql.setLength(0);
				sql.append("select t.*,(select 1 from ").append(t.getDsc())
					.append(" q where t.projectUuid=? AND q.customization_id=1 AND q.").append(pkField).append("=t.").append(pkField).append(") icb_bunny_diff from ").append(t.getDsc()).append(" t where t.projectUuid=? AND t.customization_id=?");

				List params = new ArrayList();
				params.add(newProjectId);params.add(projectId);params.add(customizationId);
				List<Map> lmo =dao.executeSQLQuery2Map(sql.toString(), params);
				if(lmo!=null)for(Map mo:lmo){
					params.clear();
					sql.setLength(0);

					switch(GenericUtil.uInt(mo.get("icb_bunny_diff"))){
					case	1://already have: update
						sql.append("update ").append(t.getDsc()).append(" t set ");
						for(W5TableField tf:t.get_tableFieldList())if(tf.getTabOrder()>1 && !GenericUtil.hasPartInside2("version_no,insert_user_id,version_user_id,insert_dttm,version_dttm", tf.getDsc())){
							sql.append(tf.getDsc()).append("=?,");
							params.add(mo.get(tf.getDsc()));
						}
						sql.setLength(sql.length()-1);
						sql.append(" where t.customization_id=1 AND t.projectUuid=? AND t.").append(pkField).append("=?");
						params.add(mo.get(pkField));
						break;
					case	0://does no: insert
						sql.append("insert into ").append(t.getDsc()).append("(");
						for(W5TableField tf:t.get_tableFieldList())if(tf.getTabOrder()>1 && !GenericUtil.hasPartInside2("version_no,insert_user_id,version_user_id,insert_dttm,version_dttm", tf.getDsc())){
							sql.append(tf.getDsc()).append("=?,");
							params.add(mo.get(tf.getDsc()));
						}
						sql.setLength(sql.length()-1);
						sql.append(" where t.customization_id=1 AND t.projectUuid=? AND t.").append(pkField).append("=?");
						params.add(mo.get(pkField));
						break;
	
					}
					dao.executeUpdateSQLQuery(sql.toString(), params);
				}
				
				
			} else tableIds.add(o.getTableId());
			dao.saveObject(o.newInstance(1, newProjectId));
		}

		*/
		if(GenericUtil.isEmpty(ll2)){
			metadataLoader.addProject2Cache(npo.getProjectUuid());
			FrameworkSetting.projectSystemStatus.put(npo.getProjectUuid(), 0);
		}
				
		return result;
	}
	
	public boolean vcsClientCopyObjects(Map<String, Object> scd, String dstProjectId, int tableId, int tablePk) {
		W5Project srcPo = FrameworkCache.getProject(scd);
		W5Project dstPo = FrameworkCache.getProject(dstProjectId);
		W5Table t = FrameworkCache.getTable(scd, tableId);
		if(dstPo==null || t==null || srcPo.getProjectUuid().equals(dstProjectId))return false;
		Map dstScd = new HashMap();dstScd.putAll(scd);dstScd.put("projectId", dstProjectId);dstScd.put("customizationId", dstPo.getCustomizationId());

		Map<String, String> srcMap = new HashMap();
		dao.findRecordChildRecords4Copy(srcMap, scd, tableId, tablePk);

		Map<String, String> dstMap = new HashMap();
		dao.findRecordChildRecords4Copy(dstMap, dstScd, tableId, tablePk);
		for(String keyz:srcMap.keySet()) {
			String srcHash = srcMap.get(keyz);
			String dstHash = dstMap.get(keyz);
			if(GenericUtil.safeEquals(srcHash, dstHash))continue;
			if(dstHash!=null)continue;//TODO
			
			int ix = keyz.indexOf('.');
			int tt = GenericUtil.uInt(keyz.substring(0, ix));
			int kk = GenericUtil.uInt(keyz.substring(ix+1));
			
			dao.copyTableRecord4VCS(scd, dstScd, tt, kk);
			
		}
		
		return true;
		
	}
	//4147a129-06ad-4983-9b1c-8e88826454ac rbac project
	public boolean vcsClientImportProject(Map<String, Object> scd, String projectId, String importedProjectId) {
		Map<String, Object> newScd = new HashMap();
		newScd.putAll(scd);newScd.put("projectId", importedProjectId);
		boolean b = metadataWriter.copyProject(newScd, projectId, (Integer)newScd.get("customizationId"));
		if(b){
			if(importedProjectId.equals("4147a129-06ad-4983-9b1c-8e88826454ac")){
				dao.executeUpdateSQLQuery("update iwb.w5_project set ui_login_template_id=2590, session_query_id=4514,authentication_func_id=1252 where project_uuid=?", projectId);
				metadataLoader.addProject2Cache(projectId);
				metadataLoader.reloadProjectCaches(projectId);
				List<Object[]> zz= dao.executeSQLQuery("select (select p.role_group_id from x_role p where p.role_group_id!=122) x1,(select max(t.user_tip) from iwb.w5_user_tip t where t.user_tip!=122 AND t.project_uuid=?) x2", projectId);
				if(GenericUtil.uInt(zz.get(0)[0])==0){
					int roleId = GenericUtil.uInt(dao.executeSQLQuery("select nextval('seq_role')").get(0));
					dao.executeUpdateSQLQuery("insert into x_role(role_id, role_name, role_group_id) values (?,?,?)", roleId, "Role "+System.currentTimeMillis(), GenericUtil.uInt(zz.get(0)[1]));
					dao.executeUpdateSQLQuery("INSERT INTO x_user_role(user_role_id, user_id, role_id)select nextval('seq_user_role'), user_id, ? from x_user",roleId);

				}

			}			
		}
		return b;
	}
	
	public String vcsClientDeleteSubProject(Map<String, Object> scd, String projectId, String subProjectId) {
		Map<String, Object> newScd = new HashMap();
		newScd.putAll(scd);newScd.put("projectId", projectId);
		String b = dao.deleteSubProject(newScd, subProjectId);
		if(b==null){
			if(subProjectId.equals("4147a129-06ad-4983-9b1c-8e88826454ac")){
				dao.executeUpdateSQLQuery("update iwb.w5_project set ui_login_template_id=0, session_query_id=0,authentication_func_id=0 where project_uuid=?", projectId);
				metadataLoader.addProject2Cache(projectId);
				metadataLoader.reloadProjectCaches(projectId);				
			}
			
		}

		return b;
	}
	
	// if newProjectId is null, then all projects will be syncronized
	public Map vcsClientProjectSynch(Map<String, Object> scd, String newProjectId) {
		int customizationId = (Integer)scd.get("ocustomizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid), npo = null;
		if(!GenericUtil.isEmpty(newProjectId)){
			npo = metadataLoader.loadProject(newProjectId);
			if(npo!=null){
				if(npo.getCustomizationId()!=customizationId){
					if(customizationId==0) { //ifdeveloper access to everything
						Map newScd = new HashMap();
						newScd.putAll(scd);
						newScd.put("customizationId", npo.getCustomizationId());
						customizationId = npo.getCustomizationId();
					} else
						throw new IWBException("vcs","vcsClientProjectSynch:Access Control", 0, null, "Forbiedd Project", null);
				}
			}
		}

		Map result = new HashMap();
		result.put("success", false);
		try {
			JSONObject params = new JSONObject(); 
			
			params.put("u", po.getVcsUserName());
			params.put("p", po.getVcsPassword());params.put("c", customizationId);params.put("r", po.getProjectUuid());
			
			List<Object> p = new ArrayList(); p.add(npo!=null ? npo.getProjectUuid() : customizationId);
			List<Map> lm = GenericUtil.isEmpty(newProjectId) ? dao.executeSQLQuery2Map("select * from iwb.w5_project x where x.customization_id=?", p) 
					: (npo!=null ? dao.executeSQLQuery2Map("select * from iwb.w5_project x where x.project_uuid=?", p) : null);
			JSONArray data = new JSONArray();
			if(lm!=null)for(Map m:lm)data.put(GenericUtil.fromMapToJSONObject(m));
			params.put("objects", data);
			String url=po.getVcsUrl();//"http://localhost:8080/q1/app/";//
			if(!url.endsWith("/"))url+="/";
			url+="serverVCSProjectSynch";
			String s = HttpUtil.sendJson(url, params);
	
			if(!GenericUtil.isEmpty(s)){
				JSONObject json;
				try {
					json = new JSONObject(s);
					if(json.get("success").toString().equals("true")){
						JSONArray serverProjects = json.getJSONArray("data");
						if(npo==null && serverProjects!=null && serverProjects.length()>0)for(int qi=0;qi< serverProjects.length();qi++){
							JSONObject prj = serverProjects.getJSONObject(qi);
							if(!GenericUtil.isEmpty(newProjectId) && !prj.getString("project_uuid").equals(newProjectId))continue;
							dao.executeUpdateSQLQuery("insert into iwb.w5_project(project_uuid, customization_id, dsc, access_users,  rdbms_schema, vcs_url, vcs_user_name, vcs_password, oproject_uuid)"
									+ " values (?,?,?, ?, ?,?,?,?, ?)", prj.getString("project_uuid"), customizationId, prj.getString("dsc"), GenericUtil.getSafeObject(prj,"access_users"),prj.getString("rdbms_schema"),GenericUtil.getSafeObject(prj,"vcs_url"),GenericUtil.getSafeObject(prj,"vcs_user_name"), GenericUtil.getSafeObject(prj,"vcs_password"), prj.getString("oproject_uuid"));
							dao.executeUpdateSQLQuery("create schema if not exists "+prj.getString("rdbms_schema") + " AUTHORIZATION iwb");
							FrameworkCache.addProject((W5Project)dao.find("from W5Project t where t.customizationId=?0 AND t.projectUuid=?1", customizationId, prj.getString("project_uuid")).get(0));
							FrameworkSetting.projectSystemStatus.put(prj.getString("project_uuid"), 0);
						}
						result.put("success", true);
						
					} else if(json.get("success").toString().equals("false"))
						return GenericUtil.fromJSONObjectToMap(json);
				} catch (JSONException e){
					throw new IWBException("vcs","vcsClientProjectSynch:JSONException", 0, s, "Error", e);
				}
			} else 
				throw new IWBException("vcs","serverVCSProjectSynch:server No response", 0, s, "Error", null);
		} catch (JSONException e){
			throw new IWBException("vcs","vcsClientProjectSynch2:JSONException", 0, null, "Error", e);
		}
		return result;
	}
	
	public Map vcsClientProjectFetch(Map<String, Object> scd, String newProjectId) {
		Map result = new HashMap();
		result.put("success", false);
		int customizationId = (Integer)scd.get("ocustomizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid), npo = null;
		npo = metadataLoader.loadProject(newProjectId);
		if(npo!=null){
			return result;
		}

		try {
			JSONObject params = new JSONObject(); 
			
			params.put("u", po.getVcsUserName());
			params.put("p", po.getVcsPassword());params.put("c", customizationId);params.put("r", po.getProjectUuid());
			params.put("nr", newProjectId);
			

			String url=po.getVcsUrl();//"http://localhost:8080/q1/app/";//
			if(!url.endsWith("/"))url+="/";
			url+="serverVCSProjectFetch";
			String s = HttpUtil.sendJson(url, params);
	
			if(!GenericUtil.isEmpty(s)){
				JSONObject json;
				try {
					json = new JSONObject(s);
					if(json.get("success").toString().equals("true") && json.has("customization") && json.has("project")){
						JSONObject cus = json.getJSONObject("customization");
						int cusId = cus.getInt("customization_id");
						if(dao.executeSQLQuery("select 1 from iwb.w5_customization x where x.customization_id=?", cusId)==null) {
							dao.executeUpdateSQLQuery("insert into iwb.w5_customization(customization_id, dsc, sub_domain) values (?,?,?)", cusId,
									cus.getString("dsc"), cus.getString("sub_domain"));
							FrameworkCache.wCustomizationMap.put(cusId,
									(W5Customization) dao.find("from W5Customization t where t.customizationId=?", cusId).get(0));

						}

						
						JSONObject prj = json.getJSONObject("project");
						if(dao.executeSQLQuery("select 1 from iwb.w5_project x where x.project_uuid=?", newProjectId)==null) {
							dao.executeUpdateSQLQuery("insert into iwb.w5_project(project_uuid, customization_id, dsc, access_users,  rdbms_schema, vcs_url, vcs_user_name, vcs_password, oproject_uuid, ui_web_frontend_tip, ui_main_template_id, ui_login_template_id, session_query_id, authentication_func_id)"
									+ " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", prj.getString("project_uuid"), cusId, prj.getString("dsc"), GenericUtil.getSafeObject(prj,"access_users"),prj.getString("rdbms_schema"),GenericUtil.getSafeObject(prj,"vcs_url"),GenericUtil.getSafeObject(prj,"vcs_user_name"), GenericUtil.getSafeObject(prj,"vcs_password"), prj.getString("oproject_uuid"), 
									GenericUtil.uInt(prj,"ui_web_frontend_tip"), 2307/*GenericUtil.uInt(prj,"ui_main_template_id")*/, 2590/*GenericUtil.uInt(prj,"ui_login_template_id")*/, 4514/*GenericUtil.uInt(prj,"session_query_id")*/, 1252/*GenericUtil.uInt(prj,"authentication_func_id")*/);
							if(!prj.getString("rdbms_schema").equals("iwb"))dao.executeUpdateSQLQuery("drop schema if exists "+prj.getString("rdbms_schema")+ " cascade");
							dao.executeUpdateSQLQuery("create schema "+prj.getString("rdbms_schema") + " AUTHORIZATION iwb");
							FrameworkCache.addProject((W5Project)dao.find("from W5Project t where t.customizationId=?0 AND t.projectUuid=?1", cusId, prj.getString("project_uuid")).get(0));
							FrameworkSetting.projectSystemStatus.put(prj.getString("project_uuid"), 0);
						}
						
						result.put("success", true);
						
					} else if(json.get("success").toString().equals("false"))
						return GenericUtil.fromJSONObjectToMap(json);
				} catch (JSONException e){
					throw new IWBException("vcs","vcsClientProjectSynch:JSONException", 0, s, "Error", e);
				}
			} else 
				throw new IWBException("vcs","serverVCSProjectSynch:server No response", 0, s, "Error", null);
		} catch (JSONException e){
			throw new IWBException("vcs","vcsClientProjectSynch2:JSONException", 0, null, "Error", e);
		}
		return result;
	}
	
	public Map vcsServerProjectSynch(String userName, String passWord, int customizationId, String projectId, JSONArray ja) {
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, projectId);
		W5Project po = FrameworkCache.getProject(projectId);

		Map m = new HashMap();
		m.put("success", true);
		try {
			List l = new ArrayList();
			m.put("data", l);
			Set<String> set = new HashSet();
			for(int qi=0;qi<ja.length();qi++){
				JSONObject prj = ja.getJSONObject(qi);
				set.add(prj.getString("project_uuid"));
				W5Project srvPrj = FrameworkCache.getProject(prj.getString("project_uuid"));
				if(srvPrj==null){ //insert here
					dao.executeUpdateSQLQuery("insert into iwb.w5_project(project_uuid, customization_id, dsc, access_users,  rdbms_schema, vcs_url, vcs_user_name, vcs_password, oproject_uuid)"
							+ " values (?,?,?, ?, ?,?,?,?, ?)", prj.getString("project_uuid"), customizationId, prj.getString("dsc"), GenericUtil.getSafeObject(prj,"access_users"),prj.getString("rdbms_schema"),GenericUtil.getSafeObject(prj,"vcs_url"),GenericUtil.getSafeObject(prj,"vcs_user_name"), GenericUtil.getSafeObject(prj,"vcs_password"), prj.getString("oproject_uuid"));
					if(customizationId==0 || FrameworkCache.getAppSettingIntValue(0, "run_global_sql")!=0)dao.executeUpdateSQLQuery("create schema "+prj.getString("rdbms_schema") + " AUTHORIZATION iwb");
					FrameworkCache.addProject((W5Project)dao.find("from W5Project t where t.customizationId=?0 AND t.projectUuid=?1", customizationId, prj.getString("project_uuid")).get(0));
					FrameworkSetting.projectSystemStatus.put(prj.getString("project_uuid"), 0);
				}
			}
			List<Object> p = new ArrayList(); p.add(customizationId);
			List<Map> lm = dao.executeSQLQuery2Map("select * from iwb.w5_project x where x.customization_id=?", p);
			for(Map mo:lm)if(!set.contains((String)mo.get("project_uuid")))l.add(mo);
			if(l.isEmpty() && customizationId==0) {
				p.clear();
				p.add(projectId);
				l.addAll(dao.executeSQLQuery2Map("select * from iwb.w5_project x where x.project_uuid=?", p));
			}
			
		} catch (JSONException e){
			throw new IWBException("vcs","vcsServerProjectSynch:JSONException", 0, null, "Error", e);
		}
		return m;
	}
	public String vcsClientListServerProjects(Map<String, Object> scd) {
		int customizationId = (Integer)scd.get("ocustomizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		
		
		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&r="+po.getProjectUuid();
		urlParameters+="&q=4536";
		String url=FrameworkCache.getAppSettingStringValue(0, "vcs_url");//po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSQueryResult";
		return HttpUtil.send(url, urlParameters);
	}
	public W5QueryResult vcsClientObjectConflicts(Map<String, Object> scd, String key) {
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		
		String[] keyz = key.replace('.', ',').split(",");
		
		
		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&t="+keyz[0]+"&k="+keyz[1]+"&r="+po.getProjectUuid();
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSObjectPull";
		String s = HttpUtil.send(url, urlParameters);
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			try {
				json = new JSONObject(s);
				if(json.get("success").toString().equals("true")){
					Map<String, Object> mo = null;
					W5Table t = FrameworkCache.getTable(scd, GenericUtil.uInt(keyz[0]));
					StringBuilder sql = new StringBuilder();
					String paramDsc = t.get_tableParamList().get(0).getExpressionDsc();
					sql.append("select x.* from ").append(t.getDsc()).append(" x where x.").append(paramDsc).append("=?");
					sql.append(DBUtil.includeTenantProjectPostSQL(scd, t, "x"));
					List p= new ArrayList();p.add(GenericUtil.uInt(keyz[1]));
					ArrayList ar = new ArrayList();
					W5QueryField field = new W5QueryField();field.setDsc("name");field.setTabOrder((short)1);ar.add(field);
					field = new W5QueryField();field.setDsc("local");field.setTabOrder((short)2);ar.add(field);
					field = new W5QueryField();field.setDsc("remote");field.setTabOrder((short)3);ar.add(field);
					field = new W5QueryField();field.setDsc("editor");field.setTabOrder((short)4);ar.add(field);
					W5QueryResult qr = new W5QueryResult(-1);
					qr.setNewQueryFields(ar);
					qr.setScd(scd);qr.setRequestParams(new HashMap());
					qr.setData(new ArrayList());
					qr.setErrorMap(new HashMap());qr.setQuery(new W5Query(-1));
					try {
						mo =(Map)dao.executeSQLQuery2Map(sql.toString(), p).get(0);
					} catch(Exception ez){
						mo = new HashMap();
					}
					JSONObject jo = json.getJSONObject("object");
					for(W5TableField tf:t.get_tableFieldList())if(tf.getTabOrder()>1 && !GenericUtil.hasPartInside("project_uuid,oproject_uuid,customization_id,insert_user_id,version_no,insert_dttm,version_user_id,version_dttm", tf.getDsc())){
						String k = tf.getDsc();
						Object lcl = mo.get(k);
						Object rmt = jo.has(k)?  jo.get(k) : null;
						if(!GenericUtil.safeEquals(rmt, lcl)){
							Object[] o = new Object[4];
							o[0]=k;
							o[1] = lcl;
							o[2] = rmt;
							o[3] = tf.getDefaultControlTip();
							qr.getData().add(o);
						}
					}
					if(qr.getData().size()==0) {//no conflict
						List lv = dao.find("from W5VcsObject t where t.tableId=?0 AND t.tablePk=?1 AND t.customizationId=?2 AND t.projectUuid=?3", t.getTableId(), GenericUtil.uInt(keyz[1]), customizationId, projectUuid);
						if(lv.size()>0) {
							W5VcsObject vo = (W5VcsObject)lv.get(0);
							vo.setVcsObjectStatusTip((short)9);
							vo.setVcsCommitRecordHash(metadataWriter.getObjectVcsHash(scd,  t.getTableId(), GenericUtil.uInt(keyz[1])));
							vo.setVcsCommitId(json.getInt("commit_id"));
							dao.updateObject(vo);
						}
						
					}
					return qr;
				}
			} catch (JSONException e){
				throw new IWBException("vcs","vcsServerObjectConflicts:JSONException", 0, null, "Error", e);
			}
		}

		return null;
	}

	public int vcsProjectCreateSavePoint(Map<String, Object> scd, String dsc) {
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		
		int maxSavePoint = 0;
		List<Object> listMaxSavePoint = dao.executeSQLQuery("select max(x.save_point_id) xx from iwb.w5_project_save_point x where project_uuid=?",projectUuid);
		if(!GenericUtil.isEmpty(listMaxSavePoint)) {
			maxSavePoint = GenericUtil.uInt(listMaxSavePoint.get(0));
		}

		int maxCommit = 0;
		List<Object> listMaxCommitId = dao.executeSQLQuery("select max(x.vcs_commit_id) xx from iwb.w5_vcs_commit x where project_uuid=?",projectUuid);
		if(!GenericUtil.isEmpty(listMaxCommitId)) {
			maxCommit = GenericUtil.uInt(listMaxCommitId.get(0));
		}
		
		List<W5Table> l = FrameworkCache.listVcsTables(null);
		int savePointId = maxSavePoint + 1;
		
		dao.executeUpdateSQLQuery("INSERT INTO iwb.w5_project_save_point(save_point_id, project_uuid, dsc, vcs_commit_id, insert_user_id, version_user_id) " +
		"VALUES (?, ?, ?, ?, ?, ?)", savePointId, projectUuid, dsc, maxCommit, scd.get("userId"), scd.get("userId"));

		FrameworkSetting.projectSystemStatus.put(projectUuid, 1);

//		String newSchemaName = "iwb_"+projectUuid.replace('-','_') + "_sp_"+savePointId;
		String newSchemaName = po.getRdbmsSchema() + "_sp_"+savePointId;
		dao.executeUpdateSQLQuery("create schema " + newSchemaName);
		for(W5Table t:l) {
			String tableName = t.getDsc();
			int ix = t.getDsc().indexOf(".");
			String newTableName = newSchemaName + "." + (ix==-1 ? tableName: tableName.substring(ix+1));
			dao.executeUpdateSQLQuery("create table " + newTableName + " as select * from " + tableName + " where project_uuid=?",projectUuid);
		}
		dao.executeUpdateSQLQuery("create table " + newSchemaName + ".w5_vcs_commit as select * from iwb.w5_vcs_commit where project_uuid=?",projectUuid);
		dao.executeUpdateSQLQuery("create table " + newSchemaName + ".w5_vcs_object as select * from iwb.w5_vcs_object where project_uuid=?",projectUuid);

		String newDbSchemaName = newSchemaName + "_db";
//		dao.executeUpdateSQLQuery("create schema " + newDbSchemaName);
		FrameworkSetting.projectSystemStatus.put(projectUuid, 0);
		dao.executeUpdateSQLQuery("SELECT iwb.clone_schema(?, ?, TRUE)", po.getRdbmsSchema(), newDbSchemaName);

		
		return savePointId;
		
	}
	public int vcsServerProjectCreateSavePoint(String userName, String passWord, int customizationId, String projectId, String dsc) {
		if(!FrameworkSetting.vcsServer)
			throw new IWBException("vcs","vcsServerProjectCreateSavePoint",0,null, "Not a VCS Server to vcsServerProjectCreateSavePoint", null);
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, projectId);
		return vcsProjectCreateSavePoint(scd, dsc);
	}
	
	public boolean vcsProjectBack2SavePoint(Map<String, Object> scd, int savePointId) {
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		int cnt = GenericUtil.uInt(dao.executeSQLQuery("select count(1) xx from iwb.w5_project_save_point x where project_uuid=? AND x.save_point_id=?", projectUuid, savePointId).get(0));
		if(cnt==0)return false;
		FrameworkSetting.projectSystemStatus.put(projectUuid, 1);

		List<W5Table> l = dao.find("from W5Table t where t.projectUuid='067e6162-3b6f-4ae2-a221-2470b63dff00' and t.vcsFlag=1 order by t.tableId desc");
		String newSchemaName = po.getRdbmsSchema() + "_sp_"+savePointId;
		for(W5Table t:l) {
			String tableName = t.getDsc();
			int ix = t.getDsc().indexOf(".");
			String newTableName = newSchemaName + "." + (ix==-1 ? tableName: tableName.substring(ix+1));
			dao.executeUpdateSQLQuery("delete from " + tableName + " where project_uuid=?",projectUuid);
			dao.executeUpdateSQLQuery("insert into " + tableName + " select * from " + newTableName  + " where project_uuid=?",projectUuid);
		}
		dao.executeUpdateSQLQuery("delete from iwb.w5_vcs_commit where project_uuid=?",projectUuid);
		dao.executeUpdateSQLQuery("insert into iwb.w5_vcs_commit select * from " + newSchemaName  + ".w5_vcs_commit where project_uuid=?",projectUuid);
		dao.executeUpdateSQLQuery("delete from iwb.w5_vcs_object where project_uuid=?",projectUuid);
		dao.executeUpdateSQLQuery("insert into iwb.w5_vcs_object select * from " + newSchemaName  + ".w5_vcs_object where project_uuid=?",projectUuid);
		FrameworkSetting.projectSystemStatus.put(projectUuid, 0);

		return true;
	}

	
	public boolean vcsProjectDeleteSavePoint(Map<String, Object> scd, int savePointId) {
		String projectUuid = (String)scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		int cnt = GenericUtil.uInt(dao.executeSQLQuery("select count(1) xx from iwb.w5_project_save_point x where x.project_uuid=? AND x.save_point_id=?", projectUuid, savePointId).get(0));
		if(cnt==0)return false;
		
		String newSchemaName = po.getRdbmsSchema() + "_sp_"+savePointId;

		dao.executeUpdateSQLQuery("drop schema " + newSchemaName + " cascade");
		dao.executeUpdateSQLQuery("delete from iwb.w5_project_save_point x where project_uuid=? AND x.save_point_id=?", projectUuid, savePointId);		
		
		return true;
	}
	
	public boolean icbVCSUpdateSqlAndFields() {
		int cusId = 0;
		String icbProjectId = "067e6162-3b6f-4ae2-a221-2470b63dff00";
		Map scd = new HashMap();
		scd.put("ocustomizationId", 0);
		scd.put("customizationId", cusId);
		scd.put("projectId", icbProjectId);
		scd.put("userId", 1);scd.put("roleId", 0);scd.put("userRoleId", 1);
		String vcsServer = FrameworkSetting.argMap.get("vcs_server");
		if(GenericUtil.isEmpty(vcsServer))vcsServer="http://34.68.231.169/app/";
		dao.executeUpdateSQLQuery("update iwb.w5_project set vcs_url=?", vcsServer);
		dao.executeUpdateSQLQuery("update iwb.w5_app_setting set val=? where dsc in ('vcs_url','vcs_url_new_project')", vcsServer);

		
		long startTime = System.currentTimeMillis();
		try {
			System.out.println("Start: icbVCSUpdateSqlAndFields: " + icbProjectId);
			List<W5Project> lp = metadataLoader.reloadProjectsCache(cusId);
			scd.put("ocustomizationId", cusId);
			scd.put("customizationId", cusId);


			vcsClientSqlCommitsFetchAndRun(scd, 0);
			
			// Application Settings
			metadataLoader.reloadTablesCache(icbProjectId);
			
	
			W5QueryResult qr = vcsClientObjectsAll(scd);
			if(qr.getData()!=null) {
				StringBuilder tableKeys = new StringBuilder();
				for(int qi=0;qi<qr.getData().size();qi++) if(GenericUtil.uInt(qr.getData().get(qi)[7])==1 && GenericUtil.uInt(qr.getData().get(qi)[1])==16){//pull && tableField
						//41.125995,41.125996
						tableKeys.append(qr.getData().get(qi)[1]).append(".").append(qr.getData().get(qi)[3]).append(",");
					
				}
				System.out.println("Table Field Keyz to update: " + tableKeys.toString());
				if(tableKeys.length()>0) {
					tableKeys.setLength(tableKeys.length() - 1);
					vcsClientObjectPullMulti(scd, tableKeys.toString(), true);
				}
			}

			
			System.out.println("End: icbVCSUpdateSqlAndFields("+(System.currentTimeMillis() - startTime)+")");
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Error: icbVCSUpdateSqlAndFields("+(System.currentTimeMillis() - startTime)+") " + e.getMessage());
			return false;
		}
		return true;
	}
	
	
	public boolean projectVCSUpdate(String projectId) {
		int cusId = 0;
		String icbProjectId = "067e6162-3b6f-4ae2-a221-2470b63dff00";
		Map scd = new HashMap();
		scd.put("ocustomizationId", 0);
		scd.put("customizationId", cusId);
		scd.put("projectId", icbProjectId);
		scd.put("userId", 1);scd.put("roleId", 0);scd.put("userRoleId", 1);

		
		long startTime = System.currentTimeMillis();
		try {
			System.out.println("Start: projectVCSUpdate: " + projectId);
			List<W5Project> lp = metadataLoader.reloadProjectsCache(cusId);
			if(!icbProjectId.equals(projectId)) {
				List lcusId = dao.executeSQLQuery("select x.customization_id from iwb.w5_project x where x.project_uuid=?", projectId); 
				if(lcusId==null) {
					vcsClientProjectFetch(scd, projectId);
					cusId= GenericUtil.uInt(dao.executeSQLQuery("select x.customization_id from iwb.w5_project x where x.project_uuid=?", projectId).get(0));
					//lp = metaDataDao.reloadProjectsCache(cusId);
				} else
					cusId= GenericUtil.uInt(lcusId.get(0));
				scd.put("projectId", projectId);
				lp = metadataLoader.reloadProjectsCache(cusId);
			}
			scd.put("ocustomizationId", cusId);
			scd.put("customizationId", cusId);


			vcsClientSqlCommitsFetchAndRun(scd, 0);
			
			// Application Settings
			metadataLoader.reloadTablesCache(projectId);
			
			W5QueryResult qr = vcsClientObjectsAll(scd);
			if(qr.getData()!=null) {
				StringBuilder tableKeys = new StringBuilder();
				for(int qi=0;qi<qr.getData().size();qi++) if(GenericUtil.uInt(qr.getData().get(qi)[7])==1){//pull && tableField
					//41.125995,41.125996
						tableKeys.append(qr.getData().get(qi)[1]).append(".").append(qr.getData().get(qi)[3]).append(",");
					
				}
				System.out.println("Others Keyz to update: " + tableKeys.toString());
				if(tableKeys.length()>0) {
					tableKeys.setLength(tableKeys.length() - 1);
					vcsClientObjectPullMulti(scd, tableKeys.toString(), true);
				}

			}
			if(icbProjectId.equals(projectId)) {
				metadataLoader.reloadDeveloperEntityKeys();
			} else {
				dao.executeUpdateSQLQuery("insert into iwb.w5_user_related_project(user_id, related_project_uuid)select u.user_id,? from iwb.w5_user u where u.user_id=10 AND not exists(select 1 from iwb.w5_user_related_project q where q.user_id=u.user_id AND related_project_uuid=?)", projectId, projectId);
				List<Object[]> res = dao.executeSQLQuery("select sum(case when t.table_id=3108 then 1 else 0 end) cnt_role, sum(case when t.table_id=3109 then 1 else 0 end) cnt_user_role from iwb.w5_table t where t.project_uuid=? AND t.table_id in (3108, 3109)", projectId);
				if(!GenericUtil.isEmpty(res) && GenericUtil.uInt(res.get(0)[0])>0) {
					dao.executeUpdateSQLQuery("insert into x_role(role_id, role_name, role_group_id)values(99998, 'First Role', (select u.user_tip from iwb.w5_user_tip u where u.user_tip!=122  AND u.project_uuid=? limit 1))", projectId);
					if(GenericUtil.uInt(res.get(0)[1])>0)
						dao.executeUpdateSQLQuery("update x_user_role set role_id=99998 where user_id=99999 and role_id=99999");
				} else {
					dao.executeUpdateSQLQuery("update iwb.w5_project p set session_query_id=0, authentication_func_id=0, ui_login_template_id=0 where p.project_uuid=?", projectId);
				}
			}
			
			System.out.println("End: projectVCSUpdate("+(System.currentTimeMillis() - startTime)+")");
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Error: projectVCSUpdate("+(System.currentTimeMillis() - startTime)+") " + e.getMessage());
			return false;
		}
		return true;
	}
	public Map vcsClientObjectPullColumn(Map<String, Object> scd, int tableId, int tablePk, String column) {
		if(FrameworkSetting.vcsServer && !FrameworkSetting.vcsServerClient)
			throw new IWBException("vcs","vcsClientObjectPullColumn",0,null, "VCS Server not allowed to vcsClientObjectPull", null);
		int customizationId = (Integer)scd.get("customizationId");
		String projectUuid = (String)scd.get("projectId");
		
		W5Project po = FrameworkCache.getProject(projectUuid);

		W5Table t = FrameworkCache.getTable(projectUuid, tableId);
		if(t.getVcsFlag()==0){
			throw new IWBException("vcs","vcsClientObjectPullColumn", t.getTableId(), po.getProjectUuid()+"!="+projectUuid, "Not VCS Table2", null);
		}
		String urlParameters = "u="+po.getVcsUserName()+"&p="+po.getVcsPassword()+"&c="+customizationId+"&t="+tableId+"&k="+tablePk+"&r="+po.getProjectUuid();
		
		List lv = dao.find("from W5VcsObject t where t.tableId=?0 AND t.tablePk=?1 AND t.customizationId=?2 AND t.projectUuid=?3", tableId, tablePk, customizationId, projectUuid);
		W5VcsObject vo = null;
		Map result = new HashMap();
		result.put("success", true);
		if(!lv.isEmpty()){
			vo = (W5VcsObject)lv.get(0);
			if(vo.getVcsObjectStatusTip()==2){
			//	throw new PromisException("vcs","vcsClientObjectPull", vo.getVcsCommitId(), null, "Object is New. Cannot be Pulled2", null);
				result.put("error", "force");
				result.put("error_msg", "Object is New. Cannot be Pulled2");
				return result;
			}
			urlParameters+="&o="+vo.getVcsCommitId();
		} else {
			throw new IWBException("vcs","vcsClientObjectPullColumn",0,null, "VCS Object not found", null);
		}

		
		String url=po.getVcsUrl();
		if(!url.endsWith("/"))url+="/";
		url+="serverVCSObjectPull";
		String s = HttpUtil.send(url, urlParameters);
		if(!GenericUtil.isEmpty(s)){
			JSONObject json;
			try {
				json = new JSONObject(s);
				if(json.get("success").toString().equals("true")){
//					String sql =json.getString("sql");
					int action = json.getInt("action");
					if(action==3)
						throw new IWBException("vcs","vcsClientObjectPullColumn",0,null, "VCS Object deleted", null);
					JSONObject jo =json.getJSONObject("object");
					int srvVcsCommitId = json.getInt("commit_id");
					int srvCommitUserId = json.getInt("user_id");
					
					short vcsObjectStatusTip = dao.updateVcsObjectColumn(scd, tableId, tablePk, column, jo);

					vo.setVcsObjectStatusTip(vcsObjectStatusTip);
					if(vcsObjectStatusTip==9) {
						vo.setVcsCommitRecordHash(metadataWriter.getObjectVcsHash(scd, tableId, tablePk));
						vo.setVersionNo((short)(vo.getVersionNo()+1));
						vo.setVersionUserId(srvCommitUserId);
						vo.setVcsCommitId(srvVcsCommitId);
						vo.setVersionDttm(new Timestamp(new Date().getTime()));
					}
					dao.updateObject(vo);
					
					if(FrameworkSetting.log2tsdb)Log4Crud(po.getRdbmsSchema(), t, action, srvVcsCommitId, tablePk, jo);
				} else
					throw new IWBException("vcs","vcsClientObjectPull:server Error Response", t.getTableId(), s, json.has("error") ? json.getString("error"): json.toString(), null);
			} catch (JSONException e){
				throw new IWBException("vcs","vcsClientObjectPull:JSONException", t.getTableId(), s, "Error", e);
			}
		}
		return result;
	}
	
	public void deleteProject(String projectId) {
		metadataWriter.deleteProjectMetadataAndDB(projectId, true);
	}
	
	public Map vcsServerProjectFetch(
      String userName,
      String passWord,
      int customizationId,
      String projectId,
      String newProjectId) {
		if(!FrameworkSetting.vcsServer || customizationId!=0)
			throw new IWBException("vcs","vcsServerProjectFetch",0,null, "Not a VCS Server to vcsServerProjectFetch", null);
		Map scd = vcsServerAuthenticate(userName, passWord, customizationId, projectId);
		W5Project po = FrameworkCache.getProject(newProjectId);
		
		Map result = new HashMap();
		result.put("success", po!=null);
		if(po==null) {
			result.put("errorMsg", "Wrong ProjectID");
			return result;
		}
		
		Map requestParams = new HashMap();
		requestParams.put("project_uuid", newProjectId);
		requestParams.put("customization_id", po.getCustomizationId());
		result.put("project", dao.runSQLQuery2Map("select * from iwb.w5_project p where p.project_uuid='${req.project_uuid}'"
				, scd, requestParams, new HashMap()));
		result.put("customization", dao.runSQLQuery2Map("select * from iwb.w5_customization p where p.customization_id=${req.customization_id}"
				, scd, requestParams, new HashMap()));
		
		return result;
  }
}
