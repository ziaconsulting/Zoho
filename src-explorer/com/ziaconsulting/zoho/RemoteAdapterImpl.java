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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Hashtable;

import org.alfresco.service.ServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adventnet.zoho.remoteagent.RemoteAdapter;

/**
 * This class is mapped to the Remote agent running in RemoteAgentWrapper by the
 * config file zoho-remote-conf.properties
 * 
 * @author dhopkins
 * 
 */
public class RemoteAdapterImpl implements RemoteAdapter {
	private static Log logger = LogFactory.getLog(RemoteAdapterImpl.class);

	/*
	 * This is a huge hack to push the service registry into this class. This
	 * class is instantiated by Spring, which sets the serviceRegistry. By the
	 * time the remote agent calls it the service registry will be set and it
	 * should have access to Alfresco.
	 */
	private static RemoteAdapterImpl instance = null;

	private ServiceRegistry serviceRegistry;

	public ServiceRegistry getServiceRegistry() {
		return instance.serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		if (instance == null) {
			instance = new RemoteAdapterImpl();
		}
		instance.serviceRegistry = serviceRegistry;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public String handleAutoSaveDraft(Hashtable info, byte[] content) {
		return "Not Implemented";
	}

	@Override
	@SuppressWarnings("rawtypes")
	public String handleZohoDocument(Hashtable info, byte[] content) {
		return saveImpl(info, content);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public String handleZohoPresentation(Hashtable info, byte[] content) {
		return saveImpl(info, content);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public String handleZohoWorkBook(Hashtable info, byte[] content) {
		return saveImpl(info, content);
	}

	@SuppressWarnings("rawtypes")
	private String saveImpl(Hashtable info, byte[] content) {
		logger.info("Saved file from remote agent");
		InputStream in = new ByteArrayInputStream(content);
		String id = info.get("id").toString();
		String nodeRefStr = id.substring(0, id.indexOf("#"));
		String ticket = id.substring(id.indexOf("#") + 1);

		boolean saved = SaveHelper.doSave(id, nodeRefStr, ticket, in,
				instance.serviceRegistry);

		return saved ? "Document saved to Alfresco" : "Document DID NOT save!";
	}
}
