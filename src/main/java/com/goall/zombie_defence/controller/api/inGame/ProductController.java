package com.goall.zombie_defence.controller.api.inGame;

import com.goall.zombie_defence.response.ApiResponse;
import com.goall.zombie_defence.service.EpicSkillService;
import com.goall.zombie_defence.service.ProductService;
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
@RequestMapping("/product")
@RestController
public class ProductController {

    @Autowired
    ProductService productService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="유저의 uuid값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="상품 정보 다 가져오기")
    @PostMapping(value = "/selectProductDataList")
    @ResponseBody
    public ResponseEntity<?> selectProductDataList(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return productService.selectProductDataList(map);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_uuid", value="저장할 유저의 uuid값", required = false, dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "product_eng_name", value="테이블 'product_data의 product_eng_name값을 고대로 잘 보내줘야함 ", required = false, dataTypeClass = Integer.class, paramType = "query"),
            @ApiImplicitParam(name = "ins_machine", value="사용 기기값", required = false, dataTypeClass = String.class, paramType = "query")
    })
    @ApiOperation(value="상품 구매 api")
    @PostMapping(value = "/productBuy")
    @ResponseBody
    public ResponseEntity<?> productBuy(@RequestBody HashMap<String, Object> map, HttpServletRequest request) throws Exception {

        return productService.productBuy(map, request);

    }

}
