package org.sxb.mail.fetch.impl;

import org.sxb.mail.MailException;
import org.sxb.mail.fetch.FetchMail;
import org.sxb.mail.fetch.ReceivedMail;

/**
 * <code>FetchMail</code>インターフェースの実装クラス。
 * <p>
 * <code>FetchMailProImpl</code>クラスに処理を委譲しています。
 * 
 * @since 1.2
 * @see FetchMailProImpl
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: FetchMailImpl.java,v 1.1.2
 */
public class FetchMailImpl implements FetchMail {

	/** デフォルトのSMTPサーバ。「localhost」 */
	public static final String DEFAULT_HOST = "localhost";

	/** デフォルトのプロトコル。「pop3」 */
	public static final String DEFAULT_PROTOCOL = "pop3";

	/**
	 * デフォルトのポート。「-1」<br>
	 * -1はプロトコルに応じた適切なポートを設定する特別な値。
	 */
	public static final int DEFAULT_PORT = -1;

	@SuppressWarnings("unused")
	private static final String INBOX_NAME = "INBOX";

	private String host = DEFAULT_HOST;

	private String protocol = DEFAULT_PROTOCOL;

	private int port = DEFAULT_PORT;

	private String username;

	private String password;

	/**
	 * コンストラクタ。
	 */
	public FetchMailImpl() {}

	/**
	 * @see org.sxb.mail.fetch.FetchMail#getMails()
	 */
	public ReceivedMail[] getMails() throws MailException {
		return getMails(false);
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMail#getMails(boolean)
	 */
	public ReceivedMail[] getMails(boolean delete) throws MailException {
		FetchMailProImpl fetchMailProImpl = createFetchMailProImpl();
		fetchMailProImpl.connect();
		try {
			return fetchMailProImpl.getMails(delete);
		} finally {
			fetchMailProImpl.disconnect();
		}
	}

	/**
	 * サーバ情報をセットしたFetchMailProImplインスタンスを生成します。
	 * 
	 * @return サーバ情報をセットしたFetchMailProImplインスタンス
	 */
	private FetchMailProImpl createFetchMailProImpl() {
		FetchMailProImpl fmp = new FetchMailProImpl();
		fmp.setHost(host);
		fmp.setPort(port);
		fmp.setProtocol(protocol);
		fmp.setUsername(username);
		fmp.setPassword(password);
		return fmp;
	}

	/**
	 * メールサーバのホスト名、またはIPアドレスをセットします。
	 * デフォルトは localhost です。
	 * 
	 * @param host メールサーバのホスト名、またはIPアドレス
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * メールサーバの認証パスワード名をセットします。
	 * 
	 * @param password メールサーバの認証パスワード
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * メール受信に使用するポート番号をセットします。
	 * プロトコルに応じたポート番号が自動的に使用されますので、通常ここでポート番号をセットする必要はありません。
	 * 
	 * @param port ポート番号
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * メール受信に使用するプロトコロルをセットします。
	 * 現在サポートされているプロトコルは、「pop3」と「imap」の二つです。
	 * デフォルトは「pop3」です。
	 * <p>
	 * POP3サーバへの認証をAPOPで行いたい場合は、プロトコル名ではありませんが、
	 * 「apop」を指定してください。APOP認証を使用するには、JavaMail 1.3.2以降が必要です。
	 * 
	 * @param protocol プロトコル
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * メールサーバの認証ユーザ名をセットします。
	 * 
	 * @param username メールサーバの認証ユーザ名
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * メールサーバのホスト名、またはIPアドレスを返します。
	 * 
	 * @return メールサーバのホスト名、またはIPアドレス
	 */
	public String getHost() {
		return host;
	}

	/**
	 * メールサーバの認証パスワードを返します。
	 * 
	 * @return メールサーバの認証パスワード
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return ポート番号
	 */
	public int getPort() {
		return port;
	}

	/**
	 * メール受信に使用するプロトコロルをセットします。
	 * 
	 * @return プロトコル
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * メールサーバの認証ユーザ名を返します。
	 * 
	 * @return メールサーバの認証ユーザ名
	 */
	public String getUsername() {
		return username;
	}
}