package com.lyun.api.response;

/**
 * @author 赵尉尉
 * @date 2016/12/20
 */

public class APIResult<T> {

    private int status;
    private String describe;
    private T content;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

}