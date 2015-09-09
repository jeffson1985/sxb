/*
 * @(#) $Id: Translator.java,v 1.1.2.1 2005/01/18 07:20:43 
 * $Revision: 1.1.2.1 $
 * Copyright (c) 2000 Shin Kinoshita All Rights Reserved.
 */

package org.sxb.mail.fetch.impl.sk_jp.text;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Character Translatorのインターフェイスです。
 * @version $Revision: 1.1.2.1 $ $Date: 2005/01/18 07:20:43 $
 * @author Shin
 */
public interface Translator {
    /**
     * 文字ストリームから入力した文字列を任意の変換を
     * 行いながら出力ストリームに書き出します。
     * <p>
     * フィルタとして用いる場合はPipedInput/OutputStreamと併用しましょう。<BR>
     * このメソッドの実装者はリエントラントに設計すべきでしょう。
     * </p>
     */
    void translate(Reader r, Writer w) throws IOException;
    /**
     * 入力文字列に任意の変換を施した文字列を返します。
     */
    String translate(String src);
}
