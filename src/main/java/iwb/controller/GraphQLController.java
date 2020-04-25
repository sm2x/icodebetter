package iwb.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
//import iwb.adapter.soap.SoapAdapter;
//import iwb.adapter.soap.impl.AxisSoap1_4;
import iwb.adapter.ui.ViewAdapter;
import iwb.adapter.ui.extjs.ExtJs3_4;
import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.domain.db.Log5Transaction;
import iwb.domain.db.W5Project;
import iwb.domain.db.W5QueryField;
import iwb.domain.db.W5WsServer;
import iwb.domain.db.W5WsServerMethod;
import iwb.domain.db.W5WsServerMethodParam;
import iwb.domain.result.W5QueryResult;
import iwb.exception.IWBException;
import iwb.service.FrameworkService;
import iwb.util.EncryptionUtil;
import iwb.util.GenericUtil;
import iwb.util.HttpUtil;
import iwb.util.LogUtil;

@Controller
@RequestMapping("/graphql")
public class GraphQLController implements InitializingBean {
	private static Logger logger = Logger.getLogger(GraphQLController.class);

	private ViewAdapter ext3_4 = new ExtJs3_4();
	
	@Autowired
	private FrameworkService service;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
	}
	
	private String generateSchema(W5WsServer wss) {
//		Map<String, Object> wsmoMap = service.getWsServerMethodObjects(wss);

		StringBuilder sdl = new StringBuilder(), helper = new StringBuilder();
		
		sdl.append("type Query {\n"); 
    	for(W5WsServerMethod wsm:wss.get_methods()) if(wsm.getObjectTip()==19){//query
    		sdl.append(" ").append(wsm.getDsc());
  //  		W5QueryResult r = (W5QueryResult)wsmoMap.get(wsm.getDsc());
    		StringBuilder params=new StringBuilder();
    		for(W5WsServerMethodParam wsmp:wsm.get_params()) if(wsmp.getOutFlag()==0){
    			params.append(wsmp.getDsc()).append(": ");
    			switch(wsmp.getParamTip()) {
//    			case	5:params.append("Boolean");break;
    			case	4:params.append("Int");break;
    			case	3:params.append("Float");break;
    			default:params.append("String");break;
    			}
    			if(wsmp.getNotNullFlag()!=0 && wsmp.getParamTip()!=5)params.append("!");
    			params.append(" ");
    		}
    		if(params.length()>0) {
    			sdl.append("(").append(params).append(")");
    		}
    		
    		sdl.append(": [").append(wsm.getDsc()).append("Result]\n");
    		
    		helper.append("type ").append(wsm.getDsc()).append("Result{\n");
    		for(W5WsServerMethodParam wsmp:wsm.get_params()) if(wsmp.getOutFlag()!=0 && wsmp.getParentWsMethodParamId()!=0){
    			helper.append(" ").append(wsmp.getDsc()).append(": ");
    			switch(wsmp.getParamTip()) {
    			case	5:helper.append("Boolean");break;
    			case	4:helper.append("Int");break;
    			case	3:helper.append("Float");break;
    			default:helper.append("String");break;
    			}
    			helper.append("\n");
    		}
    		
    		helper.append("}\n");
    		
   	}	
		
		sdl.append("}\n\n").append(helper);
		return sdl.toString();
	}
	
	private DataFetcher<List<Map>> query(final W5WsServerMethod wsm){
         return new DataFetcher<List<Map>>() {
            @Override
            public List<Map> get(DataFetchingEnvironment environment) {
            	Map<String, Object> scd = new HashMap();
            	Map<String, String> requestParams = new HashMap();
    			W5Project po = FrameworkCache.getProject(wsm.getProjectUuid());
    			scd.put("projectId", po.getProjectUuid());
    			scd.put("customizationId", po.getCustomizationId());
    			scd.put("userId", 0);
    			if(environment.getArguments()!=null)for(String k:environment.getArguments().keySet()) {
    				Object o = environment.getArguments().get(k);
    				if(o!=null)o = o.toString();
    				requestParams.put(k, (String)o);
    			}

            	
    			String transactionId =  GenericUtil.getTransactionId();
    			requestParams.put("_trid_", transactionId);
    			if(FrameworkSetting.logType>0)LogUtil.logObject(new Log5Transaction(po.getProjectUuid(), "graphql", transactionId), true);

    			W5QueryResult qr = service.executeQuery(scd, wsm.getObjectId(), requestParams);
				if(GenericUtil.isEmpty(qr.getErrorMap())) {
					if(wsm.get_params()!=null){
						List<W5QueryField> lqf = new ArrayList();
						Map<String,W5QueryField> qfm = new HashMap();
						for(W5QueryField qf:qr.getQuery().get_queryFields()){
							qfm.put(qf.getDsc(), qf);
						}
						for(W5WsServerMethodParam wsmp:wsm.get_params())if(wsmp.getOutFlag()!=0 && wsmp.getParamTip()!=10){
							lqf.add(qfm.get(wsmp.getDsc()));
						}
						qr.setNewQueryFields(lqf);								
					}
					List qdata = qr.getData();
					List<Map> data = new ArrayList(qdata.size());
					if(qr.getData().size()>0) {
						boolean isMap = (qdata.get(0) instanceof Map);
						for(Object o:qdata) {
							Map mo = new HashMap();
							for(W5QueryField f:qr.getNewQueryFields()) {
								Object obj = isMap ? ((Map)o).get(f.getDsc()) : ((Object[])o)[f.getTabOrder() - 1];
								if(obj==null)continue;
								String fdsc = f.getDsc();
								switch(f.getPostProcessTip()) {
								case	9: fdsc = "_" + fdsc;break;
								case	6: fdsc = fdsc.substring(1);break;
								}
								switch(f.getFieldTip()) {
								case	5://boolean
									mo.put(fdsc, GenericUtil.uInt(obj) != 0);break;
								case	8://object/json
									mo.put(fdsc, obj);break;
								default:
									mo.put(fdsc, obj);break;
								}
								
							}
							data.add(mo);
							
						}
					}
					return data;
				} else { //error
					throw new IWBException("validation", "Query", wsm.getObjectId(), GenericUtil.fromMapToJsonString2(qr.getErrorMap()), "Validation Error for Query", null);
				}
            }
        };
	}

    private RuntimeWiring buildWiring(final W5WsServer wss) {
//  	return null;
		return RuntimeWiring.newRuntimeWiring().type("Query", builder -> {
	    	for(W5WsServerMethod wsm:wss.get_methods()) if(wsm.getObjectTip()==19){//query
	    		builder = builder.dataFetcher(wsm.getDsc(), query(wsm));
	    	}
			return builder;
		}).build(); 
    } 


    
	@RequestMapping("/*/*")
	public void hndGraphQL( //project/GraphQLName
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException{
		request.setCharacterEncoding( "UTF-8" );
		response.setCharacterEncoding( "UTF-8" );
		try {
			Map requestParams = GenericUtil.getParameterMap(request);
			String[] u = request.getRequestURI().replace('/', ',').split(",");
			String token = (String)request.getParameter("tokenKey");
			String projectId=u[2]; 
			W5Project po = FrameworkCache.getProject(projectId);
			if(po==null) {
				throw new IWBException("ws","Invalid Project",0,null, "Invalid Project", null);
			}
			String graphQLName=u[3];
			boolean describeSdl = false;
			if(graphQLName.endsWith(".sdl")) {
				graphQLName = graphQLName.substring(0,graphQLName.length()-4);
				describeSdl = true;				
			} else if(requestParams.containsKey("sdl"))
				describeSdl = true;				

		
			response.setContentType("application/json");

			W5WsServer wss = FrameworkCache.getWsServer(projectId, graphQLName);
			if(wss==null)
				throw new IWBException("framework","WrongGraphQL",0,null, "Wrong Service: Should Be [GraphQLName]", null);
			
			if(GenericUtil.isEmpty(wss.get_graphQLSchema())) {
				String sdl = generateSchema(wss);
				SchemaParser schemaParser = new SchemaParser();
				SchemaGenerator schemaGenerator = new SchemaGenerator();
				TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(sdl);
				
				RuntimeWiring runtimeWiring = buildWiring(wss);
				

				GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

				GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();

				wss.set_graphQLSchema(sdl);
				wss.set_graphQLBuild(build);
			}
			
			if(describeSdl) {
				response.getWriter().write(wss.get_graphQLSchema());
				response.getWriter().close();
				return;
			}
			
			Map<String, Object> scd = null;
			scd = GenericUtil.isEmpty(token) ? null : GenericUtil.fromJSONObjectToMap(new JSONObject(EncryptionUtil.decryptAES(token)));
			
			if(scd==null){
				scd = new HashMap();
				scd.put("userId",1);
				scd.put("locale", FrameworkCache.getAppSettingStringValue(0, "locale", "en"));
			}
			scd.put("projectId", projectId);
			scd.put("customizationId",po.getCustomizationId());
			
			JSONObject jo = HttpUtil.getJson(request);
			if(jo!=null){
				requestParams.putAll(GenericUtil.fromJSONObjectToMap(jo));
			}
			String query = (String)requestParams.get("query");;

			ExecutionResult executionResult = wss.get_graphQLBuild().execute(query);

			if(!GenericUtil.isEmpty(executionResult.getErrors()))
				response.getWriter().write(serializeErrors(executionResult.getErrors()));
			else
				response.getWriter().write(serializeResult(executionResult.getData()));
			
			response.getWriter().close();
		} catch (Exception e) {
			if(FrameworkSetting.debug)e.printStackTrace();
			response.getWriter().write(new IWBException("framework","GraphQL Def",0,null, "Error", e).toJsonString(request.getRequestURI()));
		}
	}

	private String serializeResult(Object data) {
		if(data instanceof Map) {
			return GenericUtil.fromMapToJsonString2Recursive((Map)data);
		}
		// TODO Auto-generated method stub
		return "{success:false}";
	}

	private String serializeErrors(List<GraphQLError> errors) {
		Map m = new HashMap();
		m.put("success", false);
		List<Map> xerrors = new ArrayList();
		for(GraphQLError e:errors) {
			Map o = new HashMap();
			o.put("errorType", e.getErrorType());
			o.put("message", e.getMessage());
			o.put("path", e.getPath());
			xerrors.add(o);
		}
		m.put("errors", xerrors);
		// TODO Auto-generated method stub
		return GenericUtil.fromMapToJsonString2Recursive(m);
	}
}
