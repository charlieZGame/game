package com.beimi.backManager;

import com.alibaba.fastjson.JSONObject;
import com.beimi.web.model.ManagerUser;
import com.beimi.web.model.PlayUser;
import com.beimi.web.service.repository.jpa.PlayUserRepository;
import com.beimi.web.service.repository.jpa.ProxyUserRepository;
import com.beimi.web.service.repository.jpa.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

        PageResponse pageResponse = null;
        PageRequest pageRequest = new PageRequest(startPage, pageSize);
        try {
            if ("2".equals(usercategory)) {
                Page<ManagerUser> managerUsers = proxyUserRepository.findByUserCategory("2",pageRequest);
                if(managerUsers != null){
                    pageResponse = new PageResponse(managerUsers.getNumber(),managerUsers.getSize(),managerUsers.getTotalPages(),managerUsers.getContent());
                }
            }else if("3".equals(usercategory)) {
                Page<PlayUser> playUsers = playUserRepository.findByUsercategory(usercategory, pageRequest);
                if (playUsers != null) {
                    pageResponse = new PageResponse(playUsers.getNumber(), playUsers.getSize(), playUsers.getTotalPages(), playUsers.getContent());
                }
            } else {
                Page<PlayUser> playUsers = playUserRepository.findAll(pageRequest);
                pageResponse = new PageResponse(playUsers.getNumber(), playUsers.getSize(),
                        playUsers.getTotalPages(), playUsers.getContent());
            }
            if (pageResponse == null) {
                return new StandardResponse<PageResponse>(1, "OK", null).toJSON();
            } else {
                return new StandardResponse<PageResponse>(1, "OK", pageResponse).toJSON();
            }
        } catch (Exception e) {
            logger.error("tid:{} 获取注册用户信息异常 startPage:{},pageSize:{}", e);
            return new StandardResponse<PageResponse>(-1, e.getMessage(), null).toJSON();
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
    public String updateUserCategory(HttpServletRequest request,String userId,String usercategory){

        long tid = System.currentTimeMillis();
        try {
            if (!"1".equals(usercategory) && !"2".equals(usercategory)&& !"3".equals(usercategory)) {
                logger.error("tid:{} 修改用户信息失败 userId:{},userCategory:{}", userId, usercategory);
                return new StandardResponse<PageResponse>(-1, "category not exist", null).toJSON();
            }
            PlayUser playUser = playUserRepository.findById(userId);
            if(playUser == null){
                return new StandardResponse<PageResponse>(-1, "user not exist", null).toJSON();
            }
            playUser.setUsercategory(usercategory);
            playUserRepository.saveAndFlush(playUser);
            return new StandardResponse(1,"OK",null).toJSON();
        }catch (Exception e){
            logger.error("tid:{} 修改用户信息失败 userId:{},userCategory:{}",tid,userId,usercategory,e);
            return new StandardResponse(-1,"modify user info failed",null).toJSON();
        }
    }



}
