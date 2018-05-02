package com.beimi.backManager;

import com.alibaba.fastjson.JSONObject;
import com.beimi.util.Base64Util;
import com.beimi.web.model.*;
import com.beimi.web.service.repository.jpa.ConsumeCardRepository;
import com.beimi.web.service.repository.jpa.DealFlowRepository;
import com.beimi.web.service.repository.jpa.PlayUserRepository;
import com.beimi.web.service.repository.jpa.ProxyUserRepository;
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    @Autowired
    private ProxyUserRepository proxyUserRepository;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @ResponseBody
    @RequestMapping("/getCardInfo")
    public String getCardInfo(HttpServletRequest request,Integer startPage,Integer pageSize){

        long tid = System.currentTimeMillis();
        try {
            String checkResult = WEChartUtil.supperManagerValidate(request,proxyUserRepository);
            if(StringUtils.isNotEmpty(checkResult)){
                return checkResult;
            }
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
    @RequestMapping("/getCardInfoAnalysis")
    public String getCardInfoAnalysis(String type,String startTime,String endTime){

        long tid = System.currentTimeMillis();
        try {
           /* String checkResult = WEChartUtil.supperManagerValidate(request,proxyUserRepository);
            if(StringUtils.isNotEmpty(checkResult)){
                return checkResult;
            }*/
            List<Object> response = null;
            if("1".equals(type)) {
                response = dealFlowRepository.findByMonthRange(startTime,endTime);
            }else if("2".equals(type)){
                response = dealFlowRepository.findByDayRange(startTime,endTime);
            }else{
                return new StandardResponse<String>(1,"OK","undefined type").toJSON();
            }
            CardsAnalysis cardsanalysis = new CardsAnalysis();
            cardsanalysis.setType(type);
            List<CardsAnalysis.CardInfo> cardInfos = new ArrayList<CardsAnalysis.CardInfo>();
            for(Object obj : response){
                Object[] objects = (Object[]) obj;
                CardsAnalysis.CardInfo cardInfo = cardsanalysis.new CardInfo();
                cardInfo.setData(objects[0] == null ? null: (String)objects[0]);
                cardInfo.setNumber(objects[1] == null ? 0 : (objects[1] instanceof BigDecimal ? ((BigDecimal) objects[1]).intValue() : (Integer) objects[1]));
                cardInfos.add(cardInfo);
            }
            cardsanalysis.setCardInfo(cardInfos);

            return new StandardResponse<CardsAnalysis>(1,"OK",cardsanalysis).toJSON();
        }catch (Exception e){
            logger.error("tid:{} 查询房卡配置信息异常 ",tid,e);
            return new StandardResponse<PageResponse>(-1,e.getMessage(),null).toJSON();
        }
    }




    @ResponseBody
    @RequestMapping("updateHouseCardInfo")
    public String updateHouseCardInfo(HttpServletRequest request,Integer totalCardsNum){

        String checkResult = WEChartUtil.supperManagerValidate(request,proxyUserRepository);
        if(StringUtils.isNotEmpty(checkResult)){
            return checkResult;
        }
        if(totalCardsNum == null){
            return new StandardResponse<PageResponse>(1,"total cards null illegal",null).toJSON();
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
    @Transactional
    @RequestMapping("/recharge")
    public String recharge(HttpServletRequest request,Integer num,Integer targetUserId){

        try {
            String checkResult = WEChartUtil.supperProxyManagerValidate(request,proxyUserRepository);
            if(StringUtils.isNotEmpty(checkResult)){
                return checkResult;
            }

            num = num == null ? 0 : num;
            if(num < 0){
                return new StandardResponse(-1, "illegal card number", null).toJSON();
            }
            ProxyUser proxyUser = proxyUserRepository.findByOpenId((String)request.getAttribute("openId"));
            PlayUser playUser = null;
            if("2".equals(proxyUser.getUserCategory())) {
                playUser = playUserRepository.findByUsername(proxyUser.getUserId());
            }
            PlayUser targetPlayUser = playUserRepository.findByUsername(targetUserId);
            if (("2".equals(proxyUser.getUserCategory()) && playUser == null) || targetPlayUser == null) {
                return new StandardResponse(-1, "can't find user", null).toJSON();
            }
            if ("2".equals(proxyUser.getUserCategory()) && playUser.getCards() < num) {
                return new StandardResponse(-1, "cards number don't enough", null).toJSON();
            }
            if("3".equals(proxyUser.getUserCategory())) {
                ConsumeCard consumeCard = consumeCardRepository.findByType("1");
                if(consumeCard.getEffectiveNum() <=0 || consumeCard.getEffectiveNum() < num){
                    return new StandardResponse(-1, "cards don't enough", null).toJSON();
                }
                consumeCard.setEffectiveNum(consumeCard.getEffectiveNum() - num);
                targetPlayUser.setCards(targetPlayUser.getCards() + num);
                consumeCardRepository.saveAndFlush(consumeCard);
                playUserRepository.saveAndFlush(targetPlayUser);
                WEChartUtil.addDealflow(dealFlowRepository,proxyUser.getUserId()+"",num,"消费",proxyUser.getOpenId());
                WEChartUtil.addDealflow(dealFlowRepository,targetPlayUser.getUsername()+"",num,"买入",targetPlayUser.getOpenid());
            }else{
                if(playUser.getCards() <=0 ||playUser.getCards() < num){
                    return new StandardResponse(-1, "cards don't enough", null).toJSON();
                }
                playUser.setCards(playUser.getCards() - num);
                targetPlayUser.setCards(targetPlayUser.getCards() + num);
                playUserRepository.saveAndFlush(playUser);
                playUserRepository.saveAndFlush(targetPlayUser);
                WEChartUtil.addDealflow(dealFlowRepository,playUser.getUsername()+"",num,"消费",playUser.getOpenid());
                WEChartUtil.addDealflow(dealFlowRepository,targetPlayUser.getUsername()+"",num,"买入",targetPlayUser.getOpenid());
            }
        }catch (Exception e){
            logger.info("房卡充值异常 openId:{},num:{},targetUserId:{}",(String)request.getAttribute("openId"),num,targetUserId,e);
            return new StandardResponse(-1, e.getMessage(), null).toJSON();
        }
        return new StandardResponse(1,"OK",null).toJSON();

    }










}
