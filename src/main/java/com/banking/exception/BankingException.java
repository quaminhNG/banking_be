package com.banking.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BankingException extends RuntimeException {
    private final HttpStatus status;

    // private final BankingException bankingException; k được dùng trong trường hợp
    // này do bankingException được tạo ra mỗi khi có lỗi, nếu tạo final thì spring
    // sẽ
    // không thể bắt được lỗi
    public BankingException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public BankingException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
