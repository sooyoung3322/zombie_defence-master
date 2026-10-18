package com.goall.zombie_defence.controller.api.inGame;

import com.goall.zombie_defence.service.ProductService;
import com.goall.zombie_defence.service.SeasonPackageService;
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
@RequestMapping("/selectSeason")
@RestController
public class SeasonPackageController {

    @Autowired
    SeasonPackageService seasonPackageService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="유저의 uuid값", required = false, dataTypeClass = String.class, paramType = "query"),
    })
    @ApiOperation(value="시즌 패키지 정보 다 가져오기")
    @PostMapping(value = "/selectSeasonPackageList")
    @ResponseBody
    public ResponseEntity<?> selectSeasonPackageList(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return seasonPackageService.selectSeasonPackageList(map);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="유저값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "season_number", value="몇시즌 ", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "season_package_tier", value="패스티어", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "season_package_level", value="패스 레벨", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "user_token", value="유저토큰", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="기기값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="시즌보상받기")
    @PostMapping(value = "/insUserSeasonPackageInsertData")
    @ResponseBody
    public ResponseEntity<?> insUserSeasonPackageInsertData(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return seasonPackageService.insUserSeasonPackageInsertData(map, request);
    }

}
