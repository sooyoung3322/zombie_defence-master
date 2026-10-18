package com.goall.zombie_defence.service;

import com.goall.commonUtil.CommonUtil;
import com.goall.zombie_defence.config.JwtProvider;
import com.goall.zombie_defence.mapper.*;
import com.goall.zombie_defence.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PlayerSkillService {

    @Autowired
    PlayerSkillMapper playerSkillMapper;

    @Autowired
    UserMapper userMapper;

    @Transactional
    public ResponseEntity<?> uptUserPlayerSkill(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();
        map.put("upt_ip", CommonUtil.getRemoteIp(request));

        //결과값 클라에 줘야함
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        //유저 재화 정보 가져오기
        List<HashMap<String, Object>> userResultMap = userMapper.selectUserData(map);
        int user_level = (int) userResultMap.get(0).get("user_level"); // 사용자 레벨 개수 가져오기
        int plus_skill_point = (int) userResultMap.get(0).get("plus_skill_point"); // 사용자 플러스 플스 개수 가져오기

        int user_damage_up_skill_count = (int) map.get("user_damage_up_skill_count");
        int user_speed_attack_up_skill_count = (int) map.get("user_speed_attack_up_skill_count");
        int user_start_nut_up_skill_count = (int) map.get("user_start_nut_up_skill_count");
        int user_epic_time_down_up_skill_count = (int) map.get("user_epic_time_down_up_skill_count");
        int user_zombie_max_up_skill_count = (int) map.get("user_zombie_max_up_skill_count");
        int user_game_speed_up_skill_count = (int) map.get("user_game_speed_up_skill_count");
        int user_nut_up_skill_count = (int) map.get("user_nut_up_skill_count");
        int user_gold_up_skill_count = (int) map.get("user_gold_up_skill_count");
        int user_ruby_up_skill_count = (int) map.get("user_ruby_up_skill_count");

        if((user_level * 2) + plus_skill_point < user_damage_up_skill_count + user_speed_attack_up_skill_count + user_start_nut_up_skill_count
                + user_epic_time_down_up_skill_count + user_zombie_max_up_skill_count + user_game_speed_up_skill_count
            + user_nut_up_skill_count + user_gold_up_skill_count + user_ruby_up_skill_count)
        {

            resultMap.put("result", "false");
            resultMap.put("resultNote", "스킬 포인트 보다 더 찍으셨습니다. 다시 시도해주세요.");
            response.setBody(resultMap);

            return new ResponseEntity<>(response, response.getStatus());

        }

        if(user_damage_up_skill_count > 100){
            map.put("user_damage_up_skill_count", 100);
        }else if(user_speed_attack_up_skill_count > 100){
            map.put("user_speed_attack_up_skill_count", 100);
        }else if(user_start_nut_up_skill_count > 100){
            map.put("user_start_nut_up_skill_count", 100);
        }else if(user_epic_time_down_up_skill_count > 100){
            map.put("user_epic_time_down_up_skill_count", 100);
        }else if(user_zombie_max_up_skill_count > 50){
            map.put("user_zombie_max_up_skill_count", 50);
        }else if(user_game_speed_up_skill_count > 100){
            map.put("user_game_speed_up_skill_count", 100);
        }else if(user_nut_up_skill_count > 100){
            map.put("user_nut_up_skill_count", 100);
        }else if(user_gold_up_skill_count > 100){
            map.put("user_gold_up_skill_count", 100);
        }else if(user_ruby_up_skill_count > 100){
            map.put("user_ruby_up_skill_count", 100);
        }

        //등록된거 업데이트
        playerSkillMapper.uptUserPlayerSkill(map);

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> userPlayerSkillList(Map<String, Object> map) throws Exception {

        ApiResponse response = new ApiResponse(HttpStatus.OK, playerSkillMapper.userPlayerSkillList(map).get(0));

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> playerSkillDataList() throws Exception {

        ApiResponse response = new ApiResponse(HttpStatus.OK, playerSkillMapper.playerSkillDataList().get(0));

        return new ResponseEntity<>(response, response.getStatus());

    }

}