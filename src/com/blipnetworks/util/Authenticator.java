/*
 * @(#)Authenticator.java
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

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * TODO
 *
 * @author Jared Klett
 * @version $Id: Authenticator.java,v 1.1 2006/12/04 17:52:46 jklett Exp $
 */

public class Authenticator {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.1 $";

// Constants //////////////////////////////////////////////////////////////////

    /** TODO */
    public static final String PROPERTY_LOGIN_URI = "login.uri";

// Class variables ////////////////////////////////////////////////////////////

    /** Our logging facility. */
    private static Logger log = Logger.getLogger(Authenticator.class);

    /** TODO */
    public static Cookie authCookie;

// Constructors ///////////////////////////////////////////////////////////////

    private Authenticator() {
        // will never be called
    }

// Class methods //////////////////////////////////////////////////////////////

    /**
     *
     * @param username
     * @param password
     * @return True we got an auth cookie, false otherwise.
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     */
    public static boolean authenticate(String username, String password) {
        if (username == null || password == null)
            throw new IllegalArgumentException("Neither username nor password can be null");
        boolean okay = false;
        // TODO FIXME
        String url = "";
        String uri = "";
        PostMethod post = new PostMethod(url + uri);
        NameValuePair[] nvp = {
                new NameValuePair(Parameters.USER_PARAM_KEY, username),
                new NameValuePair(Parameters.PASS_PARAM_KEY, password),
                new NameValuePair(Parameters.SKIN_PARAM_KEY, Parameters.SKIN_PARAM_DEF)
        };
        post.setRequestBody(nvp);
        try {
            HttpClient client = new HttpClient();
            // Set a tolerant cookie policy
            client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            // Set our timeout
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            // If we had an auth cookie previously, set it in the client before
            // we send the request
            // Send the post request
            int responseCode = client.executeMethod(post);
            // Check for an authorization cookie in the response
            Cookie[] cookies = client.getState().getCookies();
            Cookie myCookie = null;
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(Uploader.AUTH_COOKIE_NAME)) {
                    myCookie = cookies[i];
                    break;
                }
            }
            // Check the HTTP response code
            boolean succeeded = responseCode < 400;
            if (succeeded && myCookie != null) {
                authCookie = myCookie;
                okay = true;
            } else {
                // TODO: error message here
                throw new IllegalStateException("Received bad response from the server, HTTP response code " + responseCode);
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
        return okay;
    }

} // class Authenticator
