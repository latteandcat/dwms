package controller;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sun.misc.BASE64Encoder;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import model.dwms_drug;
import model.dwms_inventory;
import model.dwms_log;
import model.dwms_order;
import model.dwms_user;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Page;

public class Seller_Controller extends Controller {
	/*
	 * ******************************************************经销商首页，选订药品页面************************************************
	 * 
	 */
	public void index() {
		render("/d_seller/seller_main.html");
	}

	// 处方药列表的初始化
	public void cflist() {
		String[] array = getParaValues("cfArray[]");
		int pageNumber = Integer.parseInt(array[3]);
		int infNumber = Integer.parseInt(array[4]);
		Page<dwms_drug> drugs = dwms_drug.dao.paginate(pageNumber, infNumber,
				"select * ",
				"from dwms_drug where bigclass='处方药' and name like '%"
						+ array[0] + "%' and batch like '%" + array[1]
						+ "%' and smallclass like '%" + array[2] + "%'");
		renderJson(drugs);
	}

	// 非处方药列表的初始化
	public void fcflist() {
		String[] array = getParaValues("fcfArray[]");
		int pageNumber = Integer.parseInt(array[3]);
		int infNumber = Integer.parseInt(array[4]);
		Page<dwms_drug> drugs = dwms_drug.dao.paginate(pageNumber, infNumber,
				"select * ",
				"from dwms_drug where bigclass='非处方药' and name like '%"
						+ array[0] + "%' and batch like '%" + array[1]
						+ "%' and smallclass like '%" + array[2] + "%'");
		renderJson(drugs);
	}

	// 其他药列表的初始化
	public void olist() {
		String[] array = getParaValues("oArray[]");
		int pageNumber = Integer.parseInt(array[3]);
		int infNumber = Integer.parseInt(array[4]);
		Page<dwms_drug> drugs = dwms_drug.dao.paginate(pageNumber, infNumber,
				"select * ",
				"from dwms_drug where bigclass='其它' and name like '%"
						+ array[0] + "%' and batch like '%" + array[1]
						+ "%' and smallclass like '%" + array[2] + "%'");
		renderJson(drugs);
	}

	// 添加订单
	public void addOrder() {
		String[] array = getParaValues("oarray[]");// name,batch,size,"处方药",smallclass,num,price,totalprice,datetime,address,note
		
		dwms_order order = getModel(dwms_order.class, "a");
		dwms_user user = (dwms_user) getSession().getAttribute("user");
		String name = user.getStr("name");// 获得当前登录的经销商的名字
		List<dwms_inventory> inventorys = dwms_inventory.dao
				.find("select * from dwms_inventory where name='" + array[0]
						+ "'and batch ='" + array[1] + "'");
		if (inventorys.size() != 0) {
			dwms_inventory inventory = dwms_inventory.dao
					.findFirst("select * from dwms_inventory where name = '"
							+ array[0] + "'and batch = '" + array[1] + "'");
			Integer number = inventory.getInt("number");
			Integer number2 = Integer.parseInt(array[5]);
			if (number2 > number) {
				renderText(number.toString());// 经销商填写的数量大于当前库存，返回当前库存提醒经销商
			} else {
				Integer number3 = number - number2;
				System.out.println(number3);
				inventory.set("number", number3);
				List<dwms_order> orders = dwms_order.dao
						.find("select * from dwms_order where id in(select max(id) from dwms_order)");
				if (orders.size() == 0) {
					order.set("id", 1);
					order.set("name", array[0]);
					order.set("batch", array[1]);
					order.set("size", array[2]);
					order.set("bigclass", array[3]);
					order.set("smallclass", array[4]);
					order.set("num", array[5]);
					order.set("price", array[6]);
					order.set("totalprice", array[7]);
					order.set("seller", name);
					order.set("datetime", array[8]);
					order.set("address", array[9]);
					order.set("note", array[10]);
					order.set("status", "0");
				} else {
					order.set("id", orders.get(0).getInt("id") + 1);
					order.set("name", array[0]);
					order.set("batch", array[1]);
					order.set("size", array[2]);
					order.set("bigclass", array[3]);
					order.set("smallclass", array[4]);
					order.set("num", array[5]);
					order.set("price", array[6]);
					order.set("totalprice", array[7]);
					order.set("seller", name);
					order.set("datetime", array[8]);
					order.set("address", array[9]);
					order.set("note", array[10]);
					order.set("status", "0");
				}
				order.save();// 添加订单之后更新库存表的库存数量
				inventory.update();
				renderText("success");
			}
		} else {
			renderText("nothis");// 无该药品的库存
		}
	}

	/*
	 * ******************************************************我的订单页面******************************************************
	 * 
	 */
	public void myorder() {
		render("/d_seller/seller_myorder.html");
	}

	// 订单列表的初始化
	public void orderlist() {
		String[] array = getParaValues("orArray[]");
		if (array[4] == "") {
			array[4] = "9999-9-9";// 如果查询的终止时间没有传值的话，使日期最大化
		} else {
			array[4] += " 23:59:59";// 加上时间最大，包括终止时间的那一天
		}
		dwms_user user = (dwms_user) getSession().getAttribute("user");
		String name = user.getStr("name");// 获得当前登录的经销商的名字
		int pageNumber = Integer.parseInt(array[5]);
		int infNumber = Integer.parseInt(array[6]);
		Page<dwms_order> orders = null;
		if (array[2].equals("所有药")) {
			orders = dwms_order.dao.paginate(pageNumber, infNumber, "select *",
					"from dwms_order where name like '%" + array[0]
							+ "%' and batch like '%" + array[1] + "%' and "
							+ "datetime>='" + array[3] + "' and datetime<='"
							+ array[4] + "' and seller = '" + name
							+ "'and status='0'");
		} else if (array[2].equals("处方药")) {
			orders = dwms_order.dao.paginate(pageNumber, infNumber, "select *",
					"from dwms_order where name like '%" + array[0]
							+ "%' and batch like '%" + array[1] + "%' and "
							+ "datetime>='" + array[3] + "' and datetime<='"
							+ array[4] + "' and bigclass='处方药' and seller = '"
							+ name + "'and status='0'");
		} else if (array[2].equals("非处方药")) {
			orders = dwms_order.dao.paginate(pageNumber, infNumber, "select *",
					"from dwms_order where name like '%" + array[0]
							+ "%' and batch like '%" + array[1] + "%' and "
							+ "datetime>='" + array[3] + "' and datetime<='"
							+ array[4] + "' and bigclass='非处方药'and seller = '"
							+ name + "'and status='0'");
		} else if (array[2].equals("其它")) {
			orders = dwms_order.dao.paginate(pageNumber, infNumber, "select *",
					"from dwms_order where name like '%" + array[0]
							+ "%' and batch like '%" + array[1] + "%' and "
							+ "datetime>='" + array[3] + "' and datetime<='"
							+ array[4] + "' and bigclass='其它'and seller = '"
							+ name + "'and status='0'");
		}
		renderJson(orders);
	}
	//修改订单信息
	public void updateOrder() {// id,name,batch,num,price,totalprice,datetime,note,address
		String[] array = getParaValues("ordarray[]");
		dwms_order order = dwms_order.dao.findById(Integer.parseInt(array[0]));
		dwms_inventory inventory = dwms_inventory.dao
				.findFirst("select * from dwms_inventory where name= '"
						+ array[1] + "' and batch ='" + array[2] + "'");
		Integer number = inventory.getInt("number");// 当前库存中的数量
		Integer number1 = order.getInt("num");// 当前订单中的数量
		Integer number2 = Integer.parseInt(array[3]);// 修改后订单的数量
		Integer number3 = number1 + number;

		if (number2 >= number3) {
			renderText(number3.toString());
		} else {
			int num = number3 - number2;
			inventory.set("number", num);
			order.set("num", number2);
			order.set("totalprice", array[5]);
			order.set("datetime", array[6]);
			order.set("note", array[7]);
			order.set("address", array[8]);
			order.update();
			inventory.update();
			renderText("success");
		}
	}
	//取消订单
	public void DeleteOrder() {
		dwms_order order = dwms_order.dao.findById(getPara("id"));
		String name = order.getStr("name");
		String batch = order.getStr("batch");
		List<dwms_inventory> inventorys = dwms_inventory.dao
				.find("select * from dwms_inventory where name= '" + name
						+ "' and batch ='" + batch + "'");
		if (inventorys.size() != 0) {
			dwms_inventory i = dwms_inventory.dao
					.findFirst("select * from dwms_inventory where name= '"
							+ name + "' and batch ='" + batch + "'");
			Integer number = i.getInt("number");
			Integer number1 = order.getInt("num");
			Integer number2 = number + number1;
			i.set("number", number2);
			i.update();
		}
		dwms_order.dao.deleteById(getPara("id"));
		renderText("success");
	}
	//确认完成订单
	public void OverOrder(){
		dwms_order order = dwms_order.dao.findById(getPara("id"));
		order.set("status", "1");
		order.update();
		renderText("success");
	}
	// 导出我的订单
	public void exportorderexcel() {
		writeorderExcel();
		renderFile(new File("我的订单.xlsx"));
	}

	// 导出方法
	public void writeorderExcel() {
		dwms_user user = (dwms_user) getSession().getAttribute("user");
		String name = user.getStr("name");
		WritableWorkbook book = null;
		
		try {
			String filePath = "我的订单.xlsx";
			book = Workbook.createWorkbook(new File(filePath));
			WritableSheet sheet = book.createSheet("Sheet1", 0);

			List<dwms_order> orders = dwms_order.dao
					.find("select * from dwms_order where seller = '" + name
							+ "' and status = '0'");
			
			sheet.addCell(new Label(0, 0, "订单序号"));
			sheet.addCell(new Label(1, 0, "药品名"));
			sheet.addCell(new Label(2, 0, "药品批次"));
			sheet.addCell(new Label(3, 0, "药品规格"));
			sheet.addCell(new Label(4, 0, "所属药"));
			sheet.addCell(new Label(5, 0, "药品分类"));
			sheet.addCell(new Label(6, 0, "订货数量"));
			sheet.addCell(new Label(7, 0, "药品单价"));
			sheet.addCell(new Label(8, 0, "订单总价"));
			sheet.addCell(new Label(9, 0, "下单时间"));
			sheet.addCell(new Label(10, 0, "送货地址"));
			sheet.addCell(new Label(11, 0, "订单备注"));
			System.out.println("共导出"+orders.size()+"条数据");
			
			if (orders.size() > 0) {
				for (int i = 0; i < orders.size(); i++) {
					String order_id = Integer.toString(i + 1);
					double price = orders.get(i).getDouble("price");
					String price1 = Double.toString(price);
					double totalprice = orders.get(i).getDouble("totalprice");
					String totalprice1 = Double.toString(totalprice);
					Date date = orders.get(i).getDate("datetime");
					String orderdate=date.toString();
					String address=orders.get(i).getStr("address");
					String note =orders.get(i).getStr("note");
					sheet.addCell(new Label(0, i + 1, order_id));
					sheet.addCell(new Label(1, i + 1, orders.get(i).getStr(
							"name")));
					sheet.addCell(new Label(2, i + 1, orders.get(i).getStr(
							"batch")));
					sheet.addCell(new Label(3, i + 1, orders.get(i).getStr(
							"size")));
					sheet.addCell(new Label(4, i + 1, orders.get(i).getStr(
							"bigclass")));
					sheet.addCell(new Label(5, i + 1, orders.get(i).getStr(
							"smallclass")));
					sheet.addCell(new Label(6, i + 1, orders.get(i).getInt(
							"num").toString()));
					sheet.addCell(new Label(7, i + 1, price1));
					sheet.addCell(new Label(8, i + 1, totalprice1));
					sheet.addCell(new Label(9, i + 1, orderdate));
					sheet.addCell(new Label(10, i + 1, address));
					sheet.addCell(new Label(11, i + 1, note));
				}
			}
			
			// 写入数据并关闭文件
			book.write();
		} catch (Exception e) {
			renderText("f");
		} finally {
			if (book != null) {
				try {
					book.close();
					renderText("ss");
				} catch (Exception e) {
					renderText("f");
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * ******************************************************历史订单页面******************************************************
	 */

	public void oldorder() {
		render("/d_seller/seller_oldorder.html");
	}

	// 历史订单列表的初始化
	public void orderlist2() {
		String[] array = getParaValues("ordArray[]");
		if (array[4] == "") {
			array[4] = "9999-9-9";// 如果查询的终止时间没有传值的话，使日期最大化
		} else {
			array[4] += " 23:59:59";// 加上时间最大，包括终止时间的那一天
		}
		dwms_user user = (dwms_user) getSession().getAttribute("user");
		String name = user.getStr("name");// 获得当前登录的经销商的名字
		int pageNumber = Integer.parseInt(array[5]);
		int infNumber = Integer.parseInt(array[6]);
		Page<dwms_order> orders = null;
		if (array[2].equals("所有药")) {
			orders = dwms_order.dao.paginate(pageNumber, infNumber, "select *",
					"from dwms_order where name like '%" + array[0]
							+ "%' and batch like '%" + array[1] + "%' and "
							+ "datetime>='" + array[3] + "' and datetime<='"
							+ array[4] + "' and seller = '" + name
							+ "'and status='1'");
		} else if (array[2].equals("处方药")) {
			orders = dwms_order.dao.paginate(pageNumber, infNumber, "select *",
					"from dwms_order where name like '%" + array[0]
							+ "%' and batch like '%" + array[1] + "%' and "
							+ "datetime>='" + array[3] + "' and datetime<='"
							+ array[4] + "' and bigclass='处方药' and seller = '"
							+ name + "'and status='1'");
		} else if (array[2].equals("非处方药")) {
			orders = dwms_order.dao.paginate(pageNumber, infNumber, "select *",
					"from dwms_order where name like '%" + array[0]
							+ "%' and batch like '%" + array[1] + "%' and "
							+ "datetime>='" + array[3] + "' and datetime<='"
							+ array[4] + "' and bigclass='非处方药'and seller = '"
							+ name + "'and status='1'");
		} else if (array[2].equals("其它")) {
			orders = dwms_order.dao.paginate(pageNumber, infNumber, "select *",
					"from dwms_order where name like '%" + array[0]
							+ "%' and batch like '%" + array[1] + "%' and "
							+ "datetime>='" + array[3] + "' and datetime<='"
							+ array[4] + "' and bigclass='其它'and seller = '"
							+ name + "'and status='1'");
		}
		renderJson(orders);
	}
	//删除历史订单
	public void DeleteOrder2() {
		dwms_order.dao.deleteById(getPara("id"));
		renderText("success");
	}
	
	// 导出历史订单
		public void exportoldorderexcel() {
			writeoldorderExcel();
			renderFile(new File("历史订单.xlsx"));
		}

		// 导出方法
		public void writeoldorderExcel() {
			dwms_user user = (dwms_user) getSession().getAttribute("user");
			String name = user.getStr("name");
			WritableWorkbook book = null;
			
			try {
				String filePath = "历史订单.xlsx";
				book = Workbook.createWorkbook(new File(filePath));
				WritableSheet sheet = book.createSheet("Sheet1", 0);

				List<dwms_order> orders = dwms_order.dao
						.find("select * from dwms_order where seller = '" + name
								+ "' and status = '1'");
				
				sheet.addCell(new Label(0, 0, "订单序号"));
				sheet.addCell(new Label(1, 0, "药品名"));
				sheet.addCell(new Label(2, 0, "药品批次"));
				sheet.addCell(new Label(3, 0, "药品规格"));
				sheet.addCell(new Label(4, 0, "所属药"));
				sheet.addCell(new Label(5, 0, "药品分类"));
				sheet.addCell(new Label(6, 0, "订货数量"));
				sheet.addCell(new Label(7, 0, "药品单价"));
				sheet.addCell(new Label(8, 0, "订单总价"));
				sheet.addCell(new Label(9, 0, "下单时间"));
				sheet.addCell(new Label(10, 0, "送货地址"));
				sheet.addCell(new Label(11, 0, "订单备注"));
				System.out.println("共导出"+orders.size()+"条数据");
				
				if (orders.size() > 0) {
					for (int i = 0; i < orders.size(); i++) {
						String order_id = Integer.toString(i + 1);
						double price = orders.get(i).getDouble("price");
						String price1 = Double.toString(price);
						double totalprice = orders.get(i).getDouble("totalprice");
						String totalprice1 = Double.toString(totalprice);
						Date date = orders.get(i).getDate("datetime");
						String orderdate=date.toString();
						String address=orders.get(i).getStr("address");
						String note =orders.get(i).getStr("note");
						sheet.addCell(new Label(0, i + 1, order_id));
						sheet.addCell(new Label(1, i + 1, orders.get(i).getStr(
								"name")));
						sheet.addCell(new Label(2, i + 1, orders.get(i).getStr(
								"batch")));
						sheet.addCell(new Label(3, i + 1, orders.get(i).getStr(
								"size")));
						sheet.addCell(new Label(4, i + 1, orders.get(i).getStr(
								"bigclass")));
						sheet.addCell(new Label(5, i + 1, orders.get(i).getStr(
								"smallclass")));
						sheet.addCell(new Label(6, i + 1, orders.get(i).getInt(
								"num").toString()));
						sheet.addCell(new Label(7, i + 1, price1));
						sheet.addCell(new Label(8, i + 1, totalprice1));
						sheet.addCell(new Label(9, i + 1, orderdate));
						sheet.addCell(new Label(10, i + 1, address));
						sheet.addCell(new Label(11, i + 1, note));
					}
				}
				
				// 写入数据并关闭文件
				book.write();
			} catch (Exception e) {
				renderText("f");
			} finally {
				if (book != null) {
					try {
						book.close();
						renderText("ss");
					} catch (Exception e) {
						renderText("f");
						e.printStackTrace();
					}
				}
			}
		}

	/*
	 * ******************************************************两个页面公用的方法***************************************************
	 */

	// 获取用户信息
	public void getuser() {
		dwms_user user = (dwms_user) getSession().getAttribute("user");
		renderJson(user);
	}

	// 比较密码
	public void mmpd() throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		dwms_user user = (dwms_user) getSession().getAttribute("user");
		if (checkpassword(getPara("str"), user.getStr("password")))
			renderText("true");
		else
			renderText("false");
	}

	// 修改密码
	public void uppass() throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		String password = getPara("password");
		dwms_user user = (dwms_user) getSession().getAttribute("user");
		dwms_log.addlog(user.getStr("name"), "修改密码");
		dwms_user user2 = dwms_user.dao.findById(user.getInt("id"));
		String newpassword = EncoderByMd5(password);
		user2.set("password", newpassword);
		user2.update();
		renderText("true");
	}

	// 获取当前用户姓名
	public void getname() {
		dwms_user user = (dwms_user) getSession().getAttribute("user");
		if (user != null) {
			renderJson(user);
		} else {
			renderText("error");
		}
	}

	// 退出
	public void logout() {
		dwms_user user = (dwms_user) getSession().getAttribute("user");
		if (user != null) {
			renderJson(user);
			dwms_log.addlog(user.getStr("name"), "退出登录");
			getSession().invalidate();
		} else {
			renderText("error");
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
