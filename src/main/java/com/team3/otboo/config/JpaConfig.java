package com.team3.otboo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // 👈 여기에 어노테이션을 옮겨옵니다.
public class JpaConfig {
}
