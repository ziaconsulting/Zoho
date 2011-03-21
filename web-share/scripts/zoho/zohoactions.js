(function()
{
    ZiaZoho = {};
    ZiaZoho.prototype =
    {
    		/**
    	       * The urls to be used when creating links in the action cell
    	       *
    	       * @method getActionUrls
    	       * @return {object} Object literal containing URLs to be substituted in action placeholders
    	       */
    	      getActionUrls: function DocumentActions_getActionUrls()
    	      {
    	         var urlContextSite = Alfresco.constants.URL_PAGECONTEXT + "site/" + this.options.siteId,
    	            nodeRef = this.assetData.nodeRef;

    	         return (
    	         {
    	            downloadUrl: Alfresco.constants.PROXY_URI + this.assetData.contentUrl + "?a=true",
    	            editMetadataUrl: urlContextSite + "/edit-metadata?nodeRef=" + nodeRef,
    	            zohoEditUrl: urlContextSite + "/zohoedit?nodeRef=" + nodeRef
    	         });
    	      }
    };
   
    YAHOO.lang.augmentProto(Alfresco.DocumentActions, ZiaZoho, true);
})();