/**
 * 参考aipo
 */
package org.sxb.kit;
import java.io.Serializable;
import java.util.Comparator;

/**
 * 半角文字の比較用クラスです。 <br />
 * 
 */
public abstract class AbstractComparator<T> implements Comparator<T>,
    Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * コンストラクタ
   * 
   */
  public AbstractComparator() {
  }

  /**
   * 指定されたオブジェクトを比較します。
   * 
   */
  @Override
  public int compare(Object obj1, Object obj2) {
    return compare(getCharArray(obj1), getCharArray(obj2));
  }

  /**
   * 指定されたオブジェクトを比較します。
   * 
   * @param chars1
   * @param chars2
   * @return
   */
  public final int compare(char[] chars1, char[] chars2) {
    int max;
    int ret = 0;
    int char1Len = chars1.length;
    int char2Len = chars2.length;

    if (char1Len < char2Len) {
      max = char1Len;
    } else {
      max = char2Len;
    }
    for (int i = 0; i < max; i++) {
      if ((ret = chars1[i] - chars2[i]) != 0) {
        return ret;
      }
    }
    return char1Len - char2Len;
  }

  /**
   * char配列を取得します。
   * 
   * @param obj
   * @return
   */
  protected abstract char[] getCharArray(Object obj);

}
