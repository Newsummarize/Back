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
import com.newsummarize.backend.dto.ChangePasswordRequest;


import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final InterestRepository interestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void signup(SignupRequest request) {
        // 1. 비밀번호 확인
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 2. 이메일 중복 확인
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        // 3. 관심사 매핑
        Set<String> interestNames = request.getInterests();
        Set<Interest> interestEntities = interestNames.stream()
                .map(name -> interestRepository.findByInterestCategory(name)
                        .orElseThrow(() -> new RuntimeException("해당 관심사가 존재하지 않습니다: " + name)))
                .collect(Collectors.toSet());

        // 4. User 생성 및 저장
        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAge(request.getAge());
        user.setGender(request.getGender());
        user.setInterests(interestEntities);

        userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return jwtTokenProvider.createToken(user.getEmail());
    }

    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(User user) {
        return MyPageResponse.builder()
                .userName(user.getUserName())
                .email(user.getEmail())
                .age(user.getAge())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .interests(user.getInterests().stream()
                        .map(interest -> interest.getInterestCategory())
                        .toList())
                .build();
    }

    @Transactional
    public void addInterest(String token, String newCategory) {
        String email = jwtTokenProvider.getUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Interest interest = interestRepository.findByInterestCategory(newCategory)
                .orElseGet(() -> {
                    Interest newInterest = Interest.builder()
                            .interestCategory(newCategory)
                            .build();
                    return interestRepository.save(newInterest);
                });

        if (!user.getInterests().contains(interest)) {
            user.getInterests().add(interest);
            userRepository.save(user);
        } else {
            throw new RuntimeException("이미 추가된 관심사입니다.");
        }
    }


    @Transactional
    public void removeInterest(String token, String interestCategory) {
        // "Bearer " 제거 처리 추가
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

    @Transactional
    public void changePassword(String token, ChangePasswordRequest request) {
        String pureToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        String email = jwtTokenProvider.getUsername(pureToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new RuntimeException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new RuntimeException("새 비밀번호는 이전 비밀번호와 같을 수 없습니다.");

        }


        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void deleteUser(String token) {
        String pureToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        String email = jwtTokenProvider.getUsername(pureToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 연관된 관심사 관계 제거 (Cascade 설정된 경우 생략 가능)
        user.getInterests().clear();

        // 사용자 삭제
        userRepository.delete(user);
    }

}
