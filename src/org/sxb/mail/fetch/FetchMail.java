package org.sxb.mail.fetch;

import org.sxb.mail.MailException;

/**
 * メールサーバからメールを取得するインターフェース。<br>
 * このインターフェースの実装クラスでメールサーバの情報を設定します。
 * <p>
 * getMails()メソッドはスレッドセーフです。メソッドを呼び出すとメールサーバに接続し、
 * メソッド終了時にサーバとの接続を切断します。
 * 
 * @since 1.2
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: FetchMail.java,v 1.1
 */
public interface FetchMail {

	/**
	 * メールサーバからメールを受信し、ReceivedMailインスタンスに変換して返します。<br>
	 * 受信したメールは、メールサーバに残されます。
	 * <p>
	 * このメソッドを呼び出すとメールサーバに接続します。メールを受信した後、メールサーバとの接続を切断します。
	 * <p>
	 * メールサーバがimapサーバの場合、一度受信したメールには既読フラグ(SEENフラグ)が付けられます。
	 * 
	 * @return 受信したメールのReceivedMailインスタンス配列
	 * @throws MailException
	 */
	ReceivedMail[] getMails() throws MailException;

	/**
	 * メールサーバからメールを受信し、ReceivedMailインスタンスに変換して返します。<br>
	 * deleteパラメータで、受信時にメールサーバからメールを削除するか残すかを指定します。
	 * <p>
	 * このメソッドを呼び出すとメールサーバに接続します。メールを受信した後、メールサーバとの接続を切断します。
	 * <p>
	 * メールサーバがimapサーバの場合、一度受信したメールには既読フラグ(SEENフラグ)が付けられます。
	 * 
	 * @param delete 受信時にメールサーバからメールを削除する場合 true
	 * @return 受信したメールのReceivedMailインスタンス配列
	 * @throws MailException
	 */
	ReceivedMail[] getMails(boolean delete) throws MailException;

}