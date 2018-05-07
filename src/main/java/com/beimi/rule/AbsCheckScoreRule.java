package com.beimi.rule;

import com.beimi.util.GameUtils;
import com.beimi.util.rules.model.Action;

import java.util.List;

/**
 * Created by zhengchenglei on 2018/4/16.
 */
public abstract class AbsCheckScoreRule {

    protected List<Byte> collections;

    protected List<Action> actions;

    protected byte[] powerful;


    abstract boolean isSatisfy();

    abstract CardType getHuName();

    abstract Integer getHuScore();

    /**
     * @param collections
     * @param actions
     * @param powerful
     */
    void setData(List<Byte> collections, List<Action> actions, byte[] powerful){
        this.collections = GameUtils.cloneList(collections);
        this.actions = actions;
        this.powerful = powerful;
    }

}
