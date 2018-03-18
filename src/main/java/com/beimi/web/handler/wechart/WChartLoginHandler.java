package com.beimi.web.handler.wechart;

import com.alibaba.fastjson.JSONObject;
import com.beimi.core.BMDataContext;
import com.beimi.util.*;
import com.beimi.util.cache.CacheHelper;
import com.beimi.web.handler.Handler;
import com.beimi.web.model.*;
import com.beimi.web.service.repository.es.PlayUserClientESRepository;
import com.beimi.web.service.repository.es.PlayUserESRepository;
import com.beimi.web.service.repository.es.TokenESRepository;
import com.beimi.web.service.repository.jpa.PlayUserClientRepository;
import com.beimi.web.service.repository.jpa.PlayUserRepository;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Map;

/**
 * Created by zhengchenglei on 2018/3/15.
 */
@Service
public class WChartLoginHandler {

    @Autowired
    private PlayUserESRepository playUserESRes;

    @Autowired
    private PlayUserClientESRepository playUserClientRes ;

    @Autowired
    private PlayUserRepository playUserRes ;

    @Autowired
    private TokenESRepository tokenESRes ;

    private String access_token_url="https://api.weixin.qq.com/sns/oauth2/access_token?appid=wxc46e199f49e7a958&secret=2232e4705928a1a948910637a8815c9c&code=#CODE#&grant_type=authorization_code";

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private PlayUserClientRepository playUserClientRepository;

    public String getOpenId(String appId, String appSecret, String code, Handler handler) throws IOException {

        String accessTokenUrl="https://api.weixin.qq.com/sns/oauth2/access_token?appid="+appId+"&secret="+appSecret+"&code="+code+"&grant_type=authorization_code";
        String response = CommonUtil.getInformationFromInternet(accessTokenUrl);
        logger.debug("=====获取OPENID信息为["+response+"]");
        Map<String,Object> acessjsonobj = JSONObject.parseObject(response, Map.class);
        logger.info("acessjsonobj:{}",acessjsonobj);
        //JSONObject acessjsonobj = new JSONObject(response);
        //logger.debug("====获取用户信息链接信息["+BlhrConf.getInstance().getPull_userinfo_url().
                //replace(Constant.ACCESS_TOKEN_TAG, acessjsonobj.getString(Constant.ACCESS_TOKEN)).replace(Constant.OPEN_ID_TAG, acessjsonobj.getString(Constant.OPENID))+"]");
       String userInfoUrl ="https://api.weixin.qq.com/sns/userinfo?access_token="+acessjsonobj.get(Constant.ACCESS_TOKEN)+"&openid="+acessjsonobj.get(Constant.OPENID)+"&lang=zh_CN";

        String userInfoStr = CommonUtil.getInformationFromInternet(userInfoUrl);
        logger.debug("=========获取用户信息为["+userInfoStr+"]");

        Map<String,Object> userInfo = JSONObject.parseObject(userInfoStr,Map.class);
        logger.info("openId:{}",userInfo.get(Constant.OPENID));
        logger.info("NICKNAME:{}",userInfo.get(Constant.NICKNAME));
        logger.info("HEADIMGURL:{}",userInfo.get(Constant.HEADIMGURL));
        logger.info("SEX:{}",userInfo.get(Constant.SEX));
        return (String)acessjsonobj.get(Constant.OPENID);

    }


    public String getOpenIdByServer() throws IOException {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx499ae0ab362914d1&secret=d58672de73a7ad80ea98213f2e8ebcbc";
        String returnStr  = CommonUtil.httpsRequest(url,"GET",null);
        logger.info("返回服务器相应消息 message:{}",returnStr);

        logger.info("access_token:{}",JSONObject.parseObject(returnStr,Map.class).get("access_token"));

        String openId = "https://api.weixin.qq.com/cgi-bin/user/info/batchget?access_token="+JSONObject.parseObject(returnStr,Map.class).get("access_token");
        String openIdStr  = CommonUtil.httpsRequest(openId,"GET",null);
        logger.info("返回服务器相应消息openIdStr message:{}",returnStr);



        String accessTokenUrl="https://api.weixin.qq.com/sns/oauth2/access_token?appid=wx499ae0ab362914d1&secret=d58672de73a7ad80ea98213f2e8ebcbc&code=&grant_type=authorization_code";
        String response = CommonUtil.getInformationFromInternet(accessTokenUrl);
        logger.debug("=====获取OPENID信息为["+response+"]");

        Map<String,Object> acessjsonobj = JSONObject.parseObject(response, Map.class);
        logger.info("acessjsonobj:{}",acessjsonobj);
        //JSONObject acessjsonobj = new JSONObject(response);
        //logger.debug("====获取用户信息链接信息["+BlhrConf.getInstance().getPull_userinfo_url().
        //replace(Constant.ACCESS_TOKEN_TAG, acessjsonobj.getString(Constant.ACCESS_TOKEN)).replace(Constant.OPEN_ID_TAG, acessjsonobj.getString(Constant.OPENID))+"]");
        String userInfoUrl ="https://api.weixin.qq.com/sns/userinfo?access_token="+acessjsonobj.get(Constant.ACCESS_TOKEN)+"&openid="+acessjsonobj.get(Constant.OPENID)+"&lang=zh_CN";

        String userInfoStr = CommonUtil.getInformationFromInternet(userInfoUrl);
        logger.debug("=========获取用户信息为["+userInfoStr+"]");

        Map<String,Object> userInfo = JSONObject.parseObject(userInfoStr,Map.class);
        logger.info("openId:{}",userInfo.get(Constant.OPENID));
        logger.info("NICKNAME:{}",userInfo.get(Constant.NICKNAME));
        logger.info("HEADIMGURL:{}",userInfo.get(Constant.HEADIMGURL));
        logger.info("SEX:{}",userInfo.get(Constant.SEX));
        return (String)acessjsonobj.get(Constant.OPENID);

    }


    public ResultData getUserInfo(String openId, javax.servlet.http.HttpServletRequest request){
        PlayUserClient playUserClient = null ;
        String ip = UKTools.getIpAddr(request);
        IP ipdata = IPTools.getInstance().findGeography(ip);
        if(playUserClient == null){
            try {// 构造玩家用户
                PlayUser playUser = new PlayUser();
                playUser.setId(openId);
                playUser.setOpenid(openId);
                playUserClient = register(playUser , ipdata , request) ;
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        Token userToken = new Token();
        userToken.setIp(ip);
        userToken.setRegion(ipdata.getProvince()+ipdata.getCity());
        userToken.setId(UKTools.getUUID());
        userToken.setUserid(playUserClient.getId());
        userToken.setCreatetime(new Date());
        userToken.setOrgi(playUserClient.getOrgi());
        AccountConfig config = CacheConfigTools.getGameAccountConfig(BMDataContext.SYSTEM_ORGI) ;
        if(config!=null && config.getExpdays() > 0){
            userToken.setExptime(new Date(System.currentTimeMillis()+60*60*24*config.getExpdays()*1000));//默认有效期 ， 7天
        }else{
            userToken.setExptime(new Date(System.currentTimeMillis()+60*60*24*7*1000));//默认有效期 ， 7天
        }
        userToken.setLastlogintime(new Date());
        userToken.setUpdatetime(new Date(0));
        tokenESRes.save(userToken) ;//账号信息存入ES
        playUserClient.setToken(userToken.getId()); // ApiUser中存放的是用户信息
        CacheHelper.getApiUserCacheBean().put(userToken.getId(),userToken, userToken.getOrgi());
        CacheHelper.getApiUserCacheBean().put(playUserClient.getId(),playUserClient, userToken.getOrgi());
        ResultData playerResultData = new ResultData( playUserClient!=null , playUserClient != null ? MessageEnum.USER_REGISTER_SUCCESS: MessageEnum.USER_REGISTER_FAILD_USERNAME , playUserClient , userToken) ;
        GameConfig gameConfig = CacheConfigTools.getGameConfig(userToken.getOrgi()) ; // 获取游戏配置信息
        if(gameConfig!=null){
            playerResultData.setGametype(gameConfig.getGamemodel());
            playerResultData.setNoaiwaitime(gameConfig.getTimeout());	//无AI的时候 等待时长
            playerResultData.setNoaimsg(gameConfig.getTimeoutmsg());    //无AI的时候，到达最大时长以后的 提示消息，提示完毕后，解散房间
            playerResultData.setSubsidy(gameConfig.isSubsidy());		//是否启用了破产补助
            playerResultData.setSubtimes(gameConfig.getSubtimes());		//每天破产补助的次数
            playerResultData.setSubgolds(gameConfig.getSubgolds());		//每次破产补助的金额
            playerResultData.setSubmsg(gameConfig.getSubmsg());
            playerResultData.setRecmsg(gameConfig.getRecmsg());
            playerResultData.setLefttimes(gameConfig.getSubtimes());	//需要从数据库中查询当天剩余次数

            /**
             * 封装 游戏对象，发送到客户端
             */
            /**
             * 找到游戏配置的 模式 和玩法，如果多选，则默认进入的是 大厅模式，如果是单选，则进入的是选场模式
             */
            playerResultData.setGames(GameUtils.games(gameConfig.getGametype()));
        }
        AiConfig aiConfig = CacheConfigTools.getAiConfig(userToken.getOrgi()) ;
        if(aiConfig!=null){
            playerResultData.setEnableai(aiConfig.isEnableai());
            playerResultData.setWaittime(aiConfig.getWaittime());
        }
        /**
         * 根据游戏配置 ， 选择 返回的 玩法列表
         */
        return playerResultData;
    }



    /**
     * 注册用户
     * @param player
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public PlayUserClient register(PlayUser player , IP ipdata , javax.servlet.http.HttpServletRequest request ) throws IllegalAccessException, InvocationTargetException{
        PlayUserClient playUserClient = GameUtils.create(player, ipdata, request) ;
        int users = playUserESRes.countByUsername(player.getUsername()) ;
        if(users == 0){
            UKTools.published(player , playUserESRes , playUserRes);
        }
        return playUserClient ;
    }



}
