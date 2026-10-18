package com.goall.zombie_defence.service;

import com.goall.commonUtil.CommonUtil;
import com.goall.zombie_defence.config.JwtProvider;
import com.goall.zombie_defence.mapper.*;
import com.goall.zombie_defence.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Service
public class OfflineModeService {

    @Autowired
    OfflineModeMapper offlineModeMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    GoodsMapper goodsMapper;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    CommonMapper commonMapper;

    @Transactional
    public ResponseEntity<?> selectOfflineModeLog(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        Map<String, Object> tokenMap = jwtProvider.userTokenCheck(map);

        if(tokenMap.get("result").equals("false")) {

            resultMap.put("result", "false");
            resultMap.put("resultNote", "토큰 일치 안 함(중복 로그인임)");
            response.setBody(resultMap);

            return new ResponseEntity<>(response, response.getStatus());
        }

        //오프라인 모드 계산을 위해 유저의 최고 스테이지, 웨이브를 가져오고 지금시간, 마지막으로 오프라인 모드 받은 시간을 가져온다.
        List<HashMap<String, Object>> offlineModeLog = offlineModeMapper.selectOfflineModeLog(map);

        LocalDateTime now_date_time;
        LocalDateTime start_date;
        int user_game_level;
        int user_game_wave;

        LocalDateTime now = LocalDateTime.now();

        // datetime 형식으로 포맷팅
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        //offlineModeLog 데이터가 없을때
        if (offlineModeLog.isEmpty()) {

            user_game_level = 0;
            user_game_wave = 10;

            now_date_time = LocalDateTime.parse(formattedDateTime, formatter);
            start_date = LocalDateTime.parse("2000-01-01 00:00:00", formatter);
        }else{
            user_game_level = Integer.parseInt((String) offlineModeLog.get(0).get("game_level"));
            user_game_wave = (int) offlineModeLog.get(0).get("game_wave");

            now_date_time = (LocalDateTime) offlineModeLog.get(0).get("now_date_time");
            start_date = (LocalDateTime) offlineModeLog.get(0).get("start_date");

            if (start_date == null) {
                // start_date가 null이 아닌 경우의 처리
                start_date = LocalDateTime.parse("2000-01-01 00:00:00", formatter);

            }
        }

        // LocalDateTime을 Timestamp로 변환
        Timestamp timestamp1 = Timestamp.valueOf(now_date_time);
        Timestamp timestamp2 = Timestamp.valueOf(start_date);

        // Timestamp 간의 차이 계산
        long millisecondsDifference = timestamp1.getTime() - timestamp2.getTime();
        long minutesDifference = millisecondsDifference / (60 * 1000);//최종 차이값(분단위)

        int finalTimeMinutes = (int) minutesDifference;


        if(finalTimeMinutes >= 720){
            finalTimeMinutes = 720;
        }

        //180000인 이유는 이 코드를 짤때 가장 고인물이 가장 많이 받을수있는 수준이 약 180000이였음. 그래서 뉴비랑 2배 차이 벌리기위해 정한 수치
        float finalCompensationInt = (finalTimeMinutes * ((user_game_level * 50) + user_game_wave)) + 180000;

        //소수점에서 올림
        float inserRuby = (float) Math.ceil(finalCompensationInt * 0.001);
        float inserGold = (float) Math.ceil(finalCompensationInt * 0.025);
        int insertEpickSkill = finalTimeMinutes / 120;

        resultMap.put("finalTimeMinutes", finalTimeMinutes);
        resultMap.put("inserRuby", inserRuby);
        resultMap.put("inserGold", inserGold);
        resultMap.put("insertEpickSkill", insertEpickSkill);

        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

}
