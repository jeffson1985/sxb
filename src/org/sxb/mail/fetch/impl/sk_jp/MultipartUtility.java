/*
 * @(#) $Id: MultipartUtility.java,v 1.1.2.2 2004/10/24 10:26:50 
 * $Revision: 1.1.2.2 $
 * Copyright (c) 2000 Shin Kinoshita All Rights Reserved.
 */
package org.sxb.mail.fetch.impl.sk_jp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

/**
 * メッセージボディを取り出す手段を提供するstaticメソッドのセットです。
 * <p>
 * </p>
 * @version $Revision: 1.1.2.2 $ $Date: 2004/10/24 10:26:50 $
 * @author Shin
 * @author Tomohiro Otsuka
 */
public class MultipartUtility {

	private static final String JIS_CHARSET = "ISO-2022-JP";

	/**
	 * 指定パートのボディを返します。
	 * <P>
	 * Part#getContent()の代わりです。
	 * MIMEに準拠しないContent-Type:の補正を行います。
	 * charset指定がない場合は"ISO-2022-JP"を補います。
	 * </P><P>
	 * パートがUTF-7の場合も正常に内容を取得出来ます。
	 * </P>
	 */
	public static Object getContent(Part part) throws MessagingException, IOException {
		return getContent(part, JIS_CHARSET);
	}

	private static CorrectedContentTypeDataSource correctedDataSource = new CorrectedContentTypeDataSourceUTF7Support();

	private static DataHandler correctedDataHandler = new DataHandler(correctedDataSource);

	/**
	 * 指定パートのボディを返します。
	 * <P>
	 * MIMEに準拠しないContent-Type:の補正を行います。
	 * charset指定がない場合はcharsetで指定されたもので補います。
	 * </P><P>
	 * パートがUTF-7の場合も正常に内容を取得出来ます。
	 * </P>
	 */
	public static Object getContent(Part part, String charset) throws MessagingException,
																IOException {
		synchronized (correctedDataSource) {

			correctedDataSource.setPart(part);
			try {
				correctedDataSource.setDefaultCharset(charset);
				return correctedDataHandler.getContent();
			} catch (UnsupportedEncodingException e) {
				/*
				 * 不正な文字コードがcharsetにセットされ例外がスローされた場合に
				 * JIS_CHARSETに文字コードを置き換え、再度ホディの取得を試みます。
				 * 
				 * by otsuka
				 */
				correctedDataSource.setForceCharset(JIS_CHARSET);
				return correctedDataHandler.getContent();
			}

		}
	}

	/**
	 * 指定パート配下で最初に見つけたテキストパートのボディを返します。
	 * process()を呼び出して結果を返すだけのconvenience methodです。
	 */
	public static String getFirstPlainText(Part part) throws MessagingException {
		FirstPlainPartExtractor h = new FirstPlainPartExtractor();
		process(part, h);
		return h.getText();
	}

	/**
	 * 指定パート配下のinlineなテキストパートを集めて表示用のボディを返します。
	 * process()を呼び出して結果を返すだけのconvenience methodです。
	 */
	public static String getPlainText(Part part) throws MessagingException {
		PlainPartExtractor h = new PlainPartExtractor();
		process(part, h);
		return h.getText();
	}

	/**
	 * 指定パート配下の各パートを処理します。
	 * <P>
	 * すべてのPartに対してPartHandlerが呼び出されます。<BR>
	 * </P>
	 */
	public static void process(Part part, PartHandler handler) throws MessagingException {
		process(part, handler, null);
	}

	private static boolean process(Part part, PartHandler handler, ContentType context)
																						throws MessagingException {
		try {
			if (part.isMimeType("multipart/*")) {
				Multipart mp = (Multipart)part.getContent();
				ContentType cType = new ContentType(part.getContentType());
				for (int i = 0; i < mp.getCount(); i++) {
					if (!process(mp.getBodyPart(i), handler, cType)) {
						return false;
					}
				}
				return true;
			}
			return handler.processPart(part, context);
		} catch (IOException e) {
			throw new MessagingException("Got exception \nin " + part + "\n", e);
		}
	}

	/**
	 * 指定partにbodyPartを追加します。
	 * partがマルチパーとコンテナの場合はそのコンテナにbodyPartを追加します。
	 * そうでない場合はpartのボディとしてmultipart/mixedのコンテナを設定し、
	 * 元のpartのボディとbodyPartのボディをそのコンテナに追加します。
	 */
	public static void addBodyPart(Part part, MimeBodyPart bodyPart) throws MessagingException,
																	IOException {
		if (part.isMimeType("multipart/*")) {
			((MimeMultipart)part.getContent()).addBodyPart(bodyPart);
			return;
		}
		// 仮
		MimeMultipart mp = new MimeMultipart("mixed");
		MimeBodyPart original = new MimeBodyPart();
		original.setContent(part.getContent(), part.getContentType());
		mp.addBodyPart(original);
		mp.addBodyPart(bodyPart);
		part.setContent(mp);
	}

	/**
	 * partのツリー構造をダンプするデバッグ用メソッドです。
	 */
	public static void dump(Part part) {
		dump(part, 0);
	}

	private static void dump(Part part, int layer) {
		for (int i = 0; i < layer; i++) {
			System.out.print("    ");
		}
		try {
			System.out.println(part.getClass() + ":" + part.getContentType());
			if (part.isMimeType("multipart/*")) {
				MimeMultipart mp = (MimeMultipart)part.getContent();
				for (int i = 0; i < mp.getCount(); i++) {
					dump(mp.getBodyPart(i), layer + 1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}