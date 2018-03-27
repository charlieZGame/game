package com.beimi.core.engine.game.ruleengine;

import com.beimi.core.engine.game.Message;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengchenglei on 2018/3/26.
 */
public class AbsGameBordCheck implements IGameBoardCheck {


    /**
     *
     * @param src
     * @param des
     * @return
     */
    protected List<Byte> ListByteCopy(List<Byte> src,List<Byte> des){

        if(CollectionUtils.isEmpty(src)){
            return des;
        }
        if(CollectionUtils.isEmpty(des)){
            des = new ArrayList<Byte>();
        }
        for(Byte b : src){
            des.add(b);
        }
        return des;
    }


    /**
     *
     * @param src
     * @param des
     * @return
     */
    protected Byte[] ByteArrayCopy(Byte[] src,Byte[] des){
        if(src == null || src.length == 0){
            return des;
        }
        if(des == null || des.length == 0){
            des = new Byte[src.length];
        }

        for(int i = 0;i < src.length;i++){
            des[i] = src[i];
        }
        return des;
    }


    /**
     *
     * @param src
     * @param des
     * @return
     */
    protected boolean isHaveSameColor(byte src,byte des){
        if(src < 0 || des < 0){
            return false;
        }
        if((src % 36) == (des % 36)){
            return true;
        }
        return false;
    }









    @Override
    public Message checkPlayerBoard() {
        return null;
    }
}
