package com.pulan.dialogserver.api;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * 向机器人发送请求得到响应
 * @author LiHao
 *
 */
@Component
public class RobotRequest{
	private Logger logger = LogManager.getLogger(RobotRequest.class);

	/*机器人访问 POST 请求*/
	public String postRequest(String url, String body) {
		String result ;
		BufferedReader reader ;
		try {
			URL apiUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			connection.connect();

			PrintWriter writer = new PrintWriter(connection.getOutputStream());
			writer.write(body);
			writer.flush();
			writer.close();

			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			String line;
			StringBuffer buffer = new StringBuffer();
			while ((line = reader.readLine()) != null){
				buffer.append(line);
			}
			reader.close();
			result = buffer.toString();
			logger.info("机器人应答：" + result);
			return result;
		} catch (Exception e) {
			return null;
		}

	}

	/*机器人访问 GET 请求*/
	public String getRequest(String url, String param,String type) {
		String result ;
		BufferedReader in;
		try {
			String urlNameString =null;
			if (url.contains("slot")){
				urlNameString =url+URLEncoder.encode(param, "utf-8")+"?type="+type;
			}else if (url.contains("test")){
				urlNameString =url+URLEncoder.encode(param, "utf-8");
			}
			logger.info("Request for Pulan AI Server:" + urlNameString);
			URL realUrl =new URL(urlNameString);
			//打开和URL之间的连接
			HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
			connection.setRequestMethod("GET");
			//Get请求不需要DoOutPut
			connection.setDoOutput(false);
			connection.setDoInput(true);
			//设置连接超时时间和读取超时时间
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);
			//connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
			//建立实际连接
			connection.connect();
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
			String line;
			StringBuffer buffer = new StringBuffer();
			while ((line = in.readLine()) != null){
				buffer.append(line);
			}
			in.close();
			result = buffer.toString();
			logger.info("Pulan AI Server response:" + result);
			return result;
		}catch (Exception e) {
			return null;
		}

	}

	/*public static void main(String[] args) {
		String url ="http://pulanbd.vicp.io:8800/slot/";
		String url2 ="http://pulanbd.vicp.io:8800/test/";
		String param ="罗丽萍";
		RobotRequest request =new RobotRequest();
		String rest =request.getRequest(url,param,"person");
		String rest2 =request.getRequest(url2,"我今天的考勤","");
		System.out.println(rest2);
		System.out.println(rest);
	}*/


}
