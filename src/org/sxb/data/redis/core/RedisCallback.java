/*
 * Copyright 2011-2013 the original author or authors.
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
package org.sxb.data.redis.core;

import org.sxb.data.exception.DataAccessException;
import org.sxb.data.redis.connection.RedisConnection;

/**
 * Callback interface for Redis 'low level' code. To be used with {@link RedisTemplate} execution methods, often as
 * anonymous classes within a method implementation. Usually, used for chaining several operations together (
 * {@code get/set/trim etc...}.
 * 
 * @author Costin Leau
 */
public interface RedisCallback<T> {

	/**
	 * Gets called by {@link RedisTemplate} with an active Redis connection. Does not need to care about activating or
	 * closing the connection or handling exceptions.
	 * 
	 * @param connection active Redis connection
	 * @return a result object or {@code null} if none
	 * @throws DataAccessException
	 */
	T doInRedis(RedisConnection connection) throws DataAccessException;
}