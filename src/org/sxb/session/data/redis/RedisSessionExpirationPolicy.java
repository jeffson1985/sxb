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

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.sxb.data.redis.core.BoundSetOperations;
import org.sxb.data.redis.core.RedisOperations;
import org.sxb.log.Logger;
import org.sxb.session.ExpiringSession;
import org.sxb.session.data.redis.RedisOperationsSessionRepository.RedisSession;

/**
 * A strategy for expiring {@link RedisSession} instances. This performs two
 * operations:
 *
 * Redis has no guarantees of when an expired session event will be fired. In
 * order to ensure expired session events are processed in a timely fashion the
 * expiration (rounded to the nearest minute) is mapped to all the sessions that
 * expire at that time. Whenever {@link #cleanExpiredSessions()} is invoked, the
 * sessions for the previous minute are then accessed to ensure they are deleted if expired.
 *
 * In some instances the {@link #cleanExpiredSessions()} method may not be not
 * invoked for a specific time. For example, this may happen when a server is
 * restarted. To account for this, the expiration on the Redis session is also set.
 *
 * @author Rob Winch
 * @author Jeffson
 * @since 2.0
 */
final class RedisSessionExpirationPolicy {

	private static final Logger logger = Logger.getLogger(RedisOperationsSessionRepository.class);


	private final RedisOperations<Object,Object> redis;

	private final RedisOperationsSessionRepository redisSession;

	public RedisSessionExpirationPolicy(
			RedisOperations<Object,Object> sessionRedisOperations, RedisOperationsSessionRepository redisSession) {
		super();
		this.redis = sessionRedisOperations;
		this.redisSession = redisSession;
	}

	public void onDelete(ExpiringSession session) {
		long toExpire = roundUpToNextMinute(expiresInMillis(session));
		String expireKey = getExpirationKey(toExpire);
		redis.boundSetOps(expireKey).remove(session.getId());
	}

	public void onExpirationUpdated(Long originalExpirationTimeInMilli, ExpiringSession session) {
		String keyToExpire = "expires:" + session.getId();
		if(originalExpirationTimeInMilli != null) {
			long originalRoundedUp = roundUpToNextMinute(originalExpirationTimeInMilli);
			String expireKey = getExpirationKey(originalRoundedUp);
			redis.boundSetOps(expireKey).remove(keyToExpire);
		}

		long toExpire = roundUpToNextMinute(expiresInMillis(session));

		String expireKey = getExpirationKey(toExpire);
		BoundSetOperations<Object, Object> expireOperations = redis.boundSetOps(expireKey);
		expireOperations.add(keyToExpire);

		long sessionExpireInSeconds = session.getMaxInactiveIntervalInSeconds();
		long fiveMinutesAfterExpires = sessionExpireInSeconds + TimeUnit.MINUTES.toSeconds(5);
		String sessionKey = getSessionKey(keyToExpire);

		expireOperations.expire(fiveMinutesAfterExpires, TimeUnit.SECONDS);
		redis.boundValueOps(sessionKey).append("");
		redis.boundValueOps(sessionKey).expire(sessionExpireInSeconds, TimeUnit.SECONDS);
		redis.boundHashOps(getSessionKey(session.getId())).expire(fiveMinutesAfterExpires, TimeUnit.SECONDS);
	}

	String getExpirationKey(long expires) {
		return this.redisSession.getExpirationsKey(expires);
	}

	String getSessionKey(String sessionId) {
		return this.redisSession.getSessionKey(sessionId);
	}

	public void cleanExpiredSessions() {
		long now = System.currentTimeMillis();
		long prevMin = roundDownMinute(now);

		if(logger.isDebugEnabled()) {
			logger.debug("Cleaning up sessions expiring at "+ new Date(prevMin));
		}

		String expirationKey = getExpirationKey(prevMin);
		Set<Object> sessionsToExpire = redis.boundSetOps(expirationKey).members();
		redis.delete(expirationKey);
		for(Object session : sessionsToExpire) {
			String sessionKey = getSessionKey((String) session);
			touch(sessionKey);
		}
	}

	/**
	 * By trying to access the session we only trigger a deletion if it the TTL is expired. This is done to handle
	 * https://github.com/spring-projects/spring-session/issues/93
	 *
	 * @param key
	 */
	private void touch(String key) {
		redis.hasKey(key);
	}

	static long expiresInMillis(ExpiringSession session) {
		int maxInactiveInSeconds = session.getMaxInactiveIntervalInSeconds();
		long lastAccessedTimeInMillis = session.getLastAccessedTime();
		return lastAccessedTimeInMillis + TimeUnit.SECONDS.toMillis(maxInactiveInSeconds);
	}

	static long roundUpToNextMinute(long timeInMs) {

		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(timeInMs);
		date.add(Calendar.MINUTE, 1);
		date.clear(Calendar.SECOND);
		date.clear(Calendar.MILLISECOND);
		return date.getTimeInMillis();
	}

	static long roundDownMinute(long timeInMs) {
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(timeInMs);
		date.clear(Calendar.SECOND);
		date.clear(Calendar.MILLISECOND);
		return date.getTimeInMillis();
	}
}
