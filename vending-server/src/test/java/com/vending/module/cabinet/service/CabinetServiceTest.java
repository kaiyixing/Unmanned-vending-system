package com.vending.module.cabinet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vending.BaseTest;
import com.vending.module.cabinet.entity.Cabinet;
import com.vending.module.cabinet.mapper.CabinetMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CabinetService 货柜服务测试")
class CabinetServiceTest extends BaseTest {

    @Autowired
    private CabinetService cabinetService;

    @Autowired
    private CabinetMapper cabinetMapper;

    @BeforeEach
    void setUp() {
        cabinetMapper.delete(new LambdaQueryWrapper<>());
    }

    @Test
    @DisplayName("测试新增货柜")
    void testSaveCabinet() {
        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB001");
        cabinet.setName("测试货柜");
        cabinet.setCity("北京");
        cabinet.setAddress("朝阳区某某路");
        cabinet.setCapacity(50);
        cabinet.setStatus(1);

        boolean result = cabinetService.save(cabinet);
        assertTrue(result);
        assertNotNull(cabinet.getCabinetId());
    }

    @Test
    @DisplayName("测试查询货柜列表")
    void testListCabinets() {
        for (int i = 1; i <= 3; i++) {
            Cabinet cabinet = new Cabinet();
            cabinet.setCabinetCode("CAB00" + i);
            cabinet.setName("货柜" + i);
            cabinet.setCity("城市" + i);
            cabinet.setCapacity(50);
            cabinet.setStatus(1);
            cabinetService.save(cabinet);
        }

        List<Cabinet> list = cabinetService.list();
        assertEquals(3, list.size());
    }

    @Test
    @DisplayName("测试根据ID查询货柜")
    void testGetCabinetById() {
        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB004");
        cabinet.setName("单个查询货柜");
        cabinet.setCity("上海");
        cabinet.setCapacity(100);
        cabinet.setStatus(1);
        cabinetService.save(cabinet);

        Cabinet found = cabinetService.getById(cabinet.getCabinetId());
        assertNotNull(found);
        assertEquals("单个查询货柜", found.getName());
    }

    @Test
    @DisplayName("测试更新货柜")
    void testUpdateCabinet() {
        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB005");
        cabinet.setName("旧名称");
        cabinet.setCity("旧城市");
        cabinet.setCapacity(50);
        cabinet.setStatus(1);
        cabinetService.save(cabinet);

        cabinet.setName("新名称");
        cabinet.setCity("新城市");
        cabinet.setCapacity(100);
        boolean result = cabinetService.updateById(cabinet);
        assertTrue(result);

        Cabinet updated = cabinetService.getById(cabinet.getCabinetId());
        assertEquals("新名称", updated.getName());
        assertEquals("新城市", updated.getCity());
        assertEquals(100, updated.getCapacity());
    }

    @Test
    @DisplayName("测试删除货柜")
    void testDeleteCabinet() {
        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB006");
        cabinet.setName("要删除的货柜");
        cabinet.setCity("深圳");
        cabinet.setCapacity(50);
        cabinet.setStatus(1);
        cabinetService.save(cabinet);

        boolean result = cabinetService.removeById(cabinet.getCabinetId());
        assertTrue(result);

        Cabinet deleted = cabinetService.getById(cabinet.getCabinetId());
        assertNull(deleted);
    }

    @Test
    @DisplayName("测试根据城市查询货柜")
    void testGetCabinetsByCity() {
        Cabinet c1 = new Cabinet();
        c1.setCabinetCode("CAB007");
        c1.setName("北京货柜1");
        c1.setCity("北京");
        c1.setCapacity(50);
        c1.setStatus(1);
        cabinetService.save(c1);

        Cabinet c2 = new Cabinet();
        c2.setCabinetCode("CAB008");
        c2.setName("上海货柜1");
        c2.setCity("上海");
        c2.setCapacity(50);
        c2.setStatus(1);
        cabinetService.save(c2);

        Cabinet c3 = new Cabinet();
        c3.setCabinetCode("CAB009");
        c3.setName("北京货柜2");
        c3.setCity("北京");
        c3.setCapacity(50);
        c3.setStatus(1);
        cabinetService.save(c3);

        List<Cabinet> beijingCabinets = cabinetService.lambdaQuery()
                .eq(Cabinet::getCity, "北京")
                .list();
        
        assertEquals(2, beijingCabinets.size());
    }

    @Test
    @DisplayName("测试货柜状态查询")
    void testGetCabinetsByStatus() {
        Cabinet c1 = new Cabinet();
        c1.setCabinetCode("CAB010");
        c1.setName("在线货柜");
        c1.setCity("广州");
        c1.setCapacity(50);
        c1.setStatus(1);
        cabinetService.save(c1);

        Cabinet c2 = new Cabinet();
        c2.setCabinetCode("CAB011");
        c2.setName("离线货柜");
        c2.setCity("广州");
        c2.setCapacity(50);
        c2.setStatus(0);
        cabinetService.save(c2);

        List<Cabinet> activeCabinets = cabinetService.lambdaQuery()
                .eq(Cabinet::getStatus, 1)
                .list();
        
        assertEquals(1, activeCabinets.size());
        assertEquals("在线货柜", activeCabinets.get(0).getName());
    }

    @Test
    @DisplayName("测试货柜编码唯一性校验")
    void testUniqueCabinetCode() {
        Cabinet c1 = new Cabinet();
        c1.setCabinetCode("CAB012");
        c1.setName("货柜A");
        c1.setCity("北京");
        c1.setCapacity(50);
        c1.setStatus(1);
        cabinetService.save(c1);

        Cabinet c2 = new Cabinet();
        c2.setCabinetCode("CAB012");
        c2.setName("货柜B");
        c2.setCity("北京");
        c2.setCapacity(50);
        c2.setStatus(1);

        assertThrows(Exception.class, () -> {
            cabinetService.save(c2);
        });
    }

    @Test
    @DisplayName("测试更新货柜图片")
    void testUpdateCabinetImage() {
        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB013");
        cabinet.setName("测试货柜图片");
        cabinet.setCity("深圳");
        cabinet.setCapacity(50);
        cabinet.setStatus(1);
        cabinetService.save(cabinet);

        cabinet.setImageUrl("http://example.com/new-image.jpg");
        boolean result = cabinetService.updateById(cabinet);
        assertTrue(result);

        Cabinet updated = cabinetService.getById(cabinet.getCabinetId());
        assertEquals("http://example.com/new-image.jpg", updated.getImageUrl());
    }
}
