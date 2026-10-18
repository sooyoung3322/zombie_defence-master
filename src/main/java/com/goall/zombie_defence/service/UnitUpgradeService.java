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

@RequiredArgsConstructor
@Service
public class UnitUpgradeService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UnitUpgradeMapper unitUpgradeMapper;

    @Autowired
    GoodsMapper goodsMapper;

    public ResponseEntity<?> unitUpgradeData(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse(HttpStatus.OK, unitUpgradeMapper.unitUpgradeData(map));

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> unitUpgradeButton(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        uniqueId = "unitUpgrade_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);

        map.put("unit_upgrade_unique_idx", uniqueId);

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        //유저 데이터값 가져오기
        List<HashMap<String, Object>> userResultList = userMapper.selectUserData(map);
        int user_ruby = (int) userResultList.get(0).get("user_ruby");
        int user_gold = (int) userResultList.get(0).get("user_gold");
        int user_level = (int) userResultList.get(0).get("user_level");
        int user_plus_skill_point = (int) userResultList.get(0).get("plus_skill_point");

        //클라에서 보내준 업그레이드 타입(무엇을 업그레이드 할라고 누른지)
        String upgrade_type = String.valueOf(map.get("upgrade_type"));

        List<HashMap<String, Object>> userUnitUpgradeDataList = unitUpgradeMapper.userUnitUpgradeData(map);
        int ar_unit_upgrade_level = (int) userUnitUpgradeDataList.get(0).get("ar_unit_upgrade_level");
        int sg_unit_upgrade_level = (int) userUnitUpgradeDataList.get(0).get("sg_unit_upgrade_level");
        int sp_unit_upgrade_level = (int) userUnitUpgradeDataList.get(0).get("sp_unit_upgrade_level");
        map.put("advertisement_yn", "N");//광고아님

        //유저가 찍는 레벨 확인하기
        if(upgrade_type.equals("ar")){
            map.put("upgrade_level", ar_unit_upgrade_level);
            map.put("ar_unit_upgrade_level", "Y");
            map.put("unit_upgrade_type", "ar");
            map.put("unit_upgrade_level", ar_unit_upgrade_level+1);
            resultMap.put("before_unit_level", ar_unit_upgrade_level);
            resultMap.put("after_unit_level", ar_unit_upgrade_level+1);
        }else if(upgrade_type.equals("sg")){
            map.put("upgrade_level", sg_unit_upgrade_level);
            map.put("sg_unit_upgrade_level", "Y");
            map.put("unit_upgrade_type", "sg");
            map.put("unit_upgrade_level", sg_unit_upgrade_level+1);
            resultMap.put("before_unit_level", sg_unit_upgrade_level);
            resultMap.put("after_unit_level", sg_unit_upgrade_level+1);
        }else if(upgrade_type.equals("sp")){
            map.put("upgrade_level", sp_unit_upgrade_level);
            map.put("sp_unit_upgrade_level", "Y");
            map.put("unit_upgrade_type", "sp");
            map.put("unit_upgrade_level", sp_unit_upgrade_level+1);
            resultMap.put("before_unit_level", sp_unit_upgrade_level);
            resultMap.put("after_unit_level", sp_unit_upgrade_level+1);
        }

        List<HashMap<String, Object>> unitUpgradeDataList = unitUpgradeMapper.unitUpgradeData(map);

        int need_user_level = (int) unitUpgradeDataList.get(0).get("user_level");
        String need_goods = String.valueOf(unitUpgradeDataList.get(0).get("need_goods"));
        int need_goods_quantity = (int) unitUpgradeDataList.get(0).get("need_goods_quantity");
        int bonus_player_skill = (int) unitUpgradeDataList.get(0).get("bonus_player_skill");

        map.put("bonus_player_skill", bonus_player_skill);
        map.put("unit_upgrade_goods_type", need_goods);
        map.put("unit_upgrade_goods_quantity", need_goods_quantity);

        //필요한 유저 레벨이 되는지 확인
        if(need_user_level > user_level){
            response.setResult("false");
            response.setMessage("플레이어 레벨이 부족합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        if(need_goods.equals("gold")){
            if(user_gold < need_goods_quantity){
                response.setResult("false");
                response.setMessage("골드가 부족합니다.");
                return new ResponseEntity<>(response, response.getStatus());
            }else{
                map.put("use_gold_quantity", need_goods_quantity); // DB에 넣기 위한 사용한 골드 값 저장
                map.put("before_use_gold_quantity", user_gold); // DB에 넣기 위한 사용 전 골드 값 저장
                map.put("after_use_gold_quantity", user_gold - need_goods_quantity); // DB에 넣기 위한 사용 후 골드 값 저장
                map.put("use_gold_unique_idx", uniqueId); // DB에 넣기 위한 사용 후 골드 값 저장(경로 고유값 필수)

                goodsMapper.insUseGoldLog(map); // 골드 사용 로그 저장

                map.put("user_gold", user_gold - need_goods_quantity); // user_data의 최종 키값 넣기

                goodsMapper.uptUserGold(map); // 사용자 골드 개수 수정

                resultMap.put("result_gold", user_gold - need_goods_quantity);

            }
        }else if(need_goods.equals("ruby")){
            if(user_ruby < need_goods_quantity){
                response.setResult("false");
                response.setMessage("루비가 부족합니다.");
                return new ResponseEntity<>(response, response.getStatus());
            }else{

                map.put("use_ruby_quantity", need_goods_quantity); // DB에 넣기 위한 사용한 루비 값 저장
                map.put("before_use_ruby_quantity", user_ruby); // DB에 넣기 위한 사용 전 루비 값 저장
                map.put("after_use_ruby_quantity", user_ruby - need_goods_quantity); // DB에 넣기 위한 사용 후 루비 값 저장
                map.put("use_ruby_unique_idx", uniqueId); // DB에 넣기 위한 사용 후 루비 값 저장(경로 고유값 필수)

                goodsMapper.insUseRubyLog(map); // 루비 사용 로그 저장

                map.put("user_ruby", user_ruby - need_goods_quantity); // user_data의 최종 키값 넣기

                goodsMapper.uptUserRuby(map); // 사용자 루비 개수 수정

                resultMap.put("result_ruby", user_ruby - need_goods_quantity);
            }
        }

        if(bonus_player_skill > 0 ){
            //플레이어 스킬 그만큼 더해주기
            map.put("plus_skill_point", bonus_player_skill);
            userMapper.uptUserepickSkillPlus(map);
        }

        //플스 더해줘서 결과값 보내주기
        resultMap.put("plus_skill_point", user_plus_skill_point + bonus_player_skill);

        //강화 업그레이드
        unitUpgradeMapper.uptUserUnitUpgrade(map);

        //강화 로그 쌓기
        unitUpgradeMapper.insUserUnitUpgradeLog(map);

        response.setResult("success");
        response.setMessage("강화 성공");

        //유저 강화값 가져오기
        resultMap.put("userUpgradeUnit", unitUpgradeMapper.userUnitUpgradeData(map));

        //결과값 맵 저장
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> userUnitUpgradeData(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse(HttpStatus.OK, unitUpgradeMapper.userUnitUpgradeData(map).get(0));

        return new ResponseEntity<>(response, response.getStatus());
    }

}