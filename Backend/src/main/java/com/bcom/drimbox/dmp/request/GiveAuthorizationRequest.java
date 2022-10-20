/*
 *  GiveAuthorizationRequest.java - DRIMBox
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

public class GiveAuthorizationRequest extends BaseRequest {	

	@Override
	protected String actionName() {
		return "urn:si-dmp-habilitations:HabilitationsEndPoint:setAuthorization";
	}

	@Override
	protected String serviceName() {
		return "habilitations";
	}

	public GiveAuthorizationRequest() {
		super();

		var psetAuthorization = soapRequest.createElement("setAuthorization");
		psetAuthorization.setAttribute("xmlns", "urn:si-dmp-habilitations");
		body.appendChild(psetAuthorization);

		var pAction = soapRequest.createElement("action");
		pAction.appendChild(soapRequest.createTextNode("AJOUT"));
		psetAuthorization.appendChild(pAction);
	}


}
