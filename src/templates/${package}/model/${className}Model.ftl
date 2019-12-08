package ${package}.model;

import javax.persistence.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.List;
/** 
 * <p>Description: [${table.tableDesc}model]</p>
 * Created on ${date}
 * @author  <a href="mailto: ${email}">${author}</a>
 * @version 1.0 
 * Copyright (c) 2016 klt科技有限公司
 */
@Table(name = "${table.dbName}")
@ApiModel(value = "${table.tableDesc}",description = "${table.tableDesc}")
public class ${className}Model {

	private static final long serialVersionUID = 1L;

<#list table.columns as column>
	/**
     * ${column.label}
     */
	<#if column.keyFlag>
	@Id
	</#if>
	<#if column.convert>
	@Column(name = "${column.dbName}")
	</#if>
	@ApiModelProperty(value= "${column.label}" , dataType = "${column.type}")     
	private ${column.type} ${column.name};
</#list>
	
	// setter and getter
<#list table.columns as column>
	/**
	* <p>Discription:[${column.label}]</p>
	* Created on ${date}
	* @return ${column.type}
    * @author:${author}
    */
	public ${column.type} get${column.nameUpper}(){
		return ${column.name};
	}
	/**
	* <p>Discription:[${column.label}]</p>
	* Created on ${date}
	* @author:${author}
	*/
	public void set${column.nameUpper}(${column.type} ${column.name}){
		this.${column.name} = ${column.name};
	}
</#list>
}
