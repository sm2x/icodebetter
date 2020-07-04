if (typeof iwb == "undefined") iwb = {};
if (!iwb.ui) iwb.ui = {};
if (typeof _localeMsg == "undefined") _localeMsg = {};

function getLocMsg(key) {
  if (key == null) return "";
  var val = _localeMsg[key];
  return val || key;
}

function objProp(o) {
  var t = "";
  for (var q in o)
    t +=
      o[q] instanceof Function
        ? q + " = function{}\n"
        : q + " = " + o[q] + "\n";
  return t;
}

function obj2ArrayString(o, ml) {
  if (!ml) ml = 0;
  if (ml > 3) return "";
  var t = "[";
  var b = false;
  for (var qi = 0; qi < o.length; qi++) {
    var n = o[qi];
    if (n && !(n instanceof Function)) {
      if (b) t += ",\n";
      else b = true;
      switch (typeof n) {
        case "object":
          if (n instanceof Array) {
            t += obj2ArrayString(n, ml + 1);
          } else if (n instanceof Date) {
            t += '"' + fmtDateTime(n) + '"';
          } else {
            t += obj2JsonString(n, ml + 1);
          }
          break;
        case "string":
          t += '"' + n + '"';
          break;
        default:
          t += n;
          break;
      }
    }
  }
  t += "]";
  return t;
}

function obj2JsonString(o, ml) {
  if (!ml) ml = 0;
  if (ml > 3) return "";

  var t = "{";
  var b = false;
  for (var q in o) {
    var n = o[q];
    if (n && !(n instanceof Function)) {
      if (b) t += ",";
      else b = true;
      switch (typeof n) {
        case "object":
          if (n instanceof Array) {
            t += '"' + q + '" : "' + obj2ArrayString(n, ml + 1);
          } else if (n instanceof Date) {
            t += '"' + q + '" :"' + fmtDateTime(n) + '"';
          } else {
            t += '"' + q + '":' + obj2JsonString(n, ml + 1);
          }
          break;
        case "string":
          t += '"' + q + '":"' + n + '"';
          break;
        default:
          t += '"' + q + '" : ' + n;
          break;
      }
    }
  }
  t += "}";
  return t;
}

function gridStore2JsonString(ds) {
  var items = ds.data.items;
  var newItems = [];
  for (var qi = 0; qi < items.length; qi++)
    newItems.push({ id: items[qi].id, data: items[qi].json });
  return obj2ArrayString(newItems);
}

function promisLoadException(a, b, c) {
  if (c && c.responseText) {
    ajaxErrorHandler(JSON.parse(c.responseText)); // eval("(" + c.responseText
													// + ")")
  } else {
    // Ext.Msg.show({title:getLocMsg('js_info'),msg:
	// getLocMsg('js_no_connection_error'),icon: Ext.MessageBox.WARNING})
    showStatusText(getLocMsg("connection_error"), 3); // error
  }
}

function hideStatusText() {
  return;
  var c1 = Ext.getCmp("idSouthBox");
  c1._level = 0;
  if (c1.isVisible()) {
    c1.hide();
    mainViewport.doLayout();
    var c2 = Ext.get("footer2").dom;
    c2.className = c2._oldClassName;
  }
}

function showStatusText(txt, level) {
  return;
  // 0:debug, 1:info, 2:warning, 3:error
  var c1 = Ext.getCmp("idSouthBox");
  if (!level) level = 0;
  if (!c1._level) c1._level = 0;

  if (!c1.isVisible()) {
    var c2 = Ext.get("footer2").dom;
    c2._oldClassName = c2.className;
    c1.show();
    mainViewport.doLayout();
    c2._newClassName = c2.className;
  }
  if (level >= c1._level) {
    var c2 = Ext.get("footer2").dom,
      t2 = "";
    switch (level) {
      case 1:
        t2 = "information";
        break;
      case 2:
        t2 = "warning";
        break;
      case 3:
        t2 = "error";
        break;
      default:
        t2 = "";
        break;
    }
    c2.className = c2._newClassName + " " + t2;
    c2.textContent = txt;
    c1._level = level;
  }
}

function getScreenSize() {
  var myWidth = 0,
    myHeight = 0;
  if (typeof window.innerWidth == "number") {
    // Non-IE
    myWidth = window.innerWidth;
    myHeight = window.innerHeight;
  } else if (
    document.documentElement &&
    (document.documentElement.clientWidth ||
      document.documentElement.clientHeight)
  ) {
    // IE 6+ in 'standards compliant mode'
    myWidth = document.documentElement.clientWidth;
    myHeight = document.documentElement.clientHeight;
  } else if (
    document.body &&
    (document.body.clientWidth || document.body.clientHeight)
  ) {
    // IE 4 compatible
    myWidth = document.body.clientWidth;
    myHeight = document.body.clientHeight;
  }
  return { width: myWidth, height: myHeight };
}

function getCookie(c_name) {
  if (document.cookie.length > 0) {
    c_start = document.cookie.indexOf(c_name + "=");
    if (c_start != -1) {
      c_start = c_start + c_name.length + 1;
      c_end = document.cookie.indexOf(";", c_start);
      if (c_end == -1) c_end = document.cookie.length;
      return unescape(document.cookie.substring(c_start, c_end));
    }
  }
  return "";
}

function disabledCheckBoxHtml(x) {
  return x != 0
    ? '<img src="/ext3.4.1/custom/images/checked.png" border=0>'
    : "";
}

function accessControlHtml(x) {
  return x ? '<img src="/images/custom/bullet_key.png" border=0>' : "";
}

function fileAttachmentHtml(x) {
  return x
    ? '<div class="ifile_attach"> &nbsp;</div>'
    : "";
}
function fieldFileAttachment(x) {
	  return x
	    ? '<img src="/images/custom/bullet_file_attach.png" border=0>'
	    : "";
}

function fileAttachmentRenderer(a) {
  return function(ax, bx, cx) {
    return ax
      ? '<div style="background-position-x:center" border=0 onclick="mainPanel.loadTab({attributes:{modalWindow:true, _title_:\'' +
          a.name +
          "',href:'showPage?_tid="+(_scd.customFile ? 6813:9)+"&_gid458_a=1',baseParams:{xtable_id:" +
          a.crudTableId +
          ", xtable_pk:" +
          cx.id +
          '}}});"> <span class="file-count-badge">'+ax+'</span></div>'
      : "";
  };
}

function gridGraphMarkerRenderer(a) {
  return function(ax, bx, cx) {
    if (a.gcmm && a.gcmi) {
      var q = cx.get(a.gcmi);
      if (q) {
        q = a.gcmm[q];
        if (q) bx.attr = 'style="background-color:' + q + ';"';
      }
    } // else bx.attr='style="background-color:blue;"';
    return "";
  };
}

function commentRenderer(a) {
  return function(ax, bx, cx) {
    if (ax) {
      if (cx.data.pkpkpk_cf_ext) {
        var axx = cx.data.pkpkpk_cf_ext;
        bx.attr +=
          ' ext:qtip=" <b>' +
          axx.user_dsc +
          "</b>: " +
          Ext.util.Format.htmlEncode(axx.msg) +
          "<br/><span class=cfeed> · " +
          Ext.util.Format.htmlEncode(axx.last_dttm) +
          '</span>"';
        return (
          '<img src="../images/custom/bullet_comment' +
          (axx.is_new ? "_new" : "") +
          '.png" border=0 onclick="mainPanel.loadTab({attributes:{modalWindow:true, _title_:\'' +
          a.name +
          "',href:'showPage?_tid=836',slideIn:'t',_pk:{tcomment_id:'comment_id'},baseParams:{xtable_id:" +
          a.crudTableId +
          ", xtable_pk:" +
          cx.id +
          '}}});">'
        );
      } else
        return (
          '<img src="../images/custom/bullet_comment.png" border=0 onclick="mainPanel.loadTab({attributes:{modalWindow:true, _title_:\'' +
          a.name +
          "',href:'showPage?_tid=836',slideIn:'t',_pk:{tcomment_id:'comment_id'},baseParams:{xtable_id:" +
          a.crudTableId +
          ", xtable_pk:" +
          cx.id +
          '}}});">'
        );
    } else return "";
  };
}

function commentRenderer2(a) {
  return function(ax, bx, cx) {
    if (ax) {
      if (cx.data.pkpkpk_cf_ext) {
        var axx = cx.data.pkpkpk_cf_ext;
        bx.attr +=
          ' ext:qtip=" <b>' +
          axx.user_dsc +
          "</b>: " +
          Ext.util.Format.htmlEncode(axx.msg) +
          "<br/><span class=cfeed> · " +
          Ext.util.Format.htmlEncode(axx.last_dttm) +
          '</span>"';
        var cnt = 1 * cx.data.pkpkpk_cf;
        if (cnt > 9) cnt = "+9";
        return (
          '<b style="color:' +
          (axx.is_new ? "red" : "rgb(163,181,217)") +
          '">' +
          cnt +
          '</b> <img src="../images/custom/bullet_comment.png" border=0 onclick="mainPanel.loadTab({attributes:{modalWindow:true, _title_:\'' +
          a.name +
          "',href:'showPage?_tid=836',slideIn:'t',_pk:{tcomment_id:'comment_id'},baseParams:{xtable_id:" +
          a.crudTableId +
          ", xtable_pk:" +
          cx.id +
          '}}});">'
        );
      } else
        return (
          '<img src="../images/custom/bullet_comment.png" border=0 onclick="mainPanel.loadTab({attributes:{modalWindow:true, _title_:\'' +
          a.name +
          "',href:'showPage?_tid=836',slideIn:'t',_pk:{tcomment_id:'comment_id'},baseParams:{xtable_id:" +
          a.crudTableId +
          ", xtable_pk:" +
          cx.id +
          '}}});">'
        );
    } else return "";
  };
}

function commentHtml(x) {
  return x ? '<img src="../images/custom/bullet_comment.png" border=0>' : "";
}

iwb.showApprovalLogs=function(id){
	return mainPanel.loadTab({
	    attributes: {
	      modalWindow: true,
	      href: "showPage?_tid=259&_gid1=530", //238
	      baseParams: {
	        xapproval_record_id: id
	      }
	    }
	});
}
function approvalHtml(x, y, z) {
  if (!x) return "";
  var str =
    x > 0 ? '<img src="/images/custom/bullet_approval.gif" border=0> ' : "";
  str +=
    "<a href=# onclick=\"return iwb.showApprovalLogs(" +
    z.data.pkpkpk_arf_id +
    ')"';
  if (z.data.app_role_ids_qw_ || z.data.app_user_ids_qw_) {
    str += ' title=":' + getLocMsg("approvals");
    var bb = false;
    if (z.data.app_role_ids_qw_) {
      str +=
        " [" + getLocMsg("roles") + ": " + z.data.app_role_ids_qw_ + "]";
      bb = true;
    }
    if (z.data.app_user_ids_qw_) {
      if (bb) str += ",";
      str +=
        " [" + getLocMsg("users") + ": " + z.data.app_user_ids_qw_ + "]";
    }
    str += '"';
  }
  str += ">" + z.data.pkpkpk_arf_qw_ + "</a>";
  return str;
}

function wideScreenTooltip(value, metadata, record, rowIndex, colIndex, store) {
  if (value) {
    metadata.attr +=
      ' ext:qtip=" <b>' + Ext.util.Format.htmlEncode(value) + '</b>"';
  }
  return value;
}

function mailBoxRenderer(a) {
  return function(ax, bx, cx) {
    return ax
      ? "<img src=\"../images/custom/bullet_mail.gif\" border=0 onclick=\"mainPanel.loadTab({attributes:{modalWindow:true, _iconCls:'icon-email', _title_:'" +
          a.name +
          "',href:'showPage?_tid=238&_gid1=874',baseParams:{xtable_id:" +
          a.crudTableId +
          ",xtable_pk:" +
          cx.id +
          '}}});">'
      : "";
  };
}

function fmtDecimal(value) {
  if (!value) return "0";
  var digit = _app.number_decimal_places * 1 || 4;
  var result =
    Math.round(value * Math.pow(10, digit)) / Math.pow(10, digit) + "";
  var s = 1 * result < 0 ? 1 : 0;
  var x = result.split(".");
  var x1 = x[0],
    x2 = x[1];
  for (var i = x1.length - 3; i > s; i -= 3)
    x1 = x1.substr(0, i) + (_app.digit_separator || ".") + x1.substr(i);
  if (x2 && x2 > 0) return x1 + (_app.decimal_separator || ",") + x2;
  return x1;
}

function fmtDecimalNew(value, digit) {
  if (!value) return "0";
  if (!digit) digit = _app.number_decimal_places * 1 || 4;
  var result =
    Math.round(value * Math.pow(10, digit)) / Math.pow(10, digit) + "";
  var s = 1 * result < 0 ? 1 : 0;
  var x = result.split(".");
  var x1 = x[0],
    x2 = x[1];
  for (var i = x1.length - 3; i > s; i -= 3)
    x1 = x1.substr(0, i) + (_app.digit_separator || ".") + x1.substr(i);
  if (x2 && x2 > 0) return x1 + (_app.decimal_separator || ",") + x2;
  return x1;
}

function fmtParaShow(value) {
  if (!value) value = "0";
  var digit = _app.money_decimal_places * 1 || 4;
  var result =
    Math.round(value * Math.pow(10, digit)) / Math.pow(10, digit) + "";
  var s = 1 * result < 0 ? 1 : 0;
  var x = result.split(".");
  var x1 = x[0],
    x2 = x[1];
  if (!x2) x2 = "0";

  for (var j = x2.length; j < digit; j++) {
    x2 = x2 + "0";
  }

  for (var i = x1.length - 3; i > s; i -= 3)
    x1 = x1.substr(0, i) + (_app.digit_separator || ".") + x1.substr(i);
  return x1 + (_app.decimal_separator || ",") + x2;
}

function fmtPrice(value) {
  if (!value) value = "0";
  var digit = _app.price_decimal_places * 1 || 4;
  var result =
    Math.round(value * Math.pow(10, digit)) / Math.pow(10, digit) + "";
  var s = 1 * result < 0 ? 1 : 0;
  var x = result.split(".");
  var x1 = x[0],
    x2 = x[1];
  if (!x2) x2 = "0";

  for (var j = x2.length; j < digit; j++) {
    x2 = x2 + "0";
  }

  for (var i = x1.length - 3; i > s; i -= 3)
    x1 = x1.substr(0, i) + (_app.digit_separator || ".") + x1.substr(i);
  return x1 + (_app.decimal_separator || ",") + x2;
}

function getSel(m){
  if(m.gridId)return m.sm.getSelected();
  else {
	  m=m.getSelectedRecords ? m.getSelectedRecords() : Ext.getCmp(m.id).getSelectedRecords();
	  if(!m || !m.length)return false;
	  return m[0];
  }	
}

function getSels(m){
  if(m.gridId)return m.sm.getSelections();
  else {
	  m=m.getSelectedRecords ? m.getSelectedRecords() : Ext.getCmp(m.id).getSelectedRecords();
	  if(!m || !m.length)return false;
	  return m;
  }	
}

function getGridSel(a) {
  if (!a || !a._grid) {
    Ext.infoMsg.msg("error", getLocMsg("list_not_defined"));
    return null;
  } else {
	  var m = getSel(a._grid);
	  if(!m){
	    Ext.infoMsg.msg("error", getLocMsg("select_something"));
	    return null;
	  }
    return m;
  }
}


function getMasterGridSel(a) {
  if (
    !a ||
    !a._grid ||
    !a._grid._masterGrid
  ) {
    Ext.infoMsg.msg("error", getLocMsg("master_list_not_defined"));
    return null;
  } else {
	  var m = getSel(a._grid._masterGrid);
	  if(!m){
	    Ext.infoMsg.msg("error", getLocMsg("select_something"));
	    return null;
	  }
	  return m;
  }
}

function fmtFileSize(a) {
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
}

function fmtTimeAgo(a) {
  if (!a) return "-";
  a = Math.round((1 * a) / 1000);
  var d = getLocMsg("js_saniye");
  if (a > 60) {
    a = Math.round(a / 60);
    d = getLocMsg("js_dakika");
    if (a > 60) {
      a = Math.round(a / 60);
      d = getLocMsg("js_saat");
      if (a > 24) {
        a = Math.round(a / 24);
        d = getLocMsg("js_gun");
      } else if (a > 15) {
        return getLocMsg("approx_one_day");
      }
    } else if (a > 40) {
      return getLocMsg("approx_one_hour");
    }
  } else if (a > 40) {
    return getLocMsg("approx_one_minute");
  }
  return a + " " + d + " " + getLocMsg("time_ago");
}

function fmtShortDate(x) {
  return x ? (x.dateFormat ? x.dateFormat(iwb.dateFormat) : x) : "";
}

function fmtDateTime(x) {
  return x ? (x.dateFormat ? x.dateFormat(iwb.dateFormat+" H:i:s") : x) : "";
}

function fmtDateTimeWithDay(x, addsec) {
  if (addsec) {
    return x ? (x.dateFormat ? x.dateFormat(iwb.dateFormat+" H:i:s D") : x) : "";
  } else {
    return x ? (x.dateFormat ? x.dateFormat(iwb.dateFormat+" H:i D") : x) : "";
  }
}

function fmtDateTimeWithDay2(x) {
  return x ? (x.dateFormat ? x.dateFormat(iwb.dateFormat+" H:i:s D") : x) : "";
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
  var t = dt2.getTime();
  var xt = xtimeMap[_scd.locale] || {};
  if (t + 30 * 1000 > tnow) return xt[0]; // 'Az Önce';//5 sn
  if (t + 2 * 60 * 1000 > tnow) return xt[1]; // 'Bir Dakika Önce';//1 dka
  if (t + 60 * 60 * 1000 > tnow)
    return Math.round((tnow - t) / (60 * 1000)) + xt[2]; // ' Dakika Önce';
  if (t + 24 * 60 * 60 * 1000 > tnow)
    return Math.round((tnow - t) / (60 * 60 * 1000)) + xt[3]; // ' Saat Önce';
  if (t + 2 * 24 * 60 * 60 * 1000 > tnow) return xt[4]; // 'Dün';
  if (t + 7 * 24 * 60 * 60 * 1000 > tnow)
    return daysOfTheWeek[_scd.locale][dt2.getDay()]; // 5dka
  return dt2.dateFormat(iwb.dateFormat);
}

function buildParams(params, map) {
  var bp = {};
  for (var key in params) {
    var newKey = params[key];
    if (typeof newKey == "function") {
      bp[key] = newKey(params);
    } else if (newKey.charAt(0) == "!") bp[key] = newKey.substring(1);
    else bp[key] = map[params[key]];
  }
  return bp;
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
  if (!wh) Ext.infoMsg.alert("info", getLocMsg("remove_popup_blocker"));
  else wh.focus();
  return false;
}

function grid2grid(gridMaster, gridDetail, params, tp) {
  // tabpanel
  gridDetail.store.baseParams = null;
  if (params) gridDetail._params = params;
  var gs = gridMaster.getSelectionModel ?  gridMaster.getSelectionModel() : gridMaster;
  gs.on("selectionchange", gridDetail.onSelectionChange || function(a, b, c) {
    if (
      !gridDetail.initialConfig.onlyCommitBtn &&
      gridDetail.initialConfig.editMode
    )
      gridDetail.btnEditMode.toggle(); 
    if (a.getSelections ? a.getSelections().length==1: a.hasSelection ?  a.hasSelection() : (a.getSelectionCount()==1)) { //
      if (params || gridDetail._baseParams) {
        gridDetail.store.baseParams = Ext.apply(
          gridDetail._baseParams || {},
          params ? buildParams(params, a.getSelected ? a.getSelected().data : a.getSelectedRecords()[0].data) : {}
        );
      }
      if (gridDetail.isVisible() && (!tp || tp.isVisible())) {
        if (gridDetail.initialConfig.master_column_id)
          gridDetail.store.load({
            add: false,
            params: gridDetail.store.baseParams,
            scope: gridDetail.store
          });
        else {
          if (gridDetail.pageSize)
            gridDetail.store.reload({
              add: false,
              params: gridDetail.store.baseParams,
              scope: gridDetail.store
            });
          else gridDetail.store.reload(); // Eğer burada hata olursa geri aç
        }

        // else
		// gridDetail.store.reload({add:false,params:gridDetail.store.baseParams,scope:gridDetail.store});
      } else gridDetail.loadOnShow = true;
    } else try{
      gridDetail.store.baseParams = null;
      gridDetail.store.removeAll();
    }catch(qe){}
  });

  if (!tp) {
    gridDetail.on("show", function(a, b, c) {
      if (!a.initialConfig.onlyCommitBtn && a.initialConfig.editMode)
        a.btnEditMode.toggle();
      if (a.store.baseParams) {
        if (a.initialConfig.master_column_id)
          a.store.load({
            add: false,
            params: a.store.baseParams,
            scope: a.store
          });
        else {
          if (a.pageSize)
            a.store.reload({
              add: false,
              params: a.store.baseParams,
              scope: a.store
            });
          else a.store.reload(); // Eğer burada hata olursa geri aç
        }
      }
    });
  } else {
    tp._grid = gridDetail;
    tp.on("show", function(ax, b, c) {
      var a = ax._grid;
      if (!a.initialConfig.onlyCommitBtn && a.initialConfig.editMode)
        a.btnEditMode.toggle();
      if (a.store.baseParams) {
        if (a.initialConfig.master_column_id)
          a.store.load({
            add: false,
            params: a.store.baseParams,
            scope: a.store
          });
        else {
          if (a.pageSize)
            a.store.reload({
              add: false,
              params: a.store.baseParams,
              scope: a.store
            });
          else a.store.reload(); // Eğer burada hata olursa geri aç
        }
      }
    });
  }
}

var searchFormTools = [
  {
    id: "save",
    handler: function(ev, tb, sf) {
      // event, toolbar, searchForm
      if (!sf._menu) {
        var buttons = [
          {
            text: getLocMsg("save_settings"),
            iconCls: "icon-ekle",
            handler: function(a, b, c) {
              var p = prompt("Template Name", "");
              if (p) {
                var params = sf.getForm().getValues();
                params._dsc = p;
                promisRequest({
                  url: "ajaxBookmarkForm?_fid=" + sf._formId,
                  params: params,
                  successCallback: function() {
                    sf._menu = false;
                    Ext.infoMsg.alert("success", "saved");
                  }
                });
              }
            }
          },
          {
            text: getLocMsg("update_settings"),
            iconCls: "icon-duzenle",
            handler: function(a, b, c) {
              mainPanel.loadTab({
                attributes: {
                  _title_: "Search Form",
                  modalWindow: true,
                  href: "showPage?_tid=259&_gid1=491",
                  _pk: {
                    tform_value_id: "form_value_id"
                  },
                  baseParams: {
                    xform_id: sf._formId
                  }
                }
              });
            }
          }
        ];
        promisRequest({
          url: "ajaxQueryData?_qid=483",
          params: {
            xform_id: sf._formId
          },
          successCallback: function(j) {
            if (j.success && j.data.length > 0) {
              // while (a_menu.items.items.length > 2) a_menu.remove(2);
              buttons.push("-");
              var pf = true;
              for (var q = 0; q < j.data.length; q++) {
                if (j.data[q].public_flag && pf) {
                  if (q > 0) buttons.push("-");
                  pf = false;
                }
                buttons.push({
                  text: j.data[q].dsc,
                  _id: j.data[q].form_value_id,
                  handler: function(a, b, c) {
                    promisRequest({
                      url: "ajaxQueryData?_qid=503",
                      params: {
                        xform_value_id: a._id
                      },
                      successCallback: function(j2) {
                        if (j2.success && j2.data.length > 0) {
                          var f2 = sf.getForm();
                          var j3 = {};
                          for (var q2 = 0; q2 < j2.data.length; q2++) {
                            j3[j2.data[q2].dsc] = j2.data[q2].val;
                          }
                          f2.setValues(j3);
                        }
                      }
                    });
                  }
                });
              }
            }
            sf._menu = new Ext.menu.Menu({
              enableScrolling: false,
              items: buttons
            });
            sf._menu.showAt(ev.getXY());
          }
        });
      } else sf._menu.showAt(ev.getXY());
    }
  }
];

function fnShowDetailDialog(a, b) {
  /*
	 * TODO memory leak olabilir.
	 */
  var sel = getSel(a._grid),
    dv;
  new Ext.Window({
    title: "",
    id: "grid_detail_dialog_id",
    width: 900,
    height: 600,
    autoScroll: true,
    fbar: [
      {
        text: getLocMsg("close"),
        handler: function() {
          Ext.getCmp("grid_detail_dialog_id").close();
        }
      }
    ],
    items: [
      (dv = new Ext.DataView({
        store: new Ext.data.JsonStore({
          fields: a._grid.ds.reader.meta.fields,
          root: "data"
        }),
        tpl: a._grid.detailView,
        autoScroll: true,
        itemSelector: "div.card"
      }))
    ]
  }).show();
  dv.store.loadData({ data: [sel.json] });
}

function showBulletinDetail(bulletinid) {
  mainPanel.loadTab({
    attributes: {
      href:
        "showForm?_fid=1554&a=1&tbulletin_id=" +
        bulletinid +
        "&sv_btn_visible=0"
    }
  });
  return false;
}

function fnClearFilters(a, b) {
  a._grid._gp.filters.clearFilters();
}

function fnTableImport(a, b) {
  var im = a.ximport || a._grid.crudFlags.ximport;
  if (typeof im == "boolean") {
    Ext.infoMsg.alert("info", getLocMsg("js_table_import_setting_error"));
    return;
  }

  var cfg = {
    attributes: {
      modalWindow: true,
      id: "git" + a._grid.id,
      _title_: a._grid.name,
      href:
        "showPage?_tid=178&_gid1=895&xmaster_table_id=" +
        im.xmaster_table_id +
        "&xtable_id=" +
        a._grid.crudTableId +
        (im.xobject_tip ? "&xobject_tip=" + im.xobject_tip : ""),
      _grid: a._grid,
      ximport: im
    }
  };
  mainPanel.loadTab(cfg);
}

function showTableChildList(e, vtip, vxid, mtid, mtpk, relId) {
  if (typeof e == "undefined" && window.event) {
    e = window.event;
  }
  var elx = Ext.get("idLinkRel_" + relId);
  promisRequest({
    url: "ajaxGetTableRelationData",
    params: { _tb_id: mtid, _tb_pk: mtpk, _rel_id: relId },
    successCallback: function(j) {
      var items = [];
      for (var qi = 0; qi < j.data.length; qi++)
        items.push({
          text:
            j.data[qi].dsc.length > 100
              ? j.data[qi].dsc.substring(0, 97) + "..."
              : j.data[qi].dsc,
          _id: j.data[qi].id,
          handler: function(ax) {
            fnTblRecEdit(j.queryId, ax._id);
          }
        });
      if (j.browseInfo.totalCount > j.browseInfo.fetchCount) {
        items.push("-");
        items.push({
          text:
            getLocMsg("more") +
            "....(Total " +
            j.browseInfo.totalCount +
            ")",
          handler: function() {
            Ext.infoMsg.alert("info", "soooon");
          }
        }); // TODO
      }
      new Ext.menu.Menu({ enableScrolling: false, items: items }).showAt([
        elx.getX() + 16,
        elx.getY() + 16
      ]);
    }
  });
  return false;
}

var recordInfoWindow = null;
function renderTableRecordInfo(j) {
  if (!j || !j.dsc) return false;
  var s = "<p>";
  s += "<img src='./sf/pic"+ j.insert_user_id+ ".png' style='vertical-align: bottom;' class='ppic-mini'> &nbsp;";
  s +=
    '<a href=# style="font-size:26px;color:#b5b5b5" onclick="return fnTblRecEdit(' +
    j.tableId +
    "," +
    j.tablePk +
    ', true);">' +
    j.dsc +
    " &nbsp; <i style='color: #98bdcd;float: right;' class='icon-share-alt'></i></a></p><table border=0 width=100%><tr><td width=70% valign=top>";
  if (j.commentFlag && j.commentCount > 0)
    s +=
      ' &nbsp; <img src="/ext3.4.1/custom/images/comments-16.png" title="Comments"> ' +
      j.commentCount;
  if (j.fileAttachFlag && j.fileAttachCount > 0)
    s +=
      ' &nbsp; <img src="/images/custom/bullet_file_attach.png" title="Related Files"> ' +
      j.fileAttachCount;
  s += "</td><td width=30% align=right valign=top>";

  s += "</td></tr></table>";
  var rs = j.parents;
  var ss = "";
  for (var qi = rs.length - 1; qi >= 0; qi--) {
    var r = rs[qi];
    if (qi != rs.length - 1) ss += "<br>";
    for (var zi = rs.length - 1; zi > qi; zi--) ss += " &nbsp; &nbsp;";
    ss += "<span style='color:orange'>-</span> &nbsp;" + (qi != 0 ? r.tdsc : "<b>" + r.tdsc + "</b>");
    if (r.dsc) {
      var rdsc = r.dsc;
      if (rdsc.length > 300) rdsc = rdsc.substring(0, 297) + "...";
      ss +=
        qi != 0
          ? ': <a href=# onclick="return fnTblRecEdit(' +
            r.tid +
            "," +
            r.tpk +
            ', false);">' +
            rdsc +
            "</a>"
          : ": " + rdsc; // else ss+=': (...)';
    }
  }
  ss = '<div class="dfeed" style="color: #b1b1b1;padding-left:16px;font-size: 13px;">' + ss + "</div>";

  s += ss + "<p><br>";
  if (j.insert_user_id) {
    s +=
      '<span class="cfeed"> · Created by <a href=# onclick="return openChatWindow(' +
      j.insert_user_id +
      ",'" +
      j.insert_user_id_qw_ +
      "',true)\">" +
      j.insert_user_id_qw_ +
      "</a> @ " +
      j.insert_dttm;
    if (j.version_no && j.version_no > 1)
      s +=
        '<br> · Last modified by <a href=# onclick="return openChatWindow(' +
        j.version_user_id +
        ",'" +
        j.version_user_id_qw_ +
        "',true)\">" +
        j.version_user_id_qw_ +
        "</a> @ " +
        j.version_dttm +
        " <br> · " +
        j.version_no +
        " times modified</span>";
  }

  rs = j.childs;
  if (!rs || !rs.length) return s;
  ss = "<p><br><span style='color:#aaa;font-size:20px'>Details</span><div style='padding: 4px;font-size: 13px;'>";
  for (var qi = 0; qi < rs.length; qi++) {
    var r = rs[qi];
    ss +=
      (r.vtip
        ? '<a href=# id="idLinkRel_' +
          r.rel_id +
          '" onclick="return showTableChildList(event,' +
          r.vtip +
          "," +
          r.void +
          "," +
          r.mtbid +
          "," +
          r.mtbpk +
          "," +
          r.rel_id +
          ');return false;">' +
          r.tdsc +
          "</a>"
        : r.tdsc) +
      " <span style='background: #F44336;padding: 1px 6px;border-radius: 20px;color: white;'>" +
      r.tc +
      (_app.table_children_max_record_number &&
      1 * r.tc == 1 * _app.table_children_max_record_number - 1
        ? "+"
        : "") +
      "</span>";
    if (r.tcc)
      ss +=
        ' &nbsp; <img src="/ext3.4.1/custom/images/comments-16.png" title="Comments"> ' +
        r.tcc;
    if (r.tfc)
      ss +=
        ' &nbsp; <img src="/images/custom/bullet_file_attach.png" title="Related Files"> ' +
        r.tfc;

    ss+='<br>'
    // if(r.dsc)ss+=(qi!=0 ? ': '+r.dsc:': <b>'+r.dsc+'</b>');// else ss+=':
	// (...)';
  }
  s+='</div>'
  return s + ss;
}

function fnTblRecEdit(tid, tpk, b) {
  if (b) {
    if (recordInfoWindow && recordInfoWindow.isVisible()) {
      recordInfoWindow.destroy();
      recordInfoWindow = null;
    }
    mainPanel.loadTab({
      attributes: {text:'Update Record',
        id: "g-" + tid + "-" + tpk,
        href: "showForm?_tb_id=" + tid + "&_tb_pk=" + tpk
      }
    });
  } else {
	  iwb.request({
      url: "getTableRecordInfo",
      requestWaitMsg: true,
      params: { _tb_id: tid, _tb_pk: tpk },
      successCallback: function(j) {
        if (j.dsc) {
          if (j.dsc.length > 100) j.dsc = j.dsc.substring(0, 97) + "...";
          if (recordInfoWindow && recordInfoWindow.isVisible()) {
            // recordInfoWindow.destroy();
            recordInfoWindow.update(renderTableRecordInfo(j));
            recordInfoWindow.setTitle(j.parents[0].tdsc);
//            recordInfoWindow.setIconClass("icon-folder-explorer");
          } else {
            recordInfoWindow = new Ext.Window({
              modal: true,
              shadow: false,
              title: j.parents[0].tdsc,
              width: 600, cls:'animated fadeIn',
              autoHeight: true,
              bodyStyle: "padding:10px;",
//              iconCls: "icon-folder-explorer",
              border: false,
              html: renderTableRecordInfo(j)
            });
            recordInfoWindow.show();
          }
        } else Ext.infoMsg.msg("error", "You can go back ;)");
      }
    });
  }
  return false;
}

function fnOpenUrl(url) {
  mainPanel.loadTab({ attributes: { href: url } });
  return false;
}

function fnTblRecComment(tid, tpk) {
  mainPanel.loadTab({
    attributes: {
      modalWindow: true,
      href: "showPage?_tid=836",
      slideIn: "t",
      _pk: { tcomment_id: "comment_id" },
      baseParams: { xtable_id: tid, xtable_pk: tpk }
    }
  });
  return false;
}

function fnRowBulkEdit(a, b) {
  mainPanel.loadTab({
    attributes: {
      id: "gu-" + a._grid.id,
      href: "showForm?_fid=1617&xtable_id=" + a._grid.crudTableId,
      _grid: a._grid
    }
  });
}

function fnRowBulkMail(a, b) {
  mainPanel.loadTab({
    attributes: {
      id: "gum-" + a._grid.id,
      href: "showForm?_fid=2128&xtable_id=" + a._grid.crudTableId,
      _grid: a._grid
    }
  });
}

function fnRowEdit(a, b) {
  if (!a._grid.onlyCommitBtn && a._grid.editMode) {
    Ext.infoMsg.msg("warning", getLocMsg("exit_from_edit_mode"));
    return;
  }
  var sel = getSel(a._grid);
  if (!sel) {// a._grid.sm.hasSelection()
    Ext.infoMsg.msg("warning", getLocMsg("select_something"));
    return;
  }

  if (a._grid.multiSelect) {
  }
  var href =
    "showForm?a=1&_fid=" +
    a._grid.crudFormId;
  var idz = "";
  for (var key in a._grid._pk) {
    href += "&" + key + "=" + sel.data[a._grid._pk[key]];
    idz += sel.data[a._grid._pk[key]];
  }
  if (typeof a._grid._postUpdate == "function") {
    href = a._grid._postUpdate(sel, href, a); // null donerse acilmayacak
  } else {
    if (a._grid._postUpdate) href += "&" + a._grid._postUpdate;
  }
  if (href) {
    var cfg = {
      attributes: {
        id: "g" + a._grid.id + "-" + idz,
        href: href,
        _grid: a._grid ? a._grid._refreshGrid || a._grid : null
      }
    };
    if (a.showModalWindowFlag) cfg.attributes.modalWindow = true;
    mainPanel.loadTab(cfg);
  }
}

function fnRowEdit4Log(a, b) {
	  var sel = getSel(a._grid);
  if (!sel) {// a._grid.sm.hasSelection()
    Ext.infoMsg.msg("warning", getLocMsg("select_something"));
    return;
  }
  var href =
    "showForm?a=1&_fid=" +
    a._cgrid.crudFormId +
    "&_log5_log_id=" +
    sel.data.log5_log_id;
  var idz = "";
  var _pk = a._cgrid._pk;
  for (var key in _pk) {
    href += "&" + key + "=" + sel.data[_pk[key]];
    idz += sel.data[_pk[key]];
  }
  if (typeof a._grid._postUpdate == "function") {
    href = a._grid._postUpdate(sel, href, a); // null donerse acilmayacak
  } else {
    if (a._grid._postUpdate) href += "&" + a._grid._postUpdate;
  }
  if (href) {
    var cfg = {
      attributes: {
        modalWindow: true,
        id: "g" + a._grid.id + "-" + idz,
        href: href,
        _grid: a._cgrid
      }
    };
    mainPanel.loadTab(cfg);
  }
}

function fnDataMoveUpDown(a, b) {
  if (!a._grid.onlyCommitBtn && a._grid.editMode) {
    Ext.infoMsg.alert("info", getLocMsg("exit_from_edit_mode"));
    return;
  }

  var sel = getSel(a._grid);
  iwb.request({
    url:
      "ajaxExecDbFunc?_did=701&ptable_id=" +
      a._grid.crudTableId +
      "&ptable_pk=" +
      sel.id +
      "&pdirection=" +
      a._direction,
    successDs: a._grid.ds
  });
}

function fnRowEditDblClick(a, b) {
  fnRowEdit({ _grid: a.initialConfig }, b);
}

function fnCardDblClick(a, b) {
  return   fnRowEdit({ _grid: a}, b);
}

function fnRowInsert(a, b) {
  if (!a._grid.onlyCommitBtn && a._grid.editMode) {
    Ext.infoMsg.msg("warning", getLocMsg("exit_from_edit_mode"));
    return;
  }
  var sel = getSel(a._grid);
  var href =
    "showForm?a=2&_fid=" +
    a._grid.crudFormId;
  if (typeof a._grid._postInsert == "function") {
    href = a._grid._postInsert(sel, href, a); // null donerse acilmayacak
  } else {
    if (a._grid._postInsert) href += "&" + a._grid._postInsert;
  }
  if (href) {
    var cfg = {
      attributes: {text:'New Record',
        id: "g" + a._grid.id + "-i",
        href: href,
        _grid: a._grid ? a._grid._refreshGrid || a._grid : null
      }
    };
    if (a.showModalWindowFlag) cfg.attributes.modalWindow = true;
    mainPanel.loadTab(cfg);
  }
}
function fnRowCopy(a, b) {
  var sel = getSel(a._grid);
  if (!sel) {// a._grid.sm.hasSelection()
    Ext.infoMsg.msg("warning", getLocMsg("select_something"));
    return;
  }

  if (a._grid.multiSelect) {
  }
  var href = "showForm?a=5&_fid=" + a._grid.crudFormId;
  var idz = "";
  for (var key in a._grid._pk) {
    href += "&" + key + "=" + sel.data[a._grid._pk[key]];
    idz += sel.data[a._grid._pk[key]];
  }
  if (typeof a._grid._postInsert == "function") {
    href = a._grid._postInsert(sel, href, a); // null donerse acilmayacak
  } else {
    if (a._grid._postInsert) href += "&" + a._grid._postInsert;
  }
  if (href) {
    var cfg = {
      attributes: {
        id: "gc" + a._grid.id + "-" + idz,
        href: href,
        _grid: a._grid
      }
    };
    if (a.showModalWindowFlag) cfg.attributes.modalWindow = true;
    mainPanel.loadTab(cfg);
  }
}
function fnRowDelete(a, b) {
  if (!getSel(a._grid)) {
    Ext.infoMsg.msg("warning", getLocMsg("select_something"));
    return;
  }
  if (a._grid.multiSelect) {
    var sels = a._grid.sm.getSelections();
    if (a._grid.editMode) {
      var sel = null;
      for (var zz = 0; zz < sels.length; zz++) {
        sel = sels[zz];
        var delItem = {};
        for (var key in a._grid._pk) delItem[key] = sel.data[a._grid._pk[key]];
        a._grid._deletedItems.push(delItem);
        a._grid.ds.remove(sel);
      }
      var ds = a._grid.ds || a._grid.store;
      var io = ds.indexOf(sel);
      ds.remove(sel);
      if (ds.getCount() > 0) {
        if (io >= ds.getCount()) io = ds.getCount() - 1;
        a._grid.sm.selectRow(io, false);
      }
      return;
    }

    Ext.infoMsg.confirm(
      getLocMsg("confirm_delete") +
        " (" +
        sels.length +
        " " +
        getLocMsg("record") +
        ")",
      () => {
        var href = "ajaxPostEditGrid?_fid=" + a._grid.crudFormId;
        var params = { _cnt: sels.length };
        if (typeof a._grid._postDelete == "function") {
          href = a._grid._postDelete(sels, href, a); // null donerse
														// acilmayacak
        } else {
          for (var bjk = 0; bjk < sels.length; bjk++) {
            // delete
            for (var key in a._grid._pk)
              params[key + "" + (bjk + 1)] = sels[bjk].data[a._grid._pk[key]];
            params["a" + (bjk + 1)] = 3;
          }
        }
        if (href)
        	iwb.request({
            url: href,
            params: params,
            requestWaitMsg: true,
            successDs: a._grid.ds,
            successCallback: function(j2) {
              if (j2.logErrors || j2.msgs) {
                var str = "";
                if (j2.msgs) str = j2.msgs.join("<br>") + "<p>";
                if (j2.logErrors) str += prepareLogErrors(j2);
                Ext.infoMsg.alert("info", str);
              }
            }
          });
      }
    );
  } else {
    var sel = getSel(a._grid);
    if (a._grid.onlyCommitBtn || a._grid.editMode) {
      var delItem = {};
      for (var key in a._grid._pk) delItem[key] = sel.data[a._grid._pk[key]];
      if (!a._grid._deletedItems) a._grid._deletedItems = [];
      a._grid._deletedItems.push(delItem);
      var ds = a._grid.ds || a._grid.store;
      var io = ds.indexOf(sel);
      ds.remove(sel);
      if (ds.getCount() > 0) {
        if (io >= ds.getCount()) io = ds.getCount() - 1;
        a._grid.sm.selectRow(io, false);
      }
      return;
    }
    Ext.infoMsg.confirm(getLocMsg("confirm_delete"), () => {
      var href = "ajaxPostForm?a=3&_fid=" + a._grid.crudFormId;
      if (typeof a._grid._postDelete == "function") {
        href = a._grid._postDelete(sel, href, a); // null donerse acilmayacak
      } else {
        for (var key in a._grid._pk)
          href += "&" + key + "=" + sel.data[a._grid._pk[key]];
        if (a._grid._postDelete) href += "&" + a._grid._postDelete;
      }
      if (href)
    	  iwb.request({
          url: href,
          successDs: a._grid.ds,
          requestWaitMsg: true,
          successCallback: function(j2) {
            if (!j2.logErrors) {
              var ds = a._grid.ds || a._grid.store;
              ds.remove(sel);
            }
            if (j2.logErrors || j2.msgs) {
              var str = "";
              if (j2.msgs) str = j2.msgs.join("<br>") + "<p>";
              if (j2.logErrors) str += prepareLogErrors(j2);
              Ext.infoMsg.alert("info", str);
            }
          }
        });
    });
  }
}

function fnRightClick(grid, rowIndex, e) {
  e.stopEvent();
  grid.getSelectionModel().selectRow(rowIndex);
  var coords = e.getXY();
  grid.messageContextMenu.showAt([coords[0], coords[1]]);
}


function fnCardRightClick(card, rowIndex, node, e) {
  e.stopEvent();
  card.select(rowIndex,false);
  var coords = e.getXY();
  card.messageContextMenu.showAt([coords[0], coords[1]]);
}

function selections2string(selections, seperator) {
  if (!selections) return "";
  if (!seperator) seperator = "|";
  var str = "";
  for (var d = 0; d < selections.length; d++)
    str += seperator + selections[d].id;
  return str.substring(1);
}

function fnExportGridData(b) {
  return function(a) {
    var g = a._grid;
    if (g.ds.getTotalCount() == 0) {
      Ext.infoMsg.alert("info", getLocMsg("no_data"));
      return;
    }
    var cols = "";
    for (var z = 0; z < g.columns.length; z++) {
      if (!g.columns[z].hidden && g.columns[z].dataIndex)
        cols += ";" + g.columns[z].dataIndex + "," + g.columns[z].width;
    }
    var url =
      "grd/" +
      g.name +
      "." +
      b +
      "?_gid=" +
      g.gridId +
      "&_columns=" +
      cols.substr(1);
    var vals = g.ds.baseParams;
    for (var k in vals) url += "&" + k + "=" + vals[k];
    if (g.ds.sortInfo) {
      if (g.ds.sortInfo.field) url += "&sort=" + g.ds.sortInfo.field;
      if (g.ds.sortInfo.direction) url += "&dir=" + g.ds.sortInfo.direction;
    }
    openPopup(url, "_blank", 800, 600);
  };
}

function fnGraphGridData(a) {
  var g = a._grid;
  if (g.ds.getTotalCount() == 0) {
    Ext.infoMsg.alert("info", getLocMsg("no_data"));
    return;
  }
  mainPanel.loadTab({
    attributes: { href: "showPage?_tid=480&tgrid_id=" + g.gridId, _grid: g }
  });
}

function fnGraphGridDataTree(a) {
  var g = a._grid;
  if (g.ds.getTotalCount() == 0) {
    Ext.infoMsg.alert("info", getLocMsg("no_data"));
    return;
  }
  mainPanel.loadTab({
    attributes: {
      href:
        "showPage?_tid=481&xgrid_id=" +
        g.gridId +
        "&xtable_id=" +
        g.crudTableId,
      _grid: g
    }
  });
}

function fnExportGridDataWithDetail(b) {
  return function(a) {
    var g2 = a._grid;
    if (!g2 || !g2._masterGrid) {
      return false;
    }
    var g = g2._masterGrid;
    var params = g2._gp._params;
    if (!params) {
      return false;
    }
    if (g.ds.getTotalCount() == 0) {
      Ext.infoMsg.alert("info", getLocMsg("no_data"));
      return;
    }
    var cols = "";
    for (var z = 0; z < g.columns.length; z++) {
      // master
      if (!g.columns[z].hidden && g.columns[z].dataIndex)
        cols += ";" + g.columns[z].dataIndex + "," + g.columns[z].width;
    }
    var cols2 = "";
    for (var z = 0; z < g2.columns.length; z++) {
      // detail
      if (!g2.columns[z].hidden && g2.columns[z].dataIndex)
        cols2 += ";" + g2.columns[z].dataIndex + "," + g2.columns[z].width;
    }
    var par2 = "";
    for (var z in params) {
      if (params[z]) par2 += ";" + z + "," + params[z];
    }
    var url =
      "grd2/" +
      g.name +
      "." +
      b +
      "?_gid=" +
      g.gridId +
      "&_columns=" +
      cols.substr(1) +
      "&_gid2=" +
      g2.gridId +
      "&_columns2=" +
      cols2.substr(1) +
      "&_params=" +
      par2.substr(1);
    var vals = g.ds.baseParams;
    for (var k in vals) url += "&" + k + "=" + vals[k];
    if (g.ds.sortInfo) {
      if (g.ds.sortInfo.field) url += "&sort=" + g.ds.sortInfo.field;
      if (g.ds.sortInfo.direction) url += "&dir=" + g.ds.sortInfo.direction;
    }
    openPopup(url, "_blank", 800, 600);
  };
}

function fnNewFileAttachmentMail(a) {
  var fp = a._form._cfg.formPanel;
  var hasReqestedVersion = true;
  /*
	 * var hasReqestedVersion = DetectFlashVer(9, 0, 0); // Bu flash yüklü mü
	 * değil mi onu sorguluyor. (major ver, minor ver, revision no)
	 */
  if (hasReqestedVersion) {
    var href = "showForm?_fid=750&_did=447&table_id=48&table_pk=-1";
  } else {
    var href =
      "showForm?a=2&_fid=512&table_id=" +
      a._grid.crudTableId +
      "&table_pk=" +
      table_pk.substring(1);
  }
  mainPanel.loadTab({
    attributes: {
      modalWindow: true,
      id: a._form.formId + "f",
      href: href,
      _form: fp,
      iconCls: "icon-attachment",
      title: getLocMsg("attach_files")
    }
  });
}

function fnNewFileAttachment(a) {
  var hasReqestedVersion = false; //|| DetectFlashVer(9, 0, 0); // Bu flash yüklü mü değil
  var sel = getSel(a._grid);
  if (!sel) {
    Ext.infoMsg.msg("warning", getLocMsg("select_something"));
    return;
  }
  var image_param = "";
  if (a.not_image_flag) {
    image_param = "&xnot_image_flag=1";
  }
  var table_pk = "";
  for (var key in a._grid._pk) table_pk += "|" + sel.data[a._grid._pk[key]];

   var href =
      (_scd.customFile ? "showPage?_tid=6827":"showForm?a=2&_fid=43")+"&table_id=" +
      a._grid.crudTableId +
      "&table_pk=" +
      table_pk.substring(1) +
      image_param;

  mainPanel.loadTab({
    attributes: {
      modalWindow: true,
      id: a._grid.id + "f",
      href: href,
      _grid: a._grid,
      iconCls: "icon-attachment",
      title: a._grid.name
    }
  });
}

function fnNewFileAttachment4ExternalUrl(a) {
  var sel = getSel(a._grid);
  if (!sel) {
    Ext.infoMsg.msg("warning", getLocMsg("select_something"));
    return;
  }
  var table_pk = "";
  for (var key in a._grid._pk) table_pk += "|" + sel.data[a._grid._pk[key]];

  var href =
    "showForm?a=2&_fid=2213&table_id=" +
    a._grid.crudTableId +
    "&table_pk=" +
    table_pk.substring(1);

  mainPanel.loadTab({
    attributes: {
      modalWindow: true,
      id: a._grid.id + "f",
      href: href,
      _grid: a._grid,
      iconCls: "icon-attachment",
      title: getLocMsg("js_add_from_external_url")
    }
  });
}

function fnNewFileAttachment4Form(tid, tpk, not_image_flag) {
  var image_param = "";
  if (not_image_flag) {
    image_param = "&xnot_image_flag=1";
  }

    var href =
      (_scd.customFile ? "showPage?_tid=6827":"showForm?a=2&_fid=43")+"&table_id=" + tid + "&table_pk=" + tpk + image_param;
  mainPanel.loadTab({
    attributes: {
      modalWindow: true,
      id: tid + "xf",
      href: href,
      iconCls: "icon-attachment",
      title: "Attach File"
    }
  });
  return false;
}

function fnFileAttachmentList(a) {
  var sel = getSel(a._grid);
  if (!sel) {
    Ext.infoMsg.msg("warning", getLocMsg("select_something"));
    return;
  }
  var table_pk = "";
  for (var key in a._grid._pk) table_pk += "|" + sel.data[a._grid._pk[key]];
  var cfg = {
    attributes: {
      modalWindow: true,
      href: "showPage?_tid="+ (_scd.customFile ? 6813:9) + "&_gid458_a=1",
      baseParams: {
        xtable_id: a._grid.crudTableId,
        xtable_pk: table_pk.substring(1)
      }
    }
  };
  cfg.attributes._title_ = sel.data.dsc
    ? a._grid.name + ": " + sel.data.dsc
    : a._grid.name;
  mainPanel.loadTab(cfg);
}

function fnCommit(a) {
  var params = {};
  var dirtyCount = 0;
  if (a._grid.fnCommit) {
    params = a._grid.fnCommit(a._grid);
    if (!params) return;
    dirtyCount = params._cnt;
  } else {
    var items = a._grid._deletedItems;
    if (items)
      for (var bjk = 0; bjk < items.length; bjk++) {
        // delete
        dirtyCount++;
        for (var key in items[bjk])
          params[key + "" + dirtyCount] = items[bjk][key];
        params["a" + dirtyCount] = 3;
      }

    items = a._grid.ds.data.items;
    if (items)
      for (var bjk = 0; bjk < items.length; bjk++)
        if (items[bjk].dirty) {
          // edit
          if (a._grid.editGridValidation) {
            if (a._grid.editGridValidation(items[bjk]) === false) return;
          }
          dirtyCount++;

          var changes = items[bjk].getChanges();
          for (var key in changes) {
            var valx = changes[key];
            params[key + "" + dirtyCount] =
              valx != null
                ? valx instanceof Date
                  ? fmtDateTime(valx)
                  : valx
                : null;
          }
          if (a._grid._insertedItems && a._grid._insertedItems[items[bjk].id]) {
            params["a" + dirtyCount] = 2;
            if (a._grid._postInsertParams) {
              var xparams = null;
              if (typeof a._grid._postInsertParams == "function")
                xparams = a._grid._postInsertParams(items[bjk]);
              else xparams = a._grid._postInsertParams;
              if (xparams)
                for (var key in xparams)
                  params[key + dirtyCount] = xparams[key];
            }
          } else {
            for (var key in a._grid._pk) {
              var val = a._grid._pk[key];
              params[key + "" + dirtyCount] =
                val.charAt(0) == "!" ? val.substring(1) : items[bjk].data[val];
            }
            params["a" + dirtyCount] = 1;
          }
        }
  }
  if (dirtyCount > 0) {
    params._cnt = dirtyCount;
    Ext.infoMsg.confirm(getLocMsg("confirm_update"), () => {
    	iwb.request({
        url: "ajaxPostEditGrid?_fid=" + a._grid.crudFormId,
        params: params,
        requestWaitMsg: true,
        successDs: a._grid.ds,
        successCallback: function(j2) {
          if (a._grid._deletedItems) a._grid._deletedItems = [];
          if (a._grid._insertedItems) a._grid._insertedItems = [];
          if (j2.logErrors || j2.msgs) {
            var str = "";
            if (j2.msgs) str = j2.msgs.join("<br>") + "<p>";
            if (j2.logErrors) str += prepareLogErrors(j2);
            Ext.infoMsg.msg("success", str);
          }
        }
      });
    });
  } else Ext.infoMsg.msg("info", getLocMsg("no_change"));
}

function fnToggleEditMode(a) {
  a._grid.editMode = !a._grid.editMode;

  if (typeof a._grid._postToggleEditMode == "function") {
    if (a._grid.editMode && !a._grid._postToggleEditMode(a)) {
      a._grid._gp.btnEditMode.toggle();
      return null;
    }
  }

  if (a._grid.editMode) {
    // editMode'a geçti
    a._grid._deletedItems = [];
    if (a._grid._gp.btnCommit) a._grid._gp.btnCommit.enable();
  } else {
    if (a._grid._gp.btnCommit) a._grid._gp.btnCommit.disable();
    var modified = false;
    if (a._grid._deletedItems.length > 0) modified = true;
    if (!modified) {
      var items = a._grid.ds.data.items;
      for (var bjk = 0; bjk < items.length; bjk++)
        if (items[bjk].dirty) {
          modified = true;
          break;
        }
    }

    if (modified) iwb.reload(a._grid);
  }
}

function fnGridSetting(a) {
  var cfg = null;
  if (a._grid.searchForm) {
    cfg = {
      attributes: {
        modalWindow: true,
        _width_: 600,
        _height_: 400,
        href:
          "showPage?_tid=543&_gid1=440&_gid3=439&_fid4=998&a=1&tform_id=" +
          a._grid.searchForm.formId +
          "&_fid2=999&tgrid_id=" +
          a._grid.gridId,
        _pk1: { tgrid_column_id: "grid_column_id" },
        _pk3: { tform_cell_id: "form_cell_id" },
        baseParams: {
          xgrid_id: a._grid.gridId,
          xform_id: a._grid.searchForm.formId
        }
      }
    };
  } else {
    cfg = {
      attributes: {
        modalWindow: true,
        href:
          "showPage?_tid=543&_gid1=440&_fid2=999&a=1&tgrid_id=" +
          a._grid.gridId,
        _pk1: { tgrid_column_id: "grid_column_id" },
        baseParams: { xgrid_id: a._grid.gridId }
      }
    };
  }
  cfg.attributes._title_ =
    getLocMsg("grid_settings") + " (" + a._grid.name + ")";
  mainPanel.loadTab(cfg);
}

function fnGridReportSetting(a) {
  if (!a._grid.crudTableId) return false;
  var cfg = {
    attributes: {
      modalWindow: true,
      href: "showPage?_tid=238&_gid1=1626",
      _pk1: { treport_id: "report_id" },
      baseParams: { xmaster_table_id: a._grid.crudTableId }
    }
  };
  cfg.attributes._title_ = a._grid.name; // getLocMsg('report_settings');
  mainPanel.loadTab(cfg);
}

function fnGridPrivilege(a) {
  var url = "showPage?_tid=543&_gid1=442";
  var attr = {
    modalWindow: true,
    _pk1: { ttable_field_id: "table_field_id" },
    baseParams: {
      xgrid_id: a._grid.gridId,
      xobject_tip: 5,
      xobject_id: a._grid.gridId
    },
    _title_: "Grid Yetkileri (" + a._grid.name + ")"
  };
  var adet = 1;
  if (a._grid.extraButtons && a._grid.extraButtons.length > 0) {
    adet++;
    url += "&_gid" + adet + "=838";
    attr["_pk" + adet] = { ttoolbar_item_id: "toolbar_item_id" };
  }
  if (a._grid.menuButtons && a._grid.menuButtons.items.length > 0) {
    adet++;
    url += "&_gid" + adet + "=839";
    attr["_pk" + adet] = { tmenu_item_id: "menu_item_id" };
  }
  if (a._grid.isMainGrid) {
    adet++;
    url += "&_gid" + adet + "=803";
    attr.baseParams.xparent_object_id = a._grid.extraOutMap.tplObjId;
    attr["_pk" + adet] = { ttemplate_object_id: "template_object_id" };
  }
  attr.href = url;
  mainPanel.loadTab({ attributes: attr });
}

function fnRecordComments(a) {
  // TODO: daha duzgun bir chat interface'i yap
  var sel = getSel(a._grid);
  if (!sel) {
    Ext.infoMsg.msg("warning", getLocMsg("select_something"));
    return;
  }
  var table_pk = "";
  for (var key in a._grid._pk) table_pk += "|" + sel.data[a._grid._pk[key]];
  var cfg = {
    attributes: {
      modalWindow: true,
      href: "showPage?_tid=836",
      slideIn: "t",
      _pk: { tcomment_id: "comment_id" },
      baseParams: {
        xtable_id: a._grid.crudTableId,
        xtable_pk: table_pk.substring(1)
      }
    }
  };
  cfg.attributes._title_ = sel.data.dsc
    ? a._grid.name + ": " + sel.data.dsc
    : a._grid.name;
  mainPanel.loadTab(cfg);
}

function fnRecordPrivileges(a) {
  var sel = getSel(a._grid);
  if (!sel) {
    Ext.infoMsg.msg("warning", getLocMsg("select_something"));
    return;
  }
  var cfg = {
    attributes: {
      modalWindow: true,
      href:
        "showPage?_tid=238&_gid1=477&crud_table_id=" +
        a._grid.crudTableId +
        "&_table_pk=" +
        sel.id,
      _pk: {
        access_roles: "access_roles",
        access_users: "access_users",
        paccess_flag: "access_flag",
        paccess_tip: "val",
        ptable_id: "!" + a._grid.crudTableId,
        ptable_pk: "!" + sel.id
      },
      baseParams: { xtable_id: a._grid.crudTableId, xtable_pk: sel.id }
    }
  };
  cfg.attributes._title_ = sel.data.dsc
    ? a._grid.name + ": " + sel.data.dsc
    : a._grid.name;
  mainPanel.loadTab(cfg);
}

function buildHelpWindow(cfg) {
  win = new Ext.Window({
    id: cfg.hid,
    layout: "fit",
    width: cfg.hwidth * 1,
    height: cfg.hheight * 1,
    title: cfg.htitle,
    items: [
      {
        xtype: "panel",
        autoScroll: true,
        html: '<div style="margin: 5px 5px 5px 5px">' + cfg.hdsc + "</div>"
      }
    ]
  });
  win.show();
  win.setPagePosition(
    (mainViewport.getWidth() - win.getWidth()) / 2,
    (mainViewport.getHeight() - win.getHeight()) / 2
  );
}

function fnShowLog4Update(a, b) {
  var sel = getSel(a._grid);
  if (!sel) {
    Ext.infoMsg.msg("warning", getLocMsg("select_something"));
    return;
  }
  var paramz = { _vlm: 1 };
  for (var key in a._grid._pk) paramz[key] = sel.data[a._grid._pk[key]];

  mainPanel.loadTab({
    attributes: {
      _title_:
        getLocMsg("js_duzenle_kaydi") +
        ":" +
        (sel.data.dsc || getLocMsg("record")),
      modalWindow: true,
      _grid: a._grid,
      href: "showPage?_tid=298&_gid1=" + a._grid.gridId,
      baseParams: Ext.apply(paramz, a._grid.ds.baseParams)
    }
  });
}

function fnShowLog4Delete(a, b) {
  mainPanel.loadTab({
    attributes: {
      _title_: getLocMsg("js_silinenler_kaydi") + ":",
      modalWindow: true,
      href: "showPage?_tid=298&_gid1=" + a._grid.gridId,
      _grid: a._grid,
      baseParams: Ext.apply({ _vlm: 3 }, a._grid.ds.baseParams)
    }
  });
}

function addMoveUpDownButtons(xbuttons, xgrid) {
  if (xgrid.crudTableId) {
    if (xbuttons.length > 0) xbuttons.push("-");
    xbuttons.push({
      tooltip: getLocMsg("up"),
      cls: "x-btn-icon x-grid-go-up",
      disabled: true,
      _activeOnSelection: true,
      _grid: xgrid,
      _direction: -1,
      handler: fnDataMoveUpDown
    });
    xbuttons.push({
      tooltip: getLocMsg("down"),
      cls: "x-btn-icon x-grid-go-down",
      disabled: true,
      _activeOnSelection: true,
      _grid: xgrid,
      _direction: 1,
      handler: fnDataMoveUpDown
    });
  }
}

function addDefaultCrudButtons(xbuttons, xgrid, modalflag) {
  if (xbuttons.length > 0) xbuttons.push("-");
  var xbl = xbuttons.length;
  /* crud buttons & import */
  if (xgrid.gridId && xgrid.crudFlags.insert) {
    var cfg = {
      id: "btn_add_" + xgrid.id,
      tooltip: xgrid.newRecordLabel || getLocMsg("js_new"),
      cls: "x-btn-icon x-grid-new",
      ref: "../btnInsert",
      showModalWindowFlag: modalflag || false,
      _activeOnSelection: false,
      _grid: xgrid
    };
    if (xgrid.mnuRowInsert) cfg.menu = xgrid.mnuRowInsert;
    else cfg.handler = xgrid.fnRowInsert || fnRowInsert;
    xbuttons.push(cfg);
  }

  if (xgrid.crudFlags.edit)
    xbuttons.push({
      id: "btn_edit_" + xgrid.id,
      tooltip: getLocMsg("js_edit"),
      cls: "x-btn-icon x-grid-edit",
      disabled: true,
      showModalWindowFlag: modalflag || false,
      _activeOnSelection: true,
      _grid: xgrid,
      handler: xgrid.fnRowEdit || fnRowEdit
    });
  if (xgrid.crudFlags.remove)
    xbuttons.push({
      id: "btn_delete_" + xgrid.id,
      tooltip: getLocMsg("js_delete"),
      cls: "x-btn-icon x-grid-delete",
      disabled: true,
      _activeOnSelection: true,
      _grid: xgrid,
      handler: xgrid.fnRowDelete || fnRowDelete
    });
  if (xgrid.crudFlags.xcopy)
    xbuttons.push({
      id: "btn_copy_" + xgrid.id,
      tooltip: getLocMsg("js_copy"),
      cls: "x-btn-icon x-grid-copy-record",
      disabled: true,
      showModalWindowFlag: modalflag || false,
      _activeOnSelection: true,
      _grid: xgrid,
      handler: xgrid.fnRowCopy || fnRowCopy
    });
  if (xgrid.crudFlags.ximport) {
    if (
      typeof xgrid.crudFlags.ximport == "object" &&
      typeof xgrid.crudFlags.ximport.length != "undefined"
    ) {
      var xmenu = [];
      for (var qi = 0; qi < xgrid.crudFlags.ximport.length; qi++)
        if (!xgrid.crudFlags.ximport[qi].dsc)
          xmenu.push(xgrid.crudFlags.ximport[qi]);
        else {
          // xmenu.push({text:xgrid.crudFlags.ximport[qi].dsc,cls:xgrid.crudFlags.ximport[qi].cls
			// || '', _activeOnSelection:false, _grid:xgrid,
			// ximport:xgrid.crudFlags.ximport[qi],handler:fnTableImport});
        }
      if (xgrid.extraButtons) {
        var bxx = xmenu.length > 0;
        for (var qi = 0; qi < xgrid.extraButtons.length; qi++)
          if (
            xgrid.extraButtons[qi] &&
            xgrid.extraButtons[qi].ref &&
            xgrid.extraButtons[qi].ref.indexOf("../import_") == 0
          ) {
            if (bxx) {
              bxx = false;
              xmenu.push("-");
            }
            xgrid.extraButtons[qi]._grid = xgrid;
            xmenu.push(xgrid.extraButtons[qi]);
            xgrid.extraButtons.splice(qi, 1);
            qi--;
          }
        if (xgrid.extraButtons.length == 0) xgrid.extraButtons = undefined;
      }
      xbuttons.push({
        // tooltip: getLocMsg("js_import_from_other_records"),
        cls: "x-btn-icon x-grid-import",
        _activeOnSelection: false,
        _grid: xgrid,
        menu: xmenu
      });
    } else {
      // xbuttons.push({tooltip:getLocMsg('js_import_from_other_records'),
		// cls:'x-btn-icon x-grid-import', _activeOnSelection:false,
		// _grid:xgrid, handler:fnTableImport});
    }
  }

  if (xgrid.accessControlFlag)
    xbuttons.push({
      tooltip: getLocMsg("js_kayit_bazli_yetkilendirme"),
      cls: "x-btn-icon x-grid-record-privilege",
      disabled: true,
      _activeOnSelection: true,
      _grid: xgrid,
      handler: fnRecordPrivileges
    });

  if (false && xgrid.logFlags) {
    xbuttons.push("-");
    var xmenu = [];
    if (xgrid.logFlags.edit)
      xmenu.push({
        text: getLocMsg("js_guncellenme_listesini_goster"),
        _grid: xgrid,
        handler: fnShowLog4Update
      });
    if (xgrid.logFlags.remove)
      xmenu.push({
        text: getLocMsg("js_show_deleted_records"),
        _grid: xgrid,
        handler: fnShowLog4Delete
      });
    xbuttons.push({
      // tooltip: getLocMsg("js_log"),
      cls: "x-btn-icon icon-log",
      _activeOnSelection: false,
      _grid: xgrid,
      menu: xmenu
    });
  }
}
function openFormSmsMail(tId, tPk, fsmId, fsmFrmId) {
  mainPanel.loadTab({
    attributes: {
      href:
        "showForm?_fid=650&_tableId=" +
        tId +
        "&_tablePk=" +
        tPk +
        "&_fsmId=" +
        fsmId +
        "&_fsmFrmId=" +
        fsmFrmId
    }
  });
}

function addDefaultSpecialButtons(xbuttons, xgrid) {
  var special = true;
  if (_app.mail_flag && 1 * _app.mail_flag && xgrid.sendMailFlag) {
    if (special) xbuttons.push("-");
    special = false;
    xbuttons.push({
      tooltip: getLocMsg("js_send_email"),
      cls: "x-btn-icon x-grid-mail",
      disabled: true,
      _activeOnSelection: true,
      _grid: xgrid,
      handler: fnSendMail
    });
  }
  if (xgrid.vcs) {
    xbuttons.push({
      // tooltip: getLocMsg("vcs"),
      cls: "x-btn-icon x-grid-vcs",
      _grid: xgrid,
      menu: fncMnuVcs(xgrid)
    });
  }
  if (
    (_app.form_conversion_flag &&
      1 * _app.form_conversion_flag &&
      xgrid.formConversionList) ||
    (_app.mail_flag && 1 * _app.mail_flag && xgrid.formSmsMailList)
  ) {
    if (!xgrid.menuButtons) xgrid.menuButtons = [];
    if (xgrid.menuButtons.length > 0) xgrid.menuButtons.push("-");
    if (
      _app.form_conversion_flag &&
      1 * _app.form_conversion_flag &&
      xgrid.formConversionList
    ) {
      for (var qz = 0; qz < xgrid.formConversionList.length; qz++) {
        xgrid.formConversionList[qz]._grid = xgrid;
        xgrid.formConversionList[qz].handler = function(aq, bq, cq) {
          var sels = getSels(aq._grid);
          if (!sels || !sels.length) return;
          if (aq.preview) {
            for (var qi = 0; qi < sels.length; qi++)
              mainPanel.loadTab({
                attributes: {
                  __grid:xgrid,
                  href:
                    "showForm?a=2&_fid=" +
                    aq._fid +
                    "&_cnvId=" +
                    aq.xid +
                    "&_cnvTblPk=" +
                    sels[qi].id
                }
              });
          } else {
            var pr = { _cnvId: aq.xid, _cnt: sels.length, form_id: aq._fid };
            for (var qi = 0; qi < sels.length; qi++)
              pr["srcTablePk" + (qi + 1)] = sels[qi].id;
            iwb.request({
              url: "ajaxPostEditGrid",
              params: pr,
              requestWaitMsg: true,
              successCallback: function(j) {
                Ext.infoMsg.alert("info", j.msgs.join("<br>"));
              }
            });
          }
        };
      }
      xgrid.menuButtons.push({
        text: getLocMsg("convert"),
        id: "cnv_mn_" + xgrid.id,
        iconCls: "icon-operation",
        menu: xgrid.formConversionList
      });
    }

    if (_app.mail_flag && 1 * _app.mail_flag && xgrid.formSmsMailList) {
      for (var qz = 0; qz < xgrid.formSmsMailList.length; qz++) {
        xgrid.formSmsMailList[qz]._grid = xgrid;
        xgrid.formSmsMailList[qz].handler = function(aq, bq, cq) {
          var sel = getSel(aq._grid);
          if (!sel) return;
          mainPanel.loadTab({
            attributes: {
              href:
                "showForm?_fid=" +
                (1 * aq.smsMailTip ? 650 : 631) +
                "&_tableId=" +
                aq._grid.crudTableId +
                "&_tablePk=" +
                sel.id +
                "&_fsmId=" +
                aq.xid +
                "&_fsmFrmId=" +
                aq._grid.crudFormId
            }
          });
        };
      }
      xgrid.menuButtons.push({
        text: getLocMsg("js_send_email"),
        id: "mailsms_mn_" + xgrid.id,
        iconCls: "icon-email",
        menu: xgrid.formSmsMailList
      });
      // xbuttons.push({tooltip:getLocMsg('js_send_email'),cls:'x-btn-icon
		// x-grid-mail', disabled:true, _activeOnSelection:true, _grid:xgrid,
		// menu:xgrid.formSmsMailList});
    }
  }

  special = true;
  if (
    _app.file_attachment_flag &&
    1 * _app.file_attachment_flag &&
    xgrid.fileAttachFlag
  ) {
    if (xbuttons.length > 0) xbuttons.push("-");
    special = false;

    if (xgrid.gridId * 1 != 1082) {
      xbuttons.push({
        id: "btn_attachments_" + xgrid.id,
        // tooltip: getLocMsg("js_iliskili_dosyalar"),
        cls: "x-btn-icon x-grid-attachment",
        disabled: true,
        _activeOnSelection: true,
        _grid: xgrid,
        menu: [
          {
            text: getLocMsg("attach_files"),
            _grid: xgrid,
            handler: fnNewFileAttachment
          },
          {
              text: getLocMsg("related_files"),
              _grid: xgrid,
              handler: function(a, b, c) {
                var xgrid = a._grid;
                var sel = getSel(a._grid);
                if(!sel)return;
                var cfg = {
                  attributes: {
                    modalWindow: true,
                    href: "showPage?_tid="+ (_scd.customFile ? 6813:9),
                    baseParams: {
                      xtable_id: xgrid.crudTableId,
                      xtable_pk: sel.id
                    }
                  }
                };
               // cfg.attributes._title_ = getForm.name;
                mainPanel.loadTab(cfg);
              }
          }
        
        ]
      });
    } else {
      xbuttons.push({
        tooltip: getLocMsg("attach_files"),
        cls: "x-btn-icon x-grid-attachment",
        disabled: true,
        _activeOnSelection: true,
        _grid: xgrid,
        handler: fnNewFileAttachment
      });
    }
  }
  if (
    _app.make_comment_flag &&
    1 * _app.make_comment_flag &&
    xgrid.makeCommentFlag
  ) {
    if (special) xbuttons.push("-");
    special = false;
    xbuttons.push({
      id: "btn_comments_" + xgrid.id,
      tooltip: getLocMsg("js_yorumlar"),
      cls: "x-btn-icon x-grid-comment",
      disabled: true,
      _activeOnSelection: true,
      _grid: xgrid,
      handler: fnRecordComments
    });
  }
  if (xgrid.approveBulk) {
    if (!xgrid.menuButtons) xgrid.menuButtons = [];
    if (xgrid.menuButtons.length > 0) xgrid.menuButtons.push("-");
    submenu = [];
    if (xgrid.btnApproveRequest) {
      submenu.push({
        text: getLocMsg("request_approval"),
        _grid: xgrid,
        handler: function(a, e) {
          var sels = getSels(a._grid);
          if (sels.length == 0) {
            Ext.Msg.show({
              title: getLocMsg("error"),
              msg: getLocMsg("commons.error.secim"),
              icon: Ext.MessageBox.ERROR
            });
            return;
          }

          for (var i = 0; i < sels.length; i++) {
            if (sels[i].data.pkpkpk_arf * 1 < 0) {
              Ext.Msg.show({
                title: getLocMsg("error"),
                msg: getLocMsg("js_onay_adiminda_yer_almiyorsunuz"),
                icon: Ext.MessageBox.ERROR
              });
              return;
            }
            if (sels[i].data.pkpkpk_arf * 1 != 901) {
              Ext.Msg.show({
                title: getLocMsg("error"),
                msg: getLocMsg("secilenler_onay_istenecek_olmali"),
                icon: Ext.MessageBox.ERROR
              });
              return;
            }
          }
          approveTableRecords(901, a);
        }
      });
    }

    submenu.push({
      text: getLocMsg("approve"),
      _grid: xgrid,
      handler: function(a, e) {
        approveTableRecords(1, a);
      }
    });

    submenu.push({
      text: getLocMsg("reject"),
      _grid: xgrid,
      handler: function(a, e) {
        approveTableRecords(0, a);
      }
    });

    submenu.push({
      text: getLocMsg("return"),
      _grid: xgrid,
      handler: function(a, e) {
        approveTableRecords(2, a);
      }
    });
    xgrid.menuButtons.push({
      text: getLocMsg("approve"),
      iconCls: "icon-operation",
      menu: submenu
    });
  }
}

function addGridExtraButtons(xbuttons, xgrid) {
  if (!xgrid.extraButtons) return;
  if (xbuttons.length > 0) xbuttons.push("-");
  var report_menu = [];
  var toolbar_menu = [];
  for (var j = 0; j < xgrid.extraButtons.length; j++) {
    xgrid.extraButtons[j]._grid = xgrid;
    xgrid.extraButtons[j].disabled = xgrid.extraButtons[j]._activeOnSelection;

    if (
      xgrid.extraButtons[j].ref &&
      xgrid.extraButtons[j].ref.indexOf("../report_") == 0
    ) {
      report_menu.push(xgrid.extraButtons[j]);
    } else {
      // if(toolbar_menu.length>0){toolbar_menu.push('-');}// toolbarlar
		// arasına otomatik | koyar.
      toolbar_menu.push(xgrid.extraButtons[j]);
    }
  }
  xgrid.extraButtons = [];

  if (report_menu.length != 0) {
    xgrid.extraButtons.push({
      // tooltip: getLocMsg("js_report"),
      cls: "x-btn-icon x-grid-report",
      _activeOnSelection: false,
      _grid: xgrid,
      menu: report_menu
    });
    xgrid.extraButtons.push("-");
  }
  if (toolbar_menu.length != 0) xgrid.extraButtons.push(toolbar_menu);

  xbuttons.push(xgrid.extraButtons);
}

function addDefaultReportButtons(xbuttons, xgrid, showMasterDetailReport) {
  if (!xgrid.helpButton) {
    xbuttons.push("->");
    if (xgrid.displayInfo) xbuttons.push("-");
  }
  var xxmenu = [];
  xxmenu.push({
    text: getLocMsg("pdf"),
    _activeOnSelection: false,
    _grid: xgrid,
    handler: fnExportGridData("pdf")
  });
  xxmenu.push({
    text: getLocMsg("excel"),
    _activeOnSelection: false,
    _grid: xgrid,
    handler: fnExportGridData("xls")
  });
  xxmenu.push({
    text: getLocMsg("csv"),
    _activeOnSelection: false,
    _grid: xgrid,
    handler: fnExportGridData("csv")
  });
  xxmenu.push({
    text: getLocMsg("text"),
    _activeOnSelection: false,
    _grid: xgrid,
    handler: fnExportGridData("txt")
  });
  if (false && showMasterDetailReport) {
    xxmenu.push("-");
    xxmenu.push({
      text: "MasterDetail -> " + getLocMsg("js_excele_aktar"),
      _activeOnSelection: false,
      _grid: xgrid,
      handler: fnExportGridDataWithDetail("xls")
    });
  }
  if (_app.bi_flag && 1*_app.bi_flag && xgrid.ds && !xgrid.master_column_id) {
    xxmenu.push("-");
    if (xgrid.crudTableId) {
      xxmenu.push({
        text: getLocMsg("graph"),
        _activeOnSelection: false,
        _grid: xgrid,
        handler: fnGraphGridDataTree
      });
      xxmenu.push({
        text: "BI",
        menu: [
          {
            text: "Data List",
            _grid: xgrid,
            handler: function(aq) {
              openPopup(
                "showPage?_tid=784&xtable_id=" + aq._grid.crudTableId,
                "_blank",
                1200,
                800,
                1
              );
            }
          },
          {
            text: "Pivot Table",
            _grid: xgrid,
            handler: function(aq) {
              openPopup(
                "showPage?_tid=1200&xtable_id=" +
                  aq._grid.crudTableId +
                  (showMasterDetailReport
                    ? "&xmaster_table_id=" + aq._grid._masterGrid.crudTableId
                    : ""),
                "_blank",
                1200,
                800,
                1
              );
            }
          }
        ]
      });
      // xxmenu.push({text:'Pivot Table',_activeOnSelection:false,
		// _grid:xgrid,
		// handler:function(aq){openPopup('showPage?_tid=1200&xtable_id=' +
		// aq._grid.crudTableId, '_blank', 1200, 800, 1);}});
    } else {
      xxmenu.push({
        text: getLocMsg("graph"),
        _activeOnSelection: false,
        _grid: xgrid,
        handler: fnGraphGridData
      });
      xxmenu.push({
        text: "BI",
        menu: [
          {
            text: "Data List",
            _grid: xgrid,
            handler: function(aq) {
              openPopup(
                "showPage?_tid=784&xquery_id=" + aq._grid.queryId,
                "_blank",
                1200,
                800,
                1
              );
            }
          },
          {
            text: "Pivot Table",
            _grid: xgrid,
            handler: function(aq) {
              openPopup(
                "showPage?_tid=2395&xquery_id=" + aq._grid.queryId,
                "_blank",
                1200,
                800,
                1
              );
            }
          }
        ]
      });
    }
  }
  
  if(xgrid.gsheet || (_app.grid2gsheet && 1*_app.grid2gsheet))xxmenu.push('-',{
      id: "btn_gsheet_" + xgrid.id,
      text: '<i class="icon-social-google"></i>Sheet',
      tooltip:'Export to Google Spreadsheet',
      cls: "x-btn-no-icon",
      _activeOnSelection: false,
      _grid: xgrid,
      ref: "../btnGSheet",
      handler: xgrid.fnGSheet || fnGSheet
  });
  xbuttons.push({
    id: "btn_reports_" + xgrid.id,
    // tooltip: getLocMsg("reports"),
    cls: "x-btn-icon x-grid-pdf",
    _activeOnSelection: false,
    _grid: xgrid,
    menu: xxmenu
  });
}

function addDefaultGridPersonalizationButtons(xbuttons, xgrid) {
  xbuttons.push({
    id: "grd_pers_buttons" + xgrid.gridId,
    text: getLocMsg("js_kisisellestir"),
    _grid: xgrid,
    menu: {
      items: [
        {
          text: getLocMsg("js_bu_ayarlari_kaydet"),
          iconCls: "icon-ekle",
          _grid: xgrid,
          handler: function(ax, bx, cx) {
            var pdsc = prompt(getLocMsg("js_yeni_goruntu_adi"));
            if (!pdsc) return;
            var g = ax._grid,
              cols = "",
              sort = "",
              cells = "";
            for (var z = 0; z < g.columns.length; z++)
              cols +=
                ";" +
                g.columns[z].dataIndex +
                "," +
                g.columns[z].width +
                "," +
                (!g.columns[z].hidden ? 1 : 0);
            if (g.ds.sortInfo && g.ds.sortInfo.field) {
              sort = g.ds.sortInfo.field;
              if (g.ds.sortInfo.direction)
                sort += " " + g.ds.sortInfo.direction;
            }
            var params = {
              pcolumns: cols.substr(1),
              pdsc: pdsc,
              pgrid_id: ax._grid.gridId,
              psort_dsc: sort
            };
            if (g.searchForm) {
              var fp = g.ds._formPanel;
              if (fp) {
                var m = fp.getForm().getValues();
                for (var qi in m)
                  if (m[qi])
                    cells += ";" + qi + "," + m[qi].replace(/\,/g, "~");
                if (cells) params.psfrm_cells = cells.substr(1);
              }
              params.pgrid_height = g._gp.getHeight();
              params.psfrm_visible_flag = fp.collapsed ? 0 : 1;
            }

            iwb.request({
              requestWaitMsg: true,
              url: "ajaxExecDbFunc?_did=648",
              params: params,
              successCallback: function(j) {
                Ext.infoMsg.alert(
                  "success",
                  getLocMsg("js_mazgal_yeni_ayarlarla_gorunecek")
                );
              }
            });
          }
        },
        {
          text: getLocMsg("js_kaydedilenleri_duzenle"),
          _grid: xgrid,
          handler: function(ax, bx, cx) {
            mainPanel.loadTab({
              attributes: {
                _title_: ax._grid.name,
                modalWindow: true,
                href: "showPage?_tid=238&_gid1=851&tgrid_id=" + ax._grid.gridId,
                _pk: { tuser_grid_id: "user_grid_id" },
                baseParams: { tgrid_id: ax._grid.gridId }
              }
            });
          }
        }
      ]
    }
  });
}

function addDefaultPrivilegeButtons(xbuttons, xgrid) {
  if (_scd.administratorFlag || _scd.customizationId == 0) {
    if(!xgrid.gridReport)xbuttons.push("->");
    var xxmenu = [],
      bx = false;
    if (_scd.customizationId == 0) {
      xxmenu.push({
        text: getLocMsg("settings"),
        cls: "x-btn-icon x-grid-setting",
        _activeOnSelection: false,
        _grid: xgrid,
        handler: fnGridSetting
      });
      bx = true;
    }
    if (_scd.customizationId == 0) {
      xxmenu.push({
        text: getLocMsg("access_control"),
        cls: "x-btn-icon x-grid-privilege",
        _activeOnSelection: false,
        _grid: xgrid,
        handler: fnGridPrivilege
      });
      bx = true;
    }
    if (false && xgrid.crudTableId) {
      if (bx) xxmenu.push("-");
      else bx = true;
      xxmenu.push({
        text: "Detail Form+ Builder",
        cls: "x-btn-icon x-grid-setting",
        _activeOnSelection: false,
        _grid: xgrid,
        handler: function(ax) {
          return mainPanel.loadTab({
            attributes: {
              href:
                "showPage?_tid=8&parent_table_id=" +
                ax._grid.crudTableId +
                "&tpl_id=" +
                ax._grid.tplInfo.id +
                "&po_id=" +
                ax._grid.tplInfo.objId
            }
          });
        }
      });
    }
    if (false && xgrid.saveUserInfo) {
      if (bx) xxmenu.push("-");
      else bx = true;
      addDefaultGridPersonalizationButtons(xxmenu, xgrid);
    }
    xbuttons.push({
      // tooltip: getLocMsg("js_ayarlar"),
      cls: "x-btn-icon x-grid-setting",
      _activeOnSelection: false,
      _grid: xgrid,
      menu: xxmenu
    });
  }
}

function addDefaultCommitButtons(xbuttons, xgrid) {
  xgrid.editMode = xgrid.onlyCommitBtn || false;
  if (xbuttons.length > 0 || xgrid.pageSize) xbuttons.push("-");
  if (xgrid.crudTableId)
    xbuttons.push({
      id: "btn_commit_" + xgrid.id,
      tooltip: getLocMsg("commit"),
      cls: "x-btn-icon x-grid-commit",
      disabled: !xgrid.editMode,
      _activeOnSelection: false,
      ref: "../btnCommit",
      _grid: xgrid,
      handler: xgrid.fnCommit || fnCommit
    });
  if (!xgrid.onlyCommitBtn)
    xbuttons.push({
      id: "btn_edit_mode_" + xgrid.id,
      tooltip: getLocMsg("edit_mode"),
      cls: "x-btn-icon x-grid-startedit",
      _activeOnSelection: false,
      _grid: xgrid,
      ref: "../btnEditMode",
      enableToggle: true,
      toggleHandler: fnToggleEditMode
    });
}

function exportToGSheet(a, url){
    var g = a._grid;
    if (false && g.ds.getTotalCount() == 0) {
        Ext.infoMsg.alert("info", getLocMsg("no_data"));
        return;
    }
    


    var cols = "";
    for (var z = 0; z < g.columns.length; z++) {
      if (!g.columns[z].hidden && g.columns[z].dataIndex)
        cols += ";" + g.columns[z].dataIndex + "," + g.columns[z].width;
    }
	iwb.ajax.execFunc(1962, Object.assign({_mask:!0, _columns:cols.substr(1), _url:url, _gridId:a._grid.gridId, _access_token:_scd.googleAccessToken}, a._grid.ds.baseParams||{}),
			(mj)=>{
				if(mj.result){
					if(mj.result.pout_error){
						Ext.infoMsg.msg("error", mj.result.pout_error);
						_scd.googleAccessToken = null;
						Ext.infoMsg.msg("success", "Try again please");

					} else {
						_scd.googleSheetUrl = url;
						localStorage.setItem('googleSheetUrl', url);
						var pid2 = 'px-'+_scd.googleAccessToken;
						if(!Ext.getCmp(pid2)){
							if(url.indexOf('#gid=')>-1){
								url = url.split('#gid=')[0]+'#gid='+mj.result.pout_sheet_id;
							} else url += '#gid='+mj.result.pout_sheet_id;
							if(url.indexOf('&rm=minimal')==-1)
								url+='&rm=minimal';
							var px = new Ext.Panel({
					/*			tbar:[{text:'Refresh', _result: mj.result, id:'tb-'+_webPageId+a._grid.gridId,handler:()=>
								{alert('TODO')}}],*/
							  closable: !0, title: '<i class="icon-social-google"></i> Google Sheet'  , id: pid2
							  , html: '<iframe style="border:none;width:100%;height:100%" src="' + url + '"></iframe>'
							});
							mainPanel.add(px); 
						}
	
						mainPanel.setActiveTab(pid2);
					}
					//mainPanel.loadTab({attributes:{href:'showPage?_tid=1201', id:_scd.googleAccessToken ,url:url, title:g.name}})
				}
			});
}

function fnGSheet(a){
	var gwin2= null;
	iwb.googleAuth2 = (waccess_token) => {
	    _scd.googleAccessToken = waccess_token;
	    gwin2.destroy(); gwin2 = null;
	    exportToGSheet(a, gsu);
	}
	
	var gsu = prompt('Enter the Google Spreadsheet URL', _scd.googleSheetUrl||localStorage.getItem('googleSheetUrl')||'');
	if(!gsu)return;

	if(!_scd.googleAccessToken){
		var xwid = 'id-xx-' + _webPageId;
		gwin2 = new Ext.Window({ id: xwid, modal:!0, title: 'Google Authentication', closable: !0, width: 500, height: 180, 
            html: '<iframe style="border:none; width:100%;" src="showPage?_tid=1202&callback=googleAuth2">' });
        gwin2.show();
	} else {
		exportToGSheet(a,gsu)
	}
	
}
function addTab4GridWSearchForm(obj) {
  var mainGrid = obj.grid,
    searchFormPanel = null;
  if (obj.pk) mainGrid._pk = obj.pk; // {tcase_id:'case_id',tclient_id:'client_id',tobject_tip:'!4'}

  var grdExtra = {
// stripeRows: true,
    region: "center", cls:'iwb-grid-'+mainGrid.gridId+ ' x-master-grid',
    border: false, 
    clicksToEdit: 1 * _app.edit_grid_clicks_to_edit
  };
  if (obj.t) mainGrid.id = obj.t + "-" + mainGrid.gridId;

  var buttons = [];
  if (!mainGrid.pageSize) {
    // refresh buttonu
    buttons.push({
      id: "btn_refresh_" + mainGrid.id,
      tooltip: getLocMsg("refresh"),
      iconCls: "x-tbar-loading",
      _activeOnSelection: false,
      _grid: mainGrid,
      handler: function(a) {
        iwb.reload(a._grid,{
          params: a._grid._gp.store._formPanel?a._grid._gp.store._formPanel.getForm().getValues():{}
        });
      }
    });
  }
  if (mainGrid.editGrid) addDefaultCommitButtons(buttons, mainGrid);
  if (mainGrid.crudFlags) addDefaultCrudButtons(buttons, mainGrid);
  if (mainGrid.moveUpDown) addMoveUpDownButtons(buttons, mainGrid);
  addDefaultSpecialButtons(buttons, mainGrid);
  addGridExtraButtons(buttons, mainGrid);

  if (mainGrid.menuButtons) {
    for (var j = 0; j < mainGrid.menuButtons.length; j++) {
      mainGrid.menuButtons[j]._grid = mainGrid;
    }
    /*
	 * mainGrid.menuButtons = new Ext.menu.Menu({ enableScrolling: false, items:
	 * mainGrid.menuButtons });
	 */
    if (1 * _app.toolbar_edit_btn) {
      if (buttons.length > 0) buttons.push("-");
      buttons.push({
        id: "btn_operations_" + mainGrid.id,
        cls: "x-btn-icon x-grid-menu",
        disabled: true,
        _activeOnSelection: true,
        menu: mainGrid.menuButtons
      });
    }
  }

  // addHelpButton(buttons, mainGrid, 64, mainGrid.extraOutMap.tplId);
  if (mainGrid.gridReport) addDefaultReportButtons(buttons, mainGrid);

  addDefaultPrivilegeButtons(buttons, mainGrid);

  if (mainGrid.pageSize) {
    // paging'li toolbar
    var tbarExtra = {
      xtype: "paging",
      store: mainGrid.ds,
      pageSize: mainGrid.pageSize,
      displayInfo: !0
    };
    if (buttons.length > 0) tbarExtra.items = organizeButtons(buttons);
    grdExtra.tbar = tbarExtra;
  } else if (buttons.length > 0) {
    // standart toolbar
    grdExtra.tbar = organizeButtons(buttons);
  }

  // grid
  var eg = mainGrid.master_column_id
    ? mainGrid.editGrid
      ? Ext.ux.maximgb.tg.EditorGridPanel
      : Ext.ux.maximgb.tg.GridPanel
    : mainGrid.editGrid
      ? Ext.grid.EditorGridPanel
      : Ext.grid.GridPanel;
  var mainGridPanel = new eg(Ext.apply(mainGrid, grdExtra));
  mainGrid._gp = mainGridPanel;
  if (mainGrid.editGrid) {
    mainGridPanel.getColumnModel()._grid = mainGrid;
    if (!mainGrid.onlyCommitBtn) {
      mainGridPanel.getColumnModel().isCellEditable = function(
        colIndex,
        rowIndex
      ) {
        if (
          this._grid._isCellEditable &&
          this._grid._isCellEditable(colIndex, rowIndex, this._grid) === false
        )
          return false;
        return this._grid.editMode;
      };
    } else if (mainGrid._isCellEditable)
      mainGridPanel.getColumnModel().isCellEditable = function(
        colIndex,
        rowIndex
      ) {
        return this._grid._isCellEditable(colIndex, rowIndex, this._grid);
      };
  }

  if (buttons.length > 0) {
    mainGridPanel.getSelectionModel().on("selectionchange", function(a, b, c) {
      if (!a || !a.grid) return;
      var titems = a.grid.getTopToolbar().items.items;
      for (var ti = 0; ti < titems.length; ti++) {
        if (titems[ti]._activeOnSelection)
          titems[ti].setDisabled(!a.hasSelection());
      }
    });
  }
  var items = [];
  
  if (
      mainGrid.crudFlags &&
      mainGrid.crudFlags.edit &&
      !mainGrid.crudFlags.nonEditDblClick /* && 1*_app.toolbar_edit_btn */
    ) {
      mainGridPanel.on("rowdblclick", fnRowEditDblClick);
    }
  // ---search form
  if (mainGrid.searchForm) {
    searchFormPanel = new Ext.FormPanel(
      Ext.apply(mainGrid.searchForm.render(), {
          region: "north", autoHeight: true,anchor: "100%",
// region: "west", width:300,
        cls:'iwb-search-form', // collapseMode: 'mini',
        collapsible: !iwb.noCollapsibleSearchForm, animate: false, animCollapse: false, animFloat:false,
        title: mainGrid.name,
        border: false,
        // tools:searchFormTools,
        keys: {
          key: 13,
          fn: mainGridPanel.store.reload,
          scope: mainGridPanel.store
        }
      })
    );


    // --standart beforeload, ondbliclick, onrowcontextmenu


    if (mainGrid.menuButtons /* && !1*_app.toolbar_edit_btn */) {
      mainGridPanel.messageContextMenu = mainGrid.menuButtons;
      mainGridPanel.on("rowcontextmenu", fnRightClick);
    }

    mainGridPanel.store._formPanel = searchFormPanel;
    mainGridPanel.store._grid = mainGrid;
    mainGridPanel.store.on("beforeload", function(a, b) {
      if (a) {
        if (a._grid.editMode) a._grid._deletedItems = [];
        if (a._formPanel.getForm())
          a.baseParams = Ext.apply(
            a._grid._baseParams || {},
            a._formPanel.getForm().getValues()
          );
        if (a._grid && a._grid._gp && a._grid._gp._tid) {
          var c = Ext.getCmp(a._grid._gp._tid);
          if (c && c._title) {
            c.setTitle(c._title);
            c._title = false;
          }
        }
      }
    });
    if(mainGrid.displayAgg){
    	searchFormPanel.on('afterrender',(aq)=>{
    		aq.add(new Ext.Panel({ html: '<span id="top-summary-'+mainGrid.id+'" style="padding: 10px 0 10px 88px;"></span>', autoWidth: true, height:40, border: true }));
    	});
    	 mainGridPanel.store.on("load", function(a) {
			 var d =document.getElementById('top-summary-'+mainGrid.id);
			 if(!d)return;
    		 var m = a.reader.jsonData.extraOutMap;
    		 var s = "";
    		 if(m){
    			 mainGrid.displayAgg.map(o=>{
    				 var xx = o.f(m[o.id]); 
    				if(xx)s+=xx; 
    			 });
    		 }
			 d.innerHTML= s;
    	 });
    }
    items.push(searchFormPanel);
  }

  items.push(mainGridPanel);
  var p = {
    title: obj._title_ || mainGrid.name,
    border: false,
    closable: true,
    layout: "border",
    items: items,
    refreshGrids: obj._dontRefresh ? null : [mainGridPanel]
  };
  // p.iconCls='icon-cmp';
  if (obj.t) {
    p.id = obj.t;
    mainGridPanel._tid = obj.t;
  }
  p = new Ext.Panel(p);
  p._windowCfg = { layout: "border" };
  p._callCfg = obj;
  if (mainGrid.liveSync) p._lg = true;
  if (mainGrid.searchForm) p._formId = mainGrid.searchForm.formId;
  return p;
}

function organizeButtons(items) {
  if (!items) return null;
  for (var q = 0; q < items.length; q++) {
    if (items[q]._text) items[q].tooltip = items[q]._text;
  }
  return items;
}

function fnCardSearchListener(card){
	return function(ax,e){
		if(!ax._delay){
			ax._delay = new Ext.util.DelayedTask(function() {
				if(card.pageSize){
					if(!card.store.baseParams)card.store.baseParams={};
					card.store.baseParams.xdsc=ax.getValue();
					card.store.reload();
				} else if(card.fnSearch){
					card.fnSearch(ax.getValue())
				} else {
					if(!ax.getValue())card.store.clearFilter();
					else card.store.filter(card._filterField, ax.getValue(), true);
				}
			});
		}
		ax._delay.delay(200);
	}
}
function addTab4GridWSearchFormWithDetailGrids(obj, master_flag) {
  var mainGrid = obj.grid;
  if (obj.pk) mainGrid._pk = obj.pk;

  var grdExtra = Ext.apply(
    {
      region: obj.region || (mainGrid.gridId?"north":"west"),cls:'iwb-grid-'+mainGrid.gridId+ (!master_flag?' x-master-grid':''),
      autoScroll: true,
      border: false
    },
    obj.grdExtra || {
      split: true,
// stripeRows: true,
      border: false,
      clicksToEdit: 1 * _app.edit_grid_clicks_to_edit
    }
  );
  if (obj.t) mainGrid.id = obj.t + "-" + (mainGrid.gridId || mainGrid.dataViewId);

  if (grdExtra.region == "north") {
    grdExtra.height = mainGrid.defaultHeight || 120;
    grdExtra.minSize = 90;
    grdExtra.maxSize = 300;
  } else {
    grdExtra.width = mainGrid.defaultWidth || 400;
    grdExtra.minSize = 200;
    if (grdExtra.width < 0) {
      grdExtra.width = -1 * grdExtra.width + "%";
    } else {
      grdExtra.maxSize = grdExtra.width + 100;
    }
  }

  var buttons = [];
  if (mainGrid.searchForm && !mainGrid.pageSize && mainGrid.gridId) {
    // refresh buttonu
    buttons.push({
      id: "btn_refresh_" + mainGrid.id,
      // tooltip: getLocMsg("js_refresh"),
      iconCls: "x-tbar-loading",
      _activeOnSelection: false,
      _grid: mainGrid,
      handler: function(a) {
        iwb.reload(a._grid, {
          params: a._grid._gp.store._formPanel.getForm().getValues()
        });
      }
    });
  }
  if (mainGrid.gridId && mainGrid.editGrid) addDefaultCommitButtons(buttons, mainGrid);
  if (mainGrid.crudFlags) addDefaultCrudButtons(buttons, mainGrid);
  addDefaultSpecialButtons(buttons, mainGrid);

  addGridExtraButtons(buttons, mainGrid);

  buttons.push('->');
  if (mainGrid.rmenu) {
    for (var j = 0; j < mainGrid.rmenu.length; j++) {
      mainGrid.rmenu[j]._grid = mainGrid;
    }
    mainGrid.rmenu = new Ext.menu.Menu({
      enableScrolling: false,
      items: mainGrid.rmenu
    });
    if (1 * _app.toolbar_edit_btn) {
      if (buttons.length > 0) buttons.push("-");
      buttons.push({
        // tooltip: getLocMsg("js_report"),
        cls: "x-btn-icon icon-report",
        disabled: true,
        _activeOnSelection: true,
        menu: mainGrid.rmenu
      });
    }
  }


  if (mainGrid.menuButtons) {
    for (var j = 0; j < mainGrid.menuButtons.length; j++) {
      mainGrid.menuButtons[j]._grid = mainGrid;
    }
    mainGrid.menuButtons = new Ext.menu.Menu({
      enableScrolling: false,
      items: mainGrid.menuButtons
    });
    if (1 * _app.toolbar_edit_btn) {
      if (buttons.length > 0) buttons.push("-");
      buttons.push({
        id: "btn_operations_" + mainGrid.id,
        cls: "x-btn-icon x-grid-menu",
        disabled: true,
        _activeOnSelection: true,
        menu: mainGrid.menuButtons
      });
    }
  }
  // if (master_flag && master_flag==1)addHelpButton(buttons,mainGrid, 5,
	// mainGrid.gridId);
  // else addHelpButton(buttons,mainGrid, 64, mainGrid.extraOutMap.tplId);

  mainGrid.isMainGrid = true;

  if(mainGrid.gridId){
	  if (mainGrid.gridReport) addDefaultReportButtons(buttons, mainGrid);
	  addDefaultPrivilegeButtons(buttons, mainGrid);

	  if (mainGrid.pageSize) {
	    // paging'li toolbar
	    var tbarExtra = {
	      xtype: "paging",
	      store: mainGrid.ds,
	      pageSize: mainGrid.pageSize,
	      displayInfo: !0
	    };
	    if (buttons.length > 0) tbarExtra.items = organizeButtons(buttons);
	    grdExtra.tbar = tbarExtra;
	  } else if (buttons.length > 0) {
	    // standart toolbar
	    grdExtra.tbar = organizeButtons(buttons);
	  }
  }

  var mainGridPanel = null;
  if(mainGrid.gridId){// grid
	  var eg = mainGrid.master_column_id
	    ? mainGrid.editGrid
	      ? Ext.ux.maximgb.tg.EditorGridPanel
	      : Ext.ux.maximgb.tg.GridPanel
	    : mainGrid.editGrid
	      ? Ext.grid.EditorGridPanel
	      : Ext.grid.GridPanel;
	  mainGridPanel = new eg(Ext.apply(mainGrid, grdExtra));
	  mainGrid._gp = mainGridPanel;
	  if (mainGrid.editGrid) {
	    mainGridPanel.getColumnModel()._grid = mainGrid;
	    if (!mainGrid.onlyCommitBtn) {
	      mainGridPanel.getColumnModel().isCellEditable = function(
	        colIndex,
	        rowIndex
	      ) {
	        if (
	          this._grid._isCellEditable &&
	          this._grid._isCellEditable(colIndex, rowIndex, this._grid) === false
	        )
	          return false;
	        return this._grid.editMode;
	      };
	    } else if (mainGrid._isCellEditable)
	      mainGridPanel.getColumnModel().isCellEditable = function(
	        colIndex,
	        rowIndex
	      ) {
	        return this._grid._isCellEditable(colIndex, rowIndex, this._grid);
	      };
	  } 
	} else { // card
		if(mainGrid.tpl && mainGrid.tpl.indexOf('<tpl')==-1)mainGrid.tpl='<tpl for=".">'+mainGrid.tpl+'</tpl>';
		mainGridPanel=new Ext.DataView(Ext.apply({emptyText: '<br>&nbsp; No Data',
		    singleSelect:!0, loadMask:!0, cls:'iwb-card-'+mainGrid.dataViewId,
		    itemSelector: 'div.card',autoScroll:false
		}, mainGrid));
		mainGrid._gp=mainGridPanel;
	}
  if (buttons.length > 0 && mainGrid.gridId) {
    mainGridPanel.getSelectionModel().on("selectionchange", function(a, b, c) {
      if (!a || !a.grid) return;
      var titems = a.grid.getTopToolbar().items.items;
      for (var ti = 0; ti < titems.length; ti++) {
        if (titems[ti]._activeOnSelection)
          titems[ti].setDisabled(!a.hasSelection());
      }
    });
  }
  if (mainGrid.menuButtons/* && mainGrid.gridId && !1*_app.toolbar_edit_btn */) {
    mainGridPanel.messageContextMenu = mainGrid.menuButtons;
    if(mainGrid.gridId)
    	mainGridPanel.on("rowcontextmenu", fnRightClick);
    else
    	mainGridPanel.on("contextmenu", fnCardRightClick);
  }
  // ---search form
  var searchFormPanel = null;
  if (mainGrid.searchForm) {
	  var sfCfg = {
		        region: "north",autoHeight: true, anchor: "100%",
// region: "west", width:300,
		        cls:'iwb-search-form',// collapseMode: 'mini',
		        collapsible: !iwb.noCollapsibleSearchForm, animate: false, animCollapse: false, animFloat:false,
		        title: mainGrid.gridId ? mainGrid.name : 'Advanced Search',
		        border: false,
		        id: "sf_" + (obj.t || Math.random()),
		        // tools:searchFormTools,
		        keys: {
		          key: 13,
		          fn: mainGridPanel.store.reload,
		          scope: mainGridPanel.store
		        }
		      };
	  if(mainGrid.dataViewId){
		  sfCfg.collapsed=true; sfCfg._grid=mainGrid; sfCfg.collapseMode= 'mini';
		  sfCfg.listeners={expand:function(ax,bx,cx){
			  Ext.getCmp('sf-card-'+obj.t).hide();
		  }, collapse:function(ax,bx,cx){
			  Ext.getCmp('sf-card-'+obj.t).show();
			  Ext.getCmp('sfb-card-'+obj.t).show();
			  mainGrid.store.baseParams={xdsc:Ext.getCmp('sf-card-'+obj.t).getValue()};
		  }};
		  sfCfg.bodyStyle='padding-bottom:5px;';
	  }
    searchFormPanel = (mainGrid.searchForm.fp = new Ext.FormPanel(
      Ext.apply(mainGrid.searchForm.render(), sfCfg)
    ));
    
    if(mainGrid.displayAgg){
    	searchFormPanel.on('afterrender',(aq)=>{
    		aq.add(new Ext.Panel({ html: '<span id="top-summary-'+mainGrid.id+'" style="padding: 10px 0 10px 48px;"></span>', autoWidth: true, height:40, border: true }));
    	});
    	 mainGridPanel.store.on("load", function(a) {
			 var d =document.getElementById('top-summary-'+mainGrid.id);
			 if(!d)return;
    		 var m = a.reader.jsonData.extraOutMap;
    		 var s = "";
    		 if(m){
    			 mainGrid.displayAgg.map(o=>{
    				 var xx = o.f(m[o.id]); 
    				if(xx)s+=xx; 
    			 });
    		 }
			 d.innerHTML= s;
    	 });
    }
    mainGridPanel.store._formPanel = searchFormPanel;
  }

  // --standart beforeload, ondbliclick, onrowcontextmenu
  if (
    mainGrid.crudFlags &&
    mainGrid.crudFlags.edit &&
    !mainGrid.crudFlags.nonEditDblClick /* && 1*_app.toolbar_edit_btn */
  ) {
    if(mainGrid.gridId)mainGridPanel.on("rowdblclick", fnRowEditDblClick);
    else mainGridPanel.on("dblclick", fnCardDblClick);
  }
  
  if(mainGrid.gridId){
	  mainGridPanel.store._grid = mainGrid;
	  mainGridPanel.store.on("beforeload", function(a, b) {
	    if (searchFormPanel) {
	      // mainGridPanel.store._formPanel = searchFormPanel;
	      
	      if (a._grid.editMode) a._grid._deletedItems = [];
	      a.baseParams = Ext.apply(
	        a._grid._baseParams || {},
	        a._formPanel.getForm().getValues()
	      ); // a._formPanel.getForm().getValues();
	    }
	    if (mainGridPanel.getSelectionModel().getSelected())
	      mainGridPanel._lastSelectedGridRowId = mainGridPanel
	        .getSelectionModel()
	        .getSelected().id;
	    if (a._grid && a._grid._gp && a._grid._gp._tid) {
	      var c = Ext.getCmp(a._grid._gp._tid);
	      if (c && c._title) {
	        c.setTitle(c._title);
	        c._title = false;
	      }
	    }
	  });
	  mainGridPanel.store.on("load", function(a, b) {
		    if (a.totalLength == 0) return;
		    var sm = mainGridPanel.getSelectionModel();
		    if (!sm.hasSelection()) sm.selectFirstRow();
		    if (
		      mainGridPanel._lastSelectedGridRowId &&
		      1 * mainGridPanel._lastSelectedGridRowId == 1 * sm.getSelected().id
		    ) {
		      mainGridPanel
		        .getSelectionModel()
		        .fireEvent("selectionchange", mainGridPanel.getSelectionModel());
		    }
	  });
  } else if(mainGrid.dataViewId){
	  mainGrid.store.on("beforeload", function(a, b) {
		  if(searchFormPanel && searchFormPanel.isVisible())a.baseParams = Ext.apply(
			        a._baseParams || {},
			        a._formPanel.getForm().getValues());
		  var sels = mainGrid._gp.getSelectedRecords(); 
		  mainGrid._lastSelectedGridRow = (sels && sels.length) ? sels[0] : null; 
	  });
	  mainGrid.store.on("load", function(a, b) {
	    if (a.totalLength == 0) return;
	    if(mainGrid._lastSelectedGridRow){
	    	var ix =  a.indexOfId(mainGrid._lastSelectedGridRow.id);
	    	if(ix>-1){
	    		mainGrid._gp.selectRange(ix,ix,false);
	    	}
	    }
	    if(!mainGrid._gp.getSelectionCount())mainGrid._gp.selectRange(0,0,false);		  
	  });

  }


  var mainButtons = buttons;
	  
  // detail tabs
  var detailGridPanels = [];

  if (obj.detailGrids.length > 1)
    obj.detailGrids.sort(function(a, b) {
      return (a.grid.tabOrder || -1) - (b.grid.tabOrder || -1);
    }); // gridler template object sirasina gore geliyor.
  for (var i = 0; i < obj.detailGrids.length; i++) {
	if (!obj.detailGrids[i])continue;
    if (obj.detailGrids[i].detailGrids) {
      // master/detail olacak
      if (!obj.detailGrids[i].grid.gridId) continue;

      delete obj.detailGrids[i].grid.searchForm; // Detail gridlerin
													// searchFormu olamaz.
													// Patlıyor.

      var xmxm = addTab4GridWSearchFormWithDetailGrids(Ext.apply({t:obj.t&&obj.detailGrids[i].grid?obj.t+'-'+obj.detailGrids[i].grid.gridId:null}, obj.detailGrids[i]), 1);
      obj.detailGrids[i].grid._masterGrid = mainGrid;
      if (xmxm.items.items[0].xtype == "form") {
        // ilk sıradaki gridin ,detail gridi varsa Search Formunu yok ediyor
        xmxm.items.items[0].destroy();
      }

      var detailGridPanel = xmxm.items.items[0].items.items[0];

      grid2grid(
        mainGridPanel,
        detailGridPanel,
        obj.detailGrids[i].params,
        xmxm
      );

      xmxm.closable = false;
      detailGridPanels.push(xmxm);
    } else {
      var detailGrid = obj.detailGrids[i].grid;
      if (!detailGrid || !detailGrid.gridId) continue;
      detailGrid._masterGrid = mainGrid;
      if (obj.t) detailGrid.id = obj.t + "-" + detailGrid.gridId;

      if (detailGrid._ready) {
        detailGridPanels.push(detailGrid);
        if(detailGrid._ready==2){
        	detailGrid._masterGrid = mainGridPanel;
        	grid2grid(mainGridPanel, detailGrid, obj.detailGrids[i].params);
        }
        continue;
      }
      if (obj.detailGrids[i].pk) detailGrid._pk = obj.detailGrids[i].pk;
      var grdExtra = {
        title: obj.detailGrids[i]._title_ || detailGrid.name,cls:'iwb-grid-'+detailGrid.gridId,
// stripeRows: true,
        id: "gr-" + obj.t + '-' + detailGrid.gridId, _posId:i,
        border: false,
// bodyStyle: "border-top: 1px solid #18181a;",
        autoScroll: true,
        clicksToEdit: 1 * _app.edit_grid_clicks_to_edit
      };
      var buttons = [];

      if (detailGrid.editGrid) addDefaultCommitButtons(buttons, detailGrid);

      if (detailGrid.hasFilter) {
        if (buttons.length > 0) buttons.push("-");
        buttons.push({
          // tooltip: getLocMsg("remove_filter"),
          cls: "x-btn-icon x-grid-funnel",
          _grid: detailGrid,
          handler: fnClearFilters
        });
      }

      if (detailGrid.crudFlags) addDefaultCrudButtons(buttons, detailGrid);
 // if (detailGrid.moveUpDown) addMoveUpDownButtons(buttons, detailGrid);
      addDefaultSpecialButtons(buttons, detailGrid);
      addGridExtraButtons(buttons, detailGrid);

      if(detailGrid.displayAgg){
    	  buttons.push({id:'grid-summary-'+detailGrid.id, xtype:'displayfield', value:''});
    	  var ds = (detailGrid.store || detailGrid.ds);
    	  ds._displayAgg = detailGrid.displayAgg;
    	  ds._id = detailGrid.id;
    	  ds.on("load", function(a) {
   			 var d =document.getElementById('grid-summary-'+a._id);
   			 if(!d)return;
      		 var m = a.reader.jsonData.extraOutMap;
      		 var s = "";
      		 if(m){
      			 a._displayAgg.map(o=>{
      				 var xx = o.f(m[o.id]); 
      				if(xx)s+=xx; 
      			 });
      		 }
  			 d.innerHTML= s;
      	 });
      }

      if (detailGrid.menuButtons) {
        for (var j = 0; j < detailGrid.menuButtons.length; j++) {
          detailGrid.menuButtons[j]._grid = detailGrid;
        }
        detailGrid.menuButtons = new Ext.menu.Menu({
          enableScrolling: false,
          items: detailGrid.menuButtons
        });
        if (1 * _app.toolbar_edit_btn) {
          if (buttons.length > 0) buttons.push("-");
          buttons.push({
            id: "btn_operations_" + detailGrid.id,
            cls: "x-btn-icon x-grid-menu",
            disabled: true,
            _activeOnSelection: true,
            menu: detailGrid.menuButtons
          });
        }
      }

      // addHelpButton(buttons, detailGrid, 5, detailGrid.gridId);
      if (detailGrid.gridReport)
        addDefaultReportButtons(buttons, detailGrid, true);
      addDefaultPrivilegeButtons(buttons, detailGrid);

      if (detailGrid.pageSize) {
        // paging'li toolbar
        var tbarExtra = {
          xtype: "paging",
          store: detailGrid.ds,
          pageSize: detailGrid.pageSize,
          displayInfo: detailGrid.displayInfo
        };
        if (buttons.length > 0) tbarExtra.items = organizeButtons(buttons);
        grdExtra.tbar = tbarExtra;
      } else if (buttons.length > 0) {
        // standart toolbar
        grdExtra.tbar = organizeButtons(buttons);
      }

      var eg = detailGrid.master_column_id
        ? detailGrid.editGrid
          ? Ext.ux.maximgb.tg.EditorGridPanel
          : Ext.ux.maximgb.tg.GridPanel
        : detailGrid.editGrid
          ? Ext.grid.EditorGridPanel
          : Ext.grid.GridPanel;
      var detailGridPanel = new eg(Ext.apply(detailGrid, grdExtra));
      detailGrid._gp = detailGridPanel;
      if (detailGrid.editGrid) {
        detailGridPanel.getColumnModel()._grid = detailGrid;
        if (!detailGrid.onlyCommitBtn) {
          detailGridPanel.getColumnModel().isCellEditable = function(
            colIndex,
            rowIndex
          ) {
            if (
              this._grid._isCellEditable &&
              this._grid._isCellEditable(colIndex, rowIndex, this._grid) ===
                false
            )
              return false;
            return this._grid.editMode;
          };
        } else if (detailGrid._isCellEditable)
          mainGridPanel.getColumnModel().isCellEditable = function(
            colIndex,
            rowIndex
          ) {
            return this._grid._isCellEditable(colIndex, rowIndex, this._grid);
          };
      }

      if (detailGrid.menuButtons /* && !1*_app.toolbar_edit_btn */) {
        detailGridPanel.messageContextMenu = detailGrid.menuButtons;
        detailGridPanel.on("rowcontextmenu", fnRightClick);
      }
      /*
		 * if(detailGrid.saveUserInfo)detailGridPanel.on("afterrender",function(a,b,c){
		 * detailGridPanel.getView().hmenu.add('-',{text: 'Mazgal
		 * Ayarları',cls:'grid-options1',menu: {items:[{text:'Mazgal Ayarlarını
		 * Kaydet',handler:function(){saveGridColumnInfo(grid.getColumnModel(),mainGrid.gridId)}},
		 * {text:'Varsayılan Ayarlara
		 * Dön',handler:function(){resetGridColumnInfo(mainGrid.gridId)}}]}});
		 * });
		 */
      if (
        detailGridPanel.crudFlags &&
        detailGridPanel.crudFlags.edit &&
        !detailGridPanel.crudFlags
          .nonEditDblClick /* && 1*_app.toolbar_edit_btn */
      ) {
        detailGridPanel.on("rowdblclick", fnRowEditDblClick);
      }

      grid2grid(mainGridPanel, detailGridPanel, obj.detailGrids[i].params);
      if (buttons.length > 0) {
        detailGridPanel
          .getSelectionModel()
          .on("selectionchange", function(a, b, c) {
            if (!a || !a.grid) return;
            var titems = a.grid.getTopToolbar().items.items;
            for (var ti = 0; ti < titems.length; ti++) {
              if (titems[ti]._activeOnSelection)
                titems[ti].setDisabled(!a.hasSelection());
            }
          });
      }
      detailGridPanels.push(detailGridPanel);
    }
  }
  var scrollerMenu = new Ext.ux.TabScrollerMenu({
    maxText: 15,
    pageSize: 5
  });
  var lastItems = [];
  if (mainGrid.gridId && searchFormPanel != null) {
    lastItems.push(searchFormPanel);
  }

  var _posId = window.localStorage.getItem('sub-tab-'+(mainGrid.gridId || mainGrid.dataViewId));
  var _posId2 = _posId ? parseInt(_posId) : 0;
  if(!_posId2 || _posId2>=detailGridPanels.length)_posId2=0;
  var subTab = {
    region: "center",
    enableTabScroll: true,
    activeTab: 0/*_posId2*/, cls:'iwb-detail-tab',
    border: false,
    visible: false,
    items: detailGridPanels, 
    listeners:{
    	tabchange: function(t, p) {
    		if(typeof p._posId!='undefined')window.localStorage.setItem('sub-tab-'+(mainGrid.gridId || mainGrid.dataViewId), p._posId);
    	}
    },
    plugins: [scrollerMenu]
  };

  if (obj.t) subTab.id = "sub_tab_" + obj.t;
  var detailPanel = new Ext.TabPanel(subTab);
  
  if(mainGrid.dataViewId){
	  var xbuttons=[];// [' ',' ','-'];
	  xbuttons.push(organizeButtons(mainButtons));
	  xbuttons.push({iconCls:'icon-maximize', tooltip:'Maximize',handler:function(){
		  var sfx = Ext.getCmp('sfx-'+obj.t);
		  if(sfx){
			  if(sfx.isVisible()){
				  sfx._leftPanelVis = iwb.leftPanel.isVisible();
				  sfx.collapse();
				  iwb.leftPanel.collapse();
			  } else {
				  sfx.expand();
				  if(sfx._leftPanelVis)iwb.leftPanel.expand();
			  }
		  }
	  }});
	  var subToolbar = new Ext.Toolbar({xtype:'toolbar',cls:'iwb-card-sub-toolbar',items:xbuttons}); 
	  detailPanel = {region:'center', layout:'border', border:false,tbar : subToolbar
			  ,items:[{region:'north',height:50, html:'<div class="iwb-card-sub-header"><span id="idc-'+mainGrid.id+'"></span><div id="pidc-'+mainGrid.id+'"></div></div>'},detailPanel]};
	  mainGridPanel._subToolbar = subToolbar;
	  mainGridPanel.on('selectionchange',function(ax, bx){
		  var sel=ax.getSelectedRecords();
		  sel = sel && sel.length>0 && sel[0];
		  if(sel)document.getElementById('idc-'+ax.id).innerHTML=sel.get(ax._dsc||'dsc');
		  
	      var titems = ax._subToolbar.items.items;
	      for (var ti = 0; ti < titems.length; ti++) {
	        if (titems[ti]._activeOnSelection)
	          titems[ti].setDisabled(!sel);
	      }
	  });
	  var cardWidth = mainGrid.defaultWidth||350;
	  mainGridPanel = {region:mainGrid.searchForm?'center':'west', cls:'icb-main-card',autoScroll:!0, store:mainGridPanel.store, split:!mainGrid.searchForm, collapseMode:'mini',animate: false, animCollapse: false, animFloat:false,border:false,width: cardWidth,items:mainGridPanel}
	  if (mainGrid.pageSize) {
	    // paging'li toolbar
		  mainGridPanel.bbar = {
	      xtype: "paging",displayMsg:'{0} - {1} of {2}',
	      store: mainGrid.store, cls:'iwb-card-paging',
	      pageSize: mainGrid.pageSize,
	      displayInfo: !0
	    };
	  } 
	  var tbiNums = (mainGrid.searchForm?1:0)+(mainGrid.orderNames?1:0) + (mainGrid.crudFlags && mainGrid.crudFlags.insert ? 1:0);
	  var tbarItems;
	  if(mainGrid.tbarItems)tbarItems=mainGrid.tbarItems;
	  else {
		  tbarItems = [new Ext.form.TextField({id:'sf-card-'+obj.t,emptyText:getLocMsg('quick_search'),enableKeyEvents:!0,listeners:{keyup:fnCardSearchListener(mainGrid._gp)}
		  , style:'font-size:20px !important;padding:7px 7px 7px 14px;border:0;',width:cardWidth -36*tbiNums-20}),'->'];
		  if(mainGrid.searchForm)tbarItems.push({cls:'x-btn-icon x-grid-search', id:'sfb-card-'+obj.t, _sf:searchFormPanel, tooltip:'Advanced Search', handler:function(aq){
			  if(!aq._sf.isVisible()){
				  aq._sf.expand();
				  aq.hide();
			  } else {
				  aq._sf.collapse();
			  }
		  }});
		  if(mainGrid.orderNames)tbarItems.push({cls:'x-btn-icon x-grid-sort',tooltip:'Sort',_grid:mainGrid, handler:function(aq,ev){
			  if(!mainGrid.store.sortInfo)mainGrid.store.sortInfo={field:'', direction:'ASC'};
			  var si = mainGrid.store.sortInfo;
			  var xmenus=[];
			  for(var ri=0;ri<mainGrid.orderNames.length;ri++){
				  var rr = mainGrid.orderNames[ri];
				  var o = {text:rr.dsc, _id:rr.id, handler:function(ab){
					  var xsort='ASC';
					  if(si.field==ab._id){
						  si.direction = (si.direction=='ASC') ? 'DESC':'ASC';
					  }
					  si.field=ab._id;
					  mainGrid.store.reload();
				  }};
				  if(si.field==rr.id){
					  o.cls='xg-hmenu-sort-'+si.direction.toLowerCase();
				  }
				  xmenus.push(o);
			  }
			  // console.log('xmenus',xmenus);
	        new Ext.menu.Menu({cls:'sort-menu',
	            enableScrolling: false,
	            items: xmenus
	          }).showAt(ev.getXY());			  
	
		  }});
		  if (mainGrid.crudFlags.insert) {
			    var cfg = {
			      id: "btn_add_" + mainGrid.id,
			      tooltip: mainGrid.newRecordLabel || (getLocMsg("js_new") + ' ' + (mainGrid._dscLabel || 'Record')),
			      cls: "x-btn-icon x-grid-new",
			      ref: "../btnInsert",
			      showModalWindowFlag: false,
			      _activeOnSelection: false,
			      _grid: mainGrid
			    };
			    if (mainGrid.mnuRowInsert) cfg.menu = mainGrid.mnuRowInsert;
			    else cfg.handler = mainGrid.fnRowInsert || fnRowInsert;
			    tbarItems.push(cfg);
		  }
	  }
	  mainGridPanel.tbar = {xtype:'toolbar',id:'tb-card-'+obj.t,cls:"padding0",style:mainGrid.tbarItems?'':'border-bottom:1px solid #d64e20;'// background:#323840;
		  ,items:tbarItems};
	  if (mainButtons.length > 0) {
	    // standart toolbar
		 // mainGridPanel.tbar = organizeButtons(mainButtons);
	  }
	  if(mainGrid.searchForm){
		  mainGridPanel={border:false, layout:'border', store:mainGridPanel.store,region:'west', split:!0, animate: false, collapseMode:'mini',animCollapse: false, animFloat:false,width:mainGrid.defaultWidth||400,items:[mainGridPanel.store._formPanel, mainGridPanel]}
	  }
	  mainGridPanel.id='sfx-'+obj.t;
  }
  
  lastItems.push({
    region: "center",
    layout: "border",
    items: [mainGridPanel, detailPanel]
  });
  var p = {
    layout: "border",
    title: obj._title_ || mainGrid.name,
    border: false,
    closable: true,
    items: lastItems,
    refreshGrids: obj._dontRefresh
      ? null
      : searchFormPanel || !mainGrid.gridId
        ? [mainGridPanel]
        : null
  };
  if (obj.t) {
    p.id = obj.t;
    mainGridPanel._tid = obj.t;
  }
  p = new Ext.Panel(p);
  p._windowCfg = { layout: "border" };
  p._callCfg = obj;
  if (mainGrid.liveSync) p._lg = true;
  if (mainGrid.searchForm) p._formId = mainGrid.searchForm.formId;
  return p;
}

function prepareLogErrors(obj) {
  if (!obj.logErrors) return "eksik";
  var str = "";
  for (var xi = 0; xi < obj.logErrors.length; xi++) {
    str += "<b>" + (xi + 1) + ".</b> " + obj.logErrors[xi].dsc;
    if (obj.logErrors[xi]._record)
      str += renderParentRecords(obj.logErrors[xi]._record, 1) + "<br>";
    else if (xi < obj.logErrors.length - 1) {
      str += "<br>&nbsp;<br>";
    }
  }
  return str;
}

function showSQLError(sql, xpos, err) {
  var _code = window.monaco ? new Ext.ux.form.Monaco({
    hideLabel: true,// id:'id-ahmet',
    language: "sql",
    name: "code",
    anchor: "%100",
    height: "%100",
    value: sql
  }):  new Ext.form.TextArea({
	    hideLabel: true,// id:'id-ahmet',
	    language: "sql",
	    name: "code",
	    anchor: "%100",
	    style: "height:500px",
	    value: sql
	  });

  var wx = new Ext.Window({
    modal: true,
    closable: true,
    title: "SQL Error" + (err ? ' &nbsp; <span style="color:red;font-size:.9em;">'+err+'</span>':''),
    width: 1000,
    height: 600,
    border: false,
    layout: "fit",
    items: [new Ext.FormPanel({ region: "center", items: [_code] })],
    buttons:[{text:'Format',handler:function(){
    	iwb.request({url:'ajaxFormatSQL',params:{sql:sql},successCallback:function(jj){
        	_code.editor.setValue(jj.result);
    		
    	}});
    }}, {text:'Close',handler:function(){
    	wx.destroy();
    }}]
  }).show();
  return false;
}

function showScriptError(sql, xlineNo, err) {
	  var _code = window.monaco ? new Ext.ux.form.Monaco({
		    hideLabel: true,// id:'id-ahmet',
		    language: "javascript",
		    name: "code",
		    anchor: "%100",
		    height: "%100",
		    value: sql
		  }): new Ext.form.TextArea({
			    hideLabel: true,// id:'id-ahmet',
			    language: "javascript",
			    name: "code",
			    anchor: "%100",
			    style: "height:500px",
			    value: sql
			  });

	  var wx = new Ext.Window({
	    modal: true,
	    closable: true,
	    title: "Script Error" + (err ? ' &nbsp; <span style="color:red;font-size:.9em;">'+err.substr(0,100)+'</span>':''),
	    width: 1000,
	    height: 600,
	    border: false,
	    layout: "fit",
	    items: [new Ext.FormPanel({ region: "center", items: [_code] })],
	    buttons:[{text:'Close',handler:function(){
	    	wx.destroy();
	    }}]
	  }).show();
	  if(xlineNo)_code.editor.deltaDecorations([], [
			{ range: new monaco.Range(xlineNo,1,xlineNo,1), options: { isWholeLine: true, linesDecorationsClassName: 'veliSelRed' }}
		]);
	  return false;
	}


function ajaxErrorHandler(obj) {
  if (obj.errorType && obj.errorType == "validation") {
    var msg = "<b>" + getLocMsg("js_field_validation") + "</b><ul>";
    if (obj.errors) {
      for (var i = 0; i < obj.errors.length; i++)
        if (obj.errors[i].id != "_")
          msg +=
            "<li>&nbsp;&nbsp;&nbsp;&nbsp;" +
            (obj.errors[i].dsc || obj.errors[i].id) +
            " - " +
            obj.errors[i].msg +
            "</li>";
    } else if (obj.error) {
      msg += obj.error;
    }
    msg += "</ul>";
    Ext.infoMsg.msg("error", msg, 5);
  } else if (obj.errorType && obj.errorType == "session") showLoginDialog(obj);
  else if (obj.errorType && obj.errorType == "security")
    Ext.infoMsg.msg(
      "error",
      getLocMsg("error") +
        ": <b>" +
        (obj.error || getLocMsg("js_belirtilmemis")) +
        "</b><br/>" +
        obj.objectType +
        " Id: <b>" +
        obj.objectId +
        "</b>"
    );
  else if (
    obj.errorType &&
    (obj.errorType == "sql" ||
      obj.errorType == "vcs" ||
      obj.errorType == "rhino" ||
      obj.errorType == "framework" ||
      obj.errorType == "cache")
  ) {
    var items = [];
    items.push({
      xtype: "displayfield",
      fieldLabel: "",
      anchor: "99%",
      labelSeparator: "",
      hideLabel: !0,
      value:
        '<span style="font-size:1.5em">' + (obj.error ? obj.error.substr(0,150) : "Unknown") + "</span>"
    });
    if (false && obj.objectType) {
      items.push({
        xtype: "displayfield",
        fieldLabel: obj.objectId ? obj.objectType : "Type",
        anchor: "99%",
        labelSeparator: "",
        value: obj.objectId || obj.objectType
      });
      // if(obj.objectId)items.push({xtype:'displayfield',fieldLabel:
		// 'ID',width:100, labelSeparator:'',
		// value:'<b>'+(obj.objectId)+'</b>'});
    }
    if (obj.icodebetter) {
      var ss = "",
        sqlPos = false;
      iwb.errors = [];
      for (var qi = 0; qi < obj.icodebetter.length; qi++) {
        if (qi > 0) ss += "<br>";
        for (var zi = 0; zi < qi; zi++) ss += " &nbsp;";
        var oo = obj.icodebetter[qi];
        ss += '&gt <span style="opacity:.8">' + oo.objectType + "</span>";
        if (oo.objectId) {
          if (oo.error && oo.error.startsWith("[")) {
            var tid = oo.error.substr(1).split(",")[0];
            ss +=
              ': <a href=# onclick="return fnTblRecEdit(' +
              tid +
              "," +
              oo.objectId +
              ');">' +
              oo.error +
              "</a>";
          } else ss += ": " + oo.objectId + (oo.error ? " / " + oo.error : "");
        } else {
          ss += ": " + oo.error;
        }
        if (oo.error) {
          if (oo.sql) iwb.errors[qi] = oo.sql;
          if (oo.error.endsWith("}#") && oo.error.indexOf("#{") > -1) {
            var lineNo = oo.error.substr(oo.error.indexOf("#{") + 2);
            lineNo = lineNo.substr(0, lineNo.length - 2);
            if (iwb.errors[qi])
                ss +=
                    " &nbsp; <a href=# onclick='showScriptError(iwb.errors[" +
                    (qi) +
                    "]," +
                    lineNo +
                    ",iwb.errors[" +
                    (qi) +
                    "])' style='padding:1px 5px;background:white;color:green;border-radius:20px;'>Show Code</a>";
          } else {
            if (oo.error.indexOf("Position: ") > -1) {
              sqlPos = oo.error.substr(
                oo.error.indexOf("Position: ") + "Position: ".length
              );
            } // else if(sqlPos){
            if (iwb.errors[qi]){
              // iwb.errors[qi] = oo.error;
              ss +=
                " &nbsp; <a href=# onclick='showSQLError(iwb.errors[" +
                (qi) +
                "]," +
                sqlPos +
                ",iwb.errors[" +
                (qi) +
                "])' style='padding:1px 5px;background:white;color:green;border-radius:20px;'>Show Code</a>";
            // sqlPos=false;
            }
          }
        }
      }
      items.push({
        xtype: "displayfield",
        fieldLabel: "Stack",
        hideLabel: !0,
        anchor: "99%",
        labelSeparator: "",
        value: ss
      });
    }

    var xbuttons = [];
    if (obj.errorType == "cache") {
      xbuttons.push({ text: "Reload Cache", handler: reloadCache });
    } else {
      xbuttons.push({
        text: "Convert to Task",
        handler: function() {
          mainPanel.loadTab({
            attributes: {
              modalWindow: true,
              notAutoHeight: true,
              href:
                "showForm?_fid=253&a=2&iproject_step_id=0&isubject=BUG: " +
                obj.errorType +
                "&ilong_dsc=" +
                (obj.objectType
                  ? obj.objectType + ":" + obj.objectId + ", "
                  : "") +
                (obj.error || "")
            }
          });
          wndx.close();
        }
      });
      if (obj.stack)
        xbuttons.push({
          text: "Java StackTrace",
          handler: function() {
        	  new Ext.Window({
        	      modal: true,
        	      title: "Java StackTrace",
//        	      cls: "xerror",
        	      width: 900, height:600,
//        	      autoHeight: !0,
        	      html: '<textarea  style="font-family: monospace !important;width:99.7%; height:100%;color:#ccc; font-size: 12px;overflow: auto;background: #282b31;border: none;">'+
        	      obj.stack+'</textarea>'
//        	      buttons: xbuttons
        	    }).show();
//            alert(obj.stack);
          }
        });
    }
    xbuttons.push({
      text: getLocMsg("close"),
      handler: function() {
        wndx.close();
      }
    });
    var wndx = new Ext.Window({
      modal: true,
      title: obj.errorType.toUpperCase() + " Error" + (obj.objectType ? (' - ' + obj.objectType):''),
      cls: "xerror",
      width: obj.sql ? 900 : 650,
      autoHeight: !0,
      items: [
        {
          xtype: "form",
          labelAlign: "right",
          labelWidth: 80,
          bodyStyle: "padding:10px",
          autoHeight: true,
          layout: "form",
          border: false,
          items: items
        }
      ],
      buttons: xbuttons
    });
    wndx.show();
  } else {
	  var xid=_webPageId + '-'+Math.round(10000*Math.random());
	  var pnl = new Ext.Panel({closable:true,border:false,autoScroll:!0,html:'<div style="width:100% !important;height:100% !important;font-size: 12px;" id="'+xid+'"></div>'});
	  var w =new Ext.Window({modal:true,cls:'xerror2', title:(obj.errorType? ('<span style="font-size:1.2rem">'+obj.errorType+'</span> &nbsp; '): '')+(obj.error || "Unknown"), width: 600, height: 300, items:[pnl]})
	  w.show();
	  $('#'+xid).jsonViewer(obj, {});
	  return;
    Ext.Msg.show({
      cls: "xerror",
      title: obj.errorType || getLocMsg("js_error"),
      msg: obj.error || "Unknown",
      icon: Ext.MessageBox.ERROR
    });
  }
}

var lw = null;
function ajaxAuthenticateUser() {
  iwb.mask(!0);
  Ext.getCmp("loginForm")
    .getForm()
    .submit({
      url:
        "ajaxAuthenticateUser?userRoleId=" +
        _scd.userRoleId +
        "&locale=" +
        _scd.locale +
        (_scd.projectId ? "&projectId=" + _scd.projectId : ""),
      method: "POST",
      clientValidation: true,
// waitMsg: getLocMsg("js_entering") + "...",
      success: function(o, resp) {
        iwb.mask();
        if (resp.result.success) {
           lw.destroy();
           hideStatusText();
           refreshGridsAfterRelogin();
           longPollTask.delay(0);
        } else {
          Ext.infoMsg.alert(
            "error",
            resp.errorMsg || getLocMsg("js_wrong_user_password")
          );
        }
      },
      failure: function(o, resp) {
        iwb.mask();
        var resp = JSON.parse(resp.response.responseText);// eval("(" +
															// resp.response.responseText
															// + ")");
        if (resp.errorMsg) {
          Ext.infoMsg.alert("error", resp.errorMsg, "error");
        } else {
          Ext.infoMsg.alert(
            "error",
            resp.error || getLocMsg("js_check_data"),
            "error"
          );
        }
      }
    });
  return false;
}

function showLoginDialog(xobj) {
  if (1 * _scd.customizationId > 0) {
    if(document.location.href.indexOf('/preview/')>-1)document.location = "login.htm?.r="+Math.random();
    else document.location = "/index.html?.r="+Math.random();
    return;
  }
  if (lw && lw.isVisible()) return;
  if (typeof onlineUsersGridPanel != "undefined" && onlineUsersGridPanel)
    onlineUsersGridPanel.store.removeAll();
  var fs = new Ext.form.FormPanel({
    id: "loginForm",
    name: "loginForm",
    frame: false,
    border: false,
    labelAlign: "right",
    labelWidth: 100,
    waitMsgTarget: true,
    method: "POST",
    buttonAlign: "center",
    buttons: [
      {
        text: getLocMsg("login"),
        // iconCls: 'button-enter',
        handler: ajaxAuthenticateUser
      },
      {
        text: getLocMsg("exit"),
        // iconCls: 'button-exit',
        handler: function() {
          document.location = "login.htm?r=" + new Date().getTime();
        }
      }
    ],
    items: pfrm_login.render().items[0].items
  });

  lw = new Ext.Window({
    modal: true,
    title: pfrm_login.name,
    width: 350,
    height: 225,
    layout: "fit",
    items: fs,
    bodyStyle: "padding: 10px",
    closable: false
  });
  lw.show();

  var nav = new Ext.KeyNav(
    Ext.getCmp("loginForm")
      .getForm()
      .getEl(),
    {
      enter: ajaxAuthenticateUser,
      scope: Ext.getCmp("loginForm")
    }
  );
}

function formSubmit(submitConfig) {
  var cfg = {
// waitMsg: getLocMsg("js_please_wait"),
    clientValidation: false,//typeof iwb.submitClientValidation != 'undefined' ? iwb.submitClientValidation:true,
    success: function(form, action) {
      iwb.mask();
      var myJson = JSON.parse(action.response.responseText);// eval("(" +
															// action.response.responseText
															// + ")");
      var jsonQueue = [];
      if (myJson.smsMailPreviews && myJson.smsMailPreviews.length > 0) {
        for (var ix = 0; ix < myJson.smsMailPreviews.length; ix++) {
          var smp = myJson.smsMailPreviews[ix];
          var ss = "";
          jsonQueue.push({
            attributes: {
              href:
                "showForm?_fid=" +
                (smp.fsmTip ? 650 : 631) +
                "&_tableId=" +
                smp.tbId +
                "&_tablePk=" +
                smp.tbPk +
                "&_fsmId=" +
                smp.fsmId +
                "&_fsmFrmId=" +
                myJson.formId +
                ss
            }
          });
        }
      }
      if (myJson.conversionPreviews && myJson.conversionPreviews.length > 0) {
        for (var ix = 0; ix < myJson.conversionPreviews.length; ix++) {
          var cnvp = myJson.conversionPreviews[ix];
          var ppp = {
            attributes: {
              href:
                "showForm?a=2&_fid=" +
                cnvp._fid +
                "&_cnvId=" +
                cnvp._cnvId +
                "&_cnvTblPk=" +
                cnvp._cnvTblPk
            }
          };
          if (cnvp._cnvDsc) ppp._cnvDsc = cnvp._cnvDsc;
          jsonQueue.push(ppp);
        }
      }

      /*
		 * if(myJson.alarmPreviews && myJson.alarmPreviews.length>0){
		 * Ext.infoMsg.alert('TODO ALARM PREVIEWS: ' +
		 * myJson.alarmPreviews.length + " adet");//TODO }
		 */

      if (jsonQueue.length > 0) {
        var jsonQueueCounter = 0;
        var autoOpenForms = new Ext.util.DelayedTask(function() {
          mainPanel.loadTab(jsonQueue[jsonQueueCounter]);
          if (jsonQueue[jsonQueueCounter]._cnvDsc)
            Ext.infoMsg.msg(
              "Form Conversion",
              jsonQueue[jsonQueueCounter]._cnvDsc
            );
          jsonQueueCounter++;
          if (jsonQueue.length > jsonQueueCounter) autoOpenForms.delay(1000);
        });
        autoOpenForms.delay(1);
      }

      if (myJson.logErrors || myJson.msgs) {
        var str = "";
        if (myJson.msgs) str = myJson.msgs.join("<br>") + "<p>";
        if (myJson.logErrors) str += prepareLogErrors(myJson);
        Ext.infoMsg.msg("info", str);

        // Ext.Msg.show({title: getLocMsg('js_info'),msg: str,icon:
		// Ext.MessageBox.INFO});
      } /*
		 * else if(1*_app.mail_send_background_flag!=0 && myJson.outs &&
		 * myJson.outs.thread_id){ //DEPRECATED
		 * Ext.infoMsg.msg(getLocMsg('ok,getLocMsg('js_eposta_gonderiliyor+'...'); }
		 */ else if (
        _app.show_info_msg &&
        1 * _app.show_info_msg != 0
      )
        Ext.infoMsg.msg("success", getLocMsg("operation_successful"));
      if (submitConfig.callback) {
        if (submitConfig.callback(myJson, submitConfig) === false) return;
      }

      if (submitConfig._closeWindow) {
        submitConfig._closeWindow.destroy();
      } else if (submitConfig.modalWindowFormSubmit) {
        submitConfig.tabp.remove(submitConfig.tabp.getActiveTab());
      } else if (!submitConfig.dontClose && !mainPanel.closeModalWindow()) {
        mainPanel.remove(mainPanel.getActiveTab());
      }

      if (submitConfig.resetValues) {
        submitConfig.formPanel.getForm().reset();
      } else {
        // reset special coding
        if (submitConfig.dontClose) {
          submitConfig.formPanel.getForm().items.each(function(itm) {
            if (itm._controlTip * 1 == 31) {
              itm.setValueFromSystem();
            }
          });
        }
      }

      if (submitConfig._callAttributes) {
        if (submitConfig._callAttributes._grid) {
        	var xg = submitConfig._callAttributes._grid;
        	if(!xg.ds && xg.store)xg.ds=xg.store;
          if (_app.live_sync_record && 1 * _app.live_sync_record != 0)
            Ext.defer(
              function(g) {
                iwb.reload(g);
              },
              1000,
              this,
              [xg]
            );
          else iwb.reload(xg);
        } else if (submitConfig._callAttributes._formCell) {
        	iwb.refreshFormCell(submitConfig._callAttributes._formCell);
        } else if (submitConfig._callAttributes._gridId) {
        	iwb.refreshGrid(submitConfig._callAttributes._gridId);
        }
      }
    },
    failure: function(form, action) {
      iwb.mask();
      switch (action.failureType) {
        case Ext.form.Action.CLIENT_INVALID:
          Ext.infoMsg.msg(
            "error",
            getLocMsg("js_form_field_validation_error")
          );
          break;
        case Ext.form.Action.CONNECT_FAILURE:
          Ext.infoMsg.wow("error", getLocMsg("js_no_connection_error"));
          break;
        case Ext.form.Action.SERVER_INVALID:
          if (action.result.msg) {
            Ext.infoMsg.alert("error", action.result.msg, "error");
            break;
          }
        // case Ext.form.Action.LOAD_FAILURE:
        default:
          if (
            action.result &&
            action.result.errorType &&
            action.result.errorType == "confirm"
          ) {
            var obj = action.result;
            Ext.infoMsg.confirm(obj.error, () => {
              // TODO. burda birseyler yapilacak.
				// baseParams['_confirmId_'+obj.objectId]=1 eklenecek
              var fm = submitConfig.formPanel.getForm();
              if (!fm.baseParams) fm.baseParams = {};
              fm.baseParams["_confirmId_" + obj.objectId] = 1;
              fm.submit(cfg);
            });
          } else ajaxErrorHandler(action.result);
          break;
      }
    }
  };
  cfg.params = Ext.apply(
    { ".p": _scd.projectId },
    submitConfig.extraParams || {}
  );
  iwb.mask(!0);
  submitConfig.formPanel.getForm().submit(cfg);
}

function promisLoadException(a, b, c) {
  if (c && c.responseText) {
    ajaxErrorHandler(JSON.parse(c.responseText)); // eval("(" + c.responseText
													// + ")")
  } else Ext.infoMsg.wow("error", getLocMsg("js_no_connection_error"));
}

iwb.mask=function(x){
	try{
	    document.getElementById("loading-mask-full").style.display = x?"block":"none";
	    document.getElementById("loading-mask").style.display = x?"block":"none";
	}catch(e){}
}
function promisRequest(rcfg) {
  var reqWaitMsg = 1 * _app.request_wait_msg;
  if (typeof rcfg.requestWaitMsg == "boolean") {
    if (rcfg.requestWaitMsg) reqWaitMsg = 1;
    else reqWaitMsg = 0;
  }
  if (reqWaitMsg == 1)iwb.mask(!0);
  if (!rcfg.params) rcfg.params = {};
  rcfg.params[".w"] = _webPageId;
  rcfg.params[".p"] = _scd.projectId;
  Ext.Ajax.request(
    Ext.apply(
      {
        success: function(a, b, c) {
          if (reqWaitMsg == 1)iwb.mask();
          if (rcfg.successResponse) rcfg.successResponse(a, b, c);
          else
            try {
              var json = rcfg._eval? eval("(" + a.responseText + ")"): JSON.parse(a.responseText);// eval("("
																									// +
																									// a.responseText
																									// +
																									// ")");
              if (json.success) {
                if (rcfg.successDs) {
                  if (!rcfg.successDs.length) rcfg.successDs.reload();
                  // rcfg.successDs TODO: onceden bu vardi kaldirdim, sorun
					// cikarsa geri konulur. sebep, delete'ten sonra eski
					// parametrelere gore refresh ediyordu gridi
                  else if (rcfg.successDs.length > 0) {
                    for (var qi = 0; qi < rcfg.successDs.length; qi++)
                      rcfg.successDs[qi].reload(rcfg.successDs[qi]);
                  }
                }
                if (rcfg.successCallback) rcfg.successCallback(json, rcfg);
                else if (_app.show_info_msg && 1 * _app.show_info_msg != 0) {
                  Ext.infoMsg.msg(
                    "success",
                    getLocMsg("operation_successful")
                  );
                }
              } else {
                if (rcfg.noSuccessCallback) rcfg.noSuccessCallback(json, rcfg);
                else if (
                  json.errorType &&
                  json.errorType == "confirm" &&
                  json.error
                ) {
                  Ext.infoMsg.confirm(json.error, () => {
                    rcfg.params["_confirmId_" + json.objectId] = 1;
                    iwb.request(rcfg);
                  });
                } else ajaxErrorHandler(json);
              }
            } catch (e) {
              if (1 * _app.debug != 0) {
                if (confirm("ERROR Response from Ajax.Request!!! Throw? : " + e.message))
                  throw e;
              } else
                Ext.infoMsg.alert(
                  "Error",
                  "Framework Error(Ajax.Request) : " + e.message,
                  "error"
                ); // ???
            }
        },
        failure: function(a, b, c) {
          if (reqWaitMsg == 1)iwb.mask();
          promisLoadException(a, b, c);
        }
      },
      rcfg
    )
  );
}

iwb.request = promisRequest;

function combo2combo(comboMaster, comboDetail, param, formAction) {
  // formAction:2(insert) ise ve comboDetail reload olunca 1 kayit geliyorsa
	// otomatik onu sec

  if (typeof comboMaster == "undefined" || typeof comboDetail == "undefined")
    return;
  if (typeof comboMaster.hiddenValue == "undefined") {
    comboMaster.on(comboMaster._checkbox?"change":"select", function(a, b, c) {
      if (getFieldValue(comboMaster) == "") {
        comboDetail.clearValue();
        if (comboDetail.getStore()) comboDetail.getStore().removeAll();
        comboDetail.fireEvent("select");
        return;
      }
      var p = null;
      if (typeof param == "function") {
        p = param(getFieldValue(comboMaster), b);
        if (comboDetail._controlTip != 60) {
          if (!p) {
            if (p === false) comboDetail.hide();
            comboDetail.disable();
            comboDetail.setValue("");
          } else {
            comboDetail.enable();
            comboDetail.show();
          }
        } else {
          comboDetail.clearValue(); // Aşırı sıkış
        }
      } else {
        p = {};
        p[param] = getFieldValue(comboMaster);
      }
      if (p) {
        if (typeof comboDetail.hiddenValue == "undefined") {
          comboDetail.store.baseParams = p;
          comboDetail.store.reload({
            callback: function(ax) {
            	try{
	              if (
	                typeof comboDetail._controlTip != "undefined" &&
	                (comboDetail._controlTip == 16 || comboDetail._controlTip == 60)
	              ) {
	                // lovcombo-remote
	                if (comboDetail._oldValue && comboDetail._oldValue != null) {
	                  comboDetail.setValue(comboDetail._oldValue);
	                  comboDetail._oldValue = null;
	                }
	              } else if ((ax && !ax.length) || getFieldValue(comboMaster) == "") {
	                comboDetail.clearValue();
	              } else if (
	                ax &&
	                ax.length == 1 &&
	                (comboDetail.getValue() == ax[0].id || formAction == 2) &&
	                !comboDetail._notAutoSet
	              ) {
	                comboDetail.setValue(ax[0].id);
	              } else if (ax && ax.length > 1 && comboDetail.getValue()) {
	                if (comboDetail.store.getById(comboDetail.getValue())) {
	                  comboDetail.setValue(comboDetail.getValue());
	                } else {
	                  comboDetail.clearValue();
	                }
	              }
	              if (comboDetail.getValue()) comboDetail.fireEvent("select");
            	} catch(ee){
            		Ext.infoMsg.msg('error',"ComboDetail.Load error: " + ee)
            	}
            }
          });
        } else {
          p.xid = comboDetail.hiddenValue;
          iwb.request({
            url: "ajaxQueryData",
            params: p,
            successCallback: function(j2) {
              if (j2 && j2.data && j2.data.length)
                for (var qi = 0; qi < j2.data.length; qi++)
                  if ("" + j2.data[qi].id == "" + comboDetail.hiddenValue) {
                    comboDetail.setValue("<b>" + j2.data[qi].dsc + "</b>");
                  }
            }
          });
        }
      } else {
        // Ext.infoMsg.alert(2);
      }
    });
    if (getFieldValue(comboMaster))
      comboDetail.on("afterrender", function(a, b) {
        comboMaster.fireEvent(comboMaster._checkbox?"change":"select");
      });
  } else {
    // master hiddenValue
    var p = null;
    if (typeof param == "function") {
      p = param(comboMaster.hiddenValue, comboMaster);
      if (!p) {
        comboDetail.disable();
        comboDetail.setValue("");
      } else comboDetail.enable();
    } else {
      p = {};
      p[param] = comboMaster.hiddenValue;
    }
    if (p) {
      if (typeof comboDetail.hiddenValue == "undefined") {
        comboDetail.store.baseParams = p;
        comboDetail.store.reload({
          // params:p,
          callback: function(ax) {
            if (
              typeof formAction != "undefined" &&
              formAction == 2 &&
              ax &&
              ax.length == 1
            ) {
              comboDetail.setValue(ax[0].id);
            } else if (comboDetail.getValue()) {
              comboDetail.setValue(comboDetail.getValue());
            }
            if (comboDetail.getValue()) comboDetail.fireEvent("select");
          }
        });
      } else {
        p.xid = comboDetail.hiddenValue;
        iwb.request({
          url: "ajaxQueryData",
          params: p,
          successCallback: function(j2) {
            if (j2 && j2.data && j2.data.length)
              for (var qi = 0; qi < j2.data.length; qi++)
                if ("" + j2.data[qi].id == "" + comboDetail.hiddenValue) {
                  comboDetail.setValue("<b>" + j2.data[qi].dsc + "</b>");
                }
          }
        });
      }
    }
  }
}

function loadCombo(comboMaster, param, formAction) {
  if (typeof comboMaster == "undefined" || !param) return;
  if (typeof comboMaster.hiddenValue != "undefined") {
    if (comboMaster._controlTip == 101) {
    	iwb.request({
        url: "ajaxQueryData",
        params: param,
        successCallback: function(j2) {
          if (j2 && j2.data && j2.data.length)
            for (var qi = 0; qi < j2.data.length; qi++)
              if ("" + j2.data[qi].id == "" + comboMaster.hiddenValue) {
                comboMaster.setValue("<b>" + j2.data[qi].dsc + "</b>");
              }
        }
      });
    }
    return;
  }
  comboMaster.store.reload({
    params: param,
    callback: function(ax) {
      if (
        typeof formAction != "undefined" &&
        formAction == 2 &&
        ax &&
        ax.length == 1 /* && !comboMaster.getValue() */
      ) {
        comboMaster.setValue(ax[0].id);
      } else if (comboMaster.getValue() || comboMaster._oldValue)
        comboMaster.setValue(comboMaster.getValue() || comboMaster._oldValue);
      if (comboMaster.getValue()) comboMaster.fireEvent("select");
    }
  });
}

function openModal(cfg) {
  mainPanel.loadTab({ attributes: { href: cfg.url, modalWindow: true } });
}

function gridQwRenderer(field) {
  return function(a, b, c) {
    return c.data[field + "_qw_"];
  };
}
function gridQwRendererWithLink(field, tbl_id) {
  return function(a, b, c) {
	if(!_scd.customizationId)
    return c.data[field] != undefined
      ? '<a href=# onclick="return fnTblRecEdit(' +
          tbl_id +
          "," +
          c.data[field] +
          ')">' +
          c.data[field + "_qw_"] +
          "</a>"
      : "";
          else
        	  return c.data[field + "_qw_"];
  };
}

function gridUserRenderer(field) {
  return function(a, b, c) {
    return c.data[field] == _scd.userId
      ? c.data[field + "_qw_"]
      : '<a href=# onclick="return openChatWindow(' +
          c.data[field] +
          ",'" +
          c.data[field + "_qw_"] +
          "',true)\">" +
          c.data[field + "_qw_"] +
          "</a>";
  };
}
function editGridComboRenderer(combo) {
  return function(value) {
    if (!combo || !combo.store) return "???";
    var record = combo.store.getById(value);
    return record ? record.get("dsc") : "";
  };
}
function editGridTreeComboRenderer(combo, field) {
  return function(value, b, c) {
    if (!combo || !combo.treePanel) return "???";
    var record = combo.treePanel.getNodeById(value);
    if (record) record = record.text;
    else record = value && value != 0 ? c.data[field + "_qw_"] : "";
    return record;
  };
}

function editGridLovComboRenderer(combo) {
  return function(value) {
    if (!combo) return "???";
    var valueList = [];
    if (typeof value == "undefined") return "";
    if (!value && ("" + value).length == 0) return "";
    var findArr = value.split(",");
    var i,
      l = findArr.length;
    for (i = 0; i < l; i++) {
      if ((record = combo.store.getById(findArr[i]))) {
        valueList.push(record.get("dsc"));
      }
    }
    return valueList.join(",");
  };
}

function handleMouseDown(g, rowIndex, e) {
  if (e.button !== 0 || this.isLocked()) {
    return;
  }
  var view = this.grid.getView();
  if (e.shiftKey && this.last !== false) {
    var last = this.last;
    this.selectRange(last, rowIndex, e.ctrlKey);
    this.last = last; // reset the last
    view.focusRow(rowIndex);
  } else {
    var isSelected = this.isSelected(rowIndex);
    if (e.ctrlKey && isSelected) {
      this.deselectRow(rowIndex);
    } else if (!isSelected || this.getCount() > 1) {
      this.selectRow(rowIndex, true);
      view.focusRow(rowIndex);
    }
  }
}

function approveTableRecord(aa, a) {
  var sel = getSel(a._grid);
  var rec_id;

  if (!sel) {
    Ext.infoMsg.msg("warning", getLocMsg("select_something"));
    return;
  }
  if (aa == 2 && 1 * sel.data.return_flag == 0) {
    Ext.infoMsg.alert(
      "info",
      getLocMsg("js_bu_surecte_iade_yapilamaz"),
      "info"
    );
    return;
  }

  if (sel.data.approval_record_id) {
    rec_id = sel.data.approval_record_id;
  } else {
    rec_id = sel.data.pkpkpk_arf_id;
  }

  var approveMap = [
    "",
    getLocMsg("approve"),
    getLocMsg("return"),
    getLocMsg("reject")
  ];
  approveMap[901] = getLocMsg("start_approval");
  var caption = approveMap[aa] + " (" + sel.data.dsc + ")";
  var e_sign_flag = sel.data.e_sign_flag;

  if (1 * e_sign_flag == 1 && aa * 1 == 1) {
    //
    openPopup(
      "showPage?_tid=691&_arid=" + sel.data.approval_record_id,
      "_blank",
      800,
      600,
      1
    );
    return;
  }

  var urlek = "";


  var cform = new Ext.form.FormPanel({
    baseCls: "x-plain",
    labelWidth: 150,
    frame: false,
    bodyStyle: "padding:5px 5px 0",
    labelAlign: "top",

    items: [
      {
        xtype: "textarea",
        fieldLabel: getLocMsg("enter_comment"),
        name: "_comment",
        anchor: "100% -5" // anchor width by percentage and height by raw
							// adjustment
      }
    ]
  });

  var win = new Ext.Window({
    layout: "fit",
    width: 500,
    height: 300,
    plain: true,
    buttonAlign: "center",
    modal: true,
    title: caption,

    items: cform,
    buttons: [
      {
        text: getLocMsg("ok"),
        handler: function(ax, bx, cx) {
//          var _dynamic_approval_users = win.items.items[0].items.items[0].getValue();
          var _comment = win.items.items[0].items.items[0].getValue();
          iwb.request({
            url: "ajaxApproveRecord",requestWaitMsg: true,
            params: {
              _arid: rec_id,
              _adsc: _comment,
              _aa: aa,
              _avno: sel.data.ar_version_no || sel.data.version_no,
//              _appUserIds: _dynamic_approval_users
            },
            successDs: a._grid.ds,
            successCallback:
              aa != 901
                ? win.close()
                : function() {
                    win.close();
                    Ext.infoMsg.alert(
                      "info",
                      getLocMsg("approval_started"),
                      "info"
                    );
                  }
          });
        }
      },
      {
        text: getLocMsg("cancel"),
        handler: function() {
          win.close();
        }
      }
    ]
  });
  win.show(this);
}

function approveTableRecords(aa, a) {
  var sels = a._grid.sm.getSelections();

  if (sels.length == 0) {
    Ext.Msg.show({
      title: getLocMsg("error"),
      msg: getLocMsg("select_something"),
      icon: Ext.MessageBox.ERROR
    });
    return;
  }
  var tek_kayit = sels.length == 1 ? true : false;
  var sel_ids = [];
  var urlek = "";
  var vers = [];
  var step = 0;
  var rec_id;
  var step_id;

  for (var i = 0; i < sels.length; i++) {
    if (sels[i].data.approval_record_id) {
      rec_id = sels[i].data.approval_record_id;
    } else {
      rec_id = sels[i].data.pkpkpk_arf_id;
    }

    if (sels[i].data.approval_step_id) {
      step_id = sels[i].data.approval_step_id;
    } else {
      step_id = sels[i].data.pkpkpk_arf;
    }

    if (aa != 901 && step_id == 901) {
      Ext.Msg.show({
        title: getLocMsg("error"),
        msg: getLocMsg("workflow_wrong_action"),
        icon: Ext.MessageBox.ERROR
      });
      return;
    }

    if (step != 0 && step_id * 1 != step) {
      Ext.Msg.show({
        title: getLocMsg("error"),
        msg: getLocMsg("js_secilenlerin_onay_adimi_ayni_olmali"),
        icon: Ext.MessageBox.ERROR
      });
      return;
    }
    step = step_id;

    if (step_id * 1 == 998) {
      Ext.Msg.show({
        title: getLocMsg("error"),
        msg: getLocMsg("record_already_approved"),
        icon: Ext.MessageBox.ERROR
      });
      return;
    }

    if (step_id < 0) {
      Ext.Msg.show({
        title: getLocMsg("error"),
        msg: getLocMsg("js_onay_adiminda_yer_almiyorsunuz"),
        icon: Ext.MessageBox.ERROR
      });
      return;
    }

    if (sels[i].data.in_approval_users && sels[i].data.in_approval_roles) {
      if (
        sels[i].data.in_approval_users * 1 != 1 &&
        sels[i].data.in_approval_roles * 1 != 1
      ) {
        Ext.Msg.show({
          title: getLocMsg("error"),
          msg: getLocMsg("js_onay_adiminda_yer_almiyorsunuz"),
          icon: Ext.MessageBox.ERROR
        });
        return;
      }
    }

    if (aa == 2 && 1 * sels[i].data.return_flag == 0) {
      Ext.Msg.show({
        title: getLocMsg("error"),
        msg: getLocMsg("js_bu_surecte_iade_yapilamaz"),
        icon: Ext.MessageBox.ERROR
      });
      return;
    }

    if (rec_id * 1 > 0) {
      sel_ids.push(rec_id);
      vers.push(sels[i].data.ar_version_no || sels[i].data.version_no);
    }
  }
  var approveMap = [
    "",
    getLocMsg("approve"),
    getLocMsg("return"),
    getLocMsg("reject")
  ];
  approveMap[901] = getLocMsg("start_approval");
  var caption = approveMap[aa];

  if (sel_ids.length == 0) return;


  var cform = new Ext.form.FormPanel({
    baseCls: "x-plain",
    labelWidth: 150,
    frame: false,
    bodyStyle: "padding:5px 5px 0",
    labelAlign: "top",

    items: [

      {
        xtype: "textarea",
        fieldLabel: getLocMsg("enter_comment"),
        name: "_comment",
        anchor: "100% -5" // anchor width by percentage and height by raw
							// adjustment
      }
    ]
  });

  var win = new Ext.Window({
    layout: "fit",
    width: 500,
    height: 300,
    plain: true,
    buttonAlign: "center",
    modal: true,
    title: caption,

    items: cform,
    buttons: [
      {
        text: getLocMsg("ok"),
        handler: function(ax, bx, cx) {
          var _comment = win.items.items[0].items.items[0].getValue();
          /*
			 * promisRequest({ url: 'ajaxApproveRecord',
			 * params:{_arids:sel_ids,_adsc:_comment,_aa:aa, _avnos:vers,
			 * _appUserIds:_dynamic_approval_users}, successDs: a._grid.ds
			 * ,successCallback:win.close() });
			 */

          // senkron hale getirildi
          var prms = "";
          for (var i = 0; i < sel_ids.length; i++) {
            prms += "_arids=" + sel_ids[i] + "&";
            prms += "_avnos=" + vers[i] + "&";
          }
          prms +=
            "_adsc" +
            "=" +
            encodeURIComponent(_comment) +
            "&" +
            "_aa" +
            "=" +
            encodeURIComponent(aa);

          Ext.Msg.wait("", getLocMsg("js_please_wait"));
          var request = promisManuelAjaxObject();
          request.open("POST", "ajaxApproveRecord", false);
          request.setRequestHeader(
            "Content-type",
            "application/x-www-form-urlencoded"
          );
          request.send(prms);
          var json = JSON.parse(request.responseText);// eval("(" +
														// request.responseText
														// + ")");
          Ext.Msg.hide();
          if (json.success) {
            win.close();
            iwb.reload(a._grid);
            Ext.infoMsg.msg(
              "success",
              getLocMsg("operation_successful")
            );
          } else ajaxErrorHandler(json);
        }
      },
      {
        text: getLocMsg("cancel"),
        handler: function() {
          win.close();
        }
      }
    ]
  });
  win.show(this);
}

function submitAndApproveTableRecord(aa, frm) {
  var caption = null;
  if (aa != null) {
    caption = [
      "",
      getLocMsg("approve"),
      getLocMsg("return"),
      getLocMsg("reject")
    ][aa];
  } else {
    caption = getLocMsg("js_onay_mek_baslat");
  }
  var urlek = "";

  var cform = new Ext.form.FormPanel({
    baseCls: "x-plain",
    labelWidth: 150,
    frame: false,
    bodyStyle: "padding:5px 5px 0",
    labelAlign: "top",

    items: [
      {
        xtype: "textarea",
        fieldLabel: getLocMsg("enter_comment"),
        name: "_comment",
        anchor: "100% -5" // anchor width by percentage and height by raw
							// adjustment
      }
    ]
  });

  var win = new Ext.Window({
    layout: "fit",
    width: 500,
    height: 300,
    plain: true,
    buttonAlign: "center",
    modal: true,
    title: caption,
    items: cform,
    buttons: [
      {
        text: getLocMsg("ok"),
        handler: function(ax, bx, cx) {
          var _comment = win.items.items[0].items.items[0].getValue();
          if (
            (aa == 1 &&
              (!_app.form_approval_save_flag ||
                1 * _app.form_approval_save_flag == 0)) ||
            frm.viewMode
          ) {
            var prms = frm.pk;
            prms._arid = frm.approval.approvalRecordId;
            prms._aa = aa;
            prms._adsc = _comment;
            prms._avno = frm.approval.versionNo;
            promisRequest({
              url: "ajaxApproveRecord",requestWaitMsg: true,
              params: prms,
              successCallback: function(json) {
                win.close();
                var submitConfig = frm._cfg;
                if (submitConfig._callAttributes) {
                  if (submitConfig._callAttributes._grid)
                    iwb.reload(submitConfig._callAttributes._grid);
                }
                if (submitConfig._closeWindow) {
                  submitConfig._closeWindow.destroy();
                } else if (submitConfig.modalWindowFormSubmit) {
                  submitConfig.tabp.remove(submitConfig.tabp.getActiveTab());
                } else if (
                  !mainPanel.closeModalWindow() &&
                  !submitConfig.dontClose
                ) {
                  mainPanel.remove(mainPanel.getActiveTab());
                }
              }
            });
          } else {
            if (aa != -1) {
              frm._cfg.extraParams = Ext.apply(frm._cfg.extraParams || {}, {
                _arid: frm.approval.approvalRecordId,
                _aa: aa,
                _adsc: _comment,
                _avno: frm.approval.versionNo,
//                _appUserIds: _dynamic_approval_users
              });
            } else {
              frm._cfg.extraParams = Ext.apply(frm._cfg.extraParams || {}, {
                _aa: aa,
                _adsc: _comment
              });
            }
            formSubmit(frm._cfg);
          }
          win.close();
        }
      },
      {
        text: getLocMsg("cancel"),
        handler: function() {
          win.close();
        }
      }
    ]
  });
  win.show(this);
}

function addTab4Portal(obj) {
  var detailGridPanels = [];
  for (var i = 0; i < obj.detailGrids.length; i++) {
    if (obj.detailGrids[i].dash) {
      var dg = obj.detailGrids[i].dash;
      if (!dg || !dg.dashId) continue;
      if (dg._ready) {
        detailGridPanels.push(dg);
        continue;
      }
      var gid = "dgraph_div_" + dg.dashId;
      var dgPanel = Ext.apply(
        {
          html: '<div id="' + gid + '" style="height:100%;width:100"></div>',
          header: false,
          _dg: dg,
          _gid: gid,
          listeners: {
            afterrender: function(aq, bq, cq) {
              var dg = aq._dg,
                gid = aq._gid;
              var newStat = 1 * dg.funcTip ? dg.funcFields : "";
              var params = {};
              if (newStat) params._ffids = newStat;
              if (1 * dg.graphTip >= 5) params._sfid = dg.stackedFieldId;
              promisRequest({
                url:
                  "ajaxQueryData4StatTree?_gid=" +
                  dg.gridId +
                  "&_stat=" +
                  dg.funcTip +
                  "&_qfid=" +
                  dg.groupBy +
                  "&_dtt=" +
                  dg.dtTip,
                params: Ext.apply(params, dg.queryParams),
                successCallback: function(az) {//TODO
                }
              });
            }
          },
          handlerSetting: function(aq, bq, cq) {
            mainPanel.loadTab({
              attributes: {
                _title_: cq._gp.name,
                modalWindow: true,
                href:
                  "showForm?a=1&_fid=327&tgraph_dashboard_id=" + cq._gp.dashId
              }
            });
          }
        },
        dg
      );
      var p = new Ext.Panel(dgPanel);
      dgPanel._gp = p; // dgPanel.gridId=-dg.dashId;
      detailGridPanels.push(p);
    } else {
      var detailGrid = obj.detailGrids[i].grid;
      if (!detailGrid || !detailGrid.gridId) continue;
      if (detailGrid._ready) {
        detailGridPanels.push(detailGrid);
        continue;
      }
      if (obj.detailGrids[i].pk) detailGrid._pk = obj.detailGrids[i].pk;
      var grdExtra = {
// stripeRows: true,
        id: obj.t + "-" + detailGrid.gridId,cls:'iwb-grid-'+detailGrid.gridId,
        autoScroll: true,
        border: false,
        clicksToEdit: 1 * _app.edit_grid_clicks_to_edit
      };
      var buttons = [];
      if (detailGrid.editGrid) addDefaultCommitButtons(buttons, detailGrid);

      if (detailGrid.hasFilter) {
        if (buttons.length > 0) buttons.push("-");
        buttons.push({
          tooltip: getLocMsg("remove_filter"),
          cls: "x-btn-icon x-grid-funnel",
          _grid: detailGrid,
          handler: fnClearFilters
        });
      }

      if (detailGrid.crudFlags) addDefaultCrudButtons(buttons, detailGrid);
      if (detailGrid.moveUpDown) addMoveUpDownButtons(buttons, detailGrid);
      addDefaultSpecialButtons(buttons, detailGrid);

      if (detailGrid.extraButtons) {
        if (buttons.length > 0) buttons.push("-");
        for (var j = 0; j < detailGrid.extraButtons.length; j++) {
          detailGrid.extraButtons[j]._grid = detailGrid;
          detailGrid.extraButtons[j].disabled =
            detailGrid.extraButtons[j]._activeOnSelection;
        }
        buttons.push(detailGrid.extraButtons);
      }
      if (detailGrid.menuButtons) {
        for (var j = 0; j < detailGrid.menuButtons.length; j++) {
          detailGrid.menuButtons[j]._grid = detailGrid;
        }
        detailGrid.menuButtons = new Ext.menu.Menu({
          enableScrolling: false,
          items: detailGrid.menuButtons
        });
        if (1 * _app.toolbar_edit_btn) {
          if (buttons.length > 0) buttons.push("-");
          buttons.push({
            id: "btn_operations_" + detailGrid.id,
            cls: "x-btn-icon x-grid-menu",
            disabled: true,
            _activeOnSelection: true,
            menu: detailGrid.menuButtons
          });
        }
      }
      if (detailGrid.gridReport) addDefaultReportButtons(buttons, detailGrid);
      addDefaultPrivilegeButtons(buttons, detailGrid);

      if (detailGrid.pageSize) {
        // paging'li toolbar
        var tbarExtra = {
          xtype: "paging",
          store: detailGrid.ds,
          pageSize: detailGrid.pageSize,
          displayInfo: detailGrid.displayInfo
        };
        if (buttons.length > 0) tbarExtra.items = organizeButtons(buttons);
        grdExtra.tbar = tbarExtra;
      } else if (buttons.length > 0) {
        // standart toolbar
        grdExtra.tbar = organizeButtons(buttons);
      }
      var eg = detailGrid.master_column_id
        ? detailGrid.editGrid
          ? Ext.ux.maximgb.tg.EditorGridPanel
          : Ext.ux.maximgb.tg.GridPanel
        : detailGrid.editGrid
          ? Ext.grid.EditorGridPanel
          : Ext.grid.GridPanel;
      var detailGridPanel = new eg(Ext.apply(detailGrid, grdExtra));
      if (detailGrid.crudFlags && detailGrid.crudFlags.edit)
        detailGridPanel.on("rowdblclick", fnRowEditDblClick);

      detailGrid._gp = detailGridPanel;
      if (detailGrid.editGrid) {
        detailGridPanel.getColumnModel()._grid = detailGrid;
        if (!detailGrid.onlyCommitBtn) {
          detailGridPanel.getColumnModel().isCellEditable = function(
            colIndex,
            rowIndex
          ) {
            if (
              this._grid._isCellEditable &&
              this._grid._isCellEditable(colIndex, rowIndex, this._grid) ===
                false
            )
              return false;
            return this._grid.editMode;
          };
        } else if (detailGrid._isCellEditable)
          mainGridPanel.getColumnModel().isCellEditable = function(
            colIndex,
            rowIndex
          ) {
            return this._grid._isCellEditable(colIndex, rowIndex, this._grid);
          };
      }

      if (detailGrid.menuButtons /* && !1*_app.toolbar_edit_btn */) {
        detailGridPanel.messageContextMenu = detailGrid.menuButtons;
        detailGridPanel.on("rowcontextmenu", fnRightClick);
      }

      if (buttons.length > 0) {
        detailGridPanel
          .getSelectionModel()
          .on("selectionchange", function(a, b, c) {
            if (!a || !a.grid) return;
            var titems = a.grid.getTopToolbar().items.items;
            for (var ti = 0; ti < titems.length; ti++) {
              if (titems[ti]._activeOnSelection)
                titems[ti].setDisabled(!a.hasSelection());
            }
          });
      }
      detailGridPanels.push(detailGridPanel);
    }
  }
  return detailGridPanels;
}
function fnRowInsert2(a, b) {
  var ex = new a._grid.record(Ext.apply({}, a._grid.initRecord));
  a._grid._insertedItems[ex.id] = true;
  ex.markDirty();
  var gp = Ext.getCmp(a._grid.id);
  gp.stopEditing();
  var insertIndex =
    !a._grid._insertAtLastIndex || !gp.getStore().data.items.length
      ? 0
      : gp.getStore().data.items.length;
  gp.getStore().insert(insertIndex, ex);
  gp.getView().refresh();
  gp.getSelectionModel().selectRow(insertIndex);
  gp.startEditing(
    insertIndex,
    typeof a._grid._startEditColumn == "undefined"
      ? 1
      : a._grid._startEditColumn
  );
}

function fnRowDelete2(a, b) {
  var sel = getSel(a._grid);
  if (!sel) return;
  if (a._grid._deleteControl && a._grid._deleteControl(sel, a._grid) == false) {
    return;
  }
  if (a._grid._insertedItems[sel.id]) {
    a._grid._insertedItems[sel.id] = false;
  } else {
    var delItem = {};
    for (var key in a._grid._pk) delItem[key] = sel.data[a._grid._pk[key]];
    a._grid._deletedItems.push(delItem);
  }
  var ds = a._grid.ds || a._grid.store;
  var io = ds.indexOf(sel);
  ds.remove(sel);
  if (ds.getCount() > 0) {
    if (io >= ds.getCount()) io = ds.getCount() - 1;
    a._grid.sm.selectRow(io, false);
  }
}

function prepareParams4grid(grid, prefix) {
  var dirtyCount = 0;
  var params = {};
  var items = grid._deletedItems;
  if (items)
    for (var bjk = 0; bjk < items.length; bjk++) {
      // deleted
      dirtyCount++;
      for (var key in items[bjk])
        params[key + prefix + "." + dirtyCount] = items[bjk][key];
      params["a" + prefix + "." + dirtyCount] = 3;
    }
  items = grid.ds.data.items;
  var pk = grid._pk;
  if (items)
    for (var bjk = 0; bjk < items.length; bjk++)
      if (items[bjk].dirty || grid._insertedItems[items[bjk].id]) {
        // edited&inserted
        dirtyCount++;
        for (var key in pk) {
          var val = pk[key];
          if (typeof val == "function") {
            params[key + prefix + "." + dirtyCount] = val(items[bjk].data);
          } else {
            params[key + prefix + "." + dirtyCount] =
              val.charAt(0) == "!" ? val.substring(1) : items[bjk].data[val];
          }
        }
        var changes = items[bjk].getChanges();
        for (var key in changes)
          params[key + prefix + "." + dirtyCount] = changes[key];
        if (grid._insertedItems[items[bjk].id]) {
          params["a" + prefix + "." + dirtyCount] = 2;
          if (grid._postMap)
            for (var key in grid._postMap) {
              var val = grid._postMap[key];
              if (typeof val == "function") {
                params[key + prefix + "." + dirtyCount] = val(items[bjk].data);
              } else {
                params[key + prefix + "." + dirtyCount] =
                  val.charAt(0) == "!"
                    ? val.substring(1)
                    : items[bjk].data[val];
              }
            }
          if (grid._postInsertParams) {
            for (var key in grid._postInsertParams)
              params[key + prefix + "." + dirtyCount] =
                grid._postInsertParams[key];
          }
          // Burada değişiklik var 29.06.2016
          // detayın - detayı
          if (items[bjk].children) {
            for (var i = 1; i <= items[bjk].children.length; i++) {
              params["a" + prefix + "." + dirtyCount + "_" + i + "." + 1] = 2;
              for (var key in items[bjk].children[i - 1]) {
                params[key + prefix + "." + dirtyCount + "_" + i + "." + 1] =
                  items[bjk].children[i - 1][key];
              }
            }
          }
        } else {
          params["a" + prefix + "." + dirtyCount] = 1;
        }
      }
  if (dirtyCount > 0) {
    params["_cnt" + prefix] = dirtyCount;
    params["_fid" + prefix] = grid.crudFormId;
    return params;
  } else return {};
}

function prepareParams4gridINSERT(grid, prefix) {
  // sadece master-insert durumunda cagir. farki _postMap ve hic bir zaman
	// _insertedItems,_deletedItems dikkate almamasi
  var dirtyCount = 0;
  var params = {};
  var dirtyCount = 0;
  var items = grid.ds.data.items;
  if (items)
    for (var bjk = 0; bjk < items.length; bjk++) {
      // inserted
      dirtyCount++;
      if (grid._postMap)
        for (var key in grid._postMap) {
          var val = grid._postMap[key];
          if (typeof val == "function") {
            params[key + prefix + "." + dirtyCount] = val(items[bjk].data);
          } else {
            params[key + prefix + "." + dirtyCount] =
              val.charAt(0) == "!" ? val.substring(1) : items[bjk].data[val];
          }
        }
      params["a" + prefix + "." + dirtyCount] = 2;
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
}

function prepareDetailGridCRUDButtons(grid, pk, toExtraButtonsFlag) {
  function add_menu() {
    if (grid.menuButtons) {
      for (var j = 0; j < grid.menuButtons.length; j++) {
        grid.menuButtons[j]._grid = grid;
      }
      grid.menuButtons = new Ext.menu.Menu({
        enableScrolling: false,
        items: grid.menuButtons
      });
      if (1 * _app.toolbar_edit_btn) {
        if (buttons.length > 0) buttons.push("-");
        buttons.push({
          id: "btn_operations_" + grid.id,
          cls: "x-btn-icon x-grid-menu",
          menu: grid.menuButtons
        });
      }
      grid.messageContextMenu = grid.menuButtons;
      if (!grid.listeners) grid.listeners = {};
      grid.listeners.rowcontextmenu = fnRightClick;
    }
  }
  if (pk) grid._pk = pk;
  var buttons = [];
  grid._insertedItems = {};

  if (grid.crudFlags) {
    if (grid.crudFlags.insertEditMode) {
      buttons.push({
        tooltip: grid.newRecordLabel || getLocMsg("js_add"),
        cls: "x-btn-icon x-grid-new",
        _grid: grid,
        handler: fnRowInsert2
      });
    }
    if (grid.crudFlags.remove) {
      buttons.push({
        tooltip: getLocMsg("js_delete"),
        cls: "x-btn-icon x-grid-delete",
        _grid: grid,
        handler: fnRowDelete2
      });
      grid._deletedItems = [];
    }
    if (grid.crudFlags.ximport) {
      if (
        typeof grid.crudFlags.ximport == "object" &&
        typeof grid.crudFlags.ximport.length != "undefined"
      ) {
        var xmenu = [];
        for (var qi = 0; qi < grid.crudFlags.ximport.length; qi++)
          if (!grid.crudFlags.ximport[qi].dsc)
            xmenu.push(grid.crudFlags.ximport[qi]);
          else {
            xmenu.push({
              text: grid.crudFlags.ximport[qi].dsc,
              cls: grid.crudFlags.ximport[qi].cls || "",
              _activeOnSelection: false,
              _grid: grid,
              ximport: grid.crudFlags.ximport[qi],
              handler: fnTableImport
            });
          }
        if (grid.extraButtons) {
          var bxx = xmenu.length > 0;
          for (var qi = 0; qi < grid.extraButtons.length; qi++)
            if (
              grid.extraButtons[qi] &&
              grid.extraButtons[qi].ref &&
              grid.extraButtons[qi].ref.indexOf("../import_") == 0
            ) {
              if (bxx) {
                bxx = false;
                xmenu.push("-");
              }
              grid.extraButtons[qi]._grid = grid;
              xmenu.push(grid.extraButtons[qi]);
              grid.extraButtons.splice(qi, 1);
              qi--;
            }
          if (grid.extraButtons.length == 0) grid.extraButtons = undefined;
        }
        buttons.push({
          // tooltip: getLocMsg("js_diger_kayitlardan_aktar"),
          cls: "x-btn-icon x-grid-import",
          _activeOnSelection: false,
          _grid: grid,
          menu: xmenu
        });
      } else
        buttons.push({
          // tooltip: getLocMsg("js_diger_kayitlardan_aktar"),
          cls: "x-btn-icon x-grid-import",
          _activeOnSelection: false,
          _grid: grid,
          handler: fnTableImport
        });
    }
  }

  if (buttons.length > 0) {
    if (toExtraButtonsFlag) {
      if (grid.extraButtons) {
        buttons.push("-");
        buttons.push(grid.extraButtons);
      }
      grid.extraButtons = buttons;
    } else {
      if (grid.extraButtons && grid.extraButtons.length > 0) {
        buttons.push("-");
        buttons.push(grid.extraButtons);
      }
      add_menu();
      if (grid.gridReport) addDefaultReportButtons(buttons, grid);
      if (!grid.noPrivilegeButtons) addDefaultPrivilegeButtons(buttons, grid);
      grid.tbar = buttons;
    }
  } else if (!toExtraButtonsFlag) {
    if (grid.extraButtons && grid.extraButtons.length > 0)
      buttons.push(grid.extraButtons);
    add_menu();
    if (grid.gridReport) addDefaultReportButtons(buttons, grid);
    if (!grid.noPrivilegeButtons) addDefaultPrivilegeButtons(buttons, grid);
    grid.tbar = buttons;
  }
  /*
	 * grid.ds.on('beforeload',function(){
	 * 
	 * });
	 */
}

// Multi Main Grid
function addTab4DetailGridsWSearchForm(obj) {
  var mainGrid = obj.detailGrids[0].grid,
    detailGridTabPanel = null;
  var searchFormPanel = new Ext.FormPanel(
    Ext.apply(mainGrid.searchForm.render(), {
      region: "north",autoHeight: true, anchor: "100%",
// region: "west", width:300,
      cls:'iwb-search-form', // collapseMode: 'mini',
      collapsible: !iwb.noCollapsibleSearchForm, animate: false, animCollapse: false, animFloat:false,
      title: mainGrid.name,
      border: false,
      keys: {
        key: 13,
        fn: function(ax, bx, cx) {
          detailGridTabPanel.getActiveTab().store.reload();
        }
      }
    })
  );

  // --standart beforeload, ondbliclick, onrowcontextmenu

  // detail tabs
  var detailGridPanels = [];
  for (var i = 0; i < obj.detailGrids.length; i++) {
	if (!obj.detailGrids[i])continue;
	if (obj.detailGrids[i].detailGrids) {
      // master/detail olacak
      obj.detailGrids[0].grid.searchForm = undefined;
      var xmxm = addTab4GridWSearchFormWithDetailGrids(Ext.apply({t:obj.t&&obj.detailGrids[i].grid?obj.t+'-'+obj.detailGrids[i].grid.gridId:null},obj.detailGrids[i]));
      if (xmxm.items.items[0].xtype == "form") {
        // ilk sıradaki gridin ,detail gridi varsa Search Formunu yok ediyor
        xmxm.items.items[0].destroy();
      }

      var detailGridPanel = xmxm.items.items[0].items.items[0];
      xmxm.store = detailGridPanel.store;
      detailGridPanel.store._formPanel = searchFormPanel;
      detailGridPanel.store._grid = mainGrid;
      detailGridPanel.store.on("beforeload", function(a, b) {
        if (a._grid.editMode) a._grid._deletedItems = [];
        if (a && a._formPanel.getForm())
          a.baseParams = Ext.apply(
            a._grid._baseParams || {},
            a._formPanel.getForm().getValues()
          ); // a._formPanel.getForm().getValues();
      });
      xmxm.closable = false;
      detailGridPanels.push(xmxm);
    } else {
      var detailGrid = obj.detailGrids[i].grid;
      if (!detailGrid || !detailGrid.gridId) continue;
      detailGrid._masterGrid = mainGrid;
      if (detailGrid._ready) {
        detailGridPanels.push(detailGrid);
        continue;
      }
      if (obj.detailGrids[i].pk) detailGrid._pk = obj.detailGrids[i].pk;
      var grdExtra = {
        title: obj.detailGrids[i]._title_ || detailGrid.name,cls:'iwb-grid-'+detailGrid.gridId,
// stripeRows: true,
        id: "gr" + Math.random(),
        autoScroll: true,
        border: false,
        clicksToEdit: 1 * _app.edit_grid_clicks_to_edit
      };
      var buttons = [];

      if (detailGrid.editGrid) addDefaultCommitButtons(buttons, detailGrid);

      if (detailGrid.hasFilter) {
        if (buttons.length > 0) buttons.push("-");
        buttons.push({
          tooltip: getLocMsg("remove_filter"),
          cls: "x-btn-icon x-grid-funnel",
          _grid: detailGrid,
          handler: fnClearFilters
        });
      }

      if (detailGrid.crudFlags) addDefaultCrudButtons(buttons, detailGrid);
      if (detailGrid.moveUpDown) addMoveUpDownButtons(buttons, detailGrid);
      addDefaultSpecialButtons(buttons, detailGrid);

      if (detailGrid.extraButtons) {
        if (buttons.length > 0) buttons.push("-");
        for (var j = 0; j < detailGrid.extraButtons.length; j++) {
          detailGrid.extraButtons[j]._grid = detailGrid;
          detailGrid.extraButtons[j].disabled =
            detailGrid.extraButtons[j]._activeOnSelection;
        }
        buttons.push(detailGrid.extraButtons);
      }
      if (detailGrid.menuButtons) {
        for (var j = 0; j < detailGrid.menuButtons.length; j++) {
          detailGrid.menuButtons[j]._grid = detailGrid;
        }
        detailGrid.menuButtons = new Ext.menu.Menu({
          enableScrolling: false,
          items: detailGrid.menuButtons
        });
        if (1 * _app.toolbar_edit_btn) {
          if (buttons.length > 0) buttons.push("-");
          buttons.push({
            id: "btn_operations_" + detailGrid.id,
            cls: "x-btn-icon x-grid-menu",
            disabled: true,
            _activeOnSelection: true,
            menu: detailGrid.menuButtons
          });
        }
      }
      if (detailGrid.gridReport) addDefaultReportButtons(buttons, detailGrid);
      addDefaultPrivilegeButtons(buttons, detailGrid);

      if (detailGrid.pageSize) {
        // paging'li toolbar
        var tbarExtra = {
          xtype: "paging",
          store: detailGrid.ds,
          pageSize: detailGrid.pageSize,
          displayInfo: detailGrid.displayInfo
        };
        if (buttons.length > 0) tbarExtra.items = organizeButtons(buttons);
        grdExtra.tbar = tbarExtra;
      } else if (buttons.length > 0) {
        // standart toolbar
        grdExtra.tbar = organizeButtons(buttons);
      }

      var eg = detailGrid.master_column_id
        ? detailGrid.editGrid
          ? Ext.ux.maximgb.tg.EditorGridPanel
          : Ext.ux.maximgb.tg.GridPanel
        : detailGrid.editGrid
          ? Ext.grid.EditorGridPanel
          : Ext.grid.GridPanel;
      var detailGridPanel = new eg(Ext.apply(detailGrid, grdExtra));
      detailGrid._gp = detailGridPanel;
      if (detailGrid.editGrid) {
        detailGridPanel.getColumnModel()._grid = detailGrid;
        if (!detailGrid.onlyCommitBtn) {
          detailGridPanel.getColumnModel().isCellEditable = function(
            colIndex,
            rowIndex
          ) {
            if (
              this._grid._isCellEditable &&
              this._grid._isCellEditable(colIndex, rowIndex, this._grid) ===
                false
            )
              return false;
            return this._grid.editMode;
          };
        } else if (detailGrid._isCellEditable)
          mainGridPanel.getColumnModel().isCellEditable = function(
            colIndex,
            rowIndex
          ) {
            return this._grid._isCellEditable(colIndex, rowIndex, this._grid);
          };
      }

      if (detailGrid.menuButtons /* && !1*_app.toolbar_edit_btn */) {
        detailGridPanel.messageContextMenu = detailGrid.menuButtons;
        detailGridPanel.on("rowcontextmenu", fnRightClick);
      }
      /*
		 * if(detailGrid.saveUserInfo)detailGridPanel.on("afterrender",function(a,b,c){
		 * detailGridPanel.getView().hmenu.add('-',{text: 'Mazgal
		 * Ayarları',cls:'grid-options1',menu: {items:[{text:'Mazgal Ayarlarını
		 * Kaydet',handler:function(){saveGridColumnInfo(grid.getColumnModel(),mainGrid.gridId)}},
		 * {text:'Varsayılan Ayarlara
		 * Dön',handler:function(){resetGridColumnInfo(mainGrid.gridId)}}]}});
		 * });
		 */
      if (
        detailGridPanel.crudFlags &&
        detailGridPanel.crudFlags.edit /* && 1*_app.toolbar_edit_btn */
      ) {
        detailGridPanel.on("rowdblclick", fnRowEditDblClick);
      }
      // grid2grid(mainGridPanel,detailGridPanel,obj.detailGrids[i].params);
      detailGridPanel.store._formPanel = searchFormPanel;
      detailGridPanel.store.on("beforeload", function(a, b) {
        if (a._grid.editMode) a._grid._deletedItems = [];
        if (a && a._formPanel.getForm())
          a.baseParams = Ext.apply(
            a._grid._baseParams || {},
            a._formPanel.getForm().getValues()
          ); // a._formPanel.getForm().getValues();
      });

      if (buttons.length > 0) {
        detailGridPanel
          .getSelectionModel()
          .on("selectionchange", function(a, b, c) {
            if (!a || !a.grid) return;
            var titems = a.grid.getTopToolbar().items.items;
            for (var ti = 0; ti < titems.length; ti++) {
              if (titems[ti]._activeOnSelection)
                titems[ti].setDisabled(!a.hasSelection());
            }
          });
      }
      detailGridPanel.store._formPanel = searchFormPanel;
      detailGridPanel.store._grid = mainGrid;
      detailGridPanel.store.on("beforeload", function(a, b) {
        if (a._grid.editMode) a._grid._deletedItems = [];
        if (a && a._formPanel.getForm())
          a.baseParams = Ext.apply(
            a._grid._baseParams || {},
            a._formPanel.getForm().getValues()
          ); // a._formPanel.getForm().getValues();
      });

      detailGridPanels.push(detailGridPanel);
    }
  }
  detailGridTabPanel = new Ext.TabPanel({
    region: "center",
    enableTabScroll: true,
    activeTab: 0,
    visible: false,
    items: detailGridPanels
  });
  var p = {
    layout: "border",
    title: obj._title_ || mainGrid.name,
    // id: obj.id||'x'+new Date().getTime(),
    border: false,
    closable: true,
    items: [searchFormPanel, detailGridTabPanel]
  };
  // p.iconCls='icon-cmp';
  p = new Ext.Panel(p);
  p._windowCfg = { layout: "border" };
  return p;
}

var lastNotificationCount = 0;
function promisUpdateNotifications(obj) {
  if (!obj || typeof obj.new_notification_count == "undefined") return;
  var newCount = 1 * obj.new_notification_count;
  if (lastNotificationCount != newCount) {
    var ctrl = Ext.getCmp("id_not_label");
    if (ctrl) {
      if (newCount == 0) ctrl.hide();
      else {
        ctrl.show();
        ctrl.setText(newCount);
      }
      lastNotificationCount = newCount;
    }
  }
}
/*
 * DEPRECATED function check4Notifications(nt){ promisRequest({
 * url:'ajaxQueryData?_qid=1488', requestWaitMsg:false, timeout:120000,
 * params:{}, successCallback: function(json){
 * promisUpdateNotifications({new_notification_count:json.data[0].new_notifications}); }
 * }); }
 */

function renderParentRecords(rs, sp) {
  var ss = "",
    r = null;
  if (!sp) sp = 1;
  if (rs && rs.length && rs.length >= sp) {
    for (var qi = rs.length - 1; qi >= 0; qi--) {
      r = rs[qi];
      if (qi != rs.length - 1) ss += "<br>";
      for (var zi = rs.length - 1; zi > qi; zi--) ss += " &nbsp; &nbsp;";
      ss += "&gt " + (qi != 0 ? r.tdsc : "<b>" + r.tdsc + "</b>");
      if (r.dsc)
        ss +=
          qi != 0
            ? ': <a href=# onclick="return fnTblRecEdit(' +
              r.tid +
              "," +
              r.tpk +
              ');">' +
              r.dsc +
              "</a>"
            : ': <b><a href=# onclick="return fnTblRecEdit(' +
              r.tid +
              "," +
              r.tpk +
              ');">' +
              r.dsc +
              "</a></b>"; // else ss+=': (...)';
    }
  }
  if (ss) {
    ss = '<div class="dfeed">' + ss + "</div>";
    if (r && r.tcc && r.tcc > 0)
      ss +=
        ' · <a href=# onclick="return fnTblRecComment(' +
        r.tid +
        "," +
        r.tpk +
        ');">Yorumlar (' +
        r.tcc +
        ")</a>";
  }
  return ss;
}

function renderParentRecords2(rs, sp) {
  // TODO: bizzat java 'ya gore
  var ss = "",
    r = null;
  if (!sp) sp = 1;
  if (rs && rs.length && rs.length > sp) {
    for (var qi = rs.length - 1; qi >= 0; qi--) {
      r = rs[qi];
      if (qi != rs.length - 1) ss += "<br>";
      for (var zi = rs.length - 1; zi > qi; zi--) ss += " &nbsp; &nbsp;";
      ss += "&gt " + (qi != 0 ? r._tableStr : "<b>" + r._tableStr + "</b>");
      if (r.recordDsc)
        ss +=
          qi != 0
            ? ': <a href=# onclick="return fnTblRecEdit(' +
              r.tableId +
              "," +
              r.tablePk +
              ');">' +
              r.recordDsc +
              "</a>"
            : ': <b><a href=# onclick="return fnTblRecEdit(' +
              r.tableId +
              "," +
              r.tablePk +
              ');">' +
              r.recordDsc +
              "</a></b>"; // else ss+=': (...)';
    }
  }
  if (ss) {
    ss = '<div class="dfeed">' + ss + "</div>";
    if (r && r.commentCount && r.commentCount > 0)
      ss +=
        ' · <a href=# onclick="return fnTblRecComment(' +
        r.tableId +
        "," +
        r.tablePk +
        ');">Yorumlar (' +
        r.commentCount +
        ")</a>";
  }
  return ss;
}

function manuelDateValidation(date1, date2, blankControl, dateControl) {
  if (blankControl) {
    // tarih alanlarının boş olup olmadığı kontrol ediliyor
    if (typeof date1 != "undefined") {
      if (date1.allowBlank == false && date1.getValue() == "") {
        Ext.infoMsg.msg(
          "error",
          getLocMsg("js_blank_text") + " (" + date1.fieldLabel + ")"
        );
        return false;
      }
    }

    if (typeof date2 != "undefined") {
      if (date2.allowBlank == false && date2.getValue() == "") {
        Ext.infoMsg.msg(
          "error",
          getLocMsg("js_blank_text") + " (" + date2.fieldLabel + ")"
        );
        return false;
      }
    }
  }

  if (
    dateControl &&
    typeof date1 != "undefined" &&
    typeof date2 != "undefined"
  ) {
    // birinci tarih ikinci tarihten küçük yada eşit olup olmadığı kontrol
	// ediliyor
    if (date1.getValue() > date2.getValue()) {
      Ext.infoMsg.msg(
        "error",
        getLocMsg("js_error_first_cannot_greater_than_second")
      ); // 'İlk Tarih İkinci Tarihten Büyük Olamaz'
      return false;
    }
  }
  return true;
}

/*
 * LovCombo içerisinde aranan değerler seçili mi ?
 */

function checkIncludedLovCombo(search_ids, checked_ids) {
  var result = false;
  var xsearch_ids = search_ids.split(",");
  var xchecked_ids = checked_ids.split(",");

  for (var i = 0; i < xchecked_ids.length; i++) {
    for (var j = 0; j < xsearch_ids.length; j++) {
      if (xchecked_ids[i] == xsearch_ids[j]) {
        result = true;
        break;
      }
    }
  }
  return result;
}

/*
 * Field value böyle alınmalı
 */

function getFieldValue(field) {
  if (field){
	  if(field._controlTip == 101)return field.hiddenValue;
	  if(field._checkbox){
		  var r ='';
		  if(field.items){
			  if(field.items.items){
				  var kk = field.items.items;
				  for(var qi=0;qi<kk.length;qi++){
					  var fl = kk[qi];//Ext.getCmp(kk[qi]);
					  if(fl.checked)r+=','+fl.inputValue;
				  }
			  } else if(field.items.length){//note rendered yet
				  var kk = field.items;
				  for(var qi=0;qi<kk.length;qi++){
					  var fl = kk[qi];//Ext.getCmp(kk[qi]);
					  if(fl.checked)r+=','+fl.inputValue;
				  }
			  }

		  }
		  if(r)return r.substr(1);
		  return '';

		  
	  } else return field.getValue();
	  
  }
  else return null;
}

function setFieldValue(field, value) {
  if (field) {
    if (field._controlTip != 101) field.setValue(value);
    else {
      field.hiddenValue = value;
      field.setRawValue(value);
    }
  }
}


/*
 * Eğer displayfield ise event tetiklenmeyecek ama fonksiyon çalışacak,
 * Displayfield değil ise event tetiklenerek fonksiyon çalışacak
 */

function applyEvent2Field(field, event, func, triggerOnRender) {
  if (field) {
    if (field._controlTip == 101) {
      func();
    } else {
      field.on(event, function() {
        func();
      });
      if (triggerOnRender) field.fireEvent(event);
    }
  }
}

function findInvalidFields(bf) {
  var result = [],
    it = bf.items.items,
    l = it.length,
    i,
    f;
  for (i = 0; i < l; i++) {
    if (!(f = it[i]).disabled && !f.isValid()) {
      result.push(f);
    }
  }
  return result;
}

function getSimpleCellMap(cells) {
  var jsMap = {};
  if (!cells || !cells.length) return jsMap;
  for (var qi = 0; qi < cells.length; qi++) {
    jsMap[cells[qi].id] = cells[qi];
  }
  return jsMap;
}

function timeDifDt(cd, timeTip, timeDif) {
  if (timeDif) {
    var tq = timeDif.split(":");
    if (tq.length > 1) {
      var tz = tq[0] * 60 * 60 * 1000 + tq[1] * 60 * 1000;
      if (tq.length > 2) tz += tq[2] * 1000;
      switch (timeTip) {
        case 0:
          cd.setHours(0, 0, 0, 0);
          cd = new Date(cd.getTime() + tz);
          break;
        case 1:
          cd = new Date(cd.getTime() + tz);
          break;
        case 2:
          cd = new Date(cd.getTime() - tz);
          break;
      }
    }
  }
  return cd;
}

function fileNameRender(a, b, c) {
      return (
        '<a style="font-weight:bold" target=_blank href="dl/' +
        encodeURIComponent(a) +
        "?_fai=" +
        (c.get('file_id') || c.get('file_attachment_id')) +
        '">' + a + "</a>"
      );
}

function fileNameRenderWithParent(a, b, c) {
  return (
    '<a style="font-weight:bold" target=_blank href="dl/' +
    encodeURIComponent(a) +
    "?_fai=" +
    (c.get('file_id') || c.get('file_attachment_id')) +
    '">' + a +"</a>" +
    (c.data._record ? renderParentRecords(c.data._record) : "")
  );
}

var usersBorderChat = ["#37cc00", "#5fcbff", "pink"];
function getUsers4Chat(users, pix, onlineStatus) {
  if (!users || users.length == 0) return "";
  var str = "";
  for (var qi = 0; qi < users.length; qi++)
    str +=
      ", &nbsp;" +
      (pix
        ? '<img src="sf/pic' +
          users[qi].userId +
          '.png" class="ppic-mini" style="margin-top: -2px;' +
          (onlineStatus
            ? "border:3px solid " + usersBorderChat[qi % usersBorderChat.length]
            : users[qi].userDsc.endsWith("·")
              ? "border:3px solid #37cc00"
              : "") +
          ';"> '
        : "") +
      '<a href=# onclick="return openChatWindow(' +
      users[qi].userId +
      ",'" +
      users[qi].userDsc +
      '\',true)"><span style="color: #ced5d8;">' +
      users[qi].userDsc +
      "</span></a>";
  return str.substring(2);
}

function safeIsEqual(a, b) {
  if (a == null) return b == null;
  else return a == b;
}

function syncMaptoStr(t, m, reload) {
  if (!m || !t) return null;
  var str = getUsers4Chat([m]);
  var actionMap = [
    "",
    "updated an existing record",
    "created a new record",
    "a record deleted"
  ];
  if (m.gridId) {
    var c = Ext.getCmp(t + "-" + m.gridId);
    if (!c) return null;
    if (!!reload && c.isVisible() && c.store) c.store.reload();
    if (c.name)
      str =
        '<b class="dirtyColor">' +
        c.name +
        "</b> mazgalında " +
        actionMap[m.crudAction] +
        " " +
        str +
        " tarafindan";
  } else {
    var c = Ext.getCmp(t + "-" + m.formCellId);
    if (!c) return null;
    str =
      '<b class="dirtyColor">' +
      c.fieldLabel +
      "</b> alaninda " +
      actionMap[m.crudAction] +
      " " +
      str +
      " tarafindan";
    if (!!reload) {
      if (c.label) c.label.removeClass("dirtyColor");
      if (c.store)
        try {
          c.store.reload();
        } catch (e) {
          promisRequest({
            url: "ajaxReloadFormCell?.t=" + t + "&_fcid=" + m.formCellId,
            requestWaitMsg: false,
            successCallback: function(json) {
              c.store.removeAll();
              c.store.loadData(json.data);
            }
          });
        }
    } else {
      if (c.label) c.label.addClass("dirtyColor");
    }
  }
  if (m.timeDif) str += ", " + fmtTimeAgo(m.timeDif) + " önce.";
  return !!reload || m.userId != _scd.userId ? str : null;
}

iwb.refreshFormCell = (fc)=>{
    if(fc.id){
    	var xx =fc.id.split('-'); 
    	if(xx.length==2){
    		var oldValue = fc.getValue();
    		promisRequest({
	        url: "ajaxReloadFormCell?.t=" + xx[0] + "&_fcid=" + xx[1],
	        requestWaitMsg: false,
	        successCallback: function(json) {
	          fc.store.removeAll();
	          fc.store.loadData(json.data);
	          fc.setValue(oldValue);
	        }
	      });
    	} else Ext.infoMsg.msg('error','refreshFormCell Error');
    }
}

function showNotifications(t) {
  promisRequest({
    url: "ajaxGetTabNotifications?.t=" + t,
    requestWaitMsg: false,
    successCallback: function(json) {
      if (json) {
        var tab = Ext.getCmp(json.tabId);
        if (tab) {
          if (tab._title) {
            tab.setTitle(tab._title);
            tab._title = false;
          }
          if (json.msgs) {
            for (var qi = 0; qi < json.msgs.length; qi++) {
              var msg = json.msgs[qi];
              msg.timeDif = json.time - msg.time;
              var msg = syncMaptoStr(json.tabId, msg, true);
              if (msg) Ext.infoMsg.msg("warning", msg, 10);
            }
          }
        }
      }
    }
  });

  return false;
}

function postMsgGlobal(msg, userId, userDsc) {
  promisRequest({
    url: "ajaxPostChatMsg",
    requestWaitMsg: false,
    params: { receiver_user_id: userId, msg: msg },
    successResponse: function() {
      var c = Ext.getCmp("idChatGrid_" + userId);
      if (c) c.store.reload();
      else openChatWindow(userId, userDsc, true);
    }
  });
}

function getUrlFromTab(tab) {
  try {
    if (tab._l) {
      if (!tab._l.pk) return null;
      var o = tab._l.pk.split("-");
      return (
        "showForm?_tb_id=" + o[0] + "&_tb_pk=" + o[1] + "&_fid=" + tab._formId
      );
    } else if (tab._lg && tab._tid) {
      if (!tab._callCfg) return null;
      var s = "showPage?_tid=" + tab._tid;
      if (tab._callCfg.request)
        for (var q in tab._callCfg.request)
          if (q != "_tid" && q != ".w" && q != ".t" && q != "_ServerURL_") {
            s += "&" + q + "=" + tab._callCfg.request[q];
          }
      return s;
    }
  } catch (e) {}
  return null;
}

function loadTabFromId(id) {
  promisRequest({
    url: "ajaxQueryData?_qid=2224&id=" + id,
    requestWaitMsg: false,
    successCallback: function(json) {
      if (json && json.data && json.data.length) {
        mainPanel.loadTab({ attributes: { href: json.data[0].dsc } });
      }
    }
  });
  return false;
}
function fmtTypingBlock(j) {
  if (!j || j.length == 0) return "";
  var u =
    j[0].sender_user_id != _scd.userId
      ? j[0].sender_user_id
      : j[0].receiver_user_id;
  return (
    '<tr style="display:none;" id="idTypingWith_' +
    u +
    '"><td width=24 valign=top>' +
    getPPicImgTag(u) +
    '</td><td width=5></td><td width=100% valign=top><img height=35 src="/ext3.4.1/custom/images/typing.svg"></td><td width=1></td><td width=5></td></tr>'
  );
}

function getPPicImgTag(userId, mid) {
  return (
    '<img src="sf/pic' +
    userId +
    '.png" class="ppic-' +
    (!mid ? "mini" : "middle") +
    '">'
  );
}

function loadMoreChat4(u) {
  var g = Ext.getCmp("idChatGrid_" + u);
  if (g && g.store) {
    if (!g.store.baseParams) g.store.baseParams = {};
    if (!g.store.baseParams.limit) g.store.baseParams.limit = 40;
    else g.store.baseParams.limit += 20;
    g.store.reload();
  }
  return false;
}

function fmtLoadMore(j) {
  if (!j || j.length == 0 || j.length % 20 > 0) return "";
  var u =
    j[0].sender_user_id != _scd.userId
      ? j[0].sender_user_id
      : j[0].receiver_user_id;
  return (
    '<tr><td width=24 valign=top> </td><td width=5></td><td width=100% valign=top><a href=# onclick="return loadMoreChat4(' +
    u +
    ');"><b>&nbsp;' +
    getLocMsg("js_load_more") +
    "</b></a><br/><hr/></td><td width=1></td><td width=5></td></tr>"
  );
}

function fmtChatList(j) {
  if (_scd.userId != j.sender_user_id) {
    var str =
      "<td width=24 valign=top>" +
      getPPicImgTag(j.sender_user_id) +
      "</td><td width=5></td><td width=100% valign=top>";
    if (j.msg.indexOf("!{") == 0 && j.msg.charAt(j.msg.length - 1) == "}")
      try {
        var x = j.msg.substring(2, j.msg.length - 1);
        var i = x.indexOf("-");
        var u = x.substring(0, i),
          l = x.substring(i + 1);
        str +=
          '<span style="color:red;">Link</span> :<a href=# style="text-w" onclick="return loadTabFromId(' +
          u +
          ')"><b>' +
          l +
          "</b></a><br/>";
      } catch (e) {
        str += '<b style="color:red;">!error!</b>';
      }
    else
      str +=
        '<span style="width:140px;word-wrap:break-word;display:block;">' +
        j.msg +
        "</span>";
    str +=
      '<span class="cfeed">' +
      fmtDateTimeWithDay2(j.sent_dttm) +
      "</span></td><td width=5></td><td width=1></td></tr><tr height=10><td colspan=5></td></tr>";
    return str;
  } else {
    var str =
      "<td width=24> </td><td width=5></td><td width=100% valign=top align=right>";
    if (j.msg.indexOf("!{") == 0 && j.msg.charAt(j.msg.length - 1) == "}")
      try {
        var x = j.msg.substring(2, j.msg.length - 1);
        var i = x.indexOf("-");
        var u = x.substring(0, i),
          l = x.substring(i + 1);
        str +=
          '<span style="color:red;">Link</span> :<a href=# style="text-w" onclick="return loadTabFromId(' +
          u +
          ')"><b>' +
          l +
          "</b></a><br/>";
      } catch (e) {
        str += '<b style="color:red;">!error!</b>';
      }
    else
      str +=
        '<span style="width:140px;word-wrap:break-word;display:block;">' +
        j.msg +
        "</span>";
    str +=
      '<span class="cfeed">' +
      fmtDateTimeWithDay2(j.sent_dttm) +
      "</span></td><td width=5></td><td width=24 valign=top>" +
      getPPicImgTag(j.sender_user_id) +
      "</td></tr><tr height=10><td colspan=5></td></tr>";
    return str;
  }
}
function fmtOnlineUser(j) {
  var str = '<table border=0 width=100% padding=0 style="margin-left:-1px;"';
  if (j.not_read_count > 0) str += " class='veliSelLightBlue'";
  str +=
    '><tr><td width=24 >&nbsp;'+
    '<span onclick="openChatWindow('+j.user_id+',\''+j.dsc+'\',true);">'+ 
		'<img src="sf/pic' +
		j.user_id +
		'.png" ' +
		(j.chat_status_tip
		  ? 'style="border-width: 3px;border-color:' +
		    usersBorderChat[j.chat_status_tip - 1] +
		    '" '
		  : "") +
		' class="ppic-mini">'+
    '</span>'+
    '</td><td width=99%> <span onclick="openChatWindow('+j.user_id+',\''+j.dsc+'\',true);">&nbsp; ';
  if (j.dsc.length > 20) j.dsc = j.dsc.substring(0, 18) + "...";
  str += j.dsc + "</span>"; 

  if (_scd.customizationId > 1) // linkedin vs.
	  str += '<div class="x-tool x-tool-close" id="ext-gen0199" onclick="return removeProjectMember('+j.user_id+');">&nbsp;</div>';
  
  if (j.not_read_count > 0)
    str +=
      '&nbsp; <span id="idChatNotRead_' +
      j.user_id +
      '" style="color:red;">(' +
      (j.not_read_count > 9 ? "+9" : j.not_read_count) +
      ")</span>";
  str += '<br>&nbsp; &nbsp; <span class="cfeed" style="font-size:.95em;"> ';
  var s = j.last_msg;
  if (
    s.length > 3 &&
    s.substring(0, 2) == "!{" &&
    s.substring(s.length - 1, s.length) == "}" &&
    s.indexOf("-") > -1
  ) {
    s = "Link :" + s.substring(s.indexOf("-") + 1, s.length - 1);
  }
  if (s.length > 27) s = s.substring(0, 25) + "...";
  str += s + "</span></td><td width=1%>";
  if (j.mobile) str += '<span class="status-item2 mobile-1">&nbsp;</span>';
  // str+='<span class="status-item2 status-'+j.chat_status_tip+'"
	// style="margin-right:1px;">&nbsp;</span>';
  str += "</td></tr></table>";
  return str;
}

function reEscape(s) {
  return s.replace(/([.<>!*+?^$|:/,(){}\[\]])/gm, "");
}

function reloadHomeTab() {
  Ext.infoMsg.alert("todo");
}
iwb.reload=function(g, p){
	if(!g)return;
	if(g.ds && g.ds.reload)g.ds.reload(p||{});
	else if(g.store && g.store.reload)g.store.reload(p||{});
}
iwb.refreshGrid = function(gridId){
	if(!gridId)return;
	var gxx = Ext.getCmp(gridId);
	if(gxx && gxx.store)gxx.store.reload();
}
function vcsHtml(x) {
  // 0: exclude, 1:edit, 2:insert, 3:delete, 9:synched
  if (x) {
    x = x.split(",");
    if (1 * x[0] != 9)
      return (
        '<img alt="' +
        x[1] +
        '" src="/ext3.4.1/custom/images/vcs' +
        x[0] +
        '.png" border=0>'
      );
    else return x[1];
  }
}


/* Log Utils */
if (!iwb.log) iwb.log = {};
if (!iwb.log.map) iwb.log.map = {};
iwb.log.log = function(group, msg) {
  if (!group || !msg) return;
  try {
    var m = iwb.log.map[group];
    if (!m) {
      m = {};
      iwb.log.map[group] = m;
    }
    var m2 = m[msg];
    if (!m2) {
      m[msg] = { no: 1, idttm: new Date() };
    } else {
      m2.no++;
      m2.vddtm = new Date();
    }
  } catch (e) {
    if (iwb.debug)
      console.log("error:iwb.log.log->[" + group + "," + msg + "]");
  }
};
iwb.log.persistLog = function() {};

function buildPanel(obj, isMasterFlag) {
  if (!obj) return false;
  if (obj.grid) {
    if (obj.detailGrids)
      return addTab4GridWSearchFormWithDetailGrids(obj, isMasterFlag);
    else return addTab4GridWSearchForm(obj);
  } else if (obj.detailGrids) return addTab4DetailGridsWSearchForm(obj);
  else return false;
}
iwb.ui.buildPanel = buildPanel;

iwb.ui.buildCRUDForm = function(getForm, callAttributes, _page_tab_id) {
  var extDef = getForm.render();
  if(extDef===false)return false;

  var extraItems =
    !getForm.renderTip || getForm.renderTip != 3
      ? extDef.items
      : extDef.items[0].items;


  function form_extra_processes() {
    // form-sms-mail post process
    var smtBtn = Ext.getCmp("smt_" + getForm.id);
    if (smtBtn) {
      switch (1 * _app.form_sms_mail_view_tip) {
        case 2:
          if (smtBtn && smtBtn.menu && smtBtn.menu.items.items) {
            var smsItems = smtBtn.menu.items.items;
            var smsStr = "";
            for (var qm = 0; qm < smsItems.length; qm++)
              if (smsItems[qm].checked) smsStr += "," + smsItems[qm].xid;
            if (smsStr) getForm._cfg.extraParams._smsStr = smsStr.substr(1);
          }
          break;
        case 1:
          var gals3 = getForm.smsMailTemplates,
            smsStr2 = "";
          for (var qs = 0; qs < gals3.length; qs++) {
            var cb1 = Ext.getCmp("_frm_smsmail" + getForm.id + gals3[qs].xid);
            if (cb1 && cb1.checked) {
              smsStr2 += "," + gals3[qs].xid;
            }
          }
          // Ext.infoMsg.alert(smsStr2);return false;
          if (smsStr2) getForm._cfg.extraParams._smsStr = smsStr2.substr(1);
          break;
      }
    }
    // form-conversion post process
    var cnvBtn = Ext.getCmp("cnv_" + getForm.id);
    if (cnvBtn) {
      switch (1 * _app.conversion_view_tip) {
        case 2:
          if (cnvBtn.menu && cnvBtn.menu.items.items) {
            var cnvItems = cnvBtn.menu.items.items;
            var cnvStr = "";
            for (var qm = 0; qm < cnvItems.length; qm++)
              if (cnvItems[qm].checked) cnvStr += "," + cnvItems[qm].xid;
            if (cnvStr) getForm._cfg.extraParams._cnvStr = cnvStr.substr(1);
          }
          break;
        case 1:
          var gals2 = getForm.conversionForms,
            cnvStr2 = "";
          for (var qs = 0; qs < gals2.length; qs++) {
            var cb1 = Ext.getCmp("_cnvrsn" + getForm.id + gals2[qs].xid);
            if (cb1 && cb1.checked) {
              cnvStr2 += "," + gals2[qs].xid;
            }
          }
          if (cnvStr2) getForm._cfg.extraParams._cnvStr = cnvStr2.substr(1);
          break;
      }
    }
    // for alarm
    var almBtn = Ext.getCmp("alm_" + getForm.id);
    if (almBtn) {
      switch (1 * _app.alarm_view_tip) {
        case 2:
          if (almBtn.menu && almBtn.menu.items.items) {
            var almItems = almBtn.menu.items.items;
            var almStr = "";
            for (var qm = 0; qm < almItems.length; qm++)
              if (almItems[qm].checked) {
                almStr += "," + almItems[qm].xid;
                var vx =
                  almItems[qm].menu &&
                  almItems[qm].menu.items &&
                  almItems[qm].menu.items.items &&
                  almItems[qm].menu.items.items.length > 0 &&
                  almItems[qm].menu.items.items[0].value
                    ? almItems[qm].menu.items.items[0].value
                    : null;
                if (vx) almStr += "-" + vx;
              }
            if (almStr) getForm._cfg.extraParams._almStr = almStr.substr(1);
          }
          break;
        case 1:
          var gals = getForm.alarmTemplates,
            almStr2 = "";
          for (var qs = 0; qs < gals.length; qs++) {
            var cb1 = Ext.getCmp("_alarm" + getForm.id + gals[qs].xid);
            if (cb1 && cb1.checked) {
              almStr2 += "," + gals[qs].xid;
              var dt1 = Ext.getCmp("_alarm_dttm" + getForm.id + gals[qs].xid);
              if (dt1 && dt1.dateValue)
                almStr2 += "-" + fmtDateTime(dt1.dateValue);
              else {
                Ext.infoMsg.msg(
                  "warning",
                  "Alarm tarihi alanina deger girilmeli"
                );
                return false;
              }
            }
          }
          if (almStr2) getForm._cfg.extraParams._almStr = almStr2.substr(1);
      }
    }
  }

  var realAction = 1 * extDef.baseParams.a;
  var btn = [];
  if (!getForm.viewMode) {
    var sv_btn_visible = extDef.baseParams.sv_btn_visible || 1;
    if (sv_btn_visible * 1 == 1) {
      var saveBtn = {
        text: getLocMsg((1 * getForm.a == 1 ? "update" : realAction == 5 ? "copy" : "save")),// + '
																								// '+getForm.name,
        id: "sb_" + getForm.id,
        iconAlign: "top",
        scale: "medium",
        //style:"margin-left:5px",
        cls:"isave-toolbar",
        iconCls: "ikaydet",
        handler: function(a, b, c) {
          if (
            realAction == 5 &&
            getForm.copyTableIds &&
            getForm.copyTableIds.length > 0
          ) {
            var mzmz,
              qwin = new Ext.Window({
                layout: "fit",
                width: 300,
                height: 200,
                closeAction: "destroy",
                plain: true,
                modal: true,
                title: "${copy_which_sub_info}",
                items: {
                  xtype: "form",
                  border: false,
                  labelWidth: 10,
                  items: {
                    xtype: "checkboxgroup",
                    itemCls: "x-check-group-alt",
                    columns: 1,
                    items: getForm.copyTableIds
                  }
                },
                buttons: [
                  {
                    text: "${ok}",
                    handler: function(ax, bx, cx) {
                      var tblIds = "";
                      for (var qi = 0; qi < getForm.copyTableIds.length; qi++) {
                        if (Ext.get(getForm.copyTableIds[qi].id).dom.checked) {
                          tblIds += "," + getForm.copyTableIds[qi].id.substr(7);
                        }
                      }
                      var r = null;
                      var bm = false;
                      if (extDef.componentWillPost || bm) {
                        if (getForm._cfg.formPanel.getForm().findInvalid()) {
                          var vals = getForm._cfg.formPanel
                            .getForm()
                            .getValues();
                          if (extDef.componentWillPost) {
                            r = extDef.componentWillPost(vals);
                            if (!r) return;
                          }
                        } else {
                          //getForm._cfg.formPanel.getForm().findInvalid();
                          return;
                        }
                      }
                      if (!getForm._cfg.formPanel.getForm().findInvalid()) {
                        //getForm._cfg.formPanel.getForm().findInvalid();
                        return null;
                      }
                      getForm._cfg.dontClose = 0;
                      if (typeof r == "object") getForm._cfg.extraParams = r;
                      if (tblIds) {
                        if (getForm._cfg.extraParams)
                          getForm._cfg.extraParams._copy_tbl_ids = tblIds.substr(1);
                        else
                          getForm._cfg.extraParams = {
                            _copy_tbl_ids: tblIds.substr(1)
                          };
                      }
                      qwin.destroy();
                      formSubmit(getForm._cfg);
                    }
                  },
                  {
                    text: "Cancel",
                    handler: function() {
                      qwin.destroy();
                    }
                  }
                ]
              });
            qwin.show();
            return;
          }

          var r = null;
          // manuel validation
          var bm = false;

          if (extDef.componentWillPost || bm) {
            if (getForm._cfg.formPanel.getForm().findInvalid()) {
              var vals = getForm._cfg.formPanel.getForm().getValues();
              if (extDef.componentWillPost) {
                r = extDef.componentWillPost(vals);
                if (!r) return;
              }
            } else {
              //getForm._cfg.formPanel.getForm().findInvalid();
              return;
            }
          }
          if (!getForm._cfg.formPanel.getForm().findInvalid()) {
            //getForm._cfg.formPanel.getForm().findInvalid();
            return null;
          }
          getForm._cfg.dontClose = 0;
          getForm._cfg.extraParams = {};
          if (typeof r == "object" && r != null) getForm._cfg.extraParams = r;

          form_extra_processes();

          formSubmit(getForm._cfg);
        }
      };

      btn.push(saveBtn);
    }
    // post & continue
    if (getForm.contFlag && 1 * getForm.contFlag == 1 && realAction == 2) {
      btn.push({
        text: getLocMsg("save_continue"),
        id: "cc_" + getForm.id,
        iconAlign: "top",
        scale: "medium",
        style:"margin-left:15px",
        iconCls: "isave_cont",
        handler: function(a, b, c) {
          if (
            !getForm._cfg.formPanel.getForm().isDirty() &&
            !confirm("${attention_you_save_without_change_are_you_sure}")
          )
            return;
          var r = null;
          if (extDef.componentWillPost) {
            if (getForm._cfg.formPanel.getForm().findInvalid()) {
              r = extDef.componentWillPost(
                getForm._cfg.formPanel.getForm().getValues()
              );
              if (!r) return;
            } else {
              //getForm._cfg.formPanel.getForm().findInvalid();
              return;
            }
          }
          if (!getForm._cfg.formPanel.getForm().findInvalid()) {
            //getForm._cfg.formPanel.getForm().findInvalid();
            return null;
          }
          if (!getForm._cfg.callback)
            getForm._cfg.callback = function(js, conf) {
              if (js.success) Ext.infoMsg.msg("info", getLocMsg("operation_successful"));
              if(extDef._tab_order.getValue)extDef._tab_order.setValue(extDef._tab_order.getValue()+1)
            };
          getForm._cfg.dontClose = 1;
          getForm._cfg.extraParams = {};
          if (typeof r == "object" && r != null) getForm._cfg.extraParams = r;
          form_extra_processes();
          formSubmit(getForm._cfg);
        }
      });

      if(false)btn.push({
        text: "Save&New".toUpperCase(),
        id: "cn_" + getForm.id,
        iconAlign: "top",
        scale: "medium",

//        iconCls: "isave_new",
        handler: function(a, b, c) {
          var r = null;
          if (extDef.componentWillPost) {
            if (getForm._cfg.formPanel.getForm().findInvalid()) {
              r = extDef.componentWillPost(
                getForm._cfg.formPanel.getForm().getValues()
              );
              if (!r) return;
            } else {
              //getForm._cfg.formPanel.getForm().findInvalid();
              return;
            }
          }
          if (!getForm._cfg.formPanel.getForm().findInvalid()) {
            //getForm._cfg.formPanel.getForm().findInvalid();
            return null;
          }
          if (!getForm._cfg.callback)
            getForm._cfg.callback = function(js, conf) {
              if (js.success) Ext.infoMsg.msg("info", getLocMsg("operation_successful"));
              if(extDef._tab_order.getValue)extDef._tab_order.setValue(extDef._tab_order.getValue()+1)
            };
          getForm._cfg.dontClose = 1;
          getForm._cfg.resetValues = 1;
          if (typeof r == "object") getForm._cfg.extraParams = r;
          formSubmit(getForm._cfg);
        }
      });
    }
  }
  
  // close
  if(_app.show_close_button)btn.push({
    tooltip: "Close",
    id: "cl_" + getForm.id,
    iconAlign: "top",
    scale: "medium",

    iconCls: "ikapat",
    handler: function(a, b, c) {
      function closeMe() {
        if (!callAttributes.modalWindowFlag) mainPanel.getActiveTab().destroy();
        else mainPanel.closeModalWindow();
      }
      if (
        !getForm.viewMode &&
        1 * _app.form_cancel_dirty_control &&
        getForm._cfg.formPanel.getForm().isDirty()
      )
        Ext.infoMsg.confirm(
          "There are changed fields. Do you still want to close?",
          () => {
            closeMe();
          }
        );
      else closeMe();
    }
  });

 
  // approval
  if (1 * getForm.a == 1 && getForm.approval) {
    btn.push("-");
    // btn.push({text: '${onay_adimi}<br>'+getForm.approval.stepDsc});
    if (getForm.approval.wait4start) {
      btn.push({
        text: getForm.approval.btnStartApprovalLabel||getLocMsg("wf_manual_start"),
        id: "dapp_" + getForm.id,
        iconAlign: "top",
        scale: "medium",

        iconCls: "icon-request-approve",
        handler: function(a, b, c) {
          submitAndApproveTableRecord(901, getForm, getForm.approval.dynamic);
        }
      });
    } else if(getForm.approval.approvalRecordId){
      btn.push({
        text: getForm.approval.stepDsc||getLocMsg("wf_approve"),
        id: "aapp_" + getForm.id,
        tooltip: getForm.approval.stepDsc,
        iconAlign: "top",
        scale: "medium",
// style: {margin: "0px 5px 0px 5px"},
        iconCls: "icon-approve",
        handler: function(a, b, c) {
          if (!getForm.viewMode) {
            var r = null;
            if (extDef.componentWillPost) {
              if (getForm._cfg.formPanel.getForm().findInvalid()) {
                r = extDef.componentWillPost(
                  getForm._cfg.formPanel.getForm().getValues()
                );
                if (!r) return;
              } else {
                //getForm._cfg.formPanel.getForm().findInvalid();
                return;
              }
            }
            if (!getForm._cfg.formPanel.getForm().findInvalid()) {
              //getForm._cfg.formPanel.getForm().findInvalid();
              return null;
            }
            getForm._cfg.dontClose = 0;
            if (typeof r == "object") {
              getForm._cfg.extraParams = r;
            }
          }
          if (getForm.approval.approveFormId) {
            mainPanel.loadTab({attributes:{href:"showForm?a=2&_fid="+getForm.approval.approveFormId+"&_arid=" + getForm.approval.approvalRecordId, modalWindow: true}});
// mainPanel.closeModalWindow();
            return;
          } else submitAndApproveTableRecord(1, getForm);
        }
      });
      if (getForm.approval.returnFlag) {
        btn.push({
          text: getForm.approval.returnStepDsc||getLocMsg("wf_return"),
          id: "gbapp_" + getForm.id,
          iconAlign: "top",
          scale: "medium",
// style: {margin: "0px 5px 0px 5px"},
          iconCls: "icon-return",
          handler: function(a, b, c) {
            submitAndApproveTableRecord(2, getForm);
          }
        });
      }
      btn.push({
        text: getForm.approval.rejectStepDsc||getLocMsg("wf_reject"),
        id: "rapp_" + getForm.id,
        iconAlign: "top",
        scale: "medium",
// style: {margin: "0px 5px 0px 5px"},
        iconCls: "icon-reject",
        handler: function(a, b, c) {
          submitAndApproveTableRecord(3, getForm);
        }
      });
      btn.push({
        text: getLocMsg("wf_logs"),
        id: "lapp_" + getForm.id,
        iconAlign: "top",
        scale: "medium",
// style: {margin: "0px 5px 0px 5px"},
        iconCls: "ilog",
        handler: function(a, b, c) {
        	iwb.showApprovalLogs(getForm.approval.approvalRecordId)
        }
      });
    } else {//TODO write approval name somwehere
    	
    }
  }



  if (getForm.extraButtons && getForm.extraButtons.length > 0) {
    btn.push("-");
    btn.push(getForm.extraButtons);
  }

  if (getForm.fileAttachFlag) {
// btn.push("-");
	  var xmenu=[];
	  if(!getForm.viewMode)xmenu.push({
          text: getLocMsg("attach_files"),
          _f: getForm,
          handler: function(a) {
            var getForm = a._f;
            var table_pk = "";
            if (getForm.a == 1) {
              for (var key in getForm.pk)
                if (key != "customizationId" && key != "projectId")
                  table_pk += "|" + getForm.pk[key];
            } else table_pk = "|" + getForm.tmpId;
            fnNewFileAttachment4Form(a._f.crudTableId, table_pk.substring(1));
          }
        });
	  xmenu.push({
          text: getLocMsg("related_files"),
          _f: getForm,
          handler: function(a, b, c) {
            var getForm = a._f;
            var table_pk = "";
            if (getForm.a == 1) {
              for (var key in getForm.pk)
                if (key != "customizationId" && key != "projectId")
                  table_pk += "|" + getForm.pk[key];
            } else table_pk = "|" + getForm.tmpId;
            var cfg = {
              attributes: {
                modalWindow: true,
                href: "showPage?_tid="+ (_scd.customFile ? 6813:9),
                baseParams: {
                  xtable_id: getForm.crudTableId,
                  xtable_pk: table_pk.substring(1)
                }
              }
            };
            cfg.attributes._title_ = getForm.name;
            mainPanel.loadTab(cfg);
          }
        });
    btn.push({
      tooltip:
        getLocMsg("files") +
        (getForm.fileAttachCount > 0
          ? " (" + getForm.fileAttachCount + ")"
          : ""),
      id: "af_" + getForm.id,
      iconAlign: "top",
      scale: "medium",
      text:(getForm.fileAttachCount > 0
              ? "<span class='file-count-badge'>" + getForm.fileAttachCount + "</span>"
                      : undefined),

      iconCls: "ifile_attach",
      menu: xmenu
    });
  }

  if (getForm.a == 1 || getForm.tmpId) {
//	  debugger
    var xb = false;
    if (getForm.commentFlag) {
      if (xb) {
// btn.push("-");
        xb = false;
      }


      btn.push({
    	tooltip: getLocMsg('comments'),
        id: "cd_" + getForm.id,
        iconAlign: "top",
        scale: "medium",
        text:(getForm.commentCount > 0
                ? "<span class='comment-count-badge'>" + getForm.commentCount + "</span>"
                        : undefined),
        listeners: {
          render: function(ax, bx, cx) {
            var axx = getForm.commentExtra;
            if (axx) {
              // var ax=Ext.getCmp('cd_' + getForm.id);
              var tt = new Ext.ToolTip({
                target: ax.getEl(),
                anchor: "top",
                html:
                  "<b>" +
                  axx.user_dsc +
                  "</b>: " +
                  Ext.util.Format.htmlEncode(axx.msg) +
                  "<br/><span class=cfeed> · " +
                  Ext.util.Format.htmlEncode(axx.last_dttm) +
                  "</span>",
                dismissDelay: 5000
              });
              if (axx.is_new) {
                new Ext.util.DelayedTask(function() {
                  tt.show();
                }).delay(500);
              }
            }
          }
        },
// style: {margin: "0px 5px 0px 5px"},
        iconCls: "ibig_comment",
        handler: function(a, b, c) {
/*          if (a._commentCount)
            a.setText("${comment} (" + a._commentCount + ")");
          else
            a.setText(
              "${comment}" +
                (getForm.commentCount > 0
                  ? " (" + getForm.commentCount + ")"
                  : "")
            );*/
          var table_pk = "";
          if (getForm.a == 1) {
            for (var key in getForm.pk) {
              if (key != "customizationId" && key != "projectId") {
                table_pk += "|" + getForm.pk[key];
              }
            }
          } else table_pk = "|" + getForm.tmpId;
          mainPanel.loadTab({
            attributes: {
              id:
                "modal_comment_" +
                getForm.crudTableId +
                "-" +
                table_pk.substring(1),
              modalWindow: true,
              href: "showPage?_tid=836",
              slideIn: "t",
              _title_: getForm.name,
              _pk: {
                tcomment_id: "comment_id"
              },
              baseParams: {
                xtable_id: getForm.crudTableId,
                xtable_pk: table_pk.substring(1)
              }
            }
          });
        }
      });
    }
//    if (getForm.commentFlag || getForm.fileAttachFlag) btn.push("-");
  }
  btn.push("->");


  if(!getForm.viewMode && _app.form_template && 1*_app.form_template)btn.push({
	tooltip: getLocMsg("templates"),
    id: "ttemp_" + getForm.id,
    iconAlign: "top",
    scale: "medium",
// style: {margin: "0px 5px 0px 5px"},
    iconCls: "ibookmark",
    handler: function(a, b, c) {
      if (!getForm._loaded) {
        getForm._loaded = true;
        promisRequest({
          url: "ajaxQueryData?_qid=483",
          params: {
            xform_id: getForm.formId
          },
          successCallback: function(j) {
            if (j.success && j.data.length > 0) {
              while (a.menu.items.items.length > 2) a.menu.remove(2);
              a.menu.add("-");
              var pf = true;
              for (var q = 0; q < j.data.length; q++) {
                if (j.data[q].public_flag && pf) {
                  if (q > 0) a.menu.add("-");
                  pf = false;
                }
                a.menu.add({
                  text: j.data[q].dsc,
                  _id: j.data[q].form_value_id,
                  handler: function(a, b, c) {
                    promisRequest({
                      url: "ajaxQueryData?_qid=503",
                      params: {
                        xform_value_id: a._id
                      },
                      successCallback: function(j2) {
                        if (j2.success && j2.data.length > 0) {
                          var f2 = getForm._cfg.formPanel.getForm();
                          var j3 = {};
                          for (var q2 = 0; q2 < j2.data.length; q2++) {
                            j3[j2.data[q2].dsc] = j2.data[q2].val;
                          }
                          f2.setValues(j3);
                        }
                      }
                    });
                  }
                });
              }
            }
          }
        });
      }
    },
    menu: {
      items: [
        {
          text: getLocMsg("save"),
          iconCls: "icon-ekle",
          handler: function(a, b, c) {
            var p = prompt("Template Name", "");
            if (p) {
              var params = getForm._cfg.formPanel.getForm().getValues();
              params._dsc = p;
              promisRequest({
                url: "ajaxBookmarkForm?_fid=" + getForm.formId,
                params: params,
                successCallback: function() {
                  getForm._loaded = false;
                  Ext.infoMsg.msg("success", "Template Saved", 3);
                }
              });
            }
          }
        },
        {
          text: getLocMsg("update"),
          iconCls: "icon-duzenle",
          handler: function(a, b, c) {
            mainPanel.loadTab({
              attributes: {
                _title_: getForm.name,
                modalWindow: true,
                href: "showPage?_tid=259&_gid1=491",
                _pk: {
                  tform_value_id: "form_value_id"
                },
                baseParams: {
                  xform_id: getForm.formId
                }
              }
            });
          }
        }
      ]
    }
  });

  if (_scd.customizationId == 0) {
// btn.push("-");
    var menuItems = [];
    if (_scd.administratorFlag) {
      menuItems.push({
        text: getLocMsg("settings"),
        handler: function(a, b, c) {
          mainPanel.loadTab({
            attributes: {
              _title_: getForm.name,
              _width_: 600,
              modalWindow: true,
              href:
                "showPage?_tid=543&_gid1=439&_fid2=997&a=1&tform_id=" +
                getForm.formId,
              _pk1: {
                tform_cell_id: "form_cell_id"
              },
              baseParams: {
                xform_id: getForm.formId
              }
            }
          });
        }
      });

      if (
        (_app.mail_flag && 1 * _app.mail_flag) ||
        (_app.sms_flag && 1 * _app.sms_flag)
      )
        menuItems.push({
          text:
            "SMS/E-MAIL Settings" +
            (getForm.smsMailTemplateCnt
              ? " (" + getForm.smsMailTemplateCnt + ")"
              : ""),
          handler: function(a, b, c) {
            mainPanel.loadTab({
              attributes: {
                _title_: getForm.name,
                modalWindow: true,
                href: "showPage?_tid=259&_gid1=1294",
                _pk: {
                  tform_sms_mail_id: "form_sms_mail_id"
                },
                baseParams: {
                  xform_id: getForm.formId,
                  xtable_id: getForm.crudTableId || ""
                }
              }
            });
          }
        });
      if (_app.form_conversion_flag && 1 * _app.form_conversion_flag)
        menuItems.push({
          text:
            "Conversions" +
            (getForm.conversionCnt ? " (" + getForm.conversionCnt + ")" : ""),
          handler: function(a, b, c) {
            mainPanel.loadTab({
              attributes: {
                _title_: getForm.name,
                modalWindow: true,
                href: "showPage?_tid=259&_gid1=1344",
                _pk: {
                  tconversion_id: "conversion_id"
                },
                baseParams: {
                  xsrc_form_id: getForm.formId,
                  xtable_id: getForm.crudTableId || ""
                }
              }
            });
          }
        });
    }
    if (_scd.administratorFlag) {
      menuItems.push("-", {
        text: "Form Hints",
        handler: function(a, b, c) {
          mainPanel.loadTab({
            attributes: {
              _title_: getForm.name,
              modalWindow: true,
              href: "showPage?_tid=259&_gid1=1700",
              _pk: {
                tform_hint_id: "form_hint_id"
              },
              baseParams: {
                xform_id: getForm.formId
              }
            }
          });
        }
      });
    }
    btn.push({
      tooltip: getLocMsg("settings"),
      id: "fs_" + getForm.id,
      iconAlign: "top",
      scale: "medium",
// style: {margin: "0px 5px 0px 5px"},
      iconCls: "isettings",
      menu: {
        items: menuItems
      }
    });
  }

  // manual form-conversion menu
  if (1 * getForm.a == 1) {
    var pk = null,
      toolButtons = [];
    for (var xi in getForm.pk)
      if (xi != "customizationId" && xi != "projectId") {
        pk = getForm.pk[xi];
        break;
      }
// btn.push("-", " ", " ", " ");
    if (false && (
      (getForm.manualConversionForms &&
        getForm.manualConversionForms.length > 0) ||
      (getForm.reportList && getForm.reportList.length > 0)
    )) {
      toolButtons.push({
        text: "Record Info",
        /* iconCls:'icon-info', */
        handler: function() {
          fnTblRecEdit(getForm.crudTableId, pk, false);
        }
      });
      toolButtons.push("-");
      if (
        getForm.manualConversionForms &&
        getForm.manualConversionForms.length > 0
      ) {
        for (var xi = 0; xi < getForm.manualConversionForms.length; xi++)
          getForm.manualConversionForms[xi].handler = function(aq, bq, cq) {
            mainPanel.loadTab({
              attributes: {
                href:
                  "showForm?a=2&_fid=" +
                  aq._fid +
                  "&_cnvId=" +
                  aq.xid +
                  "&_cnvTblPk=" +
                  pk
              }
            });
          };
        toolButtons.push({
          text: "Conversion",
          iconCls: "icon-operation",
          menu: getForm.manualConversionForms
        });
      }

      btn.push({
        tooltip: "Others...",
        iconAlign: "top",
        scale: "medium",
// style: {margin: "0px 5px 0px 5px"},
        iconCls: "ibig_info",
        menu: toolButtons
      });
    } else {
      if(_scd.customizationId==0)btn.push({
    	tooltip: "Record Info",
        iconAlign: "top",
        scale: "medium",
// style: {margin: "0px 5px 0px 5px"},
        iconCls: "ibig_info",
        handler: function() {
          fnTblRecEdit(getForm.crudTableId, pk, false);
        }
      });
    }
  }

  var iconCls = getForm.a == 1 ? "icon-edit" : "icon-new";

  var o = {
    autoScroll: true,
    border: false,
    tbar: btn,
// bodyStyle: "padding:3px;",
    iconCls: callAttributes.iconCls || iconCls,
    _title_: callAttributes.title || "Form: " + getForm.name,
    _width_: getForm.defaultWidth,
    _height_: getForm.defaultHeight
  };
  if (!callAttributes.modalWindowFlag) {
    o = Ext.apply({ closable: true, title: getForm.name }, o);
  }

  if (getForm.hmsgs) {
    var hints = getForm.hmsgs;
    for (var qs = hints.length - 1; qs >= 0; qs--) {
      var icn = "";
      switch (hints[qs].tip * 1) {
        case 1:
          icn = "information2";
          break;
        case 2:
          icn = "warning2";
          break;
        case 3:
          icn = "error2";
          break;
      }
      var lbl = new Ext.form.Label({
        hideLabel: true,
        html: hints[qs].text,
        cls: icn
      });
      extDef.items.unshift({
        xtype: "fieldset",
        title: "",
        cls: "xform-hint",
        labelWidth: 0,style:"padding:20px;",
        bodyStyle:
          "padding:10px;border-radius:5px;background-color:#f8fdff;border:1px solid #b2ddff;",
        items: [lbl]
      });
    }
  }

  if (_app.form_msgs_visible_on_form * 1 == 1 && getForm.msgs) {
    var lbl = new Ext.form.Label({
      hideLabel: true,
      html: getForm.msgs.join("<br>"),
      cls: "information"
    });
    extDef.items.unshift({
      xtype: "fieldset",
      cls: "xform-hint",
      title: "",
      labelWidth: 0,
      // bodyStyle: 'background-color:#FFF8C6;',
      items: [lbl]
    });
  }
  if (_app.live_sync_record && 1 * _app.live_sync_record && getForm.a == 1)
    extDef.items.unshift({
      hidden: !getForm.liveSyncBy,
      xtype: "fieldset",
      id: "live_sync_" + getForm.id,
      title: "",
      cls: "xform-live-sync",
      labelWidth: 0,
      bodyStyle: "padding:none !important;",
      // bodyStyle: 'background-color:#FFF8C6',
      items: [
        new Ext.form.Label({
          hideLabel: true,
          id: "live_sync_lbl_" + getForm.id,
          html: getForm.liveSyncBy
            ? "Live collaboration with:  " +
              getUsers4Chat(getForm.liveSyncBy, true)
            : "!",
          cls: "collaboration"
        })
      ]
    });

  var p = new Ext.FormPanel(Ext.apply(o, extDef));
  if (!getForm._cfg) getForm._cfg = {};
  getForm._cfg.formPanel = p;
  getForm._cfg._callAttributes = callAttributes;

  if (_app.form_msgs_visible_on_form * 1 != 1 && getForm.msgs)
    Ext.infoMsg.msg("info", getForm.msgs.join("<br>"), 3);

  if (
    _app.live_sync_record &&
    1 * _app.live_sync_record != 0 &&
    extDef.baseParams[".t"]
  ) {
    p._l = {
      pk: 1 * getForm.a == 1 ? getForm.crudTableId + "-" + pk : false
    };
  }

  return p;
};
iwb.isMonacoReady = function(e) {
  if (!e.editor) {
    Ext.infoMsg.msg(
      "error",
      "Monaco Editor still loading!<br/>Good things take time",
      5
    );
    return false;
  }
  return true;
};

iwb.addCss=function(cssCode,id){
	Ext.util.CSS.createStyleSheet(cssCode,"iwb-tpl-"+id);
}
iwb.loadComponent=function(id){
// Ext.util.CSS.createStyleSheet(cssCode,"iwb-tpl-"+id);
}
iwb.serverDttmDiff=0;
iwb.getDate=function(x){// server DateTime OR parse(x)
	if(!x)return iwb.serverDateDiff ? new Date(new Date().getTime()+iwb.serverDateDiff): new Date();
	if(x.length<=10)return Date.parseDate(x,iwb.dateFormat);
	return Date.parseDate(x,iwb.dateFormat+" H:i:s");
}

iwb.ajax={}
iwb.ajax.query=function(queryId,params,callback){
	params=params||{};
	iwb.request({url:'ajaxQueryData?_qid='+queryId,params:params,requestWaitMsg:!!params._mask, successCallback:callback||false})
}
iwb.ajax.postForm=function(formId,action,params,callback){
	params=params||{};
	iwb.request({url:'ajaxPostForm?_fid='+formId+'&a='+action,params:params,requestWaitMsg:!!params._mask,successCallback:callback||false})
}
iwb.ajax.execFunc=function(funcId,params,callback){
	params=params||{};
	iwb.request({url:'ajaxExecDbFunc?_did='+funcId,params:params,requestWaitMsg:!!params._mask,successCallback:callback||false})
}
iwb.ajax.REST=function(serviceName,params,callback){
	iwb.request({url:'ajaxCallWs?serviceName='+serviceName,params:params||{},successCallback:callback||false})
}

iwb.ui.openForm=function(formId,action,params, reloadGrid, callback){
	mainPanel.loadTab({successCallback:callback||false,
        attributes: {
          href: 'showForm?_fid='+formId+'&a='+action,
          id: params && params.id ? params.id :false,
          params: params||{},
          _grid:reloadGrid||false
        }
      });
}

iwb.apexCharts={}
iwb.apexGraph = function(dg, gid, callback) {
	if(!dg.groupBy)return;
	  var newStat = 1 * dg.funcTip ? dg.funcFields : "";
	  var params = {};
	  if (newStat) params._ffids = newStat;
	  if (1 * dg.graphTip >= 5) params._sfid = dg.stackedFieldId;
	  var series=[], labels=[], lookUps=[], chart =null;
	  var xid = gid;
	  var el = document.getElementById(gid);
	  if(!el)return;
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
//	                    height: 80*d.length+40,
	                    type: 'bar',
	                    stacked: true,
	                    toolbar: {show: false}
	                },
	                grid: {
	                    show: true,
	                    borderColor: '#3a3f49',
	                  },
	                plotOptions: {
//	                    bar: {horizontal: true},
	                    
	                },
	                series: series,
//title: {text: dg.name},
	                xaxis: {
	                    categories: labels,
	                },
	                yaxis: {labels: {show: !!dg.legend}, axisTicks: {color: '#777'}},
	            }
	        	break;
	        case 3:// pie
	            d.map((z)=>{
	                series.push(1*z.xres);
	                labels.push(z.dsc||'-');
	            });
	            var options = {
                    chart: {id:'apex-'+gid, type: 'donut', toolbar: {show: false}},
	                series: series, labels: labels, legend: dg.legend ? {position:'bottom'} : {show:false}
	                ,dataLabels: dg.legend ? {}:{formatter: function (val, opts) {return labels[opts.seriesIndex] + ' - ' + fmtDecimal(val);}}
	            }

	        	break;
	        case 1:// column
	        case 2:// line
	        	var colCount = newStat.split(',').length;
	        	for(var qi=0;qi<colCount;qi++){
	        		series.push({name:j.lookUps ? j.lookUps[qi] : ('Count'), data:[]})
	        	}
	        	d.map((z)=>{
	        		for(var qi=0;qi<colCount;qi++){
	        			series[qi].data.push(1*z[qi?'xres'+(qi+1):'xres']);
	        		}
	                labels.push(z.dsc);
	            });

	            options = {
                    grid: {
                        show: true,
                        borderColor: '#3a3f49',
                      },
                     chart: {
	                	id:'apex-'+gid,
//	                    height:document.getElementById()50*d.length+30,
	                    type: 1 * dg.graphTip==1?'bar':'spline',
	                    toolbar: {show: false}
	                },
	                stroke: 1 * dg.graphTip==1 ? {}:{
	                    curve: 'smooth'
	                },
	                series: series,
	                xaxis: {
	                    categories: labels,
	                },
	                yaxis: {labels: {show: !!dg.legend}},
	            }
	        	break;
			}

			if(options){
		        options.theme= {
		            mode: 'dark',
		            palette: iwb.graphPalette||'palette6',
		          };
		        options.chart.height = el.offsetHeight && el.offsetHeight>50 ?  el.offsetHeight-20 : el.offsetWidth/2;
				if(iwb.apexCharts[xid])iwb.apexCharts[xid].destroy();
				if(callback)options.chart.events= {
						dataPointSelection: function(event, chartContext, config) {
							if(config.selectedDataPoints && config.selectedDataPoints && config.selectedDataPoints.length){
								var yx = config.selectedDataPoints[0];
								callback(yx.length ? d[yx[0]].id : false);
						    }
						}
				}
	            var chart = new ApexCharts(
	                el,
	                options
	            );
	            iwb.apexCharts[xid]=chart;
	            chart.render();
			}
			
	    }
	  });
}
iwb.hideGridColumn = function(gxx, id, visible){
	if(gxx.id){
		var myGrid = Ext.getCmp(gxx.id);
		if(myGrid){
			var colModel = myGrid.getColumnModel();
			var ix = colModel.findColumnIndex(id);
			if(ix>=0)colModel.setHidden(ix, !visible);
			return;
		}
	}
	if(gxx.columns && gxx.columns.length)gxx.columns.map((o)=>{
		if(o.id==id)o.hidden=!visible;
	});
}

iwb.changeFieldLabel=function(field, label){
	if(!field)return;
    if (field.label) field.label.dom.innerHTML = label; 
    else field.fieldLabel = label;
}


iwb.hasPartInside=function(all,sub){
	if(typeof all=='undefined')return false;
	if((''+all).length==0)return false;
	if((','+all+',').indexOf(','+sub+',')==-1)return false;
	return true;
}
iwb.safeEquals= function(v1, v2){
	if(v1==='' || (typeof v1=='undefined')){
		return (v2==='' || (typeof v2=='undefined'));
	} else if(v2==='' || (typeof v2=='undefined'))return false;
	return v1==v2;
}

iwb.hasPartInside2=function(all,sub){
	if(typeof all=='undefined' || typeof sub=='undefined')return false;
	if((''+all).length==0 || (''+sub).length==0)return false;
	var sub2 = (''+sub).split(',');
	for(var qi=0;qi<sub2.length;qi++)
		if((','+all+',').indexOf(','+sub2[qi]+',')!=-1)return true;
	return false;
}

iwb.formElementProperty = function(opr, elementValue, value){
	switch(1*opr){
	case -1://is Empty
		return elementValue==='' || elementValue===null || (typeof elementValue=='undefined');
	case -2://is not empty
		return !(elementValue==='' || elementValue===null || (typeof elementValue=='undefined'));
	case	8://in
		if(value==='' || (typeof value=='undefined'))return false;
		return iwb.hasPartInside2(value, elementValue);
	case	9://not in
		if(value==='' || (typeof value=='undefined'))return true;
		return !iwb.hasPartInside2(value, elementValue);
	case	0://equals
		return iwb.safeEquals(elementValue, value);
	case	1://not equals
		return !iwb.safeEquals(elementValue, value);
		
	}
	return false;
	
}


function extractSurveyJsResult(o){
	if(o && Array.isArray(o)){
		if(o.length && o[0].content){
			return o[0].content.substr(o[0].content.lastIndexOf('=')+1);
		} else 
			return o.join(','); 
			
	} else 
		return o;
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
						params2[kk+fid+'.'+(qi+1)] = extractSurveyJsResult(cell[kk]);
					}
					if(masterParams)for(var kk in masterParams)
						params2[kk.substr(1)+fid+'.'+(qi+1)] = extractSurveyJsResult(masterParams[kk]);
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
						params2[kk+fid+'.'+(qi+1)] = extractSurveyJsResult(cell[kk]);
					}
					if(masterParams)for(var kk in masterParams)
						params2[kk.substr(1)+fid+'.'+cnt] = extractSurveyJsResult(masterParams[kk]);
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
			params2[k] =  extractSurveyJsResult(o);
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
		Ext.infoMsg.msg("success", getLocMsg("operation_successful"));
		mainPanel.remove(mainPanel.getActiveTab());
	})
}

iwb.fileUploadSurveyJs=(tableId, tablePk, survey, options)=>{
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
