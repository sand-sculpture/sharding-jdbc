package com.zheng.nie.shardingjdbc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: niezheng1
 * @Date: 2018/11/22 14:41
 */
public class App {
    public static void main(String[] args) {
        List list = new ArrayList();
        User user = new User();
        user.setAge(1);
        user.setName(33);
        list.add(user);
        User user1 = new User();
        user.setName(1);
        user.setAge(2);
        list.add(user1);
        System.out.println(list.toString());
    }
}
