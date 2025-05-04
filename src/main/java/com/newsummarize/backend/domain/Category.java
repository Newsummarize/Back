// 뉴스 기사에 사용할 카테고리 열거형(enum) 클래스
package com.newsummarize.backend.domain;

public enum Category {

    // 각 카테고리 항목 정의 (이름 그대로 사용하는 항목은 생략 가능)
    정치,
    경제,
    사회,

    // 실제 표시되는 이름(label)이 다른 경우 문자열로 별도 지정
    생활_문화("생활/문화"),
    세계,
    IT_과학("IT/과학");

    // 카테고리의 사용자 표시용 이름 (예: "생활/문화", "IT/과학")
    private final String label;

    // 기본 생성자: enum 이름 자체를 label로 사용
    Category() {
        this.label = name();
    }

    // 커스텀 label이 있는 항목에 사용되는 생성자
    Category(String label) {
        this.label = label;
    }

    // 외부에서 label 값을 가져오기 위한 getter
    public String getLabel() {
        return label;
    }
}
