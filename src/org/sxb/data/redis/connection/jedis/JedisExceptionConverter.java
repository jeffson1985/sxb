/*
 * Copyright 2013-2014 the original author or authors.
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
package org.sxb.data.redis.connection.jedis;

import java.io.IOException;
import java.net.UnknownHostException;

import org.sxb.convert.converter.Converter;
import org.sxb.data.exception.DataAccessException;
import org.sxb.data.exception.InvalidDataAccessApiUsageException;
import org.sxb.data.redis.RedisConnectionFailureException;

import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;

/**
 * Converts Exceptions thrown from Jedis to {@link DataAccessException}s
 * 
 * @author Jennifer Hickey
 * @author Thomas Darimont
 */
public class JedisExceptionConverter implements Converter<Exception, DataAccessException> {

	public DataAccessException convert(Exception ex) {

		if (ex instanceof DataAccessException) {
			return (DataAccessException) ex;
		}
		if (ex instanceof JedisDataException) {
			return new InvalidDataAccessApiUsageException(ex.getMessage(), ex);
		}
		if (ex instanceof JedisConnectionException) {
			return new RedisConnectionFailureException(ex.getMessage(), ex);
		}
		if (ex instanceof JedisException) {
			return new InvalidDataAccessApiUsageException(ex.getMessage(), ex);
		}
		if (ex instanceof UnknownHostException) {
			return new RedisConnectionFailureException("Unknown host " + ex.getMessage(), ex);
		}
		if (ex instanceof IOException) {
			return new RedisConnectionFailureException("Could not connect to Redis server", ex);
		}

		return null;
	}
}
