package iwb.model.helper;

import org.springframework.web.context.request.async.DeferredResult;




public class W5DeferredResult extends DeferredResult<String> {
	private	String	projectId;
	private	int	userId;
	private	String	webPageId;
	

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public W5DeferredResult(String projectId, int userId, String webPageId, java.lang.Long l, java.lang.Object o) {
		super(l,o);
		this.projectId = projectId;
		this.userId = userId;
		this.webPageId = webPageId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getWebPageId() {
		return webPageId;
	}

	public void setWebPageId(String webPageId) {
		this.webPageId = webPageId;
	}

}
