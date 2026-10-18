package com.goall.zombie_defence.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface MailMapper {

    List<HashMap<String, Object>> selectAllMailList();

    List<HashMap<String, Object>> selectAllMailListCheck(Map<String, Object> map);

    void insUserAllMail(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectUserMailList(Map<String, Object> map);

    List<HashMap<String, Object>> selectUserMailRewardList(Map<String, Object> map);

    void uptUserMailRewardUseYn(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectUserMailYnCheck(Map<String, Object> map);


}