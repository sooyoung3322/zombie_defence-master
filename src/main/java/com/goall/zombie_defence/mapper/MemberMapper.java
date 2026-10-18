package com.goall.zombie_defence.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface MemberMapper {

    List<HashMap<String, Object>> selectUser();


}