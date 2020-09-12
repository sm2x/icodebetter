package iwb.mq;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.exception.IWBException;
import iwb.model.db.Log5Mq;
import iwb.model.db.Log5Transaction;
import iwb.model.db.W5Mq;
import iwb.model.db.W5MqCallback;
import iwb.model.db.W5Project;
import iwb.service.FrameworkService;
import iwb.util.GenericUtil;
import iwb.util.LogUtil;

public class MQTTCallback implements MqttCallback {
	final public static Map<String, Map<Integer, MQTTCallback>> callbacks = new HashMap<String, Map<Integer, MQTTCallback>>();

	
	private W5Mq mq;
	private Map scd;
	private FrameworkService service;
	private String clientID;
	private	MqttClient mqClient;
	
	private int errorConnectionCount = 0;
	
	public MQTTCallback(W5Mq mq, FrameworkService service) {
		super();
		this.mq = mq;
		this.service = service;
		W5Project po = FrameworkCache.getProject(mq.getProjectUuid());
		scd = new HashMap();
		scd.put("projectId", mq.getProjectUuid()); scd.put("userId",999999); scd.put("roleId",999999); scd.put("userRoleId",999999); scd.put("userTip",999999);
		scd.put("locale", "en"); 
		scd.put("administratorFlag", 1); 
		scd.put("customizationId", po.getCustomizationId());
		
		clientID = "icb-" + mq.getProjectUuid() + "-" + UUID.randomUUID().toString();
		
		connect();
		
	}


	@Override
	public void connectionLost(Throwable t) {
		errorConnectionCount++;
		System.out.println("Connection lost!");
		// code to reconnect to the broker would go here if desired
		if(errorConnectionCount<10)try {
			mqClient.reconnect();
			System.out.println("Connection reconnected!");
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
//		System.out.println(topic);
		String msg = new String(message.getPayload());
		String transactionId =  GenericUtil.getTransactionId();
		Log5Mq lmq = new Log5Mq(mq.getProjectUuid(), mq.getMqId(), topic, msg, transactionId);
		if(!GenericUtil.isEmpty(mq.get_callbacks())) {
			for(W5MqCallback c:mq.get_callbacks()) try{
				Map requestMap = new HashMap();
				if(FrameworkSetting.logType>0)LogUtil.logObject(new Log5Transaction(mq.getProjectUuid(), "mqtt", transactionId), false);
				requestMap.put("_trid_", transactionId);
				requestMap.put("topic", topic);
				requestMap.put("message", msg);
				service.executeFunc(scd, c.getFuncId(), requestMap, (short) 7);
				transactionId =  GenericUtil.getTransactionId();
			} catch(Exception ee) {
				lmq.setError(ee.getMessage());
				if(ee instanceof IWBException) {
					IWBException ie = (IWBException)ee;
					System.out.println("MQ Error: " + topic + " : " + (GenericUtil.isEmpty(ie.getStack()) ? ie.getMessage() : ie.getStack().get(0).getMessage()));
					
				} else
					System.out.println("MQ Error: " + topic + " : " + ee.getMessage());
			}
		}
		LogUtil.logObject(lmq, true);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println("Pub complete" + token.getMessageId());
		
	}
	
	public MqttClient connect() {
		Map<Integer, MQTTCallback> mqMap = callbacks.get(mq.getProjectUuid()); 
		if(mqMap!=null) {
			try {
				MQTTCallback mqc = mqMap.get(mq.getMqId());
				if(mqc!=null) {
					mqMap.remove(mq.getMqId());
					mqc.disconnect();
				}
			}catch(Exception ee) {}
		} else {
			mqMap = new HashMap();
			callbacks.put(mq.getProjectUuid(), mqMap); 
		}

		MqttConnectOptions connOpt = new MqttConnectOptions();

		connOpt.setCleanSession(true);
		connOpt.setKeepAliveInterval(30);
		connOpt.setUserName(mq.getMqUsername());
		connOpt.setPassword(mq.getMqPassword().toCharArray());

		try {
			mqClient = new MqttClient(mq.getMqUrl(), clientID);
			mqClient.setCallback(this);
			mqClient.connect(connOpt);
			mqMap.put(mq.getMqId(), this);
			if(!GenericUtil.isEmpty(mq.get_callbacks())) for(int qi=0;qi<mq.get_callbacks().size();qi++){
				W5MqCallback c = mq.get_callbacks().get(qi);
				mqClient.subscribe(c.getTopic(), qi);
			}
			return mqClient;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean disconnect() {
		try {
			mqClient.disconnectForcibly();
			return true;
		} catch (MqttException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void send(String topic, String pubMsg) throws MqttPersistenceException, MqttException {
   		int pubQoS = 0;
		MqttMessage message = new MqttMessage(pubMsg.getBytes());
    	message.setQos(pubQoS);
    	message.setRetained(false);

    	// Publish the message
    	System.out.println("Publishing to topic \"" + topic + "\" qos " + pubQoS);
    	MqttDeliveryToken token = null;
   		mqClient.publish(topic, message);
	}
	
	public static void send(String projectId, int mqId, String topic, String pubMsg) throws MqttPersistenceException, MqttException {
		Map<Integer, MQTTCallback> mqcMap = callbacks.get(projectId);
		MQTTCallback mqc = null;
		if(mqcMap!=null) {
			mqc = mqcMap.get(mqId);
			if(mqc!=null)mqc.send(topic, pubMsg);
		}		
	}
	
	public static void deactivate(String projectId) {
		Map<Integer, MQTTCallback> mqcMap = callbacks.get(projectId);
		if(mqcMap!=null) for(MQTTCallback cb:mqcMap.values())cb.disconnect();

	}
}
