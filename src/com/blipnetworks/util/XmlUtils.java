/*
 * @(#)XmlUtils.java
 *
 * Copyright (c) 2006-2007 by Blip Networks, Inc.
 * 239 Centre St, 3rd Floor
 * New York, NY 10013
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Blip Networks, Inc.
 */

package com.blipnetworks.util;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * This class provides a set of static utility methods for manipulating XML to
 * and from a variety of objects.
 *
 * @author Jared Klett
 * @version $Id: XmlUtils.java,v 1.6 2009/02/17 16:08:54 dsk Exp $
 */

public class XmlUtils {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.6 $";

// Constants //////////////////////////////////////////////////////////////////

    /** The number of milliseconds we will wait for a web server to respond. */
    public static final int TIMEOUT = 30000;
    /** Matches the dot-extension on an XML file, i.e. foo.xml */
    public static final String XML_TYPE = "xml";
    /** A string which specifies the UTF-8 character encoding */
    public static final String UTF8_TYPE = "UTF-8";
    /** A standard newline: backslash-n */
    public static final String NEWLINE = "\n";

// Constructor ////////////////////////////////////////////////////////////////

    private XmlUtils() {
        // should never be called outside
    }

// Class methods //////////////////////////////////////////////////////////////

    /**
     * Attempts to load an XML document from the passed URL.
     *
     * @param url The URL to load the document from.
     * @return A Document object, or null if a document could not be retrieved.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static Document loadDocumentFromURL(String url) throws IOException, ParserConfigurationException, SAXException {
        return loadDocumentFromURL(url, null);
    }

    /**
     * Attempts to load an XML document from the passed URL.
     *
     * @param url The URL to load the document from.
     * @param authCookie A cookie to be set in the HTTP request, usually for authentication.
     * @return A Document object, or null if a document could not be retrieved.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static Document loadDocumentFromURL(String url, Cookie authCookie) throws IOException, ParserConfigurationException, SAXException {
        return loadDocumentFromURL(url, authCookie, null);
    }

    /**
     * Attempts to load an XML document from the passed URL.
     *
     * @param url The URL to load the document from.
     * @param authCookie A cookie to be set in the HTTP request, usually for authentication.
     * @param userAgent A user-agent string to be set in the HTTP request.
     * @return A Document object, or null if a document could not be retrieved.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static Document loadDocumentFromURL(String url, Cookie authCookie, String userAgent) throws IOException, ParserConfigurationException, SAXException {
        // check the URL and throw a runtime exception if we fail
        try { new URL(url); } catch (MalformedURLException e) { throw new IllegalArgumentException("URL must be valid: " + e.getMessage()); }
        // okay, on with the show...
        Document document = null;
        GetMethod method = new GetMethod(url);
        try {
            HttpClient client = new HttpClient();
            // Set a tolerant cookie policy
            client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            if (userAgent != null)
                client.getParams().setParameter(HttpMethodParams.USER_AGENT, userAgent);
            // Set our timeout
            client.getHttpConnectionManager().getParams().setConnectionTimeout(TIMEOUT);
            if (authCookie != null)
                client.getState().addCookie(authCookie);
            int responseCode = client.executeMethod(method);
            if (responseCode == HttpStatus.SC_OK)
                document = loadDocumentFromInputStream(method.getResponseBodyAsStream());
            else
                throw new RuntimeException("Got bad response code from web server: " + responseCode);
        }
        finally {
            method.releaseConnection();
        }
        return document;
    }

    public static Document loadDocumentFromInputStream(InputStream stream) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return docBuilder.parse(stream);
    }

    /**
     * Parses an XML string into at DOM Document.
     *
     * @param xml The (presumably XML) string that's to be turned into a DOM document.
     * @return A W3C DOM document that holds the contents of the given XML.
     * @throws IOException
     */
    public static Document makeDocumentFromString(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    /**
     * Parses an (XML) File into a DOM document.
     *
     * @param file The (presumably XML) file that's to be turned into a DOM document.
     * @return a W3C DOM document that holds the contents of the given File
     * @throws IOException
     */
    public static Document makeDocumentFromFile(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return builder.parse(file);
    }

    /**
     * Parses an (XML) File into a DOM document.
     *
     * @param is The (presumably XML) file inputstream that's to be turned into a DOM document.
     * @return a W3C DOM document that holds the contents of the given File
     * @throws IOException
     */
    public static Document makeDocumentFromStream(InputStream is) throws ParserConfigurationException, SAXException, IOException {
    	DocumentBuilder	builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    	return builder.parse(is);
    }
    
    /**
     * Serializes the given W3C DOM document into an XML string.
     *
     * @param doc The W3C DOM document to be serialized.
     * @return A string representation of the DOM document's data in XML.
     * @throws IOException
     */
    public static String makeStringFromDocument(Document doc) throws IOException {
        StringWriter writer = new StringWriter();
        XMLSerializer output = new XMLSerializer(writer, new OutputFormat(XML_TYPE, UTF8_TYPE, true));

        output.serialize(doc);

        return writer.toString();
    }

    /**
     * Serializes the given W3C DOM document into a File.
     *
     * @param doc The W3C DOM document to be serialized.
     * @return A string representation of the DOM document's data in XML.
     * @throws IOException
     */
    public static File makeFileFromDocument(Document doc) throws IOException {
        OutputFormat format = new OutputFormat(doc, UTF8_TYPE, true);

        format.setIndenting(false);
        format.setLineWidth(0);
        format.setLineSeparator(NEWLINE);

        // TODO: redo this
        File file = File.createTempFile("tmp", ".xml");
        file.deleteOnExit();

        XMLSerializer serializer = new XMLSerializer(new FileOutputStream(file.getAbsolutePath()), format);
        serializer.asDOMSerializer();
        serializer.serialize(doc);

        return file;
    }

} // class XmlUtils
