package org.sxb.mail;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.activation.URLDataSource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.sxb.mail.impl.ByteArrayDataSource;

/**
 * メール
 * 邮件实体类
 * 
 * @author    Jeffson  (jeffson.app@gmail.com)
 *  
 * @version $Id: Mail.java,v 1.0
 */
public class Mail {
	
	/** <code>ISO-2022-JP</code> メール本文エンコード（標準） */
	public static final String JIS_CHARSET = "ISO-2022-JP";
	
	/** <code>charset</code> メール本文エンコード */
	private String charset;
	
	/** <code>text</code> メール本文 */
	protected String text;
	
	/** <code>from</code> メールの差出人アドレス  */
	protected InternetAddress from;
	
	/** <code>subject</code> メールの件名 */
	protected String subject;
	
	/** <code>to</code> メールの送信先アドレス */
	protected List<InternetAddress> to;
	
	/** <code>cc</code> CCアドレス */
	protected List<InternetAddress> cc;
	
	/** <code>bcc</code> BCCアドレス  */
	protected List<InternetAddress> bcc;
	
	/** <code>envelopeTo</code> 送信ホストアドレス */
	protected List<InternetAddress> envelopeTo;
	
	/** <code>returnPath</code> Return-Pathアドレス */
	protected InternetAddress returnPath;
	
	/** <code>replayTo</code> 返信先アドレス */
	protected InternetAddress replyTo;
	
	/** <code>importance</code> メールの重要度（high, normal, low） */
	protected String importance;
	
	/** <code>headers</code> メールヘッダ  */
	protected Map<String, String> headers = new HashMap<String, String>();
	
	/** <code>htmlText</code> 本文内容（HTML） */
	protected String htmlText;
	
	/** <code>attachmentFiles</code> 添付ファイル */
	protected List<AttachmentFile> attachmentFiles;

	/**
	 * コンストラクタ。
	 */
	public Mail() {}

	/**
	 * コンストラクタ。
	 * 宛先や差出人の名前をエンコードする時に使用する文字コードを指定します。
	 * <p>
	 * 日本語環境で利用する場合は通常設定する必要はありません。
	 * 
	 * @param charset エンコードに使用する文字コード
	 */
	public Mail(String charset) {
		this();
		this.charset = charset;
	}

	/**
	 * コピーコンストラクタ。
	 * シャローコピー(shallow copy)です。
	 * 
	 * @author    Jeffson  (jeffson.app@gmail.com).2
	 * 
	 * @param original コピー元のMailインスタンス
	 */
	public Mail(Mail original) {
		this.bcc = original.bcc;
		this.cc = original.cc;
		this.charset = original.charset;
		this.from = original.from;
		this.importance = original.importance;
		this.replyTo = original.replyTo;
		this.returnPath = original.returnPath;
		this.subject = original.subject;
		this.text = original.text;
		this.to = original.to;
		this.headers = original.headers;
		this.htmlText = original.htmlText;
		this.attachmentFiles = original.attachmentFiles;
		this.envelopeTo = original.envelopeTo;
	}

	/**
	 * エンコードに使用する文字コードを返します。コンストラクタで設定されなかった場合はnullを返します。
	 * 
	 * @return エンコードに使用する文字コード、またはnull
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * メールの重要度をセットします。
	 * 引数で指定可能な値は「high」、「normal」、「low」のいずれかです。
	 * 
	 * @param importance メールの重要度。「high」、「normal」、「low」のいずれか。
	 * @throws IllegalArgumentException 指定可能な値以外が指定された場合
	 * 
	 * @see Mail.Importance
	 */
	public void setImportance(String importance) throws IllegalArgumentException {
		if ("high".equals(importance) || "normal".equals(importance) || "low".equals(importance)) {
			this.importance = importance;
		} else {
			throw new IllegalArgumentException("'" + importance + "'は、メール重要度には指定できない値です。");
		}
	}

	/**
	 * メールの重要度を返します。
	 * 値は「high」、「normal」、「low」のいずれかです。
	 * 
	 * @return メールの重要度。「high」、「normal」、「low」のいずれか。
	 */
	public String getImportance() {
		return importance;
	}

	/**
	 * メールの送信先アドレスを追加します。
	 * 
	 * @param address 送信先アドレス
	 */
	public void addTo(InternetAddress address) {
		if (to == null) {
			to = new ArrayList<InternetAddress>();
		}
		to.add(address);
	}

	/**
	 * メールの送信先アドレスを追加します。
	 * 
	 * @param email 送信先アドレス
	 * @throws IllegalArgumentException 不正なフォーマットのアドレスが指定された場合
	 */
	public void addTo(String email) throws IllegalArgumentException {
		try {
			addTo(new InternetAddress(email));
		} catch (AddressException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * メールの送信先名とアドレスを追加します。
	 * 
	 * @param email 送信先アドレス
	 * @param name 送信先名
	 * @throws IllegalArgumentException 不正なフォーマットのアドレスが指定された場合
	 */
	public void addTo(String email, String name) throws IllegalArgumentException {
		try {
			addTo(new InternetAddress(email, name, charset));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * メールの送信先アドレスの配列を返します。
	 * 送信先アドレスが一件もセットされていないときは空の配列を返します。
	 * 
	 * @return 送信先アドレスの配列
	 */
	public InternetAddress[] getTo() {
		if (to == null) {
			return new InternetAddress[0];
		}
		return (InternetAddress[])to.toArray(new InternetAddress[to.size()]);
	}

	/**
	 * CCアドレスを追加します。
	 * 
	 * @param address CCのアドレス
	 */
	public void addCc(InternetAddress address) {
		if (cc == null) {
			cc = new ArrayList<InternetAddress>();
		}
		cc.add(address);
	}

	/**
	 * CCアドレスを追加します。
	 * 
	 * @param email CCのアドレス
	 * @throws IllegalArgumentException 不正なフォーマットのアドレスが指定された場合
	 */
	public void addCc(String email) throws IllegalArgumentException {
		try {
			addCc(new InternetAddress(email));
		} catch (AddressException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * CCの宛名とアドレスを追加します。
	 * 
	 * @param email CCのアドレス
	 * @param name CCの宛名
	 * @throws IllegalArgumentException 不正なフォーマットのアドレスが指定された場合
	 */
	public void addCc(String email, String name) throws IllegalArgumentException {
		try {
			addCc(new InternetAddress(email, name, charset));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * メールのCCアドレス配列を返します。
	 * CCアドレスが一件もセットされていないときは空の配列を返します。
	 * 
	 * @return CCアドレスの配列
	 */
	public InternetAddress[] getCc() {
		if (cc == null) {
			return new InternetAddress[0];
		}
		return (InternetAddress[])cc.toArray(new InternetAddress[cc.size()]);
	}

	/**
	 * BCCアドレスを追加します。
	 * 
	 * @param address BCCのアドレス
	 */
	public void addBcc(InternetAddress address) {
		if (bcc == null) {
			bcc = new ArrayList<InternetAddress>();
		}
		bcc.add(address);
	}

	/**
	 * BCCアドレスを追加します。
	 * 
	 * @param email BCCのアドレス
	 * @throws IllegalArgumentException 不正なフォーマットのアドレスが指定された場合
	 */
	public void addBcc(String email) throws IllegalArgumentException {
		try {
			addBcc(new InternetAddress(email));
		} catch (AddressException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * メールのBCCアドレスの配列を返します。
	 * BCCアドレスが一件もセットされていないときは空の配列を返します。
	 * 
	 * @return BCCアドレスの配列
	 */
	public InternetAddress[] getBcc() {
		if (bcc == null) {
			return new InternetAddress[0];
		}
		return (InternetAddress[])bcc.toArray(new InternetAddress[bcc.size()]);
	}

	/**
	 * メールの差出人アドレスをセットします。
	 * 
	 * @param address 差出人アドレス
	 */
	public void setFrom(InternetAddress address) {
		from = address;
	}

	/**
	 * メールの差出人アドレスをセットします。
	 * 
	 * @param email 差出人アドレス
	 * @throws IllegalArgumentException 不正なフォーマットのアドレスが指定された場合
	 */
	public void setFrom(String email) throws IllegalArgumentException {
		try {
			setFrom(new InternetAddress(email));
		} catch (AddressException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * メールの差出人名とアドレスをセットします。
	 * 
	 * @param email 差出人アドレス
	 * @param name 差出人名
	 * @throws IllegalArgumentException 不正なフォーマットのアドレスが指定された場合
	 */
	public void setFrom(String email, String name) throws IllegalArgumentException {
		try {
			setFrom(new InternetAddress(email, name, charset));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * メールの差出人アドレスを返します。セットされていない場合はnullを返します。
	 * 
	 * @return メールの差出人アドレス
	 */
	public InternetAddress getFrom() {
		return from;
	}

	/**
	 * Return-Pathアドレスをセットします。
	 * 
	 * @param address Return-Pathアドレス
	 */
	public void setReturnPath(InternetAddress address) {
		returnPath = address;
	}

	/**
	 * Return-Pathアドレスをセットします。
	 * 
	 * @param email Return-Pathアドレス
	 * @throws IllegalArgumentException 不正なフォーマットのアドレスが指定された場合
	 */
	public void setReturnPath(String email) throws IllegalArgumentException {
		try {
			setReturnPath(new InternetAddress(email));
		} catch (AddressException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * Return-Pathアドレスを返します。
	 * 
	 * @return Return-Pathアドレス
	 */
	public InternetAddress getReturnPath() {
		return returnPath;
	}

	/**
	 * 返信先アドレスをセットします。
	 * 
	 * @param address 返信先アドレス
	 */
	public void setReplyTo(InternetAddress address) {
		replyTo = address;
	}

	/**
	 * 返信先アドレスをセットします。
	 * 
	 * @param email 返信先アドレス
	 * @throws IllegalArgumentException 不正なフォーマットのアドレスが指定された場合
	 */
	public void setReplyTo(String email) throws IllegalArgumentException {
		try {
			setReplyTo(new InternetAddress(email));
		} catch (AddressException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * メールの返信先アドレスを返します。セットされていない場合はnullを返します。
	 * 
	 * @return 返信先アドレス
	 */
	public InternetAddress getReplyTo() {
		return replyTo;
	}

	/**
	 * メールの件名を返します。セットされていない場合は空文字列を返します。
	 * 
	 * @return メールの件名
	 */
	public String getSubject() {
		if (subject == null) {
			return "";
		}
		return subject;
	}

	/**
	 * メールの件名をセットします。
	 * 
	 * @param subject メールの件名
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * メール本文を返します。
	 * 本文セットされていない場合は空文字列を返します。
	 * 
	 * @return メール本文
	 */
	public String getText() {
		if (text == null) {
			return "";
		}
		return text;
	}

	/**
	 * メール本文をセットします。
	 * 
	 * @param text メール本文
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * メールヘッダに任意のヘッダフィールドを追加します。
	 * 任意ヘッダは「X-key: value」のフォーマットでメールヘッダに組み込まれます。<br>
	 * 同じヘッダ名の値は上書きされます。
	 *  
	 * @param name 任意ヘッダ名。頭が"X-"で始まっていなければ、自動的に付与されます。
	 * @param value 任意ヘッダの値
	 */
	public void addXHeader(String name, String value) {
		if (headers == null) {
			headers = new HashMap<String, String>();
		}
		if (name.startsWith("X-")) {
			headers.put(name, value);
		} else {
			headers.put("X-" + name, value);
		}
	}

	/**
	 * メールヘッダに任意のヘッダフィールドを追加します。<br>
	 * <b>このメソッドはユーザが使用することを想定していません。</b>
	 * 使用する際は、To や From などのフィールドをセットしないように注意してください。
	 * <p>
	 * このメソッドで設定した同じヘッダ名の値は上書きされます。
	 * 
	 * @since 1.2
	 * @param name 任意ヘッダ名
	 * @param value 任意ヘッダの値
	 */
	public void addHeader(String name, String value) {
		if (headers == null) {
			headers = new HashMap<String, String>();
		}
		headers.put(name, value);
	}

	/**
	 * メールの任意ヘッダ名と値のMapインスタンスを返します。
	 * 任意ヘッダが一件もセットされていないときはnullを返します。
	 * <p>
	 * このMapインスタンスへの修正はできません。(unmodifiableMapになっています。)
	 * 
	 * @return メールの任意ヘッダ名と値のMapインスタンス。またはnull。
	 */
	public Map<String, String> getHeaders() {
		if (headers == null) {
			return null;
		}
		return Collections.unmodifiableMap(headers);
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
		buf.append("From: ").append(from != null ? from.toUnicodeString() : null).append("\n");
		buf.append("To: ").append(arrayToCommaDelimitedString(to)).append("\n");
		buf.append("Cc: ").append(arrayToCommaDelimitedString(cc)).append("\n");
		buf.append("Bcc: ").append(arrayToCommaDelimitedString(bcc)).append("\n");
		buf.append("Subject: ").append(subject).append("\n");

		if (headers != null) {
			for (Iterator<String> itr = headers.keySet().iterator(); itr.hasNext();) {
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

		return buf.toString();
	}

	/**
	 * 指定されたリストの要素をコンマ区切りの文字列に変換します。
	 * nullが指定された場合は「null」文字列を返します。
	 * 
	 * @param list
	 * @return リスト要素のコンマ区切り文字列
	 */
	protected String arrayToCommaDelimitedString(List<InternetAddress> list) {
		if (list == null) {
			return "null";
		} else {
			StringBuffer sb = new StringBuffer();
			for (int i = 0, num = list.size(); i < num; i++) {
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(((InternetAddress)list.get(i)).toUnicodeString());
			}
			return sb.toString();
		}
	}

	/**
	 * セットされている送信先アドレス(Toアドレス)を全てクリアします。
	 *
	 * @author    Jeffson  (jeffson.app@gmail.com).2
	 */
	public void clearTo() {
		to = null;
	}

	/**
	 * セットされているCCアドレスを全てクリアします。
	 *
	 * @author    Jeffson  (jeffson.app@gmail.com).2
	 */
	public void clearCc() {
		cc = null;
	}

	/**
	 * セットされているBCCアドレスを全てクリアします。
	 *
	 * @author    Jeffson  (jeffson.app@gmail.com).2
	 */
	public void clearBcc() {
		bcc = null;
	}

	/**
	 * HTMLの本文をセットします。
	 * 
	 * @since 1.1
	 * 
	 * @param htmlText HTMLの本文
	 */
	public void setHtmlText(String htmlText) {
		this.htmlText = htmlText;
	}

	/**
	 * HTMLの本文を返します。
	 * 
	 * @since 1.1
	 * 
	 * @return HTMLの本文。またはnull。
	 */
	public String getHtmlText() {
		return htmlText;
	}

	/**
	 * 指定されたファイルを添付します。
	 * 添付ファイル名には、指定されたファイルの名前が使用されます。
	 * このファイルの名前は適切な拡張子が付けられている必要があります。
	 * 
	 * @since 1.1
	 * 
	 * @param file 添付ファイル
	 */
	public void addFile(File file) {
		if (attachmentFiles == null) {
			initAttachmentFiles();
		}
		addFile(file, file.getName());
	}

	/**
	 * 指定されたファイルを添付します。
	 * 指定するファイル名には適切な拡張子が付けられている必要があります。
	 * 
	 * @since 1.1
	 * 
	 * @param file 添付ファイル
	 * @param fileName ファイル名
	 */
	public void addFile(File file, String fileName) {
		if (attachmentFiles == null) {
			initAttachmentFiles();
		}
		attachmentFiles.add(new AttachmentFile(fileName, file));
	}

	/**
	 * 指定されたURLのファイルを添付します。
	 * 指定するファイル名には適切な拡張子が付けられている必要があります。
	 * 
	 * @since 1.1
	 * 
	 * @param url 添付ファイル
	 * @param fileName ファイル名
	 */
	public void addFile(URL url, String fileName) {
		if (attachmentFiles == null) {
			initAttachmentFiles();
		}
		attachmentFiles.add(new AttachmentFile(fileName, url));
	}

	/**
	 * 指定されたInputStreamをファイルとして添付します。
	 * 指定するファイル名には適切な拡張子が付けられている必要があります。
	 * 
	 * @since 1.1
	 * 
	 * @param is 添付ファイルを生成するInputStream
	 * @param fileName ファイル名
	 */
	public void addFile(InputStream is, String fileName) {
		if (attachmentFiles == null) {
			initAttachmentFiles();
		}
		attachmentFiles.add(new AttachmentFile(fileName, is));
	}

	/**
	 * 指定されたbyte配列をファイルとして添付します。
	 * 指定するファイル名には適切な拡張子が付けられている必要があります。
	 * 
	 * @since 1.2
	 * 
	 * @param bytes 添付ファイルを生成するbyte配列
	 * @param fileName ファイル名
	 */
	public void addFile(byte[] bytes, String fileName) {
		if (attachmentFiles == null) {
			initAttachmentFiles();
		}
		attachmentFiles.add(new AttachmentFile(fileName, bytes));
	}

	/**
	 * attachmentFilesプロパティを初期化。
	 */
	private void initAttachmentFiles() {
		attachmentFiles = new ArrayList<AttachmentFile>();
	}

	/**
	 * 添付ファイルの配列を返します。
	 * 添付ファイルがセットされていない場合は、空の配列を返します。
	 * 
	 * @since 1.1
	 * 
	 * @return 添付ファイルの配列。または空の配列。
	 */
	public AttachmentFile[] getAttachmentFiles() {
		if (attachmentFiles == null) {
			return new AttachmentFile[0];
		}
		return (AttachmentFile[])attachmentFiles
				.toArray(new AttachmentFile[attachmentFiles.size()]);
	}

	/**
	 * HTMLの本文がセットされているかどうか判定します。
	 * 
	 * @since 1.1
	 * 
	 * @return HTMLの本文がセットされている場合 true
	 */
	public boolean isHtmlMail() {
		return (htmlText != null);
	}

	/**
	 * ファイルが添付されているかどうか判定します。
	 * 
	 * @since 1.1
	 * 
	 * @return ファイルが添付されている場合 true
	 */
	public boolean isFileAttached() {
		return attachmentFiles != null && attachmentFiles.size() > 0;
	}

	/**
	 * マルチパート・メールかどうか判定します。<br>
	 * HTML本文がセットされているか、ファイルが添付されている場合に true が返されます。
	 * <p>
	 * 注: ここで判定されるマルチパートは、厳密な意味でのマルチパートではありません。
	 * 
	 * @since 1.1
	 * 
	 * @return マルチパート・メールの場合 true
	 */
	public boolean isMultipartMail() {
		return isHtmlMail() || isFileAttached();
	}

	/**
	 * セットされている添付ファイルを全てクリアします。
	 * 
	 * @since 1.1
	 */
	public void clearFile() {
		initAttachmentFiles();
	}

	/**
	 * envelope-toの宛先アドレスを追加します。
	 * <p>
	 * envelope-toアドレスがセットされている場合、envelope-toのアドレスにのみメールを送信し、
	 * To、Cc、Bccアドレスには実際には送信されません。
	 * 
	 * @since 1.2
	 * @param address
	 */
	public void addEnvelopeTo(InternetAddress address) {
		if (envelopeTo == null) {
			envelopeTo = new ArrayList<InternetAddress>();
		}
		envelopeTo.add(address);
	}

	/**
	 * envelope-toの宛先アドレスを追加します。
	 * <p>
	 * envelope-toアドレスがセットされている場合、envelope-toのアドレスにのみメールを送信し、
	 * To、Cc、Bccアドレスには実際には送信されません。
	 * 
	 * @since 1.2
	 * @param email
	 * @throws IllegalArgumentException 不正なフォーマットのアドレスが指定された場合
	 */
	public void addEnvelopeTo(String email) {
		try {
			addEnvelopeTo(new InternetAddress(email));
		} catch (AddressException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * envelope-toの宛先アドレスを追加します。
	 * <p>
	 * envelope-toアドレスがセットされている場合、envelope-toのアドレスにのみメールを送信し、
	 * To、Cc、Bccアドレスには実際には送信されません。
	 * 
	 * @since 1.2
	 * @param addresses
	 */
	public void addEnvelopeTo(InternetAddress[] addresses) {
		for (int i = 0; i < addresses.length; i++) {
			addEnvelopeTo(addresses[i]);
		}
	}

	/**
	 * envelope-toの宛先アドレスを追加します。
	 * <p>
	 * envelope-toアドレスがセットされている場合、envelope-toのアドレスにのみメールを送信し、
	 * To、Cc、Bccアドレスには実際には送信されません。
	 * 
	 * @since 1.2
	 * @param emails
	 * @throws IllegalArgumentException 不正なフォーマットのアドレスが指定された場合
	 */
	public void addEnvelopeTo(String[] emails) {
		for (int i = 0; i < emails.length; i++) {
			addEnvelopeTo(emails[i]);
		}
	}

	/**
	 * セットされているenvelope-toアドレスを全てクリアします。
	 *
	 * @since 1.2
	 */
	public void clearEnvelopeTo() {
		envelopeTo = null;
	}

	/**
	 * envelope-toアドレス配列を返します。
	 * envelope-toアドレスが一件もセットされていないときは空の配列を返します。
	 * 
	 * @since 1.2
	 * @return envelope-toアドレスの配列
	 */
	public InternetAddress[] getEnvelopeTo() {
		if (envelopeTo == null) {
			return new InternetAddress[0];
		}
		return (InternetAddress[])envelopeTo.toArray(new InternetAddress[envelopeTo.size()]);
	}

	/**
	 * 添付ファイル。
	 * <p>
	 * 受信メール(ReceivedMail)の添付ファイルは、常に<code>getFile()</code>メソッドで取得します。
	 * <code>getInputStream()</code>、<code>getUrl()</code>メソッドはnullを返します。
	 * 受信メールに対しては、<code>ReceivedMail.getFiles()</code>メソッドを使うと添付ファイルの
	 * <code>File</code>インスタンス配列を取得することができます。
	 * 
	 * @since 1.1
	 * @author Tomohiro Otsuka
	 * @version $Id: Mail.java,v 1.10.2.9 2007/03/30 13:03:44 
	 */
	public class AttachmentFile {

		private String name;

		private File file;

		private InputStream is;

		private URL url;

		private byte[] bytes = null;

		/**
		 * ファイル名とファイルを指定して、このクラスのインタンスを生成します。
		 * ファイル名には適切な拡張子が付けられている必要があります。
		 * 
		 * @param name メールに表示するファイル名
		 * @param file 添付ファイル
		 */
		public AttachmentFile(String name, File file) {
			this.name = name;
			this.file = file;
		}

		/**
		 * ファイル名とInputStreamを指定して、このクラスのインタンスを生成します。
		 * ファイル名には適切な拡張子が付けられている必要があります。
		 * 
		 * @param name メールに表示するファイル名
		 * @param is 添付ファイルを生成するInputStream
		 */
		public AttachmentFile(String name, InputStream is) {
			this.name = name;
			this.is = is;
		}

		/**
		 * ファイル名とファイルロケーションのURLを指定して、このクラスのインタンスを生成します。
		 * ファイル名には適切な拡張子が付けられている必要があります。
		 * 
		 * @param name メールに表示するファイル名
		 * @param url 添付ファイルのロケーションURL
		 */
		public AttachmentFile(String name, URL url) {
			this.name = name;
			this.url = url;
		}

		/**
		 * ファイル名とbyte配列を指定して、このクラスのインタンスを生成します。
		 * ファイル名には適切な拡張子が付けられている必要があります。
		 * 
		 * @param name メールに表示するファイル名
		 * @param bytes 添付ファイルを生成するbyte配列
		 */
		public AttachmentFile(String name, byte[] bytes) {
			this.name = name;
			this.bytes = bytes;
		}

		/**
		 * 添付ファイルのDataSourceインスタンスを生成して返します。
		 * 
		 * @return 添付ファイルのDataSourceインスタンス
		 */
		public DataSource getDataSource() {
			if (file != null) {
				return new FileDataSource(file);
			}

			if (url != null) {
				return new URLDataSource(url);
			}

			// InputStreamからDataSourceを生成
			String contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(name);
			if (is != null) {
				// InputStreamからDataSourceを生成
				return new ByteArrayDataSource(is, contentType);
			} else {
				// byte配列からDataSourceを生成
				return new ByteArrayDataSource(bytes, contentType);
			}
		}

		/**
		 * 添付ファイル名を返します。
		 * 
		 * @return 添付ファイル名
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return セットされたファイル。またはnull。
		 */
		public File getFile() {
			return file;
		}

		/**
		 * @return セットされたInputStream。またはnull。
		 */
		public InputStream getInputStream() {
			return is;
		}

		/**
		 * @return セットされたURL。またはnull。
		 */
		public URL getUrl() {
			return url;
		}

		/**
		 * @return セットされたbyte配列。またはnull。
		 */
		public byte[] getBytes() {
			return bytes;
		}
	}

	/**
	 * メールの重要度。定数のみを定義。
	 * 
	 * @author Tomohiro Otsuka
	 * @version $Id: Mail.java,v 1.10.2.9 2007/03/30 13:03:44 
	 */
	public static class Importance {

		/** 重要度「高」 */
		public static final String HIGH = "high";

		/** 重要度「中」 */
		public static final String NORMAL = "normal";

		/** 重要度「低」 */
		public static final String LOW = "low";

	}
}