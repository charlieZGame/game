package com.beimi.backManager;

import com.beimi.rule.ReturnResult;
import com.beimi.util.Base64Util;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.Player;
import com.beimi.web.model.*;
import com.beimi.web.service.repository.jpa.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zhengchenglei on 2018/4/7.
 */
@Service("houseCardHandlerService")
public class HouseCardHandlerService {


    @Autowired
    private PlayUserClientRepository playUserRepository;
    @Autowired
    private DealFlowRepository dealFlowRepository;
    @Autowired
    private ConsumeCardRepository consumeCardRepository;
    @Autowired
    private PlayHistoryRepository playHistoryRepository;
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Transactional
    public void cardHandler(GameRoom gameRoom,List<PlayUserClient> players,List<ReturnResult> returnResults) {


        if(CollectionUtils.isEmpty(players)){
            return;
        }
        PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(gameRoom.getMaster(), gameRoom.getOrgi());

      //  playUser.setCards(playUser.getCards() - gameRoom.getNumofgames());
       // playUserRepository.saveAndFlush(playUser);
        for(PlayUserClient playUserClient : players){
            for(ReturnResult returnResult : returnResults){
                if(playUserClient.getId().equals(returnResult.getUserId())){
                    playUserClient.setGoldcoins(playUserClient.getGoldcoins()<=0? 0 :playUserClient.getGoldcoins() - returnResult.getScore());
                    String level = null;
                    if(playUserClient.getGoldcoins() < 500){
                        level = "贫农";
                    }else if(playUserClient.getGoldcoins() > 500 && playUserClient.getGoldcoins() < 1000){
                        level = "中农";
                    }else if(playUserClient.getGoldcoins() > 1000 && playUserClient.getGoldcoins() < 2500){
                        level = "富农";
                    }else if(playUserClient.getGoldcoins() > 2500 && playUserClient.getGoldcoins() < 6000){
                        level = "资本家";
                    }else if(playUserClient.getGoldcoins() > 6000){
                        level = "麻皇";
                    }
                    playUserClient.setPlayerlevel(level);
                }
                if(playUser.getId().equals(playUserClient.getId())){
                    playUser.setCards(playUser.getCards() - gameRoom.getNumofgames());
                   // playUserClient.setCards(playUserClient.getCards() - gameRoom.getNumofgames());
                    //每一局都结算
                    playUserClient.setCards(playUserClient.getCards() - 1);
                }
                playUserRepository.saveAndFlush(playUserClient);
            }
        }
        dealFlowRepository.save(addDealflow(playUser.getUsername() + "", gameRoom.getNumofgames(), "消费", playUser.getOpenid()));
        ConsumeCard consumeCard = consumeCardRepository.findByType("1");
        if (consumeCard == null) {
            return;
        }
        consumeCard.setEffectiveNum(consumeCard.getEffectiveNum() + gameRoom.getNumofgames());
        consumeCardRepository.saveAndFlush(consumeCard);
        dealFlowRepository.save(addDealflow("主账户", gameRoom.getNumofgames(), "买入", "88888888888888888888888888888888"));
    }

    private DealFlow addDealflow(String username, Integer num, String type, String userId) {
        DealFlow dealFlow = new DealFlow();
        dealFlow.setUserName(username);
        dealFlow.setNum(num);
        dealFlow.setCreateTime(new Date());
        dealFlow.setYxbj("1");
        dealFlow.setXybj("1");
        dealFlow.setUserId(userId);
        dealFlow.setOpenId(userId);
        dealFlow.setSrcType(type);
        return dealFlow;

    }


    public StandardResponse queryUserFlow(String userId, Integer startPage, Integer pageSize, Integer roomId) {

        try {
            if (roomId == null) {
                List<Object> summays = playHistoryRepository.findByUserId(userId);
                if (CollectionUtils.isEmpty(summays)) {
                    return new StandardResponse(1, "ok", null);
                }
                List<Integer> list = new ArrayList<Integer>();
                for (Object obj : summays) {
                    if (obj == null) {
                        continue;
                    }
                    list.add(obj instanceof BigInteger ? ((BigInteger) obj).intValue() : (Integer) obj);
                }
                if (CollectionUtils.isEmpty(list)) {
                    return new StandardResponse(-1, "roomIds2 is null", null);
                }
                List<Object> response = playHistoryRepository.summayRoom(list);
                if (CollectionUtils.isEmpty(response)) {
                    return new StandardResponse(1, "ok", null);
                }
                List<String> roomTemp = new ArrayList<String>();
                List<RoomSummary> lis = new ArrayList<RoomSummary>();
                for (Object obj : response) {
                    Object[] objects = (Object[]) obj;
                    RoomSummary roomSummay = new RoomSummary();
                    roomSummay.setRoomId(objects[0] == null ? 0 : (objects[0] instanceof BigInteger ? ((BigInteger) objects[0]).intValue() : (Integer) objects[0]));
                    roomSummay.setNum(objects[1] == null ? 0 : (objects[1] instanceof BigInteger ? ((BigInteger) objects[1]).intValue() : (Integer) objects[1]));
                    roomSummay.setNickname(objects[2] == null ? null : Base64Util.baseDencode((String) objects[2]));
                    roomSummay.setUserNo(objects[3] == null ? 0 : (objects[3] instanceof BigInteger ? ((BigInteger) objects[3]).intValue() : (Integer) objects[3]));
                    roomSummay.setPhoto(objects[4] == null ? null : (String) objects[4]);
                    roomSummay.setScore(objects[5] == null ? 0 : (objects[5] instanceof BigDecimal ? ((BigDecimal) objects[5]).intValue() : (Integer) objects[5]));
                    roomSummay.setDate(objects[6] == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(objects[6]));
                    Object cards = (objects[7] == null ? 0 : objects[7]);
                    roomSummay.setUseCards(cards == null ? 0 : cards instanceof BigDecimal ? ((BigDecimal) cards).intValue() : (Integer) cards);
                    if (objects[8] != null && userId.equals(objects[8])) {
                        roomSummay.setCurrentUser(true);
                    }
                    lis.add(roomSummay);
                }
                Map<String, Object> tempMap = new HashMap<String, Object>();
                for (RoomSummary roomSummary : lis) {
                    if (tempMap.containsKey(roomSummary.getRoomId() + "")) {
                        ((List<RoomSummary>) tempMap.get(roomSummary.getRoomId() + "")).add(roomSummary);
                    } else {
                        roomTemp.add(roomSummary.getRoomId() + "");
                        List<RoomSummary> listemp = new ArrayList<RoomSummary>();
                        listemp.add(roomSummary);
                        tempMap.put(roomSummary.getRoomId() + "", listemp);
                    }
                }
                tempMap.put("roomIds", roomTemp);
                return new StandardResponse(1, "ok", tempMap);
            } else {
                List<PlayHistory> playHistories = playHistoryRepository.findByUserIdAndRoomId(userId, roomId);
                if (CollectionUtils.isEmpty(playHistories)) {
                    return new StandardResponse(1, "ok", null);
                }
                List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
                for (PlayHistory playHistory : playHistories) {
                    playHistory.setNickname(playHistory.getNickname() == null ? null : (Base64Util.baseDencode(playHistory.getNickname())));
                    playHistory.setFriendIds(null);
                    if (StringUtils.isNotEmpty(playHistory.getCardFirends())) {
                        String tempStr = playHistory.getCardFirends();
                        System.out.println(tempStr);
                        String[] datas = tempStr.split("#");
                        for (String data : datas) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("nickname", Base64Util.baseDencode(data.split(",")[0]));
                            map.put("score", data.split(",")[1]);
                            returnList.add(map);
                        }
                    }
                    playHistory.setCardFirends(null);
                    playHistory.setObj(returnList);
                }
                return new StandardResponse(1, "ok", playHistories);
            }
        }catch (Exception e) {
            e.printStackTrace();
            return new StandardResponse(-1, "failure", null);
        }
    }


    public String queryPlayerEnd(Integer roomId) {
        try {
            if(roomId == null){
                return new StandardResponse(-1, "roomId is null", null).toJSON();
            }
            List<Integer> roomIds = new ArrayList<Integer>();
            roomIds.add(roomId);
            List<Object> response = playHistoryRepository.summayRoom(roomIds);
            if(CollectionUtils.isEmpty(response)){
                return new StandardResponse(1, "ok", null).toJSON();
            }
            List<String> roomTemp = new ArrayList<String>();
            List<RoomSummary> lis = new ArrayList<RoomSummary>();
            for (Object obj : response) {
                Object[] objects = (Object[]) obj;
                RoomSummary roomSummay = new RoomSummary();
                roomSummay.setRoomId(objects[0] == null ? 0: (objects[0] instanceof BigInteger ? ((BigInteger) objects[0]).intValue() : (Integer) objects[0]));
                roomSummay.setNum(objects[1] == null ? 0 : (objects[1] instanceof BigInteger ? ((BigInteger) objects[1]).intValue() : (Integer) objects[1]));
                roomSummay.setNickname(objects[2] == null ? null : Base64Util.baseDencode((String) objects[2]));
                roomSummay.setUserNo(objects[3] == null ? 0 : (objects[3] instanceof BigInteger ? ((BigInteger) objects[3]).intValue() : (Integer) objects[3]));
                roomSummay.setPhoto(objects[4] == null ? null : (String)objects[4]);
                roomSummay.setScore(objects[5] == null ? 0 : (objects[5] instanceof BigDecimal ? ((BigDecimal) objects[5]).intValue() : (Integer) objects[5]));
                roomSummay.setDate(objects[6] == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(objects[6]));
                Object cards = (objects[7] == null ? 0 : objects[7]);
                roomSummay.setUseCards(cards == null ? 0 :cards instanceof BigDecimal ? ((BigDecimal) cards).intValue() : (Integer) cards);
                lis.add(roomSummay);
            }
            Map<String,Object> tempMap = new HashMap<String,Object>();
            for(RoomSummary roomSummary : lis){
                if(tempMap.containsKey(roomSummary.getRoomId()+"")){
                    ((List<RoomSummary>)tempMap.get(roomSummary.getRoomId()+"")).add(roomSummary);
                }else{
                    roomTemp.add(roomSummary.getRoomId()+"");
                    List<RoomSummary> listemp = new ArrayList<RoomSummary>();
                    listemp.add(roomSummary);
                    tempMap.put(roomSummary.getRoomId()+"",listemp);
                }
            }
            tempMap.put("roomIds",roomTemp);
            return new StandardResponse(1, "ok", tempMap).toJSON();
        }catch (Exception e){
            logger.error("查询总结信息异常",e);
            return null;
        }
    }



        /**
         *
         * @param playUsers
         */
    public void saveUserFlow(GameRoom gameRoom, Board board, List<PlayUserClient> playUsers,List<ReturnResult> returnResults){

        StringBuilder nikeNames = new StringBuilder();
        StringBuilder ids = new StringBuilder();
        Map<String,PlayUserClient> map = new HashMap<String,PlayUserClient>();
        for(PlayUserClient playUser : playUsers) {
            for(ReturnResult returnResult :returnResults) {
                if(playUser.getId().equals(returnResult.getUserId())) {
                    nikeNames.append("#").append(Base64Util.baseEncode(playUser.getNickname())).append(",").append(returnResult.getScore());
                }
            }
            map.put(playUser.getId(),playUser);
            ids.append("#").append(playUser.getId());
        }
        for(Player player : board.getPlayers()) {
            for(ReturnResult returnResult :returnResults) {
                if(player.getPlayuser().equals(returnResult.getUserId())) {
                    PlayHistory playHistory = new PlayHistory();
                    playHistory.setUserId(player.getPlayuser());
                    playHistory.setRoomId(Integer.parseInt(gameRoom.getRoomid()));
                    playHistory.setCreateTime(new Date());
                    playHistory.setScore(returnResult.getScore());
                    playHistory.setCardFirends(nikeNames.length() > 0 ? nikeNames.substring(1) : "");
                    playHistory.setUsername(map.get(player.getPlayuser()).getUsername() == null ? 0 : map.get(player.getPlayuser()).getUsername());
                    playHistory.setPhoto(map.get(player.getPlayuser()).getPhoto());
                    playHistory.setNickname(map.get(player.getPlayuser()).getNickname() == null ? null : Base64Util.baseEncode(map.get(player.getPlayuser()).getNickname()));
                    if(board.getBanker().equals(player.getPlayuser())) {
                        playHistory.setCardNum(gameRoom.getNumofgames());
                    }else{
                        playHistory.setCardNum(0);
                    }
                    playHistory.setCreateTime(new Date());
                    playHistory.setIsWin(player.isWin() ? "1" : "0");
                    playHistory.setYxbj("1");
                    playHistory.setFriendIds(ids.length() == 0 ? "" : ids.substring(1));
                    playHistoryRepository.save(playHistory);
                }
            }
        }

    }



}
