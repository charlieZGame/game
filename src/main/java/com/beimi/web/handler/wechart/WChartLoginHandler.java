package com.beimi.web.handler.wechart;

import com.beimi.web.handler.Handler;
import com.beimi.web.service.repository.jpa.PlayUserClientRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by zhengchenglei on 2018/3/15.
 */
public class WChartLoginHandler {

    @Autowired
    private PlayUserClientRepository playUserClientRepository;

    public void validateUserInfo(String appId, String appSecret, String code, Handler handler){

        //playUserClientRepository.save()

    }







}
