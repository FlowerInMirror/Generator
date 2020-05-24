package ${package}.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/** 
 * <p>Description: [${table.tableDesc}pojo]</p>
 * Created on ${date}
 * @author  <a href="mailto: ${email}">${author}</a>
 * @version 1.0 
 * Copyright (c) 2016 klt科技有限公司
 */
@Data
@TableName("${table.dbName}")
public class ${className} {

<#list table.columns as column>
	/**
     * ${column.label}
     */
	<#if column.keyFlag>
	@Id
	</#if>
	<#if column.convert>
	</#if>
	private ${column.type} ${column.name};
</#list>

}
