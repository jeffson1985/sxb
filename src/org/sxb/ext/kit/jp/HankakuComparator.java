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

import java.io.Serializable;

import org.sxb.kit.AbstractComparator;

/**
 * 半角文字の比較用クラスです。 <br />
 * 
 */
public class HankakuComparator<T> extends AbstractComparator<T> implements
    Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * コンストラクタ
   * 
   */
  public HankakuComparator() {
  }

  /**
   * char配列を取得します。
   * 
   */
  @Override
  protected char[] getCharArray(Object obj) {
    if (obj instanceof char[][]) {
      return ((char[][]) obj)[KanaMapTable.INDEX_HANKAKU];
    } else {
      return (char[]) obj;
    }
  }

}
