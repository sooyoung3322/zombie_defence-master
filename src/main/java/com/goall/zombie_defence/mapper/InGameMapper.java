package com.goall.zombie_defence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface InGameMapper {

    void insGameStartLog(Map<String, Object> map) throws Exception;

    void insGameResultLog(Map<String, Object> map) throws Exception;
    void insGameUserResultLog(Map<String, Object> map) throws Exception;
    List<HashMap<String, Object>> selectGameUserResultLog(Map<String, Object> map) throws Exception;
    void uptGameUserResultLog(Map<String, Object> map) throws Exception;
    void insGameResultHackLog(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectWaveList(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectGameModeList(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectUnitList(Map<String, Object> map) throws Exception;

    void insGameExperienceLog(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> stageAllList(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectGameResultData(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectGameResultDataStartSeq(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> stageBossList(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> stageBossCountList(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> stageAllList2(Map<String, Object> map) throws Exception;
    void insGameUserWeekResultLog(Map<String, Object> map) throws Exception;

}