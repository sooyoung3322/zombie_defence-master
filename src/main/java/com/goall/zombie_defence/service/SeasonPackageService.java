package com.goall.zombie_defence.service;

import com.goall.commonUtil.CommonUtil;
import com.goall.zombie_defence.config.JwtProvider;
import com.goall.zombie_defence.mapper.EpicSkillMapper;
import com.goall.zombie_defence.mapper.GoodsMapper;
import com.goall.zombie_defence.mapper.SeasonPackageMapper;
import com.goall.zombie_defence.mapper.UserMapper;
import com.goall.zombie_defence.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class SeasonPackageService {

    @Autowired
    SeasonPackageMapper seasonPackageMapper;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    UserMapper userMapper;

    @Autowired
    GoodsMapper goodsMapper;

    @Autowired
    EpicSkillMapper epicSkillMapper;


    public ResponseEntity<?> selectSeasonPackageList(Map<String, Object> map) throws Exception {

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        //시즌 정보 가져오기
        List<HashMap<String, Object>> selectSeasonNumberList = seasonPackageMapper.selectSeasonNumberList(map);
        resultMap.put("selectSeasonNumberList", selectSeasonNumberList);

        //그래서 몇 시즌임?
        int season_number = (int) selectSeasonNumberList.get(0).get("season_number");
        map.put("season_number", season_number);

        //시즌보상 데이터 리스트 가져오기
        List<HashMap<String, Object>> selectSeasonPackageList = seasonPackageMapper.selectSeasonPackageList(map);
        resultMap.put("selectSeasonPackageList", selectSeasonPackageList);

        //유저 시즌 정보 가져오기
        List<HashMap<String, Object>> selectUserSeasonPackageList = seasonPackageMapper.selectUserSeasonPackageList(map);
        resultMap.put("selectUserSeasonPackageList", selectUserSeasonPackageList);

        //이 유저 마지막로그인과 시즌 시작날짜 계산해서 팝업창 안내해줄지말지 (트루가 팝업창 안보여주고 폴스가 보여주는거임)
        resultMap.put("isSeasonStartedTrueFalse", seasonPackageMapper.isSeasonStartedTrueFalse(map));
        ApiResponse response = new ApiResponse(HttpStatus.OK, resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> insUserSeasonPackageInsertData(Map<String, Object> map, HttpServletRequest request) throws Exception {

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

        //0. 중복으로 받는건지 체크
        int season_data_check = seasonPackageMapper.selectUserSeasonPackageInsertDataCount(map);
        if(season_data_check > 0) {
            //실패값, 받았던보상또받을라한다.
            resultMap.put("result", "false");
            resultMap.put("resultNote", "받았던 보상입니다.");
            response.setBody(resultMap);
            return new ResponseEntity<>(response, response.getStatus());
        }
        //1.티어 구매자인지 체크
        List<HashMap<String, Object>> selectUserSeasonPackageList = seasonPackageMapper.selectUserSeasonPackageList(map);
        int user_season_package_tier = (int) selectUserSeasonPackageList.get(0).get("season_package_tier");//유저 티어 상태값
        int season_package_tier = (int) map.get("season_package_tier"); // 요청한 티어값
        if(user_season_package_tier < season_package_tier){
            //실패값, 패스구매안했는데 요청하려고한다.
            resultMap.put("result", "false");
            resultMap.put("resultNote", "티어구매자가 아닙니다.");
            response.setBody(resultMap);
            return new ResponseEntity<>(response, response.getStatus());
        }
        //2.경험치 량 체크
        int user_season_package_exp = (int) selectUserSeasonPackageList.get(0).get("season_package_exp");//유저 이번시즌 경험치값
        int season_package_level = (int) map.get("season_package_level");//유저 티어 상태값
        if(user_season_package_exp / 100 < season_package_level){
            //실패값, 시즌 경험치가 부족한 상황
            resultMap.put("result", "false");
            resultMap.put("resultNote", "시즌 경험치가 부족합니다.");
            response.setBody(resultMap);
            return new ResponseEntity<>(response, response.getStatus());
        }
        //3.어떤 재화 인제 체크
        List<HashMap<String, Object>> selectSeasonNumberRewardList = seasonPackageMapper.selectSeasonNumberRewardList(map);
        String reward_kinds = String.valueOf(selectSeasonNumberRewardList.get(0).get("reward_kinds"));
        int reward_count = (int) selectSeasonNumberRewardList.get(0).get("reward_count");
        resultMap.put("reward_kinds", reward_kinds);
        resultMap.put("reward_count", reward_count);

        //4.재화 체크 후 보상 인설트
        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());
        uniqueId =  "season_package_" + season_package_tier + "_" + season_package_level + "_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);

        //유저 데이터값 가져오기
        List<HashMap<String, Object>> userResultList = userMapper.selectUserData(map);

        if(reward_kinds.equals("ruby")){
            int user_ruby = (int) userResultList.get(0).get("user_ruby");
            //루비 충전값 계산
            int ruby_count = reward_count;

            map.put("charge_ruby_unique_idx", uniqueId); // 유니크값 들어간 이유
            map.put("charge_ruby_quantity", ruby_count); // DB에 넣기 위한 게임 결과 루비
            map.put("before_charge_ruby_quantity", user_ruby); // DB에 넣기 위한 충전 전 루비값 저장
            map.put("after_charge_ruby_quantity", user_ruby + ruby_count); // DB에 넣기 위한 충전 후 루비 값 저장
            goodsMapper.insChargeRubyLog(map); // 루비 충전 로그 저장

            map.put("user_ruby", user_ruby+ ruby_count); // user_data의 최종 루비 값 넣기

            resultMap.put("user_ruby", user_ruby+ ruby_count);//결과 클라에 주기

            //유저 루비 업데이트
            goodsMapper.uptUserRuby(map);

        }else if(reward_kinds.equals("gold")){
            int user_gold = (int) userResultList.get(0).get("user_gold");
            //골드 충전값 계산
            int gold_count = reward_count;
            map.put("charge_gold_unique_idx", uniqueId); // 유니크값 들어간 이유
            map.put("charge_gold_quantity", gold_count); // DB에 넣기 위한 게임 결과 골드
            map.put("before_charge_gold_quantity", user_gold); // DB에 넣기 위한 충전 전 골드값 저장
            map.put("after_charge_gold_quantity", user_gold + gold_count); // DB에 넣기 위한 충전 후 골드 값 저장
            goodsMapper.insChargeGoldLog(map); // 골드 충전 로그 저장

            map.put("user_gold", user_gold + gold_count); // user_data의 최종 골드 값 넣기

            resultMap.put("user_gold", user_gold + gold_count);//결과 클라에 주기

            //유저 골드 업데이트
            goodsMapper.uptUserGold(map);

        }else if(reward_kinds.equals("key")){
            int user_key = (int) userResultList.get(0).get("user_key");
            //키 충전값 계산
            int key_count = reward_count;
            map.put("charge_key_unique_idx", uniqueId); // 유니크값 들어간 이유
            map.put("charge_key_quantity", key_count); // DB에 넣기 위한 충전 키 값 저장
            map.put("before_charge_key_quantity", user_key); // DB에 넣기 위한 충전 전 키 값 저장
            map.put("after_charge_key_quantity", user_key + key_count); // DB에 넣기 위한 충전 후 키 값 저장
            goodsMapper.insChargeKeyLog(map); // 키 충전 로그 저장

            map.put("user_key", user_key + key_count); // user_data의 최종 키값 넣기

            resultMap.put("user_key", user_key + key_count);//결과 클라에 주기

            //유저키업데이트
            goodsMapper.uptUserKey(map);

        }else if(reward_kinds.equals("boss")){
            int user_bp = (int) userResultList.get(0).get("user_bp");
            //보스포인트 충천 시작
            int boss_count = reward_count;
            map.put("charge_reason", "시즌패스보상");
            map.put("charge_bp_unique_idx", uniqueId); // 유니크값 들어간 이유
            map.put("charge_bp_quantity", boss_count); // DB에 넣기 위한 충전 보스포인트
            map.put("before_charge_bp_quantity", user_bp); // DB에 넣기 위한 충전 전 보스포인트값 저장
            map.put("after_charge_bp_quantity", user_bp + boss_count); // DB에 넣기 위한 충전 후 보스포인트 값 저장
            goodsMapper.insChargeBpLog(map); // 보스포인트 충전 로그 저장

            map.put("user_bp", user_bp + boss_count); // user_data의 최종 보스포인트 값 넣기
            resultMap.put("user_bp", user_bp + boss_count); // user_data의 최종 보스포인트 값 넣기

            //유저 루비 업데이트
            goodsMapper.uptUserBp(map);
        }else if(reward_kinds.equals("epic_skill")){
            //에픽 스킬 인설트 해주기
            map.put("user_epic_skill_unique_idx", uniqueId); // 유니크값 들어간 이유
            int reward_item_idx = (int) selectSeasonNumberRewardList.get(0).get("reward_item_idx");
            int reward_item_level = (int) selectSeasonNumberRewardList.get(0).get("reward_item_level");
            int reward_item_grade = (int) selectSeasonNumberRewardList.get(0).get("reward_item_grade");
            map.put("user_epic_skill_idx", reward_item_idx); // DB에 넣기 위한 아이템 SEQ값(쭝요)
            map.put("user_epic_skill_level", reward_item_level); // DB에 넣기 위한 아이템 레벨 값
            map.put("user_epic_skill_grade", reward_item_grade); // DB에 넣기 위한 아이템 등급 값

            resultMap.put("user_epic_skill_idx", reward_item_idx);//결과 클라에 주기
            resultMap.put("user_epic_skill_level", reward_item_level);//결과 클라에 주기
            resultMap.put("user_epic_skill_grade", reward_item_grade);//결과 클라에 주기

            epicSkillMapper.insUserEpicSkill(map);
        }else if(reward_kinds.equals("player_skill")){
            int plus_skill_point = reward_count;
            //플레이어 스킬 받기
            map.put("plus_skill_point", plus_skill_point);
            resultMap.put("plus_skill_point", plus_skill_point);

            //플레이어 스킬 그만큼 더해주기
            userMapper.uptUserepickSkillPlus(map);
        }

        //6.시즌 데이터 입력
        map.put("seasion_package_received", 1);
        seasonPackageMapper.insUserSeasonPackageInsertData(map);
        //7.클라쪽에 무슨 재화 받았는지, 재화를 받았다면 유저 전체 재화수 계산 후 보내주기 보내주기

        resultMap.put("userData", userMapper.selectUserData(map));

        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());
    }


}