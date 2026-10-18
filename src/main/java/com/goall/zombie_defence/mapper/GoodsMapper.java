package com.goall.zombie_defence.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface GoodsMapper {

    void insUseKeyLog(Map<String, Object> map) throws Exception;

    void insChargeKeyLog(Map<String, Object> map) throws Exception;

    void uptUserKey(Map<String, Object> map) throws Exception;

    void insUseRubyLog(Map<String, Object> map) throws Exception;

    void uptUserRuby(Map<String, Object> map) throws Exception;

    void uptUserGold(Map<String, Object> map) throws Exception;
    void uptUserBp(Map<String, Object> map) throws Exception;

    void insUseGoldLog(Map<String, Object> map) throws Exception;

    void insChargeGoldLog(Map<String, Object> map) throws Exception;

    void insChargeRubyLog(Map<String, Object> map) throws Exception;

    void insChargeBpLog(Map<String, Object> map) throws Exception;

    void insUseBpLog(Map<String, Object> map) throws Exception;

}