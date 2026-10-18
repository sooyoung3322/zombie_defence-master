package com.goall.zombie_defence.service;

import com.goall.zombie_defence.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MemberService {

    @Autowired
    MemberMapper memberMapper;

    public List<HashMap<String, Object>> selectUser() {
        return memberMapper.selectUser();
    }

}