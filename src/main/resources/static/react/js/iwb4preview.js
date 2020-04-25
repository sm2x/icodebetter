var iwb = {
  addCssString: (css = "", id = Math.random()) => {
    let style = document.createElement("style");
    style.type = "text/css";
    style.id = "iwb-tpl-" + id;
    style.styleSheet
      ? (style.styleSheet.cssText = css)
      : style.appendChild(document.createTextNode(css));
    document["head"].appendChild(style);
  },
  sidebarToggle: function(e) {
    e.preventDefault();
    document.body.classList.toggle("sidebar-hidden");
  },
  sidebarMinimize: function(e) {
    e.preventDefault();
    document.body.classList.toggle("sidebar-minimized");
    document.body.classList.toggle("brand-minimized");
  },
  mobileSidebarToggle: function(e) {
    e.preventDefault();
    document.body.classList.toggle("sidebar-mobile-show");
  },
  asideToggle: function(e) {
    e.preventDefault();
    if (iwb.asideToggleX) iwb.asideToggleX(e);
    else document.body.classList.toggle("aside-menu-hidden");
  },
  loadGrid: {},
  approvalColorMap: {
    1: "primary",
    2: "warning",
    3: "danger",
    5: "success",
    901: "secondary"
  },
  approvalLogs: arid => {
    return event => {
      event.preventDefault();
      iwb.ajax.query(1667, { xapproval_record_id: arid }, j => {
        if (j.data && j.data.length)
          iwb.showModal({
            title: "Approval Logs",
            footer: false,
            color: "primary",
            size: "lg",
            body: _(ListGroup, {
              style: { fontSize: "1.0rem" },
              children: j.data.map(item =>
                _(
                  ListGroupItem,
                  {},
                  _(
                    "span",
                    {
                      className:
                        "float-right badge badge-pill badge-" +
                        iwb.approvalColorMap[item.approval_action_tip]
                    },
                    item.approval_action_tip_qw_
                  ),
                  " ",
                  _("b", null, item.user_id_qw_),
                  " ",
                  item.step_dsc,
                  " - ",
                  _("i", { style: { color: "#aaa" } }, item.log_dttm)
                )
              )
            })
          });
        else alert("no data");
      });
    };
  }
};

function fmtDecimal(value, digit,precision) {
    if (!value) return '0';
    if (!digit) digit = 2;
    var result = Math.round(value * Math.pow(10, digit)) / Math.pow(10, digit);
    result = Number.parseFloat(result).toFixed(precision) + '';
    var s = 1 * result < 0 ? 1 : 0;
    var x = result.split('.');
    var x1 = x[0],
        x2 = x[1];
    for (var i = x1.length - 3; i > s; i -= 3) x1 = x1.substr(0, i) + ('.') + x1.substr(i);
    if (x2 && x2 > 0) return x1 + (',') + x2;
    return x1;
}

iwb.logo =
  '<svg width="32" height="22" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" viewBox="0 0 300 202.576" enable-background="new 0 0 300 202.576" class="white-logo standard-logo middle-content"><g id="svg_14"><path id="svg_15" d="m46.536,31.08c0,10.178 -8.251,18.429 -18.429,18.429c-10.179,0 -18.429,-8.251 -18.429,-18.429c0,-10.179 8.25,-18.43 18.429,-18.43c10.177,0 18.429,8.251 18.429,18.43" fill="darkorange"></path><path id="svg_16" d="m220.043,62.603c-0.859,0 -1.696,0.082 -2.542,0.128c-0.222,-0.007 -0.429,-0.065 -0.654,-0.065c-0.674,0 -1.314,0.128 -1.969,0.198c-0.032,0.003 -0.064,0.003 -0.096,0.005l0,0.005c-9.241,1.04 -16.451,8.79 -16.451,18.309c0,9.555 7.263,17.326 16.554,18.319c0,0.03 0,0.063 0,0.094c0.482,0.027 0.953,0.035 1.428,0.05c0.182,0.006 0.351,0.055 0.534,0.055c0.088,0 0.17,-0.025 0.258,-0.026c0.96,0.02 1.927,0.026 2.938,0.026c16.543,0 29.956,13.021 29.956,29.564c0,16.545 -13.412,29.956 -29.956,29.956c-15.521,0 -28.283,-11.804 -29.803,-26.924l0,-107.75l-0.054,0c-0.289,-9.926 -8.379,-17.896 -18.375,-17.896c-9.995,0 -18.086,7.971 -18.375,17.896l-0.053,0l0,118.529c0,10.175 11.796,52.85 66.661,52.85c36.815,0 66.661,-29.846 66.661,-66.662c-0.001,-36.816 -29.847,-66.661 -66.662,-66.661" fill="#20a8d8"></path><path id="svg_17" d="m153.381,143.076l-0.049,0c-0.805,8.967 -8.252,16.021 -17.428,16.021s-16.624,-7.054 -17.428,-16.021l-0.048,0l0,-66.298l-0.045,0c-0.245,-9.965 -8.36,-17.979 -18.384,-17.979s-18.139,8.014 -18.384,17.979l-0.045,0l0,66.298l-0.05,0c-0.805,8.967 -8.252,16.021 -17.428,16.021c-9.176,0 -16.624,-7.054 -17.429,-16.021l-0.048,0l0,-66.298l-0.045,0c-0.246,-9.965 -8.361,-17.978 -18.384,-17.978c-10.024,0 -18.139,8.014 -18.384,17.979l-0.046,0l0,66.298c0.836,29.321 24.811,52.849 54.335,52.849c13.79,0 26.33,-5.178 35.906,-13.636c9.577,8.458 22.116,13.636 35.906,13.636c14.604,0 27.85,-5.759 37.61,-15.128c-15.765,-13.32 -20.132,-31.532 -20.132,-37.722" fill="#bbb"></path></g></svg>';
iwb.debug = true;
iwb.graph = function(dg, gid, callback) {
	  var newStat = 1 * dg.funcTip ? dg.funcFields : "";
	  var params = {};
	  if (newStat) params._ffids = newStat;
	  if (1 * dg.graphTip >= 5) params._sfid = dg.stackedFieldId;
	  var series=[], labels=[], lookUps=[], chart =null;
	  var xid = gid;
	  iwb.request({
	    url:
	      (dg.query? "ajaxQueryData4Stat?_gid=":"ajaxQueryData4StatTree?_gid=") +
	      dg.gridId +
	      "&_stat=" +
	      dg.funcTip +
	      "&_qfid=" +
	      dg.groupBy +
	      "&_dtt=" +
	      dg.dtTip,
	    params: Object.assign(params, dg.queryParams),
	    successCallback: function(j) {
			var d= j.data;
			if(!d || !d.length)return;
			
			switch (1 * dg.graphTip) {
	        case 6: // stacked area
	        case 5: // stacked column
	        	var l= j.lookUp;
	        	for(var k in l)lookUps.push(k);
	            if(!lookUps.length)return;
	            d.map((z)=>{
	                var data=[];
	                lookUps.map((y)=>data.push(1*(z['xres_'+y]||0)));
	                series.push({name:z.dsc, data:data});
	            });
	            lookUps.map((y)=>labels.push(l[y]||'-'));

	            options = {
	                chart: {
	                	id:'apex-'+gid,
	                    height: 80*d.length+40,
	                    type: 'bar',
	                    stacked: true,
	                    toolbar: {show: false}
	                },
	                plotOptions: {
	                    bar: {horizontal: true},
	                    
	                },
	                series: series,
//title: {text: dg.name},
	                xaxis: {
	                    categories: labels,
	                },
	                yaxis: {
	                    title: {
	                        text: undefined
	                    },                
	                }
	            }
	        	break;
	        case 3:// pie
	            d.map((z)=>{
	                series.push(1*z.xres);
	                labels.push(z.dsc||'-');
	            });
	            var options = {
//title: {text: dg.name},
	                chart: {id:'apex-'+gid, type: 'donut', toolbar: {show: false}},
	                series: series, labels: labels, legend: dg.legend ? {position:'bottom'} : false
	                ,dataLabels: dg.legend ? {}:{formatter: function (val, opts) {return labels[opts.seriesIndex] + ' - ' + fmtDecimal(val);}}
	            }

	        	break;
	        case 1:// column
	        case 2:// area
	        	d.map((z)=>{
	                series.push(1*z.xres);
	                labels.push(z.dsc);
	            });

	            options = {
//title: {text: dg.name},
	                chart: {
	                	id:'apex-'+gid,
	                    height:50*d.length+30,
	                    type: 1 * dg.graphTip==1?'bar':'area',
	                    toolbar: {show: false}
	                },
	                plotOptions: {
	                    bar: {
	                        horizontal: 1 * dg.graphTip==1 // d.length>5
	                    }
	                },
	                dataLabels:  1 * dg.graphTip==1 ? {
	                    enabled: true,
	                    textAnchor: 'start',
	                    style: {
	                        colors: ['#fff']
	                    },
	                    formatter: function(val, opt) {
	                        return opt.w.globals.labels[opt.dataPointIndex] + ":  " + val
	                    },
	                    offsetX: 0,
	                    dropShadow: {
	                      enabled: true
	                    }
	                }:{},
	                series: [{
	                    data: series
	                }],
	                xaxis: {
	                    categories: labels,
	                },
	                yaxis: {labels: {show: false}},
	            }
	        	break;
			}

			if(options){
				if(iwb.charts[xid])iwb.charts[xid].destroy();
				if(callback)options.chart.events= {
						dataPointSelection: function(event, chartContext, config) {
							if(config.selectedDataPoints && config.selectedDataPoints && config.selectedDataPoints.length){
								var yx = config.selectedDataPoints[0];
								callback(yx.length ? d[yx[0]].id : false);
						    }
						}
				}
	            var chart = new ApexCharts(
	                document.getElementById(xid),
	                options
	            );
	            iwb.charts[xid]=chart;
	            chart.render();
			}
			
	    }
	  });
}
iwb.JSON2URI = function(j) {
  if (!j) return "";
  var s = "";
  for (key in j)
    s +=
      encodeURIComponent(key) +
      "=" +
      (j[key] === null || j[key] === false ? "" : encodeURIComponent(j[key])) +
      "&";
  return s;
};
iwb.gridActionColumn = !0;
var dgColors = ["warning", "secondary", "danger", "primary", "success", "info"];
iwb.onGlobalSearch = function(v) {
  var c = document.getElementById("id-global-search");
  var cc = c.classList.contains("global-search-active");
  if ((c.value && !cc) || (!c.value && cc))
    c.classList.toggle("global-search-active");
  if (iwb.onGlobalSearch2) iwb.onGlobalSearch2(v);
};
iwb.killGlobalSearch = function() {
  iwb.onGlobalSearch2 = false;
  var c = document.getElementById("id-global-search");
  c.value = "";
  c.classList.remove("global-search-active");
};

var _webPageId = "xrand";

const HashRouter = ReactRouterDOM.HashRouter;
const Route = ReactRouterDOM.Route;
const Switch = ReactRouterDOM.Switch;
const Link = ReactRouterDOM.Link;
const NavLink = ReactRouterDOM.NavLink;
const Redirect = ReactRouterDOM.Redirect;
const Container = Reactstrap.Container;

const Row = Reactstrap.Row;
const Col = Reactstrap.Col;
const Card = Reactstrap.Card;
const CardHeader = Reactstrap.CardHeader;
const CardFooter = Reactstrap.CardFooter;
const CardBlock = Reactstrap.CardBody;
const CardBody = Reactstrap.CardBody;
const Button = Reactstrap.Button;
const ButtonGroup = Reactstrap.ButtonGroup;
const Breadcrumb = Reactstrap.Breadcrumb;
const BreadcrumbItem = Reactstrap.BreadcrumbItem;
const Badge = Reactstrap.Badge;
const Label = Reactstrap.Label;
const Input = Reactstrap.Input;
const InputGroup = Reactstrap.InputGroup;
const InputGroupAddon = Reactstrap.InputGroupAddon;
const InputGroupButton = Reactstrap.InputGroupButton;
const ButtonDropdown = Reactstrap.ButtonDropdown;
const Dropdown = Reactstrap.Dropdown;
const DropdownToggle = Reactstrap.DropdownToggle;
const DropdownMenu = Reactstrap.DropdownMenu;
const DropdownItem = Reactstrap.DropdownItem;
const Form = Reactstrap.Form;
const FormGroup = Reactstrap.FormGroup;
const FormText = Reactstrap.FormText;

const Table = Reactstrap.Table;
const Pagination = Reactstrap.Pagination;
const PaginationItem = Reactstrap.PaginationItem;
const PaginationLink = Reactstrap.PaginationLink;

const Nav = Reactstrap.Nav;
const NavItem = Reactstrap.NavItem;
const NavLinkS = Reactstrap.NavLink;
const NavbarToggler = Reactstrap.NavbarToggler;
const NavbarBrand = Reactstrap.NavbarBrand;

const Modal = Reactstrap.Modal;
const ModalHeader = Reactstrap.ModalHeader;
const ModalBody = Reactstrap.ModalBody;
const ModalFooter = Reactstrap.ModalFooter;

const TabContent = Reactstrap.TabContent;
const TabPane = Reactstrap.TabPane;

const ListGroup = Reactstrap.ListGroup;
const ListGroupItem = Reactstrap.ListGroupItem;

const Select = window.Select;
const Popper = window.Popper;

const findDOMNode = ReactDOM.findDOMNode;
var _ = React.createElement;


/**
 * @description {text,callback} used for making popup dialog
 * @param {object}
 *            conf.text - body of the mesasge
 * @param {object}
 *            conf.title - title of the modal
 * @param {function}
 *            conf.callback - callback function
 * @return {boolean} - retur true or false to the call back
 * @example yesNoDialog({ text:"Are you Sure!", callback:(success)=>{ logic here
 *          }});
 */
yesNoDialog = ({
  text = "Are You Sure?",
  title = "Are You Sure?",
  callback = alert("obj.callback is not a function"),
  ...confg
}) => {
  iwb.showModal({
    body: text,
    size: "sm",
    title: title,
    color: "danger",
    footer: _(
      ModalFooter,
      null,
      _(
        Button,
        {
          className: "btn-form",
          color: "teal",
          onClick: () => {
            callback(true);
            iwb.closeModal();
          }
        },
        getLocMsg("js_tamam")
      ),
      " ",
      _(
        Button,
        {
          className: "btn-form",
          color: "light",
          style: { border: ".5px solid #e6e6e6" },
          onClick: () => {
            callback(false);
            iwb.closeModal();
          }
        },
        getLocMsg("js_cancel")
      )
    ),
    ...confg
  });
};
function disabledCheckBoxHtml(row, cell) {
  // TODO
  // return _('img',{border:0,src:'../images/custom/'+(f
  // ?'':'un')+'checked.gif'});
  return row[cell] && 1 * row[cell]
    ? _("i", {
        className: "fa fa-check",
        style: {
          color: "white",
          background: "#4dbd74",
          padding: 5,
          borderRadius: 25
        }
      })
    : null; // _('i',{className:'fa fa-check', style:{color: 'white',background:
  // 'red', padding: 5, borderRadius: 25}});
}
function gridUserRenderer(row, cell) {
  // TODO
  return row[cell + "_qw_"];
}

function gridQwRendererWithLink(t) {
  // tableId
  return function(row, cell) {
    return row[cell + "_qw_"];
  };
}
function editGridComboRenderer(cell, combo) {
  if (!combo || !combo.options)
    return function(row) {
      return "?x?: " + row[cell];
    };
  var moptions = {};
  combo.options.map(function(o) {
    moptions[o.id] = o;
  });
  combo.moptions = moptions;
  return function(row) {
    var c = row[cell];
    if (!c) return "";
    var o = combo.moptions[c];
    return o ? o.dsc : "???: " + row[cell];
  };
}

function editGridLovComboRenderer(cell, combo) {
  if (!combo || !combo.options)
    return function(row) {
      return "?x?: " + row[cell];
    };
  var moptions = {};
  combo.options.map(function(o) {
    moptions[o.id] = o;
  });
  combo.moptions = moptions;
  return function(row) {
    var c = row[cell];
    if (!c) return "";
    c = c.split(",");
    return c.map(function(o) {
      return combo.moptions[o];
    });
  };
}

function fileAttachmentHtml(row, cell) {
  // TODO
  return row[cell] && 1 * row[cell]
    ? _("i", { className: "icon-paper-clip" })
    : null;
}

function vcsHtml(row, cell) {
  return row[cell] && 1 * row[cell]
    ? _("i", { className: "icon-social-github" })
    : null;
}
function pictureHtml(row, cell) {
  return row[cell] && 1 * row[cell]
    ? _("i", { className: "icon-picture" })
    : null;
}

function mailBoxRenderer(row, cell) {
  return row[cell] && 1 * row[cell]
    ? _("i", { className: "icon-envelope" })
    : null;
}

function fmtDateTime(x) {
  return x ? moment(x).format("DD/MM/YYYY HH:ss") : "";
}

function fmtShortDate(x) {
  return x ? moment(x).format("DD/MM/YYYY") : "";
}


function strShortDate(row,cell) {
  return cell && row ? (row[cell] ? row[cell].substr(0, 10) : ""):(row ? row.substr(0, 10) : "");
}
function accessControlHtml() {
  return null;
}
function fmtDateTime(x,y) {
  return x ? moment(x).format("DD/MM/YYYY HH:mm") : "";
}
function fmtShortDate(x,y) {
  return x ? moment(x).format("DD/MM/YYYY") : "";
}
function strDateTime(row,cell) {
	return cell && row ? (row[cell] ? row[cell] : ""):(row ? row : "");
}


var daysOfTheWeek = {
  tr: [
    "Pazar",
    "Pazartesi",
    "Salı",
    "Çarşamba",
    "Perşembe",
    "Cuma",
    "Cumartesi"
  ],
  en: [
    "Sunday",
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday"
  ]
};
var xtimeMap = {
  tr: ["az önce", "bir dakika önce", "dakika önce", "saat önce", "dün"],
  en: ["seconds ago", "a minute ago", "minutes ago", "hours ago", "yesterday"]
};
function fmtDateTimeAgo(dt2) {
	  if (!dt2) return "";
	  var tnow = new Date().getTime();
	  var dt3 = moment(dt2,"DD/MM/YYYY HH:mm").toDate();
	  var t = dt3.getTime();
	  var xt = xtimeMap[_scd.locale] || {};
	  if (t + 30 * 1000 > tnow) return xt[0]; // 'Az Önce';//5 sn
	  if (t + 2 * 60 * 1000 > tnow) return xt[1]; // 'Bir Dakika Önce';//1 dka
	  if (t + 60 * 60 * 1000 > tnow)
	    return Math.round((tnow - t) / (60 * 1000)) + xt[2]; // ' Dakika Önce';
	  if (t + 24 * 60 * 60 * 1000 > tnow)
	    return Math.round((tnow - t) / (60 * 60 * 1000)) + xt[3]; // ' Saat Önce';
	  if (t + 2 * 24 * 60 * 60 * 1000 > tnow) return xt[4]; // 'Dün';
	  if (t + 7 * 24 * 60 * 60 * 1000 > tnow)
	    return daysOfTheWeek[_scd.locale][dt3.getDay()]; // 5dka
	  return dt2.substr(0,10);
}

function strDateTimeAgo(row, cell) {
	return cell && row ? (row[cell] ? fmtDateTimeAgo(row[cell]) : ""):(row ? fmtDateTimeAgo(row) : "");
}

function getStrapSize(w) {
  if (w >= 700) return "lg";
  if (w >= 400) return "md";
  return "sm";
}

iwb.emptyField = _(
  "i",
  { className: "raw-field-empty" },
  _("br"),
  " ",
  "(boş)"
);
iwb.getFieldRawValue = function(field, extraOptions) {
  if (!field || !field.value) return iwb.emptyField;
  if (field.$ === FileInput) {
    return field.fileName
      ? _(
          "a",
          {
            target: "_blank",
            style: { color: "#2196F3", fontWeight: "bold" },
            href: "dl/" + field.fileName + "?_fai=" + field.fileId,
            className: "form-control",
            disabled: true
          },
          field.fileName,
          " ",
          field.fileSize &&
            _(
              "i",
              { style: { color: "#888", fontWeight: "normal" } },
              "(" + iwb.fmtFileSize(field.fileSize) + ")"
            )
        )
      : iwb.emptyField;
  }
  // if (field.$ === MapInput) return _(field.$,{value:field.value,
  // disabled:true});
  var options = extraOptions || field.options;
  if (!options || !options.length) {
    var value = field.decimalScale
      ? Number(field.value).toFixed(field.decimalScale)
      : field.value;
    if (typeof value == "undefined" || value == "") return iwb.emptyField;
    return _("b", { className: "form-control" }, value);
  }
  var optionsMap = {};
  options.map(o => {
    optionsMap[o.id] = o.dsc;
  });
  if (field.multi) {
    var value = [],
      vs = field.value;
    if (!Array.isArray(vs)) vs = vs.split(",");
    vs.map(v => {
      value.push(optionsMap[v]);
    });
    if (!value.length) return iwb.emptyField;
    return _("b", { className: "form-control" }, value.join(", "));
  }
  var value = field.value;
  if (value.id) value = value.id;
  value = optionsMap[value];
  if (value == undefined || value == "") return iwb.emptyField;
  return _("b", { className: "form-control" }, value);
};

iwb.openForm = function(url) {
  alert(url);
  return false;
};

iwb.forms = {};
class XTabForm extends React.PureComponent {
  constructor(props) {
    if (iwb.debugConstructor) console.log("XTabForm.constructor", props);
    super(props);
    this.state = { viewMode: this.props.cfg.a == 1 };
    this.onSubmit = this.onSubmit.bind(this);
    this.toggleViewMode = this.toggleViewMode.bind(this);
    this.approvalAction = this.approvalAction.bind(this);
  }
  toggleViewMode() {
    this.setState({ viewMode: !this.state.viewMode });
  }
  onSubmit(e) {
    if (e && e.preventDefault) e.preventDefault();
    var selfie = this;
    if (this.form)
      this.form.submit({
        callback: function(json, cfg) {
          var url = "showForm";
          if (json.outs) {
            url += "?_renderer=react16&a=1&_fid=" + json.formId;
            for (var k in json.outs) url += "&t" + k + "=" + json.outs[k];
          } else {
            url += cfg.url.substring("ajaxPostForm".length);
          }
          // var url =
          // 'showForm?_renderer=react16&a=1&_fid='+props.crudFormId+pkz;

          toastr.success(
            "Click! To see saved item <a href=# onClick=\"return iwb.openForm('" +
              url +
              "')\"></a>",
            "Saved Successfully",
            { timeOut: 3000 }
          );
          var parent = selfie.props.parentCt;
          if (parent) {
            parent.closeTab();
            iwb.onGlobalSearch2("");
          }
        }
      });
    else alert("this.form not set");
    return false;
  }
  approvalAction(action) {
    return event => {
      event && event.preventDefault && event.preventDefault();
      let { formId, pk } = this.props.cfg;
      let pkz = "";
      for (let key in pk) {
        pkz += "&" + key + "=" + pk[key];
      }
      let url = "";
      switch (action) {
        case 901: // start approval
          url =
            "ajaxApproveRecord?_aa=901&_arid=" +
            this.props.cfg.approval.approvalRecordId;
          yesNoDialog({
            text: "Are you Sure to Start Approval?",
            callback: success =>
              success &&
              iwb.request({
                url,
                params: { _adsc: "start approval" },
                successCallback: () =>
                  this.props.parentCt.closeTab(event, success)
              })
          });
          break;
        default:
          var p = prompt(
            "Please enter comment",
            ["", "Approve", "Return", "Reject"][action]
          );
          if (p) {
            url =
              "ajaxApproveRecord?_aa=" +
              action +
              "&_arid=" +
              this.props.cfg.approval.approvalRecordId;
            iwb.request({
              url,
              params: { _adsc: p, _avno: this.props.cfg.approval.versionNo },
              successCallback: () => this.props.parentCt.closeTab(event, true)
            });
          }
          break;
      }
    };
  }
  render() {
    if (iwb.debugRender) console.log("XTabForm.render", this.props);
    // console.log('this.props.cfg', this.props.cfg);
    var cfg = this.props.cfg;
    var formBody = _(this.props.body, {
      parentCt: this,
      viewMode: this.state.viewMode
    });
    if (!formBody) return null;
    return _(
      Form,
      {
        onSubmit: function(e) {
          e.preventDefault();
        }
      },
      _(
        CardBlock,
        {},
        _(
          "h3",
          { className: "form-header" },
          /* _("i",{className:"icon-star form-icon"})," ", */ this.props.cfg
            .name,
          " ",
          this.state.viewMode &&
            _(
              Button,
              {
                color: "light",
                className: "btn-form-edit",
                onClick: this.toggleViewMode
              },
              _("i", { className: "icon-pencil" }),
              " ",
              "Edit"
            ),
          " ",
          this.state.viewMode &&
            _(
              Button,
              {
                color: "light",
                className: "btn-form-edit",
                onClick: this.props.parentCt.closeTab
              },
              "Close"
            ),
          _(
            Button,
            {
              className: "float-right btn-round-shadow hover-shake",
              color: "danger"
            },
            _("i", { className: "icon-options" })
          ),
          " ",
          _(
            Button,
            { className: "float-right btn-round-shadow mr-1", color: "light" },
            _("i", { className: "icon-bubbles" })
          ),
          " ",
          _(
            Button,
            { className: "float-right btn-round-shadow mr-1", color: "light" },
            _("i", { className: "icon-paper-clip" })
          ),
          _("br"),
          this.props.cfg.approval &&
            _(
              "div",
              {
                style: { fontSize: "1rem", marginTop: 5 }
              },
              _("i", { className: "icon-shuffle" }),
              // " step ",
              _(
                "span",
                null,
                " " +
                  this.props.cfg.approval.dsc +
                  (this.props.cfg.approval.stepDsc
                    ? " > " + this.props.cfg.approval.stepDsc
                    : "")
              ),
              "    ",
              this.props.cfg.approval.status > 997 &&
                this.props.cfg.approval.status < 1000 &&
                _(
                  "span",
                  {
                    className:
                      "badge badge-pill badge-" +
                      iwb.approvalColorMap[this.props.cfg.approval.status]
                  },
                  this.props.cfg.approval.status == 998
                    ? "approved"
                    : "rejected"
                )
            ),
          this.props.cfg.approval &&
            this.props.cfg.approval.wait4start &&
            _(
              Button,
              {
                color: "success",
                className: "btn-form-edit",
                onClick: approvalAction(901)
              },
              _("i", { className: "icon-support" }),
              " ",
              getLocMsg("start_approval")
            ),
          this.props.cfg.approval &&
            this.props.cfg.approval.versionNo &&
            _(
              Button,
              {
                color: "primary",
                className: "btn-form-edit",
                onClick: approvalAction(1) // approve
              },
              getLocMsg("approve")
            ),
          " ",
          this.props.cfg.approval &&
            this.props.cfg.approval.returnFlag &&
            _(
              Button,
              {
                color: "warning",
                className: "btn-form-edit",
                onClick: approvalAction(2) // return
              },
              getLocMsg("return")
            ),
          " ",
          this.props.cfg.approval &&
            this.props.cfg.approval.versionNo &&
            _(
              Button,
              {
                color: "danger",
                className: "btn-form-edit",
                onClick: approvalAction(3) // reject
              },
              getLocMsg("reject")
            ),
          " ",
          this.props.cfg.approval &&
            this.props.cfg.approval.approvalRecordId &&
            _(
              Button,
              {
                color: "light",
                className: "btn-form-edit",
                onClick: iwb.approvalLogs(
                  this.props.cfg.approval.approvalRecordId
                ) // reject
              },
              getLocMsg("logs")
            )
        ),
        this.props.cfg.msgs &&
          this.props.cfg.msgs.length &&
          _(
            "div",
            { style: { color: "#838383" } },
            this.props.cfg.msgs.map(qq =>
              _(
                "div",
                null,
                _("i", { className: "icon-flag" }),
                // " step ",
                _("span", null, " " + qq)
              )
            )
          ),
        _("hr"),
        formBody
      ),
      cfg.conversionCnt &&
        _(
          CardBlock,
          {},
          _("div", { className: "hr-text" }, _("h6", null, "Conversions")),
          _(
            Row,
            null,
            cfg.conversionForms.map((ox, i) => {
              return _(
                "div",
                { className: "col", key: i },
                _("input", {
                  type: "checkbox",
                  checked: !!ox.checked,
                  style: { width: 40 }
                }),
                ox.text
              );
            })
          )
        ),
      cfg.smsMailTemplateCnt &&
        _(
          CardBlock,
          {},
          _("div", { className: "hr-text" }, _("h6", null, "SMS/EMail")),
          _(
            Row,
            null,
            cfg.smsMailTemplates.map((ox, i) => {
              return _(
                "div",
                { className: "col", key: i },
                _("input", {
                  type: "checkbox",
                  checked: !!ox.checked,
                  style: { width: 40 }
                }),
                ox.text
              );
            })
          )
        ),

      !this.state.viewMode &&
        _(
          CardFooter,
          { style: { padding: "1.1rem 1.25rem" } },
          _(
            Button,
            {
              type: "submit",
              color: "submit",
              className: "btn-form mr-1",
              onClick: this.onSubmit
            },
            " ",
            "Save",
            " "
          ),
          " ",
          _(
            Button,
            {
              color: "light",
              style: { border: ".5px solid #e6e6e6" },
              className: "btn-form",
              onClick: this.props.parentCt.closeTab
            },
            "Cancel"
          )
        )
    );
  }
}

iwb.detailSearch = function() {
  return false;
};

class XModal extends React.Component {
  constructor(props) {
    super(props);
    this.toggle = this.toggle.bind(this);
    this.open = this.open.bind(this);
    this.state = { modal: false };
    iwb.showModal = this.open;
  }
  open(cfg) {
    this.setState({
      modal: !0,
      title: cfg.title || "Form",
      color: cfg.color || "primary",
      size: cfg.size || "lg",
      body: cfg.body,
      footer: cfg.footer !== false,
      props: cfg.props || {}
    });
    return false;
  }
  toggle() {
    this.setState({ modal: !this.state.modal });
  }
  render() {
    return (
      this.state.modal &&
      _(
        Modal,
        {
          keyboard: true,
          backdrop: this.state.footer !== false ? "static" : true,
          toggle: this.toggle,
          isOpen: this.state.modal,
          className: "modal-" + this.state.size + " primary"
        },
        _(
          ModalHeader,
          { toggle: this.toggle, className: "bg-" + this.state.color },
          this.state.title
        ),
        _(ModalBody, null, this.state.body),
        this.state.footer !== false &&
          _(
            ModalFooter,
            null,
            _(
              Button,
              { className: "btn-form", color: "teal", onClick: this.toggle },
              "Save"
            ),
            " ",
            _(
              Button,
              {
                className: "btn-form",
                color: "light",
                style: { border: ".5px solid #e6e6e6" },
                onClick: this.toggle
              },
              "Cancel"
            )
          )
      )
    );
  }
}

class XLoginDialog extends React.Component {
  constructor(props) {
    super(props);
    this.state = { modal: false, msg: false };
    this.open = this.open.bind(this);
    this.login = this.login.bind(this);
    iwb.showLoginDialog = this.open;
  }
  open() {
    this.setState({ modal: !0 });
  }
  login() {
    var self = this;
    var passWord = document.getElementById("id-password").value;
    if (!passWord) {
      self.setState({ msg: "Önce Şifre Giriniz" });
      return false;
    }
    iwb.request({
      url:
        "ajaxAuthenticateUser?userRoleId=" +
        _scd.userRoleId +
        "&locale=" +
        _scd.locale +
        (_scd.projectId ? "&projectId=" + _scd.projectId : ""),
      params: {
        customizationId: _scd.customizationId,
        userName: _scd.userName,
        passWord: passWord,
        locale: _scd.locale
      },
      callback: function(j) {
        if (j.success) {
          if (!j.waitFor) {
            if (j.session) _scd = j.session;
            self.setState({ modal: false, msg: false });
          } else {
            self.setState({ msg: "TODO! " + j.waitFor });
          }
          return false;
        } else {
          self.setState({ msg: j.errorMsg });
        }
      },
      errorCallback: function(j) {
        this.setState({ msg: "Olmadi" });
      }
    });
  }
  render() {
    return _(
      Modal,
      {
        keyboard: false,
        backdrop: "static",
        toggle: this.toggle,
        isOpen: this.state.modal,
        centered: true,
        className: "modal-sm primary"
      },
      _(
        ModalBody,
        null,
        _("h1", null, "Login"),
        _(
          "p",
          {
            className: "text-muted",
            style: { color: this.state.msg ? "red !important" : "" }
          },
          this.state.msg || "Session Timeout"
        ),
        _(
          InputGroup,
          {
            className: "mb-3"
          },
          _(
            "div",
            {
              className: "input-group-prepend"
            },
            _(
              "span",
              {
                className: "input-group-text"
              },
              _("i", {
                className: "icon-user"
              })
            )
          ),
          _(Input, {
            type: "text",
            readOnly: true,
            value: _scd.userName,
            placeholder: "Username"
          })
        ),
        _(
          InputGroup,
          {
            className: "mb-4"
          },
          _(
            "div",
            {
              className: "input-group-prepend"
            },
            _(
              "span",
              {
                className: "input-group-text"
              },
              _("i", {
                className: "icon-lock"
              })
            )
          ),
          _(Input, {
            type: "password",
            id: "id-password",
            placeholder: "Password"
          })
        )
      ),
      _(
        ModalFooter,
        null,
        _(Button, { color: "primary", onClick: this.login }, "Login"),
        " ",
        _(
          Button,
          {
            color: "secondary",
            onClick: function() {
              document.location = "login.htm?.r=" + Math.random();
            }
          },
          "Exit"
        )
      )
    );
  }
}

function buildParams2(params, map) {
  var bp = "";
  for (var key in params) {
    var newKey = params[key];
    if (typeof newKey == "function") {
      bp += "&" + key + "=" + newKey(params);
    } else if (newKey.charAt(0) == "!")
      bp += "&" + key + "=" + newKey.substring(1);
    else bp += "&" + key + "=" + map[params[key]];
  }
  return bp;
}

class XGridRowAction extends React.PureComponent {
  constructor(props) {
    super(props);
    this.toggle = this.toggle.bind(this);
    this.state = { isOpen: false };
  }
  toggle() {
    this.setState({ isOpen: !this.state.isOpen });
  }

  render() {
    return _(
      Dropdown,
      { isOpen: this.state.isOpen, toggle: this.toggle },
      // ,_('i',{className:'icon-options-vertical column-action',
      // onClick:qqq.toggleGridAction})
      _(DropdownToggle, {
        tag: "i",
        className: "icon-options-vertical column-action"
      }),
      this.state.isOpen &&
        _(
          DropdownMenu,
          { className: this.state.isOpen ? "show" : "" },
          // ,_('div',{style:{padding: "7px 13px",background: "gray", color:
          // "darkorange", fontWeight: "500", fontSize:" 16px"}},'İşlemler')
          _(
            DropdownItem,
            { ur: "123", onClick: false },
            _("i", {
              className: "icon-pencil",
              style: {
                marginRight: 5,
                marginLeft: -2,
                fontSize: 12,
                color: "#777"
              }
            }),
            "Güncelle"
          ),
          _(
            DropdownItem,
            { ur: "1223", onClick: false },
            _("i", {
              className: "icon-minus text-danger",
              style: {
                marginRight: 5,
                marginLeft: -2,
                fontSize: 12,
                color: "#777"
              }
            }),
            "Sil"
          )
          // ,_(DropdownItem,{ur:'1223',onClick:false},_('i',{className:'icon-drop',style:{marginRight:5,
          // marginLeft:-2, fontSize:12,color:'#777'}}),'Diğer İşlemler')
        )
    );
  }
}

class XGridAction extends React.PureComponent {
  constructor(props) {
    super(props);
    this.toggle = this.toggle.bind(this);
    this.state = { isOpen: false };
  }
  toggle() {
    this.setState({ isOpen: !this.state.isOpen });
  }

  render() {
    return _(
      Dropdown,
      { isOpen: this.state.isOpen, toggle: this.toggle },
      // ,_('i',{className:'icon-options-vertical column-action',
      // onClick:qqq.toggleGridAction})
      _(
        DropdownToggle,
        {
          tag: "div",
          className: "timeline-badge hover-shake " + this.props.color,
          onClick: function() {
            alert("hehey");
          }
        },
        _("i", { className: "icon-grid", style: { fontSize: 17 } })
      ),

      // {tag:'i',className: "icon-grid", color:this.props.color||'danger'}
      this.state.isOpen &&
        _(
          DropdownMenu,
          { className: this.state.isOpen ? "show" : "" },
          // ,_('div',{style:{padding: "7px 13px",background: "gray", color:
          // "darkorange", fontWeight: "500", fontSize:" 16px"}},'İşlemler')
          _(
            DropdownItem,
            { ur: "123", onClick: false },
            _("i", {
              className: "icon-plus",
              style: {
                marginRight: 5,
                marginLeft: -2,
                fontSize: 12,
                color: "#777"
              }
            }),
            "NEW RECORD"
          ),
          _("hr"),
          _(
            DropdownItem,
            { ur: "1223", onClick: false },
            _("i", {
              className: "icon-equalizer",
              style: {
                marginRight: 5,
                marginLeft: -2,
                fontSize: 12,
                color: "#777"
              }
            }),
            "Raporlar/BI"
          )
          // ,_(DropdownItem,{ur:'1223',onClick:false},_('i',{className:'icon-drop',style:{marginRight:5,
          // marginLeft:-2, fontSize:12,color:'#777'}}),'Diğer İşlemler')
        )
    );
  }
}

iwb.detailPageSize = 10;
iwb.grids = {};
var _dxgrb = window.DevExpress
    ? DevExpress.DXReactGridBootstrap4
    : DXReactGridBootstrap4,
  _dxrg = window.DevExpress ? DevExpress.DXReactGrid : DXReactGrid;
class XGrid extends React.PureComponent {
  constructor(props) {
    super(props);
    var columns = [],
      tableColumnExtensions = [];
    if (
      props.crudFlags &&
      (props.crudFlags.edit || props.crudFlags.remove) &&
      iwb.gridActionColumn
    ) {
      // columns.push({name:'_qw_',title:'.',getCellValue:function(r){return
      // _('i',{className:'icon-options-vertical
      // column-action'})}});//this.toggleGridAction
      columns.push({
        name: "_qw_",
        title: ".",
        getCellValue: function(r) {
          return _(XGridRowAction, r);
        }
      }); // this.toggleGridAction
      tableColumnExtensions.push({
        columnName: "_qw_",
        width: 50,
        align: "right",
        sortingEnabled: false
      });
    }
    var c = props.columns;
    for (var qi = 0; qi < c.length; qi++) {
      var v = { name: c[qi].name, title: c[qi].title };
      switch (c[qi].name) {
        case "pkpkpk_faf":
          v.title = _("i", { className: "icon-paper-clip" });
          break;
        case "pkpkpk_ms":
          v.title = _("i", { className: "icon-envelope" });
          break;
        case "pkpkpk_cf":
          v.title = _("i", { className: "icon-bubble" });
          break;
        case "pkpkpk_apf":
          v.title = _("i", { className: "icon-picture" });
          break;
        case "pkpkpk_vcsf":
          v.title = _("i", { className: "icon-social-github" });
          break;
      }
      // if(c[qi].formatter)console.log('c[qi].formatter',c[qi].formatter)
      if (c[qi].formatter) v.getCellValue = c[qi].formatter;
      columns.push(v);
      tableColumnExtensions.push({
        columnName: c[qi].name,
        align: c[qi].align || "left",
        width: 1 * c[qi].width,
        sortingEnabled: !!c[qi].sort
      });
    }
    this.state = {
      columns: columns,
      columnOrder: columns.map(function(a) {
        return a.name;
      }),
      tableColumnExtensions: tableColumnExtensions,
      columnWidths: tableColumnExtensions.map(function(a) {
        return { columnName: a.columnName, width: a.width };
      }),
      rows: props.rows || [],
      sorting: [],
      totalCount: 0,
      pageSize: props.pageSize || iwb.detailPageSize,
      pageSizes:
        props.pageSize > 1
          ? []//[parseInt(props.pageSize / 2), props.pageSize, 3 * props.pageSize]
          : [5, 10, 25, 100],
      currentPage: 0,
      loading: false,
      gridActionOpen: false
    };

    this.onSortingChange = this.onSortingChange.bind(this);
    this.onCurrentPageChange = this.onCurrentPageChange.bind(this);
    this.onPageSizeChange = this.onPageSizeChange.bind(this);
    this.onColumnWidthsChange = this.onColumnWidthsChange.bind(this);
    this.onColumnOrderChange = this.onColumnOrderChange.bind(this);
    this.TableRow = this.TableRow.bind(this);
  }
  componentDidMount() {
    if (!this.dontRefresh) this.loadData();
    this.dontRefresh = false;
  }
  componentDidUpdate() {
    this.loadData();
    this.dontRefresh = false;
  }
  onSortingChange(sorting) {
    this.setState({
      loading: true,
      sorting: sorting
    });
  }
  onCurrentPageChange(currentPage) {
    this.setState({
      loading: true,
      currentPage: currentPage
    });
  }
  onPageSizeChange(pageSize) {
    var totalPages = Math.ceil(this.state.totalCount / pageSize);
    var currentPage = Math.min(this.state.currentPage, totalPages - 1);

    this.setState({
      loading: true,
      pageSize: pageSize,
      currentPage: currentPage
    });
  }
  onColumnWidthsChange(widths) {
    console.log("onColumnWidthsChange", widths);
    this.setState({ columnWidths: widths });
  }
  onColumnOrderChange(order) {
    console.log("onColumnOrderChange", order);
    this.setState({ columnOrder: order });
  }
  queryString() {
    const { sorting, pageSize, currentPage } = this.state;

    let queryString =
      this.props._url +
      "&limit=" +
      pageSize +
      "&start=" +
      pageSize * currentPage;

    const columnSorting = sorting[0];
    if (columnSorting) {
      const sortingDirectionString =
        columnSorting.direction === "desc" ? " desc" : "";
      queryString +=
        "&sort=" + columnSorting.columnName + sortingDirectionString;
    }

    return queryString;
  }
  loadData(force) {
      if(!iwb.loadGrid[this.props.id])iwb.loadGrid[this.props.id]=this.loadData;

    if (this.props.rows) return;
    // if(this.state.loading===true)return;
    const queryString = this.queryString();
    if (!force && queryString === this.lastQuery) {
      // this.setState({ loading: false });
      return;
    }
    this.setState({ loading: true });
    iwb.request({
      url: queryString,
      self: this,
      params:
        this.props.searchForm &&
        iwb.getFormValues(document.getElementById(this.props.searchForm.id)),
      successCallback: function(result, cfg) {
        cfg.self.setState({
          rows: result.data,
          totalCount: result.total_count,
          loading: false
        });
      },
      errorCallback: function(error, cfg) {
        cfg.self.setState({
          rows: [],
          totalCount: 0,
          loading: false
        });
      }
    });
    this.lastQuery = queryString;
  }
  onOnNewRecord(e, grid, row) {
    if (!grid) grid = this.props;
    if (grid.crudFlags && grid.crudFlags.insert && this.props.openTab) {
      var url = "showForm?_renderer=react16&a=2&_fid=" + grid.crudFormId;
      if (grid._postInsert) {
        url = grid._postInsert(row || {}, url, grid);
        if (!url) return;
      }
      var modal = !!e.ctrlKey;
      this.props.openTab(
        "2-" + grid.gridId,
        url + (modal ? "&_modal=1" : ""),
        {},
        { modal: modal }
      );
    }
  }
  TableRow(x) {
    var props = this.props;

    return _(
      _dxgrb.Table.Row,
      props.openTab &&
      props.crudFlags &&
      props.crudFlags.edit &&
      props.pk &&
      props.crudFormId
        ? Object.assign(
            {
              onDoubleClick: function(e) {
                var pkz = buildParams2(props.pk, x.row);
                var url =
                  "showForm?_renderer=react16&a=1&_fid=" +
                  props.crudFormId +
                  pkz;
                if (props._postUpdate) {
                  var url = this.props._postUpdate(x.row, url, props);
                  if (!url) return;
                }
                var modal = !!e.ctrlKey;
                props.openTab(
                  "1-" + pkz,
                  url + (modal ? "&_modal=1" : ""),
                  {
                    ds: {
                      reload: function() {
                        alert("geldim");
                      }
                    }
                  },
                  { modal: modal }
                );
              },
              style: { cursor: "pointer" }
            },
            x
          )
        : x
    );
  }
  componentWillUnmount() {
    iwb.grids[this.props.id] = Object.assign({}, this.state);
    // console.log('XGrid.componentWillUnmount', Object.assign({},this.state));
  }
  render() {
    const {
      rows,
      columns,
      tableColumnExtensions,
      sorting,
      pageSize,
      pageSizes,
      currentPage,
      totalCount,
      loading,
      columnWidths,
      columnOrder
    } = this.state;
    console.log("pageSize", pageSize);
    if (!rows || !rows.length) return null;
    return _(
      _dxgrb.Grid,
      {
        rows: rows,
        columns: columns,
        getRowId: row => row[this.props.keyField]
      },
      _(
        _dxrg.SortingState,
        !this.props.pageSize
          ? null
          : {
              sorting: sorting,
              onSortingChange: this.onSortingChange,
              columnExtensions: tableColumnExtensions
            }
      ),
      !this.props.pageSize ? _(_dxrg.SearchState, null) : null,
      !this.props.pageSize ? _(_dxrg.RowDetailState, null) : null,
      !this.props.pageSize && rows.length > 1
        ? _(_dxrg.IntegratedFiltering, null)
        : null,
      !this.props.pageSize && rows.length > 1
        ? _(_dxrg.GroupingState, null)
        : null,
      !this.props.pageSize && rows.length > 1
        ? _(_dxrg.IntegratedGrouping, null)
        : null,
      !this.props.pageSize && rows.length > 1
        ? _(_dxrg.IntegratedSorting, null)
        : null,
      this.props.showDetail ? _(_dxrg.RowDetailState, null) : null,
      rows.length > iwb.detailPageSize || pageSize > 1
        ? _(
            _dxrg.PagingState,
            pageSize > 1
              ? {
                  currentPage: currentPage,
                  onCurrentPageChange: this.onCurrentPageChange,
                  pageSize: pageSize,
                  onPageSizeChange: this.onPageSizeChange
                }
              : {}
          )
        : null,
      pageSize > 1 && rows.length > 1
        ? _(_dxrg.CustomPaging, { totalCount: totalCount })
        : null,
      _(_dxgrb.DragDropProvider, null),
      _(_dxgrb.Table, {
        columnExtensions: tableColumnExtensions,
        rowComponent: this.TableRow
      }), // ,cellComponent: Cell
      _(_dxgrb.TableColumnReordering, {
        order: columnOrder,
        onOrderChange: this.onColumnOrderChange
      }),
      _(_dxgrb.TableColumnResizing, {
        columnWidths: columnWidths,
        onColumnWidthsChange: this.onColumnWidthsChange
      }),
      _(_dxgrb.TableHeaderRow, { showSortingControls: true }),
      this.props.showDetail
        ? _(_dxgrb.TableRowDetail, { contentComponent: this.props.showDetail })
        : null,
      rows.length > iwb.detailPageSize || this.props.pageSize > 1
        ? _(_dxgrb.PagingPanel, { pageSizes: pageSizes || iwb.detailPageSize })
        : null,
      !this.props.pageSize && rows.length > 1
        ? _(_dxgrb.TableGroupRow, null)
        : null,
      !this.props.pageSize && rows.length > 1 ? _(_dxgrb.Toolbar, null) : null,
      !this.props.pageSize && rows.length > 1
        ? _(_dxgrb.SearchPanel, {
            messages: { searchPlaceholder: "Quick Search..." },
            changeSearchValue: function(ax) {
              console.log("onValueChange", ax);
            }
          })
        : null, // TODO
      !this.props.pageSize && rows.length > 1
        ? _(_dxgrb.GroupingPanel, { showSortingControls: true })
        : null
      // ,loading && iwb.loading()
    );
  }
}

const commandComponentProps = {
  add: {
    icon: "plus",
    hint: "Create new row"
  },
  edit: {
    icon: "pencil",
    hint: "Edit row",
    color: "text-warning"
  },
  delete: {
    icon: "trash",
    hint: "Delete row",
    color: "text-danger"
  },
  /*
	 * commit: { icon: 'check', hint: 'Save changes', color: 'text-success', },
	 */
  cancel: {
    icon: "x",
    hint: "Cancel changes",
    color: "text-danger"
  }
};
const CommandButton = ({ onExecute, icon, text, hint, color, row }) => {
  return _(
    "button",
    {
      className: "btn btn-link",
      style: { padding: "11px" },
      onClick: e => {
        onExecute();
        e.stopPropagation();
      },
      title: hint
    },
    _(
      "span",
      { className: color || "undefined" },
      icon
        ? _("i", {
            className: "oi oi-" + icon,
            style: { marginRight: text ? 5 : 0 }
          })
        : null,
      text
    )
  );
};
const Command = ({ id, onExecute }) => {
  var c = commandComponentProps[id];
  return c
    ? _(CommandButton, Object.assign({}, c, { onExecute: onExecute }))
    : null;
};

iwb.prepareParams4grid = function(grid, prefix, values) {
  // sadece master-insert durumunda cagir. farki _postMap ve hic bir zaman
  // _insertedItems,_deletedItems dikkate almamasi
  var dirtyCount = 0;
  var params = {};
  var items = values.deleted;
  var pk = grid._pk || grid.pk;
  if (items)
    for (var bjk = 0; bjk < items.length; bjk++) {
      // deleted
      dirtyCount++;
      for (var key in pk) {
        var val = pk[key];
        if (typeof val == "function") {
          params[key + prefix + "." + dirtyCount] = val(items[bjk]);
        } else {
          params[key + prefix + "." + dirtyCount] =
            val.charAt(0) == "!" ? val.substring(1) : items[bjk][val];
        }
      }
      params["a" + prefix + "." + dirtyCount] = 3;
    }
  items = values.changed;
  if (items)
    for (var bjk = 0; bjk < items.length; bjk++) {
      // edited
      dirtyCount++;
      params["a" + prefix + "." + dirtyCount] = 1;
      var changes = items[bjk]._new;
      for (var key in changes)
        params[key + prefix + "." + dirtyCount] = changes[key];
      if (grid._postMap)
        for (var key in grid._postMap) {
          var val = grid._postMap[key];
          if (typeof val == "function") {
            params[key + prefix + "." + dirtyCount] = val(changes);
          } else {
            params[key + prefix + "." + dirtyCount] =
              val.charAt(0) == "!" ? val.substring(1) : changes[val];
          }
        }

      for (var key in pk) {
        var val = pk[key];
        if (typeof val == "function") {
          params[key + prefix + "." + dirtyCount] = val(items[bjk]);
        } else {
          params[key + prefix + "." + dirtyCount] =
            val.charAt(0) == "!" ? val.substring(1) : items[bjk][val];
        }
      }
    }
  items = values.inserted;
  if (items)
    for (var bjk = 0; bjk < items.length; bjk++) {
      // inserted
      dirtyCount++;
      params["a" + prefix + "." + dirtyCount] = 2;
      var changes = items[bjk]._new;
      for (var key in changes)
        params[key + prefix + "." + dirtyCount] = changes[key];

      if (grid._postMap)
        for (var key in grid._postMap) {
          var val = grid._postMap[key];
          if (typeof val == "function") {
            params[key + prefix + "." + dirtyCount] = val(changes);
          } else {
            params[key + prefix + "." + dirtyCount] =
              val.charAt(0) == "!" ? val.substring(1) : changes[val];
          }
        }
      if (grid._postInsertParams) {
        for (var key in grid._postInsertParams)
          params[key + prefix + "." + dirtyCount] = grid._postInsertParams[key];
      }
    }
  if (dirtyCount > 0) {
    params["_cnt" + prefix] = dirtyCount;
    params["_fid" + prefix] = grid.crudFormId;
    return params;
  } else return {};
};
class XEditGrid extends React.PureComponent {
  constructor(props) {
    console.log("XEditGrid.constructor", props);
    super(props);
    var oldGridState = iwb.grids[props.id];
    console.log("oldGridState", oldGridState);
    if (oldGridState) {
      this.dontRefresh = !0;
      this.state = oldGridState;
      var c = props.columns;
      this.editors = {};
      for (var qi = 0; qi < c.length; qi++) {
        var editor = c[qi].editor || false;
        if (editor) {
          this.editors[c[qi].name] = editor;
        }
      }
    } else {
      var columns = [],
        tableColumnExtensions = [];
      var c = props.columns;
      this.editors = {};
      for (var qi = 0; qi < c.length; qi++) {
        switch (c[qi].name) {
          case "pkpkpk_faf":
          case "pkpkpk_ms":
          case "pkpkpk_cf":
          case "pkpkpk_apf":
          case "pkpkpk_vcsf":
            break;
          default:
            var v = { name: c[qi].name, title: c[qi].title };
            if (c[qi].formatter) v.getCellValue = c[qi].formatter;
            columns.push(v);
            var editor = c[qi].editor || false;
            if (editor) {
              editor.autoComplete = "off";
              if (!editor.style) editor.style = {};
              editor.style.width = "100%";
              switch (1 * editor._control) {
                case 6:
                case 8:
                case 58:
                case 7:
                case 15:
                case 59:
                case 9:
                case 10: // combos
                  break;
                default:
                  editor.style.textAlign = c[qi].align || "left";
              }
              this.editors[c[qi].name] = editor;
            }
            tableColumnExtensions.push({
              columnName: c[qi].name,
              editingEnabled: !!editor,
              align: c[qi].align || "left",
              width: 1 * c[qi].width,
              sortingEnabled: !!c[qi].sort
            });
        }
      }
      this.state = {
        viewMode: !props.editable && (props.viewMode || true),
        columns: columns,
        columnOrder: columns.map(function(a) {
          return a.name;
        }),
        tableColumnExtensions: tableColumnExtensions,
        columnWidths: tableColumnExtensions.map(function(a) {
          return { columnName: a.columnName, width: a.width };
        }),
        rows: [],
        sorting: [],
        totalCount: 0,
        pageSize: props.pageSize || iwb.detailPageSize,
        pageSizes:
          props.pageSize > 1
            ? []//[parseInt(props.pageSize / 2), props.pageSize, 3 * props.pageSize]
            : [5, 10, 25, 100],
        currentPage: 0,
        loading: false,
        deletingRows: [],
        addedRows: [],
        editingRowIds: [],
        rowChanges: {},
        deletedRows: [],
        pkInsert: 0
      };
    }
    // this._pk4insert = 0; //state te olmasi lazim: TODO
    this.onSortingChange = sorting => this.setState({ sorting });
    this.onCurrentPageChange = currentPage => this.setState({ currentPage });
    this.onPageSizeChange = this.onPageSizeChange.bind(this);
    this.onColumnWidthsChange = columnWidths => this.setState({ columnWidths });
    this.onColumnOrderChange = columnOrder => this.setState({ columnOrder });
    this.changeEditingRowIds = editingRowIds => {
      this.setState({ editingRowIds });
    };
    this.getValues = () => {
      let { rows, addedRows, deletedRows, editingRowIds } = this.state;
      rows = rows.slice();
      var changedRows = [];
      editingRowIds.forEach(rowId => {
        if (rowId > 0) {
          const index = rows.findIndex(
            row => row[this.props.keyField] === rowId
          );
          if (index > -1) changedRows.push(Object.assign({}, rows[index]));
        }
      });
      return {
        inserted: addedRows,
        deleted: deletedRows,
        changed: changedRows
      };
    };

    this.commitChanges = ({ added, changed, deleted }) => {
      let { rows, deletedRows } = this.state;
      if (deleted && deleted.length && confirm("Are you sure?")) {
        rows = rows.slice();
        deleted.forEach(rowId => {
          const index = rows.findIndex(
            row => row[this.props.keyField] === rowId
          );
          if (index > -1) {
            if (rowId > 0) {
              deletedRows.push(Object.assign({}, rows[index]));
            }
            rows.splice(index, 1);
          }
        });
        this.setState({ rows, deletingRows: [], deletedRows });
      }
    };
    this.changeAddedRows = addedRows => {
      var newRecord = Object.assign({}, this.props.newRecord || {});
      var pk = this.state.pk4insert;
      --pk;
      newRecord[this.props.keyField] = pk;
      this.setState({
        pk4insert: pk,
        addedRows: addedRows.map(
          row => (Object.keys(row).length ? row : newRecord)
        )
      });
    };
    this.deleteRows = () => {
      const rows = this.state.rows.slice();
      this.state.deletingRows.forEach(rowId => {
        const index = rows.findIndex(row => row.id === rowId);
        if (index > -1) {
          rows.splice(index, 1);
        }
      });
      this.setState({ rows, deletingRows: [] });
    };
    this.changeRowChanges = rowChanges => {
      this.setState({ rowChanges });
    };

    this.EditCell = this.EditCell.bind(this);
    if (props.parentCt && props.parentCt.egrids)
      props.parentCt.egrids[props.gridId] = this;
    // this.SimpleEditCell = (props) => {console.log('SimpleEditCell',
    // props);return _(_dxgrb.TableEditRow.Cell, props);}
  }
  componentDidMount() {
    if (!this.dontRefresh) this.loadData();
  }
  componentDidUpdate() {
    if (this.props.editable && this.props.viewMode != this.state.viewMode) {
      this.setState({ viewMode: this.props.viewMode });
    }
  }
  onPageSizeChange(pageSize) {
    var totalPages = Math.ceil(this.state.totalCount / pageSize);
    var currentPage = Math.min(this.state.currentPage, totalPages - 1);

    this.setState({
      pageSize: pageSize,
      currentPage: currentPage
    });
  }
  EditCell(xprops) {
    var editor = this.editors[xprops.column.name];
    if (!editor) return _(_dxgrb.TableEditRow.Cell, xprops);

    editor = Object.assign({}, editor);
    if (!xprops.row._new) xprops.row._new = {}; // Object.assign({},xprops.row);
    if (!xprops.row._new.hasOwnProperty(xprops.column.name))
      xprops.row._new[xprops.column.name] = xprops.row[xprops.column.name];
    delete editor.defaultValue;
    switch (1 * editor._control) {
      case 3:
      case 4: // number
        editor.value = xprops.value; // xprops.row._new[xprops.column.name];
        editor.onValueChange = function(o) {
          xprops.row._new[xprops.column.name] = o.value;
          xprops.onValueChange(o.value);
        };
        break;
      case 6:
      case 8:
      case 58:
      case 7:
      case 15:
      case 59:
      case 9:
      case 10: // combos
        editor.value = xprops.row._new[xprops.column.name]; // TODO. ilk edit
        // ettigini aliyor
        editor.onChange = function(o) {
          xprops.row._new[xprops.column.name] = o.id;
          xprops.onValueChange(o.id);
        };
        break;
      default:
        editor.value = xprops.value; // xprops.row._new[xprops.column.name];
        editor.onChange = function(o) {
          xprops.row._new[xprops.column.name] = o.target.value;
          xprops.onValueChange(o.target.value);
        };
        break;
    }
    var cmp = Input;
    if (editor.$) {
      cmp = editor.$;
      delete editor.$;
    }
    return _(
      "td",
      { style: { verticalAlign: "middle", padding: 1 } },
      _(cmp, editor)
    );
  }
  queryString() {
    const { sorting, pageSize, currentPage } = this.state;

    let queryString =
      this.props._url +
      "&limit=" +
      pageSize +
      "&start=" +
      pageSize * currentPage;

    const columnSorting = sorting[0];
    if (columnSorting) {
      const sortingDirectionString =
        columnSorting.direction === "desc" ? " desc" : "";
      queryString +=
        "&sort=" + columnSorting.columnName + sortingDirectionString;
    }

    return queryString;
  }
  loadData(force) {
    const queryString = this.props._url;
    this.setState({ loading: true });
    iwb.request({
      url: queryString,
      self: this,
      params:
        this.props.searchForm &&
        iwb.getFormValues(document.getElementById(this.props.searchForm.id)),
      successCallback: function(result, cfg) {
        cfg.self.setState({
          rows: result.data,
          totalCount: result.total_count,
          loading: false
        });
      },
      errorCallback: function(error, cfg) {
        cfg.self.setState({
          rows: [],
          totalCount: 0,
          loading: false
        });
      }
    });
    this.lastQuery = queryString;
  }
  componentWillUnmount() {
    iwb.grids[this.props.id] = Object.assign({}, this.state);
    // console.log('XGrid.componentWillUnmount', Object.assign({},this.state));
  }
  render() {
    // console.log('XEditGrid:render')
    const {
      viewMode,
      rows,
      columns,
      tableColumnExtensions,
      sorting,
      pageSize,
      pageSizes,
      currentPage,
      totalCount,
      loading,
      columnWidths,
      columnOrder,
      editingRowIds,
      rowChanges,
      addedRows
    } = this.state;
    return _(
      _dxgrb.Grid,
      {
        rows: rows,
        columns: columns,
        getRowId: row => row[this.props.keyField]
      },
      _(_dxrg.SortingState, null),
      _(_dxrg.SearchState, null),
      _(_dxrg.IntegratedFiltering, null),
      _(_dxrg.GroupingState, null),
      _(_dxrg.IntegratedGrouping, null),
      _(_dxrg.IntegratedSorting, null),
      rows.length > iwb.detailPageSize
        ? _(
            _dxrg.PagingState,
            pageSize > 1
              ? {
                  currentPage: currentPage,
                  onCurrentPageChange: this.onCurrentPageChange,
                  pageSize: pageSize,
                  onPageSizeChange: this.onPageSizeChange
                }
              : {}
          )
        : null,
      !viewMode &&
        _(_dxrg.EditingState, {
          columnExtensions: tableColumnExtensions,
          editingRowIds: editingRowIds,
          onEditingRowIdsChange: this.changeEditingRowIds,
          rowChanges: rowChanges,
          onRowChangesChange: this.changeRowChanges,
          addedRows: addedRows,
          onAddedRowsChange: this.changeAddedRows,
          onCommitChanges: this.commitChanges
        }),
      _(_dxgrb.DragDropProvider, null),
      _(_dxgrb.Table, { columnExtensions: tableColumnExtensions }), // ,cellComponent:
      // Cell
      _(_dxgrb.TableColumnReordering, {
        order: columnOrder,
        onOrderChange: this.onColumnOrderChange
      }),
      _(_dxgrb.TableColumnResizing, {
        columnWidths: columnWidths,
        onColumnWidthsChange: this.onColumnWidthsChange
      }),
      _(_dxgrb.TableHeaderRow, { showSortingControls: !0 }),
      !viewMode && _(_dxgrb.TableEditRow, { cellComponent: this.EditCell }),
      !viewMode &&
        _(_dxgrb.TableEditColumn, {
          showAddCommand: this.props.crudFlags.insert,
          showEditCommand: this.props.crudFlags.edit,
          showDeleteCommand: this.props.crudFlags.remove,
          commandComponent: Command
        }),
      rows.length > iwb.detailPageSize
        ? _(_dxgrb.PagingPanel, { pageSizes: pageSizes || iwb.detailPageSize })
        : null,
      _(_dxgrb.TableGroupRow, null),
      _(_dxgrb.Toolbar, null),
      _(_dxgrb.SearchPanel, {
        messages: { searchPlaceholder: "Quick Search..." }
      }),
      _(_dxgrb.GroupingPanel, { showSortingControls: true })
    );
  }
}

function getMasterGridSel(a, sel) {
  return sel;
}

class XMainGrid extends React.PureComponent {
  constructor(props) {
    super(props);
    var oldGridState = iwb.grids[props.id];
    console.log("oldGridState", oldGridState);
    if (oldGridState) {
      this.state = oldGridState;
      this.dontRefresh = true;
    } else {
      var columns = [],
        tableColumnExtensions = [];
      /*
		 * if(iwb.gridActionColumn){
		 * columns.push({name:'_qw_',title:'.',getCellValue:function(r){return
		 * _(XGridRowAction,r);}});
		 * tableColumnExtensions.push({columnName:'_qw_',width:60,
		 * align:'right',sortingEnabled:false}); }
		 */
      var c = props.columns;
      for (var qi = 0; qi < c.length; qi++) {
        var v = { name: c[qi].name, title: c[qi].title };
        switch (c[qi].name) {
          case "pkpkpk_faf":
            v.title = _("i", { className: "icon-paper-clip" });
            break;
          case "pkpkpk_ms":
            v.title = _("i", { className: "icon-envelope" });
            break;
          case "pkpkpk_cf":
            v.title = _("i", { className: "icon-bubble" });
            break;
          case "pkpkpk_apf":
            v.title = _("i", { className: "icon-picture" });
            break;
          case "pkpkpk_vcsf":
            v.title = _("i", { className: "icon-social-github" });
            break;
        }
        // if(c[qi].formatter)console.log('c[qi].formatter',c[qi].formatter)
        if (c[qi].formatter) v.getCellValue = c[qi].formatter;
        columns.push(v);
        tableColumnExtensions.push({
          columnName: c[qi].name,
          align: c[qi].align || "left",
          width: 1 * c[qi].width,
          sortingEnabled: !!c[qi].sort
        });
      }
      var state = {
        columns: columns,
        columnOrder: columns.map(function(a) {
          return a.name;
        }),
        tableColumnExtensions: tableColumnExtensions,
        columnWidths: tableColumnExtensions.map(function(a) {
          return { columnName: a.columnName, width: a.width };
        }),
        rows: [],
        sorting: [],
        totalCount: 0,
        pageSize: props.pageSize || iwb.detailPageSize,
        pageSizes:
          props.pageSize > 1
            ? []//[parseInt(props.pageSize / 2), props.pageSize, 3 * props.pageSize]
            : [5, 10, 25, 100],
        currentPage: 0,
        hideSF: true,
        loading: false
      };
      props.detailGrids &&
        props.detailGrids.length > 1 &&
        props.detailGrids.map(function(a, key) {
          if (key < 2) state["dg-" + a.grid.gridId] = key < 2;
        });
      this.state = state;
    }

    this.onSortingChange = this.onSortingChange.bind(this);
    this.onCurrentPageChange = this.onCurrentPageChange.bind(this);
    this.onPageSizeChange = this.onPageSizeChange.bind(this);
    this.onColumnWidthsChange = this.onColumnWidthsChange.bind(this);
    this.onColumnOrderChange = this.onColumnOrderChange.bind(this);
    this.toggleSearch = this.toggleSearch.bind(this);
    this.toggleDetailGrid = this.toggleDetailGrid.bind(this);
    this.openBI = this.openBI.bind(this);
    this.onGlobalSearch = this.onGlobalSearch.bind(this);
    iwb.onGlobalSearch2 = this.onGlobalSearch;
    this.showDetail2 = this.showDetail2.bind(this);
    this.TableRow = this.TableRow.bind(this);

    if (
      this.props.searchForm ||
      (this.props.detailGrids && this.props.detailGrids.length > 1)
    ) {
      // hidden:!!this.props.grid.globalSearch
      var self = this;
      this.searchForm = _(
        Nav,
        { style: {} },
        this.props.searchForm
          ? [
              _(
                "div",
                { className: "hr-text" },
                _("h6", null, "Search Criteria")
              ),
              _(
                "div",
                { style: { zoom: ".9" } },
                _(this.props.searchForm, { parentCt: this }),
                _(
                  "div",
                  { className: "form-group", style: { paddingTop: 10 } },
                  _(
                    Button,
                    {
                      color: "danger",
                      style: { width: "100%", borderRadius: 2 },
                      onClick: () => {
                        this.loadData(!0);
                      }
                    },
                    "SEARCH"
                  )
                )
              ),

              // ,_('div',{style:{height:10}}),_('div',{className:'hr-text'},_('h6',null,'Şablonlar'))
              // ,_(Link,{style:{padding:2},to:''},_('i',{className:'icon-star'}),'
              // ',' Yıllık Faturalar') //TODO
              // ,_(Link,{style:{padding:'2px'},to:''},_('i',{className:'icon-star'}),'
              // ',' Ankara')
              // ,_(Link,{style:{padding:2,color:'#a0a0a0'},to:''},_('i',{className:'icon-plus'}),'
              // ',' Yeni Şablon Ekle')
              _("div", { style: { height: 20 } })
            ]
          : null,
        this.props.detailGrids &&
          this.props.detailGrids.length > 1 &&
          _("div", { className: "hr-text" }, _("h6", null, "Detail Records")),
        this.props.detailGrids &&
          this.props.detailGrids.length > 1 &&
          this.props.detailGrids.map(function(a, key) {
            return _(
              "div",
              {
                key: key,
                style: {
                  padding: "3px 0px 2px 3px",
                  color: "#6d7284",
                  fontSize: ".9rem"
                }
              },
              a.grid.name,
              _(
                "label",
                {
                  className:
                    "float-right switch switch-xs switch-3d switch-" +
                    dgColors[key % dgColors.length] +
                    " form-control-label"
                },
                _("input", {
                  name: "dg-" + a.grid.gridId,
                  type: "checkbox",
                  className: "switch-input form-check-input",
                  onChange: self.toggleDetailGrid,
                  defaultChecked: self.state["dg-" + a.grid.gridId]
                }),
                _("span", { className: "switch-label" }),
                _("span", { className: "switch-handle" })
              )
            );
          })
      );
    }
  }
  componentDidMount() {
    if (!this.dontRefresh) this.loadData();
    this.dontRefresh = false;
  }
  toggleDetailGrid(e) {
    var c = e.target;
    var s = {};
    s[c.name] = c.checked;
    this.setState(s);
  }
  showDetail2(dgs) {
    var xxx = this;
    return function(row) {
      if (row) {
        var r = [];
        // console.log('dgs',dgs);
        for (var qi = 0; qi < dgs.length; qi++)
          if (dgs.length == 1 || xxx.state["dg-" + dgs[qi].grid.gridId]) {
            var g2 = Object.assign({ pk: dgs[qi].pk || {} }, dgs[qi].grid); // buildParams2(obj.detailGrids[i].params,
            // sel);
            if (g2._url) g2._url += buildParams2(dgs[qi].params, row.row);
            else g2.rows = row.row[g2.detailRowsFieldName];
            g2.detailFlag = true;
            r.push(
              _(
                "li",
                { key: qi, className: "timeline-inverted" },
                // _(XGridAction,{color:dgColors[qi%dgColors.length]}),
                _(
                  "div",
                  {
                    className:
                      "timeline-badge hover-shake " +
                      dgColors[qi % dgColors.length],
                    i: qi,
                    onClick: function(e) {
                      var i = 1 * e.target.getAttribute("i");
                      console.log("dasss", i, dgs[i].grid);
                      xxx.onOnNewRecord(e, dgs[i].grid, row.row);
                    },
                    style: { cursor: "pointer" }
                  },
                  _("i", { className: "icon-grid", style: { fontSize: 17 } })
                ),
                _(
                  "div",
                  { className: "timeline-panel" },
                  _(
                    "div",
                    { className: "timeline-heading" },
                    _(
                      "h5",
                      {
                        /* style:{paddingBottom: '10px'}, */ className:
                          "timeline-title"
                      },
                      g2.name
                    )
                    // ,_('span',{className: "float-right",
                    // style:{marginTop:'-23px', marginRight:'15px'}},_('i',{
                    // className: "icon-arrow-up", style:{marginRight:
                    // '12px'}}),' ',_('i',{ className: "icon-close"}),' ')
                  ),
                  _(
                    XGrid,
                    Object.assign(
                      {
                        responsive: true,
                        openTab: xxx.props.openTab,
                        showDetail: dgs[qi].detailGrids
                          ? xxx.showDetail2(dgs[qi].detailGrids)
                          : false
                      },
                      g2
                    )
                  )
                )
              )
            );
          }
        return r.length > 0 && _("ul", { className: "timeline" }, r);
      } else return null;
    };
  }
  componentDidUpdate() {
    this.loadData();
    this.dontRefresh = false;
  }
  onSortingChange(sorting) {
    this.setState({
      loading: true,
      sorting: sorting
    });
  }
  onCurrentPageChange(currentPage) {
    this.setState({
      loading: true,
      currentPage: currentPage
    });
  }
  onPageSizeChange(pageSize) {
    var totalPages = Math.ceil(this.state.totalCount / pageSize);
    var currentPage = Math.min(this.state.currentPage, totalPages - 1);

    this.setState({
      loading: true,
      pageSize: pageSize,
      currentPage: currentPage
    });
  }
  onColumnWidthsChange(widths) {
    console.log("onColumnWidthsChange", widths);
    this.setState({ columnWidths: widths });
  }
  onColumnOrderChange(order) {
    console.log("onColumnOrderChange", order);
    this.setState({ columnOrder: order });
  }
  queryString() {
    const { sorting, pageSize, currentPage } = this.state;

    let queryString =
      this.props._url +
      "&limit=" +
      pageSize +
      "&start=" +
      pageSize * currentPage;

    const columnSorting = sorting[0];
    if (columnSorting) {
      const sortingDirectionString =
        columnSorting.direction === "desc" ? " desc" : "";
      queryString +=
        "&sort=" + columnSorting.columnName + sortingDirectionString;
    }

    return queryString;
  }
  toggleSearch() {
    /*
	 * var b = this.state.hideSF; this.setState({hideSF:!b}); return false;
	 */
    var sf = document.getElementById("sf-" + this.props.id);
    if (sf) {
      var eq = document.getElementById("eq-" + this.props.id);
      if (sf.classList.contains("sf-hidden")) {
        eq.classList.add("rotate-90deg");
      } else {
        eq.classList.remove("rotate-90deg");
      }
      sf.classList.toggle("sf-hidden");
    }
    return false;
  }
  loadData(force, params) {
      if(!iwb.loadGrid[this.props.id])iwb.loadGrid[this.props.id]=this.loadData;

    // if(this.state.loading===true)return;
    const queryString = this.queryString();
    if (!force && queryString === this.lastQuery) {
      // this.setState({ loading: false });
      return;
    }
    this.setState({ loading: true });
    // var params= Object.assign({},params||{},this.props.searchForm ?
    // iwb.getFormValues(document.getElementById('fsf-'+this.props.id)):{});
    var params = Object.assign(
      {},
      params || {},
      this.form ? this.form.getValues() : {}
    );
    // console.log(params);
    iwb.request({
      url: queryString,
      self: this,
      params: params,
      successCallback: function(result, cfg) {
        cfg.self.setState({
          rows: result.data,
          totalCount: result.total_count,
          loading: false
        });
      },
      errorCallback: function(error, cfg) {
        cfg.self.setState({
          rows: [],
          totalCount: 0,
          loading: false
        });
      }
    });
    this.lastQuery = queryString;
  }
  onOnNewRecord(e, grid, row) {
    console.log("XMainGrid.onOnNewRecord");
    if (!grid) grid = this.props;
    if (grid.crudFlags && grid.crudFlags.insert && this.props.openTab) {
      var url = "showForm?_renderer=react16&a=2&_fid=" + grid.crudFormId;
      if (grid._postInsert) {
        url = grid._postInsert(row || {}, url, grid);
        if (!url) return;
      }
      var modal = !!e.ctrlKey;
      this.props.openTab(
        "2-" + grid.gridId,
        url + (modal ? "&_modal=1" : ""),
        {},
        { modal: modal }
      );
    }
  }
  TableRow(x) {
    var props = this.props;

    return _(
      _dxgrb.Table.Row,
      props.openTab &&
      props.crudFlags &&
      props.crudFlags.edit &&
      props.pk &&
      props.crudFormId
        ? Object.assign(
            {
              onDoubleClick: function(e) {
                var pkz = buildParams2(props.pk, x.row);
                var url =
                  "showForm?_renderer=react16&a=1&_fid=" +
                  props.crudFormId +
                  pkz;
                if (props._postUpdate) {
                  var url = this.props._postUpdate(x.row, url, props);
                  if (!url) return;
                }
                var modal = !!e.ctrlKey;
                props.openTab(
                  "1-" + pkz,
                  url + (modal ? "&_modal=1" : ""),
                  {},
                  { modal: modal }
                );
              },
              style: { cursor: "pointer" }
            },
            x
          )
        : x
    );
  }
  componentWillUnmount() {
    var state = Object.assign({}, this.state);
    var sf = document.getElementById("sf-" + this.props.id);
    if (sf) {
      state.hideSF = sf.classList.contains("sf-hidden");
    }

    iwb.grids[this.props.id] = state;
    // console.log('XGrid.componentWillUnmount', Object.assign({},this.state));
  }
  openBI() {
    var props = this.props,
      columns = this.state.tableColumnExtensions,
      columnOrder = this.state.columnOrder,
      cmap = {};
    var url = "grd/" + props.name + ".";
    var params = "?_gid=" + props.gridId + "&_columns=";
    columns.map(function(oo) {
      cmap[oo.columnName] = oo.width;
    });
    columnOrder.map(function(oo) {
      params += oo + "," + (cmap[oo] || 100) + ";";
    });
    iwb.showModal({
      title: "RAPORLAR",
      footer: false,
      color: "danger",
      size: "sm",
      body: _(
        ListGroup,
        { style: { fontSize: "1.0rem" } },
        _("b", null, "Exports"),
        _(
          ListGroupItem,
          {
            tag: "a",
            href: url + "xls" + params,
            target: "_blank",
            action: !0
          },
          _("i", { className: "float-right text-success fa fa-file-excel" }),
          " ",
          "Export to Excel"
        ),
        _(
          ListGroupItem,
          {
            tag: "a",
            href: url + "pdf" + params,
            target: "_blank",
            action: !0
          },
          _("i", { className: "float-right text-danger fa fa-file-pdf" }),
          " ",
          "Export to PDF"
        ),
        _(
          ListGroupItem,
          {
            tag: "a",
            href: url + "csv" + params,
            target: "_blank",
            action: !0
          },
          _("i", { className: "float-right text-secondary fa fa-file-alt" }),
          " ",
          "Export to CSV File"
        ),
        _(
          ListGroupItem,
          {
            tag: "a",
            href: url + "txt" + params,
            target: "_blank",
            action: !0
          },
          _("i", { className: "float-right text-secondary fa fa-file-word" }),
          " ",
          "Export to Text File"
        ),
        _("hr"),
        _("b", null, "BI"),
        _(
          ListGroupItem,
          {
            tag: "a",
            href:
              "showPage?_tid=" +
              (props.crudTableId
                ? "1200&xtable_id=" + props.crudTableId
                : "2395&xquery_id=" + props.queryId),
            target: "_blank",
            action: !0 /* , className:'list-group-item-danger2' */
          },
          _("i", { className: "float-right text-primary fa fa-th" }),
          " ",
          "Pivot Table"
        ),
        _(
          ListGroupItem,
          {
            tag: "a",
            href:
              "showPage?_tid=" +
              (props.crudTableId
                ? "784&xtable_id=" + props.crudTableId
                : "2413&xquery_id=" + props.queryId),
            target: "_blank",
            action: !0
          },
          _("i", { className: "float-right text-primary fa fa-table" }),
          " ",
          "Data List"
        )
      )
    });
  }
  onGlobalSearch(v) {
    this.loadData(!0, { xsearch: v && v.target ? v.target.value : v });
  }

  render() {
    const {
      rows,
      columns,
      tableColumnExtensions,
      sorting,
      pageSize,
      pageSizes,
      currentPage,
      totalCount,
      loading,
      columnWidths,
      columnOrder
    } = this.state;
    var showDetail =
      this.props.detailGrids && this.props.detailGrids.length > 0;
    var g = _(
      _dxgrb.Grid,
      {
        rows: rows,
        columns: columns,
        getRowId: row => row[this.props.keyField]
      },
      _(
        _dxrg.SortingState,
        !pageSize
          ? null
          : {
              sorting: sorting,
              onSortingChange: this.onSortingChange,
              columnExtensions: tableColumnExtensions
            }
      ),
      !pageSize ? _(_dxrg.SearchState, null) : null,
      !pageSize ? _(_dxrg.RowDetailState, null) : null,
      !pageSize && rows.length > 1 ? _(_dxrg.IntegratedFiltering, null) : null,
      !pageSize && rows.length > 1 ? _(_dxrg.GroupingState, null) : null,
      !pageSize && rows.length > 1 ? _(_dxrg.IntegratedGrouping, null) : null,
      !pageSize && rows.length > 1 ? _(_dxrg.IntegratedSorting, null) : null,
      showDetail ? _(_dxrg.RowDetailState, null) : null,
      rows.length > iwb.detailPageSize || pageSize > 1
        ? _(
            _dxrg.PagingState,
            pageSize > 1
              ? {
                  currentPage: currentPage,
                  onCurrentPageChange: this.onCurrentPageChange,
                  pageSize: pageSize,
                  onPageSizeChange: this.onPageSizeChange
                }
              : {}
          )
        : null,
      pageSize > 1 && rows.length > 1
        ? _(_dxrg.CustomPaging, { totalCount: totalCount })
        : null,
      _(_dxgrb.DragDropProvider, null),
      _(_dxgrb.Table, {
        columnExtensions: tableColumnExtensions,
        rowComponent: this.TableRow
      }), // ,cellComponent: Cell
      _(_dxgrb.TableColumnReordering, {
        order: columnOrder,
        onOrderChange: this.onColumnOrderChange
      }),
      _(_dxgrb.TableColumnResizing, {
        columnWidths: columnWidths,
        onColumnWidthsChange: this.onColumnWidthsChange
      }),
      _(_dxgrb.TableHeaderRow, { showSortingControls: true }),
      showDetail
        ? _(_dxgrb.TableRowDetail, {
            contentComponent: this.showDetail2(this.props.detailGrids)
          })
        : null,
      rows.length > iwb.detailPageSize || pageSize > 1
        ? _(_dxgrb.PagingPanel, { pageSizes: pageSizes || iwb.detailPageSize })
        : null,
      !pageSize && rows.length > 1 ? _(_dxgrb.TableGroupRow, null) : null,
      !pageSize && rows.length > 1 ? _(_dxgrb.Toolbar, null) : null,
      !pageSize && rows.length > 1
        ? _(_dxgrb.SearchPanel, {
            messages: { searchPlaceholder: "Quick Search..." },
            changeSearchValue: function(ax) {
              console.log("onValueChange", ax);
            }
          })
        : null, // TODO
      !pageSize && rows.length > 1
        ? _(_dxgrb.GroupingPanel, { showSortingControls: true })
        : null
      // ,loading && iwb.loading()
    );

    return _(
      "div",
      { className: "tab-grid mb-4" },
      this.searchForm &&
        _(
          "nav",
          {
            id: "sf-" + this.props.id,
            className: this.state.hideSF ? "sf-hidden" : ""
          },
          this.searchForm
        ),
      _(
        "main",
        { className: "inbox" },
        _(
          CardHeader,
          {},
          this.searchForm &&
            _(
              Button,
              {
                className: "tlb-button mx-1",
                color: "secondary",
                onClick: this.toggleSearch
              },
              _("i", { id: "eq-" + this.props.id, className: "icon-magnifier" })
            ),
          this.searchForm && " ",
          !this.searchForm &&
            _(
              Button,
              {
                className: "tlb-button mx-1",
                disabled: loading,
                color: "secondary",
                onClick: () => {
                  this.loadData(!0);
                }
              },
              _("i", { className: "icon-refresh" })
            ),
          " ",
          this.props.crudFlags && this.props.crudFlags.insert
            ? _(
                Button,
                {
                  className: "tlb-button",
                  color: "primary",
                  onClick: e => {
                    this.onOnNewRecord(e, this.props);
                  }
                },
                _("i", { className: "icon-plus" }),
                " NEW RECORD"
              )
            : null,
          // ,_(Button,{className:'float-right btn-round-shadow
          // hover-shake',color:'danger',
          // onClick:this.toggleSearch},_('i',{style:{transition: "transform
          // .2s"},id:'eq-'+this.props.id,className:'icon-equalizer'+(this.state.hideSF?'':'
          // rotate-90deg')}))
          _(
            Button,
            {
              className: "float-right tlb-button",
              color: "secondary", style:{color:"red"},
              onClick: this.openBI
            },
            _("i", { className: "icon-equalizer" })
          )
          // , this.props.globalSearch && _(Input,{type:"text",
          // className:"float-right form-control w-25",
          // onChange:this.onGlobalSearch, placeholder:"Hızlı Arama...",
          // defaultValue:"", style:{marginTop: '-0.355rem',
          // marginRight:'.4rem'}})
        ),
        g
      )
    );
    // {loading && <Loading />}
  }
}

iwb.pages = {};
class XPage extends React.Component {
  constructor(props) {
    if (iwb.debugConstructor) console.log("XPage.constructor", props);
    super(props);
    this.toggle = this.toggle.bind(this);
    this.openTab = this.openTab.bind(this);
    this.closeTab = this.closeTab.bind(this);
    this.showDetail2 = this.showDetail2.bind(this);
    this.openForm = this.openForm.bind(this);
    iwb.openForm = this.openForm;

    var oldPageState = iwb.pages[props.grid.id];
    // console.log('oldPageState', oldPageState);
    if (oldPageState) {
      this.state = oldPageState;
      this.dontRefresh = true;
    } else {
      this.state = {
        activeTab: "x",
        tabs: [{ k: "x", i: "icon-list", title: "Liste", v: props.grid }]
      };
    }
  }
  toggle(e) {
    var tab = false;
    if (e.target) {
      tab = e.target.getAttribute("k");
    } else tab = e;
    if (this.state.activeTab !== tab) {
      var tabs = this.state.tabs;
      for (var qi = 0; qi < tabs.length; qi++)
        if (tabs[qi].k === tab) {
          this.setState({ activeTab: tab });
          return true;
        }
    }
    return false;
  }
  closeTab() {
    var state = this.state;
    var tab = state.activeTab;
    if (tab == "x") return;
    var tabs = state.tabs;
    for (var qi = 1; qi < tabs.length; qi++)
      if (tabs[qi].k === tab) {
        // console.log('closeTab.found:'+qi+'/'+tab + '/' + tabs.length);
        state.activeTab = "x";
        tabs.splice(qi, 1);
        state.tabs = tabs;
        this.setState(state);
        return;
      }
  }
  showDetail2(dgs) {
    var self = this;
    return function(row) {
      if (row) {
        var r = [];
        for (var qi = 0; qi < dgs.length; qi++)
          if (self.state["dg-" + dgs[qi].grid.gridId]) {
            var g2 = Object.assign({ pk: dgs[qi].pk || {} }, dgs[qi].grid); // buildParams2(obj.detailGrids[i].params,
            // sel);
            g2._url += buildParams2(dgs[qi].params, row.row);
            g2.detailFlag = true;
            r.push(
              _(
                "li",
                { key: qi, className: "timeline-inverted" },
                _(
                  "div",
                  {
                    className:
                      "timeline-badge " + dgColors[qi % dgColors.length]
                  },
                  _("i", { className: "icon-grid", style: { fontSize: 17 } })
                ),
                _(
                  "div",
                  { className: "timeline-panel" },
                  _(
                    "div",
                    { className: "timeline-heading" },
                    _(
                      "h5",
                      {
                        /* style:{paddingBottom: '10px'}, */ className:
                          "timeline-title"
                      },
                      g2.name
                    )
                    // ,_('span',{className: "float-right",
                    // style:{marginTop:'-23px', marginRight:'15px'}},_('i',{
                    // className: "icon-arrow-up", style:{marginRight:
                    // '12px'}}),' ',_('i',{ className: "icon-close"}),' ')
                  ),
                  _(
                    XGrid,
                    Object.assign(
                      {
                        responsive: true,
                        openTab: self.openTab,
                        showDetail: dgs[qi].detailGrids
                          ? self.showDetail2(dgs[qi].detailGrids)
                          : false
                      },
                      g2
                    )
                  )
                )
              )
            );
          }
        return r.length > 0 && _("ul", { className: "timeline" }, r);
      } else return null;
    };
  }
  openTab(action, url, params, callAttributes) {
    // console.log('openTab.callAttributes', callAttributes)
    if (this.state.activeTab !== action) {
      var tabs = this.state.tabs;
      for (var qi = 1; qi < tabs.length; qi++)
        if (tabs[qi].k === action) {
          this.toggle(action);
          return;
        }

      fetch(url, {
        body: JSON.stringify(params || {}), // must match 'Content-Type' header
        cache: "no-cache", // *default, no-cache, reload, force-cache,
        // only-if-cached
        credentials: "same-origin", // include, same-origin, *omit
        headers: {
          "content-type": "application/json"
        },
        method: "POST", // *GET, POST, PUT, DELETE, etc.
        mode: "cors", // no-cors, cors, *same-origin
        redirect: "follow", // *manual, follow, error
        referrer: "no-referrer" // *client, no-referrer
      })
        .then(response => {
          // status "0" to handle local files fetching (e.g. Cordova/Phonegap
          // etc.)
          if (response.status === 200 || response.status === 0) {
            return response.text();
          } else {
            return Promise.reject(
              new Error(response.text() || response.statusText)
            );
          }
        })
        .then(
          result => {
            if (result) {
              var f;
              console.log("openTab.callAttributes2", callAttributes);
              eval("f=function(callAttributes, parentCt){\n" + result + "\n}");
              var r = f(callAttributes || {}, this);
              if (r) {
                var state = this.state;
                var plus = action.substr(0, 1) == "2";
                tabs.push({
                  k: action,
                  i: plus ? "icon-plus" : "icon-doc",
                  title: plus ? " New" : " Update",
                  v: r
                });
                state.activeTab = action;
                this.setState(state);
              }
            } else {
              toastr.error("Sonuc Gelmedi", " Error");
              // alert('Hata! Sonuc Gelmedi');
            }
          },
          error => {
            toastr.error(error, "Connection Error");
            // alert('Hata! ' + error);
          }
        );
    }
  }
  openForm(url) {
    if (url) this.openTab("1-" + Math.random(), url);
    return false;
  }
  componentWillUnmount() {
    iwb.killGlobalSearch();
    iwb.pages[this.props.grid.id] = Object.assign({}, this.state);
    // console.log('XPage.componentWillUnmount', Object.assign({},this.state));
  }
  render() {
    if (iwb.debugRender) console.log("XPage.render");
    return _(
      "div",
      {},
      _(
        Row,
        null,
        _(
          Col,
          { className: "mb-4" },
          _(
            Nav,
            { tabs: true, hidden: this.state.tabs.length == 1 },
            this.state.tabs.map((o, qi) => {
              return _(
                NavItem,
                { key: qi },
                _(
                  NavLinkS,
                  {
                    className: classNames({
                      active: this.state.activeTab === o.k
                    }),
                    k: o.k,
                    onClick: e => {
                      this.toggle(e);
                    }
                  },
                  _("i", {
                    className: o.i,
                    k: o.k,
                    title: o.title,
                    onClick: e => {
                      this.toggle(e);
                    }
                  }),
                  o.title && o.k != "x" && this.state.activeTab === o.k
                    ? o.title
                    : null
                )
              );
            })
          ),
          _(
            TabContent,
            { activeTab: this.state.activeTab },
            this.state.tabs.map((o, qi) => {
              return _(
                TabPane,
                { key: qi, tabId: o.k },
                o.v.gridId
                  ? _(
                      XMainGrid,
                      Object.assign(
                        { openTab: this.openTab, closeTab: this.closeTab },
                        o.v
                      )
                    )
                  : o.v
              );
            })
          )
        )
      )
    );
  }
}

var dgColors2 = [
  "primary",
  "info",
  "secondary",
  "gray-700",
  "gray-500",
  "gray-400",
  "gray-700"
];
var detailSpinnerColors2 = [
  "#187da0",
  "#2eadd3",
  "darkorange",
  "#187da0",
  "#4d6672",
  "#626a70",
  "#66767d"
];
var dBGColors2 = [, , "#de9338", "#222", , , , ,];
class XCardMenu extends React.PureComponent {
  render() {
    return _(
      Col,
      { xs: "12", sm: "6", md: "6", lg: "6", xl: "4" },
      _(
        Link,
        { to: this.props.node.url },
        _(
          Card,
          {
            // url:this.props.node.url,onClick:(e)=>{console.log(this.props.history);console.log('this.props.router',this.props.router);this.props.history.push(this.props.node.url)},
            className: "card-menu text-white bg-" + this.props.color,
            style: this.props.fadeOut
              ? { opacity: 0, transform: "scale(.9)" }
              : this.props.fadeOut === false
                ? { transform: "scale(1.1)" }
                : {}
          },
          _("i", {
            className: "big-icon " + (this.props.node.icon || "icon-settings"),
            style: this.props.color3 ? { color: this.props.color3 } : {}
          }),
          _(
            CardBlock,
            { className: "pb-0" },
            this.props.fadeOut === false
              ? _(
                  "div",
                  {
                    className: "float-right",
                    style: {
                      height: "56px",
                      width: "56px",
                      background: "white",
                      padding: "0px",
                      borderRadius: "55px"
                    }
                  },
                  iwb.loaders.puff(56, 56, this.props.color2)
                )
              : _("i", {
                  className:
                    "float-right " + (this.props.node.icon || "icon-settings"),
                  style: {
                    fontSize: "30px",
                    background: "white",
                    padding: "13px",
                    borderRadius: "55px",
                    color: this.props.color2
                  }
                }),
            _("h1", { className: "mb-0" }, this.props.node.name),
            _("p", null, this.props.node.name + " ile ilgili işlemler")
          )
        )
      )
    );
  }
}

var dgColors3 = [
  "gray-700",
  "danger",
  "gray-500",
  "gray-400",
  "gray-700",
  "info",
  "secondary",
  "secondary",
  "secondary",
  "warning"
];
class XCardMiniMenu extends React.PureComponent {
  render() {
    return _(
      Col,
      { xs: "4", sm: "3", md: "2", lg: "2", xl: "1" },
      _(
        Link,
        { to: this.props.node.url },
        _(
          Card,
          {
            // url:this.props.node.url,onClick:(e)=>{console.log(this.props.history);console.log('this.props.router',this.props.router);this.props.history.push(this.props.node.url)},
            className: "card-mini-menu text-white bg-" + this.props.color,
            style: this.props.fadeOut
              ? { opacity: 0, transform: "scale(.9)" }
              : this.props.fadeOut === false
                ? { transform: "scale(1.1)" }
                : {}
          },
          _(
            CardBlock,
            { className: "pb-1", style: { textAlign: "center", padding: "0" } },
            _("i", {
              className: this.props.node.icon || "icon-settings",
              style: {
                fontSize: "28px",
                padding: "12px",
                color: "white"
              }
            })
          )
        )
      ),
      _("h6", { style: { textAlign: "center" } }, this.props.node.name)
    );
  }
}

class XMainNav extends React.PureComponent {
  constructor(props) {
    if (iwb.debugConstructor) console.log("XMainNav.constructor", props);
    super(props);
    this.onGlobalSearch = this.onGlobalSearch.bind(this);
    iwb.onGlobalSearch2 = this.onGlobalSearch;
    this.state = { xsearch: "" };
  }
  componentWillUnmount() {
    console.log("XMainNav.componentWillUnmount");
    iwb.killGlobalSearch();
  }
  componentDidUpdate() {
    console.log("XMainNav.componentDidUpdate");
    // if(this.state)this.setState({xsearch:''});
  }
  onGlobalSearch(v) {
    this.setState({ xsearch: v && v.target ? v.target.value : v });
  }
  render() {
    console.log("this.state.xsearch", this.state.xsearch);
    if (this.state.xsearch) {
      var nodes = iwb.nav.findNodes(this.state.xsearch.toLowerCase(), {
        name: "Home",
        children: iwb.nav.items
      });
      console.log("nodes", nodes);
      if (!nodes || !nodes.length) return "Not found :(";
      return _(
        "div",
        { className: "animated fadeIn" },
        _("div", { style: { height: "1.45rem" } }),
        "Search Results",
        _("hr", { style: { marginTop: "0.4rem" } }),
        _(
          Row,
          { style: { maxWidth: "1300px" } },
          nodes.map(function(o, qi) {
            return _(XCardMiniMenu, {
              color: dgColors3[qi % dgColors3.length],
              node: o
            });
          })
        )
      );
    }

    var path = this.props.path,
      node = this.props.node;
    var vi = false,
      siri = false;
    if (path == "/" || path == "/iwb-home") {
      (vi = []), (siri = []);
      var qi = 0,
        si = 0;
      for (var k in iwb.nav.visitedItems) {
        var o = iwb.nav.visitedItems[k];
        vi.push(
          _(XCardMiniMenu, { color: dgColors3[qi % dgColors3.length], node: o })
        );
        qi++;
        if (o.visitCnt > 2) {
          siri.push(
            _(XCardMiniMenu, {
              color: dgColors2[si % dgColors2.length],
              node: o
            })
          );
          si++;
        }
      }
      if (qi == 0) vi = false;
      else {
        vi = [
          _("div", { style: { height: "1.5rem" } }),
          "Açık Ekranlar",
          _("hr", { style: { marginTop: "0.4rem" } }),
          _(Row, { style: { maxWidth: "1300px" } }, vi)
        ];
        if (si > 0) {
          if (siri.length > 4) {
            siri.splice(4, 1000);
          }
          vi.push(
            _("div", { style: { height: "1.5rem" } }),
            "iWB Öneriler",
            _("hr", { style: { marginTop: "0.4rem" } }),
            _(Row, { style: { maxWidth: "1300px" } }, siri)
          );
        }
      }
    }

    return _(
      "div",
      { className: "animated fadeIn" },
      _("div", { style: { height: "1.45rem" } }),
      _(
        Row,
        { style: { maxWidth: "1300px" } },
        node.children.map(function(a, i) {
          return _(XCardMenu, {
            key: i,
            color: dgColors2[i % dgColors2.length],
            color2: detailSpinnerColors2[i % detailSpinnerColors2.length],
            color3: dBGColors2[i % dBGColors2.length],
            node: a
          });
        })
      ),
      vi
    );
  }
}

class XMainPanel extends React.PureComponent {
  constructor(props) {
    if (iwb.debugConstructor) console.log("XMainPanel.constructor", props);
    super(props);
    this.loadPage = this.loadPage.bind(this);
    this.state = { t: -1 };
  }
  loadPage() {
    var t = this.t;
    if (!iwb["t-" + t]) {
      fetch("showPage?_tid=" + t, {
        // body: JSON.stringify(cfg.params||{}), // must match 'Content-Type'
        // header
        cache: "no-cache", // *default, no-cache, reload, force-cache,
        // only-if-cached
        credentials: "same-origin", // include, same-origin, *omit
        headers: { "content-type": "application/json" },
        method: "POST", // *GET, POST, PUT, DELETE, etc.
        mode: "cors", // no-cors, cors, *same-origin
        redirect: "follow", // *manual, follow, error
        referrer: "no-referrer" // *client, no-referrer
      })
        .then(response => {
          // status "0" to handle local files fetching (e.g. Cordova/Phonegap
          // etc.)
          if (response.status === 200 || response.status === 0) {
            return response.text();
          } else {
            return Promise.reject(new Error(response.statusText));
          }
        })
        .then(
          result => {
            if (result) {
              var f;
              eval("f=function(callAttributes, parentCt){\n" + result + "\n}");
              var r = f(false, this);
              if (r) {
                r = _("div", { className: "animated fadeIn" }, r);
                iwb["t-" + t] = r; // r;
                this.setState({ t: t });
                iwb.nav.visitItem(this.props.match.path);
              }
            } else {
              toastr.error("No Result", " Error");
              // alert('Hata! Sonuc Gelmedi');
            }
          },
          error => {
            toastr.error(error, "Connection Error");
            // alert('ERROR! ' + error);
          }
        );
    } else if (t != this.state.t) this.setState({ t: t });
  }
  componentDidMount() {
    console.log("XMainPanel.componentDidMount", this.props.match.path);
    if (!this.l) this.loadPage();
  }
  componentDidUpdate() {
    console.log("XMainPanel.componentDidUpdate", this.props.match.path);
    if (!this.l) this.loadPage();
  }
  componentDidCatch() {
    console.log("XMainPanel.componentDidCatch", this);
  }
  componentWillUnmount() {
    console.log("XMainPanel.componentWillUnmount", this.props.match.path);
  }

  render() {
    var path = this.props.match.path;
    var children = { name: "Home", children: iwb.nav.items };
    var node =
      path == "/" || path == "/iwb-home"
        ? children
        : iwb.nav.findNode(this.props.match.path, children);
    console.log("XMainPanel:render:", path, node);
    if (node) {
      var ix = path.indexOf("showPage");
      if (ix > -1) {
        var t = 1 * path.substr(ix + "showPage".length);
        this.t = t;
        if (t != this.state.t) {
          if (this.l) {
            var ll = this.l;
            this.l = false;
            return _(
              "div",
              { className: "animated fadeIn" },
              _("div", { style: { height: "1.45rem" } }),
              _(
                Row,
                { style: { maxWidth: "1300px" } },
                ll.children.map(function(a, i) {
                  return _(XCardMenu, {
                    key: i,
                    color: dgColors2[i % dgColors2.length],
                    color2:
                      detailSpinnerColors2[i % detailSpinnerColors2.length],
                    color3: dBGColors2[i % dBGColors2.length],
                    node: a,
                    fadeOut: a.url != node.url
                  });
                })
              )
            );
          }
          return _(XLoading, null);
        }
        var r = iwb.nav.visitedItems[path];
        if (r) r.visitCnt++;
        return iwb["t-" + t]; // || null;
      } else {
        var d = document.getElementById("id-breed");
        if (d) d.innerHTML = node.name || "Home";
        this.l = node;
        return _(XMainNav, { path: path, node: node });
      }
    } else {
      this.l = false;
      return "ERROR! Wrong Page";
    }
  }
}

class XLoading extends React.Component {
  render() {
    return _(
      "span",
      { style: { position: "fixed", left: "48%", top: "45%" } },
      iwb.loading
    );
  }
}

iwb.requestErrorHandler = function(obj) {
  if (obj.errorType) {
    switch (obj.errorType) {
      case "session":
        return iwb.showLoginDialog();
      case "validation":
        toastr.error(obj.errors.join("<br/>"), "Validation Error");
        // alert('ERROR Validation: ' + obj.errors.join('\n'));
        break;
      default:
        top.ajaxErrorHandler(obj);
      // alert('ERROR['+obj.errorType+'] '+(obj.errorMsg || 'Bilinmeyen
      // ERROR'));
    }
  } else {
    // toastr.error(obj.errorMsg || "Unknown ERROR", "Request Error");
    // alert(obj.errorMsg || 'Bilinmeyen ERROR');
    top.ajaxErrorHandler(obj);
  }
};
var ajaxErrorHandler = iwb.requestErrorHandler;

iwb.request = function(cfg) {
  if (!window.fetch) {
    alert("ERROR! window.fetch not supported");
    return false;
  }
  if (!cfg || !cfg.url) {
    alert("ERROR! config missing");
    return false;
  }
  fetch(cfg.url, {
    body: JSON.stringify(cfg.params || {}), // must match 'Content-Type' header
    cache: "no-cache", // *default, no-cache, reload, force-cache,
    // only-if-cached
    credentials: "same-origin", // include, same-origin, *omit
    headers: {
      "content-type": "application/json"
    },
    method: "POST", // *GET, POST, PUT, DELETE, etc.
    mode: "cors", // no-cors, cors, *same-origin
    redirect: "follow", // *manual, follow, error
    referrer: "no-referrer" // *client, no-referrer
  })
    .then(function(response) {
      // status "0" to handle local files fetching (e.g. Cordova/Phonegap
      // etc.)
      if (response.status === 200 || response.status === 0) {
        return response.json();
      } else {
        return Promise.reject(
          new Error(response.text() || response.statusText)
        );
      }
    })
    .then(
      function(result) {
        if (cfg.callback && cfg.callback(result, cfg) === false) return;
        if (result.success) {
          if (cfg.successCallback) cfg.successCallback(result, cfg);
        } else {
          if (cfg.errorCallback && cfg.errorCallback(result, cfg) === false)
            return;
          iwb.requestErrorHandler(result);
        }
      },
      function(error) {
        if (
          cfg.errorCallback &&
          cfg.errorCallback({ error: error }, cfg) === false
        )
          return;
        toastr.error(error || "Unknown ERROR", "Request Error");
        // alert('ERROR! ' + error);
      }
    );
};

iwb.getFormValues = function(f) {
  if (!f || !f.elements) return {};
  var e = f.elements,
    p = {};
  for (var qi = 0; qi < e.length; qi++) {
    if (e[qi].name)
      switch (e[qi].type) {
        case "checkbox":
          p[e[qi].name] = e[qi].checked;
          break;
        case "hidden":
          p[e[qi].name] =
            p[e[qi].name] === undefined
              ? e[qi].value
              : p[e[qi].name] + "," + e[qi].value;
          break;
        default:
          p[e[qi].name] = e[qi].value;
      }
  }
  return p;
};

iwb.loadPage = function(cfg) {};

iwb.ui = {
  buildPanel: function(c) {
    if (!c.grid.pk) c.grid.pk = c.pk || c._pk;
    if (!c.grid.detailGrids) c.grid.detailGrids = c.detailGrids || false;
    return _(XPage, c);
  }
};

iwb.DelayedTask = function(fn, scope, args, cancelOnDelay, fireIdleEvent) {
  // @define Ext.util.DelayedTask
  // @uses Ext.GlobalEvents
  var me = this,
    delay,
    call = function() {
      me.id = null;

      if (!(scope && scope.destroyed)) {
        args ? fn.apply(scope, args) : fn.call(scope);
      }

      if (fireIdleEvent === false) {
        iwb._suppressIdle = true;
      }
    };

  // <debug>
  // DelayedTask can be called with no function upfront
  if (fn) {
    call.$origFn = fn.$origFn || fn;
    call.$skipTimerCheck = call.$origFn.$skipTimerCheck;
  }
  // </debug>

  cancelOnDelay = typeof cancelOnDelay === "boolean" ? cancelOnDelay : true;

  /**
   * @property {Number} id The id of the currently pending invocation. Will be
   *           set to `null` if there is no invocation pending.
   */
  me.id = null;

  /**
   * @method delay By default, cancels any pending timeout and queues a new
   *         one.
   *
   * If the `cancelOnDelay` parameter was specified as `false` in the
   * constructor, this does not cancel and reschedule, but just updates the
   * call settings, `newDelay`, `newFn`, `newScope` or `newArgs`, whichever
   * are passed.
   *
   * @param {Number}
   *            newDelay The milliseconds to delay. `-1` means schedule for
   *            the next animation frame if supported.
   * @param {Function}
   *            [newFn] Overrides function passed to constructor
   * @param {Object}
   *            [newScope] Overrides scope passed to constructor. Remember
   *            that if no scope is specified, `this` will refer to the
   *            browser window.
   * @param {Array}
   *            [newArgs] Overrides args passed to constructor
   * @return {Number} The timer id being used.
   */
  me.delay = function(newDelay, newFn, newScope, newArgs) {
    if (cancelOnDelay) {
      me.cancel();
    }

    if (typeof newDelay === "number") {
      delay = newDelay;
    }

    fn = newFn || fn;
    scope = newScope || scope;
    args = newArgs || args;
    me.delayTime = delay;

    // <debug>
    if (fn) {
      call.$origFn = fn.$origFn || fn;
      call.$skipTimerCheck = call.$origFn.$skipTimerCheck;
    }
    // </debug>

    if (!me.id) {
      if (delay === -1) {
        me.id = Ext.raf(call);
      } else {
        me.id = Ext.defer(call, delay || 1); // 0 == immediate call
      }
    }

    return me.id;
  };

  /**
   * Cancel the last queued timeout
   */
  me.cancel = function() {
    if (me.id) {
      if (me.delayTime === -1) {
        Ext.unraf(me.id);
      } else {
        Ext.undefer(me.id);
      }
      me.id = null;
    }
  };

  me.flush = function() {
    if (me.id) {
      me.cancel();

      // we're not running on our own timer so don't mess with whatever thread
      // is calling us...
      var was = fireIdleEvent;
      fireIdleEvent = true;

      call();

      fireIdleEvent = was;
    }
  };

  /**
   * @private Cancel the timeout if it was set for the specified fn and scope.
   */
  me.stop = function(stopFn, stopScope) {
    // This kludginess is here because Classic components use shared focus task
    // and we need to be sure the task's current timeout was set for that
    // particular component before we can safely cancel it.
    if (stopFn && stopFn === fn && (!stopScope || stopScope === scope)) {
      me.cancel();
    }
  };
};
class XForm extends React.Component {
  constructor(props) {
    super(props);
    // methods
    /**
     * sets the state with value of input
     *
     * @param {event}
     *            param0
     */
    this.onChange = ({ target }) => {
      var { values } = this.state;
      if (target) {
        values[target.name] =
          target.type == "checkbox" ? target.checked : target.value;
        this.setState({ values });
      }
    };

    /**
     * file upload function
     */
    this.onFileChange = () => (name, result, context) => {
      var values = this.state.values;
      var errors = this.state.errors;
      if (result.success) {
        values[name] = result.fileId;
        errors[name] = undefined;
      } else {
        errors[name] = result.error;
      }
      this.setState({
        errors,
        values
      });
    };
    /**
     * sets state for combo change else sets oprions of it after the request
     *
     * @param {String}
     *            inputName
     */
    this.onComboChange = inputName => {
      var self = this;
      return selectedOption => {
        var { values } = self.state;
        var slectedOption_Id = selectedOption && selectedOption.id;
        values[inputName] = slectedOption_Id;
        var triggers = self.triggerz4ComboRemotes;
        // used for remote @depricated
        if (triggers[inputName]) {
          triggers[inputName].map(zzz => {
            var nv = zzz.f(slectedOption_Id, null, values);
            var { options } = self.state;
            if (nv) {
              iwb.request({
                url:
                  "ajaxQueryData?" + iwb.JSON2URI(nv) + ".r=" + Math.random(),
                successCallback: function(res) {
                  options[zzz.n] = res.data;
                  self.setState({ options });
                }
              });
            } else {
              options[zzz.n] = [];
              self.setState({ options });
            }
          });
        }
        self.setState({ values });
      };
    };
    /**
     * sets state when low combo is entered
     *
     * @param {String}
     *            inputName
     */
    this.onLovComboChange = inputName => {
      var self = this;
      return selectedOptions => {
        var { values } = self.state;
        var slectedOption_Ids = [];
        if (selectedOptions) {
          selectedOptions.map(selectedOption => {
            slectedOption_Ids.push(selectedOption.id);
          });
        }
        values[inputName] = slectedOption_Ids.join(",");
        self.setState({ values });
      };
    };
    /**
     * sets state when number entered
     *
     * @param {String}
     *            dsc
     */
    this.onNumberChange = inputName => {
      var self = this;
      return inputEvent => {
        var { values } = self.state;
        var inputValue = inputEvent && inputEvent.value;
        values[inputName] = inputValue;
        self.setState({ values });
      };
    };
    /**
     * sends post to the server
     *
     * @param {Object}
     *            cfg
     */
    this.submit = cfg => {
      var values = { ...this.state.values };
      if (this.componentWillPost) {
        /**
         * componentWillPostResult = true || fase || {field_name : 'custom
         * value'}
         */
        var componentWillPostResult = this.componentWillPost(values, cfg || {});
        if (!componentWillPostResult) return false;
        values = { ...values, ...componentWillPostResult };
      }
      iwb.request({
        url:
          this.url +
          "?" +
          iwb.JSON2URI(this.params) +
          "_renderer=react16&.w=" +
          _webPageId +
          "&.r=" +
          Math.random(),
        params: values,
        self: this,
        errorCallback: json => {
          var errors = {};
          if (json.errorType)
            switch (json.errorType) {
              case "validation":
                toastr.error("Validation Errors");
                if (json.errors && json.errors.length) {
                  json.errors.map(oneError => {
                    errors[oneError.id] = oneError.msg;
                  });
                }
                if (json.error) {
                  iwb.showModal({
                    title: "ERROR",
                    footer: false,
                    color: "danger",
                    size: "sm",
                    body: json.error
                  });
                }
                break;
              default:
                top.ajaxErrorHandler(json);
            }
          else {
            top.ajaxErrorHandler(json);
          }
          this.setState({ errors });
          return false;
        },
        successCallback: (json, xcfg) => {
          if (cfg.callback) cfg.callback(json, xcfg);
        }
      });
    };
    /**
     * used to make form active tab and visible on the page
     *
     * @param {object}
     *            tab
     */
    this.toggleTab = tab => {
      if (this.state.activeTab !== tab) {
        this.setState({ activeTab: tab });
      }
    };
    /**
     * returns form data from state
     */
    this.getValues = () => {
      return { ...this.state.values };
    };
    /**
     * used for date inputs
     *
     * @param {String}
     *            inputName
     * @param {Boolean}
     *            isItDTTM
     */
    this.onDateChange = (inputName, isItDTTM) => {
      var self = this;
      return selectedDate => {
        var { values } = self.state;
        var dateValue = selectedDate && selectedDate._d;
        values[inputName] = isItDTTM
          ? fmtDateTime(dateValue)
          : fmtShortDate(dateValue);
        self.setState({ values });
      };
    };
  }
  componentDidMount() {
    var self = this;
    var triggers = this.triggerz4ComboRemotes;
    var { values } = this.state;
    for (var trigger in triggers) {
      if (values[trigger]) {
        triggers[trigger].map(zzz => {
          var nv = zzz.f(values[trigger], null, values);
          if (nv)
            iwb.request({
              url: "ajaxQueryData?" + iwb.JSON2URI(nv) + ".r=" + Math.random(),
              successCallback: function(result) {
                var { options } = self.state;
                options[zzz.n] = result.data;
                self.setState({ options });
              }
            });
        });
      }
    }
  }
  componentWillUnmount() {
    iwb.forms[this._id] = { ...this.state };
  }
}

class XGraph extends React.Component {
  constructor(props) {
    super(props);
  }
  componentDidMount() {
    var dg = this.props.graph;
    var gid = "idG" + dg.graphId;
    parent.iwb.graphAmchart(dg, gid);
  }
  render() {
    return _("div", {
      style: { width: "100%", height: this.props.props.height || "20vw" },
      id: "idG" + this.props.graph.graphId
    });
  }
}

iwb.createPortlet = function(o) {
  var name = o.graph || o.grid || o.card || o.query;
  if (!name) return _("div", null, "not portlet");
  if (o.query) {
    var q = o.query.data;
    if (!q || !q.length) return _("div", null, "not data");
    q = q[0];
    return _(
      Card,
      {
        className: "card-portlet text-white bg-" + (o.props.color || "primary")
      },
      _("i", { className: "big-icon " + (q.icon || "icon-settings") }),
      _(
        CardBlock,
        { className: "pb-0" },
        _(
          "div",
          {
            className: "float-right",
            style: {
              fontSize: "30px",
              background: "white",
              padding: "0 13px",
              borderRadius: "55px",
              color: "darkorange"
            }
          },
          q.xvalue
        ),
        _("h1", { className: "mb-0" }, q.dsc),
        _("div", { style: { height: "25px" } })
      )
    );
  }
  name = name.name;
  var cmp = null;
  if (o.graph) {
    return _(
      Card,
      {
        className:
          "card-portlet " + (o.props.color ? "bg-" + o.props.color : "")
      },
      _(
        "h3",
        {
          className: "form-header",
          style: {
            fontSize: "1.5rem",
            padding: "10px 12px 0px",
            marginBottom: ".5rem"
          }
        },
        name,
        _("i", { className: "portlet-refresh float-right icon-refresh" })
      ),
      _(XGraph, o)
    );
  } else if (o.grid) {
    o.grid.crudFlags = false;
    return _(
      Card,
      {
        className:
          "card-portlet " + (o.props.color ? "bg-" + o.props.color : "")
      },
      _(
        "h3",
        {
          className: "form-header",
          style: {
            fontSize: "1.5rem",
            padding: "10px 12px 0px",
            marginBottom: ".5rem"
          }
        },
        name,
        _("i", { className: "portlet-refresh float-right icon-refresh" })
      ),
      _(XGrid, o.grid)
    );
  } else if (o.card) cmp = "Card";
  else if (o.query) cmp = "KPI Card";
  return _(
    Card,
    {
      className: "card-portlet text-white bg-" + o.props.color || "primary"
    },
    _(
      CardBlock,
      {},
      _(
        "h3",
        {
          className: "form-header",
          style: { padding: "10px 12px 0px", marginBottom: ".5rem" }
        },
        name
      ),
      _("hr"),
      cmp
    )
  );
};

iwb.ui.buildDashboard = function(o) {
  if (!o || !o.rows || !o.rows.length)
    return _("div", null, "No portlets defined");
  return o.rows.map((rowItem, rowIndex) => {
    return _(Row, {
      key: rowIndex,
      children: rowItem.map((colItem, colIndex) =>
        _(Col, colItem.props, iwb.createPortlet(colItem))
      )
    });
  });
};

iwb.ajax = {};
iwb.ajax.query = function(qid, params, callback) {
  iwb.request({
    url: "ajaxQueryData?_qid=" + qid,
    params: params || {},
    successCallback: callback || false
  });
};
iwb.ajax.postForm = function(fid, action, params, callback) {
  iwb.request({
    url: "ajaxPostForm?_fid=" + fid + "&a=" + action,
    params: params || {},
    successCallback: callback || false
  });
};
iwb.ajax.execFunc = function(did, params, callback) {
  iwb.request({
    url: "ajaxExecDbFunc?_did=" + did,
    params: params || {},
    successCallback: callback || false
  });
};
function fieldFileAttachment(row, cell) {
  return row[cell] && 1 * row[cell]
    ? _(
        "a",
        {
          href: "dl/" + row[cell + "_qw_"] + "?_fai=" + row[cell],
          target: "_blank"
        },
        row[cell + "_qw_"]
      )
    : null;
}

class FileInput extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      canUpload: false,
      dragOver: false,
      file: null,
      fileUrl: props.fileId
        ? "dl/" +
          props.fileName +
          "?_fai=" +
          props.fileId +
          "&.r=" +
          Math.random()
        : null,
      fileName: props.fileName || null
    };
    this.onDrop = this.onDrop.bind(this);
    this.dragenter = this.dragenter.bind(this);
    this.dragleave = this.dragleave.bind(this);
    this.dragover = this.dragover.bind(this);
    this.onclick = this.onclick.bind(this);
    this.onchange = this.onchange.bind(this);
    this.uplaodFile = this.uplaodFile.bind(this);
    /** */
    this.downladLink = url => e => {
      e.preventDefault();
      e.stopPropagation();

      let link = document.createElement("a");
      link.href = url;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    };
  }
  componentWillMount() {}
  /** function to click input ref click */
  onclick(event) {
    event.preventDefault();
    event.stopPropagation();
    this.inpuRef.click();
  }
  /** used to disable opening file on new tab */
  dragover(event) {
    event.preventDefault();
    event.stopPropagation();
  }
  /** used with css */
  dragleave(event) {
    event.preventDefault();
    event.stopPropagation();
    this.setState({
      dragOver: false
    });
  }
  /** when the file over drag area */
  dragenter(event) {
    event.preventDefault();
    event.stopPropagation();
    this.setState({
      dragOver: true
    });
  }
  /** when the file dproped over drop area */
  onDrop(event) {
    event.preventDefault();
    event.stopPropagation();
    this.setState(
      {
        canUpload: true,
        dragOver: false,
        file: event.dataTransfer.files[0]
      },
      () => {
        this.uplaodFile();
      }
    );
  }
  /** when the file dproped over drop area */
  onchange(event) {
    event.preventDefault();
    event.stopPropagation();
    this.setState(
      {
        canUpload: true,
        dragOver: false,
        file: event.target.files[0]
      },
      () => {
        this.uplaodFile();
      }
    );
  }
  /** uploader function */
  uplaodFile() {
    if (!this.state.file) {
      return;
    }
    let formData = new FormData();
    formData.append(
      "table_pk",
      this.props.cfg.tmpId ? this.props.cfg.tmpId : json2pk(this.props.cfg.pk)
    );
    formData.append("table_id", this.props.cfg.crudTableId);
    formData.append("file", this.state.file);
    formData.append("profilePictureFlag", this.props.profilePictureFlag || 0);
    fetch("upload.form", {
      method: "POST",
      body: formData,
      cache: "no-cache",
      credentials: "same-origin",
      mode: "cors",
      redirect: "follow",
      referrer: "no-referrer"
    })
      .then(
        response =>
          response.status === 200 || response.status === 0
            ? response.json()
            : Promise.reject(new Error(response.text() || response.statusText))
      )
      .then(
        result => {
          if (result.success) {
            toastr.success(
              getLocMsg("file_sucessfully_uploaded!"),
              getLocMsg("Success"),
              {
                timeOut: 3000
              }
            );
            this.setState({
              canUpload: false,
              file: null,
              fileName: result.fileName,
              fileUrl: result.fileUrl
            });
          } else {
            if (result.error) {
              toastr.error(result.error, result.errorType);
            }
          }
          this.props.onFileChange &&
            this.props.onFileChange(this.props.name, result, this);
          return;
        },
        error => {
          toastr.error(error, getLocMsg("Error"));
        }
      );
  }
  render() {
    let defaultStyle = {
      height: "100%",
      width: "100%",
      position: "absolute",
      top: "0",
      left: "0"
    };
    return _(
      React.Fragment,
      {},
      _(
        "div",
        null,
        // this.state.file ? getLocMsg(this.state.file.name) :
        // getLocMsg('File Upload'),
        _("input", {
          className: "d-none",
          type: "file",
          onChange: this.onchange,
          ref: input => (this.inpuRef = input)
        }),
        this.props.extraButtons && this.props.extraButtons
      ),
      _(
        "div",
        {},
        _(
          "div",
          {
            className: "mx-auto",
            style: {
              height: "200px",
              width: "200px",
              position: "relative",
              border: this.state.dragOver
                ? "3px dashed #20a8d8"
                : "3px dashed #a4b7c1"
            }
          },
          _("div", {
            style: {
              ...defaultStyle,
              zIndex: "10",
              background: "gray",
              cursor: "pointer",
              opacity: this.state.canUpload ? "0" : "0.5"
            },
            className: "rounded",
            onDrop: this.onDrop,
            onDragEnter: this.dragenter,
            onDragLeave: this.dragleave,
            onDragOver: this.dragover,
            onClick: this.onclick
          }),
          _(
            "div",
            {
              style: {
                ...defaultStyle,
                display: "flex"
              }
            },
            _(XPreviewFile, {
              file: this.state.file
            })
          )
        ),
        _("div", {
          className: "clearfix"
        }),
        _(
          ListGroup,
          {},
          this.state.fileUrl &&
            _(
              ListGroupItem,
              null,
              _(
                "a",
                {
                  onClick: this.downladLink(this.state.fileUrl),
                  href: "#"
                },
                this.state.fileName
              )
            )
        )
      )
    );
  }
}
const XPreviewFile = ({ file }) => {
  let type = file ? file.type : null;
  let style = {
    fontSize: "12em"
  };
  switch (type) {
    case "image/png":
      return _("img", {
        src: URL.createObjectURL(file),
        className: "img-fluid rounded"
      });
    case "text/plain":
      return _("i", {
        style,
        className: "fas fa-file-alt m-auto"
      });
    case "application/pdf":
      return _("i", {
        style,
        className: "fas fa-file-pdf m-auto"
      });
    default:
      return _(
        "div",
        { className: "m-auto text-center" },
        file
          ? _("i", { className: "far fa-file", style })
          : _("i", { className: "fas fa-upload", style }),
        _("br", null),
        getLocMsg(file ? "undefined_type" : "choose_file_or_drag_it_here")
      );
  }
};

class XListFiles extends React.Component {
  constructor() {
    super();
    this.state = {
      files: []
    };
    this.getFileList = this.getFileList.bind(this);
    this.deleteItem = this.deleteItem.bind(this);
    this.downladLink = this.downladLink.bind(this);
  }
  /** run query to get data based on pk and id */
  getFileList() {
    iwb.request({
      url:
        "ajaxQueryData?_qid=61&xtable_id=" +
        this.props.cfg.crudTableId +
        "&xtable_pk=" +
        (this.props.cfg.tmpId
          ? this.props.cfg.tmpId
          : json2pk(this.props.cfg.pk)) +
        "&.r=" +
        Math.random(),
      successCallback: ({ data }) => {
        this.setState({
          files: data
        });
      }
    });
  }
  deleteItem(fileItem) {
    return event => {
      event.preventDefault();
      event.stopPropagation();
      /** deleteRequest */
      iwb.request({
        url:
          "ajaxPostForm?a=3&_fid=1383&tfile_attachment_id=" +
          fileItem.file_attachment_id,
        successCallback: res => {
          this.setState({
            files: this.state.files.filter(
              file => file.file_attachment_id != fileItem.file_attachment_id
            )
          });
        }
      });
    };
  }
  /** test */
  downladLink(fileItem) {
    let url =
      "dl/" +
      fileItem.original_file_name +
      "?_fai=" +
      fileItem.file_attachment_id +
      "&.r=" +
      Math.random();
    return event => {
      event.preventDefault();
      event.stopPropagation();
      const link = document.createElement("a");
      link.href = url;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    };
  }
  componentDidMount() {
    this.getFileList();
  }
  render() {
    return _(
      ListGroup,
      {},
      this.state.files.map(fileItem =>
        _(
          ListGroupItem,
          null,
          _(
            "a",
            { onClick: this.downladLink(fileItem), href: "#" },
            fileItem.original_file_name
          ),
          _("i", {
            key: fileItem.file_attachment_id,
            onClick: this.deleteItem(fileItem),
            style: { cursor: "pointer" },
            className: "icon-trash float-right text-danger"
          })
        )
      )
    );
  }
}
class XSingleUploadComponent extends React.Component {
  constructor() {
    super();
    this.state = {
      canUpload: false,
      dragOver: false,
      file: null
    };
    this.xListFilesRef = React.createRef();
    this.onDrop = this.onDrop.bind(this);
    this.dragenter = this.dragenter.bind(this);
    this.dragleave = this.dragleave.bind(this);
    this.dragover = this.dragover.bind(this);
    this.onDeleteFile = this.onDeleteFile.bind(this);
    this.onclick = this.onclick.bind(this);
    this.onchange = this.onchange.bind(this);
    this.uplaodFile = this.uplaodFile.bind(this);
  }
  /** function to click input ref click */
  onclick(event) {
    event.preventDefault();
    event.stopPropagation();
    this.inpuRef.click();
  }
  /** used to disable opening file on new tab */
  dragover(event) {
    event.preventDefault();
    event.stopPropagation();
  }
  /** used with css */
  dragleave(event) {
    event.preventDefault();
    event.stopPropagation();
    this.setState({
      dragOver: false
    });
  }
  /** when the file over drag area */
  dragenter(event) {
    event.preventDefault();
    event.stopPropagation();
    this.setState({
      dragOver: true
    });
  }
  /** when the file dproped over drop area */
  onDrop(event) {
    event.preventDefault();
    event.stopPropagation();
    this.setState(
      {
        canUpload: true,
        dragOver: false,
        file: event.dataTransfer.files[0]
      },
      () => {
        this.uplaodFile();
      }
    );
  }
  /** when the file dproped over drop area */
  onchange(event) {
    event.preventDefault();
    event.stopPropagation();
    this.setState(
      {
        canUpload: true,
        dragOver: false,
        file: event.target.files[0]
      },
      () => {
        this.uplaodFile();
      }
    );
  }
  /** remove file from form state */
  onDeleteFile(event) {
    event.preventDefault();
    event.stopPropagation();
    /** will reset to null currently uploaded file */
    this.setState({
      canUpload: false,
      file: null
    });
  }
  /** uploader function */
  uplaodFile() {
    // event.preventDefault();
    // event.stopPropagation();
    if (!this.state.file) {
      return;
    }
    let formData = new FormData();
    formData.append(
      "table_pk",
      this.props.cfg.tmpId ? this.props.cfg.tmpId : json2pk(this.props.cfg.pk)
    );
    formData.append("table_id", this.props.cfg.crudTableId);
    formData.append("file", this.state.file);
    formData.append("profilePictureFlag", this.props.profilePictureFlag || 0);
    fetch("upload.form", {
      method: "POST",
      body: formData,
      cache: "no-cache",
      credentials: "same-origin",
      mode: "cors",
      redirect: "follow",
      referrer: "no-referrer"
    })
      .then(
        response =>
          response.status === 200 || response.status === 0
            ? response.json()
            : Promise.reject(new Error(response.text() || response.statusText))
      )
      .then(
        result => {
          if (result.success) {
            toastr.success(
              getLocMsg("file_sucessfully_uploaded!"),
              getLocMsg("Success"),
              {
                timeOut: 3000
              }
            );
            this.xListFilesRef.current.getFileList();
            this.setState({
              file: null,
              canUpload: false
            });
          } else {
            if (result.error) {
              toastr.error(result.error, result.errorType);
            }
            return;
          }
        },
        error => {
          toastr.error(error, getLocMsg("Error"));
        }
      );
  }
  render() {
    let defaultStyle = {
      height: "100%",
      width: "100%",
      position: "absolute",
      top: "0",
      left: "0"
    };
    return _(
      React.Fragment,
      {},
      _(
        Button,
        {
          id: this.props.cfg.id,
          type: "button",
          className: "float-right btn-round-shadow mr-1",
          color: "light"
        },
        _("i", {
          className: "icon-paper-clip"
        }),
        this.props.cfg.fileAttachCount
          ? " " + this.props.cfg.fileAttachCount
          : ""
      ),
      _(
        Reactstrap.UncontrolledPopover,
        {
          trigger: "legacy",
          placement: "auto",
          target: this.props.cfg.id
        },
        _(
          PopoverHeader,
          null,
          this.state.file
            ? getLocMsg(this.state.file.name)
            : getLocMsg("File Upload"),
          _("input", {
            className: "d-none",
            type: "file",
            onChange: this.onchange,
            ref: input => (this.inpuRef = input)
          }),
          this.props.extraButtons && this.props.extraButtons
        ),
        _(
          PopoverBody,
          null,
          _(
            "div",
            {
              style: {
                height: "200px",
                width: "200px",
                position: "relative",
                border: this.state.dragOver
                  ? "3px dashed #20a8d8"
                  : "3px dashed #a4b7c1"
              }
            },
            _("div", {
              style: {
                ...defaultStyle,
                zIndex: "10",
                background: "gray",
                cursor: "pointer",
                opacity: this.state.canUpload ? "0" : "0.5"
              },
              className: "rounded",
              onDrop: this.onDrop,
              onDragEnter: this.dragenter,
              onDragLeave: this.dragleave,
              onDragOver: this.dragover,
              onClick: this.onclick
            }),
            _(
              "div",
              {
                style: {
                  ...defaultStyle,
                  display: "flex"
                }
              },
              _(XPreviewFile, {
                file: this.state.file
              })
            )
          ),
          _("div", {
            className: "clearfix"
          }),
          _(XListFiles, { cfg: this.props.cfg, ref: this.xListFilesRef })
        )
      )
    );
  }
}
function json2pk(j) {
  for (var key in j)
    if (key != "customizationId" && key != "projectId") return j[key];
  alert("Error: PK not found");
  return false;
}
iwb.fmtFileSize = a => {
  if (!a) return "-";
  a *= 1;
  var d = "B";
  if (a > 1024) {
    a = a / 1024;
    d = "KB";
  }
  if (a > 1024) {
    a = a / 1024;
    d = "MB";
  }
  if (a > 1024) {
    a = a / 1024;
    d = "GB";
  }
  if (d != "B") a = Math.round(a * 10) / 10;
  return a + " " + d;
};

function commentHtml(row, cell){
    return row[cell] && 1 * row[cell] ?
            _("i", { className: "icon-bubble" }) :
            null;
}


class CheckboxGroup extends React.Component {
    constructor(props) {
        super(props);
    }
    componentDidMount() {
    }
    render() {
    	var valMap={}
    	if(typeof this.props.value!='undefined'){
	    	if(!this.props.multi){
	    		valMap[this.props.value]=!0;
	    	} else {
	    		this.props.value.map((o)=>valMap[o]=!0);
	    	}
    	}
        return _("div", {
            style: {padding:5},
            
        }, this.props.options.map((o) => {
	        	return _('div',{}
	            	,_('input',{type:this.props.multi?'checkbox':'radio', checked:valMap[o[this.props.valueKey]], onClick:(aq)=>{
	            		this.props.onChange({id:aq.target.value});
	            	},
	            			name:this.props.name, value:o[this.props.valueKey]})
		        	,' ', _('label',{style:{fontWeight:400,color:'#333'}}, o[this.props.labelKey]));
        		}
        	)
        );
    }
}


iwb.hasPartInside=function(all,sub){
	if(typeof all=='undefined')return false;
	if((''+all).length==0)return false;
	if((','+all+',').indexOf(','+sub+',')==-1)return false;
	return true;
}
iwb.safeEquals= function(v1, v2){
	if(v1==='' || v1===false || (typeof v1=='undefined')){
		return (v2==='' || (typeof v2=='undefined'));
	} else if(v2==='' || (typeof v2=='undefined'))return false;
	return v1==v2;
}

iwb.formElementProperty = function(opr, elementValue, value){
	switch(1*opr){
	case -1://is Empty
		return elementValue==='' || elementValue===null || (typeof elementValue=='undefined');
	case -2://is not empty
		return !(elementValue==='' || elementValue===null || (typeof elementValue=='undefined'));
	case	8://in
		if(value==='' || (typeof value=='undefined'))return false;
		return iwb.hasPartInside(value, elementValue);
	case	9://not in
		if(value==='' || (typeof value=='undefined'))return true;
		return !iwb.hasPartInside(value, elementValue);
	case	0://equals
		return iwb.safeEquals(elementValue, value);
	case	1://not equals
		return !iwb.safeEquals(elementValue, value);
		
	}
	return false;
	
}
iwb.hideColumn= function(columns,name){
	columns.map(o => {
		if(o.name == name)o.hidden=true; 
	})
}

iwb.postSurveyJs=(formId, action, params, surveyData, masterParams)=>{
//	console.log(params)
	var params2 = {_mask:!0}, fid = 0;
	if(masterParams)for(var kk in masterParams)params2[kk] = masterParams[kk];
	for(var k in params){
		var o = params[k];
		if(k.startsWith('_form_')){
			fid++;
			params2['_fid'+fid] = k.substr('_form_'.length);
			if(action==2 || !surveyData[k] || !surveyData[k].length){
				params2['_cnt'+fid] = o.length;
				for(var qi=0;qi<o.length;qi++){
					var cell = o[qi];
					params2['a'+fid+'.'+(qi+1)] = 2;
					for(var kk in cell){
						params2[kk+fid+'.'+(qi+1)] = cell[kk];
					}
					if(masterParams)for(var kk in masterParams)
						params2[kk.substr(1)+fid+'.'+(qi+1)] = masterParams[kk];
				}			
			} else {//update
				var s = surveyData[k], cnt = 0, pkFieldName='';
				var sm = {}
				s.map(scell => {
					for(var sk in scell)if(sk.startsWith('_id_')){
						sm[scell[sk]]=scell;
						pkFieldName = sk.substr('_id_'.length);
					}
				});
				
				for(var qi=0;qi<o.length;qi++){//for each fresh data
					var cell = o[qi];
					cnt++;
					params2['a'+fid+'.'+cnt] = 2;
					for(var kk in cell)if(kk.startsWith('_id_')){
						params2['a'+fid+'.'+cnt] = 1;
						params2['t'+pkFieldName+fid+'.'+cnt] = cell[kk];
						delete sm[cell[kk]];
					} else {
						params2[kk+fid+'.'+(qi+1)] = cell[kk];
					}
					if(masterParams)for(var kk in masterParams)
						params2[kk.substr(1)+fid+'.'+cnt] = masterParams[kk];
				}
				for(var sk in sm){
					cnt++;
					params2['a'+fid+'.'+cnt] = 3;
					params2['t'+pkFieldName+fid+'.'+cnt] = sk;
				}
				if(cnt)
					params2['_cnt'+fid] = cnt;
				else {
					delete params2['_fid'+fid];
					fid--;
				}
			}
		} else {
			if(o && Array.isArray(o)){
				if(o.length && o[0].content){
					params2[k] = o[0].content.substr(o[0].content.lastIndexOf('=')+1);
				} else 
					params2[k] = o.join(','); 
					
			} else 
				params2[k] =  o;
		}
	}
	if(action==1)for(var k in surveyData)if(k.startsWith('_form_') && surveyData[k] && surveyData[k].length && 
			(!params[k] || !params[k].length)){
		var o = surveyData[k];
		fid++;
		params2['_fid'+fid] = k.substr('_form_'.length);
		params2['_cnt'+fid] = o.length;
		for(var qi=0;qi<o.length;qi++){
			var cell = o[qi];
			params2['a'+fid+'.'+(qi+1)] = 3;
			for(var kk in cell)if(kk.startsWith('_id_')){
				params2['t'+kk.substr('_id_'.length)+fid+'.'+(qi+1)] = cell[kk];
			}
		}
	}
	iwb.ajax.postForm(formId, action, params2, ()=>{
		toastr.success(
            "",
            "Saved Successfully", {
                timeOut: 3000
            }
        );
		iwb.closeTab({}, !0);
	})
}

iwb.fileUploadSurveyJs=(tableId, tablePk, survey, options)=>{
	debugger
	console.log(options)
	var formData = new FormData();
    options
        .files
        .forEach(function (file) {
            formData.append("file", file);
            formData.append("table_id", tableId);
            formData.append("table_pk", tablePk);
            formData.append("profilePictureFlag", 0);
        });
    var xhr = new XMLHttpRequest();
    xhr.responseType = "json";
    xhr.open("POST", "upload.form"); // https://surveyjs.io/api/MySurveys/uploadFiles
    xhr.onload = function () {
        var data = xhr.response;
        options.callback("success", options.files.map(file => {
            return {
                file: file,
                content: data.fileUrl
            };
        }));
    };
    xhr.send(formData);
}

function gcx(w, h, r) {
  var l = (screen.width - w) / 2;
  var t = (screen.height - h) / 2;
  r = r ? 1 : 0;
  return (
    "toolbar=0,scrollbars=0,location=0,status=1,menubar=0,resizable=" +
    r +
    ",width=" +
    w +
    ",height=" +
    h +
    ",left=" +
    l +
    ",top=" +
    t
  );
}

function openPopup(url, name, x, y, r) {
  var wh = window.open(url, name, gcx(x, y, r));
  if (!wh) toastr.error(getLocMsg("remove_popup_blocker"));
  else wh.focus();
  return false;
}

iwb.findAsyncValue = function(value, options){
	if(!value || !options || !options.length)return '';
	for(var qi=0;qi<options.length;qi++)if(options[qi].id==value)return options[qi];
	return '';
	
}