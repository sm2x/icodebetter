package iwb.adapter.ui.f7;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import iwb.adapter.ui.ViewMobileAdapter;
import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.enums.FieldDefinitions;
import iwb.model.db.M5List;
import iwb.model.db.W5BIGraphDashboard;
import iwb.model.db.W5Detay;
import iwb.model.db.W5Form;
import iwb.model.db.W5FormCell;
import iwb.model.db.W5FormModule;
import iwb.model.db.W5FormSmsMail;
import iwb.model.db.W5LookUp;
import iwb.model.db.W5LookUpDetay;
import iwb.model.db.W5ObjectMenuItem;
import iwb.model.db.W5ObjectToolbarItem;
import iwb.model.db.W5QueryField;
import iwb.model.db.W5Table;
import iwb.model.db.W5Workflow;
import iwb.model.helper.W5FormCellHelper;
import iwb.model.helper.W5TableRecordHelper;
import iwb.model.result.M5ListResult;
import iwb.model.result.W5FormResult;
import iwb.model.result.W5PageResult;
import iwb.model.result.W5QueryResult;
import iwb.util.GenericUtil;
import iwb.util.UserUtil;

public class F7 implements ViewMobileAdapter {
	final private static String[] labelMap = new String[]{"info","warning","error"};
	final private static String[] labelMapColor = new String[]{"rgba(33, 150, 243, 0.1)","rgba(255, 152, 0, 0.2);","rgba(255, 0, 0, 0.1);"};
	private StringBuilder serializeTableHelperList(Map scd, List<W5TableRecordHelper> ltrh) {
		StringBuilder buf = new StringBuilder();
		boolean bq = false;
		buf.append("[");
		if (ltrh != null)
			for (W5TableRecordHelper trh : ltrh) {
				W5Table dt = FrameworkCache.getTable(scd,
						trh.getTableId());
				if (dt == null)
					break;
				if (bq)
					buf.append(",");
				else
					bq = true;
				buf.append("{\"tid\":")
						.append(trh.getTableId())
						.append(",\"tpk\":")
						.append(trh.getTablePk())
						.append(",\"tcc\":")
						.append(trh.getCommentCount())
						.append(",\"tdsc\":\"")
						.append(LocaleMsgCache.get2(scd,
								dt.getDsc())).append("\"")
						.append(",\"dsc\":\"")
						.append(GenericUtil.stringToJS2(trh.getRecordDsc()))
						.append("\"}");
			}
		buf.append("]");
		return buf;
	}
	public StringBuilder serializeQueryData(W5QueryResult queryResult) {
		if (queryResult.getQuery().getQueryType() == 10)
			return null;
		if (queryResult.getQuery().getQueryType() == 14)
			return null;
		int customizationId = (Integer) queryResult.getScd().get("customizationId");
		String xlocale = (String) queryResult.getScd().get("locale");
		String userIdStr = queryResult.getScd().containsKey("userId") ? queryResult.getScd().get("userId").toString() : null;
		List<Object[]> datas = queryResult.getData();
		StringBuilder buf = new StringBuilder();
		buf.append("{\"success\":").append(queryResult.getErrorMap().isEmpty())
				.append(",\"queryId\":").append(queryResult.getQueryId())
				.append(",\"execDttm\":\"")
				.append(GenericUtil.uFormatDateTime(new Date())).append("\"");
		if (queryResult.getErrorMap().isEmpty()) {
			buf.append(",\n\"data\":["); // ana
			if (datas != null && datas.size() > 0) {
				boolean bx = false;
				for (Object[] o : datas) {
					if (bx)
						buf.append(",\n");
					else
						bx = true;
					buf.append("{"); // satir
					boolean b = false;
					for (W5QueryField f : queryResult.getNewQueryFields()) {
						if (b)
							buf.append(",");
						else
							b = true;
						if (f.getPostProcessType() == 9)
							buf.append("\"_");
						else
							buf.append("\"");
						buf.append(f.getPostProcessType() == 6 ? f.getDsc()
								.substring(1) : f.getDsc());
						Object obj = o[f.getTabOrder() - 1];
						if (f.getFieldType() == 5) {// boolean
							buf.append("\":").append(GenericUtil.uInt(obj) != 0);
							continue;
						}
						if (f.getFieldType() == 6) {// auto
							buf.append("\":");
							if (obj == null || obj.toString().equals("0"))
								buf.append("null");
							else if (GenericUtil.uInt(obj) != 0)
								buf.append(obj);
							else
								buf.append("\"").append(obj).append("\"");
							continue;
						}
						buf.append("\":\"");
						if (obj != null)
							switch (f.getPostProcessType()) { // queryField
																// PostProcessTip
							case 3:
								buf.append(GenericUtil.onlyHTMLToJS(obj
										.toString()));
								break;
							case 8:
								buf.append(GenericUtil.stringToHtml2(obj));
								break;
							case 20: // user LookUp
								buf.append(obj)
										.append("\",\"")
										.append(f.getDsc())
										.append("_qw_\":\"")
										.append(UserUtil.getUserName(GenericUtil.uInt(obj)));
								break;
							case 21: // users LookUp
								String[] ids = ((String) obj).split(",");
								if (ids.length > 0) {
									String res = "";
									for (String s : ids) {
										res += ","
												+ UserUtil.getUserName(GenericUtil.uInt(s));
									}
									buf.append(obj).append("\",\"")
											.append(f.getDsc())
											.append("_qw_\":\"")
											.append(res.substring(1));
								}
								break;
							case 53: // User LookUp Real Name
								buf.append(obj)
										.append("\",\"")
										.append(f.getDsc())
										.append("_qw_\":\"")
										.append(UserUtil.getUserDsc(GenericUtil.uInt(obj)));
								break;
							case 54: // Users LookUp Real Name
								String[] ids11 = ((String) obj).split(",");
								if (ids11.length > 0) {
									String res = "";
									for (String s : ids11) {
										res += ","
												+ UserUtil.getUserDsc(GenericUtil.uInt(s));
									}
									buf.append(obj).append("\",\"")
											.append(f.getDsc())
											.append("_qw_\":\"")
											.append(res.substring(1));
								}
								break;
							case 22:
							case 23: // roles: TODO
								buf.append(obj);
								break;
							case 1:// duz
								buf.append(obj);
								break;
							case 2: // locale filtresinden gececek
								buf.append(LocaleMsgCache.get2(
										customizationId, xlocale,
										obj.toString()));
								break;
							case 10:
							case 11: // demek ki static lookup'li deger
										// tutulacak
								buf.append(GenericUtil.stringToJS2(obj
										.toString()));
								if (f.getLookupQueryId() == 0)
									break;
								W5LookUp lookUp = FrameworkCache.getLookUp(
										customizationId, f.getLookupQueryId());
								if (lookUp == null)
									break;
								buf.append("\",\"").append(f.getDsc())
										.append("_qw_\":\"");
								String[] objs = f.getPostProcessType() == 11 ? ((String) obj)
										.split(",") : new String[] { obj
										.toString() };
								boolean bz = false;
								for (String q : objs) {
									if (bz)
										buf.append(", ");
									else
										bz = true;
									W5LookUpDetay d = lookUp.get_detayMap()
											.get(q);
									if (d != null) {
										String s = d.getDsc();
										if (s != null) {
											s = LocaleMsgCache.get2(
														customizationId,
														xlocale, s);
											buf.append(GenericUtil
													.stringToJS2(s));
										}
									} else {
										buf.append("???: ").append(q);
									}
								}
								break;
							case 13:
							case 12:// table Lookup
								buf.append(GenericUtil.stringToJS2(obj
										.toString()));
								break;
							case	48://comment extra info
								String[] ozc = ((String) obj).split(";");//commentCount;commentUserId;lastCommentDttm;viewUserIds-msg
								int ndx = ozc[3].indexOf('-');
								buf.append(ozc[0]).append("\",\"").append(FieldDefinitions.queryFieldName_CommentExtra)
									.append("\":{\"last_dttm\":\"").append(ozc[2])
									.append("\",\"user_id\":").append(ozc[1])
									.append(",\"user_dsc\":\"").append(UserUtil.getUserDsc(GenericUtil.uInt(ozc[1])))
									.append("\",\"is_new\":").append(!GenericUtil.hasPartInside(ozc[3].substring(0,ndx), userIdStr))
									.append(",\"msg\":\"").append(GenericUtil.stringToHtml(ozc[3].substring(ndx+1)))
									.append("\"}");
								continue;
//								break;
							case 49:// approval _qw_
								String[] ozs = ((String) obj).split(";");
								int appId = GenericUtil.uInt(ozs[1]);// approvalId:
																	// kendisi
																	// yetkili
																	// ise + ,
																	// aksi
																	// halde -
								int appStepId = GenericUtil.uInt(ozs[2]);// approvalStepId
								if (appStepId != 998
										&& !GenericUtil.accessControl(
												queryResult.getScd(),
												(short) 1,
												ozs.length > 3 ? ozs[3] : null,
												ozs.length > 4 ? ozs[4] : null))
									buf.append("-");
								buf.append(ozs[2]);
								W5Workflow appr = FrameworkCache.getWorkflow(queryResult.getScd(),appId);
								String appStepDsc = "";
								if (appr != null
										&& appr.get_approvalStepMap().get(
												Math.abs(appStepId)) != null)
									appStepDsc = appr.get_approvalStepMap()
											.get(Math.abs(appStepId)).getDsc();

								buf.append("\",\"pkpkpk_arf_id\":")
										.append(ozs[0])
										.append(",\"")
										.append(f.getDsc())
										.append("_qw_\":\"")
										.append(LocaleMsgCache.get2(
												customizationId, xlocale,
												appStepDsc));
								if (ozs.length > 3 && ozs[3] != null
										&& ozs[3].length() > 0) {// roleIds
									buf.append("\",\"app_role_ids_qw_\":\"");
									String[] roleIds = ozs[3].split(",");
									for (String rid : roleIds) {
										buf.append(
												FrameworkCache.wRoles.get(
														customizationId).get(
														GenericUtil.uInt(rid)) != null ? FrameworkCache.wRoles
														.get(customizationId)
														.get(GenericUtil
																.uInt(rid))
														: "null").append(", ");
									}
									buf.setLength(buf.length() - 2);
								}
								if (ozs.length > 4 && ozs[4] != null
										&& ozs[4].length() > 0) {// userIds
									buf.append("\",\"app_user_ids_qw_\":\"");
									String[] userIds = ozs[4].split(",");
									for (String uid : userIds) {
										buf.append(
												UserUtil.getUserDsc(GenericUtil.uInt(uid)))
												.append(", ");
									}
									buf.setLength(buf.length() - 2);
								}
								break;
							/*
							 * case 49://approval _qw_ buf.append(obj); int
							 * appStepId = PromisUtil.uInt(obj);
							 * buf.append("\",\""
							 * ).append(f.getDsc()).append("_qw_\":\""
							 * ).append(PromisCache
							 * .wApprovals.get(f.getLookupQueryId
							 * ()).get_approvalStepMap
							 * ().get(Math.abs(appStepId)).getDsc()); break;
							 */
						
							default:
								buf.append(GenericUtil.stringToJS2(obj
										.toString()));
							}
						buf.append("\"");

					}
					if (queryResult.getQuery().getShowParentRecordFlag() != 0
							&& o[o.length - 1] != null) {
						buf.append(",\"").append(FieldDefinitions.queryFieldName_HierarchicalData).append("\":")
								.append(serializeTableHelperList(
										queryResult.getScd(),
										(List<W5TableRecordHelper>) o[o.length - 1]));
					}
					buf.append("}"); // satir
				}
			}
			buf.append("],\n\"browseInfo\":{\"startRow\":")
					.append(queryResult.getStartRowNumber())
					.append(",\"fetchCount\":")
					.append(queryResult.getFetchRowCount())
					.append(",\"totalCount\":")
					.append(queryResult.getResultRowCount()).append("}");
			if (FrameworkSetting.debug && queryResult.getExecutedSql() != null) {
				buf.append(",\n\"sql\":\"")
						.append(GenericUtil.stringToJS2(GenericUtil.replaceSql(
								queryResult.getExecutedSql(),
								queryResult.getSqlParams()))).append("\"");
			}
			if (!GenericUtil.isEmpty(queryResult.getExtraOutMap()))
				buf.append(",\n \"extraOutMap\":").append(
						GenericUtil.fromMapToJsonString(queryResult
								.getExtraOutMap()));
		} else
			buf.append(",\n\"errorType\":\"validation\",\n\"errors\":")
					.append(serializeValidatonErrors(queryResult.getErrorMap(),
							xlocale));

		return buf.append("}");
	}
	public StringBuilder serializeValidatonErrors(Map<String, String> errorMap,
			String locale) {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		boolean b = false;
		for (String q : errorMap.keySet()) {
			if (b)
				buf.append("\n,");
			else
				b = true;
			buf.append("{\"id\":\"").append(q).append("\",\"msg\":\"")
					.append(GenericUtil.stringToJS2(errorMap.get(q)))
					.append("\",\"dsc\":\"")
					.append(LocaleMsgCache.get2(0, locale, q)).append("\"}");// TODO.
																				// aslinda
																				// customizationId
																				// olmasi
																				// lazim
		}
		buf.append("]");
		return buf;
	}
	
	public	StringBuilder	serializeList(M5ListResult	listResult){
		int deviceType = GenericUtil.uInt(listResult.getScd().get("mobile")); 

		if(deviceType!=2)return serializeListiOS(listResult);
		else return serializeListMaterial(listResult);
	}
	private	String	getJsCode(M5List l){
		if(GenericUtil.isEmpty(l.getJsCode())){
			return "var json=callAttributes.json;\n"+
				"if(!iwb.tpls[json.listId] && json.htmlData)iwb.tpls[json.listId]=Template7.compile(json.htmlData);\n" +
				"var tpl=iwb.tpls[json.listId];\n" +
				"var data=iwb.apply({},json.baseParams||{});\n" +
				"if(json.pageSize)data.limit=json.pageSize;\n" +
				"iwb.request({url:json.dataUrl, data:data, success: function (j) {callAttributes.next(json.htmlPage.replace('${iwb-data}',tpl(j)))}});\n"+
				"return false;";
		} else return l.getJsCode();

	}
	private	StringBuilder	serializeListMaterial(M5ListResult	listResult){
		M5List l = listResult.getList();
		StringBuilder buf = new StringBuilder();

		String htmlDataCode=l.getHtmlDataCode();
		if(htmlDataCode==null)htmlDataCode="";
		htmlDataCode=htmlDataCode.replace("iwb-link-7 ", "iwb-link-"+l.getListId()+" ");

		buf.append("{success:true, listId:").append(l.getListId()).append(", listTip:").append(l.getListTip()==1 || l.getListTip()==4 ?1:l.getListTip()).append(",\n name:'")
			.append(LocaleMsgCache.get2(listResult.getScd(), l.getLocaleMsgKey()))
			.append("',\n htmlData:'").append(GenericUtil.stringToJS2(htmlDataCode))
			.append("',\n init:function(callAttributes){\n").append(getJsCode(l))
			.append("\n},\n dataUrl:'ajaxQueryData?_qid=").append(l.getQueryId());
		if(l.getListTip()==3)buf.append("&_pids=1");
		buf.append("'");
	
		
		if(l.getDefaultPageRecordNumber()>0)
			buf.append(", pageSize: ").append(l.getDefaultPageRecordNumber());
		if(!GenericUtil.isEmpty(l.get_orderQueryFieldNames())){
			buf.append(",\n orderNames:[");
			for(String f:l.get_orderQueryFieldNames()){
				buf.append("{id:'").append(f).append("',dsc:'").append(LocaleMsgCache.get2(listResult.getScd(), f)).append("'},");
			}
			buf.setLength(buf.length()-1);
			buf.append("]");
		}

		
		boolean insertFlag = false;
		if(l.getDefaultCrudFormId()!=0 && l.get_mainTable()!=null){
			W5Table t = l.get_mainTable();
			insertFlag = GenericUtil.accessControl(listResult.getScd(),
					t.getAccessInsertTip(), t.getAccessInsertRoles(),
					t.getAccessInsertUsers());
			buf.append(",\n crudFormId:")
			.append(l.getDefaultCrudFormId())
			.append(",\n crudTableId:")
			.append(t.getTableId()).append(",\n pkName:'")
			.append(t.get_tableParamList().get(0).getDsc())
			.append("',\n crudFlags:{insert:")
			.append(insertFlag)
			.append(",edit:")
			.append(t.getAccessUpdateUserFields() != null
					|| GenericUtil.accessControl(listResult.getScd(),
							t.getAccessUpdateTip(),
							t.getAccessUpdateRoles(),
							t.getAccessUpdateUsers()))
			.append(",remove:")
			.append(t.getAccessDeleteUserFields() != null
					|| GenericUtil.accessControl(listResult.getScd(),
							t.getAccessDeleteTip(),
							t.getAccessDeleteRoles(),
							t.getAccessDeleteUsers()));
			buf.append("}");
		}
		
		StringBuilder s2= new StringBuilder();
		if(!GenericUtil.isEmpty(l.get_detailMLists())){
			for(M5List d:l.get_detailMLists())s2.append("{icon:'list', text:'").append(LocaleMsgCache.get2(listResult.getScd(), d.getLocaleMsgKey()))
			.append("',href:'showMList?_lid=").append(d.getListId()).append("&x").append(l.get_mainTable().get_tableFieldList().get(0).getDsc()).append("='},"); //TODO. parent'takine gore degil de, farkli olmasi gerekli
		}
		
		if(!GenericUtil.isEmpty(l.get_menuItemList())){
			for(W5ObjectMenuItem d:l.get_menuItemList())if(d.getItemTip()==1 && !GenericUtil.isEmpty(d.getCode())){ //record ile ilgili
				s2.append("{icon:'").append(d.getImgIcon()).append("', text:'").append(LocaleMsgCache.get2(listResult.getScd(), d.getLocaleMsgKey())).append("'");
				if(d.getCode().charAt(0)!='!')s2.append(",click:function(ax,bx,cx){\n").append(d.getCode()).append("\n}");
				else s2.append(",href:'").append(d.getCode().substring(1)).append("'");
				s2.append("},");
			}
		}
		
		if(s2.length()>0){
			s2.setLength(s2.length()-1);
			buf.append("\n, recordButtons:[").append(s2).append("]");
		}
		buf.append("\n, htmlPage:'");
		if(GenericUtil.isEmpty(l.getHtmlPageCode())){
			
			boolean searchBar = l.getDefaultPageRecordNumber()==0 && (l.getListTip()==1 || l.getListTip()==4);
			//StringBuilder s2= new StringBuilder();
			s2.setLength(0);
			s2.append("<div class=\"page\" data-page=\"mlist-").append(l.getListId()).append("-view\">")
			.append("<div class=\"navbar\"><div class=\"navbar-inner iwb-navbar-list\"><div class=\"left\">");
			if(l.getParentListId()!=0)s2.append("<a href=# class=\"back link icon-only\"><i class=\"icon f7-icons\">left_arrow</i></a>");
			else s2.append("<a href=# class=\"open-panel link icon-only\"><i class=\"icon f7-icons\">bars</i></a>");
			s2.append("</div><div class=\"center\">").append(LocaleMsgCache.get2(listResult.getScd(), l.getLocaleMsgKey())).append("</div>");
			//if(l.getParentListId()==0)s2.append("<div class=\"right\"><a href=# id=\"idx-o-users-").append(l.getListId()).append("\" class=\"link icon-only\"><i class=\"icon material-icons\">forum<span id=\"idx-chat-badge\" class=\"badge bg-red\" style=\"display:none;\">1</span></i></a></div>");
			s2.append("</div></div>");
			
			StringBuilder s3= new StringBuilder();
			if(!GenericUtil.isEmpty(l.get_orderQueryFieldNames())){
				s3.append("<a href=# id=\"idx-sort-").append(l.getListId()).append("\"><i class=\"icon material-icons\">sort</i></a>");
			}
			if(listResult.getSearchFormResult()!=null){
				s3.append("<a href=# id=\"idx-filter-").append(l.getListId()).append("\" style=\"background-color:darkgray;\"><i class=\"icon material-icons\">search</i></a>");
			}
			if(insertFlag){
				s3.append("<a href=\"showMForm?a=2&_fid=").append(l.getDefaultCrudFormId()).append("\" class=\"item-link\" id=\"idx-insert-").append(l.getDefaultCrudFormId()).append("\" style=\"background-color:#4caf50;\"><i class=\"icon material-icons\">add</i></a>");
			}
			if(s3.length()>0)
				s2.append("<div class=\"speed-dial\"><a href=# class=\"floating-button\"><i class=\"icon icon-plus\"></i><i class=\"icon icon-close\"></i></a><div class=\"speed-dial-buttons\">").append(s3).append("</div></div>");  

			if(searchBar){
				s2.append("<form data-search-list=\".search-here\" data-search-in=\".item-title\" class=\"searchbar searchbar-init\"><div class=\"searchbar-input\"><input type=\"search\" placeholder=\"Search\"/><a href=# class=\"searchbar-clear\"></a></div></form><div class=\"searchbar-overlay\"></div>");
			}
		  
			s2.append("<div  id=\"idx-page-content-").append(l.getListId()).append("\" class=\"page-content pull-to-refresh-content");
			if(l.getDefaultPageRecordNumber()>0)s2.append(" infinite-scroll");
			if(l.getHideBarsOnScrollFlag()!=0)s2.append(" hide-bars-on-scroll");
			s2.append("\">");
			if(searchBar){
				s2.append("<div class=\"list-block searchbar-not-found\"><ul><li class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title\">Nothing found</div></div></li></ul></div>");
			}
			    
			s2.append("<div class=\"pull-to-refresh-layer\"><div class=\"preloader\"></div><div class=\"pull-to-refresh-arrow\"></div></div>");    
			
			s2.append("<div class=\"list-block").append(l.getListTip()==4?" media-list":"").append(searchBar ? " search-here searchbar-found":"").append("\"><ul id=\"idx-").append(l.getListId()).append("\">${iwb-data}</ul></div></div></div>");
			buf.append(GenericUtil.stringToJS2(s2.toString()));
		} else //PromisUtil.filterExt(f.getJsCode().substring(0, f.getJsCode().indexOf("${iwb-data}")),listResult.getScd(), listResult.getRequestParams(),null))
			buf.append(GenericUtil.stringToJS2(GenericUtil.filterExt(l.getHtmlPageCode(),listResult.getScd(), listResult.getRequestParams(),null).toString()));
		buf.append("'");
		
		if(listResult.getSearchFormResult()!=null){
			buf.append(",\n searchForm:").append(serializeGetForm(listResult.getSearchFormResult()));
		}
		
		String w = (String)listResult.getScd().get("mobileDeviceId");
		listResult.getRequestParams().put(".w", w);
		listResult.getRequestParams().put(".t", w+"-l-"+l.getListId());
		listResult.getRequestParams().put("_gid", ""+(1000000+l.getListId()));
		buf.append(",\n baseParams:").append(GenericUtil.fromMapToJsonString(listResult.getRequestParams()));
		
		
		buf.append("}");
		
		return buf;
	}
	public	StringBuilder	serializePage(W5PageResult	pageResult) {
		return null;
	}
	public	StringBuilder serializeGetForm(W5FormResult formResult){
		int deviceType = GenericUtil.uInt(formResult.getScd().get("mobile")); 
		if(deviceType!=2)return serializeGetFormiOS(formResult); //not android
		else return serializeGetFormMaterial(formResult); //andorid
	}
	
	private	StringBuilder serializeGetFormMaterial(W5FormResult formResult){
		W5Form f = formResult.getForm();
		Map scd =formResult.getScd();
		String xlocale = (String)scd.get("locale");
		int customizationId = (Integer)scd.get("customizationId");
		StringBuilder s = new StringBuilder();
		s.append("{success:true, formId:").append(f.getFormId()).append(",\n name:'")
			.append(LocaleMsgCache.get2(formResult.getScd(), f.getLocaleMsgKey()))
			.append("'");
		boolean liveSyncRecord = false, pictureFlag=false;
		
		Map<Integer, List<W5FormCellHelper>> map = new HashMap<Integer, List<W5FormCellHelper>>();
		map.put(0, new ArrayList<W5FormCellHelper>());
		if (formResult.getForm().get_moduleList() != null)
			for (W5FormModule m : formResult.getForm().get_moduleList()) {
				map.put(m.getFormModuleId(), new ArrayList<W5FormCellHelper>());
			}
		else {
			formResult.getForm().set_moduleList(new ArrayList());
		}
		for (W5FormCellHelper m : formResult.getFormCellResults())if (m.getFormCell().getActiveFlag() != 0) {
			List<W5FormCellHelper> l = map.get(m.getFormCell().getFormModuleId());
			if (l == null)l = map.get(0);
			l.add(m);
		}
		
		boolean masterDetail = false;
		
		if (f.getObjectType() == 2){
			W5Table t = FrameworkCache.getTable(scd, f.getObjectId());
			liveSyncRecord = FrameworkSetting.liveSyncRecord && t.getLiveSyncFlag() != 0 && !formResult.isViewMode();
			pictureFlag =FrameworkCache.getAppSettingIntValue(scd, "attach_picture_flag") != 0
					&& t.getFileAttachmentFlag() != 0;
			// insert AND continue control
			s.append(",\n crudTableId:").append(f.getObjectId());
			if (formResult.getAction() == 2) { // insert
				long tmpId = -GenericUtil.getNextTmpId();
				s.append(",\n contFlag:").append(f.getContEntryFlag() != 0)
						.append(",\n tmpId:").append(tmpId);
				formResult.getRequestParams().put("_tmpId", "" + tmpId);
			} else if (formResult.getAction() == 1) { // edit
				s.append(",id:'").append(formResult.getUniqueId()).append("',\n pk:")
						.append(GenericUtil.fromMapToJsonString(formResult
								.getPkFields()));
				if (liveSyncRecord) {
					s.append(",\n liveSync:true");
					String webPageId = formResult.getRequestParams().get(".w");
					if (webPageId != null) {
						String key = "";
						for (String k : formResult.getPkFields().keySet())
							if (!k.startsWith("customization"))
								key += "*" + formResult.getPkFields().get(k);
						if (key.length() > 0) {
							key = t.getTableId() + "-" + key.substring(1);
							formResult.setLiveSyncKey(key);
							List<Object> l = UserUtil
									.syncGetListOfRecordEditUsers(
											(String)scd.get("projectId"), key,
											webPageId);
							if (!GenericUtil.isEmpty(l)) {// buna duyurulacak
								s.append(",\n liveSyncBy:")
										.append(GenericUtil
												.fromListToJsonString2Recursive((List) l));
							}
						}
					}
				}

			}
//			if (pictureFlag)s.append(",\n pictureFlag:true, pictureCount:").append(formResult.getPictureCount());
			if (FrameworkCache.getAppSettingIntValue(scd, "file_attachment_flag") != 0
					&& t.getFileAttachmentFlag() != 0
					&& FrameworkCache.roleAccessControl(scd, 101))
				s.append(",\n fileAttachFlag:true, fileAttachCount:").append(
						formResult.getFileAttachmentCount());

			if(!GenericUtil.isEmpty(f.get_moduleList()) && formResult.getModuleListMap()!=null){
				s.append(",\n subLists:[");
				boolean bq = false;
				for(W5FormModule fm:f.get_moduleList())if(fm.getModuleType()==10 && (fm.getModuleViewType()==0 || formResult.getAction()==fm.getModuleViewType())){
					M5ListResult mlr = formResult.getModuleListMap().get(fm.getObjectId());
					if(mlr==null)continue;
					if(bq)s.append("\n,"); else bq=true;
					s.append(serializeListMaterial(mlr));
					masterDetail = true;
				}
				
				s.append("]");
			}
		}
		
		if (formResult.isViewMode())
			s.append(",\n viewMode:true");
		

		
		if (!formResult.getOutputMessages().isEmpty()) {
			s.append(",\n\"msgs\":[");
			boolean b = false;
			for (String sx : formResult.getOutputMessages()) {
				if (b)
					s.append("\n,");
				else
					b = true;
				s.append("'").append(GenericUtil.stringToJS(sx)).append("'");
			}
			s.append("]");
		}
		StringBuilder jsCode = new StringBuilder();
		s.append(",\n baseParams:").append(GenericUtil.fromMapToJsonString(formResult.getRequestParams()))
			.append(",\n htmlPage:'");
		
//		buf.append(PromisUtil.filterExt(fc.getExtraDefinition(), formResult.getScd(), formResult.getRequestParams(), o));

/*		if(f.getRenderTip()==5 && !GenericUtil.isEmpty(f.getJsCode())){
			s.append(GenericUtil.stringToJS(GenericUtil.filterExt(f.getJsCode().substring(0, f.getJsCode().indexOf("${iwb-data}")),formResult.getScd(), formResult.getRequestParams(),null)));
		} else */if (f.getObjectType() == 2){
			s.append("<div data-page=\"iwb-form-").append(formResult.getFormId()).append("\" class=\"page\"><div class=\"navbar\">")
				.append("<div class=\"navbar-inner iwb-navbar-form\"><div class=\"left\"><a href=# class=\"back link icon-only\"><i class=\"icon f7-icons\">left_arrow</i></a></div><div class=\"center\">")
				.append(formResult.getForm().getLocaleMsgKey()).append("</div><div class=\"right\">");
			if(pictureFlag){
				s.append("<a href=# id=\"idx-photo-").append(formResult.getFormId()).append("\" class=\"link\"><i class=\"icon f7-icons\">camera_fill<span id=\"idx-photo-badge-").append(formResult.getFormId()).append("\" class=\"badge bg-red\"");
//				if(formResult.getPictureCount()>0)s.append(">").append(formResult.getPictureCount());else 
				s.append(" style=\"display:none;\">1");
				s.append("</span></i></a>");
			}
			s.append("</div></div></div> <div class=\"page-content\"><form class=\"list-block inputs-list\" id=\"idx-form-")
				.append(formResult.getFormId()).append("\"><ul>");
		}
		  
//		    <div class="content-block-title">With Floating Labels</div>
		List<W5FormModule> ml = new ArrayList();
		boolean found = false;
		for(W5FormModule m:formResult.getForm().get_moduleList()) if(m.getFormModuleId()==0) {
			found = true;
			break;
		}
		if(!found)ml.add(new W5FormModule());
		ml.addAll(formResult.getForm().get_moduleList());
		

		for (W5FormModule m : ml){
			List<W5FormCellHelper> r = map.get(m.getFormModuleId());
			if(GenericUtil.isEmpty(r))continue;
			if(m.getFormModuleId()!=0){
				W5FormCell fcx = new W5FormCell(); fcx.setControlType((short)102);fcx.setLookupQueryId(10); fcx.setLocaleMsgKey(m.getLocaleMsgKey());
				s.append(GenericUtil.stringToJS(serializeFormCellMaterial(customizationId, xlocale,new W5FormCellHelper(fcx), formResult)));
			}
			for (W5FormCellHelper fc : r){
				s.append(GenericUtil.stringToJS(serializeFormCellMaterial(customizationId, xlocale,fc, formResult)));
				switch(fc.getFormCell().getControlType()){
				case	2://date
					jsCode.append("iwb.app.calendar({input: '#idx-formcell-").append(fc.getFormCell().getFormCellId()).append("',dateFormat: 'dd/mm/yyyy'});\n");
					break;
				case	10://autocomplete
				case	61://autocomplete-multi
					jsCode.append("iwb.app.autocomplete(iwb.apply({multiple:").append(fc.getFormCell().getControlType()==61).append(", opener: $$('#idx-formcell-").append(fc.getFormCell().getFormCellId()).append("'), params:'_qid=").append(fc.getFormCell().getLookupQueryId());
					if (fc.getFormCell().getLookupIncludedParams() != null && fc.getFormCell().getLookupIncludedParams().length() > 2)
						jsCode.append("&").append(fc.getFormCell().getLookupIncludedParams());
					jsCode.append("'}, ");
					boolean dependantCombo=false;
					for (W5FormCellHelper cfc : formResult.getFormCellResults()) {
						if (cfc.getFormCell().getParentFormCellId() == fc.getFormCell().getFormCellId()) {
							if(!GenericUtil.isEmpty(cfc.getFormCell().getLookupIncludedParams()))
								switch(cfc.getFormCell().getControlType()){
							case	9:case	16:
								jsCode.append("iwb.autoCompleteJson4Autocomplete('")
									.append(GenericUtil.isEmpty(fc.getValue()) ? "":fc.getValue()).append("','#idx-formcell-")
									.append(cfc.getFormCell().getFormCellId()).append("',function(ax,bx){\n") 
									.append(cfc.getFormCell().getLookupIncludedParams()) 
									.append("\n})));\n");
								dependantCombo=true;
								break;
							default:
								//jsCode.append("{}));\n");
	
								break;
							}
							break;

						}
					}
					if(!dependantCombo) {
						jsCode.append("iwb.autoCompleteJson));\n");
					}
					break;
				case	9:case	16:
					if (formResult != null && !GenericUtil.isEmpty(fc.getFormCell().getLookupIncludedParams()) && fc.getFormCell().getParentFormCellId() > 0) {
						for (W5FormCellHelper rfc : formResult.getFormCellResults()) {
							if (rfc.getFormCell().getFormCellId() == fc.getFormCell().getParentFormCellId()) {
								W5FormCell pfc = rfc.getFormCell();
								if (pfc.getControlType() == 6
										|| pfc.getControlType() == 7
										|| pfc.getControlType() == 9)
									jsCode.append(
											"iwb.combo2combo('#idx-formcell-").append(pfc.getFormCellId())
											.append("','#idx-formcell-").append(fc.getFormCell().getFormCellId()).append("',function(ax,bx){\n")
											.append(fc.getFormCell().getLookupIncludedParams())
											.append("\n});\n");
								break;
							}
						}
					}
				}

			}
		}

		if (f.getObjectType() == 2 && f.get_formSmsMailList() != null && !f.get_formSmsMailList().isEmpty()) { // automatic sms isleri varsa
			StringBuilder s2 = new StringBuilder();
			int cnt = 0;
			for (W5FormSmsMail fsm : f.get_formSmsMailList())
				if (((fsm.getSmsMailType() == 0 && FrameworkSetting.sms && FrameworkCache.getAppSettingIntValue(customizationId, "sms_flag") != 0) || 
						(fsm.getSmsMailType() != 0 && FrameworkSetting.mail && FrameworkCache.getAppSettingIntValue(customizationId, "mail_flag") != 0))
						&& fsm.getAlarmFlag() == 0 && fsm.getPreviewFlag()==0
						&& GenericUtil.hasPartInside2(fsm.getActionTypes(), formResult.getAction())
						&& GenericUtil.hasPartInside2(fsm.getWebMobileTips(),"2")) {
					cnt++;
				}
			if (cnt > 0) {
//				s2.append(",\n\"smsMailTemplateCnt\":").append(cnt).append(",\n\"smsMailTemplates\":[");
				boolean b = false;
				for (W5FormSmsMail fsm : f.get_formSmsMailList())
					if (((fsm.getSmsMailType() == 0 && FrameworkSetting.sms && FrameworkCache.getAppSettingIntValue(customizationId, "sms_flag") != 0) || 
						 (fsm.getSmsMailType() != 0 && FrameworkSetting.mail && FrameworkCache.getAppSettingIntValue(customizationId, "mail_flag") != 0))
							&& fsm.getAlarmFlag() == 0 && fsm.getPreviewFlag()==0
							&& GenericUtil.hasPartInside2(fsm.getActionTypes(), formResult.getAction())
							&& GenericUtil.hasPartInside2(fsm.getWebMobileTips(), "2")) {
						W5FormCell fcx = new W5FormCell(); fcx.setControlType((short)5);fcx.setLookupQueryId(10); 
						fcx.setLocaleMsgKey((fsm.getSmsMailType() == 0 ? "[SMS] " : "[" + (LocaleMsgCache.get2(customizationId, xlocale, "email_upper")) + "] ") + LocaleMsgCache.get2(customizationId, xlocale, fsm.getDsc())
						+ (fsm.getPreviewFlag() != 0 ? " (" + (LocaleMsgCache.get2(customizationId, xlocale, "with_preview")) + ")" : ""));
						fcx.setLookupQueryId(fsm.getFormSmsMailId()); fcx.setDsc("_smsStr");
						W5FormCellHelper fcr = new W5FormCellHelper(fcx);
						if(fsm.getSmsMailSentType() == 1 || fsm.getSmsMailSentType() == 0) fcr.setValue("1");
						s2.append(GenericUtil.stringToJS(serializeFormCellMaterial(customizationId, xlocale, fcr, formResult)));
						
						/*if (b)s2.append("\n,");
						else b = true;
						s2.append("{\"xid\":")
								.append(fsm.getFormSmsMailId())
								.append(",\"text\":\"")
								.append(fsm.getSmsMailTip() == 0 ? "[SMS] " : "[" + (PromisLocaleMsg.get2(customizationId, xlocale, "email_upper")) + "] ")
								.append(PromisLocaleMsg.get2(customizationId, xlocale, fsm.getDsc()))
								.append(fsm.getPreviewFlag() != 0 ? " (" + (PromisLocaleMsg.get2(customizationId, xlocale, "with_preview")) + ")" : "")
								.append("\",\"checked\":")
								.append(fsm.getSmsMailSentTip() == 1 || fsm.getSmsMailSentTip() == 0)
								.append(",\"smsMailTip\":")
								.append(fsm.getSmsMailTip())
								.append(",\"previewFlag\":")
						
								.append(fsm.getPreviewFlag() != 0);
						if (fsm.getSmsMailSentTip() == 0)
							s2.append(",\"disabled\":true");
						s2.append("}"); */
					}
			//	s2.append("]");
			}
			if(s2.length()>0){
					W5FormCell fcx = new W5FormCell(); fcx.setControlType((short)102);fcx.setLookupQueryId(11); fcx.setLocaleMsgKey("SMS/E-Posta Dönüşümleri");
					s.append(GenericUtil.stringToJS(serializeFormCellMaterial(customizationId, xlocale,new W5FormCellHelper(fcx), formResult)));
					s.append(s2);
			}
/*
			cnt = 0; //alarm control
			for (W5FormSmsMail fsm : f.get_formSmsMailList()) 
				if (((fsm.getSmsMailTip() == 0 && PromisSetting.sms && PromisCache.getAppSettingIntValue(customizationId, "sms_flag") != 0) 
						|| (fsm.getSmsMailTip() != 0 && PromisSetting.mail && PromisCache.getAppSettingIntValue(customizationId, "mail_flag") != 0))
						&& fsm.getAlarmFlag() != 0 && fsm.getPreviewFlag()==0
						&& PromisUtil.hasPartInside2(fsm.getActionTips(), formResult.getAction())
						&& PromisUtil.hasPartInside2(fsm.getWebMobileTips(),"2")) {
					cnt++;
				}
			if (cnt > 0) {
				Map<Integer, W5FormSmsMailAlarm> alarmMap = new HashMap();
				if (!PromisUtil.isEmpty(formResult.getFormAlarmList()))
					for (W5FormSmsMailAlarm a : formResult.getFormAlarmList()) {
						alarmMap.put(a.getFormSmsMailId(), a);
					}
			//	s2.append(",\n\"alarmTemplateCnt\":").append(cnt++).append(",\n\"alarmTemplates\":[");
				boolean b = false;
				for (W5FormSmsMail fsm : f.get_formSmsMailList())
					if (((fsm.getSmsMailTip() == 0 && PromisSetting.sms && PromisCache.getAppSettingIntValue(customizationId, "sms_flag") != 0) || 
							(fsm.getSmsMailTip() != 0 && PromisSetting.mail && PromisCache.getAppSettingIntValue(customizationId, "mail_flag") != 0))
							&& fsm.getAlarmFlag() != 0 && fsm.getPreviewFlag()==0
							&& PromisUtil.hasPartInside2(fsm.getActionTips(), formResult.getAction())
							&& PromisUtil.hasPartInside2(fsm.getWebMobileTips(), "2")) {
						W5FormSmsMailAlarm a = alarmMap.get(fsm.getFormSmsMailId());
						W5FormCell fcx = new W5FormCell(); fcx.setControlTip((short)5);fcx.setLookupQueryId(10); 
						fcx.setLocaleMsgKey((fsm.getSmsMailTip() == 0 ? "[SMS] " : "[" + (PromisLocaleMsg.get2(customizationId, xlocale, "email_upper")) + "] ") + PromisLocaleMsg.get2(customizationId, xlocale, fsm.getDsc())
						+ (fsm.getPreviewFlag() != 0 ? " (" + (PromisLocaleMsg.get2(customizationId, xlocale, "with_preview")) + ")" : ""));
						fcx.setLookupQueryId(fsm.getFormSmsMailId()); fcx.setDsc("_rrr");
						W5FormCellHelper fcr = new W5FormCellHelper(fcx);
						if(a != null || fsm.getSmsMailSentTip() == 1 || fsm.getSmsMailSentTip() == 0) fcr.setValue("1");
						s2.append(PromisUtil.stringToJS(serializeFormCell(customizationId, xlocale, fcr, formResult)));


					}
			//	s2.append("]");
			}
			*/
		}

/*		if(f.getRenderTip()==5 && !GenericUtil.isEmpty(f.getJsCode())){
			s.append(GenericUtil.stringToJS(f.getJsCode().substring(f.getJsCode().indexOf("${iwb-data}")+11)));
		} else */if (f.getObjectType() == 2){
			s.append("</ul>");
			if (!formResult.isViewMode()){//kaydet butonu
				if(masterDetail) //master detail
					s.append("<div class=\"content-block\"><p class=\"buttons-row\"><a href=# class=\"button button-big button-fill button-raised color-blue\" id=\"iwb-continue-").append(formResult.getFormId()).append("\">Next</a></p></div>");
				else
					s.append("<div class=\"content-block\"><p class=\"buttons-row\"><a href=# class=\"button button-big button-fill button-raised color-blue\" id=\"iwb-submit-").append(formResult.getFormId()).append("\">Save</a></p></div>");
			}
			s.append("</form></div></div>");
			
		}
//		for(W5FormCe){}
		s.append("'");
		if(!GenericUtil.isEmpty(f.getJsCode())){
			jsCode.append("\n").append(f.getJsCode()).append("\n");
		}
		if(jsCode.length()>0){
			StringBuilder  bx= new StringBuilder();
			bx.append("if(!callAttributes.json)callAttributes.json={};callAttributes.json.postInit=function(j){\n").append(jsCode).append("\n}\n;");
			jsCode = bx;
		}

		if(jsCode.length()>0)s.append(",\n init:function(callAttributes){\n")
			.append(jsCode).append("\n}");
		s.append("}");
		return s;
	}
	@SuppressWarnings("unchecked")
	private StringBuilder serializeFormCellMaterial(int customizationId,
			String xlocale, W5FormCellHelper cellResult, W5FormResult formResult) {
		W5FormCell fc = cellResult.getFormCell();
		String value = cellResult.getValue(); // bu ilerde hashmap ten gelebilir
		// int customizationId =
		// PromisUtil.uInt(formResult.getScd().get("customizationId"));
		StringBuilder buf = new StringBuilder();

		String fieldLabel = LocaleMsgCache.get2(customizationId, xlocale, fc.getLocaleMsgKey());
		String readOnly = cellResult.getHiddenValue() != null ? " readonly style=\"background-color:#eee;\"":"";
		String notNull = cellResult.getHiddenValue()==null && fc.getNotNullFlag()!=0 ? " style=\"color:red\"":"";
		
		if(!GenericUtil.isEmpty(fc.getExtraDefinition())){
			Map o = new HashMap();
			o.put("value", value);
			o.put("name", fc.getDsc());
			o.put("label", fieldLabel);
			o.put("readOnly", readOnly);
			o.put("notNull", notNull);
			buf.append(GenericUtil.filterExt(fc.getExtraDefinition(), formResult.getScd(), formResult.getRequestParams(), o));
			return buf;
			
		}
		if ((fc.getControlType() == 101 || cellResult.getHiddenValue() != null)/* && (fc.getControlTip()!=9 && fc.getControlTip()!=16) */) { //readonly
			buf.append("<li id=\"id-formcell-").append(fc.getFormCellId()).append("\"><div class=\"item-content\"><div class=\"item-inner\">")
			.append("<div class=\"item-title label\">").append(fieldLabel).append("</div>")
			.append("<div class=\"item-after\"><b>").append(GenericUtil.stringToHtml(value)).append("</b>");;
			buf.append("</div></div></div></li>");
			return buf;
		}
		
		switch(fc.getControlType()){
		case	102://label
			if(fc.getLookupQueryId()>=10){
				buf.append("<li class=\"iwb-form-tab iwb-type-").append(fc.getLookupQueryId()).append("\"><div class=\"content-block-title\" style=\"height: 30px;text-align:center;\">").append(GenericUtil.uStrNvl(value, fieldLabel)).append("</div></li>");
			} else {
			 buf.append("<li><div class=\"content-block-title iwb-label-").append(fc.getLookupQueryId()).append("\"><i class=\"icon material-icons\" style=\"margin-top: -4px;\">").append(labelMap[fc.getLookupQueryId()]).append("</i>&nbsp; ").append(GenericUtil.uStrNvl(value, fieldLabel)).append("</div></li>");
			}
			break;
		case	1:case	3:case	4: case 21://string, integer, double, localeMsgKey
		case	19: //ozel string
			buf.append("<li id=\"id-formcell-").append(fc.getFormCellId()).append("\"><div class=\"item-content\"><div class=\"item-inner\">")
			.append("<div class=\"item-title floating-label\"").append(notNull).append(">").append(fieldLabel).append("</div>")
			.append("<div class=\"item-input\"><input type=\"text\" name=\"").append(fc.getDsc()).append("\"").append(readOnly);
			if(!GenericUtil.isEmpty(value))buf.append(" value=\"").append(value).append("\"");
			buf.append(" placeholder=\"\">")
			.append("</div></div></div></li>");
			return buf;
		case	2://date
			buf.append("<li id=\"id-formcell-").append(fc.getFormCellId()).append("\"><div class=\"item-content\"><div class=\"item-inner\">")
			.append("<div class=\"item-title floating-label\">").append(fieldLabel).append("</div>")
			.append("<div class=\"item-input\"><input type=\"text\" readonly name=\"").append(fc.getDsc()).append("\"").append(" id=\"idx-formcell-").append(fc.getFormCellId()).append("\"").append(readOnly);
			if(!GenericUtil.isEmpty(value))buf.append(" value=\"").append(value).append("\"");
			buf.append(" placeholder=\"\">")
			.append("</div></div></div></li>");
			return buf;			
		case	10://autocomplete
		case	61://autocomplete-multi
			buf.append("<li id=\"id-formcell-").append(fc.getFormCellId()).append("\"><a href=# id=\"idx-formcell-").append(fc.getFormCellId()).append("\" class=\"item-link autocomplete-opener\"><input type=\"hidden\" name=\"").append(fc.getDsc()).append("\"");
			if(!GenericUtil.isEmpty(value))buf.append(" value=\"").append(value).append("\"");
			buf.append("><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title label\">").append(fieldLabel).append("</div>")
			.append("<div class=\"item-after\">");
			if (value != null && cellResult.getLookupQueryResult() != null && cellResult.getLookupQueryResult().getData().size() > 0) {
				Object[] oo = cellResult.getLookupQueryResult().getData().get(0);
				buf.append(oo[0]);
			}
			
			buf.append("</div></div></div></a></li>");
			return buf;			
		case	6: case 7://static, query combo
		case	8: case	15://lov-static, lov-query combo
		case	58: case	59://superbox lov-static, superbox lov-query combo
		case    51: case 52://user defined combo, multi
			boolean multi = fc.getControlType()!=6 && fc.getControlType()!=7 && fc.getControlType()!=51;
			StringBuilder resultText = new StringBuilder();
			buf.append("<li id=\"id-formcell-").append(fc.getFormCellId()).append("\"><a href=#").append(multi ? "":" data-back-on-select=\"true\"").append(" class=\"item-link smart-select\"")
				.append((cellResult.getLookupListValues() != null && cellResult.getLookupListValues().size()>40) || (cellResult.getLookupQueryResult() != null && cellResult.getLookupQueryResult().getData().size()>40) ? " data-virtual-list=\"true\"" : "")
				.append(" data-searchbar=\"true\" data-searchbar-placeholder=\"Ara..\">")
				.append("<select id=\"idx-formcell-").append(fc.getFormCellId()).append("\" name=\"").append(fc.getDsc()).append("\"").append(multi ? " multiple":"").append(">");
			if(fc.getNotNullFlag()==0 && !multi)buf.append("<option value=\"\"></option>");
			if (cellResult.getLookupListValues() != null) { //lookup static
				for (W5Detay p : (List<W5Detay>) cellResult.getLookupListValues()) {
					buf.append("<option value=\"").append(p.getVal()).append("\"");
					if((!multi && GenericUtil.safeEquals(value, p.getVal())) || (multi && GenericUtil.hasPartInside2(value, p.getVal()))){
						buf.append(" selected");
						resultText.append(cellResult.getLocaleMsgFlag() != 0 ? LocaleMsgCache.get2(customizationId, xlocale, p.getDsc()) : p.getDsc()).append(", ");
					}
					buf.append(">").append(cellResult.getLocaleMsgFlag() != 0 ? LocaleMsgCache.get2(customizationId, xlocale, p.getDsc()) : p.getDsc()).append("</option>");
				}
			} else if (cellResult.getLookupQueryResult() != null) { // QueryResult'tan geliyor
				for (Object[] p : cellResult.getLookupQueryResult().getData()) {
					buf.append("<option value=\"").append(p[1]).append("\"");
					if((!multi && GenericUtil.safeEquals(value, p[1])) || (multi && GenericUtil.hasPartInside2(value, p[1]))){
						buf.append(" selected");
						resultText.append(p[0]).append(", ");
					}
					buf.append(">").append(p[0]).append("</option>");
				}
			}
			buf.append("</select><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title label\"").append(notNull).append(">").append(fieldLabel).append("</div><div class=\"item-after\">");
			if(resultText.length()>0){
				resultText.setLength(resultText.length()-2);
				buf.append(resultText);
			}
			buf.append("</div></div></div></a></li>");
			return buf;
			
		case	9://combo-remote
		case	16://lovcombo-remote
			multi = fc.getControlType()==16;
			buf.append("<li id=\"id-formcell-").append(fc.getFormCellId()).append("\"><a href=#").append(multi ? "":" data-back-on-select=\"true\"").append(" class=\"item-link smart-select\"")
				.append(" data-searchbar=\"true\" data-searchbar-placeholder=\"Ara..\">")
				.append("<select id=\"idx-formcell-").append(fc.getFormCellId()).append("\" name=\"").append(fc.getDsc()).append("\"").append(multi ? " multiple":"");
			if(!GenericUtil.isEmpty(value))buf.append(" iwb-value=\"").append(value).append("\"");
			buf.append(">");
			buf.append("</select><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title label\"").append(notNull).append(">").append(fieldLabel).append("</div><div class=\"item-after\">");
			buf.append("</div></div></div></a></li>");
			return buf;
		case	11://textarea
		case 41: case 25: //codemirror, ozel tanimlama textarea
			buf.append("<li id=\"id-formcell-").append(fc.getFormCellId()).append("\" class=\"align-top\"><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title floating-label\"").append(notNull).append(">").append(fieldLabel)
			.append("</div><div class=\"item-input\"><textarea").append(readOnly).append(" name=\"").append(fc.getDsc()).append("\" class=\"resizable\">").append(value!=null ? value:"").append("</textarea></div></div></div></li>");
			return buf;
		case	5://checkbox
			if(fc.getLookupQueryId()==0){
				buf.append("<li id=\"id-formcell-").append(fc.getFormCellId()).append("\"><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-input\"><label class=\"label-switch\"><input type=\"checkbox\"").append(GenericUtil.uInt(value)!=0 ?" checked":"").append(" name=\"").append(fc.getDsc())
				.append("\"");
				if(fc.getLookupQueryId()!=0)buf.append(" value=").append(fc.getLookupQueryId());
				buf.append("/><div class=\"checkbox\"></div><div class=\"item-title\" style=\"margin-top:-18px; margin-left:50px; color: rgba(0, 0, 0, 0.7);overflow: inherit;font-size: 15px;\">")
				.append(fieldLabel).append("</div></label></div></div></div></li>");
				
				/*
				buf.append("<li><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title label\">")
				.append(fieldLabel).append("</div><div class=\"item-input\"><label class=\"label-switch\"><input type=\"checkbox\"").append(PromisUtil.uInt(value)!=0 ?" checked":"").append(" name=\"").append(fc.getDsc())
				.append("\"/><div class=\"checkbox\"></div></label></div></div></div></li>");			 */
				return buf;
			} else {
				buf.append("<li style=\"top: 0px;\"><label class=\"label-checkbox item-content\"><input type=\"checkbox\" name=\"").append(fc.getDsc())
				.append("\"").append(GenericUtil.uInt(value)!=0 ?" checked":"").append(" value=").append(fc.getLookupQueryId()).append("><div class=\"item-media\"><i class=\"icon icon-form-checkbox\"></i></div><div class=\"item-inner\"><div class=\"item-title\" style=\"color: #757575;margin-top: 8px;font-size: 15px;\">")
				.append(fieldLabel).append("</div></div></label></li>");
				return buf;
				
			}
		}
		return buf;
	}
	
	public	StringBuilder serializeFormFromJSON(JSONObject formBuilder) throws JSONException{
		StringBuilder buf = new StringBuilder();
		W5FormResult r = new W5FormResult(1);r.setScd(new HashMap());r.setRequestParams(new HashMap());
		buf.append("{\"success\":true,\n \"formBuilder\":{");
	/*
{"title":"FORM PREVIEW -> (unnamed)","labelWidth":100,"labelAlign":"right","xtype":"form","bodyStyle":"padding:7px","items":[
{"xtype":"textfield","xorder_id":0,"xname":"asd23dd","name":"asd23dd","fieldLabel":"asd23dd","labelSeparator":"","listeners":{},"width":200,"value":""},
{"xtype":"datefield","xorder_id":1,"xname":"fqwwwww3gqwgwww","name":"fqwwwww3gqwgwww","fieldLabel":"fqwwwww3gqwgwww","labelSeparator":"","listeners":{},"width":200,"value":""},
{"xtype":"numberfield","xorder_id":2,"xname":"eewwddf","name":"eewwddf","fieldLabel":"eewwddf","labelSeparator":"","listeners":{},"width":200,allowBlank:false, "value":""},
{"xtype":"textarea","xorder_id":3,"xname":"wgqwggwq","name":"wgqwggwq","fieldLabel":"wgqwggwq","labelSeparator":"","listeners":{},"width":500,"value":""},
{"xtype":"combo","xorder_id":4,"xname":"jjjjjjjj","name":"jjjjjjjj","fieldLabel":"jjjjjjjj","labelSeparator":"","listeners":{},"width":200,"value":"","store":[[1,"Value 1"],[2,"Value 2"],[3,"etc..."]]}]}:	
	 */
		StringBuilder jsCode = new StringBuilder();
		buf.append("\n htmlPage:'");
		W5FormCell c0 = new W5FormCell();
		c0.setLocaleMsgKey(formBuilder.getString("title"));c0.setControlType((short)102);
		W5FormCellHelper cellResult0 = new W5FormCellHelper(c0);
		buf.append(serializeFormCellMaterial(0,"tr", cellResult0, r));
		
		if(formBuilder.getJSONArray("items")!=null){
			JSONArray items = formBuilder.getJSONArray("items");
			for(int qi=0;qi<items.length();qi++){
				JSONObject o = items.getJSONObject(qi);
				W5FormCell c = new W5FormCell();
				W5FormCellHelper cellResult = new W5FormCellHelper(c);
				if(o.has("store")){
					JSONArray storeItems = o.getJSONArray("store");
					cellResult.setLookupListValues(new ArrayList());
					for(int jq=0;jq<storeItems.length();jq++){
						W5LookUpDetay d = new W5LookUpDetay();
						JSONArray i = storeItems.getJSONArray(jq);
						try{
							d.setVal(""+i.getInt(0));
						} catch (Exception e){d.setVal(i.getString(0));}
						d.setDsc(i.getString(1));
						cellResult.getLookupListValues().add(d);
					}
					c.setControlType((short)6);;
				} else
					c.setControlType(o.has("_controlTip") ? (short)o.getInt("_controlTip") : 1);;
				if(c.getControlType()==2){
					int formCellId = (int)new Date().getTime();
					jsCode.append("iwb.app.calendar({input: '#idx-formcell-").append(formCellId).append("',dateFormat: 'dd/mm/yyyy'});\n");
					c.setFormCellId(formCellId);
				}
				c.setLocaleMsgKey(o.getString("fieldLabel"));;
				c.setDsc(o.getString("name"));;
				cellResult.setValue("{{"+c.getDsc()+"}}");
				if(o.has("allowBlank"))c.setNotNullFlag((short)(o.getBoolean("allowBlank") ? 0:1));

					
				c.setControlWidth((short)o.getInt("width"));;
				c.setLocaleMsgKey(o.getString("fieldLabel"));;
				buf.append(serializeFormCellMaterial(0,"tr", cellResult, r)); 
			}			
		}
		buf.append("'");
		if(jsCode.length()>0)buf.append(",\n init:function(callAttributes){\n")
		.append(jsCode).append("\n}");

		
		
		buf.append("}}");
		return buf;
	}
	
	@SuppressWarnings("unchecked")
	private StringBuilder serializeFormCelliOS(int customizationId,
			String xlocale, W5FormCellHelper cellResult, W5FormResult formResult) {
		W5FormCell fc = cellResult.getFormCell();
		String value = cellResult.getValue(); // bu ilerde hashmap ten gelebilir
		// int customizationId =
		// PromisUtil.uInt(formResult.getScd().get("customizationId"));
		StringBuilder buf = new StringBuilder();

		String fieldLabel = LocaleMsgCache.get2(customizationId, xlocale, fc.getLocaleMsgKey());
		String readOnly = cellResult.getHiddenValue() != null ? " readonly style=\"background-color:#eee;\"":"";
		String notNull = cellResult.getHiddenValue()==null && fc.getNotNullFlag()!=0 ? " style=\"color:red\"":"";
		
		if(!GenericUtil.isEmpty(fc.getExtraDefinition())){
			Map o = new HashMap();
			o.put("value", value);
			o.put("name", fc.getDsc());
			o.put("label", fieldLabel);
			o.put("readOnly", readOnly);
			o.put("notNull", notNull);
			buf.append(GenericUtil.filterExt(fc.getExtraDefinition(), formResult.getScd(), formResult.getRequestParams(), o));
			return buf;
			
		}
		if ((fc.getControlType() == 101 || cellResult.getHiddenValue() != null)/* && (fc.getControlTip()!=9 && fc.getControlTip()!=16) */) { //readonly
			buf.append("<li id=\"id-formcell-").append(fc.getFormCellId()).append("\"><div class=\"item-content\"><div class=\"item-inner\">")
			.append("<div class=\"item-title label\">").append(fieldLabel).append("</div>")
			.append("<div class=\"item-after\"><b>").append(GenericUtil.stringToHtml(value)).append("</b>");;
			buf.append("</div></div></div></li>");
			return buf;
		}
		
		switch(fc.getControlType()){
		case	102://label
			if(fc.getLookupQueryId()>=10){
				buf.append("<li class=\"iwb-form-tab iwb-type-").append(fc.getLookupQueryId()).append("\"><div class=\"content-block-title\" style=\"height: 30px;text-align:center;\">").append(GenericUtil.uStrNvl(value, fieldLabel)).append("</div></li>");
			} else {
			 buf.append("<li><div class=\"content-block-title iwb-label-").append(fc.getLookupQueryId()).append("\"><i class=\"icon f7-icons\" style=\"margin-top: -4px;\">").append(labelMap[fc.getLookupQueryId()]).append("</i>&nbsp; ").append(GenericUtil.uStrNvl(value, fieldLabel)).append("</div></li>");
			}
			break;
		case	1:case	3:case	4: case 21://string, integer, double, localeMsgKey
		case	19: //ozel string
			buf.append("<li id=\"id-formcell-").append(fc.getFormCellId()).append("\"><div class=\"item-content\"><div class=\"item-inner\">")
			.append("<div class=\"item-title label\"").append(notNull).append(">").append(fieldLabel).append("</div>")
			.append("<div class=\"item-input\"><input type=\"text\" name=\"").append(fc.getDsc()).append("\"").append(readOnly);
			if(!GenericUtil.isEmpty(value))buf.append(" value=\"").append(value).append("\"");
			buf.append(" placeholder=\"\">")
			.append("</div></div></div></li>");
			return buf;
		case	2://date
			buf.append("<li id=\"id-formcell-").append(fc.getFormCellId()).append("\"><div class=\"item-content\"><div class=\"item-inner\">")
			.append("<div class=\"item-title label\">").append(fieldLabel).append("</div>")
			.append("<div class=\"item-input\"><input type=\"text\" readonly name=\"").append(fc.getDsc()).append("\"").append(" id=\"idx-formcell-").append(fc.getFormCellId()).append("\"").append(readOnly);
			if(!GenericUtil.isEmpty(value))buf.append(" value=\"").append(value).append("\"");
			buf.append(" placeholder=\"\">")
			.append("</div></div></div></li>");
			return buf;			
		case	10://autocomplete
		case	61://autocomplete-multi
			buf.append("<li id=\"id-formcell-").append(fc.getFormCellId()).append("\"><a href=# id=\"idx-formcell-").append(fc.getFormCellId()).append("\" class=\"item-link autocomplete-opener\"><input type=\"hidden\" name=\"").append(fc.getDsc()).append("\"");
			if(!GenericUtil.isEmpty(value))buf.append(" value=\"").append(value).append("\"");
			buf.append("><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title label\">").append(fieldLabel).append("</div>")
			.append("<div class=\"item-after\">");
			if (value != null && cellResult.getLookupQueryResult() != null && cellResult.getLookupQueryResult().getData().size() > 0) {
				Object[] oo = cellResult.getLookupQueryResult().getData().get(0);
				buf.append(oo[0]);
			}
			
			buf.append("</div></div></div></a></li>");
			return buf;			
		case	6: case 7://static, query combo
		case	8: case	15://lov-static, lov-query combo
		case	58: case	59://superbox lov-static, superbox lov-query combo
		case    51: case 52://user defined combo, multi
			boolean multi = fc.getControlType()!=6 && fc.getControlType()!=7 && fc.getControlType()!=51;
			StringBuilder resultText = new StringBuilder();
			buf.append("<li id=\"id-formcell-").append(fc.getFormCellId()).append("\"><a href=#").append(multi ? "":" data-back-on-select=\"true\"").append(" class=\"item-link smart-select\"")
				.append((cellResult.getLookupListValues() != null && cellResult.getLookupListValues().size()>40) || (cellResult.getLookupQueryResult() != null && cellResult.getLookupQueryResult().getData().size()>40) ? " data-virtual-list=\"true\"" : "")
				.append(" data-searchbar=\"true\" data-searchbar-placeholder=\"Ara..\">")
				.append("<select id=\"idx-formcell-").append(fc.getFormCellId()).append("\" name=\"").append(fc.getDsc()).append("\"").append(multi ? " multiple":"").append(">");
			if(fc.getNotNullFlag()==0 && !multi)buf.append("<option value=\"\"></option>");
			if (cellResult.getLookupListValues() != null) { //lookup static
				for (W5Detay p : (List<W5Detay>) cellResult.getLookupListValues()) {
					buf.append("<option value=\"").append(p.getVal()).append("\"");
					if((!multi && GenericUtil.safeEquals(value, p.getVal())) || (multi && GenericUtil.hasPartInside2(value, p.getVal()))){
						buf.append(" selected");
						resultText.append(cellResult.getLocaleMsgFlag() != 0 ? LocaleMsgCache.get2(customizationId, xlocale, p.getDsc()) : p.getDsc()).append(", ");
					}
					buf.append(">").append(cellResult.getLocaleMsgFlag() != 0 ? LocaleMsgCache.get2(customizationId, xlocale, p.getDsc()) : p.getDsc()).append("</option>");
				}
			} else if (cellResult.getLookupQueryResult() != null) { // QueryResult'tan geliyor
				for (Object[] p : cellResult.getLookupQueryResult().getData()) {
					buf.append("<option value=\"").append(p[1]).append("\"");
					if((!multi && GenericUtil.safeEquals(value, p[1])) || (multi && GenericUtil.hasPartInside2(value, p[1]))){
						buf.append(" selected");
						resultText.append(p[0]).append(", ");
					}
					buf.append(">").append(p[0]).append("</option>");
				}
			}
			buf.append("</select><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title label\"").append(notNull).append(">").append(fieldLabel).append("</div><div class=\"item-after\">");
			if(resultText.length()>0){
				resultText.setLength(resultText.length()-2);
				buf.append(resultText);
			}
			buf.append("</div></div></div></a></li>");
			return buf;
			
		case	9://combo-remote
		case	16://lovcombo-remote
			multi = fc.getControlType()==16;
			buf.append("<li id=\"id-formcell-").append(fc.getFormCellId()).append("\"><a href=#").append(multi ? "":" data-back-on-select=\"true\"").append(" class=\"item-link smart-select\"")
				.append(" data-searchbar=\"true\" data-searchbar-placeholder=\"Ara..\">")
				.append("<select id=\"idx-formcell-").append(fc.getFormCellId()).append("\" name=\"").append(fc.getDsc()).append("\"").append(multi ? " multiple":"");
			if(!GenericUtil.isEmpty(value))buf.append(" iwb-value=\"").append(value).append("\"");
			buf.append(">");
			buf.append("</select><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title label\"").append(notNull).append(">").append(fieldLabel).append("</div><div class=\"item-after\">");
			buf.append("</div></div></div></a></li>");
			return buf;
		case	11://textarea
		case 41: case 25: //codemirror, ozel tanimlama textarea
			buf.append("<li id=\"id-formcell-").append(fc.getFormCellId()).append("\" class=\"align-top\"><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title label\"").append(notNull).append(">").append(fieldLabel)
			.append("</div><div class=\"item-input\"><textarea").append(readOnly).append(" name=\"").append(fc.getDsc()).append("\" class=\"resizable\">").append(value!=null ? value:"").append("</textarea></div></div></div></li>");
			return buf;
		case	5://checkbox
			buf.append("<li id=\"id-formcell-").append(fc.getFormCellId()).append("\"><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-input\"><label class=\"label-switch\"><input type=\"checkbox\"").append(GenericUtil.uInt(value)!=0 ?" checked":"").append(" name=\"").append(fc.getDsc())
			.append("\"/><div class=\"checkbox\"></div><div class=\"item-title iwb-checkbox-label\">")
			.append(fieldLabel).append("</div></label></div></div></div></li>");
			
			/*
			buf.append("<li><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title label\">")
			.append(fieldLabel).append("</div><div class=\"item-input\"><label class=\"label-switch\"><input type=\"checkbox\"").append(PromisUtil.uInt(value)!=0 ?" checked":"").append(" name=\"").append(fc.getDsc())
			.append("\"/><div class=\"checkbox\"></div></label></div></div></div></li>");			 */
			return buf;
		}
		return buf;
	}
	
	private	StringBuilder serializeGetFormiOS(W5FormResult formResult){
		W5Form f = formResult.getForm();
		Map scd =formResult.getScd();
		String xlocale = (String)scd.get("locale");
		int customizationId = (Integer)scd.get("customizationId");
		StringBuilder s = new StringBuilder();
		s.append("{success:true, formId:").append(f.getFormId()).append(",\n name:'")
			.append(LocaleMsgCache.get2(formResult.getScd(), f.getLocaleMsgKey()))
			.append("'");
		boolean liveSyncRecord = false, pictureFlag=false;
		
		Map<Integer, List<W5FormCellHelper>> map = new HashMap<Integer, List<W5FormCellHelper>>();
		map.put(0, new ArrayList<W5FormCellHelper>());
		if (formResult.getForm().get_moduleList() != null)
			for (W5FormModule m : formResult.getForm().get_moduleList()) {
				map.put(m.getFormModuleId(), new ArrayList<W5FormCellHelper>());
			}
		else {
			formResult.getForm().set_moduleList(new ArrayList());
		}
		for (W5FormCellHelper m : formResult.getFormCellResults())if (m.getFormCell().getActiveFlag() != 0) {
			List<W5FormCellHelper> l = map.get(m.getFormCell().getFormModuleId());
			if (l == null)l = map.get(0);
			l.add(m);
		}
		
		boolean masterDetail = false;
		if (f.getObjectType() == 2){
			W5Table t = FrameworkCache.getTable(scd, f.getObjectId());
			liveSyncRecord = FrameworkSetting.liveSyncRecord && t.getLiveSyncFlag() != 0 && !formResult.isViewMode();
			pictureFlag = FrameworkCache.getAppSettingIntValue(scd, "attach_picture_flag") != 0
					&& t.getFileAttachmentFlag() != 0;
			// insert AND continue control
			s.append(",\n crudTableId:").append(f.getObjectId());
			if (formResult.getAction() == 2) { // insert
				long tmpId = -GenericUtil.getNextTmpId();
				s.append(",\n contFlag:").append(f.getContEntryFlag() != 0)
						.append(",\n tmpId:").append(tmpId);
				formResult.getRequestParams().put("_tmpId", "" + tmpId);
			} else if (formResult.getAction() == 1) { // edit
				s.append(",id:'").append(formResult.getUniqueId()).append("',\n pk:")
						.append(GenericUtil.fromMapToJsonString(formResult
								.getPkFields()));
				if (liveSyncRecord) {
					s.append(",\n liveSync:true");
					String webPageId = formResult.getRequestParams().get(".w");
					if (webPageId != null) {
						String key = "";
						for (String k : formResult.getPkFields().keySet())
							if (!k.startsWith("customization"))
								key += "*" + formResult.getPkFields().get(k);
						if (key.length() > 0) {
							key = t.getTableId() + "-" + key.substring(1);
							formResult.setLiveSyncKey(key);
							List<Object> l = UserUtil
									.syncGetListOfRecordEditUsers((String)scd.get("projectId"), key,
											webPageId);
							if (!GenericUtil.isEmpty(l)) {// buna duyurulacak
								s.append(",\n liveSyncBy:")
										.append(GenericUtil
												.fromListToJsonString2Recursive((List) l));
							}
						}
					}
				}

			}
//			if (pictureFlag)s.append(",\n pictureFlag:true, pictureCount:").append(formResult.getPictureCount());
			if (FrameworkCache.getAppSettingIntValue(scd, "file_attachment_flag") != 0
					&& t.getFileAttachmentFlag() != 0
					&& FrameworkCache.roleAccessControl(scd, 101))
				s.append(",\n fileAttachFlag:true, fileAttachCount:").append(
						formResult.getFileAttachmentCount());

			if(!GenericUtil.isEmpty(f.get_moduleList()) && formResult.getModuleListMap()!=null){
				s.append(",\n subLists:[");
				boolean bq = false;
				for(W5FormModule fm:f.get_moduleList())if(fm.getModuleType()==10 && (fm.getModuleViewType()==0 || formResult.getAction()==fm.getModuleViewType())){
					M5ListResult mlr = formResult.getModuleListMap().get(fm.getObjectId());
					if(mlr==null)continue;
					if(bq)s.append("\n,"); else bq=true;
					s.append(serializeListiOS(mlr));
					masterDetail = true;
				}
				
				s.append("]");
			}
		}
		
		if (formResult.isViewMode())
			s.append(",\n viewMode:true");
		
		if (!formResult.getOutputMessages().isEmpty()) {
			s.append(",\n\"msgs\":[");
			boolean b = false;
			for (String sx : formResult.getOutputMessages()) {
				if (b)
					s.append("\n,");
				else
					b = true;
				s.append("'").append(GenericUtil.stringToJS(sx)).append("'");
			}
			s.append("]");
		}
		StringBuilder jsCode = new StringBuilder();
		s.append(",\n baseParams:").append(GenericUtil.fromMapToJsonString(formResult.getRequestParams()))
			.append(",\n htmlPage:'");
		
//		buf.append(PromisUtil.filterExt(fc.getExtraDefinition(), formResult.getScd(), formResult.getRequestParams(), o));

/*		if(f.getRenderTip()==5 && !GenericUtil.isEmpty(f.getJsCode())){
			s.append(GenericUtil.stringToJS(GenericUtil.filterExt(f.getJsCode().substring(0, f.getJsCode().indexOf("${iwb-data}")),formResult.getScd(), formResult.getRequestParams(),null)));
		} else */if (f.getObjectType() == 2){
			s.append("<div data-page=\"iwb-form-").append(formResult.getFormId()).append("\" class=\"page\"><div class=\"navbar\">")
				.append("<div class=\"navbar-inner iwb-navbar-form\"><div class=\"left\"><a href=# class=\"back link icon-only\"><i class=\"icon f7-icons\">left_arrow</i></a></div><div class=\"center\">")
				.append(formResult.getForm().getLocaleMsgKey()).append("</div><div class=\"right\">");
			if(pictureFlag){
				s.append("<a href=# id=\"idx-photo-").append(formResult.getFormId()).append("\" class=\"link icon-only\"><i class=\"icon f7-icons\">camera_fill<span id=\"idx-photo-badge-").append(formResult.getFormId()).append("\" class=\"badge bg-red\"");
//				if(formResult.getPictureCount()>0)s.append(">").append(formResult.getPictureCount());else 
				s.append(" style=\"display:none;\">1");
				s.append("</span></i></a>");
			}
			s.append("</div></div></div> <div class=\"page-content\"><form class=\"list-block inputs-list\" id=\"idx-form-")
				.append(formResult.getFormId()).append("\"><ul>");
		}
		  
//		    <div class="content-block-title">With Floating Labels</div>
		List<W5FormModule> ml = new ArrayList();
		boolean found = false;
		for(W5FormModule m:formResult.getForm().get_moduleList()) if(m.getFormModuleId()==0) {
			found = true;
			break;
		}
		if(!found)ml.add(new W5FormModule());
		ml.addAll(formResult.getForm().get_moduleList());
		

		for (W5FormModule m : ml){
			List<W5FormCellHelper> r = map.get(m.getFormModuleId());
			if(GenericUtil.isEmpty(r))continue;
			if(m.getFormModuleId()!=0){
				W5FormCell fcx = new W5FormCell(); fcx.setControlType((short)102);fcx.setLookupQueryId(10); fcx.setLocaleMsgKey(m.getLocaleMsgKey());
				s.append(GenericUtil.stringToJS(serializeFormCelliOS(customizationId, xlocale,new W5FormCellHelper(fcx), formResult)));
			}
			for (W5FormCellHelper fc : r){
				s.append(GenericUtil.stringToJS(serializeFormCelliOS(customizationId, xlocale,fc, formResult)));
				switch(fc.getFormCell().getControlType()){
				case	2://date
					jsCode.append("iwb.app.calendar({input: '#idx-formcell-").append(fc.getFormCell().getFormCellId()).append("',dateFormat: 'dd/mm/yyyy'});\n");
					break;
				case	10://autocomplete
				case	61://autocomplete-multi
					jsCode.append("iwb.app.autocomplete(iwb.apply({multiple:").append(fc.getFormCell().getControlType()==61).append(", opener: $$('#idx-formcell-").append(fc.getFormCell().getFormCellId()).append("'), params:'_qid=").append(fc.getFormCell().getLookupQueryId());
					if (fc.getFormCell().getLookupIncludedParams() != null && fc.getFormCell().getLookupIncludedParams().length() > 2)
						jsCode.append("&").append(fc.getFormCell().getLookupIncludedParams());
					jsCode.append("'}, ");
					boolean dependantCombo=false;
					for (W5FormCellHelper cfc : formResult.getFormCellResults()) {
						if (cfc.getFormCell().getParentFormCellId() == fc.getFormCell().getFormCellId()) {
							if(!GenericUtil.isEmpty(cfc.getFormCell().getLookupIncludedParams()))
								switch(cfc.getFormCell().getControlType()){
							case	9:case	16:
								jsCode.append("iwb.autoCompleteJson4Autocomplete('")
									.append(GenericUtil.isEmpty(fc.getValue()) ? "":fc.getValue()).append("','#idx-formcell-")
									.append(cfc.getFormCell().getFormCellId()).append("',function(ax,bx){\n") 
									.append(cfc.getFormCell().getLookupIncludedParams()) 
									.append("\n})));\n");
								dependantCombo=true;
								break;
							default:
								//jsCode.append("{}));\n");
	
								break;
							}
							break;

						}
					}
					if(!dependantCombo) {
						jsCode.append("iwb.autoCompleteJson));\n");
					}
					break;
				case	9:case	16://remote-combo, lov-combo-remote
					if (formResult != null && !GenericUtil.isEmpty(fc.getFormCell().getLookupIncludedParams()) && fc.getFormCell().getParentFormCellId() > 0) {
						for (W5FormCellHelper rfc : formResult.getFormCellResults()) {
							if (rfc.getFormCell().getFormCellId() == fc.getFormCell().getParentFormCellId()) {
								W5FormCell pfc = rfc.getFormCell();
								if (pfc.getControlType() == 6
										|| pfc.getControlType() == 7
										|| pfc.getControlType() == 9)
									jsCode.append(
											"iwb.combo2combo('#idx-formcell-").append(pfc.getFormCellId())
											.append("','#idx-formcell-").append(fc.getFormCell().getFormCellId()).append("',function(ax,bx){\n")
											.append(fc.getFormCell().getLookupIncludedParams())
											.append("\n});\n");
								break;
							}
						}
					}
				}

			}
		}
		
		if (f.getObjectType() == 2 && f.get_formSmsMailList() != null && !f.get_formSmsMailList().isEmpty()) { // automatic sms isleri varsa
			StringBuilder s2 = new StringBuilder();
			int cnt = 0;
			for (W5FormSmsMail fsm : f.get_formSmsMailList())
				if (((fsm.getSmsMailType() == 0 && FrameworkSetting.sms && FrameworkCache.getAppSettingIntValue(customizationId, "sms_flag") != 0) || 
						(fsm.getSmsMailType() != 0 && FrameworkSetting.mail && FrameworkCache.getAppSettingIntValue(customizationId, "mail_flag") != 0))
						&& fsm.getAlarmFlag() == 0 && fsm.getPreviewFlag()==0
						&& GenericUtil.hasPartInside2(fsm.getActionTypes(), formResult.getAction())
						&& GenericUtil.hasPartInside2(fsm.getWebMobileTips(),"2")) {
					cnt++;
				}
			if (cnt > 0) {
//				s2.append(",\n\"smsMailTemplateCnt\":").append(cnt).append(",\n\"smsMailTemplates\":[");
				boolean b = false;
				for (W5FormSmsMail fsm : f.get_formSmsMailList())
					if (((fsm.getSmsMailType() == 0 && FrameworkSetting.sms && FrameworkCache.getAppSettingIntValue(customizationId, "sms_flag") != 0) || 
						 (fsm.getSmsMailType() != 0 && FrameworkSetting.mail && FrameworkCache.getAppSettingIntValue(customizationId, "mail_flag") != 0))
							&& fsm.getAlarmFlag() == 0 && fsm.getPreviewFlag()==0
							&& GenericUtil.hasPartInside2(fsm.getActionTypes(), formResult.getAction())
							&& GenericUtil.hasPartInside2(fsm.getWebMobileTips(), "2")) {
						W5FormCell fcx = new W5FormCell(); fcx.setControlType((short)5);fcx.setLookupQueryId(10); 
						fcx.setLocaleMsgKey((fsm.getSmsMailType() == 0 ? "[SMS] " : "[" + (LocaleMsgCache.get2(customizationId, xlocale, "email_upper")) + "] ") + LocaleMsgCache.get2(customizationId, xlocale, fsm.getDsc())
						+ (fsm.getPreviewFlag() != 0 ? " (" + (LocaleMsgCache.get2(customizationId, xlocale, "with_preview")) + ")" : ""));
						fcx.setLookupQueryId(fsm.getFormSmsMailId()); fcx.setDsc("_smsStr");
						W5FormCellHelper fcr = new W5FormCellHelper(fcx);
						if(fsm.getSmsMailSentType() == 1 || fsm.getSmsMailSentType() == 0) fcr.setValue("1");
						s2.append(GenericUtil.stringToJS(serializeFormCelliOS(customizationId, xlocale, fcr, formResult)));
						
						/*if (b)s2.append("\n,");
						else b = true;
						s2.append("{\"xid\":")
								.append(fsm.getFormSmsMailId())
								.append(",\"text\":\"")
								.append(fsm.getSmsMailTip() == 0 ? "[SMS] " : "[" + (PromisLocaleMsg.get2(customizationId, xlocale, "email_upper")) + "] ")
								.append(PromisLocaleMsg.get2(customizationId, xlocale, fsm.getDsc()))
								.append(fsm.getPreviewFlag() != 0 ? " (" + (PromisLocaleMsg.get2(customizationId, xlocale, "with_preview")) + ")" : "")
								.append("\",\"checked\":")
								.append(fsm.getSmsMailSentTip() == 1 || fsm.getSmsMailSentTip() == 0)
								.append(",\"smsMailTip\":")
								.append(fsm.getSmsMailTip())
								.append(",\"previewFlag\":")
						
								.append(fsm.getPreviewFlag() != 0);
						if (fsm.getSmsMailSentTip() == 0)
							s2.append(",\"disabled\":true");
						s2.append("}"); */
					}
			//	s2.append("]");
			}
			if(s2.length()>0){
					W5FormCell fcx = new W5FormCell(); fcx.setControlType((short)102);fcx.setLookupQueryId(11); fcx.setLocaleMsgKey("SMS/E-Posta Dönüşümleri");
					s.append(GenericUtil.stringToJS(serializeFormCelliOS(customizationId, xlocale,new W5FormCellHelper(fcx), formResult)));
					s.append(s2);
			}
/*
			cnt = 0; //alarm control
			for (W5FormSmsMail fsm : f.get_formSmsMailList()) 
				if (((fsm.getSmsMailTip() == 0 && PromisSetting.sms && PromisCache.getAppSettingIntValue(customizationId, "sms_flag") != 0) 
						|| (fsm.getSmsMailTip() != 0 && PromisSetting.mail && PromisCache.getAppSettingIntValue(customizationId, "mail_flag") != 0))
						&& fsm.getAlarmFlag() != 0 && fsm.getPreviewFlag()==0
						&& PromisUtil.hasPartInside2(fsm.getActionTips(), formResult.getAction())
						&& PromisUtil.hasPartInside2(fsm.getWebMobileTips(),"2")) {
					cnt++;
				}
			if (cnt > 0) {
				Map<Integer, W5FormSmsMailAlarm> alarmMap = new HashMap();
				if (!PromisUtil.isEmpty(formResult.getFormAlarmList()))
					for (W5FormSmsMailAlarm a : formResult.getFormAlarmList()) {
						alarmMap.put(a.getFormSmsMailId(), a);
					}
			//	s2.append(",\n\"alarmTemplateCnt\":").append(cnt++).append(",\n\"alarmTemplates\":[");
				boolean b = false;
				for (W5FormSmsMail fsm : f.get_formSmsMailList())
					if (((fsm.getSmsMailTip() == 0 && PromisSetting.sms && PromisCache.getAppSettingIntValue(customizationId, "sms_flag") != 0) || 
							(fsm.getSmsMailTip() != 0 && PromisSetting.mail && PromisCache.getAppSettingIntValue(customizationId, "mail_flag") != 0))
							&& fsm.getAlarmFlag() != 0 && fsm.getPreviewFlag()==0
							&& PromisUtil.hasPartInside2(fsm.getActionTips(), formResult.getAction())
							&& PromisUtil.hasPartInside2(fsm.getWebMobileTips(), "2")) {
						W5FormSmsMailAlarm a = alarmMap.get(fsm.getFormSmsMailId());
						W5FormCell fcx = new W5FormCell(); fcx.setControlTip((short)5);fcx.setLookupQueryId(10); 
						fcx.setLocaleMsgKey((fsm.getSmsMailTip() == 0 ? "[SMS] " : "[" + (PromisLocaleMsg.get2(customizationId, xlocale, "email_upper")) + "] ") + PromisLocaleMsg.get2(customizationId, xlocale, fsm.getDsc())
						+ (fsm.getPreviewFlag() != 0 ? " (" + (PromisLocaleMsg.get2(customizationId, xlocale, "with_preview")) + ")" : ""));
						fcx.setLookupQueryId(fsm.getFormSmsMailId()); fcx.setDsc("_rrr");
						W5FormCellHelper fcr = new W5FormCellHelper(fcx);
						if(a != null || fsm.getSmsMailSentTip() == 1 || fsm.getSmsMailSentTip() == 0) fcr.setValue("1");
						s2.append(PromisUtil.stringToJS(serializeFormCell(customizationId, xlocale, fcr, formResult)));


					}
			//	s2.append("]");
			}
			*/
		}

/*		if(f.getRenderTip()==5 && !GenericUtil.isEmpty(f.getJsCode())){
			s.append(GenericUtil.stringToJS(f.getJsCode().substring(f.getJsCode().indexOf("${iwb-data}")+11)));
		} else */if (f.getObjectType() == 2){
			s.append("</ul>");
			if (!formResult.isViewMode()){//kaydet butonu
				if(masterDetail) //master detail
					s.append("<div class=\"content-block\"><p class=\"buttons-row\"><a href=# class=\"button button-big button-fill button-raised color-blue\" id=\"iwb-continue-").append(formResult.getFormId()).append("\">Next</a></p></div>");
				else
					s.append("<div class=\"content-block\"><p class=\"buttons-row\"><a href=# class=\"button button-big button-fill button-raised color-blue\" id=\"iwb-submit-").append(formResult.getFormId()).append("\">Save</a></p></div>");
			}
			s.append("</form></div></div>");
			
		}
//		for(W5FormCe){}
		s.append("'");
		if(!GenericUtil.isEmpty(f.getJsCode())){
			jsCode.append("\n").append(f.getJsCode()).append("\n");
		}

		if(jsCode.length()>0){
			StringBuilder  bx= new StringBuilder();
			bx.append("if(!callAttributes.json)callAttributes.json={};callAttributes.json.postInit=function(j){\n").append(jsCode).append("\n}\n;");
			jsCode = bx;
		}

		if(jsCode.length()>0)s.append(",\n init:function(callAttributes){\n")
			.append(jsCode).append("\n}");
		s.append("}");
		return s;
	}

	private	StringBuilder	serializeListiOS(M5ListResult	listResult){
		M5List l = listResult.getList();
		StringBuilder buf = new StringBuilder();

		String htmlDataCode=l.getHtmlDataCode();
		if(htmlDataCode==null)htmlDataCode="";
		htmlDataCode=htmlDataCode.replace("iwb-link-7 ", "iwb-link-"+l.getListId()+" ");
		buf.append("{success:true, listId:").append(l.getListId()).append(", listTip:").append(l.getListTip()==1 || l.getListTip()==4 ?1:l.getListTip()).append(",\n name:'")
			.append(LocaleMsgCache.get2(listResult.getScd(), l.getLocaleMsgKey()))
			.append("',\n htmlData:'").append(GenericUtil.stringToJS2(htmlDataCode))
			.append("',\n init:function(callAttributes){\n").append(getJsCode(l))
			.append("\n},\n dataUrl:'ajaxQueryData?_qid=").append(l.getQueryId());
		if(l.getListTip()==3)buf.append("&_pids=1");
		buf.append("'");
	
		
		if(l.getDefaultPageRecordNumber()>0)
			buf.append(", pageSize: ").append(l.getDefaultPageRecordNumber());
		if(!GenericUtil.isEmpty(l.get_orderQueryFieldNames())){
			buf.append(",\n orderNames:[");
			for(String f:l.get_orderQueryFieldNames()){
				buf.append("{id:'").append(f).append("',dsc:'").append(LocaleMsgCache.get2(listResult.getScd(), f)).append("'},");
			}
			buf.setLength(buf.length()-1);
			buf.append("]");
		}

		
		boolean insertFlag = false;
		if(l.getDefaultCrudFormId()!=0 && l.get_mainTable()!=null){
			W5Table t = l.get_mainTable();
			insertFlag = GenericUtil.accessControl(listResult.getScd(),
					t.getAccessInsertTip(), t.getAccessInsertRoles(),
					t.getAccessInsertUsers());
			buf.append(",\n crudFormId:")
			.append(l.getDefaultCrudFormId())
			.append(",\n crudTableId:")
			.append(t.getTableId()).append(",\n pkName:'")
			.append(t.get_tableParamList().get(0).getDsc())
			.append("',\n crudFlags:{insert:")
			.append(insertFlag)
			.append(",edit:")
			.append(t.getAccessUpdateUserFields() != null
					|| GenericUtil.accessControl(listResult.getScd(),
							t.getAccessUpdateTip(),
							t.getAccessUpdateRoles(),
							t.getAccessUpdateUsers()))
			.append(",remove:")
			.append(t.getAccessDeleteUserFields() != null
					|| GenericUtil.accessControl(listResult.getScd(),
							t.getAccessDeleteTip(),
							t.getAccessDeleteRoles(),
							t.getAccessDeleteUsers()));
			buf.append("}");
		}
		StringBuilder s2= new StringBuilder();
		if(!GenericUtil.isEmpty(l.get_detailMLists())){
			for(M5List d:l.get_detailMLists()) {
				s2.append("{icon:'list', text:'").append(LocaleMsgCache.get2(listResult.getScd(), d.getLocaleMsgKey()))
				.append("',href:'showMList?_lid=").append(d.getListId());
				if(l.get_mainTable()!=null)
					s2.append("&x").append(l.get_mainTable().get_tableFieldList().get(0).getDsc()).append("='},"); //TODO. parent'takine gore degil de, farkli olmasi gerekli
				else if(d.get_query()!=null){
					if(GenericUtil.isEmpty(d.get_query().get_queryParams()))
						s2.append("&").append(d.get_query().get_queryFields().get(0).getDsc()).append("='},"); //TODO. parent'takine gore degil de, farkli olmasi gerekli
					else
						s2.append("&").append(d.get_query().get_queryParams().get(0).getDsc()).append("='},"); //TODO. parent'takine gore degil de, farkli olmasi gerekli
				} else 
					s2.append("&x").append(l.get_query().get_queryFields().get(0).getDsc()).append("='},"); //TODO. parent'takine gore degil de, farkli olmasi gerekli
			}
		}
		
		if(!GenericUtil.isEmpty(l.get_menuItemList())){
			for(W5ObjectMenuItem d:l.get_menuItemList())if(d.getItemTip()==1 && !GenericUtil.isEmpty(d.getCode())){ //record ile ilgili
				s2.append("{icon:'").append(d.getImgIcon()).append("', text:'").append(LocaleMsgCache.get2(listResult.getScd(), d.getLocaleMsgKey())).append("'");
				if(d.getCode().charAt(0)!='!')s2.append(",click:function(ax,bx,cx){\n").append(d.getCode()).append("\n}");
				else s2.append(",href:'").append(d.getCode().substring(1)).append("'");
				s2.append("},");
			}
		}
		
		if(s2.length()>0){
			s2.setLength(s2.length()-1);
			buf.append("\n, recordButtons:[").append(s2).append("]");
		}
		buf.append("\n, htmlPage:'");
		if(GenericUtil.isEmpty(l.getHtmlPageCode())){
			
			boolean searchBar = l.getDefaultPageRecordNumber()==0 && (l.getListTip()==1 || l.getListTip()==4);
//			StringBuilder s2= new StringBuilder();
			s2.setLength(0);
			s2.append("<div class=\"page\" data-page=\"mlist-").append(l.getListId()).append("-view\">")
			.append("<div class=\"navbar\"><div class=\"navbar-inner iwb-navbar-").append(l.getParentListId()!=0?"d":"").append("list\"><div class=\"left\">");
			if(l.getParentListId()!=0)s2.append("<a href=# class=\"back link icon-only\"><i class=\"icon f7-icons\">left_arrow</i></a>");
			else s2.append("<a href=# class=\"open-panel link icon-only\"><i class=\"icon f7-icons\">bars</i></a>");
			s2.append("</div><div class=\"center\">").append(LocaleMsgCache.get2(listResult.getScd(), l.getLocaleMsgKey())).append("</div>");
			//if(l.getParentListId()==0)s2.append("<div class=\"right\"><a href=# id=\"idx-o-users-").append(l.getListId()).append("\" class=\"link icon-only\"><i class=\"icon f7-icons\">persons<span id=\"idx-chat-badge\" class=\"badge bg-red\" style=\"display:none;\">1</span></i></a></div>");
			s2.append("</div></div>");
			
			StringBuilder s3= new StringBuilder();
			if(!GenericUtil.isEmpty(l.get_orderQueryFieldNames())){
				s3.append("<a href=# id=\"idx-sort-").append(l.getListId()).append("\"><i class=\"icon f7-icons\">sort</i></a>");
			}
			if(listResult.getSearchFormResult()!=null){
				s3.append("<a href=# id=\"idx-filter-").append(l.getListId()).append("\" style=\"background-color:darkgray;\"><i class=\"icon f7-icons\">search</i></a>");
			}
			if(insertFlag){
				s3.append("<a href=\"showMForm?a=2&_fid=").append(l.getDefaultCrudFormId()).append("\" class=\"item-link\" id=\"idx-insert-").append(l.getDefaultCrudFormId()).append("\" style=\"background-color:#4caf50;\"><i class=\"icon f7-icons\">add</i></a>");
			}
			
			if(!GenericUtil.isEmpty(l.get_toolbarItemList())){
				for(W5ObjectToolbarItem d:l.get_toolbarItemList())if(/*d.getItemTip()==1 && */!GenericUtil.isEmpty(d.getCode())){ //record ile ilgili
					s3.append("<a href=\"").append(d.getCode()).append("\" class=\"item-link\" id=\"idx-xinsert-").append(d.getToolbarItemId()).append("\" style=\"background-color:#4caf50;\"><i class=\"icon f7-icons\">").append(GenericUtil.isEmpty(d.getImgIcon()) ? "bolt":d.getImgIcon()).append("</i></a>");
				}
			}
			
			if(s3.length()>0)
				s2.append("<div class=\"speed-dial\"><a href=# class=\"floating-button\"><i class=\"icon\" style=\"width: 40px;height: 40px;background: white;border-radius: 50%;\"></i><i class=\"icon f7-icons\">close</i></a><div class=\"speed-dial-buttons\">").append(s3).append("</div></div>");  

			if(searchBar){
				s2.append("<form data-search-list=\".search-here\" data-search-in=\".item-title\" class=\"searchbar searchbar-init\"><div class=\"searchbar-input\"><input type=\"search\" placeholder=\"Search\"/><a href=# class=\"searchbar-clear\"></a></div></form><div class=\"searchbar-overlay\"></div>");
			}
		  
			s2.append("<div  id=\"idx-page-content-").append(l.getListId()).append("\" class=\"page-content pull-to-refresh-content");
			if(l.getDefaultPageRecordNumber()>0)s2.append(" infinite-scroll");
			if(l.getHideBarsOnScrollFlag()!=0)s2.append(" hide-bars-on-scroll");
			s2.append("\">");
			if(searchBar){
				s2.append("<div class=\"list-block searchbar-not-found\"><ul><li class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title\">Nothing found</div></div></li></ul></div>");
			}
			    
			s2.append("<div class=\"pull-to-refresh-layer\"><div class=\"preloader\"></div><div class=\"pull-to-refresh-arrow\"></div></div>");    
			
			s2.append("<div class=\"list-block").append(l.getListTip()==4?" media-list":"").append(searchBar ? " search-here searchbar-found":"").append("\"><ul id=\"idx-").append(l.getListId()).append("\">${iwb-data}</ul></div></div></div>");
			buf.append(GenericUtil.stringToJS2(s2.toString()));
		} else //PromisUtil.filterExt(f.getJsCode().substring(0, f.getJsCode().indexOf("${iwb-data}")),listResult.getScd(), listResult.getRequestParams(),null))
			buf.append(GenericUtil.stringToJS2(GenericUtil.filterExt(l.getHtmlPageCode(),listResult.getScd(), listResult.getRequestParams(),null).toString()));
		buf.append("'");
		
		if(listResult.getSearchFormResult()!=null){
			buf.append(",\n searchForm:").append(serializeGetForm(listResult.getSearchFormResult()));
		}
		
		String w = (String)listResult.getScd().get("mobileDeviceId");
		listResult.getRequestParams().put(".w", w);
		listResult.getRequestParams().put(".t", w+"-l-"+l.getListId());
		listResult.getRequestParams().put("_gid", ""+(1000000+l.getListId()));
		buf.append(",\n baseParams:").append(GenericUtil.fromMapToJsonString(listResult.getRequestParams()));
		
		buf.append("}");
		
		return buf;
	}

	public StringBuilder serializeGraphDashboard(W5BIGraphDashboard gd, Map<String, Object> scd){
		StringBuilder buf = new StringBuilder();
		buf.append("{\"dashId\":").append(gd.getGraphDashboardId())
		 .append(",\"name\":\"").append(LocaleMsgCache.get2(scd, gd.getLocaleMsgKey())).append("\", \"gridId\":").append(gd.getGridId()).append(",\"tableId\":").append(gd.getTableId())
		 .append(",\"is3d\":").append(gd.getIs3dFlag()!=0)
		.append(",\"graphTip\":").append(gd.getGraphTip()).append(",\"groupBy\":\"").append(gd.getGraphGroupByField()).append("\",\"funcTip\":").append(gd.getGraphFuncTip()).append(",\"funcFields\":\"").append(gd.getGraphFuncFields())
		.append("\", \"queryParams\":").append(gd.getQueryBaseParams());
		if(gd.getStackedQueryField()!=0)buf.append(",\"stackedFieldId\":").append(gd.getStackedQueryField());
		if(gd.getDefaultHeight()!=0)buf.append(",\"height\":").append(gd.getDefaultHeight());
		if(gd.getLegendFlag()!=0)buf.append(",\"legend\":true");
		buf.append("}");
		return buf;
	}
}
