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
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class MailService {

    @Autowired
    MailMapper mailMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    GoodsMapper goodsMapper;

    @Autowired
    EpicSkillMapper epicSkillMapper;

    @Autowired
    JwtProvider jwtProvider;

    public ResponseEntity<?> selectUserMailList(Map<String, Object> map) throws Exception {

        ApiResponse response = new ApiResponse();

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        Map<String, Object> tokenMap = jwtProvider.userTokenCheck(map);

        if(tokenMap.get("result").equals("false")) {

            resultMap.put("result", "false");
            resultMap.put("resultNote", "토큰 일치 안 함(중복 로그인임)");
            response.setBody(resultMap);

            return new ResponseEntity<>(response, response.getStatus());
        }


        //전체 우편 한 번 확인

        //전체 메일을 다른 map에 넣어서 유저 개인 우편으로 만들어야함. 그걸 담아주는 map
        Map<String, Object> userMailMap;
        userMailMap = new HashMap<String, Object>();

        //우편함 있는지 없는지 일단 확인
        int allMailListSize = mailMapper.selectAllMailList().size();

        if(allMailListSize != 0) {
            List<HashMap<String, Object>> selectAllMailList = mailMapper.selectAllMailList(); //일단 우편값 다 가져옴
            for(int i = 0; i <= selectAllMailList.size()-1; i++){

                map.put("reward_unique", selectAllMailList.get(i).get("reward_unique"));
                map.put("seq", selectAllMailList.get(i).get("seq"));

                int allMailCheckSize = mailMapper.selectAllMailListCheck(map).size();

                if(allMailCheckSize == 0){
                    userMailMap.put("user_uuid", map.get("user_uuid"));
                    userMailMap.put("all_mail_unique", selectAllMailList.get(i).get("all_mail_unique"));
                    userMailMap.put("reward_unique", selectAllMailList.get(i).get("reward_unique"));
                    userMailMap.put("mail_title", selectAllMailList.get(i).get("all_mail_title"));
                    userMailMap.put("mail_content", selectAllMailList.get(i).get("all_mail_content"));
                    userMailMap.put("mail_sender", selectAllMailList.get(i).get("all_mail_sender"));
                    userMailMap.put("mail_note", selectAllMailList.get(i).get("all_mail_note"));
                    userMailMap.put("ins_ip", map.get("ins_ip"));
                    userMailMap.put("ins_machine", map.get("ins_machine"));
                    mailMapper.insUserAllMail(userMailMap);

                }
            }
        }
        //전체 우편 로직 끝


        int useMailListSize = mailMapper.selectUserMailList(map).size();

        //우편함 사이즈 확인
        if(useMailListSize == 0) {
            response.setResult("mail_list_not");
            response.setMessage("우편이 없습니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        List<HashMap<String, Object>> selectUserMailList = mailMapper.selectUserMailList(map);

        for(int i = 0; i <= selectUserMailList.size()-1; i++){
            String reward_unique = String.valueOf(selectUserMailList.get(i).get("reward_unique"));
            map.put("reward_unique", reward_unique);

            if(reward_unique == null || reward_unique.length() == 0 || reward_unique.equals("null")){
                resultMap.put("userMail_" + i, selectUserMailList.get(i));
            }else{
                Map<String, Object> rewards = new HashMap<>();
                List<HashMap<String, Object>> selectUserMailRewardList = mailMapper.selectUserMailRewardList(map);

                for(int j = 0; j < selectUserMailRewardList.size(); j++) {
                    rewards.put("reward" + j, selectUserMailRewardList.get(j));
                }
                selectUserMailList.get(i).put("rewards", rewards);
                resultMap.put("userMail_" + i, selectUserMailList.get(i));
            }
        }
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> selectUserMailRewardList(Map<String, Object> map) throws Exception {

        ApiResponse response = new ApiResponse(HttpStatus.OK, mailMapper.selectUserMailRewardList(map));

        return new ResponseEntity<>(response, response.getStatus());

    }
    @Transactional
    public ResponseEntity<?> insertUserMailReward(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        /*System.out.println("##############" + map);*/

        int use_yn_size = mailMapper.selectUserMailYnCheck(map).size();

        if(use_yn_size == 0) {
            response.setResult("mail_check_not");
            response.setMessage("받을수 없는 우편입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }else{
            String use_yn = String.valueOf(mailMapper.selectUserMailYnCheck(map).get(0).get("use_yn"));
            if (use_yn.equals("N")) {
                response.setResult("mail_check_N");
                response.setMessage("받았던 우편입니다.");
                return new ResponseEntity<>(response, response.getStatus());
            }
        }

        //0.5초 기다리기
        /*Thread.sleep(500);*/

        // 이전에 실행된 시간을 가져와서, 일정 시간이 지나지 않으면 요청을 처리하지 않도록 합니다.
        Long insertMailpreviousTime = (Long) request.getSession().getAttribute("insertMailCurrentTime");
        Long insertMailCurrentTime = System.currentTimeMillis();
        if (insertMailpreviousTime != null && insertMailCurrentTime - insertMailpreviousTime < TimeUnit.SECONDS.toMillis(1)) {
            response.setResult("false");
            return new ResponseEntity<>(response, response.getStatus());
        }
        request.getSession().setAttribute("insertMailCurrentTime", insertMailCurrentTime);

        map.put("ins_ip", CommonUtil.getRemoteIp(request));
        List<HashMap<String, Object>> selectAllMailList = mailMapper.selectUserMailRewardList(map); //이 우편 보상 리스트 가져오기

        String reward_unique = String.valueOf(map.get("reward_unique"));

        /*System.out.println("$$$$$$$$$$$$$$$$" + reward_unique);*/

        //보상 없으면 걍 삭제
        if(reward_unique.equals("empty")){
            //메일 읽은걸로 처리
            mailMapper.uptUserMailRewardUseYn(map);

            response.setResult("delete_mail");
            response.setMessage("받을 보상이 없어 삭제완료");

            return new ResponseEntity<>(response, response.getStatus());
        }

        //결과값 클라에 줘야함
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());
        uniqueId =  reward_unique + "_mail_reward_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);

        //유저 데이터값 가져오기
        List<HashMap<String, Object>> userResultList = userMapper.selectUserData(map);
        int user_gold = (int) userResultList.get(0).get("user_gold");
        int user_ruby = (int) userResultList.get(0).get("user_ruby");
        int user_key = (int) userResultList.get(0).get("user_key");

        for(int i = 0; i <= selectAllMailList.size() -1; i++){
            String reward_kinds = String.valueOf(selectAllMailList.get(i).get("reward_kinds"));
            if(reward_kinds.equals("ruby")){

                //루비 충전값 계산
                int ruby_count = (int) selectAllMailList.get(i).get("reward_count");

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

                //골드 충전값 계산
                int gold_count = (int) selectAllMailList.get(i).get("reward_count");
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

                //키 충전값 계산
                int key_count = (int) selectAllMailList.get(i).get("reward_count");
                map.put("charge_key_unique_idx", uniqueId); // 유니크값 들어간 이유
                map.put("charge_key_quantity", key_count); // DB에 넣기 위한 충전 키 값 저장
                map.put("before_charge_key_quantity", user_key); // DB에 넣기 위한 충전 전 키 값 저장
                map.put("after_charge_key_quantity", user_key + key_count); // DB에 넣기 위한 충전 후 키 값 저장
                goodsMapper.insChargeKeyLog(map); // 키 충전 로그 저장

                map.put("user_key", user_key + key_count); // user_data의 최종 키값 넣기

                resultMap.put("user_key", user_key + key_count);//결과 클라에 주기

                //유저키업데이트
                goodsMapper.uptUserKey(map);

            }else if(reward_kinds.equals("epic_skill")){
                //에픽 스킬 인설트 해주기
                map.put("user_epic_skill_unique_idx", uniqueId); // 유니크값 들어간 이유
                int reward_item_idx = (int) selectAllMailList.get(i).get("reward_item_idx");
                int reward_item_level = (int) selectAllMailList.get(i).get("reward_item_level");
                int reward_item_grade = (int) selectAllMailList.get(i).get("reward_item_grade");
                map.put("user_epic_skill_idx", reward_item_idx); // DB에 넣기 위한 아이템 SEQ값(쭝요)
                map.put("user_epic_skill_level", reward_item_level); // DB에 넣기 위한 아이템 레벨 값
                map.put("user_epic_skill_grade", reward_item_grade); // DB에 넣기 위한 아이템 등급 값
/*

                resultMap.put("user_epic_skill_idx", reward_item_idx);//결과 클라에 주기
                resultMap.put("user_epic_skill_level", reward_item_level);//결과 클라에 주기
                resultMap.put("user_epic_skill_grade", reward_item_grade);//결과 클라에 주기
*/

                epicSkillMapper.insUserEpicSkill(map);

            }
        }

        //메일 읽은걸로 처리
        mailMapper.uptUserMailRewardUseYn(map);

        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

}