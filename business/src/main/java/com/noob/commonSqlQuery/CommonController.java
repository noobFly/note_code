package com.noob.commonSqlQuery;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.poi.excel.ExcelUtil;
import com.utrustfund.dp.core.config.Constants;
import com.utrustfund.dp.core.config.SystemProperties;
import com.utrustfund.dp.core.config.util.FileUploadUtils;
import com.utrustfund.dp.core.domain.SysDictData;
import com.utrustfund.dp.core.service.system.ISysDictTypeService;
import com.utrustfund.dp.framework.annotation.AutoWrap;
import com.utrustfund.dp.framework.exception.BizException;
import com.utrustfund.dp.framework.service.jasypt.AesEncoder;
import com.utrustfund.dp.framework.util.Kv;
import com.utrustfund.dp.framework.util.ServerConfig;
import com.utrustfund.dp.framework.util.StringUtils;
import com.utrustfund.dp.framework.util.file.FileUtils;
import com.utrustfund.dp.framework.util.http.RequestUtils;
import com.utrustfund.dp.framework.web.BaseResource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通用请求处理
 *
 */
@RestController
public class CommonController {


    @Autowired
    CommonQueryHandler commonService;




    /**
     * 通用的查询功能
     *
     * @return
     */
    @PostMapping("/common/queryAdm")
    public Object queryAdm(@Validated @RequestBody CommonQueryDTO param) {
        if (param.isStartPage()) {
            // TODO 开启分页
        }
        List<Map<String, Object>> list = commonService.queryAdm(param);
        return param.isStartPage() ? toPage(list) : list;
    }

    private Object toPage(List<Map<String,Object>> list) {
        return null; //转换分页
    }


    /**
     *通用的查询导出功能
     *
     * @return
     */
    @PostMapping("/common/downloadAdm")
    public void downloadAdm(@Validated @RequestBody CommonQueryDTO param, String fileName, HttpServletResponse response) throws Exception {
        List<Map<String, Object>> dataList = commonService.queryAdm(param);
        if(CollectionUtils.isEmpty(dataList)) {
            throw new RuntimeException("无可导出数据");
        }

        // 从字典值里拿到数据字段配置。 TODO
        List<Map<String,  Object>>  finalDataList  = null;
       /*List<SysDictData>  dictList = dictTypeService.getDictDataByType(CommonQueryHandler.TableEnum.findTable(param.getType()).name()).stream().sorted(Comparator.comparing(SysDictData::getDictSort)).collect(Collectors.toList());


        List<String> selectColumnList = param.getSelectColumnList();
        List<SysDictData> rowList = CollectionUtils.isEmpty(selectColumnList) ? dictList : dictList.stream().filter
                (t-> selectColumnList.stream().anyMatch(a -> a.equals(t.getDictValue()))).collect(Collectors.toList()); // 需要的字段

        List<Map<String,  Object>>  finalDataList = dataList.stream().map(originMap -> {
            Map<String, Object> map = new LinkedHashMap<>();
            rowList.forEach(dict-> map.put(dict.getDictLabel(), originMap.get(dict.getDictValue())));
               return map;
        }).collect(Collectors.toList());*/

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(fileName, "utf-8"));
        ExcelUtil.getWriter().write(finalDataList).autoSizeColumnAll().flush(response.getOutputStream()).close();

    }


}
