package com.goall.zombie_defence.controller.api.inGame;

import com.goall.zombie_defence.mapper.QuestMapper;
import com.goall.zombie_defence.service.CommonService;
import com.goall.zombie_defence.service.QuestService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/quest")
@RestController
public class QuestController {

    @Autowired
    QuestService questService;

    @Autowired
    QuestMapper questMapper;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="정보 가져올 유저 uuid값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="유저의 퀘스트 리스트 카운트 가져오기")
    @PostMapping(value = "/selectQuestCountList")
    @ResponseBody
    public ResponseEntity<?> selectQuestCountList(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return questService.selectQuestCountList(map, request);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="정보 가져올 유저 uuid값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "quest_eng_name", value="퀘스트 이름값 뿌려줘야함 ", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="사용 기기값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="퀘스트 클리어했을때")
    @PostMapping(value = "/questClear")
    @ResponseBody
    public ResponseEntity<?> questClear(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return questService.questClear(map, request);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="정보 가져올 유저 uuid값", required = false, dataTypeClass = String.class, paramType = "query"),
    })
    @ApiOperation(value="광고보고 보상받는 재화 리스트 가져오기")
    @PostMapping(value = "/selectAdRewardList")
    @ResponseBody
    public List<HashMap<String, Object>> selectAdRewardList(@RequestBody HashMap<String, Object> map) throws Exception {

        return questMapper.selectAdRewardList(map);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="정보 가져올 유저 uuid값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "quest_eng_name", value="퀘스트 이름값 뿌려줘야함 ", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="사용 기기값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "advertisement_yn", value="광고인지 아닌지 Y,N값으로", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="광고보고 보상받는거 보상 받기")
    @PostMapping(value = "/questAdRewardClear")
    @ResponseBody
    public ResponseEntity<?> questAdRewardClear(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return questService.questAdRewardClear(map, request);
    }
}
