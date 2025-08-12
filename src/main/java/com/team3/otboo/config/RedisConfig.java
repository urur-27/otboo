package com.team3.otboo.config;

import com.team3.otboo.domain.dm.service.SubscribeService;
import com.team3.otboo.domain.notification.service.RedisSubscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {


	@Value("${spring.data.redis.host}")
	private String host;
	@Value("${spring.data.redis.port}")
	private int port;

	@Bean
	@Qualifier("chatPubSub")
	public RedisConnectionFactory chatPubSubFactory() {
		RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
		configuration.setHostName(host);
		configuration.setPort(port);
		return new LettuceConnectionFactory(configuration);
	}

	// publish 객체 .
	@Bean
	@Qualifier("chatPubSub")
	public RedisTemplate<String, Object> redisTemplate(
		@Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

		return redisTemplate;
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
		@Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory,
		MessageListenerAdapter dmListenerAdapter,
			RedisSubscriber redisSubscriber
	) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		container.addMessageListener(
				dmListenerAdapter,
			new PatternTopic("direct-messages") // 수신 엔드 포인트 .
		);
		container.addMessageListener(
				redisSubscriber,
				new PatternTopic("notification-channel") // 수신 엔드 포인트 .
		);
		return container;
	}

	@Bean
	public MessageListenerAdapter dmListenerAdapter(SubscribeService subscribeService) {
		return new MessageListenerAdapter(subscribeService, "onMessage");
	}

}
