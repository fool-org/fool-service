package org.fool.framework.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CommonResponse<T> {
    public static final int UNION_ERR_CODE = -1;
    private static final String SUCCESS_MESSAGE = "success";
    private static final int SUCCCESS_CODE = 0;
    public static CommonResponse UNOWN_ERROR_RESPONSE;

    static {
        UNOWN_ERROR_RESPONSE = builder()
                .code(UNION_ERR_CODE)
                .message("发生未知错误")
                .build();
    }

    private int code;
    private String message;
    private T data;


    public CommonResponse(CommonException ex) {
        this.code = ex.getCode();
        this.message = ex.getMessage();
    }


    public CommonResponse(T data
    ) {
        this.code = SUCCCESS_CODE;
        this.message = SUCCESS_MESSAGE;
        this.data = data;
    }

}
