package com.pl.face.aip.controller;

import com.alibaba.fastjson.JSON;
import com.baidu.aip.face.AipFace;
import com.baidu.aip.speech.AipSpeech;
import com.pl.face.aip.entity.ReturnMsg;
import com.pl.face.aip.entity.User;
import com.pl.face.aip.service.IFaceService;
import com.pl.face.aip.service.UserService;
import com.pl.face.aip.untils.FileUtil;
import com.pl.face.aip.untils.UtilHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@RestController
public class FaceRecongnice {
	@Autowired
	private UserService userServiceImpl;
	@Autowired
	private IFaceService iFaceService;

	@Autowired
	@Qualifier("faceClient")
	private AipFace client;

	@Autowired
	@Qualifier("ttsClient")
	private AipSpeech aipSpeech;

	// 处理文件上传
	@RequestMapping(value = "/uploadimg", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject uploadImg(@RequestParam("file") MultipartFile file, @RequestParam("code") Integer code,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "user_info", required = false) String user_info, HttpServletRequest request) {
		JSONObject result = new JSONObject();
		String contentType = file.getContentType();
		String fileName = file.getOriginalFilename();
		/*
		 * System.out.println("fileName-->" + fileName);
		 * System.out.println("getContentType-->" + contentType);
		 */
		String filePath = request.getSession().getServletContext().getRealPath("imgupload/");
		System.out.println("filePath:" + filePath);
		try {
			byte[] files = file.getBytes();
			FileUtil.uploadFile(files, filePath, fileName);
			switch (code) {
			case 1:
				result = iFaceService.faceDetection(client, files);
			case 2:
				result = iFaceService.faceSetAddUser(client, uid, user_info, files, "vanke_group");
			case 3:
				result = iFaceService.faceVerifyUser(client, uid, Arrays.asList("vanke_group"), files);
			default:
				result.put("error", "face Error");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		// 返回json
		return result;
	}

	@RequestMapping(value = "/apiface", method = RequestMethod.POST)
	public String uploadBase64Img(@RequestBody String msgbody, HttpServletRequest request) {
		JSONObject msgData = null;
		int code = -1;
		String image = null;
		String content = "";
		try {
			msgData = new JSONObject(msgbody);
			image = msgData.getString("image");
			code = msgData.getInt("code");
//			content = msgData.getString("content");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		UUID uuid = UUID.randomUUID();
		JSONObject result = new JSONObject();
		ReturnMsg ret = new ReturnMsg();
		try {
			byte[] files = UtilHelper.base64String2ByteFun(image);
			switch (code) {
			case 0:
				String filePath = request.getSession().getServletContext().getRealPath("voice/");
				result = iFaceService.baiDuTTS(aipSpeech, content, filePath);
				break;
			case 1:
				result = iFaceService.faceDetection(client, files);
				ret.setCode("0");
				break;
			case 2:
				String username = msgData.getString("username");
				String sex = msgData.getString("sex");
				String phone = msgData.getString("phone");
				int age = msgData.getInt("age");
				String position = msgData.getString("position");
				String department = msgData.getString("department");
				String nickname = msgData.getString("nickname");
				String vip_flag = msgData.getString("vip_flag");
				String idcard = msgData.getString("idcard");
				String reason = msgData.getString("reason");
				JSONObject result1 = iFaceService.faceRecognize(client, files, "vankegroup");
				//先做人脸识别 若存在就append 若不存在就注册新增
				if(result1.length()==0||result1.has("error_code")){
					String uid = uuid.toString().replace("-", "");
					String pinyin = UtilHelper.hanzi2pinyin(username);
					User user = new User(uid,username,sex,phone,age,position,department,nickname,vip_flag,pinyin,idcard,reason);
					userServiceImpl.addUser(user);
					result = iFaceService.faceSetAddUser(client, uid, username, files, "vankegroup");
				}else{
					String uid = result1.getJSONArray("result").getJSONObject(0).getString("uid");
					result = iFaceService.faceSetAddUser(client, uid, username, files, "vankegroup");
				}
				ret.setCode("0");
				break;
			case 3:
				JSONObject result2 = iFaceService.faceRecognize(client, files, "vankegroup");
				if(result2.length()==0){
					ret.setCode("1");
					ret.setErrorMsg("没有找到人员信息");
				}else {
					JSONObject jo = result2.getJSONArray("result").getJSONObject(0);
					Double score = jo.getJSONArray("scores").getDouble(0);
					if(score<80){
						ret.setCode("0");
						User user =new User();
						user.setVip_flag("3");
						ret.setContent(JSON.toJSONString(user));
//						result.put("error","没有找到人员信息");
					}else {
					String name = jo.getString("user_info");
					User user = userServiceImpl.getUserByName(name).get(0);
//					result.put("user", user.toString());
					ret.setCode("0");
					ret.setContent(JSON.toJSONString(user));
					}
				}
				break;
			default:
				ret.setCode("1");
				ret.setErrorMsg("code错误");
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
		}
		// 返回json
		System.out.println("return:"+JSON.toJSONString(ret));
		return JSON.toJSONString(ret);
	}

	@RequestMapping("/test")
	public String test() {
		return "你好呀，小伙子！";
	}

}
