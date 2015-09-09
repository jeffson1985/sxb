/*
 * @(#) $Id: EntityRefEncoder.java,v 1.1.2.1 2005/01/18 07:20:43 
 * $Revision: 1.1.2.1 $
 * Copyright (c) 2000 Shin Kinoshita All Rights Reserved.
 */

package org.sxb.mail.fetch.impl.sk_jp.text;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * &lt;&gt;&amp;&quot;を&amp;lt;&amp;gt;&amp;amp;&amp;quot;に
 * 変換するTranslatorです。
 * リエントラントなので、通常はINSTANCE/CANONICAL_INSTANCEを用いればよいです。
 * またStringオブジェクトに対してはencode()メソッドが使用できます。
 * @version $Revision: 1.1.2.1 $ $Date: 2005/01/18 07:20:43 $
 * @author Shin
 */
public class EntityRefEncoder implements Translator {
    public static final EntityRefEncoder CANONICAL_INSTANCE =
            new EntityRefEncoder(true);
    public static final EntityRefEncoder INSTANCE =
            new EntityRefEncoder(false);

    public EntityRefEncoder() {
        this(false);
    }
    public EntityRefEncoder(boolean canonical) {
        setCanonical(canonical);
    }

    private boolean canonicalStatus;
    private void setCanonical(boolean canonical) {
//  public void setCanonical(boolean canonical) {
        this.canonicalStatus = canonical;
    }

    /**
     * 文字ストリームから入力した文字列を任意の変換を
     * 行いながら出力ストリームに書き出します。
     * <p>
     * </p>
     */
    public void translate(Reader r, Writer w) throws IOException {
        int c;
        while ((c = r.read()) != -1) {
            translate((char)c, w, canonicalStatus);
        }
        w.flush();
    }

    public String translate(String source) {
        return encode(source);
    }
/*
    public static String encode(String s) {
        if (s == null) return "";

        StringWriter w = new StringWriter();
        try {
            EntityRefEncoder.INSTANCE.translate(new StringReader(s), w);
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return w.toString();
    }
*/

    /**
     * 文字列の実体参照化を行います。
     * @param s 対象文字列
     * @return 変換後文字列
     */
    // 似たようなコードを書きたくは無いが高速化の為…
    public static String encode(String s) {
        if (s == null) return "";

        int len = s.length();
        StringBuffer buf = new StringBuffer(len + 128);
        char c;

        for (int i = 0; i < len; i++) {
            c = s.charAt(i);
            switch (c) {
                case '<': buf.append("&lt;"); break;
                case '>': buf.append("&gt;"); break;
                case '&': buf.append("&amp;"); break;
                case '"': buf.append("&quot;"); break;
                default:
                    buf.append(c);
            }
        }
        return new String(buf);
    }

    /**
     * 特定の文字を実体参照に変換して書き出します。
     */
    public static void translate(char c, Writer w, boolean canonical)
                throws IOException {
        switch (c) {
            case '<': w.write("&lt;"); break;
            case '>': w.write("&gt;"); break;
            case '&': w.write("&amp;"); break;
            case '"': w.write("&quot;"); break;
            case '\r':
            case '\n':
                if (canonical) {
                    w.write("&#");
                    w.write(Integer.toString(c));
                    w.write(';');
                } else {
                    w.write(c);
                }
                break;
            default:
                w.write(c);
        }
    }

    /**
     * 実体参照変換されている文字列をもとに戻します。
     * @param s 対象文字列
     * @return 変換後文字列
     */
    public static String decode(String s) {
        if (s == null) return "";

        int len = s.length();
        StringBuffer buf = new StringBuffer(len);
        char c;

        for (int i = 0; i < len; i++) {
            c = s.charAt(i);
            if (c != '&' || i > len - 4) {
                buf.append(c);
                continue;
            }
            if ((s.charAt(i + 2) == 't' || s.charAt(i + 2) == 'T') &&
                s.charAt(i + 3) == ';') {
                switch (s.charAt(i + 1)) {
                    case 'l':
                    case 'L':
                        buf.append('<');
                        i += 3;
                        continue;
                    case 'g':
                    case 'G':
                        buf.append('>');
                        i += 3;
                        continue;
                }
            } else if (i < len - 4 &&
                (s.charAt(i + 1) == 'a' || s.charAt(i + 1) == 'A') &&
                (s.charAt(i + 2) == 'm' || s.charAt(i + 2) == 'M') &&
                (s.charAt(i + 3) == 'p' || s.charAt(i + 3) == 'P') &&
                s.charAt(i + 4) == ';') {
                buf.append('&');
                i += 4;
                continue;
            } else if (i < len - 5 &&
                (s.charAt(i + 1) == 'q' || s.charAt(i + 1) == 'Q') &&
                (s.charAt(i + 2) == 'u' || s.charAt(i + 2) == 'U') &&
                (s.charAt(i + 3) == 'o' || s.charAt(i + 3) == 'O') &&
                (s.charAt(i + 4) == 't' || s.charAt(i + 4) == 'T') &&
                s.charAt(i + 5) == ';') {
                buf.append('"');
                i += 5;
                continue;
            }
            buf.append(c);
        }
        return new String(buf);
    }
}
