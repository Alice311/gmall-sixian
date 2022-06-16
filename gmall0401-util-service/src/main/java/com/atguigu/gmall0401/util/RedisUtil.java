package com.atguigu.gmall0401.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public  class RedisUtil {

private JedisPool jedisPool;

public  void  initJedisPool(String host,int port,int database){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 总数：跟并发有关系。
        jedisPoolConfig.setMaxTotal(200);

        // 如果到最大数（200），设置等待
        jedisPoolConfig.setBlockWhenExhausted(true);

        // 获取连接时等待的最大毫秒
        jedisPoolConfig.setMaxWaitMillis(1000);
        // 最少剩余数：连接池可以释放链接，但是最少要保留10个
        jedisPoolConfig.setMinIdle(10);
	//最大剩余数：要求释放。
	//最少最多都是10:代表最好维持在10个
        jedisPoolConfig.setMaxIdle (10);

        // 在获取连接时，检查是否有效
	//测试从连接池借到的连接是不是好连接。解决：连接池释放点的时间不一致
        jedisPoolConfig.setTestOnBorrow(true);

       // 创建连接池。连接池初始化
        jedisPool = new JedisPool(jedisPoolConfig,host,port,2*1000);

        }
        public Jedis getJedis(){
                Jedis jedis = jedisPool.getResource();
                return jedis;

        }
}