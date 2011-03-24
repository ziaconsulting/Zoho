Here's a [tech talk](http://www.youtube.com/user/ziaconsulting#p/u/1/LSbdYOoD2YM) about the integration.

#Zoho Installation Guide#
##Remote Agent##
Create a zoho account at https://accounts.zoho.com/register
Get an API key at http://apihelp.wiki.zoho.com/Generate-API-Key.html

##Notes##
Make sure to confirm your email address.

Configure *alfresco-global.properties*
Here's the usual config, use the username (not your email) and the password that was setup
`
zoho.useRemoteAgent=true
zoho.agentname=
zoho.agentpasswd=
zoho.apikey=
zoho.ssl=true
zoho.skey=

zoho.saveDomain=
`
*Save domain is not necessary unless you're using the saveUrl method of saving.*

##Install the amps##
Not much new here, make sure that the version is set to update.

## Build ##
`ant package`
*Make sure that you have alfresco source downloaded and linked in the build file*

##Fire it up##
On start up monitor the catalina.out, the api for zoho writes to standard out, which doesn't appear in the alfresco.log Near the beginning of the log you should see:
`
Reading conf from --->/opt/alfresco/tomcat/temp/alfresco-remote-properties6512484253718904665.properties
Connecting Zoho Accounts ...
sucessfully logged in....
Registered for the identifier....
Registered with Zoho WMSserver.....
Zoho RemoteAgent started sucessfully.....1276027051850
`

If that runs and there are no issues starting the server it should all work.

## Usage ##
There should be an "Edit document" button on the document details of all of the nodes. Click that and it will open a Zoho iFrame that allows editing.
