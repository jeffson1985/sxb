/*
 * Copyright 2002-2015 the original author or authors.
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

package org.sxb.convert.converter;

import org.sxb.convert.sp.ConditionalConverter;

/**
 * A converter converts a source object of type {@code S} to a target of type {@code T}.<br>
 * 转换源对象类型{@code S}到目标对象类型{@code T}的转换接口
 *
 * <p>Implementations of this interface are thread-safe and can be shared.<br>
 * <p>实现本接口的类将是线程安全并可以共享的<br>
 *
 * <p>Implementations may additionally implement {@link ConditionalConverter}.<br>
 * <p>实现类可以另外实现其他接口{@link ConditionalConverter}
 *
 * @author Keith Donald
 * @author  Jeffson
 * @since 2.0
 * @param <S> the source type  源类型
 * @param <T> the target type  目标类型
 */
public interface Converter<S, T> {

	/**
	 * Convert the source object of type {@code S} to target type {@code T}.
	 * @param source the source object to convert, which must be an instance of {@code S} (never {@code null})
	 * @return the converted object, which must be an instance of {@code T} (potentially {@code null})
	 * @throws IllegalArgumentException if the source cannot be converted to the desired target type
	 */
	T convert(S source);

}
