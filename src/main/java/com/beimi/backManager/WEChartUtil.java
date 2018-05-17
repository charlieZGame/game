package com.beimi.backManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beimi.util.Base64Util;
import com.beimi.web.model.DealFlow;
import com.beimi.web.model.PlayUserClient;
import com.beimi.web.model.ProxyUser;
import com.beimi.web.service.repository.jpa.DealFlowRepository;
import com.beimi.web.service.repository.jpa.ProxyUserRepository;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Created by zhengchenglei on 2018/4/7.
 */
public class WEChartUtil {

    private static Logger logger = LoggerFactory.getLogger(WEChartUtil.class);

   private static String appId="wx7ea232488bb78296";
   private static String appSecret="b5d1af889096d3060d213b0aaf906351";
   private static String grantType="authorization_code";


    public static String getSessionKeyOrOpenid(long tid,HttpServletRequest request,String codeInfo) throws IOException {

        if(request == null && StringUtils.isEmpty(codeInfo)){
            return null;
        }
        String code = StringUtils.isEmpty(codeInfo) ? request.getHeader("accesstoken") : codeInfo;
        logger.info("获取OPENID应用信息 code:{}",code);
     //   Locale locale = new Locale("en", "US");
       // Properties resource = new Properties();
       // resource.load(WEChartUtil.class.getResourceAsStream("config/appInfo.properties"));
        String requestUrl = "https://api.weixin.qq.com/sns/jscode2session";  //请求地址 https://api.weixin.qq.com/sns/jscode2session
        Map<String, String> requestUrlParam = new HashMap<String, String>();
        //logger.info("获取OPENID应用信息 appId:{},appSecret:{}", resource.getProperty("appId"),resource.getProperty("appSecret"));
        requestUrlParam.put("appid", appId);  //开发者设置中的appId
        requestUrlParam.put("secret", appSecret); //开发者设置中的appSecret
        requestUrlParam.put("js_code", code); //小程序调用wx.login返回的code
        requestUrlParam.put("grant_type", grantType);    //默认参数 authorization_code
        //发送post请求读取调用微信 https://api.weixin.qq.com/sns/jscode2session 接口获取openid用户唯一标识
        String response = sendPost(requestUrl, requestUrlParam);
        logger.info("tid:{} 返回数据为 response:{}",tid,response);
        JSONObject jsonObject = JSON.parseObject(response);
        String openId = (String)jsonObject.get("openid");
        logger.info("tid:{} 返回数据为 openId:{}",tid,openId);
        return openId;

    }

    public static void main(String[] args) {
        InputStream in = WEChartUtil.class.getResourceAsStream("config/appInfo.properties");
        System.out.println(in);
    }


    public static String supperManagerValidate(HttpServletRequest request, ProxyUserRepository proxyUserRepository){

        if(request == null){
            return new StandardResponse<PageResponse>(-2,"no permission",null).toJSON();
        }
        String openId = (String)request.getAttribute("openId");
        if(StringUtils.isEmpty(openId)){
            return new StandardResponse<PageResponse>(-2,"no permission",null).toJSON();
        }
        ProxyUser proxyUser = proxyUserRepository.findByOpenId(openId);
        if(proxyUser == null || !"3".equals(proxyUser.getUserCategory())){
            return new StandardResponse<PageResponse>(-2,"no permission",null).toJSON();
        }

        return null;
    }

    public static String supperProxyManagerValidate(HttpServletRequest request, ProxyUserRepository proxyUserRepository){

        if(request == null){
            return new StandardResponse<PageResponse>(-2,"no permission",null).toJSON();
        }
        String openId = (String)request.getAttribute("openId");
        if(StringUtils.isEmpty(openId)){
            return new StandardResponse<PageResponse>(-2,"no permission",null).toJSON();
        }
        ProxyUser proxyUser = proxyUserRepository.findByOpenId(openId);
        if(proxyUser == null || (!"3".equals(proxyUser.getUserCategory()) && !"2".equals(proxyUser.getUserCategory()))){
            return new StandardResponse<PageResponse>(-2,"no permission",null).toJSON();
        }

        return null;
    }

    public static ProxyUser addManagerUser(long tid,ProxyUserRepository repository,String openId,String nickname,String photo){

        if(repository == null || StringUtils.isEmpty(openId)){
            return null;
        }
        logger.info("tid:{} 新增用户查询openId为 openId:{}",tid,openId);
        ProxyUser proxyUser = repository.findByOpenId(openId);
        if(proxyUser == null){
            ProxyUser user = new ProxyUser();
            user.setId(openId);
            user.setOpenId(openId);
            user.setNickname(Base64Util.baseEncode(nickname));
            user.setPhoto(photo);
            user.setYxbj("1");
            user.setUserCategory("1");
            user.setCreateTime(new Date());
            repository.save(user);
            proxyUser = user;
        }else{
            proxyUser.setNickname(Base64Util.baseEncode(nickname));
            repository.saveAndFlush(proxyUser);
        }
        return proxyUser;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url 发送请求的 URL
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, Map<String, ?> paramMap) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";

        String param = "";
        Iterator<String> it = paramMap.keySet().iterator();

        while (it.hasNext()) {
            String key = it.next();
            param += key + "=" + paramMap.get(key) + "&";
        }

        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取UzRLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }


    public static void addDealflow(DealFlowRepository repository,String username, Integer num, String type, String userId,String nickName,String createPin){
        DealFlow dealFlow = new DealFlow();
        dealFlow.setUserName(username);
        dealFlow.setNum(num);
        dealFlow.setCreateTime(new Date());
        dealFlow.setYxbj("1");
        dealFlow.setXybj("1");
        dealFlow.setUserId(userId);
        dealFlow.setOpenId(userId);
        dealFlow.setSrcType(type);
        dealFlow.setCreatePin(Base64Util.baseEncode(nickName));
        dealFlow.setHandlerUserId(createPin);
        repository.save(dealFlow);

    }


    public static PlayUserClient clonePlayUserClient(PlayUserClient playUserClient){
        if(playUserClient == null){
            return playUserClient;
        }
        PlayUserClient temp = new PlayUserClient();
        temp.setId(playUserClient.getId());
        temp.setNickname(Base64Util.baseDencode(playUserClient.getNickname()));
        temp.setGoldcoins(playUserClient.getGoldcoins());
        temp.setOrgi(playUserClient.getOrgi());
        temp.setOpenid(playUserClient.getOpenid());
        temp.setPhoto(playUserClient.getPhoto());
        temp.setRoomid(playUserClient.getRoomid());
        temp.setUsername(playUserClient.getUsername());
        temp.setToken(playUserClient.getToken());
        temp.setPlayerlevel(playUserClient.getPlayerlevel());
        temp.setCards(playUserClient.getCards());
        temp.setUsercategory(playUserClient.getUsercategory());
        return temp;
    }



}
