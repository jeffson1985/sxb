package org.sxb.data.redis.connection.jedis;

import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

public class TestJedisConn {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
		
		try (Jedis jedis = pool.getResource()) {
			  /// ... do stuff here ... for example
			  jedis.set("foo", "bar");
			  String foobar = jedis.get("foo");
			  jedis.zadd("sose", 0, "car"); jedis.zadd("sose", 0, "bike"); 
			  Set<String> sose = jedis.zrange("sose", 0, -1);
			}
		
		Jedis jedis = pool.getResource();
		Pipeline p = jedis.pipelined();
		p.set("hel", "こんいちは"); 
		p.zadd("very", 1, "とても");  
		//p.zadd("foo", 0, "barinsky");
		//p.zadd("foo", 1, "barikoviev");
		//Response<String> pipeString = p.get("fool");
		//Response<Set<String>> sose = p.zrange("foo", 0, -1);
		p.sync(); 

		//int soseSize = sose.get().size();
		//Set<String> setBack = sose.get();
		
		pool.destroy();
		
		
		/*
		
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		List<JedisShardInfo> info = new ArrayList<>();
		JedisShardInfo i = new JedisShardInfo("localhost");
		info.add(i);
		ShardedJedisPool pool=new ShardedJedisPool(config, info);
		
		ShardedJedis jedis=pool.getResource();
		//Jedis jedis = new Jedis();
		//JedisConnection conn = new JedisConnection(jedis);
		jedis.set("nihao", "hello");
		jedis.set("hello", "今日は");
		jedis.set("good", "いいね");
		//jedis.save();
		
		System.out.println("get from redis->" + jedis.get("nihao"));
		System.out.println("get from redis->" + jedis.get("hello"));
		System.out.println("get from redis->" + jedis.get("good"));
		//conn.close();
		 
		 */

	}

}
