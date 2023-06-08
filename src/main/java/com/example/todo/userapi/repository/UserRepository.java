package com.example.todo.userapi.repository;

import com.example.todo.userapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    // 쿼리 메서드




    // 이메일 회원 정보 조회
    Optional<User> findByEmail(String email);


    // 이메일 중복 체크
    // existBy 의 이름을 가진 메서드는 Query 문이 자동으로 나감
    boolean existsByEmail(String email);



}
