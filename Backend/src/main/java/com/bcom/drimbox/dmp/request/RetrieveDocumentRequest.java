/*
 *  RetrieveDocumentRequest.java - DRIMBox
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
