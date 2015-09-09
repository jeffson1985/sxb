package org.sxb.mail.mock;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.sxb.log.Logger;
import org.sxb.mail.MailException;
import org.sxb.mail.NotConnectedException;
import org.sxb.mail.fetch.FetchMailPro;
import org.sxb.mail.fetch.ReceivedMail;

/**
 * FetchMailProImplクラスのMock。
 * 
 * @since 1.2
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: MockFetchMailPro.java,v 1.1.2
 */
public class MockFetchMailPro implements FetchMailPro {

	private static Logger log = Logger.getLogger(MockFetchMailPro.class);

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

	private boolean javaMailLogEnabled;

	private boolean connected = false;

	private List<ReceivedMail> receivedMails;

	/**
	 * コンストラクタ。
	 */
	public MockFetchMailPro() {
		super();
		receivedMails = new ArrayList<ReceivedMail>();
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#connect()
	 */
	public synchronized void connect() throws MailException {
		if (isConnected()) {
			log.warn("既にサーバ[" + host + "]に接続されています。再接続するには先に接続を切断する必要があります。");
			return;
		}

		log.debug(protocol.toUpperCase() + "サーバ[" + host + "]に接続するフリ。");
		connected = true;
		log.info(protocol.toUpperCase() + "サーバ[" + host + "]に接続したフリ。");
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#disconnect()
	 */
	public synchronized void disconnect() throws MailException {
		if (isConnected()) {
			log.debug(protocol.toUpperCase() + "サーバ[" + host + "]との接続を切断するフリ。");
			connected = false;
			log.debug(protocol.toUpperCase() + "サーバ[" + host + "]との接続を切断したフリ。");
		}
	}

	/**
	 * <code>MockFetchMailPro</code>の<code>getMails()</code>メソッドが返す
	 * <code>ReceivedMail</code>インスタンスをセットします。
	 * 
	 * @param mail <code>getMails()</code>メソッドが返す<code>ReceivedMail</code>インスタンス
	 */
	public void setupGetMails(ReceivedMail mail) {
		receivedMails.add(mail);
	}

	/**
	 * <code>MockFetchMailPro</code>の<code>getMails()</code>メソッドが返す
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
	 * @see org.sxb.mail.fetch.FetchMailPro#getMailCount()
	 */
	public int getMailCount() throws MailException {
		return receivedMails.size();
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#getMail(int)
	 */
	public synchronized ReceivedMail getMail(int num) throws MailException {
		return getMail(num, false);
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#getMail(int, boolean)
	 */
	public synchronized ReceivedMail getMail(int num, boolean delete) throws MailException {
		if (isConnected()) {
			if (delete) {
				return (ReceivedMail)receivedMails.remove(num - 1);
			} else {
				return (ReceivedMail)receivedMails.get(num - 1);
			}
		} else {
			throw new NotConnectedException(protocol.toUpperCase() + "サーバ[" + host + "]に接続されていません。");
		}
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#getMails(boolean)
	 */
	public synchronized ReceivedMail[] getMails(boolean delete) throws MailException {
		if (isConnected()) {
			ReceivedMail[] results = (ReceivedMail[])receivedMails
					.toArray(new ReceivedMail[receivedMails.size()]);
			if (delete) {
				receivedMails.clear();
			}
			return results;
		} else {
			throw new NotConnectedException(protocol.toUpperCase() + "サーバ[" + host + "]に接続されていません。");
		}
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#getMessage(int)
	 */
	public MimeMessage getMessage(int num) throws MailException {
		throw new UnsupportedOperationException("申し訳ございません。MockFetchMailProでは、このメソッドをサポートしていません。");
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#getMessages(boolean)
	 */
	public MimeMessage[] getMessages(boolean delete) throws MailException {
		throw new UnsupportedOperationException("申し訳ございません。MockFetchMailProでは、このメソッドをサポートしていません。");
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#changeFolder(java.lang.String)
	 */
	public synchronized void changeFolder(String folderName) throws MailException {
		if (!isConnected()) {
			log.warn("メールサーバに接続されていません。");
			return;
		}

		log.debug("メッセージフォルダ[" + folderName + "]をオープンするフリ。");
		log.debug("メッセージフォルダ[" + folderName + "]をオープンしたフリ。");
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#isConnected()
	 */
	public boolean isConnected() {
		return connected;
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
	 * @return Returns the javaMailLogEnabled.
	 */
	public boolean isJavaMailLogEnabled() {
		return javaMailLogEnabled;
	}

	/**
	 * @param javaMailLogEnabled The javaMailLogEnabled to set.
	 */
	public void setJavaMailLogEnabled(boolean javaMailLogEnabled) {
		this.javaMailLogEnabled = javaMailLogEnabled;
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