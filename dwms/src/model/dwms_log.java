package model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.jfinal.plugin.activerecord.Model;

public class dwms_log extends Model<dwms_log>{
	public static final dwms_log dao =new dwms_log();
	
	public List<dwms_log> getlogs() {
		List<dwms_log> list = dwms_log.dao.find("select * from dwms_log");
		return list;
	}
	
	//添加日志־
		public static void addlog(String name,String lx){
			String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new Date());
			dwms_log log=new dwms_log();
			log.set("operation_user", name).set("operation_genre",lx).set("operation_time", time);
			log.save();
		}
}
