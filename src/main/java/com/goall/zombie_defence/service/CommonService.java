package com.goall.zombie_defence.service;

import com.goall.zombie_defence.mapper.CommonMapper;
import com.goall.zombie_defence.mapper.InGameMapper;
import com.goall.zombie_defence.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CommonService {

    @Autowired
    CommonMapper commonMapper;

    @Autowired
    InGameMapper inGameMapper;

    public ResponseEntity<?> nowDataTimeAndGameVersion(Map<String, Object> map) throws Exception {

        ApiResponse response = new ApiResponse(HttpStatus.OK, commonMapper.nowDataTimeAndGameVersion(map));

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> gameCommonData(Map<String, Object> map) throws Exception {

        ApiResponse response = new ApiResponse();

        Map<String, Object> stageMap;
        stageMap = new HashMap<String, Object>();

        Map<String, Object> inGameRulesMap;
        inGameRulesMap = new HashMap<String, Object>();

        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

        //스테이지 정보 가져오기
        stageMap.put("stage_data", inGameMapper.stageAllList2(map));
        resultList.add(stageMap);

        response.setBody(resultList);

        return new ResponseEntity<>(response, response.getStatus());

    }

}
/*

    // 스테이지별로 구분할 Map 생성
    Map<String, Map<String, Map<String, Object>>> stageMap = new HashMap<>();
    // DB에서 스테이지 정보를 가져옴
    List<Map<String, Object>> responseBody = inGameMapper.waveAllList(map);

// 응답바디를 순회하면서 각 스테이지별로 정보를 저장
for (Map<String, Object> stageInfo : responseBody) {
        // 각 스테이지의 game_mode, game_level, wave_num 가져옴
        String gameMode = (String) stageInfo.get("game_mode");
        int gameLevel = (Integer) stageInfo.get("game_level");
        int waveNum = (Integer) stageInfo.get("wave_num");
        // stage_1, stage_2, ... 이런식으로 stage key 생성
        String stageKey = "stage_" + gameLevel;
        // stageMap에 stageKey가 없다면 새로 생성
        if (!stageMap.containsKey(stageKey)) {
        stageMap.put(stageKey, new HashMap<>());
        }
        // wave_1, wave_2, ... 이런식으로 wave key 생성
        String waveKey = "wave_" + waveNum;
        // 해당 stage에 waveKey가 없다면 새로 생성
        if (!stageMap.get(stageKey).containsKey(waveKey)) {
        stageMap.get(stageKey).put(waveKey, new HashMap<>());
        }
        // game_mode가 easy인 경우만 저장
        if (gameMode.equals("easy")) {
        stageMap.get(stageKey).get(waveKey).putAll(stageInfo);
        }
        }*/
