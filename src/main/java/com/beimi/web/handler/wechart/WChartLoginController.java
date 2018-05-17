package com.beimi.web.handler.wechart;

import com.alibaba.fastjson.JSONObject;
import com.beimi.backManager.WEChartUtil;
import com.beimi.core.BMDataContext;
import com.beimi.util.*;
import com.beimi.util.cache.CacheHelper;
import com.beimi.web.handler.Handler;
import com.beimi.web.model.*;
import com.beimi.web.service.repository.es.PlayUserClientESRepository;
import com.beimi.web.service.repository.es.PlayUserESRepository;
import com.beimi.web.service.repository.es.TokenESRepository;
import com.beimi.web.service.repository.jpa.*;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import sun.misc.BASE64Encoder;

import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.UUID;

/**
 * Created by zhengchenglei on 2018/3/15.
 */
@Controller
@RequestMapping("wechart")
public class WChartLoginController extends Handler{


    @Autowired
    private PlayUserClientRepository playUserClientRepository;
    @Autowired
    private PlayUserRepository playUserRes ;
    @Autowired
    private AnnouncementRespository announcementRespository;
    @Autowired
    private TokenRepository tokenRepository ;

    private String key = "LYJM#888888&6666$3333@END";


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 注册用户
     * @param player
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public PlayUserClient register(PlayUser player , IP ipdata , javax.servlet.http.HttpServletRequest request ) throws IllegalAccessException, InvocationTargetException{
        PlayUserClient playUserClient = GameUtils.create(player, ipdata, request) ;
        return playUserClient ;
    }

    @RequestMapping("/login")
    public ResponseEntity<ResultData> login(HttpServletRequest request ,
                                            String openId, String nickname, Integer sex, String avatar,String pwd) throws Exception {

        ResponseEntity<ResultData> responseEntity =  loginHandler(request,openId,nickname,sex,avatar,pwd);
        ResultData resultData = responseEntity.getBody();
        PlayUserClient playUserClient = null;
        if(resultData != null) {
            playUserClient = (PlayUserClient) resultData.getData();
            resultData.setData(WEChartUtil.clonePlayUserClient(playUserClient));
            CacheHelper.getApiUserCacheBean().put(playUserClient.getId(), playUserClient, playUserClient.getOrgi());
        }
        logger.info("登录返回数据 data:{}", JSONObject.toJSONString(responseEntity));
        return responseEntity;
    }


    @Transactional
    private ResponseEntity<ResultData> loginHandler(HttpServletRequest request ,
                              String openId, String nickname, Integer sex, String avatar,String pwd)throws Exception{
        PlayUserClient playUserClient = null;
        Token userToken = null;
        long tid = System.currentTimeMillis();
        logger.info("tid:{}微信用户开始登陆 openId:{},nickname:{},avatar:{},pwd:{}",tid,openId,nickname,avatar,pwd);

        if (StringUtils.isEmpty(openId)|| StringUtils.isEmpty(pwd)) {
            return new ResponseEntity(null, HttpStatus.FORBIDDEN);
        }

        String mymd5 = UKTools.md5One(openId+key).toUpperCase();
        if(!pwd.equals(mymd5)){
            logger.info("tid:{} 密码校验不通过 mymd5:{},pwd:{}",tid,mymd5,pwd);
            return new ResponseEntity(null, HttpStatus.FORBIDDEN);
        }

        playUserClient = playUserClientRepository.findById(openId);
        if (playUserClient != null) {
            playUserClient.setToken(playUserClient.getUsername() + "");
        }
        String ip = UKTools.getIpAddr(request);
        IP ipdata = IPTools.getInstance().findGeography(ip);
        if (playUserClient == null) {
            logger.info("tid:{}构造玩家用户",tid);
            // 构造玩家用户
            PlayUser playUser = new PlayUser();
            playUser.setId(openId);
            playUser.setOpenid(openId);
            playUser.setNickname(Base64Util.baseEncode(nickname));
            playUser.setUsercategory("1");
            playUser.setPlayerlevel("贫农");
            playUser.setPhoto(avatar);
            register(playUser, ipdata, request);
            playUserRes.save(playUser);
            playUser.setNickname(nickname);
            playUserClient = playUserClientRepository.findById(playUser.getId());
        }
        userToken = tokenRepository.findById(playUserClient.getUsername() + "");
        if (userToken == null) { //生成token
            logger.info("tid:{}生成token",tid);
            userToken = new Token();
            userToken.setIp(ip);
            userToken.setRegion(ipdata.getProvince() + ipdata.getCity());
            userToken.setId(UUID.randomUUID().toString().replaceAll("-", ""));
            userToken.setUserid(openId);
            userToken.setCreatetime(new Date());
            userToken.setOrgi(playUserClient.getOrgi());
            AccountConfig config = CacheConfigTools.getGameAccountConfig(BMDataContext.SYSTEM_ORGI);
            if (config != null && config.getExpdays() > 0) {
                userToken.setExptime(new Date(System.currentTimeMillis() + 60 * 60 * 24 * config.getExpdays() * 1000));//默认有效期 ， 7天
            } else {
                userToken.setExptime(new Date(System.currentTimeMillis() + 60 * 60 * 24 * 7 * 1000));//默认有效期 ， 7天
            }
            userToken.setLastlogintime(new Date());
            userToken.setUpdatetime(new Date(0));
            tokenRepository.save(userToken);//账号信息存入ES
        }
        playUserClient.setToken(userToken.getId()); // ApiUser中存放的是用户信息
        CacheHelper.getApiUserCacheBean().put(userToken.getId(), userToken, userToken.getOrgi());
        ResultData playerResultData = new ResultData(playUserClient != null, playUserClient != null ? MessageEnum.USER_REGISTER_SUCCESS : MessageEnum.USER_REGISTER_FAILD_USERNAME, playUserClient, userToken);
        GameConfig gameConfig = CacheConfigTools.getGameConfig(userToken.getOrgi()); // 获取游戏配置信息
        if (gameConfig != null) {
            playerResultData.setGametype(gameConfig.getGamemodel());
            playerResultData.setNoaiwaitime(gameConfig.getTimeout());    //无AI的时候 等待时长
            playerResultData.setNoaimsg(gameConfig.getTimeoutmsg());    //无AI的时候，到达最大时长以后的 提示消息，提示完毕后，解散房间

            playerResultData.setSubsidy(gameConfig.isSubsidy());        //是否启用了破产补助
            playerResultData.setSubtimes(gameConfig.getSubtimes());        //每天破产补助的次数
            playerResultData.setSubgolds(gameConfig.getSubgolds());        //每次破产补助的金额

            playerResultData.setSubmsg(gameConfig.getSubmsg());

            playerResultData.setRecmsg(gameConfig.getRecmsg());

            playerResultData.setLefttimes(gameConfig.getSubtimes());    //需要从数据库中查询当天剩余次数

            /**
             * 封装 游戏对象，发送到客户端
             */
            /**
             * 找到游戏配置的 模式 和玩法，如果多选，则默认进入的是 大厅模式，如果是单选，则进入的是选场模式
             */
            playerResultData.setGames(GameUtils.games(gameConfig.getGametype()));
        }
        AiConfig aiConfig = CacheConfigTools.getAiConfig(userToken.getOrgi());
        if (aiConfig != null) {
            playerResultData.setEnableai(aiConfig.isEnableai());
            playerResultData.setWaittime(aiConfig.getWaittime());
        }

        // 查询公告信息
        Announcement announcement = announcementRespository.findByType("1");
        if (announcement != null) {
            playerResultData.setAnnouncement(announcement.getContent());
        }

        /**
         * 根据游戏配置 ， 选择 返回的 玩法列表
         */
        playUserClientRepository.saveAndFlush(playUserClient);
        return new ResponseEntity<>(playerResultData, HttpStatus.OK);

    }


    public static void main(String[] args) {

        System.out.println(UKTools.md5One("wx7788992669ok1"+"LYJM#888888&6666$3333@END").toUpperCase());
    }


}
