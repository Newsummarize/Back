// 사용자(User) 정보를 데이터베이스에서 조회/저장/삭제하기 위한 리포지토리 클래스
package com.newsummarize.backend.repository;

import com.newsummarize.backend.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// 이 클래스가 리포지토리 계층임을 명시 (스프링 컴포넌트 스캔 대상)
@Repository

// 기본적으로 read-only 트랜잭션으로 처리 (쓰기 메서드는 별도 @Transactional 지정)
@Transactional(readOnly = true)
public class UserRepository {

    // JPA의 EntityManager 주입
    @PersistenceContext
    private EntityManager em;

    // 사용자 저장 (회원가입 등)
    @Transactional
    public void save(User user) {
        em.persist(user);
    }

    // 사용자 ID(PK)로 조회
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    // 이메일로 사용자 단건 조회
    public Optional<User> findByEmail(String email) {
        List<User> list = em.createQuery(
                        "SELECT u FROM User u WHERE u.email = :email", User.class
                )
                .setParameter("email", email)
                .getResultList();

        // 결과가 없으면 Optional.empty(), 있으면 첫 번째 요소 반환
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    // 이메일로 사용자 + 관심사(Set<Interest>)를 함께 조회 (Fetch Join)
    public Optional<User> findWithInterestsByEmail(String email) {
        List<User> list = em.createQuery(
                        "SELECT DISTINCT u FROM User u " +
                                "LEFT JOIN FETCH u.interests " +
                                "WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultList();

        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    // 사용자 삭제 (탈퇴 시)
    @Transactional
    public void delete(User user) {
        // 영속 상태인지 확인 후, 아니면 merge 후 삭제
        em.remove(em.contains(user) ? user : em.merge(user));
    }
}
