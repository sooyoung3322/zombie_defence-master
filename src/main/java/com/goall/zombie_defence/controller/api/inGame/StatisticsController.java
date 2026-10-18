package com.goall.zombie_defence.controller.api.inGame;

import com.goall.zombie_defence.response.ApiResponse;
import com.goall.zombie_defence.service.InGameService;
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
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/statistics")
@RestController
public class StatisticsController {

    @Autowired
    StatisticsService statisticsService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "game_mode", value="게임모드", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "game_level", value="게임레벨", required = false, dataTypeClass = String.class, paramType = "query"),
    })
    @ApiOperation(value="게임 점수 랭크 리스트(탑 10)")
    @PostMapping(value = "/totalGameRanking")
    @ResponseBody
    public ResponseEntity<?> totalGameRanking(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return statisticsService.totalGameRanking(map);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "game_mode", value="게임모드", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "game_level", value="게임레벨", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "user_uuid", value="검색할 유저 uuid", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="나의 최고 점수 리스트 검색")
    @PostMapping(value = "/myTotalGameRanking")
    @ResponseBody
    public ResponseEntity<?> myTotalGameRanking(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return statisticsService.myTotalGameRanking(map);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="검색할 유저 uuid", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="렙 랭킹 정보")
    @PostMapping(value = "/totalLevelRanking")
    @ResponseBody
    public ResponseEntity<?> totalLevelRanking(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return statisticsService.totalLevelRanking(map);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="검색할 유저 uuid", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="기기값", required = false, dataTypeClass = String.class, paramType = "query")

    })
    @ApiOperation(value="저번주 주간 보상 받기")
    @PostMapping(value = "/myLastWeekTotalGameRanking")
    @ResponseBody
    public ResponseEntity<?> myLastWeekTotalGameRanking(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return statisticsService.myLastWeekTotalGameRanking(map, request);

    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="검색할 유저 uuid", required = false, dataTypeClass = String.class, paramType = "query"),

    })
    @ApiOperation(value="저번주 주간 보상 받을 수 있는지 유무")
    @PostMapping(value = "/gameRankRewardLogList")
    @ResponseBody
    public ResponseEntity<?> gameRankRewardLogList(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return statisticsService.gameRankRewardLogList(map, request);

    }


}
