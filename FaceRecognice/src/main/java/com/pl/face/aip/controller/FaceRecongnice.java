package com.pl.face.aip.controller;

import com.baidu.aip.face.AipFace;
import com.baidu.aip.speech.AipSpeech;
import com.pl.face.aip.service.IFaceService;
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
		String content = null;
		try {
			msgData = new JSONObject(msgbody);
			image = msgData.getString("image");
			code = msgData.getInt("code");
			content = msgData.getString("content");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		UUID uuid = UUID.randomUUID();
		JSONObject result = new JSONObject();
		try {
			byte[] files = UtilHelper.base64String2ByteFun(image);
			switch (code) {
			case 0:
				String filePath = request.getSession().getServletContext().getRealPath("voice/");
				result = iFaceService.baiDuTTS(aipSpeech, content, filePath);
				break;
			case 1:
				result = iFaceService.faceDetection(client, files);
				break;
			case 2:
				String user_info = msgData.getString("user_info");
				String uid = uuid.toString().replace("-", "");
				result = iFaceService.faceSetAddUser(client, uid, user_info, files, "vanke_group");
				break;
			case 3:
				result = iFaceService.faceRecognize(client, files, "vanke_group");
				break;
			default:
				result.put("error", "face Error");
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		// 返回json
		return result.toString();
	}

	@RequestMapping("/test")
	public String test() {
		return "你好呀，小伙子！";
	}

}
