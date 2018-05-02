package com.beimi.rule;

import com.beimi.util.rules.model.Action;

import java.util.List;

/**
 * Created by zhengchenglei on 2018/4/16.
 */
public interface ICheckScoreRule {


    boolean isSatisfy();

    CardType getHuName();

    Integer getHuScore();

    /**
     * @param collections
     * @param actions
     * @param powerful
     */
    void setData(List<Byte> collections, List<Action> actions, byte[] powerful);

}
