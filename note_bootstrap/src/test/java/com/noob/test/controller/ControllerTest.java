package com.noob.test.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.noob.request.component.ITestTransactionOnInterfaceService;
import com.noob.request.controller.GroupTestDTO;
import com.noob.util.JacksonUtil;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Map;

// 测试API各种访问方式的Mock
public class ControllerTest extends BaseTest {
    @Resource
    ITestTransactionOnInterfaceService testTransactionOnInterfaceService;

    public void test() {
        testMultipartFile();
        test1();
        test2();
        test3();
        testGroup();

    }

    @Test
    public void testGroup() {
        testTransactionOnInterfaceService.testTransactionOnInterface();
        GroupTestDTO test = new GroupTestDTO("address", "name", "code", "phone", null, null);
        try {
            System.out.println(mockMvc
                    .perform(MockMvcRequestBuilders.get("/validate/testAdvice").param("testParamters", "testParamters")
                            .contentType(MediaType.APPLICATION_JSON_UTF8).content(JacksonUtil.toJson(test)))
                    .andDo(MockMvcResultHandlers.print()).andReturn().getResponse().getContentAsString());
            System.out.println(mockMvc
                    .perform(MockMvcRequestBuilders.get("/validate/testGroupDefault")
                            .contentType(MediaType.APPLICATION_JSON_UTF8).content(JacksonUtil.toJson(test)))
                    .andDo(MockMvcResultHandlers.print()).andReturn().getResponse().getContentAsString());

            System.out.println(mockMvc
                    .perform(MockMvcRequestBuilders.get("/validate/testGroupParent")
                            .contentType(MediaType.APPLICATION_JSON_UTF8).content(JacksonUtil.toJson(test)))
                    .andDo(MockMvcResultHandlers.print()).andReturn().getResponse().getContentAsString());

            System.out.println(mockMvc
                    .perform(MockMvcRequestBuilders.get("/validate/testGroupExtends")
                            .contentType(MediaType.APPLICATION_JSON_UTF8).content(JacksonUtil.toJson(test)))
                    .andDo(MockMvcResultHandlers.print()).andReturn().getResponse().getContentAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void testMultipartFile() {
        MockMultipartHttpServletRequestBuilder mockMulHttpServletRequestBuilder;
        try {
            File testFile = new File("C:\\Users\\admin\\Desktop\\TiDB 适用场景.pdf");

            mockMulHttpServletRequestBuilder = MockMvcRequestBuilders.multipart(new URI("/request/testMultipartFile"));
            MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "TiDB 适用场景.pdf",
                    MediaType.APPLICATION_PDF_VALUE, new FileInputStream(testFile));
            mockMulHttpServletRequestBuilder.file(mockMultipartFile);

            System.out.println(mockMvc.perform(mockMulHttpServletRequestBuilder)
                    .andExpect(MockMvcResultMatchers.status().isOk()).andReturn().getResponse().getContentAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // GET & POST 都可以
    public void test1() {
        while (true) {
            Map<String, String> map = Maps.newHashMap();
            map.put("param_1", "test1");
            map.put("param_2", "test2");

            try {
                System.out.println(mockMvc
                        .perform(MockMvcRequestBuilders.post("/request/test1").content(JacksonUtil.toJson(map))
                                .param("param1", "param1"))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
                        .andReturn().getResponse().getContentAsString());

                System.out.println(mockMvc
                        .perform(MockMvcRequestBuilders.get("/request/test1").param("param1", "param1")
                                .contentType(MediaType.APPLICATION_JSON_UTF8).content(JacksonUtil.toJson(map)))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
                        .andReturn().getResponse().getContentAsString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // GET & POST 都可以
    public void test2() {
        try {
            System.out.println(mockMvc
                    .perform(MockMvcRequestBuilders.post("/request/test2").contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(JacksonUtil.toJson(Lists.newArrayList("1", "2", "3")))
                            .param("list2", new String[]{"4", "5", "6"}))
                    .andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn()
                    .getResponse().getContentAsString());
            System.out.println(mockMvc
                    .perform(MockMvcRequestBuilders.get("/request/test2").contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(JacksonUtil.toJson(Lists.newArrayList("1", "2", "3")))
                            .param("list2", new String[]{"4", "5", "6"}))
                    .andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn()
                    .getResponse().getContentAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET & POST 都可以
    public void test3() {
        try {
            System.out.println(mockMvc.perform(MockMvcRequestBuilders.get("/request/test3/" + 11))
                    .andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn()
                    .getResponse().getContentAsString());
            System.out.println(mockMvc.perform(MockMvcRequestBuilders.post("/request/test3/" + 22))
                    .andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print()).andReturn()
                    .getResponse().getContentAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
