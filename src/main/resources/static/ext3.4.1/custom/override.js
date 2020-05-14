//Mypage
if (!Ext.ux) Ext.ux = {};
try {
  if (Ext.ux.TabCloseMenu)
    Ext.override(Ext.ux.TabCloseMenu, {
      createMenu: function() {
        //  if(!this.menu){
        var items = [
          {
            itemId: "close",
            text: getLocMsg("js_close_tab"),
            scope: this,
            handler: this.onClose
          }
        ];
        if (this.showCloseAll) {
          items.push("-");
        }
        items.push({
          itemId: "closeothers",
          text: getLocMsg("js_close_other_tabs"),
          scope: this,
          handler: this.onCloseOthers
        });
        if (this.showCloseAll) {
          items.push({
            itemId: "closeall",
            text: getLocMsg("js_close_all"),
            scope: this,
            handler: this.onCloseAll
          });
        }
        
        this.menu = new Ext.menu.Menu({
          items: items
        });
        //}

        return this.menu;
      }
    });
} catch (eq) {
  console.log(eq);
}
//GridPanel
if (!Ext.ux.tree) Ext.ux.tree = {};

try {
  if (Ext.ux.tree.CheckTreePanel)
    Ext.override(Ext.ux.tree.CheckTreePanel, {
      getPath: function(id) {
        var node = this.getNodeById(id);
        if (node) {
          return node.getPath();
        }
        var paths = this.root.getPath();
        forEach = function(list, fun, sope) {
          if (!list || list.length == 0) {
            return;
          }
          for (var i = 0, length = list.length; i < length; i++) {
            var node = list[i];
            var args = [];
            args.push(node);
            if (arguments.length > 3) {
              for (var ii = 3; ii < arguments.length; ii++) {
                args.push(arguments[ii]);
              }
            }
            var result = fun.apply(sope, args);
            if (result) {
              return result;
            }
          }
        };

        getChildNodes = function(parent) {
          var children = parent.children || parent.childNodes;
          if (children && children.length == 0 && parent.attributes) {
            children = parent.attributes.children;
          }
          return children;
        };

        getPath = function(item, paths) {
          if (item.id == id) {
            return paths + "/" + item.id;
          }
          return forEach(
            getChildNodes(item),
            getPath,
            this,
            paths + "/" + item.id
          );
        };
        return forEach(getChildNodes(this.root), getPath, this, paths);
      },
      setValue: function(val) {
        // uncheck all first
        this.clearValue();

        // process arguments
        this.value = this.convertValue.apply(this, arguments);
        //	alert(objProp(this.getLoader().store));

        // check nodes
        Ext.each(
          this.value,
          function(id) {
            var paths = this.getPath(id);
            this.expandPath(paths);
            var n = this.getNodeById(id);
            if (n) {
              var ui = n.getUI();
              if (ui && ui.setChecked) {
                ui.setChecked(true);
                // expand checked nodes
                if (true === this.expandOnCheck) {
                  n.bubbleExpand();
                }
              }
            }
          },
          this
        );
        return this.value;
      }
    });
} catch (eq) {
  console.log(eq);
}

try {
  // Checkbox Selection Model Select All Reset
  Ext.override(Ext.grid.CheckboxSelectionModel, {
    clearSelections: function(fast) {
      this.constructor.superclass.clearSelections.apply(this, [fast]);
      var hd = this.grid.el.query(".x-grid3-hd-checker");
      if (!hd) {
        return;
      }
      hd = Ext.get(hd[0]);
      var isChecked = hd.hasClass("x-grid3-hd-checker-on");
      if (isChecked) {
        hd.removeClass("x-grid3-hd-checker-on");
      }
    }
  });
} catch (eq) {
  console.log(eq);
}

try {
  var doQueryExtended = {
    doQuery: function(q, forceAll) {
      q = Ext.isEmpty(q) ? "" : q;
      var qe = {
        query: q,
        forceAll: forceAll,
        combo: this,
        cancel: false
      };
      if (this.fireEvent("beforequery", qe) === false || qe.cancel) {
        return false;
      }
      q = qe.query;
      forceAll = qe.forceAll;
      if (forceAll === true || q.length >= this.minChars) {
        if (this.lastQuery !== q) {
          this.lastQuery = q;
          if (this.mode == "local") {
            this.selectedIndex = -1;
            if (forceAll) {
              this.store.clearFilter();
            } else {
              this.store.filter(this.displayField, q, true, false); // supply the anyMatch option
            }
            this.onLoad();
          } else {
            this.store.baseParams[this.queryParam] = q;
            this.store.load({
              params: this.getParams(q)
            });
            this.expand();
          }
        } else {
          this.selectedIndex = -1;
          this.onLoad();
        }
      }
    }
  };

  Ext.override(Ext.form.ComboBox, doQueryExtended);
  if (!Ext.ux.form) Ext.ux.form = {};
  if (Ext.ux.form.SuperBoxSelect)
    Ext.override(Ext.ux.form.SuperBoxSelect, doQueryExtended);
} catch (eq) {
  console.log(eq);
}

try {
  //--- A ComboBox with a secondary trigger button that clears the contents of the ComboBox
  Ext.form.ClearableComboBox = Ext.extend(Ext.form.ComboBox, {
    initComponent: function() {
      this.triggerConfig = {
        tag: "span",
        cls: "x-form-twin-triggers",
        cn: [
          { tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger" },
          {
            tag: "img",
            src: Ext.BLANK_IMAGE_URL,
            cls: "x-form-trigger x-form-clear-trigger"
          }
        ]
      };
      Ext.form.ClearableComboBox.superclass.initComponent.call(this);
    },
    onTrigger2Click: function() {
      this.collapse();
      this.setValue("");
      this.clearValue();
      this.fireEvent("select");
    },

    getTrigger: Ext.form.TwinTriggerField.prototype.getTrigger,
    initTrigger: Ext.form.TwinTriggerField.prototype.initTrigger,
    onTrigger1Click: Ext.form.ComboBox.prototype.onTriggerClick,
    trigger1Class: Ext.form.ComboBox.prototype.triggerClass
  });
  //--- A ComboBox with a secondary trigger button that clears the contents of the ComboBox
  Ext.form.AddComboBox = Ext.extend(Ext.form.ComboBox, {
    initComponent: function() {
      this.triggerConfig = {
        tag: "span",
        cls: "x-form-twin-triggers",
        cn: [
          { tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger" },
          {
            tag: "img",
            src: Ext.BLANK_IMAGE_URL,
            cls: "x-form-trigger x-form-add-trigger"
          }
        ]
      };
      Ext.form.AddComboBox.superclass.initComponent.call(this);
    },
    onTrigger2Click: function() {
      alert("override trigger2Click");
    },

    getTrigger: Ext.form.TwinTriggerField.prototype.getTrigger,
    initTrigger: Ext.form.TwinTriggerField.prototype.initTrigger,
    onTrigger1Click: Ext.form.ComboBox.prototype.onTriggerClick,
    trigger1Class: Ext.form.ComboBox.prototype.triggerClass
  });
  //--- A ComboBox with a secondary trigger button that clears the contents of the ComboBox
  Ext.form.ClearableAddComboBox = Ext.extend(Ext.form.ComboBox, {
    initComponent: function() {
      this.triggerConfig = {
        tag: "span",
        cls: "x-form-twin-triggers",
        cn: [
          { tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger" },
          {
            tag: "img",
            src: Ext.BLANK_IMAGE_URL,
            cls: "x-form-trigger x-form-clear-trigger"
          },
          {
            tag: "img",
            src: Ext.BLANK_IMAGE_URL,
            cls: "x-form-trigger x-form-add-trigger"
          }
        ]
      };
      Ext.form.ClearableAddComboBox.superclass.initComponent.call(this);
    },
    onTrigger2Click: function() {
      this.collapse();
      this.setValue("");
      this.clearValue();
      this.fireEvent("select");
    },

    onTrigger3Click: function() {
      alert("override trigger3Click");
    },

    getTrigger: Ext.form.TwinTriggerField.prototype.getTrigger,
    initTrigger: Ext.form.TwinTriggerField.prototype.initTrigger,
    onTrigger1Click: Ext.form.ComboBox.prototype.onTriggerClick,
    trigger1Class: Ext.form.ComboBox.prototype.triggerClass
  });

  //--- A ComboBox with a secondary trigger button that opens the dialog
  Ext.form.DialogComboBox = Ext.extend(Ext.form.ComboBox, {
    initComponent: function() {
      this.triggerConfig = {
        tag: "span",
        cls: "x-form-twin-triggers",
        cn: [
          { tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger" },
          {
            tag: "img",
            src: Ext.BLANK_IMAGE_URL,
            cls: "x-form-trigger x-form-search-trigger"
          }
        ]
      };
      Ext.form.DialogComboBox.superclass.initComponent.call(this);
    },
    onTrigger2Click: function() {
      alert("Override it");
    },

    getTrigger: Ext.form.TwinTriggerField.prototype.getTrigger,
    initTrigger: Ext.form.TwinTriggerField.prototype.initTrigger,
    onTrigger1Click: Ext.form.ComboBox.prototype.onTriggerClick,
    trigger1Class: Ext.form.ComboBox.prototype.triggerClass
  });
} catch (eq) {
  console.log(eq);
}

try {
  if (Ext.ux.form && Ext.ux.form.LovCombo)
    Ext.ux.form.DialogLovCombo = Ext.extend(Ext.ux.form.LovCombo, {
      initComponent: function() {
        // template with checkbox
        if (!this.tpl) {
          this.tpl =
            '<tpl for=".">' +
            '<div class="x-combo-list-item">' +
            '<img src="' +
            Ext.BLANK_IMAGE_URL +
            '" ' +
            'class="ux-lovcombo-icon ux-lovcombo-icon-' +
            "{[values." +
            this.checkField +
            '?"checked":"unchecked"' +
            ']}">' +
            '<div class="ux-lovcombo-item-text">{' +
            (this.displayField || "text") +
            ":htmlEncode}</div>" +
            "</div>" +
            "</tpl>";
        }

        this.triggerConfig = {
          tag: "span",
          cls: "x-form-twin-triggers",
          cn: [
            { tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger" },
            {
              tag: "img",
              src: Ext.BLANK_IMAGE_URL,
              cls: "x-form-trigger x-form-search-trigger"
            }
          ]
        };

        // call parent
        Ext.ux.form.LovCombo.superclass.initComponent.apply(this, arguments);

        // remove selection from input field
        this.onLoad = this.onLoad.createSequence(function() {
          if (this.el) {
            var v = this.el.dom.value;
            this.el.dom.value = "";
            this.el.dom.value = v;
          }
        });
      },
      onTrigger2Click: function() {
        alert("override it");
      },

      getTrigger: Ext.form.TwinTriggerField.prototype.getTrigger,
      initTrigger: Ext.form.TwinTriggerField.prototype.initTrigger,
      onTrigger1Click: Ext.form.ComboBox.prototype.onTriggerClick,
      trigger1Class: Ext.form.ComboBox.prototype.triggerClass
    });
} catch (eq) {
  console.log(eq);
}

try {
  //--- A ComboBox with a secondary trigger button that clears the contents of the ComboBox
  if (Ext.ux.form.LovCombo)
    Ext.ux.form.AddLovCombo = Ext.extend(Ext.ux.form.LovCombo, {
      initComponent: function() {
        // template with checkbox
        if (!this.tpl) {
          this.tpl =
            '<tpl for=".">' +
            '<div class="x-combo-list-item">' +
            '<img src="' +
            Ext.BLANK_IMAGE_URL +
            '" ' +
            'class="ux-lovcombo-icon ux-lovcombo-icon-' +
            "{[values." +
            this.checkField +
            '?"checked":"unchecked"' +
            ']}">' +
            '<div class="ux-lovcombo-item-text">{' +
            (this.displayField || "text") +
            ":htmlEncode}</div>" +
            "</div>" +
            "</tpl>";
        }

        this.triggerConfig = {
          tag: "span",
          cls: "x-form-twin-triggers",
          cn: [
            { tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger" },
            {
              tag: "img",
              src: Ext.BLANK_IMAGE_URL,
              cls: "x-form-trigger x-form-add-trigger"
            }
          ]
        };

        // call parent
        Ext.ux.form.LovCombo.superclass.initComponent.apply(this, arguments);

        // remove selection from input field
        this.onLoad = this.onLoad.createSequence(function() {
          if (this.el) {
            var v = this.el.dom.value;
            this.el.dom.value = "";
            this.el.dom.value = v;
          }
        });
      },
      onTrigger2Click: function() {
        alert("override it");
      },

      getTrigger: Ext.form.TwinTriggerField.prototype.getTrigger,
      initTrigger: Ext.form.TwinTriggerField.prototype.initTrigger,
      onTrigger1Click: Ext.form.ComboBox.prototype.onTriggerClick,
      trigger1Class: Ext.form.ComboBox.prototype.triggerClass
    });
} catch (eq) {
  console.log(eq);
}

try {
  /*
 * form tabpanel render even not active
 */

  Ext.override(Ext.layout.CardLayout, {
    renderItem: function(c) {
      if (!this.deferredRender && c && c.doLayout && !c.rendered) {
        c.forceLayout = true;
      }
      Ext.layout.CardLayout.superclass.renderItem.apply(this, arguments);
    }
  });
} catch (eq) {
  console.log(eq);
}

try {
  Ext.form.LocaleMsgKey = Ext.extend(Ext.form.TwinTriggerField, {
    initComponent: function() {
      Ext.form.LocaleMsgKey.superclass.initComponent.call(this);
      if(false)this.on(
        "specialkey",
        function(f, e) {
          if (e.getKey() == e.ENTER) {
            this.onTrigger2Click();
          }
        },
        this
      );
    },

    validationEvent: false,
    validateOnBlur: false,
    trigger2Class: "x-form-lang-trigger",
    hideTrigger1: true,
    width: 180,

    onTrigger2Click: function() {
      var v = this.getRawValue();
      if (v)
        mainPanel.loadTab({
          attributes: {
            modalWindow: true,
            href: "showFormByQuery?_fid=1627&_qid=1648&xlocale_msg_key=" + v
          }
        });
      this.focus();
    }
  });
} catch (eq) {
  console.log(eq);
}

try {
  /*
 * Bu override checkbox readonly için gerekiyor!
 */

  Ext.override(Ext.form.Checkbox, {
    onClick: function(e, o) {
      if (this.readOnly === true) {
        e.preventDefault();
      } else {
        if (this.el.dom.checked != this.checked) {
          this.setValue(this.el.dom.checked);
        }
      }
    }
  });
} catch (eq) {
  console.log(eq);
}

try {
  Ext.ux.TreeCombo = Ext.extend(Ext.form.TriggerField, {
    // private
    onRender: function(ct, position) {
      this.submitValue = false;
      Ext.ux.TreeCombo.superclass.onRender.call(this, ct, position);
      if (this.hiddenName) {
        this.hiddenField = this.el.insertSibling(
          {
            tag: "input",
            type: "hidden",
            name: this.hiddenName,
            id: this.hiddenId || Ext.id()
          },
          "before",
          true
        );
        if (typeof this.value != "undefined")
          this.hiddenField.value = this.value;
      }
      this.filter = new Ext.tree.TreeFilter(this, {
        clearBlank: true,
        autoClear: true
      });

      if (Ext.isGecko) {
        this.el.dom.setAttribute("autocomplete", "off");
      }
    },
    doQuery: function(text, e) {
      if (!text) {
        this.filter.clear();
        this.treePanel.collapseAll();
        return;
      }
      if (this._controlTip != 24) this.treePanel.expandAll();

      var tqt = text.toLowerCase();
      var fxx = function(n) {
        if (n.attributes.leaf) {
          if (n.text.toLowerCase().indexOf(tqt) > -1) {
            return true;
          } else if (
            n.parentNode &&
            n.parentNode.text &&
            n.parentNode.text.toLowerCase().indexOf(tqt) > -1
          ) {
            return true;
          }
        } else {
          if (n.text.toLowerCase().indexOf(tqt) > -1) {
            return true;
          } else if (n.childNodes.length > 0)
            for (var qi = 0; qi < n.childNodes.length; qi++) {
              if (fxx(n.childNodes[qi])) return true;
            }
        }
        return false;
      };
      this.filter.filterBy(fxx, this, this.treePanel.root);
    },

    initComponent: function() {
      this.triggerConfig = {
        tag: "span",
        cls: "x-form-twin-triggers",
        cn: [{ tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger" }]
      };
      if (this.allowBlank) {
        this.triggerConfig.cn.push({
          tag: "img",
          src: Ext.BLANK_IMAGE_URL,
          cls: "x-form-trigger x-form-clear-trigger"
        });
      }
      Ext.ux.TreeCombo.superclass.initComponent.call(this);
      this.getTree();
    },
    // private
    initEvents: function() {
      Ext.ux.TreeCombo.superclass.initEvents.call(this);

      this.dqTask = new Ext.util.DelayedTask(this.initQuery, this);
      if (!this.enableKeyEvents) {
        this.mon(this.el, "keyup", this.onKeyUp, this);
      }
    },
    initQuery: function() {
      this.doQuery(this.getRawValue());
    },
    isExpanded: function() {
      return this.treePanel && this.treePanel.isVisible();
    },
    // private
    onKeyUp: function(e) {
      var k = e.getKey();
      if (
        this.editable !== false &&
        this.readOnly !== true &&
        (k == e.BACKSPACE || !e.isSpecialKey())
      ) {
        this.lastKey = k;
        if (!this.isExpanded()) this.onTrigger1Click();
        this.dqTask.delay(this.queryDelay);
      }
      Ext.ux.TreeCombo.superclass.onKeyUp.call(this, e);
    },
    onTrigger1Click: function() {
      if (!this.isExpanded()) {
        this.getTree().show();
        this.getTree()
          .getEl()
          .alignTo(this.wrap, "tl-bl?");
      } else {
        this.getTree().hide();
      }
    },
    onTrigger2Click: function() {
      this.collapse();
      this.setValue("");
      this.fireEvent("select");
    },
    getTree: function() {
      if (!this.treePanel) {
        if (!this.treeWidth) {
          this.treeWidth = Math.max(200, this.width || 200);
        }
        if (!this.treeHeight) {
          this.treeHeight = 200;
        }
        this.treePanel = new Ext.tree.TreePanel({
          renderTo: Ext.getBody(),
          loader: new Ext.tree.TreeLoader({
            preloadChildren: false,
            url: this.url
          }),
          root: new Ext.tree.AsyncTreeNode({ children: this.children }),
          rootVisible: false,
          floating: true,
          autoScroll: true,
          minWidth: 200,
          minHeight: 200,
          width: this.treeWidth,
          height: this.treeHeight,
          listeners: {
            hide: this.onTreeHide,
            show: this.onTreeShow,
            click: this.onTreeNodeClick,
            expandnode: this.onExpandOrCollapseNode,
            collapsenode: this.onExpandOrCollapseNode,
            resize: this.onTreeResize,
            scope: this
          }
        });
        this.treePanel.show();
        this.treePanel.hide();
        this.relayEvents(this.treePanel.loader, [
          "beforeload",
          "load",
          "loadexception"
        ]);
        if (this.resizable) {
          this.resizer = new Ext.Resizable(this.treePanel.getEl(), {
            pinned: true,
            handles: "se"
          });
          this.mon(
            this.resizer,
            "resize",
            function(r, w, h) {
              this.treePanel.setSize(w, h);
            },
            this
          );
        }
      }
      return this.treePanel;
    },

    onExpandOrCollapseNode: function() {
      if (!this.maxHeight || this.resizable) return; // -----------------------------> RETURN
      var treeEl = this.treePanel.getTreeEl();
      var heightPadding = treeEl.getHeight() - treeEl.dom.clientHeight;
      var ulEl = treeEl.child("ul"); // Get the underlying tree element
      var heightRequired = ulEl.getHeight() + heightPadding;
      if (heightRequired > this.maxHeight) heightRequired = this.maxHeight;
      this.treePanel.setHeight(heightRequired);
    },

    onTreeResize: function() {
      if (this.treePanel) this.treePanel.getEl().alignTo(this.wrap, "tl-bl?");
    },

    onTreeShow: function() {
      Ext.getDoc().on("mousewheel", this.collapseIf, this);
      Ext.getDoc().on("mousedown", this.collapseIf, this);
    },

    onTreeHide: function() {
      Ext.getDoc().un("mousewheel", this.collapseIf, this);
      Ext.getDoc().un("mousedown", this.collapseIf, this);
    },

    collapseIf: function(e) {
      if (!e.within(this.wrap) && !e.within(this.getTree().getEl())) {
        this.collapse();
      }
    },

    collapse: function() {
      this.getTree().hide();
      if (this.resizer) this.resizer.resizeTo(this.treeWidth, this.treeHeight);
    },

    // private
    validateBlur: function() {
      return !this.treePanel || !this.treePanel.isVisible();
    },

    setValue: function(v) {
      this.startValue = this.value = v;
      if (this.hiddenField) {
        this.hiddenField.value = v;
      }
      if (this.treePanel && v) {
        var n = this.treePanel.getNodeById(v);
        if (!n) {
          var resx = [];
          function findSubNode(childx, vx) {
            if (!childx || !childx.length) return null;
            for (var qu = 0; qu < childx.length; qu++) {
              if (childx[qu].id == vx) {
                resx.push(childx[qu]);
                return true;
              } else if (childx[qu].children) {
                if (findSubNode(childx[qu].children, vx)) {
                  resx.push(childx[qu]);
                  return true;
                }
              }
            }
          }
          for (var qi in this.treePanel.nodeHash) {
            if (this.treePanel.nodeHash[qi].attributes.children) {
              if (
                findSubNode(this.treePanel.nodeHash[qi].attributes.children, v)
              ) {
                break;
              }
            }
          }
          if (resx.length) {
            var txt = "";
            for (var qi = 0; qi < resx.length; qi++)
              txt = "\\" + resx[qi].text + txt;
            this.setRawValue(txt.substring(1));
          }
        } else {
          var txt = n.text;
          if (!this.hideParentNodes) {
            while (n && n.getRootNode) {
              n = n.getRootNode();
              if (n.text) txt = n.text + "\\" + txt;
              else break;
            }
          }
          this.setRawValue(txt);
        }
      } else this.setRawValue("");
    },

    getValue: function() {
      return this.value;
    },

    onTreeNodeClick: function(node, e) {
      if (this.selectOnlyLeafNode && !node.isLeaf()) {
        if (node.expanded) node.collapse();
        else node.expand();
        return;
      }

      var txt = node.text;
      if (!this.hideParentNodes) {
        var n = node;
        while ((n = n.parentNode))
          if (n.text) txt = n.text + "\\" + txt;
          else break;
      }
      this.setRawValue(txt);
      this.value = node.id;
      if (this.hiddenField) {
        this.hiddenField.value = node.id;
      }
      //        alert("onTreeNodeClick="+node.id+";"+this.hiddenField);
      this.fireEvent("select", this, node);
      this.collapse();
    },
    getTrigger: Ext.form.TwinTriggerField.prototype.getTrigger,
    initTrigger: Ext.form.TwinTriggerField.prototype.initTrigger,
    trigger1Class: Ext.form.ComboBox.prototype.triggerClass
  });
  Ext.reg("treecombo", Ext.ux.TreeCombo);
} catch (eq) {
  console.log(eq);
}

try {
  Ext.override(Ext.form.HtmlEditor, {
    onRender: function(ct, position) {
      Ext.form.HtmlEditor.superclass.onRender.call(this, ct, position);
      this.el.dom.style.border = "0 none";
      this.el.dom.setAttribute("tabIndex", -1);
      this.el.addClass("x-hidden");
      if (Ext.isIE) {
        // fix IE 1px bogus margin
        this.el.applyStyles("margin-top:-1px;margin-bottom:-1px;");
      }
      this.wrap = this.el.wrap({
        cls: "x-html-editor-wrap",
        cn: { cls: "x-html-editor-tb" }
      });

      this.createToolbar(this);

      this.disableItems(true);

      this.tb.doLayout();

      this.createIFrame();

      if (!this.width) {
        var sz = this.el.getSize();
        this.setSize(sz.width, this.height || sz.height);
      }
      this.resizeEl = this.positionEl = this.wrap;
    },
    toggleSourceEdit: function(sourceEditMode) {
      var iframeHeight, elHeight;

      if (sourceEditMode === undefined) {
        sourceEditMode = !this.sourceEditMode;
      }
      this.sourceEditMode = sourceEditMode === true;
      var btn = this.tb.getComponent("sourceedit");

      if (btn.pressed !== this.sourceEditMode) {
        btn.toggle(this.sourceEditMode);
        if (!btn.xtbHidden) {
          return;
        }
      }

      if (this.sourceEditMode) {
        // grab the height of the containing panel before we hide the iframe
        this.previousSize = this.getSize();

        iframeHeight = Ext.get(this.iframe).getHeight();

        this.disableItems(true);
        this.syncValue();
        this.iframe.className = "x-hidden";
        this.el.removeClass("x-hidden");
        this.el.dom.removeAttribute("tabIndex");
        this.el.focus();
        this.el.dom.style.height = iframeHeight + "px";
      } else {
        elHeight = parseInt(this.el.dom.style.height, 10);
        if (this.initialized) {
          this.disableItems(this.readOnly);
        }
        this.pushValue();
        this.iframe.className = "";
        this.el.addClass("x-hidden");
        this.el.dom.setAttribute("tabIndex", -1);
        this.deferFocus();

        this.setSize(this.previousSize);
        delete this.previousSize;
        this.iframe.style.height = elHeight + "px";
      }
      this.fireEvent("editmodechange", this, this.sourceEditMode);
    }
  });
} catch (eq) {
  console.log(eq);
}


try {
  Ext.ux.BubblePanel = Ext.extend(Ext.Panel, {
    baseCls: "x-bubble",
    frame: true
  });
} catch (eq) {
  console.log(eq);
}

try {
  Ext.override(Ext.form.BasicForm, {
    findInvalid: function() {
      var result = "",
        it = this.items.items,
        l = it.length,
        i,
        f;
      for (i = 0; i < l; i++) {
        if (!(f = it[i]).disabled && !f.isValid()) {
          f.ensureVisible();
          result += '<li>' + f.fieldLabel;
        }
      }
      if (l > 0)
/*        Ext.Msg.show({
          title: getLocMsg("js_alan_hatali_girilmistir"),
          msg: result,
          icon: Ext.MessageBox.ERROR
        });*/
    	  Ext.infoMsg.msg('error','<b>Validation Errors</b><br>'+result,4)
    }
  });
} catch (eq) {
  console.log(eq);
}

try {
  Ext.override(Ext.Component, {
    ensureVisible: function(stopAt) {
      var p;
      this.ownerCt.bubble(function(c) {
        if ((p = c.ownerCt)) {
          if (p instanceof Ext.TabPanel) {
            // p.setActiveTab(c);
            p.setTabColor(c, "red");
          } else if (p.layout.setActiveItem) {
            // p.layout.setActiveItem(c);
          }
        }
        //return (c !== stopAt);
      });
      //this.el.scrollIntoView(this.el.up(':scrollable'));
      return this;
    }
  });
} catch (eq) {
  console.log(eq);
}
/*Ext.DomQuery.pseudos.scrollable = function(c, t) {
    var r = [], ri = -1;
    for(var i = 0, ci; ci = c[i]; i++){
        var o = ci.style.overflow;
        if(o=='auto'||o=='scroll') {
            if (ci.scrollHeight < Ext.fly(ci).getHeight(true)) r[++ri] = ci;
        }
    }
    return r;
};*/
try {
  Ext.override(Ext.TabPanel, {
    setTabColor: function(tab, newColor) {
      var tabEl = Ext.get(this.getTabEl(tab));
      tabEl.removeClass("x-tab-" + tab.tabColor);
      if (newColor) tabEl.addClass("x-tab-" + newColor);
      tab.tabColor = newColor;
    }
  });
} catch (eq) {
  console.log(eq);
}

try {
  if (Ext.ux.form.SuperBoxSelect)
    Ext.override(Ext.ux.form.SuperBoxSelect, {
      assertValue: null,
      beforeBlur: Ext.emptyFn
    });
} catch (eq) {
  console.log(eq);
}

Ext.namespace("Ext.ux.panel");
try {
  /**
   * @class Ext.ux.panel.DDTabPanel
   * @extends Ext.TabPanel
   * @author
   *     Original by
   *         <a href="http://extjs.com/forum/member.php?u=22731">thommy</a> and
   *         <a href="http://extjs.com/forum/member.php?u=37284">rizjoj</a><br />
   *     Published and polished by: Mattias Buelens (<a href="http://extjs.com/forum/member.php?u=41421">Matti</a>)<br />
   *     With help from: <a href="http://extjs.com/forum/member.php?u=1459">mystix</a>
   *     Polished and debugged by: Tobias Uhlig (info@internetsachen.com) 04-25-2009
   *     Ported to Ext-3.1.1 by: Tobias Uhlig (info@internetsachen.com) 02-14-2010
   *     Updated by <a href="http://www.sencha.com/forum/member.php?56442-brombs">brombs</a>
   *     to include reorder event
   *     Modified by <a href="http://www.onenaught.com">Anup Shah</a> to work as a plugin
   *     instead of subclass of TabPanel
   * @license Licensed under the terms of the Open Source <a href="http://www.gnu.org/licenses/lgpl.html">LGPL 3.0 license</a>.
   * Commercial use is permitted to the extent that the code/component(s) do NOT
   * become part of another Open Source or Commercially licensed development library
   * or toolkit without explicit permission.
   * @version 2.0.1 (Jan 11, 2013)
   */
  Ext.ux.panel.DraggableTabs = Ext.extend(Object, {
    constructor: function(config) {
      if (config) {
        Ext.apply(this, config);
      }
    },

    init: function(tp) {
      if (tp instanceof Ext.TabPanel === false) return;

      // make these available onto the TabPanel as per original plugin, where used externally
      tp.arrowOffsetX = this.arrowOffsetX;
      tp.arrowOffsetY = this.arrowOffsetY;

      tp.addEvents("reorder");

      // TODO: check if ddGroupId can be left as a property of this plugin rather than on the TabPanel
      if (!tp.ddGroupId) {
        //            tp.ddGroupId = 'dd-tabpanel-group-' + tp.getId();
        tp.ddGroupId = "dd-main-tabpanel-group";
      }

      // New Event fired after drop tab. Is there a cleaner way to do this?
      tp.reorder = this.reorder;
      tp.oldinitTab = tp.initTab;
      tp.initTab = this.initTab;
      tp.onRemove = this.onRemove;

      tp.on("afterrender", this.afterRender, this);

      this.tabPanel = tp;
    },

    destroy: function() {
      tp.un("afterrender", this.afterRender, this);
      delete this.tabPanel;
      Ext.destroy(this.dd, this.arrow);
    },

    /**
     * @cfg {Number} arrowOffsetX The horizontal offset for the drop arrow indicator, in pixels (defaults to -9).
     */
    arrowOffsetX: -9,
    /**
     * @cfg {Number} arrowOffsetY The vertical offset for the drop arrow indicator, in pixels (defaults to -8).
     */
    arrowOffsetY: -8,

    reorder: function(tab) {
      this.fireEvent("reorder", this, tab);
    },

    // Declare the tab panel as a drop target
    /** @private */
    afterRender: function() {
      // Create a drop arrow indicator
      this.tabPanel.arrow = Ext.DomHelper.append(
        Ext.getBody(),
        '<div class="dd-arrow-down"></div>',
        true
      );
      this.tabPanel.arrow.hide();
      // Create a drop target for this tab panel
      var tabsDDGroup = this.tabPanel.ddGroupId;
      this.dd = new Ext.ux.panel.DraggableTabs.DropTarget(this, {
        ddGroup: tabsDDGroup
      });

      // needed for the onRemove-Listener
      this.move = false;
    },

    // Init the drag source after (!) rendering the tab
    /** @private */
    initTab: function(tab, index) {
      this.oldinitTab(tab, index);

      var id = this.id + "__" + tab.id;
      // Hotfix 3.2.0
      Ext.fly(id).on("click", function() {
        tab.ownerCt.setActiveTab(tab.id);
      });
      // Enable dragging on all tabs by default
      Ext.applyIf(tab, { allowDrag: true });

      // Extend the tab
      Ext.apply(tab, {
        // Make this tab a drag source
        ds: new Ext.dd.DragSource(id, {
          ddGroup: this.ddGroupId,
          dropEl: tab,
          dropElHeader: Ext.get(id, true),
          scroll: false,

          // Update the drag proxy ghost element
          onStartDrag: function() {
            if (this.dropEl.iconCls) {
              var el = this.getProxy()
                .getGhost()
                .select(".x-tab-strip-text");
              el.addClass("x-panel-inline-icon");

              var proxyText = el.elements[0].innerHTML;
              proxyText = Ext.util.Format.stripTags(proxyText);
              el.elements[0].innerHTML = proxyText;

              el.applyStyles({
                paddingLeft: "20px"
              });
            }
          },

          // Activate this tab on mouse up
          // (Fixes bug which prevents a tab from being activated by clicking it)
          onMouseUp: function(event) {
            if (this.dropEl.ownerCt.move) {
              if (
                !this.dropEl.disabled &&
                this.dropEl.ownerCt.activeTab == null
              ) {
                this.dropEl.ownerCt.setActiveTab(this.dropEl);
              }
              this.dropEl.ownerCt.move = false;
              return;
            }
            if (!this.dropEl.isVisible() && !this.dropEl.disabled) {
              this.dropEl.show();
            }
          }
        }),
        // Method to enable dragging
        enableTabDrag: function() {
          this.allowDrag = true;
          return this.ds.unlock();
        },
        // Method to disable dragging
        disableTabDrag: function() {
          this.allowDrag = false;
          return this.ds.lock();
        }
      });

      // Initial dragging state
      if (tab.allowDrag) {
        tab.enableTabDrag();
      } else {
        tab.disableTabDrag();
      }
    },

    /** @private */
    onRemove: function(c) {
      var te = Ext.get(c.tabEl);
      // check if the tabEl exists, it won't if the tab isn't rendered
      if (te) {
        // DragSource cleanup on removed tabs
        //Ext.destroy(c.ds.proxy, c.ds);
        te.select("a").removeAllListeners();
        Ext.destroy(te);
      }

      // ignore the remove-function of the TabPanel
      Ext.TabPanel.superclass.onRemove.call(this, c);

      this.stack.remove(c);
      delete c.tabEl;
      c.un("disable", this.onItemDisabled, this);
      c.un("enable", this.onItemEnabled, this);
      c.un("titlechange", this.onItemTitleChanged, this);
      c.un("iconchange", this.onItemIconChanged, this);
      c.un("beforeshow", this.onBeforeShowItem, this);

      // if this.move, the active tab stays the active one
      if (c == this.activeTab) {
        if (!this.move) {
          var next = this.stack.next();
          if (next) {
            this.setActiveTab(next);
          } else if (this.items.getCount() > 0) {
            this.setActiveTab(0);
          } else {
            this.activeTab = null;
          }
        } else {
          this.activeTab = null;
        }
      }
      if (!this.destroying) {
        this.delegateUpdates();
      }
    }
  });
} catch (eq) {
  console.log(eq);
}

try {
  Ext.preg("draggabletabs", Ext.ux.panel.DraggableTabs);

  // Ext.ux.panel.DraggableTabs.DropTarget
  // Implements the drop behavior of the tab panel
  /** @private */
  Ext.ux.panel.DraggableTabs.DropTarget = Ext.extend(Ext.dd.DropTarget, {
    constructor: function(dd, config) {
      this.tabpanel = dd.tabPanel;
      // The drop target is the tab strip wrap
      Ext.ux.panel.DraggableTabs.DropTarget.superclass.constructor.call(
        this,
        this.tabpanel.stripWrap,
        config
      );
    },

    notifyOver: function(dd, e, data) {
      var tabs = this.tabpanel.items;
      var last = tabs.length;
      if (!e.within(this.getEl()) || dd.dropEl == this.tabpanel) {
        return "x-dd-drop-nodrop";
      }

      var larrow = this.tabpanel.arrow;

      // Getting the absolute Y coordinate of the tabpanel
      var tabPanelTop = this.el.getY();

      var left, prevTab, tab;
      var eventPosX = e.getPageX();

      for (var i = 0; i < last; i++) {
        prevTab = tab;
        tab = tabs.itemAt(i);
        // Is this tab target of the drop operation?
        var tabEl = tab.ds.dropElHeader;
        // Getting the absolute X coordinate of the tab
        var tabLeft = tabEl.getX();
        // Get the middle of the tab
        var tabMiddle = tabLeft + tabEl.dom.clientWidth / 2;

        if (eventPosX <= tabMiddle) {
          left = tabLeft;
          break;
        }
      }

      if (typeof left == "undefined") {
        var lastTab = tabs.itemAt(last - 1);
        if (lastTab == dd.dropEl) return "x-dd-drop-nodrop";
        var dom = lastTab.ds.dropElHeader.dom;
        left = new Ext.Element(dom).getX() + dom.clientWidth + 3;
      } else if (tab == dd.dropEl || prevTab == dd.dropEl) {
        this.tabpanel.arrow.hide();
        return "x-dd-drop-nodrop";
      }

      larrow
        .setTop(tabPanelTop + this.tabpanel.arrowOffsetY)
        .setLeft(left + this.tabpanel.arrowOffsetX)
        .show();

      return "x-dd-drop-ok";
    },

    notifyDrop: function(dd, e, data) {
      this.tabpanel.arrow.hide();

      // no parent into child
      if (dd.dropEl == this.tabpanel) {
        return false;
      }
      var tabs = this.tabpanel.items;
      var eventPosX = e.getPageX();

      for (var i = 0; i < tabs.length; i++) {
        var tab = tabs.itemAt(i);
        // Is this tab target of the drop operation?
        var tabEl = tab.ds.dropElHeader;
        // Getting the absolute X coordinate of the tab
        var tabLeft = tabEl.getX();
        // Get the middle of the tab
        var tabMiddle = tabLeft + tabEl.dom.clientWidth / 2;
        if (eventPosX <= tabMiddle) break;
      }

      // do not insert at the same location
      if (tab == dd.dropEl || tabs.itemAt(i - 1) == dd.dropEl) {
        return false;
      }

      dd.proxy.hide();

      // if tab stays in the same tabPanel
      if (dd.dropEl.ownerCt == this.tabpanel) {
        if (i > tabs.indexOf(dd.dropEl)) i--;
      }

      this.tabpanel.move = true;
      var dropEl = dd.dropEl.ownerCt.remove(dd.dropEl, false);

      this.tabpanel.insert(i, dropEl);
      // Event drop
      this.tabpanel.fireEvent("drop", this.tabpanel);
      // Fire event reorder
      this.tabpanel.reorder(tabs.itemAt(i));

      return true;
    },

    notifyOut: function(dd, e, data) {
      this.tabpanel.arrow.hide();
    }
  });
} catch (eq) {
  console.log(eq);
}

Ext.infoMsg = {
  msgTypes: { info: "bottom", success: "bottom", warning: "top", error: "top" },
  alert: function(type, msg) {
    if (!msg) {
      msg = type || "msg";
      type = "info";
    }
    var c = {
      text: msg,
      theme: "metroui",
      type: type,
      modal: !0,
      layout: "center",
      animation: { open: null, close: null },
      buttons: [
        Noty.button(
          "OK",
          "noty-btn btn-success",
          function() {
            nw.close();
          },
          {}
        )
      ]
    };
    var nw = new Noty(c);
    nw.show();
  },
  confirm: function(msg, callback, type) {
    if (!callback) {
      alert("confirm.define callback for: " + msg);
      return;
    }
    var c = {
      text: msg,
      theme: "metroui",
      type: type || "info",
      modal: !0,
      layout: "center",
      animation: { open: null, close: null },
      closeWith: [],
      buttons: [
        Noty.button(
          "OK",
          "noty-btn btn-success",
          function() {
            nw.close();
            callback();
          },
          {}
        ),
        Noty.button(
          "Cancel",
          "noty-btn",
          function() {
            nw.close();
          },
          {}
        )
      ]
    };
    var nw = new Noty(c);
    nw.show();
  },
  prompt: function(msg, defaultValue, callback, type) {
    if (!callback) {
      alert("prompt.define callback for: " + msg);
      return;
    }
    defaultValue = defaultValue || "";
    var idx = "idx-" + new Date().getTime();
    var c = {
      text:
        msg +
        '<br/><br/><input autofocus value="' +
        defaultValue +
        '" id="' +
        idx +
        '">',
      theme: "metroui",
      type: type || "info",
      modal: !0,
      layout: "center",
      animation: { open: null, close: null },
      closeWith: [],
      buttons: [
        Noty.button(
          "OK",
          "noty-btn btn-success",
          function() {
            nw.close();
            var val = document.getElementById(idx);
            if (val.value) callback(val.value);
          },
          {}
        ),
        Noty.button(
          "Cancel",
          "noty-btn",
          function() {
            nw.close();
          },
          {}
        )
      ]
    };
    var nw = new Noty(c);
    nw.show();
  },
  msg: function(type, msg, timeout) {
    var pos = Ext.infoMsg.msgTypes[type];
    if (!pos) {
      msg = "<b>" + type + "</b><br/>" + msg;
      type = "info";
      pos = "bottom";
    }
    new Noty({
      text: msg,
      theme: "metroui",
      type: type,
      layout: pos + "Right",
      timeout: 1000 * (timeout || 3),
      animation: {
        open: "animated bounceInRight", // Animate.css class names
        close: "animated bounceOutRight" // Animate.css class names
      }
    }).show();
  },
  log: function(type, msg) {
    var pos = Ext.infoMsg.msgTypes[type];
    if (!pos) {
      msg = '<b style="color: #ff5025;">' + type + "</b><br/>" + msg;
      type = false;
    } else msg = "<b>" + type.toUpperCase() + "</b><br/>" + msg;
    msg = {
      text: msg,
      theme: "metroui",
      layout: "bottomCenter",
      animation: {
        open: "animated bounceInUp", // Animate.css class names
        close: "animated bounceOutDown" // Animate.css class names
      }
    };
    if (type) msg.type = type;
    new Noty(msg).show();
  },
  wow: function(type, msg, timeout) {
    var pos = Ext.infoMsg.msgTypes[type];
    if (!pos) {
      type = "info";
    }
    new Noty({
      text: msg,
      theme: "metroui",
      type: type,
      layout: "topCenter",
      timeout: 1000 * (timeout || 10),
      animation: {
        open: "animated fadeInDown", // Animate.css class names
        close: "animated fadeOut" // Animate.css class names
      }
    }).show();
  }
};
try {
  window.alert = Ext.infoMsg.alert;
  alert = Ext.infoMsg.alert;
} catch (ee) {}

/* IBAN Validation */

Ext.form.VTypes[
  "ibanVal"
] = /[a-zA-Z]{2}[0-9]{2}[-\s][0-9]{4}[-\s][0-9]{4}[-\s][0-9]{4}[-\s][0-9]{4}[-\s][0-9]{4}$|[a-zA-Z]{2}[0-9]{22}$/;
Ext.form.VTypes["ibanMask"] = /[-\sA-Za-z0-9]/;
Ext.form.VTypes["ibanText"] = getLocMsg("js_invalid_iban");
Ext.form.VTypes["iban"] = function(v) {
  return Ext.form.VTypes["ibanVal"].test(v);
};

/* T.C. Kimlik No */

Ext.form.VTypes["tckimliknoVal"] = /^[1-9]{1}[0-9]{10}$/;
Ext.form.VTypes["tckimliknoMask"] = /[-\s0-9]/;
Ext.form.VTypes["tckimliknoText"] = getLocMsg("js_invalid_tckimlikno");
Ext.form.VTypes["tckimlikno"] = function(v) {
  return Ext.form.VTypes["tckimliknoVal"].test(v);
};

/* Vergi Kimlik No */

Ext.form.VTypes["vkimliknoVal"] = /^[1-9]{1}[0-9]{9}$/;
Ext.form.VTypes["vkimliknoMask"] = /[-\s0-9]/;
Ext.form.VTypes["vkimliknoText"] = getLocMsg("js_invalid_vkimlikno");
Ext.form.VTypes["vkimlikno"] = function(v) {
  return Ext.form.VTypes["vkimliknoVal"].test(v);
};

/* Bizim eklediğimiz validation typelar */

Ext.apply(Ext.form.VTypes, {
  daterange: function(val, field) {
    // Daha Küçük bir tarihin ileriki tarih olarak girilememesi
    var date = field.parseDate(val);
    if (!date) {
      return false;
    }
    if (field.startDateField) {
      var start = Ext.getCmp(field.startDateField);
      if (!start.maxValue || date.getTime() != start.maxValue.getTime()) {
        start.setMaxValue(date);
        start.validate();
      }
    } else if (field.endDateField) {
      var end = Ext.getCmp(field.endDateField);
      if (!end.minValue || date.getTime() != end.minValue.getTime()) {
        end.setMinValue(date);
        end.validate();
      }
    }
    return true;
  }
});

/* formats */
try {
  Ext.util.Format.fmtShortDate = fmtShortDate;
  Ext.util.Format.fmtDateTime = fmtDateTime;
  Ext.util.Format.fmtDateTimeWithDay = fmtDateTimeWithDay;
  Ext.util.Format.fmtDateTimeWithDay2 = fmtDateTimeWithDay2;

  Ext.util.Format.fmtParaShow = fmtParaShow;
  Ext.util.Format.disabledCheckBoxHtml = disabledCheckBoxHtml;

  Ext.util.Format.fmtTest = function(j) {
    console.log("fmtTest");
    console.log(j);
  };
  Ext.util.Format.fmtTypingBlock = fmtTypingBlock;
  Ext.util.Format.fmtLoadMore = fmtLoadMore;
  Ext.util.Format.fmtChatList = fmtChatList;
  Ext.util.Format.fmtOnlineUser = fmtOnlineUser;

  Ext.util.Format.fmtVcs = function(x){
	  var v='-';
	  if(x && x.pkpkpk_vcsf){
		x = x.pkpkpk_vcsf.split(",");
		if (1 * x[0] != 9)
		  v = (
		    '<img alt="' +
		    x[1] +
		    '" src="/ext3.4.1/custom/images/vcs' +
		    x[0] +
		    '.png" border=0>'
		  );
		else return '';
	  }
	  return '<span style="zoom:.9;font-size:12px;color:#888">'+v+'</span>';
  }
  Ext.util.Format.fmtAgo = function(dt2){
	if(!dt2)return '';
	var tnow = iwb.getDate().getTime();
	  try{
		var t = dt2.getTime();
		if(t+30*1000>tnow)return 'Now';//5 sn
		if(t+2*60*1000>tnow)return '1m';//1 dka
		if(t+60*60*1000>tnow)return Math.round((tnow-t)/(60*1000)) + 'm';
		if(t+24*60*60*1000>tnow)return Math.round((tnow-t)/(60*60*1000)) + 'h';
		if(t+2*24*60*60*1000>tnow)return 'Yesterday';
		if(t+7*24*60*60*1000>tnow)return daysOfTheWeek[_scd.locale][dt2.getDay()];//5dka
		if(t+364*24*60*60*1000>tnow)return dt2.dateFormat('d/m');//5dka
		return dt2.dateFormat('m/Y');
	  }catch(e){return dt2};
  }
  
  Ext.util.Format.getPic2 = function(rrr) {
    return getPictureUrl(rrr.profile_picture_id, true);
  };
  Ext.util.Format.getPic3Mini = function(rrr) {
    return !rrr || !rrr.profile_picture_id
      ? ""
      : '<img style="border-radius:32px;vertical-align:middle;" src="' +
          getPictureUrl(rrr.profile_picture_id, true) +
          '" width=' +
          _app.profile_picture_width_mini +
          " height=" +
          _app.profile_picture_height_mini +
          ">";
  };

  Ext.util.Format.getRecordTree = function(rrr) {
    var s = "";
    if (rrr._record && rrr._record.length) {
      var ss = "";
      var rs = rrr._record;
      for (var qi = rs.length - 1; qi >= 0; qi--) {
        var r = rs[qi];
        if (qi != rs.length - 1) ss += "<br>";
        for (var zi = rs.length - 1; zi > qi; zi--) ss += " &nbsp; &nbsp;";
        ss += "&gt " + (qi != 0 ? r.tdsc : "<b>" + r.tdsc + "</b>");
        var rdsc = r.dsc;
        if (!rdsc) rdsc = "(...)";
        else if (rdsc.length > 200) rdsc = rdsc.substring(0, 197) + "...";
        ss +=
          qi != 0
            ? ': <a href=# onclick="return fnTblRecEdit(' +
              r.tid +
              "," +
              r.tpk +
              ');">' +
              rdsc +
              "</a>"
            : ': <b><a href=# onclick="return fnTblRecEdit(' +
              r.tid +
              "," +
              r.tpk +
              ');">' +
              rdsc +
              "</a></b>"; // else ss+=': (...)';
      }
      if (ss) s += '<div class="dfeed">' + ss + "</div>";
    }
    if (rrr.insert_dttm)
      s +=
        '<span class="cfeed"> · ' +
        fmtDateTimeWithDay2(rrr.insert_dttm) +
        "</span> · " +
        ('<a href=# onclick="return fnTblRecComment(' +
          rrr.tid +
          "," +
          rrr.tpk +
          ');">' +
          (!rrr.tcc
            ? "Coment"
            : (rrr.comments && rrr.comments.length < rrr.tcc ? "Bütün " : "") +
              "Comments (" +
              rrr.tcc +
              ")") +
          "</a>");
    return s;
  };
} catch (e) {
  if (
    1 * _app.debug != 0 &&
    confirm("ERROR Ext.util.Format definitions!!! throw?: " + e.message)
  )
    throw e;
}

try {
  Ext.override(Ext.form.Field, {
    reset: function() {
      this.setValue(this.originalValue);
      this.clearInvalid();
    }
  });
} catch (eq) {}

Ext.dd.DragDropMgr.getZIndex = function(element) {
  var body = document.body,
    z,
    zIndex = -1;
  var overTargetEl = element;

  element = Ext.getDom(element);
  while (element !== body) {
    // this fixes the problem
    if (!element) {
      this._remove(overTargetEl); // remove the drop target from the manager
      break;
    }
    // fix end

    if (!isNaN((z = Number(Ext.fly(element).getStyle("zIndex"))))) {
      zIndex = z;
    }
    element = element.parentNode;
  }
  return zIndex;
};


Ext.layout.ColumnLayout = Ext.extend(Ext.layout.ContainerLayout, {
    // private
    monitorResize:true,

    type: 'column',

    extraCls: 'x-column',

    scrollOffset : 0,

    // private

    targetCls: 'x-column-layout-ct',

    isValidParent : function(c, target){
        return this.innerCt && c.getPositionEl().dom.parentNode == this.innerCt.dom;
    },

    getLayoutTargetSize : function() {
        var target = this.container.getLayoutTarget(), ret;
        if (target) {
            ret = target.getViewSize();

            // IE in strict mode will return a width of 0 on the 1st pass of getViewSize.
            // Use getStyleSize to verify the 0 width, the adjustment pass will then work properly
            // with getViewSize
            if (Ext.isIE && Ext.isStrict && ret.width == 0){
                ret =  target.getStyleSize();
            }

            ret.width -= target.getPadding('lr');
            ret.height -= target.getPadding('tb');
        }
        return ret;
    },

    renderAll : function(ct, target) {
        if(!this.innerCt){
            // the innerCt prevents wrapping and shuffling while
            // the container is resizing
            this.innerCt = target.createChild({cls:'x-column-inner'});
            this.innerCt.createChild({cls:'x-clear'});
        }
        Ext.layout.ColumnLayout.superclass.renderAll.call(this, ct, this.innerCt);
    },

    // private
    onLayout : function(ct, target){
        var cs = ct.items.items,
            len = cs.length,
            c,
            i,
            m,
            margins = [];
//console.log(ct,target);
        this.renderAll(ct, target);

        var size = this.getLayoutTargetSize();

        if(size.width < 1 && size.height < 1){ // display none?
            return;
        }

        var w = size.width - this.scrollOffset,
            h = size.height,
            pw = w;
//if(w<450)w=450;
        this.innerCt.setWidth(w);

        // some columns can be percentages while others are fixed
        // so we need to make 2 passes

        for(i = 0; i < len; i++){
            c = cs[i];
            m = c.getPositionEl().getMargins('lr');
            margins[i] = m;
            if(!c.columnWidth){
                pw -= (c.getWidth() + m);
            }
        }

        pw = pw < 0 ? 0 : pw;

        for(i = 0; i < len; i++){
            c = cs[i];
            m = margins[i];
            if(c.columnWidth){
            	var size = Math.floor(c.columnWidth * pw) - m;
            	if(c.minW && size<c.minW)size=c.minW;
                c.setSize(size);
            }
        }

        // Browsers differ as to when they account for scrollbars.  We need to re-measure to see if the scrollbar
        // spaces were accounted for properly.  If not, re-layout.
        if (Ext.isIE) {
            if (i = target.getStyle('overflow') && i != 'hidden' && !this.adjustmentPass) {
                var ts = this.getLayoutTargetSize();
                if (ts.width != size.width){
                    this.adjustmentPass = true;
                    this.onLayout(ct, target);
                }
            }
        }
        delete this.adjustmentPass;
    }

    /**
     * @property activeItem
     * @hide
     */
});

Ext.Container.LAYOUTS['column'] = Ext.layout.ColumnLayout;
/**
 * Copyright(c) 2011
 *
 * Licensed under the terms of the Open Source LGPL 3.0
 * http://www.gnu.org/licenses/lgpl.html
 * @author Greivin Britton, brittongr@gmail.com
 *     
 * @changes
 * No currency symbol by default    
 * No decimalPrecision in config
 * Supporting any character as thousand separator
 * Improved getFormattedValue
 * Removed unnecessary code to create format template, now using float.toFixed(this.decimalPrecission)    
 */
	
Ext.ux.NumericField = function(config){
	var defaultConfig = 
    {
		style: 'text-align:right;'
    };
    
    Ext.ux.NumericField.superclass.constructor.call(this, Ext.apply(defaultConfig, config));

    //Only if thousandSeparator doesn't exists is assigned when using decimalSeparator as the same as thousandSeparator
	if(this.useThousandSeparator && this.decimalSeparator == ',' && Ext.isEmpty(config.thousandSeparator))
		this.thousandSeparator = '.';
	else
		if(this.allowDecimals && this.thousandSeparator == '.' && Ext.isEmpty(config.decimalSeparator))
			this.decimalSeparator = ',';
		
    this.onFocus = this.onFocus.createSequence(this.onFocus);
};

Ext.extend(Ext.ux.NumericField, Ext.form.NumberField, 
{
    currencySymbol: null,
	useThousandSeparator: true,
	thousandSeparator: ',',
	alwaysDisplayDecimals: false,
	setValue: function(v){
	   Ext.ux.NumericField.superclass.setValue.call(this, v);
       
	   this.setRawValue(this.getFormattedValue(this.getValue()));
    },
	/**
	 * No more using Ext.util.Format.number, Ext.util.Format.number in ExtJS versions
	 * less thant 4.0 doesn't allow to use a different thousand separator than "," or "."
	 * @param {Number} v
	 */
    getFormattedValue: function(v){
       
		if (Ext.isEmpty(v) || !this.hasFormat()) 
            return v;
	    else 
        {
			var neg = null;
			
			v = (neg = v < 0) ? v * -1 : v;	
			v = this.allowDecimals && this.alwaysDisplayDecimals ? v.toFixed(this.decimalPrecision) : v;
			
			if(this.useThousandSeparator)
			{
				if(this.useThousandSeparator && Ext.isEmpty(this.thousandSeparator))
					throw ('NumberFormatException: invalid thousandSeparator, property must has a valid character.');
				
				if(this.thousandSeparator == this.decimalSeparator)
					throw ('NumberFormatException: invalid thousandSeparator, thousand separator must be different from decimalSeparator.');
				
				var v = String(v);
		
				var ps = v.split('.');
                ps[1] = ps[1] ? ps[1] : null;
                
                var whole = ps[0];
                
                var r = /(\d+)(\d{3})/;

				var ts = this.thousandSeparator;
				
                while (r.test(whole)) 
                    whole = whole.replace(r, '$1' + ts + '$2');
            
			    v = whole + (ps[1] ? this.decimalSeparator + ps[1] : '');
			}
			
			return String.format('{0}{1}{2}', (neg ? '-' : ''), (Ext.isEmpty(this.currencySymbol) ? '' : this.currencySymbol + ' '), v);
        }
    },
    /**
     * overrides parseValue to remove the format applied by this class
     */
    parseValue: function(v){
		//Replace the currency symbol and thousand separator
        return Ext.ux.NumericField.superclass.parseValue.call(this, this.removeFormat(v));
    },
    /**
     * Remove only the format added by this class to let the superclass validate with it's rules.
     * @param {Object} v
     */
    removeFormat: function(v){
        if (Ext.isEmpty(v) || !this.hasFormat()) 
            return v;
        else 
        {
			v = v.replace(this.currencySymbol + ' ', '');
			
			v = this.useThousandSeparator ? v.replace(new RegExp('[' + this.thousandSeparator + ']', 'g'), '') : v;
			//v = this.allowDecimals && this.decimalPrecision > 0 ? v.replace(this.decimalSeparator, '.') : v;
			
            return v;
        }
    },
    /**
     * Remove the format before validating the the value.
     * @param {Number} v
     */
    getErrors: function(v){
        return Ext.ux.NumericField.superclass.getErrors.call(this, this.removeFormat(v));
    },
	hasFormat: function()
	{
		return this.decimalSeparator != '.' || this.useThousandSeparator == true || !Ext.isEmpty(this.currencySymbol) || this.alwaysDisplayDecimals;	
	},
    /**
     * Display the numeric value with the fixed decimal precision and without the format using the setRawValue, don't need to do a setValue because we don't want a double
     * formatting and process of the value because beforeBlur perform a getRawValue and then a setValue.
     */
    onFocus: function(){
		this.setRawValue(this.removeFormat(this.getRawValue()));
    }
});
Ext.reg('numericfield', Ext.ux.NumericField);

if(false)Ext.lib.Ajax.serializeForm = function(form) {
	if(typeof form == 'string') {
        form = (document.getElementById(form) || document.forms[form]);
    }

    var el, name, val, disabled, data = '', hasSubmit = false;
    for (var i = 0; i < form.elements.length; i++) {
        el = form.elements[i];
        disabled = form.elements[i].disabled;
        name = form.elements[i].name;
        val = form.elements[i].value;

        if (!disabled && name){
        	
            switch (el.type){
                case 'select-one':
                case 'select-multiple':
                    for (var j = 0; j < el.options.length; j++) {
                        if (el.options[j].selected) {
                            if (Ext.isIE) {
                                data += encodeURIComponent(name) + '=' + encodeURIComponent(el.options[j].attributes['value'].specified ? el.options[j].value : el.options[j].text) + '&';
                            }
                            else {
                                data += encodeURIComponent(name) + '=' + encodeURIComponent(el.options[j].hasAttribute('value') ? el.options[j].value : el.options[j].text) + '&';
                            }
                        }
                    }
                    break;
                case 'radio':
                case 'checkbox':
                    if (el.checked) {
                        data += encodeURIComponent(name) + '=' + encodeURIComponent(val) + '&';
                    }
                    break;
                case 'file':	
                case undefined:	
                case 'reset':
                case 'button':
                    break;
                case 'submit':
                    if(hasSubmit == false) {
                        data += encodeURIComponent(name) + '=' + encodeURIComponent(val) + '&';
                        hasSubmit = true;
                    }
                    break;
                default:
                	if(el.className.indexOf("x-form-num-field")>-1){
                		var s=val;
                		s=s.split('.').join('');
                		s=s.split(',').join('.');
                		data += encodeURIComponent(name) + '=' + encodeURIComponent(s) + '&';
                	}else{
                		data += encodeURIComponent(name) + '=' + encodeURIComponent(val) + '&';
                	}
                    break;
            }
        }
    }
    data = data.substr(0, data.length - 1);
    return data;
}