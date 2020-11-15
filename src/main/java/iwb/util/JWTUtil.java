package iwb.util;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONObject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JWTUtil {
	
	public static final String SECRET_KEY = "PromisJWT";
    public static final long EXPIRATION_TIME = TimeUnit.DAYS.toMillis(1); // 1 gün
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    public static String createJWT(String id, String sessionStr) {
    	  
        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        //Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder().setId(id)
                .setIssuedAt(now)
                //.setIssuer(issuer)
                .setSubject(sessionStr)
                .signWith(signatureAlgorithm, signingKey);
      
        //if it has been specified, let's add the expiration
        if (EXPIRATION_TIME > 0) {
            long expMillis = nowMillis + EXPIRATION_TIME;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }  
      
        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }
    
    public static Map<String, Object> decodeJWT(String jwt) {
        //This line will throw an exception if it is not a signed JWS (as expected)
        Claims claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
                .parseClaimsJws(jwt).getBody();
        //return claims;
        @SuppressWarnings("unchecked")
		Map<String, Object> scd=GenericUtil.fromJSONObjectToMap(new JSONObject(claims.getSubject()));
        return scd;
    }
}
