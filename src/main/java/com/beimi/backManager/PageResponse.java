package com.beimi.backManager;

import java.util.List;

/**
 * Created by zhengchenglei on 2018/4/6.
 */
public class PageResponse<T> {


    private int number;

    private int pageSize;

    private int totalPage;

    private List<T> content;


    public PageResponse(int number, int pageSize, int totalPage, List<T> content) {
        this.number = number;
        this.pageSize = pageSize;
        this.totalPage = totalPage;
        this.content = content;
    }

    public PageResponse() {

    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }
}
