package com.goall.zombie_defence.service;

import com.goall.commonUtil.CommonUtil;
import com.goall.zombie_defence.config.JwtProvider;
import com.goall.zombie_defence.mapper.*;
import com.goall.zombie_defence.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class GoodsService {

    @Autowired
    GoodsMapper goodsMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    CommonMapper commonMapper;

    @Autowired
    MailMapper mailMapper;

    @Autowired
    EpicSkillMapper epicSkillMapper;

    @Autowired
    SeasonPackageMapper seasonPackageMapper;


    @Transactional
    public ResponseEntity<?> totalKeyUseAndCharge(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        Map<String, Object> tokenMap = jwtProvider.userTokenCheck(map);
/*

        if(tokenMap.get("result").equals("false")) {

            resultMap.put("result", "false");
            resultMap.put("resultNote", "토큰 일치 안 함(중복 로그인임)");
            response.setBody(resultMap);

            return new ResponseEntity<>(response, response.getStatus());
        }
*/

        int user_key = (int) userMapper.selectUserData(map).get(0).get("user_key"); // 사용자 키 개수 가져오기

        int use_key_count = (int) map.get("use_key_count"); // 클라에서 보내준 사용한 키 개수

        //0.5초 기다리기
        /*Thread.sleep(700);*/

        // 이전에 실행된 시간을 가져와서, 일정 시간이 지나지 않으면 요청을 처리하지 않도록 합니다.
        Long previousTime = (Long) request.getSession().getAttribute("keyPreviousTime");
        Long keyCurrentTime = System.currentTimeMillis();
        if (previousTime != null && keyCurrentTime - previousTime < TimeUnit.SECONDS.toMillis(3)) {
            /*resultMap.put("result_key", user_key);
            response.setBody(resultMap);*/
            response.setResult("false");
            return new ResponseEntity<>(response, response.getStatus());
        }
        request.getSession().setAttribute("keyPreviousTime", keyCurrentTime);

        if(use_key_count < 0){ // 보내준 값이 음수라면(키를 사용 했다면)
            map.put("use_key_quantity", use_key_count); // DB에 넣기 위한 사용한 키 값 저장
            map.put("before_use_key_quantity", user_key); // DB에 넣기 위한 사용 전 키 값 저장
            map.put("after_use_key_quantity", user_key + use_key_count); // DB에 넣기 위한 사용 후 키 값 저장
            goodsMapper.insUseKeyLog(map); // 키 사용 로그 저장

            map.put("user_key", user_key + use_key_count); // user_data의 최종 키값 넣기

        }else{ // 보내준 값이 양수라면(구매 충전 및 자동 충전)
            map.put("charge_key_quantity", use_key_count); // DB에 넣기 위한 충전 키 값 저장
            map.put("before_charge_key_quantity", user_key); // DB에 넣기 위한 충전 전 키 값 저장
            map.put("after_charge_key_quantity", user_key + use_key_count); // DB에 넣기 위한 충전 후 키 값 저장
            goodsMapper.insChargeKeyLog(map); // 키 충전 로그 저장

            map.put("user_key", user_key + use_key_count); // user_data의 최종 키값 넣기

            if(map.get("charge_reason").equals("auto")){ // 자동 충전 이라면
                map.put("reason_auto", "Y"); // xml 조건문에 필요한 값
                map.put("user_key_remain_time", "60"); // xml 조건문에 필요한 값
            }
        }

        resultMap.put("result_key", user_key + use_key_count);

        goodsMapper.uptUserKey(map);

        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

        //map.put("ins_ip", CommonUtil.getRemoteIp(request));

        //goodsMapper.insUseKeyLog(map);
    }
    @Transactional
    public ResponseEntity<?> keyProductSale(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        //상품명 가져오기
        String product_eng_name = String.valueOf(map.get("product_eng_name"));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //유저 데이터값 가져오기
        List<HashMap<String, Object>> userResultList = userMapper.selectUserData(map);

        //결과값 클라에 보내주기 위해 세팅
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        //상품 구매로 인한 열쇠 충전인지 확인
        List<HashMap<String, Object>> productResultList = productMapper.selectProductData(map);

        int user_key = (int) userResultList.get(0).get("user_key");
        int user_ruby = (int) userResultList.get(0).get("user_ruby");
        int use_key_count = (int) productResultList.get(0).get("product_give_key");

        //상품 재화 뭘로 사용해서 구매하는지 값
        String product_goods_type = String.valueOf(productResultList.get(0).get("product_goods_type"));

        //이 상품 구매 가능한거임?
        String product_use_yn = String.valueOf(productResultList.get(0).get("product_use_yn"));
        if (product_use_yn.equals("N")) {
            response.setResult("product_use_yn_N");
            response.setMessage("구매할수 없는 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //뽑기에 필요한 재화가 충분한지 확인
        if (product_goods_type.equals("ruby")) {
            int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity"); // 상품 구매에 필요한 루비 개수
            if (user_ruby - product_goods_quantity < 0) {
                response.setResult("fail_ruby_lack");
                response.setMessage("루비가 부족합니다.");
                return new ResponseEntity<>(response, response.getStatus());
            }
        } else if (product_goods_type.equals("gold")) {
            //골드일때인데 지금은 골드로 못삼
        }

        String advertisement = ""; // 광고 확인
        String advertisement_type = ""; // 광고로 뽑고, 시간 초기화 하기 위해 mysql 쿼리 분기 처리(uptAdvertisementTimeData)
        //상품명에 광고 포함이면 광고 값 넣어주기
        if (product_eng_name.contains("advertisement")) {
            advertisement = "advertisement";
            advertisement_type = "game_key_50";
        }

        map.put("advertisement_type", advertisement_type);

        //상품 로그값 쌓기
        uniqueId =  product_eng_name + "_key_sale_" + advertisement + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);
        map.put("product_eng_name", product_eng_name);
        productMapper.insProductSaleLog(map);

        //키 충전때 필요한 유니크값 저장
        map.put("charge_key_unique_idx", uniqueId);

        map.put("charge_reason", product_eng_name);

        //루비 사용 계신
        //재화 사용 로그, 유저 데이터 재화 수정
        if((int) productResultList.get(0).get("product_goods_quantity") > 0) { // 보내준 값이 음수 or 0이라면(키를 사용 했다면)(0은 광고)

            //루비면은
            if (product_goods_type.equals("ruby")) {

                int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity"); // 상품 구매에 필요한 루비 개수

                map.put("use_ruby_quantity", product_goods_quantity); // DB에 넣기 위한 사용한 루비 값 저장
                map.put("before_use_ruby_quantity", user_ruby); // DB에 넣기 위한 사용 전 루비 값 저장
                map.put("after_use_ruby_quantity", user_ruby - product_goods_quantity); // DB에 넣기 위한 사용 후 루비 값 저장
                map.put("use_ruby_unique_idx", uniqueId); // DB에 넣기 위한 사용 후 루비 값 저장(경로 고유값 필수)
                map.put("advertisement_yn", "N");//광고아님

                goodsMapper.insUseRubyLog(map); // 루비 사용 로그 저장

                map.put("user_ruby", user_ruby - product_goods_quantity); // user_data의 최종 키값 넣기

                goodsMapper.uptUserRuby(map); // 사용자 루비 개수 수정

                resultMap.put("result_ruby", user_ruby - product_goods_quantity);

                //골드면은
            } else if (product_goods_type.equals("gold")) {
                //골드 구매 했을때인데 일단 없어서 비워둠
            }
        }else{
            /*0원 구매일때(광고) 어떻게 할지 장소 (일단 패스)*/
            map.put("advertisement_unique_idx", uniqueId);
            commonMapper.uptAdvertisementTimeData(map); // 광고 시간 초기화

            String user_skip_yn = String.valueOf(userResultList.get(0).get("advertisement_buy_yn"));
            map.put("skip_yn", user_skip_yn);
            commonMapper.insAdvertisementLog(map); // 광고 본거 로그

            resultMap.put("user_data", userMapper.selectUserData(map));
        }

        //상품 구매 개수 카운트하기
        productMapper.uptProductSaleCount(map);

        // 보내준 값이 양수라면(구매 충전 및 자동 충전)
        map.put("charge_key_quantity", use_key_count); // DB에 넣기 위한 충전 키 값 저장
        map.put("before_charge_key_quantity", user_key); // DB에 넣기 위한 충전 전 키 값 저장
        map.put("after_charge_key_quantity", user_key + use_key_count); // DB에 넣기 위한 충전 후 키 값 저장
        goodsMapper.insChargeKeyLog(map); // 키 충전 로그 저장

        map.put("user_key", user_key + use_key_count); // user_data의 최종 키값 넣기

        if(user_key + use_key_count >= 50){ // 자동 충전 이라면 or 구매하고 열쇠 총량이 50개(맥스치) 이상이면
            map.put("reason_auto", "Y"); // xml 조건문에 필요한 값
            map.put("user_key_remain_time", "60"); //시간 60초로 초기화
        }

        //키 결과값 클라에 뿌려주기위해
        resultMap.put("result_key", user_key + use_key_count);

        //유저 키 업데이트
        goodsMapper.uptUserKey(map);

        //결과값 맵 저장
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> goldProductSale(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        //상품명 가져오기
        String product_eng_name = String.valueOf(map.get("product_eng_name"));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //유저 데이터값 가져오기
        List<HashMap<String, Object>> userResultList = userMapper.selectUserData(map);

        //결과값 클라에 보내주기 위해 세팅
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> productResultList = productMapper.selectProductData(map);

        int user_key = (int) userResultList.get(0).get("user_key");
        int user_gold = (int) userResultList.get(0).get("user_gold");
        int user_ruby = (int) userResultList.get(0).get("user_ruby");

        int use_gold_count = (int) productResultList.get(0).get("product_give_gold");

        //상품 재화 뭘로 사용해서 구매하는지 값
        String product_goods_type = String.valueOf(productResultList.get(0).get("product_goods_type"));

        //이 상품 구매 가능한거임?
        String product_use_yn = String.valueOf(productResultList.get(0).get("product_use_yn"));
        if (product_use_yn.equals("N")) {
            response.setResult("product_use_yn_N");
            response.setMessage("구매할수 없는 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //뽑기에 필요한 재화가 충분한지 확인
        if (product_goods_type.equals("ruby")) {
            int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity"); // 상품 구매에 필요한 루비 개수
            if (user_ruby - product_goods_quantity < 0) {
                response.setResult("fail_ruby_lack");
                response.setMessage("루비가 부족합니다.");
                return new ResponseEntity<>(response, response.getStatus());
            }
        } else if (product_goods_type.equals("gold")) {
            //골드일때인데 지금은 골드로 못삼
        }

        String advertisement = ""; // 광고 확인
        //상품명에 광고 포함이면 광고 값 넣어주기
        if (product_eng_name.contains("advertisement")) {
            advertisement = "advertisement";
        }

        //상품 로그값 쌓기
        uniqueId =  product_eng_name + "_gold_sale_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);
        map.put("product_eng_name", product_eng_name);
        productMapper.insProductSaleLog(map);

        //루시 충전때 필요한 유니크값 저장
        map.put("charge_gold_unique_idx", uniqueId);

        map.put("charge_reason", product_eng_name);

        //루비 사용 계신
        //재화 사용 로그, 유저 데이터 재화 수정
        if((int) productResultList.get(0).get("product_goods_quantity") > 0) { // 보내준 값이 음수 or 0이라면(키를 사용 했다면)(0은 광고)

            //루비면은
            if (product_goods_type.equals("ruby")) {

                int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity"); // 상품 구매에 필요한 루비 개수

                map.put("use_ruby_quantity", product_goods_quantity); // DB에 넣기 위한 사용한 루비 값 저장
                map.put("before_use_ruby_quantity", user_ruby); // DB에 넣기 위한 사용 전 루비 값 저장
                map.put("after_use_ruby_quantity", user_ruby - product_goods_quantity); // DB에 넣기 위한 사용 후 루비 값 저장
                map.put("use_ruby_unique_idx", uniqueId); // DB에 넣기 위한 사용 후 루비 값 저장(경로 고유값 필수)
                map.put("advertisement_yn", "N");//광고아님

                goodsMapper.insUseRubyLog(map); // 루비 사용 로그 저장

                map.put("user_ruby", user_ruby - product_goods_quantity); // user_data의 최종 키값 넣기

                goodsMapper.uptUserRuby(map); // 사용자 루비 개수 수정

                resultMap.put("result_ruby", user_ruby - product_goods_quantity);

                //골드면은
            } else if (product_goods_type.equals("gold")) {
                //골드 구매 했을때인데 일단 없어서 비워둠
            }
        }else{
            /*0원 구매일때(광고) 어떻게 할지 장소 (일단 패스)*/
        }

        //상품 구매 개수 카운트하기
        productMapper.uptProductSaleCount(map);

        // 보내준 값이 양수라면(구매 충전 및 자동 충전)
        map.put("charge_gold_quantity", use_gold_count); // DB에 넣기 위한 충전 골드 값 저장
        map.put("before_charge_gold_quantity", user_gold); // DB에 넣기 위한 충전 전 골드값 저장
        map.put("after_charge_gold_quantity", user_gold + use_gold_count); // DB에 넣기 위한 충전 후 골드 값 저장
        goodsMapper.insChargeGoldLog(map); // 키 충전 로그 저장

        map.put("user_gold", user_gold + use_gold_count); // user_data의 최종 골드 값 넣기

        //키 결과값 클라에 뿌려주기위해
        resultMap.put("result_gold", user_gold + use_gold_count);

        //유저 키 업데이트
        goodsMapper.uptUserGold(map);

        //결과값 맵 저장
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> rubyProductSale(Map<String, Object> map, HttpServletRequest request) throws Exception {

        //※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※
        //※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※
        //※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※
        //※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※※중요※
        //여기는 웬만하면 루비 캐쉬 구매일때만 사용하는걸로//
        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        //상품명 가져오기
        String product_eng_name = String.valueOf(map.get("product_eng_name"));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //유저 데이터값 가져오기
        List<HashMap<String, Object>> userResultList = userMapper.selectUserData(map);

        //결과값 클라에 보내주기 위해 세팅
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> productResultList = productMapper.selectProductDataFirstBuyList(map);

        int user_ruby = (int) userResultList.get(0).get("user_ruby");
        int product_give_ruby = (int) productResultList.get(0).get("product_give_ruby");
        int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity");
        String first_buy = String.valueOf(productResultList.get(0).get("first_buy"));

        //첫번쨰 구매면 2배로 루비 주기 중요한거임 N이면 첫 구매임
        int event_bonus = 1;
        if(first_buy.equals("N")){
            event_bonus = 2;
        }

        //상품 재화 뭘로 사용해서 구매하는지 값
        String product_goods_type = String.valueOf(productResultList.get(0).get("product_goods_type"));

        //뽑기에 필요한 재화가 충분한지 확인
        if (!product_goods_type.equals("cash")) {
            response.setResult("not_cash");
            response.setMessage("현금으로만 구매 가능한 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //이 상품 구매 가능한거임?
        String product_use_yn = String.valueOf(productResultList.get(0).get("product_use_yn"));
        if (product_use_yn.equals("N")) {
            response.setResult("product_use_yn_N");
            response.setMessage("구매할수 없는 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //상품 로그값 쌓기
        uniqueId =  product_eng_name + "_ruby_sale_first_buy_" + first_buy + "_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);
        map.put("product_eng_name", product_eng_name);
        productMapper.insProductSaleLog(map);

        //캐쉬 얼마나 사용하였는지 로그 쌓기
        map.put("use_cash_unique_idx", uniqueId);
        map.put("use_cash_product_name", product_eng_name);
        map.put("use_cash_quantity", product_goods_quantity);
        productMapper.insUseCashLog(map);

        //루시 충전때 필요한 유니크값 저장
        map.put("charge_ruby_unique_idx", uniqueId);
        map.put("charge_reason", product_eng_name);

        //상품 구매 개수 카운트하기
        productMapper.uptProductSaleCount(map);

        // 보내준 값이 양수라면(구매 충전 및 자동 충전)
        map.put("charge_ruby_quantity", product_give_ruby * event_bonus); // DB에 넣기 위한 충전 루비 값 저장
        map.put("before_charge_ruby_quantity", user_ruby); // DB에 넣기 위한 충전 전 루비값 저장
        map.put("after_charge_ruby_quantity", user_ruby + (product_give_ruby * event_bonus) ); // DB에 넣기 위한 충전 후 루비 값 저장
        goodsMapper.insChargeRubyLog(map); // 루비 충전 로그 저장

        map.put("user_ruby", user_ruby + (product_give_ruby * event_bonus)); // user_data의 최종 루비 값 넣기

        //키 결과값 클라에 뿌려주기위해
        resultMap.put("result_ruby", user_ruby + (product_give_ruby * event_bonus));

        //유저 루비 업데이트
        goodsMapper.uptUserRuby(map);

        //결과값 맵 저장
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> expBoosterProduct(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        //상품명 가져오기
        String product_eng_name = String.valueOf(map.get("product_eng_name"));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //결과값 클라에 보내주기 위해 세팅
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> productResultList = productMapper.selectProductDataFirstBuyList(map);
        int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity");

        //첫 구매인가?
        String first_buy = String.valueOf(productResultList.get(0).get("first_buy"));

        //상품 재화 뭘로 사용해서 구매하는지 값
        String product_goods_type = String.valueOf(productResultList.get(0).get("product_goods_type"));

        //뽑기에 필요한 재화가 충분한지 확인
        if (!product_goods_type.equals("cash")) {
            response.setResult("not_cash");
            response.setMessage("현금으로만 구매 가능한 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //이 상품 구매 가능한거임?
        String product_use_yn = String.valueOf(productResultList.get(0).get("product_use_yn"));
        if (product_use_yn.equals("N")) {
            response.setResult("product_use_yn_N");
            response.setMessage("구매할수 없는 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //상품 로그값 쌓기
        uniqueId =  product_eng_name + "_exp_booster_2x_28day_" + first_buy + "_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);
        map.put("product_eng_name", product_eng_name);
        productMapper.insProductSaleLog(map);

        //캐쉬 얼마나 사용하였는지 로그 쌓기
        map.put("use_cash_unique_idx", uniqueId);
        map.put("use_cash_product_name", product_eng_name);
        map.put("use_cash_quantity", product_goods_quantity);
        productMapper.insUseCashLog(map);

        //N이면 첫 구매라서 인설트해주고 Y면 첫 구매가 아니기 때문에 있던 데이터에서 업데이트
        if(first_buy.equals("N")){
            productMapper.insExpBooster(map);
        }else{
            productMapper.uptExpBooster(map);
        }

        //상품 구매 개수 카운트하기
        productMapper.uptProductSaleCount(map);

        //넣은 데이트값 뿌려주기
        resultMap.put("expBoosterDate", productMapper.selectExpBooster(map));

        //결과값 맵 저장
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> startPack(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        //상품명 가져오기
        String product_eng_name = String.valueOf(map.get("product_eng_name"));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //결과값 클라에 보내주기 위해 세팅
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> productResultList = productMapper.selectProductDataFirstBuyList(map);
        int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity");

        //첫 구매인가?
        String first_buy = String.valueOf(productResultList.get(0).get("first_buy"));

        if(first_buy.equals("Y")){
            response.setResult("not_first_buy");
            response.setMessage("스타트 패키지는 한 번만 구매 가능합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //상품 재화 뭘로 사용해서 구매하는지 값
        String product_goods_type = String.valueOf(productResultList.get(0).get("product_goods_type"));

        //뽑기에 필요한 재화가 충분한지 확인
        if (!product_goods_type.equals("cash")) {
            response.setResult("not_cash");
            response.setMessage("현금으로만 구매 가능한 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //이 상품 구매 가능한거임?
        String product_use_yn = String.valueOf(productResultList.get(0).get("product_use_yn"));
        if (product_use_yn.equals("N")) {
            response.setResult("product_use_yn_N");
            response.setMessage("구매할수 없는 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //상품 로그값 쌓기
        uniqueId =  product_eng_name + "_start_pack_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);
        map.put("product_eng_name", product_eng_name);
        productMapper.insProductSaleLog(map);

        //캐쉬 얼마나 사용하였는지 로그 쌓기
        map.put("use_cash_unique_idx", uniqueId);
        map.put("use_cash_product_name", product_eng_name);
        map.put("use_cash_quantity", product_goods_quantity);
        productMapper.insUseCashLog(map);

        //상품 구매 개수 카운트하기
        productMapper.uptProductSaleCount(map);

        //상품 우편 넣어주기
        Map<String, Object> fromInsMail;
        fromInsMail = new HashMap<String, Object>();

        fromInsMail.put("user_uuid", map.get("user_uuid"));
        fromInsMail.put("all_mail_unique", uniqueId);
        fromInsMail.put("reward_unique", "start_pack");
        fromInsMail.put("mail_title", "스타트 패키지 구매");
        fromInsMail.put("mail_content", "스타트 패키지를 구매해 주셔서 감사합니다.");
        fromInsMail.put("mail_sender", "운영진");
        fromInsMail.put("mail_note", "스타트 패키지 구매");
        mailMapper.insUserAllMail(fromInsMail);

        //결과값 맵 저장
        //response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> advertisementSkipYn(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        //상품명 가져오기
        String product_eng_name = String.valueOf(map.get("product_eng_name"));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //결과값 클라에 보내주기 위해 세팅
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> productResultList = productMapper.selectProductDataFirstBuyList(map);
        int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity");

        //첫 구매인가?
        String first_buy = String.valueOf(productResultList.get(0).get("first_buy"));

        //상품 재화 뭘로 사용해서 구매하는지 값
        String product_goods_type = String.valueOf(productResultList.get(0).get("product_goods_type"));

        //뽑기에 필요한 재화가 충분한지 확인
        if (!product_goods_type.equals("cash")) {
            response.setResult("not_cash");
            response.setMessage("현금으로만 구매 가능한 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //이 상품 구매 가능한거임?
        String product_use_yn = String.valueOf(productResultList.get(0).get("product_use_yn"));
        if (product_use_yn.equals("N")) {
            response.setResult("product_use_yn_N");
            response.setMessage("구매할수 없는 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //Y이면 이미 구려 이력이있어서 또 구매 못하게 막아야함
        if(first_buy.equals("Y")){
            response.setResult("error");
            response.setMessage("광고 스킵은 한 번만 구매 가능합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //상품 로그값 쌓기
        uniqueId =  product_eng_name + "_advertisement_buy_yn_" + first_buy + "_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);
        map.put("product_eng_name", product_eng_name);
        productMapper.insProductSaleLog(map);

        //캐쉬 얼마나 사용하였는지 로그 쌓기
        map.put("use_cash_unique_idx", uniqueId);
        map.put("use_cash_product_name", product_eng_name);
        map.put("use_cash_quantity", product_goods_quantity);
        productMapper.insUseCashLog(map);

        //상품 구매 개수 카운트하기
        productMapper.uptProductSaleCount(map);

        //유저 정보에 광고 yn을 Y로 바꿔주기
        userMapper.uptUserAdvertisementSkipYn(map);

        //유저값 다시 가져와주기
        resultMap.put("expBoosterDate", userMapper.selectUserData(map));

        //결과값 맵 저장
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }


    public void insUseKeyLog(Map<String, Object> map, HttpServletRequest request) throws Exception {

        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        goodsMapper.insUseKeyLog(map);
    }

    @Transactional
    public void insChargeKeyLog(Map<String, Object> map, HttpServletRequest request) throws Exception {

        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        goodsMapper.uptUserKey(map);
        goodsMapper.insChargeKeyLog(map);
    }

    @Transactional
    public ResponseEntity<?> failSale(Map<String, Object> map, HttpServletRequest request, String memo) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        //상품명 가져오기
        String product_eng_name = String.valueOf(map.get("product_eng_name"));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //결과값 클라에 보내주기 위해 세팅
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        //상품 로그값 쌓기
        uniqueId =  "FAIL_" + product_eng_name + "_MEMO" + memo + "_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);
        map.put("product_eng_name", product_eng_name);

        resultMap.put("log", "오류");
        resultMap.put("memo", memo);

        response.setBody(resultMap);

        productMapper.insProductSaleLog(map);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> playerSkillPlus(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        //상품명 가져오기
        String product_eng_name = String.valueOf(map.get("product_eng_name"));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //결과값 클라에 보내주기 위해 세팅
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> productResultList = productMapper.selectProductDataFirstBuyList(map);
        int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity");

        //첫 구매인가?
        String first_buy = String.valueOf(productResultList.get(0).get("first_buy"));

        //상품 재화 뭘로 사용해서 구매하는지 값
        String product_goods_type = String.valueOf(productResultList.get(0).get("product_goods_type"));

        //뽑기에 필요한 재화가 충분한지 확인
        if (!product_goods_type.equals("cash")) {
            response.setResult("not_cash");
            response.setMessage("현금으로만 구매 가능한 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //이 상품 구매 가능한거임?
        String product_use_yn = String.valueOf(productResultList.get(0).get("product_use_yn"));
        if (product_use_yn.equals("N")) {
            response.setResult("product_use_yn_N");
            response.setMessage("구매할수 없는 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //Y이면 이미 구려 이력이있어서 또 구매 못하게 막아야함
        if(first_buy.equals("Y")){
            response.setResult("error");
            response.setMessage("에픽 스킬 포인트는 한 번씩만 구매 가능합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //상품 로그값 쌓기
        uniqueId =  product_eng_name + "_playerSkillPlus_" + first_buy + "_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);
        map.put("product_eng_name", product_eng_name);
        productMapper.insProductSaleLog(map);

        //캐쉬 얼마나 사용하였는지 로그 쌓기
        map.put("use_cash_unique_idx", uniqueId);
        map.put("use_cash_product_name", product_eng_name);
        map.put("use_cash_quantity", product_goods_quantity);
        productMapper.insUseCashLog(map);

        //상품 구매 개수 카운트하기
        productMapper.uptProductSaleCount(map);

        if(product_eng_name.equals("player_skill_plus_20")){
            map.put("plus_skill_point", 20);
        }else if (product_eng_name.equals("player_skill_plus_30")){
            map.put("plus_skill_point", 30);
        }else{
            map.put("plus_skill_point", 0);
        }

        //플레이어 스킬 그만큼 더해주기
        userMapper.uptUserepickSkillPlus(map);

        //유저값 다시 가져와주기
        resultMap.put("userData", userMapper.selectUserData(map));

        //결과값 맵 저장
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> playerSkillPack(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        //상품명 가져오기
        String product_eng_name = String.valueOf(map.get("product_eng_name"));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //결과값 클라에 보내주기 위해 세팅
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> productResultList = productMapper.selectProductDataFirstBuyList(map);
        int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity");

        //첫 구매인가?
        String first_buy = String.valueOf(productResultList.get(0).get("first_buy"));

        if(first_buy.equals("Y")){
            response.setResult("not_first_buy");
            response.setMessage("플레이어 스킬 패키지는 한 번만 구매 가능합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //상품 재화 뭘로 사용해서 구매하는지 값
        String product_goods_type = String.valueOf(productResultList.get(0).get("product_goods_type"));

        //뽑기에 필요한 재화가 충분한지 확인
        if (!product_goods_type.equals("cash")) {
            response.setResult("not_cash");
            response.setMessage("현금으로만 구매 가능한 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //이 상품 구매 가능한거임?
        String product_use_yn = String.valueOf(productResultList.get(0).get("product_use_yn"));
        if (product_use_yn.equals("N")) {
            response.setResult("product_use_yn_N");
            response.setMessage("구매할수 없는 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //상품 로그값 쌓기
        uniqueId =  product_eng_name + "_player_skill_pack_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);
        map.put("product_eng_name", product_eng_name);
        productMapper.insProductSaleLog(map);

        //캐쉬 얼마나 사용하였는지 로그 쌓기
        map.put("use_cash_unique_idx", uniqueId);
        map.put("use_cash_product_name", product_eng_name);
        map.put("use_cash_quantity", product_goods_quantity);
        productMapper.insUseCashLog(map);

        //상품 구매 개수 카운트하기
        productMapper.uptProductSaleCount(map);

        map.put("plus_skill_point", 20);

        //플레이어 스킬 그만큼 더해주기
        userMapper.uptUserepickSkillPlus(map);

        //상품 우편 넣어주기
        Map<String, Object> fromInsMail;
        fromInsMail = new HashMap<String, Object>();

        fromInsMail.put("user_uuid", map.get("user_uuid"));
        fromInsMail.put("all_mail_unique", uniqueId);
        fromInsMail.put("reward_unique", "player_skill_pack");
        fromInsMail.put("mail_title", "플레이어 스킬 패키지 구매");
        fromInsMail.put("mail_content", "플레이어 스킬 패키지를 구매해 주셔서 감사합니다.");
        fromInsMail.put("mail_sender", "운영진");
        fromInsMail.put("mail_note", "플레이어 스킬 패키지 구매");
        mailMapper.insUserAllMail(fromInsMail);

        //결과값 맵 저장
        //response.setBody(resultMap);

        //유저값 다시 가져와주기
        resultMap.put("userData", userMapper.selectUserData(map));

        //결과값 맵 저장
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> happy1000Pack(Map<String, Object> map, HttpServletRequest request) throws Exception {

       /* 1000_ruby_pack
        1000_gold_pack
        1000_key_pack*/
        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        //상품명 가져오기
        String product_eng_name = String.valueOf(map.get("product_eng_name"));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //결과값 클라에 보내주기 위해 세팅
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> productResultList = productMapper.selectProductDataFirstBuyList(map);
        int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity");

        //첫 구매인가?
        String first_buy = String.valueOf(productResultList.get(0).get("first_buy"));

        if(first_buy.equals("Y")){
            response.setResult("not_first_buy");
            response.setMessage(product_eng_name + "_는 한 번만 구매 가능합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //상품 재화 뭘로 사용해서 구매하는지 값
        String product_goods_type = String.valueOf(productResultList.get(0).get("product_goods_type"));

        //현금인지 확인
        if (!product_goods_type.equals("cash")) {
            response.setResult("not_cash");
            response.setMessage("현금으로만 구매 가능한 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //이 상품 구매 가능한거임?
        String product_use_yn = String.valueOf(productResultList.get(0).get("product_use_yn"));
        if (product_use_yn.equals("N")) {
            response.setResult("product_use_yn_N");
            response.setMessage("구매할수 없는 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //상품 로그값 쌓기
        uniqueId =  product_eng_name + "_start_pack_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);
        map.put("product_eng_name", product_eng_name);
        productMapper.insProductSaleLog(map);

        //캐쉬 얼마나 사용하였는지 로그 쌓기
        map.put("use_cash_unique_idx", uniqueId);
        map.put("use_cash_product_name", product_eng_name);
        map.put("use_cash_quantity", product_goods_quantity);
        productMapper.insUseCashLog(map);

        //상품 구매 개수 카운트하기
        productMapper.uptProductSaleCount(map);

        //상품 우편 넣어주기
        Map<String, Object> fromInsMail;
        fromInsMail = new HashMap<String, Object>();

        /* 1000_ruby_pack
        1000_gold_pack
        1000_key_pack*/

        fromInsMail.put("user_uuid", map.get("user_uuid"));
        fromInsMail.put("all_mail_unique", uniqueId);
        if(product_eng_name.equals("1000_ruby_pack")){
            fromInsMail.put("reward_unique", "1000_ruby_pack");
            fromInsMail.put("mail_title", "천원의 행복 루비 패키지 구매");
            fromInsMail.put("mail_content", "천원의 행복 루비 패키지를 구매해 주셔서 감사합니다.");
            fromInsMail.put("mail_note", "천원의 행복 루비 패키지 구매");
        }else if(product_eng_name.equals("1000_gold_pack")){
            fromInsMail.put("reward_unique", "1000_gold_pack");
            fromInsMail.put("mail_title", "천원의 행복 골드 패키지 구매");
            fromInsMail.put("mail_content", "천원의 행복 골드 패키지를 구매해 주셔서 감사합니다.");
            fromInsMail.put("mail_note", "천원의 행복 골드 패키지 구매");
        }else if(product_eng_name.equals("1000_key_pack")){
            fromInsMail.put("reward_unique", "1000_key_pack");
            fromInsMail.put("mail_title", "천원의 행복 열쇠 패키지 구매");
            fromInsMail.put("mail_content", "천원의 행복 열쇠 패키지를 구매해 주셔서 감사합니다.");
            fromInsMail.put("mail_note", "천원의 행복 열쇠 패키지 구매");
        }
        fromInsMail.put("mail_title", "천원의 행복 패키지 구매");

        mailMapper.insUserAllMail(fromInsMail);

        //결과값 맵 저장
        //response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> goldPack(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        //상품명 가져오기
        String product_eng_name = String.valueOf(map.get("product_eng_name"));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //결과값 클라에 보내주기 위해 세팅
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> productResultList = productMapper.selectProductDataFirstBuyList(map);
        int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity");

        //첫 구매인가?
        String first_buy = String.valueOf(productResultList.get(0).get("first_buy"));

        if(first_buy.equals("Y")){
            response.setResult("not_first_buy");
            response.setMessage("골드 패키지는 한 번만 구매 가능합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //상품 재화 뭘로 사용해서 구매하는지 값
        String product_goods_type = String.valueOf(productResultList.get(0).get("product_goods_type"));

        //뽑기에 필요한 재화가 충분한지 확인
        if (!product_goods_type.equals("cash")) {
            response.setResult("not_cash");
            response.setMessage("현금으로만 구매 가능한 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //이 상품 구매 가능한거임?
        String product_use_yn = String.valueOf(productResultList.get(0).get("product_use_yn"));
        if (product_use_yn.equals("N")) {
            response.setResult("product_use_yn_N");
            response.setMessage("구매할수 없는 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //상품 로그값 쌓기
        uniqueId =  product_eng_name + "_gold_pack_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);
        map.put("product_eng_name", product_eng_name);
        productMapper.insProductSaleLog(map);

        //캐쉬 얼마나 사용하였는지 로그 쌓기
        map.put("use_cash_unique_idx", uniqueId);
        map.put("use_cash_product_name", product_eng_name);
        map.put("use_cash_quantity", product_goods_quantity);
        productMapper.insUseCashLog(map);

        //상품 구매 개수 카운트하기
        productMapper.uptProductSaleCount(map);

        //상품 우편 넣어주기
        Map<String, Object> fromInsMail;
        fromInsMail = new HashMap<String, Object>();

        fromInsMail.put("user_uuid", map.get("user_uuid"));
        fromInsMail.put("all_mail_unique", uniqueId);
        fromInsMail.put("reward_unique", "gold_pack");
        fromInsMail.put("mail_title", "골드 패키지 구매");
        fromInsMail.put("mail_content", "골드 패키지를 구매해 주셔서 감사합니다.");
        fromInsMail.put("mail_sender", "운영진");
        fromInsMail.put("mail_note", "골드 패키지 구매");
        mailMapper.insUserAllMail(fromInsMail);

        //결과값 맵 저장
        //response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> boosEpicSkill(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        //상품명 가져오기
        String product_eng_name = String.valueOf(map.get("product_eng_name"));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        uniqueId =  product_eng_name + "_bp_epic_skill_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);
        map.put("product_eng_name", product_eng_name);

        //결과값 클라에 보내주기 위해 세팅
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> productResultList = productMapper.selectProductDataFirstBuyList(map);
        int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity");

        //이 상품 구매 가능한거임?
        String product_use_yn = String.valueOf(productResultList.get(0).get("product_use_yn"));
        if (product_use_yn.equals("N")) {
            response.setResult("product_use_yn_N");
            response.setMessage("구매할수 없는 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //유저 데이터값 가져오기
        List<HashMap<String, Object>> userResultList = userMapper.selectUserData(map);
        int user_bp = (int) userResultList.get(0).get("user_bp");

        //뽑기에 필요한 재화가 충분한지 확인
        if (user_bp - product_goods_quantity < 0) {
            response.setResult("fail_bp_lack");
            response.setMessage("보스포인트가 부족합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //인벤토리 공간 여유 있는지 확인
        int user_epic_bag_max = (int) userResultList.get(0).get("user_epic_bag_max"); // 사용자 에픽 스킬 가방 맥스치 수 가져오기
        int user_epic_skill_count = epicSkillMapper.userEpicSkillCount(map); // 사용자가 가지고있는 에픽스킬 수량
        int product_drawing_count = 1; // 이 에픽 스킬의 뽑기가 몇 뽑인지 확인(보스포인트 구매니 무조건 1개)
        int user_epic_result_count = user_epic_bag_max - user_epic_skill_count - product_drawing_count; // 인벤 창 음수 나오는지 계산
        if (user_epic_result_count < 0) {
            response.setResult("fail_epic_bag_lack");
            response.setMessage("에픽 스킬 가방 공간이 부족합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //에픽스킬 어떤거 구매했는지 정보 저장
        int product_give_epic_idx = (int) productResultList.get(0).get("product_give_epic_idx");
        int product_give_epic_level = (int) productResultList.get(0).get("product_give_epic_level");
        int product_give_epic_grade = (int) productResultList.get(0).get("product_give_epic_grade");


        //메인 맵에 넣어서 인설트 해준다.(에픽 스킬 바로 넣어주기)
        map.put("user_epic_skill_unique_idx", uniqueId);
        map.put("user_epic_skill_idx", product_give_epic_idx);
        map.put("user_epic_skill_grade", product_give_epic_grade);
        map.put("user_epic_skill_level", product_give_epic_level);
        epicSkillMapper.insUserEpicSkill(map);

        //상품 로그값 쌓기
        productMapper.insProductSaleLog(map);

        //상품 구매 개수 카운트하기
        productMapper.uptProductSaleCount(map);

        //보스 포인트 소모 로그//
        map.put("use_bp_unique_idx", uniqueId);
        map.put("use_bp_quantity", product_goods_quantity);
        map.put("before_use_bp_quantity", user_bp);
        map.put("after_use_bp_quantity", user_bp - product_goods_quantity);
        map.put("use_bp_log_note", "보스 포인트로 에픽스킬 구매");
        map.put("advertisement_yn", "N");//광고아님
        goodsMapper.insUseBpLog(map);

        //유저 보스 포인트 최종 업데이트 해야함
        map.put("user_bp", user_bp - product_goods_quantity);
        goodsMapper.uptUserBp(map);

        //결과값 맵 저장
        resultMap.put("result_bp", user_bp - product_goods_quantity);
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> boosGoods(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        //상품명 가져오기
        String product_eng_name = String.valueOf(map.get("product_eng_name"));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        uniqueId =  product_eng_name + "_bp_goods_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);
        map.put("product_eng_name", product_eng_name);

        //결과값 클라에 보내주기 위해 세팅
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> productResultList = productMapper.selectProductDataFirstBuyList(map);
        int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity");

        //이 상품 구매 가능한거임?
        String product_use_yn = String.valueOf(productResultList.get(0).get("product_use_yn"));
        if (product_use_yn.equals("N")) {
            response.setResult("product_use_yn_N");
            response.setMessage("구매할수 없는 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //유저 데이터값 가져오기
        List<HashMap<String, Object>> userResultList = userMapper.selectUserData(map);
        int user_bp = (int) userResultList.get(0).get("user_bp");
        int user_ruby = (int) userResultList.get(0).get("user_ruby");
        int user_gold = (int) userResultList.get(0).get("user_gold");

        //뽑기에 필요한 재화가 충분한지 확인
        if (user_bp - product_goods_quantity < 0) {
            response.setResult("fail_bp_lack");
            response.setMessage("보스포인트가 부족합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        if(product_eng_name.equals("boss_goods_ruby")){
            int product_give_ruby = (int) productResultList.get(0).get("product_give_ruby");

            map.put("charge_ruby_unique_idx", uniqueId); // 유니크값 들어간 이유
            map.put("charge_ruby_quantity", product_give_ruby); // DB에 넣기 위한 게임 결과 루비
            map.put("before_charge_ruby_quantity", user_ruby); // DB에 넣기 위한 충전 전 루비값 저장
            map.put("after_charge_ruby_quantity", user_ruby + product_give_ruby); // DB에 넣기 위한 충전 후 루비 값 저장
            goodsMapper.insChargeRubyLog(map); // 루비 충전 로그 저장

            map.put("user_ruby", user_ruby+ product_give_ruby); // user_data의 최종 루비 값 넣기

            resultMap.put("result_ruby", user_ruby+ product_give_ruby);//결과 클라에 주기
            resultMap.put("reward_goods", product_give_ruby);//보상 리워드값

            //유저 루비 업데이트
            goodsMapper.uptUserRuby(map);

        }else if(product_eng_name.equals("boss_goods_gold")){
            int product_give_gold = (int) productResultList.get(0).get("product_give_gold");

            //골드 충전값 계산
            map.put("charge_gold_unique_idx", uniqueId); // 유니크값 들어간 이유
            map.put("charge_gold_quantity", product_give_gold); // DB에 넣기 위한 게임 결과 골드
            map.put("before_charge_gold_quantity", user_gold); // DB에 넣기 위한 충전 전 골드값 저장
            map.put("after_charge_gold_quantity", user_gold + product_give_gold); // DB에 넣기 위한 충전 후 골드 값 저장
            goodsMapper.insChargeGoldLog(map); // 골드 충전 로그 저장

            map.put("user_gold", user_gold + product_give_gold); // user_data의 최종 골드 값 넣기

            resultMap.put("result_gold", user_gold + product_give_gold);//결과 클라에 주기
            resultMap.put("reward_goods", product_give_gold);//보상 리워드값

            //유저 골드 업데이트
            goodsMapper.uptUserGold(map);
        }

        //상품 구매 개수 카운트하기
        productMapper.uptProductSaleCount(map);

        //보스 포인트 소모 로그//
        map.put("use_bp_unique_idx", uniqueId);
        map.put("use_bp_quantity", product_goods_quantity);
        map.put("before_use_bp_quantity", user_bp);
        map.put("after_use_bp_quantity", user_bp - product_goods_quantity);
        map.put("use_bp_log_note", "보스 포인트로 에픽스킬 구매");
        map.put("advertisement_yn", "N");//광고아님
        goodsMapper.insUseBpLog(map);

        //유저 보스 포인트 최종 업데이트 해야함
        map.put("user_bp", user_bp - product_goods_quantity);
        goodsMapper.uptUserBp(map);

        //결과값 맵 저장
        resultMap.put("result_bp", user_bp - product_goods_quantity);
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> seasonPack(@NotNull Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        //상품명 가져오기
        String product_eng_name = String.valueOf(map.get("product_eng_name"));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        uniqueId =  product_eng_name + "seasonPack" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);
        map.put("product_eng_name", product_eng_name);

        //결과값 클라에 보내주기 위해 세팅
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> productResultList = productMapper.selectProductDataFirstBuyList(map);
        int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity");

        //1. 시즌당 구매 한번만 가능하게
        List<HashMap<String, Object>> selectSeasonNumberList = seasonPackageMapper.selectSeasonNumberList(map);
        int now_season_number = (int) selectSeasonNumberList.get(0).get("season_number");//현시즌 몇 시즌인지 확인
        map.put("season_number", now_season_number);

        //2. 시즌날짜에 안겹치는날 받을라고할때
        // 현재 날짜와 시간
        // 날짜 형식 정의
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime now = LocalDateTime.now();
        String startStr = String.valueOf(selectSeasonNumberList.get(0).get("season_start_date"));
        String endStr = String.valueOf(selectSeasonNumberList.get(0).get("season_end_date"));

        // 문자열을 LocalDateTime으로 변환
        LocalDateTime startDateTime = LocalDateTime.parse(startStr, formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(endStr, formatter);

        List<HashMap<String, Object>> selectUserSeasonPackageList = seasonPackageMapper.selectUserSeasonPackageList(map);
        int user_season_package_tier = (int) selectUserSeasonPackageList.get(0).get("season_package_tier");//유저 지금 시즌 몇 티어인지 확인

        // 현재 날짜 및 시간이 시즌 시작 및 종료 날짜 사이에 있는지 확인
        if (now.isBefore(startDateTime) || now.isAfter(endDateTime)) {
            response.setResult("false");
            response.setMessage("시즌 기간이 아닙니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        // 패키지 구매 티어 확인
        if(product_eng_name.equals("season_pack_tier_1")){
            if(user_season_package_tier == 1 || user_season_package_tier == 3){
                response.setResult("false");
                response.setMessage("이미 이번시즌 첫 번째 패키지는 구매했습니다.");
                return new ResponseEntity<>(response, response.getStatus());
            }
        }else if(product_eng_name.equals("season_pack_tier_2")){
            if(user_season_package_tier == 2 || user_season_package_tier == 3){
                response.setResult("false");
                response.setMessage("이미 이번시즌 두 번째 패키지는 구매했습니다.");
                return new ResponseEntity<>(response, response.getStatus());
            }
        }
        int season_package_tier = 0;
        if(product_eng_name.equals("season_pack_tier_1") && user_season_package_tier == 0){
            season_package_tier = 1;
        }else if(product_eng_name.equals("season_pack_tier_1") && user_season_package_tier == 2){
            season_package_tier = 3;
        }else if(product_eng_name.equals("season_pack_tier_2") && user_season_package_tier == 0){
            season_package_tier = 2;
        }else if(product_eng_name.equals("season_pack_tier_2") && user_season_package_tier == 1){
            season_package_tier = 3;
        }

        map.put("season_package_tier", season_package_tier);

        //상품 재화 뭘로 사용해서 구매하는지 값
        String product_goods_type = String.valueOf(productResultList.get(0).get("product_goods_type"));

        //뽑기에 필요한 재화가 충분한지 확인
        if (!product_goods_type.equals("cash")) {
            response.setResult("not_cash");
            response.setMessage("현금으로만 구매 가능한 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //이 상품 구매 가능한거임?
        String product_use_yn = String.valueOf(productResultList.get(0).get("product_use_yn"));
        if (product_use_yn.equals("N")) {
            response.setResult("product_use_yn_N");
            response.setMessage("구매할수 없는 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //캐쉬 얼마나 사용하였는지 로그 쌓기
        map.put("use_cash_unique_idx", uniqueId);
        map.put("use_cash_product_name", product_eng_name);
        map.put("use_cash_quantity", product_goods_quantity);
        productMapper.insUseCashLog(map);

        //상품 구매 개수 카운트하기
        productMapper.uptProductSaleCount(map);

        //상품 로그값 쌓기
        map.put("unique_idx", uniqueId);
        productMapper.insProductSaleLog(map);

        //상품 구매 완료
        seasonPackageMapper.uptUserSeasonPackageTier(map);

        resultMap.put("selectUserSeasonPackageList", seasonPackageMapper.selectUserSeasonPackageList(map));
        //결과값 맵 저장
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> epicSkill5RandomPack(Map<String, Object> map, HttpServletRequest request) throws Exception {
        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request)); // 유저 아이피 저장

        //상품명 가져오기
        String product_eng_name = String.valueOf(map.get("product_eng_name"));

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //결과값 클라에 보내주기 위해 세팅
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        List<HashMap<String, Object>> productResultList = productMapper.selectProductDataFirstBuyList(map);
        int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity");

        //첫 구매인가?
        String first_buy = String.valueOf(productResultList.get(0).get("first_buy"));

        if(first_buy.equals("Y")){
            response.setResult("not_first_buy");
            response.setMessage("에픽스킬 5성 랜덤 패키지는 한 번만 구매 가능합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //상품 재화 뭘로 사용해서 구매하는지 값
        String product_goods_type = String.valueOf(productResultList.get(0).get("product_goods_type"));

        //뽑기에 필요한 재화가 충분한지 확인
        if (!product_goods_type.equals("cash")) {
            response.setResult("not_cash");
            response.setMessage("현금으로만 구매 가능한 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //이 상품 구매 가능한거임?
        String product_use_yn = String.valueOf(productResultList.get(0).get("product_use_yn"));
        if (product_use_yn.equals("N")) {
            response.setResult("product_use_yn_N");
            response.setMessage("구매할수 없는 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //상품 로그값 쌓기
        uniqueId =  product_eng_name + "epic_skill_5_random_pack" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        map.put("unique_idx", uniqueId);
        map.put("product_eng_name", product_eng_name);
        productMapper.insProductSaleLog(map);

        //캐쉬 얼마나 사용하였는지 로그 쌓기
        map.put("use_cash_unique_idx", uniqueId);
        map.put("use_cash_product_name", product_eng_name);
        map.put("use_cash_quantity", product_goods_quantity);
        productMapper.insUseCashLog(map);

        //상품 구매 개수 카운트하기
        productMapper.uptProductSaleCount(map);

        //보상 넣어주기
        //랜덤 5성 가져오기
        List<HashMap<String, Object>> buyEpicSkill5Random = epicSkillMapper.buyEpicSkill5Random(map);

        map.put("user_epic_skill_unique_idx", uniqueId); // 유니크값 들어간 이유
        int reward_item_idx = (int) buyEpicSkill5Random.get(0).get("epic_skill_idx");
        int reward_item_level = 1;
        int reward_item_grade = (int) buyEpicSkill5Random.get(0).get("epic_skill_grade");
        map.put("user_epic_skill_idx", reward_item_idx); // DB에 넣기 위한 아이템 SEQ값(쭝요)
        map.put("user_epic_skill_level", reward_item_level); // DB에 넣기 위한 아이템 레벨 값
        map.put("user_epic_skill_grade", reward_item_grade); // DB에 넣기 위한 아이템 등급 값
        resultMap.put("user_epic_skill_idx", reward_item_idx);//결과 클라에 주기
        resultMap.put("user_epic_skill_level", reward_item_level);//결과 클라에 주기
        resultMap.put("user_epic_skill_grade", reward_item_grade);//결과 클라에 주기

        epicSkillMapper.insUserEpicSkill(map);
        //보너스 루비 넣어주기

        //유저 데이터값 가져오기
        List<HashMap<String, Object>> userResultList = userMapper.selectUserData(map);

        int user_ruby = (int) userResultList.get(0).get("user_ruby");
        int product_give_ruby = (int) productResultList.get(0).get("product_give_ruby");

        //루시 충전때 필요한 유니크값 저장
        map.put("charge_ruby_unique_idx", uniqueId);
        map.put("charge_reason", product_eng_name);

        // 보내준 값이 양수라면(구매 충전 및 자동 충전)
        map.put("charge_ruby_quantity", product_give_ruby); // DB에 넣기 위한 충전 루비 값 저장
        map.put("before_charge_ruby_quantity", user_ruby); // DB에 넣기 위한 충전 전 루비값 저장
        map.put("after_charge_ruby_quantity", user_ruby + product_give_ruby ); // DB에 넣기 위한 충전 후 루비 값 저장
        goodsMapper.insChargeRubyLog(map); // 루비 충전 로그 저장

        map.put("user_ruby", user_ruby + product_give_ruby); // user_data의 최종 루비 값 넣기

        //키 결과값 클라에 뿌려주기위해

        resultMap.put("reward_goods_ruby", product_give_ruby);
        resultMap.put("user_ruby", user_ruby + product_give_ruby);

        //유저 루비 업데이트
        goodsMapper.uptUserRuby(map);

        //결과값 맵 저장
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    }

}