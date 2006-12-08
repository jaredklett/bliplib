/*
 * @(#)Uploader.java
 *
 * Copyright (c) 2006 by Blip Networks, Inc.
 * 239 Centre St, 3rd Floor
 * New York, NY 10013
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Blip Networks, Inc.
 */

package com.blipnetworks.util;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.cookie.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A stateful class to handle uploads to Blip.
 *
 * @author Jared Klett
 * @version $Id: Uploader.java,v 1.2 2006/12/08 21:21:11 jklett Exp $
 */

public class Uploader {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.2 $";

// Constants //////////////////////////////////////////////////////////////////

    /** The name of the cookie that contains the authcode. */
    public static final String AUTH_COOKIE_NAME = "otter_auth";

    public static final int ERROR_UNKNOWN = 10;
    public static final int ERROR_BAD_AUTH = 11;
    public static final int ERROR_SERVER = 12;

// Class variables ////////////////////////////////////////////////////////////

    /** Our logging facility. */
    private static Logger log = Logger.getLogger(Uploader.class);

// Instance variables /////////////////////////////////////////////////////////

    private Cookie authCookie;
    private String url;
    private String urlWithGuid;
    private String postURL;
    private int timeout;
    private int errorCode;

// Constructor ////////////////////////////////////////////////////////////////

    public Uploader(String url) {
        this(url, 30000);
    }

    public Uploader(String url, int timeout) {
        // check the URL and throw a runtime exception if we fail
        try { new URL(url); } catch (MalformedURLException e) { throw new IllegalArgumentException("URL must be valid: " + e.getMessage()); }
        // okay, on with the show...
        this.url = url;
        this.timeout = timeout;
    }

// Instance methods ///////////////////////////////////////////////////////////

    public boolean uploadFile(File file, Properties parameters) {
        return uploadFile(file, null, parameters);
    }

    /**
     *
     */
    public boolean uploadFile(File videoFile, File thumbnailFile, Properties parameters) {
        return uploadFile(videoFile, thumbnailFile, parameters, null);
    }

    // TODO: break this beast up
    public boolean uploadFile(File videoFile, File thumbnailFile, Properties parameters, List crossposts) {
        PostMethod post = new PostMethod(urlWithGuid);
        FilePart videoFilePart;
        try {
            videoFilePart = new FilePart(Parameters.FILE_PARAM_KEY, videoFile);
        } catch (FileNotFoundException fnfe) {
            log.error("Could not locate file: " + videoFile, fnfe);
            return false;
        }

        FilePart thumbnailFilePart = null;
        if (thumbnailFile != null) {
            try {
                thumbnailFilePart = new FilePart(Parameters.THUMB_PARAM_KEY, thumbnailFile);
            } catch (FileNotFoundException fnfe) {
                log.error("Could not locate file: " + thumbnailFile, fnfe);
                return false;
            }
        }

        Part[] parts;
        Part[] typeArray = new Part[0];
        List list = new ArrayList();
        list.add(videoFilePart);
        if (thumbnailFilePart != null)
            list.add(thumbnailFilePart);
        list.add(new StringPart(Parameters.TITLE_PARAM_KEY, parameters.getProperty(Parameters.TITLE_PARAM_KEY, Parameters.TITLE_PARAM_DEF)));
        list.add(new StringPart(Parameters.POST_PARAM_KEY, parameters.getProperty(Parameters.POST_PARAM_KEY, Parameters.POST_PARAM_DEF)));
        list.add(new StringPart(Parameters.CAT_PARAM_KEY, parameters.getProperty(Parameters.CAT_PARAM_KEY, Parameters.CAT_PARAM_DEF)));
        list.add(new StringPart(Parameters.TAGS_PARAM_KEY, parameters.getProperty(Parameters.TAGS_PARAM_KEY, Parameters.TAGS_PARAM_DEF)));
        list.add(new StringPart(Parameters.LICENSE_PARAM_KEY, parameters.getProperty(Parameters.LICENSE_PARAM_KEY, Parameters.LICENSE_PARAM_DEF)));
        list.add(new StringPart(Parameters.SKIN_PARAM_KEY, parameters.getProperty(Parameters.SKIN_PARAM_KEY, Parameters.SKIN_PARAM_DEF)));
        list.add(new StringPart(Parameters.DESC_PARAM_KEY, parameters.getProperty(Parameters.DESC_PARAM_KEY, Parameters.DESC_PARAM_DEF)));
        list.add(new StringPart(Parameters.INGEST_PARAM_KEY, parameters.getProperty(Parameters.INGEST_PARAM_KEY, Parameters.INGEST_PARAM_DEF)));
        if (crossposts != null) {
            for (int i = 0; i < crossposts.size(); i++)
                list.add(new StringPart(Parameters.CROSSPOST_PARAM_KEY, (String)crossposts.get(i)));
        }
        String ia = parameters.getProperty(Parameters.IA_PARAM_KEY);
        if (ia != null)
            list.add(new StringPart(Parameters.IA_PARAM_KEY, ia));
        // We want to omit the un/pw parts if we have an auth cookie
        if (authCookie == null) {
            list.add(new StringPart(Parameters.USER_PARAM_KEY, parameters.getProperty(Parameters.USER_PARAM_KEY, Parameters.USER_PARAM_DEF)));
            list.add(new StringPart(Parameters.PASS_PARAM_KEY, parameters.getProperty(Parameters.PASS_PARAM_KEY, Parameters.PASS_PARAM_DEF)));
        }
        parts = (Part[])list.toArray(typeArray);

        post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

        boolean succeeded = false;

        try {
            HttpClient client = new HttpClient();
            // Set a tolerant cookie policy
            client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            // TODO FIXME
            client.getParams().setParameter(HttpMethodParams.USER_AGENT, "TODO FIXME");
            // Set our timeout
            client.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
            // If we had an auth cookie previously, set it in the client before
            // we send the request
            if (authCookie != null)
                client.getState().addCookie(authCookie);
            // Send the post request
            int responseCode = client.executeMethod(post);
            // Check for an authorization cookie in the response
            if (authCookie == null) {
                Cookie[] cookies = client.getState().getCookies();
                for (int i = 0; i < cookies.length; i++) {
                    if (cookies[i].getName().equals(AUTH_COOKIE_NAME)) {
                        authCookie = cookies[i];
                        break;
                    }
                }
            }

            // Check the HTTP response code
            succeeded = responseCode < 400;
            // Read the response
            InputStream responseStream = post.getResponseBodyAsStream();
            DocumentBuilder docBuilder;
            Document document = null;
            try {
                docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = docBuilder.parse(responseStream);
            } catch (ParserConfigurationException e) {
                log.error("Could not configure XML parser!", e);
            } catch (SAXException e) {
                log.error("Got a general parsing error!", e);
            }

            // agree on a proper schema for responses from Blip.
            if (responseCode == HttpStatus.SC_OK) {
                if (document != null) {
                    // attempt to discern the status from the respose
                    String responseText = document.getElementsByTagName("response").item(0).getFirstChild().getNodeValue();
                    String[] lines = responseText.trim().split("\n");
                    if (lines.length >= 2) {
                        try {
                            URL testURL = new URL(lines[1]);
                            postURL = testURL.toString();
                        } catch (Exception e) {
                            log.error("Couldn't find valid URL in response text:\n" + responseText, e);
                        }
                    }
                    if (responseText.indexOf("couldn't find an account") != -1)
                        errorCode = ERROR_BAD_AUTH;
                    return responseText.indexOf("has been successfully posted") != -1;
/*
                    "username and password combination";
                    "You must";
                    "critical error";
*/
                }
            } else {
                succeeded = false;
                errorCode = ERROR_SERVER;
            }
        }
        catch (HttpException e) {
            log.error("Got an error while making HTTP request!", e);
        }
        catch (IOException e) {
            log.error("Got a general I/O error!", e);
        }
        finally {
            post.releaseConnection();
        }
        return succeeded;
    } // method uploadFile

// Accessors //////////////////////////////////////////////////////////////////

    /** TODO */
    public int getErrorCode() {
        return errorCode;
    }

    /** TODO */
    public String getPostURL() {
        return postURL;
    }

// Mutators ///////////////////////////////////////////////////////////////////

    /** TODO */
    public void setGuid(String guid) {
        urlWithGuid = url + guid;
    }

    /** TODO */
    public void setAuthCookie(Cookie authCookie) {
        this.authCookie = authCookie;
    }

// Main method ////////////////////////////////////////////////////////////////

    /** TODO */
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java Uploader <url> <file> <user> <pass>");
            System.out.println("Optional parameters: <title> <desc>");
            return;
        }
        File f = new File(args[1]);
        Properties p = new Properties();
        p.put(Parameters.USER_PARAM_KEY, args[2]);
        p.put(Parameters.PASS_PARAM_KEY, args[3]);
        if (args.length > 4) {
            p.put(Parameters.TITLE_PARAM_KEY, args[4]);
            p.put(Parameters.DESC_PARAM_KEY, args[5]);
        }

        Uploader uploader = new Uploader(args[0]);
        uploader.uploadFile(f, p);
    }

} // class Uploader
