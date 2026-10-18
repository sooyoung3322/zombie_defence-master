package com.goall.zombie_defence.service;

import com.goall.commonUtil.CommonUtil;
import com.goall.zombie_defence.config.JwtProvider;
import com.goall.zombie_defence.mapper.EpicSkillMapper;
import com.goall.zombie_defence.mapper.GoodsMapper;
import com.goall.zombie_defence.mapper.ProductMapper;
import com.goall.zombie_defence.mapper.UserMapper;
import com.goall.zombie_defence.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;

@RequiredArgsConstructor
@Service
public class ProductService {

    @Value("${live-or-test}")
    private String liveOrTest;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    EpicSkillService epicSkillService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    InAppPurchaseService inAppPurchaseService;

    @Value("${IosSharedSecret}")
    private String IosSharedSecret;


    public ResponseEntity<?> selectProductDataList(Map<String, Object> map) throws Exception {

        ApiResponse response = new ApiResponse(HttpStatus.OK, productMapper.selectProductDataList(map));

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> productBuy(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request));


        JSONObject jsonObj = new JSONObject(map);

/*

        System.out.println("####################" + jsonObj.toString());
        System.out.println("===========================================");
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%" + map.get("receipt"));
*/

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        Map<String, Object> tokenMap = jwtProvider.userTokenCheck(map);

        if(tokenMap.get("result").equals("false")) {

            resultMap.put("result", "false");
            resultMap.put("resultNote", "토큰 일치 안 함(중복 로그인임)");
            response.setBody(resultMap);

            return new ResponseEntity<>(response, response.getStatus());
        }

        //어떤 상품임? 누가 구매함????????
        String productName = String.valueOf(map.get("product_eng_name"));
        String user_uuid = String.valueOf(map.get("user_uuid"));
        String ins_machine = String.valueOf(map.get("ins_machine"));

        //다른 클래스에 보내줄 맵 새로 파주고
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("product_eng_name", productName);
        productMap.put("user_uuid", user_uuid);
        productMap.put("ins_machine", ins_machine);

        //유니크값 만들기
        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //상품 구매했을때 혹시몰라 구매 시작 로그를 쌓음
        Map<String, Object> startSaleproductMap = new HashMap<>();
        uniqueId =  "SALE_START_" + productName + "_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);//키 상품 유니크 값
        startSaleproductMap.put("product_eng_name", "sale_start_" + productName);
        startSaleproductMap.put("unique_idx", uniqueId);
        startSaleproductMap.put("user_uuid", user_uuid);
        startSaleproductMap.put("ins_ip", CommonUtil.getRemoteIp(request));
        startSaleproductMap.put("ins_machine", ins_machine);
        productMapper.insProductSaleLog(startSaleproductMap);

        boolean isValidPurchase = false;

        String platform = String.valueOf(map.get("platform"));

        //플랫폼 ios인지 확인
        productMap.put("platform", "ios");

        if(!platform.equals("ios")){
            productMap.put("platform", "aos");
        }
        String APPLE_VERIFY_RECEIPT_URL;

        //결제주소가 테스트, 라이브랑 달라서 구분
        if(liveOrTest.equals("test")) {
            APPLE_VERIFY_RECEIPT_URL = "https://sandbox.itunes.apple.com/verifyReceipt";
        }else {
            APPLE_VERIFY_RECEIPT_URL = "https://buy.itunes.apple.com/verifyReceipt";
        }

        if(liveOrTest.equals("test")){
            isValidPurchase = true;
        }else {

            if (map.get("receipt") != null) {
                // "receipt" 키가 있고, 해당하는 값이 null이 아닌 경우 실행할 코드

                // 구매 유효성 검사
                try {

                    if(platform.equals("ios")){


                        String receiptData = String.valueOf(map.get("receipt"));

                        RestTemplate restTemplate = new RestTemplate();
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);

                        // VerifyReceipt API 호출을 위한 요청 객체 생성
                        String requestBody = "{\"receipt-data\":\"" + receiptData + "\",\"password\":\"" + IosSharedSecret + "\"}";
                        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

                        // VerifyReceipt API 호출
                        ResponseEntity<String> response2 = restTemplate.exchange(APPLE_VERIFY_RECEIPT_URL, HttpMethod.POST, requestEntity, String.class);

/*
                        String receiptData = String.valueOf(map.get("receipt"));

                        // VerifyReceipt API 호출을 위한 요청 객체 생성
                        Map<String, Object> requestMap = new HashMap<>();
                        requestMap.put("receipt-data", receiptData);
                        requestMap.put("password", IosSharedSecret); // Apple Developer 계정의 Shared Secret 값으로 대체

                        // VerifyReceipt API 호출
                        RestTemplate restTemplate = new RestTemplate();
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestMap, headers);
                        ResponseEntity<String> response2 = restTemplate.exchange(APPLE_VERIFY_RECEIPT_URL, HttpMethod.POST, requestEntity, String.class);
*/

                        // 영수증 검증 결과 확인
                        if (response2.getStatusCode().is2xxSuccessful()) {
                            String responseBody = response2.getBody();
                            // 검증 결과를 확인하고 적절한 처리 수행
                            // responseBody를 JSON으로 파싱하여 필요한 정보를 추출할 수 있습니다.
                            /*return goodsService.successSale(productMap, request, "영수증 검증 성공");*/
                            isValidPurchase = true;

                        } else {
                            // 검증 실패
                            return goodsService.failSale(productMap, request, "영수증 검증 실패");
                        }
                    }

                    else {

                        String receipt = String.valueOf(map.get("receipt"));
                        JSONObject jsonObj1 = new JSONObject(receipt);

                        String Payload = String.valueOf(jsonObj1.get("Payload"));
                        JSONObject jsonObj2 = new JSONObject(Payload);

                        String json = String.valueOf(jsonObj2.get("json"));
                        JSONObject jsonObj3 = new JSONObject(json);

                        String packageName = String.valueOf(jsonObj3.get("packageName"));
                        String productId = String.valueOf(jsonObj3.get("productId"));
                        String purchaseToken = String.valueOf(jsonObj3.get("purchaseToken"));

                        Map<String, Object> receiptRequest = new HashMap<>();
                        receiptRequest.put("packageName", packageName);
                        receiptRequest.put("productId", productId);
                        receiptRequest.put("purchaseToken", purchaseToken);


                        isValidPurchase = inAppPurchaseService.googleInAppPurchaseVerify(receiptRequest);
                    }

                    if (!isValidPurchase) {
                        // 유효하지 않은 구매일 경우 처리
                        return goodsService.failSale(productMap, request, "유효하지않음");
                    }
                } catch (GeneralSecurityException | IOException e) {
                    // 예외 처리
                    return goodsService.failSale(productMap, request, "결제정보확인중 예외처리");

                }
            }
        }

        //ios는 "_ios"뺴고 넘겨야함
        
        productName = productName.replaceAll("_ios$", "");
        productMap.put("product_eng_name", productName);


        try {
            //어 이런 상품 구매 했어, 분기 처리
            //에픽 스킬 뽑기
            if (productName.equals("epic_skill_normal_1") || productName.equals("epic_skill_normal_11") || productName.equals("epic_skill_normal_1_advertisement") || productName.equals("epic_skill_advanced_1") || productName.equals("epic_skill_advanced_1_advertisement") || productName.equals("epic_skill_advanced_11")) {
                return epicSkillService.insUserEpicSkill(productMap, request);
            }

            //열쇠 구매
            if (productName.equals("game_key_20") || productName.equals("game_key_50_advertisement") || productName.equals("game_key_100")) {
                return goodsService.keyProductSale(productMap, request);
            }

            //골드 구매
            if (productName.equals("gold_6000") || productName.equals("gold_18000") || productName.equals("gold_72000")) {
                return goodsService.goldProductSale(productMap, request);
            }

            //루비 구매(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)
            if (productName.equals("ruby_42000") || productName.equals("ruby_19000") || productName.equals("ruby_7000") || productName.equals("ruby_3200") || productName.equals("ruby_1500") || productName.equals("ruby_250")) {

                if (!isValidPurchase) {
                    // 유효하지 않은 구매일 경우 처리
                    return goodsService.failSale(productMap, request, "유효하지않음");
                }

                return goodsService.rubyProductSale(productMap, request);
            }

            //경험치 부스터 구매(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)
            if (productName.equals("exp_booster_2x_28day")) {
                if (!isValidPurchase) {
                    // 유효하지 않은 구매일 경우 처리
                    return goodsService.failSale(productMap, request, "유효하지않음");
                }
                return goodsService.expBoosterProduct(productMap, request);
            }

            //광고 무제한 스킵권 구매(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)
            if (productName.equals("advertisement_skip_yn")) {
                if (!isValidPurchase) {
                    // 유효하지 않은 구매일 경우 처리
                    return goodsService.failSale(productMap, request, "유효하지않음");
                }
                return goodsService.advertisementSkipYn(productMap, request);
            }

            //광고 무제한 스킵권 구매(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)
            if (productName.equals("player_skill_plus_20") || productName.equals("player_skill_plus_30")) {
                if (!isValidPurchase) {
                    // 유효하지 않은 구매일 경우 처리
                    return goodsService.failSale(productMap, request, "유효하지않음");
                }
                return goodsService.playerSkillPlus(productMap, request);
            }

            //광고 무제한 스킵권 구매(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)
            if (productName.equals("player_skill_pack")) {
                if (!isValidPurchase) {
                    // 유효하지 않은 구매일 경우 처리
                    return goodsService.failSale(productMap, request, "유효하지않음");
                }
                return goodsService.playerSkillPack(productMap, request);
            }

            //골드 패키지(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)
            if (productName.equals("gold_pack")) {
                if (!isValidPurchase) {
                    // 유효하지 않은 구매일 경우 처리
                    return goodsService.failSale(productMap, request, "유효하지않음");
                }
                return goodsService.goldPack(productMap, request);
            }

            //골드 패키지(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)
            if (productName.equals("season_pack_tier_1") || productName.equals("season_pack_tier_2")) {
                if (!isValidPurchase) {
                    // 유효하지 않은 구매일 경우 처리
                    return goodsService.failSale(productMap, request, "유효하지않음");
                }
                return goodsService.seasonPack(productMap, request);
            }

            //골드 패키지(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)
            if (productName.equals("epic_skill_5_random_pack")) {
                if (!isValidPurchase) {
                    // 유효하지 않은 구매일 경우 처리
                    return goodsService.failSale(productMap, request, "유효하지않음");
                }
                return goodsService.epicSkill5RandomPack(productMap, request);
            }

            //천원의 행복(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)
            if (productName.equals("1000_ruby_pack") || productName.equals("1000_gold_pack") || productName.equals("1000_key_pack")) {
                if (!isValidPurchase) {
                    // 유효하지 않은 구매일 경우 처리
                    return goodsService.failSale(productMap, request, "유효하지않음");
                }
                return goodsService.happy1000Pack(productMap, request);
            }

            if(productName.contains("boss_epic_skill")){
                return goodsService.boosEpicSkill(productMap, request);
            }

            if(productName.contains("boss_goods")){
                return goodsService.boosGoods(productMap, request);
            }

            //스타트 패키지(우편으로 아이템 주는 패키지임)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)(#####유료결제#####)
            if (productName.equals("start_pack")) {
                if (!isValidPurchase) {
                    // 유효하지 않은 구매일 경우 처리
                    return goodsService.failSale(productMap, request, "유효하지않음");
                }
                return goodsService.startPack(productMap, request);
            } else {
                /*return "success";*/
                return goodsService.failSale(productMap, request, "스타트패키지예외처리");
            }
        }catch(Exception e1) {

            return goodsService.failSale(productMap, request, "모든 상품 예외 처리");

        }
    }
}