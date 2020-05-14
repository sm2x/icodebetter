Ext.ns("Ext.ux.form");
async function fetchColumns(table_name) {
	try{
		let response = await fetch("ajaxQueryData?_qid=4809&table_name="+table_name);
		let columns = await response.json();		
		return columns.data;
	} catch(error) {
		console.log(error);
	}
}
var defineSQLCompletionItemProvider = {		
		triggerCharacters:['.'],
	    provideCompletionItems: (model, position) => {	    	
	    	try{	    		
	    		var textUntilPosition = model.getValueInRange({startLineNumber: 1, startColumn: 1, endLineNumber: position.lineNumber, endColumn: position.column});
	    		var leftRight = false;
	    		var notList = false;
	    		var target = '';
	    		var noBreakLines = textUntilPosition.replace(/\r?\n|\r/g, "");
	    		var section = '';
	    		if(textUntilPosition.search(/order by/i) >= 0){	
	    			section = noBreakLines.split(/order by/i);	
	    		} else if (textUntilPosition.search(/group by/i) >= 0) {	
	    			section = noBreakLines.split(/group by/i);
	    		} else if (textUntilPosition.search(/where/i) >= 0) {
	    			section = noBreakLines.split(/where/i);
	    			notList = true;
	    		} else {
	    			let text = model.getValue();
	    			noBreakLines = text.replace(/\r?\n|\r/g, "");
	    			section = noBreakLines.split(/select/i);
	    			leftRight = true;
	    		}
	    		var x = section[1].split('.');
	    		target = x[0].trim();

	    		//console.log(target);
	    		var sectionSide = leftRight ? section[1] : section[0];
	    		if(sectionSide.search(/where/i) >= 0){
	    			let noWhere = sectionSide.split(/where/i);
	    			sectionSide = noWhere;
	    		}

	    		var splitFrom = notList ? sectionSide.split(/from/i) : sectionSide[0].split(/from/i);
	    		var noComments = splitFrom[1].replace(/(\/\*[^*]*\*\/)|(\/\/[^*]*)/g, '');
	    		var withSpace = noComments.replace(target, "");
	    		var tableName = withSpace.trim();
	    		if (tableName.includes('.')) {
	    			var schemaTable = tableName.split('.');
	    			tableName = schemaTable[1];
	    		}
	    		//console.log(tableName);
		    	
		    	
		    	var promise = fetchColumns(tableName);
		    	//console.log(promise);
		    	
		    	return promise.then((columnArray)=> {
			    	var completionList = [];		    	
			    	for (var i = 0; i < columnArray.length; i++) {
			    		var obj = {};
			    		obj.label = columnArray[i].column_name;obj.detail = columnArray[i].data_type;
			    		obj.kind = monaco.languages.CompletionItemKind.Function;
			    		obj.insertText = columnArray[i].column_name;	    		
			    		completionList.push(obj);
			    	}
			    	//console.log(completionList);
			    	return completionList;
		    	});	    
	    		
	    	} catch(error) {
	    		console.log(error);
	    	}		    	
	    }
}
var defineJSCompletionItemProvider = {
    triggerCharacters:['$'],
    provideCompletionItems: (model, position) => {
      return [
        {
          label: '$.getTableJSON',
          kind: monaco.languages.CompletionItemKind.Function,
          documentation: "Load Table Record as JSON",
          detail: 'Get Record from Table as JSON',insertText:'$.getTableJSON("table_name",pk)'
        },
        {
          label: '$.postForm',
          kind: monaco.languages.CompletionItemKind.Function,
          documentation: "Post Form",
          detail: 'Post to Form',insertText:'$.postForm(formId,action,{params})'
        },{
            label: '$.query',
            kind: monaco.languages.CompletionItemKind.Function,
            documentation: "Run Code2 Query",
            detail: 'Execute Code2 Query',insertText:'$.query(queryId, {params})'
        },{
          label: '$.sqlQuery',
          kind: monaco.languages.CompletionItemKind.Function,
          documentation: "Run SELECT SQL",
          detail: 'Execute RAW SQL for Listig',insertText:'$.sqlQuery("SELECT * FROM x WHERE y", {params})'
        },
        {
          label: '$.sqlExecute',
          kind: monaco.languages.CompletionItemKind.Function,
          documentation: "INSERT/UPDATE/DELETE SQLs",
          detail: 'Execute RAW SQL for Update',insertText:'$.sqlExecute("UPDATE x set y WHERE z", {params})'
        },
        {
          label: '$.execFunc',
          kind: monaco.languages.CompletionItemKind.Function,
          documentation: "Execute Code2 Global Funcction",
          detail: 'Execute GlobalFunction',insertText:'$.execFunc(globalFuncId, {params})'
        },
        {
          label: '$.REST',
          kind: monaco.languages.CompletionItemKind.Function,
          documentation: "Call REST Method",
          detail: 'Map',insertText:'$.REST("serviceName.methodName", {params})'
        },{
            label: '$.console',
            kind: monaco.languages.CompletionItemKind.Function,
            documentation: "Console",
            detail: 'Write to the Backend Console',insertText:'$.console("Hello")'
        },{
            label: '$.sendFormSmsMail',
            kind: monaco.languages.CompletionItemKind.Function,
            documentation: "Notification",
            detail: 'Send Notification',insertText:'$.sendFormSmsMail(NOTIFICATION_ID,{PK_FIELD_NAME:PK_VALUE})'
        },{
            label: '$.influxQuery',
            kind: monaco.languages.CompletionItemKind.Function,
            documentation: "Influx Query",
            detail: 'null',insertText:'$.influxQuery(externalDbId, \'select X from Y where Z=1\')'
        }/*,
        {
        	label: '$.influxWrite',
            kind: monaco.languages.CompletionItemKind.Function,
            documentation: "Influx Query",
            detail: 'null',insertText:'$.influxWrite(externalDbId, "measName", {tags}, {fields})'
	    },
        {
        	label: '$.influxWriteRaw',
            kind: monaco.languages.CompletionItemKind.Function,
            documentation: "Influx Query",
            detail: 'null',insertText:'$.influxWriteRaw(externalDbId, "meas_name,tag1=1 field1=2")'
	    },{
            label: '$.redisClient',
            kind: monaco.languages.CompletionItemKind.Function,
            documentation: "RedissonClient",
            detail: 'null',insertText:'$.redisClient(externalDbId) //.getMap("initial-map") -> get("key"), put("key", value)'
        },{
            label: '$.mqttSend',
            kind: monaco.languages.CompletionItemKind.Function,
            documentation: "MQQT Send",
            detail: 'null',insertText:'$.mqttSend(mqId, "topic", "message")'
        },{
            label: '$.mongoDatabase',
            kind: monaco.languages.CompletionItemKind.Function,
            documentation: "Mongo Database",
            detail: 'null',insertText:'$.mongoDatabase(externalDbId) // .getCollection("collection-name") -> insert(mongoBasicDBObject), find(mongoBasicDBObject)'
        },{
            label: '$.mongoQuery',
            kind: monaco.languages.CompletionItemKind.Function,
            documentation: "Mongo Query",
            detail: 'null',insertText:'$.mongoQuery(externalDbId, "collection-name" , {params})'
        },{
            label: '$.mongoInsert',
            kind: monaco.languages.CompletionItemKind.Function,
            documentation: "Mongo Insert",
            detail: 'null',insertText:'$.mongoInsert(externalDbId, "collection-name" , {values})'
        },{
            label: '$.mongoUpdate',
            kind: monaco.languages.CompletionItemKind.Function,
            documentation: "Mongo Update",
            detail: 'null',insertText:'$.mongoUpdate(externalDbId, "collection-name" , {values}, {params})'
        },{
            label: '$.mongoDelete',
            kind: monaco.languages.CompletionItemKind.Function,
            documentation: "Mongo Query",
            detail: 'null',insertText:'$.mongoDelete(externalDbId, "collection-name" , {params})'
        }
        
/*        ,{
            label: '$.redisPut',
            kind: monaco.languages.CompletionItemKind.Function,
            documentation: "Redist PUT",
            detail: 'null',insertText:'$.redisPut("host", "key", {})'
        },
        {
            label: '$.redisGet',
            kind: monaco.languages.CompletionItemKind.Function,
            documentation: "Redist GET String",
            detail: 'null',insertText:'$.redisGet("host", "key")'
	    },
	    {
	        label: '$.redisGetJSON',
	        kind: monaco.languages.CompletionItemKind.Function,
	        documentation: "Redist GET JSON",
	        detail: 'null',insertText:'$.redisGetJSON("host", "key")'
	    },
	    {
	        label: '$.mqBasicPublish',
	        kind: monaco.languages.CompletionItemKind.Function,
	        documentation: "RabbitMQ Publish",
	        detail: 'null',insertText:'$.mqBasicPublish("host", "queueName", "msg")'
	    }*/
	    
	    
      ];
    }
  };
/*
function sqlFmt(sql){
    var url = "ajaxFormatSQL?sql=" + encodeURIComponent(sql);
    return fetch(url).then((response)=>{
    	var f = response.text();
    	return f;
    	console.log('aa',f)
    	f = eval('('+f+')');
    	console.log('bb',f)
      return f.result;
    });
  };*/
  
Ext.ux.form.Monaco = Ext.extend(Ext.BoxComponent, {
  value: null,
  language: "javascript",html:'<span style="color:#ce9178">Loading Monaco Editor...<\span>',

  initComponent: function() {
    Ext.ux.form.Monaco.superclass.initComponent.apply(this, arguments);

    this.on(
      {
        afterrender: function() {
          var self = this;
          require.config({ paths: { vs: "/monaco/min/vs" } });
          require(["vs/editor/editor.main"], function() {
	    	  self.el.dom.innerHTML='';
	    	  self.editor = monaco.editor.create(self.el.dom, {
	              value: self.value,
	              language: self.language
	            });
	    	  monaco.editor.setTheme(iwb.monacoTheme || "vs-dark");
	    	  if(defineJSCompletionItemProvider && self.language=='javascript'){
	    		  monaco.languages.registerCompletionItemProvider('javascript', defineJSCompletionItemProvider);
	    		  defineJSCompletionItemProvider=false;
	    	  }
	    	  if(defineSQLCompletionItemProvider && self.language=='sql'){
	    		  monaco.languages.registerCompletionItemProvider('sql',defineSQLCompletionItemProvider);
	    		  defineSQLCompletionItemProvider=false;
	    	  }
            /*
          console.log("element: ", self.getEl());
          console.log("ownerCt: ", self.ownerCt);
          console.log("container: ", self.ownerCt.container);
          console.log("self.editor: ", self.editor);
          */
            var containerHeight = self.ownerCt.container.dom.clientHeight;
//            console.log('containerHeight',containerHeight)
            var containerWidth = self.ownerCt.container.dom.clientWidth;

            if (containerHeight < 300) {// || containerWidth < 500 
              containerHeight = 300;
//              containerWidth = 500;
              self.ownerCt.setHeight(300);
        //      self.ownerCt.setWidth(500);
              self.ownerCt.container.setHeight(300);
       //       self.ownerCt.container.setWidth(500);
            }
            self.setHeight(containerHeight);
      //      self.setWidth(containerWidth);
            self.editor.layout();

            var itemLength = self.ownerCt.items.items.length;
            var totalChildHeight = 0;
            if (itemLength > 1) {
              for (var i = 0; i < itemLength; i++) {
                if (
                  self.ownerCt.items.items[i].editor == null &&
                  self.ownerCt.items.items[i].el
                ) {
                  totalChildHeight +=
                    self.ownerCt.items.items[i].el.dom.clientHeight;
                }
              }
              var editorHeight = containerHeight - totalChildHeight - 70;
              self.setHeight(editorHeight);
              self.editor.layout();
            }

            function resizeEditor() {
              if (
                (self.el.dom &&
                  self.editor.getDomNode() &&
                  self.editor.getDomNode().clientHeight !=
                    self.el.dom.clientHeight) ||
                (self.el.dom &&
                  self.editor.getDomNode() &&
                  self.editor.getDomNode().clientWidth !=
                    self.el.dom.clientWidth)
              ) {
                self.editor.layout();
                setTimeout(resizeEditor, 100);
              } else {
                setTimeout(resizeEditor, 100);
              }
            }
            resizeEditor();

            window.onresize = function() {
              self.editor.layout();
              if(self.ownerCt.container != null){
            	  containerHeight = self.ownerCt.container.dom.clientHeight;
                  self.setHeight(containerHeight);
              }
/*              containerHeight = self.ownerCt.container.dom.clientHeight;
              containerWidth = self.ownerCt.container.dom.clientWidth;
              self.setHeight(containerHeight);
              self.setWidth(containerWidth);
*/
              self.editor.layout();
            };
          });
        },
        show: function() {
          var self = this;
          var containerHeight = self.ownerCt.container.dom.clientHeight;
          if (containerHeight < 500) {
            containerHeight = 500;
          }
          self.setHeight(containerHeight);
          self.editor.layout();
        }
      },
      this
    );
  },

  getValue: function() {
    return this.editor.getValue();
  }
});

Ext.reg("monacoeditor", Ext.ux.form.Monaco);
