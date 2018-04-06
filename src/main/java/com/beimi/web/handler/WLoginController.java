/*
package com.beimi.web.handler;

import com.alibaba.fastjson.JSONObject;
import com.beimi.model.GameResponse;
import com.beimi.web.handler.wechart.WChartLoginHandler;
import com.beimi.web.model.ResultData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

*/
/**
 * Created by zhengchenglei on 2018/3/15.
 *//*

@Controller
//@RequestMapping("/wchartLogin")
//@RequestMapping("/login")
public class WLoginController extends Handler {

    private String loginCheckUrl = "http://oauth.anysdk.com/api/User/LoginOauth/";

    private int connectTimeOut = 30000;

    private int timeOut = 30000;

    private static final String userAgent = "px v1.0";
    @Autowired
    WChartLoginHandler loginHandler;

    private Logger logger = LoggerFactory.getLogger(WLoginController.class);


    @ResponseBody
    //@RequestMapping("/check")
    public ResponseEntity<ResultData> check(HttpServletRequest request, HttpServletResponse response) {
        long tid = System.currentTimeMillis();
        try {
            Map<String, String[]> params = request.getParameterMap();
            String validateResponse = paramValidate(params);
            if (StringUtils.isNotEmpty(validateResponse)) {
                logger.error("tid:{} wechart user login failue. userInfo:{} reason:{}",tid,JSONObject.toJSONString(params),validateResponse);
                ResultData resultData = new ResultData(false,"request parameter illegal",null);
                return new ResponseEntity<>(resultData , HttpStatus.OK);
            }

           // String openId = loginHandler.getOpenId((String)(params.get("uapi_key")[0]),(String)(params.get("uapi_secret")[0]),(String)(params.get("code")[0]),this);
            String openId = loginHandler.getOpenIdByServer();

            logger.info("获取OPENID 信息 openId:{}",openId);

            ResultData resultData = loginHandler.getUserInfo(openId,request);

            return new ResponseEntity<ResultData>(resultData, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("tid:{} wechart login failed",tid,e);
            e.printStackTrace();
            ResultData resultData = new ResultData(false,e.getMessage(),null);
            return new ResponseEntity<>(resultData , HttpStatus.OK);
        }

    }


    */
/**
     *
     * @param params
     * @return
     *//*

    private String paramValidate(Map<String, String[]> params) {
        if (!params.containsKey("channel") || params.get("channel") == null ||  params.get("channel").length <= 0) {
            return "channel can't be null";
        }
        if (!params.containsKey("uapi_key") || params.get("uapi_key") == null ||  params.get("uapi_key").length <= 0) {
            return "uapi key can't be null";
        }
        if(!params.containsKey("uapi_secret")|| params.get("uapi_secret") == null ||  params.get("uapi_secret").length <= 0 ){
            return "uapi_secret can't be null";
        }
        if(!params.containsKey("code") ||  params.get("code") == null || params.get("code").length <= 0 ){
            return "code is null";
        }
        return null;
    }

    private String getQueryString(HttpServletRequest request) {
        Map params = request.getParameterMap();
        String queryString = "";
        for (Object key : params.keySet()) {
            String[] values = (String[]) params.get(key);
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                queryString = new StringBuilder().append(queryString).append(key).append("=").append(value).append("&").toString();
            }
        }
        queryString = queryString.substring(0, queryString.length() - 1);
        return queryString;
    }

    private String stream2String(InputStream is) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String str1 = sb.toString();
            return str1;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tryClose(br);
        }
        return "";
    }

    private void sendToClient(HttpServletResponse response, GameResponse gameResponse) {
        response.setContentType("text/plain;charset=utf-8");
        try {
            PrintWriter writer = response.getWriter();
            writer.write(JSONObject.toJSONString(gameResponse));
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryClose(OutputStream os) {
        try {
            if (null != os) {
                os.close();
                os = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryClose(Writer writer) {
        try {
            if (null != writer) {
                writer.close();
                writer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryClose(Reader reader) {
        try {
            if (null != reader) {
                reader.close();
                reader = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLoginCheckUrl(String loginCheckUrl) {
        this.loginCheckUrl = loginCheckUrl;
    }

    public void setConnectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

}
*/
