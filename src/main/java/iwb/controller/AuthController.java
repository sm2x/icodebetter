package iwb.controller;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.auth0.AuthenticationController;
import com.auth0.IdentityVerificationException;
import com.auth0.Tokens;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.service.FrameworkService;
import iwb.util.GenericUtil;
import iwb.util.HttpUtil;

@Controller
@RequestMapping("/auth")
public class AuthController {

  @Autowired private FrameworkService service;

  private AuthenticationController originController = null;
  private String userInfoAudience = null;

  private final String redirectOnFail = "../auth/login";
  private final String redirectOnSuccess = "/app/main.htm";

  Map<String, String> inviteMap = new ConcurrentHashMap<>();
  
  private void initialize() {
	  originController = AuthenticationController.newBuilder(FrameworkCache.getAppSettingStringValue(0, "auth0_domain"), 
    		  FrameworkCache.getAppSettingStringValue(0, "auth0_client_id"), 
			  FrameworkCache.getAppSettingStringValue(0, "auth0_client_secret")).build();
	  userInfoAudience = String.format("https://%s/userinfo", FrameworkCache.getAppSettingStringValue(0, "auth0_domain"));
	  
  }

  public Tokens handleRequest(HttpServletRequest request) throws IdentityVerificationException {
	  if(originController==null)initialize();
    return originController.handle(request);
  }

  public String buildAuthorizeUrl(HttpServletRequest request, String redirectUri) {
	  if(originController==null)initialize();
    return originController
        .buildAuthorizeUrl(request, redirectUri)
        .withAudience(userInfoAudience)
        .withScope("openid profile email")
        .build();
  }

  @CrossOrigin(origins = "*")
  @RequestMapping(value = "/callback", method = RequestMethod.GET)
  protected void getCallback(final HttpServletRequest req, final HttpServletResponse res)
      throws ServletException, IOException {
    handle(req, res);
  }

  @CrossOrigin(origins = "*")
  @RequestMapping(value = "/callback", method = RequestMethod.POST)
  protected void postCallback(final HttpServletRequest req, final HttpServletResponse res)
      throws ServletException, IOException {
    handle(req, res);
  }

  private void handle(HttpServletRequest req, HttpServletResponse res) throws IOException {
    try {
      Tokens tokens = handleRequest(req);
      String idToken = tokens.getIdToken();
      String issuer = "https://iwb.auth0.com/";

      Algorithm algorithm = Algorithm.HMAC256(FrameworkCache.getAppSettingStringValue(0, "auth0_client_secret"));
      JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuer).acceptLeeway(300).build();
      DecodedJWT jwt = verifier.verify(idToken);

      Map<String, Claim> claims = jwt.getClaims();
      Claim fullNameClaim = claims.get("name");
      Claim nickClaim = claims.get("nickname");
      Claim eClaim = claims.get("email");
      Claim subClaim = claims.get("sub");
      Claim picClaim = claims.get("picture");

      String subject = subClaim.asString();
      int index = subject.indexOf("|");
      String fullName = fullNameClaim.asString();
      String socialNet = subject.substring(0, index);
      String email = eClaim.asString();
      String pictureUrl = picClaim.asString();
      String nickname = nickClaim.asString();

      int socialCon = 0;
      if (socialNet.equals("linkedin")) {
        socialCon = 1;
      }
      if (socialNet.equals("facebook")) {
        socialCon = 2;
      }
      if (socialNet.equals("google-oauth2")) {
        socialCon = 3;
      }

      HttpSession session = req.getSession(true);
      String there = (String) session.getAttribute("hello");
      session.setAttribute("authToken", email);
      Map scd = service.generateScdFromAuth(socialCon, email);
      if (scd == null) {
        Map m = checkVcsTenant(socialCon, email, nickname, socialNet);
        int customizationId = GenericUtil.uInt(m.get("customizationId"));
        int userId = GenericUtil.uInt(m.get("userId"));

        List<Map> projectList = (List<Map>) m.get("projects");
        List<Map> userTips = (List<Map>) m.get("userTips");
        service.saveCredentials(
            customizationId,
            userId,
            pictureUrl,
            fullName,
            socialCon,
            email,
            nickname,
            projectList,
            userTips);

        if (!inviteMap.isEmpty()) {
          Iterator<Entry<String, String>> it = inviteMap.entrySet().iterator();
          while (it.hasNext()) {
            Entry<String, String> entry = (Entry<String, String>) it.next();
            String invitationEmail = entry.getValue();
            String inviteProjectId = entry.getKey();
            service.addToProject(userId, inviteProjectId, invitationEmail);
            it.remove();
          }
        }

      } else {
        int profilePictureId = GenericUtil.uInt(scd.get("ppictureId"));
        int cusId = GenericUtil.uInt(scd.get("customizationId"));
        int userId = GenericUtil.uInt(scd.get("userId"));

        if (profilePictureId < 3) {
          service.saveImage(pictureUrl, userId, cusId, (String) scd.get("projectId"));
        }
        session.setAttribute("iwb-scd", scd);

        if (!inviteMap.isEmpty()) {
          Iterator<Entry<String, String>> it = inviteMap.entrySet().iterator();
          while (it.hasNext()) {
            Entry<String, String> entry = (Entry<String, String>) it.next();
            String invitationEmail = entry.getValue();
            String inviteProjectId = entry.getKey();
            service.addToProject(userId, inviteProjectId, invitationEmail);
            it.remove();
          }
        }
      }
      res.sendRedirect(redirectOnSuccess);
    } catch (IdentityVerificationException e) {
      if (FrameworkSetting.debug) e.printStackTrace();
      res.sendRedirect(redirectOnFail);
    } catch (JWTVerificationException exception) {
      if (FrameworkSetting.debug) exception.printStackTrace();
      res.sendRedirect(redirectOnFail);
    }
  }

  private Map checkVcsTenant(int socialCon, String email, String nickname, String socialNet) {
    String vcsUrl = FrameworkCache.getAppSettingStringValue(0, "vcs_url");
    try {
      JSONObject params = new JSONObject();
      params.put("email", email);
      params.put("socialCon", socialCon);
      params.put("nickname", nickname);
      params.put("socialNet", socialNet);
      String s = HttpUtil.sendJson(vcsUrl + "serverVCSTenantCheck", params);
      if (!GenericUtil.isEmpty(s)) {
        JSONObject json;
        try {
          json = new JSONObject(s);
          if (json.get("success").toString().equals("true")) {
            Map map = GenericUtil.fromJSONObjectToMap(json);
            return map;
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    } catch (JSONException e) {
      throw new RuntimeException();
    }
    return null;
  }

  @RequestMapping("/login")
  protected void login(final HttpServletRequest req, HttpServletResponse res) throws IOException {
    if (FrameworkSetting.projectId != null && FrameworkSetting.projectId.length() == 36) {
      res.getWriter().write("/preview/" + FrameworkSetting.projectId + "/main.htm");
      res.getWriter().close();
      return;
    }
    String email = req.getParameter("email");
    String projectId = req.getParameter("projectId");
    if (email != null && projectId != null) {
      inviteMap.put(projectId, email);
    }
    
    /*String redirectUri =
        req.getScheme()
            + "://"
            + req.getServerName()
            + (req.getServerPort() != 80 ? ":" + req.getServerPort() : "")
            + "/auth/callback";*/
    
    String redirectUri = FrameworkSetting.argMap.get("redirect_uri");
    if(redirectUri == null){
      redirectUri =
        req.getScheme()
            + "://"
            + req.getServerName()
            + (req.getServerPort() != 80 ? ":" + req.getServerPort() : "")
            + "/auth/callback";
    }

    String authorizeUrl = buildAuthorizeUrl(req, redirectUri);
    res.getWriter().write(authorizeUrl);
    res.getWriter().close();
  }

  @RequestMapping(value = "/logout", method = RequestMethod.GET)
  protected String logout(final HttpServletRequest req) {
    invalidateSession(req);
    String returnTo = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
    String logoutUrl =
        String.format("https://%s/v2/logout?client_id=%s&returnTo=%s", FrameworkCache.getAppSettingStringValue(0, "auth0_domain"), 
        		FrameworkCache.getAppSettingStringValue(0, "auth0_client_id"), returnTo);
    return "redirect:" + logoutUrl;
  }

  private void invalidateSession(HttpServletRequest request) {
    if (request.getSession() != null) {
      request.getSession().invalidate();
    }
  }

  @RequestMapping("/redirectAuth")
  private void authRedirect(HttpServletRequest req, HttpServletResponse res) throws IOException {
    res.sendRedirect(redirectOnFail);
  }

  
}
