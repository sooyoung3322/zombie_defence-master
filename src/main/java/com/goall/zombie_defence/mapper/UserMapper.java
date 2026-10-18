package com.goall.zombie_defence.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    List<HashMap<String, Object>> userUuidCheck(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> userNickNameCheck(Map<String, Object> map) throws Exception;

    void insUserData(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectUserData(Map<String, Object> map) throws Exception;

    void insUserLoginLog(Map<String, Object> map) throws Exception;

    void uptUserGameCount(Map<String, Object> map) throws Exception;

    void uptUserToken(Map<String, Object> map) throws Exception;

    void uptUserLevelExp(Map<String, Object> map) throws Exception;

    void uptUserAdvertisementSkipYn(Map<String, Object> map) throws Exception;

    void insUserRecommendations(Map<String, Object> map) throws Exception;

    void uptUserlastLogin(Map<String, Object> map) throws Exception;

    void uptUserepickSkillPlus(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectUserRecommendationsFrom(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> selectUserRecommendationsTo(Map<String, Object> map) throws Exception;

    /*###################유저 연동 쿼리 모음*/

    void insUserLinkLog(Map<String, Object> map) throws Exception;

    void uptLinkUserAdvertisementLog(Map<String, Object> map) throws Exception;

    void uptLinkUserAdvertisementTimeData(Map<String, Object> map) throws Exception;

    void uptLinkUserBuyGoodsLog(Map<String, Object> map) throws Exception;

    void uptLinkUserChargeGoldLog(Map<String, Object> map) throws Exception;

    void uptLinkUserChargeKeyLog(Map<String, Object> map) throws Exception;

    void uptLinkUserChargeRubyLog(Map<String, Object> map) throws Exception;

    void uptLinkUserEpicSkillChangeLog(Map<String, Object> map) throws Exception;

    void uptLinkUserEpicSkillSellLog(Map<String, Object> map) throws Exception;

    void uptLinkUserepicSkillSynthesisLog(Map<String, Object> map) throws Exception;

    void uptLinkUserEpicSkillUpgradeLog(Map<String, Object> map) throws Exception;

    void uptLinkUserExpBoosterData(Map<String, Object> map) throws Exception;

    void uptLinkUserGameRankRewardLog(Map<String, Object> map) throws Exception;

    void uptLinkUserGameResultData(Map<String, Object> map) throws Exception;

    void uptLinkUserGameResultDataHack(Map<String, Object> map) throws Exception;

    void uptLinkUserGameStartData(Map<String, Object> map) throws Exception;

    void uptLinkUserProductSaleLog(Map<String, Object> map) throws Exception;

    void uptLinkUserQuestLog(Map<String, Object> map) throws Exception;

    void uptLinkUserUesrLevelLog(Map<String, Object> map) throws Exception;

    void uptLinkUserUserData(Map<String, Object> map) throws Exception;

    void uptLinkUserUserDrone(Map<String, Object> map) throws Exception;

    void uptLinkUserUserEpicSkillData(Map<String, Object> map) throws Exception;

    void uptLinkUserUserLoginLog(Map<String, Object> map) throws Exception;

    void uptLinkUserUserMailData(Map<String, Object> map) throws Exception;

    void uptLinkUserUserPlayerSkillData(Map<String, Object> map) throws Exception;

    void uptLinkUserUseCashLog(Map<String, Object> map) throws Exception;

    void uptLinkUserUseGoldLog(Map<String, Object> map) throws Exception;

    void uptLinkUserUseGoodsLog(Map<String, Object> map) throws Exception;

    void uptLinkUserUseKeyLog(Map<String, Object> map) throws Exception;

    void uptLinkUserUseRubyLog(Map<String, Object> map) throws Exception;

    void uptLinkUserWeekRankData(Map<String, Object> map) throws Exception;

    void uptUnitUpgradeLogData(Map<String, Object> map) throws Exception;

    void uptUserUnitUpgradeData(Map<String, Object> map) throws Exception;

    void uptChargeBpLogData(Map<String, Object> map) throws Exception;

    void uptUseBpLogData(Map<String, Object> map) throws Exception;

    void uptGameUserMaxData(Map<String, Object> map) throws Exception;

    void uptUserRecommendationsData(Map<String, Object> map) throws Exception;

    void uptUserSeasonPackageExpData(Map<String, Object> map) throws Exception;

    void uptUserSeasonPackageInsertData(Map<String, Object> map) throws Exception;

    void uptGameWeekResultData(Map<String, Object> map) throws Exception;


    List<HashMap<String, Object>> adminPageNicknameSearch(Map<String, Object> map) throws Exception;

    List<HashMap<String, Object>> adminPageCafeEventYN(Map<String, Object> map) throws Exception;

    void insCafeEvent(Map<String, Object> map) throws Exception;

}