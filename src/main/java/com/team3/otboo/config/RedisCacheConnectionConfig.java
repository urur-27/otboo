package com.team3.otboo.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@EnableCaching
@Configuration
public class RedisCacheConnectionConfig {

    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;
    @Value("${spring.data.redis.password:}")
    private String password;
    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${spring.data.redis.database:0}") private int dbIndex; // 배포=0, 로컬=원하면 1
    @Value("${spring.data.redis.cache-prefix:otboo:cache:}") String cachePrefix;

    // 캐시 전용 커넥션 (DB index 분리 + SSL 반영)
    @Bean
    @Qualifier("cache")
    public RedisConnectionFactory cacheConnectionFactory() {
        var conf = new RedisStandaloneConfiguration(host, port);
        conf.setDatabase(dbIndex);
        if (password != null && !password.isBlank()) {
            conf.setPassword(RedisPassword.of(password));
        }
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder();
        if (sslEnabled) builder.useSsl();
        return new LettuceConnectionFactory(conf, builder.build());
    }

    @Bean
    public RedisCacheManager cacheManager(
            @Qualifier("cache") RedisConnectionFactory cf
    ) {
        // 타입정보 포함(no-arg) → LinkedHashMap 캐스트 이슈 방지
        var serializer = new GenericJackson2JsonRedisSerializer();

        var defaults = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith(cachePrefix)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();

        return RedisCacheManager.builder(cf)
                .cacheDefaults(defaults)
                // 캐시별 TTL
                .withCacheConfiguration("clothingList",  defaults.entryTtl(Duration.ofSeconds(60)))
                .withCacheConfiguration("attrSnapshot",  defaults.entryTtl(Duration.ofHours(6)))
                .withCacheConfiguration("attrDefsPage", defaults.entryTtl(Duration.ofSeconds(300)))
                .build();
    }
}
