/*
 *  CMoveSCU.java - DRIMBox
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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.Priority;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class CMoveSCU {
	private Device device;
	private ApplicationEntity ae;

	@ConfigProperty(name="dcm.cmove.callingAET")
	String callingAET;
	@ConfigProperty(name="dcm.cmove.calledAET")
	String calledAET;
	@ConfigProperty(name="dcm.cmove.port")
	int port;

	private Attributes request;

	// Cache of instance data
	@Inject
	CStoreSCP cStoreSCP;


	public byte[] cMove(String studyUID, String serieUID) throws Exception {
		request = new Attributes(2);
		this.request.setString(Tag.QueryRetrieveLevel, VR.CS,"SERIES");
		this.request.setString(Tag.StudyInstanceUID, VR.UI, studyUID);
		this.request.setString(Tag.SeriesInstanceUID, VR.UI, serieUID);
		
		cStoreSCP.resetMultipart();
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		try {
			device = new Device("c-move-scu");
			ae = new ApplicationEntity(this.callingAET);
			Connection conn = new Connection();
			device.addApplicationEntity(ae);
			device.addConnection(conn);
			ae.addConnection(conn);

			setExecutor(executor);
			setScheduledExecutor(scheduledExecutor);
			doCMove();
		} finally {
			executor.shutdown();
			scheduledExecutor.shutdown();
		}
		return cStoreSCP.getMultipartGlobal();
	}

	public void setExecutor(Executor executor) {
		device.setExecutor(executor);
	}

	public void setScheduledExecutor(ScheduledExecutorService executor) {
		device.setScheduledExecutor(executor);
	}

	private void doCMove()
			throws IOException, InterruptedException, GeneralSecurityException, IncompatibleConnectionException {

		Association as = ae.connect(getConnection(this.cStoreSCP.getHost(), this.port), makeAAssociateRQ(this.calledAET));
		try {
			as.cmove(UID.StudyRootQueryRetrieveInformationModelMove,
					Priority.NORMAL,
					this.request,
					UID.ImplicitVRLittleEndian,
					this.cStoreSCP.getAET(),
					new DimseRSPHandler(as.nextMessageID()));
		} catch (IOException e) {
			System.err.printf("Failed to invoke C-MOVE-RQ to %s - %s%n", this.calledAET, e);
		}
		as.waitForOutstandingRSP();
		as.release();
	}

	private AAssociateRQ makeAAssociateRQ(String calledAET) {
		AAssociateRQ aarq = new AAssociateRQ();
		aarq.setCallingAET(ae.getAETitle()); // optional: will be set in ae.connect() if not explicitly set.
		aarq.setCalledAET(calledAET);
		aarq.addPresentationContext(
				new PresentationContext(1,
						UID.StudyRootQueryRetrieveInformationModelMove,
						UID.ImplicitVRLittleEndian));
		return aarq;
	}

	private Connection getConnection(String hostName, int port) {
		return new Connection(null, hostName, port);
	}


	public void setAttributeRequests(String[] args) {
		this.request = new Attributes(args.length);
		String type = "";
		if (args.length == 1) {
			type = "STUDY";
		} else if (args.length == 2) {
			type = "SERIES";
		} else {
			type = "IMAGE";
		}

		this.request.setString(Tag.QueryRetrieveLevel, VR.CS, type);
		this.request.setString(Tag.StudyInstanceUID, VR.UI, args[0]);
		if (args.length > 1) {
			this.request.setString(Tag.SeriesInstanceUID, VR.UI, args[1]);
			if (args.length > 2) {
				String[] iuids = new String[args.length - 2];
				System.arraycopy(args, 2, iuids, 0, iuids.length);
				this.request.setString(Tag.SOPInstanceUID, VR.UI, iuids);
			}
		}
	}
}
