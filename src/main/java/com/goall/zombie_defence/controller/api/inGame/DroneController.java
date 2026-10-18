package com.goall.zombie_defence.controller.api.inGame;

import com.goall.zombie_defence.response.ApiResponse;
import com.goall.zombie_defence.service.EpicSkillService;
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
@RequestMapping("/epicSkill")
@RestController
public class DroneController {

    @Autowired
    EpicSkillService epicSkillService;


}
