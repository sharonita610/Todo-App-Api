package com.example.todo.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NoRegisteredArgmentsException extends RuntimeException {


    // 기본 생성자 + 에러메시지를 받는 생성자
    public NoRegisteredArgmentsException(String message){
        super(message);
    }
}
