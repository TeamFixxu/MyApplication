package com.example.myapplication;

public class MarkerData {
    private int addPersonCount;
    private Boolean isCreator; //0이면 신고자가 아님. 1이면 신고자
    private String description;
    private int pinType;

    public MarkerData(){} //firebase 사용하려면 인수없는 기본 생성자 필요

    public MarkerData(int addPersonCount, boolean isCreator,String description, int pinType){
        this.addPersonCount = addPersonCount;
        this.isCreator = isCreator;
        this.description = description;
        this.pinType = pinType;
    }

    public int getAddPersonCount(){
        return addPersonCount;
    }

    public void setAddPersonCount(int count){
        this.addPersonCount = count;
    }
    public boolean getIsCreator(){
        return isCreator;
    }

    public void setIsCreator(boolean bool){
        this.isCreator = bool;
    }
    public String getDescription(){
        return description;
    }

    public void setDescription(String text){
        this.description = text;
    }

    public int getPinType(){
        return pinType;
    }

    public void setPinType(int type){
        this.pinType = type;
    }
}