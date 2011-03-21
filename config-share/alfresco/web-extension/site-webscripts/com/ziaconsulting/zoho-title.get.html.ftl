<div class="page-title search-title theme-bg-color-1">
	<div class="title">
      <h1 class="theme-color-3">${msg("zoho-title.title")}</h1>
   </div>
   <div class="links title-button">
    <span class="yui-button yui-link-button">
     <span class="first-child">
        <a href="${url.context}/page/site/${page.url.templateArgs.site!}/document-details?nodeRef=${page.url.args.nodeRef!}">${msg("zoho-title.back")}</a>
     </span>
    </span>
    <span id="fullscreenButton" class="yui-button yui-checkbox-button">
     <span class="first-child">
     	<button type="button">${msg("zoho-title.fullscreen")}</button>
     </span>
    </span>
   </div>
</div>	
<script>
	var footerOriginalHeight = 0;
	var headerOriginalHeight = 0;
	var Dom = YAHOO.util.Dom;
	
	function fullscreenToggle( p_oEvent ){
		var footer = Dom.getElementsByClassName('sticky-footer')[0];
		var footer2 = Dom.getElementsByClassName('sticky-push')[0];
		var header = Dom.get('global_x002e_header');
		 
		var animationTime = 0;
		if( this.get("checked") ){
			YAHOO.util.Dom.setStyle( footer, 'display', 'none' );
			YAHOO.util.Dom.setStyle( footer2, 'display', 'none' );
			YAHOO.util.Dom.setStyle( header, 'display', 'none' );
			zoho.resizeIframe();
		}
		else{
			YAHOO.util.Dom.setStyle( footer, 'display', 'block' );
			YAHOO.util.Dom.setStyle( footer2, 'display', 'block' );
			YAHOO.util.Dom.setStyle( header, 'display', 'block' );
			zoho.resizeIframe();
		}
	}
	
	function onButtonsReady() {
		
	    var oFullscreenButton = new YAHOO.widget.Button("fullscreenButton", {
	    type: "checkbox",
	    onclick: { fn: fullscreenToggle }
	    });
	}
	
	YAHOO.util.Event.onContentReady("fullscreenButton", onButtonsReady);
	
</script>