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

import java.io.File;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import com.adventnet.zoho.remoteagent.Starter;

import de.schlichtherle.io.FileOutputStream;

public class RemoteAgentWrapper implements ApplicationContextAware {
	private Log logger = LogFactory.getLog(RemoteAgentWrapper.class);
	private Resource confFileResource;
	private Properties confFileProperties;
	private volatile Thread agentThread = null;
	private String agentName;
	private String agentPassword;
	private Boolean ssl;
	private ApplicationContext applicationContext;

	public Boolean getSsl() {
		return ssl;
	}

	public void setSsl(Boolean ssl) {
		this.ssl = ssl;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getAgentPassword() {
		return agentPassword;
	}

	public void setAgentPassword(String agentPassword) {
		this.agentPassword = agentPassword;
	}

	public Properties getConfFileProperties() {
		return confFileProperties;
	}

	public void setConfFileProperties(Properties confFileProperties) {
		this.confFileProperties = confFileProperties;
	}

	public String getConfFile() {
		return confFileResource.getFilename();
	}

	public void setConfFile(String confFile) {
		this.confFileResource = applicationContext.getResource(confFile);
	}

	public void initialize() {
		if (agentThread == null || !agentThread.isAlive()) {

			// Add the name and password to the properties and store it to a tmp
			// file
			confFileProperties.put("agentname", agentName);
			confFileProperties.put("agentpasswd", agentPassword);
			confFileProperties.put("ssl", ssl.toString());
			File tmpConfFile = null;
			try {
				tmpConfFile = File.createTempFile("alfresco-remote",
						".properties");
				confFileProperties.store(new FileOutputStream(tmpConfFile), "");
			} catch (Exception e) {
				logger.fatal("Couldn't create temp file", e);
				return;
			}

			agentThread = new Thread(new RemoteAgentRunner(
					tmpConfFile.getAbsolutePath()), "RemoteAgentThread");
			agentThread.setDaemon(true);
			agentThread.start();
			logger.debug("Started Remote Agent");
		}
	}

	private static class RemoteAgentRunner implements Runnable {
		private String confFileLocation;

		public RemoteAgentRunner(String confFileLocation) {
			this.confFileLocation = confFileLocation;
		}

		@Override
		public void run() {
			String[] args = { confFileLocation };
			Starter.main(args);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
}
