package iwb.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import iwb.cache.FrameworkCache;

public class EncryptionUtil {
	private static SecretKeySpec secretKey4AES = null;
	private static SecretKey secretKey4DES = null;
    private static byte[] key;
 
    public static void setKey4AES(String myKey) 
    {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); 
            secretKey4AES = new SecretKeySpec(key, "AES");
        } 
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } 
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    
    public static void setKey4DES(String myKey) 
    {
        try {
        	DESKeySpec dks = new DESKeySpec(myKey.getBytes("UTF-8"));
    		SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
    		secretKey4DES = skf.generateSecret(dks);
        } 
        catch (Throwable e) {
			e.printStackTrace();
        } 
    }
    
 
    public static String encryptAES(String strToEncrypt) 
    {
        try
        {
            if(secretKey4AES==null)setKey4AES(FrameworkCache.getAppSettingStringValue(0, "aes_secret_key", "code2rox"));
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey4AES);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } 
        catch (Exception e) 
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }
 
   
    public static String decryptAES(String strToDecrypt) 
    {
        try
        {
        	if(secretKey4AES==null)setKey4AES(FrameworkCache.getAppSettingStringValue(0, "aes_secret_key", "code2rox"));
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey4AES);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } 
        catch (Exception e) 
        {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
    
    

    
    public static String encryptDES(String strToEncrypt) 
    {
        try
        {
            if(secretKey4DES==null)setKey4DES(FrameworkCache.getAppSettingStringValue(0, "des_secret_key", "code2rox"));
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey4DES);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } 
        catch (Exception e) 
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }
    

    public static String decryptDES(String strToDecrypt) 
    {
        try
        {
        	if(secretKey4DES==null)setKey4DES(FrameworkCache.getAppSettingStringValue(0, "des_secret_key", "code2rox"));
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey4DES);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } 
        catch (Exception e) 
        {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
    
    public static String encrypt(String strToEncrypt, int encryptionType) {
    	switch(encryptionType) {
    	case 1:return encryptAES(strToEncrypt);
    	case 2:return encryptDES(strToEncrypt);
    	default: return encryptAES(strToEncrypt);
    	}    
    }
    public static String decrypt(String strToDecrypt, int encryptionType) {
    	switch(encryptionType) {
    	case 1:return decryptAES(strToDecrypt);
    	case 2:return decryptDES(strToDecrypt);
    	default: return decryptAES(strToDecrypt);
    	}    	
    }
}
