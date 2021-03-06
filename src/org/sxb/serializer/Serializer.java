/*
 * Copyright 2002-2012 the original author or authors.
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

package org.sxb.serializer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A strategy interface for streaming an object to an OutputStream.<br>
 * 对象转换到输出流的策略接口
 *
 * @author Jeffson
 * @author Mark Fisher
 * @since 2.0
 */
public interface Serializer<T> {

	/**
	 * Write an object of type T to the given OutputStream.<br>
	 * 将T类型的对象写到指定输出流<br>
	 * <p>Note: Implementations should not close the given OutputStream
	 * (or any decorators of that OutputStream) but rather leave this up
	 * to the caller.
	 * @param object the object to serialize
	 * @param outputStream the output stream
	 * @throws IOException in case of errors writing to the stream
	 */
	void serialize(T object, OutputStream outputStream) throws IOException;

}
