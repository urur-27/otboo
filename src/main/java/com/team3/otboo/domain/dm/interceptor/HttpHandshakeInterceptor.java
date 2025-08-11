package com.team3.otboo.domain.dm.interceptor;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
@RequiredArgsConstructor
public class HttpHandshakeInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(
		ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler wsHandler,
		Map<String, Object> attributes) throws Exception {

//		// 토큰 검증
//		if(!request.getHeaders().containsKey("Authorization")) {
//			return null;
//		}
//		String token = request.getHeaders().getFirst("Authorization");
//		if(token != null && jwtService.validate(token)){
//			UUID userId = jwtService.getJwtSession(token).getUserId();
//			attributes.put("userId", userId);
//			return true;
//		}
//		response.setStatusCode(HttpStatus.UNAUTHORIZED);
//		return false;
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Exception exception) {

	}
}
