(function() {
	
	/* This is the constructor */
	Zoho = function(htmlId) {
		this.id = htmlId;
		this.name = "Zoho";
		/* Initialise prototype properties */
	    this.widgets = {};
	    this.modules = {};

		/* Register this component */
		Alfresco.util.ComponentManager.register(this);

		/* Load YUI Components */
		Alfresco.util.YUILoaderHelper.require(["button", "container", "datasource", "datatable", "json"], this.onComponentsLoaded, this);
	}

	Zoho.prototype = {
		id:"",
		name:"",
		widgets: null,
		modules: null,
		options: {},
		loadingMessage: null,
		nodeRef: "",
		
		onComponentsLoaded : function() {
		    YAHOO.util.Event.onContentReady(this.id, this.OnDocumentReady, this, true);
		},

		OnDocumentReady : function() {
			// Start the load
			this.loadingMessage = Alfresco.util.PopupManager.displayMessage( {
				text: 'Loading',
				displayTime: 0
			});
			
			Alfresco.util.Ajax.request({
				url: Alfresco.constants.PROXY_URI + "/zoho/edit/" + this.nodeRef.replace(":/",''),
				successCallback: {fn: this.SuccessCallback, scope:this},
				failureMessage: "Could not load document, try refreshing.", 
				scope: this
			});
			
			YAHOO.util.Event.addListener( window, 'resize', this.resizeIframe, this, true );
			YAHOO.util.Event.onContentReady( 'alf-ft', this.resizeIframe, this, true);
			YAHOO.util.Event.onContentReady( 'alf-hd', this.resizeIframe, this, true );
		},
		
		SuccessCallback: function(o){
			var iframe = YAHOO.util.Dom.get( this.id + '-zohoiframe' );
			
			if( o.json.status && o.json.status != "error" && o.json.zohourl){
				iframe.src = o.json.zohourl;
			}

			if( this.loadingMessage ){
				this.loadingMessage.hide();
				this.loadingMessage = null;
			}
			
		},
		
		resizeIframe: function(){
			var iframe = YAHOO.util.Dom.get( this.id + '-zohoiframe' );
			var footer = YAHOO.util.Dom.get('global_x002e_footer' );
			var header = YAHOO.util.Dom.get('alf-hd');
			
			var footerRegion = YAHOO.util.Dom.getRegion( footer );
			var footerHeight = 0 
			if( footerRegion ){
				footerHeight = footerRegion.bottom - footerRegion.top;
			}
			
			var headerRegion = YAHOO.util.Dom.getRegion( header );
			
			var headerHeight = 0;
			if( headerRegion ){
				headerHeight = headerRegion.bottom - headerRegion.top;
			}
			
			iframe.height = YAHOO.util.Dom.getViewportHeight() - ( footerHeight + headerHeight + 35 );
			iframe.width = YAHOO.util.Dom.getViewportWidth() - 20;
		}
	}
})();
