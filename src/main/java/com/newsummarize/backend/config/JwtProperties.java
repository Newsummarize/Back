// 이 클래스는 JWT 설정 값을 외부 설정 파일로부터 주입받기 위한 설정 클래스이다.
package com.newsummarize.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// 해당 클래스가 스프링의 Bean으로 등록되도록 지정
@Component
// application.yml 또는 properties에서 "jwt"로 시작하는 설정을 이 클래스 필드에 바인딩
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    // JWT 서명을 위한 비밀 키 값
    private String secret;

    // JWT 토큰의 만료 시간 (밀리초 단위)
    private Long expiration;

    // getter: 다른 클래스에서 비밀 키 값을 가져올 수 있게 함
    public String getSecret() {
        return secret;
    }

    // setter: 외부 설정 값이 자동으로 이 필드에 주입되도록 함
    public void setSecret(String secret) {
        this.secret = secret;
    }

    // getter: 토큰 만료 시간을 반환
    public Long getExpiration() {
        return expiration;
    }

    // setter: 외부 설정으로부터 만료 시간 값을 주입받음
    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }
}
