package org.sxb.mail.fetch;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.sxb.mail.Mail;

/**
 * 受信メール。
 * <p>
 * <code>FetchMail</code>、<code>FetchMailPro</code>の実装クラスで受信したメールが、
 * インターネットメールとしての仕様を満たしていないヘッダ(FromやToなど)の値がセットされていた場合、
 * そのヘッダに該当する<code>ReceivedMail</code>インスタンスのプロパティには何もセットされません。
 * 
 * @since 1.2
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: ReceivedMail.java,v 1.1.1
 */
public class ReceivedMail extends Mail {

	private String replySubjectPrefix = "Re: ";

	private Date date;

	private String messageId;

	private int size;

	private List<ReceivedHeader> receivedHeaders;

	private MimeMessage message;

	/**
	 * コンストラクタ。
	 */
	public ReceivedMail() {
		super();
	}

	/**
	 * コンストラクタ。
	 * 
	 * @param charset 
	 */
	public ReceivedMail(String charset) {
		super(charset);
	}

	/**
	 * コピーコンストラクタ。
	 * 
	 * @param original 
	 */
	public ReceivedMail(Mail original) {
		super(original);
	}

	/**
	 * 送信日時を返します。
	 * <p>
	 * 注: メールの受信日時ではありません。
	 * 
	 * @return 送信日時
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * 送信日時をセットします。
	 * 
	 * @param date 送信日時
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * 前後に&lt;&gt;が付いたメッセージIDを返します。
	 * 受信メールにメッセージIDが存在しない場合はnullを返します。
	 * 
	 * @return 前後に&lt;&gt;が付いたメッセージID、またはnull
	 */
	public String getMessageId() {
		if (messageId == null || messageId.length() == 0) {
			return null;
		}
		if (messageId.startsWith("<") && messageId.endsWith(">")) {
			return messageId;
		}
		return "<" + messageId + ">";
	}

	/**
	 * メッセージIDを返します。前後に&lt;&gt;は付きません。
	 * 受信メールにメッセージIDが存在しない場合はnullを返します。
	 * 
	 * @return メッセージID、またはnull
	 */
	public String getMessageIdWithoutBracket() {
		if (messageId == null || messageId.length() == 0) {
			return null;
		}
		if (messageId.startsWith("<") && messageId.endsWith(">")) {
			return messageId.substring(1, messageId.length() - 1);
		}
		return messageId;
	}

	/**
	 * メッセージIDをセットします。
	 * 
	 * @param messageId メッセージID
	 */
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	/**
	 * In-Reply-Toヘッダの値を返します。
	 * In-Reply-Toヘッダがない場合はnullを返します。
	 * 
	 * @return In-Reply-Toヘッダの値
	 */
	public String getInReplyTo() {
		return headers == null ? null : (String)headers.get("In-Reply-To");
	}

	/**
	 * Referencesヘッダの値を返します。
	 * Referencesヘッダがない場合はnullを返します。
	 * 
	 * @return Referencesヘッダの値
	 */
	public String getRefereces() {
		return headers == null ? null : (String)headers.get("References");
	}

	/**
	 * @return 返信時の件名に付ける接頭辞
	 */
	public String getReplySubjectPrefix() {
		return replySubjectPrefix;
	}

	/**
	 * 返信時の件名に付ける接頭辞をセットします。
	 * デフォルトは「Re: 」。
	 * 
	 * @param replySubjectPrefix 返信時の件名に付ける接頭辞
	 */
	public void setReplySubjectPrefix(String replySubjectPrefix) {
		this.replySubjectPrefix = replySubjectPrefix;
	}

	/**
	 * メール内容を出力します。<br>
	 * メールのソースに似たフォーマットで出力されます。
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer(1000);
		buf.append("Mail\n");
		buf.append("Return-Path: ").append(returnPath).append("\n");
		buf.append("Message-ID: ").append(messageId).append("\n");
		buf.append("Date: ").append(date).append("\n");
		buf.append("From: ").append(from != null ? from.toUnicodeString() : null).append("\n");
		buf.append("To: ").append(arrayToCommaDelimitedString(to)).append("\n");
		buf.append("Cc: ").append(arrayToCommaDelimitedString(cc)).append("\n");
		buf.append("Bcc: ").append(arrayToCommaDelimitedString(bcc)).append("\n");
		buf.append("Reply-To: ").append(replyTo != null ? replyTo.toUnicodeString() : null).append(
				"\n");
		buf.append("Subject: ").append(subject).append("\n");

		if (headers != null) {
			for (Iterator<?> itr = headers.keySet().iterator(); itr.hasNext();) {
				String header = (String)itr.next();
				String value = (String)headers.get(header);
				buf.append(header).append(": ").append(value).append("\n");
			}
		}

		buf.append("\n");
		buf.append(text);

		if (htmlText != null) {
			buf.append("\n\n-----\n\n");
			buf.append(htmlText);
		}

		if (isFileAttached()) {
			buf.append("\n\nAttachments\n");
			for (int i = 0, num = attachmentFiles.size(); i < num; i++) {
				AttachmentFile f = (AttachmentFile)attachmentFiles.get(i);
				buf.append("[").append(i + 1).append("] ").append(f.getName()).append("\n");
			}
		}

		return buf.toString();
	}

	/**
	 * @return Returns the message.
	 */
	public MimeMessage getMessage() {
		return message;
	}

	/**
	 * @param message The message to set.
	 */
	public void setMessage(MimeMessage message) {
		this.message = message;
	}

	/**
	 * メールサーバとの接続切断時に、このメールをメールサーバから削除します。
	 * 削除できるように設定ができた場合に true を返します。
	 * <p>
	 * このメソッドは、<code>FetchMailPro</code>のメソッドによって取得された
	 * <code>ReceivedMail</code>インスタンスでのみ有効です。
	 * また、<code>FetchMailPro</code>インスタンスがメールサーバに
	 * 接続されている状態での呼び出しのみ有効です。<br>
	 * これらの条件が満たされない時にこのメソッドが呼び出された場合
	 * false を返します。
	 * 
	 * TODO: うまく動いてない。
	 * 
	 * @see FetchMailPro
	 * @param delete 削除するように設定する場合 true
	 * @return 削除設定が正常に行われた場合 true
	 */
	public boolean setDelete(boolean delete) {
		if (message != null) {
			try {
				message.setFlag(Flags.Flag.DELETED, delete);
			} catch (MessagingException e) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * メールのサイズ(容量)を返します。単位はbyte。
	 * この値は厳密なものではないので注意してください。
	 * 
	 * @see MimeMessage#getSize()
	 * @return メールのサイズ(単位はbyte)
	 */
	public int getSize() {
		return size;
	}

	/**
	 * メールのサイズ(容量)をセットします。単位はbyte。
	 * 
	 * @param size メールのサイズ(単位はbyte)
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * 添付ファイルのFileインスタンス配列を返します。
	 * 添付ファイルがない場合は空の配列を返します。
	 * 
	 * @return 添付ファイルのFileインスタンス配列
	 */
	public File[] getFiles() {
		AttachmentFile[] aFiles = getAttachmentFiles();
		File[] files = new File[aFiles.length];
		for (int i = 0; i < aFiles.length; i++) {
			AttachmentFile aFile = aFiles[i];
			files[i] = aFile.getFile();
		}
		return files;
	}

	/**
	 * このメールの返信メール用Mailインスタンスを生成して返します。
	 * <ul>
	 * <li>宛先(Toアドレス)には、このメールのReply-To、またはFromがセットされます。</li>
	 * <li>件名には、このメールの件名が大文字小文字問わず「Re:」で始まっていなければ、「Re: 」を頭に付けた件名がセットされます。「Re:」で始まっている場合には、その件名をそのままセットします。</li>
	 * <li>本文には、何もセットされません。</li>
	 * <li>このメールにMessage-IDがセットされていれば、In-Reply-Toヘッダにその値がセットされます。</li>
	 * <li>このメールにMessage-IDがセットされていれば、Referencesヘッダにその値が加えられます。</li>
	 * </ul>
	 * 
	 * @return 返信用のMailインスタンス
	 */
	public Mail reply() {
		Mail mail = new Mail();

		// 宛先
		if (getReplyTo() != null) {
			mail.addTo(getReplyTo());
		} else {
			mail.addTo(getFrom());
		}

		// 件名
		String subject = getSubject();
		if ((subject.length() >= 3 && !"Re:".equalsIgnoreCase(subject.substring(0, 3)))
				|| subject.length() < 3) {
			subject = replySubjectPrefix + subject;
		}
		mail.setSubject(subject);

		// In-Reply-To, References
		String messageId = getMessageId();
		if (messageId != null && !"<>".equals(messageId)) {
			String references = getRefereces();
			if (references != null) {
				references = messageId + " " + references;
			} else if (getInReplyTo() != null) {
				references = messageId + " " + getInReplyTo();
			} else {
				references = messageId;
			}
			mail.addHeader("References", references);
			mail.addHeader("In-Reply-To", messageId);
		}

		return mail;
	}

	/**
	 * Receivedヘッダフィールドを追加します。
	 * 
	 * @param rh Receivedヘッダフィールド
	 */
	public void addReceviedHeader(ReceivedHeader rh) {
		if (receivedHeaders == null) {
			receivedHeaders = new ArrayList<ReceivedHeader>();
		}
		receivedHeaders.add(rh);
	}

	/**
	 * Receivedヘッダフィールドの配列を返します。<br>
	 * 自分のサーバ(このメールが届いたサーバ)から送信元のメールサーバを辿る順で並んでいます。<br>
	 * 受信メールがReceivedヘッダフィールドを持たない、または解析できなかった場合は空の配列を返します。
	 * 
	 * @return Receivedヘッダフィールドの配列
	 */
	public ReceivedHeader[] getReceivedHeaders() {
		if (receivedHeaders == null) {
			return new ReceivedHeader[0];
		}
		return (ReceivedHeader[])receivedHeaders
				.toArray(new ReceivedHeader[receivedHeaders.size()]);
	}

	/**
	 * Receviedヘッダフィールドを表すクラス。
	 */
	public static class ReceivedHeader {

		private String from;

		private String by;

		/**
		 * @param from メールを送信したサーバのホスト名
		 * @param by メールを受信したサーバのホスト名
		 */
		public ReceivedHeader(String from, String by) {
			this.from = from;
			this.by = by;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "Sent from " + from + " and received by " + by;
		}

		/**
		 * メールを受信したサーバのホスト名を返します。
		 * 
		 * @return メールを受信したサーバのホスト名
		 */
		public String getBy() {
			return by;
		}

		/**
		 * メールを送信したサーバのホスト名を返します。
		 * 
		 * @return メールを送信したサーバのホスト名
		 */
		public String getFrom() {
			return from;
		}
	}
}