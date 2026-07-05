package com.example.restaurantshiftmanager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

// 画面を表示するコントローラー
@Controller
public class HomeController {

    // 「/」にアクセスされたらトップページを表示する
    @GetMapping("/")
    public String index(Model model) {

        // 日本時間で今月を取得する
        YearMonth currentMonth = YearMonth.now(ZoneId.of("Asia/Tokyo"));

        // 今月1日
        LocalDate firstDay = currentMonth.atDay(1);

        // 月曜日始まりにするため、月初より前の空白マスを作る
        int blankCount = firstDay.getDayOfWeek().getValue() - 1;

        List<LocalDate> calendarDates = new ArrayList<>();

        // 月初より前の空白
        for (int i = 0; i < blankCount; i++) {
            calendarDates.add(null);
        }

        // 今月の日付を追加
        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            calendarDates.add(currentMonth.atDay(day));
        }

        // 画面に渡す
        model.addAttribute("yearMonthLabel", currentMonth.getYear() + "年" + currentMonth.getMonthValue() + "月");
        model.addAttribute("calendarDates", calendarDates);

        return "index";
    }
}