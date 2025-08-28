package com.team3.otboo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisTemplateConfig {

//    /**  캐시(DB=1, SSL 반영된 @Qualifier("cache") 커넥션 사용) */
//    @Bean(name = "cacheStringRedisTemplate")
//    public StringRedisTemplate cacheStringRedisTemplate(
//            @Qualifier("cache") RedisConnectionFactory cf) {
//        return new StringRedisTemplate(cf);
//    }

    /** 필요 시 pub/sub 쪽에서도 StringRedisTemplate이 요구되면 사용 */
    @Bean(name = "chatStringRedisTemplate")
    public StringRedisTemplate chatStringRedisTemplate(
            @Qualifier("chatPubSub") RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }
}
