/*
 *  FindFilterDocumentRequest.java - DRIMBox
 *
 * N°IDDN : IDDN.FR.001.020012.000.S.C.2023.000.30000
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

package com.bcom.drimbox.dmp.request;

import java.io.InputStream;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * Implemenation of TD 3.1
 */
public class FindFilterDocumentRequest extends BaseRequest {
	String modal = "";
	String regionOR = "";
	
	protected static final String FIND_DOCUMENTS_BY_REFERENCEID = "urn:uuid:12941a89-e02e-4be5-967c-ce4bfc8fe492";
	protected static final String FIND_DOCUMENTS = "urn:uuid:14d4debf-8f97-4251-9a74-a90016b0af0d";
	
	@Override
	protected String actionName() {
		return "urn:ihe:iti:2007:RegistryStoredQuery";
	}

	@Override
	protected String serviceName() {
		return "registry";
	}

	public FindFilterDocumentRequest(String patientINS, List<String> modalities, List<String> regions, String start, String stop, String accessionNumber) {
		super();

		var pAdhocQueryRequest = soapRequest.createElement("ns3:AdhocQueryRequest");
		pAdhocQueryRequest.setAttribute("xmlns", "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0");
		pAdhocQueryRequest.setAttribute("xmlns:ns2", "urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0");
		pAdhocQueryRequest.setAttribute("xmlns:ns3", "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0");
		pAdhocQueryRequest.setAttribute("xmlns:ns4", "urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0");
		body.appendChild(pAdhocQueryRequest);

		var pResponseOption = soapRequest.createElement("ns3:ResponseOption");
		pResponseOption.setAttribute("returnComposedObjects", "true");
		pResponseOption.setAttribute("returnType", "LeafClass");
		pAdhocQueryRequest.appendChild(pResponseOption);

		var pAdhocQuery = soapRequest.createElement("AdhocQuery");
		if (accessionNumber != null) {
			pAdhocQuery.setAttribute("id", FIND_DOCUMENTS_BY_REFERENCEID);
		}
		else {
			pAdhocQuery.setAttribute("id", FIND_DOCUMENTS);
		}
		pAdhocQueryRequest.appendChild(pAdhocQuery);

		createSlot(pAdhocQuery,"$XDSDocumentEntryPatientId", "'" + patientINS + "^^^&1.2.250.1.213.1.4.10&ISO^NH'");

		if(!modalities.isEmpty()) {
			modalities.forEach(modality 
					-> 			modal += "'" + modality + "^^1.2.250.1.213.1.1.5.618',");
			createSlot(pAdhocQuery,"$XDSDocumentEntryEventCodeList", "(" + modal.substring(0, modal.length() - 1) + ")");
		}

		if(!regions.isEmpty()) {
			regions.forEach(region 
					->{		region = this.getCisisValue(region);
					regionOR += "'" + region + "^^1.2.250.1.213.1.1.5.695',";});
			createSlot(pAdhocQuery,"$XDSDocumentEntryEventCodeList", "(" + regionOR.substring(0, regionOR.length() - 1) + ")");
		}

		if(start != null) {
			createSlot(pAdhocQuery,"$XDSDocumentEntryCreationTimeFrom", start);
		}

		if(stop != null) {
			createSlot(pAdhocQuery,"$XDSDocumentEntryCreationTimeTo", stop);
		}

		if(accessionNumber != null) {
			createSlot(pAdhocQuery, "$XDSDocumentEntryReferenceIdList", "('" + accessionNumber + "^^^&1.2.3.4.5.6&ISO^urn:ihe:iti:xds:2013:accession')");
		}

		createSlot(pAdhocQuery,"$XDSDocumentEntryStatus", "('urn:oasis:names:tc:ebxml-regrep:StatusType:Approved')");
	}

	private String getCisisValue(String value) {
		InputStream is = getFileFromResourceAsStream("CISIS/ValueRegion.json");
		JsonReader rdr = Json.createReader(is);
		JsonObject json = rdr.readObject();
		JsonArray jArr = json.getJsonObject("compose").getJsonArray("include").getJsonObject(0).getJsonArray("concept");
		for (int i=0; i < jArr.size(); i++) {
			if(jArr.getJsonObject(i).getString("display").equals(value))
				return jArr.getJsonObject(i).getString("code");
		}
		return "error";
	}

	private InputStream getFileFromResourceAsStream(String fileName) {

		// The class loader that loaded the class
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(fileName);

		// the stream holding the file content
		if (inputStream == null) {
			throw new IllegalArgumentException("file not found! " + fileName);
		} else {
			return inputStream;
		}

	}
}
