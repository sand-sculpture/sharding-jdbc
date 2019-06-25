package com.zheng.nie.shardingjdbc.entity;

import com.zheng.nie.shardingjdbc.enums.UserSexEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: niezheng1
 * @Date: 2018/11/19 20:03
 */
@Data
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private Long order_id;
    private Long user_id;
    private String userName;
    private String passWord;
    private UserSexEnum userSex;
    private String nickName;


}

