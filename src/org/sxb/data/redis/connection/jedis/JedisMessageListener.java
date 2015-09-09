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
package org.sxb.data.redis.connection.jedis;

import org.sxb.data.redis.connection.DefaultMessage;
import org.sxb.data.redis.connection.MessageListener;
import org.sxb.kit.Assert;

import redis.clients.jedis.BinaryJedisPubSub;

/**
 * MessageListener adapter on top of Jedis.
 * 
 * @author Costin Leau
 */
class JedisMessageListener extends BinaryJedisPubSub {

	private final MessageListener listener;

	JedisMessageListener(MessageListener listener) {
		Assert.notNull(listener, "message listener is required");
		this.listener = listener;
	}

	public void onMessage(byte[] channel, byte[] message) {
		listener.onMessage(new DefaultMessage(channel, message), null);
	}

	public void onPMessage(byte[] pattern, byte[] channel, byte[] message) {
		listener.onMessage(new DefaultMessage(channel, message), pattern);
	}

	public void onPSubscribe(byte[] pattern, int subscribedChannels) {
		// no-op
	}

	public void onPUnsubscribe(byte[] pattern, int subscribedChannels) {
		// no-op
	}

	public void onSubscribe(byte[] channel, int subscribedChannels) {
		// no-op
	}

	public void onUnsubscribe(byte[] channel, int subscribedChannels) {
		// no-op
	}
}
