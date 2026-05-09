package com.vending.module.cabinet.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vending.BaseTest;
import com.vending.module.cabinet.entity.Cabinet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CabinetMapper 货柜数据访问层测试")
class CabinetMapperTest extends BaseTest {

    @Autowired
    private CabinetMapper cabinetMapper;

    @BeforeEach
    void setUp() {
        cabinetMapper.delete(new LambdaQueryWrapper<>());
    }

    @Test
    @DisplayName("测试插入货柜")
    void testInsertCabinet() {
        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB-M01");
        cabinet.setName("Mapper测试货柜");
        cabinet.setCity("北京");
        cabinet.setAddress("朝阳区建国路88号");
        cabinet.setCapacity(100);
        cabinet.setStatus(1);

        int result = cabinetMapper.insert(cabinet);
        assertEquals(1, result);
        assertNotNull(cabinet.getCabinetId());
    }

    @Test
    @DisplayName("测试根据ID查询货柜")
    void testSelectById() {
        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB-M02");
        cabinet.setName("Mapper查询货柜");
        cabinet.setCity("上海");
        cabinet.setCapacity(150);
        cabinet.setStatus(1);
        cabinetMapper.insert(cabinet);

        Cabinet found = cabinetMapper.selectById(cabinet.getCabinetId());
        assertNotNull(found);
        assertEquals("Mapper查询货柜", found.getName());
    }

    @Test
    @DisplayName("测试根据条件查询货柜列表")
    void testSelectList() {
        for (int i = 1; i <= 4; i++) {
            Cabinet cabinet = new Cabinet();
            cabinet.setCabinetCode("CAB-M0" + i);
            cabinet.setName("货柜" + i);
            cabinet.setCity(i % 2 == 0 ? "北京" : "上海");
            cabinet.setCapacity(50);
            cabinet.setStatus(i % 3 == 0 ? 0 : 1);
            cabinetMapper.insert(cabinet);
        }

        LambdaQueryWrapper<Cabinet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cabinet::getCity, "北京");
        List<Cabinet> list = cabinetMapper.selectList(wrapper);
        
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("测试根据城市查询")
    void testSelectByCity() {
        Cabinet c1 = new Cabinet();
        c1.setCabinetCode("CAB-M05");
        c1.setName("北京货柜1");
        c1.setCity("北京");
        c1.setCapacity(50);
        c1.setStatus(1);
        cabinetMapper.insert(c1);

        Cabinet c2 = new Cabinet();
        c2.setCabinetCode("CAB-M06");
        c2.setName("广州货柜1");
        c2.setCity("广州");
        c2.setCapacity(50);
        c2.setStatus(1);
        cabinetMapper.insert(c2);

        Cabinet c3 = new Cabinet();
        c3.setCabinetCode("CAB-M07");
        c3.setName("北京货柜2");
        c3.setCity("北京");
        c3.setCapacity(50);
        c3.setStatus(1);
        cabinetMapper.insert(c3);

        LambdaQueryWrapper<Cabinet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cabinet::getCity, "北京");
        List<Cabinet> beijingCabinets = cabinetMapper.selectList(wrapper);
        
        assertEquals(2, beijingCabinets.size());
    }

    @Test
    @DisplayName("测试更新货柜")
    void testUpdateCabinet() {
        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB-M08");
        cabinet.setName("旧名称");
        cabinet.setCity("旧城市");
        cabinet.setCapacity(50);
        cabinet.setStatus(1);
        cabinetMapper.insert(cabinet);

        cabinet.setName("新名称");
        cabinet.setCity("新城市");
        cabinet.setCapacity(200);
        cabinet.setImageUrl("http://example.com/image.jpg");
        int result = cabinetMapper.updateById(cabinet);
        assertEquals(1, result);

        Cabinet updated = cabinetMapper.selectById(cabinet.getCabinetId());
        assertEquals("新名称", updated.getName());
        assertEquals("新城市", updated.getCity());
        assertEquals(200, updated.getCapacity());
        assertEquals("http://example.com/image.jpg", updated.getImageUrl());
    }

    @Test
    @DisplayName("测试删除货柜")
    void testDeleteCabinet() {
        Cabinet cabinet = new Cabinet();
        cabinet.setCabinetCode("CAB-M09");
        cabinet.setName("要删除的货柜");
        cabinet.setCity("深圳");
        cabinet.setCapacity(50);
        cabinet.setStatus(1);
        cabinetMapper.insert(cabinet);

        int result = cabinetMapper.deleteById(cabinet.getCabinetId());
        assertEquals(1, result);

        Cabinet deleted = cabinetMapper.selectById(cabinet.getCabinetId());
        assertNull(deleted);
    }

    @Test
    @DisplayName("测试批量删除")
    void testBatchDelete() {
        Long[] ids = new Long[4];
        for (int i = 0; i < 4; i++) {
            Cabinet cabinet = new Cabinet();
            cabinet.setCabinetCode("CAB-M1" + i);
            cabinet.setName("货柜" + i);
            cabinet.setCity("深圳");
            cabinet.setCapacity(50);
            cabinet.setStatus(1);
            cabinetMapper.insert(cabinet);
            ids[i] = cabinet.getCabinetId();
        }

        int result = cabinetMapper.deleteBatchIds(List.of(ids));
        assertEquals(4, result);

        List<Cabinet> remaining = cabinetMapper.selectList(null);
        assertEquals(0, remaining.size());
    }

    @Test
    @DisplayName("测试分页查询")
    void testSelectPage() {
        for (int i = 1; i <= 15; i++) {
            Cabinet cabinet = new Cabinet();
            cabinet.setCabinetCode("CAB-M" + String.format("%02d", i + 10));
            cabinet.setName("分页货柜" + i);
            cabinet.setCity("城市" + i);
            cabinet.setCapacity(50);
            cabinet.setStatus(1);
            cabinetMapper.insert(cabinet);
        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Cabinet> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(2, 10);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Cabinet> result = 
            cabinetMapper.selectPage(page, null);
        
        assertEquals(15, result.getTotal());
        assertEquals(5, result.getRecords().size());
    }
}
