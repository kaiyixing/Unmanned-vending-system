package com.vending.module.pickup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vending.module.pickup.entity.PickupCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PickupCodeMapper extends BaseMapper<PickupCode> {

    @Select("SELECT COUNT(*) FROM pickup_code WHERE code_value = #{codeValue}")
    int existsByCodeValue(@Param("codeValue") String codeValue);
}
