/*
 *  CStoreSCP.java - DRIMBox
 *  Copyright 2022 b<>com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bcom.drimbox.pacs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Singleton;

import org.apache.commons.fileupload.MultipartStream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRQHandler;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.util.StreamUtils;

@Singleton
public class CStoreSCP {
	private Device device;
	private int currentID = 1;
	private ByteArrayOutputStream output;
	// Boundary for multipart request
	private static final String BOUNDARY = "myBoundary";

	private String host;
	private String aet;

	public void startCStore(String calledAET, String bindAddress, int port) throws Exception {
		this.host = bindAddress;
		this.aet = calledAET;

		this.output = new ByteArrayOutputStream();

		ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		ExecutorService executor = Executors.newCachedThreadPool();


		device = new Device("c-echo-scp");
		ApplicationEntity ae = new ApplicationEntity(calledAET);
		Connection conn = new Connection(null, "127.0.0.1", port);
		conn.setBindAddress(bindAddress);
		// enable Asynchronous Operations
		conn.setMaxOpsInvoked(0);
		conn.setMaxOpsPerformed(0);
		device.addApplicationEntity(ae);
		device.addConnection(conn);
		ae.addConnection(conn);
		ae.addTransferCapability(new TransferCapability(null,
				"*", TransferCapability.Role.SCP, "*"));
		device.setDimseRQHandler(createServiceRegistry());


		setExecutor(executor);
		setScheduledExecutor(scheduledExecutor);
		start();
	}


	public void setExecutor(Executor executor) {
		device.setExecutor(executor);
	}

	public void setScheduledExecutor(ScheduledExecutorService executor) {
		device.setScheduledExecutor(executor);
	}

	public void start() throws IOException, GeneralSecurityException {
		device.bindConnections();
	}

	private DimseRQHandler createServiceRegistry() {
		DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
		serviceRegistry.addDicomService(new BasicCEchoSCP());
		serviceRegistry.addDicomService(new BasicCStoreSCP("*") {
			@Override
			protected void store(Association as, PresentationContext pc, Attributes rq,
					PDVInputStream data, Attributes rsp) throws IOException {
				CStoreSCP.this.store(as, pc, rq, data);
			}
		});
		return serviceRegistry;
	}

	public byte[] getMultipartGlobal() throws Exception {

		 this.output.write(("--" + BOUNDARY + "--").getBytes());
		 return this.output.toByteArray();
	}
	
	public void resetMultipart() {
		this.output.reset();
		this.currentID = 1;
	}

	private void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data)
			throws IOException {
		String cuid = rq.getString(Tag.AffectedSOPClassUID);
		String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
		String tsuid = pc.getTransferSyntax();
		Attributes fmi = as.createFileMetaInformation(iuid, cuid, tsuid);
		this.output.write(("--" + BOUNDARY + "\r\nContent-ID: <"+ currentID +"@resteasy-multipart>\r\nContent-Type: application/dicom;transfer-syntax=1.2.840.10008.1.2.1\r\n\r\n").getBytes());

		try (DicomOutputStream dos = new DicomOutputStream(this.output, tsuid)) {
			dos.writeFileMetaInformation(fmi);
			StreamUtils.copy(data, dos);
		}	
		this.output.write(data.readAllBytes());
		this.output.write(("\r\n").getBytes());
		currentID++;

	}

	public String getHost() {
		return host;
	}

	public String getAET() {
		return aet;
	}
}
















