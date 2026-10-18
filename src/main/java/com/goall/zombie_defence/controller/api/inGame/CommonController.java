package com.goall.zombie_defence.controller.api.inGame;

import com.goall.zombie_defence.service.CommonService;
import com.goall.zombie_defence.service.StatisticsService;
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
@RequestMapping("/common")
@RestController
public class CommonController {

    @Autowired
    CommonService commonService;

    @ApiOperation(value="DB 서버 시간, 게임 버전 가져오기")
    @PostMapping(value = "/nowDataTimeAndGameVersion")
    @ResponseBody
    public ResponseEntity<?> nowDataTimeAndGameVersion(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return commonService.nowDataTimeAndGameVersion(map);
    }

    @ApiOperation(value="게임 스테이지 등 이제 여기서 다 정보 가져올것")
    @PostMapping(value = "/gameCommonData")
    @ResponseBody
    public ResponseEntity<?> gameCommonData(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return commonService.gameCommonData(map);
    }

}
