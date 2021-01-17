package com.github.yfge.fool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CommonResponse<T> {

    private int code;
    private String message;
    private T data;

    public CommonResponse(T data) {
        this.data = data;
        this.code = 0;
        this.message = "SUCCESS";
    }

}
