/**
 * Copyright (c) 2011-2015, Jeff  Son   (jeffson.app@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sxb.ext.kit.jp;

/**
 * 半角と全角の変換テーブルクラスです。 <br />
 * 
 */
public class KanaMapTable {

  /** インデックス（半角） */
  public static final int INDEX_HANKAKU = 0;

  /** インデックス（全角） */
  public static final int INDEX_ZENKAKU = 1;

  /** インデックス（半角濁点なし） */
  public static final int INDEX_HANKAKU_BASE = 0;

  /** インデックス（半角濁点） */
  public static final int INDEX_HANKAKU_DAKUTEN = 1;

  /** 半角と全角の変換テーブル */
  public static final char[][][] TABLE_HANKAKU2ZENKAKU = new char[][][] {
    { { '。' }, { '。' } },
    { { '「' }, { '「' } },
    { { '」' }, { '」' } },
    { { '、' }, { '、' } },
    { { '・' }, { '・' } },
    { { 'ヲ' }, { 'ヲ' } },
    { { 'ァ' }, { 'ァ' } },
    { { 'ィ' }, { 'ィ' } },
    { { 'ゥ' }, { 'ゥ' } },
    { { 'ェ' }, { 'ェ' } },
    { { 'ォ' }, { 'ォ' } },
    { { 'ャ' }, { 'ャ' } },
    { { 'ュ' }, { 'ュ' } },
    { { 'ョ' }, { 'ョ' } },
    { { 'ッ' }, { 'ッ' } },
    { { 'ー' }, { 'ー' } },
    { { 'ア' }, { 'ア' } },
    { { 'イ' }, { 'イ' } },
    { { 'ウ' }, { 'ウ' } },
    { { 'ウ', '゛' }, { 'ヴ' } },
    { { 'エ' }, { 'エ' } },
    { { 'オ' }, { 'オ' } },
    { { 'カ' }, { 'カ' } },
    { { 'カ', '゛' }, { 'ガ' } },
    { { 'キ' }, { 'キ' } },
    { { 'キ', '゛' }, { 'ギ' } },
    { { 'ク' }, { 'ク' } },
    { { 'ク', '゛' }, { 'グ' } },
    { { 'ケ' }, { 'ケ' } },
    { { 'ケ', '゛' }, { 'ゲ' } },
    { { 'コ' }, { 'コ' } },
    { { 'コ', '゛' }, { 'ゴ' } },
    { { 'サ' }, { 'サ' } },
    { { 'サ', '゛' }, { 'ザ' } },
    { { 'シ' }, { 'シ' } },
    { { 'シ', '゛' }, { 'ジ' } },
    { { 'ス' }, { 'ス' } },
    { { 'ス', '゛' }, { 'ズ' } },
    { { 'セ' }, { 'セ' } },
    { { 'セ', '゛' }, { 'ゼ' } },
    { { 'ソ' }, { 'ソ' } },
    { { 'ソ', '゛' }, { 'ゾ' } },
    { { 'タ' }, { 'タ' } },
    { { 'タ', '゛' }, { 'ダ' } },
    { { 'チ' }, { 'チ' } },
    { { 'チ', '゛' }, { 'ヂ' } },
    { { 'ツ' }, { 'ツ' } },
    { { 'ツ', '゛' }, { 'ヅ' } },
    { { 'テ' }, { 'テ' } },
    { { 'テ', '゛' }, { 'デ' } },
    { { 'ト' }, { 'ト' } },
    { { 'ト', '゛' }, { 'ド' } },
    { { 'ナ' }, { 'ナ' } },
    { { 'ニ' }, { 'ニ' } },
    { { 'ヌ' }, { 'ヌ' } },
    { { 'ネ' }, { 'ネ' } },
    { { 'ノ' }, { 'ノ' } },
    { { 'ハ' }, { 'ハ' } },
    { { 'ハ', '゛' }, { 'バ' } },
    { { 'ハ', '゜' }, { 'パ' } },
    { { 'ヒ' }, { 'ヒ' } },
    { { 'ヒ', '゛' }, { 'ビ' } },
    { { 'ヒ', '゜' }, { 'ピ' } },
    { { 'フ' }, { 'フ' } },
    { { 'フ', '゛' }, { 'ブ' } },
    { { 'フ', '゜' }, { 'プ' } },
    { { 'ヘ' }, { 'ヘ' } },
    { { 'ヘ', '゛' }, { 'ベ' } },
    { { 'ヘ', '゜' }, { 'ペ' } },
    { { 'ホ' }, { 'ホ' } },
    { { 'ホ', '゛' }, { 'ボ' } },
    { { 'ホ', '゜' }, { 'ポ' } },
    { { 'マ' }, { 'マ' } },
    { { 'ミ' }, { 'ミ' } },
    { { 'ム' }, { 'ム' } },
    { { 'メ' }, { 'メ' } },
    { { 'モ' }, { 'モ' } },
    { { 'ヤ' }, { 'ヤ' } },
    { { 'ユ' }, { 'ユ' } },
    { { 'ヨ' }, { 'ヨ' } },
    { { 'ラ' }, { 'ラ' } },
    { { 'リ' }, { 'リ' } },
    { { 'ル' }, { 'ル' } },
    { { 'レ' }, { 'レ' } },
    { { 'ロ' }, { 'ロ' } },
    { { 'ワ' }, { 'ワ' } },
    { { 'ン' }, { 'ン' } },
    { { '゛' }, { '゛' } },
    { { '゜' }, { '゜' } } };

  /**
   * コンストラクタ
   * 
   */
  private KanaMapTable() {
  }

}
