package com.beimi.web.model;

import com.beimi.util.UKTools;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;


/**
 *
 */
@Entity
@Table(name = "consume_card")
@org.hibernate.annotations.Proxy(lazy = false)
public class ConsumeCard implements Serializable {


    @Id
    private String id = UKTools.getUUID().toLowerCase();

    private String type;

    private String name;

    private Integer effectiveNum;

    private Integer totalNum;

    private String createTime;

    private String updateTime;

    private String createPin;

    private String updatePin;

    private String xybj;

    private String yxbj;


    @Id
    @Column(length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "assigned")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getEffectiveNum() {
        if(effectiveNum == null){
            return 0;
        }
        return effectiveNum;
    }

    public void setEffectiveNum(Integer effectiveNum) {
        if(effectiveNum == null){
            this.effectiveNum = 0;
        }
        this.effectiveNum = effectiveNum;
    }

    public Integer getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(Integer totalNum) {
        this.totalNum = totalNum;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }


    public String getCreatePin() {
        return createPin;
    }

    public void setCreatePin(String createPin) {
        this.createPin = createPin;
    }

    public String getUpdatePin() {
        return updatePin;
    }

    public void setUpdatePin(String updatePin) {
        this.updatePin = updatePin;
    }

    public String getXybj() {
        return xybj;
    }

    public void setXybj(String xybj) {
        this.xybj = xybj;
    }

    public String getYxbj() {
        return yxbj;
    }

    public void setYxbj(String yxbj) {
        this.yxbj = yxbj;
    }
}
