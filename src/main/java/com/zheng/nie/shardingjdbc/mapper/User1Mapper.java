package com.zheng.nie.shardingjdbc.mapper;

import com.zheng.nie.shardingjdbc.entity.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: niezheng1
 * @Date: 2018/11/19 20:06
 */
public interface User1Mapper {

    List<Order> getAll();

    void insert(Order user);

    Long getTotal();

    Order getUser(@Param("id") Long id);

    List<Order> getOrder(List<Long> userIds);

}
