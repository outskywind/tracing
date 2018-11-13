package com.dafy.skye.alertmanager.notifier;

import com.dafy.skye.alertmanager.config.QywxConfigProperties;
import com.dafy.skye.alertmanager.constant.NotificationChannel;
import com.dafy.skye.alertmanager.dto.AlertExtraInfo;
import com.dafy.base.qywx.sdk.AppClient;
import com.dafy.base.qywx.sdk.AppClientFactory;
import com.dafy.base.qywx.sdk.dto.GetTokenResponse;
import com.dafy.base.qywx.sdk.dto.SendMessageBody;
import com.dafy.base.qywx.sdk.dto.SendMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WechatNotifier extends DefaultNotifier {

    // token有效时间。默认情况是7200秒（即2个小时），这里为了防止因接口响应时间过长导致时间出现偏差，所以这里使用7000秒作为超时时间
    private static final int TOKEN_TIMEOUT_IN_MILLISECONDS = 7000 * 1000;

    private final QywxConfigProperties configProperties;
    private final AppClient appClient;

    // 最近一次获取token的时间
    private long lastGetTokenTime;

    // 企业微信接口使用的token
    private String accessToken;

    @Autowired
    public WechatNotifier(QywxConfigProperties configProperties) {
        super(NotificationChannel.WECHAT);
        this.configProperties = configProperties;
        this.appClient = new AppClientFactory().getAppClient();
    }

    @Override
    protected String[] getUserIdsForReceivers(String service) {
        return null;
    }

    private void refreshToken() {
        GetTokenResponse response = appClient.getToken(configProperties.getCorpId(), configProperties.getSecret());
        if(response != null && response.isSuccess()){
            accessToken = response.getAccess_token();
            lastGetTokenTime = System.currentTimeMillis();
            log.info("WechatNotifier get token success:{}",response);
        }else{
            log.error("WechatNotifier get token fail:{}", response);
            throw new RuntimeException("获取accessToken失败");
        }
    }

    private void checkToken() {
        if(System.currentTimeMillis() - lastGetTokenTime >= TOKEN_TIMEOUT_IN_MILLISECONDS) {
            synchronized (this) {
                if(System.currentTimeMillis() - lastGetTokenTime >= TOKEN_TIMEOUT_IN_MILLISECONDS) {
                    refreshToken();
                }
            }
        }
    }

    @Override
    protected void sendAlert(String alertContent, AlertExtraInfo extraInfo) {
        checkToken();
        sendByIds(alertContent, extraInfo.getReceiverIds());
        sendByIds(alertContent, extraInfo.getCcIds());
    }

    private void sendByIds(String alertContent, String[] ids) {
        if(ArrayUtils.isNotEmpty(ids)) {
            SendMessageBody.Text text = new SendMessageBody.Text();
            text.setContent(alertContent);
            for (String userId: ids) {
                SendMessageBody body = new SendMessageBody();
                body.setAgentid(configProperties.getAgentId());
                body.setText(text);
                body.setMsgtype("text");
                body.setTouser(userId);
                try {
                    SendMessageResponse sendMessageResponse = appClient.sendMessage(this.accessToken, body);
                    if(!sendMessageResponse.isSuccess()){
                        log.error("WechatNotifier fail to send message! {}", sendMessageResponse);
                    }
                } catch (Exception e) {
                    log.error("WechatNotifier send message error! ", e);
                }
            }
        }
    }
}
