package com.pl.face.aip;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSON;
import com.pl.face.aip.untils.UtilHelper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FaceRecognitionApplicationTests {

	@Test
	public void contextLoads() throws UnsupportedEncodingException {
		String url = "http://210.75.8.38:8021/pulan/api";
		String param = "{'apikey':'test123456','userid':'1246','content':'查考勤'}";
		System.out.println(UtilHelper.postRequest(url, param));
	}

}
