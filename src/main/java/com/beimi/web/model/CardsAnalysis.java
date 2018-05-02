package com.beimi.web.model;

import java.io.Serializable;
import java.util.List;
/**
 * Created by zhengchenglei on 2018/4/26.
 */
public class CardsAnalysis implements Serializable {


    private String type;

    private List<CardInfo> cardInfo;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<CardInfo> getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(List<CardInfo> cardInfo) {
        this.cardInfo = cardInfo;
    }

    public class CardInfo{

        private String data;

        private int number;


        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }




}
