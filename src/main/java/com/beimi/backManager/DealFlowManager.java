package com.beimi.backManager;

import com.beimi.util.Base64Util;
import com.beimi.web.model.DealFlow;
import com.beimi.web.service.repository.jpa.DealFlowRepository;
import com.beimi.web.service.repository.jpa.ProxyUserRepository;
import org.apache.commons.collections4.CollectionUtils;
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

import javax.servlet.http.HttpServletRequest;

/**
 * Created by zhengchenglei on 2018/4/6.
 */
@Controller
@RequestMapping("dealFlow")
public class DealFlowManager {


    @Autowired
    private DealFlowRepository dealFlowRepository;
    @Autowired
    private ProxyUserRepository proxyUserRepository;
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @ResponseBody
    @RequestMapping("getAllDealFlowInfo")
    public String getAllDealFlowInfo(HttpServletRequest request, String type, Integer startPage, Integer pageSize) {

        long tid = System.currentTimeMillis();
        PageRequest pageRequest = new PageRequest(startPage, pageSize, new Sort(Sort.Direction.DESC, "createTime"));

        try {
            String checkResult = null;
            checkResult = WEChartUtil.supperManagerValidate(request,proxyUserRepository);
            if(StringUtils.isEmpty(checkResult)){
                Page<DealFlow> dealFlowPage = null;
                if ("1".equals(type)) {
                    dealFlowPage = dealFlowRepository.findBySrcType("消费", pageRequest);
                } else if ("2".equals(type)) {
                    dealFlowPage = dealFlowRepository.findBySrcType("买入", pageRequest);
                } else {
                    dealFlowPage = dealFlowRepository.findAll(pageRequest);
                }
               return genereateResponse(dealFlowPage);
            }



            // 暂时屏蔽代理用户  supperManagerValidate
            //checkResult = WEChartUtil.supperProxyManagerValidate(request,proxyUserRepository);
            checkResult = WEChartUtil.supperManagerValidate(request,proxyUserRepository);
            if(StringUtils.isEmpty(checkResult)){
                Page<DealFlow> dealFlowPage = null;
                String openId = (String)request.getAttribute("openId");
                if ("1".equals(type)) {
                    dealFlowPage = dealFlowRepository.findByUserIdAndSrcType(openId,"消费", pageRequest);
                } else if ("2".equals(type)) {
                    dealFlowPage = dealFlowRepository.findByUserIdAndSrcType(openId,"买入", pageRequest);
                }
                return genereateResponse(dealFlowPage);
            }
            return checkResult;

        }catch (Exception e){
            logger.error("tid:{} 查询消费流水信息异常 type:{},startPage:{},pageSize:{}",tid,type,startPage,pageSize,e);
            return new StandardResponse<PageResponse>(-1,e.getMessage(),null).toJSON();
        }
    }

    private String genereateResponse(Page<DealFlow> dealFlowPage){

        if(dealFlowPage == null){
            return new StandardResponse<PageResponse>(1,"OK",null).toJSON();
        }else {
            if(CollectionUtils.isNotEmpty(dealFlowPage.getContent())){
                for(DealFlow dealFlow : dealFlowPage.getContent()){
                    dealFlow.setCreatePin(Base64Util.baseDencode(dealFlow.getCreatePin()));
                }
            }
            PageResponse pageResponse = new PageResponse(dealFlowPage.getNumber(), dealFlowPage.getSize(),
                    dealFlowPage.getTotalPages(), dealFlowPage.getContent());
            return new StandardResponse<PageResponse>(1,"OK",pageResponse).toJSON();
        }

    }



}
