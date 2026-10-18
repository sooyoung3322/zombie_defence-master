package com.goall.zombie_defence.controller.api.inGame;

import com.goall.zombie_defence.mapper.UnitUpgradeMapper;
import com.goall.zombie_defence.response.ApiResponse;
import com.goall.zombie_defence.service.UnitUpgradeService;
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
@RequestMapping("/unitUpgrade")
@RestController
public class UnitUpgradeController {

    @Autowired
    UnitUpgradeService unitUpgradeService;

    @ApiOperation(value="유닛 업그레이드 테이블 데이터 다 가져오기")
    @PostMapping(value = "/unitUpgradeData")
    @ResponseBody
    public ResponseEntity<?> unitUpgradeData(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return unitUpgradeService.unitUpgradeData(map, request);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="회원 guest uuid", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "upgrade_type", value="업그레이드 할 타입(ar, sp, sg)", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="사용기기", required = false, dataTypeClass = Integer.class, paramType = "query"),
    })

    @ApiOperation(value="개별 유닛 업그레이드 버튼 클릭")
    @PostMapping(value = "/unitUpgradeButton")
    public ResponseEntity<?> unitUpgradeButton(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return unitUpgradeService.unitUpgradeButton(map, request);

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="회원 guest uuid", required = false, dataTypeClass = String.class, paramType = "query"),
    })

    @ApiOperation(value="유저의 개별 강화 데이터값 가져오기")
    @PostMapping(value = "/userUnitUpgradeData")
    public ResponseEntity<?> userUnitUpgradeData(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return unitUpgradeService.userUnitUpgradeData(map, request);

    }

}
