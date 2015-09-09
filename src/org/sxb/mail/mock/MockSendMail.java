package org.sxb.mail.mock;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.sxb.mail.Mail;
import org.sxb.mail.MailBuildException;
import org.sxb.mail.MailException;
import org.sxb.mail.SendMail;
import org.sxb.mail.impl.MimeMessageBuilder;

/**
 * SendMailImplクラスのMock。<br>
 * 実存するSMTPサーバを設定しても、実際には送信されません。
 * デバッグモードを有効にすると、メールを送信するタイミングでコンソールに送信メール内容が出力されます。
 * <p>
 * Mailインスタンスを addExpectedMail() にセットし verify() メソッドを実行すると、send() されたMailインスタンスと全てのプロパティ(XHeader、添付ファイルを除く)が一致しなければAssertionFailedExceptionがスローされます。
 * <p>
 * 例えば、send() されたMailインスタンスのFromアドレスと件名だけチェックし、その他のプロパティはチェックしたくない場合は、MockMailインスタンスを使用します。
 * <pre>Mail sentMail = new Mail();
 *sentMail.setFrom("from@example.com");
 *sentMail.setSubject("件名");
 *sentMail.addTo("to@example.com");
 *sentMail.setText("動的生成される本文");
 *
 *Mail expectedMail = new Mail();
 *expectedMail.setFrom("from@example.com");
 *expectedMail.setSubject("件名");
 *
 *MockMail mockMail = new MockMail();
 *mockMail.setFrom("from@example.com");
 *mockMail.setSubject("件名");
 *
 *MockSendMail sendMail = new MockSendMail();
 *sendMail.addExpectedMail(expectedMail);
 *sendMail.send(sentMail);
 *sendMail.verify(); // 失敗 AssertionFailedException
 *
 *sendMail = new MockSendMail();
 *sendMail.addExpectedMail(mockMail);
 *sendMail.send(sentMail);
 *sendMail.verify(); // 成功</pre>
 * <p>
 * <strong>注:</strong> 添付ファイルは比較対象になりません。
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: MockSendMail.java,v 1.10.2
 */
public class MockSendMail implements SendMail {

	/** デフォルトのプロトコル。「smtp」 */
	public static final String DEFAULT_PROTOCOL = "smtp";

	/** デフォルトのポート。「-1」 */
	public static final int DEFAULT_PORT = -1;

	/** デフォルトのSMTPサーバ。「localhost」 */
	public static final String DEFAULT_HOST = "localhost";

	/** ISO-2022-JP */
	public static final String JIS_CHARSET = "ISO-2022-JP";

	@SuppressWarnings("unused")
	private static final String RETURN_PATH_KEY = "mail.smtp.from";

	private String protocol = DEFAULT_PROTOCOL;

	private String host = DEFAULT_HOST;

	private int port = DEFAULT_PORT;

	private String username;

	private String password;

	private String charset = JIS_CHARSET;

	private String returnPath;

	private List<Mail> sentMails;

	private List<MimeMessage> mimeMessages;

	private List<Mail> expectedMails;

	private boolean debug;

	/**
	 * コンストラクタ。
	 */
	public MockSendMail() {
		super();
		initialize();
	}

	/**
	 * MockSendMailインスタンスを初期化します。
	 */
	public void initialize() {
		sentMails = new ArrayList<Mail>();
		expectedMails = new ArrayList<Mail>();
		mimeMessages = new ArrayList<MimeMessage>();
	}

	/**
	 * 送信されたメールのMimeMessageインスタンスを返します。
	 * 送信順の配列です。
	 * 
	 * @return 送信メールのMimeMessageインスタンス配列
	 */
	public MimeMessage[] getMimeMessages() {
		return (MimeMessage[])mimeMessages.toArray(new MimeMessage[mimeMessages.size()]);
	}

	/**
	 * 送信されたMailインスタンスを返します。送信順の配列です。
	 * 
	 * @return 送信メールのMailインスタンス配列
	 */
	public Mail[] getSentMails() {
		return (Mail[])sentMails.toArray(new Mail[sentMails.size()]);
	}

	/**
	 * デバッグモードが有効になっているか判定します。
	 * 
	 * @return Returns 有効になっている場合 true
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * デバッグモード(コンソールにログを出力)を有効にします。
	 * デフォルトは無効です。
	 * 
	 * @param debug デバッグモードを有効にする場合 true
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * デバッグモードが有効のとき、指定されたメッセージをコンソールに出力します。
	 * 
	 * @param message コンソール出力するメッセージ
	 */
	private void debug(String message) {
		if (debug) {
			System.out.println("[" + Thread.currentThread().getName() + "] DEBUG "
					+ getClass().getName() + " - " + message);
		}
	}

	/**
	 * 
	 * @param expectedMail
	 */
	public void addExpectedMail(Mail expectedMail) {
		expectedMails.add(expectedMail);
	}

	/**
	 * 
	 * @param expectedMails
	 */
	public void addExpectedMail(Mail[] expectedMails) {
		for (int i = 0; i < expectedMails.length; i++) {
			addExpectedMail(expectedMails[i]);
		}
	}

	/**
	 * 
	 * @throws AssertionFailedException
	 */
	public void verify() throws AssertionFailedException {
		debug("======================================================");
		debug("                      verify()                        ");
		debug("======================================================");

		// メールの数を比較
		int numOfExpectedMails = expectedMails.size();
		int numOfSentMails = sentMails.size();
		if (numOfExpectedMails != numOfSentMails) {
			throw new AssertionFailedException("期待メール数<" + numOfExpectedMails + ">と送信メール数<"
					+ numOfSentMails + ">が一致しませんでした。");
		}

		debug("期待メール数と送信メール数は一致しました。[" + numOfExpectedMails + "通]");

		// メール内容を比較
		for (int i = 0; i < numOfExpectedMails; i++) {
			Mail expected = (Mail)expectedMails.get(i);
			Mail sent = (Mail)sentMails.get(i);
			debug((i + 1) + "通目のチェックを開始します。("
					+ ((expected instanceof MockMail) ? "MockMail" : "Mail") + " - Mail)");
			checkEquality(expected, sent, i + 1);
			debug((i + 1) + "通目の期待メールと送信メール内容は一致しました。");
		}

		debug("verifyプロセスは全て成功しました。");
		debug("======================================================");
	}

	/**
	 * @param expected
	 * @param sent 
	 * @throws AssertionFailedException
	 */
	public void checkEquality(Mail expected, Mail sent, int num) throws AssertionFailedException {
		boolean mockMode = (expected instanceof MockMail);

		// マルチパートメールの場合
		if (expected.isMultipartMail()) {

			// HTML
			if (!mockMode) {
				if ((expected.getHtmlText() == null && sent.getHtmlText() != null)
						|| (expected.getHtmlText() != null && sent.getHtmlText() == null)
						|| (!expected.getHtmlText().equals(sent.getHtmlText()))) {
					throwExceptioWithMessage("HTML本文", expected.getHtmlText(), sent.getHtmlText(),
							num);
				}
			} else if (mockMode && expected.getHtmlText() != null) {
				if (!expected.getHtmlText().equals(sent.getHtmlText())) {
					throwExceptioWithMessage("HTML本文", expected.getHtmlText(), sent.getHtmlText(),
							num);
				}
			}
		}

		// Return-Path
		if (!mockMode || (mockMode && expected.getReturnPath() != null)) {
			if (expected.getReturnPath() != null && sent.getReturnPath() != null) {
				if (!expected.getReturnPath().equals(sent.getReturnPath())) {
					throwExceptioWithMessage("Return-Pathアドレス", expected.getReturnPath()
							.toUnicodeString(), sent.getReturnPath().toUnicodeString(), num);
				}
			} else if ((expected.getReturnPath() != null && sent.getReturnPath() == null)
					|| (expected.getReturnPath() == null && sent.getReturnPath() != null)) {
				throw new AssertionFailedException();
			}
		}

		// From
		if (!mockMode || (mockMode && expected.getFrom() != null)) {
			if (expected.getFrom() != null && sent.getFrom() != null) {
				if (!EqualityCheck.equals(expected.getFrom(), sent.getFrom())) {
					throwExceptioWithMessage("Fromアドレス", expected.getFrom().toUnicodeString(), sent
							.getFrom().toUnicodeString(), num);
				}
			} else if ((expected.getFrom() != null && sent.getFrom() == null)
					|| (expected.getFrom() == null && sent.getFrom() != null)) {
				throw new AssertionFailedException();
			}
		}

		// to
		InternetAddress[] expectedAddresses = expected.getTo();
		InternetAddress[] sentAddresses = sent.getTo();
		if (!mockMode || (mockMode && expectedAddresses.length > 0)) {
			if (expectedAddresses.length != sentAddresses.length) {
				throwExceptioWithMessage("Toアドレス数", Integer.toString(expectedAddresses.length),
						Integer.toString(sentAddresses.length), num);
			}
			for (int i = 0; i < expectedAddresses.length; i++) {
				if (!EqualityCheck.equals(expectedAddresses[i], sentAddresses[i])) {
					throwExceptioWithMessage("Toアドレス", expectedAddresses[i].toUnicodeString(),
							sentAddresses[i].toUnicodeString(), num);
				}
			}
		}

		// cc
		expectedAddresses = expected.getCc();
		sentAddresses = sent.getCc();
		if (!mockMode || (mockMode && expectedAddresses.length > 0)) {
			if (expectedAddresses.length != sentAddresses.length) {
				throwExceptioWithMessage("Ccアドレス数", Integer.toString(expectedAddresses.length),
						Integer.toString(sentAddresses.length), num);
			}
			for (int i = 0; i < expectedAddresses.length; i++) {
				if (!EqualityCheck.equals(expectedAddresses[i], sentAddresses[i])) {
					throwExceptioWithMessage("Ccアドレス", expectedAddresses[i].toUnicodeString(),
							sentAddresses[i].toUnicodeString(), num);
				}
			}
		}

		// bcc
		expectedAddresses = expected.getBcc();
		sentAddresses = sent.getBcc();
		if (!mockMode || (mockMode && expectedAddresses.length > 0)) {
			if (expectedAddresses.length != sentAddresses.length) {
				throwExceptioWithMessage("Bccアドレス数", Integer.toString(expectedAddresses.length),
						Integer.toString(sentAddresses.length), num);
			}
			for (int i = 0; i < expectedAddresses.length; i++) {
				if (!EqualityCheck.equals(expectedAddresses[i], sentAddresses[i])) {
					throwExceptioWithMessage("Bccアドレス", expectedAddresses[i].toUnicodeString(),
							sentAddresses[i].toUnicodeString(), num);
				}
			}
		}

		// Reply-To
		if (!mockMode || (mockMode && expected.getReplyTo() != null)) {
			if (expected.getReplyTo() != null && sent.getReplyTo() != null) {
				if (!EqualityCheck.equals(expected.getReplyTo(), sent.getReplyTo())) {
					throwExceptioWithMessage("ReplyToアドレス",
							expected.getReplyTo().toUnicodeString(), sent.getReplyTo()
									.toUnicodeString(), num);
				}
			} else if ((expected.getReplyTo() != null && sent.getReplyTo() == null)
					|| (expected.getReplyTo() == null && sent.getReplyTo() != null)) {
				throw new AssertionFailedException();
			}
		}

		// 件名
		if (!mockMode || (mockMode && expected.getSubject().length() > 0)) {
			if (!expected.getSubject().equals(sent.getSubject())) {
				throwExceptioWithMessage("件名", expected.getSubject(), sent.getSubject(), num);
			}
		}

		// 本文
		if (!mockMode || (mockMode && expected.getText().length() > 0)) {
			if (!expected.getText().equals(sent.getText())) {
				throwExceptioWithMessage("本文", expected.getText(), sent.getText(), num);
			}
		}

	}

	/**
	 * 引数の値を受けてエラーメッセージを生成し、AssertionFailedErrorをスローします。
	 * 
	 * @param name 一致しなかった項目名
	 * @param expectedValue 期待値
	 * @param sentValue 実際値
	 * @param num N番目のメール
	 * @throws AssertionFailedException 生成された例外
	 */
	protected void throwExceptioWithMessage(String name, String expectedValue, String sentValue,
											int num) throws AssertionFailedException {
		String message = num + "番目のメッセージで、「" + name + "」が一致しませんでした。期待値='" + expectedValue
				+ "', 送信値='" + sentValue + "'";

		debug(message);
		debug("verifyプロセスは失敗しました。");
		debug("******************************************************");

		throw new AssertionFailedException(message);
	}

	/**
	 * @see org.sxb.mail.SendMail#send(org.sxb.mail.Mail)
	 */
	public void send(Mail mail) throws MailException {
		send(new Mail[] { mail });
	}

	/**
	 * @see org.sxb.mail.SendMail#send(org.sxb.mail.Mail[])
	 */
	public void send(Mail[] mails) throws MailException {
		debug("SMTPサーバ[" + host + "]に接続するフリ。");
		debug("SMTPサーバ[" + host + "]に接続したフリ。");

		Session session = Session.getInstance(new Properties());
		for (int i = 0; i < mails.length; i++) {

			Mail mail = mails[i];

			// MimeMessageを生成
			MimeMessage message = new MimeMessage(session);
			MimeMessageBuilder builder = new MimeMessageBuilder(message);
			try {
				builder.buildMimeMessage(mail);
			} catch (UnsupportedEncodingException e) {
				throw new MailBuildException("サポートされていない文字コードが指定されました。", e);
			} catch (MessagingException e) {
				throw new MailBuildException("MimeMessageの生成に失敗しました。", e);
			}
			mimeMessages.add(message);

			debug("メールを送信するフリ。");
			sentMails.add(mail);
			debug(mail.toString());
			debug("メールを送信したフリ。");
		}

		debug("SMTPサーバ[" + host + "]との接続を切断するフリ。");
		debug("SMTPサーバ[" + host + "]との接続を切断したフリ。");
	}

	/**
	 * @see org.sxb.mail.SendMail#send(javax.mail.internet.MimeMessage)
	 */
	public void send(MimeMessage mimeMessage) throws MailException {
		throw new UnsupportedOperationException("申し訳ございません。MockSendMailでは、このメソッドをサポートしていません。");
	}

	/**
	 * @see org.sxb.mail.SendMail#send(javax.mail.internet.MimeMessage[])
	 */
	public void send(MimeMessage[] mimeMessages) throws MailException {
		throw new UnsupportedOperationException("申し訳ございません。MockSendMailでは、このメソッドをサポートしていません。");
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
	 * 
	 * @param charset エンコーディングに使用する文字コード
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * セットされたSMTPサーバのホスト名、またはIPアドレスを返します。
	 * 
	 * @return SMTPサーバのホスト名、またはIPアドレス
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
	 * @return Return-Pathアドレス
	 */
	public String getReturnPath() {
		return returnPath;
	}

	/**
	 * Return-Pathアドレスをセットします。
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

}