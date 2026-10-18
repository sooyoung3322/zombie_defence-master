package com.goall.zombie_defence.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface UnitUpgradeMapper {
    List<HashMap<String, Object>> unitUpgradeData(Map<String, Object> map) throws Exception;
    void insUserUnitUpgrade(Map<String, Object> map) throws Exception;
    void uptUserUnitUpgrade(Map<String, Object> map) throws Exception;
    List<HashMap<String, Object>> userUnitUpgradeYn(Map<String, Object> map) throws Exception;
    List<HashMap<String, Object>> userUnitUpgradeData(Map<String, Object> map) throws Exception;

    void insUserUnitUpgradeLog(Map<String, Object> map) throws Exception;

}