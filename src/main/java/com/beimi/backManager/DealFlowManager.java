package com.beimi.backManager;

import com.alibaba.fastjson.JSONObject;
import com.beimi.web.model.DealFlow;
import com.beimi.web.service.repository.jpa.DealFlowRepository;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by zhengchenglei on 2018/4/6.
 */
@Controller
@RequestMapping("dealFlow")
public class DealFlowManager {


    @Autowired
    private DealFlowRepository dealFlowRepository;
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @ResponseBody
    @RequestMapping("getAllDealFlowInfo")
    public String getAllDealFlowInfo(String type,Integer startPage,Integer pageSize) {

        long tid = System.currentTimeMillis();

        try {
            PageRequest pageRequest = new PageRequest(startPage, pageSize, new Sort(Sort.Direction.DESC, "createTime"));
            Page<DealFlow> dealFlowPage = null;
            if ("1".equals(type)) {
                dealFlowPage = dealFlowRepository.findBySrcType("消费", pageRequest);
            } else if ("2".equals(type)) {
                dealFlowPage = dealFlowRepository.findBySrcType("买入", pageRequest);
            } else {
                dealFlowPage = dealFlowRepository.findAll(pageRequest);
            }
            if(dealFlowPage == null){
                return new StandardResponse<PageResponse>(1,"OK",null).toJSON();
            }else {
                PageResponse pageResponse = new PageResponse(dealFlowPage.getNumber(), dealFlowPage.getSize(),
                        dealFlowPage.getTotalPages(), dealFlowPage.getContent());
                return new StandardResponse<PageResponse>(1,"OK",pageResponse).toJSON();
            }
        }catch (Exception e){
            logger.error("tid:{} 查询消费流水信息异常 type:{},startPage:{},pageSize:{}",tid,type,startPage,pageSize,e);
            return new StandardResponse<PageResponse>(-1,e.getMessage(),null).toJSON();
        }
    }



}
