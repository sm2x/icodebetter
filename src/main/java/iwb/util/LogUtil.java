package iwb.util;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.Channel;

import iwb.cache.FrameworkSetting;
import iwb.model.db.Log5Base;

public class LogUtil {
	private static int errorCount = 0;
	private static Channel mqChannel = null;
	
	public static void activateMQ4Log(){
		if(FrameworkSetting.logType!=2)return;
		try{
			mqChannel = MQUtil.getChannel4Queue(FrameworkSetting.log2mqUrl, FrameworkSetting.log2mqQueue);
		}catch(Exception e){
			if(FrameworkSetting.debug)e.printStackTrace();
		}
		if(mqChannel==null)FrameworkSetting.logType = 0;

	}	
	
	public static void logObject(Log5Base o, boolean withTime){
		if(!FrameworkSetting.log2tsdb || o==null)return;
		String str = o.toInfluxDB();
		if(GenericUtil.isEmpty(str))return;
		StringBuilder s=new StringBuilder(str);
		if(withTime)s.append(" ").append(Instant.now().toEpochMilli()).append("000000");
		try {
			if(FrameworkSetting.logType==2){ //Asynchronized RabbitMQ
				//mqChannel.basicPublish("", FrameworkSetting.mqTsdbQueue, null, s.toString().getBytes());
				mqChannel.basicPublish("", FrameworkSetting.log2mqQueue, null, s.toString().getBytes("UTF-8"));
			} else { //Synchronized
				InfluxUtil.write(FrameworkSetting.log2tsdbUrl, FrameworkSetting.log2tsdbDbName, s.toString());
			}
			errorCount = 0;
		}catch (Exception e) {
			errorCount++;
			if(errorCount % 100 == 1){//TODO 100 defada bir birseyler yap
				
			}
		}
	}
	
	/*
	public static String queryLog(String measName, String projectId, int start, int limit){
		if(!FrameworkSetting.log2tsdb)return "";
		try {
			String influxQL = "select * from "+measName+" where project_uuid='"+projectId+"'";
			if(start>0)influxQL+=" offset " + start;
			if(limit>0)influxQL+=" limit " + limit;			
			String sq = iquery( FrameworkSetting.log2tsdbUrl,FrameworkSetting.log2tsdbDbName,influxQL);
			errorCount = 0;
			return sq;
		}catch (Exception e) {
			errorCount++;
			if(errorCount % 100 == 1){//TODO 100 defada bir birseyler yap
				
			}
			return "";
		}
	}
	*/
	

	
	public static void logCrud(String str){
		if(str==null)return;
		StringBuilder s=new StringBuilder(str);
		s.append(" ").append(Instant.now().toEpochMilli()).append("000000");
		try {
			if(FrameworkSetting.logType==2){ //Asynchronized RabbitMQ
				//mqChannel.basicPublish("", FrameworkSetting.mqTsdbQueue, null, s.toString().getBytes());
				mqChannel.basicPublish("", FrameworkSetting.log2mqQueue, null, s.toString().getBytes("UTF-8"));
			} else { //Synchronized
				Map m = new HashMap();
				m.put("Content-Type", "application/json");
				HttpUtil.send(FrameworkSetting.log2tsdbUrl+"/write?db="+FrameworkSetting.log2tsdbDbName4Crud,s.toString(),"POST", m);
			}
			errorCount = 0;
		}catch (Exception e) {
			errorCount++;
			if(errorCount % 100 == 1){//TODO 100 defada bir birseyler yap
				
			}
		}
	}

	public static void logVcs(String str) {
		if(str==null)return;
		StringBuilder s=new StringBuilder(str);
		s.append(" ").append(Instant.now().toEpochMilli()).append("000000");
		try {
			if(FrameworkSetting.logType==2){ //Asynchronized RabbitMQ
				//mqChannel.basicPublish("", FrameworkSetting.mqTsdbQueue, null, s.toString().getBytes());
				mqChannel.basicPublish("", FrameworkSetting.log2mqQueue, null, s.toString().getBytes("UTF-8"));
			} else { //Synchronized
//				Map m = new HashMap();
//				m.put("Content-Type", "application/json");
//				HttpUtil.send(FrameworkSetting.log2tsdbUrl+"/write?db="+FrameworkSetting.log2tsdbDbName4Vcs,s.toString(),"POST", m);
				InfluxUtil.write(FrameworkSetting.log2tsdbUrl, FrameworkSetting.log2tsdbDbName4Vcs, s.toString());

			}
			errorCount = 0;
		}catch (Exception e) {
			errorCount++;
			if(errorCount % 100 == 1){//TODO 100 defada bir birseyler yap
				
			}
		}	
	}

	public static boolean activateInflux4Log() {
		try {
			Map m = new HashMap();m.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
//			String logStr = HttpUtil.send(FrameworkSetting.log2tsdbUrl+"/query","q=CREATE+DATABASE+"+FrameworkSetting.log2tsdbDbName,"POST", m);
			String logStr = "success"; 
			InfluxUtil.query(FrameworkSetting.log2tsdbUrl, null, "CREATE DATABASE "+FrameworkSetting.log2tsdbDbName);
			System.out.println("InfluxDB CREATE DATABASE " + FrameworkSetting.log2tsdbDbName + " result: " + logStr);
//			logStr = HttpUtil.send(FrameworkSetting.log2tsdbUrl+"/query","q=CREATE DATABASE "+FrameworkSetting.log2tsdbDbName4Crud,"POST", m);
			InfluxUtil.query(FrameworkSetting.log2tsdbUrl, null, "CREATE DATABASE "+FrameworkSetting.log2tsdbDbName4Crud);
			System.out.println("InfluxDB CREATE DATABASE " + FrameworkSetting.log2tsdbDbName4Crud + " result: " + logStr);
//			logStr = HttpUtil.send(FrameworkSetting.log2tsdbUrl+"/query","q=CREATE DATABASE "+FrameworkSetting.log2tsdbDbName4Vcs,"POST", m);
			InfluxUtil.query(FrameworkSetting.log2tsdbUrl, null, "CREATE DATABASE "+FrameworkSetting.log2tsdbDbName4Vcs);
			System.out.println("InfluxDB CREATE DATABASE " + FrameworkSetting.log2tsdbDbName4Vcs + " result: " + logStr);
			return true;

		}catch (Exception e) {
			return true;
		}		
	}
}