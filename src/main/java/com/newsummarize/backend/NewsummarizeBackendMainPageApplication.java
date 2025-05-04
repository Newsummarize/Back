// Newsummarize 프로젝트의 백엔드 메인 실행 클래스
package com.newsummarize.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Spring Boot 애플리케이션임을 명시하는 어노테이션
// 컴포넌트 스캔, 자동 설정 등을 활성화함
@SpringBootApplication
public class NewsummarizeBackendMainPageApplication {

	// 애플리케이션의 시작 지점
	public static void main(String[] args) {
		// Spring Boot 애플리케이션 실행
		SpringApplication.run(NewsummarizeBackendMainPageApplication.class, args);
	}
}
