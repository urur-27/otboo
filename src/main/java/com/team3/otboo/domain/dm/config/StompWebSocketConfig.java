package com.team3.otboo.domain.dm.config;

import com.team3.otboo.domain.dm.interceptor.HttpHandshakeInterceptor;
import com.team3.otboo.domain.dm.interceptor.WebSocketChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final HttpHandshakeInterceptor handShakeInterceptor;
	private final WebSocketChannelInterceptor channelInterceptor;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/sub"); // 메시지 수신 엔드 포인트
		registry.setApplicationDestinationPrefixes("/pub"); // 메시지 송신 엔드 포인트
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws") // 웹소켓 연결 엔드 포인트 .
			.setAllowedOriginPatterns("*") // cors 설정
			.withSockJS();
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(channelInterceptor) // interceptor 설정 .
			.taskExecutor(inboundChannelExecutor());
		// 스레풀을 지정하지 않으면 Spring 이 스레드를 무제한으로 생성함 .
	}

	@Override
	public void configureClientOutboundChannel(ChannelRegistration registration) {
		registration.taskExecutor(outboundChannelExecutor());
	}

	@Bean("websocketTaskScheduler")
	public TaskScheduler websocketTaskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
		scheduler.setThreadNamePrefix("websocket-broker-");
		scheduler.setWaitForTasksToCompleteOnShutdown(true);
		scheduler.setAwaitTerminationSeconds(10);
		scheduler.initialize();
		return scheduler;
	}

	@Bean("websocketInboundExecutor")
	public ThreadPoolTaskExecutor inboundChannelExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(8);
		executor.setMaxPoolSize(16);
		executor.setQueueCapacity(1000);
		executor.setThreadNamePrefix("inbound-");
		executor.initialize();
		return executor;
	}

	@Bean("websocketOutboundExecutor")
	public ThreadPoolTaskExecutor outboundChannelExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(8);
		executor.setMaxPoolSize(16);
		executor.setQueueCapacity(1000);
		executor.setThreadNamePrefix("outbound-");
		executor.initialize();
		return executor;
	}
}
