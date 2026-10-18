package com.goall.zombie_defence.controller.api.inGame;

import com.goall.zombie_defence.response.ApiResponse;
import com.goall.zombie_defence.service.StatisticsService;
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
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
public class UserController {

    @Autowired
    UserService userService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="체크할 uuid", required = false, dataTypeClass = String.class, paramType = "query"),
    })
    @ApiOperation(value="uuid 있는지 검사")
    @PostMapping(value = "/userUuidCheck")
    @ResponseBody
    public ResponseEntity<?> userUuidCheck(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return userService.userUuidCheck(map);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_nickname", value="체크할 닉네임", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "user_uuid", value="체크할 user_uuid", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="nickName,user_uuid 있는지 검사")
    @PostMapping(value = "/userNickNameCheck")
    @ResponseBody
    public ResponseEntity<?> userNickNameCheck(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return userService.userNickNameCheck(map);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="회원 uuid", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "user_nickname", value="회원 닉네임", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "user_ruby", value="회원 루비", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_gold", value="회원 골드", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_level", value="회원 레벨", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_level_experience", value="회원 레벨 경험치", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_key", value="회원 키(입장 비용 재화)", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_key_remain_time", value="회원 키 키 충전 시간", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_key_last_charge_time", value="회원 키 마지막 바뀐 시간", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "user_game_count", value="게임 얼마나 했는지 카운드", required = false, dataTypeClass = Integer.class, paramType = "query")
    })

    @ApiOperation(value="유저 데이터 저장(회원가입)")
    @PostMapping(value = "/insUserData")
    public ResponseEntity<?> insUserData(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        userService.insUserData(map, request);

        ApiResponse response = new ApiResponse();

        return new ResponseEntity<>(response, response.getStatus());
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="회원 uuid", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="회원 기기값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="유저 정보값 가져오기")
    @PostMapping(value = "/selectUserData")
    @ResponseBody
    public ResponseEntity<?> selectUserData(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return userService.selectUserData(map, request);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="회원 uuid", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "user_nickname", value="회원 닉네임", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="회원 기기값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="유저 로그인 이력 로그")
    @PostMapping(value = "/insUserLoginLog")
    public ResponseEntity<?> insUserLoginLog(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return userService.insUserLoginLog(map, request);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="회원 uuid", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="유저 기본 정보 값이 아닌 필요한 공통 데이터 값 가져오기")
    @PostMapping(value = "/selectCommonUserData")
    @ResponseBody
    public ResponseEntity<?> selectCommonUserData(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return userService.selectCommonUserData(map, request);

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="회원 uuid", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "user_nickname", value="추천 할 user_nickname", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="회원 기기값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="친구 추천 로직")
    @PostMapping(value = "/insUserRecommendations")
    @ResponseBody
    public ResponseEntity<?> insUserRecommendations(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return userService.insUserRecommendations(map, request);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "guest_user_uuid", value="회원 guest uuid", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "normal_user_uuid", value="회원 normal uuid", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="사용기기", required = false, dataTypeClass = Integer.class, paramType = "query"),
    })

    @ApiOperation(value="게스트 유저 노멀 유저로 전환")
    @PostMapping(value = "/insUserLink")
    public ResponseEntity<?> insUserLink(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return userService.insUserLink(map, request);

    }

}
