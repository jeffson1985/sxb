/*
 * Copyright (c) 2003 Shin Kinoshita All Rights Reserved.
 */
package org.sxb.mail.fetch.impl.sk_jp.io;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * 文字関係のコンバータです。
 * 一部コードのオリジナルは<a href="http://www-cms.phys.s.u-tokyo.ac.jp/~naoki/CIPINTRO/CCGI/kanjicod.html">Japanese Kanji Code</a>にて公開されているものです。
 * また、http://www.sk-jp.com/cgi-bin/treebbs.cgi?kako=1&all=644&s=681
 * にて YOSI さんが公開されたコードも参考にしています(というか実質同じです)。
 *
 * @author Shin
 * @version $Revision: 1.1.2.1 $ $Date: 2005/01/18 07:20:36 $
 */
public class CharCodeConverter {
    public static final byte[] SJIS_KANA;
    static {
        try {
            // 全角への変換テーブル
            SJIS_KANA = "。「」、・ヲァィゥェォャュョッーアイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワン゛゜".getBytes("Shift_JIS");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("CANT HAPPEN");
        }
    }

    /**
     * Shift_JIS エンコーディングスキームに基づくバイト列を
     * ISO-2022-JP エンコーディングスキームに変換します。
     * 「半角カナ」は対応する全角文字に変換します。
     */
    public static byte[] sjisToJis(byte[] sjisBytes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean nonAscii = false;
        int len = sjisBytes.length;
        for (int i = 0; i < len; i++ ) {
            if (sjisBytes[i] >= 0) {
                if (nonAscii) {
                    nonAscii = false;
                    out.write(0x1b);
                    out.write('(');
                    out.write('B');
                }
                out.write(sjisBytes[i]);
            } else {
                if (!nonAscii) {
                    nonAscii = true;
                    out.write(0x1b);
                    out.write('$');
                    out.write('B');
                }
                int b = sjisBytes[i] & 0xff;
                if (b >= 0xa1 && b <= 0xdf) {
                    // 半角カナは全角に変換
                    int kanaIndex = (b - 0xA1) * 2;
                    sjisToJis(out, SJIS_KANA[kanaIndex], SJIS_KANA[kanaIndex + 1]);
                } else {
                    i++;
                    if (i == len) break;
                    sjisToJis(out, sjisBytes[i - 1], sjisBytes[i]);
                }
            }
        }
        if (nonAscii) {
            out.write(0x1b);
            out.write('(');
            out.write('B');
        }
        return out.toByteArray();
    }
    /**
     * １文字の２バイト Shift_JIS コードを JIS コードに変換して書き出します。
     */
    private static void sjisToJis(
                ByteArrayOutputStream out, byte bh, byte bl) {
        int h = (bh << 1) & 0xFF;
        int l = bl & 0xFF;
        if (l < 0x9F) {
            if (h < 0x3F) h += 0x1F; else h -= 0x61;
            if (l > 0x7E) l -= 0x20; else l -= 0x1F;
        } else {
            if (h < 0x3F) h += 0x20; else h -= 0x60;
            l -= 0x7E;
        }
        out.write(h);
        out.write(l);
    }

    /**
     * 配列はワイドキャラクタの境界にないことを前提としています。
     * また、エスケープシーケンスが適切に含まれることも前提です。
     * エスケープシーケンスが"(B"/"$B"以外の場合は無視します。
     */
    public byte[] jisTosjis(byte[] jisBytes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean nonAscii = false;
        boolean kana = false;
        int len = jisBytes.length;
        for (int i = 0; i < len; i++) {
            if (jisBytes[i] == 0x1b) {
                if (i + 2 >= len) break;
                if (jisBytes[i + 1] == '$' && jisBytes[i + 2] == 'B') {
                    nonAscii = true;
                    i += 2;
                } else if (jisBytes[i + 1] == '(' && jisBytes[i + 2] == 'I') {
                    kana = true;
                    i += 2;
                } else if (jisBytes[i + 1] == '(' && jisBytes[i + 2] == 'B') {
                    nonAscii = false;
                    kana = false;
                    i += 2;
                } else {
                    // illegal sequence は当面無視
                    nonAscii = false;
                    kana = false;
                }
                continue;
            }
            if (jisBytes[i] == 0x0e) { // SO
                kana = true;
                continue;
            }
            if (jisBytes[i] == 0x0f) { // SI
                kana = false;
                continue;
            }
            if (kana) {
                out.write(jisBytes[i] | 0x80);
            } else if (nonAscii) {
                i++;
                if (i == jisBytes.length) break;
                jisToSjis(out, jisBytes[i - 1], jisBytes[i]);
            } else {
                out.write(jisBytes[i]);
            }
        }
        return out.toByteArray();
    }
    /**
     * １文字の２バイト JIS コードを Shift_JIS に変換して書き出します。
     */
    private static void jisToSjis(
                ByteArrayOutputStream out, byte bh, byte bl) {
        int h = bh & 0xFF;
        int l = bl & 0xFF;
        if ((h & 0x01) > 0) {
            h >>= 1;
            if (h < 0x2F) h += 0x71; else h -= 0x4F;
            if (l > 0x5F) l += 0x20; else l += 0x1F;
        } else {
            h >>= 1;
            if (h < 0x2F) h += 0x70; else h -= 0x50;
            l += 0x7E;
        }
        out.write(h);
        out.write(l);
    }
}
