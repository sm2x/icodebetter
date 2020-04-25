package iwb.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import iwb.cache.FrameworkSetting;
import iwb.exception.IWBException;

public class HttpUtil {
	public static String send(String targetURL, String urlParameters) {
		return send(targetURL, urlParameters, "POST", null);
		
	}
	public static String send(String targetURL, String urlParameters, String method, Map<String, String> reqPropMap) {
		HttpURLConnection connection = null;
		try {
			URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(method);
			if(GenericUtil.isEmpty(reqPropMap)){
				connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
				connection.setRequestProperty("Content-Language", "en-EN");
			} else for(String key:reqPropMap.keySet()){
				connection.setRequestProperty(key,reqPropMap.get(key));
			}
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Send request
			if(!GenericUtil.isEmpty(urlParameters) || method.equals("POST")){
				DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
				wr.write((GenericUtil.isEmpty(urlParameters) ? "":urlParameters).getBytes("UTF-8"));
				wr.flush();
				wr.close();
			}

			// Get Response
			boolean success = connection.getResponseCode()>=200 && connection.getResponseCode()<300;
			InputStream is = success ? connection.getInputStream() : connection.getErrorStream();

			StringBuilder response = new StringBuilder();
			if(is!=null) {
				BufferedReader rd = new BufferedReader(new InputStreamReader(is,"UTF-8"));
				String line;
				
				while ((line = rd.readLine()) != null) {
					response.append(line);
					response.append('\r');
				}
				rd.close();
			} else {
				response.append("ERROR CODE: ").append(connection.getResponseCode());
			}
			if(!success) {
				if(response.length()>0) {
					if(response.charAt(0)=='{') 
						response = response.insert(1, "\"_code_\":"+connection.getResponseCode()+",");
				} else response.append("{\"_code_\":").append(connection.getResponseCode()).append("}");
			}
			return response.toString();


		} catch (Exception e) {
			if(FrameworkSetting.debug)e.printStackTrace();
			 throw new IWBException(
			          "framework",
			          "HTTPUtil.send",
			          0,
			          null,
			          targetURL + (urlParameters!=null ? "?"+urlParameters:""),
			          e);
			
//			throw ne;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	public static byte[] send4bin(String targetURL, String urlParameters, String method, Map<String, String> reqPropMap) {
		HttpURLConnection connection = null;
		try {
			URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(method);
			if(GenericUtil.isEmpty(reqPropMap)){
				connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
				connection.setRequestProperty("Content-Language", "en-EN");
			} else for(String key:reqPropMap.keySet()){
				connection.setRequestProperty(key,reqPropMap.get(key));
			}
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Send request
			if(!GenericUtil.isEmpty(urlParameters)){
				DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
				wr.write(urlParameters.getBytes("UTF-8"));
				wr.flush();
				wr.close();
			}

			// Get Response
			InputStream is = connection.getResponseCode()>=200 && connection.getResponseCode()<300 ? connection.getInputStream() : connection.getErrorStream();
			
			
			int maxLen = 32*1024*1024; //32mb
			byte[] buffer = new byte[maxLen];
		    int bytesRead, offset = 0;
		    while ((bytesRead = is.read(buffer, offset, 1024)) != -1 && offset < maxLen-1024) {
		        offset += bytesRead;
		    }
		    byte[] result = new byte[offset];
			for(int qi=0;qi<offset;qi++)result[qi] = buffer[qi]; 
		    return result;

		} catch (Exception e) {
			if(FrameworkSetting.debug)e.printStackTrace();
			 throw new IWBException(
			          "framework",
			          "HTTPUtil.send",
			          0,
			          null,
			          targetURL + (urlParameters!=null ? "?"+urlParameters:""),
			          e);
			
//			throw ne;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	public static JSONObject getJson(HttpServletRequest request) throws IOException, JSONException{
		StringBuilder jb = new StringBuilder();
		String line = null;
		BufferedReader reader = request.getReader();
		while ((line = reader.readLine()) != null)jb.append(line);
		return jb!=null && jb.length()>2 ? new JSONObject(jb.toString()):null;
	}
	
	public static String sendJson(String targetURL, JSONObject json){
		HttpURLConnection conn = null;
		try {
	        URL url = new URL(targetURL);
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setConnectTimeout(5000);
	        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	        conn.setDoOutput(true);
	        conn.setDoInput(true);
	        conn.setRequestMethod("POST");
	
	        OutputStream os = conn.getOutputStream();
	        os.write(json.toString().getBytes("UTF-8"));
	        os.close();
	
	        // read the response
	        InputStream in = new BufferedInputStream(conn.getInputStream());
	        String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
	
	        in.close();
	
	        return result;
		} catch (Exception e) {
			if(FrameworkSetting.debug)e.printStackTrace();
			return null;
		}finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
}
