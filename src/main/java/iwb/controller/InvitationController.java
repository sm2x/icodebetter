package iwb.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import iwb.cache.FrameworkCache;
import iwb.model.db.W5Project;
import iwb.service.FrameworkService;
import iwb.util.GenericUtil;

@Controller
@RequestMapping("/invitation")
public class InvitationController {
	
	@Autowired
	private FrameworkService engine;
	
	@RequestMapping("/accept")
	public void hndAccept(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		String code = request.getParameter("invitation_code");
		if(!GenericUtil.isEmpty(code)) {
			String projectUuid = code.substring(0,36);
			W5Project po = FrameworkCache.getProject(projectUuid);
			if(po!=null) {
				String email= request.getParameter("email");
				Map scd = engine.userExists(email);
				if(scd == null){
					//+"&.r="+System.currentTimeMillis()
					response.sendRedirect("/index.html?email_projectid="+ projectUuid + ""+ email);
				} else {
					int userId = GenericUtil.uInt(scd.get("user_id"));
					engine.addToProject(userId, projectUuid, email);
				}
			}
		}

		//response.getWriter().write("{\"success\"");
		//response.getWriter().close();		
	}

}
