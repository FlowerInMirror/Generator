package ${package}.web;

import com.fmall.common.enums.HttpStatus;
import com.fmall.common.exception.FmallException;
import com.fmall.common.vo.ResponseBody;
import ${package}.service.${className}Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Description: [${table.tableDesc}接口]
 * Created on ${date}
 * @author  <a href="mailto: ${email}">${author}</a>
 * @version 1.0 
 * Copyright (c) ${year} klt科技有限公司
 */
@Slf4j
@RestController
@RequestMapping("${classNameLower}Api")
public class ${className}Controller {

    @Resource
    private ${className}Service ${classNameLower}Service;

    @PostMapping("add")
    public ResponseBody<Boolean> add(@RequestBody Long id) {
        try {
            return ResponseBody.ok(true);
        } catch (Exception e) {
            if (e instanceof FmallException) {
                return ResponseBody.error(((FmallException) e).getHttpStatus());
            }
            log.error("添加商品评价", e);
            return ResponseBody.error(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

 }