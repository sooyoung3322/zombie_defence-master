package com.goall.zombie_defence.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {

    List<HashMap<String, Object>> totalGameRanking(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> myTotalGameRanking(Map<String, Object> map) throws Exception;

    void insWeekGameRanking(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> TotalLevelRanking(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> myTotalLevelRanking(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> myLastWeekTotalGameRanking(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> stageList(Map<String, Object> map) throws Exception;

    void insGameRankRewardLog(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> gameRankRewardLogList(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> bossStageHighScoreList(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> totalWeekGameRanking(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> myTotalWeekGameRanking(Map<String, Object> map) throws Exception;

    void delGameWeekResultData(Map<String, Object> map) throws Exception;

}