package com.goall.zombie_defence.controller.api.inGame;

import com.goall.zombie_defence.response.ApiResponse;
import com.goall.zombie_defence.service.MailService;
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
@RequestMapping("/mail")
@RestController
public class MailController {

    @Autowired
    MailService mailService;


    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="회원 uuid", required = false, dataTypeClass = String.class, paramType = "query"),
    })
    @ApiOperation(value="유저가 가져올수 있는 우편 가져오기")
    @PostMapping(value = "/selectUserMailList")
    @ResponseBody
    public ResponseEntity<?> selectUserMailList(@RequestBody HashMap<String, Object> map) throws Exception {

        return mailService.selectUserMailList(map);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "reward_unique", value="우편 보상 유니크값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "user_uuid", value="회원 uuid", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="우편의 보상 리스트 가져오기")
    @PostMapping(value = "/selectUserMailRewardList")
    @ResponseBody
    public ResponseEntity<?> selectUserMailRewardList(@RequestBody HashMap<String, Object> map) throws Exception {

        return mailService.selectUserMailRewardList(map);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="유저의 uuid값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="사용 기기값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "reward_unique", value="우편 보상 유니크값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "seq", value="우편 보상 유니크값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="우편 보상 모두 받기 눌렀을때")
    @PostMapping(value = "/insertUserMailReward")
    @ResponseBody
    public ResponseEntity<?> insertUserMailReward(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return mailService.insertUserMailReward(map, request);
    }


}
