package com.zheng.nie.shardingjdbc.controller;

import com.zheng.nie.shardingjdbc.entity.Order;
import com.zheng.nie.shardingjdbc.enums.UserSexEnum;
import com.zheng.nie.shardingjdbc.mapper.User1Mapper;
import com.zheng.nie.shardingjdbc.service.User1Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: niezheng1
 * @Date: 2018/11/19 20:11
 */
@Service
@RestController
public class UserController {

    @Autowired
    private User1Service user1Service;

    @Autowired
    private User1Mapper user1Mapper;

    @RequestMapping("/getUsers")
    public List<Order> getUsers() {
        List<Order> users=user1Service.getUsers();
        return users;
    }

    //测试
    @RequestMapping(value="insert")
    public String test1() {
        for (long i = 1;i <=5;i++){
            Order order = new Order();
            order.setId(i);
            order.setUser_id(i);
            order.setOrder_id(i+(int)(1+Math.random()*(10-1+1)));
            order.setNickName("nickName"+i);
            order.setPassWord("passWord"+i);
            order.setUserName("userName"+i);
            order.setUserSex(UserSexEnum.WOMAN);
            user1Service.insert(order);
        }
        return "test1";
    }


    //测试

    @RequestMapping(value="insert2")
    @ResponseBody
    public List<Order> test2() {
        List<Long> userIds = new ArrayList<>();
            userIds.add(2L);
            userIds.add(3L);
            userIds.add(4L);
        List<Order> order = user1Mapper.getOrder(userIds);
        System.out.println("size ----"+order.size());
        System.out.println("ssssssssssss---"+order);
        return order;
    }
    @RequestMapping("total")
    public Long getTotal(){
        return user1Service.getAll();
    }

    @RequestMapping("user")
    public Order getUser(Long id){
        return user1Service.findUser(id);
    }

}

