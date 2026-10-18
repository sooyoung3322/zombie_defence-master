package com.goall.zombie_defence.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface PlayerSkillMapper {

    List<HashMap<String, Object>> playerSkillYn(Map<String, Object> map) throws Exception;

    void insUserPlayerSkill(Map<String, Object> map) throws Exception;

    void uptUserPlayerSkill(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> userPlayerSkillList(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> playerSkillDataList() throws Exception;

}