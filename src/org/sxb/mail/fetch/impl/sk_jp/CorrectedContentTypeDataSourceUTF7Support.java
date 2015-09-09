/*
 * @(#) $Id: CorrectedContentTypeDataSourceUTF7Support.java,v 1.1.2.1 2004/09/29 00:57:59 
 * $Revision: 1.1.2.1 $
 * Copyright (c) 2000 Shin Kinoshita All Rights Reserved.
 */
package org.sxb.mail.fetch.impl.sk_jp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataSource;
import javax.mail.MessageAware;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.ParseException;

import org.sxb.mail.fetch.impl.sk_jp.io.ByteToCharUTF7;

/**
 * Content-Type:の不適合をISO-2022-JPに補正します。
 * さらにcharset=UTF-7の場合にUTF-16のストリームに変換してgetContent()を
 * 無理やり成功させます。<BR>
 * また、未知のTES(Content-Transfer-Encoding:)だった場合に、"7bit"
 * と見なしてボディを取得します。
 * 使用方法は<PRE>
 * Object o = new DataHandler(
 *               new CorrectedContentTypeDataSourceUTF7Support(part, charset)
 *            ).getContent();
 * </PRE><P>のようになります。</P><P>
 * スレッドセーフではありませんので利用者側で排他制御を行ってください。
 * </P>
 * @author Shin
 * @version $Revision: 1.1.2.1 $ $Date: 2004/09/29 00:57:59 $
 */
class CorrectedContentTypeDataSourceUTF7Support extends CorrectedContentTypeDataSource {

	private boolean utf7 = false;

	public CorrectedContentTypeDataSourceUTF7Support() {}

	public CorrectedContentTypeDataSourceUTF7Support(DataSource dataSource, String defaultCharset) {
		super(dataSource, defaultCharset);
	}

	public CorrectedContentTypeDataSourceUTF7Support(Part part, String defaultCharset)
																						throws MessagingException {
		super(part, defaultCharset);
	}

	public void setDataSource(DataSource newSource) {
		super.setDataSource(newSource);
		utf7 = false;
	}

	public void setDefaultCharset(String defaultCharset) {
		super.setDefaultCharset(defaultCharset);
		utf7 = false;
	}

	public String getContentType() {
		try {
			ContentType contentType = new ContentType(super.getContentType());
			String specifiedCharset = contentType.getParameter("charset");
			if ("UTF-7".equalsIgnoreCase(specifiedCharset)) {
				// UTF-7コンバータが存在しない為、
				// 独自フィルタストリームを用いる。
				contentType.setParameter("charset", "UTF-16");
				utf7 = true;
			}
			return contentType.toString();
		} catch (ParseException e) {
			throw new InternalError();
		}
	}

	public InputStream getInputStream() throws IOException {
		InputStream in = null;
		if (isInvalidEncodingAsMultipart()) {
			// multipart/*でありながら、不正なTransfer-Encodingだった場合
			// 2001/09/01 JPhone(SH07)の送信する画像付きメイルが、
			// Content-Type: multipart/mixed
			// Content-Transfer-Encoding: base64
			// 等というメッセージを送る場合があり、JavaMailが
			// これをデコードできない問題を回避。
			// multipart/*の場合のContent-Transfer-Encodingは、
			// "7bit""8bit""binary"に限られる。
			// それ以外の場合は生ストリームを返すようにしておく。
			in = getRawInputStream();
		}
		if (in == null) {
			try {
				in = super.getInputStream();
			} catch (IOException e) {
				// ここでのIOExceptionはエンコーディング不良の可能性が高い。
				// 生InputStreamを得てリトライ
				in = getRawInputStream();
				if (in == null)
					throw e;
			}
		}
		if (!utf7) {
			return in;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int c;

		while ((c = in.read()) != -1) {
			out.write(c);
		}

		ByteToCharUTF7 btc = new ByteToCharUTF7();
		byte[] bytes = out.toByteArray();
		char[] chars = new char[bytes.length];

		// Bug fixed. Thanx to MOHI.
		// http://www.sk-jp.com/cgi-bin/treebbs.cgi?all=1220&s=1220
		int len = btc.convert(bytes, 0, bytes.length, chars, 0, chars.length);
		char[] w = new char[len];
		System.arraycopy(chars, 0, w, 0, len);
		String string = new String(w);
		return new ByteArrayInputStream(string.getBytes("UTF-16"));
	}

	// Transfer-Encodingにしたがったデコードを行う前のストリームを得ます。
	// sourceがMessageAwareでない場合はnullが返されます。
	private InputStream getRawInputStream() throws IOException {
		if (!(source instanceof MessageAware)) {
			return null;
		}
		Part part = ((MessageAware)source).getMessageContext().getPart();
		try {
			if (part instanceof MimeMessage) {
				return ((MimeMessage)part).getRawInputStream();
			} else if (part instanceof MimeBodyPart) {
				return ((MimeBodyPart)part).getRawInputStream();
			} else {
				return null;
			}
		} catch (MessagingException mex) {
			throw new IOException(mex.toString());
		}
	}

	// 不正なContent-Transfer-Encodingの場合にtrueを返します。
	private boolean isInvalidEncodingAsMultipart() {
		try {
			if (!new ContentType(getContentType()).match("multipart/*")) {
				return false;
			}
			if (!(source instanceof MessageAware)) {
				return false;
			}
			Part part = ((MessageAware)source).getMessageContext().getPart();
			String encoding = ((javax.mail.internet.MimePart)part).getEncoding();
			if ("7bit".equalsIgnoreCase(encoding) || "8bit".equalsIgnoreCase(encoding)
					|| "binary".equalsIgnoreCase(encoding)) {
				return false;
			}
		} catch (Exception e) {
			// この場合も不正だ、と。
		}
		return true;
	}

}