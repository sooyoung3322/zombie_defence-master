package com.goall.zombie_defence.controller.api;

import com.goall.zombie_defence.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
//@RequestMapping("/member")
@Controller
public class MemberController {

    @Autowired
    MemberService memberService;

    @GetMapping(value ="/index")
    public String index(Model model){

        return "index";

    }

    @GetMapping(value ="/cafe_event")
    public String cafe_event(Model model){

        return "cafe_event";

    }

    @GetMapping("/login") // 로그인 페이지 요청 처리
    public String loginPage() {
        return "login"; // login.jsp 파일을 렌더링하는 View 이름 반환
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login";
    }

}
