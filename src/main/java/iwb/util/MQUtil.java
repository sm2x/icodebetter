package iwb.util;

import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.model.db.W5Mq;
import iwb.mq.MQTTCallback;
import iwb.service.FrameworkService;


public class MQUtil {
	private static Map<String, Connection> connMap = new HashMap<String, Connection>();
	private static Map<String, Channel> channelMap = new HashMap<String, Channel>();
	private static Map<String, DeclareOk> queueMap = new HashMap<String, DeclareOk>();
	
	public static Connection getConnection(String host){
		Connection conn = connMap.get(host);
		if(conn!=null){
			if(conn.isOpen())return conn;
			connMap.remove(host);
			try {conn.close();} 
			catch (Exception e) {}
		}
		
	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost(host);
	    try {
			conn = factory.newConnection();
		} catch (Exception e) {
			if(FrameworkSetting.debug)e.printStackTrace();
			return null;
		}
	    connMap.put(host, conn);
		return conn;
	}
	
	public static void close(String host, String queueName){
		if(queueName!=null) {
			String key = host + queueName;
			Channel channel = channelMap.get(key);
			if(channel!=null){
				if(channel.isOpen())try {channel.close();} 
				catch (Exception e) {}
				connMap.remove(key);
			}
				
		}
		
		Connection conn = connMap.get(host);
		if(conn!=null){
			if(conn.isOpen())try {conn.close();} 
			catch (Exception e) {}
			connMap.remove(host);


		}

	}
	
	
	public static Channel getChannel4Queue(String host, String queueName){
		String key = host + queueName;
		Channel channel = channelMap.get(key);
		if(channel!=null){
			if(channel.isOpen())return channel;
			connMap.remove(key);
			try {channel.close();} 
			catch (Exception e) {}
		}
		Connection conn = getConnection(host);
		if(conn!=null)try{
			channel = conn.createChannel();
			DeclareOk dok = channel.queueDeclare(queueName, false, false, false, null);
			channelMap.put(key, channel);
			queueMap.put(key, dok);
		    return channel;
		}catch (Exception e) {
			if(FrameworkSetting.debug)e.printStackTrace();
		}
		return null;
	}

	public static int getQueueMsgCount(String host, String queueName){
		String key = host + queueName;
		DeclareOk dok = queueMap.get(key);
		if(dok!=null){
			return dok.getMessageCount();
		}
		return 0;
	}

	public static void activateMQs(FrameworkService service, String projectId, boolean deactivate) {
		// TODO Auto-generated method stub
		
		if(projectId==null)for(String pid:FrameworkCache.wMqs.keySet()) {
			Map<Integer, W5Mq> mqMap = FrameworkCache.wMqs.get(pid); 
			if(!GenericUtil.isEmpty(mqMap)) if(deactivate) {
				MQTTCallback.deactivate(pid);
			} else for(W5Mq mq:mqMap.values())switch(mq.getLkpMqType()){
			case	1://mqtt
				MQTTCallback m = new MQTTCallback(mq, service);
				break;
				
			case	2://amq
				break;
				
			}
			
		} else {
			Map<Integer, W5Mq> mqMap = FrameworkCache.wMqs.get(projectId); 
			if(!GenericUtil.isEmpty(mqMap)) if(deactivate) {
				MQTTCallback.deactivate(projectId);
			} else  for(W5Mq mq:mqMap.values())switch(mq.getLkpMqType()){
			case	1://mqtt
				MQTTCallback m = new MQTTCallback(mq, service);
				break;
				
			case	2://amq
				break;
				
			}
		}
		
	}
	
}
