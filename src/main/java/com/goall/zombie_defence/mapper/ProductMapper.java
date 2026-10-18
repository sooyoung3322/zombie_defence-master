package com.goall.zombie_defence.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper {

    List<HashMap<String, Object>> selectProductDataList(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectProductData(Map<String, Object> map) throws Exception;

    void insProductSaleLog(Map<String, Object> map) throws Exception;

    void uptProductSaleCount(Map<String, Object> map) throws Exception;

    void insUseCashLog(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectProductDataFirstBuyList(Map<String, Object> map) throws Exception;

    void insExpBooster(Map<String, Object> map) throws Exception;

    void uptExpBooster(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectExpBooster(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectExpBoosterYn(Map<String, Object> map) throws Exception;

}