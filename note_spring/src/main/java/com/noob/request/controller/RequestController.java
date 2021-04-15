package com.noob.request.controller;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

/**
 * 验证请求入参出参注解的解析
 * <p>
 * @RequestBody、@RequestParam 在 GET POST 都可以正确读入；与 数据组装的方式有关。
 *
 */
@Slf4j
@RequestMapping("/request")
@RestController
public class RequestController {

	private static final String TMP = System.getProperty("java.io.tmpdir") + File.separator;

	/**
	 * 可支持多文件上传。
	 * <p>
	 * #文件写入磁盘的阈值 spring.servlet.multipart.file-size-threshold: 0
	 * <p>
	 * # 最大文件大小 spring.servlet.multipart.max-file-size: 200MB
	 * <p>
	 * # 最大请求大小 spring.servlet.multipart.max-request-size: 215MB
	 * {@link MultipartAutoConfiguration}
	 * 
	 * @param multipartFile
	 */
	@PostMapping(value = "/testMultipartFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String testMultipartFile(
			@RequestParam(value = "file[]", required = true) List<MultipartFile> multipartFile) {
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
	public void test3(@PathVariable String info, HttpServletRequest request) {

		log.info("requestMethod: {}", request.getMethod());
		log.info("contentType: {}", request.getContentType());
		log.info(info);
	}

}
