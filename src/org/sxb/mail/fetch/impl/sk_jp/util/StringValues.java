/*
 * @(#) $Id: StringValues.java,v 1.1.2.1 2005/01/18 07:20:49 
 * $Revision: 1.1.2.1 $
 */
package org.sxb.mail.fetch.impl.sk_jp.util;

import java.util.*;

/**
 * StringValues.
 * <p>
 * 複数の文字列をカンマやタブ区切りなどの一つの文字列として管理可能とします。
 * <br>内部的にはListに入れますのでindexによるアクセスも可能ですが、
 * 構築時、及びgetString()時に区切り文字で一本にされた文字列として
 * やりとりすることもできます。
 * エスケープ処理がされないので、デリミタを含むトークンを認識できません。
 * </p>
 * @version $Revision: 1.1.2.1 $ $Date: 2005/01/18 07:20:49 $
 * @author Shin
 */
public class StringValues {
    private List<String>                src_ = new ArrayList<String>();
    /**
     * 複数文字列管理オブジェクトを生成します。
     */
    public StringValues() {}
    /**
     * 複数文字列管理オブジェクトを生成します。
     * @param str 区切り文字で連結された文字列
     */
    public StringValues(String str) {
        parse(str);
    }
    /**
     * 複数文字列管理オブジェクトを生成します。
     * @param str 区切り文字で連結された文字列
     * @param delim デリミタ
     */
    public StringValues(String str, String delim) {
        parse(str, delim);
    }
    /**
     * 複数文字列管理オブジェクトを生成します。
     * @param strings 文字列の配列
     */
    public StringValues(String[] strings) {
        add(strings);
    }
    /**
     * 複数文字列管理オブジェクトを生成します。
     * @param strings 文字列の配列
     */
    public StringValues(Object[] o) {
        for (int i = 0; i < o.length; i++) {
            add(o[i].toString());
        }
    }
//////////////////////////////////////////////////////////////////////////////
    /**
     * デリミタで区切られた文字列を分割して追加します。
     * <p>
     * デリミタはStringTokenizerの標準のデリミタ
     * <code>"&#92;t&#92;n&#92;r&#92;f"</code>が使われます。
     * </p>
     * @param str 区切り文字で連結された文字列
     * @param delim デリミタ
     */
    public void parse(String str) {
        StringTokenizer         st = new StringTokenizer(str);
        parse(st);
    }
    /**
     * デリミタで区切られた文字列を分割して追加します。
     * <p>
     * </p>
     * @param str 区切り文字で連結された文字列
     * @param delim デリミタ
     */
    public void parse(String str, String delim) {
        StringTokenizer         st = new StringTokenizer(str, delim);
        parse(st);
    }
    private void parse(StringTokenizer st) {
        while (st.hasMoreTokens()) {
            add(st.nextToken());
        }
    }
    /**
     * 文字列群へのイテレータを返します。
     * @return Iteratorオブジェクト
     */
    public Iterator<String> iterator() {
        return src_.iterator();
    }

    /**
     * カンマ区切り文字列を得ます。
     * <p>
     * 区切り文字列は文字列同士の間に単純に挿入されます。
     * </p>
     * @return 文字列化したオブジェクト
     */
    public String getString() {
        return getString(", ");
    }
    /**
     * 指定した区切り文字で連結された文字列を得ます。
     * <p>
     * 区切り文字列は文字列同士の間に単純に挿入されます。
     * </p>
     * @param delim 区切り文字列
     * @return 文字列化したオブジェクト
     */
    public String getString(String delim) {
        StringBuffer            buf = new StringBuffer();
        Iterator<?>                iterator = iterator();
        if (iterator.hasNext()) {
            buf.append(iterator.next());
        }
        while (iterator.hasNext()) {
            buf.append(delim).append(iterator.next());
        }
        return new String(buf);
    }
    public String toString() {
        return getString();
    }
    /**
     * 指定indexの文字列を取得します。
     * @param index 文字列群中の位置
     */
    public String get(int index) {
        return (String)src_.get(index);
    }
    /**
     * 文字列を追加します。
     * @param str 追加する文字列
     */
    public void add(String str) {
        src_.add(str);
    }
    /**
     * 文字列群を追加します。
     * @param str 追加する文字列
     */
    public void add(String[] str) {
        for (int i = 0; i < str.length; i++) {
            src_.add(str[i]);
        }
    }
    /**
     * 管理している文字列を削除します。
     */
    public void clear() {
        src_.clear();
    }
}
