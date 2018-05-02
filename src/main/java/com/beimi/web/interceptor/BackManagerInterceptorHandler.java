package com.beimi.web.interceptor;

import com.beimi.backManager.Session;
import com.beimi.backManager.SessionMapping;
import com.beimi.backManager.WEChartUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhengchenglei on 2018/4/7.
 */
public class BackManagerInterceptorHandler extends HandlerInterceptorAdapter {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        long tid = System.currentTimeMillis();
        String code = request.getHeader("accesstoken");
        logger.info("tid:{}， 访问 code:{}",tid,code);
        if(StringUtils.isEmpty(code)){
            logger.info("tid:{}用户访问管理端数据不通过",tid);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }

        String openId = null;
        if(SessionMapping.containsKey(code)){
            openId = SessionMapping.getOpenId(code);
        }else {
            openId = WEChartUtil.getSessionKeyOrOpenid(tid, request, null);
            if(StringUtils.isNotEmpty(openId)) {
                Session session = new Session();
                session.setOpenId(openId);
                SessionMapping.put(code,session);
            }
        }
        if(StringUtils.isEmpty(openId)){
            logger.info("tid:{}用户访问管理端数据不通过",tid);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }
        request.setAttribute("openId",openId);
        logger.info("tid:{}用户访问管理端数据通过",tid);
        return true;
    }

}
