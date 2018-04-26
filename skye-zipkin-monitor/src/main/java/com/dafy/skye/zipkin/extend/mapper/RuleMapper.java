package com.dafy.skye.zipkin.extend.mapper;

import com.dafy.skye.zipkin.extend.dto.Rule;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * Created by quanchengyun on 2018/4/12.
 */
public interface RuleMapper {

    void add(List<Rule> rules);

    List<Rule> list(@Param("favServices") Set<String> favServices);

    void modify(Rule rule);

    void delete(List<Rule> rules);
}
