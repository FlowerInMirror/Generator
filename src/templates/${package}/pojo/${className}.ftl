package ${package}.pojo;

import javax.persistence.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.List;
/** 
 * <p>Description: [${table.tableDesc}pojo]</p>
 * Created on ${date}
 * @author  <a href="mailto: ${email}">${author}</a>
 * @version 1.0 
 * Copyright (c) 2016 klt科技有限公司
 */
@Table(name = "${table.dbName}")
@ApiModel(value = "${table.tableDesc}",description = "${table.tableDesc}")
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
