package com.beimi.web.service.repository.jpa;

import com.beimi.web.model.PlayHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by zhengchenglei on 2018/4/9.
 */
public abstract interface PlayHistoryRepository extends JpaRepository<PlayHistory, String> {

    /*@Query(value="select * from (select t.room_id,count(1) num,t.card_firends from play_history t  where t.user_id =:userId group by t.room_id,t.card_firends order by create_time) m",nativeQuery=true)
    public abstract List<Object> findByUserInfo(@Param("userId") String userId);*/

/*
    @Query(value="select * from play_history t where t.user_id =:userId and t.room_id=:roomId",nativeQuery=true)
*/
    public abstract List<PlayHistory> findByUserIdAndRoomUuid(String userId,String roomUuid);

    @Query(value="select * from (select t.room_uuid from play_history t  where t.user_id =:userId group by t.room_uuid order by create_time desc) m limit 0,50",nativeQuery=true)
    public abstract List<Object> findByUserId(@Param("userId")String userId);

    @Query(value="select * from (select t.room_id,count(1) num,t.nickname,t.username,t.photo, sum(t.score) score,t.create_time,sum(t.card_num) card_num,t.user_id,t.room_uuid from play_history t  where t.room_uuid in(:roomIds) group by t.room_uuid,t.nickname,t.username,t.photo,t.user_id order by create_time desc) m",nativeQuery=true)
    public abstract List<Object> summayRoom(@Param("roomIds") List<String> roomIds);

}
