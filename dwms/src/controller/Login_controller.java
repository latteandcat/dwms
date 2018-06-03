package controller;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Encoder;
import model.dwms_log;
import model.dwms_user;

import com.jfinal.core.Controller;
import com.jfinal.log.Log;

public class Login_controller extends Controller{
	public void index(){
		render("/d_user/user_login.html");
	}
	public void alogin() throws NoSuchAlgorithmException, UnsupportedEncodingException{
		String username = getPara("username");
		String password = getPara("password");
		System.out.println(password);
		String sql      = "select * from dwms_user where username = '"+username+"'";
		dwms_user user = dwms_user.dao.findFirst(sql);
		if (user==null) {
			System.out.println("登录失败");
			renderText("failed");
		} 
		else if (user!=null) {
			String oldpasswd = user.getStr("password");
			if(checkpassword(password, oldpasswd)==false){
				System.out.println("密码错误");
				renderText("pwfalse");
			}else{
				String poisition = user.getStr("poisition");
				getSession().setAttribute("user",user);
				dwms_log.addlog(user.getStr("name"),"登录");
				if("管理员".equals(poisition)){
					renderJson("1");
				}
				else if("仓库管理员".equals(poisition)){
					renderJson("2");
				}
				else if ("经销商".equals(poisition)){
					renderJson("3");
				}
			}
		}
		
	}

	/**
	 * @param str
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public String EncoderByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException{
			//确定计算方法
			System.out.println(str);
			MessageDigest md5=MessageDigest.getInstance("MD5");
			BASE64Encoder base64en = new BASE64Encoder();
			//加密后的字符串
			String newstr=base64en.encode(md5.digest(str.getBytes("utf-8")));
			System.out.println(newstr);
			return newstr;
	}
	/**判断用户密码是否正确
　　　　* @param newpasswd  用户输入的密码
　　　　 * @param oldpasswd  数据库中存储的密码－－用户密码的摘要
　　　　* @return
　　　　* @throws NoSuchAlgorithmException
　　　　* @throws UnsupportedEncodingException
　　　　*/
	public boolean checkpassword(String newpasswd,String oldpasswd) throws NoSuchAlgorithmException, UnsupportedEncodingException{
			if(EncoderByMd5(newpasswd).equals(oldpasswd))
				return true;
			else
				return false;
	}
	
}
