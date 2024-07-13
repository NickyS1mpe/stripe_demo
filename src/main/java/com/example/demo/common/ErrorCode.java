package com.example.demo.common;

public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "Params error"),
    NOT_LOGIN_ERROR(40100, "Not login"),
    NO_AUTH_ERROR(40101, "No permission"),
    NOT_FOUND_ERROR(40400, "Data not found"),
    FORBIDDEN_ERROR(40300, "Forbidden"),
    SYSTEM_ERROR(50000, "System error"),
    OPERATION_ERROR(50001, "Operation error");

    private final int code;

    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
