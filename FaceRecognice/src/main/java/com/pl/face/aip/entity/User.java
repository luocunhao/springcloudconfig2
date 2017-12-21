package com.pl.face.aip.entity;

import com.baomidou.mybatisplus.annotations.TableField;

import java.util.Calendar;

public class User {
    private String userid;
    private  String name;
    private String sex;
    private String phone;
    private int age;
    private String position;
    private String department;
    private String nickname;
    private String vip_flag;
    private String pinyin;
    private String idcard;
    private String reason;
    private String when;
    public User(){}
    public User(String userid, String name, String sex, String phone, int age, String position, String department,String nickname,String vip_flag,String pinyin,String idcard,String reason) {
        this.userid = userid;
        this.name = name;
        this.sex = sex;
        this.phone = phone;
        this.age = age;
        this.position = position;
        this.department = department;
        this.nickname = nickname;
        this.vip_flag = vip_flag;
        this.pinyin = pinyin;
        this.idcard = idcard;
        this.reason = reason;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setVip_flag(String vip_flag) {
        this.vip_flag = vip_flag;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getUserid() {
        return userid;
    }

    public String getName() {
        return name;
    }

    public String getSex() {
        return sex;
    }

    public String getPhone() {
        return phone;
    }

    public int getAge() {
        return age;
    }

    public String getPosition() {
        return position;
    }

    public String getDepartment() {
        return department;
    }

    public String getNickname() {
        return nickname;
    }
    public String getVip_flag() {
        return vip_flag;
    }

    public String getWhen() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if(hour==8){
            this.when = "0";
        }else {
            this.when = "1";
        }
        return when;
    }
}
