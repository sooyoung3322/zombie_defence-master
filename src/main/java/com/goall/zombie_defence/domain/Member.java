package com.goall.zombie_defence.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class Member {
    private String industry1;
    private String industry2;
    private int businessNumber;
    private String representativeName;
    private int zipCode;
    private String addr1;
    private String addr2;
    private String managerName;
    private String managerEmail;
    private String password;
    private String phoneNumber;
    private String telNumber;
    private int birthday;
    private String useLanguage;
    private String terms1;
    private String terms2;
    private String terms3;
    private String useYn;
    private String regYn;
    private String regYnId; // 등록 신청 누가 받았는지
    private Date regYnDt; // 등록 신청 누가 받았는지
    private String regId; // 아이디 생성 누가 햇는지
    private Date regDt; //
    private String updId;
    private Date updDt;

}
