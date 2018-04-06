package com.beimi.web.service.repository.jpa;

import com.beimi.web.model.ManagerUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by zhengchenglei on 2018/4/7.
 */
public abstract interface ProxyUserRepository extends JpaRepository<ManagerUser, String> {

    public abstract Page<ManagerUser> findByUserCategory(String userCategory, Pageable pageable);

    public abstract ManagerUser findByUserId(Integer userId);


}
