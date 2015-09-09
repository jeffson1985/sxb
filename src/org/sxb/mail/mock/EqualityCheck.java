package org.sxb.mail.mock;

import javax.mail.internet.InternetAddress;

import org.sxb.mail.Mail;

/**
 * メールが同値であることを調べるメソッドを提供。
 * <p>
 * <strong>注:</strong> 添付ファイルは比較対象になりません。
 * 
 * @since 1.1
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: EqualityCheck.java,v 1.3
 */
public class EqualityCheck {

	private EqualityCheck() {}

	/**
	 * expectedとsentのメール内容が同一であるかどうかを判定します。<br>
	 * MultipartMailのインスタンスも指定できます。但し、添付ファイルはチェックされません。
	 * 
	 * @param expected
	 * @param sent
	 * @return expectedとsentのメール内容が同一である場合 true
	 */
	public static boolean equals(Mail expected, Mail sent) {
		boolean mockMode = (expected instanceof MockMail);

		// マルチパートメールの場合
		if (expected.isMultipartMail()) {

			// HTML
			if (!mockMode) {
				if ((expected.getHtmlText() == null && sent.getHtmlText() != null)
						|| (expected.getHtmlText() != null && sent.getHtmlText() == null)
						|| (!expected.getHtmlText().equals(sent.getHtmlText()))) {
					return false;
				}
			} else if (mockMode && expected.getHtmlText() != null) {
				if (!expected.getHtmlText().equals(sent.getHtmlText())) {
					return false;
				}
			}
		}

		// Return-Path
		if (!mockMode || (mockMode && expected.getReturnPath() != null)) {
			if (expected.getReturnPath() != null && sent.getReturnPath() != null) {
				if (!expected.getReturnPath().equals(sent.getReturnPath())) {
					return false;
				}
			} else if ((expected.getReturnPath() != null && sent.getReturnPath() == null)
					|| (expected.getReturnPath() == null && sent.getReturnPath() != null)) {
				return false;
			}
		}

		//	From
		if (!mockMode || (mockMode && expected.getFrom() != null)) {
			if (expected.getFrom() != null && sent.getFrom() != null) {
				if (!equals(expected.getFrom(), sent.getFrom())) {
					return false;
				}
			} else if ((expected.getFrom() != null && sent.getFrom() == null)
					|| (expected.getFrom() == null && sent.getFrom() != null)) {
				return false;
			}
		}

		// to
		InternetAddress[] expectedAddresses = expected.getTo();
		InternetAddress[] sentAddresses = sent.getTo();
		if (!mockMode || (mockMode && expectedAddresses.length > 0)) {
			if (expectedAddresses.length != sentAddresses.length) {
				return false;
			}
			for (int i = 0; i < expectedAddresses.length; i++) {
				if (!equals(expectedAddresses[i], sentAddresses[i])) {
					return false;
				}
			}
		}

		// cc
		expectedAddresses = expected.getCc();
		sentAddresses = sent.getCc();
		if (!mockMode || (mockMode && expectedAddresses.length > 0)) {
			if (expectedAddresses.length != sentAddresses.length) {
				return false;
			}
			for (int i = 0; i < expectedAddresses.length; i++) {
				if (!equals(expectedAddresses[i], sentAddresses[i])) {
					return false;
				}
			}
		}

		// bcc
		expectedAddresses = expected.getBcc();
		sentAddresses = sent.getBcc();
		if (!mockMode || (mockMode && expectedAddresses.length > 0)) {
			if (expectedAddresses.length != sentAddresses.length) {
				return false;
			}
			for (int i = 0; i < expectedAddresses.length; i++) {
				if (!equals(expectedAddresses[i], sentAddresses[i])) {
					return false;
				}
			}
		}

		// Reply-To
		if (!mockMode || (mockMode && expected.getReplyTo() != null)) {
			if (expected.getReplyTo() != null && sent.getReplyTo() != null) {
				if (!equals(expected.getReplyTo(), sent.getReplyTo())) {
					return false;
				}
			} else if ((expected.getReplyTo() != null && sent.getReplyTo() == null)
					|| (expected.getReplyTo() == null && sent.getReplyTo() != null)) {
				return false;
			}
		}

		// 件名
		if (!mockMode || (mockMode && expected.getSubject().length() > 0)) {
			if (!expected.getSubject().equals(sent.getSubject())) {
				return false;
			}
		}

		// 本文
		if (!mockMode || (mockMode && expected.getText().length() > 0)) {
			if (!expected.getText().equals(sent.getText())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 指定された二つのInternetAddressインスタンスが等しいかどうか判定します。
	 * <p>
	 * InternetAddress#equals()メソッドでは、メールアドレスしか検査しないため、
	 * このメソッドではInternetAddressに名前が含まれている場合、その名前も
	 * 等しいかどうか検査します。
	 * 
	 * @since 1.1.3
	 * @param a 比較するInternetAddressインスタンス
	 * @param b 比較するInternetAddressインスタンス
	 * @return 二つのInternetAddressインスタンスが等しい場合 true
	 */
	public static boolean equals(InternetAddress a, InternetAddress b) {
		if (a.equals(b)) {
			if (a.getPersonal() != null || b.getPersonal() != null) {
				return a.getPersonal().equals(b.getPersonal());
			} else {
				return true;
			}
		}
		return false;
	}
}