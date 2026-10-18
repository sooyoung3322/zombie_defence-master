package com.goall.zombie_defence.controller.api.admin;

import com.goall.zombie_defence.response.ApiResponse;
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
@RequestMapping("/adminPage/user")
@RestController
public class UserAdminPageController {

    @Autowired
    UserService userService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_nickname", value="회원 닉네임", required = false, dataTypeClass = String.class, paramType = "query"),
    })
    @ApiOperation(value="닉네임 유저 검색(관리자 페이지용)")
    @PostMapping(value = "/adminPageNicknameSearch")
    @ResponseBody
    public ResponseEntity<?> adminPageNicknameSearch(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return userService.adminPageNicknameSearch(map, request);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="회원 uuid", required = false, dataTypeClass = String.class, paramType = "query"),
    })

    @ApiOperation(value="카페 가입 이벤트")
    @PostMapping(value = "/adminPageInsCafeEvent")
    public ResponseEntity<?> adminPageInsCafeEvent(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        userService.adminPageInsCafeEvent(map, request);

        ApiResponse response = new ApiResponse();

        return new ResponseEntity<>(response, response.getStatus());
    }


}
