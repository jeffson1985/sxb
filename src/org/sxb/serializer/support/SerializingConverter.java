/*
 * Copyright 2002-2014 the original author or authors.
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

package org.sxb.serializer.support;

import java.io.ByteArrayOutputStream;

import org.sxb.convert.converter.Converter;
import org.sxb.kit.Assert;
import org.sxb.serializer.DefaultSerializer;
import org.sxb.serializer.Serializer;

/**
 * A {@link Converter} that delegates to a {@link org.sxb.serializer.Serializer}
 * to convert an object to a byte array.<br>
 * 一个行化接口的代理，负责串转换对象到字节数组
 *
 * @author Mark Fisher
 * @author Jeffson
 *
 * @since 2.0
 */
public class SerializingConverter implements Converter<Object, byte[]> {

	private final Serializer<Object> serializer;


	/**
	 * Create a default SerializingConverter that uses standard Java serialization.
	 */
	public SerializingConverter() {
		this.serializer = new DefaultSerializer();
	}

	/**
	 * Create a SerializingConverter that delegates to the provided {@link Serializer}
	 */
	public SerializingConverter(Serializer<Object> serializer) {
		Assert.notNull(serializer, "Serializer must not be null");
		this.serializer = serializer;
	}


	/**
	 * Serializes the source object and returns the byte array result.
	 */
	@Override
	public byte[] convert(Object source) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(256);
		try  {
			this.serializer.serialize(source, byteStream);
			return byteStream.toByteArray();
		}
		catch (Throwable ex) {
			throw new SerializationFailedException("Failed to serialize object using " +
					this.serializer.getClass().getSimpleName(), ex);
		}
	}

}
