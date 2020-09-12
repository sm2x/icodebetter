package iwb.util;

import java.io.File;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.springframework.stereotype.Component;

import com.sun.mail.smtp.SMTPAddressFailedException;
import com.sun.mail.smtp.SMTPMessage;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.model.db.W5Email;
import iwb.model.db.W5FileAttachment;
import iwb.model.db.W5ObjectMailSetting;

@Component
public class MailUtil {
	public static InternetAddress[] getEmailAddress(String[] adresler){
		InternetAddress[] ia = null;		
		try {
			Vector<String> v = new Vector<String>();
			for(int i = 0; i<adresler.length; i++){
				adresler[i]=adresler[i].trim();
				if(isValidEmailAddress(adresler[i]) && !v.contains(adresler[i]))	{
					v.add(adresler[i]);
				}
			}			
			ia = new InternetAddress[v.size()];			
			for(int i=0; i<v.size() ; i++){
				ia[i] = new InternetAddress(v.get(i)); 
			}
		} catch (AddressException e) {
			if(FrameworkSetting.debug)e.printStackTrace();
		}
		
		return ia;  
	} 


	public static String sendMail(Map<String, Object> scd, W5Email email) {
		String txt;
		boolean mailDebug = FrameworkCache.getAppSettingIntValue(0, "mail_debug_flag")!=0;
		W5ObjectMailSetting oms = email.get_oms();
		email.setStatus((short)1);
		try {
						
			Properties properties = new Properties();			
			String hostingType = oms.getOutboxServerSslFlag() == 1 ? "smtps" : "smtp";
			
			properties.put("mail.mime.charset", "UTF-8"); 	
			properties.put("mail.transport.protocol", hostingType);
			properties.put("mail."+hostingType+".host", oms.getOutboxServer());
			properties.put("mail."+hostingType+".auth", oms.getOutboxAuthTip()==0 ? "false" :"true");
			properties.put("mail."+hostingType+".port", oms.getOutboxServerPort());	
            properties.put("mail."+hostingType+".sendpartial", "true");

			Session session = Session.getInstance(properties);
			Transport transport = session.getTransport();
			Multipart multipart = new MimeMultipart();
			//txt = (PromisUtil.uInt(email.getMailKeepBodyOriginal()) != 0) ? email.getMailBody() : email.getMailBody().replaceAll("\n", "<br>");
			//txt = (PromisUtil.uInt(email.getMailKeepBodyOriginal()) != 0) ? email.getMailBody() : email.getMailBody();
			txt = new HtmlFilter().filter(email.getMailBody());
			SMTPMessage  message = new SMTPMessage(session);
			message.setHeader("X-Mailer", MimeUtility.encodeText("Code2 LCP Mailer, version 0.3b"));
			message.setSentDate(Calendar.getInstance().getTime());
			if(oms.getOutboxRequestReadFlag()!=0){  
				message.setHeader("Disposition-Notification-To", MimeUtility.encodeText(oms.getEmailAddress().indexOf(">")>-1 ? oms.getEmailAddress():"\""+oms.getDsc()+"\" <"+oms.getEmailAddress()+">"));
			}
			
			InternetAddress[] addressTo = email.getMailTo() != null ? getEmailAddress(email.getMailTo().split(",")) : null;
			InternetAddress[] addressCC = email.getMailCc() != null ? getEmailAddress(email.getMailCc().split(",")) : null;
			InternetAddress[] addressBCC = email.getMailBcc() != null ? getEmailAddress(email.getMailBcc().split(",")) : null;	
			
			message.setFrom(new InternetAddress(oms.getEmailAddress()));
			//message.setReplyTo(addresses);
			
			if(addressTo != null){ message.setRecipients(Message.RecipientType.TO, addressTo); }
			if(addressCC != null){message.setRecipients(Message.RecipientType.CC, addressCC);}
			if(addressBCC != null){message.setRecipients(Message.RecipientType.BCC, addressBCC);}
			
			MimeBodyPart messagePart = new MimeBodyPart();
			
			message.setSubject(email.getMailSubject(), "utf-8");
			messagePart.setContent(txt, "text/html; charset=utf-8");
			
			multipart.addBodyPart(messagePart);
			
			// Attachmentlar ekleniyor
			
			if(email.get_fileAttachments() != null && email.get_fileAttachments().size()>0){	
				for (W5FileAttachment f1 : email.get_fileAttachments()){
					MimeBodyPart attachmentPart = new MimeBodyPart();
					
					FileDataSource fileDataSource = new FileDataSource(new File(FrameworkCache.getAppSettingStringValue(scd, "file_local_path")+File.separator+GenericUtil.uInt((Object)scd.get("customizationId"))+File.separator+"attachment"+File.separator+f1.getSystemFileName()));
			        attachmentPart.setDataHandler(new DataHandler(fileDataSource));
			        attachmentPart.setFileName(f1.getOrijinalFileName());
					multipart.addBodyPart(attachmentPart);
				}
			}
	
			message.setContent(multipart);
			if(oms.getOutboxRequestDeliveryFlag()!=0){
				message.setReturnOption(SMTPMessage.RETURN_HDRS);
				message.setNotifyOptions(SMTPMessage.NOTIFY_DELAY|SMTPMessage.NOTIFY_FAILURE|SMTPMessage.NOTIFY_SUCCESS);
			}	
			
			if(oms.getOutboxAuthTip()==0)transport.connect();	//userName PassWord gerektirmeyen durumlarda.*
			else transport.connect(oms.getOutboxServer(), oms.getOutboxServerPort(), oms.getOutboxServerUserName(), oms.getOutboxServerPassWord()/*GenericUtil.PRMDecrypt(oms.getOutboxServerPassWord())*/);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		}catch (SMTPAddressFailedException e) {//Yanlis adres ise hata verme
			if(FrameworkSetting.debug || mailDebug)e.printStackTrace();
			return null;
		}catch (MessagingException e) {
			if(FrameworkSetting.debug || mailDebug)e.printStackTrace();		
			if(e.getNextException() !=null && e.getNextException().getClass().getName().equals("com.sun.mail.smtp.SMTPAddressFailedException"))	return null;
			email.setStatus((short)0);//error
			if(e.getMessage()==null) return "unknown error";
			if(e.getMessage().toLowerCase().contains("authentication")) return LocaleMsgCache.get2(scd, "error.mail.authentication_failed");
			return e.getMessage();
			
		}catch (Exception e) {
			if(FrameworkSetting.debug || mailDebug)e.printStackTrace();
			email.setStatus((short)0);//error
			return e.getMessage()==null ? "unknown error" : e.getMessage();
		} finally {
			LogUtil.logObject(email, true);
		}
		return null;
	}
	
	public static boolean isValidEmailAddress(String emailAddress)
	{  
		if(emailAddress!=null &&emailAddress.indexOf("<")!=-1 &&emailAddress.indexOf(">")!=-1) emailAddress=emailAddress.substring(emailAddress.indexOf("<")+1,emailAddress.indexOf(">"));
		String expression= "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)" ;
		CharSequence inputStr = emailAddress;  
		Pattern pattern = Pattern.compile(expression,Pattern.CASE_INSENSITIVE);  
		Matcher matcher = pattern.matcher(inputStr);
		return matcher.matches();     
	}
	
	public static String organizeMailAdress(String emailAddress)
	{  
		if(emailAddress==null || emailAddress.length()<3)return "";
		String[] qx = emailAddress.split(",");
		String newEmailAddress="";
		for(String s:qx){
			if(isValidEmailAddress(s))newEmailAddress+=","+s.trim();
		}
		return newEmailAddress.length()>1 ? newEmailAddress.substring(1): "";     
	}
}
