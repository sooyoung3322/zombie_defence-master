package com.goall.zombie_defence.controller.api.inGame;

import com.goall.zombie_defence.response.ApiResponse;
import com.goall.zombie_defence.service.EpicSkillService;
import com.goall.zombie_defence.service.PlayerSkillService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@RequiredArgsConstructor
@RequestMapping("/playerSkill")
@RestController
public class PlayerSkillController {

    @Autowired
    PlayerSkillService playerSkillService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="저장할 유저의 uuid값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "user_damage_up_skill_count", value="유저가 찍은 데미지 증가 스킬 개수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_speed_attack_up_skill_count", value="유저가 찍은 유닛 공격속도 스킬 개수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_start_nut_up_skill_count", value="유저가 찍은 시작너트 스킬 개수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_epic_time_down_up_skill_count", value="유저가 찍은 에픽 쿨타임 증가 스킬 개수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_zombie_max_up_skill_count", value="유저가 찍은 좀비 맥스치 증가 스킬 개수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_game_speed_up_skill_count", value="유저가 찍은 게임 속도 증가 스킬 개수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_nut_up_skill_count", value="유저가 찍은 너츠 드랍 확률 증가 스킬 개수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_gold_up_skill_count", value="유저가 찍은 골드 드랍 확률 증가 스킬 개수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_ruby_up_skill_count", value="유저가 찍은 루비 드랍 확률 증가 스킬 개수", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "upt_machine", value="사용 기기값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="저장된 플레이어 스킬 업데이트")
    @PostMapping(value = "/uptUserPlayerSkill")
    public ResponseEntity<?> uptUserPlayerSkill(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return playerSkillService.uptUserPlayerSkill(map, request);

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="회원 uuid", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="유저가 저장한 플레이어 스킬 가져오기")
    @PostMapping(value = "/userPlayerSkillList")
    @ResponseBody
    public ResponseEntity<?> userPlayerSkillList(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return playerSkillService.userPlayerSkillList(map);
    }

    @ApiOperation(value="플레이어 스킬 밸류값, 최대치값 가져오기")
    @PostMapping(value = "/playerSkillDataList")
    @ResponseBody
    public ResponseEntity<?> playerSkillDataList() throws Exception {

        return playerSkillService.playerSkillDataList();
    }

}
