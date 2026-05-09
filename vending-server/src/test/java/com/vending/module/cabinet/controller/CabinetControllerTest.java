package com.vending.module.cabinet.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vending.BaseTest;
import com.vending.common.cache.RedisCacheUtil;
import com.vending.module.cabinet.entity.Cabinet;
import com.vending.module.cabinet.mapper.CabinetMapper;
import com.vending.module.cabinet.service.CabinetService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@AutoConfigureMockMvc
@DisplayName("CabinetController 货柜控制器测试")
class CabinetControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CabinetService cabinetService;

    @Autowired
    private CabinetMapper cabinetMapper;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @BeforeEach
    void setUp() {
        cabinetMapper.delete(new LambdaQueryWrapper<>());
    }

    @Test
    @DisplayName("测试获取货柜列表")
    void testGetCabinetList() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Cabinet cabinet = new Cabinet();
            cabinet.setCabinetCode("CAB-CT" + i);
            cabinet.setName("列表货柜" + i);
            cabinet.setCity(i % 2 == 0 ? "北京" : "上海");
            cabinet.setCapacity(50);
            cabinet.setStatus(1);
            cabinetService.save(cabinet);
        }

        MvcResult result = mockMvc.perform(get("/api/cabinet/list")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        log.info("货柜列表响应: {}", content);
        assertNotNull(content);
        assertTrue(content.contains("列表货柜1"));
    }

    @Test
    @DisplayName("测试根据城市获取货柜列表")
    void testGetCabinetListByCity() throws Exception {
        Cabinet c1 = new Cabinet();
        c1.setCabinetCode("CAB-CT6");
        c1.setName("北京货柜A");
        c1.setCity("北京");
        c1.setCapacity(50);
        c1.setStatus(1);
        cabinetService.save(c1);

        Cabinet c2 = new Cabinet();
        c2.setCabinetCode("CAB-CT7");
        c2.setName("上海货柜B");
        c2.setCity("上海");
        c2.setCapacity(50);
        c2.setStatus(1);
        cabinetService.save(c2);

        MvcResult result = mockMvc.perform(get("/api/cabinet/list")
                        .param("page", "1")
                        .param("size", "10")
                        .param("city", "北京")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        log.info("按城市查询响应: {}", content);
        assertNotNull(content);
        assertTrue(content.contains("北京货柜A"));
        assertFalse(content.contains("上海货柜B"));
    }

    @Test
    @DisplayName("测试根据ID获取货柜")
    void testGetCabinetById() throws Exception {
        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB-CT8");
        cabinet.setName("单个查询货柜");
        cabinet.setCity("深圳");
        cabinet.setCapacity(100);
        cabinet.setStatus(1);
        cabinetService.save(cabinet);

        MvcResult result = mockMvc.perform(get("/api/cabinet/{id}", cabinet.getCabinetId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        log.info("单个货柜查询响应: {}", content);
        assertNotNull(content);
        assertTrue(content.contains("单个查询货柜"));
    }

    @Test
    @DisplayName("测试添加货柜")
    void testSaveCabinet() throws Exception {
        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB-CT9");
        cabinet.setName("新添加货柜");
        cabinet.setCity("广州");
        cabinet.setAddress("天河区天河路385号");
        cabinet.setCapacity(80);
        cabinet.setStatus(1);

        MvcResult result = mockMvc.perform(post("/api/cabinet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(cabinet)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        log.info("添加货柜响应: {}", content);
        assertNotNull(content);

        long count = cabinetService.count();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("测试更新货柜")
    void testUpdateCabinet() throws Exception {
        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB-CT10");
        cabinet.setName("旧货柜");
        cabinet.setCity("旧城市");
        cabinet.setCapacity(50);
        cabinet.setStatus(1);
        cabinetService.save(cabinet);

        cabinet.setName("新货柜");
        cabinet.setCity("新城市");
        cabinet.setCapacity(150);
        cabinet.setImageUrl("http://example.com/new-image.jpg");

        MvcResult result = mockMvc.perform(put("/api/cabinet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(cabinet)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        log.info("更新货柜响应: {}", content);
        assertNotNull(content);

        Cabinet updated = cabinetService.getById(cabinet.getCabinetId());
        assertEquals("新货柜", updated.getName());
        assertEquals("新城市", updated.getCity());
        assertEquals(150, updated.getCapacity());
        assertEquals("http://example.com/new-image.jpg", updated.getImageUrl());
    }

    @Test
    @DisplayName("测试删除货柜")
    void testDeleteCabinet() throws Exception {
        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB-CT11");
        cabinet.setName("要删除的货柜");
        cabinet.setCity("深圳");
        cabinet.setCapacity(50);
        cabinet.setStatus(1);
        cabinetService.save(cabinet);

        MvcResult result = mockMvc.perform(delete("/api/cabinet/{id}", cabinet.getCabinetId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        log.info("删除货柜响应: {}", content);
        assertNotNull(content);

        Cabinet deleted = cabinetService.getById(cabinet.getCabinetId());
        assertNull(deleted);
    }

    @Test
    @DisplayName("测试获取货柜商品列表")
    void testGetCabinetProducts() throws Exception {
        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB-CT12");
        cabinet.setName("商品查询货柜");
        cabinet.setCity("北京");
        cabinet.setCapacity(100);
        cabinet.setStatus(1);
        cabinetService.save(cabinet);

        MvcResult result = mockMvc.perform(get("/api/cabinet/{id}/products", cabinet.getCabinetId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        log.info("货柜商品列表响应: {}", content);
        assertNotNull(content);
    }
}
