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

			int columnSize = rs.getInt("COLUMN_SIZE");
			if(dbType.equals("TINYINT")&&columnSize>1){
				c.setType("Integer");
			}else if(dbType.equals("TINYINT")&&columnSize==1){
				c.setType("Boolean");
			}else{
				String type = properties.getProperty(dbType);
				c.setType(type == null ? "String" : type);
			}
			c.setDbType(dbType);
			c.setLength(rs.getInt("COLUMN_SIZE"));
			c.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
			c.setNullable(rs.getBoolean("NULLABLE"));
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

		root.put("author", "王文亮");
		root.put("email", "wangwenliang@camelotchina.com");

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
//		map.put("news", "新闻表");
//		map.put("news_class", "新闻分类表");
//		map.put("news_label", "新闻标签表");
//		map.put("notice", "公告表");
//		map.put("notice_type", "公告类别表");
//		map.put("projectdata", "项目数据表");
//		map.put("apply", "申请表");
//		map.put("banner", "轮播图表");
//		map.put("faqs", "常见问题表");
//		map.put("leave_message", "留言表");
//		map.put("mail_template", "邮件模板表");
//		map.put("message_template", "短信模板表");
//		map.put("user", "用户表");

//		map.put("academic_cooperation", "雅典娜计划申请表");
//		map.put("forum_elite","精英论坛");
//		map.put("laboratory","联合实验室");
//		map.put("national_research_project","国家大型科技项目");
//		map.put("theme_achievements","成果展示管理");
//		map.put("theme_science","科技项目管理");
//		map.put("province", "省份信息表");
//		map.put("school", "学校表");
//		map.put("dictionary", "字典表");
//		map.put("exception_log", "异常信息表");
//		map.put("function_table", "扩充功能表");
//		map.put("t_bscard_apply_card", "名片申请表");
//		map.put("t_bscard_commpany_address", "公司地址表");
//		map.put("t_bscard_commpany_name", "公司名称表");
//		map.put("t_bscard_dictionary", "字典表");
//		map.put("t_bscard_export_record", "导出记录表");
//		map.put("t_bscard_harvest_address", "收获地址表");
//		map.put("t_bscard_organization", "公司主体表");
//		map.put("t_bscard_template", "名片模板表");
//		map.put("t_bscard_user", "用户表");
		map.put("t_dictionary", "字典表");
		


		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, String> e = it.next();
			g.gen(e.getKey(), e.getValue());
		}
		System.out.println("模版文件生成完毕……");
	}
}
