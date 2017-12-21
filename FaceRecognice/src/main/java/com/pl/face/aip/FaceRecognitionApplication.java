package com.pl.face.aip;

import cn.jsms.api.common.SMSClient;
import com.baidu.aip.face.AipFace;
import com.baidu.aip.speech.AipSpeech;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;

import javax.servlet.MultipartConfigElement;

@SpringBootApplication
public class FaceRecognitionApplication {

	@Value("${baidu.appid}")
	private String APP_ID;
	@Value("${baidu.apikey}")
	private String API_KEY;
	@Value("${baidu.secretkey}")
	private String SECRET_KEY;
	@Value("${jiguang.mastersecret}")
	private String MASTERSECRET;
	@Value("${jiguang.appkey}")
	private String APPKEY;
	public static void main(String[] args) {
		SpringApplication.run(FaceRecognitionApplication.class, args);
	}

	//文件传输工具类。
	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize("1048576KB");
		factory.setMaxRequestSize("1048576KB");
		//  factory.setLocation("/app/pttms/tmp");
		return factory.createMultipartConfig();
	}

	//百度人脸识别Client init
	@Bean(name = "faceClient")
	public AipFace faceClient() {
		// 初始化一个AipFace
		AipFace client = new AipFace(APP_ID, API_KEY, SECRET_KEY);
		client.setConnectionTimeoutInMillis(2000);
		return client;
	}
	//极光短信
	@Bean(name="smsClient")
	public SMSClient smsClient(){
		SMSClient smsClient = new SMSClient(MASTERSECRET,APPKEY);
		return smsClient;
	}

	//百度语言Client init
	@Bean(name = "ttsClient")
	public AipSpeech ttsClient() {
		// 初始化一个AipSpeech
		AipSpeech client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);
		client.setConnectionTimeoutInMillis(2000);
		return client;
	}
}
