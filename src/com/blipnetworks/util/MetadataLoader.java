/*
 * @(#)MetadataLoader.java
 *
 * Copyright (c) 2005-2009 by Blip Networks, Inc.
 * 407 Broome St., 5th Floor
 * New York, NY 10013
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Blip Networks, Inc.
 */

package com.blipnetworks.util;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.apache.commons.httpclient.Cookie;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;
import java.util.TreeMap;
import java.io.IOException;

/**
 * This class provides a single static method which loads metadata options from
 * Blip.tv. These options currently span:
 * <ul>
 * <li>Content categories (i.e. Music and Entertainment)</li>
 * <li>Content licenses (i.e. Creative Commons)</li>
 * <li>Blogs (i.e. for cross-posting)</li>
 * <li>Spoken languages (i.e. Danish)</li>
 * <li>Content ratings (i.e. TV MA)</li>
 * <li>Cross-upload destinations (i.e. the Internet Archive)</li>
 * </ul>
 * The metadata takes of the form of a name and an associated numerical ID,
 * which are stored in instances of <code>java.util.TreeMap</code> as static
 * class members.
 *
 *
 * @author Jared Klett
 * @version $Id: MetadataLoader.java,v 1.7 2009/06/13 21:35:48 dsk Exp $
 */

public class MetadataLoader {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.7 $";

// Constants //////////////////////////////////////////////////////////////////

    private static final String CATEGORY_TAG = "category";
    private static final String LICENSE_TAG = "license";
    private static final String BLOG_TAG = "blog";
    private static final String LANGUAGE_TAG = "language";
    private static final String RATING_TAG = "rating";
    private static final String XUPLOADS_TAG = "crossupload";
    private static final String	CONVERSION_TAG = "conversiontarget";
    private static final String	PRIVACY_TAG = "hidden";

    protected static final String ID_KEY = "id";
    protected static final String NAME_KEY = "name";

    private static final String	NONE_TEXT = "general.none.text";
    
// Class variables ////////////////////////////////////////////////////////////

    /** A map of content license names to numerical IDs. */
    public static Map<String, String> licenses;
    /** A map of category names to numerical IDs. */
    public static Map<String, String> categories;
    /** A map of cross-post-able blog names to numerical IDs. */
    public static Map<String, String> blogs;
    /** A map of spoken language names to numerical IDs. */
    public static Map<String, String> languages;
    /** A map of content rating names to numerical IDs. */
    public static Map<String, String> ratings;
    /** A map of cross-upload destination names to numerical IDs. */
    public static Map<String, String> crossuploads;
    
    public static Map<String, String>	conversionTargets;
    public static Map<String, String>	privacySettings;

// Constructor ////////////////////////////////////////////////////////////////

    private MetadataLoader() {
        // should never be called outside
    }

// Class methods //////////////////////////////////////////////////////////////

    /**
     * Loads the metadata from the server and populates the publicly-accessible
     * maps.
     * Requires a valid cookie for authentication.
     * @param authCookie The authentication cookie.
     * @throws IOException If an error occurs while talking to the server.
     * @throws ParserConfigurationException If we can't create an XML parser.
     * @throws SAXException If an error occurs while parsing the XML response.
     */
    public synchronized static void load(Cookie authCookie) throws IOException, ParserConfigurationException, SAXException {
        String baseURL = Parameters.config.getProperty(Parameters.BASE_URL, Parameters.BASE_URL_DEF);
        String metaURI = Parameters.config.getProperty(Parameters.META_URI, Parameters.META_URI_DEF);
        String url = baseURL + metaURI;
        // URL will be checked by XMLUtils, so just check the cookie
        if (authCookie == null) {
            throw new IllegalArgumentException("Cookie cannot be null");
        }
        
        licenses = new TreeMap<String, String>();
        categories = new TreeMap<String, String>();
        blogs = new TreeMap<String, String>();
        languages = new TreeMap<String, String>();
        ratings = new TreeMap<String, String>();
        
        ratings.put(I18n.getString(NONE_TEXT), "");
        crossuploads = new TreeMap<String, String>();
        conversionTargets = new TreeMap<String, String>();
        privacySettings = new TreeMap<String, String>();
        
        Document document = XmlUtils.loadDocumentFromURL(url, authCookie);
        if (document != null) {
            addToMap(document, CATEGORY_TAG, categories);
            addToMap(document, LICENSE_TAG, licenses);
            addToMap(document, BLOG_TAG, blogs);
            addToMap(document, LANGUAGE_TAG, languages);
            addToMap(document, RATING_TAG, ratings);
            addToMap(document, XUPLOADS_TAG, crossuploads);
            addToMap(document, CONVERSION_TAG, conversionTargets);
            addToMap(document, PRIVACY_TAG, privacySettings);
        }
    }

    private static void addToMap(Document document, String tag, Map<String, String> map) {
        NodeList nodes = document.getElementsByTagName(tag);
        for (int i = 0; i < nodes.getLength(); i++) {
            NodeList children = nodes.item(i).getChildNodes();
            String id = null;
            String name = null;
            for (int x = 0; x < children.getLength(); x++) {
                Node node = children.item(x);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String content = node.getFirstChild().getNodeValue();
                    if (children.item(x).getNodeName().equals(ID_KEY)) {
                        id = content;
                    }
                    else if (children.item(x).getNodeName().equals(NAME_KEY)) {
                        name = content;
                    }
                }
            }
            map.put(name, id);
        }
    }

} // class MetadataLoader
