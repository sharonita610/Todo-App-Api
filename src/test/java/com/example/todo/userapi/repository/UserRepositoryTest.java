package com.example.todo.userapi.repository;

import com.example.todo.userapi.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;

import javax.persistence.Table;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("회원가입 테스트")
    void saveTest(){

        // given
        User newUser = User.builder()
                .email("abc123@aaa.com")
                .password("1234")
                .userName("망아지")
                .build();

        // when
        User saved = userRepository.save(newUser);

        // then
        assertNotNull(saved);
    }


    @Test
    @DisplayName("이메일로 회원 조회하기")
    void findByEmailTest(){

        String email = "abc123@aaa.com";

        Optional<User> byEmail = userRepository.findByEmail(email);

        assertTrue(byEmail.isPresent());
        User user = byEmail.get();
        assertEquals("망아지", user.getUserName());


        System.out.println("\n\n\n\n\n\n");
        System.out.println("user = " + user.getUserName());
        System.out.println("\n\n\n\n\n\n");


    }

    @Test
    @DisplayName("이메일로 중복 체크를 하면 중복값이 true여야한다")
    void email(){

        String email = "abc123@aaa.com";

        boolean flag = userRepository.existsByEmail(email);

        assertTrue(flag);


    }

    @Test
    @DisplayName("이메일로 중복 체크를 하면 중복값이 false여야한다")
    void emailFalse(){

        String email = "abc121@aaa.com";

        boolean flag = userRepository.existsByEmail(email);

        assertTrue(flag);


    }

}