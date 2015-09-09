/*
 * @(#) $Id: PlainPartExtractor.java,v 1.1.2.1 2004/09/29 00:57:59 
 * $Revision: 1.1.2.1 $
 * Copyright (c) 2000 Shin Kinoshita All Rights Reserved.
 */
package org.sxb.mail.fetch.impl.sk_jp;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;

/**
 * text/plainを結合した文字列を得るPartHandlerです。
 * 
 * @version $Revision: 1.1.2.1 $ $Date: 2004/09/29 00:57:59 $
 * @author Shin
 */
public class PlainPartExtractor implements PartHandler {

	private String text = null;

	public boolean processPart(Part part, ContentType context) throws MessagingException,
																IOException {
		if (!part.isMimeType("text/plain")) {
			return true;
		}
		if (text == null) {
			// 最初のテキストパートを無条件に抽出
			text = (String)MultipartUtility.getContent(part);
		} else {
			String disposition = part.getDisposition();
			if (disposition == null || disposition.equalsIgnoreCase(Part.INLINE)) {
				text += "\r\n\r\n-- inline --\r\n\r\n" + (String)MultipartUtility.getContent(part);
			}
		}
		return true;
	}

	public String getText() {
		return text;
	}

	public static void main(String[] args) throws Exception {
		MimeMessage msg = new MimeMessage(javax.mail.Session.getDefaultInstance(System
				.getProperties(), null), System.in);
		PlainPartExtractor h = new PlainPartExtractor();
		MultipartUtility.process(msg, h);

		System.out.println("This is the detected text/plain parts.");
		System.out.println(h.getText());
	}
}