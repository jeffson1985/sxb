package org.sxb.mail.fetch.impl;

import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import org.sxb.log.Logger;
import org.sxb.mail.MailAuthenticationException;
import org.sxb.mail.MailException;
import org.sxb.mail.NotConnectedException;
import org.sxb.mail.fetch.FetchMailPro;
import org.sxb.mail.fetch.MailConverter;
import org.sxb.mail.fetch.MailFetchException;
import org.sxb.mail.fetch.ReceivedMail;

/**
 * <code>FetchMail</code>インターフェースの実装クラス。
 * <p>
 * このクラスのインスタンスは、インスタンス変数を用いて状態を保持するため、
 * ステートレスではありません。ステートフルです。
 * 
 * @since 1.0
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: FetchMailProImpl.java,v 1.0
 */
public class FetchMailProImpl implements FetchMailPro {

	private static Logger log = Logger.getLogger(FetchMailProImpl.class);

	/** デフォルトのSMTPサーバ。「localhost」 */
	public static final String DEFAULT_HOST = "localhost";

	/** デフォルトのプロトコル。「pop3」 */
	public static final String DEFAULT_PROTOCOL = "pop3";

	/**
	 * デフォルトのポート。「-1」<br>
	 * -1はプロトコルに応じた適切なポートを設定する特別な値。
	 */
	public static final int DEFAULT_PORT = -1;

	private static final String INBOX_NAME = "INBOX";

	private String host = DEFAULT_HOST;

	private String protocol = DEFAULT_PROTOCOL;

	private int port = DEFAULT_PORT;

	private String username;

	private String password;

	private boolean javaMailLogEnabled;

	private Store store;

	private Folder currentFolder;

	/** MailConver の実装インスタンス。 */
	private MailConverter mailConverter = new MailConverterImpl();

	/**
	 * コンストラクタ。
	 */
	public FetchMailProImpl() {
		System.setProperty("mail.mime.multipart.ignoremissingendboundary", "true");
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#connect()
	 */
	public synchronized void connect() throws MailException {
		if (isConnected()) {
			log.warn("既にサーバ[" + host + "]に接続されています。再接続するには先に接続を切断する必要があります。");
			return;
		}

		log.debug(protocol.toUpperCase() + "サーバ[" + host + "]に接続します。");
		Session session = Session.getInstance(createProperties(), null);
		if (javaMailLogEnabled) {
			session.setDebug(true);
		}
		try {
			store = session.getStore(protocol);
			store.connect(host, port, username, password);
		} catch (NoSuchProviderException e) {
			log.error("指定されたプロトコル[" + protocol + "]はサポートされていません。", e);
			throw new MailException("指定されたプロトコル[" + protocol + "]はサポートされていません。", e);
		} catch (AuthenticationFailedException e) {
			log.error(protocol.toUpperCase() + "サーバ[" + host + "]への接続認証に失敗しました。", e);
			throw new MailAuthenticationException(protocol.toUpperCase() + "サーバ[" + host
					+ "]への接続認証に失敗しました。", e);
		} catch (MessagingException e) {
			log.error(protocol.toUpperCase() + "サーバ[" + host + "]への接続に失敗しました。", e);
			throw new MailException(protocol.toUpperCase() + "サーバ[" + host + "]への接続に失敗しました。", e);
		}
		log.info(protocol.toUpperCase() + "サーバ[" + host + "]に接続しました。");

		changeFolder(INBOX_NAME);
	}

	/**
	 * Sessionに渡すPropertiesインスタンスを返します。
	 * APOP認証を行う場合に、"mail.pop3.apop.enable"をセットします。
	 * 
	 * @return Sessionに渡すPropertiesインスタンス
	 */
	private Properties createProperties() {
		Properties prop = new Properties();
		if ("apop".equalsIgnoreCase(protocol)) {
			prop.put("mail.pop3.apop.enable", "true");
		}
		return prop;
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#disconnect()
	 */
	public synchronized void disconnect() throws MailException {
		try {
			closeCurrentFolderIfOpen();
		} finally {
			if (isConnected()) {
				log.debug(protocol.toUpperCase() + "サーバ[" + host + "]との接続を切断します。");
				try {
					store.close();
					store = null;
				} catch (MessagingException e) {
					throw new MailException("サーバ[" + host + "]との接続切断に失敗しました。", e);
				}
			}
		}
		log.info(protocol.toUpperCase() + "サーバ[" + host + "]との接続を切断しました。");
	}

	/**
	 * 現在のメッセージフォルダをクローズします。
	 * 
	 * @throws MailException メッセージフォルダのクローズに失敗した場合
	 */
	private void closeCurrentFolderIfOpen() throws MailException {
		if (currentFolder != null && currentFolder.isOpen()) {
			log.debug("メッセージフォルダ[" + currentFolder.getName() + "]をクローズします。");
			try {
				currentFolder.close(true);
			} catch (MessagingException e) {
				log.error("メッセージフォルダ[" + currentFolder.getName() + "]のクローズに失敗しました。", e);
				throw new MailException("メッセージフォルダ[" + currentFolder.getName() + "]のクローズに失敗しました。",
						e);
			}
			log.debug("メッセージフォルダ[" + currentFolder.getName() + "]をクローズしました。");
			currentFolder = null;
		}
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#changeFolder(java.lang.String)
	 */
	public synchronized void changeFolder(String folderName) throws MailException {
		if (!isConnected()) {
			log.warn("メールサーバに接続されていません。");
			return;
		}

		closeCurrentFolderIfOpen();
		log.debug("メッセージフォルダ[" + folderName + "]をオープンします。");
		try {
			currentFolder = store.getFolder(folderName);
			currentFolder.open(Folder.READ_WRITE);
		} catch (MessagingException e) {
			log.error("メッセージフォルダ[" + folderName + "]のオープンに失敗しました。", e);
			throw new MailException("メッセージフォルダ[" + folderName + "]のオープンに失敗しました。", e);
		}
		log.debug("メッセージフォルダ[" + folderName + "]をオープンしました。");
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#getMailCount()
	 */
	public int getMailCount() throws MailException {
		checkIfCurrentFolderIsOpen();
		try {
			return currentFolder.getMessageCount();
		} catch (MessagingException e) {
			throw new MailFetchException("メール数の取得に失敗しました。", e);
		}
	}
	
	
	/**
	 * 新規メール数の取得
	 * @return
	 * @throws MailException
	 */
	public int getNewMailCount() throws MailException{
		checkIfCurrentFolderIsOpen();
		try {
			return currentFolder.getNewMessageCount();
		} catch (MessagingException e) {
			throw new MailFetchException("新規メール数の取得に失敗しました。", e);
		}
	}
	
	/**
	 * 新規メール数の取得
	 * @return
	 * @throws MailException
	 */
	public int getUnreadMailCount() throws MailException{
		checkIfCurrentFolderIsOpen();
		try {
			return currentFolder.getUnreadMessageCount();
		} catch (MessagingException e) {
			throw new MailFetchException("未読メール数の取得に失敗しました。", e);
		}
	}

	/**
	 * メールサーバに接続されていて、フォルダが操作できる状態かどうか調べます。
	 * フォルダが操作できる状態にない場合、NotConnectedExceptionをスローします。
	 * 
	 * @throws NotConnectedException
	 */
	private void checkIfCurrentFolderIsOpen() throws NotConnectedException {
		if (currentFolder == null || !currentFolder.isOpen()) {
			throw new NotConnectedException(protocol.toUpperCase() + "サーバ[" + host + "]に接続されていません。");
		}
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#getMail(int)
	 */
	public ReceivedMail getMail(int num) throws MailException {
		return getMail(num, false);
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#getMail(int, boolean)
	 */
	public ReceivedMail getMail(int num, boolean delete) throws MailException {
		MimeMessage mimeMessage = getMessage(num);
		try {
			mimeMessage.setFlag(Flags.Flag.DELETED, delete);
			log.debug(num + "番目のメッセージにDELETEDフラグをセットしました。");
		} catch (MessagingException e) {
			throw new MailException("DELETEDフラグのセットに失敗しました。", e);
		}
		return mailConverter.convertIntoMail(mimeMessage);
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#getMails(boolean)
	 */
	public ReceivedMail[] getMails(boolean delete) throws MailException {
		MimeMessage[] mimeMessages = getMessages(delete);
		return mailConverter.convertIntoMails(mimeMessages);
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#getMessage(int)
	 */
	public synchronized MimeMessage getMessage(int num) throws MailException {
		checkIfCurrentFolderIsOpen();
		try {
			return (MimeMessage)currentFolder.getMessage(num);
		} catch (MessagingException e) {
			log.error("メッセージの取得に失敗しました。", e);
			throw new MailFetchException("メッセージの取得に失敗しました。", e);
		}
	}

	public synchronized MimeMessage[] getMessages(boolean delete) throws MailException {
		checkIfCurrentFolderIsOpen();
		try {
			Message[] messages = currentFolder.getMessages();
			if (log.isInfoEnabled()) {
				if (messages.length > 0) {
					log.info(messages.length + "通のメールを受信します。");
				} else {
					log.info("受信するメールはありません。");
				}
			}
			// SEENフラグを立てる
			currentFolder.setFlags(messages, new Flags(Flags.Flag.SEEN), true);
			// DELETEDフラグを立てる
			if (delete) {
				currentFolder.setFlags(messages, new Flags(Flags.Flag.DELETED), true);
			}
			MimeMessage[] mimeMessages = new MimeMessage[messages.length];
			for (int i = 0; i < messages.length; i++) {
				mimeMessages[i] = (MimeMessage)messages[i];
			}
			return mimeMessages;
		} catch (MessagingException e) {
			log.error("メッセージの取得に失敗しました。", e);
			throw new MailFetchException("メッセージの取得に失敗しました。", e);
		}
	}

	/**
	 * @see org.sxb.mail.fetch.FetchMailPro#isConnected()
	 */
	public boolean isConnected() {
		return store != null && store.isConnected();
	}

	/**
	 *  メールサーバのホスト名、またはIPアドレスを返します。
	 * 
	 * @return  メールサーバのホスト名、またはIPアドレス
	 */
	public String getHost() {
		return host;
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
	 * メールサーバの認証パスワードを返します。
	 * 
	 * @return メールサーバの認証パスワード
	 */
	public String getPassword() {
		return password;
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
	 * メール受信に使用するプロトコロルをセットします。
	 * 
	 * @return プロトコル
	 */
	public String getProtocol() {
		return protocol;
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
	 * @return 認証ユーザ名
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * メールサーバの認証ユーザ名をセットします。
	 * 
	 * @param username 認証ユーザ名
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return ポート番号
	 */
	public int getPort() {
		return port;
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
	 * JavaMailのデバッグが有効かどうか判定します。
	 * 
	 * @return JavaMailのデバッグが有効な場合 ture
	 */
	public boolean isJavaMailLogEnabled() {
		return javaMailLogEnabled;
	}

	/**
	 * JavaMailのデバッグを有効にするかどうか指定します。
	 * 有効にすると、<code>System.out</code>のデバッグメッセージが出力されます。<br>
	 * デフォルトは無効になっています。
	 * 
	 * @see javax.mail.session#setDebug(boolean)
	 * @param javaMailLogEnabled The javaMailLogEnabled to set.
	 */
	public void setJavaMailLogEnabled(boolean javaMailLogEnabled) {
		this.javaMailLogEnabled = javaMailLogEnabled;
	}

	/**
	 * MailConveterインターフェースの実装インスタンスをセットします。
	 * デフォルトでは、MailConverterImplが使用されます。
	 * 
	 * @see org.sxb.mail.fetch.MailConveter
	 * @see org.sxb.mail.fetch.impl.MailConveterImpl
	 * @param mailConverter MailConveterインターフェースの実装インスタンス
	 */
	public void setMailConverter(MailConverter mailConverter) {
		this.mailConverter = mailConverter;
	}
}