package com.beimi.web.service.repository.jpa;

import com.beimi.web.model.DealFlow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

/**
 * Created by zhengchenglei on 2018/3/20.
 */
public abstract class DealFlowRepository implements JpaRepository<DealFlow, String> {


    public abstract Page<DealFlow> findById(String id);

    public abstract Page<DealFlow> findByUserId(String userId,Pageable paramPageable);

    public abstract Page<DealFlow> findByUserName(String userName,Pageable paramPageable);

    public abstract Page<DealFlow> findByConsumeCardId(String consumeCardId,Pageable paramPageable);

    public abstract Page<DealFlow> findByCreatePin(String createPin,Pageable paramPageable);

    @Query(value="select t from DealFlow t  where t.createTime >=:startTime and t.creatTime <=:endTime",nativeQuery=true)
    public abstract Page<DealFlow> findByTimeRange(@Param("startTime") Date startTime, @Param("endTime")Date endTime);


}
