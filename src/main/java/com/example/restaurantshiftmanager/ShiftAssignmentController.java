package com.example.restaurantshiftmanager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 作成されたシフト結果を画面に表示するためのController
 * Controllerは、
 * 「ブラウザからアクセスされたときに、どの処理をするか」を決める役割がある
 */
@Controller
public class ShiftAssignmentController {

    /**
     * シフト結果をデータベースから取得するためのRepository
     */
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    /**
     * シフトを自動作成するためのService
     * 自動作成の細かい処理はControllerに直接書かず、
     * ShiftCreationServiceに任せる
     */
    private final ShiftCreationService shiftCreationService;

    /**
     * コンストラクタ
     * Springが自動で
     * ShiftAssignmentRepository と ShiftCreationService を渡してくれる
     */
    public ShiftAssignmentController(
            ShiftAssignmentRepository shiftAssignmentRepository,
            ShiftCreationService shiftCreationService) {

        this.shiftAssignmentRepository = shiftAssignmentRepository;
        this.shiftCreationService = shiftCreationService;
    }

    /**
     * シフト結果一覧画面を表示
     * 例：
     * /shift-assignments
     * → 今月のシフト結果を表示
     * /shift-assignments?year=2026&month=7
     * → 2026年7月のシフト結果を表示
     *
     * @param year 表示したい年
     * @param month 表示したい月
     * @param model HTMLにデータを渡すための入れ物
     * @return 表示するHTMLファイル名
     */
    @GetMapping("/shift-assignments")
    public String list(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model) {

        // 今日の日付を取得
        LocalDate today = LocalDate.now();

        // 年が指定されていない場合は、今年を使用
        if (year == null) {
            year = today.getYear();
        }

        // 月が指定されていない場合は、今月を使用
        if (month == null) {
            month = today.getMonthValue();
        }

        // 指定された年月の1日を作成
        LocalDate startDate = LocalDate.of(year, month, 1);

        // 指定された月の末日を取得
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 指定された月のシフト結果をデータベースから取得
        List<ShiftAssignment> shiftAssignments = shiftAssignmentRepository
                .findByWorkDateBetween(startDate, endDate)
                .stream()
                .sorted(
                        Comparator
                                .comparing(ShiftAssignment::getWorkDate)
                                .thenComparing(ShiftAssignment::getStartTime)
                )
                .toList();

        /*
         * 日付ごとにシフト結果をまとめる
         * 日付を文字列に変換してキーにする
         *
         * 例：
         * "2026-07-01" → [山田さん 09:00〜21:00]
         *
         * LocalDateのままキーにすると、HTML側で取り出しにくい場合があるため、
         * 文字列にして扱いやすくする
         */
        Map<String, List<ShiftAssignment>> shiftAssignmentsByDate = shiftAssignments
                .stream()
                .collect(Collectors.groupingBy(shiftAssignment -> shiftAssignment.getWorkDate().toString()));

        /*
         * カレンダー表示用に、1か月分の日付を週ごとにまとめる
         */
        List<List<LocalDate>> calendarWeeks = createCalendarWeeks(startDate, endDate);

        // HTMLで使えるように、シフト結果一覧をmodelに入れる
        model.addAttribute("shiftAssignments", shiftAssignments);

        // 日付ごとにまとめたシフト結果をHTMLに渡す
        model.addAttribute("shiftAssignmentsByDate", shiftAssignmentsByDate);

        // カレンダー用の日付データをHTMLに渡す
        model.addAttribute("calendarWeeks", calendarWeeks);

        // HTMLで表示するために、年と月もmodelに入れる
        model.addAttribute("year", year);
        model.addAttribute("month", month);

        // templatesフォルダ内の shift-assignments.html を表示
        return "shift-assignments";
    }

    /**
     * シフト自動作成ボタンが押されたときに動く処理
     * 例：
     * 2026年7月の画面でボタンを押す
     * → 2026年7月分のシフトを自動作成する
     * → 作成後、2026年7月のシフト一覧画面に戻る
     *
     * @param year 作成したい年
     * @param month 作成したい月
     * @return 作成後に移動するURL
     */
    @PostMapping("/shift-assignments/create")
    public String create(
            @RequestParam Integer year,
            @RequestParam Integer month) {

        /*
         * ShiftCreationServiceに自動作成処理を任せる
         *
         * Controllerは「ボタンが押されたことを受け取る係」、
         * Serviceは「実際にシフトを作る係」という分担
         */
        shiftCreationService.createMonthlyShift(year, month);

        /*
         * 自動作成が終わったら、同じ年月のシフト一覧画面に戻る
         * redirect: を付けると、別のURLに移動できます。
         */
        return "redirect:/shift-assignments?year=" + year + "&month=" + month;
    }

    /**
     * カレンダー表示用に、指定された月の日付を週ごとにまとめるメソッド
     * 1週間を「日・月・火・水・木・金・土」の7日で作成
     *
     * @param startDate 月初の日付
     * @param endDate 月末の日付
     * @return 週ごとにまとめた日付リスト
     */
    private List<List<LocalDate>> createCalendarWeeks(LocalDate startDate, LocalDate endDate) {

        // カレンダー全体を入れるリスト
        List<List<LocalDate>> calendarWeeks = new ArrayList<>();

        // 1週間分の日付を入れるリスト
        List<LocalDate> week = new ArrayList<>();

        /*
         * Javaの曜日は、
         * 月曜=1、火曜=2、...、日曜=7
         * という番号で表される
         *
         * 今回は日曜始まりにしたいので、
         * 日曜の場合は0、月曜の場合は1、火曜の場合は2...になるように計算
         */
        int blankDays = startDate.getDayOfWeek().getValue() % 7;

        // 月初より前の空白マスを追加
        for (int i = 0; i < blankDays; i++) {
            week.add(null);
        }

        // 月初から月末まで、1日ずつ追加していく
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {

            // 1週間分たまったら、calendarWeeksに追加して新しい週を作成
            if (week.size() == 7) {
                calendarWeeks.add(week);
                week = new ArrayList<>();
            }

            // 現在の日付を週のリストに追加
            week.add(currentDate);

            // 次の日に進める
            currentDate = currentDate.plusDays(1);
        }

        // 月末のあとに空白マスを追加して、最後の週も7日分にそろえる
        while (week.size() < 7) {
            week.add(null);
        }

        // 最後の週をカレンダー全体に追加
        calendarWeeks.add(week);

        return calendarWeeks;
    }
}