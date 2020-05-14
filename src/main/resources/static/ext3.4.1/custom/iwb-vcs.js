
function vcsFix(xgridId, tid, action) {
  Ext.infoMsg.confirm("Would you like to FIX the problem?", () => {
    promisRequest({
      url: "ajaxVCSFix",
      requestWaitMsg: true,
      params: { t: tid, a: action },
      successCallback: function(j) {
        Ext.getCmp(xgridId).store.reload();
      }
    });
  });
  return false;
}

function vcsPush(xgrid, tid, tpk) {
  var xparams = { t: tid, k: tpk };
  var xgrd = xgrid;
  if(_app.vcs_comment && 1*_app.vcs_comment){//0:none, 1:required, 2:not required
	  var req = 1*_app.vcs_comment;
	  var comment = prompt("Push Comment?"+ (req==1? " (required)":""));
	  if(req==1 && !comment){
		  Ext.infoMsg.msg("error", "Push Canceled")
		  return;
	  }
	  if(comment)xparams.comment=comment;
  }
  promisRequest({
    url: "ajaxVCSObjectPush",
    params: xparams,
    successCallback: function(j) {
      if (j.error) {
        switch (j.error) {
          case "force":
            Ext.infoMsg.confirm(
              "There is conflicts. Do you STILL want to Overwrite?",
              () => {
                xparams.f = 1;
                var xxgrd = xgrd;
                promisRequest({
                  url: "ajaxVCSObjectPush",
                  params: xparams,
                  successCallback: function(j) {
                    Ext.infoMsg.msg("success", "VCS Objects Pushed");
                    iwb.reload(xxgrd);
                  }
                });
              }
            );
            break;
          default:
            Ext.infoMsg.alert("Error", "unknown error: " + j.error, "error");
        }
        return;
      }
      Ext.infoMsg.msg("success", "VCS Objects Pushed");
      iwb.reload(xgrd);
    }
  });
}

function vcsPushMulti(xgrid, tid, tpk, force) {
  var xparams = { t: tid, k: tpk };
  if(_app.vcs_comment && 1*_app.vcs_comment){//0:none, 1:required, 2:not required
	  var req = 1*_app.vcs_comment;
	  var comment = prompt("Push Comment?"+ (req==1? " (required)":""));
	  if(req==1 && !comment){
		  Ext.infoMsg.msg("error", "Push Canceled")
		  return;
	  }
	  if(comment)xparams.comment=comment;
  }
  if(force)xparams.f=1;
  var xgrd = xgrid;
  promisRequest({
    url: "ajaxVCSObjectPushMulti",
    params: xparams,requestWaitMsg: true,
    successCallback: function(j) {
      if (j.error) {
        switch (j.error) {
          case "force":
            Ext.infoMsg.confirm(
              "There is conflicts. Do you want to Force PUSH?",
              () => {
//            	  if(prompt('Then type [force]')!=='force')return;
                xparams.f = 1;
                var xxgrd = xgrd;
                promisRequest({
                  url: "ajaxVCSObjectPushMulti",
                  params: xparams,requestWaitMsg: true,
                  successCallback: function(j) {
                    Ext.infoMsg.msg("success", "["+j.commitCount+"] VCS Objects Pushed");
                    iwb.reload(xxgrd);
                  }
                });
              }
            );
            break;
          default:
            Ext.infoMsg.alert("Error", j.error, "error");
        }
        return;
      }
      Ext.infoMsg.msg("success", "["+j.commitCount+"] VCS Objects Pushed");
      iwb.reload(xgrd);
    }
  });
}
function vcsPushAll(xgrid, keyz, force) {
  var xparams = { k: keyz };
  if(_app.vcs_comment && 1*_app.vcs_comment){//0:none, 1:required, 2:not required
	  var req = 1*_app.vcs_comment;
	  var comment = prompt("Push Comment?"+ (req==1? " (required)":""));
	  if(req==1 && !comment){
		  Ext.infoMsg.msg("error", "Push Canceled")
		  return;
	  }
	  if(comment)xparams.comment=comment;
  }
  if(force)xparams.f=1;
  var xgrd = xgrid;
  promisRequest({
    url: "ajaxVCSObjectPushAll",
    requestWaitMsg: true,
    params: xparams,
    successCallback: function(j) {
      if (j.error) {
        switch (j.error) {
          case "force":
            Ext.infoMsg.confirm(
              "There is conflicts. Do you want to Force PUSH?",
              () => {
//            	  if(prompt('Then type [force]')!=='force')return;
                xparams.f = 1;
                var xxgrd = xgrd;
                promisRequest({
                  url: "ajaxVCSObjectPushAll",
                  params: xparams,requestWaitMsg: true,
                  successCallback: function(j) {
                    Ext.infoMsg.msg("success", "["+j.commitCount+"] VCS Objects Pushed");
                    iwb.reload(xxgrd);
                  }
                });
              }
            );
            break;
          default:
            Ext.infoMsg.alert("Error", j.error, "error");
        }
        return;
      }
      Ext.infoMsg.msg("success", "["+j.commitCount+"] VCS Objects Pushed");
      iwb.reload(xgrd);
    }
  });
}

function vcsPull(xgrid, tid, tpk) {
  var xparams = { t: tid, k: tpk };
  var xgrd = xgrid;
  promisRequest({
    url: "ajaxVCSObjectPull",
    requestWaitMsg: true,
    params: xparams,
    successCallback: function(j) {
      if (j.error) {
        switch (j.error) {
          case "force":
            Ext.infoMsg.confirm(
              (j.error_msg || "There is conflicts.") +
                " Do you STILL want to Overwrite?",
              () => {
                xparams.f = 1;
                var xxgrd = xgrd;
                promisRequest({
                  url: "ajaxVCSObjectPull",
                  requestWaitMsg: false,
                  params: xparams,
                  successCallback: function(j) {
                    Ext.infoMsg.msg("success", "VCS Objects Pulled");
                    iwb.reload(xxgrd);
                  }
                });
              }
            );
            break;
        }
        return;
      }
      Ext.infoMsg.msg("success", "VCS Objects Pulled");
      iwb.reload(xgrd);
    }
  });
}

iwb.valsDiffData = false;
iwb.showValsDiffinMonaco = function (qi) {
	if(!window.monaco){
		Ext.infoMsg.msg("info", "Loading Monaco", 2);
		require.config({ paths: { vs: "/monaco/min/vs" } });
		require(["/monaco/min/vs/editor/editor.main"], function() {
// iwb.showValsDiffinMonaco(qi);
		});
		return;
	}
  var win = new Ext.Window({
    layout: 'fit',
    width: 900,
    height: 800, title: '<span style="color:red">'+iwb.valsDiffData[qi].name+'</span> Field Differences',
    closeAction: 'destroy',
    plain: true,

    html: '<div id="idx-mnc2-' + _page_tab_id + '" style="height:770px"></div>',
    listeners: {
      'afterrender': function () {
    	monaco.editor.setTheme(iwb.monacoTheme || "vs-dark");
        var originalModel = monaco.editor.createModel(iwb.valsDiffData[qi].local, "javascript");
        var modifiedModel = monaco.editor.createModel(iwb.valsDiffData[qi].remote, "javascript");

        var diffEditor = monaco.editor.createDiffEditor(document.getElementById("idx-mnc2-" + _page_tab_id), {
          // You can optionally disable the resizing
          enableSplitViewResizing: false,

          // Render the diff inline
          renderSideBySide: false
        });
        diffEditor.setModel({
          original: originalModel,
          modified: modifiedModel
        });
      }
    },
    buttons: [{
      text: 'Close',
      handler: function () {
        win.close();
      }
    }]
  });
  win.show();

}
iwb.fnTblRecColumnVCSUpdate=function (tid, tpk, clmn, i) {
	if(confirm('Are you sure to Update Local Value')){
// alert('todo: ' + tid + ' / ' + tpk + ' / ' + clmn);
		iwb.request({
  		  url: 'ajaxVCSObjectPullColumn', params: { t: tid, k: tpk, c: clmn }, requestWaitMsg: true, successCallback: function (j) {
	  	          Ext.infoMsg.msg('success', 'VCS Object Column Succesfully Pulled');
	  	          var el = document.getElementById('vcs-col-'+i);
	  	          if(el)el.outerHTML='';
  		  }
  	  }); 
	}
	return false;
}
iwb.fnTblRecVCSDiff = function (tid, tpk, a, dsc) {
	iwb.request({
    url: 'ajaxVCSObjectConflicts', params: { k: tid + '.' + tpk }, requestWaitMsg: true, successCallback: function (j) {
      if (j.data) {
    	  if(!j.data.length){
    		  Ext.infoMsg.msg("info", "No difference between VCS Server and Local", 2);
    		  return;
    	  }
        iwb.valsDiffData = j.data;
        var s = '<table width=100%><thead style="background:rgba(255,255,255,.2)"><tr><td width=10% style="padding: 5px;">field name</td><td width=45% style="padding: 5px;">local value</td><td width=45% style="padding: 5px;">remote value</td></tr></thead>';
        for (var qi = 0; qi < j.data.length; qi++)
          if (j.data[qi].editor != 11 && j.data[qi].editor != 41) s += '<tr style="color: #ccc;" id="vcs-col-'+qi+'"><td>' + j.data[qi].name + '</td><td>' + j.data[qi].local 
          	+ '<a title="Update Local" href=# style="float:right" onclick="return iwb.fnTblRecColumnVCSUpdate('+tid+','+tpk+',\''+j.data[qi].name+'\','+qi+')"><div style="width: 20px;height: 20px;    background-position: center; transform: rotate(90deg);" class="icon-vcs-pull">&nbsp;</div></a></td><td>' + j.data[qi].remote + '</td></tr>';
          else s += '<tr style="color: #ccc;background:rgba(0,0,0,.2)"><td>' + j.data[qi].name + '</td><td align=center colspan=2><a href=# onclick="return iwb.showValsDiffinMonaco(' + qi + ')">show diff in editor</a></td></tr>';
        s += '</table>';
        var wndx = new Ext.Window({
          modal: true,closeAction: 'destroy',
          title: 'Record Differences'+ (dsc ? ' <span class="vcs-diff">' + unescape(dsc)+'</span>':''),
          width: 800,
          autoHeight: true,
          html: s,
          buttons: [{ text: 'Force Pull', handler: function () {         	  
        	  if(confirm('Are you sure to Force Pull?'))iwb.request({
        		  url: 'ajaxVCSObjectPull', params: { t: tid, k: tpk, f: 1 }, successCallback: function (j) {
		  	          Ext.infoMsg.msg('success', 'VCS Object Succesfully Pulled');
		  	          wndx.close(); 
        		  }
        	  }); 
          } }, { text: 'Force Push', handler: function () {         	  
        	  if(confirm('Are you sure to FORCE-PUSH to VCS Server?') && prompt('type push')=='push')iwb.request({
        		  url: 'ajaxVCSObjectPush', params: { t: tid, k: tpk, f: 1 }, successCallback: function (j) {
		  	          Ext.infoMsg.msg('success', 'VCS Object Succesfully Pushed');
		  	          wndx.close(); 
        		  }
        	  }); 
          } }, { text: 'Close', handler: function () { wndx.close(); } }]
        });
        wndx.show();
      } else if (j.lcl) fnTblRecEdit(tid, tpk);
      else alert('Remote:\n' + objProp(j.rmt));
    }
  });

}


function fncMnuVcs(xgrid) {
  return [
    {
      text: "Push",
      iconCls: "icon-vcs-push",
      _grid: xgrid,
      handler: function(aq) {
        var sel = getSels(aq._grid);// ._gp.getSelectionModel().getSelections();
        if (sel && sel.length > 0) {
          var d = sel[0].data.pkpkpk_vcsf;
          if (d) {
            vcsPush(aq._grid, aq._grid.crudTableId, sel[0].id);
          } else Ext.infoMsg.alert("error", "Not VCS Object", "error");
        } else Ext.infoMsg.alert("error", "Not selection");
      }
    },
    {
      text: "Pull",
      iconCls: "icon-vcs-pull",
      _grid: xgrid,
      handler: function(aq) {
        var sel = getSels(aq._grid);// ._gp.getSelectionModel().getSelections();
        if (sel && sel.length > 0) {
          var d = sel[0].data.pkpkpk_vcsf;
          if (d) {
            vcsPull(aq._grid, aq._grid.crudTableId, sel[0].id);
          } else Ext.infoMsg.alert("error", "Not VCS Object");
        } else Ext.infoMsg.alert("error", "Not selection");
      }
    },
    "-",
    {
        text: "Show Diff",
        _grid: xgrid,
        handler: function(aq) {
          var sel = getSels(aq._grid);// ._gp.getSelectionModel().getSelections();
          sel &&
            sel.length > 0 &&
            sel[0].data.pkpkpk_vcsf &&
            iwb.fnTblRecVCSDiff(aq._grid.crudTableId,sel[0].id, 1, sel[0].data.dsc);;
        }
      },'-',
    /*
	 * ,{text:'Synchronize Selected Record(Recursive)', _grid:xgrid,
	 * handler:function(aq){ Ext.infoMsg.alert('TODO') }}
	 */
    {
      text: "Synchronize Table Records",
      _grid: xgrid,
      handler: function(aq) {
        mainPanel.loadTab({
          attributes: {
            modalWindow: true,
            _title_: aq._grid.name,
            href: "showPage?_tid=238&_gid1=2127",
            tid: aq._grid.crudTableId
          }
        });
        // promisRequest({url:'ajaxVCSObjectsList',params:{_tid:aq._grid.crudTableId},
		// successCallback:function(j){Ext.infoMsg.alert('info',j.msgs.join('<br>'));}});
      }
    },
    "-",
    {
      text: "Add to VCS",
      _grid: xgrid,
      handler: function(aq) {
        var sel = getSels(aq._grid);// ._gp.getSelectionModel().getSelections();
        if (sel && sel.length > 0 && !sel[0].data.pkpkpk_vcsf) {
          promisRequest({
            url: "ajaxVCSObjectAction",
            params: { t: aq._grid.crudTableId, k: sel[0].id, a: 2 },
            successCallback: function(j) {
              Ext.infoMsg.msg("success", "Added to VCS");
              iwb.reload(aq._grid);
            }
          });
        }
      }
    },
    {
        text: "Ignore",
        _grid: xgrid,
        handler: function(aq) {
          var sel = getSels(aq._grid);// ._gp.getSelectionModel().getSelections();
          sel &&
            sel.length > 0 &&
            sel[0].data.pkpkpk_vcsf &&
            Ext.infoMsg.confirm("Are you sure?", () => {
              promisRequest({
                url: "ajaxVCSObjectAction",
                params: { t: aq._grid.crudTableId, k: sel[0].id, a: 3 },
                successCallback: function(j) {
                  Ext.infoMsg.msg("success", "Ignored from VCS");
                  iwb.reload(aq._grid);
                }
              });
            });
        }
      },'-',
      {text: "History", menu:[
    	  {text:'Local History', _grid: xgrid, handler:(aq)=>{
              mainPanel.loadTab({
                  attributes: {
//                    _title_: "Search Form",
                    modalWindow: true,
                    href: "showPage?_tid=259&_gid1=835",
                    baseParams: {
                      xtable_id:aq._grid.crudTableId, 
                      xtable_pk: getSels(aq._grid)[0].id
                    }
                  }
                });
    	  }}
    	  , {text:'VCS Server History', _grid: xgrid, handler:()=>alert('TODO')}
      ]},'-',
      {
          text: "Copy to Another Project",
          _grid: xgrid,
          handler: function(aq) {
            var sel = getSels(aq._grid);// ._gp.getSelectionModel().getSelections();
            if(sel &&
              sel.length > 0 &&
              sel[0].data.pkpkpk_vcsf){
            	iwb.ajax.query(2765,{},(jj)=>{
            	    var cmbSt2=[];
            	    var zz = jj.data;
            	    if(zz.length)for(var qi=0;qi<zz.length;qi++)if(_scd.projectId!=zz[qi].id)cmbSt2.push([zz[qi].id, zz[qi].dsc]);
            	    var cmbProject3 = new Ext.form.ComboBox({width:399, typeAhead: false, mode: 'local',triggerAction: 'all',selectOnFocus:true,forceSelection:true, store:cmbSt2, emptyText:'Select Project'});
            	    var wx=new Ext.Window({
            	        modal:true, shadow:false, border:false,
            	        width:400,
            	        autoHeight:true,
            	        closeAction:'destroy',
            	        title:'Copy to Project',

            	        items: [cmbProject3],

            	        buttons: [{
            	            text: 'Copy',
            	            handler: function(){
            		
            	            	var d = cmbProject3.getValue();
				                if(d)promisRequest({
				                  url: "ajaxCopyTableRecursive",
				                  params: { d:d, t: aq._grid.crudTableId, k: sel[0].id, a: 3 },
				                  successCallback: function(j) {
				                    Ext.infoMsg.msg("success", "Copied");
				                    iwb.reload(aq._grid);
				                    wx.destroy();
				                  }
				                });
            	            }}]
            	    })
            	    wx.show();

            	});
            }
          }
        }
  ];
}