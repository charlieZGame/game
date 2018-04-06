package com.beimi.backManager;

import com.alibaba.fastjson.JSONObject;
import com.beimi.web.model.ConsumeCard;
import com.beimi.web.model.DealFlow;
import com.beimi.web.model.PlayUser;
import com.beimi.web.service.repository.jpa.ConsumeCardRepository;
import com.beimi.web.service.repository.jpa.DealFlowRepository;
import com.beimi.web.service.repository.jpa.PlayUserRepository;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
/**
 * Created by zhengchenglei on 2018/4/6.
 */
@Controller
@RequestMapping("houseCard")
public class HouseCardManager {


    @Autowired
    private ConsumeCardRepository consumeCardRepository;

    @Autowired
    private DealFlowRepository dealFlowRepository;
    @Autowired
    private PlayUserRepository playUserRepository;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @ResponseBody
    @RequestMapping("/getCardInfo")
    public String getCardInfo(Integer startPage,Integer pageSize){

        long tid = System.currentTimeMillis();
        try {
            PageRequest pageRequest = new PageRequest(startPage, pageSize);
            Page<ConsumeCard> consumeCards = consumeCardRepository.findAll(pageRequest);
            PageResponse pageResponse = new PageResponse(consumeCards.getNumber(),consumeCards.getSize(),
                    consumeCards.getTotalPages(),consumeCards.getContent());
            return new StandardResponse<PageResponse>(1,"OK",pageResponse).toJSON();
        }catch (Exception e){
            logger.error("tid:{} 查询房卡配置信息异常 startPage:{},pageSize:{}",tid,startPage,pageSize,e);
            return new StandardResponse<PageResponse>(-1,e.getMessage(),null).toJSON();
        }
    }


    @ResponseBody
    @RequestMapping("updateHouseCardInfo")
    public String updateHouseCardInfo(HttpServletRequest request,String id,Integer totalCardsNum){

        if(StringUtils.isEmpty(id) || totalCardsNum == null){
            return new StandardResponse<PageResponse>(1,"id or total cards null illegal",null).toJSON();
        }
        ConsumeCard consumeCard = consumeCardRepository.findByType("fangka");
        if(consumeCard == null){
            return new StandardResponse<PageResponse>(-1,"house cards is null",null).toJSON();
        }

        if(consumeCard.getTotalNum() > totalCardsNum){
            int offsetCards = consumeCard.getTotalNum() - totalCardsNum;
            if(consumeCard.getEffectiveNum() < offsetCards){
                return new StandardResponse<PageResponse>(-1,"剩余房卡小于需要减少的房卡数量",null).toJSON();
            }else{
                consumeCard.setEffectiveNum(consumeCard.getEffectiveNum() - offsetCards);
                consumeCard.setTotalNum(totalCardsNum);
                DealFlow dealFlow = new DealFlow();
                dealFlow.setConsumeCardId(consumeCard.getId());
                //// TODO: 2018/4/6  需要添加用户信息
                dealFlow.setCreatePin("");
                dealFlow.setNum(consumeCard.getTotalNum() - totalCardsNum);
                dealFlow.setSrcType("总房卡操作减少");
                dealFlow.setOpenId("");
                dealFlow.setCreateTime(new Date());
                dealFlow.setUserName("");
                dealFlow.setXybj("1");
                dealFlow.setYxbj("1");
                dealFlowRepository.save(dealFlow);
            }
        }else{
            int offset = totalCardsNum - consumeCard.getTotalNum();
            consumeCard.setEffectiveNum(consumeCard.getEffectiveNum()+offset);
            consumeCard.setTotalNum(totalCardsNum);
            DealFlow dealFlow = new DealFlow();
            dealFlow.setConsumeCardId(consumeCard.getId());
            //// TODO: 2018/4/6  需要添加用户信息
            dealFlow.setCreatePin("");
            dealFlow.setNum(consumeCard.getTotalNum() - totalCardsNum);
            dealFlow.setSrcType("总房卡操作增加");
            dealFlow.setOpenId("");
            dealFlow.setCreateTime(new Date());
            dealFlow.setUserName("");
            dealFlow.setXybj("1");
            dealFlow.setYxbj("1");
            dealFlowRepository.save(dealFlow);
        }

        return null;
    }


    @ResponseBody
    @RequestMapping("/recharge")
    @Transactional
    public String recharge(HttpServletRequest request,String code,Integer num,Integer targetUserId){

        try {
            PlayUser playUser = playUserRepository.findByUsername(1);
            PlayUser targetPlayUser = playUserRepository.findByUsername(targetUserId);
            if (playUser == null || targetPlayUser == null) {
                return new StandardResponse(-1, "can't find user", null).toJSON();
            }
            if (playUser.getCards() < num) {
                return new StandardResponse(-1, "cards number don't enough", null).toJSON();
            }
            playUser.setCards(playUser.getCards() - num);
            targetPlayUser.setCards(targetPlayUser.getCards() + num);

            playUserRepository.saveAndFlush(playUser);
            playUserRepository.saveAndFlush(targetPlayUser);
        }catch (Exception e){
            logger.info("房卡充值异常 code:{},num:{},targetUserId:{}",code,num,targetUserId);
            return new StandardResponse(-1, e.getMessage(), null).toJSON();
        }
        return new StandardResponse(1,"OK",null).toJSON();

    }







}
