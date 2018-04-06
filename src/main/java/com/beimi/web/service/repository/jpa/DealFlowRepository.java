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

    public abstract Page<DealFlow> findByUserName(String userName,Pageable paramPageable);

    public abstract Page<DealFlow> findByCreatePin(String createPin,Pageable paramPageable);

    public abstract Page<DealFlow> findBySrcType(String srcType,Pageable paramPageable);

    @Query(value="select t from DealFlow t  where t.createTime >=:startTime and t.creatTime <=:endTime limit startRow pageSize",nativeQuery=true)
    public abstract List<DealFlow> findByTimeRange(@Param("startTime") Date startTime, @Param("endTime")Date endTime);


}
