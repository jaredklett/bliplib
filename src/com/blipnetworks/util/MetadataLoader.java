/*
 * @(#)MetadataLoader.java
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

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.apache.commons.httpclient.Cookie;
import org.apache.log4j.Logger;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;
import java.util.TreeMap;
import java.io.IOException;

/**
 * This is a placeholder description of this class.
 * TODO
 *
 * @author Jared Klett
 * @version $Id: MetadataLoader.java,v 1.1 2006/12/04 17:52:46 jklett Exp $
 */

public class MetadataLoader {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_ID = "$Id: MetadataLoader.java,v 1.1 2006/12/04 17:52:46 jklett Exp $";
    public static final String CVS_REV = "$Revision: 1.1 $";

// Constants //////////////////////////////////////////////////////////////////

    private static final String CATEGORY_TAG = "category";
    private static final String LICENSE_TAG = "license";
    private static final String BLOG_TAG = "blog";
    private static final String LANGUAGE_TAG = "language";
    private static final String RATING_TAG = "rating";
    private static final String XUPLOADS_TAG = "crossupload";

// Class variables ////////////////////////////////////////////////////////////

    /** Our logging facility. */
    private static Logger log = Logger.getLogger(MetadataLoader.class);
    /** TODO */
    public static Map licenses;
    /** TODO */
    public static Map categories;
    /** TODO */
    public static Map blogs;
    /** TODO */
    public static Map languages;
    /** TODO */
    public static Map ratings;
    /** TODO */
    public static Map crossuploads;

// Constructor ////////////////////////////////////////////////////////////////

    private MetadataLoader() {
        // should never be called outside
    }

// Class methods //////////////////////////////////////////////////////////////

    /**
     * TODO
     * @param url
     * @param authCookie
     */
    public synchronized static void load(String url, Cookie authCookie) {
        if (licenses == null && categories == null && blogs == null) {
            licenses = new TreeMap();
            categories = new TreeMap();
            blogs = new TreeMap();
            languages = new TreeMap();
            ratings = new TreeMap();
            // TODO: load this somehow?
            ratings.put("None", "");
            crossuploads = new TreeMap();
            Document document = null;
            try {
                document = XmlUtils.loadDocumentFromURL(url, authCookie);
            } catch (IOException e) {
                log.error("Got a general I/O error!", e);
            } catch (ParserConfigurationException e) {
                log.error("Could not configure XML parser!", e);
            } catch (SAXException e) {
                log.error("Got a general parsing error!", e);
            }
            if (document != null) {
                addToMap(document, CATEGORY_TAG, categories);
                addToMap(document, LICENSE_TAG, licenses);
                addToMap(document, BLOG_TAG, blogs);
                addToMap(document, LANGUAGE_TAG, languages);
                addToMap(document, RATING_TAG, ratings);
                addToMap(document, XUPLOADS_TAG, crossuploads);
            }
        }
    }

    private static void addToMap(Document document, String tag, Map map) {
        NodeList nodes = document.getElementsByTagName(tag);
        for (int i = 0; i < nodes.getLength(); i++) {
            NodeList children = nodes.item(i).getChildNodes();
            String id = null;
            String name = null;
            for (int x = 0; x < children.getLength(); x++) {
                Node node = children.item(x);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String content = node.getFirstChild().getNodeValue();
                    if (children.item(x).getNodeName().equals("id"))
                        id = content;
                    else if (children.item(x).getNodeName().equals("name"))
                        name = content;
                }
            }
            map.put(name, id);
        }
    }

} // class MetadataLoader
