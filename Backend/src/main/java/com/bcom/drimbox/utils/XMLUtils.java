/*
 *  XMLUtils.java - DRIMBox
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

package com.bcom.drimbox.utils;

import io.quarkus.logging.Log;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class XMLUtils {

    /**
     * Export given node to a file
     *
     * @param node     Node to export
     * @param fileName file name
     * @return true if success, false otherwise
     */
    public static boolean exportXMLToFile(Node node, String fileName) {
        try {
            StringWriter writer = getStringWriter(node);

            PrintWriter printWriter = new PrintWriter(fileName, StandardCharsets.UTF_8);
            Objects.requireNonNull(printWriter).println(writer);
            printWriter.close();
        } catch (Exception e) {
            Log.info(e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Get XML node representation to String
     *
     * @param node
     * @return
     */
    public static String xmlToString(Node node) {
        try {
            StringWriter writer = getStringWriter(node);

            return writer.toString();

        } catch (Exception e) {
            Log.info(e.getMessage());
            return "";
        }
    }

    private static StringWriter getStringWriter(Node node) throws TransformerException {
        StringWriter writer = new StringWriter();

        // An XML External Entity or XSLT External Entity (XXE) vulnerability can occur when a
        // javax.xml.transform.Transformer is created without enabling "Secure Processing" or when one is created without disabling external DTDs.
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = factory.newTransformer();

        transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.toString());
        //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, ""); // Causes to add new line after prolog
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer;

//        Document doc = node.getOwnerDocument();
//        if (doc == null) {
//            doc = (Document) node;
//        }
//
//        DOMImplementationLS domImp = (DOMImplementationLS) doc.getImplementation();
//        LSOutput lsOutput = domImp.createLSOutput();
//        lsOutput.setEncoding("UTF-8");
//
//        LSSerializer ser = domImp.createLSSerializer();
//        ser.getDomConfig().setParameter("discard-default-content", false);
//        ser.getDomConfig().setParameter("format-pretty-print", true);
//        Log.info(ser.getDomConfig());
//
//        StringWriter stringWriter = new StringWriter();
//        lsOutput.setCharacterStream(stringWriter);
//        ser.write(doc, lsOutput);
//
//        return stringWriter;
    }
}


