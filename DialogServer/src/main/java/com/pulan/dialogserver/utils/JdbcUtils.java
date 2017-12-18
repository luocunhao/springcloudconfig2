package com.pulan.dialogserver.utils;

import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.entity.Function;
import com.pulan.dialogserver.entity.Schedual;
import com.pulan.dialogserver.entity.SemanticSlots;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcUtils {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(JdbcUtils.class.getName());


	public List<Schedual> getSchedual() {
		String sql = "select * from schedual";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
		List<Schedual> reslo = new ArrayList<>();
		for (Map<String, Object> rs : list) {
			String open_id = rs.get("open_id").toString();
			String service = rs.get("service").toString();
			String event_date = rs.get("event_date").toString();
			String content = rs.get("content").toString();
			String text = rs.get("text").toString();
			Schedual schedual = new Schedual(open_id, service, event_date, content, text);
			reslo.add(schedual);
		}
		return reslo;
	}

	public List<Function> getFunctions() {
		logger.info("query ..start");
		List<Function> list = new ArrayList<Function>();
		String sql = "select * from function order by fun_id";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		logger.info("listsize" + rows.size());
		for (Map<String, Object> map : rows) {
			int fun_id =(int) map.get("fun_id");
			String funct =map.get("function").toString();
			Function function =new Function(fun_id, funct);
			list.add(function);
		}
		return list;
	}

	public List<String> getAskKu(int id) {
		String sql = "select ask_method from askku where ask_id = ?";
		List<String> list = jdbcTemplate.queryForList(sql,String.class,id);
		return list;
	}

	public List<String> getChineseName(String pinyin){
		String sql = "select cn_name from ai_user where pinyin = ?";
		List<String> list = new ArrayList<String>();
		try {
		list = jdbcTemplate.queryForList(sql,String.class,pinyin);
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("error " + e.getMessage());
		}
		return list;
		}
	//颜色插入或更新
	public int saveOrUpdateColor(String color_name,String imei){
		String sql = "replace into color (color_name,imei) values(?,?)";
		int ret = jdbcTemplate.update(sql, new Object[]{color_name,imei});
		return ret;
	}
	//查询imei颜色
	public String getColor(String imei){
		String sql = "select color_name from color where imei = ?";
		String ret = "";
		try {
			  ret =  jdbcTemplate.queryForObject(sql, String.class, imei);
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("error >>>>>>>>>>"+e.getMessage());
			ret = "";
		}
		return ret;
	}

	public List<JSONObject> getAllSchedual(String open_id) {
		// TODO Auto-generated method stub
		String sql ="SELECT * from schedual_app where open_id = ?";
		List<JSONObject> list = new ArrayList<JSONObject>();
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql,open_id);
		logger.info("rows"+rows.isEmpty());
		if(!rows.isEmpty()){
		for (Map<String, Object> map : rows) {
			JSONObject jo = new JSONObject();
			String openid = map.get("open_id").toString();
			String content = map.get("content").toString();
			String pushtime = map.get("pushtime").toString();
			jo.put("open_id", openid);
			jo.put("content",content);
			jo.put("pushtime", pushtime);
			list.add(jo);
		}
		return list;
		}else{
			logger.info("query mLync is null");
			return list;
		}

	
	}

	public int saveSchedual(String open_id, String content, String event_date) {
		// TODO Auto-generated method stub
		String sql = "insert into schedual_app (open_id,content,pushtime)values(?,?,?)";
		String eventdate = "";
		if(event_date.length()<10){
			LocalDateTime now = LocalDateTime.now();  
	        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");  
	        String nowStr = now .format(format);
	        eventdate = nowStr+event_date;
		}else{
			eventdate = event_date;
		}
		int ss =jdbcTemplate.update(sql, new Object[]{open_id,content,eventdate});
		return ss;
	}
	public String getMailByName(String name){
		String sql = "select email from base_info where cn_name = ?";
		return jdbcTemplate.queryForObject(sql, String.class, name);
	}
	//imei是否已注册
	public JSONObject getImei(String imei){
		JSONObject jo = new JSONObject();
		String sql = "select mail_name,password from ai_user where imei = ?";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, new Object[] {imei});
		if(!rows.isEmpty()){
			for (Map<String, Object> map : rows) {
				String mail_name = map.get("mail_name").toString();
				String password = map.get("password").toString();
				jo.put("mail_name", mail_name);
				jo.put("password",password);
				}
		}
			return jo;
	} 
	//注册imei号
	public int registImei(String imei,String email_name,String password){
		String sql = "update ai_user set imei = ? ,password = ? where mail_name = ?";
		try {
			  return jdbcTemplate.update(sql, new Object[] {imei,password,email_name});
			} catch (Exception e) {
				// TODO: handle exception
				return 0;
			}
	}

	//获取Mysql返回的邮件名称。
	public String getEmail(String name){
		String rest ="";
		String sql ="select email from ai_user where pinyin =?";
		try {
			rest = jdbcTemplate.queryForObject(sql,String.class,name);
		}catch (Exception e){
			rest ="";
		}
		return rest;
	}
	//获取Mysql返回的邮件名称。
	public String getEmailByCnName(String name){
		List<String>  rest =new ArrayList<>();
		String sql ="select email from ai_user where cn_name =?";
		try {
			 rest = jdbcTemplate.queryForList(sql,String.class,name);
		}catch (Exception e){

		}
		return rest.size()==0?"":rest.get(0);
	}

	//获取imei 通过emalname
	public String getImeiByEmail(String name) {
		String sql = "select imei from ai_user where email =?";
		String rest ;
		try {
			rest = jdbcTemplate.queryForObject(sql, String.class, name);
		} catch (Exception e) {
			rest = "";
		}
		return rest;
	}
	//获取唤醒词
	public String getAwakenWord(){
		String rest ="";
		String sql ="select content from verbal_trick where type ='hello' and category ='hyn' ";
		try {
			rest = jdbcTemplate.queryForObject(sql,String.class);
		}catch (Exception e){
			rest ="";
		}
		return rest;
	}
    //根据Username删除imei 
	public int updateImei(String username){
		String sql ="update ai_user set imei = ? where mail_name = ?";
		try {
			  return jdbcTemplate.update(sql, new Object[] {"",username});
			} catch (Exception e) {
				// TODO: handle exception
				return 0;
			}
	}
	//更改密码
	public int updatePassword(String username,String password){
		String sql = "update ai_user set password = ? where mail_name = ?";
		try {
			  return jdbcTemplate.update(sql, new Object[] {password,username});
			} catch (Exception e) {
				// TODO: handle exception
				return 0;
			}
	}

	/**
	 *
	 *
	 * @param intent 根据意图查询模版是否存在。
	 * @return
	 */
	public boolean isSemanticModelExist(String service,String intent){
		String rest ="";
		String sql ="select template_id from ai_dialog_template where template_service =? AND template_code =?";
		try {
			rest = jdbcTemplate.queryForObject(sql,String.class,service,intent);
		}catch (Exception e){
			logger.info("SemanticModelException:"+e.getMessage());
		}
		return !StringUtils.isEmpty(rest);
	}
	//根据语义意图和Service服务类型查询定义的语义槽模版
	public List<SemanticSlots> getSemanticSlot(String intent,String service){
		List<SemanticSlots> ssts = null;
		String sql ="select * from ai_dialog_slot where template_intent =? AND template_service =? ORDER BY slot_order ASC";
		try{
			ssts =jdbcTemplate.query(sql,new BeanPropertyRowMapper(SemanticSlots.class),intent,service);
		}catch (Exception e){
			logger.info("SemanticSlotException:"+e.getMessage());
		}
		return ssts;
	}
	//查询网页地址。
	public String getNetUrl(String webName){
		String rest;
		String sql ="select web_url from ai_web_url where url_name =? ";
		try {
			rest = jdbcTemplate.queryForObject(sql,String.class,webName);
		}catch (Exception e){
			rest ="";
		}
		return rest;
	}

}
