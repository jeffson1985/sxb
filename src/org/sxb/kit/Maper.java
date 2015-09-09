package org.sxb.kit;

import java.util.HashMap;
import java.util.Map;

/**
 * 定时任务处理用 Map类 QuartzJob
 * Created by son on 14-12-29.
 */
public class Maper {
  public static <K, V> Map<K, V> of() {
    return new HashMap<K, V>();
  }

  public static <K, V> Map<K, V> of(final K k1, final V v1) {
    return new HashMap<K, V>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
      put(k1, v1);
    }};
  }

  public static <K, V> Map<K, V> of(final K k1, final V v1, final K k2, final V v2) {
    return new HashMap<K, V>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
      put(k1, v1);
      put(k2, v2);
    }};
  }

  public static <K, V> Map<K, V> of(final K k1, final V v1, final K k2, final V v2, final K k3, final V v3) {
    return new HashMap<K, V>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
      put(k1, v1);
      put(k2, v2);
      put(k3, v3);
    }};
  }

  public static <K, V> Map<K, V> of(final K k1, final V v1, final K k2, final V v2, final K k3, final V v3, final K k4, final V v4) {
    return new HashMap<K, V>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
      put(k1, v1);
      put(k2, v2);
      put(k3, v3);
      put(k4, v4);
    }};
  }

  public static <K, V> Map<K, V> of(final K k1, final V v1, final K k2, final V v2, final K k3, final V v3, final K k4, final V v4, final K k5, final V v5) {
    return new HashMap<K, V>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
      put(k1, v1);
      put(k2, v2);
      put(k3, v3);
      put(k4, v4);
      put(k5, v5);
    }};
  }

  public static <K, V> Map<K, V> copyOf(Map<K, V> maps) {
    Map<K, V> map = new HashMap<K, V>();
    map.putAll(maps);
    return map;
  }
}
