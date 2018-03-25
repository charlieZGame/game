package com.beimi.config.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * AnySDK 支付通知验签算法
 *
 * @author libo<libo@chukong-inc.com>
 * @date 2014-08-13
 */
@Controller
@RequestMapping("/pay")
public class PayNotifyController {

    private String loginCheckUrl = "http://oauth.anysdk.com/api/User/LoginOauth/";
    /**
     * connect time out
     *
     * @var int
     */
    private int connectTimeOut = 30 * 1000;

    /**
     * time out second
     *
     * @var int
     */
    private int timeOut = 30 * 1000;

    /**
     * user agent
     *
     * @var string
     */
    private static final String userAgent = "px v1.0";



    @ResponseBody
    @RequestMapping(value = "/check")
    public String check(HttpServletRequest request, HttpServletResponse response){

        System.out.println("调入系统");

        return "ok";

    }


    @RequestMapping(value = "/validate")
    public void validate(HttpServletRequest request, HttpServletResponse response) {

        System.out.println("调入系统2222");

        try {
            URL url = new URL(loginCheckUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setReadTimeout(timeOut);
            conn.setConnectTimeout(connectTimeOut);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write("ok");
            writer.flush();
            tryClose(writer);
            tryClose(os);
            conn.connect();

            InputStream is = conn.getInputStream();
            String result = stream2String(is);
            System.out.println("result22222===" + result);
            sendToClient(response, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendToClient(response, "Unknown error!");

    }



    public void setLoginCheckUrl(String loginCheckUrl) {
        this.loginCheckUrl = loginCheckUrl;
    }

    /**
     * 设置连接超时
     *
     * @param connectTimeOut
     */
    public void setConnectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    /**
     * 设置超时时间
     *
     * @param timeOut
     */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }


    /**
     * check needed parameters isset 检查必须的参数 channel
     * uapi_key：渠道提供给应用的app_id或app_key（标识应用的id）
     * uapi_secret：渠道提供给应用的app_key或app_secret（支付签名使用的密钥）
     *
     * @param params
     * @return boolean
     */
    private boolean parametersIsset(Map<String, String[]> params) {
        return !(params.containsKey("channel") && params.containsKey("uapi_key")
                && params.containsKey("uapi_secret"));
    }

    /**
     * 获取查询字符串
     *
     * @param request
     * @return
     */
    private String getQueryString(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        String queryString = "";
        for (String key : params.keySet()) {
            String[] values = params.get(key);
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                queryString += key + "=" + value + "&";
            }
        }
        queryString = queryString.substring(0, queryString.length() - 1);
        return queryString;
    }

    /**
     * 获取流中的字符串
     *
     * @param is
     * @return
     */
    private String stream2String(InputStream is) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new java.io.InputStreamReader(is));
            String line = "";
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tryClose(br);
        }
        return "";
    }

    /**
     * 向客户端应答结果
     *
     * @param response
     * @param content
     */
    private void sendToClient(HttpServletResponse response, String content) {
        response.setContentType("text/plain;charset=utf-8");
        try {
            PrintWriter writer = response.getWriter();
            writer.write(content);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭输出流
     *
     * @param os
     */
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

    /**
     * 关闭writer
     *
     * @param writer
     */
    private void tryClose(java.io.Writer writer) {
        try {
            if (null != writer) {
                writer.close();
                writer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭Reader
     *
     * @param reader
     */
    private void tryClose(java.io.Reader reader) {
        try {
            if (null != reader) {
                reader.close();
                reader = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}