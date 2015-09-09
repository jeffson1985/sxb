/*
 * @(#) $Id: CorrectedContentTypeDataSource.java,v 1.1.2.2 2004/10/24 10:27:40 
 * $Revision: 1.1.2.2 $
 * Copyright (c) 2000 Shin Kinoshita All Rights Reserved.
 */
package org.sxb.mail.fetch.impl.sk_jp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import javax.mail.MessageAware;
import javax.mail.MessageContext;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

/**
 * Content-Type:の不適合をISO-2022-JPに補正します。
 * 使用方法は<PRE>
 * Object o = new DataHandler(
 *               new CorrectedContentTypeDataSource(part, charset)
 *            ).getContent();
 * </PRE><P>のようになります。</P><P>
 * スレッドセーフではありませんので利用者側で排他制御を行ってください。
 * </P>
 * @author Shin
 * @version $Revision: 1.1.2.2 $ $Date: 2004/10/24 10:27:40 $
 */
class CorrectedContentTypeDataSource implements DataSource, MessageAware {

	protected DataSource source;

	protected String defaultCharset;

	protected String forceCharset;

	public CorrectedContentTypeDataSource() {}

	public CorrectedContentTypeDataSource(DataSource dataSource, String defaultCharset) {
		setDataSource(dataSource);
		setDefaultCharset(defaultCharset);
	}

	public CorrectedContentTypeDataSource(Part part, String defaultCharset)
																			throws MessagingException {
		setPart(part);
		setDefaultCharset(defaultCharset);
	}

	public void setPart(Part part) throws MessagingException {
		// getDataHandler() method creates a implicit DataSource.
		setDataSource(part.getDataHandler().getDataSource());
	}

	public void setDataSource(DataSource newSource) {
		source = newSource;
	}

	public void setDefaultCharset(String defaultCharset) {
		this.defaultCharset = defaultCharset;
	}

	/**
	 * 指定された文字コードで既存の文字コードを上書きします。
	 * 
	 * @param forceCharset 強制的に適用する文字コード
	 * @author Tomohiro Otsuka
	 */
	public void setForceCharset(String forceCharset) {
		this.forceCharset = forceCharset;
	}

	public String getContentType() {
		ContentType contentType = null;
		try {
			contentType = new ContentType(source.getContentType());
		} catch (ParseException e) {
			return "text/plain; charset=" + defaultCharset;
		}
		String specifiedCharset = contentType.getParameter("charset");
		if (specifiedCharset == null) {
			// Content-Type:が存在しない場合は"text/plain"になってしまう。
			// 本当にtext/plainだった場合は正しくない事になるが、
			// charset=ISO-2022-JPにする場合は一応表示上は問題ない。
			contentType.setParameter("charset", defaultCharset);
		} else if (forceCharset != null) {
			contentType.setParameter("charset", forceCharset);
		}
		return contentType.toString();
	}

	public String getName() {
		return source.getName();
	}

	public InputStream getInputStream() throws IOException {
		return source.getInputStream();
	}

	public OutputStream getOutputStream() throws IOException {
		return source.getOutputStream();
	}

	public synchronized MessageContext getMessageContext() {
		if (source instanceof MessageAware) {
			return ((MessageAware)source).getMessageContext();
		}
		throw new RuntimeException(source + " isn't MessageAware.");
	}
}