package ${package}.service.impl;

import java.util.Map;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import ${package}.dao.${className}Dao;
import ${package}.model.${className}Model;
import ${package}.service.${className}Service;

/** 
 * Description: [${table.tableDesc}服务实现]
 * Created on ${date}
 * @author  <a href="mailto: ${email}">${author}</a>
 * @version 1.0 
 * Copyright (c) ${year} klt科技有限公司
 */
@Service("${classNameLower}Service")
public class ${className}ServiceImpl extends BaseServiceImpl<${className}Model,${className}Mapper> implements ${className}Service {
	
	/**
	 * <p>Discription:[${table.tableDesc}Mapper]</p>
	 */	
	@Resource
	private ${className}Mapper ${classNameLower}Mapper;
	
	

}