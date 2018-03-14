package com.beimi.wlogin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping({"/wlogin"})
public class WLogin
{
    private String loginCheckUrl = "http://oauth.anysdk.com/api/User/LoginOauth/";

    private int connectTimeOut = 30000;

    private int timeOut = 30000;
    private static final String userAgent = "px v1.0";

    @ResponseBody
    @RequestMapping({"check"})
    public boolean check(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            Map params = request.getParameterMap();

            if (parametersIsset(params)) {
                sendToClient(response, "parameter not complete");
                return false;
            }

            String queryString = getQueryString(request);

            URL url = new URL(this.loginCheckUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
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
            sendToClient(response, result);
            return true;
        } catch (Exception e) {
            e.printStackTrace();

            sendToClient(response, "Unknown error!");
        }return false;
    }

    public void setLoginCheckUrl(String loginCheckUrl)
    {
        this.loginCheckUrl = loginCheckUrl;
    }

    public void setConnectTimeOut(int connectTimeOut)
    {
        this.connectTimeOut = connectTimeOut;
    }

    public void setTimeOut(int timeOut)
    {
        this.timeOut = timeOut;
    }

    private boolean parametersIsset(Map<String, String[]> params)
    {
        return (!params.containsKey("channel")) || (!params.containsKey("uapi_key")) ||
                (!params
                        .containsKey("uapi_secret"));
    }

    private String getQueryString(HttpServletRequest request)
    {
        Map params = request.getParameterMap();
        String queryString = "";
        for (Object key : params.keySet()) {
            String[] values = (String[])params.get(key);
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                queryString = new StringBuilder().append(queryString).append(key).append("=").append(value).append("&").toString();
            }
        }
        queryString = queryString.substring(0, queryString.length() - 1);
        return queryString;
    }

    private String stream2String(InputStream is)
    {
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

    private void sendToClient(HttpServletResponse response, String content)
    {
        response.setContentType("text/plain;charset=utf-8");
        try {
            PrintWriter writer = response.getWriter();
            writer.write(content);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryClose(OutputStream os)
    {
        try
        {
            if (null != os) {
                os.close();
                os = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryClose(Writer writer)
    {
        try
        {
            if (null != writer) {
                writer.close();
                writer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryClose(Reader reader)
    {
        try
        {
            if (null != reader) {
                reader.close();
                reader = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}