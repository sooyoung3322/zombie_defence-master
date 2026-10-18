package com.goall.zombie_defence.service;

import com.goall.commonUtil.CommonUtil;
import com.goall.zombie_defence.config.JwtProvider;
import com.goall.zombie_defence.mapper.*;
import com.goall.zombie_defence.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class EpicSkillService {

    @Autowired
    EpicSkillMapper epicSkillMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    GoodsMapper goodsMapper;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    CommonMapper commonMapper;
    @Autowired
    private QuestMapper questMapper;

    public ResponseEntity<?> selectEpicSkillData(Map<String, Object> map) throws Exception {

        ApiResponse response = new ApiResponse(HttpStatus.OK, epicSkillMapper.selectEpicSkillData(map));

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> totalUserEpicSkillData(Map<String, Object> map) throws Exception {


        ApiResponse response = new ApiResponse();

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        Map<String, Object> tokenMap = jwtProvider.userTokenCheck(map);

        if(tokenMap.get("result").equals("false")) {

            resultMap.put("result", "false");
            resultMap.put("resultNote", "토큰 일치 안 함(중복 로그인임)");
            response.setBody(resultMap);

            return new ResponseEntity<>(response, response.getStatus());
        }

        response = new ApiResponse(HttpStatus.OK, epicSkillMapper.totalUserEpicSkillData(map));

        return new ResponseEntity<>(response, response.getStatus());

    }

    public ResponseEntity<?> upgradeSelectUserEpicSkill(Map<String, Object> map) throws Exception {

        ApiResponse response = new ApiResponse(HttpStatus.OK, epicSkillMapper.upgradeSelectUserEpicSkill(map));

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public ResponseEntity<?> insUserEpicSkill(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();
        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        //고유 랜덤값 설정
        String uniqueId = "";
        String uniqueId2 = "";//자꾸 중첩되서 그냥 하나 더 만듬
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId2 = sdf.format(dateTime.getTime());

        //뽑기 결과 값 클라에 뿌려주기위한 초기 세팅
        List<Map<String, Object>> epicResultList = new ArrayList<Map<String, Object>>();
        //각종 결과값 뿌려주기
        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        //어떤 에픽 뽑기를 했는지 확인
        List<HashMap<String, Object>> productResultList = productMapper.selectProductData(map);
        //상품 이름
        String product_eng_name = String.valueOf(productResultList.get(0).get("product_eng_name"));

        //이 상품 구매 가능한거임?
        String product_use_yn = String.valueOf(productResultList.get(0).get("product_use_yn"));
        if (product_use_yn.equals("N")) {
            response.setResult("product_use_yn_N");
            response.setMessage("구매할수 없는 상품입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //유저 재화 정보 가져오기
        List<HashMap<String, Object>> userResultMap = userMapper.selectUserData(map);
        int user_ruby = (int) userResultMap.get(0).get("user_ruby"); // 사용자 루비 개수 가져오기
        int user_gold = (int) userResultMap.get(0).get("user_gold"); // 사용자 골드 개수 가져오기

        //뽑기에 필요한 재화가 충분한지 확인
        String product_goods_type = String.valueOf(productResultList.get(0).get("product_goods_type"));
        if (product_goods_type.equals("ruby")) {
            int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity"); // 상품 구매에 필요한 루비 개수
            if (user_ruby - product_goods_quantity < 0) {
                response.setResult("fail_ruby_lack");
                response.setMessage("루비가 부족합니다.");
                return new ResponseEntity<>(response, response.getStatus());
            }
        } else if (product_goods_type.equals("gold")) {
            int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity"); // 상품 구매에 필요한 루비 개수
            if (user_gold - product_goods_quantity < 0) {
                response.setResult("fail_gold_lack");
                response.setMessage("골드가 부족합니다.");
                return new ResponseEntity<>(response, response.getStatus());
            }
        }

        //인벤토리 공간 여유 있는지 확인
        int user_epic_bag_max = (int) userResultMap.get(0).get("user_epic_bag_max"); // 사용자 에픽 스킬 가방 맥스치 수 가져오기
        int user_epic_skill_count = epicSkillMapper.userEpicSkillCount(map); // 사용자가 가지고있는 에픽스킬 수량
        int product_drawing_count = (int) productResultList.get(0).get("product_drawing_count"); // 이 에픽 스킬의 뽑기가 몇 뽑인지 확인
        int user_epic_result_count = user_epic_bag_max - user_epic_skill_count - product_drawing_count; // 인벤 창 음수 나오는지 계산
        if (user_epic_result_count < 0) {
            response.setResult("fail_epic_bag_lack");
            response.setMessage("에픽 스킬 가방 공간이 부족합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //노말 뽑긴지 고급 뽑긴지 나누자
        String productType = ""; //노말인지 고급인지
        int epicSkillGrade = 0; // 노말은 0부터 시작 고급은 1부터 시작
        String advertisement = ""; // 광고 확인
        String advertisement_type = ""; // 광고로 뽑고, 시간 초기화 하기 위해 mysql 쿼리 분기 처리(uptAdvertisementTimeData)
        String bonus = "";
        //상품명에 광고 포함이면 광고 값 넣어주기
        if (product_eng_name.contains("advertisement")) {
            advertisement = "advertisement";
        }

        //유니크값 저장, 일반, 고급 뽑기 분기처리를 위한 값 저장
        if (product_eng_name.contains("epic_skill_normal")) {
            productType = "normal";
            advertisement_type = "epic_skill_normal";
            //advanced_epic_skill

        } else if (product_eng_name.contains("epic_skill_advanced")) {
            productType = "advanced";
            advertisement_type = "epic_skill_advanced";
            epicSkillGrade = 1;
        }
        map.put("advertisement_type", advertisement_type);
        //상품 이름에 따른 분기 처리(※포함 조건문임 주의)
        //노말 뽑기 확률 가져온다.
        map.put("epic_skill_draw_type", productType);
        List<HashMap<String, Object>> epicSkillList = epicSkillMapper.epicSkillDrawSelect(map);

        //각 등급별로 모두 더한값을 가져와서 가지고있는다.
        double epicGatcha1 = (double) epicSkillList.get(0).get(productType + "_grade_" + (epicSkillGrade + 1) + "_sum"); /*63.8000020980834*/
        double epicGatcha2 = (double) epicSkillList.get(0).get(productType + "_grade_" + (epicSkillGrade + 2) + "_sum"); /*31.1999597549438*/
        double epicGatcha3 = (double) epicSkillList.get(0).get(productType + "_grade_" + (epicSkillGrade + 3) + "_sum"); /*4.99999490380287*/

        Random GradeRandomSize = new Random();
        Map<String, Object> epic_skill_random_select_map = new HashMap<>(); //확률뽑기에서 1개 나온 값 저장용
        List<HashMap<String, Object>> gradeSelectEpicSkillList; //확률뽑기에서 1개 나온 값 저장용

        //몇 뽑인지에 따른 for 값
        for (int i = 0; i < product_drawing_count; i++) {

            float tmpRandom = (float) (Math.random() * 100);

            //마지막 뽑기는 3성 확정
            if (i == 10) {
                int finalEpicSkillGrade = epicSkillGrade;
                gradeSelectEpicSkillList = epicSkillList.stream().filter(x -> x.get("epic_skill_grade").equals(finalEpicSkillGrade + 2)).collect(Collectors.toList());
                int randomIndex = GradeRandomSize.nextInt(gradeSelectEpicSkillList.size());
                epic_skill_random_select_map = gradeSelectEpicSkillList.get(randomIndex);
                epic_skill_random_select_map.put("bonus_yn", "Y");
                bonus = "bonus";
            }
            //일반 뽑기는 3성, 고급 뽑기는 4성
            if (tmpRandom <= epicGatcha3 && i != 10) {

                int finalEpicSkillGrade = epicSkillGrade;
                gradeSelectEpicSkillList = epicSkillList.stream().filter(x -> x.get("epic_skill_grade").equals(finalEpicSkillGrade + 3)).collect(Collectors.toList());
                int randomIndex = GradeRandomSize.nextInt(gradeSelectEpicSkillList.size());
                epic_skill_random_select_map = gradeSelectEpicSkillList.get(randomIndex);
                bonus = "";
            //일반 뽑기는 2성, 고급 뽑기는 3성
            } else if (tmpRandom <= epicGatcha2 && i != 10) {

                int finalEpicSkillGrade = epicSkillGrade;
                gradeSelectEpicSkillList = epicSkillList.stream().filter(x -> x.get("epic_skill_grade").equals(finalEpicSkillGrade + 2)).collect(Collectors.toList());
                int randomIndex = GradeRandomSize.nextInt(gradeSelectEpicSkillList.size());
                epic_skill_random_select_map = gradeSelectEpicSkillList.get(randomIndex);
                bonus = "";
            //일반 뽑기는 1성, 고급 뽑기는 2성(위에 값 다 안걸리면 걍 이걸로 나가짐)
            } else if(i != 10) {

                int finalEpicSkillGrade = epicSkillGrade;
                gradeSelectEpicSkillList = epicSkillList.stream().filter(x -> x.get("epic_skill_grade").equals(finalEpicSkillGrade + 1)).collect(Collectors.toList());
                int randomIndex = GradeRandomSize.nextInt(gradeSelectEpicSkillList.size());
                epic_skill_random_select_map = gradeSelectEpicSkillList.get(randomIndex);
                bonus = "";

            }

            //결과값을 클라에 뿌려주기위에 for문 돌때마다 리스트에 담아둔다.
            epicResultList.add(epic_skill_random_select_map);
            //유니크값저장
            uniqueId = productType + "_epic_skill_" + advertisement + "_" + product_drawing_count + "_" + bonus + "_" + uniqueId2 + "_" + RandomStringUtils.randomAlphanumeric(6);


            //메인 맵에 넣어서 인설트 해준다.
            map.put("user_epic_skill_unique_idx", uniqueId);
            map.put("user_epic_skill_idx", epic_skill_random_select_map.get("epic_skill_idx"));
            map.put("user_epic_skill_grade", epic_skill_random_select_map.get("epic_skill_grade"));
            map.put("user_epic_skill_level", 1);
            //인설트 하고
            epicSkillMapper.insUserEpicSkill(map);

        }

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
                int product_goods_quantity = (int) productResultList.get(0).get("product_goods_quantity"); // 상품 구매에 필요한 골드 개수

                map.put("use_gold_quantity", product_goods_quantity); // DB에 넣기 위한 사용한 골드 값 저장
                map.put("before_use_gold_quantity", user_gold); // DB에 넣기 위한 사용 전 골드 값 저장
                map.put("after_use_gold_quantity", user_gold - product_goods_quantity); // DB에 넣기 위한 사용 후 골드 값 저장
                map.put("use_gold_unique_idx", uniqueId); // DB에 넣기 위한 사용 후 골드 값 저장(경로 고유값 필수)
                map.put("advertisement_yn", "N");//광고아님

                goodsMapper.insUseGoldLog(map); // 골드 사용 로그 저장

                map.put("user_gold", user_gold - product_goods_quantity); // user_data의 최종 키값 넣기

                goodsMapper.uptUserGold(map); // 사용자 골드 개수 수정

                resultMap.put("result_gold", user_gold - product_goods_quantity);
            }
        }else{
            /*0원 구매일때(광고) 어떻게 할지 장소 (일단 패스)*/
            map.put("advertisement_unique_idx", uniqueId);
            commonMapper.uptAdvertisementTimeData(map); // 광고 시간 초기화

            String user_skip_yn = String.valueOf(userResultMap.get(0).get("advertisement_buy_yn"));
            map.put("skip_yn", user_skip_yn);
            commonMapper.insAdvertisementLog(map); // 광고 본거 로그

            resultMap.put("user_data", userMapper.selectUserData(map));
        }

        //상품테이블에 로그 쌓기
        uniqueId = productType + "_epic_skill_" + advertisement + "_" + product_drawing_count + "_" + uniqueId2 + "_" + RandomStringUtils.randomAlphanumeric(6);
        map.put("unique_idx", uniqueId);
        map.put("product_eng_name", product_eng_name);
        productMapper.insProductSaleLog(map);

        //상품 구매 개수 카운트하기
        productMapper.uptProductSaleCount(map);

        //결과값 넣어주는곳
        resultMap.put("epicResultList", epicResultList);
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());

    /*
    //개별 뽑기인데 맘에 안들어서 안씀. 그래도 일단 백업(for문을 너무 많이 돌림)
    map.put("epic_skill_draw_type", "normal");
    List<HashMap<String, Object>> normalEpicSkillList = epicSkillMapper.epicSkillDrawSelect(map);
    normalEpicSkillList = normalEpicSkillList.stream().sorted((o1, o2) -> o2.get("epic_skill_draw_type_normal_percentage").toString().compareTo(o1.get("epic_skill_draw_type_normal_percentage").toString()) ).collect(Collectors.toList());

                float tmpRandom = (float) (Math.random() * 100);
                float totalPercent = 0f;

                for(int i = 0; i < normalEpicSkillList.size(); i++){
                    totalPercent += (float) normalEpicSkillList.get(i).get("epic_skill_draw_type_normal_percentage");
                    if(tmpRandom <= totalPercent){
                        System.out.println("#######" + tmpRandom);
                        System.out.println("#######" + normalEpicSkillList.get(i).get("epic_skill_idx"));
                        map.put("epic_skill_idx" , normalEpicSkillList.get(i).get("epic_skill_idx"));
                        List<HashMap<String, Object>> test2 = epicSkillMapper.selectEpicSkillData(map);

                        break;
                    }
                }
    */

    }

    /*@Transactional
    public ResponseEntity<?> insUserEpicSkill(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //어디뽑기인지 고유값 인설트(로그테이블에도 같은걸로 넣어야함)
        uniqueId = map.get("draw_type") + "_epic_skill_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);

        /////////////////로그 값 저장, 루비 사용 시작 ////////////////////

        int user_ruby= (int) userMapper.selectUserData(map).get(0).get("user_ruby"); // 사용자 루비 개수 가져오기
        int use_ruby_count = (int) map.get("use_ruby_count"); // 클라에서 보내준 사용한 루비 개수

        //회원의 루비가 부족한지 확인
        if(user_ruby + use_ruby_count < 0 ){
            *//*return "fail_ruby_lack";*//*
            response.setResult("fail_ruby_lack");
            response.setMessage("루비가 부족합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //뽑기 개수를 파악하고 자기 자신의 인벤에 들어갈수 있는지 파악
        int user_epic_bag_max= (int) userMapper.selectUserData(map).get(0).get("user_epic_bag_max"); // 사용자 에픽 스킬 가방 맥스치 수 가져오기
        int user_epic_skill_count = epicSkillMapper.userEpicSkillCount(map); // 사용자가 가지고있는 에픽스킬 수량

        //몇 뽑했는지 확인
        Object user_epic_skill_idx_if = map.get("user_epic_skill_idx");
        List<String> user_epic_skill_idx_list_if = new ArrayList<>(Arrays.asList(user_epic_skill_idx_if.toString().split(",")));

        int user_epic_result_count = user_epic_bag_max - user_epic_skill_count - user_epic_skill_idx_list_if.size();

        if(user_epic_result_count < 0){
            response.setResult("fail_epic_bag_lack");
            response.setMessage("에픽 스킬 가방 공간이 부족합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        if(use_ruby_count <= 0){ // 보내준 값이 음수 or 0이라면(키를 사용 했다면)(0은 광고)

            map.put("use_ruby_quantity", use_ruby_count); // DB에 넣기 위한 사용한 루비 값 저장
            map.put("before_use_ruby_quantity", user_ruby); // DB에 넣기 위한 사용 전 루비 값 저장
            map.put("after_use_ruby_quantity", user_ruby + use_ruby_count); // DB에 넣기 위한 사용 후 루비 값 저장
            map.put("use_ruby_unique_idx", uniqueId); // DB에 넣기 위한 사용 후 루비 값 저장(경로 고유값 필수)

            goodsMapper.insUseRubyLog(map); // 루비 사용 로그 저장

            map.put("user_ruby", user_ruby + use_ruby_count); // user_data의 최종 키값 넣기

        }

        goodsMapper.uptUserRuby(map);

        ///////////////내 가방에 에픽 스킬 인설트/////////////////////

        Object user_epic_skill_idx = map.get("user_epic_skill_idx");
        Object user_epic_skill_grade = map.get("user_epic_skill_grade");
        Object user_epic_skill_level = map.get("user_epic_skill_level");
        List<String> user_epic_skill_idx_list = new ArrayList<>(Arrays.asList(user_epic_skill_idx.toString().split(",")));
        List<String> user_epic_skill_grade_list = new ArrayList<>(Arrays.asList(user_epic_skill_grade.toString().split(",")));
        List<String> user_epic_skill_level_list = new ArrayList<>(Arrays.asList(user_epic_skill_level.toString().split(",")));

        Map<String, Object> resultMap;
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < user_epic_skill_idx_list.size(); i++) {

            resultMap = new HashMap<String, Object>();

            resultMap.put("user_uuid", map.get("user_uuid"));
            resultMap.put("user_epic_skill_unique_idx", uniqueId);
            resultMap.put("user_epic_skill_idx", user_epic_skill_idx_list.get(i));
            resultMap.put("user_epic_skill_grade", user_epic_skill_grade_list.get(i));
            resultMap.put("user_epic_skill_level", user_epic_skill_level_list.get(i));
            resultMap.put("ins_ip", map.get("ins_ip"));
            resultMap.put("ins_machine", map.get("ins_machine"));

            resultList.add(resultMap);

        }
        map.put("resultList", resultList);

        epicSkillMapper.insUserEpicSkillList(map);//유저 정보에 획득한 스킬 저장

        *//*return "success";*//*
        return new ResponseEntity<>(response, response.getStatus());
    }
*/
    public ResponseEntity<?> epicSkillDrawSelect(Map<String, Object> map) throws Exception {

        ApiResponse response = new ApiResponse(HttpStatus.OK, epicSkillMapper.epicSkillDrawSelect(map));

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Transactional
    public void uptUserEpicSkillEquip(Map<String, Object> map, HttpServletRequest request) throws Exception {

        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        epicSkillMapper.uptUserEpicSkillEquip(map);
    }

    @Transactional
    public ResponseEntity<?> uptUpgradeEpicSkill(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //골드 소비 로그값을 위한 고유값 인설트(로그테이블에도 같은걸로 넣어야함)
        uniqueId = "epic_skill_upgrade_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);
        map.put("use_gold_unique_idx", uniqueId);

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        map.put("seq", map.get("user_epic_skill_seq"));//강화 당하는 seq값
        int user_epic_skill_grade = (int) epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("user_epic_skill_grade");//당하는 놈 등급
        int epic_skill_upgrade_gold = (int) epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_gold");//당하는 놈 필요 골드

        Object epic_skill_seq = map.get("epic_skill_seq_list");//클라에서 보내준 재료의 seq 리스트
        List<String> epic_skill_seq_list = new ArrayList<>(Arrays.asList(epic_skill_seq.toString().split(",")));

        /*골드체크*/
        int user_gold = (int) userMapper.selectUserData(map).get(0).get("user_gold"); // 사용자 골드 가져오기
        //int use_gold_count = (int) map.get("use_gold_count"); // 클라에서 보내준 사용한 골드 개수
        int use_gold_count = -(epic_skill_upgrade_gold * epic_skill_seq_list.size()); //재료에 사용되는 골드 일단 계산

        //회원의 골드가 부족한지 확인
        if(user_gold + use_gold_count < 0 ){
            /*return "fail_ruby_lack";*/
            response.setResult("fail_gold_lack");
            response.setMessage("골드가 부족합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        /*강화 시작*/

        List<String> epic_skill_percent_list = new ArrayList<>();
        /*확률 가져오기*/

        for (int i = 0; i < epic_skill_seq_list.size(); i++) {
            map.put("seq", epic_skill_seq_list.get(i));//i번째 재료의 seq값
            int epic_skill_grade = (int) epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("user_epic_skill_grade");//재료의 등급값
            map.put("seq", map.get("user_epic_skill_seq"));//강화 당하는 seq값
            if (user_epic_skill_grade == 1) {
                if(epic_skill_grade == 1) {
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade1_percent")));
                }else if(epic_skill_grade == 2){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade2_percent")));
                }else if(epic_skill_grade == 3){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade3_percent")));
                }else if(epic_skill_grade == 4){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade4_percent")));
                }else if(epic_skill_grade == 5){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade5_percent")));
                }
            }else if(user_epic_skill_grade == 2){
                if(epic_skill_grade == 1) {
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade1_percent")));
                }else if(epic_skill_grade == 2){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade2_percent")));
                }else if(epic_skill_grade == 3){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade3_percent")));
                }else if(epic_skill_grade == 4){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade4_percent")));
                }else if(epic_skill_grade == 5){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade5_percent")));
                }
            }else if(user_epic_skill_grade == 3){
                if(epic_skill_grade == 1) {
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade1_percent")));
                }else if(epic_skill_grade == 2){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade2_percent")));
                }else if(epic_skill_grade == 3){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade3_percent")));
                }else if(epic_skill_grade == 4){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade4_percent")));
                }else if(epic_skill_grade == 5){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade5_percent")));
                }
            }else if(user_epic_skill_grade == 4){
                if(epic_skill_grade == 1) {
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade1_percent")));
                }else if(epic_skill_grade == 2){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade2_percent")));
                }else if(epic_skill_grade == 3){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade3_percent")));
                }else if(epic_skill_grade == 4){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade4_percent")));
                }else if(epic_skill_grade == 5){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade5_percent")));
                }
            }else if(user_epic_skill_grade == 5){
                if(epic_skill_grade == 1) {
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade1_percent")));
                }else if(epic_skill_grade == 2){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade2_percent")));
                }else if(epic_skill_grade == 3){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade3_percent")));
                }else if(epic_skill_grade == 4){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade4_percent")));
                }else if(epic_skill_grade == 5){
                    epic_skill_percent_list.add(String.valueOf(epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("epic_skill_upgrade_grade5_percent")));
                }
            }

        }

        /*Object epic_skill_percent = map.get("epic_skill_percent_list");//클라에서 보내준 재료의 퍼샌트 리스트
        List<String> epic_skill_percent_list = new ArrayList<>(Arrays.asList(epic_skill_percent.toString().split(",")));*/
        map.put("seq", map.get("user_epic_skill_seq"));//강화 당하는 seq값
        int user_epic_skill_level= (int) epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0).get("user_epic_skill_level"); //클라에서 보내준 강화 당하는 seq의 레벨

        /*만약 강화 당하는 놈의 레벨이 30이라면 팅겨내야함*/
        if(user_epic_skill_level >= 30){
            response.setResult("fail_epic_skill_over_level");
            response.setMessage("30레벨 이상인 에픽스킬입니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        int randombox;
        int upgrade_epic_skill_count = 0; //강화 수치 값(성공)
        int upgrade_epic_skill_fail_count = 0; //강화 수치 값(실패)
        int upgrade_epic_skill_order = -1; //재료로 사용된 순서

        Map<String, Object> upgradeResultMap;

        List<Map<String, Object>> seqList = new ArrayList<Map<String, Object>>();

        List<Map<String, Object>> upgradeResultList = new ArrayList<Map<String, Object>>();


        List<String> result_success_or_fail = new ArrayList<>();

        // 반복문을 사용하여 하나씩 강화 계산 성공/실패 로직

        for (int i = 0; i < epic_skill_percent_list.size(); i++) {

            randombox = (int) (Math.random() * (100 - 1 + 1) + 1);

            if (randombox <= Integer.parseInt(epic_skill_percent_list.get(i))) {

                //결과값 순서대로 넣기위해 맵 새로 파서 넣기
                upgradeResultMap = new HashMap<String, Object>();
                upgradeResultMap.put("epic_skill_seq_list", epic_skill_seq_list.get(i));
                upgradeResultMap.put("upgrade_result", "success");
                upgradeResultMap.put("upgrade_order", upgrade_epic_skill_order += 1);
                upgradeResultList.add(upgradeResultMap);

                seqList.add(upgradeResultMap); // 모든 seq 값 추가
                result_success_or_fail.add("success");

                upgrade_epic_skill_count += 1; //강화 수치 상승
                /*System.out.print("성공######" + epic_skill_seq_list.get(i));*/

                if(user_epic_skill_level + upgrade_epic_skill_count >= 30) {
                 break;
                }


            } else {

                //결과값 순서대로 넣기위해 맵 새로 파서 넣기
                upgradeResultMap = new HashMap<String, Object>();
                upgradeResultMap.put("epic_skill_seq_list", epic_skill_seq_list.get(i));
                upgradeResultMap.put("upgrade_result", "fail");
                upgradeResultMap.put("upgrade_order", upgrade_epic_skill_order += 1);
                upgradeResultList.add(upgradeResultMap);

                upgrade_epic_skill_fail_count += 1; //실패

                seqList.add(upgradeResultMap); // 모든 seq값 추가
                /*System.out.print("실패######" + epic_skill_seq_list.get(i));*/
                result_success_or_fail.add("fail");
            }
        }

        //퀘스트 클리어
        Map<String, Object> questMap;
        questMap = new HashMap<String, Object>();
        int epic_skill_upgrade_count = upgrade_epic_skill_count + upgrade_epic_skill_fail_count;
        questMap.put("epic_skill_upgrade_count", epic_skill_upgrade_count);
        questMap.put("user_uuid", map.get("user_uuid"));
        questMapper.uptUserDayQuestEpicSkillUpgradeData(questMap);

        //결과값 클라에 보내주기
        resultMap.put("resultLevel", user_epic_skill_level + upgrade_epic_skill_count);
        resultMap.put("result_success_or_fail", result_success_or_fail);
        /*사용 한 강화 재료 사용했으니 use_yn n으로 바꾸고*/
        map.put("seqList", seqList);
        map.put("use_yn_n_reason", "강화 재료로 사용");
        epicSkillMapper.uptUpgradeEpicSkillYn(map);//강화 재료 쓴거 use_yn을 N으로 수정

        /*강화된 수치 값 강화 당한 놈한테 넣기*/
        map.put("user_epic_skill_level", user_epic_skill_level + upgrade_epic_skill_count);
        epicSkillMapper.uptUpgradeEpicSkill(map);

        /*결과 로그 쌓기*/
        /*모든 로그 값 넣기*/
        map.put("use_gold", epic_skill_upgrade_gold);
        if(upgradeResultList.size() != 0) {
            map.put("upgradeResultList", upgradeResultList);
            epicSkillMapper.insUpgradeEpicSkillLog(map);
        }

        /*map.put("use_gold", epic_skill_upgrade_gold);
        if(successResultList.size() != 0) {
            map.put("successResultList", successResultList);
            resultMap.put("successResultList", successResultList);
            map.put("upgrade_result", "success");
            epicSkillMapper.insUpgradeEpicSkillLog(map);
        }
        *//*실패 로그 값 넣기*//*
        if(failResultList.size() != 0) {
            map.put("failResultList", failResultList);
            resultMap.put("failResultList", failResultList);
            map.put("upgrade_result", "fail");
            epicSkillMapper.insUpgradeEpicSkillLog(map);
        }*/

        //골드 최종값 계산 후 유저 테이블에 저장
        map.put("user_gold", user_gold - (epic_skill_upgrade_gold * result_success_or_fail.size()));
        resultMap.put("result_gold", user_gold - (epic_skill_upgrade_gold * result_success_or_fail.size()));

        goodsMapper.uptUserGold(map);

        //골드 소모 로그//
        map.put("use_gold_quantity", epic_skill_upgrade_gold * result_success_or_fail.size());
        map.put("before_use_gold_quantity", user_gold);
        map.put("after_use_gold_quantity", user_gold - (epic_skill_upgrade_gold * result_success_or_fail.size()));
        map.put("advertisement_yn", "N");//광고아님
        goodsMapper.insUseGoldLog(map);
        /*완료*/
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());
    }

    @Transactional
    public ResponseEntity<?> insertEpicSkillSynthesis(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();
        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        //첫 번째 스킬 레벨 체크 (30강만 들어와야함)
        map.put("seq", map.get("epic_skill_seq_1"));
        HashMap<String, Object> epic_skill_check_map_1 = epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0);
        int epic_skill_level_check_1 = (int) epic_skill_check_map_1.get("user_epic_skill_level");
        int epic_skill_grade_check_1 = (int) epic_skill_check_map_1.get("user_epic_skill_grade");
        int epic_skill_seq_1 = (int) epic_skill_check_map_1.get("seq");

        //두 번째 스킬 레벨 체크 (30강만 들어와야함)
        map.put("seq", map.get("epic_skill_seq_2"));
        HashMap<String, Object> epic_skill_check_map_2 = epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0);
        int epic_skill_level_check_2 = (int) epic_skill_check_map_2.get("user_epic_skill_level");
        int epic_skill_grade_check_2 = (int) epic_skill_check_map_2.get("user_epic_skill_grade");
        int epic_skill_seq_2 = (int) epic_skill_check_map_2.get("seq");

        //레벨체크 시작
        if(epic_skill_level_check_1 != 30 || epic_skill_level_check_2 != 30){
            response.setResult("not_epic_skill_level_30");
            response.setMessage("에픽 스킬은 레벨 30강만 가능합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //같은 등급인지 확인
        if(epic_skill_grade_check_1 != epic_skill_grade_check_2){
            response.setResult("not_quals_epic_skill");
            response.setMessage("서로 같은 등급의 에픽 스킬만 합성이 가능합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //같은 에픽 스킬 들어오면 안뎀
        if(epic_skill_seq_1 == epic_skill_seq_2){
            response.setResult("not_quals_epic_skill");
            response.setMessage("서로 같은 스킬은 넣을 수 없습니다..");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //어떤 등급을 합성했는지 체크(받아온 등급값의 +1을 해야 다읍 등급의 스킬 획득이 가능하겠지?)
        map.put("epic_skill_grade", epic_skill_grade_check_1 + 1);
        List<HashMap<String, Object>> epic_skill_grade_list = epicSkillMapper.selectEpicSkillData(map);

        Random random = new Random();
        int randomIndex = random.nextInt(epic_skill_grade_list.size());

        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //합성 고유값 가져와서 저장 고유값 인설트(로그테이블에도 같은걸로 넣어야함)
        uniqueId = "epic_skill_synthesis_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);

        //랜덤으로 선택된 스킬 맵으로 저장
        Map<String, Object> epic_skill_random_select_map = epic_skill_grade_list.get(randomIndex);

        //메인 맵에 넣어서 인설트 해준다.
        map.put("user_epic_skill_unique_idx", uniqueId);
        map.put("user_epic_skill_idx", epic_skill_random_select_map.get("epic_skill_idx"));
        map.put("user_epic_skill_grade", epic_skill_random_select_map.get("epic_skill_grade"));
        map.put("user_epic_skill_level", 1);

        //인설트 하고
        epicSkillMapper.insUserEpicSkill(map);
        //인설 트 한 seq값 가져오기
        int start_seq = Integer.parseInt(String.valueOf(map.get("start_seq")));

        //합성 당한 놈들 use_yn을 N으로 바꾸어준다. 리스트를 맵에 넣는 방법!
        Map<String, Object> sqlResultMap;
        List<Map<String, Object>> seqList = new ArrayList<Map<String, Object>>();

        sqlResultMap = new HashMap<String, Object>(); //새로운 맵에
        sqlResultMap.put("epic_skill_seq_list", map.get("epic_skill_seq_1")); // 첫 번째 에픽 스킬 seq를 담는다.
        seqList.add(sqlResultMap); // 그 맵을 리스트에 추가 한다.

        sqlResultMap = new HashMap<String, Object>(); // 새로운 맵을 초기화 해서
        sqlResultMap.put("epic_skill_seq_list", map.get("epic_skill_seq_2")); // 두 번째 에픽 스킬 seq를 담는다.
        seqList.add(sqlResultMap); // 그리고 그 맵을 리스트에 추가 한다.(리스트 add는 덮는게 아니고 추가된다.)

        //메인으로 쓸 map에 위 리스트를 추가한다.
        map.put("seqList", seqList);
        map.put("use_yn_n_reason", "합성 재료로 사용");
        epicSkillMapper.uptUpgradeEpicSkillYn(map);//강화 재료 쓴거 use_yn을 N으로 수정

        //합성 로그 쌓기
/*
        #{synthesis_unique_idx},
        #{user_uuid},
        #{to_epic_skill_seq_1},
        #{to_epic_skill_seq_2},
        #{from_epic_skill_seq},
*/
        map.put("synthesis_unique_idx", uniqueId);
        map.put("to_epic_skill_seq_1", map.get("epic_skill_seq_1"));
        map.put("to_epic_skill_seq_2", map.get("epic_skill_seq_2"));
        map.put("from_epic_skill_seq", start_seq);
        epicSkillMapper.insSynthesisEpicSkillLog(map);

        /*완료값 클라에 뿌려주기*/
        resultMap.put("result_epic_skill_seq", start_seq);
        resultMap.put("result_epic_skill", epic_skill_random_select_map);
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());
    }

    @Transactional
    public ResponseEntity<?> insSellEpicSkill(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //골드 충전 로그값을 위한 고유값 인설트(로그테이블에도 같은걸로 넣어야함)
        uniqueId = "epic_skill_sell_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);
        map.put("charge_gold_unique_idx", uniqueId);
        map.put("sell_unique_idx", uniqueId);

        Object epic_skill_seq = map.get("epic_skill_seq_list");//클라에서 보내준 판매될놈의 seq 리스트
        List<String> epic_skill_seq_list = new ArrayList<>(Arrays.asList(epic_skill_seq.toString().split(",")));

        /*골드체크*/
        int user_gold = (int) userMapper.selectUserData(map).get(0).get("user_gold"); // 사용자 골드 가져오기
        List<Map<String, Object>> sellResultList = new ArrayList<Map<String, Object>>();

        //판매로 인해 얻는 골드 합계
        int sum_gold = 0;
        
        for(int i = 0; i <= epic_skill_seq_list.size()-1; i++){
            //차례대로 판매되는 놈들 정보
            map.put("seq", epic_skill_seq_list.get(i));
            List<HashMap<String, Object>> sellEpicSkillList = epicSkillMapper.upgradeSelectUserEpicSkill(map);

            Map<String, Object> sellResultMap = new HashMap<String, Object>();

            if(sellEpicSkillList != null && sellEpicSkillList.size()>0 && sellEpicSkillList.get(0)!=null && sellEpicSkillList.get(0).size() != 0 ) {
                //로그에 저장될 seq
                sellResultMap.put("sell_epic_skill_seq", epic_skill_seq_list.get(i));
                //에픽 스킬 사용 여부 수정해주는거의 쿼리에서 쓰기위해 여기다가도 저장해야함 (uptUpgradeEpicSkillYn)
                sellResultMap.put("epic_skill_seq_list", epic_skill_seq_list.get(i));
                //로그에 저장될 스킬 등급
                sellResultMap.put("sell_epic_skill_grade", sellEpicSkillList.get(0).get("user_epic_skill_grade"));
                //로그에 저장될 골드
                sellResultMap.put("sell_gold", sellEpicSkillList.get(0).get("epic_skill_sell_gold"));
                //정보 저장 하기
                sellResultList.add(sellResultMap);
                //골드 합계에 더해주기
                sum_gold += (int) sellEpicSkillList.get(0).get("epic_skill_sell_gold");
            }else{
                response.setResult("not_epic_skill");
                response.setMessage("판매할수없는 스킬입니다.");
                return new ResponseEntity<>(response, response.getStatus());
            }

        }

        if(sellResultList.size() != 0) {

            //에픽 스킬 판매 로그
            map.put("sellResultList", sellResultList);
            epicSkillMapper.insSellEpicSkillLog(map);
            //골드 충전 로그
            map.put("charge_reason", "에픽스킬 판매");
            map.put("charge_gold_quantity", sum_gold); // DB에 넣기 위한 총 판매 골드
            map.put("before_charge_gold_quantity", user_gold); // DB에 넣기 위한 충전 전 골드값 저장
            map.put("after_charge_gold_quantity", user_gold + sum_gold); // DB에 넣기 위한 충전 후 골드 값 저장
            goodsMapper.insChargeGoldLog(map); // 골드 충전 로그 저장

            /*사용 한 강화 재료 사용했으니 use_yn n으로 바꾸고*/
            map.put("seqList", sellResultList);
            map.put("use_yn_n_reason", "판매 완료");
            epicSkillMapper.uptUpgradeEpicSkillYn(map);//강화 재료 쓴거 use_yn을 N으로 수정
            
        }
        
        //유저 정보에 골드 업데이트 해주기
        map.put("user_gold", user_gold + sum_gold);
        goodsMapper.uptUserGold(map);

        resultMap.put("user_gold", user_gold + sum_gold);
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());
    }

    @Transactional
    public ResponseEntity<?> Change5EpicSkillCheck(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        //가져옴 스킬 레벨 체크 (30강만 들어와야함)
        map.put("seq", map.get("epic_skill_seq"));
        HashMap<String, Object> epic_skill_check_map = epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0);
        int epic_skill_level_check = (int) epic_skill_check_map.get("user_epic_skill_level");
        int epic_skill_grade_check = (int) epic_skill_check_map.get("user_epic_skill_grade");

        //레벨체크 시작
        if (epic_skill_grade_check != 5) {
            response.setResult("not_epic_skill_grade_5");
            response.setMessage("5성 에픽 스킬만 교환이 가능합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //레벨체크 시작
        if (epic_skill_level_check != 30) {
            response.setResult("not_epic_skill_level_30");
            response.setMessage("에픽 스킬은 레벨 30강만 가능합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        map.put("epic_skill_grade", epic_skill_grade_check);
        map.put("user_epic_skill_grade", epic_skill_grade_check);

        //없는 스킬 체크해서 리스트에 담아줘야함
        List<HashMap<String, Object>> notUserGradeEpicSkillList = epicSkillMapper.selectNotUserGradeEpicSkill(map);

        if (notUserGradeEpicSkillList == null || notUserGradeEpicSkillList.size() == 0) {
            //더이상 가질 스킬이 없을때
            response.setResult("all_epic_skill_grade_" + epic_skill_grade_check);
            response.setMessage(epic_skill_grade_check + "성의 에픽스킬을 다 가지고있는데 진짜로 진행할꺼냐?");
            return new ResponseEntity<>(response, response.getStatus());
        }else{
            response.setResult("not_all_epic_skill_grade_" + epic_skill_grade_check);
            response.setMessage(epic_skill_grade_check + "성의 에픽 스킬을 다 안 가지고있음");
            resultMap.put("notUserGradeEpicSkillList", notUserGradeEpicSkillList);
            response.setBody(resultMap);
            return new ResponseEntity<>(response, response.getStatus());
        }
    }

    @Transactional
    public ResponseEntity<?> ins5ChangeEpicSkill(Map<String, Object> map, HttpServletRequest request) throws Exception {

        ApiResponse response = new ApiResponse();

        map.put("ins_ip", CommonUtil.getRemoteIp(request));

        Map<String, Object> resultMap;
        resultMap = new HashMap<String, Object>();

        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        //골드 충전 로그값을 위한 고유값 인설트(로그테이블에도 같은걸로 넣어야함)
        uniqueId = "epic_skill_5_change_" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);

        //가져옴 스킬 레벨 체크 (30강만 들어와야함)
        map.put("seq", map.get("epic_skill_seq"));
        HashMap<String, Object> epic_skill_check_map = epicSkillMapper.upgradeSelectUserEpicSkill(map).get(0);
        int epic_skill_level_check = (int) epic_skill_check_map.get("user_epic_skill_level");
        int epic_skill_grade_check = (int) epic_skill_check_map.get("user_epic_skill_grade");
        int user_epic_skill_idx = (int) epic_skill_check_map.get("user_epic_skill_idx");
        int epic_skill_seq = (int) epic_skill_check_map.get("seq");

        //레벨체크 시작
        if (epic_skill_grade_check != 5) {
            response.setResult("not_epic_skill_grade_5");
            response.setMessage("5성 에픽 스킬만 교환이 가능합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        //레벨체크 시작
        if (epic_skill_level_check != 30) {
            response.setResult("not_epic_skill_level_30");
            response.setMessage("에픽 스킬은 레벨 30강만 가능합니다.");
            return new ResponseEntity<>(response, response.getStatus());
        }

        map.put("epic_skill_grade", epic_skill_grade_check);
        map.put("user_epic_skill_grade", epic_skill_grade_check);

        //없는 스킬 체크해서 리스트에 담아줘야함
        List<HashMap<String, Object>> notUserGradeEpicSkillList = epicSkillMapper.selectNotUserGradeEpicSkill(map);

        if (notUserGradeEpicSkillList == null || notUserGradeEpicSkillList.size() == 0) {
            //더이상 가질 스킬이 없을때
            notUserGradeEpicSkillList = epicSkillMapper.selectEpicSkillData(map);
        }


        List<Integer> missingEpicSkillIdxList = new ArrayList<>();
        for (HashMap<String, Object> notUserGradeEpicSkill : notUserGradeEpicSkillList) {
            int epicSkillIdx = (int)notUserGradeEpicSkill.get("epic_skill_idx");
            missingEpicSkillIdxList.add(epicSkillIdx);
        }

        // missingEpicSkillIdxList에서 랜덤하게 하나의 스킬을 선택합니다.
        Random random = new Random();
        int randomIndex = random.nextInt(missingEpicSkillIdxList.size());
        int randomEpicSkillIdx = missingEpicSkillIdxList.get(randomIndex);

        // 랜덤하게 선택한 스킬의 epic_skill_idx를 출력합니다.
        System.out.println("랜덤하게 선택한 스킬의 epic_skill_idx: " + randomEpicSkillIdx);
        map.put("randomEpicSkillIdx", randomEpicSkillIdx);

        //랜덤으로 선택된 에픽스킬 정보 가져오기
        Map<String, Object> selectEpicSkillDataMap;
        selectEpicSkillDataMap = new HashMap<String, Object>();
        selectEpicSkillDataMap.put("epic_skill_idx", randomEpicSkillIdx);
        HashMap<String, Object> selectEpicSkillData = epicSkillMapper.selectEpicSkillData(selectEpicSkillDataMap).get(0);

        //교환당한 놈은 N으로 바꿔줘야함
        List<Map<String, Object>> sellResultList = new ArrayList<Map<String, Object>>();
        map.put("epic_skill_seq_list", map.get("epic_skill_seq")); // 첫 번째 에픽 스킬 seq를 담는다.
        sellResultList.add(map); // 그 맵을 리스트에 추가 한다.

        map.put("seqList", sellResultList);
        map.put("use_yn_n_reason", "5성 교환으로 사용");
        epicSkillMapper.uptUpgradeEpicSkillYn(map);//강화 재료 쓴거 use_yn을 N으로 수정

        //유저 에픽 스킬로 넣기
        map.put("user_epic_skill_unique_idx", uniqueId);
        map.put("user_epic_skill_idx", selectEpicSkillData.get("epic_skill_idx"));
        map.put("user_epic_skill_grade", selectEpicSkillData.get("epic_skill_grade"));
        map.put("user_epic_skill_level", 1);
        epicSkillMapper.insUserEpicSkill(map);
        //인설 트 한 seq값 가져오기
        int start_seq = Integer.parseInt(String.valueOf(map.get("start_seq")));

        //교환 로그 넣기
        map.put("change_unique_idx", uniqueId);
        map.put("to_epic_skill_seq", epic_skill_seq);
        map.put("to_epic_skill_idx", user_epic_skill_idx);
        map.put("from_epic_skill_seq", start_seq);
        map.put("from_epic_skill_idx", selectEpicSkillData.get("epic_skill_idx"));
        epicSkillMapper.insSellEpicSkillChangeLog(map);

        resultMap.put("user_epic_skill_idx", selectEpicSkillData.get("epic_skill_idx"));
        resultMap.put("user_epic_skill_grade", selectEpicSkillData.get("epic_skill_grade"));
        response.setBody(resultMap);

        return new ResponseEntity<>(response, response.getStatus());
    }

}