package org.sxb.kit.scan;

import java.lang.annotation.Annotation;

/**
 * Created by jeffson on 14-12-19.
 */
public class AnnotationScaner<T> extends Scaner<T> {

  private Class<? extends Annotation> target;

  public AnnotationScaner(Class<? extends Annotation> target) {
    this.target = target;
  }

  /**
   * 要扫描的类父级
   *
   * @param target class
   * @return scaner
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
public static AnnotationScaner<?> of(Class<? extends Annotation> target) {
    return new AnnotationScaner(target);
  }

  /**
   * 检测目标类
   *
   * @param clazz
   * @return
   */
  public boolean checkTarget(Class<?> clazz) {
    return clazz.getAnnotation(target) != null;
  }
}