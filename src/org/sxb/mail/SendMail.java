package org.sxb.mail;

import javax.mail.internet.MimeMessage;

/**
 * SendMailインターフェース。
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: SendMail.java,v 1.0
 */
public interface SendMail {

	/**
	 * 指定されたメールを送信します。
	 * 
	 * @param mail 送信するメールのMailインスタンス
	 * @throws MailException メール送信に失敗した場合
	 */
	void send(Mail mail) throws MailException;

	/**
	 * 指定されたメールを送信します。
	 * 
	 * @param mails 送信するメールのMailインスタンス配列
	 * @throws MailException メール送信に失敗した場合
	 */
	void send(Mail[] mails) throws MailException;

	/**
	 * 指定されたMimeMessageを送信します。
	 * 
	 * @param mimeMessage 送信するメールのMimeMessageインスタンス
	 * @throws MailException メール送信に失敗した場合
	 */
	void send(MimeMessage mimeMessage) throws MailException;

	/**
	 * 指定されたMimeMessageを送信します。
	 * 
	 * @param mimeMessages 送信するメールのMimeMessageインスタンス配列
	 * @throws MailException メール送信に失敗した場合
	 */
	void send(MimeMessage[] mimeMessages) throws MailException;

}