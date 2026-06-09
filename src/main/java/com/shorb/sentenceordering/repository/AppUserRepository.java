package com.shorb.sentenceordering.repository;

import com.shorb.sentenceordering.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    List<AppUser> findByRoleAndGradeOrderByUsernameAsc(AppUser.Role role, Integer grade);
}
