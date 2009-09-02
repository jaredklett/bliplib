/*
 * @(#)MetadataLoader.java
 *
 * Copyright (c) 2005-2009 by Blip Networks, Inc.
 * 187 Lafayette St, 6th Floor
 * New York, NY 10013
 * All rights reserved.
 *
 * This software is the confidential and
 * proprietary information of Blip Networks, Inc.
 */
 
package com.blipnetworks.util;

/**
 * Local exception class to handle no form data received from blip.tv
 *
 * @author Donald Klett
 * @version $Id: NoDataLoadedFromHostException.java,v 1.1 2009/09/02 14:13:45 dsk Exp $
 */

@SuppressWarnings("serial")
public class NoDataLoadedFromHostException extends Exception {
	
	public NoDataLoadedFromHostException(String message) {
		super(message);
	}

}
