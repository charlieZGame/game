package com.beimi.web.service.repository.jpa;

import java.util.List;
import com.beimi.web.model.ProxyUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by zhengchenglei on 2018/4/7.
 */
public abstract interface ProxyUserRepository extends JpaRepository<ProxyUser, String> {

    public abstract Page<ProxyUser> findByUserCategory(String userCategory, Pageable pageable);

    public abstract ProxyUser findByOpenId(String openId);

    public abstract ProxyUser findByUserId(Integer userId);

    public abstract List<ProxyUser> findByNickname(String nickname);


}
