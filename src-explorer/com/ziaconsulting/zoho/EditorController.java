/*******************************************************************************
 * Copyright (c) 2011 Zia Consulting
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package com.ziaconsulting.zoho;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class EditorController extends DeclarativeWebScript {
	private static String[] zohoDocFileExtensions = { "doc", "docx", "html",
			"odt", "rtf", "sxw", "txt" };
	private static String[] zohoXlsFileExtensions = { "csv", "ods", "sxc",
			"tsv", "xls", "xlsx" };
	private static String[] zohoPptFileExtensions = { "odp", "ppt", "pps",
			"sxi" };
	private final Logger log = Logger.getLogger(EditorController.class);

	private String apiKey = null;
	private String saveUrl = null;
	private Boolean useRemoteAgent = true;
	private String sheetUrl = "sheet.zoho.com";
	private String showUrl = "show.zoho.com";
	private String writerUrl = "writer.zoho.com";
	private String remoteAgentName = "ziaconsultingintegation";
	private Boolean ssl = false;
	private String skey = "";

	private ServiceRegistry serviceRegistry;

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public Boolean getSsl() {
		return ssl;
	}

	public void setSsl(Boolean ssl) {
		this.ssl = ssl;
	}

	public String getRemoteAgentName() {
		return remoteAgentName;
	}

	public void setRemoteAgentName(String remoteAgentName) {
		this.remoteAgentName = remoteAgentName;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setSaveUrl(String url) {
		this.saveUrl = url;
	}

	public void setUseRemoteAgent(Boolean b) {
		this.useRemoteAgent = b;
	}

	public void setSheetUrl(String sheetUrl) {
		this.sheetUrl = sheetUrl;
	}

	public void setShowUrl(String showUrl) {
		this.showUrl = showUrl;
	}

	public void setWriterUrl(String writerUrl) {
		this.writerUrl = writerUrl;
	}

	public void initialize() {
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,
			Status status, Cache cache) {

		Match match = req.getServiceMatch();
		Map<String, String> vars = match.getTemplateVars();
		NodeRef theNodeRef = new NodeRef(vars.get("store_type"),
				vars.get("store_id"), vars.get("id"));

		String fileName = serviceRegistry.getNodeService()
				.getProperty(theNodeRef, ContentModel.PROP_NAME).toString();

		String protocol = ssl ? "https" : "http";

		String extension;
		if (fileName.lastIndexOf('.') != -1) {
			extension = fileName.substring(fileName.lastIndexOf('.') + 1);
		} else {
			String contentMime = serviceRegistry.getContentService()
					.getReader(theNodeRef, ContentModel.PROP_CONTENT)
					.getMimetype();
			extension = serviceRegistry.getMimetypeService()
					.getExtensionsByMimetype().get(contentMime);

			// Attach this extension to the filename
			fileName += "." + extension;
			log.debug("Extension not found, using mimetype " + contentMime
					+ " to guess the extension file name is now " + fileName);
		}

		String zohoUrl = "";
		if (Arrays.binarySearch(zohoDocFileExtensions, extension) >= 0) {
			zohoUrl = writerUrl;
		} else if (Arrays.binarySearch(zohoXlsFileExtensions, extension) >= 0) {
			zohoUrl = sheetUrl;
		} else if (Arrays.binarySearch(zohoPptFileExtensions, extension) >= 0) {
			zohoUrl = showUrl;
		} else {
			log.info("Invalid extension " + extension);
			return getErrorMap("Invalid extension");
		}

		// Create multipart form for post
		List<Part> parts = new ArrayList<Part>();

		String output = "";
		if (vars.get("mode").equals("edit")) {
			output = "url";
			parts.add(new StringPart("mode", "collabedit"));
		} else if (vars.get("mode").equals("view")) {
			output = "viewurl";
			parts.add(new StringPart("mode", "view"));
		}

		String docIdBase = vars.get("store_type") + vars.get("store_id")
				+ vars.get("id");
		parts.add(new StringPart("documentid", generateDocumentId(docIdBase)));

		String id = theNodeRef.toString() + "#"
				+ serviceRegistry.getAuthenticationService().getCurrentTicket();
		parts.add(new StringPart("id", id));
		parts.add(new StringPart("format", extension));
		parts.add(new StringPart("filename", fileName));
		parts.add(new StringPart("username", serviceRegistry
				.getAuthenticationService().getCurrentUserName()));
		parts.add(new StringPart("skey", skey));

		if (useRemoteAgent) {
			parts.add(new StringPart("agentname", remoteAgentName));
		} else {
			String saveUrl;
			saveUrl = protocol + "://" + this.saveUrl + "/alfresco/s/zohosave";
			parts.add(new StringPart("saveurl", saveUrl));
		}

		ContentReader cr = serviceRegistry.getContentService().getReader(
				theNodeRef, ContentModel.PROP_CONTENT);
		if (!(cr instanceof FileContentReader)) {
			log.error("The content reader was not a FileContentReader");
			return getErrorMap("Error");
		}

		PartSource src = null;
		try {
			src = new FilePartSource(fileName,
					((FileContentReader) cr).getFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error("The content did not exist.");
			return getErrorMap("Error");
		}

		parts.add(new FilePart("content", src, cr.getMimetype(), null));

		HttpClient client = new HttpClient();

		String zohoFormUrl = protocol + "://" + zohoUrl + "/remotedoc.im?"
				+ "apikey=" + apiKey + "&output=" + output;
		PostMethod httppost = new PostMethod(zohoFormUrl);
		httppost.setRequestEntity(new MultipartRequestEntity(parts
				.toArray(new Part[0]), httppost.getParams()));

		Map<String, Object> returnMap = getReturnMap();

		String retStr = "";
		int zohoStatus = 0;
		try {
			zohoStatus = client.executeMethod(httppost);
			retStr = httppost.getResponseBodyAsString();
		} catch (HttpException he) {
			log.error("Error", he);
			returnMap = getErrorMap("Error");
		} catch (IOException io) {
			io.printStackTrace();
			log.error("Error", io);
			returnMap = getErrorMap("Error");
		}

		if (zohoStatus == 200) {
			Map<String, String> parsedResponse = parseResponse(retStr);

			if (parsedResponse.containsKey("RESULT")
					&& parsedResponse.get("RESULT").equals("TRUE")) {
				returnMap.put("zohourl", parsedResponse.get("URL"));
			} else if (parsedResponse.containsKey("RESULT")
					&& parsedResponse.get("RESULT").equals("FALSE")) {
				returnMap = getErrorMap(parsedResponse.get("ERROR"));
			}
		} else {
			returnMap = getErrorMap("Remote server did not respond");
		}

		return returnMap;
	}

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	private Map<String, Object> getErrorMap(String msg) {
		Map<String, Object> returnMap = getReturnMap();
		returnMap.put("status", "error");
		returnMap.put("error", msg);
		return returnMap;
	}

	private Map<String, Object> getReturnMap() {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("error", "");
		returnMap.put("zohourl", " ");
		returnMap.put("status", "success");

		return returnMap;
	}

	private String generateDocumentId(String base) {
		byte messageDigest[] = {};
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(base.getBytes());
			messageDigest = algorithm.digest();
		} catch (NoSuchAlgorithmException nsae) {

		}

		String md5DocId;
		StringBuffer md5DocIdBuffer = new StringBuffer();
		for (byte msg : messageDigest) {
			md5DocIdBuffer.append(Byte.toString(msg));
		}

		// Changed this to invalidate the current documents
		// Id needs to be only integers, < 19 characters long
		md5DocId = "6"
				+ md5DocIdBuffer.toString().replace("-", "").substring(0, 17);

		return md5DocId;
	}

	private Map<String, String> parseResponse(String response) {
		Map<String, String> retMap = new HashMap<String, String>();

		String[] lines = response.split("\n");
		for (String line : lines) {

			retMap.put(line.substring(0, line.indexOf("=")),
					line.substring(line.indexOf("=") + 1));
		}

		return retMap;
	}
}
