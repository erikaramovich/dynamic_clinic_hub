package com.miro.project.repository;

import com.miro.project.model.Role;
import com.miro.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByNameAndRole(String name, Role role);
    boolean existsByNameAndRole(String name, Role role);
    boolean existsByEmailAndRole(String email, Role role);
}
