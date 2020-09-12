package iwb.model.db;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;


@Entity
@Immutable
@Table(name="w5_menu", schema="iwb")
public class W5Menu implements java.io.Serializable {
/*TABLE_ID: 65*/
	
	private static final long serialVersionUID = 33331924324231L;
	private int menuId;	
	private int parentMenuId;	
	private int roleGroupId;//each project may have different types of role groups
	private int nodeType;
	
	private String localeMsgKey;
	private int tabOrder;
	private String imgIcon;
	private String url;
	
	private short accessViewTip;
	private String accessViewRoles;
	private String accessViewUsers;

	private List<W5Menu> _children;
	
	
	private String projectUuid;
	
	@Id
	@Column(name="menu_id")
	public int getMenuId() {
		return menuId;
	}

	public void setMenuId(int menuId) {
		this.menuId = menuId;
	}

	@Column(name="parent_menu_id")
	public int getParentMenuId() {
		return parentMenuId;
	}

	public void setParentMenuId(int parentMenuId) {
		this.parentMenuId = parentMenuId;
	}

	@Column(name="user_tip")
	public int getRoleGroupId() {
		return roleGroupId;
	}

	public void setRoleGroupId(int roleGroupId) {
		this.roleGroupId = roleGroupId;
	}

	@Column(name="node_tip")
	public int getNodeType() {
		return nodeType;
	}

	public void setNodeType(int nodeType) {
		this.nodeType = nodeType;
	}

	@Column(name="locale_msg_key")
	public String getLocaleMsgKey() {
		return localeMsgKey;
	}

	public void setLocaleMsgKey(String localeMsgKey) {
		this.localeMsgKey = localeMsgKey;
	}

	@Column(name="tab_order")
	public int getTabOrder() {
		return tabOrder;
	}

	public void setTabOrder(int tabOrder) {
		this.tabOrder = tabOrder;
	}

	@Column(name="img_icon")
	public String getImgIcon() {
		return imgIcon;
	}

	public void setImgIcon(String imgIcon) {
		this.imgIcon = imgIcon;
	}

	@Column(name="url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name="access_view_tip")
	public short getAccessViewTip() {
		return accessViewTip;
	}

	public void setAccessViewTip(short accessViewTip) {
		this.accessViewTip = accessViewTip;
	}

	@Column(name="access_view_roles")
	public String getAccessViewRoles() {
		return accessViewRoles;
	}

	public void setAccessViewRoles(String accessViewRoles) {
		this.accessViewRoles = accessViewRoles;
	}

	@Column(name="access_view_users")
	public String getAccessViewUsers() {
		return accessViewUsers;
	}

	public void setAccessViewUsers(String accessViewUsers) {
		this.accessViewUsers = accessViewUsers;
	}

	@Id
	@Column(name="project_uuid")
	public String getProjectUuid() {
		return projectUuid;
	}

	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}

	public boolean equals(Object o) {
		if(o==null || !(o instanceof W5Menu))return false;
		W5Menu c = (W5Menu)o;
		return c!=null && c.getMenuId()==getMenuId() && c.getProjectUuid().equals(projectUuid);
	}
	
	public int hashCode() {
		return projectUuid.hashCode() + 100*getMenuId();
	}

	public boolean safeEquals(W5Base q) {
		return false;
	}

	@Transient
	public List<W5Menu> get_children() {
		return _children;
	}

	public void set_children(List<W5Menu> _children) {
		this._children = _children;
	}
	

}
