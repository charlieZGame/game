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

    private String num;

    private String create_time;

    private String update_time;

    private String create_pin;

    private String update_pin;

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

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getCreate_pin() {
        return create_pin;
    }

    public void setCreate_pin(String create_pin) {
        this.create_pin = create_pin;
    }

    public String getUpdate_pin() {
        return update_pin;
    }

    public void setUpdate_pin(String update_pin) {
        this.update_pin = update_pin;
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
