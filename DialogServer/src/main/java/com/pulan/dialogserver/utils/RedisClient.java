package com.pulan.dialogserver.utils;

import com.alibaba.fastjson.JSON;
import com.pulan.dialogserver.service.IRedisEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.Set;

/**
 * 定义对Redis 的一系列操作。
 * @author LiHao
 *
 */
@Component
public class RedisClient {

	@Autowired
	private JedisPool jedisPool;

	//将一对键值对儿存入Redis。
	public void set(int db_index,String key, String value) throws Exception {
		try(Jedis jedis = jedisPool.getResource()) {
			jedis.select(db_index);
			jedis.set(key,value);
		}

    }
    //取出指定Key的所有消息
    public String get(int data_base,String key) throws Exception {
		try(Jedis jedis = jedisPool.getResource()) {
			jedis.select(data_base);
			String ret =jedis.get(key);
			if (null ==ret) ret ="";
			return ret;
		}

    }
    //写入消息队列（写入 redis list 先进先出规则）
    public void lpush(String key, String value) throws Exception {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.lpush(key, value);
		} finally {
			if (jedis != null) jedis.close();
		}
    }
    //删除消息队列
    public void del(int data_base,String key) throws Exception {
		try (Jedis jedis = jedisPool.getResource()){
			jedis.select(data_base);
			jedis.del(key);
		}
	}
    //从队列取出消息,移出并获取列表的最后一个元素 阻塞队列。
	public List<String> brpop(String key, int timeout) throws Exception {
        try (Jedis jedis = jedisPool.getResource()){
            return jedis.brpop(timeout, key);
        }
    }
	//设置Key过期时间(自动删除)
	public void expire(int dataIndex,String key, int seconds) throws Exception {
		try (Jedis jedis = jedisPool.getResource()){
			jedis.select(dataIndex);
			jedis.expire(key, seconds);
		}
	}
	//发布消息
	public void publish(String key, String msg) throws Exception {
		try (Jedis jedis = jedisPool.getResource()){
			jedis.publish(key, msg);
		}
	}
	//订阅消息
	public void subscrib(String channel, IRedisEventHandler eventHandler) throws Exception {
		try (Jedis jedis = jedisPool.getResource()){
			JedisPubSub jedisPubSub = new JedisPubSub() {
				@Override
				public void onMessage(String channel, String message) {
					eventHandler.onEvent(channel, message);
				}
			};
			jedis.subscribe(jedisPubSub, channel);
		}
	}
	//Set集合存储数据
	public void sadd(String key,String msg) throws Exception{

		try (Jedis jedis = jedisPool.getResource()){
			jedis.sadd(key,msg);
		}
	}
	//从Set集合取出所有数据
	public Set<String> smembers(String key) throws Exception{
		Set<String> sets =null;
		try (Jedis jedis = jedisPool.getResource()){
			sets =jedis.smembers(key);
			if (sets.isEmpty()){
				sets =null;
			}
			return sets;
		}
	}
	//获取Redis 模糊 Key
	public Set<String> muhuKey(int data_base,String key) throws Exception{
		Set<String> sets ;
		try (Jedis jedis = jedisPool.getResource()){
			jedis.select(data_base);
			sets = jedis.keys(key);
			if (sets.isEmpty()){
				sets =null;
			}
			return sets;
		}
	}
	//取出list集合中的所有值
	public List<String> lrange(int data_base,String key){
		List<String> list ;
		try (Jedis jedis = jedisPool.getResource()){
			jedis.select(data_base);
			list = jedis.lrange(key,0,-1);
			if (list.isEmpty()){
				list = null;
			}
			return list;
		}
	}
	//存储json对象字符串到Redis.
	public void saveSemanticModel(int db_index,String key,Object obj) throws Exception{
		String jsObj = JSON.toJSONString(obj);
		try (Jedis jedis = jedisPool.getResource()){
			jedis.select(db_index);
			jedis.set(key, jsObj);
		}
	}

}
