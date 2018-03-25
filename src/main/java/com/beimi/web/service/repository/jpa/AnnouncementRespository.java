package com.beimi.web.service.repository.jpa;

import com.beimi.web.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by zhengchenglei on 2018/3/21.
 */
public abstract interface  AnnouncementRespository  extends JpaRepository<Announcement, String> {


    abstract Announcement findByType(String type);


}
