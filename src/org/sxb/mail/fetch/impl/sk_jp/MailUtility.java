/*
 * @(#) $Id: MailUtility.java,v 1.1.2.1 2005/01/18 07:20:59 
 * Copyright (c) 2000-2004 Shin Kinoshita All Rights Reserved.
 */
package org.sxb.mail.fetch.impl.sk_jp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentDisposition;
import javax.mail.internet.ContentType;
import javax.mail.internet.HeaderTokenizer;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

import org.sxb.common.Base64;
import org.sxb.mail.fetch.impl.sk_jp.io.CharCodeConverter;
import org.sxb.mail.fetch.impl.sk_jp.io.UnicodeCorrector;
import org.sxb.mail.fetch.impl.sk_jp.text.EntityRefEncoder;
import org.sxb.mail.fetch.impl.sk_jp.util.StringValues;

import com.sun.mail.util.BASE64EncoderStream;

/**
 * JavaMailのサポートクラスです。
 * <P>
 * 主にヘッダに対するさまざまな加工機能を提供します。
 * </P>
 * 
 * @author Shin
 * @version $Revision: 1.1.2.1 $ $Date: 2005/01/18 07:20:59 $
 */
public class MailUtility {

	public static String getPersonal(InternetAddress a) {
		if (a.getPersonal() != null)
			return a.getPersonal();
		return a.toString();
	}

	/** get comma separated E-Mail addresses. */
	public static String getMailAddresses(InternetAddress[] addresses) {
		if (addresses == null)
			return null;
		StringValues buf = new StringValues();
		for (int i = 0; i < addresses.length; i++) {
			buf.add(addresses[i].getAddress());
		}
		return buf.getString();
	}

	/** get comma separated personal names. */
	public static String getPersonalNames(InternetAddress[] addresses) {
		if (addresses == null)
			return null;
		StringValues buf = new StringValues();
		String name;
		for (int i = 0; i < addresses.length; i++) {
			name = decodeText(unfold(addresses[i].getPersonal()));
			if (name == null) {
				name = addresses[i].toString();
			}
			buf.add(name);
		}
		return buf.getString();
	}

	public static String getAddressesHTML(InternetAddress[] addresses) {
		if (addresses == null)
			return null;
		StringValues buf = new StringValues();
		StringBuffer href = new StringBuffer();
		String name;
		for (int i = 0; i < addresses.length; i++) {
			href.append("<a href=\"mailto:");
			href.append(addresses[i].getAddress());
			href.append("\">");
			name = addresses[i].getPersonal();
			if (name != null) {
				name = decodeText(name);
			}
			if (name == null) {
				name = addresses[i].toString();
			}
			href.append(EntityRefEncoder.encode(name));
			href.append("</a>");
			buf.add(new String(href));
			href.setLength(0);
		}
		return buf.getString();
	}

	/** get the Content-Transfer-Encoding: header value. */
	public static String getTransferEncoding(byte[] b) {
		int nonAscii = 0;
		for (int i = 0; i < b.length; i++) {
			if (b[i] < 0) {
				nonAscii++;
			}
		}
		if (nonAscii == 0)
			return "7bit";
		if (nonAscii < b.length - nonAscii)
			return "quoted-printable";
		return "base64";
	}

	/**
	 * パートを保有する親Messageオブジェクトを返します。
	 * 
	 * @param part
	 *            パート
	 * @return ツリー構造の最上位にあたるメッセージオブジェクト
	 */
	public static Message getParentMessage(Part part) {
		Part current = part;
		Multipart mp;
		while (!(current instanceof Message)) {
			mp = ((BodyPart) current).getParent();
			if (mp == null)
				return null; // Should it throw exception?
			current = mp.getParent();
			if (current == null)
				return null; // Should it throw exception?
		}
		return (Message) current;
	}

	// ////////////////////////////////////////////////////////////////////////
	// note: JavaMail1.2 later
	private static MailDateFormat mailDateFormat = new MailDateFormat();

	/**
	 * Date構文の誤った"JST"タイムゾーンの補正を行います。
	 * <P>
	 * JavaMailは"JST"と記述されるタイムゾーンを解釈しません。 ここは本来"+0900"でなければならないところです。 <BR>
	 * 仕方がないので" JST"が含まれる文字列の場合は"+0900"を補完して
	 * MailDateFormat#parse()を通すようなparse()のラッパを用意します。
	 * </P>
	 * <P>
	 * この実装は一時回避的なものであり、完全なものではありません。
	 * </P>
	 */
	public static Date parseDate(String rfc822DateString) {
		if (rfc822DateString == null) {
			return null;
		}
		try {
			if (rfc822DateString.indexOf(" JST") == -1
					|| rfc822DateString.indexOf('+') >= 0) {
				synchronized (mailDateFormat) {
					return mailDateFormat.parse(rfc822DateString);
				}
			}
			// correct the pseudo header
			StringBuffer buf = new StringBuffer(rfc822DateString.substring(0,
					rfc822DateString.indexOf("JST")));
			buf.append("+0900");
			synchronized (mailDateFormat) {
				return mailDateFormat.parse(new String(buf));
			}
		} catch (java.text.ParseException e) {
			return null;
		}
	}

	// ////////////////////////////////////////////////////////////////////////
	/**
	 * Subject:に"Re: "を付加します。
	 * <P>
	 * ある程度寛容に"Re: "に近い文字列と"[hoge]"を取り除きます。 <BR>
	 * ただし、意図しない部分が消されてしまう事もあり得ます。 <BR>
	 * JavaMailのreply()では"Re: "がエンコードされていた場合に 正しく"Re: "を取り除いてくれません。
	 * </P>
	 */
	public static String createReplySubject(String src) {
		if (src == null || src.length() == 0) {
			return "Re: (no subject)";
		}
		String work = src;
		if (work.charAt(0) == '[' && work.indexOf(']') > 0) {
			int afterBracket = indexOfNonLWSP(work, work.indexOf(']') + 1,
					false);
			if (afterBracket < 0) {
				work = "";
			} else {
				work = work.substring(afterBracket);
			}
		}
		if (work.length() > 3 && "Re:".equalsIgnoreCase(work.substring(0, 3))) {
			int afterRe = indexOfNonLWSP(work, 3, false);
			if (afterRe < 0) {
				work = "";
			} else {
				work = work.substring(afterRe);
			}
		}
		return "Re: " + work;
	}

	// ////////////////////////////////////////////////////////////////////////
	/**
	 * 入力されたアドレスをInternetAddress形式に変換します。
	 * <p>
	 * "名無し君 &lt; abc@example.com &gt;(コメント)"等の文字列(エンコード無し)を
	 * 渡されても、正しくpersonal文字列が設定されるようにします。 <br>
	 * InternetAddress#parse()はエンコード済みの文字列を前提にしているため、 このメソッドの目的には沿いません。
	 * </p>
	 * 
	 * @param addressesString
	 *            メイルアドレス文字列(カンマ区切り)
	 */
	public static InternetAddress[] parseAddresses(String addressesString)
			throws AddressException {
		return parseAddresses(addressesString, true);
	}

	public static InternetAddress[] parseAddresses(String addressesString,
			boolean strict) throws AddressException {
		if (addressesString == null)
			return null;
		try {
			InternetAddress[] addresses = InternetAddress.parse(
					addressesString, strict);
			// correct personals
			for (int i = 0; i < addresses.length; i++) {
				addresses[i].setPersonal(addresses[i].getPersonal(),
						"ISO-2022-JP");
			}
			return addresses;
		} catch (UnsupportedEncodingException e) {
			throw new InternalError(e.toString());
		}
	}

	// InternetAddress.parse(
	// encodeText(addressesString, "ISO-2022-JP", "B"), strict);
	// で良さそうなものだが、これでは・・たしかなんか問題があったはず。
	// ////////////////////////////////////////////////////////////////////////
	/**
	 * header valueの unfolding を行います。 空白を厳密に扱うためには decodeText より先に呼び出す必要があります。
	 */
	public static String unfold(String source) {
		if (source == null)
			return null;
		StringBuffer buf = new StringBuffer();
		boolean skip = false;
		char c;
		// <CRLF>シーケンスを前提とするならindexOf()で十分ですが、
		// 念のためCR、LFいずれも許容します。
		for (int i = 0; i < source.length(); i++) {
			c = source.charAt(i);
			if (skip) {
				if (isLWSP(c)) {
					continue;
				}
				skip = false;
			}
			if (c != '\r' && c != '\n') {
				buf.append(c);
			} else {
				buf.append(' ');
				skip = true;
			}
		}
		return new String(buf);
	}

	/**
	 * header valueの folding を行います。
	 * <P>
	 * white spaceをfolding対象にします。 <BR>
	 * 76bytesを超えないwhite space位置に &lt; CRLF &gt;を挿入します。
	 * </P>
	 * <P>
	 * 注:quoteを無視しますので、structured fieldでは不都合が 発生する可能性があります。
	 * </P>
	 * 
	 * @param used
	 *            ヘッダの':'までの文字数。76 - usedが最初のfolding候補桁
	 * @return foldingされた( &lt; CRLF &gt;SPACEが挿入された)文字列
	 */
	public static String fold(String source, int used) {
		if (source == null)
			return null;
		StringBuffer buf = new StringBuffer();
		String work = source;
		int lineBreakIndex;
		while (work.length() > 76) {
			lineBreakIndex = work.lastIndexOf(' ', 76);
			if (lineBreakIndex == -1)
				break;
			buf.append(work.substring(0, lineBreakIndex));
			buf.append("\r\n");
			work = work.substring(lineBreakIndex);
		}
		buf.append(work);
		return new String(buf);
	}

	// ////////////////////////////////////////////////////////////////////////
	/**
	 * パートにテキストをセットします。 Part#setText() の代わりにこちらを使うことで、 "ISO-2022-JP"
	 * コンバータではエンコードできない CP932 の 文字をエンコードできます。
	 */
	public static void setTextContent(Part p, String s)
			throws MessagingException {
		// p.setText(content, "ISO-2022-JP");
		p.setDataHandler(new DataHandler(new JISDataSource(s)));
		p.setHeader("Content-Transfer-Encoding", "7bit");
	}

	/**
	 * 日本語を含むヘッダ用テキストを生成します。 変換結果は ASCII なので、これをそのまま setSubject や
	 * InternetAddress のパラメタとして使用してください。 "ISO-2022-JP" コンバータではエンコードできない CP932 の
	 * 文字をエンコードできます。ただし、encodeText() と異なり、 folding の意識をしておらず、また ASCII 部分を除いて分割
	 * エンコードを行うこともできません。
	 */
	public static String encodeWordJIS(String s) {
		try {
			return "=?ISO-2022-JP?B?"
					+ new String(BASE64EncoderStream.encode(CharCodeConverter
							.sjisToJis(UnicodeCorrector
									.getInstance("Windows-31J").correct(s)
									.getBytes("Windows-31J")))) + "?=";
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("CANT HAPPEN");
		}
	}

	// ////////////////////////////////////////////////////////////////////////
	/**
	 * ヘッダ内の文字列をデコードします。
	 * <p>
	 * MimeUtilityの制約を緩めて日本で流通するエンコード形式に対応。
	 * 本来は、encoded-wordとnon-encoded-wordの間にはlinear-white-spaceが必要
	 * なのですが、空白が無い場所でエンコードするタコメイラが多いので。
	 * </p>
	 * <p>
	 * JISコードをエンコード無しで記述するタコメイラもあります。 <br>
	 * ソースにESCが含まれていたら生JISと見なします。
	 * </p>
	 * <p>
	 * =?utf-8?Q?・・・JISコード・・?=なんてさらにタコなメイラも。 <br>
	 * 試しにデコード後にまだESCが残ってたらISO-2022-JPと見なすことにします。
	 * </p>
	 * <p>
	 * さらに、multibyte character の前後で別の encoded-word に切ってしまう メイラも…。隣接する
	 * encoded-word の CES が同じ場合はバイト列の 結合を行ってから CES デコードを行うようにした…。
	 * </p>
	 * <p>
	 * 日本語に特化してますねえ・・・。
	 * </p>
	 * 
	 * @param source
	 *            encoded text
	 * @return decoded text
	 */
	public static String decodeText(String source) {
		if (source == null)
			return null;
		// specially for Japanese
		if (source.indexOf('\u001b') >= 0) {
			// ISO-2022-JP
			try {
				return new String(source.getBytes("ISO-8859-1"), "ISO-2022-JP");
			} catch (UnsupportedEncodingException e) {
				throw new InternalError();
			}
		}
		String decodedText = new RFC2047Decoder(source).get();
		if (decodedText.indexOf('\u001b') >= 0) {
			try {
				return new String(decodedText.getBytes("ISO-8859-1"),
						"ISO-2022-JP");
			} catch (UnsupportedEncodingException e) {
				throw new InternalError();
			}
		}
		return decodedText;
	}

	// 日本語をデコードする上で問題があるので、encoded-wordの切り出しはすべて独自に
	// Netscapeなどは"()."等の文字でencoded-wordを切ってしまうが、JavaMailは
	// このときencoded-wordの終わりを判定できず、一部の文字を欠落させてしまう。
	// また、encoded-word を文字デコードするのを遅延させ、隣接する encoded-word
	// の CES が同じ場合は、先に TES デコードを行ったバイト列を結合してから
	// CES に従ったデコードを行う。マルチバイト文字を分断する sender がいるから。
	static class RFC2047Decoder {

		private String source;

		private String pooledCES;

		private byte[] pooledBytes;

		private StringBuffer buf;

		private int pos = 0;

		private int startIndex;

		private int endIndex;

		public RFC2047Decoder(String source) {
			this.source = source;
			buf = new StringBuffer(source.length());
			parse();
		}

		private void parse() {
			while (hasEncodedWord()) {
				String work = source.substring(pos, startIndex);
				if (indexOfNonLWSP(work, 0, false) > -1) {
					sweepPooledBytes();
					buf.append(work);
				} // encoded-word同士の間のLWSPは削除
				parseWord();
			}
			sweepPooledBytes();
			buf.append(source.substring(pos));
		}

		// encoded-word があった場合、startIndex/endIndex をセットする
		private boolean hasEncodedWord() {
			startIndex = source.indexOf("=?", pos);
			if (startIndex == -1)
				return false;
			endIndex = source.indexOf("?=", startIndex + 2);
			if (endIndex == -1)
				return false;
			// 本来は encoded-word 中に LWSP があってはいけないが
			// encoded-word の途中で folding してしまう sender がいるらしい
			// 以下をコメントにすることで encoded-word の誤認識の可能性も
			// 出てくるが、誤認識になる確率以上に前記のような illegal な
			// メッセージの方が多いのが実情のようだ。
			// thx > YOSI
			// int i = indexOfLWSP(source, startIndex + 2, false, (char)0);
			// if (i >= 0 && i < endIndex)
			// return false;
			endIndex += 2;
			return true;
		}

		private void parseWord() {
			try {
				int s = startIndex + 2;
				int e = source.indexOf('?', s);
				if (e == endIndex - 2)
					throw new RuntimeException();
				String ces = source.substring(s, e);
				try {
					"".getBytes(ces); // FIXME: check whether supported or not
				} catch (UnsupportedEncodingException ex) {
					ces = "JISAutoDetect";
				}
				s = e + 1;
				e = source.indexOf('?', s);
				if (e == endIndex - 2)
					throw new RuntimeException();
				String tes = source.substring(s, e);
				byte[] bytes = decodeByTES(
						source.substring(e + 1, endIndex - 2), tes);
				if (ces.equals(pooledCES)) {
					// append bytes
					byte[] w = new byte[pooledBytes.length + bytes.length];
					System.arraycopy(pooledBytes, 0, w, 0, pooledBytes.length);
					System.arraycopy(bytes, 0, w, pooledBytes.length,
							bytes.length);
					pooledBytes = w;
				} else {
					sweepPooledBytes();
					pooledCES = ces;
					pooledBytes = bytes;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				// contains RuntimeException
				buf.append(source.substring(startIndex, endIndex));
			}
			pos = endIndex;
		}

		private void sweepPooledBytes() {
			if (pooledBytes == null)
				return;
			try {
				buf.append(new String(pooledBytes, pooledCES));
			} catch (UnsupportedEncodingException e) {
				throw new InternalError("CANT HAPPEN: Illegal encoding = "
						+ pooledCES);
			}
			pooledCES = null;
			pooledBytes = null;
		}

		public String get() {
			return new String(buf);
		}
	}

	private static byte[] decodeByTES(String s, String tes) {
		// 通常あり得ないが、LWSP を詰める
		int i;
		while ((i = indexOfLWSP(s, 0, false, (char) 0)) >= 0)
			s = s.substring(0, i) + s.substring(i + 1);
		if (tes.equalsIgnoreCase("B") && s.length() % 4 != 0) {
			// BASE64DecoderStream は正確にパディングされていないと
			// IOException になるので、無理やり矯正。
			switch (4 - s.length() % 4) {
			case 1:
				s += '=';
				break;
			case 2:
				s += "==";
				break;
			case 3:
				if (s.charAt(s.length() - 1) != '=')
					s += "===";
				else
					s = s.substring(0, s.length() - 1);
				break;
			}
		}
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(
					com.sun.mail.util.ASCIIUtility.getBytes(s));
			InputStream is;
			if (tes.equalsIgnoreCase("B"))
				//is = new com.sun.mail.util.BASE64DecoderStream(bis);
			    is = Base64.getDecoder().wrap(bis);
			else if (tes.equalsIgnoreCase("Q"))
				is = new com.sun.mail.util.QDecoderStream(bis);
			else
				throw new UnsupportedEncodingException(tes);
			int count = bis.available();
			byte[] bytes = new byte[count];
			count = is.read(bytes, 0, count);
			if (count != bytes.length) {
				byte[] w = new byte[count];
				System.arraycopy(bytes, 0, w, 0, count);
				bytes = w;
			}
			return bytes;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("CANT HAPPEN");
		}
	}

	/**
	 * 文字列をエンコードします。
	 * <p>
	 * MimeUtility(強いてはMimeMessage等も)では、1字でも非ASCII文字が含まれる と文字列全体をエンコードしてしまいます。 <br>
	 * このメソッドでは空白で区切られた範囲だけをエンコードします。 <br>
	 * Subjectの"Re: "等がエンコードされていると、この文字列でIn-Reply-To:
	 * References:の代わりにスレッドを形成しようとしても失敗することになる
	 * ため、こちらのエンコード方式を用いたがる人もいるかもしれません・・。
	 * </p>
	 * <p>
	 * 方針は、ASCII部に前後の空白一つを含ませ、それ以外は空白も含めて全て
	 * encoded-wordとします。()の内側は空白無しでもエンコード対象です。
	 * </p>
	 * 
	 * @param source
	 *            text
	 * @return encoded text
	 */
	// "()" の扱いにこだわりすぎて異常に汚い-_-。
	// "()"なんか無視してまとめて encode するようにすればすっきるするけど…。
	public static String encodeText(String source, String charset,
			String encoding) throws UnsupportedEncodingException {
		if (source == null)
			return null;
		int boundaryIndex;
		int startIndex;
		int endIndex = 0;
		int lastLWSPIndex;
		StringBuffer buf = new StringBuffer();
		while (true) {
			// check the end of ASCII part
			boundaryIndex = indexOfNonAscii(source, endIndex);
			if (boundaryIndex == -1) {
				buf.append(source.substring(endIndex));
				return new String(buf);
			}
			// any LWSP has taken (back track).
			lastLWSPIndex = indexOfLWSP(source, boundaryIndex, true, '(');
			startIndex = indexOfNonLWSP(source, lastLWSPIndex, true) + 1;
			// ASCII part の終了位置は、次の non ASCII と比べて
			// 最も ASCII 文字よりの空白文字位置または'('の次位置
			startIndex = (endIndex > startIndex) ? endIndex : startIndex;
			if (startIndex > endIndex) {
				// ASCII part
				buf.append(source.substring(endIndex, startIndex));
				// JavaMailはencodeWord内でfoldingするけどそれはencodedWord
				// に対してのみ。ヘッダそのものに対するfoldingはしてくれない。
				if (isLWSP(source.charAt(startIndex))) {
					// folding により 空白一つが確保されるのでスキップ
					buf.append("\r\n ");
					startIndex++;
					// なお、'('の場合は空白を入れないので folding しない
				}
			}
			// any LWSP has taken.
			endIndex = indexOfNonLWSP(source, boundaryIndex, false);
			while ((endIndex = indexOfLWSP(source, endIndex, false, ')')) != -1) {
				endIndex = indexOfNonLWSP(source, endIndex, false);
				int nextBoundary = indexOfLWSP(source, endIndex, false,
						(char) 0);
				if (nextBoundary == -1) {
					if (indexOfNonAscii(source, endIndex) != -1) {
						endIndex = -1;
						break;
					}
				} else {
					int nonAscii = indexOfNonAscii(source, endIndex);
					if (nonAscii != -1 && nonAscii < nextBoundary) {
						endIndex = nextBoundary;
						continue;
					}
				}
				break;
			}
			boolean needFolding = false;
			if (endIndex < 0) {
				endIndex = source.length();
			} else if (isLWSP(source.charAt(endIndex - 1))) {
				// folding により 空白一つが確保される(予定)なので減らす
				endIndex--;
				needFolding = true;
			}
			String encodeTargetText = source.substring(startIndex, endIndex);
			buf.append(MimeUtility.encodeWord(encodeTargetText, charset,
					encoding));
			if (needFolding) {
				// folding により 空白一つが確保されるのでスキップ
				endIndex++;
				buf.append("\r\n ");
			}
		}
	}

	/**
	 * 指定位置から最初に見つかった非ASCII文字のIndexを返します。 startIndex が範囲外の場合は -1 を返します。
	 * (IndexOutOfBoundsException ではない)
	 * 
	 * @param source
	 *            検索する文字列
	 * @param startIndex
	 *            検索開始位置
	 * @return 検出した非ASCII文字Index。見つからなければ-1。
	 */
	public static int indexOfNonAscii(String source, int startIndex) {
		for (int i = startIndex; i < source.length(); i++) {
			if (source.charAt(i) > 0x7f) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 指定位置から最初に見つかったLWSP以外の文字のIndexを返します。 startIndex が範囲外の場合は -1 を返します。
	 * (IndexOutOfBoundsException ではない)
	 * 
	 * @param source
	 *            検索する文字列
	 * @param startIndex
	 *            検索開始位置
	 * @param decrease
	 *            trueで後方検索
	 * @return 検出した非ASCII文字Index。見つからなければ-1。
	 */
	public static int indexOfNonLWSP(String source, int startIndex,
			boolean decrease) {
		char c;
		int inc = 1;
		if (decrease)
			inc = -1;
		for (int i = startIndex; i >= 0 && i < source.length(); i += inc) {
			c = source.charAt(i);
			if (!isLWSP(c)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 指定位置から最初に見つかったLWSPのIndexを返します。 startIndex が範囲外の場合は -1 を返します。
	 * (IndexOutOfBoundsException ではない)
	 * 
	 * @param source
	 *            検索する文字列
	 * @param startIndex
	 *            検索開始位置
	 * @param decrease
	 *            trueで後方検索
	 * @param additionalDelimiter
	 *            LWSP以外に区切りとみなす文字(1字のみ)
	 * @return 検出した非ASCII文字Index。見つからなければ-1。
	 */
	public static int indexOfLWSP(String source, int startIndex,
			boolean decrease, char additionalDelimiter) {
		char c;
		int inc = 1;
		if (decrease)
			inc = -1;
		for (int i = startIndex; i >= 0 && i < source.length(); i += inc) {
			c = source.charAt(i);
			if (isLWSP(c) || c == additionalDelimiter) {
				return i;
			}
		}
		return -1;
	}

	public static boolean isLWSP(char c) {
		return c == '\r' || c == '\n' || c == ' ' || c == '\t';
	}

	// ////////////////////////////////////////////////////////////////////////
	/**
	 * This method set Content-Disposition: with RFC2231 encoding. It is
	 * required JavaMail1.2.
	 */
	/**
	 * Part#setFileName()のマルチバイト対応版です。 JavaMail1.2でなければコンパイルできません
	 */
	public static void setFileName(Part part, String filename, String charset,
			String lang) throws MessagingException {
		// Set the Content-Disposition "filename" parameter
		ContentDisposition disposition;
		String[] strings = part.getHeader("Content-Disposition");
		if (strings == null || strings.length < 1) {
			disposition = new ContentDisposition(Part.ATTACHMENT);
		} else {
			disposition = new ContentDisposition(strings[0]);
			disposition.getParameterList().remove("filename");
		}
		part.setHeader("Content-Disposition", disposition.toString()
				+ encodeParameter("filename", filename, charset, lang));
		ContentType cType;
		strings = part.getHeader("Content-Type");
		if (strings == null || strings.length < 1) {
			cType = new ContentType(part.getDataHandler().getContentType());
		} else {
			cType = new ContentType(strings[0]);
		}
		try {
			// I want to public the MimeUtility#doEncode()!!!
			String mimeString = MimeUtility.encodeWord(filename, charset, "B");
			// cut <CRLF>...
			StringBuffer sb = new StringBuffer();
			int i;
			while ((i = mimeString.indexOf('\r')) != -1) {
				sb.append(mimeString.substring(0, i));
				mimeString = mimeString.substring(i + 2);
			}
			sb.append(mimeString);
			cType.setParameter("name", new String(sb));
		} catch (UnsupportedEncodingException e) {
			throw new MessagingException("Encoding error", e);
		}
		part.setHeader("Content-Type", cType.toString());
	}

	/**
	 * This method encodes the parameter.
	 * <P>
	 * But most MUA cannot decode the encoded parameters by this method. <BR>
	 * I recommend using the "Content-Type:"'s name parameter both.
	 * </P>
	 */
	/**
	 * ヘッダのパラメタ部のエンコードを行います。
	 * <P>
	 * 現状は受信できないものが多いのでこのメソッドだけでは使えません。 <BR>
	 * Content-Disposition:のfilenameのみに使用し、さらに Content-Type:のnameにMIME
	 * encodingでの記述も行うのが妥当でしょう。 <BR>
	 * パラメタは必ず行頭から始まるものとします。 (ヘッダの開始行から折り返された位置を開始位置とします)
	 * </P>
	 * <P>
	 * foldingの方針はascii/non ascii境界のみをチェックします。 現状は連続するascii/non
	 * asciiの長さのチェックは現状行っていません。 (エンコード後のバイト数でチェックしなければならないのでかなり面倒)
	 * </P>
	 * 
	 * @param name
	 *            パラメタ名
	 * @param value
	 *            エンコード対象のパラメタ値
	 * @param encoding
	 *            文字エンコーディング
	 * @param lang
	 *            言語指定子
	 * @return エンコード済み文字列 ";\r\n name*0*=ISO-8859-2''・・・;\r\n name*1*=・・"
	 */
	// 1.全体をエンコードして長かったら半分に切ってエンコードを繰り返す
	public static String encodeParameter(String name, String value,
			String encoding, String lang) {
		StringBuffer result = new StringBuffer();
		StringBuffer encodedPart = new StringBuffer();
		boolean needWriteCES = !isAllAscii(value);
		boolean CESWasWritten = false;
		boolean encoded;
		boolean needFolding = false;
		int sequenceNo = 0;
		int column;
		while (value.length() > 0) {
			// index of boundary of ascii/non ascii
			int lastIndex;
			boolean isAscii = value.charAt(0) < 0x80;
			for (lastIndex = 1; lastIndex < value.length(); lastIndex++) {
				if (value.charAt(lastIndex) < 0x80) {
					if (!isAscii)
						break;
				} else {
					if (isAscii)
						break;
				}
			}
			if (lastIndex != value.length())
				needFolding = true;
			RETRY: while (true) {
				encodedPart.setLength(0);
				String target = value.substring(0, lastIndex);
				byte[] bytes;
				try {
					if (isAscii) {
						bytes = target.getBytes("us-ascii");
					} else {
						bytes = target.getBytes(encoding);
					}
				} catch (UnsupportedEncodingException e) {
					bytes = target.getBytes(); // use default encoding
					encoding = MimeUtility.mimeCharset(MimeUtility
							.getDefaultJavaCharset());
				}
				encoded = false;
				// It is not strict.
				column = name.length() + 7; // size of " " and "*nn*=" and ";"
				for (int i = 0; i < bytes.length; i++) {
					if ((bytes[i] >= '0' && bytes[i] <= '9')
							|| (bytes[i] >= 'A' && bytes[i] <= 'Z')
							|| (bytes[i] >= 'a' && bytes[i] <= 'z')
							|| bytes[i] == '$' || bytes[i] == '.'
							|| bytes[i] == '!') {
						// 2001/09/01 しかるべき文字が符号化されない問題修正
						// attribute-char(符号化しなくてもよい文字)の定義は
						// <any (US-ASCII) CHAR except SPACE, CTLs,
						// "*", "'", "%", or tspecials>
						// だが、ややこしいので英数字のみとしておく
						// "$.!"はおまけ^^。エンコード時は大して意識はいらない
						encodedPart.append((char) bytes[i]);
						column++;
					} else {
						encoded = true;
						encodedPart.append('%');
						String hex = Integer.toString(bytes[i] & 0xff, 16);
						if (hex.length() == 1) {
							encodedPart.append('0');
						}
						encodedPart.append(hex);
						column += 3;
					}
					if (column > 76) {
						needFolding = true;
						lastIndex /= 2;
						continue RETRY;
					}
				}
				result.append(";\r\n ").append(name);
				if (needFolding) {
					result.append('*').append(sequenceNo);
					sequenceNo++;
				}
				if (!CESWasWritten && needWriteCES) {
					result.append("*=");
					CESWasWritten = true;
					result.append(encoding).append('\'');
					if (lang != null)
						result.append(lang);
					result.append('\'');
				} else if (encoded) {
					result.append("*=");
					/*
					 * 本当にcharacter encodingは先頭パートに書かないとだめなのか? if (encoded) {
					 * result.append("*="); if (!CESWasWritten && needWriteCES)
					 * { CESWasWritten = true;
					 * result.append(encoding).append('\''); if (lang != null)
					 * result.append(lang); result.append('\''); }
					 */
				} else {
					result.append('=');
				}
				result.append(new String(encodedPart));
				value = value.substring(lastIndex);
				break;
			}
		}
		return new String(result);
	}

	/** check if contains only ascii characters in text. */
	public static boolean isAllAscii(String text) {
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) > 0x7f) { // non-ascii
				return false;
			}
		}
		return true;
	}

	// ////////////////////////////////////////////////////////////////////////
	/**
	 * This method decode the RFC2231 encoded filename parameter instead of
	 * Part#getFileName().
	 */
	/**
	 * Part#getFileName()のマルチバイト対応版です。
	 */
	public static String getFileName(Part part) throws MessagingException {
		String[] disposition = part.getHeader("Content-Disposition");
		// A patch by YOSI (Thanx)
		// http://www.sk-jp.com/cgibin/treebbs.cgi?kako=1&all=227&s=227
		String filename;
		if (disposition == null
				|| disposition.length < 1
				|| (filename = getParameter(disposition[0], "filename")) == null) {
			filename = part.getFileName();
			if (filename != null) {
				return decodeParameterSpciallyJapanese(filename);
			}
			return null;
		}
		return filename;
	}

	static class Encoding {

		String encoding = "us-ascii";

		String lang = "";
	}

	/**
	 * This method decodes the parameter which be encoded (folded) by RFC2231
	 * method.
	 * <P>
	 * The parameter's order should be considered.
	 * </P>
	 */
	/**
	 * ヘッダのパラメタ部のデコードを行います。
	 * <P>
	 * RFC2231形式でfolding(分割)されたパラメタを結合し、デコードします。
	 * 尚、RFC2231にはパラメタの順番に依存するなと書かれていますが、 それを実装すると大変面倒(一度分割された全てのパートを
	 * 保持してソートしなければならない)なので、 シーケンス番号に関係なく(0から)順番に 並んでいるものとみなして処理することにします。
	 * </P>
	 * 
	 * @param header
	 *            ヘッダの値全体
	 * @param name
	 *            取得したいパラメタ名
	 * @return デコード済み文字列 (パラメタが存在しない場合は null)
	 */
	public static String getParameter(String header, String name)
			throws ParseException {
		if (header == null)
			return null;
		// 本来これは不要。日本固有のデコード処理です。
		// 2001/07/22 書籍版では"あ.txt"の生JISパラメタ値がデコードできない
		// これは、ISO-2022-JPバイト列のままHeaderTokenizerにかけると、
		// "あ"のバイトシーケンスに含まれる0x22がダブルクォートと
		// 解釈されるため。
		// JIS/Shift_JISの生バイトと思われるもののデコードを先に行う事で回避
		header = decodeParameterSpciallyJapanese(header);
		HeaderTokenizer tokenizer = new HeaderTokenizer(header, ";=\t ", true);
		HeaderTokenizer.Token token;
		StringBuffer sb = new StringBuffer();
		// It is specified in first encoded-part.
		Encoding encoding = new Encoding();
		String n;
		String v;
		try {
			while (true) {
				token = tokenizer.next();
				if (token.getType() == HeaderTokenizer.Token.EOF)
					break;
				if (token.getType() != ';')
					continue;
				token = tokenizer.next();
				checkType(token);
				n = token.getValue();
				token = tokenizer.next();
				if (token.getType() != '=') {
					throw new ParseException("Illegal token : "
							+ token.getValue());
				}
				token = tokenizer.next();
				checkType(token);
				v = token.getValue();
				if (n.equalsIgnoreCase(name)) {
					// It is not divided and is not encoded.
					return v;
				}
				int index = name.length();
				if (!n.startsWith(name) || n.charAt(index) != '*') {
					// another parameter
					continue;
				}
				// be folded, or be encoded
				int lastIndex = n.length() - 1;
				if (n.charAt(lastIndex) == '*') {
					// http://www.sk-jp.com/cgibin/treebbs.cgi?all=399&s=399
					if (index == lastIndex || n.charAt(index + 1) == '0') {
						// decode as initial-section
						sb.append(decodeRFC2231(v, encoding, true));
					} else {
						// decode as other-sections
						sb.append(decodeRFC2231(v, encoding, false));
					}
				} else {
					sb.append(v);
				}
				if (index == lastIndex) {
					// not folding
					break;
				}
			}
			if (sb.length() == 0)
				return null;
			return new String(sb);
		} catch (UnsupportedEncodingException e) {
			throw new ParseException(e.toString());
		}
	}

	private static void checkType(HeaderTokenizer.Token token)
			throws ParseException {
		int t = token.getType();
		if (t != HeaderTokenizer.Token.ATOM
				&& t != HeaderTokenizer.Token.QUOTEDSTRING) {
			throw new ParseException("Illegal token : " + token.getValue());
		}
	}

	// "lang" tag is ignored...
	private static String decodeRFC2231(String s, Encoding encoding,
			boolean isInitialSection) throws ParseException,
			UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer();
		int i = 0;
		if (isInitialSection) {
			int work = s.indexOf('\'');
			if (work > 0) {
				encoding.encoding = s.substring(0, work);
				work++;
				i = s.indexOf('\'', work);
				if (i < 0) {
					throw new ParseException("lang tag area was missing.");
				}
				encoding.lang = s.substring(work, i);
				i++;
			}
		}
		try {
			for (; i < s.length(); i++) {
				if (s.charAt(i) == '%') {
					sb.append((char) Integer.parseInt(
							s.substring(i + 1, i + 3), 16));
					i += 2;
					continue;
				}
				sb.append(s.charAt(i));
			}
			return new String(new String(sb).getBytes("ISO-8859-1"),
					encoding.encoding);
		} catch (IndexOutOfBoundsException e) {
			throw new ParseException(s + " :: this string were not decoded.");
		}
	}

	// 日本語向けデコード
	private static String decodeParameterSpciallyJapanese(String s)
			throws ParseException {
		try {
			// decode by character encoding.
			// if string are all ASCII, it is not translated.
			s = new String(s.getBytes("ISO-8859-1"), "JISAutoDetect");
			// decode by RFC2047.
			// if string doesn't contain encoded-word, it is not translated.
			return decodeText(s);
		} catch (UnsupportedEncodingException e) {
		}
		throw new ParseException("Unsupported Encoding");
	}

	private MailUtility() {
	}
}