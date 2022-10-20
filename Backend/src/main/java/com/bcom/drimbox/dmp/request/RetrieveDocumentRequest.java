/*
 *  RetrieveDocumentRequest.java - DRIMBox
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

public class RetrieveDocumentRequest extends BaseRequest {
	@Override
	protected String actionName() {
		return "urn:ihe:iti:2007:RetrieveDocumentSet";
	}

	@Override
	protected String serviceName() {
		return "repository";
	}

	public RetrieveDocumentRequest(String repositoryId, String uniqueId) {
		super();

		var pRetrieveDocumentSetRequest = soapRequest.createElement("RetrieveDocumentSetRequest");
		pRetrieveDocumentSetRequest.setAttribute("xmlns", "urn:ihe:iti:xds-b:2007");
		pRetrieveDocumentSetRequest.setAttribute("xmlns:ns2", "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0");
		pRetrieveDocumentSetRequest.setAttribute("xmlns:ns3", "urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0");
		pRetrieveDocumentSetRequest.setAttribute("xmlns:ns4", "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0");
		pRetrieveDocumentSetRequest.setAttribute("xmlns:ns5", "urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0");
		body.appendChild(pRetrieveDocumentSetRequest);

		var pDocumentRequest = soapRequest.createElement("DocumentRequest");
		pRetrieveDocumentSetRequest.appendChild(pDocumentRequest);

		var pRepositoryUniqueId = soapRequest.createElement("RepositoryUniqueId");
		pRepositoryUniqueId.appendChild(soapRequest.createTextNode(repositoryId));
		pDocumentRequest.appendChild(pRepositoryUniqueId);

		var pDocumentUniqueId = soapRequest.createElement("DocumentUniqueId");
		pDocumentUniqueId.appendChild(soapRequest.createTextNode(uniqueId));
		pDocumentRequest.appendChild(pDocumentUniqueId);
	}
}
