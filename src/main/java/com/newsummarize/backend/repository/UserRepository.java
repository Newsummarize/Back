package com.newsummarize.backend.repository;

import com.newsummarize.backend.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class UserRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void save(User user) {
        em.persist(user);
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    public Optional<User> findByEmail(String email) {
        List<User> list = em.createQuery(
                        "SELECT u FROM User u WHERE u.email = :email", User.class
                )
                .setParameter("email", email)
                .getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<User> findWithInterestsByEmail(String email) {
        List<User> list = em.createQuery(
                        "SELECT DISTINCT u FROM User u " +
                                "LEFT JOIN FETCH u.interests " +
                                "WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Transactional
    public void delete(User user) {
        em.remove(em.contains(user) ? user : em.merge(user));
    }

}
