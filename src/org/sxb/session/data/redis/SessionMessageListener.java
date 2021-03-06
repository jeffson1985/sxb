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
package org.sxb.session.data.redis;

import org.sxb.kit.Assert;
import org.sxb.log.Logger;
import org.sxb.session.events.ApplicationEvent;
import org.sxb.session.events.SessionDeletedEvent;
import org.sxb.session.events.SessionExpiredEvent;

/**
 * Listen for Redis {@link Message} notifications. If it is a "del"
 * translate into a {@link SessionDeletedEvent}. If it is an "expired"
 * translate into a {@link SessionExpiredEvent}.
 *
 * @author Jeffson
 * @author Mark Anderson
 * @since 1.0
 */
public class SessionMessageListener implements MessageListener {
	private static final Logger logger = Logger.getLogger(SessionMessageListener.class);

	private final ApplicationEventPublisher eventPublisher;

	/**
	 * Creates a new instance
	 *
	 * @param eventPublisher the {@link ApplicationEventPublisher} to use. Cannot be null.
	 */
	public SessionMessageListener(ApplicationEventPublisher eventPublisher) {
		Assert.notNull(eventPublisher, "eventPublisher cannot be null");
		this.eventPublisher = eventPublisher;
	}

	public void onMessage(Message message, byte[] pattern) {
		byte[] messageChannel = message.getChannel();
		byte[] messageBody = message.getBody();
		if(messageChannel == null || messageBody == null) {
			return;
		}
		String channel = new String(messageChannel);
		if(!(channel.endsWith(":del") || channel.endsWith(":expired"))) {
			return;
		}
		String body = new String(messageBody);
		if(!body.startsWith("spring:session:sessions:")) {
			return;
		}

		int beginIndex = body.lastIndexOf(":") + 1;
		int endIndex = body.length();
		String sessionId = body.substring(beginIndex, endIndex);

		if(logger.isDebugEnabled()) {
			logger.debug("Publishing SessionDestroyedEvent for session " + sessionId);
		}

		if(channel.endsWith(":del")) {
			publishEvent(new SessionDeletedEvent(this, sessionId));
		} else {
			publishEvent(new SessionExpiredEvent(this, sessionId));
		}
	}

	private void publishEvent(ApplicationEvent event) {
		try {
			this.eventPublisher.publishEvent(event);
		}
		catch (Throwable ex) {
			logger.error("Error publishing " + event + ".", ex);
		}
	}

}
