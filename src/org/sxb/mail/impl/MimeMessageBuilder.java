package org.sxb.mail.impl;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;

import org.sxb.mail.Mail;

/**
 * MimeMessageインスタンスを生成するクラス。Mail一通毎に生成されます。
 * 
 * @author Jeffson (jeffson.app@gmail.com)
 * @version $Id: MimeMessageBuilder.java,v 1.11.2
 */
public class MimeMessageBuilder {

	private MimeMessage mimeMessage;

	private String charset = Mail.JIS_CHARSET;

	private boolean hasRecipient = false;

	/**
	 * コンストラクタ。 デフォルトの文字コード ISO-2022-JP がエンコーディングに使用されます。
	 * 
	 * @param mimeMessage
	 */
	public MimeMessageBuilder(MimeMessage mimeMessage) {
		this.mimeMessage = mimeMessage;
	}

	/**
	 * コンストラクタ。 本文や件名のエンコーディングに使用する文字コードを指定します。
	 * 
	 * @param mimeMessage
	 * @param charset
	 *            エンコーディングに使用する文字コード
	 */
	public MimeMessageBuilder(MimeMessage mimeMessage, String charset) {
		this.mimeMessage = mimeMessage;
		this.charset = charset;
	}

	/**
	 * コンストラクタの引数で渡されたMimeMessageをそのまま返します。
	 * 
	 * @return MimeMessage
	 */
	public MimeMessage getMimeMessage() {
		return this.mimeMessage;
	}

	/**
	 * 指定されたメールからMimeMessageを生成します。
	 * 
	 * @param mail
	 *            MimeMessageのソースとなるMail
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public void buildMimeMessage(Mail mail)
			throws UnsupportedEncodingException, MessagingException {
		setCharset(mail);

		setTo(mail);
		setCc(mail);
		setBcc(mail);
		// 宛先の指定がない場合エラー
		if (!hasRecipient) {
			throw new MessagingException(
					"宛先の指定がありません。To、Cc、Bccのいずれか一つは指定する必要があります。");
		}
		setFrom(mail);
		setSubject(mail);
		setReplyTo(mail);
		setXHeaders(mail);
		setImportance(mail);

		if (mail.isMultipartMail()) {

			if (!mail.isFileAttached() && mail.isHtmlMail()) { // Plain text,
																// HTML

				if (mail.getText() != null && mail.getText().length() > 0) { // Plain
																				// text,
																				// HTML

					MimeMultipart textAndHtmlMultipart = new MimeMultipart(
							"alternative");
					setPlainText(mail, textAndHtmlMultipart);
					setHtmlText(mail, textAndHtmlMultipart);
					this.mimeMessage.setContent(textAndHtmlMultipart);

				} else { // HTML Only マルチパートは使用しない

					setHtmlText(mail.getHtmlText(), this.mimeMessage);

				}

			} else if (mail.isFileAttached() && mail.isHtmlMail()) { // Plain
																		// text,
																		// HMTL,
																		// File

				MimeMultipart textAndHtmlMultipart = new MimeMultipart(
						"alternative");
				setPlainText(mail, textAndHtmlMultipart);
				setHtmlText(mail, textAndHtmlMultipart);

				MimeMultipart containingMultipart = new MimeMultipart();
				MimeBodyPart textBodyPart = createMimeBodyPart(containingMultipart);
				textBodyPart.setContent(textAndHtmlMultipart);
				setAttachmentFiles(mail, containingMultipart);

				this.mimeMessage.setContent(containingMultipart);

			} else if (mail.isFileAttached() && !mail.isHtmlMail()) { // Plain
																		// text,
																		// File

				MimeMultipart textAndFileMultipart = new MimeMultipart();
				setPlainText(mail, textAndFileMultipart);
				setAttachmentFiles(mail, textAndFileMultipart);
				this.mimeMessage.setContent(textAndFileMultipart);

			} else { // Plain text only マルチパートは使用しない

				setText(mail.getText(), this.mimeMessage);

			}

		} else {

			setText(mail.getText(), this.mimeMessage);

		}

	}

	/**
	 * メールに使用するcharsetを決定します。 Mail#charsetが設定されている場合、その値が優先されます。
	 * 
	 * @since 1.2.1
	 * @param mail
	 */
	private void setCharset(Mail mail) {
		if (mail.getCharset() != null && !"".equals(mail.getCharset())) {
			charset = mail.getCharset();
		}
	}

	/**
	 * 
	 * @since 1.1
	 * 
	 * @param mail
	 * @param mimeMultipart
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	private void setAttachmentFiles(Mail mail, MimeMultipart mimeMultipart)
			throws MessagingException, UnsupportedEncodingException {
		Mail.AttachmentFile[] files = mail.getAttachmentFiles();
		for (int i = 0; i < files.length; i++) {
			MimeBodyPart bodyPart = createMimeBodyPart(mimeMultipart);
			Mail.AttachmentFile attachmentFile = files[i];
			addAttachment(attachmentFile.getName(),
					attachmentFile.getDataSource(), bodyPart);
		}
	}

	/**
	 * 
	 * @since 1.1
	 * 
	 * @param mail
	 * @param mimeMultipart
	 * @throws MessagingException
	 */
	private void setHtmlText(Mail mail, MimeMultipart mimeMultipart)
			throws MessagingException {
		if (mail.isHtmlMail()) {
			MimeBodyPart bodyPart = createMimeBodyPart(mimeMultipart);
			setHtmlText(mail.getHtmlText(), bodyPart);
		}
	}

	/**
	 * 
	 * @since 1.1
	 * 
	 * @param mail
	 * @param mimeMultipart
	 * @throws MessagingException
	 */
	private void setPlainText(Mail mail, MimeMultipart mimeMultipart)
			throws MessagingException {
		if (mail.getText() != null && mail.getText().length() > 0) {
			MimeBodyPart bodyPart = createMimeBodyPart(mimeMultipart);
			setText(mail.getText(), bodyPart);
		}
	}

	/**
	 * 新しいMimeBodyPartインスタンスを生成し、指定されたMimeMultipartに登録します。
	 * 
	 * このメソッドはマルチパートメール生成時にのみ呼び出すことができます。 プレーンテキストメール生成時には、mimeMulipartがnullなので、
	 * NullPointerExceptionがスローされます。
	 * 
	 * @since 1.1
	 * 
	 * @param mm
	 * @return 生成されたMimeBodyPart
	 * @throws MessagingException
	 */
	private MimeBodyPart createMimeBodyPart(MimeMultipart mm)
			throws MessagingException {
		MimeBodyPart bodyPart = new MimeBodyPart();
		mm.addBodyPart(bodyPart);
		return bodyPart;
	}

	/**
	 * @since 1.1
	 * 
	 * @param htmlText
	 * @param mimePart
	 * @throws MessagingException
	 */
	private void setHtmlText(final String htmlText, MimePart mimePart)
			throws MessagingException {
		if (charset != null) {
			mimePart.setContent(htmlText, "text/html; charset=" + charset);
		} else {
			mimePart.setContent(htmlText, "text/html");
		}
		setContentTransferEncoding(mimePart);
	}

	/**
	 * @param mail
	 * @throws MessagingException
	 */
	private void setXHeaders(Mail mail) throws MessagingException {
		Map<?, ?> headers = mail.getHeaders();
		if (headers == null) {
			return;
		}

		Iterator<?> itr = headers.keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			String value = (String) headers.get(key);
			mimeMessage.setHeader(key, value);
		}
	}

	/**
	 * @param mail
	 * @throws MessagingException
	 */
	private void setImportance(Mail mail) throws MessagingException {
		if (mail.getImportance() != null) {
			mimeMessage.setHeader("Importance", mail.getImportance());

			int level = 3;
			if (Mail.Importance.HIGH.equals(mail.getImportance())) {
				level = 1;
			} else if (Mail.Importance.LOW.equals(mail.getImportance())) {
				level = 5;
			}
			mimeMessage.setHeader("X-Priority", String.valueOf(level));
		}
	}

	/**
	 * @param mail
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	private void setReplyTo(Mail mail) throws MessagingException,
			UnsupportedEncodingException {
		if (mail.getReplyTo() != null) {
			mimeMessage.setReplyTo(new InternetAddress[] { convertCharset(mail
					.getReplyTo()) });
		}
	}

	/**
	 * @param mail
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	private void setBcc(Mail mail) throws MessagingException,
			UnsupportedEncodingException {
		if (mail.getBcc().length > 0) {
			mimeMessage.setRecipients(MimeMessage.RecipientType.BCC,
					convertCharset(mail.getBcc()));
			hasRecipient = true;
		}
	}

	/**
	 * @param mail
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	private void setCc(Mail mail) throws MessagingException,
			UnsupportedEncodingException {
		if (mail.getCc().length > 0) {
			mimeMessage.setRecipients(MimeMessage.RecipientType.CC,
					convertCharset(mail.getCc()));
			hasRecipient = true;
		}
	}

	/**
	 * @param mail
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	private void setTo(Mail mail) throws MessagingException,
			UnsupportedEncodingException {
		if (mail.getTo().length > 0) {
			mimeMessage.setRecipients(MimeMessage.RecipientType.TO,
					convertCharset(mail.getTo()));
			hasRecipient = true;
		}
	}

	/**
	 * 本文をセット。
	 * <p>
	 * NOTE: 本文の最後に改行がないとMozilla系のメーラーで最終行の日本語が文字化けしてしまう為、
	 * message.setTextの引数で最後に\nを追加している。
	 * 
	 * @since 1.1
	 * 
	 * @param text
	 *            本文
	 * @param mimePart
	 *            本文をセットするMimePart
	 * @throws MessagingException
	 */
	private void setText(String text, MimePart mimePart)
			throws MessagingException {
		if (charset != null) {
			if (charset.equalsIgnoreCase(Mail.JIS_CHARSET)) {
				// Cp932クラスを使用して、怪しい記号を強制的にJIS変換
				mimePart.setText(Cp932.toJIS(text) + "\n", charset);
			} else {
				mimePart.setText(text + "\n", charset);
			}
		} else {
			mimePart.setText(text + "\n");
		}
		setContentTransferEncoding(mimePart);
	}

	/**
	 * charsetに応じてContent-Trnasfer-Encodingを設定します。
	 * が、UTF-8の時に8bitになること以外分かりません。。。
	 * 
	 * @since 1.2.1
	 * @param mimePart
	 * @throws MessagingException
	 */
	private void setContentTransferEncoding(MimePart mimePart)
			throws MessagingException {
		String contentTransferEncoding = "7bit";
		if ("UTF-8".equalsIgnoreCase(charset)) {
			contentTransferEncoding = "8bit";
		}
		mimePart.setHeader("Content-Transfer-Encoding", contentTransferEncoding);
	}

	/**
	 * @param mail
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	private void setSubject(Mail mail) throws UnsupportedEncodingException,
			MessagingException {
		if (charset != null) {
			if (Mail.JIS_CHARSET.equalsIgnoreCase(charset)) {
				String subject = Cp932.toJIS(mail.getSubject());
				mimeMessage.setSubject(MimeUtility.encodeText(subject, charset,
						"B"));
			} else {
				mimeMessage.setSubject(mail.getSubject(), charset);
			}
		} else {
			mimeMessage.setSubject(mail.getSubject());
		}
	}

	/**
	 * @param mail
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	private void setFrom(Mail mail) throws MessagingException,
			UnsupportedEncodingException {
		InternetAddress address = convertCharset(mail.getFrom());
		mimeMessage.setFrom(address);
	}

	private InternetAddress convertCharset(InternetAddress address)
			throws UnsupportedEncodingException {
		String name = address.getPersonal();
		if (name != null && !"".equals(name)
				&& Mail.JIS_CHARSET.equalsIgnoreCase(charset)) {
			name = Cp932.toJIS(name);
		}
		return new InternetAddress(address.getAddress(), name, charset);
	}

	private InternetAddress[] convertCharset(InternetAddress[] addresses)
			throws UnsupportedEncodingException {
		for (int i = 0; i < addresses.length; i++) {
			addresses[i] = convertCharset(addresses[i]);
		}
		return addresses;
	}

	/**
	 * 添付ファイルデータを指定されたMimeBodyPartにセットします。
	 * 
	 * @since 1.1
	 * 
	 * @param fileName
	 * @param dataSource
	 * @param mimeBodyPart
	 *            ファイルデータをセットするMimeBodyPart
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	private void addAttachment(String fileName, DataSource dataSource,
			MimeBodyPart mimeBodyPart) throws UnsupportedEncodingException,
			MessagingException {
		if (charset != null) {
			// ファイル名のエンコード
			mimeBodyPart.setFileName(MimeUtility.encodeText(fileName, charset,
					"B"));
		} else {
			mimeBodyPart.setFileName(fileName);
		}

		mimeBodyPart.setDataHandler(new DataHandler(dataSource));
	}

}