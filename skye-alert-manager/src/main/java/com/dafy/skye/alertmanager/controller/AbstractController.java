package com.dafy.skye.alertmanager.controller;

import com.dafy.skye.alertmanager.constant.ResultCode;
import com.dafy.base.nodepencies.model.Response;

public abstract class AbstractController {

    private Response<?> PARAM_ERROR = new Response<>(ResultCode.PARAM_ERROR.getCode(), ResultCode.PARAM_ERROR.getDesc());

    protected Response<?> getResponseForParamError() {
        return PARAM_ERROR;
    }

    protected Response<?> getResponse(ResultCode resultCode) {
        return new Response(resultCode.getCode(), resultCode.getDesc());
    }
}
