/*
 *  VIHFBase.java - DRIMBox
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

package com.bcom.drimbox.dmp.vihf;

import io.quarkus.logging.Log;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Base class that represent a VIHF.
 * Defines all the fields that are used in a VIHF. The implementation will be handled in subclasses.
 *
 * Sample API :
 * <pre>
 *     VIHFBase vihf = new VIHFImpl();
 *     vihf.setIssuer("issuer");
 *     vihf.setNameID("nameID");
 *
 *     // ...
 *
 *     vihf.build();
 *     vihf.sign();
 *
 *     Element vihfNode = vihf.getVIHF();
 * </pre>
 */
public abstract class VIHFBase {

    /**
     * VIHF auth context field
     */
    public enum AuthContext {
        TLS
    }

    /**
     * VIHF AuthentificationMode
     */
    public enum AuthentificationMode {
        DIRECT("DIRECTE"),
        INDIRECT("INDIRECTE"),
        AIR("INDIRECTE_RENFORCEE");

        private String fieldName;

        @Override
        public String toString(){
            return this.fieldName;
        }
        private AuthentificationMode(String fieldName){
            this.fieldName = fieldName;
        }
    }

    /**
     * Common VIHF attribute represented by :
     *  - code
     *  - codeSystem
     *  - codeSystemName
     *  - displayName
     */
    public static class CommonVIHFAttribute {
        public CommonVIHFAttribute(String code, String codeSystem, String codeSystemName, String displayName) {
            this.code = code;
            this.codeSystem = codeSystem;
            this.codeSystemName = codeSystemName;
            this.displayName = displayName;
        }

        public String code;
        public String codeSystem;
        public String codeSystemName;
        public String displayName;
    }

    String issuer;
    String nameID;
    AuthContext authContext;

    // List of simple attributes fields
    EnumMap<VIHFField, String> vihfSimpleAttributes = new EnumMap<>(VIHFField.class);

    List<CommonVIHFAttribute> roles = new ArrayList<>();
    CommonVIHFAttribute purposeOfUse;


    /**
     * @see #setIssuer(String)
     * @return Issuer field
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * {@code <saml2:Issuer Format="urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName">issuer</saml2:Issuer> }
     * @param issuer Issuer
     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * @see #setNameID(String)
     * @return Name ID field
     */
    public String getNameID() {
        return nameID;
    }

    /**
     * <pre>{@code
     * <saml2:Subject>
     *  <saml2:NameID>nameID</saml2:NameID>
     * </saml2:Subject>
     * }</pre>
     * @param nameID Name ID
     */
    public void setNameID(String nameID) {
        this.nameID = nameID;
    }

    /**
     * @see #setAuthContext(AuthContext)
     * @return AuthContext field
     */
    public AuthContext getAuthContext() {
        return authContext;
    }

    /**
     * <pre>{@code
     * <saml2:AuthnContext>
     * <saml2:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:{authContext}</saml2:AuthnContextClassRef>
     * </saml2:AuthnContext>
     * }</pre>
     * @param authContext Auth context enum that will be converted to the urn:oasis:names:tc:SAML:2.0:ac:classes enum
     */
    public void setAuthContext(AuthContext authContext) {
        this.authContext = authContext;
    }

    /**
     * Add a new {@code <saml2:AttributeValue>} in : {@code <saml2:Attribute Name="urn:oasis:names:tc:xacml:2.0:subject:role">}
     *
     * <pre>{@code
     * <saml2:Attribute Name="urn:oasis:names:tc:xacml:2.0:subject:role">
     * <saml2:AttributeValue>
     *  <Role xmlns="urn:hl7-org:v3" code="x" codeSystem="x" codeSystemName="x" displayName="x" xsi:type="CE"/>
     * </saml2:AttributeValue>
     * </saml2:Attribute>
     * }</pre>
     * @param attr Role to add
     */
    public void addRole(CommonVIHFAttribute attr) {
        roles.add(attr);
    }

    /**
     * @return Get a list of all roles
     */
    public List<CommonVIHFAttribute> getRoles() {
        return roles;
    }


    public CommonVIHFAttribute getPurposeOfUse() {
        return purposeOfUse;
    }

    /**
     * <pre>{@code
     * <saml2:Attribute Name="urn:oasis:names:tc:xspa:1.0:subject:purposeofuse">
     * <saml2:AttributeValue>
     * <PurposeOfUse xmlns="urn:hl7-org:v3" code="x" codeSystem="x" codeSystemName="x" displayName="x" xsi:type="CE"/>
     * </saml2:AttributeValue>
     * </saml2:Attribute>
     * }</pre>
     * @param purposeOfUse Set purpose of use field with given attributes
     */
    public void setPurposeOfUse(CommonVIHFAttribute purposeOfUse) {
        this.purposeOfUse = purposeOfUse;
    }

    /**
     * Add Attribute node with given field name and value :
     *
     * <pre>{@code
     * <saml2:Attribute Name="field">
     * <saml2:AttributeValue>value</saml2:AttributeValue>
     * </saml2:Attribute>
     * }</pre>
     *
     * @param field VIHF field name
     * @param value Field value
     */
    public void setSimpleAttribute(VIHFField field, String value) {
        vihfSimpleAttributes.put(field, value);
    }


    /**
     * Sign the VIHF. Must be implemented in subclass.
     */
    public abstract void sign();

    /**
     * Build the VIHF with the current informations.
     * @return True if success, false otherwise
     */
    public abstract Boolean build();

    /**
     * Get VIHF root element
     * @implNote The returned element might be NULL if you don't call build beforehand
     * @return VIHF root element
     */
    public abstract Element getVIHF();

    /**
     * Export current representation of the VIHF to an xml file.
     * Used for debug purposes.
     * @param fileName XML file name
     * @implNote This will do nothing if the VIHF is not built
     */
    public void exportVIHFToXML(String fileName) {
        try {
            Element element = getVIHF();

            StringWriter writer = new StringWriter();

            // An XML External Entity or XSLT External Entity (XXE) vulnerability can occur when a
            // javax.xml.transform.Transformer is created without enabling "Secure Processing" or when one is created without disabling external DTDs.
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            Transformer transformer = factory.newTransformer();

            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(element), new StreamResult(writer));

            PrintWriter printWriter = new PrintWriter(fileName);
            Objects.requireNonNull(printWriter).println(writer.toString());
            printWriter.close();
        } catch (Exception e) {
            Log.info(e.getMessage());
        }
    }


}
