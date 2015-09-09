package org.sxb.mail.impl;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.sxb.log.Logger;
import org.sxb.mail.Mail;
import org.sxb.mail.MailAuthenticationException;
import org.sxb.mail.MailBuildException;
import org.sxb.mail.MailException;
import org.sxb.mail.MailSendException;
import org.sxb.mail.NotConnectedException;
import org.sxb.mail.SendMailPro;

/**
 * SendMailProインターフェースの実装クラス。
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: SendMailProImpl.java,v 1.3
 */
public class SendMailProImpl implements SendMailPro {

	/** smtp */
	public static final String DEFAULT_PROTOCOL = "smtp";

	/** -1 */
	public static final int DEFAULT_PORT = -1;

	/** localhost */
	public static final String DEFAULT_HOST = "localhost";

	private static final boolean DEFAULT_USE_TLS = false;
	
	/** ISO-2022-JP */
	public static final String JIS_CHARSET = "ISO-2022-JP";

	private static final String RETURN_PATH_KEY = "mail.smtp.from";

	private static Logger log = Logger.getLogger(SendMailProImpl.class);

	/** 接続タイムアウト */
	private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;

	/** 読込タイムアウト */
	private static final int DEFAULT_READ_TIMEOUT = 5000;

	private String protocol = DEFAULT_PROTOCOL;

	private String host = DEFAULT_HOST;

	private int port = DEFAULT_PORT;

	private boolean isUseTLS = DEFAULT_USE_TLS;
	
	private String username;

	private String password;

	private String charset = JIS_CHARSET;

	private String returnPath;

	private Session session;

	private Transport transport;

	private boolean connected;

	private String messageId;

	protected int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

	protected int readTimeout = DEFAULT_READ_TIMEOUT;

	/**
	 * コンストラクタ。
	 */
	public SendMailProImpl() {}

	/**
	 * コンストラクタ。使用するSMTPサーバを指定します。
	 * 
	 * @param host SMTPサーバのホスト名、またはIPアドレス
	 */
	public SendMailProImpl(String host) {
		this();
		setHost(host);
	}

	/**
	 * @see org.sxb.mail.SendMailPro#connect()
	 */
	public synchronized void connect() throws MailException {
		if (session == null) {
			initSession();
		}

		// グローバルReturn-Pathの設定
		putOnReturnPath(this.returnPath);

		try {
			// SMTPサーバに接続
			log.debug("SMTPサーバ[" + host + "]に接続します。");

			transport = session.getTransport(protocol);
			transport.connect(host, port, username, password);
		} catch (AuthenticationFailedException ex) {
			log.error("SMTPサーバ[" + host + "]への接続認証に失敗しました。", ex);
			throw new MailAuthenticationException(ex);
		} catch (MessagingException ex) {
			log.error("SMTPサーバ[" + host + "]への接続に失敗しました。", ex);
			throw new MailSendException("SMTPサーバ[" + host + "]への接続に失敗しました。", ex);
		}

		log.debug("SMTPサーバ[" + host + "]に接続しました。");

		connected = true;
	}

	/**
	 * <p>
	 * {@link #initProperties()} から返された Properties を引数として Session の初期化を行います。
	 * </p>
	 */
	private void initSession() {
		Properties prop = initProperties();
		session = Session.getInstance(prop);
	}

	/**
	 * <p>Session 生成時に渡される Properties オブジェクトを生成して返します。<br>
	 * デフォルトの実装では、タイムアウトおよび SMTP 認証有効化のプロパティを設定しています。<p>
	 * 
	 * @return 生成した Properties オブジェクト
	 */
	protected Properties initProperties() {
		Properties prop = new Properties();
		prop.put("mail.smtp.host", host);
		// タイムアウトの設定
		prop.put("mail.smtp.connectiontimeout", String.valueOf(connectionTimeout));
		prop.put("mail.smtp.timeout", String.valueOf(readTimeout));
		//	mail.smtp.authプロパティの設定
		if (username != null && !"".equals(username) && password != null && !"".equals(password)) {
			prop.put("mail.smtp.auth", "true");
		}
		
		if(isUseTLS){
			 prop.put("mail.smtp.starttls.enable", "true");
		}
		return prop;
	}

	/**
	 * @see org.sxb.mail.SendMailPro#disconnect()
	 */
	public synchronized void disconnect() throws MailException {
		if (connected) {
			try {
				log.debug("SMTPサーバ[" + host + "]との接続を切断します。");

				// SMTPサーバとの接続を切断
				transport.close();
				connected = false;

				log.debug("SMTPサーバ[" + host + "]との接続を切断しました。");
			} catch (MessagingException ex) {
				log.error("SMTPサーバ[" + host + "]との接続切断に失敗しました。", ex);
				throw new MailException("SMTPサーバ[" + host + "]との接続切断に失敗しました。");
			} finally {
				// グローバルReturn-Pathの解除
				releaseReturnPath(false);
			}
		} else {
			log.warn("SMTPサーバ[" + host + "]との接続が確立されていない状態で、接続の切断がリクエストされました。");
		}
	}

	/**
	 * ReturnPathをセットします。
	 * 
	 * @param returnPath
	 */
	private void putOnReturnPath(String returnPath) {
		if (returnPath != null) {
			session.getProperties().put(RETURN_PATH_KEY, returnPath);
			log.debug("Return-Path[" + returnPath + "]を設定しました。");
		}
	}

	/**
	 * ReturnPathの設定をクリアします。
	 * <p>
	 * setGlobalReturnPathAgainがtrueに指定されている場合、一旦Return-Path設定をクリアした後に、
	 * グローバルなReturn-Path(setReturnPath()メソッドで、このインスタンスにセットされたReturn-Pathアドレス)を設定します。
	 * グローバルなReturn-PathがセットされていなければReturn-Pathはクリアされたままになります。
	 * <p>
	 * クリアされた状態でsend()メソッドが実行されると、Fromの値がReturn-Pathに使用されます。
	 * 
	 * @param setGlobalReturnPathAgain Return-Path設定をクリアした後、再度グローバルなReturn-Pathをセットする場合 true
	 */
	private void releaseReturnPath(boolean setGlobalReturnPathAgain) {
		session.getProperties().remove(RETURN_PATH_KEY);
		log.debug("Return-Path設定をクリアしました。");

		if (setGlobalReturnPathAgain && this.returnPath != null) {
			putOnReturnPath(this.returnPath);
		}
	}

	/**
	 * @see org.sxb.mail.SendMailPro#send(javax.mail.internet.MimeMessage)
	 */
	public void send(MimeMessage mimeMessage) throws MailException {
		Address[] addresses;
		try {
			addresses = mimeMessage.getAllRecipients();
		} catch (MessagingException ex) {
			log.error("メールの送信に失敗しました。", ex);
			throw new MailSendException("メールの送信に失敗しました。", ex);
		}
		processSend(mimeMessage, addresses);
	}

	/**
	 * @param mimeMessage 
	 */
	private void processSend(MimeMessage mimeMessage, Address[] addresses) {
		if (!connected) {
			log.error("SMTPサーバへの接続が確立されていません。");
			throw new NotConnectedException("SMTPサーバへの接続が確立されていません。");
		}

		try {
			// 送信日時をセット
			mimeMessage.setSentDate(new Date());
			mimeMessage.saveChanges();
			// 送信
			log.debug("メールを送信します。");
			transport.sendMessage(mimeMessage, addresses);
			log.debug("メールを送信しました。");
		} catch (MessagingException ex) {
			log.error("メールの送信に失敗しました。", ex);
			throw new MailSendException("メールの送信に失敗しました。", ex);
		}
	}

	/**
	 * @see org.sxb.mail.SendMailPro#send(org.sxb.mail.Mail)
	 */
	public void send(Mail mail) throws MailException {
		if (mail.getReturnPath() != null) {
			sendMailWithReturnPath(mail);
		} else {
			sendMail(mail);
		}
	}

	/**
	 * 指定されたMailからMimeMessageを生成し、send(MimeMessage)メソッドに渡します。
	 * 
	 * @param mail
	 * @throws MailException
	 */
	private void sendMail(Mail mail) throws MailException {
		// MimeMessageの生成
		MimeMessage message = createMimeMessage();
		if (isMessageIdCustomized()) {
			mail.addHeader("Message-ID", ((OMLMimeMessage)message).getMessageId());
		}
		MimeMessageBuilder builder = new MimeMessageBuilder(message, charset);
		try {
			builder.buildMimeMessage(mail);
		} catch (UnsupportedEncodingException e) {
			throw new MailBuildException("サポートされていない文字コードが指定されました。", e);
		} catch (MessagingException e) {
			throw new MailBuildException("MimeMessageの生成に失敗しました。", e);
		}
		// 送信
		if (mail.getEnvelopeTo().length > 0) {
			log.debug("メールはenvelope-toアドレスに送信されます。");
			processSend(message, mail.getEnvelopeTo());
		} else {
			send(message);
		}
	}

	/**
	 * 指定されたMailにセットされたReturn-Pathを設定して、メールを送信します。
	 * 同期メソッドです。
	 * 
	 * @param mail
	 * @throws MailException
	 */
	private synchronized void sendMailWithReturnPath(Mail mail) throws MailException {
		putOnReturnPath(mail.getReturnPath().getAddress());

		sendMail(mail);

		releaseReturnPath(true);
	}

	/**
	 * 新しいMimeMessageオブジェクトを生成します。
	 * 
	 * @return 新しいMimeMessageオブジェクト
	 */
	public MimeMessage createMimeMessage() {
		if (isMessageIdCustomized()) {
			return new OMLMimeMessage(session, messageId);
		}
		return new MimeMessage(session);
	}

	/**
	 * Message-Idヘッダのドメイン部分を独自にセットしているかどうか判定します。
	 * 
	 * @return Message-Idヘッダのドメイン部分を独自にセットしている場合 true
	 */
	private boolean isMessageIdCustomized() {
		return messageId != null;
	}

	/**
	 * @return Sessionインスタンス
	 */
	protected Session getSession() {
		return session;
	}

	/**
	 * エンコーディングに使用する文字コードを返します。
	 * 
	 * @return エンコーディングに使用する文字コード
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * メールの件名や本文のエンコーディングに使用する文字コードを指定します。
	 * デフォルトは ISO-2022-JP です。
	 * <p>
	 * 日本語環境で利用する場合は通常変更する必要はありません。
	 * 
	 * @param charset エンコーディングに使用する文字コード
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * @return Returns the host.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * SMTPサーバのホスト名、またはIPアドレスをセットします。
	 * デフォルトは localhost です。
	 * 
	 * @param host SMTPサーバのホスト名、またはIPアドレス
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return SMTPサーバ認証パスワード
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * SMTPサーバの接続認証が必要な場合にパスワードをセットします。
	 * 
	 * @param password SMTPサーバ認証パスワード
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return SMTPサーバのポート番号
	 */
	public int getPort() {
		return port;
	}

	/**
	 * SMTPサーバのポート番号をセットします。
	 * 
	 * @param port SMTPサーバのポート番号
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	

	public boolean isUseTLS() {
		return isUseTLS;
	}

	public void setUseTLS(boolean isUseTLS) {
		this.isUseTLS = isUseTLS;
	}

	/**
	 * プロトコルを返します。
	 * 
	 * @return プロトコル
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * プロトコルをセットします。デフォルトは「smtp」。
	 * 
	 * @param protocol プロトコル
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return Return-Pathアドレス
	 */
	public String getReturnPath() {
		return returnPath;
	}

	/**
	 * Return-Pathアドレスをセットします。
	 * <p>
	 * 送信するMailインスタンスに指定されたFromアドレス以外のアドレスをReturn-Pathとしたい場合に使用します。
	 * ここでセットされたReturn-Pathより、MailインスタンスにセットされたReturn-Pathが優先されます。
	 * 
	 * @param returnPath Return-Pathアドレス
	 */
	public void setReturnPath(String returnPath) {
		this.returnPath = returnPath;
	}

	/**
	 * @return SMTPサーバ認証ユーザ名
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * SMTPサーバの接続認証が必要な場合にユーザ名をセットします。
	 * 
	 * @param username SMTPサーバ認証ユーザ名
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * 生成されるMimeMessageに付けられるMessage-Idヘッダのドメイン部分を指定します。<br>
	 * 指定されない場合(nullや空文字列の場合)は、JavaMailがMessage-Idヘッダを生成します。
	 * JavaMailが生成する「JavaMail.実行ユーザ名@ホスト名」のMessage-Idを避けたい場合に、このメソッドを使用します。
	 * <p>
	 * messageIdプロパティがセットされている場合、Mailから生成されるMimeMessageのMessage-Idには
	 * <code>タイムスタンプ + ランダムに生成される16桁の数値 + ここでセットされた値</code>
	 * が使用されます。
	 * <p>
	 * 生成されるMessage-Idの例。 (実際の数値部分は送信メール毎に変わります)<ul>
	 * <li>messageIdに'example.com'を指定した場合・・・1095714924963.5619528074501343@example.com</li>
	 * <li>messageIdに'@example.com'を指定した場合・・・1095714924963.5619528074501343@example.com (上と同じ)</li>
	 * <li>messageIdに'OML@example.com'を指定した場合・・・1095714924963.5619528074501343.OML@example.com</li>
	 * <li>messageIdに'.OML@example.com'を指定した場合・・・1095714924963.5619528074501343.OML@example.com (上と同じ)</li>
	 * </ul>
	 * <p>
	 * <strong>注:</strong> このMessage-Idは<code>send(Mail)</code>か<code>send(Mail[])</code>メソッドが呼びだれた時にのみ有効です。MimeMessageを直接送信する場合には適用されません。
	 * 
	 * @param messageId メールに付けられるMessage-Idヘッダのドメイン部分
	 * @throws IllegalArgumentException @を複数含んだ文字列を指定した場合
	 */
	public void setMessageId(String messageId) {
		if (messageId == null || messageId.length() < 1) {
			return;
		}
		String[] parts = messageId.split("@");
		if (parts.length > 2) {
			throw new IllegalArgumentException("messageIdプロパティに'@'を複数含むことはできません。[" + messageId
					+ "]");
		}
		this.messageId = messageId;
	}

	/**
	 * SMTPサーバとの接続タイムアウトをセットします。
	 * 単位はミリ秒。デフォルトは5,000ミリ秒(5秒)です。
	 * <p>
	 * -1を指定すると無限大になりますが、お薦めしません。
	 * 
	 * @since 1.1.4
	 * @param connectionTimeout SMTPサーバとの接続タイムアウト
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * SMTPサーバへの送受信時のタイムアウトをセットします。
	 * 単位はミリ秒。デフォルトは5,000ミリ秒(5秒)です。
	 * <p>
	 * -1を指定すると無限大になりますが、お薦めしません。
	 * 
	 * @since 1.1.4
	 * @param readTimeout SMTPサーバへの送受信時のタイムアウト
	 */
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
}