package com.mingleroom.domain.users.repository;

import com.mingleroom.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer> {
}
