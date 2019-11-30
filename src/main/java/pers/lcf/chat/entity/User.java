package pers.lcf.chat.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @Param: 
 * @Return: 
 * @Author: lcf
 * @Date: 2019/10/20 12:46
 */
@Data

public class User implements Serializable {

    private String id;

    private String name;

    private String avatar;

    public void setName(String name) {
        this.name = name.trim();
    }
}
