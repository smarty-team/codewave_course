package com.codewave.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.netease.lowcode.core.annotation.NaslLogic;

/**
 * Hello world!
 *
 */
@Component
public class MyComponent {

    @Value("${myHost}")
    private String myHost;


    @Value("${spring.mongo.host}")
    private  String mongoHost;

    /**
     * 示例逻辑：相加
     * 
     * @param a
     * @param b
     * @return
     */
    @NaslLogic
    public Integer add2(Integer a, Integer b) {
        return a + b;
    }

    /**
     * 获取MyHost
     * @return
     */
    @NaslLogic
    public String getMyHost() {
        return myHost;
    }

    /**
     * 获取MyRedisHost
     * @return
     */
    @NaslLogic
    public String getMongoHost() {
        return mongoHost;
    }

}
