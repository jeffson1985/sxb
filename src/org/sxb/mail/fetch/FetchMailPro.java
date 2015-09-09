package org.sxb.mail.fetch;

import javax.mail.internet.MimeMessage;

import org.sxb.mail.MailException;

/**
 * メールサーバからメールを取得する上級インターフェース。<br>
 * このインターフェースの実装クラスでメールサーバの情報を設定します。
 * <p>
 * <code>FetchMail</code>インターフェースと異なり、メール取得時に例外が発生しても、
 * メールサーバとの接続は切断されません。<code>finally</code>ブロックを使用するなりして
 * メールサーバとの接続を確実に切断できるようにすることを推奨します。
 * <p>
 * このインターフェース実装クラスのインスタンスは、メールサーバとの接続を保持するため、
 * スレッドセーフではありません。<br>
 * DIコンテナでの使用の際はシングルトンでインスタンスを取得しないように注意してください。
 * 
 * @see FetchMail
 * @since 1.2
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: FetchMailPro.java,v 1.1
 */
public interface FetchMailPro {

	/**
	 * メールサーバに接続し、「INBOX」フォルダをオープンします。
	 * 
	 * @throws MailException メールサーバとの接続に失敗した場合
	 */
	void connect() throws MailException;

	/**
	 * メールサーバとの接続を切断します。接続されていなければ何も行いません。
	 * 
	 * @throws MailException メールサーバとの接続切断に失敗した場合
	 */
	void disconnect() throws MailException;

	/**
	 * 現在のフォルダに届いているメール数を返します。
	 * 
	 * @return 現在のフォルダにあるメール数
	 * @throws MailException
	 */
	int getMailCount() throws MailException;

	/**
	 * 現在のフォルダにある指定されたメッセージ番号のメールをReceivedMailに変換して返します。
	 * メッセージ番号は1始まりです。
	 * <p>
	 * メッセージはサーバから削除されません。
	 * 
	 * @param num メッセージ番号。1始まり。
	 * @return 指定されたメッセージ番号のReceivedMailインスタンス
	 * @throws MailException メール取得に失敗した場合
	 */
	ReceivedMail getMail(int num) throws MailException;

	/**
	 * 現在のフォルダにある指定されたメッセージ番号のメールをReceivedMailに変換して返します。
	 * メッセージ番号は1始まりです。
	 * 指定した番号のメッセージをサーバから削除するかどうかを指定できます。
	 * 
	 * @param num メッセージ番号。1始まり。
	 * @param delete 指定された番号のメッセージをサーバから削除する場合 true を指定
	 * @return 指定されたメッセージ番号のReceivedMailインスタンス
	 * @throws MailException メール取得に失敗した場合
	 */
	ReceivedMail getMail(int num, boolean delete) throws MailException;

	/**
	 * 現在のフォルダにある全メールをReceivedMailに変換して返します。
	 * 
	 * @param delete メール取得後にサーバからメールを削除する場合 true
	 * @return 現在のフォルダにある全メールのReceivedMailインスタンス
	 * @throws MailException メール取得に失敗した場合
	 */
	ReceivedMail[] getMails(boolean delete) throws MailException;

	/**
	 * 現在のフォルダにある指定されたメッセージ番号のメールを返します。
	 * メッセージ番号は1始まりです。
	 * 
	 * @see javax.mail.Folder#getMessage(int)
	 * @param num メッセージ番号。1始まり。
	 * @return 指定された番号のMimeMessageインスタンス
	 * @throws MailException メール取得に失敗した場合
	 */
	MimeMessage getMessage(int num) throws MailException;

	/**
	 * 現在のフォルダにある全メールを返します。
	 * 
	 * @param delete メール取得後にサーバからメールを削除する場合 true
	 * @return 現在のフォルダにある全メールのMimeMessageインスタンス
	 * @throws MailException メール取得に失敗した場合
	 */
	MimeMessage[] getMessages(boolean delete) throws MailException;

	/**
	 * 指定された名前のフォルダに移動します。
	 * フォルダ名は"INBOX/XXXX"のように、INBOXからのパス指定します。
	 * <p>
	 * <strong>注:</strong> このメソッドは、メールサーバがimapサーバの時にのみ使用可能です。
	 * 
	 * @param folderName 移動先のフォルダ名
	 * @throws MailException
	 */
	void changeFolder(String folderName) throws MailException;

	/**
	 * メールサーバと接続しているかどうか判定します。
	 * 
	 * @return 接続している場合 true
	 */
	boolean isConnected();
}