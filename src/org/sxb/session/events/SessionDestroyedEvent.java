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
package org.sxb.session.events;

import org.sxb.session.Session;

/**
 * Base class for events fired when a {@link Session} is destroyed explicitly.
 *
 * @author Jeffson
 * @since 1.0
 *
 */
public class SessionDestroyedEvent extends AbstractSessionEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6891367035807644048L;

	public SessionDestroyedEvent(Object source, String sessionId) {
		super(source, sessionId);
	}

	/**
	 * @param source
	 * @param session
	 */
	public SessionDestroyedEvent(Object source, Session session) {
		super(source, session);
	}
}