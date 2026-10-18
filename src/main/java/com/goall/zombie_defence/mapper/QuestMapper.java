package com.goall.zombie_defence.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface QuestMapper {

    List<HashMap<String, Object>> selectQuestCountDayList(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectQuestCountWeekList(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectQuestList(Map<String, Object> map) throws Exception;

    void insUserQuestLog(Map<String, Object> map) throws Exception;

    int selectQuestLogDayCount(Map<String, Object> map) throws Exception;

    int selectQuestLogWeekCount(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectDayUserQuestClearList(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectWeekUserQuestClearList(Map<String, Object> map) throws Exception;

    /*광고보고 보상받는 부분 추가(230817)*/

    List<HashMap<String, Object>> selectAdRewardList(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectQuestLogAdRewardCount(Map<String, Object> map) throws Exception;

    int countUserDayQuestData(Map<String, Object> map) throws Exception;

    void insUserDayQuestData(Map<String, Object> map) throws Exception;

    int countUserWeekQuestData(Map<String, Object> map) throws Exception;

    void insUserWeekQuestData(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> userDayQuestData(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> userWeekQuestData(Map<String, Object> map) throws Exception;

    void uptUserDayQuestData(Map<String, Object> map) throws Exception;

    void uptUserWeekQuestData(Map<String, Object> map) throws Exception;

    int countUserQuestLog(Map<String, Object> map) throws Exception;
    void uptUserDayQuestIngameData(Map<String, Object> map) throws Exception;
    void uptUserWeekQuestIngameData(Map<String, Object> map) throws Exception;
    void uptUserDayQuestEpicSkillUpgradeData(Map<String, Object> map) throws Exception;
}