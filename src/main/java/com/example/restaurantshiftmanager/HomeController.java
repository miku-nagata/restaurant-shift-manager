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

// 画面を表示するコントローラー
@Controller
public class HomeController {

    private final RegularHolidayRepository regularHolidayRepository;

    public HomeController(RegularHolidayRepository regularHolidayRepository) {
        this.regularHolidayRepository = regularHolidayRepository;
    }

    // 「/」にアクセスされたらトップページを表示する
    @GetMapping("/")
    public String index(Model model) {

        // 日本時間で今月を取得する
        YearMonth currentMonth = YearMonth.now(ZoneId.of("Asia/Tokyo"));

        // 今月1日
        LocalDate firstDay = currentMonth.atDay(1);

        // 月曜日始まりにするため、月初より前の空白マスを作る
        int blankCount = firstDay.getDayOfWeek().getValue() - 1;

        // 登録済みの定休日の曜日を取得する
        Set<Integer> regularHolidayDays = regularHolidayRepository.findAll()
                .stream()
                .map(RegularHoliday::getDayOfWeek)
                .collect(Collectors.toSet());

        List<DashboardCalendarDay> calendarDays = new ArrayList<>();

        // 月初より前の空白
        for (int i = 0; i < blankCount; i++) {
            calendarDays.add(DashboardCalendarDay.blank());
        }

        // 今月の日付を追加
        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            LocalDate date = currentMonth.atDay(day);
            int dayOfWeek = date.getDayOfWeek().getValue();

            if (regularHolidayDays.contains(dayOfWeek)) {
                calendarDays.add(DashboardCalendarDay.of(date, "定休日", "status-closed"));
            } else {
                calendarDays.add(DashboardCalendarDay.of(date, "未作成", "status-warning"));
            }
        }

        // 画面に渡す
        model.addAttribute("yearMonthLabel", currentMonth.getYear() + "年" + currentMonth.getMonthValue() + "月");
        model.addAttribute("calendarDays", calendarDays);

        return "index";
    }
}