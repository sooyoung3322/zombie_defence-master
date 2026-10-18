package com.goall.zombie_defence.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.goall.commonUtil.CommonUtil;
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
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class StatisticsService {

    @Autowired
    StatisticsMapper statisticsMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    GoodsMapper goodsMapper;

    @Autowired
    InGameMapper inGameMapper;

    public ResponseEntity<?> totalGameRanking(Map<String, Object> map) throws Exception {

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> totalGameRanking = statisticsMapper.totalGameRanking(map);
        resultMap.put("totalGameRanking", totalGameRanking);

        /*map.put("week_data", "Y");*/

        List<HashMap<String, Object>> weekTotalGameRanking = statisticsMapper.totalWeekGameRanking(map);
        resultMap.put("weekTotalGameRanking", weekTotalGameRanking);

        ApiResponse response = new ApiResponse(HttpStatus.OK, resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> myTotalGameRanking(Map<String, Object> map) throws Exception {

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> myTotalGameRanking = statisticsMapper.myTotalGameRanking(map);
        resultMap.put("myTotalGameRanking", myTotalGameRanking);

        /*map.put("week_data", "Y");*/

        List<HashMap<String, Object>> weekMyTotalGameRanking = statisticsMapper.myTotalWeekGameRanking(map);
        resultMap.put("weekMyTotalGameRanking", weekMyTotalGameRanking);

        ApiResponse response = new ApiResponse(HttpStatus.OK, resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> totalLevelRanking(Map<String, Object> map) throws Exception {

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> TotalLevelRanking = statisticsMapper.TotalLevelRanking(map);
        resultMap.put("TotalLevelRanking", TotalLevelRanking);

        List<HashMap<String, Object>> myTotalLevelRanking = statisticsMapper.myTotalLevelRanking(map);
        resultMap.put("myTotalLevelRanking", myTotalLevelRanking);

        ApiResponse response = new ApiResponse(HttpStatus.OK, resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> myLastWeekTotalGameRanking(Map<String, Object> map, HttpServletRequest request) throws Exception {

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        List<HashMap<String, Object>> gameRankRewardLogList = statisticsMapper.gameRankRewardLogList(map);

        if (gameRankRewardLogList.size() != 0) {
            resultMap.put("result", "reward_not");
            resultMap.put("result_memo", "이미 보상을 받은 유저");
            resultMap.put("resultNote", "이미 보상을 받았거나 보상을 받을 수 없는 순위 조건입니다.");
            response.setBody(resultMap);
            return new ResponseEntity<>(response, response.getStatus());
        }

        HashMap<String, Object> stageMap;
        stageMap = new HashMap<String, Object>();

        Map<String, Object> resultListMap;
        resultListMap = new HashMap<String, Object>();

        Map<String, Object> rankRewardLogMap;
        rankRewardLogMap = new HashMap<String, Object>();

        List<Map<String, Object>> rankLogList = new ArrayList<Map<String, Object>>();

        List<HashMap<String, Object>> stageAllList = inGameMapper.stageAllList2(map);
        int reward_ruby_sum = 0;
        int reward_bp_sum = 0;

        //충전 전 루비
        List<HashMap<String, Object>> notChargeUserResultList = userMapper.selectUserData(map);
        int not_charge_user_ruby = (int) notChargeUserResultList.get(0).get("user_ruby");
        int not_charge_user_bp = (int) notChargeUserResultList.get(0).get("user_bp");

        for (int i = 0; i <= stageAllList.size() - 1; i++) {

            stageMap = new HashMap<String, Object>();
            resultListMap = new HashMap<String, Object>();
            rankRewardLogMap = new HashMap<String, Object>();

            String game_mode = String.valueOf(stageAllList.get(i).get("game_mode"));
            stageMap.put("game_mode", game_mode);
            String game_level = String.valueOf(stageAllList.get(i).get("game_level"));
            stageMap.put("game_level", game_level);
            String game_title = String.valueOf(stageAllList.get(i).get("game_title"));
            stageMap.put("game_title", game_title);
            stageMap.put("user_uuid", map.get("user_uuid"));

            List<HashMap<String, Object>> myLastWeekTotalGameRanking = statisticsMapper.myLastWeekTotalGameRanking(stageMap);

            String uniqueId = "";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            Calendar dateTime = Calendar.getInstance();
            uniqueId = sdf.format(dateTime.getTime());

            //유니크값 넣기
            uniqueId = game_mode + "_" + game_level + "_" + "game_reward_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);
            map.put("game_reward_unique_idx", uniqueId);
            rankRewardLogMap.put("game_reward_unique_idx", uniqueId);

            //보상 받을게 없을때는 빈값 저장
            if (myLastWeekTotalGameRanking.size() == 0) {

                resultListMap.put("game_mode_" + i, game_mode);
                resultListMap.put("game_level_" + i, game_level);
                resultListMap.put("game_title_" + i, game_title);
                resultListMap.put("game_reward_" + i, 0);
                resultListMap.put("game_rank_" + i, 0);
                resultMap.put("stage_"+i, resultListMap);

                //로그 데이터 넣을라면 추가했어야함
                rankRewardLogMap.put("start_seq", 0);
                rankRewardLogMap.put("game_mode", game_mode);
                rankRewardLogMap.put("game_level", game_level);
                rankRewardLogMap.put("user_rank", 0);
                rankRewardLogMap.put("reward_ruby", 0);
                rankRewardLogMap.put("reward_bp", 0);

                rankLogList.add(rankRewardLogMap);

                continue;
            }

            int game_rank = Integer.parseInt(String.valueOf(myLastWeekTotalGameRanking.get(0).get("game_rank")));
            int rank_reward;

            if(game_rank == 1){
                rank_reward = (int) stageAllList.get(i).get("week_rank_1_reward");
            }else if(game_rank == 2){
                rank_reward = (int) stageAllList.get(i).get("week_rank_2_reward");
            }else if(game_rank == 3){
                rank_reward = (int) stageAllList.get(i).get("week_rank_3_reward");
            }else if(game_rank >= 4 && game_rank <= 10){
                rank_reward = (int) stageAllList.get(i).get("week_rank_4_10_reward");
            }else if(game_rank >= 11 && game_rank <= 30){
                rank_reward = (int) stageAllList.get(i).get("week_rank_11_30_reward");
            }else{
                rank_reward = (int) stageAllList.get(i).get("week_rank_etc_reward");
            }

            //보상 받을꺼 있을때 저장
            resultListMap.put("game_mode_" + i, game_mode);
            resultListMap.put("game_level_" + i, game_level);
            resultListMap.put("game_title_" + i, game_title);
            resultListMap.put("game_reward_" + i, rank_reward);
            resultListMap.put("game_rank_" + i, game_rank);
            if(game_mode.equals("easy")) {
                reward_ruby_sum = reward_ruby_sum + rank_reward;
            }else if(game_mode.equals("boss")){
                reward_bp_sum = reward_bp_sum + rank_reward;
            }
            resultMap.put("stage_"+i, resultListMap);

            if(game_mode.equals("easy")) {

                //루비 충전 시작
                List<HashMap<String, Object>> userResultList = userMapper.selectUserData(map);
                int user_ruby = (int) userResultList.get(0).get("user_ruby");

                map.put("charge_ruby_unique_idx", uniqueId); // 유니크값 들어간 이유
                map.put("charge_ruby_quantity", rank_reward); // DB에 넣기 위한 충전 루비
                map.put("before_charge_ruby_quantity", user_ruby); // DB에 넣기 위한 충전 전 루비값 저장
                map.put("after_charge_ruby_quantity", user_ruby + rank_reward); // DB에 넣기 위한 충전 후 루비 값 저장
                goodsMapper.insChargeRubyLog(map); // 루비 충전 로그 저장

                map.put("result_ruby", user_ruby + rank_reward); // user_data의 최종 루비 값 넣기
                map.put("user_ruby", user_ruby + rank_reward); // user_data의 최종 루비 값 넣기

                //유저 루비 업데이트
                goodsMapper.uptUserRuby(map);

                //로그 데이터 넣을라면 추가했어야함
                rankRewardLogMap.put("start_seq", myLastWeekTotalGameRanking.get(0).get("start_seq"));
                rankRewardLogMap.put("game_mode", game_mode);
                rankRewardLogMap.put("game_level", game_level);
                rankRewardLogMap.put("user_rank", game_rank);
                rankRewardLogMap.put("reward_ruby", rank_reward);
                rankRewardLogMap.put("reward_bp", 0); // 루비충전쪽은 보스 포인트 0개
                /*rankRewardLogMap.put("rank_start_date", myLastWeekTotalGameRanking.get(0).get("rank_start_date"));
                rankRewardLogMap.put("rank_end_date", myLastWeekTotalGameRanking.get(0).get("rank_end_date"));*/

                rankLogList.add(rankRewardLogMap);
            }else if(game_mode.equals("boss")){
                //보스포인트 충천 시작
                List<HashMap<String, Object>> userResultList = userMapper.selectUserData(map);
                int user_bp = (int) userResultList.get(0).get("user_bp");

                map.put("charge_bp_unique_idx", uniqueId); // 유니크값 들어간 이유
                map.put("charge_bp_quantity", rank_reward); // DB에 넣기 위한 충전 루비
                map.put("before_charge_bp_quantity", user_bp); // DB에 넣기 위한 충전 전 루비값 저장
                map.put("after_charge_bp_quantity", user_bp + rank_reward); // DB에 넣기 위한 충전 후 루비 값 저장
                goodsMapper.insChargeBpLog(map); // 루비 충전 로그 저장

                map.put("result_bp", user_bp + rank_reward); // user_data의 최종 루비 값 넣기
                map.put("user_bp", user_bp + rank_reward); // user_data의 최종 루비 값 넣기

                //유저 루비 업데이트
                goodsMapper.uptUserBp(map);

                //로그 데이터 넣을라면 추가했어야함
                rankRewardLogMap.put("start_seq", myLastWeekTotalGameRanking.get(0).get("start_seq"));
                rankRewardLogMap.put("game_mode", game_mode);
                rankRewardLogMap.put("game_level", game_level);
                rankRewardLogMap.put("user_rank", game_rank);
                rankRewardLogMap.put("reward_ruby", 0);// 보스충전쪽은 루비 0개
                rankRewardLogMap.put("reward_bp", rank_reward);
                /*rankRewardLogMap.put("rank_start_date", myLastWeekTotalGameRanking.get(0).get("rank_start_date"));
                rankRewardLogMap.put("rank_end_date", myLastWeekTotalGameRanking.get(0).get("rank_end_date"));*/

                rankLogList.add(rankRewardLogMap);
            }
        }

        map.put("rankLogList", rankLogList);

        //로그 인설트
        statisticsMapper.insGameRankRewardLog(map);

        resultMap.put("result_ruby", reward_ruby_sum + not_charge_user_ruby);
        resultMap.put("result_bp", reward_bp_sum + not_charge_user_bp);

        response.setBody(resultMap);

        //0.5초 기다리기
        /*Thread.sleep(500);*/

        // 이전에 실행된 시간을 가져와서, 일정 시간이 지나지 않으면 요청을 처리하지 않도록 합니다.
        Long rankRewardClearpreviousTime = (Long) request.getSession().getAttribute("rankRewardClearCurrentTime");
        Long rankRewardClearCurrentTime = System.currentTimeMillis();
        if (rankRewardClearpreviousTime != null && rankRewardClearCurrentTime - rankRewardClearpreviousTime < TimeUnit.SECONDS.toMillis(1)) {
            response.setResult("false");
            return new ResponseEntity<>(response, response.getStatus());
        }
        request.getSession().setAttribute("rankRewardClearCurrentTime", rankRewardClearCurrentTime);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> gameRankRewardLogList(Map<String, Object> map, HttpServletRequest request) throws Exception {

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        Map<String, Object> stageMap;
        stageMap = new HashMap<String, Object>();

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        List<HashMap<String, Object>> gameRankRewardLogList = statisticsMapper.gameRankRewardLogList(map);

        //랭킹 결과 값에 쓸꺼 있는지 확인
        List<HashMap<String, Object>> stageAllList = inGameMapper.stageAllList(map);
        int reward_ruby_sum = 0;
        int game_reward_check = 0;
        int game_reward_check_2 = 0;

        for (int i = 0; i <= stageAllList.size() - 1; i++) {

            stageMap = new HashMap<String, Object>();

            String game_mode = String.valueOf(stageAllList.get(i).get("game_mode"));
            stageMap.put("game_mode", game_mode);
            String game_level = String.valueOf(stageAllList.get(i).get("game_level"));
            stageMap.put("game_level", game_level);
            String game_title = String.valueOf(stageAllList.get(i).get("game_title"));
            stageMap.put("game_title", game_title);
            stageMap.put("user_uuid", map.get("user_uuid"));

            List<HashMap<String, Object>> myLastWeekTotalGameRanking = statisticsMapper.myLastWeekTotalGameRanking(stageMap);

            if(myLastWeekTotalGameRanking.size() != 0){
                game_reward_check = game_reward_check + 1;
            }

        }

        LocalDate startDate = LocalDate.of(2023, 3, 20);
        LocalDate endDate = LocalDate.of(2023, 3, 26);
        LocalDate dateToCheck = LocalDate.now();

        if (dateToCheck.isEqual(startDate) || dateToCheck.isEqual(endDate) ||
                (dateToCheck.isAfter(startDate) && dateToCheck.isBefore(endDate))) {
            // 2023, 3, 20 이상, 2023, 3, 26 이하인 경우
        } else {
            // 그 외의 경우
            game_reward_check_2++;
        }

        if (gameRankRewardLogList.size() != 0 || game_reward_check == 0 || game_reward_check_2 == 0) {
            resultMap.put("result", "reward_n");
            resultMap.put("result_memo", "이미 보상을 받은 유저");
            resultMap.put("resultNote", "이미 보상을 받았거나 보상을 받을 수 없는 순위 조건입니다.");
            response.setBody(resultMap);
            return new ResponseEntity<>(response, response.getStatus());
        }else{
            resultMap.put("result", "reward_y");
            resultMap.put("result_memo", "보상을 받을 수 있습니다.");
            resultMap.put("resultNote", "보상을 받을 수 있는 상태임");
            response.setBody(resultMap);
            return new ResponseEntity<>(response, response.getStatus());
        }
    }
}