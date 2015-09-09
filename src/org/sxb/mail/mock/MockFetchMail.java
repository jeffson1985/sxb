package org.sxb.mail.mock;

import java.util.ArrayList;
import java.util.List;

import org.sxb.log.Logger;
import org.sxb.mail.MailException;
import org.sxb.mail.fetch.FetchMail;
import org.sxb.mail.fetch.ReceivedMail;

/**
 * FetchMailImplクラスのMock。<br>
 * <code>setupGetMails()</code>メソッドで<code>ReceivedMail</code>インスタンスをセットすると、<code>getMails()</code>メソッドがそのインスタンスを返します。
 * 
 * @since 1.2
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: MockFetchMail.java,v 1.1.2
 */
public class MockFetchMail implements FetchMail {

	private static Logger log = Logger.getLogger(MockFetchMail.class);

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

	private List<ReceivedMail> receivedMails;

	/**
	 * コンストラクタ。
	 */
	public MockFetchMail() {
		super();
		receivedMails = new ArrayList<ReceivedMail>();
	}

	/**
	 * <code>MockFetchMail</code>の<code>getMails()</code>メソッドが返す
	 * <code>ReceivedMail</code>インスタンスをセットします。
	 * 
	 * @param mail <code>getMails()</code>メソッドが返す<code>ReceivedMail</code>インスタンス
	 */
	public void setupGetMails(ReceivedMail mail) {
		receivedMails.add(mail);
	}

	/**
	 * <code>MockFetchMail</code>の<code>getMails()</code>メソッドが返す
	 * <code>ReceivedMail</code>インスタンスをセットします。
	 * 
	 * @param mails <code>getMails()</code>メソッドが返す<code>ReceivedMail</code>インスタンス配列
	 */
	public void setupGetMails(ReceivedMail[] mails) {
		for (int i = 0; i < mails.length; i++) {
			ReceivedMail mail = mails[i];
			setupGetMails(mail);
		}
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMail#getMails()
	 */
	public ReceivedMail[] getMails() throws MailException {
		log.debug(protocol.toUpperCase() + "サーバ[" + host + "]に接続しるフリ。");
		log.debug(protocol.toUpperCase() + "サーバ[" + host + "]に接続したフリ。");

		if (receivedMails.size() > 0) {
			log.debug(receivedMails.size() + "通のメールを受信するフリ。");
		} else {
			log.debug("受信するフリをするメールはありません。");
		}
		try {
			return (ReceivedMail[])receivedMails.toArray(new ReceivedMail[receivedMails.size()]);
		} finally {
			log.debug(protocol.toUpperCase() + "サーバ[" + host + "]との接続を切断するフリ。");
			log.debug(protocol.toUpperCase() + "サーバ[" + host + "]との接続を切断したフリ。");
		}
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMail#getMails(boolean)
	 */
	public ReceivedMail[] getMails(boolean delete) throws MailException {
		ReceivedMail[] result = getMails();
		if (delete) {
			receivedMails.clear();
		}
		return result;
	}

	/**
	 * @return Returns the host.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host The host to set.
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return Returns the port.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port The port to set.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return Returns the protocol.
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol The protocol to set.
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return Returns the username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username The username to set.
	 */
	public void setUsername(String username) {
		this.username = username;
	}
}