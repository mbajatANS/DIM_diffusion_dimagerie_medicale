/*
 *  FindAllDocumentRequest.java - DRIMBox
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

package com.bcom.drimbox.dmp.request;

/**
 * Implemenation of TD 3.1
 */
public class FindAllDocumentRequest extends BaseRequest {
    @Override
    protected String actionName() {
        return "urn:ihe:iti:2007:RegistryStoredQuery";
    }

    @Override
    protected String serviceName() {
        return "registry";
    }

    public FindAllDocumentRequest(String patientINS) {
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
        pAdhocQuery.setAttribute("id", "urn:uuid:14d4debf-8f97-4251-9a74-a90016b0af0d");
        pAdhocQueryRequest.appendChild(pAdhocQuery);

        createSlot(pAdhocQuery,"$XDSDocumentEntryPatientId", "'" + patientINS + "^^^&1.2.250.1.213.1.4.10&ISO^NH'");
        createSlot(pAdhocQuery,"$XDSDocumentEntryStatus", "('urn:oasis:names:tc:ebxml-regrep:StatusType:Approved')");

    }
}
