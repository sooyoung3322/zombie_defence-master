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
public class InGameService {

    @Autowired
    InGameMapper inGameMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    GoodsMapper goodsMapper;
    @Autowired
    JwtProvider jwtProvider;
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    ProductMapper productMapper;
    @Autowired
    StatisticsMapper statisticsMapper;
    @Autowired
    GoodsService goodsService;
    @Autowired
    SeasonPackageMapper seasonPackageMapper;
    @Autowired
    QuestMapper questMapper;

    @Transactional
    public ResponseEntity<?> insGameStartLog(Map<String, Object> map, HttpServletRequest request) throws Exception {

        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        ApiResponse response = new ApiResponse();

        //0.5초 기다리기
        /*Thread.sleep(700);*/

        // 이전에 실행된 시간을 가져와서, 일정 시간이 지나지 않으면 요청을 처리하지 않도록 합니다.
        Long previousTime = (Long) request.getSession().getAttribute("keyPreviousTime");
        Long keyCurrentTime = System.currentTimeMillis();
        if (previousTime != null && keyCurrentTime - previousTime < TimeUnit.SECONDS.toMillis(3)) {
            /*resultMap.put("result_key", user_key);
            response.setBody(resultMap);*/
            response.setResult("false");
            return new ResponseEntity<>(response, response.getStatus());
        }
        request.getSession().setAttribute("keyPreviousTime", keyCurrentTime);

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        Map<String, Object> tokenMap = jwtProvider.userTokenCheck(map);

        if (tokenMap.get("result").equals("false")) {

            resultMap.put("result", "false");
            resultMap.put("resultNote", "토큰 일치 안 함(중복 로그인임)");
            response.setBody(resultMap);

            return new ResponseEntity<>(response, response.getStatus());
        }

        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        uniqueId = "boss_plus_ins_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);

        userMapper.uptUserGameCount(map);//유저 테이블의 게임 카운트 +1 해주기


        //인게임 웨이브 가져오기
        resultMap.put("wave_list", inGameMapper.selectWaveList(map));

        int use_key_count = 0;//사용키값
        int use_ruby_count = 0;//사용키값(추가입장 루비)
        String skip_mode_yn = String.valueOf(map.get("skip_mode_yn"));
        String key_or_ruby = "";//어떤 재화 사용하냐

        if (map.get("game_mode").equals("easy")) {
            if(skip_mode_yn.equals("Y")){
                use_key_count = -30;
            }else{
                use_key_count = -10;
            }
            key_or_ruby = "key";
        } else if (map.get("game_mode").equals("boss")) {
            int bossDayCount;
            List<HashMap<String, Object>> stageBossCountResult = inGameMapper.stageBossCountList(map);
            //보스 포인트 몇개 주는지(입장하면 바로 줌)
            List<HashMap<String, Object>> bossStageList = statisticsMapper.stageList(map);
            int boss_point_reward = Integer.parseInt(String.valueOf(bossStageList.get(0).get("boss_point_reward")));
            bossDayCount = Integer.parseInt(String.valueOf(stageBossCountResult.get(0).get("boss_day_count")));
           if(bossDayCount == 0){
               use_key_count = -20;
               key_or_ruby = "key";
           } else if (bossDayCount == 1) {
                use_ruby_count = 200;//추입량 200루비
                key_or_ruby = "ruby";
            } else if (bossDayCount >= 2) {
                response.setResult("day_in_2_fail");
                response.setMessage("오늘 보스전 입장 두번 끝났습니다.");
                return new ResponseEntity<>(response, response.getStatus());
            }

            /*################보스 포인트 충전 시작###################*/

            int user_bp = (int) userMapper.selectUserData(map).get(0).get("user_bp"); // 사용자 보스포인트 개수 가져오기

            //충전이 된 이유
            map.put("charge_reason", "보스전게임시작보상");

            //보스포인트 충전때 필요한 유니크값 저장
            map.put("charge_bp_unique_idx", uniqueId);

            //보스포인트 충전값 계산
            map.put("charge_bp_quantity", boss_point_reward); // DB에 넣기 위한 게임 결과 보스포인트
            map.put("before_charge_bp_quantity", user_bp); // DB에 넣기 위한 충전 전 보스포인트값 저장
            map.put("after_charge_bp_quantity", user_bp + boss_point_reward); // DB에 넣기 위한 충전 후 보스포인트 값 저장
            goodsMapper.insChargeBpLog(map); // 보스포인트 충전 로그 저장

            map.put("user_bp", user_bp + boss_point_reward); // user_data의 최종 보스포인트 값 넣기

            //보스포인트 결과값 클라에 뿌려주기위해
            resultMap.put("result_bp", user_bp + boss_point_reward);

            //유저 보스포인트 업데이트
            goodsMapper.uptUserBp(map);

        }

        if (key_or_ruby.equals("key")) {

            int user_key = (int) userMapper.selectUserData(map).get(0).get("user_key"); // 사용자 키 개수 가져오기

            if (user_key + use_key_count < 0) {
                response.setResult("fail_key");
                response.setMessage("열쇠가 부족합니다.");
                return new ResponseEntity<>(response, response.getStatus());
            }

            inGameMapper.insGameStartLog(map);

            int start_seq;

            start_seq = Integer.parseInt(String.valueOf(map.get("start_seq")));
            response.setAutoSeq(start_seq);

            //열쇠 소모 시키기
            map.put("start_seq", start_seq);
            map.put("charge_reason", map.get("game_mode") + "입장");

            map.put("use_key_quantity", use_key_count); // DB에 넣기 위한 사용한 키 값 저장
            map.put("before_use_key_quantity", user_key); // DB에 넣기 위한 사용 전 키 값 저장
            map.put("after_use_key_quantity", user_key + use_key_count); // DB에 넣기 위한 사용 후 키 값 저장
            goodsMapper.insUseKeyLog(map); // 키 사용 로그 저장

            map.put("user_key", user_key + use_key_count); // user_data의 최종 키값 넣기

            resultMap.put("result_key", user_key + use_key_count);

            goodsMapper.uptUserKey(map);

            response.setBody(resultMap);

            return new ResponseEntity<>(response, response.getStatus());

        } else {

            List<HashMap<String, Object>> userResultMap = userMapper.selectUserData(map);
            int user_ruby = (int) userResultMap.get(0).get("user_ruby"); // 사용자 루비 개수 가져오기
            int user_key = (int) userResultMap.get(0).get("user_key"); // 사용자 골드 개수 가져오기

            if (user_ruby - use_ruby_count < 0) {
                response.setResult("fail_ruby");
                response.setMessage("루비가 부족합니다.");
                return new ResponseEntity<>(response, response.getStatus());
            }

            inGameMapper.insGameStartLog(map);

            int start_seq;

            start_seq = Integer.parseInt(String.valueOf(map.get("start_seq")));
            response.setAutoSeq(start_seq);

            map.put("use_ruby_quantity", use_ruby_count); // DB에 넣기 위한 사용한 루비 값 저장
            map.put("before_use_ruby_quantity", user_ruby); // DB에 넣기 위한 사용 전 루비 값 저장
            map.put("after_use_ruby_quantity", user_ruby - use_ruby_count); // DB에 넣기 위한 사용 후 루비 값 저장
            map.put("use_ruby_unique_idx", uniqueId); // DB에 넣기 위한 사용 후 루비 값 저장(경로 고유값 필수)
            map.put("use_ruby_log_note", "보스 추가 입장"); // DB에 넣기 위한 사용 후 루비 값 저장(경로 고유값 필수)
            
            map.put("advertisement_yn", "N");//광고아님

            goodsMapper.insUseRubyLog(map); // 루비 사용 로그 저장

            map.put("user_ruby", user_ruby - use_ruby_count); // user_data의 최종 키값 넣기

            goodsMapper.uptUserRuby(map); // 사용자 루비 개수 수정

            resultMap.put("result_ruby", user_ruby - use_ruby_count);
            resultMap.put("result_key", user_key);

            response.setBody(resultMap);

            return new ResponseEntity<>(response, response.getStatus());
        }
    }

    @Transactional
    public ResponseEntity<?> insGameResultLog(Map<String, Object> map, HttpServletRequest request) throws Exception {


        /*System.out.println(map);*/
        String game_result = String.valueOf(map.get("game_result"));

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        Map<String, Object> levelResultMap;
        levelResultMap = new HashMap<String, Object>();

        Map<String, Object> tokenMap = jwtProvider.userTokenCheck(map);

        if(tokenMap.get("result").equals("false")) {

            resultMap.put("result", "false");
            resultMap.put("resultNote", "토큰 일치 안 함(중복 로그인임)");
            response.setBody(resultMap);

            return new ResponseEntity<>(response, response.getStatus());

        }
/*

        //start_seq값은 하나만 있어야한다. 중복값이 들어오고있는 버그가 있어서 추가(일단 DB 유니크값으로 수정함)
        List<HashMap<String, Object>> selectGameResultDataStartSeq = inGameMapper.selectGameResultDataStartSeq(map);

        if (selectGameResultDataStartSeq != null && selectGameResultDataStartSeq.size() != 0) {
            resultMap.put("result", "false");
            resultMap.put("resultNote", "이미 결과값이 저장된 데이터입니다.");
            response.setBody(resultMap);
            return new ResponseEntity<>(response, response.getStatus());
        }
*/

        String game_mode = String.valueOf(map.get("game_mode"));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //유저 데이터값 가져오기
        List<HashMap<String, Object>> userResultList = userMapper.selectUserData(map);
        int user_gold = (int) userResultList.get(0).get("user_gold");
        int user_ruby = (int) userResultList.get(0).get("user_ruby");

        //유니크값 지정
        uniqueId = "game_result_" + game_result + "_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);

        if(game_mode.equals("easy")) {

            map.put("user_ruby", user_ruby);

            //게임에서 드랍되어진 재화 값
            int drop_gold_result;
            int drop_ruby_result;

            //포기하면 뭣도없어!!!
            if (game_result.equals("Retire")) {
                //게임에서 드랍되어진 재화 값
                drop_gold_result = 0;
                drop_ruby_result = 0;
                //충전이 된 이유
                map.put("charge_reason", "중도포기로인한보상없음");
            } else {
                //게임에서 드랍되어진 재화 값
                drop_gold_result = (int) map.get("drop_gold");
                drop_ruby_result = (int) map.get("drop_ruby");

                //충전이 된 이유
                map.put("charge_reason", "게임결과에대한보상");
            }

            //루비 충전때 필요한 유니크값 저장
            map.put("charge_gold_unique_idx", uniqueId);
            map.put("charge_ruby_unique_idx", uniqueId);

            //골드 충전값 계산
            map.put("charge_gold_quantity", drop_gold_result); // DB에 넣기 위한 게임 결과 골드
            map.put("before_charge_gold_quantity", user_gold); // DB에 넣기 위한 충전 전 골드값 저장
            map.put("after_charge_gold_quantity", user_gold + drop_gold_result); // DB에 넣기 위한 충전 후 골드 값 저장
            goodsMapper.insChargeGoldLog(map); // 골드 충전 로그 저장

            map.put("user_gold", user_gold + drop_gold_result); // user_data의 최종 골드 값 넣기

            //키 결과값 클라에 뿌려주기위해
            resultMap.put("result_gold", user_gold + drop_gold_result);

            //유저 골드 업데이트
            goodsMapper.uptUserGold(map);

            /*################루비 충전 시작###################*/

            //루비 충전값 계산
            map.put("charge_ruby_quantity", drop_ruby_result); // DB에 넣기 위한 게임 결과 루비
            map.put("before_charge_ruby_quantity", user_ruby); // DB에 넣기 위한 충전 전 루비값 저장
            map.put("after_charge_ruby_quantity", user_ruby + drop_ruby_result); // DB에 넣기 위한 충전 후 루비 값 저장
            goodsMapper.insChargeRubyLog(map); // 루비 충전 로그 저장

            int level_up_reward_ruby = 0;

            //경험치 계산 시작//
            if (!map.get("game_result").equals("Retire")) {
                levelResultMap = experienceLevelCalc(map);
                level_up_reward_ruby = (int) levelResultMap.get("level_up_reward_ruby");
                resultMap.put("level_up_result", levelResultMap);
            }

            map.put("user_ruby", user_ruby + drop_ruby_result + level_up_reward_ruby); // user_data의 최종 루비 값 넣기

            //루비 결과값 클라에 뿌려주기위해
            resultMap.put("result_ruby", user_ruby + drop_ruby_result + level_up_reward_ruby);

            //유저 루비 업데이트
            goodsMapper.uptUserRuby(map);

        }

        //시즌 경험치 쌓기
        if(map.get("game_mode").equals("easy")) {
            if(map.get("game_result").equals("Clear") || map.get("game_result").equals("Failed")) {
                //시즌 경험치 충전
                //얻은 경험치 량
                List<HashMap<String, Object>> selectGameModeList = inGameMapper.selectGameModeList(map);
                float exp_bonus = (float) selectGameModeList.get(0).get("exp_bonus");
                int experienceQuantity = Math.round((((int) map.get("game_wave") - 1) * exp_bonus));
                map.put("season_exp", experienceQuantity);
                //시즌 정보 가져오기
                List<HashMap<String, Object>> selectSeasonNumberList = seasonPackageMapper.selectSeasonNumberList(map);
                //그래서 몇 시즌임?
                int season_number = (int) selectSeasonNumberList.get(0).get("season_number");
                map.put("season_number", season_number);
                seasonPackageMapper.uptUserSeasonPackage(map);

            }
        }

        //이게 최종 결과값 서비스
        inGameMapper.insGameResultLog(map);

        //시즌패스 경험치 계산해주기
        resultMap.put("result_seq", map.get("result_seq"));

        //주간디비도 넣어준다.
        inGameMapper.insGameUserWeekResultLog(map);

        //결과값이 본인의 최고기록이면 "game_user_result_data"여기에 넣는다(240407)
        //일단 본 게임의 게임 레벨, 게임 모드 점수 가져와서 비교해야함

        List<HashMap<String, Object>> selectGameUserResultLog = inGameMapper.selectGameUserResultLog(map);

        if(selectGameUserResultLog != null && selectGameUserResultLog.size()>0 && selectGameUserResultLog.get(0)!=null && selectGameUserResultLog.get(0).size() != 0 ) {
            //최고 점수가 있다면
            float user_best_game_result = (float) selectGameUserResultLog.get(0).get("result_score");
            Object resultScoreObj = map.get("result_score");
            float now_game_result = ((Number) resultScoreObj).floatValue();
            //만약 최고기록을 달성했다면 최고기록을 update 처줘야함
            if(now_game_result > user_best_game_result){
                inGameMapper.uptGameUserResultLog(map);
            }else{
                //최고기록 달성 못하면 인설트,업데이트 모두 없음
            }
        }else{
            //만약 처음 하는 모드고 레벨이면 인설트해줘야겠지?
            inGameMapper.insGameUserResultLog(map);
        }

        //결과값 맵 저장
        response.setBody(resultMap);

        Map<String, Object> questMap;
        questMap = new HashMap<String, Object>();

        int unit_upgrade_count = 0;
        int drawing_count = 0;
        int result_kill_zombie = 0;
        int epic_skill_count = 0;
        int use_goods = 0;
        int game_clear_count_week = 0;
        int result_kill_zombie_week = 0;
        int unit_relics_count = 0;
        int unit_narrative_count = 0;
        int unit_legend_count = 0;
        int unit_mythology_count = 0;
        int unit_beginning_count = 0;

        //퀘스트 데이터 인설트
        if(game_result.equals("Clear") || game_result.equals("Failed")){
            if(game_result.equals("Clear")){

                game_clear_count_week += 1;
                //day
                unit_upgrade_count += Integer.parseInt(String.valueOf(map.get("ar_upgrade_count"))) + Integer.parseInt(String.valueOf(map.get("shotgun_upgrade_count"))) + Integer.parseInt(String.valueOf(map.get("sniping_upgrade_count")));
                drawing_count += Integer.parseInt(String.valueOf(map.get("drawing_count")));
                result_kill_zombie += Integer.parseInt(String.valueOf(map.get("result_kill_zombie")));
                epic_skill_count += Integer.parseInt(String.valueOf(map.get("epic_skill_1_count"))) + Integer.parseInt(String.valueOf(map.get("epic_skill_2_count")));
                use_goods += Integer.parseInt(String.valueOf(map.get("use_goods")));

                //week
                result_kill_zombie_week += Integer.parseInt(String.valueOf(map.get("result_kill_zombie")));
                unit_relics_count += Integer.parseInt(String.valueOf(map.get("ar_relics"))) + Integer.parseInt(String.valueOf(map.get("shotgun_relics"))) + Integer.parseInt(String.valueOf(map.get("sniping_relics")));
                unit_narrative_count += Integer.parseInt(String.valueOf(map.get("ar_narrative"))) + Integer.parseInt(String.valueOf(map.get("shotgun_narrative"))) + Integer.parseInt(String.valueOf(map.get("sniping_narrative")));
                unit_legend_count += Integer.parseInt(String.valueOf(map.get("ar_legend"))) + Integer.parseInt(String.valueOf(map.get("shotgun_legend"))) + Integer.parseInt(String.valueOf(map.get("sniping_legend")));
                unit_mythology_count += Integer.parseInt(String.valueOf(map.get("ar_mythology"))) + Integer.parseInt(String.valueOf(map.get("shotgun_mythology"))) + Integer.parseInt(String.valueOf(map.get("sniping_mythology")));
                unit_beginning_count += Integer.parseInt(String.valueOf(map.get("ar_beginning"))) + Integer.parseInt(String.valueOf(map.get("shotgun_beginning"))) + Integer.parseInt(String.valueOf(map.get("sniping_beginning")));
            }else{
                //clear, Retire 둘 중 하나
                //day
                unit_upgrade_count += Integer.parseInt(String.valueOf(map.get("ar_upgrade_count"))) + Integer.parseInt(String.valueOf(map.get("shotgun_upgrade_count"))) + Integer.parseInt(String.valueOf(map.get("sniping_upgrade_count")));
                drawing_count += Integer.parseInt(String.valueOf(map.get("drawing_count")));
                result_kill_zombie += Integer.parseInt(String.valueOf(map.get("result_kill_zombie")));
                epic_skill_count += Integer.parseInt(String.valueOf(map.get("epic_skill_1_count"))) + Integer.parseInt(String.valueOf(map.get("epic_skill_2_count")));
                use_goods += Integer.parseInt(String.valueOf(map.get("use_goods")));

                //week
                result_kill_zombie_week += Integer.parseInt(String.valueOf(map.get("result_kill_zombie")));
                unit_relics_count += Integer.parseInt(String.valueOf(map.get("ar_relics"))) + Integer.parseInt(String.valueOf(map.get("shotgun_relics"))) + Integer.parseInt(String.valueOf(map.get("sniping_relics")));
                unit_narrative_count += Integer.parseInt(String.valueOf(map.get("ar_narrative"))) + Integer.parseInt(String.valueOf(map.get("shotgun_narrative"))) + Integer.parseInt(String.valueOf(map.get("sniping_narrative")));
                unit_legend_count += Integer.parseInt(String.valueOf(map.get("ar_legend"))) + Integer.parseInt(String.valueOf(map.get("shotgun_legend"))) + Integer.parseInt(String.valueOf(map.get("sniping_legend")));
                unit_mythology_count += Integer.parseInt(String.valueOf(map.get("ar_mythology"))) + Integer.parseInt(String.valueOf(map.get("shotgun_mythology"))) + Integer.parseInt(String.valueOf(map.get("sniping_mythology")));
                unit_beginning_count += Integer.parseInt(String.valueOf(map.get("ar_beginning"))) + Integer.parseInt(String.valueOf(map.get("shotgun_beginning"))) + Integer.parseInt(String.valueOf(map.get("sniping_beginning")));
            }
        }

        questMap.put("user_uuid", map.get("user_uuid"));

        questMap.put("unit_upgrade_count", unit_upgrade_count);
        questMap.put("drawing_count", drawing_count);
        questMap.put("result_kill_zombie", result_kill_zombie);
        questMap.put("epic_skill_count", epic_skill_count);
        questMap.put("use_goods", use_goods);

        questMap.put("game_clear_count_week", game_clear_count_week);
        questMap.put("result_kill_zombie_week", result_kill_zombie_week);
        questMap.put("unit_relics_count", unit_relics_count);
        questMap.put("unit_narrative_count", unit_narrative_count);
        questMap.put("unit_legend_count", unit_legend_count);
        questMap.put("unit_mythology_count", unit_mythology_count);
        questMap.put("unit_beginning_count", unit_beginning_count);

        questMapper.uptUserDayQuestIngameData(questMap);
        questMapper.uptUserWeekQuestIngameData(questMap);

        //좀비수, 웨이브수, 드랍골드, 드랍루비 이상 치 확인하기(해킹 대비)
        int hack_check_result_kill_zombie = (int) map.get("result_kill_zombie");
        int hack_check_drop_gold_result = (int) map.get("drop_gold");
        int hack_check_drop_ruby_result = (int) map.get("drop_ruby");
        int hack_check_use_goods_result = (int) map.get("use_goods");

        int hack_check_game_wave = (int) map.get("game_wave");

        int hack_check_ar_upgrade_count_result = (int) map.get("ar_upgrade_count");
        int hack_check_shotgun_upgrade_count_result = (int) map.get("shotgun_upgrade_count");
        int hack_check_sniping_upgrade_count_result = (int) map.get("sniping_upgrade_count");

        Object resultScoreObj = map.get("result_score");
        if (resultScoreObj != null && resultScoreObj instanceof Number) {
            double hack_check_result_score = ((Number) resultScoreObj).doubleValue();
            if (!map.get("game_mode").equals("boss")) {
                if (hack_check_result_score > 100) {
                    map.put("hack_memo", hack_check_result_score + "_many_hack_check_result_score");
                    resultMap.put("hack_check_result_score", "many_hack_check_result_score");
                    inGameMapper.insGameResultHackLog(map);
                }
            }
        }

        if(hack_check_result_kill_zombie > 4000){
            map.put("hack_memo", hack_check_result_kill_zombie + "_many_hack_check_result_kill_zombie");
            resultMap.put("zombie_kill_hack_doubt", "many_hack_check_result_kill_zombie");
            inGameMapper.insGameResultHackLog(map);
        }
        if(hack_check_game_wave > 90){
            map.put("hack_memo", hack_check_game_wave + "_many_hack_check_game_wave");
            resultMap.put("wave_hack_doubt", "many_hack_check_game_wave");
            inGameMapper.insGameResultHackLog(map);
        }
        if(hack_check_drop_gold_result > 2000){
            map.put("hack_memo", hack_check_drop_gold_result + "_many_hack_check_drop_gold_result");
            resultMap.put("gold_hack_doubt", "many_hack_check_drop_gold_result");
            inGameMapper.insGameResultHackLog(map);
        }
        if(hack_check_drop_ruby_result > 150){
            map.put("hack_memo", hack_check_drop_ruby_result + "_many_hack_check_drop_ruby_result");
            resultMap.put("ruby_hack_doubt", "many_hack_check_drop_ruby_result");
            inGameMapper.insGameResultHackLog(map);
        }
        if(hack_check_use_goods_result > 9000){
            map.put("hack_memo", hack_check_use_goods_result + "_many_hack_check_use_goods_result");
            resultMap.put("goods_hack_doubt", "many_hack_check_use_goods_result");
            inGameMapper.insGameResultHackLog(map);
        }

        if(hack_check_ar_upgrade_count_result > 60){
            map.put("hack_memo", hack_check_ar_upgrade_count_result + "_hack_check_ar_upgrade_count_result_result");
            resultMap.put("goods_hack_doubt", "hack_check_ar_upgrade_count_result");
            inGameMapper.insGameResultHackLog(map);
        }

        if(hack_check_shotgun_upgrade_count_result > 60){
            map.put("hack_memo", hack_check_shotgun_upgrade_count_result + "_hack_check_shotgun_upgrade_count_result_result");
            resultMap.put("goods_hack_doubt", "hack_check_shotgun_upgrade_count_result");
            inGameMapper.insGameResultHackLog(map);
        }

        if(hack_check_sniping_upgrade_count_result > 60){
            map.put("hack_memo", hack_check_sniping_upgrade_count_result + "_hack_check_sniping_upgrade_count_result_result");
            resultMap.put("goods_hack_doubt", "hack_check_sniping_upgrade_count_result");
            inGameMapper.insGameResultHackLog(map);
        }

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> selectWaveList(Map<String, Object> map) throws Exception {

        ApiResponse response = new ApiResponse(HttpStatus.OK, inGameMapper.selectWaveList(map));

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> selectGameModeList(Map<String, Object> map) throws Exception {

        ApiResponse response = new ApiResponse(HttpStatus.OK, inGameMapper.selectGameModeList(map));

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> selectUnitList(Map<String, Object> map) throws Exception {

        ApiResponse response = new ApiResponse(HttpStatus.OK, inGameMapper.selectUnitList(map));

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public Map<String, Object> experienceLevelCalc(Map<String, Object> map) throws Exception {

        ApiResponse response = new ApiResponse();

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        Map<String, Object> finalResultMap;
        finalResultMap = new HashMap<String, Object>();

        //유저 부스터 있는지 없는지 확인 있다면 2배주기
        int booster_int = 1;
        List<HashMap<String, Object>> selectExpBoosterYnList = productMapper.selectExpBoosterYn(map);

        if(selectExpBoosterYnList.size() != 0) {
            String booster_yn = String.valueOf(selectExpBoosterYnList.get(0).get("booster_yn"));
            if(booster_yn.equals("Y")){
                booster_int = 2;
                resultMap.put("etc_note", "경험치 부스터");
                finalResultMap.put("booster_yn", "Y");
            }else{
                finalResultMap.put("booster_yn", "N");
            }
        }else{
            finalResultMap.put("booster_yn", "N");
        }

        List<HashMap<String, Object>> selectGameModeList = inGameMapper.selectGameModeList(map);

        float exp_bonus = (float) selectGameModeList.get(0).get("exp_bonus");

        //유저 데이터값 가져오기
        List<HashMap<String, Object>> userResultList = userMapper.selectUserData(map);
        //유저 uuid
        resultMap.put("user_uuid", map.get("user_uuid"));
        //유저 ip
        resultMap.put("ins_ip", map.get("ins_ip"));
        //유저 ins_machine
        resultMap.put("ins_machine", map.get("ins_machine"));
        //유저 start_seq
        resultMap.put("start_seq", map.get("start_seq"));
        //현 유저 레벨, 렙업전 레벨
        int user_level = (int) userResultList.get(0).get("user_level");
        resultMap.put("before_level", user_level);
        finalResultMap.put("before_level", user_level);
        //얻은 경험치 량
        int experienceQuantity = Math.round((((int) map.get("game_wave") - 1) * exp_bonus * booster_int));
        resultMap.put("experience_quantity", experienceQuantity);
        //현 유저 경험치
        int user_level_experience = (int) userResultList.get(0).get("user_level_experience");
        resultMap.put("before_experience", user_level_experience);
        finalResultMap.put("before_experience", user_level_experience);
        //현 유저 총 경험치
        int user_level_experience_max = (int) userResultList.get(0).get("user_level_experience_max");
        resultMap.put("before_experience_max", user_level_experience_max);
        finalResultMap.put("before_experience_max", user_level_experience_max);
        //얻고 난 후의 총 경험치
        int afterExperienceMax =   user_level_experience_max + experienceQuantity;
        resultMap.put("after_experience_max", afterExperienceMax);
        resultMap.put("user_level_experience_max", afterExperienceMax);

        finalResultMap.put("after_experience_max", afterExperienceMax);
        finalResultMap.put("user_level_experience_max", afterExperienceMax);
        //유저 루비값 다시 가져오기
        //유저 데이터값 가져오기
        int user_ruby = (int) userResultList.get(0).get("user_ruby");
        map.put("user_ruby", user_ruby);


        //경험치 얻고 난 후의 레벨 계산
      /*1 = 100 100 1*50+50
        2 = 150 250 2*50+50
        3 = 200 450 3*50+50
        4 = 250 700 4*50+50
        5 = 300 1000 5*50+50
        6 = 350 1350
        */
/*ex) 지금 렙 3에 경험치 1000을먹음   420*/

        int plus_Level = 0;
        Map<String, Object> levelReward = new HashMap<>();

        //지금 렙의 총 경험치
        int experience_max_table = 0;
        for (int i = 1; i <= user_level + plus_Level; i++) {
            experience_max_table += i * 50 + 50;
            if(user_level + plus_Level == i) {
                if (experience_max_table - afterExperienceMax <= 0) {
                    plus_Level += 1;
                }
            }
        }

        //레벨업이 일어날 때마다, plus_Level만큼 재화를 지급하는 기능
        int level_up_reward_ruby = 0;
        if(plus_Level > 0) { //레벨업이 일어났을 경우

            //유니크값 만들기
            String uniqueId = "";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            Calendar dateTime = Calendar.getInstance();
            uniqueId = sdf.format(dateTime.getTime());

            //유니크값 지정
            uniqueId =  "game_result_level_up_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//유니크값
            map.put("charge_ruby_unique_idx", uniqueId);

            user_ruby = user_ruby + (int) map.get("drop_ruby");

            for (int i = 1; i <= plus_Level; i++) {
                int reward = (user_level + i) * 50; //레벨업한 레벨에 비례한 보상
                level_up_reward_ruby += reward;

                if(reward >= 500){
                    level_up_reward_ruby = 500;
                    reward = 500;
                }

                Map<String, Integer> levelData = new HashMap<>();
                levelData.put("level", user_level + i);
                levelData.put("reward_ruby", reward);
                levelReward.put("level_"+ (user_level + i), levelData);

                //루비 충전값 계산
                map.put("charge_reason", "레벨_"+(user_level + i)+"로 올라 축하 루비");
                map.put("charge_ruby_quantity", reward); // DB에 넣기 위한 레벨업 결과 루비
                map.put("before_charge_ruby_quantity", user_ruby); // DB에 넣기 위한 충전 전 루비값 저장
                map.put("after_charge_ruby_quantity", user_ruby + reward); // DB에 넣기 위한 충전 후 루비 값 저장
                user_ruby += reward;
                goodsMapper.insChargeRubyLog(map); // 루비 충전 로그 저장

                finalResultMap.put("level_up_result_list", levelReward);
            }


        }

        finalResultMap.put("level_up_reward_ruby", level_up_reward_ruby);

        //잔여 경험치 계산을 위한 식
        int experience_max_table_1_before = 0;
        for (int i = 1; i <= user_level + plus_Level - 1; i++) {
            experience_max_table_1_before += i * 50 + 50;

        }

        //렙업 하고 나서의 렙(렙업 안 되면 렙업 안된 상태랑 같은 값 들어갈것)
        int after_level = user_level + plus_Level;
        resultMap.put("after_level", after_level);
        resultMap.put("user_level", after_level);

        finalResultMap.put("after_level", after_level);
        finalResultMap.put("user_level", after_level);

        //잔여 경험치
        int after_experience = 0;
        if(plus_Level > 0) { //레업이 한 번이라도 있을때
            after_experience = (experience_max_table_1_before - afterExperienceMax) * -1;
        }else{ //렙업이 없을때
            after_experience = user_level_experience + experienceQuantity;
        }
        resultMap.put("after_experience", after_experience);
        resultMap.put("user_level_experience", after_experience);

        finalResultMap.put("after_experience", after_experience);
        finalResultMap.put("user_level_experience", after_experience);

        //레벨 로그 쌓기
        inGameMapper.insGameExperienceLog(resultMap);
        //유저 데이터 수정
        userMapper.uptUserLevelExp(resultMap);

        return finalResultMap;

    }

    @Transactional
    public ResponseEntity<?> insGameResultAdvertisementGoods(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();
        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        //그 게임 결과 값 가져오기
        List<HashMap<String, Object>> selectGameResultData = inGameMapper.selectGameResultData(map);

        int game_result_gold = (int) selectGameResultData.get(0).get("drop_gold");
        int game_result_ruby = (int) selectGameResultData.get(0).get("drop_ruby");
        String unique_idx = String.valueOf(selectGameResultData.get(0).get("unique_idx"));

        //유저 데이터값 가져오기
        List<HashMap<String, Object>> userResultList = userMapper.selectUserData(map);

        int user_gold = (int) userResultList.get(0).get("user_gold");
        int user_ruby = (int) userResultList.get(0).get("user_ruby");

        String uniqueId = "seq_" + map.get("result_seq") + "_advertisement_" + unique_idx;

        //골드 충전값 계산
        map.put("charge_gold_quantity", game_result_gold); // DB에 넣기 위한 추과 골드 값
        map.put("before_charge_gold_quantity", user_gold); // DB에 넣기 위한 충전 전 골드값 저장
        map.put("after_charge_gold_quantity", user_gold + game_result_gold); // DB에 넣기 위한 충전 후 골드 값 저장
        map.put("charge_reason", "광고시청으로 인한 보상2배");
        map.put("charge_gold_unique_idx", uniqueId);
        goodsMapper.insChargeGoldLog(map); // 골드 충전 로그 저장

        map.put("user_gold", user_gold + game_result_gold); // user_data의 최종 골드 값 넣기

        //키 결과값 클라에 뿌려주기위해
        resultMap.put("result_gold", user_gold + game_result_gold);

        //유저 골드 업데이트
        goodsMapper.uptUserGold(map);

        /*################루비 충전 시작###################*/
        //루비 충전값 계산
        map.put("charge_ruby_quantity", game_result_ruby); // DB에 넣기 위한 게임 결과 루비
        map.put("before_charge_ruby_quantity", user_ruby); // DB에 넣기 위한 충전 전 루비값 저장
        map.put("after_charge_ruby_quantity", user_ruby + game_result_ruby); // DB에 넣기 위한 충전 후 루비 값 저장
        map.put("charge_reason", "광고시청으로 인한 보상2배");
        map.put("charge_ruby_unique_idx", uniqueId);
        goodsMapper.insChargeRubyLog(map); // 루비 충전 로그 저장

        map.put("user_ruby", user_ruby + game_result_ruby); // user_data의 최종 루비 값 넣기

        //루비 결과값 클라에 뿌려주기위해
        resultMap.put("result_ruby", user_ruby + game_result_ruby);

        //유저 루비 업데이트
        goodsMapper.uptUserRuby(map);

        //게임 결과 유니크 값을 광고 로그 유니크 값으로 쓰기
        map.put("advertisement_unique_idx", uniqueId);
        String user_skip_yn = String.valueOf(userResultList.get(0).get("advertisement_buy_yn"));
        map.put("skip_yn", user_skip_yn);

        //광고 로그 쌓기
        commonMapper.insAdvertisementLog(map);

        //결과값 맵 저장
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

}