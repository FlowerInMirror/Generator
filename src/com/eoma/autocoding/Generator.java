package com.eoma.autocoding;

import com.eoma.autocoding.common.Column;
import com.eoma.autocoding.common.Table;
import com.eoma.autocoding.utils.CamelCaseUtils;
import com.eoma.autocoding.utils.FileHelper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.log4j.Logger;

import java.io.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class Generator {
	private Logger logger = Logger.getLogger(this.getClass());
	private Properties properties = null;

	public Generator() throws Exception {
		properties = new Properties();
		String fileDir = this.getClass().getClassLoader().getResource("generator.xml").getFile();
		properties.loadFromXML(new FileInputStream(fileDir));
	}

	public Table parseTable(String tableName) throws Exception {
		String driverName = properties.getProperty("jdbc.driver");
		String userName = properties.getProperty("jdbc.username");
		String userPwd = properties.getProperty("jdbc.password");
		String dbURL = properties.getProperty("jdbc.url");

		String catalog = properties.getProperty("jdbc.catalog");
		String schema = properties.getProperty("jdbc.schema");
		schema = schema == null ? "%" : schema;
		String column = "%";

		logger.debug("driver>>"+driverName);
		logger.debug("url>>"+dbURL);
		logger.debug("name>>"+userName);
		logger.debug("password>>"+userPwd);
		logger.debug("catalog>>"+catalog);
		logger.debug("schema>>"+schema);

		Class.forName(driverName);
		Connection conn = java.sql.DriverManager.getConnection(dbURL, userName, userPwd);
		DatabaseMetaData dmd = conn.getMetaData();

		ResultSet rs = dmd.getColumns(catalog, schema, tableName, column);

		List<Column> columns = new ArrayList<Column>();
 		while (rs.next()) {
			Column c = new Column();
			
			c.setLabel(rs.getString("REMARKS"));

			String name = rs.getString("COLUMN_NAME");
			if(name.equals("id")){
				c.setKeyFlag(true);
			}else{
				c.setKeyFlag(false);
			}
			if(name.indexOf("_")!=-1){
				c.setConvert(true);
			}else{
				c.setConvert(false);
			}
			c.setName(CamelCaseUtils.toCamelCase(name));
			c.setDbName(name);

			String dbType = rs.getString("TYPE_NAME");

			int column_size = rs.getInt("COLUMN_SIZE");
			int columnSize = column_size;
			if(dbType.equals("TINYINT")&&columnSize>1){
				c.setType("Integer");
			}else if(dbType.equals("TINYINT")&&columnSize==1){
				c.setType("Boolean");
			}else{
				String type = properties.getProperty(dbType);
				c.setType(type == null ? "String" : type);
			}
			if (dbType.equals("VARCHAR") || dbType.equals("TEXT")) {
				c.setStr(true);
			}
			c.setDbType(dbType);
			c.setLength(column_size);
			c.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
			c.setNullable(rs.getBoolean("NULLABLE"));
			if (rs.isLast()) {
				c.setEnd(false);
			}
			columns.add(c);
		}

		List<Column> pkColumns = new ArrayList<Column>();
		ResultSet pkrs = dmd.getPrimaryKeys(catalog, schema, tableName);
		while(pkrs.next()){
			Column c = new Column();
			String name = pkrs.getString("COLUMN_NAME");
			c.setName(CamelCaseUtils.toCamelCase(name));
			c.setDbName(name);
			pkColumns.add(c);
		}

		conn.close();

		Table t = new Table();

		String prefiex = properties.getProperty("tableRemovePrefixes");
		String name = tableName;
		if( prefiex != null && !"".equals(prefiex) ){
			name = tableName.split(prefiex)[0];
		}
		t.setName(CamelCaseUtils.toCamelCase(name));
		t.setDbName(tableName);
		t.setColumns(columns);
		t.setPkColumns(pkColumns);
		return t;
	}

	/**
	 * <p>Discription:[生成映射文件和实体类]</p>
	 * Created on 2015年2月4日
	 * @param tableName 要声称映射文件和实体类的表名称
	 * @param tableDescAndCat 表描述
	 * @throws Exception
	 */
	public void gen(String tableName,String tableDescAndCat) throws Exception {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_21);

		String outRoot = properties.getProperty("outRoot");
		String basepackage = properties.getProperty("basepackage");
		//获取当前日期
		SimpleDateFormat sm_date = new SimpleDateFormat("yyyy年MM月dd日");
		SimpleDateFormat sm_year = new SimpleDateFormat("yyyy年");

		Map<String, Object> root = new HashMap<String, Object>();
		Table t = this.parseTable(tableName);
		t.setTableDesc(tableDescAndCat.split("_")[0]);
		root.put("table", t);
		root.put("className", t.getNameUpper());
		root.put("singlePackageName", "ped");
		root.put("classNameLower", t.getName());
		root.put("package", basepackage);
		root.put("date", sm_date.format(new Date()));
		root.put("year", sm_year.format(new Date()));

		root.put("author", "");
		root.put("email", "");

		String templateDir = this.getClass().getClassLoader().getResource("templates").getPath();

		File tdf = new File(templateDir);
		List<File> files = FileHelper.findAllFile(tdf);

		for(File f: files){
			String parentDir = "";
			if( f.getParentFile().compareTo(tdf) != 0 ){
				parentDir = f.getParent().split("templates")[1];
			}
			cfg.setClassForTemplateLoading(this.getClass(), "/templates" + parentDir);

			Template template = cfg.getTemplate(f.getName());
			template.setEncoding("UTF-8");
			String parentFileDir = FileHelper.genFileDir(parentDir, root);
			parentFileDir = parentFileDir.replace(".", "/");
			String file = FileHelper.genFileDir(f.getName(),root).replace(".ftl", ".java");
			System.out.println(file);

			File newFile = FileHelper.makeFile(outRoot + parentFileDir + "/" + file);
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( newFile ), "UTF-8"));
			template.process(root, out);
			logger.debug("已生成文件：" + outRoot + parentFileDir + "/" + file);
		}
	}

	public static void main(String[] args) throws Exception {
		Generator g = new Generator();
		Map<String, String> map = new HashMap<>();

		// basecenter
//		map.put("country", "国家代号与区号");
//		map.put("region", "省市县");
//		map.put("sys_menu", "菜单表");
//		map.put("sys_role", "角色表");
//		map.put("sys_role_menu", "角色-菜单表");
//		map.put("sys_role_user", "角色-用户表");

		// goodscenter
//		map.put("item", "商品");
//		map.put("item_brand", "品牌");
//		map.put("item_category", "商品类目");
//		map.put("item_category_brand", "商品分类品牌关联关系");
//		map.put("item_evaluation", "商品评价");
//		map.put("item_favourite", "商品收藏");
//		map.put("item_sales_volume", "商品销量");
//		map.put("item_spec", "规格参数组下的参数名");
//		map.put("item_spec_group", "规格参数的分组表，每个商品分类下有多个规格参数组");
//		map.put("item_spec_value", "商品规格值");
//		map.put("sku", "商品sku");
//		map.put("stock", "库存信息表");
//		map.put("stock_record", "库存出入库记录/库存流水");
//		map.put("item_evaluate_reply", "买家与卖家之间对于买家初始评论的回复");

		// homecenter
//		map.put("mall_banner", "轮播图");
//		map.put("mall_floor", "商城首页楼层");
//		map.put("mall_floor_item", "楼层推荐为商品");
//		map.put("mall_floor_nav", "商城首页楼层导航");

		// tradecenter
//		map.put("order_item", "订单商品表");
//		map.put("order_item_discount", "订单商品活动");
		map.put("order_item_express", "订单商品物流信息");
//		map.put("orders", "订单");
//		map.put("pay_log", "");
//		map.put("freight_template", "运费模板,商品所在地到配送地址所在地区");
//		map.put("freight_standard", "运费模版的运费计算标准");
//		map.put("aftersale", "售后单");
//		map.put("aftersale_express", "售后退款退货-换货(买家将商品退给卖家时的退货信息)");
//		map.put("aftersale_item", "售后商品");
//		map.put("aftersale_record", "售后流水记录");

		// promotioncenter
//		map.put("coupon", "优惠券");
//		map.put("coupon_user", "用户使用优惠券");
//		map.put("promotion", "促销活动");
//		map.put("promotion_item", "活动商品");
//		map.put("user_credit", "用户积分");
//		map.put("user_credit_log", "用户积分来源与使用");

		// storecenter
//		map.put("qq_customer", "腾讯客服");
//		map.put("shop_brand", "店铺经营的类目品牌");
//		map.put("shop_category", "店铺商品分类");
//		map.put("shop_favourite", "收藏店铺");
//		map.put("shop_frame", "店铺框架(装修)");
//		map.put("shop_info", "店铺信息");
//		map.put("shop_logistics", "店铺物流");
//		map.put("shop_modify_detail", "店铺修改详情");
//		map.put("shop_modify_info", "店铺修改信息");
//		map.put("shop_sales_volume", "店铺销量");
//		map.put("shop_templates", "店铺使用模板");
//		map.put("shop_widget", "店铺控件(装修)");
//		map.put("shop_widget_sheet", "店铺控件单元数据(装修)");

		// usercenter
//		map.put("buyer", "会员表(买家)");
//		map.put("buyer_credit_log", "买家用户积分记录");
//		map.put("buyer_receive_address", "买家收货地址");
//		map.put("field_certification", "认证信息");
//		map.put("field_identification_audit", "实地认证审核表");
//		map.put("field_identification_picture", "实地认证图片表");
//		map.put("seller", "卖家辅助信息");
//		map.put("seller_audit", "用户审核表");
//		map.put("seller_audit_log", "用户审核表记录");
//		map.put("seller_bank_info", "用户-银行账户信息(针对用户资质)");
//		map.put("seller_business_licence", "卖家-营业执照(针对用户资质)");
//		map.put("seller_contract_audit", "用户合同审核表");
//		map.put("seller_operate_info", "用户-运营信息(针对用户资质)");
//		map.put("seller_organization", "用户-组织机构(针对用户资质)");
//		map.put("sys_user", "平台系统用户");
//		map.put("user", "用户中心表（用户基本信息）");
//		map.put("user_modify_detail", "用户信息修改详情");
//		map.put("user_modify_info", "用户修改信息");

        // aftercenter
//        map.put("after_sale", "售后单");
//        map.put("complaint", "买家投诉");
//        map.put("refund", "退款单");
//        map.put("return_express", "售后退款退货-换货");
//        map.put("return_item", "售后

		// mallsetcenter
//		map.put("floor_img", "楼层图片");
//		map.put("floor_item", "楼层商品");
//		map.put("floor_tab", "楼层页签");
//		map.put("mall_floor", "商城页面模版中楼层信息");
//		map.put("mall_page", "商城页面模版");
//		map.put("mall_banner", "商城首页轮播图");


		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, String> e = it.next();
			g.gen(e.getKey(), e.getValue());
		}
		System.out.println("模版文件生成完毕……");
	}
}
