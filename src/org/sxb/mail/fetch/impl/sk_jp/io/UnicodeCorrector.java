/*
 * @(#) $Id: UnicodeCorrector.java,v 1.1.2.1 2005/01/18 07:20:36 
 * $Revision: 1.1.2.1 $
 * Copyright (c) 2000 Shin Kinoshita All Rights Reserved.
 */
package org.sxb.mail.fetch.impl.sk_jp.io;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * UnicodeCorrector.
 * <p>
 * CorrectOutputStreamWriterで用いられる文字列バッファ補正クラスです。<br>
 * 出力エンコーディングによって補正すべき文字コードが異なるので、
 * 実際の補正処理はサブクラスで行います。<br>
 *
 * 対応しているのは以下のコンバータ名です。
 * </p>
 * <UL>
 * <LI>ISO2022JP
 * <LI>ISO-2022-JP
 * <LI>EUC-JP
 * <LI>EUCJIS
 * <LI>SJIS
 * <LI>Shift_JIS
 * <LI>MS932
 * <LI>Windows-31J
 * </UL>
 * @author Shin
 * @version $Revision: 1.1.2.1 $ $Date: 2005/01/18 07:20:36 $
 */
public abstract class UnicodeCorrector {
    private static final Map<String, Class<?>> correctorMap = new HashMap<String, Class<?>>();
    static {
        // x-sjis-cp932等でおかしく変換されたUnicodeを他のエンコーディングで
        // 出力する場合です
        correctorMap.put("iso2022jp", FromCP932Corrector.class);
        correctorMap.put("iso-2022-jp", FromCP932Corrector.class);
        correctorMap.put("euc-jp", FromCP932Corrector.class);
        correctorMap.put("eucjis", FromCP932Corrector.class);
        // 同じ"SJIS"でもSunとMS-VMでは異なります。
        // Sun-JDK1.1はSJISはx-sjis-jdk-1.1.7で、MS-VMはx-sjis-cp932です。
        // しかし、MS-JDK中のクラスライブラリだけはx-sjis-jdk-1.1.7相当になるので
        // 困ったものです。(ファイル書き出し等のときのみおかしくなる)
        // これはWindowsネイティブな変換表で生成されたUnicodeを
        // x-sjis-jdk-1.1.7で出力するためのものです。
        correctorMap.put("sjis", FromCP932Corrector.class);
        correctorMap.put("shift_jis", FromCP932Corrector.class);
        // MS-VMでファイル出力を行う場合に限り、こちらでなければなりません。
        //      correctorMap.put("sjis", ToCP932Corrector.class);
        // JDK1.2からはWindowsのデフォルトが"MS932"(x-sjis-cp932相当)となりました。
        // これで、他のエンコーディングで変換された(正しい)Unicodeが"ms932"
        // での出力時におかしくなってしまいます。
        // これを補正します。
        correctorMap.put("ms932", ToCP932Corrector.class);
        correctorMap.put("windows-31j", ToCP932Corrector.class);
    }

    /**
     * Create an UnicodeCorrector that uses
     * the named character encoding.
     * @param  enc  Name of the encoding to be used
     * @exception  UnsupportedEncodingException
     *             If the named encoding is not supported
     */
    public static UnicodeCorrector getInstance(String enc)
            throws UnsupportedEncodingException {
        Class<?> correctorClass = (Class<?>)correctorMap.get(enc.toLowerCase());
        if (correctorClass == null) {
            throw new UnsupportedEncodingException(enc);
        }
        try {
            return (UnicodeCorrector)correctorClass.newInstance();
        } catch (Exception e) {
            throw new UnsupportedEncodingException(
                    correctorClass + " cannot get newInstance.\n" + e);
        }
    }

    /**
     * Unicode文字配列の補正を行います。
     * <p>
     * 特定の文字を特定エンコーディングで出力しようとした際の
     * sun.ioコンバータでは正常に変換できない部分を補正します。
     * </p>
     * @param  cbuf  Buffer of characters
     * @param  off   Offset from which to start writing characters
     * @param  len   Number of characters to write
     * @return Result that corrected.
     *         Note:Return array is different from <code>cbuf</code>
     *              in case of different result size.
     */
    public char[] correct(char cbuf[], int off, int len) {
        StringBuffer buf = new StringBuffer();
        for (int i = off; i < len; i++) {
            buf.append(correct(cbuf[i]));
        }
        return new String(buf).toCharArray();
    }

    public String correct(String s) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            buf.append(correct(s.charAt(i)));
        }
        return new String(buf);
    }

    // 性能的にこれをabstractにするのはためらったが・・・
    public abstract char correct(char c);

    /**
     * 新しいUnicodeCorrectorを追加します。
     * <p>
     * このソースコードを変えずに、動的に新たな出力エンコーディングに
     * 対応したUnicodeCorrectorを登録したい場合に用います。
     * </p>
     * @param enc 対応するエンコーディング名
     * @param correctorClass UnicodeCorrectorサブクラスのClassオブジェクト
     */
    public static void addCorrector(String enc, Class<?> correctorClass) {
        if (!correctorClass.isInstance(UnicodeCorrector.class)) {
            throw new IllegalArgumentException(
                    "Corrector is not UnicodeCorrector type.");
        }
        correctorMap.put(enc, correctorClass);
    }
}