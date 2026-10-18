package com.goall.zombie_defence.batchScheduler;
import com.goall.zombie_defence.mapper.InGameMapper;
import com.goall.zombie_defence.mapper.MailMapper;
import com.goall.zombie_defence.mapper.SeasonPackageMapper;
import com.goall.zombie_defence.mapper.StatisticsMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BatchScheduler {

    @Autowired
    StatisticsMapper statisticsMapper;

    @Autowired
    InGameMapper inGameMapper;

    @Autowired
    MailMapper mailMapper;

    @Autowired
    SeasonPackageMapper seasonPackageMapper;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Transactional
    //매주 일요일 23시 59분 57초에 랭킹 저장 후 삭제
    //@Scheduled(cron = "00 20 04 * * TUE")
    //@Scheduled(cron = "0 40 44 * * THU")
    @Scheduled(cron = "57 59 23 * * SUN")
    public void testSchedule() throws Exception {
        logger.info("[Scheduled] week rank GOODS {}", LocalDateTime.now());

        String uniqueId = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar dateTime = Calendar.getInstance();
        uniqueId = sdf.format(dateTime.getTime());

        uniqueId = "week_rank_goods" + uniqueId + "_" + RandomStringUtils.randomAlphanumeric(6);

        HashMap<String, Object> map;
        map = new HashMap<String, Object>();

        List<HashMap<String, Object>> stageAllList = inGameMapper.stageAllList2(map);

        for (int i = 0; i <= stageAllList.size() - 1; i++) {

            map = new HashMap<String, Object>();

            Map<String, Object> resultMap;
            resultMap = new HashMap<String, Object>();

            /*map.put("week_data", "Y");*/
            String game_mode = String.valueOf(stageAllList.get(i).get("game_mode"));
            map.put("game_mode", game_mode);
            resultMap.put("game_mode", game_mode);
            String game_level = String.valueOf(stageAllList.get(i).get("game_level"));
            map.put("game_level", game_level);
            resultMap.put("game_level", game_level);
            String game_title = String.valueOf(stageAllList.get(i).get("game_title"));

            //주간 랭킹 데이터값 가져오기
            List<HashMap<String, Object>> totalGameRanking = statisticsMapper.totalWeekGameRanking(map);

            if (totalGameRanking != null && totalGameRanking.size() != 0) {

                resultMap.put("weekRankResultList", totalGameRanking);
                //넣기
                statisticsMapper.insWeekGameRanking(resultMap);

                /*for (int j = 1; j <= 3; j++) {
                    int finalJ = j;
                    List<String> user_uuid = totalGameRanking.stream()
                            .filter(data -> Integer.parseInt(String.valueOf(data.get("game_rank"))) == finalJ)
                            .map(data -> data.get("user_uuid").toString())
                            .collect(Collectors.toList());

                    //1~3위 랭커 보상 주기
                    Map<String, Object> fromInsMail;
                    fromInsMail = new HashMap<String, Object>();

                    if (user_uuid != null && user_uuid.size() != 0) {
                        fromInsMail.put("user_uuid", user_uuid.get(0));
                        fromInsMail.put("all_mail_unique", uniqueId);
                        fromInsMail.put("reward_unique", "week_rank_" + finalJ);
                        fromInsMail.put("mail_title", "주간 보상_" + finalJ + "위");
                        fromInsMail.put("mail_content", game_title + " 스테이지_" + finalJ + "위 주간 보상입니다.<br>" + "축하드립니다!!");
                        fromInsMail.put("mail_sender", "운영진");
                        fromInsMail.put("mail_note", "주간 랭킹 보상_" + finalJ + "위");
                        mailMapper.insUserAllMail(fromInsMail);
                    }
                }*/
            }
        }
        //보상 다 넣어주고 3주전 주간랭킹쪽 초기화(2주전껀 보상넣을때써야함)
        statisticsMapper.delGameWeekResultData(map);
    }

    @Transactional
    //매달 14일 2시 시즌 끝
    @Scheduled(cron = "0 0 2 14 * *")
    public void seasonEnd() throws Exception {

        HashMap<String, Object> map;
        map = new HashMap<String, Object>();
        List<HashMap<String, Object>> selectSeasonNumberList = seasonPackageMapper.selectSeasonNumberList(map);
        int season_number = (int) selectSeasonNumberList.get(0).get("season_number");
        map.put("season_number", season_number);
        seasonPackageMapper.uptUserSeasonNumberDate(map);

    }

    @Transactional
    //매달 15일 2시 시즌 시작(재접속해야함)
    @Scheduled(cron = "0 0 2 15 * *")
    public void seasonStart() throws Exception {

        HashMap<String, Object> map;
        map = new HashMap<String, Object>();
        List<HashMap<String, Object>> selectSeasonNumberList = seasonPackageMapper.selectSeasonNumberList(map);
        int season_number = (int) selectSeasonNumberList.get(0).get("season_number");
        map.put("season_number", season_number + 1);

        // 시간대 설정, 서버와 일치시켜야 할 수도 있습니다.
        ZoneId zoneId = ZoneId.systemDefault();

        // 현재 날짜와 시간
        LocalDateTime now = LocalDateTime.now(zoneId);

        // 이번 달의 15일 오전 2시
        LocalDateTime seasonStartDate = now.withDayOfMonth(15).withHour(2).withMinute(0).withSecond(0).withNano(0);

        // 다음 달의 14일 오전 2시
        LocalDateTime seasonEndDate = now.plusMonths(1).withDayOfMonth(14).withHour(2).withMinute(0).withSecond(0).withNano(0);

        // MySQL DATETIME 형식으로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String formattedStart = formatter.format(seasonStartDate);
        String formattedEnd = formatter.format(seasonEndDate);
        map.put("season_start_date", formattedStart);
        map.put("season_end_date", formattedEnd);

        seasonPackageMapper.insUserSeasonNumberDate(map);

    }
}