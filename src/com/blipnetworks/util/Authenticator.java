/*
 * @(#)Authenticator.java
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

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;

/**
 * This class provides a method of submitting a username and password to
 * Blip.tv and get an authentication cookie back in the response.
 *
 * @author Jared Klett
 * @version $Id: Authenticator.java,v 1.6 2009/06/13 21:35:48 dsk Exp $
 */

public class Authenticator {

// CVS info ///////////////////////////////////////////////////////////////////

    public static final String CVS_REV = "$Revision: 1.6 $";

// Constants //////////////////////////////////////////////////////////////////

    private static final String EMPTY_STRING = "";

// Constructors ///////////////////////////////////////////////////////////////

    private Authenticator() {
        // will never be called
    }

// Class methods //////////////////////////////////////////////////////////////

    /**
     * Sends an authentication request to Blip.tv with the passed username and
     * password. If the request is granted, an authentication cookie will be
     * issued.
     *
     * @param username The username to be used.
     * @param password The password to be used.
     * @return A cookie that can be used to authenticate the user.
     * @throws HttpException If an error occurs while talking to the server.
     * @throws IOException If an error occurs while talking to the server.
     * @throws IllegalArgumentException If bad username/password is passed in.
     * @throws IllegalStateException If we get a bad response from the server.
     */
    public static Cookie authenticate(String username, String password) throws HttpException, IOException {
        if (username == null || password == null)
            throw new IllegalArgumentException("Neither username nor password can be null");
        if (username.trim().equals(EMPTY_STRING) || password.trim().equals(EMPTY_STRING))
            throw new IllegalArgumentException("Neither username nor password can be empty");
        Cookie authCookie = null;
        String baseURL = Parameters.config.getProperty(Parameters.BASE_URL, Parameters.BASE_URL_DEF);
        String authURI = Parameters.config.getProperty(Parameters.AUTH_URI, Parameters.AUTH_URI_DEF);
        PostMethod post = new PostMethod(baseURL + authURI);
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
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(Uploader.AUTH_COOKIE_NAME)) {
                    authCookie = cookies[i];
                    break;
                }
            }
            // Check the HTTP response code
            boolean succeeded = responseCode < 400;
            if (!(succeeded && authCookie != null)) {
                throw new IllegalStateException("Received bad response from the server, HTTP response code " + responseCode);
            }
        }
        finally {
            post.releaseConnection();
        }
        return authCookie;
    }

} // class Authenticator
