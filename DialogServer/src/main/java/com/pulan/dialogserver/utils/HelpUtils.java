package com.pulan.dialogserver.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class HelpUtils {

    //随机数，随机返回结果
    public int getRandomNumber(int max){
        Random random =new Random();
        int result =random.nextInt(max+1);
        return result;
    }
    //是否是你可以做什么。
    public boolean isDoSomething(String voicetext){
        if(voicetext.contains("你可以做什么")){
            return true;
        }
        return false;
    }
}
