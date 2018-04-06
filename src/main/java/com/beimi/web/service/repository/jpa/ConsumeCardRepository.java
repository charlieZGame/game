package com.beimi.web.service.repository.jpa;

import com.beimi.web.model.ConsumeCard;
import com.beimi.web.model.DealFlow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by zhengchenglei on 2018/3/20.
 */
public abstract interface ConsumeCardRepository extends JpaRepository<ConsumeCard, String> {


    public abstract ConsumeCard findById(String id);

    public abstract ConsumeCard findByType(String type);

    public abstract Page<ConsumeCard> findByCreatePin(String createPin,Pageable paramPageable);


}
