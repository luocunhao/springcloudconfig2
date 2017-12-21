package com.pl.face.aip.entity;

public class ReturnMsg {
    private String code;
    private String content;
    private String errorMsg;
    public ReturnMsg(){}
    public ReturnMsg(String code, String content, String errorMsg) {
        this.code = code;
        this.content = content;
        this.errorMsg = errorMsg;
    }

    public String getCode() {
        return code;
    }

    public String getContent() {
        return content;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "ReturnMsg{" +
                "code='" + code + '\'' +
                ", content='" + content + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
