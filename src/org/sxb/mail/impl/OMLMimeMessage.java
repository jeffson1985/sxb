package org.sxb.mail.impl;

import java.util.Random;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * Message-Idヘッダがカスタマイズ可能なMimeMessageのサブクラス。
 * 
 * @since 1.1
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: OMLMimeMessage.java,v 1.3
 */
public class OMLMimeMessage extends MimeMessage {

	private static Random random = new Random();

	private String domainPartOfMessageId;

	private String messageId;

	/**
	 * コンストラクタ。
	 * 
	 * @param session 
	 * @param domainPartOfMessageId Message-Idヘッダのドメイン部分に使用する文字列
	 */
	public OMLMimeMessage(Session session, String domainPartOfMessageId) {
		super(session);

		String[] parts = domainPartOfMessageId.split("@");
		if (parts.length == 1) {
			this.domainPartOfMessageId = "@" + domainPartOfMessageId;
		} else if (parts.length == 2) {
			if (parts[0].length() > 0 && !parts[0].startsWith(".")) {
				this.domainPartOfMessageId = "." + domainPartOfMessageId;
			} else {
				this.domainPartOfMessageId = domainPartOfMessageId;
			}
		}
		messageId = generateRandomMessageId();
	}

	/**
	 * Message-Idヘッダをここでセットします。
	 * <p>
	 * 参考ページ<br>
	 * <a href="http://java.sun.com/products/javamail/FAQ.html#msgid">http://java.sun.com/products/javamail/FAQ.html#msgid</a>
	 * 
	 * @see javax.mail.internet.MimeMessage#updateHeaders()
	 */
	protected void updateHeaders() throws MessagingException {
		super.updateHeaders();
		setHeader("Message-ID", messageId);
	}

	/**
	 * タイムスタンプ + 16桁の乱数 + messageIdプロパティを連結した文字列を返します。
	 * 
	 * @return タイムスタンプ + 16桁の乱数 + messageIdプロパティを連結した文字列
	 */
	protected String generateRandomMessageId() {
		StringBuffer buf = new StringBuffer();
		buf.append("<");
		buf.append(System.currentTimeMillis()).append(".");
		for (int i = 0; i < 16; i++) {
			long num = Math.abs(random.nextInt(10));
			buf.append(num);
		}
		buf.append(domainPartOfMessageId);
		buf.append(">");
		return buf.toString();
	}

	/**
	 * 生成されたMessage-Idを返します。
	 * 
	 * @return 生成されたMessage-Id
	 */
	public String getMessageId() {
		return messageId;
	}
}