package com.goall.zombie_defence.controller.api.inGame;

import com.goall.zombie_defence.response.ApiResponse;
import com.goall.zombie_defence.service.EpicSkillService;
import com.goall.zombie_defence.service.UserService;
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
@RequestMapping("/epicSkill")
@RestController
public class EpicSkillController {

    @Autowired
    EpicSkillService epicSkillService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "epic_skill_idx", value="에픽 스킬 인덱스 값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "epic_skill_grade", value="에픽 스킬의 등급", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "epic_skill_level", value="에픽 스킬의 레벨", required = false, dataTypeClass = String.class, paramType = "query")
            
    })
    @ApiOperation(value="에픽 스킬 값 가쟈오기(json값 비우면 다 가져옴)")
    @PostMapping(value = "/selectEpicSkillData")
    @ResponseBody
    public ResponseEntity<?> selectEpicSkillData(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return epicSkillService.selectEpicSkillData(map);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="검색할 유저 uuid값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="유저가 가지고있는 에픽 장비 다! 가져오기")
    @PostMapping(value = "/totalUserEpicSkillData")
    @ResponseBody
    public ResponseEntity<?> totalUserEpicSkillData(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return epicSkillService.totalUserEpicSkillData(map);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="검색할 유저 uuid값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "seq", value="에픽 스킬의 고유값인 seq", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="유저가 업그레이드 할 때 장비를 하나 선택할때 쓰는 api")
    @PostMapping(value = "/upgradeSelectUserEpicSkill")
    @ResponseBody
    public ResponseEntity<?> upgradeSelectUserEpicSkill(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return epicSkillService.upgradeSelectUserEpicSkill(map);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="저장할 유저의 uuid값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "user_epic_skill_idx", value="epic_skill_data 테이블의 epic_skill_idx값(ex- user_epic_skill_idx:'test1','test2','test3')(공백없이 보내줘야함)", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "user_epic_skill_grade", value="epic_skill_data 테이블의 epic_skill_grade값(ex- epic_skill_grade:'test1','test2','test3')(공백없이 보내줘야함)", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "user_epic_skill_level", value="epic_skill_data 테이블의 epic_skill_level값(ex- epic_skill_level:'test1','test2','test3')(공백없이 보내줘야함)", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "user_epic_skill_exp", value="epic_skill_data 테이블의 epic_skill_exp값(ex- epic_skill_exp:'test1','test2','test3')(공백없이 보내줘야함)", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="저장할 유저의 기기값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "use_ruby_count", value="사용한 루비의 수(사용 안 했다면 0개로 보내줘야함)", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "draw_type", value="고급(advanced)뽑기인지 일반(normal)뽑기 인지", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "advertisement_yn", value="광고인지 아닌지 Y,N값으로", required = false, dataTypeClass = String.class, paramType = "query")


    })
    @ApiOperation(value="뽑기하고 유저의 인벤에 저장하고 루비 사용 로그 및 루비 사용 양 계산 위한 api")
    @PostMapping(value = "/insUserEpicSkill")
    public ResponseEntity<?> insUserEpicSkill(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return epicSkillService.insUserEpicSkill(map, request);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "epic_skill_draw_type", value="노말 뽑기(normal), 고급 뽑기(advanced), 확정 뽑기(confirmation)", required = false, dataTypeClass = String.class, paramType = "query"),

    })
    @ApiOperation(value="뽑기할때 필요한 정보 값(뽑기 확률 포함)!(epic_skill_draw_type의 값을 잘 보내줘야함)")
    @PostMapping(value = "/epicSkillDrawSelect")
    @ResponseBody
    public ResponseEntity<?> epicSkillDrawSelect(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return epicSkillService.epicSkillDrawSelect(map);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="저장할 유저의 uuid값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "user_epic_skill_1", value="첫 번째 스킬 SEQ 값 넣어야함(빈값이면 0으로보내줘!)", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_epic_skill_2", value="두 번째 스킬 SEQ 값 넣어야함(빈값이면 0으로보내줘!)", required = false, dataTypeClass = Integer.class, paramType = "query")

    })
    @ApiOperation(value="에픽 스킬 착용 API")
    @PostMapping(value = "/uptUserEpicSkillEquip")
    @ResponseBody
    public ResponseEntity<?> uptUserEpicSkillEquip(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        epicSkillService.uptUserEpicSkillEquip(map, request);

        ApiResponse response = new ApiResponse();

        return new ResponseEntity<>(response, response.getStatus());

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="저장할 유저의 uuid값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "epic_skill_seq_list", value="재료가 되는 에픽 스킬의 seq값들(ex_user_epic_skill_idx:'2318','2317','2316')공백 없이", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_epic_skill_seq", value="강화에 당하는 에픽 스킬의 seq값", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="사용 기기값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="에픽 스킬 업그레이드 버튼 누를떄 필요한 api")
    @PostMapping(value = "/uptUpgradeEpicSkill")
    @ResponseBody
    public ResponseEntity<?> uptUpgradeEpicSkill(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return epicSkillService.uptUpgradeEpicSkill(map, request);

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="저장할 유저의 uuid값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "epic_skill_seq_1", value="강화 할 첫 번째 에픽 스킬의 seq", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "epic_skill_seq_2", value="강화 할 두 번째 에픽 스킬의 seq", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="사용 기기값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="에픽 스킬 합성 api")
    @PostMapping(value = "/insertEpicSkillSynthesis")
    @ResponseBody
    public ResponseEntity<?> insertEpicSkillSynthesis(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return epicSkillService.insertEpicSkillSynthesis(map, request);

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="저장할 유저의 uuid값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "epic_skill_seq_list", value="판매가 되는 에픽 스킬의 seq값들(ex_user_epic_skill_idx:'2318','2317','2316')공백 없이", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="사용 기기값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="에픽 스킬 판매 버튼 누를떄 필요한 api")
    @PostMapping(value = "/insSellEpicSkill")
    @ResponseBody
    public ResponseEntity<?> insSellEpicSkill(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return epicSkillService.insSellEpicSkill(map, request);

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="저장할 유저의 uuid값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "epic_skill_seq", value="교환할 5성 sqe", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="사용 기기값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="5성 에픽 스킬 재교환 하기 전에 그 등급의 모든 스킬을 다 가지고있는지 체크")
    @PostMapping(value = "/Change5EpicSkillCheck")
    @ResponseBody
    public ResponseEntity<?> Change5EpicSkillCheck(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return epicSkillService.Change5EpicSkillCheck(map, request);

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="저장할 유저의 uuid값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "epic_skill_seq", value="교환할 5성 sqe", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="사용 기기값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="5성 에픽 스킬 재교환 api")
    @PostMapping(value = "/ins5ChangeEpicSkill")
    @ResponseBody
    public ResponseEntity<?> ins5ChangeEpicSkill(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return epicSkillService.ins5ChangeEpicSkill(map, request);

    }

}
