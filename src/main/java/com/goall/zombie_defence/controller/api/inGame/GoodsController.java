package com.goall.zombie_defence.controller.api.inGame;

import com.goall.zombie_defence.response.ApiResponse;
import com.goall.zombie_defence.service.GoodsService;
import com.goall.zombie_defence.service.InGameService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@RequiredArgsConstructor
@RequestMapping("/goods")
@RestController
public class GoodsController {

    @Autowired
    GoodsService goodsService;
/*

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="사용 유저의 uuid", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "start_seq", value="game_start_data의 seq", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "game_mode", value="열쇠를 사용한 게임 모드", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "game_level", value="열쇠를 사용한 게임 레벨", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "use_key_quantity", value="사용한 열쇠 수량", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "before_use_key_quantity", value="사용 전 열쇠", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "after_use_key_quantity", value="사용 후 열쇠", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "use_key_log_note", value="이 테이블의 비고(ex_운영자 권한 인설트 있을수있음)", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="ins_machine", required = false, dataTypeClass = String.class, paramType = "query")
    })

    @ApiOperation(value="키 사용 로그 쌓기")
    @PostMapping(value = "/insUseKeyLog")
    public ResponseEntity<?> insUseKeyLog(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        goodsService.insUseKeyLog(map, request);

        ApiResponse response = new ApiResponse();

        return new ResponseEntity<>(response, response.getStatus());
    }

    @ApiImplicitParams({
            //insChargeKeyLog
            @ApiImplicitParam(name = "user_uuid", value="(charge_key_log 테이블 값)사용 유저의 uuid", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "charge_reason", value="(charge_key_log 테이블 값)충전이 된 방법(자동, 구매, 광고 등)", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "charge_key_quantity", value="(charge_key_log 테이블 값)키 몇개 충전 되었는지", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "before_charge_key_quantity", value="(charge_key_log 테이블 값)충전 전 키 수량", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "after_charge_key_quantity", value="(charge_key_log 테이블 값)충전 후 키 수령", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="(charge_key_log 테이블 값)기기값", required = false, dataTypeClass = String.class, paramType = "query"),
            //uptUserKey
            @ApiImplicitParam(name = "user_key", value="(user_data 테이블 값)업데이트될 최종 키값", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_key_remain_time", value="(user_data 테이블 값)회원 키 키 충전 시간", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "user_key_last_charge_time", value="(user_data 테이블 값)회원 키 마지막 바뀐 시간", required = false, dataTypeClass = String.class, paramType = "query")
    })

    @ApiOperation(value="charge_key_log 테이블에 키 충전 로그 쌓고, user_data 테이블의 key관련된 데이터 다시 넣기")
    @PostMapping(value = "/insChargeKeyLog")
    public ResponseEntity<?> insChargeKeyLog(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        goodsService.insChargeKeyLog(map, request);

        ApiResponse response = new ApiResponse();

        return new ResponseEntity<>(response, response.getStatus());
    }
*/

    @ApiImplicitParams({
            //사용
            @ApiImplicitParam(name = "user_uuid", value="(공통)사용 유저의 uuid", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "use_key_count", value="(공통)키를 몇개 사용 했거나, 몇개 충전 되었거나", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="(공통)사용 기기값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "game_mode", value="(사용 테이블 use_key_log)열쇠 사용할때 게임 모드", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "game_level", value="(사용 테이블 use_key_log)열쇠 사용할때 게임 레벨", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "start_seq", value="(사용 테이블 use_key_log)game_start_data의 seq", required = false, dataTypeClass = Integer.class, paramType = "query"),
            //충전
            @ApiImplicitParam(name = "charge_reason", value="(충전 테이블 charge_key_log)자동 충전일때는 'auto'로 보내줘야함", required = false, dataTypeClass = String.class, paramType = "query")
    })

    @ApiOperation(value="열쇠 사용 및 충전 통합 api")
    @PostMapping(value = "/totalKeyUseAndCharge")
    public ResponseEntity<?> totalKeyUseAndCharge(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        String contentType = request.getHeader("Content-Type");
        if (contentType == null || !contentType.equals("application/json")) {
            return new ResponseEntity<>("Invalid Content-Type. Expected: application/json", HttpStatus.BAD_REQUEST);
        }

        return goodsService.totalKeyUseAndCharge(map, request);
    }

}
