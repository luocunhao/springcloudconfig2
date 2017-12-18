package com.pulan.dialogserver.utils;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HanZi2PinYingUtil {

	private Logger logger = Logger.getLogger(HanZi2PinYingUtil.class.getName());

	@Autowired
	private JdbcUtils utils;

	/**
	 * 将错误的中文名通过拼音转换查询替换成正确的中文名。
	 * @param resp
	 * @return
	 */
	public List<String> getPinYin(String resp) {
		String result = "";
		List<String> list = new ArrayList<>();
		if (resp.contains("考勤") || resp.contains("饱和度") || resp.contains("邮件") || resp.contains("聊天记录")
				|| resp.contains("打电话") || resp.contains("待办任务") || resp.contains("会议")) {
			logger.info("pinyin start...");
			String[] strs = new String[] { "查一下", "查下", "看一下", "我想查", "给" };
			String yindaoci = "";
			String noYinDaoCi = "";
			for (String str : strs) {
				if (resp.contains(str)) {
					noYinDaoCi = resp.replace(str, "");
					yindaoci = str;
					break;
				} else {
					noYinDaoCi = resp;
				}
			}
			logger.info("yindaoci " + yindaoci);
			logger.info("noYinDaoCi " + noYinDaoCi);
			if (noYinDaoCi.length() > 4) {
				try {
					String fourwordspy = PinyinHelper.convertToPinyinString(noYinDaoCi.substring(0, 4), "",
							PinyinFormat.WITHOUT_TONE);
					String threewordspy = PinyinHelper.convertToPinyinString(noYinDaoCi.substring(0, 3), "",
							PinyinFormat.WITHOUT_TONE);
					String twowordspy = PinyinHelper.convertToPinyinString(noYinDaoCi.substring(0, 2), "",
							PinyinFormat.WITHOUT_TONE);
					String onewordspy = PinyinHelper.convertToPinyinString(noYinDaoCi.substring(0, 1), "",
							PinyinFormat.WITHOUT_TONE);
					List<String> fourwords = utils.getChineseName(fourwordspy);
					List<String> threewords = utils.getChineseName(threewordspy);
					logger.info("twowordspy :" + twowordspy);
					List<String> twowords = utils.getChineseName(twowordspy);
					List<String> onewords = utils.getChineseName(onewordspy);
					logger.info("fourwords" + fourwords.isEmpty());
					logger.info("threewords" + threewords.isEmpty());
					logger.info("twowords" + twowords.isEmpty());
					logger.info("onewords" + onewords.isEmpty());
					if (!fourwords.isEmpty()) {
						for (String str : fourwords) {
							result = yindaoci + str + noYinDaoCi.substring(4, noYinDaoCi.length());
							list.add(result);
						}
					} else if (!threewords.isEmpty()) {
						for (String str : threewords) {
							result = yindaoci + str + noYinDaoCi.substring(3, noYinDaoCi.length());
							list.add(result);
						}
					} else if (!twowords.isEmpty()) {
						for (String str : twowords) {
							result = yindaoci + str + noYinDaoCi.substring(2, noYinDaoCi.length());
							list.add(result);
						}
					} else if (!onewords.isEmpty()) {
						for (String str : onewords) {
							result = yindaoci + str + noYinDaoCi.substring(1, noYinDaoCi.length());
							list.add(result);
						}
					} else {
						result = resp;
						list.add(result);
					}

				} catch (PinyinException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return list;
			} else {
				list.add(yindaoci + noYinDaoCi);
				return list;
			}
		} else {
//			List<String> ret = this.getNameByPinyin(resp);
//			if ("1".equals(ret) || ret == null) {
//				list.add(resp);
//			} else {
//				list.add(ret);
//			}
		}
		return list;
	}

	public String getNamePinYin(String name) {
		try {
			String pinyin = PinyinHelper.convertToPinyinString(name, "", PinyinFormat.WITHOUT_TONE);
			return pinyin;
		} catch (PinyinException e) {
			e.printStackTrace();
			return null;
		}

	}

	// 拼音查人
	public List<String> getCnNameByPinyin(String name) {
		List<String> namelist = new ArrayList<String>();
		try {
			String pinyin = PinyinHelper.convertToPinyinString(name, "", PinyinFormat.WITHOUT_TONE);
			namelist = utils.getChineseName(pinyin);
		} catch (PinyinException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return namelist;
	}
}
