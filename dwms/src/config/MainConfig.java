package config;


import model.dwms_drug;
import model.dwms_inventory;
import model.dwms_log;
import model.dwms_order;
import model.dwms_record;
import model.dwms_supplier;
import model.dwms_user;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;

import controller.Admin_Controller;
import controller.Seller_Controller;
import controller.WarehouseAdmin_Controller;
import controller.Login_controller;

public class MainConfig extends JFinalConfig {
	@Override
	public void configConstant(Constants arg0) {
		// TODO Auto-generated method stub
		arg0.setDevMode(true);
	}
	@Override
	public void configHandler(Handlers arg0) {
		// TODO Auto-generated method stub
	}
	@Override
	public void configInterceptor(Interceptors arg0) {
		// TODO Auto-generated method stub
	}
	@Override
	public void configPlugin(Plugins arg0) {
		// TODO Auto-generated method stub
		C3p0Plugin c3p0Plugin = new C3p0Plugin("jdbc:mysql://127.0.0.1:3306/dwms?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull","root","121023");
		arg0.add(c3p0Plugin);
		// 采用DB+ActiveRecord模式  
		ActiveRecordPlugin arp = new ActiveRecordPlugin(c3p0Plugin);
		arg0.add(arp);
		// 进行DB映射  
		arp.addMapping("dwms_drug", dwms_drug.class);
		arp.addMapping("dwms_inventory", dwms_inventory.class);
		arp.addMapping("dwms_supplier", dwms_supplier.class);
		arp.addMapping("dwms_order", dwms_order.class);
		arp.addMapping("dwms_log", dwms_log.class);
		arp.addMapping("dwms_user", dwms_user.class);
		arp.addMapping("dwms_record", dwms_record.class);
		arp.setShowSql(true);
	}
	@Override
	//页面路由配置
	public void configRoute(Routes me) {
		// TODO Auto-generated method stub
		me.add("/",Login_controller.class);
		me.add("/login",Login_controller.class);
		me.add("/admin", Admin_Controller.class);
		me.add("/seller",Seller_Controller.class);
		me.add("/warehouseAdmin",WarehouseAdmin_Controller.class);
		
	}
	

}
