package com.dafy.skye.hadhoop.workflow.action;

import com.dafy.skye.model.Result;

import java.util.Map;

/**
 * Created by quanchengyun on 2017/10/24.
 */
public interface OozieAction {

    Result doAction(Map configs) throws Exception;
}
