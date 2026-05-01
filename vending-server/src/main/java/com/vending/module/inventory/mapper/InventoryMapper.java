package com.vending.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vending.module.inventory.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    @Update("UPDATE inventory SET quantity = quantity - #{quantity}, update_time = NOW() " +
            "WHERE cabinet_id = #{cabinetId} AND product_id = #{productId} AND quantity >= #{quantity}")
    int deductStock(@Param("cabinetId") Long cabinetId,
                    @Param("productId") Long productId,
                    @Param("quantity") Integer quantity);

    @Update("UPDATE inventory SET quantity = quantity + #{quantity}, update_time = NOW() " +
            "WHERE cabinet_id = #{cabinetId} AND product_id = #{productId}")
    int addStock(@Param("cabinetId") Long cabinetId,
                 @Param("productId") Long productId,
                 @Param("quantity") Integer quantity);
}
