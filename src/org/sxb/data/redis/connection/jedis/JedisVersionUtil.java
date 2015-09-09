/*
 * Copyright 2014 the original author or authors.
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

import java.util.Properties;

import org.sxb.data.redis.Version;
import org.sxb.data.redis.VersionParser;
import org.sxb.kit.PropKit;
import org.sxb.kit.StringKits;

import redis.clients.jedis.Jedis;

/**
 * @author Christoph Strobl
 * @since 1.3
 */
public class JedisVersionUtil {

	private static Version jedisVersion = parseVersion(resolveJedisVersion());

	/**
	 * @return current {@link redis.clients.jedis.Jedis} version.
	 */
	public static Version jedisVersion() {
		return jedisVersion;
	}

	/**
	 * Parse version string {@literal eg. 1.1.1} to {@link Version}.
	 * 
	 * @param version
	 * @return
	 */
	static Version parseVersion(String version) {
		return VersionParser.parseVersion(version);
	}

	/**
	 * @return true if used jedis version is at minimum {@literal 2.4}.
	 */
	public static boolean atLeastJedis24() {
		return atLeast("2.4");
	}

	private static String resolveJedisVersion() {

		String version = Jedis.class.getPackage().getImplementationVersion();

		if (!StringKits.hasText(version)) {
			//Properties props = PropertiesLoaderUtils.loadAllProperties("META-INF/maven/redis.clients/jedis/pom.properties");
			Properties props = PropKit.use("META-INF/maven/redis.clients/jedis/pom.properties").getProperties();
			if (props.containsKey("version")) {
				version = props.getProperty("version");
			}
		}
		return version;
	}

	/**
	 * Compares given version string against current jedis version.
	 * 
	 * @param version
	 * @return true in case given version is greater than equal to current one.
	 */
	public static boolean atLeast(String version) {
		return jedisVersion.compareTo(parseVersion(version)) >= 0;
	}

	/**
	 * Compares given version string against current jedis version.
	 * 
	 * @param version
	 * @return true in case given version is less than equal to current one.
	 */
	public static boolean atMost(String version) {
		return jedisVersion.compareTo(parseVersion(version)) <= 0;
	}

}
