package com.goall.zombie_defence.controller.api.inGame;

import com.goall.zombie_defence.service.InGameService;
import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.goall.zombie_defence.response.ApiResponse;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@RequestMapping("/inGame")
@RestController
public class InGameController {

    @Autowired
    InGameService inGameService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="유저아이디", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "game_mode", value="게임모드", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "game_level", value="게임레벨", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "skip_mode_yn", value="스킵모드", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="기기값", required = false, dataTypeClass = String.class, paramType = "query")
    })

    @ApiOperation(value="게임 시작할시 로그 저장 and 'user_data' 테이블의 'user_gaem_count' 컬럼 플러스 1해주기")
    @PostMapping(value = "/insGameStartLog")
    public ResponseEntity<?> insGameStartLog(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        //0.5초 기다리기
        //Thread.sleep(700);
/*

        // 이전에 실행된 시간을 가져와서, 일정 시간이 지나지 않으면 요청을 처리하지 않도록 합니다.
        Long previousTime = (Long) request.getSession().getAttribute("previousTime");
        Long currentTime = System.currentTimeMillis();
        int start_seq;
        if (previousTime != null && currentTime - previousTime < TimeUnit.SECONDS.toMillis(3)) {
            ApiResponse response = new ApiResponse();
            response.setAutoSeq((Integer) request.getSession().getAttribute("start_seq"));
            return new ResponseEntity<>(response, response.getStatus());
        }
        request.getSession().setAttribute("previousTime", currentTime);
*/

        return inGameService.insGameStartLog(map, request);

        /*ApiResponse response = new ApiResponse();

        start_seq = Integer.parseInt(String.valueOf(map.get("start_seq")));
        response.setAutoSeq(start_seq);
        request.getSession().setAttribute("start_seq", start_seq);

        return new ResponseEntity<>(response, response.getStatus());*/
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "start_seq", value="game_start_data 테이블의 seq값", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_uuid", value="유저아이디", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "game_mode", value="게임모드", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "epic_skill_1", value="사용한 스킬1 값", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "epic_skill_1_count", value="사용한 스킬1 몇 번 사용했는지", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "epic_skill_1_level", value="사용한 스킬1 값 레벨", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "epic_skill_2", value="사용한 스킬2 값", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "epic_skill_2_count", value="사용한 스킬2 몇 번 사용했는지", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "epic_skill_2_level", value="사용한 스킬2 값 레벨", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "drop_gold", value="드랍된 골드", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "drop_ruby", value="드랍된 루비", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "game_level", value="게임레벨", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "game_result", value="게임결과(ex false, success)", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "game_play_time", value="게임 플레이 타임", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "use_goods", value="소모한 재화(유닛, 업그레이드 할때 쓰는)", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "remaining_goods", value="남은 재화(유닛뽑할때 쓰는)", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "game_wave", value="끝난 최종 wave", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "drawing_count", value="유닛 뽑기 횟수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "result_unit", value="최종 유닛 마리수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "result_gold", value="결과 보상 골드", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "result_bp", value="결과 보상 보스포인트", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "result_adv_yn", value="광고 시청 했는지", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "result_score", value="게임 결과 점수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ar_upgrade_count", value="ar 업그레이드 카운트", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "shotgun_upgrade_count", value="샷건 업그레이드 카운트", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "sniping_upgrade_count", value="저격 업그레이드 카운트", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ar_normal", value="ar 노말 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ar_rare", value="ar 레어 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ar_ancient", value="ar 고대 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ar_relics", value="ar 유물 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ar_narrative", value="ar 서사 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ar_legend", value="ar 전설 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ar_mythology", value="ar 신화 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ar_beginning", value="ar 태초 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "shotgun_normal", value="샷건 노말 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "shotgun_rare", value="샷건 레어 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "shotgun_ancient", value="샷건 고대 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "shotgun_relics", value="샷건 유물 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "shotgun_narrative", value="샷건 서사 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "shotgun_legend", value="샷건 전설 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "shotgun_mythology", value="샷건 신화 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "shotgun_beginning", value="샷건 태초 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "sniping_normal", value="저격 노말 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "sniping_rare", value="저격 레어 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "sniping_ancient", value="저격 고대 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "sniping_relics", value="저격 유물 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "sniping_narrative", value="저격 서사 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "sniping_legend", value="저격 전설 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "sniping_mythology", value="저격 신화 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "sniping_beginning", value="저격 태초 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ar_normal_sell", value="ar 노말 판매 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ar_rare_sell", value="ar 레어 판매 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ar_ancient_sell", value="ar 고대 판매 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ar_relics_sell", value="ar 유물 판매 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "shotgun_normal_sell", value="샷건 노말 판매 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "shotgun_rare_sell", value="샷건 레어 판매 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "shotgun_ancient_sell", value="샷건 고대 판매 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "shotgun_relics_sell", value="샷건 유물 판매 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "sniping_normal_sell", value="저격 노말 판매 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "sniping_rare_sell", value="저격 레어 판매 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "sniping_ancient_sell", value="저격 고대 판매 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "sniping_relics_sell", value="저격 유물 판매 유닛수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_ar_damage_up_skill_count", value="플레이어 스킬 ar 데미지 증가 카운트", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_shotgun_damage_up_skill_count", value="플레이어 스킬 샷건 데미지 증가 카운트", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_sniper_damage_up_skill_count", value="플레이어 스킬 저격 데미지 증가 카운트", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_ar_speed_attack_up_skill_count", value="플레이어 스킬 ar 공격 속도 증가 카운트", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_shotgun_speed_attack_up_skill_count", value="플레이어 스킬 샷건 공격 속도 증가 카운트", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_sniper_speed_attack_up_skill_count", value="플레이어 스킬 저격 공격 속도 증가 카운트", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_nut_up_skill_count", value="플레이어 너츠 드랍 확률 증가 카운트", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_gold_up_skill_count", value="플레이어 골드 드랍 확률 증가 카운트", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_ruby_up_skill_count", value="플레이어 루비 드랍 확률 증가 카운트", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "unit_narrative_exchange_count", value="서사 교환 횟수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "unit_legend_exchange_count", value="전설 교환 횟수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "unit_mythology_exchange_count", value="신화 교환 횟수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "special_draw_count", value="에픽 스킬의 인재 발굴 스킬을 몇번 사용했는지.", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "normal_exchange_count", value="에픽 스킬의 화려한 전역 스킬로 인해 노말 등급이 몇번 변경 되었는지", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="플레이한 기기", required = false, dataTypeClass = String.class, paramType = "query"),
    })

    @ApiOperation(value="게임 끝나면 결과 로그 저장")
    @PostMapping(value = "/insGameResultLog")
    public ResponseEntity<?> insGameResultLog(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return inGameService.insGameResultLog(map, request);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "game_mode", value="게임모드", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "game_level", value="게임레벨", required = false, dataTypeClass = String.class, paramType = "query"),
    })
    @ApiOperation(value="웨이브 정보값 가져오기")
    @PostMapping(value = "/selectWaveList")
    @ResponseBody
    public ResponseEntity<?> selectWaveList(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return inGameService.selectWaveList(map);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "game_mode", value="게임모드", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "game_level", value="게임레벨", required = false, dataTypeClass = String.class, paramType = "query"),
    })
    @ApiOperation(value="게임 모드 정보 가져오기")
    @PostMapping(value = "/selectGameModeList")
    @ResponseBody
    public ResponseEntity<?> selectGameModeList(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return inGameService.selectGameModeList(map);
    }

    @ApiOperation(value="유닛 정보 가져오기")
    @PostMapping(value = "/selectUnitList")
    @ResponseBody
    public ResponseEntity<?> selectUnitList(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return inGameService.selectUnitList(map);
    }
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uesr_uuid", value="유저아이디값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "result_seq", value="그 게임의 seq값(result_seq)", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="플레이한 기기", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="광고 보고 게임 결과 보상 2배")
    @PostMapping(value = "/insGameResultAdvertisementGoods")
    public ResponseEntity<?> insGameResultAdvertisementGoods(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return inGameService.insGameResultAdvertisementGoods(map, request);
    }

}
