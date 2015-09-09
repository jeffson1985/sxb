/*
Copyright (c) 2011-2015, Jeff  Son   (jeffson.app@gmail.com).

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.sxb.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * 集合工具类，本着替换掉google  guava 的想法
 * 奈何 Google大爷太给力，很多地方都用到就暂时两者并用吧
 *  @author Jeffson
 */
@SuppressWarnings("unchecked")
public final class Collections {
    private Collections() {
        // not instantiable
    }

    /**
     * Create array from values.
     * @param values values to create array from.
     * @return array filled with values from arguments.
     */
    
	
	public static <T> T[] arr(T... values) {
        return values;
    }

    /**
     * Create array from values.
     * @param values values to create array from.
     * @return array filled with values from arguments.
     */
    public static <T> T[] array(T... values) {
        return values;
    }

    /**
     * Creates a set from values.
     *
     * @param values values for a set.
     * @return set filled with values.
     */
    public static <T> Set<T> set(T... values) {
        return new HashSet<T>(Arrays.asList(values));
    }

    /**
     * Create a map from keys and values.
     *
     * @param keysAndValues list of key value pairs. The number of items must be even.
     * The argument at index 0 is a key of the value at index 1, argument at index 2 is key of value at index 3, and so on.
     * @return Map filled with keys and values.
     */
    public static <K, V> Map<K, V> map(Object... keysAndValues) {
        if (keysAndValues.length % 2 != 0) { throw new IllegalArgumentException("number of arguments must be even"); }

        Map<K, V> map = new HashMap<K, V>(Math.max(keysAndValues.length, 16));
        for (int i = 0; i < keysAndValues.length;) {
            map.put((K) keysAndValues[i++], (V) keysAndValues[i++]);
        }
        return map;
    }

    /**
     * Create a list from values.
     *
     * @param values values to create a list from.
     * @return list with values.
     */
    public static <T> List<T> li(T... values) {
        return list(values);
    }

    /**
     * Create a list from values.
     *
     * @param values values to create a list from.
     * @return list with values.
     */
    public static <T> List<T> list(T... values) {
        return new ArrayList<T>(Arrays.asList(values));
    }
}
