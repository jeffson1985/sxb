package org.sxb.kit.scan;

/**
 * Created by son on 14-12-19.
 */
public class ClassScaner<T> extends Scaner<T> {

  private Class<?> target;

  public ClassScaner(Class<?> target) {
    this.target = target;
  }

  /**
   * 要扫描的类父级
   *
   * @param target class
   * @return scaner
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
public static ClassScaner<?> of(Class<?> target) {
    return new ClassScaner(target);
  }

  /**
   * 检测目标类
   *
   * @param clazz
   * @return
   */
  public boolean checkTarget(Class<?> clazz) {
    return target.isAssignableFrom(clazz) && target != clazz;
  }
}