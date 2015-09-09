/*
 * @(#) $Id: FromCP932Corrector.java,v 1.1.2.1 2005/01/18 07:20:36 
 * $Revision: 1.1.2.1 $
 * Copyright (c) 2000 Shin Kinoshita All Rights Reserved.
 */
package org.sxb.mail.fetch.impl.sk_jp.io;

/**
 * FromCP932Corrector.
 * <p>
 * CorrectOutputStreamWriterで用いられる文字列バッファ補正クラスです。<br>
 * 風間一洋さんのJavaHouse-Brewers投稿記事[14452]のCp932クラスから
 * 変換表を参考にさせていただいています。<br>
 * </p>
 * @version $Revision: 1.1.2.1 $ $Date: 2005/01/18 07:20:36 $
 * @author Shin
 */
public class FromCP932Corrector extends UnicodeCorrector {
    /**
     * Unicode文字配列の補正を行います。
     * <p>
     * "MS932""Shift_JIS"エンコーディング以外で出力しようとした際の
     * sun.ioやcom.msコンバータでは正常に変換できない部分を補正します。
     * </p>
     * @param  c     source character
     * @return Result character that corrected.
     */
    public char correct(char c) {
        switch (c) {
            // ISO-2022-JPコンバータが正しく解釈しているようだ
//          case 0xff3c:        // FULLWIDTH REVERSE SOLIDUS ->
//              return 0x005c;  // REVERSE SOLIDUS
            case 0xff5e:        // FULLWIDTH TILDE ->
                return 0x301c;  // WAVE DASH
            case 0x2225:        // PARALLEL TO ->
                return 0x2016;  // DOUBLE VERTICAL LINE
            case 0xff0d:        // FULLWIDTH HYPHEN-MINUS ->
                return 0x2212;  // MINUS SIGN
            case 0xffe0:        // FULLWIDTH CENT SIGN ->
                return 0x00a2;  // CENT SIGN
            case 0xffe1:        // FULLWIDTH POUND SIGN ->
                return 0x00a3;  // POUND SIGN
            case 0xffe2:        // FULLWIDTH NOT SIGN ->
                return 0x00ac;  // NOT SIGN
        }
        return c;
    }
}
