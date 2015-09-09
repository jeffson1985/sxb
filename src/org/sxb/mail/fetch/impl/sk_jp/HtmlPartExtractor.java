/*
 * @(#) $Id: HtmlPartExtractor.java,v 1.1.2.1 2004/09/29 00:57:59 
 * $Revision: 1.1.2.1 $
 * Copyright (c) 2000 Shin Kinoshita All Rights Reserved.
 */
package org.sxb.mail.fetch.impl.sk_jp;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentType;

/**
 * text/htmlを結合した文字列を得るPartHandlerです。
 * 
 * @version $Revision: 1.1.2.1 $ $Date: 2004/09/29 00:57:59 $
 * @author Shin
 */
public class HtmlPartExtractor implements PartHandler {

	private String html = null;

	public boolean processPart(Part part, ContentType context) throws MessagingException,
																IOException {
		if (!part.isMimeType("text/html")) {
			return true;
		}
		if (html == null) {
			// 最初のテキストパートを無条件に抽出
			html = (String)MultipartUtility.getContent(part);
		} else {
			String disposition = part.getDisposition();
			if (disposition == null || disposition.equalsIgnoreCase(Part.INLINE)) {
				html += "\r\n\r\n-- inline --\r\n\r\n" + (String)MultipartUtility.getContent(part);
			}
		}
		return true;
	}

	public String getHtml() {
		return html;
	}

}