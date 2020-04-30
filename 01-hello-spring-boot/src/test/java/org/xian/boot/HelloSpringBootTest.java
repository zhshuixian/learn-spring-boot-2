package org.xian.boot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = BootApplication.class)
@AutoConfigureMockMvc
class HelloSpringBootTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("TestString")
    void testHelloString() throws Exception {
        // URL 和 验证返回内容的代码在 mvc.perform 中
        // andExpect 对返回结果进行验证，如果不正确将视为测试案例运行失败
        this.mvc.perform(get("/hello/string")).andExpect(status().isOk())
                .andExpect(content().string("Hello Spring Boot"));
    }

    @Test
    @DisplayName("Test JSON")
    void testHelloJson() throws Exception {
        // content().json() 对返回的 JSON 数据进行验证
        this.mvc.perform(get("/hello/json")).andExpect(status().isOk())
                .andExpect(content().json("{'status':'success';'messages':'Hello Spring Boot By JSON'}"));
    }

    @Test
    void testParam() {
        Messages messages = this.restTemplate.getForObject("/hello/param?username=xiaoxian", Messages.class);
        assertThat(messages.getMessages()).isEqualTo("Hello xiaoxian");
    }

    @Test
    void testPathVariable() throws Exception {
        this.mvc.perform(post("/hello/path/xiaoxian")).andExpect(status().isOk())
                .andExpect(content().json("{'status':'success';'messages':'Hello xiaoxian'}"));
    }

    @Test
    void testBody() {
        SysUser sysUser = new SysUser("user", "springboot");
        Messages messages = this.restTemplate.postForObject("/hello/body", sysUser, Messages.class);
        assertThat(messages.getStatus()).isEqualTo("success");
        assertThat(messages.getMessages()).isEqualTo("Login  Success");
    }

    @Test
    void testText() {
        String string = "Hi,Spring Boot";
        String result = this.restTemplate.postForObject("/hello/text", string, String.class);
        assertThat(result).isEqualTo(string);
    }

    @Test
    void testForm() throws Exception {
        // contentType 指定上传数据的类型
        this.mvc.perform(post("/hello/form")
                .contentType("application/x-www-form-urlencoded")
                .param("username", "user")
                .param("password", "springboot"))
                .andExpect(status().isOk())
                .andExpect(content().json("{'status':'success';'messages':'Login  Success'}"));
    }
}