package com.goall.zombie_defence.service;

import com.goall.zombie_defence.config.JwtProvider;
import com.goall.zombie_defence.mapper.*;
import com.goall.zombie_defence.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

import com.goall.commonUtil.CommonUtil;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    PlayerSkillMapper playerSkillMapper;

    @Autowired
    MailMapper mailMapper;

    @Autowired
    InGameMapper inGameMapper;

    @Autowired
    StatisticsMapper statisticsMapper;

    @Autowired
    CommonMapper commonMapper;

    @Autowired
    UnitUpgradeMapper unitUpgradeMapper;

    @Autowired
    SeasonPackageMapper seasonPackageMapper;

    public ResponseEntity<?> userUuidCheck(Map<String, Object> map) throws Exception {

        List<HashMap<String, Object>> map2 = userMapper.userUuidCheck(map);

        String uuidCheck = String.valueOf(map2.get(0).get("uuidCheck"));

        if(Objects.equals(uuidCheck, "0")){
            map.put("uuidResult", 502); // uuid 중복 안뎀!
        }else if(Objects.equals(uuidCheck, "1")){
            map.put("uuidResult", 500); // uuid 중복!
        }else{
            map.put("uuidResult", 404);
        }

        ApiResponse response = new ApiResponse(HttpStatus.OK, map);

        return new ResponseEntity<>(response, response.getStatus());

    }
/*    public ResponseEntity<?> userNickNameCheck(Map<String, Object> map) throws Exception {

        List<HashMap<String, Object>> map2 = userMapper.userNickNameCheck(map);

        String nickNameCheck = String.valueOf(map2.get(0).get("nickNameCheck"));

        if(Objects.equals(nickNameCheck, "0")){
            map.put("nickNamedResult", 500); //닉네임 중복 안뎀
        }else if(Objects.equals(nickNameCheck, "1")){
            map.put("nickNamedResult", 502); //닉네임 중복!
        }else{
            map.put("nickNamedResult", 404);
        }

        ApiResponse response = new ApiResponse(HttpStatus.OK, map);

        return new ResponseEntity<>(response, response.getStatus());

    }*/

    public ResponseEntity<?> userNickNameCheck(Map<String, Object> map) throws Exception {

        //user_uuid 체크
        List<HashMap<String, Object>> map3 = userMapper.userUuidCheck(map);
        String uuidCheck = String.valueOf(map3.get(0).get("uuidCheck"));

        //닉네임 체크
        List<HashMap<String, Object>> map2 = userMapper.userNickNameCheck(map);
        String nickNameCheck = String.valueOf(map2.get(0).get("nickNameCheck"));

        if(Objects.equals(uuidCheck, "0") && Objects.equals(nickNameCheck, "0")){
            map.put("uuid_nickName_result", 100); // uuid 중복 안뎀!
            map.put("memo", "닉네임, user_uuid 모두 중복 안 되어 회원 가입 가능");
        }else if(Objects.equals(uuidCheck, "1")){
            map.put("uuid_nickName_result", 200); // uuid 중복!
            map.put("memo", "user_uuid 중봅됩니다.");
        }else if(Objects.equals(nickNameCheck, "1")){
            map.put("uuid_nickName_result", 300); //닉네임 중복!
            map.put("memo", "닉네임 중복 됩니다.");
        }

        ApiResponse response = new ApiResponse(HttpStatus.OK, map);

        return new ResponseEntity<>(response, response.getStatus());

    }

    public void insUserData(Map<String, Object> map, HttpServletRequest request) throws Exception {

        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        String platform = String.valueOf(map.get("platform"));

        if(!platform.equals("ios")){
         map.put("platform", "aos");
         //안드쪽에서는 이거 안보내주고있어서 일단 급하게 수정
         map.put("user_position", "normal");
        }

        //유닛 개발 업그레이드 초기 데이터
        unitUpgradeMapper.insUserUnitUpgrade(map);

        //광고 시간 초기 데이터값 넣기
        commonMapper.insAdvertisementFirstTimeData(map);

        userMapper.insUserData(map);
    }

    public ResponseEntity<?> selectUserData(Map<String, Object> map, HttpServletRequest request) throws Exception {

        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        int playerSkillYn =  Integer.parseInt(String.valueOf(playerSkillMapper.playerSkillYn(map).get(0).get("player_skill_yn")));

        //플레이어 스킬이 없다면 기본값으로 인설트 해준다.
        if(playerSkillYn == 0){
            //최초 등록
            playerSkillMapper.insUserPlayerSkill(map);
        }

        //유닛 업그레이드 최초로 하는거면 넣어줘야함
        int unitUpgradeYn =  Integer.parseInt(String.valueOf(unitUpgradeMapper.userUnitUpgradeYn(map).get(0).get("user_unit_upgrade_yn")));

        //유닛업그레이드데이터있는지확인
        if(unitUpgradeYn == 0){
            //최초 등록
            unitUpgradeMapper.insUserUnitUpgrade(map);
        }

        String token = jwtProvider.createToken(String.valueOf(map.get("user_uuid")));
        map.put("user_token", token);

        userMapper.uptUserToken(map);
        //토큰 값 수정 끝

        //전체 우편 확인 후 안 받은게 있다면 유저 메일에 넣어주기

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

        //현 시즌 데이터 체크
        // 날짜 형식을 정의 (예: "yyyy-MM-dd HH:mm:ss")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        List<HashMap<String, Object>> selectSeasonNumberList = seasonPackageMapper.selectSeasonNumberList(map);
        int season_number = (int) selectSeasonNumberList.get(0).get("season_number");
        Date season_start_date = formatter.parse(String.valueOf(selectSeasonNumberList.get(0).get("season_start_date")));
        Date season_end_date = formatter.parse(String.valueOf(selectSeasonNumberList.get(0).get("season_end_date")));
        map.put("season_number", season_number);
        map.put("season_start_date", season_start_date);
        map.put("season_end_date", season_end_date);


        //시즌 경험치 데이터 확인 후 없으면 디폴트값 인설트
        int searchUserSeasonPackage =  seasonPackageMapper.searchUserSeasonPackage(map);

        //시즌값이 없다면 기본값으로 인설트 해준다.
        if(searchUserSeasonPackage == 0){
            //최초 등록
            seasonPackageMapper.insUserSeasonPackage(map);
        }

        ApiResponse response = new ApiResponse(HttpStatus.OK, userMapper.selectUserData(map));

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> insUserLoginLog(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();
        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        userMapper.insUserLoginLog(map);
        userMapper.uptUserlastLogin(map);

        //서버 접속 가능 여부//
        String game_connect_yn = String.valueOf(commonMapper.nowDataTimeAndGameVersion(map).get(0).get("game_connect_yn"));
        //서버 접속 가능 여부//
        String use_yn = String.valueOf(userMapper.selectUserData(map).get(0).get("use_yn")); // 회원 접속 YN
        String user_position = String.valueOf(userMapper.selectUserData(map).get(0).get("user_position")); // 회원 등급
        if(game_connect_yn.equals("N")){

            if(user_position.equals("admin")){
                map.put("connectYn", "Y");
                map.put("connectResult", "관리자 계정으로 접속하셨습니다.");
                map.put("memo", "버전 테이블 접속은 N값이지만 관리자(admin)라 접속 가능함");
            }else{
                map.put("connectYn", "N");
                map.put("connectResult", "점검중입니다. 공식 카페를 확인해주세요.");
                map.put("memo", "버전 테이블 접속 값 N으로 되어있음");
            }

        }else{

            if(use_yn.equals("N")){
                map.put("connectYn", "N");
                map.put("connectResult", "정지된 이용자입니다. 문의는 이메일이나 공식 카페에 문의해주세요.");
                map.put("memo", "버전 테이블 접속은 Y이지만 회원의 사용 가능 여부가 N이다.");
            }else{
                map.put("connectYn", "Y");
                map.put("connectResult", "좀운디에 오신걸 환영합니다.");
                map.put("memo", "버전 테이블 접속은 Y이고 회원 사용 가능 여부도 Y 여서 접속 완료");
            }
        }

        response.setBody(map);

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> selectCommonUserData(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        Map<String, Object> userStageRankMap;
        userStageRankMap = new HashMap<String, Object>();

        Map<String, Object> userStageRankMap2;
        userStageRankMap2 = new HashMap<String, Object>();

        Map<String, Object> userBossStageCountMap;
        userBossStageCountMap = new HashMap<String, Object>();

        Map<String, Object> userBossStageCountMap2;
        userBossStageCountMap2 = new HashMap<String, Object>();

        Map<String, Object> userStageRankTotalMap;
        userStageRankTotalMap = new HashMap<String, Object>();

        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

        //유저 스테이지 별 랭킹 가장 높은 정보 가져오기
        List<HashMap<String, Object>> stageAllList = inGameMapper.stageAllList(map);

        for(int i = 0; i <= stageAllList.size()-1; i++){
            String game_mode = String.valueOf(stageAllList.get(i).get("game_mode"));
            String game_level = String.valueOf(stageAllList.get(i).get("game_level"));
            map.put("game_mode", game_mode);
            map.put("game_level", game_level);

            List<HashMap<String, Object>> result = statisticsMapper.myTotalGameRanking(map);
            if(result != null && result.size()>0 && result.get(0)!=null && result.get(0).size() != 0 ) {
                userStageRankMap.put("stage_"+i, result.get(0));
            }else{
                userStageRankMap.put("stage_"+i, 0);
            }
            userStageRankMap2.put("stage_rank_data", userStageRankMap);
        }

        resultList.add(userStageRankMap2);

        //보스 스테이지 몇회 들어갔는지 확인하기
        List<HashMap<String, Object>> stageBossList = inGameMapper.stageBossList(map);

        for(int i = 0; i <= stageBossList.size()-1; i++){
            String game_mode = String.valueOf(stageBossList.get(i).get("game_mode"));
            String game_level = String.valueOf(stageBossList.get(i).get("game_level"));
            map.put("game_mode", game_mode);
            map.put("game_level", game_level);

            List<HashMap<String, Object>> result = inGameMapper.stageBossCountList(map);
            if(result != null && result.size()>0 && result.get(0)!=null && result.get(0).size() != 0 ) {
                userBossStageCountMap.put("boss_stage_"+i, result.get(0));
            }else{
                userBossStageCountMap.put("boss_stage_"+i, 0);
            }
            userBossStageCountMap2.put("boss_stage_count", userBossStageCountMap);
        }

        resultList.add(userBossStageCountMap2);

        //친구 추천 했는지 보여주기
        Map<String, Object> userRecommendationsMap;
        userRecommendationsMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> fromMap = userMapper.selectUserRecommendationsFrom(map);
        String from_user_uuid_check = String.valueOf(fromMap.get(0).get("from_user_uuid"));

        //N은 추천 한적 없음, Y는 추천 한적 있음
        if (Objects.equals(from_user_uuid_check, "0")) {
            userRecommendationsMap.put("userRecommendationsYn", "N");
        }else{
            userRecommendationsMap.put("userRecommendationsYn", "Y");
        }

        resultList.add(userRecommendationsMap);

        //유저 개별 강화 가져오기
        Map<String, Object> userUnitUpgrade;
        userUnitUpgrade = new HashMap<String, Object>();

        userUnitUpgrade.put("userUnitUpgrade", unitUpgradeMapper.userUnitUpgradeData(map).get(0));

        resultList.add(userUnitUpgrade);
        
        //이번주 보스 최고점수 가져오기
        Map<String, Object> stageBossHighScoreMap;
        stageBossHighScoreMap = new HashMap<String, Object>();

        Map<String, Object> stageBossHighScoreMap2;
        stageBossHighScoreMap2 = new HashMap<String, Object>();

        List<HashMap<String, Object>> stageBossHighScoreList = inGameMapper.stageBossList(map);

        for(int i = 0; i <= stageBossHighScoreList.size()-1; i++){
            String game_mode = String.valueOf(stageBossHighScoreList.get(i).get("game_mode"));
            String game_level = String.valueOf(stageBossHighScoreList.get(i).get("game_level"));
            map.put("game_mode", game_mode);
            map.put("game_level", game_level);

            List<HashMap<String, Object>> result = statisticsMapper.bossStageHighScoreList(map);
            if(result != null && result.size()>0 && result.get(0)!=null && result.get(0).size() != 0 ) {
                stageBossHighScoreMap.put("boss_stage_"+i, result.get(0));
            }else{
                stageBossHighScoreMap.put("boss_stage_"+i, 0);
            }
            stageBossHighScoreMap2.put("boss_stage_rank_data", stageBossHighScoreMap);
        }

        resultList.add(stageBossHighScoreMap2);

        response.setBody(resultList);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> insUserRecommendations(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();
        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        //유저 재화 정보 가져오기
        List<HashMap<String, Object>> userResultMap = userMapper.selectUserData(map);
        String from_user_nickname = String.valueOf(userResultMap.get(0).get("user_nickname")); // 로그인한 닉네임

        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        uniqueId = "user_recommendations" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);

        //서버에 추천할 닉네임이 있는지 확인
        List<HashMap<String, Object>> map2 = userMapper.userNickNameCheck(map);

        String nickNameCheck = String.valueOf(map2.get(0).get("nickNameCheck"));
        String to_user_uuid = String.valueOf(map2.get(0).get("user_uuid"));
        map.put("to_user_uuid", to_user_uuid);

        //본인이 본인 추천은 불가능(대소문자 포함 equalsIgnoreCase)
        if(!from_user_nickname.equalsIgnoreCase(String.valueOf(map.get("user_nickname")))) {

            //닉네임이 있는지 체크
            if (Objects.equals(nickNameCheck, "1")) {

                //닉네임이 있다면 진행
                List<HashMap<String, Object>> fromMap = userMapper.selectUserRecommendationsFrom(map);
                String from_user_uuid_check = String.valueOf(fromMap.get(0).get("from_user_uuid"));

                //추천하는 사람이 추천을 한번이라도 했는지 안 했는치 체크
                if (Objects.equals(from_user_uuid_check, "0")) {

                    //추천 한 적이 없다면 진행(0)
                    List<HashMap<String, Object>> toMap = userMapper.selectUserRecommendationsTo(map);
                    int to_user_uuid_check = Integer.parseInt(String.valueOf(toMap.get(0).get("to_user_uuid")));

                    //받는 사람 추천수가 10 미만일때는 추천 가능
                    if (to_user_uuid_check < 10) {
                        //추천 테이블에 저장
                        userMapper.insUserRecommendations(map);
                        //추천 한 사람 루비 우편 받기 로직
                        Map<String, Object> fromInsMail;
                        fromInsMail = new HashMap<String, Object>();

                        fromInsMail.put("user_uuid", map.get("user_uuid"));
                        fromInsMail.put("all_mail_unique", uniqueId);
                        fromInsMail.put("reward_unique", "user_recommendations");
                        fromInsMail.put("mail_title", "친구 추천 보상");
                        fromInsMail.put("mail_content", map.get("user_nickname") + "님을 추천 하셔서 받는 보상입니다.");
                        fromInsMail.put("mail_sender", "운영진");
                        fromInsMail.put("mail_note", "친구 추천을 하여 받는 보상우편");
                        mailMapper.insUserAllMail(fromInsMail);

                        //추천 받는 사람 우편 넣기

                        Map<String, Object> toInsMail;
                        toInsMail = new HashMap<String, Object>();

                        toInsMail.put("user_uuid", to_user_uuid);
                        toInsMail.put("all_mail_unique", uniqueId);
                        toInsMail.put("reward_unique", "user_recommendations");
                        toInsMail.put("mail_title", "친구 추천을 받으셨습니다.");
                        toInsMail.put("mail_content", from_user_nickname + "님이 친구 추천을 해주셔서 받는 보상입니다.");
                        toInsMail.put("mail_sender", "운영진");
                        toInsMail.put("mail_note", "친구 추천을 받아 받는 보상우편");

                        mailMapper.insUserAllMail(toInsMail);

                        map.put("RecommResult", "친구 추천 성공");

                    } else {
                        //나머지는 실패
                        map.put("RecommResult", map.get("user_nickname") + "님은(는) 이미 10번 추천 받았습니다.");
                    }


                } else if (Objects.equals(from_user_uuid_check, "1")) {
                    map.put("RecommResult", "이미 추천을 하셨습니다.");
                } else {
                    map.put("RecommResult", 404);
                }

            } else if (Objects.equals(nickNameCheck, "0")) {
                map.put("RecommResult", "그 닉네임은 존재하지 않습니다.");
            } else {
                map.put("RecommResult", 404);
            }
        }else{
            map.put("RecommResult", "본인은 추천할수 없습니다.");
        }

        response.setBody(map);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<ApiResponse> insUserLink(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        //연동 시작
        userMapper.uptLinkUserAdvertisementLog(map);
        userMapper.uptLinkUserAdvertisementTimeData(map);
        userMapper.uptLinkUserBuyGoodsLog(map);
        userMapper.uptLinkUserChargeGoldLog(map);
        userMapper.uptLinkUserChargeKeyLog(map);
        userMapper.uptLinkUserChargeRubyLog(map);
        userMapper.uptLinkUserEpicSkillChangeLog(map);
        userMapper.uptLinkUserEpicSkillSellLog(map);
        userMapper.uptLinkUserepicSkillSynthesisLog(map);
        userMapper.uptLinkUserEpicSkillUpgradeLog(map);
        userMapper.uptLinkUserExpBoosterData(map);
        userMapper.uptLinkUserGameRankRewardLog(map);
        userMapper.uptLinkUserGameResultData(map);
        userMapper.uptLinkUserGameResultDataHack(map);
        userMapper.uptLinkUserGameStartData(map);
        userMapper.uptLinkUserProductSaleLog(map);
        userMapper.uptLinkUserQuestLog(map);
        userMapper.uptLinkUserUesrLevelLog(map);
        userMapper.uptLinkUserUserData(map);
        userMapper.uptLinkUserUserDrone(map);
        userMapper.uptLinkUserUserEpicSkillData(map);
        userMapper.uptLinkUserUserLoginLog(map);
        userMapper.uptLinkUserUserMailData(map);
        userMapper.uptLinkUserUserPlayerSkillData(map);
        userMapper.uptLinkUserUseCashLog(map);
        userMapper.uptLinkUserUseGoldLog(map);
        userMapper.uptLinkUserUseGoodsLog(map);
        userMapper.uptLinkUserUseKeyLog(map);
        userMapper.uptLinkUserUseRubyLog(map);
        userMapper.uptLinkUserWeekRankData(map);

        //유닛업그레이드 강화 추가쪽
        userMapper.uptUnitUpgradeLogData(map);
        userMapper.uptUserUnitUpgradeData(map);

        //연동 링크 로그 쌀기
        userMapper.insUserLinkLog(map);

        //보스포인트 업데이트 230928
        userMapper.uptChargeBpLogData(map);
        userMapper.uptUseBpLogData(map);

        //랭킹 시간 오래걸리는거 개선 240407
        userMapper.uptGameUserMaxData(map);

        //240428 업데이트
        userMapper.uptUserRecommendationsData(map);
        userMapper.uptUserSeasonPackageExpData(map);
        userMapper.uptUserSeasonPackageInsertData(map);
        userMapper.uptGameWeekResultData(map);

        map.put("result", "true");

        response.setBody(map);

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> adminPageNicknameSearch(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        List<HashMap<String, Object>> userResultMap = userMapper.adminPageNicknameSearch(map);

        if(userResultMap != null && userResultMap.size()>0 && userResultMap.get(0)!=null && userResultMap.get(0).size() != 0 ) {
            String user_uuid = (String) userResultMap.get(0).get("user_uuid");

            map.put("nickNameSearch", userResultMap);
            map.put("user_uuid", user_uuid);
            map.put("cafeEventYN", userMapper.adminPageCafeEventYN(map));
        }else{
            map.put("nickNameSearch", userResultMap);
        }

        response.setBody(map);

        return new ResponseEntity<>(response, response.getStatus());

    }

    public void adminPageInsCafeEvent(Map<String, Object> map, HttpServletRequest request) throws Exception {

        userMapper.insCafeEvent(map);
    }

}