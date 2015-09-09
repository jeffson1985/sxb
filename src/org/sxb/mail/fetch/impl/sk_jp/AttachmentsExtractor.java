/*
 * @(#) $Id: AttachmentsExtractor.java,v 1.1.2.2 2005/09/25 12:51:38 
 * Copyright (c) 2000 Shin Kinoshita All Rights Reserved.
 */
package org.sxb.mail.fetch.impl.sk_jp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentType;

/**
 * 添付ファイルを抽出するPartHandlerです。
 * <p>
 * MultipartUtility#process()呼び出し後にgetFileNames()によって、 添付ファイル名の配列を得ることができます。
 * </p>
 * <p>
 * ファイル名配列のindexを指定してその添付ファイルに対する
 * InputStreamを得たり、渡されたOutputStreamに対して書き出すことができます。
 * </p>
 * @version $Revision: 1.1.2.2 $ $Date: 2005/09/25 12:51:38 $
 * @author Shin
 */
public class AttachmentsExtractor implements PartHandler {

	/** message/*のパートを無視します。 */
	public static final int MODE_IGNORE_MESSAGE = 1;

	/** Content-Disposition: inline; パートはfilenameがあっても無視します。 */
	public static final int MODE_IGNORE_INLINE = 2;

	private final int mode;

	private final List<Part> attachmentParts = new ArrayList<Part>();

	/**
	 * 添付ファイル一覧を得るためのPartHandlerを作成します。 message/*のパートやinline且つファイル名指定ありのパートも
	 * 添付ファイルとして扱います。
	 */
	public AttachmentsExtractor() {
		this(0);
	}

	/**
	 * 添付ファイル一覧を得るためのPartHandlerを作成します。
	 * @param mode 動作モード。MODE_で始まる識別子をor指定します。
	 */
	public AttachmentsExtractor(int mode) {
		this.mode = mode;
	}

	/** MultipartUtility#process()から呼びだされるメソッドです。 */
	public boolean processPart(Part part, ContentType context) throws MessagingException,
																IOException {
		// Apple Mail対策
		if (part.getContentType().indexOf("application/applefile") != -1) {
			return true;
		}

		if (part.isMimeType("message/*")) {
			if ((mode & MODE_IGNORE_MESSAGE) != 0) {
				return true;
			}
			attachmentParts.add(part);
			return true;
		}
		if (MailUtility.getFileName(part) == null) {
			return true;
		}
		if ((mode & MODE_IGNORE_INLINE) != 0 && Part.INLINE.equalsIgnoreCase(part.getDisposition())) {
			return true;
		}

		attachmentParts.add(part);
		return true;
	}

	/**
	 * 添付ファイル個数を返します。
	 */
	public int getCount() {
		return attachmentParts.size();
	}

	/**
	 * 添付ファイル名の配列を返します。
	 * <P>
	 * 添付ファイルが存在しない場合は空の配列を返します。 <BR>
	 * ファイル名は同一のものが複数存在する事もありえます。
	 * </P>
	 */
	public String[] getFileNames() throws MessagingException {
		String[] names = new String[getCount()];
		for (int i = 0; i < names.length; i++) {
			names[i] = getFileName(i);
		}
		return names;
	}

	/**
	 * 指定添付ファイルのファイル名を返します。
	 */
	public String getFileName(int index) throws MessagingException {
		Part part = (Part)attachmentParts.get(index);
		String name = MailUtility.getFileName(part);
		if (name == null) {
			// 添付ファイル名が取得できない場合は、指定されていなかった場合か、
			// あるいはmessage/*のパートの場合です。
			// この場合は仮のファイル名を付けることとします。
			if (part.isMimeType("message/*")) {
				// If part is Message, create temporary filename.
				name = "message" + index + ".eml";
			} else {
				name = "file" + index + ".tmp";
			}
		}
		return name;
	}

	/**
	 * 指定添付ファイルのContent-Typeを返します。
	 */
	public String getContentType(int index) throws MessagingException {
		return MailUtility.unfold(((Part)attachmentParts.get(index)).getContentType());
	}

	/**
	 * 指定添付ファイルのサイズを返します。
	 */
	public int getSize(int index) throws MessagingException {
		return ((Part)attachmentParts.get(index)).getSize();
	}

	/**
	 * 指定添付ファイルを読み込むストリームを返します。
	 */
	public InputStream getInputStream(int index) throws MessagingException, IOException {
		return ((Part)attachmentParts.get(index)).getInputStream();
	}

	/**
	 * 指定添付ファイルを指定ストリームに書き出します。
	 */
	public void writeTo(int index, OutputStream out) throws MessagingException, IOException {
		InputStream in = getInputStream(index);
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) != -1) {
			out.write(buf, 0, len);
		}
	}

	public static void main(String[] args) throws Exception {
		javax.mail.internet.MimeMessage msg = new javax.mail.internet.MimeMessage(
				javax.mail.Session.getDefaultInstance(System.getProperties(), null), System.in);
		AttachmentsExtractor h = new AttachmentsExtractor();
		MultipartUtility.process(msg, h);
		for (int i = 0; i < h.getCount(); i++) {
			System.out.println("Attachment no : " + i);
			System.out.println("Filename = " + h.getFileName(i));
			System.out.println("******************");
			h.writeTo(i, System.out);
		}
	}
}