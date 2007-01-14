/*
 * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
 * This is a java.net project, see https://jets3t.dev.java.net/
 * 
 * Copyright 2006 James Murty
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.jets3t.apps.uploader;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.jets3t.service.utils.ServiceUtils;
import org.jets3t.service.utils.gatekeeper.SignatureRequest;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Generates XML documents containing metadata about files uploaded to Amazon S3 
 * by the Uploader. The XML document includes metadata for user inputs, 
 * inputs sourced from applet parameter tags, and additional information 
 * available from the uploader such as filenames and generated UUID.
 * <p>
 * XML documents produced by this class have the following format:<br>
 * <tt>
 * &ltUploader uploadDate="2006-11-07T10:37:53.077Z" version="1.0">
 * &ltProperty name="AspectRatio" source="userinput">&lt![CDATA[4:3]]>&lt/Property>
 * &ltProperty name="title" source="parameter">&lt![CDATA[A great title]]>&lt/Property>
 * &ltProperty name="originalFilename" source="uploader">&lt![CDATA[jug-asl-2.0.0 copy.jar.avi]]>&lt/Property>
 * &ltProperty name="uploaderUUID" source="uploader">&lt![CDATA[c759568f-f238-3972-82fd-f07ed7baa400]]>&lt/Property>
 * &lt/Uploader>
 * </tt>
 *   
 * @author James Murty
 */
public class XmlGenerator {
    public static final String xmlVersionNumber = "1.0";
    
    private List objectRequestList = new ArrayList();
    private Map applicationProperties = new HashMap();
    private Map messageProperties = new HashMap();
    
    public void addObjectRequestDetails(String key, String bucketName, Map metadata, 
        SignatureRequest signatureRequest) 
    {
        objectRequestList.add(new ObjectRequestDetails(key, bucketName, metadata, signatureRequest));
    }
    
    public void addApplicationProperties(Map properties) {
        applicationProperties.putAll(properties);
    }
    
    public void addMessageProperties(Map properties) {
        messageProperties.putAll(properties);
    }
    
    private class ObjectRequestDetails {
        public String key = null;
        public String bucketName = null;        
        public Map metadata = null;
        public SignatureRequest signatureRequest = null;
        
        public ObjectRequestDetails(String key, String bucketName, Map metadata, 
            SignatureRequest signatureRequest) 
        {
            this.key = key;
            this.bucketName = bucketName;
            this.metadata = metadata;
            this.signatureRequest = signatureRequest;
        }
    }

    /**
     * Generates an XML document containing metadata information as Property elements.
     * The root of the document is the element Uploader.
     * 
     * @return
     * an XML document string containing Property elements.
     * 
     * @throws Exception
     */
    public String generateXml() throws Exception
    {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        
        Document document = builder.newDocument();
        Element rootElem = document.createElement("Uploader");
        document.appendChild(rootElem);
        rootElem.setAttribute("version", xmlVersionNumber);
        rootElem.setAttribute("uploadDate",
            ServiceUtils.formatIso8601Date(new Date()));
        
        // Add application properties (user inputs and application parameters) to XML document.
        for (Iterator iter = applicationProperties.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String propertyName = (String) entry.getKey();
            String propertyValue = (String) entry.getValue();            
            rootElem.appendChild(createPropertyElement(document, propertyName, propertyValue, "ApplicationProperty"));
        }

        // Add message properties (user inputs and application parameters) to XML document.
        for (Iterator iter = messageProperties.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String propertyName = (String) entry.getKey();
            String propertyValue = (String) entry.getValue();            
            rootElem.appendChild(createPropertyElement(document, propertyName, propertyValue, "MessageProperty"));
        }
        
        // Add Object request details to XML document.
        ObjectRequestDetails[] details = (ObjectRequestDetails[]) objectRequestList
            .toArray(new ObjectRequestDetails[objectRequestList.size()]);
        for (int i = 0; i < details.length; i++) {
            ObjectRequestDetails objectDetails = details[i];
            rootElem.appendChild(createSignatureRequestElement(document, objectDetails));
        }
        
        // Serialize XML document to String.
        OutputFormat outputFormat = new OutputFormat(document);
        outputFormat.setIndenting(true);
        StringWriter writer = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(writer, outputFormat);
        serializer.serialize(document);
        
        return writer.toString();
    }
    
    /**
     * Creates a Property XML element for a document.
     * 
     * @param document 
     * the document the property is being created for.
     * @param propertyName 
     * the property's name, becomes a <b>name</b> attribute of the element.
     * @param propertyValue 
     * the property's value, becomes the CDATA text value of the element. If this value
     * is null, the Property element is empty.
     * @param source 
     * text to describe the source of the information, such as userinput or parameter. 
     * Becomes a <b>source</b> attribute of the element.
     * 
     * @return
     * a Property element.
     */
    private Element createPropertyElement(
        Document document, String propertyName, String propertyValue, String source) 
    {
        Element propertyElem = document.createElement(source);
        if (propertyName != null) {
            propertyElem.setAttribute("name", propertyName);
        }
        if (propertyValue != null) {
            CDATASection cdataSection = document.createCDATASection(propertyValue);
            propertyElem.appendChild(cdataSection);
        }
        return propertyElem;        
    }
    
    private Element createSignatureRequestElement(Document document, ObjectRequestDetails details) {
        SignatureRequest request = details.signatureRequest;
        
        Element requestElem = document.createElement("SignatureRequest");
        requestElem.setAttribute("type", request.getSignatureType());
        requestElem.setAttribute("signed", String.valueOf(request.isSigned()));
        requestElem.appendChild(
            createObjectElement(document, details.key, details.bucketName, details.metadata, "RequestObject"));

        if (request.isSigned()) {
            requestElem.appendChild(
                createObjectElement(document, request.getObjectKey(), request.getBucketName(), 
                    request.getObjectMetadata(), "SignedObject"));
            requestElem.appendChild(
                createPropertyElement(document, null, request.getSignedUrl(), "SignedURL"));
        } else {
            requestElem.appendChild(
                createPropertyElement(document, null, request.getDeclineReason(), "DeclineReason"));            
        }
        return requestElem;        
    }
    
    private Element createObjectElement(Document document, String key, String bucketName, Map metadata, String elementName) {
        Element objectElement = document.createElement(elementName);
        objectElement.setAttribute("key", key);
        objectElement.setAttribute("bucketName", bucketName);
        Iterator iter = metadata.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String metadataName = (String) entry.getKey();
            String metadataValue = (String) entry.getValue();
            
            objectElement.appendChild(
                createPropertyElement(document, metadataName, metadataValue, "Metadata"));
        }
        return objectElement;
    }
    
}
