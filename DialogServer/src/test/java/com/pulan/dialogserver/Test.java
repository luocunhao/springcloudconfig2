package com.pulan.dialogserver;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Test {

    public ShardedJedisPool shardedJedisPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(2);
        jedisPoolConfig.setMaxIdle(1);
        jedisPoolConfig.setMaxWaitMillis(2000);
        List<JedisShardInfo> jedisShardInfos = new ArrayList<>();
        JedisShardInfo jedisShardInfo =new JedisShardInfo("192.168.0.112", 6379, 3000);
        jedisShardInfo.setPassword("AI-assist-MQ");
        jedisShardInfos.add(jedisShardInfo);
        ShardedJedisPool jedisPool = new ShardedJedisPool(jedisPoolConfig, jedisShardInfos);
        return jedisPool;
    }


    public static void main(String[] args) {
        Test test = new Test();
        /*ShardedJedisPool jedisPool = test.shardedJedisPool();
        //进行查询等其他操作
        ShardedJedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.pipelined();
            jedis.set("test", "test");
            jedis.set("test1", "test1");
            jedis.expire("test",20);
            //jedis.set("test","001");
           // jedis.del("test");
            String lkm = jedis.get("test");
            System.out.println(lkm);
        } finally {
            //使用后一定关闭，还给连接池
            if (jedis != null) {
                jedis.close();
            }
        }*/
       /* DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime date = LocalDateTime.parse("2017-11-15 09:00:00",f);
        long ls = date.toInstant(ZoneOffset.ofHours(8)).getEpochSecond();
        Instant now1 = Instant.now().plusMillis(TimeUnit.HOURS.toMillis(8));
        Instant now = Instant.now();
        long nowTime =now.getEpochSecond();
        System.out.println("转换时间："+ls);
        System.out.println("当前时间："+nowTime);
        String slot_value ="2018-09-01 10:00:00";
        String pattern = "\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}";
        boolean isMatch = Pattern.matches(pattern, slot_value);
        System.out.println(isMatch);
        String mailName ="luolp@cnfantasia.com";
        System.out.println(mailName.split("@")[0]);*/
        test.tesIg("mm63878078@163.com");
    }


    private void tesIg(String ls){

        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        Date date =new Date();
        String time = df.format(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);
        String time2 =df.format(calendar.getTime());
        System.out.println(time2);
        System.out.println(time);
    }

}
