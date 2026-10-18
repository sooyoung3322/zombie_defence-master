package com.goall.zombie_defence.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface SeasonPackageMapper {

    List<HashMap<String, Object>> selectSeasonPackageList(Map<String, Object> map) throws Exception;
    List<HashMap<String, Object>> selectSeasonNumberList(Map<String, Object> map) throws Exception;
    void insUserSeasonPackage(Map<String, Object> map) throws Exception;
    int searchUserSeasonPackage(Map<String, Object> map) throws Exception;
    void uptUserSeasonPackage(Map<String, Object> map) throws Exception;
    List<HashMap<String, Object>> selectUserSeasonPackageList(Map<String, Object> map) throws Exception;
    void insUserSeasonPackageInsertData(Map<String, Object> map) throws Exception;
    int selectUserSeasonPackageInsertDataCount(Map<String, Object> map) throws Exception;
    List<HashMap<String, Object>> selectSeasonNumberRewardList(Map<String, Object> map) throws Exception;
    void uptUserSeasonPackageTier(Map<String, Object> map) throws Exception;
    void insUserSeasonNumberDate(Map<String, Object> map) throws Exception;
    void uptUserSeasonNumberDate(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> isSeasonStartedTrueFalse(Map<String, Object> map) throws Exception;

}