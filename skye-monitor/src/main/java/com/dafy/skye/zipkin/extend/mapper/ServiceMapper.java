package com.dafy.skye.zipkin.extend.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * Created by quanchengyun on 2018/4/13.
 */
public interface ServiceMapper {

    void deleteUserFollow(@Param("user") String user,@Param("services") Set<String> set);

    void addUserFollow(@Param("user") String user, @Param("services") Set<String> set);

    List<String> listFollow(@Param("user") String user);
}
