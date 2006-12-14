/*
 * @(#)BlipLibSample.java
 *
 * Copyright (c) 1996-2006 by Blip Networks, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of javaforge.org nor the names of its contributors may
 *    be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import com.blipnetworks.util.*;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.Iterator;

import org.apache.commons.httpclient.Cookie;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * A simple class that demonstrates how to use BlipLib.
 *
 * @author Jared Klett
 * @version $Id: BlipLibSample.java,v 1.1 2006/12/14 18:01:32 jklett Exp $
 */

public class BlipLibSample {

    private Cookie authCookie;
    private String guid;

    public BlipLibSample(String username, String password) {
        try {
            authCookie = Authenticator.authenticate(username, password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getCategories() {
        try {
            // First we have to load the metadata from the server
            MetadataLoader.load(authCookie);
            // There is a map for each type of metadata
            // Let's look at categories, for example
            Set set = MetadataLoader.categories.keySet();
            Iterator i = set.iterator();
            while (i.hasNext()) {
                Object key = i.next();
                Object value = MetadataLoader.categories.get(key);
                // the mappings are name -> id
                System.out.println("Category name: " + key + ", category ID: " + value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public void checkUploadStatus() {
        try {
            // Create an instance of the class with the current GUID set in the uploader object
            UploadStatus status = UploadStatus.getStatus(guid, authCookie);
            // Now we can do some calculations based on the status information
            int remaining = status.getTotal() - status.getRead();
            System.out.println("There are " + remaining + " bytes left to be uploaded.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public void doUpload(File file, String title, String description) {
        // Create a properties object to hold the form data/metadata
        Properties props = new Properties();
        // Add the title
        props.put(Parameters.TITLE_PARAM_KEY, title);
        // Add the description
        props.put(Parameters.DESC_PARAM_KEY, description);
        // Create an uploader instance with our auth cookie
        Uploader uploader = new Uploader(authCookie);
        // Create a new random GUID
        guid = new RandomGUID().toString();
        // Set the GUID in the uploader
        uploader.setGuid(guid);
        // do it
        try {
            uploader.uploadFile(file, props);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

} // class BlipLib
