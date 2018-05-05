package com.beimi.web.service.repository.jpa;

import com.beimi.web.model.DealFlow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
/**
 * Created by zhengchenglei on 2018/3/20.
 */
public abstract interface DealFlowRepository extends JpaRepository<DealFlow, String> {


    public abstract DealFlow findById(String id);

    public abstract Page<DealFlow> findByUserId(String userId,Pageable paramPageable);

    public abstract Page<DealFlow> findByUserIdAndSrcType(String userId,String srcType,Pageable paramPageable);

    public abstract Page<DealFlow> findByUserName(String userName,Pageable paramPageable);

    public abstract Page<DealFlow> findByCreatePin(String createPin,Pageable paramPageable);

    public abstract Page<DealFlow> findBySrcType(String srcType,Pageable paramPageable);

    @Query(value="select DATE_FORMAT(t.create_time,'%Y-%m') month,sum(t.num) as num from deal_flow t  where  open_id=:openId and src_type='消费' " +
            "and DATE_FORMAT(t.create_time,'%Y-%m') >=:startTime and DATE_FORMAT(t.create_time,'%Y-%m') <=:endTime group by DATE_FORMAT(t.create_time,'%Y-%m')",nativeQuery=true)
    public abstract List<Object> findByMonthRange(@Param("openId")String openId,@Param("startTime") String startTime, @Param("endTime")String endTime);

    @Query(value="select  DATE_FORMAT(t.create_time,'%Y-%m-%d') data,sum(t.num) as num from deal_flow t  where  open_id =:openId  " +
            "and src_type='消费' and DATE_FORMAT(t.create_time,'%Y-%m-%d') >=:startTime and DATE_FORMAT(t.create_time,'%Y-%m-%d') <=:endTime group by  DATE_FORMAT(t.create_time,'%Y-%m-%d')",nativeQuery=true)
    public abstract List<Object> findByDayRange(@Param("openId")String openId,@Param("startTime") String startTime, @Param("endTime")String endTime);




}
