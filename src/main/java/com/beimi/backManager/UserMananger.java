package com.beimi.backManager;

import com.beimi.util.Base64Util;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.cache.hazelcast.impl.PlayerCach;
import com.beimi.web.model.PlayUser;
import com.beimi.web.model.PlayUserClient;
import com.beimi.web.model.ProxyUser;
import com.beimi.web.service.repository.jpa.PlayUserRepository;
import com.beimi.web.service.repository.jpa.ProxyUserRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by zhengchenglei on 2018/4/5.
 */
@Controller
@RequestMapping("userManager")
public class UserMananger {


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PlayUserRepository playUserRepository;
    @Autowired
    private ProxyUserRepository proxyUserRepository;


    @ResponseBody
    @RequestMapping("/getAllUserInfo")
    public String getAllUserInfo(HttpServletRequest request, String token,String code, Integer startPage, Integer pageSize,String usercategory) {

        long tid = System.currentTimeMillis();
        logger.info("tid:{}用户访问数据信息 token:{},code:{},startPage:{},pageSize:{}", tid, token, code, startPage, pageSize);

        String openId = (String) request.getAttribute("openId");
        logger.info("tid:{}获取到OPENID 为 openId:{}", tid, openId);
        PageResponse pageResponse = null;
        PageRequest pageRequest = new PageRequest(startPage, pageSize);
        try {
            String checkResult = WEChartUtil.supperManagerValidate(request, proxyUserRepository);
            if (StringUtils.isNotEmpty(checkResult)) {
                return checkResult;
            }
            Page<PlayUser> playUsers = null;
            if ("2".equals(usercategory) || "3".equals(usercategory)) {
                playUsers = playUserRepository.findByUsercategory(usercategory, pageRequest);
            } else {
                playUsers = playUserRepository.findAll(pageRequest);
            }
            if (playUsers != null) {
                if(CollectionUtils.isNotEmpty(playUsers.getContent())) {
                    for (PlayUser playUser : playUsers) {
                        playUser.setNickname(Base64Util.baseDencode(playUser.getNickname()));
                        userDataHandler(playUser);
                    }
                }
                pageResponse = new PageResponse(playUsers.getNumber(), playUsers.getSize(),
                        playUsers.getTotalPages(), playUsers.getContent());
            }
            return new StandardResponse<PageResponse>(1, "OK", pageResponse).toJSON();
        } catch (Exception e) {
            logger.error("tid:{} 获取注册用户信息异常 startPage:{},pageSize:{}", e);
            return new StandardResponse<PageResponse>(-1, e.getMessage(), null).toJSON();
        }
    }

    private void userDataHandler(PlayUser playUser){
        if(playUser == null){
            return;
        }
        playUser.setBirthday(null);
        playUser.setCards(null);
        playUser.setBrowser(null);
        playUser.setUsertype(null);
        playUser.setProvince(null);
        playUser.setCountry(null);
        playUser.setDatastatus(null);
        playUser.setDiamonds(null);
        playUser.setDisabled(null);
        playUser.setEmail(null);
        playUser.setExperience(null);
        playUser.setFans(null);
        playUser.setFirstname(null);
        playUser.setFollows(null);
        playUser.setGender(null);
        playUser.setGoldcoins(null);
        playUser.setHeadimg(null);
        playUser.setIntegral(null);
        playUser.setIsp(null);
        playUser.setJobtitle(null);
        playUser.setLanguage(null);
        playUser.setUsertype(null);
        playUser.setUseragent(null);
        playUser.setRegion(null);
        playUser.setLogin(null);
        playUser.setUname(null);
        playUser.setSecureconf(null);
        playUser.setQqid(null);
        playUser.setPassword(null);
        playUser.setOrgi(null);
        playUser.setPassupdatetime(null);
        playUser.setMemo(null);
        playUser.setMidname(null);
        playUser.setMobile(null);
    }

    @ResponseBody
    @RequestMapping("getOnlineUser")
    public String getOnlineUser(){

        if(PlayerCach.hazelcastInstance.isEmpty()){
            return new StandardResponse<Integer>(1, "OK", 0).toJSON();
        }
        int onlineNum = 0;
        for(Map.Entry<String,Object> entry : PlayerCach.hazelcastInstance.entrySet()){
            if(entry.getValue() instanceof List){
                onlineNum = onlineNum + ((List)entry.getValue()).size();
            }
        }
        return new StandardResponse<Integer>(1, "OK", onlineNum).toJSON();
    }

    @ResponseBody
    @RequestMapping("addUserInfo")
    public String addUserInfo(HttpServletRequest request,String nickname,String photo){
        try {
            long tid = System.currentTimeMillis();
            ProxyUser proxyUser = WEChartUtil.addManagerUser(tid,proxyUserRepository, (String) request.getAttribute("openId"), nickname, photo);
            proxyUser.setNickname(Base64Util.baseEncode(proxyUser.getNickname()));
            return new StandardResponse(1,"OK",proxyUser).toJSON();
        }catch (Exception e){
            logger.info("用户登录异常",e);
            return new StandardResponse(-1,e.getMessage(),null).toJSON();
        }
    }


    /**
     * update user info
     * @param request
     * @param userId
     * @param usercategory
     * @return
     */
    @ResponseBody
    @RequestMapping("/updateUserCategory")
    public String updateUserCategory(HttpServletRequest request,String userId,String usercategory) {

        long tid = System.currentTimeMillis();
        try {
            String checkResult = WEChartUtil.supperManagerValidate(request,proxyUserRepository);
            if(StringUtils.isNotEmpty(checkResult)){
                return checkResult;
            }
            if (!"1".equals(usercategory) && !"2".equals(usercategory) && !"3".equals(usercategory)) {
                logger.error("tid:{} 修改用户信息失败 userId:{},userCategory:{}", userId, usercategory);
                return new StandardResponse<PageResponse>(-1, "category not exist", null).toJSON();
            }
            PlayUser playUser = playUserRepository.findById(userId);
            if (playUser == null) {
                return new StandardResponse<PageResponse>(-1, "user not exist", null).toJSON();
            }
            playUser.setUsercategory(usercategory);
            playUserRepository.saveAndFlush(playUser);
            return new StandardResponse(1, "OK", null).toJSON();
        } catch (Exception e) {
            logger.error("tid:{} 修改用户信息失败 userId:{},userCategory:{}", tid, userId, usercategory, e);
            return new StandardResponse(-1, "modify user info failed", null).toJSON();
        }
    }


    @ResponseBody
    @RequestMapping("queryProxyUser")
    public String queryProxyUser(HttpServletRequest request,Integer startPage,Integer pageSize){

        long tid = System.currentTimeMillis();
        PageResponse pageResponse = null;
        try {
            String checkResult = WEChartUtil.supperManagerValidate(request,proxyUserRepository);
            if(StringUtils.isNotEmpty(checkResult)){
                return checkResult;
            }
            PageRequest pageRequest = new PageRequest(startPage, pageSize);
            logger.info("tid:{} 开始查询代理用户信息 openId:{},startPage:{},pageSize:{}",tid,request.getParameter("openId"),startPage,pageSize);
            Page<ProxyUser> playUsers = proxyUserRepository.findByUserCategory("2",pageRequest);
            if (playUsers != null) {
                for (ProxyUser playUser : playUsers) {
                    playUser.setNickname(Base64Util.baseDencode(playUser.getNickname()));
                }
                pageResponse = new PageResponse(playUsers.getNumber(), playUsers.getSize(), playUsers.getTotalPages(), playUsers.getContent());
            }
        }catch (Exception e){
            logger.error("tid:{}查询代理用户异常",e);
        }
        return new StandardResponse<PageResponse>(1,"OK",pageResponse).toJSON();

    }

    @ResponseBody
    @RequestMapping("/queryProxyUserByUserId")
    public String queryProxyUserByUserId(HttpServletRequest request,Integer targetUserId){
        String checkResult = WEChartUtil.supperManagerValidate(request,proxyUserRepository);
        if(StringUtils.isNotEmpty(checkResult)){
            return checkResult;
        }
        ProxyUser proxyUser = proxyUserRepository.findByUserId(targetUserId);
        proxyUser.setNickname(Base64Util.baseDencode(proxyUser.getNickname()));
        return new StandardResponse<ProxyUser>(1,"OK",proxyUser).toJSON();
    }


    @ResponseBody
    @RequestMapping("/queryProxyUserByNickname")
    public String queryProxyUserByNickname(HttpServletRequest request,String nickname){
        String checkResult = WEChartUtil.supperManagerValidate(request,proxyUserRepository);
        if(StringUtils.isNotEmpty(checkResult)){
            return checkResult;
        }
        List<ProxyUser> proxyUsers = proxyUserRepository.findByNickname(nickname);
        if(proxyUsers != null){
            for (ProxyUser playUser : proxyUsers) {
                playUser.setNickname(Base64Util.baseDencode(playUser.getNickname()));
            }
        }
        return new StandardResponse<List<ProxyUser>>(1,"OK",proxyUsers).toJSON();
    }



    @ResponseBody
    @RequestMapping("/addProxyUser")
    public String addProxyUser(HttpServletRequest request,String targetOpenId,Integer targetUserId){

        long tid = System.currentTimeMillis();
        try {
            String checkResult = WEChartUtil.supperManagerValidate(request,proxyUserRepository);
            if(StringUtils.isNotEmpty(checkResult)){
                return checkResult;
            }
            logger.info("tid:{} 开始添加代理用户 openId:{}",tid,targetOpenId);
            ProxyUser proxyUser = proxyUserRepository.findByOpenId(targetOpenId);
            if(proxyUser == null){
                return new StandardResponse<ProxyUser>(-1,"proxy user not exist",null).toJSON();
            }
            if("2".equals(proxyUser.getUserCategory())){
                return new StandardResponse<ProxyUser>(-1,"current user is already proxy user",null).toJSON();
            }
            if("3".equals(proxyUser.getUserCategory())){
                return new StandardResponse<ProxyUser>(-1,"current user is already manager user",null).toJSON();
            }
            if(!"1".equals(proxyUser.getUserCategory())){
                return new StandardResponse<ProxyUser>(-1,"unknown user role",null).toJSON();
            }
            proxyUser.setUserCategory("2");
            proxyUser.setUserId(targetUserId);
            proxyUserRepository.saveAndFlush(proxyUser);
            return new StandardResponse<ProxyUser>(1,"OK",null).toJSON();
        }catch (Exception e){
            logger.error("tid:{}加代理用户异常",tid,e);
            return new StandardResponse<ProxyUser>(-1,"add proxy user failure",null).toJSON();
        }

    }


    @ResponseBody
    @RequestMapping("/delProxyUser")
    public String delProxyUser(HttpServletRequest request,String targetOpenId){
        long tid = System.currentTimeMillis();
        try {
            String checkResult = WEChartUtil.supperManagerValidate(request,proxyUserRepository);
            if(StringUtils.isNotEmpty(checkResult)){
                return checkResult;
            }
            logger.info("tid:{} 开始删除代理用户 openId:{}",tid,targetOpenId);
            ProxyUser proxyUser = proxyUserRepository.findByOpenId(targetOpenId);
            if(proxyUser == null){
                return new StandardResponse<ProxyUser>(-1,"proxy user not exist",null).toJSON();
            }
            proxyUser.setUserCategory("1");
            proxyUserRepository.saveAndFlush(proxyUser);
            return new StandardResponse<ProxyUser>(1,"OK",null).toJSON();
        }catch (Exception e){
            logger.error("tid:{}开始删除代理异常",tid,e);
            return new StandardResponse<ProxyUser>(-1,"add proxy user failure",null).toJSON();
        }

    }





}
