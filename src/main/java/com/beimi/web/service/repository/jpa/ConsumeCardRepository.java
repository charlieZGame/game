package com.beimi.web.service.repository.jpa;

import com.beimi.web.model.ConsumeCard;
import com.beimi.web.model.DealFlow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by zhengchenglei on 2018/3/20.
 */
public abstract class ConsumeCardRepository implements JpaRepository<ConsumeCard, String> {


    public abstract ConsumeCard findById(String id);

    public abstract ConsumeCard findByUserId(String userId,Pageable paramPageable);

    public abstract ConsumeCard findByUserName(String userName,Pageable paramPageable);


    public abstract ConsumeCard findByConsumeCardId(String consumeCardId,Pageable paramPageable);


    public abstract ConsumeCard findByCreatePin(String createPin,Pageable paramPageable);


}
