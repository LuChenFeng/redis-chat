package pers.lcf.chat.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @Param:
 * @Return:
 * @Author: lcf
 * @Date: 2019/10/20 12:34
 * 全局Runtime异常捕获
 */
public class GlobalException extends RuntimeException {

    @Getter
    @Setter
    private String msg;

    public GlobalException(String message) {
        this.msg = message;
    }
}
