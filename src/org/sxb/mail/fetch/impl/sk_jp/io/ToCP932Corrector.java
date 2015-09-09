/*
 * @(#) $Id: ToCP932Corrector.java,v 1.1.2.1 2005/01/18 07:20:36 
 * $Revision: 1.1.2.1 $
 * Copyright (c) 2000 Shin Kinoshita All Rights Reserved.
 */
package org.sxb.mail.fetch.impl.sk_jp.io;

/**
 * ToCP932Corrector.
 * <p>
 * CorrectOutputStreamWriterで用いられる文字列バッファ補正クラスです。<br>
 * 風間一洋さんのJavaHouse-Brewers投稿記事[14452]のCp932クラス
 * (XML日本語プロファイルも同じです)から
 * 変換表を参考にさせていただいています。<br>
 * </p>
 * @version $Revision: 1.1.2.1 $ $Date: 2005/01/18 07:20:36 $
 * @author Shin
 */
public class ToCP932Corrector extends UnicodeCorrector {
    /**
     * Unicode文字の補正を行います。
     * <p>
     * 特定の文字を"MS932""Shift_JIS"エンコーディングで出力しようとした際の
     * sun.ioコンバータでは正常に変換できない部分を補正します。
     * </p>
     * @param  c     source character
     * @return Result character that corrected.
     */
    public char correct(char c) {
        switch (c) {
//          case 0x005c:        // REVERSE SOLIDUS ->
//              return 0xff3c;  // FULLWIDTH REVERSE SOLIDUS
            case 0x301c:        // WAVE DASH ->
                return 0xff5e;  // FULLWIDTH TILDE
            case 0x2016:        // DOUBLE VERTICAL LINE ->
                return 0x2225;  // PARALLEL TO
            case 0x2212:        // MINUS SIGN ->
                return 0xff0d;  // FULLWIDTH HYPHEN-MINUS
            // MS932コンバータが正しく解釈しているようだ
/*
            case 0x00a2:        // CENT SIGN ->
                return 0xffe0;  // FULLWIDTH CENT SIGN
            case 0x00a3:        // POUND SIGN ->
                return 0xffe1;  // FULLWIDTH POUND SIGN
            case 0x00ac:        // NOT SIGN ->
                return 0xffe2;  // FULLWIDTH NOT SIGN
*/
        }
        return c;
    }
}
