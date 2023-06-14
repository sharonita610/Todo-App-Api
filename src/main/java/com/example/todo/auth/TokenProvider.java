package com.example.todo.auth;

import com.example.todo.userapi.entity.Role;
import com.example.todo.userapi.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
// 역할 : 토큰을 발급하고, 서명 위조를 검사하는 객체
public class TokenProvider {

    // 서명에 사용할 값(512바이트 이상의 랜덤 문자열)
    @Value("${jwt.secret}")
    private String SECRET_KEY;


    /**
     * Json WEB Token을 생성하는 메서드
     *
     * @return - 생성된 json을 암호화한 토큰 값
     * @Param userEntity - 토큰의 내용(클레임)에 포함될 유저 정보
     */
    // 토큰 생성 메서드
    public String createToken(User userEntity) {

        // 토큰 만료 시간 생성
        Date expiry = Date.from(
          Instant.now().plus(1, ChronoUnit.DAYS)
        );

        // 토큰 생성
        /*
            {
                "iss" : "딸기곤듀",
                "exp" : "2023-07-12",
                "iat" : "2023-06-12",
                "email" : "로그인한 사람 메일",
                "role" : "Premium",

                == 서명
            }
         */

        // 추가 클레임 정의
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", userEntity.getEmail());
        claims.put("role", userEntity.getRole().toString());

        return Jwts.builder()
                // 토큰 header에 들어갈 서명
                .signWith(
                        Keys.hmacShaKeyFor(SECRET_KEY.getBytes())
                        , SignatureAlgorithm.HS512
                )
                // token payload에 들어갈 클레임 설정
                .setClaims(claims) // 추가 클레임은 먼저 설정해야함
                .setIssuer("바닐라 건듀") // iss : 발급자 정보
                .setIssuedAt(new Date()) // iat : 발급 시간
                .setExpiration(expiry) // exp : 토큰 만료 시간
                .setSubject(userEntity.getId()) // sub : 토큰을 식별 할 수 있는 주요데이터
                .compact();
    }

    /**
     * 클라이언트가 전송한 토큰을 디코딩하여 토큰의 위조여부를 확인
     * 토큰을 json 으로 파싱해서 클레임(토큰 정보)를 리턴
     * @param token
     * @return - 토큰 안에있는 인증된 유저 정보를 반환
     */
    public TokenUserInfo validateAndGetTokenUserInfo(String token){

        Claims claims = Jwts.parserBuilder()
                // 토큰 발급자의 발급 당시의 서명을 넣어줌
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                // 서명 위조 검사 : 위조된 경우 예외가 발생함
                // 위조가 되지 않았을 경우 클레임을 리턴
                .build()
                .parseClaimsJws(token)
                .getBody();

        log.info("claims : {}", claims);

        return TokenUserInfo.builder()
                .userId(claims.getSubject()) // subject 안에다가 id를 넣었기 때문에 이렇게 사용한다
                .email(claims.get("email", String.class))
                .role(Role.valueOf(claims.get("role", String.class)))
                .build();

    }


}
