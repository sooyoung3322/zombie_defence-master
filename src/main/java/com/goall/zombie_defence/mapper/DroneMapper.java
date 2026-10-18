package com.goall.zombie_defence.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface DroneMapper {

    void insUserDrone(Map<String, Object> map) throws Exception;

}