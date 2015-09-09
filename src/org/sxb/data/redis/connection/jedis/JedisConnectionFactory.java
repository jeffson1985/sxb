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
package org.sxb.data.redis.connection.jedis;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sxb.bean.DisposableBean;
import org.sxb.bean.InitializingBean;
import org.sxb.data.exception.DataAccessException;
import org.sxb.data.exception.InvalidDataAccessResourceUsageException;
import org.sxb.data.redis.ExceptionTranslationStrategy;
import org.sxb.data.redis.PassThroughExceptionTranslationStrategy;
import org.sxb.data.redis.RedisConnectionFailureException;
import org.sxb.data.redis.connection.RedisConnection;
import org.sxb.data.redis.connection.RedisConnectionFactory;
import org.sxb.data.redis.connection.RedisNode;
import org.sxb.data.redis.connection.RedisSentinelConfiguration;
import org.sxb.data.redis.connection.RedisSentinelConnection;
import org.sxb.kit.Assert;
import org.sxb.kit.CollectionKits;
import org.sxb.kit.ReflectionKits;
import org.sxb.kit.StringKits;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Protocol;
import redis.clients.util.Pool;

/**
 * Connection factory creating <a href="http://github.com/xetorthio/jedis">Jedis</a> based connections.
 * 
 * @author Costin Leau
 * @author Thomas Darimont
 * @author Christoph Strobl
 */
public class JedisConnectionFactory implements InitializingBean, DisposableBean, RedisConnectionFactory {

	private final static Log log = LogFactory.getLog(JedisConnectionFactory.class);
	private static final ExceptionTranslationStrategy EXCEPTION_TRANSLATION = new PassThroughExceptionTranslationStrategy(
			JedisConverters.exceptionConverter());

	private static final Method SET_TIMEOUT_METHOD;
	private static final Method GET_TIMEOUT_METHOD;

	static {

		// We need to configure Jedis socket timeout via reflection since the method-name was changed between releases.
		Method setTimeoutMethodCandidate = ReflectionKits.findMethod(JedisShardInfo.class, "setTimeout", int.class);
		if (setTimeoutMethodCandidate == null) {
			// Jedis V 2.7.x changed the setTimeout method to setSoTimeout
			setTimeoutMethodCandidate = ReflectionKits.findMethod(JedisShardInfo.class, "setSoTimeout", int.class);
		}
		SET_TIMEOUT_METHOD = setTimeoutMethodCandidate;

		Method getTimeoutMethodCandidate = ReflectionKits.findMethod(JedisShardInfo.class, "getTimeout");
		if (getTimeoutMethodCandidate == null) {
			getTimeoutMethodCandidate = ReflectionKits.findMethod(JedisShardInfo.class, "getSoTimeout");
		}

		GET_TIMEOUT_METHOD = getTimeoutMethodCandidate;
	}

	private JedisShardInfo shardInfo;
	private String hostName = "localhost";
	private int port = Protocol.DEFAULT_PORT;
	private int timeout = Protocol.DEFAULT_TIMEOUT;
	private String password;
	private boolean usePool = true;
	private Pool<Jedis> pool;
	private JedisPoolConfig poolConfig = new JedisPoolConfig();
	private int dbIndex = 0;
	private boolean convertPipelineAndTxResults = true;
	private RedisSentinelConfiguration sentinelConfig;

	/**
	 * Constructs a new <code>JedisConnectionFactory</code> instance with default settings (default connection pooling, no
	 * shard information).
	 */
	public JedisConnectionFactory() {}

	/**
	 * Constructs a new <code>JedisConnectionFactory</code> instance. Will override the other connection parameters passed
	 * to the factory.
	 * 
	 * @param shardInfo shard information
	 */
	public JedisConnectionFactory(JedisShardInfo shardInfo) {
		this.shardInfo = shardInfo;
	}

	/**
	 * Constructs a new <code>JedisConnectionFactory</code> instance using the given pool configuration.
	 * 
	 * @param poolConfig pool configuration
	 */
	public JedisConnectionFactory(JedisPoolConfig poolConfig) {
		this(null, poolConfig);
	}

	/**
	 * Constructs a new {@link JedisConnectionFactory} instance using the given {@link JedisPoolConfig} applied to
	 * {@link JedisSentinelPool}.
	 * 
	 * @param sentinelConfig
	 * @since 1.4
	 */
	public JedisConnectionFactory(RedisSentinelConfiguration sentinelConfig) {
		this(sentinelConfig, null);
	}

	/**
	 * Constructs a new {@link JedisConnectionFactory} instance using the given {@link JedisPoolConfig} applied to
	 * {@link JedisSentinelPool}.
	 * 
	 * @param sentinelConfig
	 * @param poolConfig pool configuration. Defaulted to new instance if {@literal null}.
	 * @since 1.4
	 */
	public JedisConnectionFactory(RedisSentinelConfiguration sentinelConfig, JedisPoolConfig poolConfig) {
		this.sentinelConfig = sentinelConfig;
		this.poolConfig = poolConfig != null ? poolConfig : new JedisPoolConfig();
	}

	/**
	 * Returns a Jedis instance to be used as a Redis connection. The instance can be newly created or retrieved from a
	 * pool.
	 * 
	 * @return Jedis instance ready for wrapping into a {@link RedisConnection}.
	 */
	protected Jedis fetchJedisConnector() {
		try {
			if (usePool && pool != null) {
				return pool.getResource();
			}
			Jedis jedis = new Jedis(getShardInfo());
			//Jedis jedis = new Jedis("localhost");
			// force initialization (see Jedis issue #82)
			jedis.connect();
			return jedis;
		} catch (Exception ex) {
			throw new RedisConnectionFailureException("Cannot get Jedis connection", ex);
		}
	}

	/**
	 * Post process a newly retrieved connection. Useful for decorating or executing initialization commands on a new
	 * connection. This implementation simply returns the connection.
	 * 
	 * @param connection
	 * @return processed connection
	 */
	protected JedisConnection postProcessConnection(JedisConnection connection) {
		return connection;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {
		if (shardInfo == null) {
			shardInfo = new JedisShardInfo(hostName, port);

			if (StringKits.hasLength(password)) {
				shardInfo.setPassword(password);
			}

			if (timeout > 0) {
				setTimeoutOn(shardInfo, timeout);
			}
		}

		if (usePool) {
			this.pool = createPool();
		}
	}

	private Pool<Jedis> createPool() {

		if (isRedisSentinelAware()) {
			return createRedisSentinelPool(this.sentinelConfig);
		}
		return createRedisPool();
	}

	/**
	 * Creates {@link JedisSentinelPool}.
	 * 
	 * @param config
	 * @return
	 * @since 1.4
	 */
	protected Pool<Jedis> createRedisSentinelPool(RedisSentinelConfiguration config) {
		return new JedisSentinelPool(config.getMaster().getName(), convertToJedisSentinelSet(config.getSentinels()),
				getPoolConfig() != null ? getPoolConfig() : new JedisPoolConfig(), getTimeoutFrom(getShardInfo()),
				getShardInfo().getPassword());
	}

	/**
	 * Creates {@link JedisPool}.
	 * 
	 * @return
	 * @since 1.4
	 */
	protected Pool<Jedis> createRedisPool() {
		return new JedisPool(getPoolConfig(), getShardInfo().getHost(), getShardInfo().getPort(),
				getTimeoutFrom(getShardInfo()), getShardInfo().getPassword());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() {
		if (usePool && pool != null) {
			try {
				pool.destroy();
			} catch (Exception ex) {
				log.warn("Cannot properly close Jedis pool", ex);
			}
			pool = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisConnectionFactory#getConnection()
	 */
	public JedisConnection getConnection() {
		Jedis jedis = fetchJedisConnector();
		JedisConnection connection = (usePool ? new JedisConnection(jedis, pool, dbIndex) : new JedisConnection(jedis,
				null, dbIndex));
		connection.setConvertPipelineAndTxResults(convertPipelineAndTxResults);
		return postProcessConnection(connection);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.dao.support.PersistenceExceptionTranslator#translateExceptionIfPossible(java.lang.RuntimeException)
	 */
	public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
		return EXCEPTION_TRANSLATION.translate(ex);
	}

	/**
	 * Returns the Redis hostName.
	 * 
	 * @return Returns the hostName
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * Sets the Redis hostName.
	 * 
	 * @param hostName The hostName to set.
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * Returns the password used for authenticating with the Redis server.
	 * 
	 * @return password for authentication
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password used for authenticating with the Redis server.
	 * 
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Returns the port used to connect to the Redis instance.
	 * 
	 * @return Redis port.
	 */
	public int getPort() {
		return port;

	}

	/**
	 * Sets the port used to connect to the Redis instance.
	 * 
	 * @param port Redis port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Returns the shardInfo.
	 * 
	 * @return Returns the shardInfo
	 */
	public JedisShardInfo getShardInfo() {
		return shardInfo;
	}

	/**
	 * Sets the shard info for this factory.
	 * 
	 * @param shardInfo The shardInfo to set.
	 */
	public void setShardInfo(JedisShardInfo shardInfo) {
		this.shardInfo = shardInfo;
	}

	/**
	 * Returns the timeout.
	 * 
	 * @return Returns the timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout The timeout to set.
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Indicates the use of a connection pool.
	 * 
	 * @return Returns the use of connection pooling.
	 */
	public boolean getUsePool() {
		return usePool;
	}

	/**
	 * Turns on or off the use of connection pooling.
	 * 
	 * @param usePool The usePool to set.
	 */
	public void setUsePool(boolean usePool) {
		this.usePool = usePool;
	}

	/**
	 * Returns the poolConfig.
	 * 
	 * @return Returns the poolConfig
	 */
	public JedisPoolConfig getPoolConfig() {
		return poolConfig;
	}

	/**
	 * Sets the pool configuration for this factory.
	 * 
	 * @param poolConfig The poolConfig to set.
	 */
	public void setPoolConfig(JedisPoolConfig poolConfig) {
		this.poolConfig = poolConfig;
	}

	/**
	 * Returns the index of the database.
	 * 
	 * @return Returns the database index
	 */
	public int getDatabase() {
		return dbIndex;
	}

	/**
	 * Sets the index of the database used by this connection factory. Default is 0.
	 * 
	 * @param index database index
	 */
	public void setDatabase(int index) {
		Assert.isTrue(index >= 0, "invalid DB index (a positive index required)");
		this.dbIndex = index;
	}

	/**
	 * Specifies if pipelined results should be converted to the expected data type. If false, results of
	 * {@link JedisConnection#closePipeline()} and {@link JedisConnection#exec()} will be of the type returned by the
	 * Jedis driver
	 * 
	 * @return Whether or not to convert pipeline and tx results
	 */
	public boolean getConvertPipelineAndTxResults() {
		return convertPipelineAndTxResults;
	}

	/**
	 * Specifies if pipelined results should be converted to the expected data type. If false, results of
	 * {@link JedisConnection#closePipeline()} and {@link JedisConnection#exec()} will be of the type returned by the
	 * Jedis driver
	 * 
	 * @param convertPipelineAndTxResults Whether or not to convert pipeline and tx results
	 */
	public void setConvertPipelineAndTxResults(boolean convertPipelineAndTxResults) {
		this.convertPipelineAndTxResults = convertPipelineAndTxResults;
	}

	/**
	 * @return true when {@link RedisSentinelConfiguration} is present.
	 * @since 1.4
	 */
	public boolean isRedisSentinelAware() {
		return sentinelConfig != null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisConnectionFactory#getSentinelConnection()
	 */
	@Override
	public RedisSentinelConnection getSentinelConnection() {

		if (!isRedisSentinelAware()) {
			throw new InvalidDataAccessResourceUsageException("No Sentinels configured");
		}

		return new JedisSentinelConnection(getActiveSentinel());
	}

	private Jedis getActiveSentinel() {

		Assert.notNull(this.sentinelConfig);
		for (RedisNode node : this.sentinelConfig.getSentinels()) {
			Jedis jedis = new Jedis(node.getHost(), node.getPort());
			if (jedis.ping().equalsIgnoreCase("pong")) {
				return jedis;
			}
		}

		throw new InvalidDataAccessResourceUsageException("no sentinel found");
	}

	private Set<String> convertToJedisSentinelSet(Collection<RedisNode> nodes) {

		if (CollectionKits.isEmpty(nodes)) {
			return Collections.emptySet();
		}

		Set<String> convertedNodes = new LinkedHashSet<String>(nodes.size());
		for (RedisNode node : nodes) {
			if (node != null) {
				convertedNodes.add(node.asString());
			}
		}
		return convertedNodes;
	}

	private void setTimeoutOn(JedisShardInfo shardInfo, int timeout) {
		ReflectionKits.invokeMethod(SET_TIMEOUT_METHOD, shardInfo, timeout);
	}

	private int getTimeoutFrom(JedisShardInfo shardInfo) {
		return (Integer) ReflectionKits.invokeMethod(GET_TIMEOUT_METHOD, shardInfo);
	}
	
	public static void main(String args[]){
		JedisConnectionFactory  fa = new JedisConnectionFactory();
		fa.setHostName("localhost");
		fa.setPort(6379);
		fa.setDatabase(8);
		fa.setUsePool(true);
		//fa.createRedisPool();
		//Jedis jedis = fa.fetchJedisConnector();
		//JedisConnection conn = fa.getConnection();
		//Jedis jedis = conn.getNativeConnection();
		//jedis.set("jeff", "hope-engine");
	}

}
