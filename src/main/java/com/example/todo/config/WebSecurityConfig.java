package com.example.todo.config;

import com.example.todo.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;

@EnableWebSecurity
@RequiredArgsConstructor
// 자동 권한 검사를 수행하기 위한 설정
@EnableGlobalMethodSecurity(prePostEnabled = true) // 자동권한 검사를 여기서 해준다
//@Configuration
public class WebSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    // 다른 사람이 만들어 둔 것을 주입 받을 때 필요하다. DI, dependency injection
    @Bean
    public PasswordEncoder passwordEncoder(){

        return new BCryptPasswordEncoder();
    }


    // 시큐리티 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors()
                .and()
                .csrf().disable()
                .httpBasic().disable()
                // 세션인증을 사용하지 않겠다
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //  세션 인증 안쓴다는 설정
                .and()
                // 어떤 요청에서 인증을 안할 것인지 설정, 언제 할 것인지 설정
                .authorizeRequests()
                .antMatchers(HttpMethod.PUT, "/api/auth/promote").authenticated()
                .antMatchers("/", "/api/auth/**").permitAll()

                .anyRequest().authenticated()
        ;

        // 토큰인증 필터 연결
        http.addFilterAfter(
          jwtAuthFilter
          , CorsFilter.class       // import 시 주의 : Spring 걸로 가져와야됨
        );

        return http.build();
    }



}
