package org.sxb.mail;

import javax.mail.internet.MimeMessage;

/**
 * SMTPサーバとの接続、切断を任意のタイミングで行いたい場合に使用するSendMailインターフェース。
 * <p>
 * 大量メール配信で、MailやMimeMessageの配列を用意するとメモリを圧迫してしまう場合などに使用します。<br>
 * 接続のクローズを忘れないように注意してください。
 * <p>
 * このインターフェース実装クラスのインスタンスは、メールサーバとの接続を保持するため、
 * スレッドセーフではありません。<br>
 * DIコンテナでの使用の際はシングルトンでインスタンスを取得しないように注意してください。
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: SendMailPro.java,v 1.0.2
 */
public interface SendMailPro {

	/**
	 * SMTPサーバに接続します。
	 * 
	 * @throws MailException
	 */
	void connect() throws MailException;

	/**
	 * SMTPサーバとの接続をクローズします。
	 * 接続していない時にこのメソッドを呼んでも何も行いません。
	 * 
	 * @throws MailException
	 */
	void disconnect() throws MailException;

	/**
	 * 指定されたMimeMessageを送信します。SMTPサーバに接続していない場合は例外をスローします。
	 * 
	 * @param mimeMessage
	 * @throws MailException
	 */
	void send(MimeMessage mimeMessage) throws MailException;

	/**
	 * 指定されたMailを送信します。SMTPサーバに接続していない場合は例外をスローします。
	 * 
	 * @param mail
	 * @throws MailException
	 */
	void send(Mail mail) throws MailException;

}