package com.noob.request.controller;

import com.ecc.emp.data.DataElement;
import com.noob.json.JSON;
import com.noob.util.ExceptionUtil;
import com.noob.util.File.GZIPUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 验证请求入参出参注解的解析
 * <p>
 *
 * @RequestBody、@RequestParam 在 GET POST 都可以正确读入；与 数据组装的方式有关。
 */
@Slf4j
@RequestMapping("/request")
@RestController
public class RequestController {

    private static final String TMP = System.getProperty("java.io.tmpdir") + File.separator; // On Windows: java.io.tmpdir:[C:\Users\用户名\AppData\Local\Temp\]

public static void main(String args[]){
    System.out.println(System.getProperty("java.io.tmpdir"));
}
    /**
     * 可支持多文件上传。
     * <p>
     * #文件写入磁盘的阈值 spring.servlet.multipart.file-size-threshold: 0
     * <p>
     * # 最大文件大小 spring.servlet.multipart.max-file-size: 200MB
     * <p>
     * # 最大请求大小 spring.servlet.multipart.max-request-size: 215MB
     *
     * @param multipartFile
     */
    @PostMapping(value = "/testMultipartFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String testMultipartFile(
            @RequestParam(value = "file", required = true) List<MultipartFile> multipartFile) {
        String fileName = multipartFile.get(0).getOriginalFilename();
        String pathname = TMP + fileName;
        try {
            multipartFile.get(0).transferTo(new File(pathname));
        } catch (Exception e) {
            log.error("testMultipartFile exception!", e);
        }
        return fileName;
    }

    @PostMapping(value = "/upload")
    public String upload(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest = WebUtils.getNativeRequest(request,
                MultipartHttpServletRequest.class);
        multipartRequest.getFileNames().forEachRemaining(t -> log.info(t));
        List<MultipartFile> multipartFiles = multipartRequest.getFiles("file[]");
        MultipartFile[] multipartFile = multipartFiles.toArray(new MultipartFile[0]);
        String fileName = multipartFile[0].getOriginalFilename();
        String pathname = TMP + fileName;
        return pathname;
    }


    @PostMapping(value = "/file/upload")
    public void upload(@RequestParam String name, @RequestPart("file") MultipartFile file) throws IOException {
        byte[] data = file.getBytes();
        ExceptionUtil.throwException(data == null || data.length == 0, "文件数据不能为空！");
        String originalFilename = file.getOriginalFilename();
        //获取后缀
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
        ExceptionUtil.throwException(!".doc".equalsIgnoreCase(substring) && !".docx".equalsIgnoreCase(substring), "只能上传doc或docx类型文件");

        int length = data.length;
        data = GZIPUtils.compress(data);
        System.out.println(name + "压缩前: " + length + " 压缩后: " + data.length);

    }

    /**
     * sheet表的标题  requestParam入参结构：[{"name":"信息表","topic":1,"indexList":[1,2,3]}]
     *
     * @param requestParam 额外的复杂参数需要: postman上就用form-data传入文本 eg. requestParam:[{"name":"情况表","topic":5,"indexList":[1]}]！
     *                     在url后拼接参数对简单类型可以处理，但复杂入参无法用postman模拟出！ 代码里@RequestPart和@RequestParam都能接收
     * @return
     */
    @GetMapping("/sheetColumn")
    public void sheetColumn(@RequestPart("requestParam") String requestParam, @RequestPart("file") MultipartFile file, HttpServletRequest request) {
        System.out.println(requestParam);
        System.out.println(file.getOriginalFilename());
        List<DataElement> list = JSON.parseArray(requestParam, DataElement.class);
        String tmp = request.getParameter("requestParam");
        System.out.println(tmp);
    }

    @PostMapping(value = "/file/download")
    public void download(@RequestParam String templateCode, HttpServletResponse response) throws IOException {
        System.out.println(templateCode);
        byte[] data = new byte[1024]; // 从DB里获取文件数据
        response.setContentLength(data.length);
        response.setContentType("application/msword");//word文档
        response.getOutputStream().write(data);

    }


    /**
     * GET POST 都可以正确读入
     * <p>
     * 使用 @RequestBody
     * 一定要指定contentType(MediaType.APPLICATION_JSON_UTF8)。否则在执行到AbstractMessageConverterMethodArgumentResolver.readWithMessageConverters时默认设置为MediaType.APPLICATION_OCTET_STREAM.
     * 因没有支持的HttpMessageConverter，导致解析不了入参所以 报错HttpMediaTypeNotSupportedException
     * <p>
     * 对 @RequestBody | @RequestParam 的入参解析的路由在
     * InvocableHandlerMethod.getMethodArgumentValues时执行HandlerMethodArgumentResolverComposite.resolveArgument选择HandlerMethodArgumentResolver来解析
     * <li>@RequestBody 执行了RequestResponseBodyMethodProcessor ,
     * <li>@RequestParam 执行了 RequestParamMethodArgumentResolver ,
     * <li>@PathVariable 执行了 PathVariableMethodArgumentResolver
     *
     * @param map
     * @param param1
     * @param request
     */
    @RequestMapping("/test1")
    public void test1(@RequestBody Map<String, String> map, @RequestParam String param1, HttpServletRequest request) {
        log.info("requestMethod: {}", request.getMethod());
        log.info("contentType: {}", request.getContentType());

        log.info(JSON.toJSONString(map));
        log.info(param1);

    }

    /**
     * GET POST 都可以正确读入
     * <p>
     * 通常@RequestBody的方式会做成DTO对象,便于前端传值.
     *
     * @param list
     * @param request
     */
    @RequestMapping("/test2")
    public void test2(@RequestBody List<String> list, @RequestParam List<String> list2, HttpServletRequest request) {
        log.info("requestMethod: {}", request.getMethod());
        log.info("contentType: {}", request.getContentType());
        log.info(JSON.toJSONString(list));
        log.info(JSON.toJSONString(list2));

    }

    @RequestMapping("/test3/{info}")
    private String test3(@PathVariable String info, HttpServletRequest request) {

        log.info("requestMethod: {}", request.getMethod());
        log.info("contentType: {}", request.getContentType());
        log.info(info);
        return info;
    }

}
