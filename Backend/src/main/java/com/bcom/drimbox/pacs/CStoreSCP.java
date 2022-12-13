/*
 *  CStoreSCP.java - DRIMBox
 *
 * MIT License
 *
 * Copyright (c) 2022 b<>com
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.bcom.drimbox.pacs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

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

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.core.eventbus.EventBus;

@Singleton
public class CStoreSCP {
	// Current index of multipart request
	private static final int BASE_INDEX = 1;
	// Vertx event bus address sent when all the images are received
	public static final String EB_DONE_ADDRESS = "done";
	// Vertx event bus address sent when an image is received
	public static final String EB_IMAGE_ADDRESS = "image";


	private Device device;
	private int currentID = BASE_INDEX;

	private String host;
	private String aet;
	
	private String boundary;
	
	private ApplicationEntity ae;

	@Inject
	EventBus eventBus;



	public void startCStore(String calledAET, String bindAddress, int port) throws Exception {
		this.host = bindAddress;
		this.aet = calledAET;

		ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		ExecutorService executor = Executors.newCachedThreadPool();


		device = new Device("c-echo-scp");
		this.ae = new ApplicationEntity(calledAET);
		Connection conn = new Connection(null, "127.0.0.1", port);
		conn.setBindAddress(bindAddress);
		// enable Asynchronous Operations
		conn.setMaxOpsInvoked(0);
		conn.setMaxOpsPerformed(0);
		device.addApplicationEntity(this.ae);
		device.addConnection(conn);
		this.ae.addConnection(conn);

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

	public void done() {
		eventBus.publish(EB_DONE_ADDRESS, true);
	}


	public Multi<byte[]> getResponseStream() {
		return Multi.createFrom().emitter( em -> {
			// Emit a new image when received
			eventBus.consumer(EB_IMAGE_ADDRESS).handler(m-> {
				em.emit((byte[]) m.body());
			});

			// Mark the end of the stream
			eventBus.consumer(EB_DONE_ADDRESS).handler(m-> {
				em.emit(("--" + this.boundary + "--").getBytes());
				em.complete();
			});
		});
	}
	
	public void resetMultipart() {
		this.currentID = BASE_INDEX;
	}
	
	public void setBoundary(String boundary) {
		this.boundary = boundary;
	}
	
	public void resetTransferSyntaxes(List<String> transferSyntaxs) {
		
		  String[] str = new String[transferSyntaxs.size()];
		  
	        for (int i = 0; i < transferSyntaxs.size(); i++) {
	            str[i] = transferSyntaxs.get(i);
	        }
	        

		this.ae.removeTransferCapabilityFor("*", TransferCapability.Role.SCP);		
		this.ae.addTransferCapability(new TransferCapability(null,
				"*", TransferCapability.Role.SCP, str));
		}

	private void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data)
			throws IOException {
		String cuid = rq.getString(Tag.AffectedSOPClassUID);
		String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
		String tsuid = pc.getTransferSyntax();
		Attributes fmi = as.createFileMetaInformation(iuid, cuid, tsuid);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		output.write(("--" + this.boundary + "\r\nContent-ID: <"+ currentID +"@resteasy-multipart>\r\nContent-Type: application/dicom;transfer-syntax=1.2.840.10008.1.2.1\r\n\r\n").getBytes());

		try (DicomOutputStream dos = new DicomOutputStream(output, tsuid)) {
			dos.writeFileMetaInformation(fmi);
			StreamUtils.copy(data, dos);
		}

		output.write(data.readAllBytes());
		output.write(("\r\n").getBytes());
		output.flush();

		// Tell vertx we have a new image
		eventBus.publish(EB_IMAGE_ADDRESS, output.toByteArray());

		currentID++;

	}

	public String getHost() {
		return host;
	}

	public String getAET() {
		return aet;
	}
}
















