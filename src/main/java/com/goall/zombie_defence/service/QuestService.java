package com.goall.zombie_defence.service;

import com.goall.commonUtil.CommonUtil;
import com.goall.zombie_defence.config.JwtProvider;
import com.goall.zombie_defence.mapper.*;
import com.goall.zombie_defence.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class QuestService {

    @Autowired
    QuestMapper questMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    GoodsMapper goodsMapper;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    CommonMapper commonMapper;

    @Transactional
    public ResponseEntity<?> selectQuestCountList(Map<String, Object> map, HttpServletRequest request) throws Exception {

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

        //유저별 일일, 주간 테이블 따로 만들었음.240521
        //일일 테이블 부터 확인 한다.
        int countUserDayQuestData = questMapper.countUserDayQuestData(map);
        if (countUserDayQuestData > 0) {
            //이미 데이터 있음
        } else {
            //데이터 없으니 기본값 인설트 해줘야함
            questMapper.insUserDayQuestData(map);
        }
        //다음은 주간 테이블 확인한다.
        int countUserWeekQuestData = questMapper.countUserWeekQuestData(map);
        if (countUserWeekQuestData > 0) {
            //이미 데이터 있음
        } else {
            //데이터 없으니 기본값 인설트 해줘야함
            questMapper.insUserWeekQuestData(map);
        }

        //유저가 플레이 한 데이터(일일)
        List<HashMap<String, Object>> selectQuestCountDayList = questMapper.userDayQuestData(map);
        //유저가 플레이 한 데이터(주간)
        List<HashMap<String, Object>> selectQuestCountWeekList = questMapper.userWeekQuestData(map);
        //일퀘, 주간퀘 가져오기
        List<HashMap<String, Object>> selectQuestList = questMapper.selectQuestList(map);

        /*//일간 퀘스트 로그를 사용하여 오늘 일퀘 몇 개 했는지 카운트
        List<HashMap<String, Object>> selectQuestLogDayCount = questMapper.selectQuestLogDayCount(map);
        resultMap.put("selectQuestLogDayCount", selectQuestLogDayCount);

        //주간 퀘스트 로그를 사용하여 오늘 일퀘 몇 개 했는지 카운트
        List<HashMap<String, Object>> selectQuestLogWeekCount = questMapper.selectQuestLogWeekCount(map);
        resultMap.put("selectQuestLogWeekCount", selectQuestLogWeekCount);
*/
        //일일 퀘스트 클리어 했는지 안 했는지 리스트
        List<HashMap<String, Object>> selectDayUserQuestClearList = questMapper.selectDayUserQuestClearList(map);
        resultMap.put("selectDayUserQuestClearList", selectDayUserQuestClearList);

        //주간 퀘스트 클리어 했는지 안 했는지 리스트
        List<HashMap<String, Object>> selectWeekUserQuestClearList = questMapper.selectWeekUserQuestClearList(map);
        resultMap.put("selectWeekUserQuestClearList", selectWeekUserQuestClearList);

        resultMap.put("uesrQuestCountDayList", selectQuestCountDayList);
        resultMap.put("uesrQuestCountWeekList", selectQuestCountWeekList);
        resultMap.put("selectQuestList", selectQuestList);

        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> questClear(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();
        map.put("ins_ip", CommonUtil.getRemoteIp(request));
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> tokenMap = jwtProvider.userTokenCheck(map);
        if (tokenMap.get("result").equals("false")) {
            resultMap.put("result", "false");
            resultMap.put("resultNote", "토큰 일치 안 함(중복 로그인임)");
            response.setBody(resultMap);
            return new ResponseEntity<>(response, response.getStatus());
        }

        // quest_eng_name을 콤마로 분리하여 배열로 변환
        String questEngNames = String.valueOf(map.get("quest_eng_name"));
        String[] questEngNameArray = questEngNames.split(",");

        //유저 데이터값 가져오기
        List<HashMap<String, Object>> userResultList = userMapper.selectUserData(map);
        int user_gold = (int) userResultList.get(0).get("user_gold");
        int user_ruby = (int) userResultList.get(0).get("user_ruby");
        int user_key = (int) userResultList.get(0).get("user_key");

        int reward_goods_ruby = 0;
        int reward_goods_gold = 0;
        int reward_goods_key = 0;


        for (String quest_eng_name : questEngNameArray) {
            // 각각의 퀘스트 이름에 대해 처리
            map.put("quest_eng_name", quest_eng_name.trim());

            List<HashMap<String, Object>> selectQuestList = questMapper.selectQuestList(map);

            String quest_reward = String.valueOf(selectQuestList.get(0).get("quest_reward"));
            String quest_day_type = String.valueOf(selectQuestList.get(0).get("quest_day_type")); // day인지 week인지 알아내고 저장해야함
            int quest_reward_count = (int) selectQuestList.get(0).get("quest_reward_count");

            //필터 시작
            //이 퀘스트를 클리어 하기위한 카운트 개수
            int quest_count = (int) selectQuestList.get(0).get("quest_count");

            //유저가 플레이 한 카운트 개수
            int user_quest_count;
            if (quest_day_type.equals("day")) {
                List<HashMap<String, Object>> userDayQuestData = questMapper.userDayQuestData(map);
                user_quest_count = Integer.parseInt(String.valueOf(userDayQuestData.get(0).get(quest_eng_name)));
            } else {
                List<HashMap<String, Object>> userWeekQuestData = questMapper.userWeekQuestData(map);
                user_quest_count = Integer.parseInt(String.valueOf(userWeekQuestData.get(0).get(quest_eng_name)));
            }

            if (quest_count > user_quest_count) {
                resultMap.put("result", "false");
                resultMap.put("resultNote", "퀘스트에 못 미치는 클리어 조건 입니다.");
                response.setBody(resultMap);
                return new ResponseEntity<>(response, response.getStatus());
            }

            String uniqueId = "quest_clear_" + quest_eng_name + "_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(Calendar.getInstance().getTime()) + "_" + RandomStringUtils.randomAlphanumeric(6);
            map.put("quest_unique_idx", uniqueId);
            map.put("quest_day_type", quest_day_type);
            map.put("clear_quest_name", quest_eng_name);

            if (quest_reward.equals("ruby")) {
                map.put("charge_ruby_unique_idx", uniqueId);
                map.put("charge_ruby_quantity", quest_reward_count);
                map.put("before_charge_ruby_quantity", user_ruby);
                map.put("after_charge_ruby_quantity", user_ruby + quest_reward_count);
                goodsMapper.insChargeRubyLog(map);
                map.put("user_ruby", user_ruby + quest_reward_count);
                goodsMapper.uptUserRuby(map);
                reward_goods_ruby += quest_reward_count;
            } else if (quest_reward.equals("gold")) {
                map.put("charge_gold_unique_idx", uniqueId);
                map.put("charge_gold_quantity", quest_reward_count);
                map.put("before_charge_gold_quantity", user_gold);
                map.put("after_charge_gold_quantity", user_gold + quest_reward_count);
                goodsMapper.insChargeGoldLog(map);
                map.put("user_gold", user_gold + quest_reward_count);
                goodsMapper.uptUserGold(map);
                reward_goods_gold += quest_reward_count;

            } else if (quest_reward.equals("key")) {
                map.put("charge_key_unique_idx", uniqueId);
                map.put("charge_key_quantity", quest_reward_count);
                map.put("before_charge_key_quantity", user_key);
                map.put("after_charge_key_quantity", user_key + quest_reward_count);
                goodsMapper.insChargeKeyLog(map);
                map.put("user_key", user_key + quest_reward_count);
                goodsMapper.uptUserKey(map);
                reward_goods_key += quest_reward_count;
            }

            if (quest_day_type.equals("day")) {
                questMapper.uptUserDayQuestData(map);
            } else {//week
                questMapper.uptUserWeekQuestData(map);
            }

            //유저 퀘스트 클리어 로그 저장
            questMapper.insUserQuestLog(map);
        }

        //일일, 주간 퀘스트 카운트 계산해서 퀘스트 완료되었는지

        map.put("clear_quest_name", "day_quest");
        int countUserQuestLogDay = questMapper.countUserQuestLog(map);
        map.put("clear_quest_name", "week_quest");
        int countUserQuestLogWeek = questMapper.countUserQuestLog(map);
        
        if(countUserQuestLogDay <= 0){
            //일일 퀘스트 클리어 된거 없음
            int selectQuestLogDayCount = questMapper.selectQuestLogDayCount(map);
            if(selectQuestLogDayCount >= 6){

                // 각각의 퀘스트 이름에 대해 처리
                map.put("quest_eng_name", "day_quest");
                map.put("clear_quest_name", "day_quest");

                List<HashMap<String, Object>> selectQuestList = questMapper.selectQuestList(map);

                String quest_day_type = String.valueOf(selectQuestList.get(0).get("quest_day_type")); // day인지 week인지 알아내고 저장해야함
                int quest_reward_count = (int) selectQuestList.get(0).get("quest_reward_count");

                String uniqueId = "quest_clear_" + "day_quest" + "_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(Calendar.getInstance().getTime()) + "_" + RandomStringUtils.randomAlphanumeric(6);
                map.put("quest_unique_idx", uniqueId);
                map.put("quest_day_type", quest_day_type);
                map.put("clear_quest_name", "day_quest");

                //클리어한 퀘스트가 6개 이상이면 데이 퀘스트 보상 줘야함
                map.put("charge_ruby_unique_idx", uniqueId);
                map.put("charge_ruby_quantity", quest_reward_count);
                map.put("before_charge_ruby_quantity", user_ruby);
                map.put("after_charge_ruby_quantity", user_ruby + quest_reward_count);
                goodsMapper.insChargeRubyLog(map);
                map.put("user_ruby", user_ruby + quest_reward_count);
                goodsMapper.uptUserRuby(map);
                reward_goods_ruby += quest_reward_count;

                //유저 퀘스트 클리어 로그 저장
                questMapper.insUserQuestLog(map);
            }
        }else{
            //일일 퀘스트 클리어 된거 있음
        }

        if(countUserQuestLogWeek <= 0){
            //주간 퀘스트 클리어 된거 없음
            int selectQuestLogWeekCount = questMapper.selectQuestLogWeekCount(map);
            if(selectQuestLogWeekCount >= 6){
                //클리어한 퀘스트가 6개 이상이면 주간 퀘스트 보상 줘야함
                map.put("quest_eng_name", "week_quest");
                map.put("clear_quest_name", "week_quest");

                List<HashMap<String, Object>> selectQuestList = questMapper.selectQuestList(map);

                String quest_day_type = String.valueOf(selectQuestList.get(0).get("quest_day_type"));
                int quest_reward_count = (int) selectQuestList.get(0).get("quest_reward_count");

                String uniqueId = "quest_clear_" + "week_quest" + "_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(Calendar.getInstance().getTime()) + "_" + RandomStringUtils.randomAlphanumeric(6);
                map.put("quest_unique_idx", uniqueId);
                map.put("quest_day_type", quest_day_type);
                map.put("clear_quest_name", "week_quest");

                map.put("charge_ruby_unique_idx", uniqueId);
                map.put("charge_ruby_quantity", quest_reward_count);
                map.put("before_charge_ruby_quantity", user_ruby);
                map.put("after_charge_ruby_quantity", user_ruby + quest_reward_count);
                goodsMapper.insChargeRubyLog(map);
                map.put("user_ruby", user_ruby + quest_reward_count);
                goodsMapper.uptUserRuby(map);
                reward_goods_ruby += quest_reward_count;

                //유저 퀘스트 클리어 로그 저장
                questMapper.insUserQuestLog(map);
            }
        }else{
            //주간 퀘스트 클리어 된거 있음
        }

        resultMap.put("reward_goods_ruby", reward_goods_ruby);
        resultMap.put("reward_goods_gold", reward_goods_gold);
        resultMap.put("reward_goods_key", reward_goods_key);
        resultMap.put("user_goods_ruby", user_ruby + reward_goods_ruby);
        resultMap.put("user_goods_gold", user_gold + reward_goods_gold);
        resultMap.put("user_goods_key", user_key + reward_goods_key);

        //일일 퀘스트 클리어 했는지 안 했는지 리스트
        List<HashMap<String, Object>> selectDayUserQuestClearList = questMapper.selectDayUserQuestClearList(map);
        resultMap.put("selectDayUserQuestClearList", selectDayUserQuestClearList);

        //주간 퀘스트 클리어 했는지 안 했는지 리스트
        List<HashMap<String, Object>> selectWeekUserQuestClearList = questMapper.selectWeekUserQuestClearList(map);
        resultMap.put("selectWeekUserQuestClearList", selectWeekUserQuestClearList);

        response.setBody(resultMap);
        return new ResponseEntity<>(response, response.getStatus());
    }


    @Transactional
    public ResponseEntity<?> questAdRewardClear(Map<String, Object> map, HttpServletRequest request) throws Exception {

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

        List<HashMap<String, Object>> selectQuestList = questMapper.selectQuestList(map);

        String quest_reward = String.valueOf(selectQuestList.get(0).get("quest_reward"));
        String quest_day_type = String.valueOf(selectQuestList.get(0).get("quest_day_type"));
        int quest_reward_count = (int) selectQuestList.get(0).get("quest_reward_count");
        String quest_eng_name = String.valueOf(map.get("quest_eng_name"));

        map.put("clear_quest_name", quest_eng_name);

        //클리어 했던건지 확인하기
        int user_quest_count;

        List<HashMap<String, Object>> selectQuestLogAdRewardCount = questMapper.selectQuestLogAdRewardCount(map);
        user_quest_count = Integer.parseInt(String.valueOf(selectQuestLogAdRewardCount.get(0).get("adReward_quest")));

        if(1 <= user_quest_count){
            resultMap.put("result", "false");
            resultMap.put("resultNote", "받았던 보상입니다.");
            response.setBody(resultMap);
            return new ResponseEntity<>(response, response.getStatus());
        }

        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        uniqueId = "quest_adReward_clear_" + quest_eng_name + "_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);
        map.put("quest_unique_idx", uniqueId);
        map.put("advertisement_unique_idx", uniqueId);

        map.put("quest_day_type", quest_day_type);

        map.put("clear_quest_name", quest_eng_name);

        //유저 데이터값 가져오기
        List<HashMap<String, Object>> userResultList = userMapper.selectUserData(map);
        int user_gold = (int) userResultList.get(0).get("user_gold");
        int user_ruby = (int) userResultList.get(0).get("user_ruby");
        int user_key = (int) userResultList.get(0).get("user_key");

        //0.5초 기다리기
        /*Thread.sleep(500);*/

        // 이전에 실행된 시간을 가져와서, 일정 시간이 지나지 않으면 요청을 처리하지 않도록 합니다.
/*
        Long questClearpreviousTime = (Long) request.getSession().getAttribute("questClearCurrentTime");
        Long questClearCurrentTime = System.currentTimeMillis();
        if (questClearpreviousTime != null && questClearCurrentTime - questClearpreviousTime < TimeUnit.SECONDS.toMillis(1)) {
            response.setResult("false");
            return new ResponseEntity<>(response, response.getStatus());
        }
        request.getSession().setAttribute("questClearCurrentTime", questClearCurrentTime);
*/

        if(quest_reward.equals("ruby")){

            map.put("charge_ruby_unique_idx", uniqueId); // 유니크값 들어간 이유
            map.put("charge_ruby_quantity", quest_reward_count); // DB에 넣기 위한 게임 결과 루비
            map.put("before_charge_ruby_quantity", user_ruby); // DB에 넣기 위한 충전 전 루비값 저장
            map.put("after_charge_ruby_quantity", user_ruby + quest_reward_count); // DB에 넣기 위한 충전 후 루비 값 저장
            goodsMapper.insChargeRubyLog(map); // 루비 충전 로그 저장

            map.put("user_ruby", user_ruby+ quest_reward_count); // user_data의 최종 루비 값 넣기

            resultMap.put("user_ruby", user_ruby+ quest_reward_count);//결과 클라에 주기
            resultMap.put("reward_goods", quest_reward_count);//보상 리워드값

            //유저 루비 업데이트
            goodsMapper.uptUserRuby(map);

        }else if(quest_reward.equals("gold")){

            //골드 충전값 계산
            map.put("charge_gold_unique_idx", uniqueId); // 유니크값 들어간 이유
            map.put("charge_gold_quantity", quest_reward_count); // DB에 넣기 위한 게임 결과 골드
            map.put("before_charge_gold_quantity", user_gold); // DB에 넣기 위한 충전 전 골드값 저장
            map.put("after_charge_gold_quantity", user_gold + quest_reward_count); // DB에 넣기 위한 충전 후 골드 값 저장
            goodsMapper.insChargeGoldLog(map); // 골드 충전 로그 저장

            map.put("user_gold", user_gold + quest_reward_count); // user_data의 최종 골드 값 넣기
            resultMap.put("reward_goods", quest_reward_count);//보상 리워드값

            resultMap.put("user_gold", user_gold + quest_reward_count);//결과 클라에 주기

            //유저 골드 업데이트
            goodsMapper.uptUserGold(map);

        }else if(quest_reward.equals("key")){

            map.put("charge_key_unique_idx", uniqueId); // 유니크값 들어간 이유
            map.put("charge_key_quantity", quest_reward_count); // DB에 넣기 위한 충전 키 값 저장
            map.put("before_charge_key_quantity", user_key); // DB에 넣기 위한 충전 전 키 값 저장
            map.put("after_charge_key_quantity", user_key + quest_reward_count); // DB에 넣기 위한 충전 후 키 값 저장
            goodsMapper.insChargeKeyLog(map); // 키 충전 로그 저장

            map.put("user_key", user_key + quest_reward_count); // user_data의 최종 키값 넣기
            resultMap.put("reward_goods", quest_reward_count);//보상 리워드값

            resultMap.put("user_key", user_key + quest_reward_count);//결과 클라에 주기

            //유저키업데이트
            goodsMapper.uptUserKey(map);

        }

        //광고 봤냐 안봤냐
        String advertisement_yn = String.valueOf(map.get("advertisement_yn"));
        map.put("skip_yn", advertisement_yn);
        commonMapper.insAdvertisementLog(map); // 광고 본거 로그

        //유저 퀘스트 클리어 로그 저장
        questMapper.insUserQuestLog(map);
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

}
