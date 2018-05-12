package com.beimi.web.handler;

import com.beimi.backManager.StandardResponse;
import com.beimi.util.Base64Util;
import com.beimi.web.model.PlayHistory;
import com.beimi.web.model.RoomSummary;
import com.beimi.web.service.repository.jpa.PlayHistoryRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengchenglei on 2018/4/19.
 */
@RequestMapping("/userCase")
@Controller
public class UserCaseTest {


    @Autowired
    private PlayHistoryRepository playHistoryRepository;

    @ResponseBody
    @RequestMapping("playHistoryTest")
    public String playHistoryRepositoryTest(String category,String userId,String roomId) throws IOException {

        if(StringUtils.isEmpty(category) || "1".equals(category)) {
            List<Object> summays = playHistoryRepository.findByUserId(userId);
            if (CollectionUtils.isEmpty(summays)) {
                return new StandardResponse(1, "ok", null).toJSON();
            }
            List<String> list = new ArrayList<String>();
            for (Object obj : summays) {
                if (obj == null) {
                    continue;
                }
                list.add((String) obj);
            }
            if (CollectionUtils.isEmpty(list)) {
                return new StandardResponse(-1, "roomIds2 is null", null).toJSON();
            }
            List<Object> response = playHistoryRepository.summayRoom(list);
            if (CollectionUtils.isEmpty(response)) {
                return new StandardResponse(1, "ok", null).toJSON();
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
                roomSummay.setRoomUuid((String)objects[9]);
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
            return new StandardResponse(1, "ok", tempMap).toJSON();
        }else{
            List<PlayHistory> playHistories = playHistoryRepository.findByUserIdAndRoomUuid(userId,roomId);
            List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
            if (CollectionUtils.isNotEmpty(playHistories)) {
                for (PlayHistory playHistory : playHistories) {
                    playHistory.setNickname(playHistory.getNickname() == null ? null : (Base64Util.baseDencode(playHistory.getNickname())));
                    playHistory.setFriendIds(null);
                    if(StringUtils.isNotEmpty(playHistory.getCardFirends())){
                        String tempStr = playHistory.getCardFirends();
                        System.out.println(tempStr);
                        String[] datas = tempStr.split("#");
                        for(String data : datas) {
                            Map<String,Object> map = new HashMap<String,Object>();
                            map.put("nickname", Base64Util.baseDencode(data.split(",")[0]));
                            map.put("score", data.split(",")[1]);
                            returnList.add(map);
                        }
                    }
                    playHistory.setCardFirends(null);
                    playHistory.setObj(returnList);
                }
            }
            return new StandardResponse(1, "ok", playHistories).toJSON();
        }
    }


}
