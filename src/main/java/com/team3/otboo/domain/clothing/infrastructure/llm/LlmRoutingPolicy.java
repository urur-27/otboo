package com.team3.otboo.domain.clothing.infrastructure.llm;

import com.team3.otboo.config.LlmProps;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 기본 라우팅 정책:
 * - application.yml에 llm.default-provider / llm.default-model이 있으면 그 값을 사용
 * - 없으면 둘 다 null 반환 → 파이썬 .env 기본값 사용
 */
@Component
@RequiredArgsConstructor
public class LlmRoutingPolicy {

    private final LlmProps props;

    // tenantId 나중에 A/B/테넌트 분기용으로 씀. 지금은 무시.
    public LlmRoute choose(String tenantId) {
        String provider = props.defaultProvider(); // 없으면 null 가능
        String model    = props.defaultModel();
        return new LlmRoute(provider, model);
    }
}