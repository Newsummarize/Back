// 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
package com.newsummarize.backend.service;

import com.newsummarize.backend.domain.Interest;
import com.newsummarize.backend.domain.User;
import com.newsummarize.backend.dto.ChangePasswordRequest;
import com.newsummarize.backend.dto.LoginRequest;
import com.newsummarize.backend.dto.MyPageResponse;
import com.newsummarize.backend.dto.SignupRequest;
import com.newsummarize.backend.repository.InterestRepository;
import com.newsummarize.backend.repository.UserRepository;
import com.newsummarize.backend.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final InterestRepository interestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입 처리
    @Transactional
    public void signup(SignupRequest request) {
        // 비밀번호 일치 여부 검증
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 이메일 중복 여부 확인
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        // 관심사 문자열 Set → Interest 엔티티 Set으로 변환
        Set<String> interestNames = request.getInterests();
        Set<Interest> interestEntities = interestNames.stream()
                .map(name -> interestRepository.findByInterestCategory(name)
                        .filter(Interest::isDefault)
                        .orElseThrow(() -> new RuntimeException("해당 관심사는 회원가입 시 선택할 수 없습니다: " + name)))
                .collect(Collectors.toSet());


        // 생년월일로부터 나이 계산
        LocalDate birthDate = LocalDate.of(request.getYear(), request.getMonth(), request.getDay());
        int age = Period.between(birthDate, LocalDate.now()).getYears();

        // User 엔티티 생성 및 저장
        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAge(age);
        user.setGender(request.getGender());
        user.setBirthDate(birthDate);
        user.setInterests(interestEntities);

        userRepository.save(user);
    }

    // 로그인 처리 및 토큰 발급
    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 이메일 기반 JWT 토큰 생성
        return jwtTokenProvider.createToken(user.getEmail());
    }

    // 마이페이지 사용자 정보 반환
    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(User user) {
        List<String> defaultInterests = user.getInterests().stream()
                .filter(Interest::isDefault)
                .map(Interest::getInterestCategory)
                .toList();

        List<String> customInterests = user.getInterests().stream()
                .filter(interest -> !interest.isDefault())
                .map(Interest::getInterestCategory)
                .toList();

        // birthDate에서 연, 월, 일 추출
        int year = 0, month = 0, day = 0;
        if (user.getBirthDate() != null) {
            year = user.getBirthDate().getYear();
            month = user.getBirthDate().getMonthValue();
            day = user.getBirthDate().getDayOfMonth();
        }

        return MyPageResponse.builder()
                .userName(user.getUserName())
                .email(user.getEmail())
                .age(user.getAge())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .defaultInterests(defaultInterests)
                .customInterests(customInterests)
                .year(year)
                .month(month)
                .day(day)
                .build();
    }



    // 관심사 추가
    @Transactional
    public void addInterest(String token, String newCategory) {
        String email = jwtTokenProvider.getUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 관심사 없으면 새로 생성 → 기본값은 isDefault = false
        Interest interest = interestRepository.findByInterestCategory(newCategory)
                .orElseGet(() -> interestRepository.save(
                        Interest.builder()
                                .interestCategory(newCategory)
                                .isDefault(false) // 나중에 사용자 정의로 추가된 관심사
                                .build()
                ));

        // 중복되지 않으면 추가
        if (!user.getInterests().contains(interest)) {
            user.getInterests().add(interest);
            userRepository.save(user);
        } else {
            throw new RuntimeException("이미 추가된 관심사입니다.");
        }
    }

    // 관심사 제거
    @Transactional
    public void removeInterest(String token, String interestCategory) {
        String pureToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        String email = jwtTokenProvider.getUsername(pureToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        Interest interest = interestRepository.findByInterestCategory(interestCategory)
                .orElseThrow(() -> new RuntimeException("해당 관심사가 존재하지 않습니다."));

        if (!user.getInterests().contains(interest)) {
            throw new RuntimeException("사용자의 관심사에 존재하지 않는 항목입니다.");
        }

        user.getInterests().remove(interest);
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(String token, ChangePasswordRequest request) {
        String pureToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        String email = jwtTokenProvider.getUsername(pureToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 확인
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new RuntimeException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 기존 비밀번호와 같은지 확인
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new RuntimeException("새 비밀번호는 이전 비밀번호와 같을 수 없습니다.");
        }

        // 새 비밀번호로 업데이트
        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(String token) {
        String pureToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        String email = jwtTokenProvider.getUsername(pureToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 사용자-관심사 관계 해제
        user.getInterests().clear();

        // 사용자 삭제
        userRepository.delete(user);
    }
}
