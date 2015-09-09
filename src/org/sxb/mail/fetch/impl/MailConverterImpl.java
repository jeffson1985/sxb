package org.sxb.mail.fetch.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.sxb.log.Logger;
import org.sxb.mail.fetch.MailConverter;
import org.sxb.mail.fetch.ReceivedMail;
import org.sxb.mail.fetch.ReceivedMail.ReceivedHeader;
import org.sxb.mail.fetch.impl.sk_jp.AttachmentsExtractor;
import org.sxb.mail.fetch.impl.sk_jp.HtmlPartExtractor;
import org.sxb.mail.fetch.impl.sk_jp.MailUtility;
import org.sxb.mail.fetch.impl.sk_jp.MultipartUtility;

/**
 * MimeMessageからMailを生成するクラス。
 * <p>
 * 変換時に生じたチェック例外は、このクラス内でキャッチされ無視されます。
 * 例外が生じた項目(差出人や宛先など)に該当するMailインスタンスのプロパティには何もセットされません。
 * 
 * @since 1.2
 * @author    Jeffson  (jeffson.app@gmail.com)
 * @version $Id: MailConverterImpl.java,v 1.1.2
 */
public class MailConverterImpl implements MailConverter {

	private static final String ATTACHMENT_DIR_PREFIX = "OML_";

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	private static Logger log = Logger.getLogger(MailConverterImpl.class);

	private static Pattern receivedHeaderPattern = Pattern.compile("^from (.+?) .*by (.+?) .*$");

	/**
	 * 保存された添付ファイルの生存時間。デフォルトは12時間。
	 */
	private long attachmentLifetime = 3600 * 1000 * 12;

	/**
	 * コンストラクタ。
	 */
	public MailConverterImpl() {}

	/**
	 * @see org.sxb.mail.fetch.MailConverter#convertIntoMails(javax.mail.internet.MimeMessage[])
	 */
	public ReceivedMail[] convertIntoMails(MimeMessage[] messages) {
		log.debug("計" + messages.length + "通のMimeMessageをMailに変換します。");
		ReceivedMail[] results = new ReceivedMail[messages.length];
		for (int i = 0; i < messages.length; i++) {
			log.debug((i + 1) + "通目のMimeMessageをMailに変換します。");
			results[i] = convertIntoMail(messages[i]);
			log.debug((i + 1) + "通目のMimeMessageをMailに変換しました。");
			log.debug(results[i].toString());
		}
		log.debug("計" + messages.length + "通のMimeMessageをMailに変換しました。");
		return results;
	}

	/**
	 * @param mm
	 * @param mail 
	 */
	private void setReceivedHeaders(MimeMessage mm, ReceivedMail mail) {
		String[] headerValues = null;
		try {
			headerValues = mm.getHeader("Received");
		} catch (MessagingException e) {
			// ignore
			log.warn(e.getMessage());
		}
		if (headerValues != null) {
			for (int i = 0; i < headerValues.length; i++) {
				String received = headerValues[i];
				// from で始まるものだけを抽出し、改行を削除
				if (received.startsWith("from")) {
					received = received.replaceAll("\n", "").replaceAll("\\s+", " ");
					log.debug("Received='" + received + "'");

					Matcher m = receivedHeaderPattern.matcher(received);
					if (m.matches()) {
						String from = m.group(1);
						String by = m.group(2);
						log.debug("Sent from '" + from + "', Received by '" + by + "'");
						ReceivedHeader rh = new ReceivedHeader(from, by);
						mail.addReceviedHeader(rh);
					}
				}
			}

		}
	}

	/**
	 * 指定されたMimeMessageに添付されているファイルを全て抽出し、
	 * システムプロパティにセットされた一時ファイルディレクトリ内に保存した後、
	 * そのファイルを指定されたReceivedMailにセットします。
	 * <p>
	 * 保存された添付ファイルはJVM終了時に削除されます。
	 * 
	 * @param mm
	 * @param mail 
	 */
	private void setAttachmentFiles(MimeMessage mm, ReceivedMail mail) {
		try {
			cleanTempDir();

			AttachmentsExtractor ae = new AttachmentsExtractor(
					AttachmentsExtractor.MODE_IGNORE_MESSAGE);
			MultipartUtility.process(mm, ae);
			for (int i = 0, num = ae.getCount(); i < num; i++) {
				String fileName = ae.getFileName(i);
				if (fileName == null || "".equals(fileName)) {
					fileName = "attachment" + (i + 1) + ".tmp";
				}
				String path = getTempDirPath() + File.separator + ATTACHMENT_DIR_PREFIX
						+ System.currentTimeMillis() + File.separator + fileName;
				log.debug((i + 1) + "個目の添付ファイルを保存します。[" + path + "]");
				File f = new File(path);
				f.getParentFile().mkdirs();
				InputStream is = ae.getInputStream(i);
				try {
					writeTo(f, is);
				} finally {
					if (is != null) {
						is.close();
					}
				}

				f.getParentFile().deleteOnExit();
				f.deleteOnExit();

				mail.addFile(f, fileName);
				log.debug((i + 1) + "個目の添付ファイルを保存しました。[" + path + "]");
			}
		} catch (IOException e) {
			log.error("添付ファイルの取得に失敗しました。", e);
		} catch (MessagingException e) {
			// ignore
			log.warn(e.getMessage());
		}
	}

	/**
	 * 一時ディレクトリ内に保存された添付ファイルの内、生存時間を越えているものを削除します。
	 */
	private void cleanTempDir() {
		File tempDir = new File(getTempDirPath());
		File[] omlDirs = tempDir.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.startsWith(ATTACHMENT_DIR_PREFIX);
			}
		});
		log.debug("現在" + omlDirs.length + "個の添付ファイル用ディレクトリ[" + tempDir.getAbsolutePath()
				+ "]が一時ディレクトリに存在します。");
		long now = System.currentTimeMillis();
		for (int i = 0; i < omlDirs.length; i++) {
			File dir = omlDirs[i];
			log.debug(dir.lastModified() + "");
			if (now - dir.lastModified() >= attachmentLifetime) {
				deleteDir(dir);
			}
		}
	}

	/**
	 * 一時ディレクトリのパスを返します。
	 * 
	 * @return 一時ディレクトリのパス
	 */
	private String getTempDirPath() {
		return System.getProperty(JAVA_IO_TMPDIR);
	}

	/**
	 * 指定されたディレクトリを中身のファイルを含めて削除します。
	 * 
	 * @param dir 削除するディレクトリ
	 */
	private void deleteDir(File dir) {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			f.delete();
		}
		dir.delete();
	}

	/**
	 * 指定されたInputStreamデータを指定されたファイルに保存します。
	 * 
	 * @param destFile 保存するファイル
	 * @param is ソースとなるInputStream
	 * @throws FileNotFoundException
	 * @throws IOException 
	 */
	private void writeTo(File destFile, InputStream is) throws FileNotFoundException, IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile),
				1024 * 50);
		try {
			copy(is, bos);
		} finally {
			if (bos != null) {
				bos.close();
			}
		}
	}

	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024 * 4];
		int n = 0;
		while (-1 != (n = in.read(buffer))) {
			out.write(buffer, 0, n);
		}
	}

	/**
	 * 指定されたMimeMessageからX-Header、References、In-Reply-Toヘッダを解析し、
	 * 指定されたReceivedMailにセットします。
	 * 
	 * @param mm
	 * @param mail
	 */
	private void setXHeaders(MimeMessage mm, ReceivedMail mail) {
		log.debug("X-HeaderをMailにセットします。");
		Enumeration<?> headerEnum = null;
		try {
			headerEnum = mm.getAllHeaders();
		} catch (MessagingException e) {
			// ignore
			log.warn(e.getMessage());
		}
		while (headerEnum != null && headerEnum.hasMoreElements()) {
			Header header = (Header)headerEnum.nextElement();
			if (header.getName().startsWith("X-")
					|| "References".equalsIgnoreCase(header.getName())
					|| "In-Reply-To".equalsIgnoreCase(header.getName())) {
				mail.addHeader(header.getName(), header.getValue());
				log.debug(header.getName() + "をMailにセットしました。[" + header.getName() + "='"
						+ header.getValue() + "']");
			}
		}
	}

	@SuppressWarnings("unused")
	private void setReplyToAddress(MimeMessage mm, ReceivedMail mail) {
		log.debug("Reply-ToアドレスをMailにセットします。");
		Address[] addresses = null;
		try {
			addresses = mm.getReplyTo();
		} catch (MessagingException e) {
			// ignore
			log.warn(e.getMessage());
		}
		if (addresses != null) {
			log.debug(addresses.length + "つのReply-Toアドレスが見つかりました。最初のアドレスのみ取得されます。");
			for (int j = 0; j < addresses.length; j++) {
				Address address = addresses[j];
				mail.setReplyTo((InternetAddress)address);
				break;
			}
		} else {
			log.debug("Reply-Toアドレスは見つかりませんでした。");
		}
	}

	/**
	 * メールの容量(byte)をMimeMessageから取得してReceivedMailにセットします。
	 * 取得に失敗した場合は -1 をセットします。
	 * 
	 * @param mm
	 * @param mail 
	 */
	private void setSize(MimeMessage mm, ReceivedMail mail) {
		try {
			mail.setSize(mm.getSize());
		} catch (MessagingException e) {
			mail.setSize(-1);
		}
	}

	/**
	 * @param mm
	 * @param mail
	 * @throws MessagingException 
	 */
	private void setHtmlText(MimeMessage mm, ReceivedMail mail) {
		try {
			HtmlPartExtractor hpe = new HtmlPartExtractor();
			MultipartUtility.process(mm, hpe);
			String htmlText = hpe.getHtml();
			mail.setHtmlText(htmlText);
		} catch (MessagingException e) {
			// ignore
			log.warn(e.getMessage());
		}
	}

	private void setText(MimeMessage mm, ReceivedMail mail) {
		try {
			String text = MultipartUtility.getPlainText(mm);
			mail.setText(text);
		} catch (MessagingException e) {
			// ignore
			log.warn(e.getMessage());
		}
	}

	private void setMessageId(MimeMessage mm, ReceivedMail mail) {
		try {
			String messageId = mm.getMessageID();
			mail.setMessageId(messageId);
			log.debug("Message-IDをMailにセットしました。[Message-ID='" + messageId + "']");
		} catch (MessagingException e) {
			// ignore
			log.warn(e.getMessage());
		}
	}

	/**
	 * 指定されたMimeMessageから件名を取得し、ReceivedMailにセットします。
	 * sk_jpのMailUtility.decodeText()メソッドを用いて、件名の文字化けを回避します。
	 * 
	 * @param mm
	 * @param mail
	 */
	private void setSubject(MimeMessage mm, ReceivedMail mail) {
		try {
			String subject = MailUtility.decodeText(mm.getSubject());
			mail.setSubject(subject);
		} catch (MessagingException e) {
			// ignore
			log.warn(e.getMessage());
		}
	}

	private void setDate(MimeMessage mm, ReceivedMail mail) {
		try {
			Date d = mm.getSentDate();
			mail.setDate(d);
		} catch (MessagingException e) {
			// ignore
			log.warn(e.getMessage());
		}
	}

	/**
	 * Return-Pathアドレスは必ずしもセットされてはいません。
	 * 特にspam系のメールでは不正なフォーマットのメールアドレスが
	 * セットされている場合もあるので要注意。
	 * 
	 * @param mm
	 * @param mail
	 */
	private void setReturnPath(MimeMessage mm, ReceivedMail mail) {
		log.debug("Return-Pathアドレスを検出します。");
		String[] returnPath = null;
		try {
			returnPath = mm.getHeader("Return-Path");
		} catch (MessagingException e) {
			// ignore
			log.warn(e.getMessage());
		}
		if (returnPath != null && returnPath.length > 0) {
			String email = returnPath[0].substring(1, returnPath[0].length() - 1);
			if (email.length() > 0) {
				try {
					mail.setReturnPath(email);
					log.debug("Return-PathアドレスをMailにセットしました。[Return-Path='" + email + "']");
				} catch (IllegalArgumentException e) {
					log.warn("Return-Pathアドレスが不正なメールアドレスフォーマットです。[Return-Path='" + email + "']");
				}
			} else {
				log.debug("Return-Pathアドレスは見つかりませんでした。");
			}
		} else {
			log.debug("Return-Pathアドレスは見つかりませんでした。");
		}
	}

	private void setFromAddress(MimeMessage mm, ReceivedMail mail) {
		log.debug("Fromアドレスを検出します。");
		Address[] addresses = null;
		try {
			addresses = mm.getFrom();
		} catch (MessagingException e) {
			// ignore
			log.warn(e.getMessage());
		}
		if (addresses != null) {
			log.debug(addresses.length + "つのFromアドレスが見つかりました。");
			for (int j = 0; j < addresses.length; j++) {
				InternetAddress address = (InternetAddress)addresses[j];
				mail.setFrom(address);
				log.debug("FromアドレスをMailにセットしました。[From='" + address.toUnicodeString() + "']");
			}
		} else {
			log.debug("Fromアドレスは見つかりませんでした。");
		}
	}

	private void setRecipientAddresses(MimeMessage mm, ReceivedMail mail) {
		/*
		 * TOアドレスのパース
		 */
		log.debug("Toアドレスを検出します。");
		Address[] toAddresses = null;
		try {
			toAddresses = mm.getRecipients(Message.RecipientType.TO);
		} catch (AddressException e) {
			log.warn("不正なメールアドレスが検出されました。[" + e.getRef() + "]");
		} catch (MessagingException e) {
			// ignore
			log.warn(e.getMessage());
		}
		if (toAddresses != null) {
			log.debug(toAddresses.length + "つのToアドレスが見つかりました。");
			for (int j = 0; j < toAddresses.length; j++) {
				InternetAddress address = (InternetAddress)toAddresses[j];
				mail.addTo(address);
				log.debug("ToアドレスをMailにセットしました。[To='" + address.toUnicodeString() + "']");
			}
		} else {
			log.debug("Toアドレスは見つかりませんでした。");
		}

		/*
		 * CCアドレスのパース
		 */
		log.debug("Ccアドレスを検出します。");
		Address[] ccAddresses = null;
		try {
			ccAddresses = mm.getRecipients(Message.RecipientType.CC);
		} catch (AddressException e) {
			log.warn("不正なメールアドレスが検出されました。[" + e.getRef() + "]");
		} catch (MessagingException e) {
			// ignore
			log.warn(e.getMessage());
		}
		if (ccAddresses != null) {
			log.debug(ccAddresses.length + "つのCcアドレスが見つかりました。");
			for (int j = 0; j < ccAddresses.length; j++) {
				InternetAddress address = (InternetAddress)ccAddresses[j];
				mail.addCc(address);
				log.debug("CcアドレスをMailにセットしました。[Cc='" + address.toUnicodeString() + "']");
			}
		} else {
			log.debug("Ccアドレスは見つかりませんでした。");
		}
	}

	/**
	 * @see org.sxb.mail.fetch.MailConverter#convertIntoMail(javax.mail.internet.MimeMessage)
	 */
	public ReceivedMail convertIntoMail(MimeMessage mm) {
		ReceivedMail mail = createReceivedMail();
		setReturnPath(mm, mail);
		setReceivedHeaders(mm, mail);
		setDate(mm, mail);
		setFromAddress(mm, mail);
		setRecipientAddresses(mm, mail);
		setMessageId(mm, mail);
		setReplyToAddress(mm, mail);
		setSubject(mm, mail);
		setXHeaders(mm, mail);
		setText(mm, mail);
		setHtmlText(mm, mail);
		setAttachmentFiles(mm, mail);
		setSize(mm, mail);
		mail.setMessage(mm);
		return mail;
	}

	protected ReceivedMail createReceivedMail() {
		return new ReceivedMail();
	}

}