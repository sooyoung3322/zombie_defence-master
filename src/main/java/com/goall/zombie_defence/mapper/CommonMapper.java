package com.goall.zombie_defence.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface CommonMapper {

    List<HashMap<String, Object>> nowDataTimeAndGameVersion(Map<String, Object> map) throws Exception;

    void insAdvertisementLog(Map<String, Object> map) throws Exception;

    void insAdvertisementFirstTimeData(Map<String, Object> map) throws Exception;

    void uptAdvertisementTimeData(Map<String, Object> map) throws Exception;

}