package com.example.todo.userapi.service;

import antlr.Token;
import com.example.todo.auth.TokenProvider;
import com.example.todo.auth.TokenUserInfo;
import com.example.todo.exception.DuplicatedEmailException;
import com.example.todo.exception.NoRegisteredArgmentsException;
import com.example.todo.userapi.dto.request.LoginRequestDTO;
import com.example.todo.userapi.dto.request.UserSignUpRequestDTO;
import com.example.todo.userapi.dto.response.LoginResponseDTO;
import com.example.todo.userapi.dto.response.UserSignUpResponseDTO;
import com.example.todo.userapi.entity.Role;
import com.example.todo.userapi.entity.User;
import com.example.todo.userapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final TokenProvider tokenProvider;


    // 회원가입 처리
    public UserSignUpResponseDTO create(final UserSignUpRequestDTO dto) throws RuntimeException {

        if (dto == null) {
            throw new NoRegisteredArgmentsException("가입 정보가 없습니다");
        }

        String email = dto.getEmail();
        if (userRepository.existsByEmail(email)) {
            log.warn("이메일이 중복 되었습니다. - {}", email);
            throw new DuplicatedEmailException("중복된 이메일입니다");
        }

        // 패스워드 인코딩
        String encoded = encoder.encode(dto.getPassword());
        dto.setPassword(encoded);

        // 유저 엔터티로변환
        User user = dto.toEntity();

        User saved = userRepository.save(user);

        log.info("회원가입 정상 수행됨 - saved user - {}", saved);

        return new UserSignUpResponseDTO(saved);


    }

    public boolean isDuplicate(String email) {

        return userRepository.existsByEmail(email);
    }

    public LoginResponseDTO authenticate(final LoginRequestDTO dto) {

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("가입된 회원이 아닙니다!"));

        // 패스워드 검정
        String rawPassword = dto.getPassword(); // 입력 비번
        String encodedPassword = user.getPassword(); // db에 저장된 비번
        if (!encoder.matches(rawPassword, encodedPassword)) {
            throw new RuntimeException("비밀번호가 틀렸습니다");


        }
        log.info("{}님 로그인 성공!", user.getUserName());

        // 로그인 성공 후에 클라이언트에 뭘 리턴 할것인가?
        // -> JWT를 클라이언트에게 발급 해줘야 함.
        String token = tokenProvider.createToken(user);

        return new LoginResponseDTO(user, token);



    }


    // 프리미엄으로 등급업
    public LoginResponseDTO promoteToPremium(TokenUserInfo userInfo) throws NoRegisteredArgmentsException,
            IllegalStateException {

        // 예외처리
        User foundUser = userRepository.findById(userInfo.getUserId())
                .orElseThrow(() -> new NoRegisteredArgmentsException("회원 조회에 실패"));


        // 일반 회원이 아니면 예외
        if(userInfo.getRole() != Role.COMMON){
            throw new IllegalStateException("일반 회원이 아니면 등급을 상승 시킬 수 없습니다");
        }

        // 등급 변경
        foundUser.changeRole(Role.PREMIUM);
        User saved = userRepository.save(foundUser);

        // 토큰을 재발급
        String token = tokenProvider.createToken(saved);

        return new LoginResponseDTO(saved, token);

    }
}
