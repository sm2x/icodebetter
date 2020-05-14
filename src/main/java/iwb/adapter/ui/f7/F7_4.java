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
import iwb.domain.db.M5List;
import iwb.domain.db.W5BIGraphDashboard;
import iwb.domain.db.W5Detay;
import iwb.domain.db.W5Form;
import iwb.domain.db.W5FormCell;
import iwb.domain.db.W5FormModule;
import iwb.domain.db.W5FormSmsMail;
import iwb.domain.db.W5LookUp;
import iwb.domain.db.W5LookUpDetay;
import iwb.domain.db.W5ObjectMenuItem;
import iwb.domain.db.W5Page;
import iwb.domain.db.W5QueryField;
import iwb.domain.db.W5Table;
import iwb.domain.db.W5Workflow;
import iwb.domain.helper.W5FormCellHelper;
import iwb.domain.helper.W5TableRecordHelper;
import iwb.domain.result.M5ListResult;
import iwb.domain.result.W5FormResult;
import iwb.domain.result.W5PageResult;
import iwb.domain.result.W5QueryResult;
import iwb.enums.FieldDefinitions;
import iwb.util.GenericUtil;
import iwb.util.UserUtil;

public class F7_4 implements ViewMobileAdapter {
  private static final String[] labelMap = new String[] {"info", "warning", "error"};
  private static final String[] labelMapColor =
      new String[] {"rgba(33, 150, 243, 0.1)", "rgba(255, 152, 0, 0.2);", "rgba(255, 0, 0, 0.1);"};

  private StringBuilder serializeTableHelperList(Map scd, List<W5TableRecordHelper> ltrh) {
    StringBuilder buf = new StringBuilder();
    boolean bq = false;
    buf.append("[");
    if (ltrh != null)
      for (W5TableRecordHelper trh : ltrh) {
        W5Table dt = FrameworkCache.getTable(scd, trh.getTableId());
        if (dt == null) break;
        if (bq) buf.append(",");
        else bq = true;
        buf.append("{\"tid\":")
            .append(trh.getTableId())
            .append(",\"tpk\":")
            .append(trh.getTablePk())
            .append(",\"tcc\":")
            .append(trh.getCommentCount())
            .append(",\"tdsc\":\"")
            .append(LocaleMsgCache.get2(scd, dt.getDsc()))
            .append("\"")
            .append(",\"dsc\":\"")
            .append(GenericUtil.stringToJS2(trh.getRecordDsc()))
            .append("\"}");
      }
    buf.append("]");
    return buf;
  }

  public StringBuilder serializeValidatonErrors(Map<String, String> errorMap, String locale) {
    StringBuilder buf = new StringBuilder();
    buf.append("[");
    boolean b = false;
    for (String q : errorMap.keySet()) {
      if (b) buf.append("\n,");
      else b = true;
      buf.append("{\"id\":\"")
          .append(q)
          .append("\",\"msg\":\"")
          .append(GenericUtil.stringToJS2(errorMap.get(q)))
          .append("\",\"dsc\":\"")
          .append(LocaleMsgCache.get2(0, locale, q))
          .append("\"}"); // TODO.
      // aslinda
      // customizationId
      // olmasi
      // lazim
    }
    buf.append("]");
    return buf;
  }

  
	private StringBuilder serializeQueryData(W5QueryResult qr) {
		int customizationId = (Integer) qr.getScd().get("customizationId");
		String xlocale = (String) qr.getScd().get("locale");
		String userIdStr = qr.getScd().containsKey("userId") ? qr.getScd().get("userId").toString() : null;
		List datas = qr.getData();
		StringBuilder buf = new StringBuilder();
		buf.append("{\"success\":").append(qr.getErrorMap().isEmpty())
				.append(",\"queryId\":").append(qr.getQueryId())
				.append(",\"execDttm\":\"")
				.append(GenericUtil.uFormatDateTime(new Date())).append("\"");
		boolean dismissNull = qr.getRequestParams()!=null && GenericUtil.uInt(qr.getRequestParams(), "_dismissNull")!=0;
		if (qr.getErrorMap().isEmpty()) {
			buf.append(",\n\"data\":["); // ana
			if (!GenericUtil.isEmpty(datas)) {
				boolean bx = false;
				boolean isMap = (datas.get(0) instanceof Map);
				
				
				for (Object o : datas) {
					if (bx)
						buf.append(",\n");
					else
						bx = true;
					buf.append("{"); // satir
					boolean b = false;
					for (W5QueryField f : qr.getNewQueryFields()) {
						Object obj = isMap ? ((Map)o).get(f.getDsc()) : ((Object[])o)[f.getTabOrder() - 1];
						if(obj==null && dismissNull)continue;
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
						} else if(f.getFieldType() == 8) {
							buf.append("\":");
							if (obj == null)buf.append("null");
							else if(obj instanceof Map)buf.append(GenericUtil.fromMapToJsonString2Recursive((Map)obj));
							else if(obj instanceof List)buf.append(GenericUtil.fromListToJsonString2Recursive((List)obj));
							else buf.append(obj);
							continue;
						}
						
						buf.append("\":\"");
						if (obj != null)
							switch (f.getPostProcessType()) { // queryField PostProcessTip
							case	15://convert to []
								buf.setLength(buf.length()-1);
								buf.append("[").append(obj).append("]");
								continue;
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
										res += ","+ UserUtil.getUserName(GenericUtil.uInt(s));
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
										res += ","+ UserUtil.getUserDsc(GenericUtil.uInt(s));
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
										qr.getScd(), f.getLookupQueryId());
								if (lookUp == null)
									break;
								buf.append("\",\"").append(f.getDsc())
										.append("_qw_\":\"");
								String[] objs = f.getPostProcessType() == 11 ? ((String) obj)
										.split(",") : new String[] { obj
										.toString() };
								boolean bz = false;
								if(lookUp.get_detayMap()!=null)for (String q : objs) {
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
									.append(",\"user_dsc\":\"").append(UserUtil.getUserDsc( GenericUtil.uInt(ozc[1])))
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
												qr.getScd(),
												(short) 1,
												ozs.length > 3 ? ozs[3] : null,
												ozs.length > 4 ? ozs[4] : null))
									buf.append("-");
								buf.append(ozs[2]);
								W5Workflow appr = FrameworkCache.getWorkflow(qr.getScd(), appId);
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
									Map<Integer, String> roles = FrameworkCache.wRoles.get(customizationId);
									if(roles!=null)for (String rid : roleIds) {
										buf.append(
												roles.get(
														GenericUtil.uInt(rid)) != null ? roles.get(GenericUtil
																.uInt(rid))
														: "null").append(", ");
									} else buf.append("null, ");
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
					if (qr.getQuery().getShowParentRecordFlag() != 0 && !isMap && ((Object[])o)[((Object[])o).length - 1] != null) {
						buf.append(",\"").append(FieldDefinitions.queryFieldName_HierarchicalData).append("\":")
								.append(serializeTableHelperList(
										qr.getScd(),
										(List<W5TableRecordHelper>) ((Object[])o)[((Object[])o).length - 1]));
					}
					buf.append("}"); // satir
				}
			}
			buf.append("],\n\"browseInfo\":{\"startRow\":")
					.append(qr.getStartRowNumber())
					.append(",\"fetchCount\":")
					.append(qr.getFetchRowCount())
					.append(",\"totalCount\":")
					.append(qr.getResultRowCount()).append("}");
			if (FrameworkSetting.debug && GenericUtil.uInt(qr.getScd().get("mobile"))==0 && qr.getExecutedSql() != null) {
				buf.append(",\n\"sql\":\"")
						.append(GenericUtil.stringToJS2(GenericUtil.replaceSql(
								qr.getExecutedSql(),
								qr.getSqlParams()))).append("\"");
			}
			if (!GenericUtil.isEmpty(qr.getExtraOutMap()))
				buf.append(",\n \"extraOutMap\":").append(
						GenericUtil.fromMapToJsonString2Recursive(qr
								.getExtraOutMap()));
		} else
			buf.append(",\n\"errorType\":\"validation\",\n\"errors\":")
					.append(serializeValidatonErrors(qr.getErrorMap(),
							xlocale));

		return buf.append("}");
	}
	
  public StringBuilder serializePage(W5PageResult pageResult) {
    StringBuilder buf = new StringBuilder();
    W5Page p = pageResult.getPage();
    buf.append(GenericUtil.isEmpty(p.getCode()) ? "{\"success\":true}":p.getCode());
    if (!GenericUtil.isEmpty(p.getCssCode())) {
      int ix = buf.lastIndexOf("}");
      if (ix > -1) buf.insert(ix, ",style:`" + p.getCssCode() + "`");
    }
    if(!GenericUtil.isEmpty(pageResult.getPageObjectList())) {
    	StringBuilder sb = new StringBuilder();
    	if(buf.indexOf("template:")==-1) {
    		StringBuilder code = new StringBuilder(), data = new StringBuilder();
    		data.append("_tid:").append(p.getPageId());
    		code.append("on:{pageDestroy:function(){if(this.ptr)this.ptr.destroy('#idx-tpage-")
    		.append(p.getPageId()).append("');},pageInit:function(){var self=this;setTimeout(function(){self.ptr=iwb.app.ptr.get('#idx-tpage-")
    		.append(p.getPageId())
    		.append(".ptr-content');self.ptr.on('refresh',self.reload);self.reload();},100);}},methods:{clickMenu:function(){},clickLink:function(url){iwb.app.views.main.router.navigate(url);},reload:function(){var self=this;\n");
    		sb.append(", template:`<div data-page=\"iwb-page-").append(p.getPageId()).append("\" class=\"page\">\n <div class=\"navbar\"><div class=\"navbar-inner\"><div class=\"left\"><a href=\"#\" class=\"link icon-only panel-open\" data-panel=\"left\"><i class=\"icon f7-icons if-not-md\">menu</i><i class=\"icon material-icons if-md\">menu</i></a></div><div class=\"title\">")
    			.append(LocaleMsgCache.get2(pageResult.getScd(),p.getDsc()))
    			.append("</div><div class=\"right\"></div></div></div>\n <div id=\"idx-tpage-").append(p.getPageId()).append("\" class=\"page-content ptr-content\">\n");
    	    sb.append(
    	              "<div class=\"ptr-preloader\"><div class=\"preloader\"></div><div class=\"ptr-arrow\"></div></div>");

    		boolean lastBadge = false, lastGauge = false;
    		for(Object o:pageResult.getPageObjectList()) if(o!=null && !(o instanceof String)){	
    			if(lastBadge && (!(o instanceof W5QueryResult) || ((W5QueryResult)o).getQuery().getQueryType()!=21)) {
    				sb.append("</div></div>");
    				lastBadge = false;
    			}
    			if(lastGauge && (!(o instanceof W5QueryResult) || ((W5QueryResult)o).getQuery().getQueryType()!=22)) {
    				sb.append("</div></div>");
    				lastGauge = false;
    			}
    			if(o instanceof W5BIGraphDashboard) {
    				W5BIGraphDashboard gd = (W5BIGraphDashboard)o;
        			String pk = "o9_" + gd.getGraphDashboardId();
        			sb.append("\n  	<div class=\"card\"><div class=\"card-header\">").append(LocaleMsgCache.get2(pageResult.getScd(),gd.getLocaleMsgKey()));
    				if(false)sb.append(" @click=\"clickLink('showMList?_lid=1')\"");
    				sb.append("</div><div class=\"card-content card-content-padding\" id=\"idx-obj-").append(pk).append("\"></div></div>");
    				code.append("\n iwb.graph(").append(serializeGraphDashboard(gd, pageResult.getScd())).append(",'idx-obj-").append(pk).append("');");
	    		} else if(o instanceof M5ListResult) {
	    			M5ListResult lr = (M5ListResult)o;
        			String pk = "o11_" + lr.getListId();
        			sb.append("\n  <div class=\"card\"><div class=\"card-header\">").append(LocaleMsgCache.get2(pageResult.getScd(),lr.getList().getLocaleMsgKey()));
    				if(false)sb.append(" @click=\"clickLink('showMList?_lid=1')\"");
    				sb.append("</div><div class=\"card-content\" id=\"idx-obj-").append(pk).append("\">");
					if(lr.getList().getListTip()==1 || lr.getList().getListTip()==4)sb.append("<div class=\"list\"><ul>");
					sb.append(lr.getList().getHtmlDataCode().replace("data}}","data_"+lr.getListId()+"}}"));
					if(lr.getList().getListTip()==1 || lr.getList().getListTip()==4)sb.append("</ul></div>");
    				sb.append("</div></div>");
    				data.append(", data_").append(lr.getListId()).append(":[]");
    				code.append("\niwb.request({url:'ajaxQueryData?_qid=").append(lr.getList().getQueryId()).append("&limit=")
    					.append(lr.getList().getDefaultPageRecordNumber()>0 ? lr.getList().getDefaultPageRecordNumber():50)
    					.append("', success:function(jj){self.$setState({data_").append(lr.getListId()).append(":jj.data||[]})}});");
	    		} else if(o instanceof W5QueryResult) {
	    			W5QueryResult qr = (W5QueryResult)o;
        			String pk = "o"+qr.getQuery().getQueryType()+"_" + qr.getQueryId();
	    			switch(qr.getQuery().getQueryType()) {
	    			case	21://badge
	    				if(!lastBadge) {
	    					sb.append("\n  <div class=\"block\" style=\"padding-left: inherit; padding-right: inherit; margin-bottom: 0;\"><div class=\"row\">");
	    				}
	    				lastBadge = true;
	    				sb.append("{{#if ").append(pk).append("}}<div class=\"col-50\"><div");
	    				if(false)sb.append(" @click=\"clickLink('showMList?_lid=1')\"");
	    				sb.append(" class=\"card bg-color-{{#if ").append(pk).append(".bgcolor}}{{").append(pk).append(".bgcolor}}{{else}}blue{{/if}}\" style=\"color:{{#if ")
	    					.append(pk).append(".color}}{{").append(pk)
	    					.append(".color}}{{else}}white{{/if}} \"><div class=\"card-content\" style=\"padding:5px 10px; text-align:right;font-size:2rem; display: grid;\"><div style=\"text-align: left;font-size: 1rem;\">{{")
	    					.append(pk).append(".title}}</div><div>{{").append(pk).append(".val}}</div></div></div></div>{{/if}}\n");
    				break;
	    			case	22://gauge
	    				if(!lastGauge) {
	    					sb.append("\n  <div class=\"block block-strong\"><div class=\"row\">");
	    				}
	    				lastGauge = true;
	    				sb.append("{{#if ").append(pk).append("}}<div class=\"col-50\"><div");
	    				if(false)sb.append(" @click=\"clickLink('showMList?_lid=1')\"");
	    				sb.append(" class=\"gauge gauge-init\" data-type=\"{{#if ").append(pk).append(".xtype}}{{").append(pk).append(".xtype}}{{else}}semicircle{{/if}}\" data-value=\"{{").append(pk).append(".xval}}\" data-value-text=\"{{")
	    					.append(pk).append(".val}}\" data-value-text-color=\"{{").append(pk).append(".color}}\" data-border-color=\"{{")
	    					.append(pk).append(".color}}\" data-label-text=\"{{").append(pk).append(".title}}\"></div></div>{{/if}}\n");
	    				break;
	    			}
    				code.append("\n iwb.request({url:'ajaxQueryData?_qid=").append(qr.getQuery().getQueryId()).append("', success:function(jj){self.$setState({").append(pk).append(":jj.data[0]||{}})}});");
	    		}
    		}
			if(lastBadge)sb.append("</div></div>");
			else if(lastGauge)sb.append("</div></div>");
			sb.append("\n\n </div>\n</div>");
    		code.append("\n if(self.ptr)self.ptr.done();}}");
    		sb.append("`,").append(code).append(", data:function(){return {").append(data).append("}}");
    		int ix = buf.lastIndexOf("}");
            if (ix > -1) buf.insert(ix, sb.toString());
    	} else {
    		sb.append(", items:[");
	    	for(Object o:pageResult.getPageObjectList()) {
	    		if(o instanceof W5BIGraphDashboard) {
	    			sb.append(serializeGraphDashboard((W5BIGraphDashboard)o, pageResult.getScd()));
	    		} else if(o instanceof M5ListResult) {
	    			sb.append(serializeList4Page((M5ListResult)o));
	    		} else if(o instanceof W5QueryResult) {
	    			sb.append(serializeQueryData((W5QueryResult)o));
	    		} else sb.append("{}");
	    		sb.append(",");
	    	}
	    	if(sb.length()>1) {
	    		sb.setLength(sb.length()-1);
	    		sb.append("]");
	    		int ix = buf.lastIndexOf("}");
	            if (ix > -1) buf.insert(ix, sb.toString());
	    	}
    	}
    	
    }
    return buf;
  }
  private StringBuilder serializeList4Page(M5ListResult listResult) {
	StringBuilder buf = new StringBuilder();
    M5List l = listResult.getList();

    String htmlDataCode = l.getHtmlDataCode();
    if (htmlDataCode == null) htmlDataCode = ""; 

    buf.append("{listId:")
    .append(l.getListId())
    .append(", queryId:").append(l.getQueryId()).append(", name:'")
    .append(LocaleMsgCache.get2(listResult.getScd(), l.getLocaleMsgKey()))
    .append("'");
    buf.append("\n}, template:`");
    buf.append(htmlDataCode.replace("data}}","data_"+listResult.getListId()+"}}").replace("@click","_dummy")).append("`");    
    buf.append("}");

    return buf;
  }

  public StringBuilder serializeList(M5ListResult listResult) {
    StringBuilder buf = new StringBuilder();
    M5List l = listResult.getList();

    String htmlDataCode = l.getHtmlDataCode();
    if (htmlDataCode == null) htmlDataCode = "";
    // htmlDataCode=htmlDataCode.replace("iwb-link-7 ", "iwb-link-"+l.getListId()+" ");

    buf.append("{success:true, props:{listId:")
        .append(l.getListId())
        .append(", listTip:")
        .append(l.getListTip() == 1 || l.getListTip() == 4 ? 1 : l.getListTip())
        .append(",\n name:'")
        .append(LocaleMsgCache.get2(listResult.getScd(), l.getLocaleMsgKey()))
        .append("'");

//    if (l.getDefaultPageRecordNumber() > 0)buf.append(", pageSize: ").append(l.getDefaultPageRecordNumber());
    if (!GenericUtil.isEmpty(l.get_orderQueryFieldNames())) {
      buf.append(",\n orderNames:[");
      for (String f : l.get_orderQueryFieldNames()) {
        buf.append("{id:'")
            .append(f)
            .append("',dsc:'")
            .append(LocaleMsgCache.get2(listResult.getScd(), f))
            .append("'},");
      }
      buf.setLength(buf.length() - 1);
      buf.append("]");
    }

    boolean insertFlag = false, searchBar = false;

    if (listResult.getSearchFormResult() != null) {
      buf.append(",\n searchForm:").append(serializeGetForm(listResult.getSearchFormResult()));
    }

//    buf.append(",\n baseParams:").append(GenericUtil.fromMapToJsonString(listResult.getRequestParams()));

    buf.append("\n}, template:`");
    if (GenericUtil.isEmpty(l.getHtmlPageCode())) {

      searchBar =
          l.getDefaultPageRecordNumber() == 0
              && (l.getListTip() == 1
                  || l.getListTip() == 4); // && listResult.getSearchFormResult()==null
      // StringBuilder s2= new StringBuilder();
      StringBuilder s2 = new StringBuilder();
      s2.append("<div class=\"page");
      if (searchBar) s2.append(" page-with-subnavbar");
      s2.append("\" data-name=\"mlist-")
          .append(l.getListId())
          .append("-view\">")
          .append("\n<div class=\"navbar\"><div class=\"navbar-inner\"><div class=\"left\">");

      if (l.getParentListId() != 0 || GenericUtil.uInt(listResult.getRequestParams().get("_back"))!=0) {
          s2.append(
                  "<a href=\"#\" class=\"link back\"> <i class=\"icon icon-back\"></i> <span class=\"if-not-md\">")
          .append(LocaleMsgCache.get2(listResult.getScd(), "back"))
          .append("</span></a>");
      } else {
          s2.append(
                  "<a href=\"#\" class=\"link icon-only panel-open\" data-panel=\"left\"><i class=\"icon f7-icons if-not-md\">menu</i><i class=\"icon material-icons if-md\">menu</i></a>");
      }
      
      s2.append("</div>");

      s2.append("<div class=\"title")
//          .append(l.getParentListId() == 0 ? " sliding" : "")
          .append("\">")
          .append(LocaleMsgCache.get2(listResult.getScd(), l.getLocaleMsgKey()))
          .append(l.getParentListId() == 0 ?  "{{#if browseInfo.totalCount>1}}<span class=\"sub-title\">{{browseInfo.totalCount}} "
            + LocaleMsgCache.get2(listResult.getScd(), "records") + "</span>{{/if}}":"")
          .append("</div>");
      if (listResult.getSearchFormResult() != null) {
        s2.append(
            "<div class=\"right\"><a href=# class=\"link icon-only panel-open\" data-panel=\"right\" @click=\"clickFilter\"><i class=\"icon f7-icons if-not-md\">search</i><i class=\"icon material-icons if-md\">search</i></a></div>");
      }
      if (false && l.getParentListId() == 0)
        s2.append("<div class=\"title-large\"><div class=\"title-large-text\">")
            .append(LocaleMsgCache.get2(listResult.getScd(), l.getLocaleMsgKey()))
            .append("</div></div>");
      if (searchBar) {
        s2.append(
                "<div class=\"subnavbar\"><form id=\"idx-searchbar-").append(l.getListId()).append("\" class=\"searchbar\"><div class=\"searchbar-inner\"><div class=\"searchbar-input-wrap\"><input type=\"search\" placeholder=\"")
            .append(LocaleMsgCache.get2(listResult.getScd(), "search"))
            .append(
                "\"><i class=\"searchbar-icon\"></i><span class=\"input-clear-button\"></span></div><span class=\"searchbar-disable-button if-not-aurora\">Cancel</span></div></form></div>");
      }
      s2.append("</div></div>");

      StringBuilder s3 = new StringBuilder();
      if (!GenericUtil.isEmpty(l.get_orderQueryFieldNames())) {
        s3.append("<a href=# class=\"fab-label-button\" @click=\"clickSort\"><span><i class=\"icon material-icons\">sort</i></span><span class=\"fab-label\">Sort</span></a>");
      }
      if (l.getDefaultCrudFormId() != 0 && l.get_mainTable() != null) {
        W5Table t = l.get_mainTable();
        insertFlag =
            GenericUtil.accessControl(
                listResult.getScd(),
                t.getAccessInsertTip(),
                t.getAccessInsertRoles(),
                t.getAccessInsertUsers());
      }
      if (insertFlag) {
        s3.append("<a class=\"fab-label-button\" @click=\"clickNewRecord\" href=# class=\"item-link\"><span><i class=\"icon material-icons\">add</i></span><span class=\"fab-label\">Add</span></a>");
      }
      if (s3.length() > 0)
        s2.append(
                "\n<div class=\"fab fab-right-bottom\"><a href=\"#\"><i class=\"icon f7-icons if-not-md\">add</i><i class=\"icon f7-icons if-not-md\">close</i>\r\n"
                    + "      <i class=\"icon material-icons md-only\">add</i><i class=\"icon material-icons md-only\">close</i>\r\n"
                    + "    </a><div class=\"fab-buttons fab-buttons-top\">")
            .append(s3)
            .append("</div></div>");

      s2.append("\n<div id=\"idx-page-content-")
          .append(l.getListId())
          .append("\" class=\"page-content ptr-content");
      //			if(l.getDefaultPageRecordNumber()>0)s2.append("{{#if infiniteScroll}}
      // infinite-scroll-content infinite-scroll-top{{/if}}");
      //			if(l.getHideBarsOnScrollFlag()!=0)s2.append(" hide-bars-on-scroll");
      s2.append("\">");

      s2.append(
          "<div class=\"ptr-preloader\"><div class=\"preloader\"></div><div class=\"ptr-arrow\"></div></div>");
      if (searchBar) {
        s2.append("<div class=\"searchbar-backdrop\"></div>");
      }


      if(l.getListTip()==1 || l.getListTip()==4)s2.append("<div class=\"list").append(searchBar ? " searchbar-found" : "").append("\"><ul>");
      if (!GenericUtil.isEmpty(htmlDataCode)) s2.append(htmlDataCode);
      if (l.getDefaultPageRecordNumber() > 0) s2.append("\n{{#if infiniteScroll}}<div><p class=\"row\"><button @click=\"moreLoad\" class=\"button col\">more...</button></p><p class=\"row\"></p></div>{{/if}}");
      if(l.getListTip()==1 || l.getListTip()==4)s2.append("</ul></div>");
      // if(l.getDefaultPageRecordNumber()>0)s2.append("{{#if infiniteScroll}}<div class=\"preloader
      // infinite-scroll-preloader\"></div>{{/if}}");

      if (searchBar)
        s2.append(
            "<div class=\"block searchbar-not-found\"><div class=\"block-inner\">Nothing found</div></div>");
      s2.append("</div></div>");

      buf.append(s2.toString());
    } else {
      buf.append(
          GenericUtil.filterExt(
                  l.getHtmlPageCode().replace("${iwb-data}", htmlDataCode),
                  listResult.getScd(),
                  listResult.getRequestParams(),
                  null)
              .toString());
    }
    buf.append("`");
    //state
    buf.append(",\n data:function(){return {sort:'',dir:'',data:[], infiniteScroll:false, params:{}, baseParams:").append(GenericUtil.fromMapToJsonString(listResult.getRequestParams())).append(", browseInfo:{startRow:0, totalCount:0}}}");
    buf.append(",\n on:{pageDestroy:function(){if(this.ptr)this.ptr.destroy('#idx-page-content-")
        .append(l.getListId())
        .append(".ptr-content');},pageMounted:function(){this.load(0);");
    if(searchBar)buf.append("iwb.app.searchbar.create({el: '#idx-searchbar-").append(l.getListId()).append(".searchbar',searchContainer: '.list',searchIn: '.item-title'});");
    buf.append("},pageInit: function (e, page) {var self=this;setTimeout(function(){self.ptr=iwb.app.ptr.get('#idx-page-content-")
        .append(l.getListId())
        .append(".ptr-content');self.ptr.on('refresh',self.firstLoad);");
    // if(l.getDefaultPageRecordNumber()>0)buf.append("var
    // ic=$$('#idx-page-content-").append(l.getListId()).append(".infinite-scroll-content');console.log('infinite-scroll-content', ic);if(ic && ic.length)iwb.app.on('infinite', function () {console.log('!iwb.allowInfinite',iwb.allowInfinite,self);if(!iwb.allowInfinite)return;iwb.allowInfinite=false;self.load(self.browseInfo ? self.browseInfo.startRow:0,function(){iwb.allowInfinite=!0;});});");
    buf.append("},100);");
    if (!GenericUtil.isEmpty(l.getJsCode())) {
        if(l.getJsCode().charAt(0)!='{')buf.append("\ntry{").append(l.getJsCode()).append("\n}catch(e){if(iwb.debug && confirm('iwb.request.pageInit Exception. Throw?'))throw e;}");
        else buf.append("\nif(this.init)this.init();");
    }
    buf.append("}},\n methods:{firstLoad:function(){this.load(0);},moreLoad:function(){this.load(this.browseInfo.startRow);},load:function(start,callback,params){if(!start)start=0;var self = this;iwb.request({url:'ajaxQueryData?_renderer=ext3_4&_qid=")
        .append(listResult.getList().getQueryId());
    if (l.getParentListId() != 0) {
      for (String key : listResult.getRequestParams().keySet())
        if (key.startsWith("x")) {
          int val = GenericUtil.uInt(listResult.getRequestParams().get(key));
          if (val > 0) buf.append("&").append(key).append("=").append(val);
        }
    }
    buf.append("', data:Object.assign({sort:this.sort,dir:this.dir");
    if (l.getDefaultPageRecordNumber() > 0)
      buf.append(",start:start,limit:").append(l.getDefaultPageRecordNumber());
    buf.append("},params||this.params), success:function(j){if(callback)callback();if(params)j.params=params;");
    if (l.getDefaultPageRecordNumber() > 0)
      buf.append(
          "var b=j.browseInfo;j.infiniteScroll=b.startRow+b.fetchCount<b.totalCount;if(b.fetchCount){b.startRow+=b.fetchCount;if(start){j.data=self.data.concat(j.data);}};");
    buf.append(
        "self.$setState(j);if(self.ptr)self.ptr.done()}});}, reload:function(){this.load(0);},clickMenu:function(event){iwb.showRecordMenu({_event:event, _this:this");
    if (l.getDefaultCrudFormId() != 0 && l.get_mainTable() != null) {
      W5Table t = l.get_mainTable();
      insertFlag =
          GenericUtil.accessControl(
              listResult.getScd(),
              t.getAccessInsertTip(),
              t.getAccessInsertRoles(),
              t.getAccessInsertUsers());
      buf.append(",\n crudFormId:")
          .append(l.getDefaultCrudFormId())
          .append(",\n crudTableId:")
          .append(t.getTableId())
          .append(",\n pkName:'")
          .append(t.get_tableParamList().get(0).getDsc())
          .append("',\n crudFlags:{insert:")
          .append(insertFlag)
          .append(",edit:")
          .append(
              t.getAccessUpdateUserFields() != null
                  || GenericUtil.accessControl(
                      listResult.getScd(),
                      t.getAccessUpdateTip(),
                      t.getAccessUpdateRoles(),
                      t.getAccessUpdateUsers()))
          .append(",remove:")
          .append(
              t.getAccessDeleteUserFields() != null
                  || GenericUtil.accessControl(
                      listResult.getScd(),
                      t.getAccessDeleteTip(),
                      t.getAccessDeleteRoles(),
                      t.getAccessDeleteUsers()));
      buf.append("}");
    }

    StringBuilder s2 = new StringBuilder();
    if (!GenericUtil.isEmpty(l.get_detailMLists())) {
      for (M5List d : l.get_detailMLists())
        s2.append("{icon:'list', text:'")
            .append(LocaleMsgCache.get2(listResult.getScd(), d.getLocaleMsgKey()))
            .append("',href:'/showMList?_lid=")
            .append(d.getListId())
            .append("&x")
            .append(l.get_mainTable().get_tableFieldList().get(0).getDsc())
            .append("='},"); // TODO. parent'takine gore degil de, farkli olmasi gerekli
    }

    if (!GenericUtil.isEmpty(l.get_menuItemList())) {
      for (W5ObjectMenuItem d : l.get_menuItemList())
        if (d.getItemTip() == 1 && !GenericUtil.isEmpty(d.getCode())) { // record ile ilgili
          s2.append("{icon:'")
              .append(d.getImgIcon())
              .append("', text:'")
              .append(LocaleMsgCache.get2(listResult.getScd(), d.getLocaleMsgKey()))
              .append("'");
          if (d.getCode().charAt(0) != '!')
            s2.append(",click:function(ax,bx,cx){\n").append(d.getCode()).append("\n}");
          else s2.append(",href:'").append(d.getCode().substring(1)).append("'");
          s2.append("},");
        }
    }

    if (s2.length() > 0) {
      s2.setLength(s2.length() - 1);
      buf.append("\n, recordButtons:[").append(s2).append("]");
    }

    buf.append(
        "}, event.target);},clickSort:function(){iwb.orderList(this);},clickFilter:function(){if(this.sf)return;this.sf=this.$options.props.searchForm;iwb.currentLoader=this.load;$$('#idx-search-panel').html(this.sf.template);\nif(this.sf.on && this.sf.on.pageMounted){var xpageMounted=this.sf.on.pageMounted; setTimeout(()=>{xpageMounted()\n},200);}}");
    if(insertFlag) {
    	buf.append(",\n clickNewRecord:function(){iwb.currentLoader=this.load;var url='/showMForm?a=2&_fid=").append(l.getDefaultCrudFormId())
    		.append("';if(this._postInsert){url=this._postInsert(url, this);if(url===false)return;};this.$router.navigate(url)}");
    }
    buf.append("}");

    //custom methods if starts wih {init:function(){}....
/*    if (!GenericUtil.isEmpty(l.getJsCode()) && l.getJsCode().charAt(0)=='{') {
    	String jsCode = l.getJsCode().trim().substring(1);
    	if(jsCode.endsWith("}"))jsCode=jsCode.substring(0, jsCode.length()-1);
        buf.append(",\n").append(jsCode);
    } */

    if (!GenericUtil.isEmpty(l.getJsCode()) && l.getJsCode().charAt(0)=='{') {
    /*	String jsCode2 = f.getJsCode().trim().substring(1);
    	if(jsCode2.endsWith("}")){
    		jsCode2 = jsCode2.substring(0, jsCode2.length()-1);
    	}*/
      buf.append(",\n extended:").append(l.getJsCode());
    }
    
    buf.append("}");

    return buf;
  }

  public StringBuilder serializeGetForm(W5FormResult fr) {
    W5Form f = fr.getForm();
    Map scd = fr.getScd();
    String xlocale = (String) scd.get("locale");
    int customizationId = (Integer) scd.get("customizationId");
    StringBuilder s = new StringBuilder();
    s.append("{success:true, props:{formId:")
        .append(f.getFormId())
        .append(",\n name:'")
        .append(LocaleMsgCache.get2(fr.getScd(), f.getLocaleMsgKey()))
        .append("'");
    boolean pictureFlag = false;

    Map<Integer, List<W5FormCellHelper>> map = new HashMap<Integer, List<W5FormCellHelper>>();
    map.put(0, new ArrayList<W5FormCellHelper>());
    if (fr.getForm().get_moduleList() != null)
      for (W5FormModule m : fr.getForm().get_moduleList()) {
        map.put(m.getFormModuleId(), new ArrayList<W5FormCellHelper>());
      }
    else {
      fr.getForm().set_moduleList(new ArrayList());
    }
    for (W5FormCellHelper m : fr.getFormCellResults())
      if (m.getFormCell().getActiveFlag() != 0) {
        List<W5FormCellHelper> l = map.get(m.getFormCell().getFormModuleId());
        if (l == null) l = map.get(0);
        l.add(m);
      }

    boolean masterDetail = false;

    if (f.getObjectType() == 2) {
      W5Table t = FrameworkCache.getTable(scd, f.getObjectId());
      pictureFlag = t.getFileAttachmentFlag() != 0;
      // insert AND continue control
      s.append(",\n crudTableId:").append(f.getObjectId());
      if (fr.getAction() == 2) { // insert
        long tmpId = -GenericUtil.getNextTmpId();
        s.append(",\n contFlag:")
            .append(f.getContEntryFlag() != 0)
            .append(",\n tmpId:")
            .append(tmpId);
        fr.getRequestParams().put("_tmpId", "" + tmpId);
      } else if (fr.getAction() == 1) { // edit
        s.append(",id:'")
            .append(fr.getUniqueId())
            .append("',\n pk:")
            .append(GenericUtil.fromMapToJsonString(fr.getPkFields()));
      }
      //			if (pictureFlag)s.append(",\n pictureFlag:true,
      // pictureCount:").append(fr.getPictureCount());
      if (FrameworkCache.getAppSettingIntValue(scd, "file_attachment_flag") != 0
          && t.getFileAttachmentFlag() != 0
          && FrameworkCache.roleAccessControl(scd, 101))
        s.append(",\n fileAttachFlag:true, fileAttachCount:")
            .append(fr.getFileAttachmentCount());
      if (!GenericUtil.isEmpty(f.get_moduleList()) && fr.getModuleListMap() != null) {
        s.append(",\n subLists:[");
        boolean bq = false;
        for (W5FormModule fm : f.get_moduleList())
          if (fm.getModuleType() == 10
              && (fm.getModuleViewType() == 0 || fr.getAction() == fm.getModuleViewType())) {
            M5ListResult mlr = fr.getModuleListMap().get(fm.getObjectId());
            if (mlr == null) continue;
            if (bq) s.append("\n,");
            else bq = true;
            s.append(serializeList(mlr));
            masterDetail = true;
          }

        s.append("]");
      }
    }

    if (fr.isViewMode()) s.append(",\n viewMode:true");

    if (!fr.getOutputMessages().isEmpty()) {
      s.append(",\n\"msgs\":[");
      boolean b = false;
      for (String sx : fr.getOutputMessages()) {
        if (b) s.append("\n,");
        else b = true;
        s.append("'").append(GenericUtil.stringToJS(sx)).append("'");
      }
      s.append("]");
    }
    StringBuilder jsCode = new StringBuilder();
    s.append(",\n baseParams:")
        .append(GenericUtil.fromMapToJsonString(fr.getRequestParams()))
        .append("},\n template:`");

    //		buf.append(PromisUtil.filterExt(fc.getExtraDefinition(), fr.getScd(),
    // fr.getRequestParams(), o));

    if (f.getObjectType() == 2) {
      s.append("<div data-page=\"iwb-form-")
          .append(fr.getFormId())
          .append("\" class=\"page\"><div class=\"navbar\">")
          .append(
              "<div class=\"navbar-inner\"><div class=\"left\"><a href=\"#\" class=\"link back\"> <i class=\"icon icon-back\"></i> <span class=\"if-not-md\">Back</span></a></div><div class=\"center\">")
          .append(fr.getForm().getLocaleMsgKey())
          .append("</div><div class=\"right\">");
      if (pictureFlag) {
        s.append(
            "<a @click=\"clickPhoto\" href=# class=\"link\"><i class=\"icon f7-icons\">camera_fill<span d=\"idx-photo-badge-").append(fr.getFormId()).append("\" class=\"badge color-red\"");
        if (fr.getFileAttachmentCount() > 0)
          s.append(">").append(fr.getFileAttachmentCount());
        else s.append(" style=\"display:none;\">1");
        s.append("</span></i></a>");
      }
      s.append("</div></div></div> <div class=\"page-content\"><form class=\"list\" id=\"idx-form-")
          .append(fr.getFormId())
          .append("\">");
      if (fr.getApprovalRecord() != null) { // Approval Started
			W5Workflow a = FrameworkCache.getWorkflow(fr.getScd(),fr.getApprovalRecord().getApprovalId());
			if (fr.getApprovalRecord().getApprovalStepId() == 901) {// kendisi start for approval yapacak
				if ((a.getManualAppUserIds() == null
						&& a.getManualAppRoleIds() == null
						&& GenericUtil
								.accessControl(scd, fr
										.getApprovalRecord()
										.getApprovalActionTip() /*
																 * ??? Bu ne
																 */,
										fr.getApprovalRecord()
												.getApprovalRoles(), fr
												.getApprovalRecord()
												.getApprovalUsers()) || (GenericUtil
						.hasPartInside2(a.getManualAppRoleIds(),
								scd.get("roleId")) || GenericUtil
						.hasPartInside2(a.getManualAppUserIds(),
								scd.get("userId")))) // Burası daha güzel
														// yazılabilir
				)// TODO:Buraya tableUserIdField yetki kontrolü eklenecek
		        s.append(
				  "<div class=\"block\"><i class=\"icon f7-icons\" style=\"font-size: 17px;margin-top: -5px;\">bolt_fill</i> ")
				  .append(LocaleMsgCache.get2(scd, a.getDsc()))
				  .append("<p class=\"buttons-row\"><a href=# class=\"button button-big button-fill button-raised color-orange\">")
				  .append(LocaleMsgCache.get2(scd, a.getApprovalRequestMsg()))
				  .append("</a></p></div>");
			} else if (GenericUtil.accessControl(scd, (short) 1, fr
					.getApprovalRecord().getApprovalRoles(), fr
					.getApprovalRecord().getApprovalUsers())) {
				// TODO:buraya e-sign ile ilgili kontrol eklenecek. dinamik onay
				// varsa approval değilse aprrovalstep kontrol edilecek
		        s.append(
				  "<div class=\"block\"><i class=\"icon f7-icons\" style=\"font-size: 17px;margin-top: -5px;\">bolt_fill</i> ")
				  .append(LocaleMsgCache.get2(scd, a.getDsc()));
				if(fr.getApprovalStep()!=null)s.append(" &gt ").append(LocaleMsgCache.get2(scd, fr.getApprovalStep().getDsc()));
				s.append("<p class=\"buttons-row row\"><a href=# class=\"button button-large button-fill button-raised color-red\">")
				  .append(LocaleMsgCache.get2(scd, "reject"))
				  .append("</a>");
				if(fr.getApprovalRecord().getReturnFlag() != 0)s.append("<a href=# class=\"button button-large button-fill button-raised color-gray\">")
				  .append(LocaleMsgCache.get2(scd, fr.getApprovalStep()!=null ? fr.getApprovalStep().getBtnReturnLabel():"return"))
				  .append("</a>");
				  
				s.append("<a href=# class=\"button button-large button-fill button-raised color-green\">")
				  .append(LocaleMsgCache.get2(scd, fr.getApprovalStep()!=null ? fr.getApprovalStep().getBtnApproveLabel():"approve"))
				  .append("</a>");
				  
				s.append("</p></div>");

/*				  
				s.append(",\n approval:{approvalRecordId:")
						.append(fr.getApprovalRecord()
								.getApprovalRecordId())
						.append(",dsc:\"").append(LocaleMsgCache.get2(scd, a.getDsc()))
						.append("\", versionNo:")
						.append(fr.getApprovalRecord().getVersionNo())
						.append(",returnFlag:")
						.append(fr.getApprovalRecord().getReturnFlag() != 0);
				W5WorkflowStep wfs = a.get_approvalStepMap().get(fr.getApprovalRecord().getApprovalStepId());
				if(wfs.getOnApproveFormId()!=null)s.append(",approveFormId:").append(wfs.getOnApproveFormId());
				if(wfs.getOnRejectFormId()!=null)s.append(",rejectFormId:").append(wfs.getOnRejectFormId());
				if(wfs.getOnReturnFormId()!=null)s.append(",returnFormId:").append(wfs.getOnReturnFormId());
				s.append(",stepDsc:\"")
						.append(fr.getApprovalStep() != null ? fr.getApprovalStep().getDsc() : "-")
						.append("\"}");*/
			} else {
		        s.append(
				  "<div class=\"block\"><i class=\"icon f7-icons\" style=\"font-size: 17px;margin-top: -5px;\">bolt_fill</i> ")
				  .append(LocaleMsgCache.get2(scd, a.getDsc()));
				if(fr.getApprovalStep()!=null)s.append(" &gt ").append(LocaleMsgCache.get2(scd, fr.getApprovalStep().getDsc()));
				s.append("</div>");
			}
		} else { // Onay mekanizması başlamamış ama acaba başlatma isteği manual

		}
      s.append("<ul>");
    }

    //		    <div class="block-title">With Floating Labels</div>
    List<W5FormModule> ml = new ArrayList();
    boolean found = false;
    for (W5FormModule m : fr.getForm().get_moduleList())
      if (m.getFormModuleId() == 0) {
        found = true;
        break;
      }
    if (!found) ml.add(new W5FormModule());
    ml.addAll(fr.getForm().get_moduleList());

    for (W5FormModule m : ml) {
      List<W5FormCellHelper> r = map.get(m.getFormModuleId());
      if (GenericUtil.isEmpty(r)) continue;
      if (m.getFormModuleId() != 0) {
        W5FormCell fcx = new W5FormCell();
        fcx.setControlType((short) 102);
        fcx.setLookupQueryId(10);
        fcx.setLocaleMsgKey(m.getLocaleMsgKey());
        s.append(
            GenericUtil.stringToJS(
                serializeFormCell(
                    customizationId, xlocale, new W5FormCellHelper(fcx), fr)));
      }
      for (W5FormCellHelper fc : r) {
        s.append(
            GenericUtil.stringToJS(serializeFormCell(customizationId, xlocale, fc, fr)));
        switch (fc.getFormCell().getControlType()) {
          case 2: // date
            jsCode
                .append("iwb.app.calendar.create({inputEl: '#idx-formcell-")
                .append(fc.getFormCell().getFormCellId())
                .append("',dateFormat: 'dd/mm/yyyy'});\n");
            break;
          case 10: // autocomplete
          case 61: // autocomplete-multi
        	  jsCode
	          	.append("iwb.app.autocomplete.create(");
	          Map<Integer, String> multiRelatedComboMap = new HashMap();
	          for (W5FormCellHelper cfc : fr.getFormCellResults()) {
	            if (cfc.getFormCell().getParentFormCellId() == fc.getFormCell().getFormCellId()) {
	              if (!GenericUtil.isEmpty(cfc.getFormCell().getLookupIncludedParams()))
	                switch (cfc.getFormCell().getControlType()) {
	                  case 9:
	                  case 16:
	                  	multiRelatedComboMap.put(cfc.getFormCell().getFormCellId(), cfc.getFormCell().getLookupIncludedParams());
	                    break;
	                  default:
	                    // jsCode.append("{}));\n");
	
	                    break;
	                }
	            }
	          }
	          if(!multiRelatedComboMap.isEmpty()){
	              jsCode
	              .append("iwb.autoCompleteJson4AutocompleteMulti(")
	              .append(fc.getFormCell().getLookupQueryId()) //queryId
	              .append(",'")
	              .append(GenericUtil.isEmpty(fc.getValue()) ? "" : fc.getValue()) //initVal
	              .append("','#idx-formcell-") // initCombo
	              .append(fc.getFormCell().getFormCellId())
	              .append("',[");
	              for(Integer childFormCellId:multiRelatedComboMap.keySet())
	              	jsCode.append("{cmb:'#idx-formcell-")
		                	.append(childFormCellId)
		                	.append("',fnc:function(ax,bx){\n")
		                	.append(multiRelatedComboMap.get(childFormCellId))
		                .append("\n}},");
	              jsCode.setLength(jsCode.length()-1);
	              
	              jsCode.append("]));\n");
	
	          } else {
	            jsCode.append("iwb.autoCompleteJson('").append(fc.getFormCell().getLookupQueryId());
	            if (fc.getFormCell().getLookupIncludedParams() != null
	                && fc.getFormCell().getLookupIncludedParams().length() > 2)
	              jsCode.append("&").append(fc.getFormCell().getLookupIncludedParams());
	            jsCode.append("','#idx-formcell-").append(fc.getFormCell().getFormCellId()).append("'));\n");
	          }
            break;
          case 9:
          case 16:
            if (fr != null
                && !GenericUtil.isEmpty(fc.getFormCell().getLookupIncludedParams())
                && fc.getFormCell().getParentFormCellId() > 0) {
              for (W5FormCellHelper rfc : fr.getFormCellResults()) {
                if (rfc.getFormCell().getFormCellId() == fc.getFormCell().getParentFormCellId()) {
                  W5FormCell pfc = rfc.getFormCell();
                  if (pfc.getControlType() == 6
                      || pfc.getControlType() == 7
                      || pfc.getControlType() == 9)
                    jsCode
                        .append("iwb.combo2combo('#idx-formcell-")
                        .append(pfc.getFormCellId())
                        .append("','#idx-formcell-")
                        .append(fc.getFormCell().getFormCellId())
                        .append("',function(ax,bx){\n")
                        .append(fc.getFormCell().getLookupIncludedParams())
                        .append("\n});\n");
                  break;
                }
              }
            }
            break;
          case 6:
          case 7: // static, query combo
          case 8:
          case 15: // lov-static, lov-query combo
          case 58:
          case 59: // superbox lov-static, superbox lov-query combo
          case 51:
          case 52: // user defined combo, multi
        	  if(f.getObjectType() == 10) {
        		  jsCode
  	          	.append("iwb.app.smartSelect.create({routableModals: false, closeOnSelect:!0, el: '#idx-formcell-")
  	                .append(fc.getFormCell().getFormCellId())
  	                .append("'});\n");
        	  }
        }
      }
    }

    if (f.getObjectType() == 2
        && f.get_formSmsMailList() != null
        && !f.get_formSmsMailList().isEmpty()) { // automatic sms isleri varsa
      StringBuilder s2 = new StringBuilder();
      int cnt = 0;
      for (W5FormSmsMail fsm : f.get_formSmsMailList())
        if (((fsm.getSmsMailType() == 0
                    && FrameworkSetting.sms
                    && FrameworkCache.getAppSettingIntValue(customizationId, "sms_flag") != 0)
                || (fsm.getSmsMailType() != 0
                    && FrameworkSetting.mail
                    && FrameworkCache.getAppSettingIntValue(customizationId, "mail_flag") != 0))
            && fsm.getAlarmFlag() == 0
            && fsm.getPreviewFlag() == 0
            && GenericUtil.hasPartInside2(fsm.getActionTypes(), fr.getAction())
            && GenericUtil.hasPartInside2(fsm.getWebMobileTips(), "2")) {
          cnt++;
        }
      if (cnt > 0) {
        //
        //	s2.append(",\n\"smsMailTemplateCnt\":").append(cnt).append(",\n\"smsMailTemplates\":[");
        boolean b = false;
        for (W5FormSmsMail fsm : f.get_formSmsMailList())
          if (((fsm.getSmsMailType() == 0
                      && FrameworkSetting.sms
                      && FrameworkCache.getAppSettingIntValue(customizationId, "sms_flag") != 0)
                  || (fsm.getSmsMailType() != 0
                      && FrameworkSetting.mail
                      && FrameworkCache.getAppSettingIntValue(customizationId, "mail_flag") != 0))
              && fsm.getAlarmFlag() == 0
              && fsm.getPreviewFlag() == 0
              && GenericUtil.hasPartInside2(fsm.getActionTypes(), fr.getAction())
              && GenericUtil.hasPartInside2(fsm.getWebMobileTips(), "2")) {
            W5FormCell fcx = new W5FormCell();
            fcx.setControlType((short) 5);
            fcx.setLookupQueryId(10);
            fcx.setLocaleMsgKey(
                (fsm.getSmsMailType() == 0
                        ? "[SMS] "
                        : "["
                            + (LocaleMsgCache.get2(customizationId, xlocale, "email_upper"))
                            + "] ")
                    + LocaleMsgCache.get2(customizationId, xlocale, fsm.getDsc())
                    + (fsm.getPreviewFlag() != 0
                        ? " ("
                            + (LocaleMsgCache.get2(customizationId, xlocale, "with_preview"))
                            + ")"
                        : ""));
            fcx.setLookupQueryId(fsm.getFormSmsMailId());
            fcx.setDsc("_smsStr");
            W5FormCellHelper fcr = new W5FormCellHelper(fcx);
            if (fsm.getSmsMailSentType() == 1 || fsm.getSmsMailSentType() == 0) fcr.setValue("1");
            s2.append(
                GenericUtil.stringToJS(
                    serializeFormCell(customizationId, xlocale, fcr, fr)));

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
      if (s2.length() > 0) {
        W5FormCell fcx = new W5FormCell();
        fcx.setControlType((short) 102);
        fcx.setLookupQueryId(11);
        fcx.setLocaleMsgKey("SMS/E-Posta Dönüşümleri");
        s.append(
            GenericUtil.stringToJS(
                serializeFormCell(
                    customizationId, xlocale, new W5FormCellHelper(fcx), fr)));
        s.append(s2);
      }
    }

    if (f.getObjectType() == 2) {
      s.append("</ul>");
      if (!fr.isViewMode()) { // kaydet butonu
        if (masterDetail) // master detail
        s.append(
              "<div class=\"block\"><p class=\"buttons-row\"><a href=# @click=\"clickSaveContinue\" class=\"button button-big button-fill button-raised color-blue\">Next</a></p></div>");
        else
          s.append(
                  "<div class=\"block\"><p class=\"buttons-row\"><a href=# @click=\"clickSave\" class=\"button button-big button-fill button-raised color-blue\">")
              .append(LocaleMsgCache.get2(scd, "save"))
              .append("</a></p></div>");
      }
      s.append("</form></div></div>");
    }
    //		for(W5FormCe){}
    s.append("`,\n on:{");

    if (jsCode.length() > 0) s.append("pageMounted:function(){\n").append(jsCode).append("\n},");

    if (!GenericUtil.isEmpty(f.getJsCode())) {
      s.append("\n pageInit:function(){");//.append(f.getJsCode()).append("\n}");
      if(f.getJsCode().charAt(0)!='{')s.append("try{").append(f.getJsCode()).append("\n}catch(e){if(iwb.debug && confirm('iwb.request.pageInit Exception. Throw?'))throw e;}");
      else s.append("\nif(this.init)this.init();");
      s.append("\n}");
    }

    s.append("}");
    if (f.getObjectType() == 2) {
      s.append(",\n methods:{clickPhoto:function(){iwb.formPhotoMenu(this.$options.props);}");
      if (!fr.isViewMode()) { // kaydet butonu
        if (!masterDetail) // master detail
        s.append(",clickSave:function(){var self=this;var baseParams=")
              .append(GenericUtil.fromMapToJsonString(fr.getRequestParams()))
              .append(
                  ";if(self.componentWillPost){var r=self.componentWillPost(baseParams);if(r===false)return;baseParams=Object.assign(baseParams, r||{})};iwb.submit('#idx-form-")
              .append(f.getFormId())
              .append("',baseParams,function(j){var loader=self.$options.parentLoader;self.$router.back({force:!loader});if(loader)loader();});}");
        else
          s.append(
                  ",clickSaveContinue:function(){var self=this;if(!self.iwbSaveContinue){alert('self.iwbSaveContinue not defined');return;};var baseParams=")
              .append(GenericUtil.fromMapToJsonString(fr.getRequestParams()))
              .append(
                  ";if(self.componentWillPost)baseParams=self.componentWillPost(baseParams);if(baseParams!==false)iwb.submit('#idx-form-")
              .append(f.getFormId())
              .append("',baseParams,function(j){var loader=self.$options.parentLoader;self.$router.back({force:!loader});if(loader)loader();});}");
      }
      
/*      if (!GenericUtil.isEmpty(f.getJsCode()) && f.getJsCode().charAt(0)=='{') {
      	String jsCode2 = f.getJsCode().trim().substring(1);
      	if(jsCode2.endsWith("}"))jsCode2 = jsCode2.substring(0, jsCode2.length()-1);
          s.append(",\n").append(jsCode2);
      } */
      if (!GenericUtil.isEmpty(f.getJsCode()) && f.getJsCode().charAt(0)=='{') {
      /*	String jsCode2 = f.getJsCode().trim().substring(1);
      	if(jsCode2.endsWith("}")){
      		jsCode2 = jsCode2.substring(0, jsCode2.length()-1);
      	}*/
        s.append(",\n extended:").append(f.getJsCode());
      }

      s.append("}");
    }

    s.append("}");
    return s;
  }
  

  @SuppressWarnings("unchecked")
  private StringBuilder serializeFormCell(
      int customizationId, String xlocale, W5FormCellHelper cellResult, W5FormResult fr) {
    W5FormCell fc = cellResult.getFormCell();
    String value = cellResult.getValue(); // bu ilerde hashmap ten gelebilir
    // int customizationId =
    // PromisUtil.uInt(fr.getScd().get("customizationId"));
    StringBuilder buf = new StringBuilder();

    String fieldLabel = LocaleMsgCache.get2(customizationId, xlocale, fc.getLocaleMsgKey());
    String readOnly =
        cellResult.getHiddenValue() != null ? " readonly style=\"background-color:#eee;\"" : "";
    String notNull =
        cellResult.getHiddenValue() == null && fc.getNotNullFlag() != 0
            ? " style=\"color:red\""
            : "";

    if (!GenericUtil.isEmpty(fc.getExtraDefinition())) {
      Map o = new HashMap();
      o.put("value", value);
      o.put("name", fc.getDsc());
      o.put("label", fieldLabel);
      o.put("readOnly", readOnly);
      o.put("notNull", notNull);
      buf.append(
          GenericUtil.filterExt(
              fc.getExtraDefinition(), fr.getScd(), fr.getRequestParams(), o));
      return buf;
    }
    if ((fc.getControlType() == 101
        || cellResult.getHiddenValue()
            != null) /* && (fc.getControlTip()!=9 && fc.getControlTip()!=16) */) { // readonly
      buf.append("<li id=\"id-formcell-")
          .append(fc.getFormCellId())
          .append(
              "\"><div class=\"item-content item-input item-input-outline\"><div class=\"item-inner\">")
          .append("<div class=\"item-title item-floating-label\" style=\"border: 1px solid rgba(0,0,0,.2);border-radius: 3px !important; padding: 1px 4px 2px;\">")
          .append(fieldLabel)
          .append("</div>")
          .append(
              "<div class=\"item-input-wrap\"><input type=text readonly style=\"background-color:rgba(0,0,0,.04)\" value=\"");
      if (!GenericUtil.isEmpty(value))buf.append(value);
          buf.append("\"/>");
      ;
      buf.append("</div></div></div></li>");
      return buf;
    }

    switch (fc.getControlType()) {
      case 102: // label
        if (fc.getLookupQueryId() >= 10) {
          buf.append("<li class=\"iwb-form-tab iwb-type-")
              .append(fc.getLookupQueryId())
              .append(
                  "\"><div class=\"block-title\" style=\"text-transform: uppercase;text-align:center;\">")
              .append(GenericUtil.uStrNvl(value, fieldLabel))
              .append("</div></li>");
        } else {
          buf.append("<li><div class=\"block-title iwb-label-")
              .append(fc.getLookupQueryId())
              .append("\"><i class=\"icon material-icons\">")
              .append(labelMap[fc.getLookupQueryId()])
              .append("</i>&nbsp; ")
              .append(GenericUtil.uStrNvl(value, fieldLabel))
              .append("</div></li>");
        }
        break;
      case 1:
      case 3:
      case 4:
      case 21: // string, integer, double, localeMsgKey
      case 19: // ozel string
        buf.append("<li id=\"id-formcell-")
            .append(fc.getFormCellId())
            .append(
                "\"><div class=\"item-content item-input item-input-outline\"><div class=\"item-inner\">")
            .append("<div class=\"item-title item-floating-label\"")
            .append(notNull)
            .append(">")
            .append(fieldLabel)
            .append("</div>")
            .append("<div class=\"item-input-wrap\"><input type=\"text\" name=\"")
            .append(fc.getDsc())
            .append("\"")
            .append(readOnly);
        if (!GenericUtil.isEmpty(value)) buf.append(" value=\"").append(value).append("\"");
        buf.append(" placeholder=\"\">") // <span class="input-clear-button"></span>
            .append("</div></div></div></li>");
        return buf;
      case 2: // date
        buf.append("<li id=\"id-formcell-")
            .append(fc.getFormCellId())
            .append("\"><div class=\"item-content\"><div class=\"item-inner\">")
            .append("<div class=\"item-title\">")
            .append(fieldLabel)
            .append("</div>")
            .append("<div class=\"item-input\"><input type=\"text\" readonly name=\"")
            .append(fc.getDsc())
            .append("\"")
            .append(" id=\"idx-formcell-")
            .append(fc.getFormCellId())
            .append("\"")
            .append(readOnly);
        if (!GenericUtil.isEmpty(value)) buf.append(" value=\"").append(value).append("\"");
        buf.append(" placeholder=\"\">") // <span class="input-clear-button"></span>
            .append("</div></div></div></li>");
        return buf;
      case 10: // autocomplete
      case 61: // autocomplete-multi
        buf.append("<li id=\"id-formcell-")
            .append(fc.getFormCellId())
            .append("\"><a href=# id=\"idx-formcell-")
            .append(fc.getFormCellId())
            .append("\" class=\"item-link item-content\"><input type=\"hidden\" name=\"")
            .append(fc.getDsc())
            .append("\"");
        if (!GenericUtil.isEmpty(value)) buf.append(" value=\"").append(value).append("\"");
        buf.append(
                "><div class=\"item-inner\"><div class=\"item-title\">")
            .append(fieldLabel)
            .append("</div>")
            .append("<div class=\"item-after\">");
        if (value != null
            && cellResult.getLookupQueryResult() != null
            && cellResult.getLookupQueryResult().getData().size() > 0) {
          Object[] oo = cellResult.getLookupQueryResult().getData().get(0);
          buf.append(oo[0]);
        }

        buf.append("</div></div></a></li>");
        return buf;
      case 6:
      case 7: // static, query combo
      case 8:
      case 15: // lov-static, lov-query combo
      case 58:
      case 59: // superbox lov-static, superbox lov-query combo
      case 51:
      case 52: // user defined combo, multi
        boolean multi =
            fc.getControlType() != 6 && fc.getControlType() != 7 && fc.getControlType() != 51;
        StringBuilder resultText = new StringBuilder();
        buf.append("<li id=\"id-formcell-")
            .append(fc.getFormCellId())
            .append("\"><a href=#")
            .append(multi ? "" : " data-close-on-select=\"true\"")
            .append(" id=\"sid-formcell-")
            .append(fc.getFormCellId())
            .append("\"")
            .append(" class=\"item-link smart-select smart-select-init\"");
        int len =
            cellResult.getLookupListValues() != null
                ? cellResult.getLookupListValues().size()
                : (cellResult.getLookupQueryResult() != null
                    ? cellResult.getLookupQueryResult().getData().size()
                    : 0);
        if (len > 100) buf.append(" data-virtual-list=\"true\"");

        if (len > 10)
          buf.append(
                  " data-open-in=\"popup\" data-searchbar=\"true\" data-searchbar-placeholder=\"")
              .append(LocaleMsgCache.get2(fr.getScd(), "search"))
              .append("..\"");
        else buf.append(" data-open-in=\"popover\"");
        buf.append("><select id=\"idx-formcell-")
            .append(fc.getFormCellId())
            .append("\" name=\"")
            .append(fc.getDsc())
            .append("\"")
            .append(multi ? " multiple" : "")
            .append(">");
        if (fc.getNotNullFlag() == 0 && !multi) buf.append("<option value=\"\"></option>");
        if (cellResult.getLookupListValues() != null) { // lookup static
          for (W5Detay p : (List<W5Detay>) cellResult.getLookupListValues()) {
            buf.append("<option value=\"").append(p.getVal()).append("\"");
            if ((!multi && GenericUtil.safeEquals(value, p.getVal()))
                || (multi && GenericUtil.hasPartInside2(value, p.getVal()))) {
              buf.append(" selected");
              resultText
                  .append(
                      cellResult.getLocaleMsgFlag() != 0
                          ? LocaleMsgCache.get2(customizationId, xlocale, p.getDsc())
                          : p.getDsc())
                  .append(", ");
            }
            buf.append(">")
                .append(
                    cellResult.getLocaleMsgFlag() != 0
                        ? LocaleMsgCache.get2(customizationId, xlocale, p.getDsc())
                        : p.getDsc())
                .append("</option>");
          }
        } else if (cellResult.getLookupQueryResult() != null) { // QueryResult'tan geliyor
          for (Object[] p : cellResult.getLookupQueryResult().getData()) {
            buf.append("<option value=\"").append(p[1]).append("\"");
            if ((!multi && GenericUtil.safeEquals(value, p[1]))
                || (multi && GenericUtil.hasPartInside2(value, p[1]))) {
              buf.append(" selected");
              resultText.append(p[0]).append(", ");
            }
            buf.append(">").append(p[0]).append("</option>");
          }
        }
        buf.append(
                "</select><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title label\"")
            .append(notNull)
            .append(">")
            .append(fieldLabel)
            .append("</div><div class=\"item-after\">");
        if (resultText.length() > 0) {
          resultText.setLength(resultText.length() - 2);
          buf.append(resultText);
        }
        buf.append("</div></div></div></a></li>");
        return buf;

      case 9: // combo-remote
      case 16: // lovcombo-remote
        multi = fc.getControlType() == 16;
        buf.append("<li id=\"id-formcell-")
            .append(fc.getFormCellId())
            .append("\"><a href=#")
            .append(multi ? "" : " data-close-on-select=\"true\"")
            .append(" id=\"sid-formcell-")
            .append(fc.getFormCellId())
            .append("\"")
            .append(" class=\"item-link smart-select smart-select-init\"")
            .append(" data-searchbar=\"true\" data-searchbar-placeholder=\"")
            .append(LocaleMsgCache.get2(fr.getScd(), "search"))
            .append("...\" data-open-in=\"popup\">")
            .append("<select id=\"idx-formcell-")
            .append(fc.getFormCellId())
            .append("\" name=\"")
            .append(fc.getDsc())
            .append("\"")
            .append(multi ? " multiple" : "");
        if (!GenericUtil.isEmpty(value)) buf.append(" data-value=\"").append(value).append("\"");
        buf.append(">");
        buf.append(
                "</select><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title label\"")
            .append(notNull)
            .append(">")
            .append(fieldLabel)
            .append("</div><div class=\"item-after\">");
        buf.append("</div></div></div></a></li>");
        return buf;
      case 11: // textarea
      case 41:
      case 25: // codemirror, ozel tanimlama textarea
        buf.append("<li id=\"id-formcell-")
            .append(fc.getFormCellId())
            .append(
                "\" class=\"align-top\"><div class=\"item-content item-input item-input-outline\"><div class=\"item-inner\"><div class=\"item-title item-floating-label\"")
            .append(notNull)
            .append(">")
            .append(fieldLabel)
            .append("</div><div class=\"item-input-wrap\"><textarea")
            .append(readOnly)
            .append(" name=\"")
            .append(fc.getDsc())
            .append("\" class=\"resizable\">")
            .append(value != null ? value : "")
            .append("</textarea></div></div></div></li>");
        return buf;
      case 5: // checkbox
        if (fc.getLookupQueryId() == 0) {
          buf.append("<li id=\"id-formcell-")
              .append(fc.getFormCellId())
              .append(
                  "\"><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title\">")
              .append(fieldLabel)
              .append(
                  "</div><div class=\"item-after\"><label class=\"toggle toggle-init\"><input type=\"checkbox\"")
              .append(GenericUtil.uInt(value) != 0 ? " checked" : "")
              .append(" name=\"")
              .append(fc.getDsc())
              .append("\"");
          buf.append("/><i class=\"toggle-icon\"></i></label></div></div></li>");

          return buf;
        } else {
          buf.append(
                  "<li style=\"top: 0px;\"><label class=\"label-checkbox item-content\"><input type=\"checkbox\" name=\"")
              .append(fc.getDsc())
              .append("\"")
              .append(GenericUtil.uInt(value) != 0 ? " checked" : "")
              .append(" value=")
              .append(fc.getLookupQueryId())
              .append(
                  "><div class=\"item-media\"><i class=\"icon icon-form-checkbox\"></i></div><div class=\"item-inner\"><div class=\"item-title\" style=\"color: #757575;margin-top: 8px;font-size: 15px;\">")
              .append(fieldLabel)
              .append("</div></div></label></li>");
          return buf;
        }
    }
    return buf;
  }
  
  @SuppressWarnings("unchecked")
  private StringBuilder serializeFormCellOld(
      int customizationId, String xlocale, W5FormCellHelper cellResult, W5FormResult fr) {
    W5FormCell fc = cellResult.getFormCell();
    String value = cellResult.getValue(); // bu ilerde hashmap ten gelebilir
    // int customizationId =
    // PromisUtil.uInt(fr.getScd().get("customizationId"));
    StringBuilder buf = new StringBuilder();

    String fieldLabel = LocaleMsgCache.get2(customizationId, xlocale, fc.getLocaleMsgKey());
    String readOnly =
        cellResult.getHiddenValue() != null ? " readonly style=\"background-color:#eee;\"" : "";
    String notNull =
        cellResult.getHiddenValue() == null && fc.getNotNullFlag() != 0
            ? " style=\"color:red\""
            : "";

    if (!GenericUtil.isEmpty(fc.getExtraDefinition())) {
      Map o = new HashMap();
      o.put("value", value);
      o.put("name", fc.getDsc());
      o.put("label", fieldLabel);
      o.put("readOnly", readOnly);
      o.put("notNull", notNull);
      buf.append(
          GenericUtil.filterExt(
              fc.getExtraDefinition(), fr.getScd(), fr.getRequestParams(), o));
      return buf;
    }
    if ((fc.getControlType() == 101
        || cellResult.getHiddenValue()
            != null) /* && (fc.getControlTip()!=9 && fc.getControlTip()!=16) */) { // readonly
      buf.append("<li id=\"id-formcell-")
          .append(fc.getFormCellId())
          .append(
              "\"><div class=\"item-content item-input item-input-outline\"><div class=\"item-inner\">")
          .append("<div class=\"item-title item-floating-label\">")
          .append(fieldLabel)
          .append("</div>")
          .append(
              "<div class=\"item-input-wrap\"><input type=text readonly style=\"background-color:rgba(0,0,0,.07)\" value=\"")
          .append(value)
          .append("\"/>");
      ;
      buf.append("</div></div></div></li>");
      return buf;
    }

    switch (fc.getControlType()) {
      case 102: // label
        if (fc.getLookupQueryId() >= 10) {
          buf.append("<li class=\"iwb-form-tab iwb-type-")
              .append(fc.getLookupQueryId())
              .append(
                  "\"><div class=\"block-title\" style=\"text-transform: uppercase;text-align:center;\">")
              .append(GenericUtil.uStrNvl(value, fieldLabel))
              .append("</div></li>");
        } else {
          buf.append("<li><div class=\"block-title iwb-label-")
              .append(fc.getLookupQueryId())
              .append("\"><i class=\"icon material-icons\">")
              .append(labelMap[fc.getLookupQueryId()])
              .append("</i>&nbsp; ")
              .append(GenericUtil.uStrNvl(value, fieldLabel))
              .append("</div></li>");
        }
        break;
      case 1:
      case 3:
      case 4:
      case 21: // string, integer, double, localeMsgKey
      case 19: // ozel string
        buf.append("<li id=\"id-formcell-")
            .append(fc.getFormCellId())
            .append(
                "\"><div class=\"item-content item-input item-input-outline\"><div class=\"item-inner\">")
            .append("<div class=\"item-title item-floating-label\"")
            .append(notNull)
            .append(">")
            .append(fieldLabel)
            .append("</div>")
            .append("<div class=\"item-input-wrap\"><input type=\"text\" name=\"")
            .append(fc.getDsc())
            .append("\"")
            .append(readOnly);
        if (!GenericUtil.isEmpty(value)) buf.append(" value=\"").append(value).append("\"");
        buf.append(" placeholder=\"\">") // <span class="input-clear-button"></span>
            .append("</div></div></div></li>");
        return buf;
      case 2: // date
        buf.append("<li id=\"id-formcell-")
            .append(fc.getFormCellId())
            .append("\"><div class=\"item-content\"><div class=\"item-inner\">")
            .append("<div class=\"item-title\">")
            .append(fieldLabel)
            .append("</div>")
            .append("<div class=\"item-input\"><input type=\"text\" readonly name=\"")
            .append(fc.getDsc())
            .append("\"")
            .append(" id=\"idx-formcell-")
            .append(fc.getFormCellId())
            .append("\"")
            .append(readOnly);
        if (!GenericUtil.isEmpty(value)) buf.append(" value=\"").append(value).append("\"");
        buf.append(" placeholder=\"\">") // <span class="input-clear-button"></span>
            .append("</div></div></div></li>");
        return buf;
      case 10: // autocomplete
      case 61: // autocomplete-multi
        buf.append("<li id=\"id-formcell-")
            .append(fc.getFormCellId())
            .append("\"><a href=# id=\"idx-formcell-")
            .append(fc.getFormCellId())
            .append("\" class=\"item-link autocomplete-opener\"><input type=\"hidden\" name=\"")
            .append(fc.getDsc())
            .append("\"");
        if (!GenericUtil.isEmpty(value)) buf.append(" value=\"").append(value).append("\"");
        buf.append(
                "><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title label\">")
            .append(fieldLabel)
            .append("</div>")
            .append("<div class=\"item-after\">");
        if (value != null
            && cellResult.getLookupQueryResult() != null
            && cellResult.getLookupQueryResult().getData().size() > 0) {
          Object[] oo = cellResult.getLookupQueryResult().getData().get(0);
          buf.append(oo[0]);
        }

        buf.append("</div></div></div></a></li>");
        return buf;
      case 6:
      case 7: // static, query combo
      case 8:
      case 15: // lov-static, lov-query combo
      case 58:
      case 59: // superbox lov-static, superbox lov-query combo
      case 51:
      case 52: // user defined combo, multi
        boolean multi =
            fc.getControlType() != 6 && fc.getControlType() != 7 && fc.getControlType() != 51;
        StringBuilder resultText = new StringBuilder();
        buf.append("<li id=\"id-formcell-")
            .append(fc.getFormCellId())
            .append("\"><a href=#")
            .append(multi ? "" : " data-close-on-select=\"true\"")
            .append(" class=\"item-link smart-select\"");
        int len =
            cellResult.getLookupListValues() != null
                ? cellResult.getLookupListValues().size()
                : (cellResult.getLookupQueryResult() != null
                    ? cellResult.getLookupQueryResult().getData().size()
                    : 0);
        if (len > 100) buf.append(" data-virtual-list=\"true\"");

        if (len > 10)
          buf.append(
                  " data-open-in=\"popup\" data-searchbar=\"true\" data-searchbar-placeholder=\"")
              .append(LocaleMsgCache.get2(fr.getScd(), "search"))
              .append("..\"");
        else buf.append(" data-open-in=\"popover\"");
        buf.append("><select id=\"idx-formcell-")
            .append(fc.getFormCellId())
            .append("\" name=\"")
            .append(fc.getDsc())
            .append("\"")
            .append(multi ? " multiple" : "")
            .append(">");
        if (fc.getNotNullFlag() == 0 && !multi) buf.append("<option value=\"\"></option>");
        if (cellResult.getLookupListValues() != null) { // lookup static
          for (W5Detay p : (List<W5Detay>) cellResult.getLookupListValues()) {
            buf.append("<option value=\"").append(p.getVal()).append("\"");
            if ((!multi && GenericUtil.safeEquals(value, p.getVal()))
                || (multi && GenericUtil.hasPartInside2(value, p.getVal()))) {
              buf.append(" selected");
              resultText
                  .append(
                      cellResult.getLocaleMsgFlag() != 0
                          ? LocaleMsgCache.get2(customizationId, xlocale, p.getDsc())
                          : p.getDsc())
                  .append(", ");
            }
            buf.append(">")
                .append(
                    cellResult.getLocaleMsgFlag() != 0
                        ? LocaleMsgCache.get2(customizationId, xlocale, p.getDsc())
                        : p.getDsc())
                .append("</option>");
          }
        } else if (cellResult.getLookupQueryResult() != null) { // QueryResult'tan geliyor
          for (Object[] p : cellResult.getLookupQueryResult().getData()) {
            buf.append("<option value=\"").append(p[1]).append("\"");
            if ((!multi && GenericUtil.safeEquals(value, p[1]))
                || (multi && GenericUtil.hasPartInside2(value, p[1]))) {
              buf.append(" selected");
              resultText.append(p[0]).append(", ");
            }
            buf.append(">").append(p[0]).append("</option>");
          }
        }
        buf.append(
                "</select><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title label\"")
            .append(notNull)
            .append(">")
            .append(fieldLabel)
            .append("</div><div class=\"item-after\">");
        if (resultText.length() > 0) {
          resultText.setLength(resultText.length() - 2);
          buf.append(resultText);
        }
        buf.append("</div></div></div></a></li>");
        return buf;

      case 9: // combo-remote
      case 16: // lovcombo-remote
        multi = fc.getControlType() == 16;
        buf.append("<li id=\"id-formcell-")
            .append(fc.getFormCellId())
            .append("\"><a href=#")
            .append(multi ? "" : " data-close-on-select=\"true\"")
            .append(" class=\"item-link smart-select\"")
            .append(" data-searchbar=\"true\" data-searchbar-placeholder=\"")
            .append(LocaleMsgCache.get2(fr.getScd(), "search"))
            .append("...\" data-open-in=\"popup\">")
            .append("<select id=\"idx-formcell-")
            .append(fc.getFormCellId())
            .append("\" name=\"")
            .append(fc.getDsc())
            .append("\"")
            .append(multi ? " multiple" : "");
        if (!GenericUtil.isEmpty(value)) buf.append(" data-value=\"").append(value).append("\"");
        buf.append(">");
        buf.append(
                "</select><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title label\"")
            .append(notNull)
            .append(">")
            .append(fieldLabel)
            .append("</div><div class=\"item-after\">");
        buf.append("</div></div></div></a></li>");
        return buf;
      case 11: // textarea
      case 41:
      case 25: // codemirror, ozel tanimlama textarea
        buf.append("<li id=\"id-formcell-")
            .append(fc.getFormCellId())
            .append(
                "\" class=\"align-top\"><div class=\"item-content item-input item-input-outline\"><div class=\"item-inner\"><div class=\"item-title item-floating-label\"")
            .append(notNull)
            .append(">")
            .append(fieldLabel)
            .append("</div><div class=\"item-input-wrap\"><textarea")
            .append(readOnly)
            .append(" name=\"")
            .append(fc.getDsc())
            .append("\" class=\"resizable\">")
            .append(value != null ? value : "")
            .append("</textarea></div></div></div></li>");
        return buf;
      case 5: // checkbox
        if (fc.getLookupQueryId() == 0) {
          buf.append("<li id=\"id-formcell-")
              .append(fc.getFormCellId())
              .append(
                  "\"><div class=\"item-content\"><div class=\"item-inner\"><div class=\"item-title\">")
              .append(fieldLabel)
              .append(
                  "</div><div class=\"item-after\"><label class=\"toggle toggle-init\"><input type=\"checkbox\"")
              .append(GenericUtil.uInt(value) != 0 ? " checked" : "")
              .append(" name=\"")
              .append(fc.getDsc())
              .append("\"");
          buf.append("/><i class=\"toggle-icon\"></i></label></div></div></div></li>");

          return buf;
        } else {
          buf.append(
                  "<li style=\"top: 0px;\"><label class=\"label-checkbox item-content\"><input type=\"checkbox\" name=\"")
              .append(fc.getDsc())
              .append("\"")
              .append(GenericUtil.uInt(value) != 0 ? " checked" : "")
              .append(" value=")
              .append(fc.getLookupQueryId())
              .append(
                  "><div class=\"item-media\"><i class=\"icon icon-form-checkbox\"></i></div><div class=\"item-inner\"><div class=\"item-title\" style=\"color: #757575;margin-top: 8px;font-size: 15px;\">")
              .append(fieldLabel)
              .append("</div></div></label></li>");
          return buf;
        }
    }
    return buf;
  }

  public StringBuilder serializeFormFromJSON(JSONObject formBuilder) throws JSONException {
    StringBuilder buf = new StringBuilder();
    W5FormResult r = new W5FormResult(1);
    r.setScd(new HashMap());
    r.setRequestParams(new HashMap());
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
    c0.setLocaleMsgKey(formBuilder.getString("title"));
    c0.setControlType((short) 102);
    W5FormCellHelper cellResult0 = new W5FormCellHelper(c0);
    buf.append(serializeFormCell(0, "tr", cellResult0, r));

    if (formBuilder.getJSONArray("items") != null) {
      JSONArray items = formBuilder.getJSONArray("items");
      for (int qi = 0; qi < items.length(); qi++) {
        JSONObject o = items.getJSONObject(qi);
        W5FormCell c = new W5FormCell();
        W5FormCellHelper cellResult = new W5FormCellHelper(c);
        if (o.has("store")) {
          JSONArray storeItems = o.getJSONArray("store");
          cellResult.setLookupListValues(new ArrayList());
          for (int jq = 0; jq < storeItems.length(); jq++) {
            W5LookUpDetay d = new W5LookUpDetay();
            JSONArray i = storeItems.getJSONArray(jq);
            try {
              d.setVal("" + i.getInt(0));
            } catch (Exception e) {
              d.setVal(i.getString(0));
            }
            d.setDsc(i.getString(1));
            cellResult.getLookupListValues().add(d);
          }
          c.setControlType((short) 6);
          ;
        } else c.setControlType(o.has("_controlTip") ? (short) o.getInt("_controlTip") : 1);
        ;
        if (c.getControlType() == 2) {
          int formCellId = (int) new Date().getTime();
          jsCode
              .append("iwb.app.calendar({input: '#idx-formcell-")
              .append(formCellId)
              .append("',dateFormat: 'dd/mm/yyyy'});\n");
          c.setFormCellId(formCellId);
        }
        c.setLocaleMsgKey(o.getString("fieldLabel"));
        ;
        c.setDsc(o.getString("name"));
        ;
        cellResult.setValue("{{" + c.getDsc() + "}}");
        if (o.has("allowBlank")) c.setNotNullFlag((short) (o.getBoolean("allowBlank") ? 0 : 1));

        c.setControlWidth((short) o.getInt("width"));
        ;
        c.setLocaleMsgKey(o.getString("fieldLabel"));
        ;
        buf.append(serializeFormCell(0, "tr", cellResult, r));
      }
    }
    buf.append("'");
    if (jsCode.length() > 0)
      buf.append(",\n init:function(callAttributes){\n").append(jsCode).append("\n}");

    buf.append("}}");
    return buf;
  }

  public StringBuilder serializeGraphDashboard(W5BIGraphDashboard gd, Map<String, Object> scd) {
    StringBuilder buf = new StringBuilder();
    buf.append("{\"dashId\":")
        .append(gd.getGraphDashboardId())
        .append(",\"name\":\"")
        .append(LocaleMsgCache.get2(scd, gd.getLocaleMsgKey()))
        .append("\", \"gridId\":")
        .append(gd.getGridId())
        .append(",\"tableId\":")
        .append(gd.getTableId())
        .append(",\"is3d\":")
        .append(gd.getIs3dFlag() != 0)
        .append(",\"graphTip\":")
        .append(gd.getGraphTip())
        .append(",\"groupBy\":\"")
        .append(gd.getGraphGroupByField())
        .append("\",\"funcTip\":")
        .append(gd.getGraphFuncTip())
        .append(",\"funcFields\":\"")
        .append(gd.getGraphFuncFields())
        .append("\", \"queryParams\":")
        .append(gd.getQueryBaseParams());
    if (gd.getStackedQueryField() != 0)
      buf.append(",\"stackedFieldId\":").append(gd.getStackedQueryField());
    if (gd.getDefaultHeight() != 0) buf.append(",\"height\":").append(gd.getDefaultHeight());
    if (gd.getLegendFlag() != 0) buf.append(",\"legend\":true");
    buf.append("}");
    return buf;
  }
}
