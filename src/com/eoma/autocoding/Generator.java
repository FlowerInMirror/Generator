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
//		map.put("stock_info", "库存信息表");
//		map.put("stock_order", "订单库存表");
//		map.put("stock_out_detail", "库存出管详情表");
//		map.put("stock_out_mag", "库存出管表");

		// homecenter
//		map.put("mall_banner", "轮播图");
//		map.put("mall_floor", "商城首页楼层");
//		map.put("mall_floor_item", "楼层推荐为商品");
//		map.put("mall_floor_nav", "商城首页楼层导航");

		// tradecenter
//		map.put("order_item", "订单商品表");
//		map.put("order_item_discount", "订单商品活动");
//		map.put("orders", "订单");
//		map.put("pay_log", "");
//		map.put("shop_freight_template", "运费模板,商品所在地到配送地址所在地区");

		// promotioncenter
		map.put("coupon", "优惠券");
		map.put("coupon_user", "用户使用优惠券");
		map.put("promotion", "促销活动");
		map.put("promotion_item", "活动商品");
		map.put("user_credit", "用户积分");
		map.put("user_credit_log", "用户积分来源与使用");


		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, String> e = it.next();
			g.gen(e.getKey(), e.getValue());
		}
		System.out.println("模版文件生成完毕……");
	}
}
