package com.newsummarize.backend.dto;

import com.newsummarize.backend.domain.Category;
import com.newsummarize.backend.domain.Gender;
import lombok.*;

import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SignupRequest {
    private String userName;       // user_name
    private int year;
    private int month;
    private int day;
    private Gender gender;         // M, F, Other
    private String email;          // email
    private String password;       // raw password
    private String confirmPassword;
    private Set<String> interests;  // interest_category 문자열들

}
