/*
 * Copyright 2011-2015 the original author or authors.
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
package org.sxb.data.redis.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sxb.data.exception.DataAccessException;
import org.sxb.data.redis.cache.transaction.AbstractTransactionSupportingCacheManager;
import org.sxb.data.redis.cache.transaction.TransactionAwareCacheDecorator;
import org.sxb.data.redis.connection.RedisConnection;
import org.sxb.data.redis.core.RedisCallback;
import org.sxb.data.redis.core.RedisOperations;
import org.sxb.kit.Assert;
import org.sxb.kit.CollectionKits;

/**
 * {@link CacheManager} implementation for Redis. By default saves the keys directly, without appending a prefix (which
 * acts as a namespace). To avoid clashes, it is recommended to change this (by setting 'usePrefix' to 'true'). <br>
 * By default {@link RedisCache}s will be lazily initialized for each {@link #getCache(String)} request unless a set of
 * predefined cache names is provided. <br>
 * <br>
 * Setting {@link #setTransactionAware(boolean)} to {@code true} will force Caches to be decorated as
 * {@link TransactionAwareCacheDecorator} so values will only be written to the cache after successful commit of
 * surrounding transaction.
 * 
 * @author Costin Leau
 * @author Christoph Strobl
 * @author Thomas Darimont
 */
public class RedisCacheManager extends AbstractTransactionSupportingCacheManager {

	private final Log logger = LogFactory.getLog(RedisCacheManager.class);

	@SuppressWarnings("rawtypes")//
	private final RedisOperations redisOperations;

	private boolean usePrefix = false;
	private RedisCachePrefix cachePrefix = new DefaultRedisCachePrefix();
	private boolean loadRemoteCachesOnStartup = false;
	private boolean dynamic = true;

	// 0 - never expire
	private long defaultExpiration = 0;
	private Map<String, Long> expires = null;

	private Set<String> configuredCacheNames;

	/**
	 * Construct a {@link RedisCacheManager}.
	 * 
	 * @param redisOperations
	 */
	@SuppressWarnings("rawtypes")
	public RedisCacheManager(RedisOperations redisOperations) {
		this(redisOperations, Collections.<String> emptyList());
	}

	/**
	 * Construct a static {@link RedisCacheManager}, managing caches for the specified cache names only.
	 * 
	 * @param redisOperations
	 * @param cacheNames
	 * @since 1.2
	 */
	@SuppressWarnings("rawtypes")
	public RedisCacheManager(RedisOperations redisOperations, Collection<String> cacheNames) {
		this.redisOperations = redisOperations;
		setCacheNames(cacheNames);
	}

	@Override
	public Cache getCache(String name) {
		Cache cache = super.getCache(name);
		if (cache == null && this.dynamic) {
			return createAndAddCache(name);
		}

		return cache;
	}

	/**
	 * Specify the set of cache names for this CacheManager's 'static' mode. <br>
	 * The number of caches and their names will be fixed after a call to this method, with no creation of further cache
	 * regions at runtime. <br>
	 * Calling this with a {@code null} or empty collection argument resets the mode to 'dynamic', allowing for further
	 * creation of caches again.
	 */
	public void setCacheNames(Collection<String> cacheNames) {

		Set<String> newCacheNames = CollectionKits.isEmpty(cacheNames) ? Collections.<String> emptySet()
				: new HashSet<String>(cacheNames);

		this.configuredCacheNames = newCacheNames;
		this.dynamic = newCacheNames.isEmpty();
	}

	public void setUsePrefix(boolean usePrefix) {
		this.usePrefix = usePrefix;
	}

	/**
	 * Sets the cachePrefix. Defaults to 'DefaultRedisCachePrefix').
	 * 
	 * @param cachePrefix the cachePrefix to set
	 */
	public void setCachePrefix(RedisCachePrefix cachePrefix) {
		this.cachePrefix = cachePrefix;
	}

	/**
	 * Sets the default expire time (in seconds).
	 * 
	 * @param defaultExpireTime time in seconds.
	 */
	public void setDefaultExpiration(long defaultExpireTime) {
		this.defaultExpiration = defaultExpireTime;
	}

	/**
	 * Sets the expire time (in seconds) for cache regions (by key).
	 * 
	 * @param expires time in seconds
	 */
	public void setExpires(Map<String, Long> expires) {
		this.expires = (expires != null ? new ConcurrentHashMap<String, Long>(expires) : null);
	}

	/**
	 * If set to {@code true} {@link RedisCacheManager} will try to retrieve cache names from redis server using
	 * {@literal KEYS} command and initialize {@link RedisCache} for each of them.
	 * 
	 * @param loadRemoteCachesOnStartup
	 * @since 1.2
	 */
	public void setLoadRemoteCachesOnStartup(boolean loadRemoteCachesOnStartup) {
		this.loadRemoteCachesOnStartup = loadRemoteCachesOnStartup;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.cache.support.AbstractCacheManager#loadCaches()
	 */
	@Override
	protected Collection<? extends Cache> loadCaches() {

		Assert.notNull(this.redisOperations, "A redis template is required in order to interact with data store");
		return addConfiguredCachesIfNecessary(loadRemoteCachesOnStartup ? loadAndInitRemoteCaches() : Collections
				.<Cache> emptyList());
	}

	/**
	 * Returns a new {@link Collection} of {@link Cache} from the given caches collection and adds the configured
	 * {@link Cache}s of they are not already present.
	 * 
	 * @param caches must not be {@literal null}
	 * @return
	 */
	protected Collection<? extends Cache> addConfiguredCachesIfNecessary(Collection<? extends Cache> caches) {

		Assert.notNull(caches, "Caches must not be null!");

		Collection<Cache> result = new ArrayList<Cache>(caches);

		for (String cacheName : getCacheNames()) {

			boolean configuredCacheAlreadyPresent = false;

			for (Cache cache : caches) {

				if (cache.getName().equals(cacheName)) {
					configuredCacheAlreadyPresent = true;
					break;
				}
			}

			if (!configuredCacheAlreadyPresent) {
				result.add(getCache(cacheName));
			}
		}

		return result;
	}

	protected Cache createAndAddCache(String cacheName) {
		addCache(createCache(cacheName));
		return super.getCache(cacheName);
	}

	@SuppressWarnings("unchecked")
	protected RedisCache createCache(String cacheName) {
		long expiration = computeExpiration(cacheName);
		return new RedisCache(cacheName, (usePrefix ? cachePrefix.prefix(cacheName) : null), redisOperations, expiration);
	}

	protected long computeExpiration(String name) {
		Long expiration = null;
		if (expires != null) {
			expiration = expires.get(name);
		}
		return (expiration != null ? expiration.longValue() : defaultExpiration);
	}

	protected List<Cache> loadAndInitRemoteCaches() {

		List<Cache> caches = new ArrayList<Cache>();

		try {
			Set<String> cacheNames = loadRemoteCacheKeys();
			if (!CollectionKits.isEmpty(cacheNames)) {
				for (String cacheName : cacheNames) {
					if (null == super.getCache(cacheName)) {
						caches.add(createCache(cacheName));
					}
				}
			}
		} catch (Exception e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Failed to initialize cache with remote cache keys.", e);
			}
		}

		return caches;
	}

	@SuppressWarnings("unchecked")
	protected Set<String> loadRemoteCacheKeys() {
		return (Set<String>) redisOperations.execute(new RedisCallback<Set<String>>() {

			@Override
			public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {

				// we are using the ~keys postfix as defined in RedisCache#setName
				Set<byte[]> keys = connection.keys(redisOperations.getKeySerializer().serialize("*~keys"));
				Set<String> cacheKeys = new LinkedHashSet<String>();

				if (!CollectionKits.isEmpty(keys)) {
					for (byte[] key : keys) {
						cacheKeys.add(redisOperations.getKeySerializer().deserialize(key).toString().replace("~keys", ""));
					}
				}

				return cacheKeys;
			}
		});
	}

	@SuppressWarnings("rawtypes")
	protected RedisOperations getRedisOperations() {
		return redisOperations;
	}

	protected RedisCachePrefix getCachePrefix() {
		return cachePrefix;
	}

	protected boolean isUsePrefix() {
		return usePrefix;
	}

	/**
	 * The number of caches and their names will be fixed after a call to this method, with no creation of further cache
	 * regions at runtime.
	 * 
	 * @see org.springframework.cache.support.AbstractCacheManager#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {

		if (!CollectionKits.isEmpty(configuredCacheNames)) {

			for (String cacheName : configuredCacheNames) {
				createAndAddCache(cacheName);
			}

			configuredCacheNames.clear();
		}

		super.afterPropertiesSet();
	}

	/* (non-Javadoc)
	* @see
	org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager#decorateCache(org.springframework.cache.Cache)
	*/
	@Override
	protected Cache decorateCache(Cache cache) {

		if (isCacheAlreadyDecorated(cache)) {
			return cache;
		}

		return super.decorateCache(cache);
	}

	protected boolean isCacheAlreadyDecorated(Cache cache) {
		return isTransactionAware() && cache instanceof TransactionAwareCacheDecorator;
	}
}
