package ${package}.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fmall.common.vo.ResponseBody;
import org.springframework.web.bind.annotation.*;

/**
 * Description: [${table.tableDesc}接口]
 * Created on ${date}
 * @author  <a href="mailto: ${email}">${author}</a>
 * @version 1.0 
 * Copyright (c) ${year} klt科技有限公司
 */
@RequestMapping("${classNameLower}Api")
public interface ${className}Api {

    @PostMapping("add")
    ResponseBody<Boolean> add(@RequestBody Long id);

 }