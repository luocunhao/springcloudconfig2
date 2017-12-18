package com.pulan.dialogserver.entity;

import java.io.Serializable;

/**
 * 查询类语义槽模版。
 */
public class SemanticSlots implements Serializable {

    private String slotId;

    private String templateService;

    private String templateIntent;

    private String slotCode;

    private String slotName;

    private String slotValue;

    private String required;

    private String defaultValue;

    private String prompt;

    private String utterance;

    private String wordClass;

    private Integer slotOrder;

    private Integer tryCount;

    public SemanticSlots() {
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public String getTemplateService() {
        return templateService;
    }

    public void setTemplateService(String templateService) {
        this.templateService = templateService;
    }

    public String getTemplateIntent() {
        return templateIntent;
    }

    public void setTemplateIntent(String templateIntent) {
        this.templateIntent = templateIntent;
    }

    public String getSlotCode() {
        return slotCode;
    }

    public void setSlotCode(String slotCode) {
        this.slotCode = slotCode;
    }

    public String getSlotName() {
        return slotName;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }

    public String getSlotValue() {
        return slotValue;
    }

    public void setSlotValue(String slotValue) {
        this.slotValue = slotValue;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getUtterance() {
        return utterance;
    }

    public void setUtterance(String utterance) {
        this.utterance = utterance;
    }

    public String getWordClass() {
        return wordClass;
    }

    public void setWordClass(String wordClass) {
        this.wordClass = wordClass;
    }

    public Integer getSlotOrder() {
        return slotOrder;
    }

    public void setSlotOrder(Integer slotOrder) {
        this.slotOrder = slotOrder;
    }

    public Integer getTryCount() {
        return tryCount;
    }

    public void setTryCount(Integer tryCount) {
        this.tryCount = tryCount;
    }

    @Override
    public String toString() {
        return "SemanticSlots{" +
                "slotId='" + slotId + '\'' +
                ", templateService='" + templateService + '\'' +
                ", templateIntent='" + templateIntent + '\'' +
                ", slotCode='" + slotCode + '\'' +
                ", slotName='" + slotName + '\'' +
                ", slotValue='" + slotValue + '\'' +
                ", required='" + required + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", prompt='" + prompt + '\'' +
                ", utterance='" + utterance + '\'' +
                ", wordClass='" + wordClass + '\'' +
                ", slotOrder=" + slotOrder +
                ", tryCount=" + tryCount +
                '}';
    }
}
