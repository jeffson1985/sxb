/*
 * @(#) $Id: FirstPlainPartExtractor.java,v 1.1.2.1 2004/09/29 00:57:59 
 * $Revision: 1.1.2.1 $
 * Copyright (c) 2000 Shin Kinoshita All Rights Reserved.
 */
package org.sxb.mail.fetch.impl.sk_jp;

import java.io.IOException;
import javax.mail.Part;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;

/**
 * 最初に見つけたtext/plainパートの本文を得るPartHandlerです。
 * <P>
 * </P>
 * @version $Revision: 1.1.2.1 $ $Date: 2004/09/29 00:57:59 $
 * @author Shin
 */
public class FirstPlainPartExtractor implements PartHandler {

	private String text = null;

	public boolean processPart(Part part, ContentType context) throws MessagingException,
																IOException {
		String type = part.getContentType();
		// Bug fixed. Thx > ei
		// http://www.sk-jp.com/cgi-bin/treebbs.cgi?kako=1&all=1292&s=1292
		if (!part.isMimeType("text/plain") && type != null && !type.trim().equalsIgnoreCase("text")) {
			return true;
		}
		text = (String)MultipartUtility.getContent(part);
		return false;
	}

	public String getText() {
		return text;
	}

	public static void main(String[] args) throws Exception {
		javax.mail.internet.MimeMessage msg = new javax.mail.internet.MimeMessage(
				javax.mail.Session.getDefaultInstance(System.getProperties(), null), System.in);
		FirstPlainPartExtractor h = new FirstPlainPartExtractor();
		MultipartUtility.process(msg, h);

		System.out.println("This is the first detected text/plain part.");
		System.out.println(h.getText());
	}
}