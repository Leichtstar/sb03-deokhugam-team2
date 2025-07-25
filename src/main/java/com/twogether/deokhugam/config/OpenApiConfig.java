package com.twogether.deokhugam.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Twogether-Read팀 API 문서",
                version = "1.0.0",
                description = "덕후감 프로젝트의 백엔드 API 명세입니다."
        )
)
public class OpenApiConfig {
}