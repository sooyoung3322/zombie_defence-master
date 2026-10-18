package com.goall.zombie_defence.controller.api.inGame;

import com.goall.zombie_defence.mapper.QuestMapper;
import com.goall.zombie_defence.service.OfflineModeService;
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
@RequestMapping("/offlineMode")
@RestController
public class OfflineModeController {

    @Autowired
    OfflineModeService offlineModeService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="정보 가져올 유저 uuid값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="유저의 오프라인 모드 몇개 재화 받을수있는지 확인 화면")
    @PostMapping(value = "/selectOfflineModeLog")
    @ResponseBody
    public ResponseEntity<?> selectOfflineModeLog(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return offlineModeService.selectOfflineModeLog(map, request);
    }

}
