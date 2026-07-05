package com.example.restaurantshiftmanager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// トップページを表示するためのコントローラー
@Controller
public class HomeController {

    // 定休日データを取得するためのRepository
    private final RegularHolidayRepository regularHolidayRepository;

    // 臨時休業日データを取得するためのRepository
    private final TemporaryClosureRepository temporaryClosureRepository;

    // コンストラクタ
    // Springが自動でRepositoryを渡してくれる
    public HomeController(
            RegularHolidayRepository regularHolidayRepository,
            TemporaryClosureRepository temporaryClosureRepository
    ) {
        this.regularHolidayRepository = regularHolidayRepository;
        this.temporaryClosureRepository = temporaryClosureRepository;
    }

    // 「/」にアクセスされたとき、トップページを表示
    @GetMapping("/")
    public String index(Model model) {

        // 日本時間で現在の年月を取得
        YearMonth currentMonth = YearMonth.now(ZoneId.of("Asia/Tokyo"));

        // 今月の1日を取得
        LocalDate firstDay = currentMonth.atDay(1);

        // JavaのDayOfWeekでは、月曜日が1、日曜日が7
        // 月曜始まりのカレンダーにするため、月初より前に必要な空白マスの数を計算
        int blankCount = firstDay.getDayOfWeek().getValue() - 1;

        // 登録済みの定休日を取得
        // 例：月曜日が定休日なら 1、火曜日なら 2
        Set<Integer> regularHolidayDays = regularHolidayRepository.findAll()
                .stream()
                .map(RegularHoliday::getDayOfWeek)
                .collect(Collectors.toSet());

        // 登録済みの臨時休業日を取得
        // 例：2026-07-10 が臨時休業なら、そのLocalDateが入る
        Set<LocalDate> temporaryClosureDates = temporaryClosureRepository.findAll()
                .stream()
                .map(TemporaryClosure::getClosureDate)
                .collect(Collectors.toSet());

        // カレンダーに表示する1日分のデータを入れるリスト
        List<DashboardCalendarDay> calendarDays = new ArrayList<>();

        // 月初より前の空白マスを追加
        // 例：1日が水曜日なら、月・火の2マスを空白
        for (int i = 0; i < blankCount; i++) {
            calendarDays.add(DashboardCalendarDay.blank());
        }

        // 今月の日付を1日から月末まで追加
        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {

            // 今見ている日付を作る
            LocalDate date = currentMonth.atDay(day);

            // その日付の曜日を取得
            // 月曜=1、火曜=2、...、日曜=7
            int dayOfWeek = date.getDayOfWeek().getValue();

            // 臨時休業日は、定休日より優先して表示
            if (temporaryClosureDates.contains(date)) {
                calendarDays.add(DashboardCalendarDay.of(date, "臨時休業", "status-temporary"));

                // 臨時休業日ではなく、曜日が定休日に該当する場合
            } else if (regularHolidayDays.contains(dayOfWeek)) {
                calendarDays.add(DashboardCalendarDay.of(date, "定休日", "status-closed"));

                // 休業日ではない通常営業日
            } else {
                calendarDays.add(DashboardCalendarDay.of(date, "未作成", "status-warning"));
            }
        }

        // 画面に「2026年7月」のような表示用文字列を渡す
        model.addAttribute("yearMonthLabel", currentMonth.getYear() + "年" + currentMonth.getMonthValue() + "月");

        // 画面にカレンダーの日付一覧を渡す
        model.addAttribute("calendarDays", calendarDays);

        // templates/index.html を表示
        return "index";
    }
}