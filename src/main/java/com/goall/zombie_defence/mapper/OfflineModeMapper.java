package com.goall.zombie_defence.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface OfflineModeMapper {

    List<HashMap<String, Object>> selectOfflineModeLog(Map<String, Object> map) throws Exception;

}