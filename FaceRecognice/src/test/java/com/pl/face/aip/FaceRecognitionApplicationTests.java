package com.pl.face.aip;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
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
		Calendar cal = Calendar.getInstance();
		System.out.println(cal.get(Calendar.HOUR_OF_DAY));
	}

}
