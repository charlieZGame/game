package com.beimi.model;

/**
 * Created by zhengchenglei on 2018/3/15.
 */
public class GameResponse<T> {


    private Integer returnCode;

    private String msg;

    private T content;


    public GameResponse() {
    }

    public GameResponse(Integer returnCode, String msg, T content) {
        this.returnCode = returnCode;
        this.msg = msg;
        this.content = content;
    }

    public static GameResponse<?> gameErrorResponse(String msg,Object content){

        return new GameResponse(-1,msg,content);

    }


    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
