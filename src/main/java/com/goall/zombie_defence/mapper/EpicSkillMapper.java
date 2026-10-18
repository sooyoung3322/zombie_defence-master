package com.goall.zombie_defence.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface EpicSkillMapper {

    List<HashMap<String, Object>> selectEpicSkillData(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> totalUserEpicSkillData(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> upgradeSelectUserEpicSkill(Map<String, Object> map) throws Exception;

    void insUserEpicSkillList(Map<String, Object> map) throws Exception;

    void insUserEpicSkill(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> epicSkillDrawSelect(Map<String, Object> map) throws Exception;

    void uptUserEpicSkillEquip(Map<String, Object> map) throws Exception;

    int userEpicSkillCount(Map<String, Object> map) throws Exception;

    void uptUpgradeEpicSkill(Map<String, Object> map) throws Exception;

    void uptUpgradeEpicSkillYn(Map<String, Object> map) throws Exception;

    void insUpgradeEpicSkillLog(Map<String, Object> map) throws Exception;

    void insSynthesisEpicSkillLog(Map<String, Object> map) throws Exception;

    void insSellEpicSkillLog(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectNotUserGradeEpicSkill(Map<String, Object> map) throws Exception;

    void insSellEpicSkillChangeLog(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> buyEpicSkill5Random(Map<String, Object> map) throws Exception;

}