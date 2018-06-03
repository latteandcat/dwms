package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sun.misc.BASE64Encoder;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import model.dwms_drug;
import model.dwms_inventory;
import model.dwms_log;
import model.dwms_order;
import model.dwms_record;
import model.dwms_supplier;
import model.dwms_user;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.upload.UploadFile;

public class WarehouseAdmin_Controller extends Controller{
	
		// ************************************首页************************************
	
		public void index(){
			render("/d_warehouseAdmin/warehouseAdmin_main.html");
		}
		public void  outofDatelist(){
			String[] array= getParaValues("ggArray[]");
			int pageNumber = Integer.parseInt(array[0]);
			int infNumber = Integer.parseInt(array[1]);
			Date now = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_MONTH, -7); 
			Date dBefore = calendar.getTime();
			DateFormat d1 = DateFormat.getDateInstance(); 
			String strnow = d1.format(dBefore);
			Page<dwms_inventory> outs = dwms_inventory.dao.paginate(pageNumber, infNumber, "select * ", "from dwms_inventory where expiry_date <= '"+ strnow+"'");
			renderJson(outs);
		}
		
		public void notEnoughlist(){
			String[] array= getParaValues("gArray[]");
			int pageNumber = Integer.parseInt(array[0]);
			int infNumber = Integer.parseInt(array[1]);
			Page<dwms_inventory> nots = dwms_inventory.dao.paginate(pageNumber, infNumber, "select * ", "from dwms_inventory where number <= 1000 and number >0");
			System.out.println(nots.getList().isEmpty());
			renderJson(nots);
		}
		
		// ************************************库存管理************************************
		public void inventory(){
			render("/d_warehouseAdmin/warehouseAdmin_inventory.html");
		}
		
		public void inventorylist(){
			String[] array = getParaValues("gArray[]");
			int pageNumber = Integer.parseInt(array[3]) ;
			int infNumber = Integer.parseInt(array[4]);
			String product_date = array[2];
			Page<dwms_inventory> inventorys;
			if (product_date==""){
				inventorys = dwms_inventory.dao.paginate(pageNumber, infNumber, "select *",
						"from dwms_inventory where name like '%"+array[0]+
						"%' and batch like '%"+array[1]+"%' ");
			}else{
				inventorys = dwms_inventory.dao.paginate(pageNumber, infNumber, "select *",
						"from dwms_inventory where name like '%"+array[0]+
						"%' and batch like '%"+array[1]+"%' and product_date = '"+array[2]+"'");
			}
			renderJson(inventorys);
		}
		public void dlist(){
			String[] array = getParaValues("dArray[]");
			int pageNumber = Integer.parseInt(array[1]);
			Page<dwms_drug> drugs = dwms_drug.dao.paginate(pageNumber, 8,"select * ", "from dwms_drug where name like '%"+array[0]+"%'");
			System.out.println(drugs);
			renderJson(drugs);
		}
		public void addInventory(){
			String[] array = getParaValues("iArray[]");
			List<dwms_inventory> inventorys = dwms_inventory.dao.find("select * from dwms_inventory where name= '"+array[0]+ "' and batch='"+array[1]+"'");
			if(inventorys.size()==0){
				renderText("failed");
			}else{
				//添加库存
				dwms_inventory inventory = dwms_inventory.dao.findFirst("select * from dwms_inventory where name= '"+array[0]+ "' and batch='"+array[1]+"'");
				int number =  inventory.getInt("number");
				int number1= Integer.parseInt(array[2]);
				int number2= number1+number;
				inventory.set("number", number2);
				inventory.update();
				//添加入库记录
				dwms_user user= (dwms_user) getSession().getAttribute("user");
				String name = user.getStr("name");
				dwms_record record=getModel(dwms_record.class);
				List<dwms_record> records = dwms_record.dao.find("select * from dwms_record where id in(select max(id) from dwms_record)");
				if(records.size()==0){
					record.set("id", 1);
					record.set("drug_name", array[0]);
					record.set("batch", array[1]);
					record.set("number", array[2]);
					record.set("date", array[3]);
					record.set("registrar", name);
				}else{
					record.set("id", records.get(0).getInt("id")+1);
					record.set("drug_name", array[0]);
					record.set("batch", array[1]);
					record.set("number", array[2]);
					record.set("date", array[3]);
					record.set("registrar", name);
				}
				record.save();
				renderText("success");
			}
		}
		public void addnewInventory(){
			String[] array = getParaValues("iArray[]");
			List<dwms_inventory> inventorys = dwms_inventory.dao.find("select * from dwms_inventory where name= '"+array[0]+ "' and batch='"+array[1]+"'");
			if(inventorys.size()!=0){
				renderText("failed");
			}else{
				//添加库存
				dwms_inventory inventory=getModel(dwms_inventory.class);
				List<dwms_inventory> inventoryss = dwms_inventory.dao.find("select * from dwms_inventory where id in(select max(id) from dwms_inventory)");
				if(inventoryss.size()==0){
					inventory.set("id", 1);
					inventory.set("name", array[0]);
					inventory.set("batch", array[1]);
					inventory.set("number", array[2]);
					inventory.set("product_date", array[4]);
					inventory.set("expiry_date", array[5]);
					inventory.set("note", array[6]);
				}else{
					inventory.set("id", inventoryss.get(0).getInt("id")+1);
					inventory.set("name", array[0]);
					inventory.set("batch", array[1]);
					inventory.set("number", array[2]);
					inventory.set("product_date", array[4]);
					inventory.set("expiry_date", array[5]);
					inventory.set("note", array[6]);
				}
				inventory.save();
				//添加入库记录
				dwms_user user= (dwms_user) getSession().getAttribute("user");
				String name = user.getStr("name");
				dwms_record record=getModel(dwms_record.class);
				List<dwms_record> records = dwms_record.dao.find("select * from dwms_record where id in(select max(id) from dwms_record)");
				if(records.size()==0){
					record.set("id", 1);
					record.set("drug_name", array[0]);
					record.set("batch", array[1]);
					record.set("number", array[2]);
					record.set("date", array[3]);
					record.set("registrar", name);
				}else{
					record.set("id", records.get(0).getInt("id")+1);
					record.set("drug_name", array[0]);
					record.set("batch", array[1]);
					record.set("number", array[2]);
					record.set("date", array[3]);
					record.set("registrar", name);
				}
				record.save();
				
				renderText("success");
			}
		}
		public void deleteInventory(){
			dwms_inventory ins = dwms_inventory.dao.findById(getPara("id"));
			int number = ins.getInt("number");
			if(number==0){
				dwms_inventory.dao.deleteById(getPara("id"));
				renderText("success");
			}else{
				renderText("failed");
			}
		}
		
		// ************************************药品管理************************************

		public void drug(){
			render("/d_warehouseAdmin/warehouseAdmin_drug.html");
		}
		//处方药
		public void cflist(){
			String[] array = getParaValues("cfArray[]");
			int pageNumber = Integer.parseInt(array[4]);
			int infNumber = Integer.parseInt(array[5]);
			Page<dwms_drug> drugs = dwms_drug.dao.paginate(pageNumber, infNumber, "select * ", "from dwms_drug where bigclass='处方药' and name like '%" + array[0]
					+ "%' and batch like '%"+array[1] + "%' and smallclass like '%"+ array[3]+"%' and location like '%"+array[2]+"%'");
			//SELECT * FROM dwms_drug where name like  '%阿%' and batch like '%2017%' and smallclass like '%抗%' and location like '%A%'
			renderJson(drugs);
		}
		//非处方药
		public void fcflist(){
			String[] array = getParaValues("fcfArray[]");
			int pageNumber = Integer.parseInt(array[4]);
			int infNumber = Integer.parseInt(array[5]);
			Page<dwms_drug> drugs = dwms_drug.dao.paginate(pageNumber, infNumber, "select * ", "from dwms_drug where bigclass='非处方药' and name like '%" + array[0]
					+ "%' and batch like '%"+array[1] + "%' and smallclass like '%"+ array[3]+"%' and location like '%"+array[2]+"%'");
			//SELECT * FROM dwms_drug where name like  '%阿%' and batch like '%2017%' and smallclass like '%抗%' and location like '%A%'
			renderJson(drugs);
		}
		//其他药
		public void olist(){
			String[] array = getParaValues("oArray[]");
			int pageNumber = Integer.parseInt(array[4]);
			int infNumber = Integer.parseInt(array[5]);
			Page<dwms_drug> drugs = dwms_drug.dao.paginate(pageNumber, infNumber, "select * ", "from dwms_drug where bigclass='其它' and name like '%" + array[0]
					+ "%' and batch like '%"+array[1] + "%' and smallclass like '%"+ array[3]+"%' and location like '%"+array[2]+"%'");
			//SELECT * FROM dwms_drug where name like  '%阿%' and batch like '%2017%' and smallclass like '%抗%' and location like '%A%'
			renderJson(drugs);
		}
		public void suplist(){
			String[] array = getParaValues("supArray[]");
			int pageNumber = Integer.parseInt(array[1]);
			Page<dwms_supplier> suppliers = dwms_supplier.dao.paginate(pageNumber, 8,"select * ", "from dwms_supplier where supplier_name like '%"+array[0]+"%'");
			System.out.println(suppliers);
			renderJson(suppliers);
		}
		//var cfdArray=new Array(name,size,"处方药",smallclass,price,supplier,batch,functions,taboo,location,reaction,note);
		public void addDrug(){
			String[] array = getParaValues("darray[]");
			String name = array[0];
			String batch = array[6];
			List<dwms_drug> adrug = dwms_drug.dao.find("select * from dwms_drug where name= '"+ name +"'and batch = '"+batch+"'");
			if(adrug.isEmpty()){
				dwms_drug drug=getModel(dwms_drug.class,"a");
				List<dwms_drug> drugs = dwms_drug.dao.find("select * from dwms_drug where id in(select max(id) from dwms_drug)");
				if(drugs.size()==0){
					drug.set("id", 1);
					drug.set("name",array[0]);
					drug.set("size", array[1]);
					drug.set("bigclass", array[2]);
					drug.set("smallclass", array[3]);
					drug.set("price", array[4]);
					drug.set("supplier", array[5]);
					drug.set("batch", array[6]);
					drug.set("functions", array[7]);
					drug.set("taboo",array[8]);
					drug.set("location", array[9]);
					drug.set("reaction",array[10]);
					drug.set("note", array[11]);
				}else{
					drug.set("id", drugs.get(0).getInt("id")+1);
					drug.set("name",array[0]);
					drug.set("size", array[1]);
					drug.set("bigclass", array[2]);
					drug.set("smallclass", array[3]);
					drug.set("price", array[4]);
					drug.set("supplier", array[5]);
					drug.set("batch", array[6]);
					drug.set("functions", array[7]);
					drug.set("taboo",array[8]);
					drug.set("location", array[9]);
					drug.set("reaction",array[10]);
					drug.set("note", array[11]);
				}
				drug.save();
				renderText("success");
			}else{
				renderText("failed");
			}
			
		}
		//删除药品
		public void deleteDrug(){
			dwms_drug d = dwms_drug.dao.findById(getPara("id"));
			String name=d.getStr("name");
			String batch=d.getStr("batch");
			List<dwms_inventory> ins =dwms_inventory.dao.find("select * from dwms_inventory where name='"+name+"'and batch='"+batch+"'");
			//无库存信息时才能删除药品
			if(ins.size()==0){
				dwms_drug.dao.deleteById(getPara("id"));
				renderText("success");
			}else{
				renderText("failed");
			}
		}
		
		public void updateDrug(){
			String[] array = getParaValues("upArray[]");
			int id = Integer.parseInt(array[0]);
			dwms_drug drug = dwms_drug.dao.findById(id);
			drug.set("id", id);
			drug.set("name", array[1]);
			drug.set("size", array[2]);
			drug.set("price", array[3]);
			drug.set("batch", array[4]);
			drug.set("supplier", array[5]);
			drug.set("location", array[6]);
			drug.update();
			renderNull();
		}
		
		// ************************************出入库************************************

		public void inout(){
			render("/d_warehouseAdmin/warehouseAdmin_inout.html");
		}
		
		public void inlist(){
			String[] array = getParaValues("inArray[]");
			if(array[3]==""){
				array[3]="9999-9-9";//如果查询的终止时间没有传值的话，使日期最大化
			}else{
				array[3]+=" 23:59:59";//加上时间最大，包括终止时间的那一天
			}
			int pageNumber = Integer.parseInt(array[4]);
			int infNumber = Integer.parseInt(array[5]);
			Page<dwms_record> records= dwms_record.dao.paginate(pageNumber, infNumber, "SELECT *"
						,"FROM dwms_record where "+"date>='"+array[2]+
							"' and date <='"+array[3]+"' and drug_name like '%"+array[0]+"%' and batch like '%"+array[1]+"%'");
			renderJson(records);
		}
		public void outlist(){
			String[] array = getParaValues("outArray[]");
			if(array[3]==""){
				array[3]="9999-9-9";//如果查询的终止时间没有传值的话，使日期最大化
			}else{
				array[3]+=" 23:59:59";//加上时间最大，包括终止时间的那一天
			}
			int pageNumber = Integer.parseInt(array[4]);
			int infNumber = Integer.parseInt(array[5]);
			Page<dwms_order> orders= dwms_order.dao.paginate(pageNumber, infNumber, "SELECT *"
						,"FROM dwms_order where "+"datetime>='"+array[2]+
							"' and datetime <='"+array[3]+"' and name like '%"+array[0]+"%' and batch like '%"+array[1]+"%'");
			renderJson(orders);
		}
		public void deleteRecord(){
			dwms_record.dao.deleteById(getPara("id"));
			renderText("success");
		}
		
		// ************************************供应商管理************************************

		public void supplier(){
			render("/d_warehouseAdmin/warehouseAdmin_supplier.html");
		}
		
		public void supplierlist(){
			String[] array = getParaValues("gysArray[]");
			int pageNumber = Integer.parseInt(array[2]) ;
			int infNumber = Integer.parseInt(array[3]);
			Page<dwms_supplier> suppliers = dwms_supplier.dao.paginate(pageNumber, infNumber, "SELECT *", 
					"FROM dwms_supplier where supplier_name like '%"+array[0]
							+"%' and contact like '%"+array[1]+"%'");
			renderJson(suppliers);
		}
		public void deleteSupplier(){
			dwms_supplier.dao.deleteById(getPara("id"));
			renderText("success");
		}
		public void updateSupplier(){
			String[] array = getParaValues("upArray[]");
			int id = Integer.parseInt(array[0]);
			dwms_supplier supplier = dwms_supplier.dao.findById(id);
			supplier.set("id", id);
			supplier.set("supplier_name", array[1]);
			supplier.set("supplier_phone", array[2]);
			supplier.set("contact", array[3]);
			supplier.set("address", array[4]);
			supplier.set("note", array[5]);
			supplier.update();
			renderNull();
		}
		public void addSupplier(){
			String[] array = getParaValues("sArray[]");
			String name = array[0];
			List<dwms_supplier> asupplier = dwms_supplier.dao.find("select * from dwms_supplier where supplier_name = '"+ name +"' ");
			if(asupplier.isEmpty()){
				dwms_supplier supplier=getModel(dwms_supplier.class,"a");
				List<dwms_supplier> suppliers = dwms_supplier.dao.find("select * from dwms_supplier where id in(select max(id) from dwms_supplier)");
				if(suppliers.size()==0){
					supplier.set("id", 1);
					supplier.set("supplier_name", array[0]);
					supplier.set("supplier_phone", array[1]);
					supplier.set("contact", array[2]);
					supplier.set("address", array[3]);
					supplier.set("note", array[4]);
				}else{
					supplier.set("id", suppliers.get(0).getInt("id")+1);
					supplier.set("supplier_name", array[0]);
					supplier.set("supplier_phone", array[1]);
					supplier.set("contact", array[2]);
					supplier.set("address", array[3]);
					supplier.set("note", array[4]);
				}
				supplier.save();
				renderText("success");
			}else{
				renderText("failed");
			}
			
		}
		/*
		 * ************************************导入EXCEL************************************
		 */
		
		public void getupload() throws BiffException, IOException{
			int error=0;
			Workbook book = null;
			UploadFile uploadFile=	getFile("file123");
		    String fileName=uploadFile.getOriginalFileName();
		    File file=uploadFile.getFile(); 
		    System.out.println(file);
		    InputStream stream = new FileInputStream(file);
		    book = Workbook.getWorkbook(stream);
		    Sheet sheet = book.getSheet(0);
			System.out.println("excel的单元个行数是："+sheet.getRows());
		    System.out.println(fileName);
			  for (int i = 1; i < sheet.getRows(); i++) {
			  		String supplier_name=sheet.getCell(0, i).getContents();	
			  		List<dwms_supplier> d = dwms_supplier.dao.find("select * from dwms_supplier where supplier_name = '"+supplier_name+"'");
		    		if(d.size()!=0){
		    			error++;
		    			continue;
		    		}
		    		String supplier_phone = sheet.getCell(1, i).getContents();
					String contact = sheet.getCell(2, i).getContents();
					String address = sheet.getCell(3, i).getContents();
					String note = sheet.getCell(4, i).getContents();
					dwms_supplier s=new dwms_supplier();
					s.set("supplier_name",supplier_name)
					     .set("supplier_phone", supplier_phone)
					     .set("contact",contact)
					     .set("address", address)
					     .set("note", note).save();
				}
				Map<String,String> map = new HashMap<String, String>(); 
				String errors=String.valueOf(error);
				System.out.println("error"+errors);
				map.put("status", "success");
				map.put("error", errors);
				renderJson(map);
			}

		public void importdrug() throws BiffException, IOException{
			int error=0;
			Workbook book = null;
			UploadFile uploadFile=	getFile("file123");
		    String fileName=uploadFile.getOriginalFileName();
		    File file=uploadFile.getFile(); 
		    System.out.println(file);
		    InputStream stream = new FileInputStream(file);
		    book = Workbook.getWorkbook(stream);
		    Sheet sheet = book.getSheet(0);
			System.out.println("excel的单元个行数是："+sheet.getRows());
		    System.out.println(fileName);
			  for (int i = 1; i < sheet.getRows(); i++) {
			  		String name=sheet.getCell(0, i).getContents();	
		    		String batch = sheet.getCell(1, i).getContents();
		    		List<dwms_drug> d = dwms_drug.dao.find("select * from dwms_drug where name = '"+name+"' and batch = '"+batch+"'");
		    		if(d.size()!=0){
		    			error++;
		    			continue;
		    		}
					String size = sheet.getCell(2, i).getContents();
					String bigclass = sheet.getCell(3, i).getContents();
					if(bigclass.equals("处方药")==false && bigclass.equals("非处方药")==false && bigclass.equals("其它")==false ){
						error++;
						continue;
					}
					
					String smallclass = sheet.getCell(4, i).getContents();
					String price = sheet.getCell(5, i).getContents();
					String supplier = sheet.getCell(6, i).getContents();
					String functions = sheet.getCell(7, i).getContents();
					String taboo = sheet.getCell(8, i).getContents();
					String location = sheet.getCell(9, i).getContents();
					char l=location.charAt(0);
					if(bigclass.equals("处方药")){
						if(l!='A' && l!='B' && l!='C' && l!='D'){
							error++;
							continue;
						}
					}
					if(bigclass.equals("非处方药")){
						if(l!='E' && l!='F' && l!='G' && l!='H'){
							error++;
							continue;
						}
					}
					if(bigclass.equals("其它")){
						if(l!='X' && l!='Y' && l!='M' && l!='N'){
							error++;
							continue;
						}
					}
					String reaction = sheet.getCell(10, i).getContents();
					String note = sheet.getCell(11, i).getContents();
					dwms_drug s=new dwms_drug();
					s.set("name",name)
					     .set("batch", batch)
					     .set("size",size)
					     .set("bigclass", bigclass)
					     .set("smallclass", smallclass)
					     .set("price", price)
					     .set("supplier", supplier)
					     .set("functions", functions)
					     .set("taboo", taboo)
					     .set("location", location)
					     .set("reaction", reaction)
					     .set("note", note)
					     .save();
				}
				Map<String,String> map = new HashMap<String, String>(); 
				String errors=String.valueOf(error);
				System.out.println("error"+errors);
				map.put("error", errors);
				map.put("status", "success");
				renderJson(map);
			}
		
		/*
		 * ************************************导出EXCEL************************************
		 */
			// 导出药品信息
			public void exportdrugexcel() {
				writedrugExcel();
				renderFile(new File("药品信息.xls"));
			}

			// 导出方法
			public void writedrugExcel() {
				
				WritableWorkbook book = null;
				
				try {
					String filePath = "药品信息.xls";
					book = Workbook.createWorkbook(new File(filePath));
					WritableSheet sheet = book.createSheet("Sheet1", 0);

					List<dwms_drug> drugs = dwms_drug.dao
							.find("select * from dwms_drug");
					
					sheet.addCell(new Label(0, 0, "药品序号"));
					sheet.addCell(new Label(1, 0, "药品名"));
					sheet.addCell(new Label(2, 0, "药品批次"));
					sheet.addCell(new Label(3, 0, "药品规格"));
					sheet.addCell(new Label(4, 0, "所属药"));
					sheet.addCell(new Label(5, 0, "药品分类"));
					sheet.addCell(new Label(6, 0, "存放位置"));
					sheet.addCell(new Label(7, 0, "药品单价"));
					sheet.addCell(new Label(8, 0, "供应商"));
					/*sheet.addCell(new Label(9, 0, "功能主治"));
					sheet.addCell(new Label(10, 0, "禁忌"));
					sheet.addCell(new Label(11, 0, "不良反应"));
					sheet.addCell(new Label(12, 0, "药品备注"));*/
					System.out.println("共导出"+drugs.size()+"条数据");
					
					if (drugs.size() > 0) {
						for (int i = 0; i < drugs.size(); i++) {
							String drug_id = Integer.toString(i + 1);
							double price = drugs.get(i).getDouble("price");
							String price1 = Double.toString(price);
							sheet.addCell(new Label(0, i + 1, drug_id));
							sheet.addCell(new Label(1, i + 1, drugs.get(i).getStr(
									"name")));
							sheet.addCell(new Label(2, i + 1, drugs.get(i).getStr(
									"batch")));
							sheet.addCell(new Label(3, i + 1, drugs.get(i).getStr(
									"size")));
							sheet.addCell(new Label(4, i + 1, drugs.get(i).getStr(
									"bigclass")));
							sheet.addCell(new Label(5, i + 1, drugs.get(i).getStr(
									"smallclass")));
							sheet.addCell(new Label(6, i + 1, drugs.get(i).getStr(
									"location")));
							sheet.addCell(new Label(7, i + 1, price1));
							sheet.addCell(new Label(8, i + 1, drugs.get(i).getStr(
									"supplier")));
							/*sheet.addCell(new Label(9, i + 1, drugs.get(i).getStr(
									"functions")));
							sheet.addCell(new Label(10, i + 1, drugs.get(i).getStr(
									"taboo")));
							sheet.addCell(new Label(11, i + 1, drugs.get(i).getStr(
									"reaction")));
							sheet.addCell(new Label(12, i + 1, drugs.get(i).getStr(
									"note")));*/
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

			// 导出库存信息
			public void exportinventoryrexcel() {
				writeinventoryExcel();
				Date now=new Date();
				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
				String strnow = sdf.format(now);
				renderFile(new File(strnow+"库存信息.xls"));
			}

			// 导出方法
			public void writeinventoryExcel() {
				WritableWorkbook book = null;
				
				try {
					Date now=new Date();
					SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
					String strnow = sdf.format(now);
					String filePath = strnow+"库存信息.xls";
					book = Workbook.createWorkbook(new File(filePath));
					WritableSheet sheet = book.createSheet("Sheet1", 0);

					List<dwms_inventory> ins = dwms_inventory.dao
							.find("select * from dwms_inventory");
					
					sheet.addCell(new Label(0, 0, "订单序号"));
					sheet.addCell(new Label(1, 0, "药品名"));
					sheet.addCell(new Label(2, 0, "药品批次"));
					sheet.addCell(new Label(3, 0, "库存数量"));
					sheet.addCell(new Label(4, 0, "生产日期"));
					sheet.addCell(new Label(5, 0, "失效日期"));
					sheet.addCell(new Label(6, 0, "备注"));
				
					System.out.println("共导出"+ins.size()+"条数据");
					
					if (ins.size() > 0) {
						for (int i = 0; i < ins.size(); i++) {
							String ins_id = Integer.toString(i + 1);
							Date date = ins.get(i).getDate("product_date");
							String product_date=date.toString();
							Date date1 = ins.get(i).getDate("expiry_date");
							String expiry_date=date.toString();
							
							sheet.addCell(new Label(0, i + 1, ins_id));
							sheet.addCell(new Label(1, i + 1, ins.get(i).getStr(
									"name")));
							sheet.addCell(new Label(2, i + 1, ins.get(i).getStr(
									"batch")));
							sheet.addCell(new Label(3, i + 1, ins.get(i).getInt(
									"number").toString()));
							sheet.addCell(new Label(4, i + 1, product_date));
							sheet.addCell(new Label(5, i + 1, expiry_date));
							sheet.addCell(new Label(6, i + 1, ins.get(i).getStr(
									"note")));
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
			// 导出供应商信息
			public void exportsupplierexcel() {
				writesupplierExcel();
				renderFile(new File("供应商信息.xls"));
			}

			// 导出方法
			public void writesupplierExcel() {
				WritableWorkbook book = null;
				try {
					String filePath = "供应商信息.xls";
					book = Workbook.createWorkbook(new File(filePath));
					WritableSheet sheet = book.createSheet("Sheet1", 0);

					List<dwms_supplier> s = dwms_supplier.dao
							.find("select * from dwms_supplier");
					
					sheet.addCell(new Label(0, 0, "供应商序号"));
					sheet.addCell(new Label(1, 0, "供应商名"));
					sheet.addCell(new Label(2, 0, "供应商联系方式"));
					sheet.addCell(new Label(3, 0, "供应商联系人"));
					sheet.addCell(new Label(4, 0, "供应商地址"));
					sheet.addCell(new Label(5, 0, "备注"));
					System.out.println("共导出"+s.size()+"条数据");
					
					if (s.size() > 0) {
						for (int i = 0; i < s.size(); i++) {
							String s_id = Integer.toString(i + 1);
							sheet.addCell(new Label(0, i + 1, s_id));
							sheet.addCell(new Label(1, i + 1, s.get(i).getStr(
									"supplier_name")));
							sheet.addCell(new Label(2, i + 1, s.get(i).getStr(
									"supplier_phone")));
							sheet.addCell(new Label(3, i + 1, s.get(i).getStr(
									"contact")));
							sheet.addCell(new Label(4, i + 1, s.get(i).getStr(
									"address")));
							sheet.addCell(new Label(5, i + 1, s.get(i).getStr(
									"note")));
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
		
			// 导出入库记录
			public void exportin() {
				/*String[] array = getParaValues("dArray[]");
				String a = array[0];
				String b = array[1];*/
				writeinExcel();
				renderFile(new File("入库记录.xls"));
			}

			// 导出方法
			public void writeinExcel() {
				dwms_user user = (dwms_user) getSession().getAttribute("user");
				String name = user.getStr("name");
				WritableWorkbook book = null;
				
				try {
					String filePath = "入库记录.xls";
					book = Workbook.createWorkbook(new File(filePath));
					WritableSheet sheet = book.createSheet("Sheet1", 0);
					/*if(a==""){
						b="9999-9-9";//如果查询的终止时间没有传值的话，使日期最大化
					}else{
						b+=" 23:59:59";//加上时间最大，包括终止时间的那一天
					}*/
					List<dwms_record> r = dwms_record.dao
							.find("select * from dwms_record where registrar = '" + name
									+ "' ");
					
					sheet.addCell(new Label(0, 0, "记录序号"));
					sheet.addCell(new Label(1, 0, "药品名"));
					sheet.addCell(new Label(2, 0, "药品批次"));
					sheet.addCell(new Label(3, 0, "入库数量"));
					sheet.addCell(new Label(4, 0, "入库时间"));
					sheet.addCell(new Label(5, 0, "操作人"));
					System.out.println("共导出"+r.size()+"条数据");
					
					if (r.size() > 0) {
						for (int i = 0; i < r.size(); i++) {
							String in_id = Integer.toString(i + 1);
							Date date = r.get(i).getDate("date");
							String date1=date.toString();
							int num=r.get(i).getInt("number");
							String number=String.valueOf(num);
							sheet.addCell(new Label(0, i + 1, in_id));
							sheet.addCell(new Label(1, i + 1, r.get(i).getStr(
									"drug_name")));
							sheet.addCell(new Label(2, i + 1, r.get(i).getStr(
									"batch")));
							sheet.addCell(new Label(3, i + 1, number));
							sheet.addCell(new Label(4, i + 1, date1));
							sheet.addCell(new Label(5, i + 1, r.get(i).getStr(
									"registrar")));
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
			// 导出订货记录
			public void exportout() {
				writeoutExcel();
				renderFile(new File("订货记录.xls"));
			}

			// 导出方法
			public void writeoutExcel() {
				dwms_user user = (dwms_user) getSession().getAttribute("user");
				String name = user.getStr("name");
				WritableWorkbook book = null;
				
				try {
					String filePath = "订货记录.xls";
					book = Workbook.createWorkbook(new File(filePath));
					WritableSheet sheet = book.createSheet("Sheet1", 0);

					List<dwms_order> orders = dwms_order.dao
							.find("select * from dwms_order");
					
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
					sheet.addCell(new Label(12, 0, "经销商"));
					sheet.addCell(new Label(13, 0, "订单状态"));
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
							sheet.addCell(new Label(12, i + 1, orders.get(i).getStr(
									"seller")));
							String s=orders.get(i).getStr("status");
							if(s.equals("0")){
								sheet.addCell(new Label(13, i + 1, "未完成"));
							}else{
								sheet.addCell(new Label(13, i + 1, "已完成"));
							}
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
		
		// ************************************公用方法************************************

	    //获取用户信息
		public void getuser(){
			dwms_user user= (dwms_user) getSession().getAttribute("user");
			renderJson(user);
		}
		//比较密码
		public void mmpd() throws NoSuchAlgorithmException, UnsupportedEncodingException{							
			dwms_user user=(dwms_user)getSession().getAttribute("user");
			if (checkpassword(getPara("str"), user.getStr("password")))
				renderText("true");
			else
				renderText("false");
		}
		//修改密码
		public void uppass() throws NoSuchAlgorithmException, UnsupportedEncodingException{
			String password=getPara("password");
			dwms_user user=(dwms_user)getSession().getAttribute("user");
			dwms_log.addlog(user.getStr("name"),"修改密码");
			dwms_user user2=dwms_user.dao.findById(user.getInt("id"));
			String newpassword = EncoderByMd5(password);
			user2.set("password", newpassword);
			user2.update();
			renderText("true");
		}
		//获取用户姓名
		public void getname(){
			dwms_user user = (dwms_user) getSession().getAttribute("user");
			if (user!=null) {
				renderJson(user);
			} else {
				renderText("error");
			}
		}
		//退出
		public void logout(){
			dwms_user user= (dwms_user) getSession().getAttribute("user");
			if (user!=null) {
				renderJson(user);
				dwms_log.addlog(user.getStr("name"),"退出登录");
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
