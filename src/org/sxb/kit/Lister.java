package org.sxb.kit;

import org.sxb.kit.serialize.Serializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 定时任务处理用 
 * Created by son on 14-12-29.
 */
@SuppressWarnings("unchecked")
public class Lister {
 
public static <T> List<T> of(Object... objects) {
    if (objects == null || objects.length == 0) return new ArrayList<T>();
    return (List<T>) Arrays.asList(objects);
  }

  public static <T> List<T> copyOf(List<T> objects) {
    if (objects == null || objects.size() == 0) return new ArrayList<T>();
    return (List<T>) Serializer.unserialize(Serializer.serialize(objects));
  }
}
