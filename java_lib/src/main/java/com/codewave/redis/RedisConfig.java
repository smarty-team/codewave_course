package com.codewave.redis;

import com.netease.lowcode.core.annotation.NaslConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    /**
     * redis 地址
     */
    @NaslConfiguration(systemScope= true, alias="spring.redis.host")
    public String redisHost;

    /**
     * redis 端口
     */
    @NaslConfiguration(systemScope= true, alias="spring.redis.port")
    public String redisPort;

    /**
     * redis 数据库
     */
    @NaslConfiguration(systemScope= true, alias="spring.redis.database")
    public String database;

    /**
     * redis 密码
     */
    @NaslConfiguration(systemScope= true, alias="spring.redis.password")
    public String password;


//    /**
//     * redis sentinel 节点
//     */
//    @NaslConfiguration
//    public String redisSentinelNodes;
//    /**
//     * redis哨兵密码
//     */
//    @NaslConfiguration
//    private String redisSentinelPassword;
//    /**
//     * redis cluster 节点
//     */
//    @NaslConfiguration
//    public String redisClusterNodes;
//    /**
//     * redis slave 地址
//     */
//    @NaslConfiguration
//    public String redisSlaveHost;
//    /**
//     * redis slave 端口
//     */
//    @NaslConfiguration
//    public String redisSlavePort;
//    /**
//     * Redis 数据库索引
//     */
//    @NaslConfiguration
//    private String redisDatabase;
//
//    /**
//     * 连接超时时间，默认为 0，单位为毫秒。
//     */
//    @NaslConfiguration
//    private String springRedisTimeout;
//    /**
//     * 是否启用 SSL 连接，默认为 false。
//     */
//    @NaslConfiguration
//    private String springRedisSsl;
//    /**
//     * 连接池最大连接数。
//     */
//    @NaslConfiguration
//    private String springRedisLettucePoolMaxActive;
//    /**
//     * 连接池中的最大空闲连接。
//     */
//    @NaslConfiguration
//    private String springRedisLettucePoolMaxIdle;
//    /**
//     * 连接池中的最小空闲连接。
//     */
//    @NaslConfiguration
//    private String springRedisLettucePoolMinIdle;

//    /**
//     * 执行命令的超时时间，默认为 3 秒。
//     */
//    @NaslConfiguration
//    private String springRedisLettuceCommandTimeout;

//    /**
//     * 关闭连接时的超时时间，默认为 100 毫秒。
//     */
//    @NaslConfiguration
//    private String springRedisLettuceShutdownTimeout;

//    /**
//     * 设置客户端名称。
//     */
//    @NaslConfiguration
//    private String springRedisLettuceClientName;
}
