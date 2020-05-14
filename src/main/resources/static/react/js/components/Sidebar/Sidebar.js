var routes={'/': 'Home'};
function rMenu(m, p){
	if(m.length) {
		var n=[];
		for(var qi=0;qi<m.length;qi++){
			var r=rMenu(m[qi],p)
			if(r){
//				routes[p+'/'+m[qi].id]=m[qi].value;
				n.push(r);
			}
		}
		return n;
	} else if(!m.href){ //parent
		var n=[];
		if(m.data){
			for(var qi=0;qi<m.data.length;qi++){
				var r=rMenu(m.data[qi],p+'/'+m.id)
				if(r)n.push(r);
			}
		}
		if(!n.length)return false;
		routes[p+'/'+m.id]=m.value;
		return {
            name: m.value,
            url: p+'/'+m.id,
            icon: m.icon && m.icon.substr(0,1)!='.' ? ('icon-'+m.icon) : 'icon-puzzle',children:n
          }
	} else  { // node
		var qi=m.href.indexOf('_tid=');
		if(qi>-1){
			qi=m.href.substr(qi+'_tid='.length);
			routes[p+'/showPage'+qi]=m.value;
			return {
	            name: m.value,
	            url: p+'/showPage'+qi,
	            icon: m.icon && m.icon.substr(0,1)!='.'  ? ('icon-'+m.icon) : 'icon-heart',
	            visited: false
	          }
		}
	
	}
	return false;
}
iwb.nav={items:rMenu(qry_user_menu4webix,'')};

function findNode(id, currentNode) {
    if (currentNode.url && id == currentNode.url) {
        return currentNode;
    } else if(currentNode.children){
        for (var qi = 0; qi < currentNode.children.length; qi++) {
            var result = findNode(id, currentNode.children[qi]);
            if (result !== false) {
                return result;
            } 
        } 
    } 
    return false;
}
iwb.nav.findNode = findNode;
function findNodes(name, currentNode) {
	var r=[]
    if (currentNode.url && currentNode.name && currentNode.name.toLowerCase().indexOf(name)>-1) {
        r.push(currentNode);
    }
	if(currentNode.children){
        for (var qi = 0; qi < currentNode.children.length; qi++) {
            var result = findNodes(name, currentNode.children[qi]);
            if (result !== false) for(var zi=0;zi<result.length;zi++){
                r.push(result[zi]);
            } 
        } 
    } 
    return r.length==0 ? false:r;
}
iwb.nav.findNodes = findNodes;

iwb.nav.visitedItems={};


class Sidebar extends React.Component {
	constructor(props){
		super(props);
	    this.visitItem = this.visitItem.bind(this);
		iwb.nav.visitItem=this.visitItem;
		this.state=iwb.nav;
		
	}
  handleClick(e) {
    e.preventDefault();
    e.target.parentElement.classList.toggle('open');
  }
  
  visitItem(path){
	  // console.log('visitItem',path);
	  var items = this.state.items;
	  var r = iwb.nav.findNode(path, {children:items});
	  if(r){
		  if(!r.visited){
			  r.visited = true;
			  r.visitCnt = 1;
			  this.setState({items:this.state.items});
			  iwb.nav.visitedItems[path]=r;
		  } else
			  r.visitCnt++;

	  }
  }
  
  activeRoute(routeName, props) {
    // return this.props.location.pathname.indexOf(routeName) > -1 ? 'nav-item nav-dropdown open' : 'nav-item nav-dropdown';
    return props.location.pathname.indexOf(routeName) > -1 ? 'nav-item nav-dropdown open' : 'nav-item nav-dropdown';

  }

  render() {
    const props = this.props;
    const activeRoute = this.activeRoute;
    const handleClick = this.handleClick;

    // badge addon to NavItem
    const badge = (badge) => {
      if (badge) {
        const classes = classNames( badge.class );
        return _(Badge,{ className: classes, color: badge.variant },badge.text);
      }
    };

    const visited = (visited) => {
        if (visited) {
          return _("i",{className: "float-right menu-visited"});
        }
      };
      
    // simple wrapper for nav-title item
    const wrapper = item => { return (!item.wrapper ? item.name : (_(item.wrapper.element, item.wrapper.attributes, item.name))) };

    // nav list section title
    const title =  (title, key) => {
      const classes = classNames( "nav-title", title.class);
      return _("li",{ key: key, className: classes },wrapper(title)," ");
    };

    // nav list divider
    const divider = (divider, key) => _("li", { key: key, className: "divider" });

    // nav item with nav link
    const navItem = (item, key) => {
      const classes = classNames( "nav-link", item.class);
      return _(NavItem,{ key: key },
    		  _(NavLink,{ to: item.url, className: classes, activeClassName: "active" },
    				  visited(item.visited),_("i", { className: item.icon }),item.name)
//				    _("i", { className: item.icon, style:{color: item.visited?'red':''} }),item.name)
    				);

    };

    // nav dropdown
    const navDropdown = (item, key) => {
      return _("li",{ key: key, className: activeRoute(item.url, props) },
    		  _("a",{ className: "nav-link nav-dropdown-toggle", href: "#", onClick: handleClick.bind(this) },
    				    _("i", { className: item.icon })," ",item.name),
    				  _("ul",{ className: "nav-dropdown-items" },navList(item.children))
    				)
    };

    // nav link
    const navLink = (item, idx) =>
      item.title ? title(item, idx) :
      item.divider ? divider(item, idx) :
      item.children ? navDropdown(item, idx)
                    : navItem(item, idx) ;

    // nav list
    const navList = (items) => {
    	var r=items.map( (item, index) => navLink(item, index) )
//    	r.splice(0,0,_('div',{className:'bg-pic'}));
        return r;
    };

    // sidebar-nav root
    return _( "div",{ className: "sidebar" },
    		  _("nav",{ className: "sidebar-nav" },_(Nav, null/*,_('div',{className:'bg-pic'})*/, navList(this.state.items)))
    );
  }
}

/*export default Sidebar;*/