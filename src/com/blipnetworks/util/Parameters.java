/*
 * @(#)Parameters.java
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

import org.apache.commons.httpclient.methods.multipart.StringPart;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

/**
 * Just a class to hold static strings that are used in multiple classes
 * outside this one.
 *
 * @author Jared Klett
 * @version $Id: Parameters.java,v 1.3 2006/12/08 23:16:48 jklett Exp $
 */

public class Parameters {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.3 $";

// Constants //////////////////////////////////////////////////////////////////

    /** The hash key to the file parameter. */
    public static final String FILE_PARAM_KEY = "file";
    /** The hash key to the file parameter. */
    public static final String THUMB_PARAM_KEY = "thumbnail";
    /** The hash key to the title parameter. */
    public static final String TITLE_PARAM_KEY = "title";
    /** The hash key to the post ID parameter. */
    public static final String POST_PARAM_KEY = "post";
    /** The hash key to the license parameter. */
    public static final String LICENSE_PARAM_KEY = "license";
    /** The hash key to the category ID parameter. */
    public static final String CAT_PARAM_KEY = "categories_id";
    /** The hash key to the topics parameter. */
    public static final String TAGS_PARAM_KEY = "topics";
    /** The hash key to the username parameter. */
    public static final String USER_PARAM_KEY = "userlogin";
    /** The hash key to the password parameter. */
    public static final String PASS_PARAM_KEY = "password";
    /** The hash key to the password parameter. */
    public static final String WEAK_PASS_PARAM_KEY = "lowpassword";
    /** The hash key to the skin parameter. */
    public static final String SKIN_PARAM_KEY = "skin";
    /** The hash key to the description parameter. */
    public static final String DESC_PARAM_KEY = "description";
    /** The hash key to the form cookie GUID parameter. */
    public static final String GUID_PARAM_KEY = "form_cookie";
    /** The hash key to the ingest method parameter. */
    public static final String INGEST_PARAM_KEY = "ingest_method";
    /** The hash key to the cross-post parameter. */
    public static final String CROSSPOST_PARAM_KEY = "crosspost";
    /** The hash key to the IA cross-upload parameter. */
    public static final String IA_PARAM_KEY = "crossupload_archiveorg";

    /** Default: the title of the post, if none is supplied. */
    public static final String TITLE_PARAM_DEF = "Working title";
    /** Default: the post ID of the post, if none is supplied. */
    public static final String POST_PARAM_DEF = "1";
    /** Default: the license ID of the post, if none is supplied. */
    public static final String LICENSE_PARAM_DEF = "-1";
    /** Default: the topics for the post, if none is supplied. */
    public static final String TAGS_PARAM_DEF = "";
    /** Default: the category ID of the post, if none is supplied. */
    public static final String CAT_PARAM_DEF = "-1";
    /** Default: the user login - this should be supplied. */
    public static final String USER_PARAM_DEF = "nobody";
    /** Default: the password - this should be supplied. */
    public static final String PASS_PARAM_DEF = "nopass";
    /** Default: the skin for the response, if none is supplied. */
    public static final String SKIN_PARAM_DEF = "xmlhttprequest";
    /** Default: the description of the post - this should be supplied. */
    public static final String DESC_PARAM_DEF = "";
    /** Default: the ingest method - this should be supplied. */
    public static final String INGEST_PARAM_DEF = "bliplib";

    private static Map defaultMap;

    static {
        defaultMap = new HashMap();
        defaultMap.put(TITLE_PARAM_KEY, TITLE_PARAM_DEF);
        defaultMap.put(POST_PARAM_KEY, POST_PARAM_DEF);
        defaultMap.put(LICENSE_PARAM_KEY, LICENSE_PARAM_DEF);
        defaultMap.put(TAGS_PARAM_KEY, TAGS_PARAM_DEF);
        defaultMap.put(CAT_PARAM_KEY, CAT_PARAM_DEF);
//        defaultMap.put(USER_PARAM_KEY, USER_PARAM_DEF);
//        defaultMap.put(PASS_PARAM_KEY, PASS_PARAM_DEF);
        defaultMap.put(SKIN_PARAM_KEY, SKIN_PARAM_DEF);
        defaultMap.put(DESC_PARAM_KEY, DESC_PARAM_DEF);
        defaultMap.put(INGEST_PARAM_KEY, INGEST_PARAM_DEF);
    }

    /**
     * Returns an object suitable for use
     * @param parameters
     * @param key
     * @return A <code>StringPart</code> object.
     */
    public static StringPart getStringPart(Properties parameters, String key) {
        String defaultValue;
        Object value = defaultMap.get(key);
        if (value == null)
            throw new IllegalArgumentException("No default value available for key: " + key);
        defaultValue = (String)value;
        return new StringPart(key, parameters.getProperty(key, defaultValue));
    }

} // class Parameters
