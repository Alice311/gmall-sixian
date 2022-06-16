package com.atguigu.gmall0401.config;


import com.atguigu.gmall0401.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    //读取配置文件中的redis的ip地址
	//@value:从配置文件读取相关配置
    @Value("${spring.redis.host:disabled}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;



    @Bean
    public RedisUtil getRedisUtil(){
	//判断是否生成连接池
        if(host.equals("disabled")){
            return null;
        }
        RedisUtil redisUtil=new RedisUtil();
	//连接池初始化。host:地址	port:端口号	database:数据库
        redisUtil.initJedisPool(host,port,database);
        return redisUtil;
    }

}
