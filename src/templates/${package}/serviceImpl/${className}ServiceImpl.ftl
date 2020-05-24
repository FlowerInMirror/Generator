package ${package}.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import ${package}.dao.${className}Mapper;
import ${package}.pojo.${className};
import ${package}.service.${className}Service;

/** 
 * Description: [${table.tableDesc}服务实现]
 * Created on ${date}
 * @author  <a href="mailto: ${email}">${author}</a>
 * @version 1.0 
 * Copyright (c) ${year} klt科技有限公司
 */
@Service("${classNameLower}Service")
public class ${className}ServiceImpl extends ServiceImpl<${className}Mapper, ${className}> implements ${className}Service {

}