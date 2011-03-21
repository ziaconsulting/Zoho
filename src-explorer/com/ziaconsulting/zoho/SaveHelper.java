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

import java.io.InputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;

import com.ziaconsulting.alfresco.AlfUtil;

public class SaveHelper {
	public static boolean doSave(String id, String nodeRefStr, String ticket,
			InputStream newFile, ServiceRegistry serviceRegistry) {
		try {
			serviceRegistry.getAuthenticationService().validate(ticket);
		} catch (AuthenticationException ae) {
			return false;
		}

		NodeRef nodeRef = new NodeRef(nodeRefStr);
		ContentWriter fcw = (ContentWriter) AlfUtil.services()
				.getContentService()
				.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
		try {
			fcw.putContent(newFile);
		} catch (ContentIOException e1) {
			e1.printStackTrace();
		}

		return true;
	}
}
