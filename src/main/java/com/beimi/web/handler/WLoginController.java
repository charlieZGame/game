package com.beimi.web.handler;

import com.alibaba.fastjson.JSONObject;
import com.beimi.model.GameResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by zhengchenglei on 2018/3/15.
 */
@Controller
@RequestMapping("/wchartLogin")
public class WLoginController extends Handler {

    private String loginCheckUrl = "http://oauth.anysdk.com/api/User/LoginOauth/";

    private int connectTimeOut = 30000;

    private int timeOut = 30000;

    private static final String userAgent = "px v1.0";

    private Logger logger = LoggerFactory.getLogger(WLoginController.class);


    @ResponseBody
    @RequestMapping({"check"})
    public boolean check(HttpServletRequest request, HttpServletResponse response) {
        long tid = System.currentTimeMillis();
        try {
            Map params = request.getParameterMap();
            String validateResponse = paramValidate(params);
            if (StringUtils.isNotEmpty(validateResponse)) {
                logger.error("tid:{} wechart user login failue. userInfo:{} reason:{}",tid,JSONObject.toJSONString(params),validateResponse);
                sendToClient(response, GameResponse.gameErrorResponse(validateResponse,null));
                return false;
            }

            String queryString = getQueryString(request);

            URL url = new URL(this.loginCheckUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "px v1.0");
            conn.setReadTimeout(this.timeOut);
            conn.setConnectTimeout(this.connectTimeOut);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(queryString);
            writer.flush();
            tryClose(writer);
            tryClose(os);
            conn.connect();

            InputStream is = conn.getInputStream();
            String result = stream2String(is);
            sendToClient(response, new GameResponse<String>(1,"OK",result));
            return true;
        } catch (Exception e) {
            logger.error("tid:{} wechart login failed",tid,e);
            e.printStackTrace();
            sendToClient(response, GameResponse.gameErrorResponse(e.getMessage(),null));
        }
        return false;
    }


    /**
     *
     * @param params
     * @return
     */
    private String paramValidate(Map<String, String[]> params) {
        if (!params.containsKey("channel")) {
            return "channel can't be null";
        }
        if (!params.containsKey("uapi_key")) {
            return "uapi key can't be null";
        }
        if(!params.containsKey("uapi_secret") ){
            return "uapi_secret can't be null";
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
