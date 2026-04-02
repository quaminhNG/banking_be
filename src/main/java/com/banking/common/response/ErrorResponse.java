package com.banking.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;

    public static ErrorResponse build(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, path, LocalDateTime.now());
    }
}
