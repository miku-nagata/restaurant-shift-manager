package com.example.restaurantshiftmanager;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 不足状況を表示するためのController
 *
 * このControllerでは、
 * 必要人数に対して、作成済みシフト結果が足りているかを確認する
 */
@Controller
public class ShortageController {

    /**
     * 必要人数データを取得するためのRepository
     */
    private final RequiredStaffRepository requiredStaffRepository;

    /**
     * 作成済みシフト結果を取得するためのRepository
     *
     * 不足状況カレンダーでは、
     * 勤務希望ではなく、実際に作成されたシフト結果をもとに不足を確認する
     */
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    /**
     * 定休日データを取得するためのRepository
     */
    private final RegularHolidayRepository regularHolidayRepository;

    /**
     * 臨時休業データを取得するためのRepository
     */
    private final TemporaryClosureRepository temporaryClosureRepository;

    /**
     * シフトを自動作成するためのService
     *
     * 不足状況カレンダー画面で
     * 「この月のシフトを自動作成」ボタンが押されたときに使う
     */
    private final ShiftCreationService shiftCreationService;

    /**
     * コンストラクタ
     *
     * Springが必要なRepositoryやServiceを自動で渡してくれる
     */
    public ShortageController(RequiredStaffRepository requiredStaffRepository,
                              ShiftAssignmentRepository shiftAssignmentRepository,
                              RegularHolidayRepository regularHolidayRepository,
                              TemporaryClosureRepository temporaryClosureRepository,
                              ShiftCreationService shiftCreationService) {

        this.requiredStaffRepository = requiredStaffRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
        this.regularHolidayRepository = regularHolidayRepository;
        this.temporaryClosureRepository = temporaryClosureRepository;
        this.shiftCreationService = shiftCreationService;
    }

    /**
     * 不足状況一覧を表示する処理
     *
     * 例：
     * /shortages
     * → 全日付の不足状況を表示
     *
     * /shortages?date=2026-07-06
     * → 指定した日付の不足状況だけ表示
     */
    @GetMapping("/shortages")
    public String list(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model
    ) {
        // 必要人数データを取得する
        List<RequiredStaff> requiredStaffList = requiredStaffRepository.findAll();

        // 作成済みシフト結果を取得する
        List<ShiftAssignment> shiftAssignments = shiftAssignmentRepository.findAll();

        // 日付が指定されている場合は、その日付の必要人数だけに絞る
        if (date != null) {
            requiredStaffList = requiredStaffList.stream()
                    .filter(requiredStaff -> requiredStaff.getWorkDate().equals(date))
                    .toList();
        }

        // 必要人数と作成済みシフト結果を比較して、不足状況の行を作成する
        List<ShortageRow> shortageRows = createShortageRows(requiredStaffList, shiftAssignments);

        // HTMLに不足状況一覧を渡す
        model.addAttribute("shortageRows", shortageRows);

        // HTMLに選択中の日付を渡す
        model.addAttribute("selectedDate", date);

        return "shortages/list";
    }

    /**
     * 不足状況カレンダーを表示する処理
     *
     * 例：
     * /shortages/calendar
     * → 今月の不足状況カレンダーを表示
     *
     * /shortages/calendar?year=2026&month=7
     * → 2026年7月の不足状況カレンダーを表示
     */
    @GetMapping("/shortages/calendar")
    public String calendar(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model
    ) {
        YearMonth targetMonth;

        // 年月が指定されていない場合は、今月を表示する
        if (year == null || month == null) {
            targetMonth = YearMonth.now();
        } else {
            targetMonth = YearMonth.of(year, month);
        }

        // 対象月の月初と月末を作成する
        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();

        // 必要人数データを取得する
        List<RequiredStaff> requiredStaffList = requiredStaffRepository.findAll();

        // 作成済みシフト結果を取得する
        List<ShiftAssignment> shiftAssignments = shiftAssignmentRepository.findAll();

        // 対象月の必要人数だけに絞る
        List<RequiredStaff> monthlyRequiredStaffList = requiredStaffList.stream()
                .filter(requiredStaff ->
                        !requiredStaff.getWorkDate().isBefore(monthStart)
                                && !requiredStaff.getWorkDate().isAfter(monthEnd))
                .toList();

        // 対象月の不足状況を作成する
        List<ShortageRow> shortageRows = createShortageRows(monthlyRequiredStaffList, shiftAssignments);

        /*
         * 日付ごとに不足状況をまとめる
         *
         * 例：
         * 2026-07-06 → [09:00〜09:30の不足状況, 09:30〜10:00の不足状況]
         */
        Map<LocalDate, List<ShortageRow>> rowsByDate = new HashMap<>();
        for (ShortageRow row : shortageRows) {
            rowsByDate.computeIfAbsent(row.getWorkDate(), key -> new ArrayList<>()).add(row);
        }

        /*
         * 必要人数が設定されている日付をSetに入れる
         *
         * Setは重複を持たない入れ物
         * 同じ日付が何回出ても1つだけ保存される
         */
        Set<LocalDate> datesWithRequiredStaff = new HashSet<>();
        for (RequiredStaff requiredStaff : monthlyRequiredStaffList) {
            datesWithRequiredStaff.add(requiredStaff.getWorkDate());
        }

        /*
         * 定休日の曜日を取得する
         *
         * Javaの曜日は、
         * 月曜=1、火曜=2、...、日曜=7
         */
        Set<Integer> regularHolidayDayOfWeeks = new HashSet<>();
        for (RegularHoliday regularHoliday : regularHolidayRepository.findAll()) {
            regularHolidayDayOfWeeks.add(regularHoliday.getDayOfWeek());
        }

        /*
         * 対象月の臨時休業日を取得する
         *
         * 日付をキー、理由を値として保存する
         */
        Map<LocalDate, String> temporaryClosureReasons = new HashMap<>();
        for (TemporaryClosure temporaryClosure : temporaryClosureRepository.findAll()) {
            LocalDate closureDate = temporaryClosure.getClosureDate();

            if (!closureDate.isBefore(monthStart) && !closureDate.isAfter(monthEnd)) {
                temporaryClosureReasons.put(closureDate, temporaryClosure.getReason());
            }
        }

        // カレンダー表示用に、週ごとの日付データを作成する
        List<List<ShortageCalendarDay>> calendarWeeks = createCalendarWeeks(
                targetMonth,
                rowsByDate,
                datesWithRequiredStaff,
                regularHolidayDayOfWeeks,
                temporaryClosureReasons
        );

        // カレンダー上部に表示する集計値
        int shortageDayCount = 0;
        int noSettingDayCount = 0;
        int regularHolidayCount = 0;
        int temporaryClosureCount = 0;
        int maxShortageCount = 0;
        LocalDate mostDangerousDate = null;
        int mostDangerousSlotCount = 0;

        /*
         * カレンダーの日付を確認しながら、
         * 不足日数、未設定日数、定休日数などを数える
         */
        for (List<ShortageCalendarDay> week : calendarWeeks) {
            for (ShortageCalendarDay day : week) {
                if (day == null) {
                    continue;
                }

                if (day.isTemporaryClosure()) {
                    temporaryClosureCount++;
                    continue;
                }

                if (day.isRegularHoliday()) {
                    regularHolidayCount++;
                    continue;
                }

                if (!day.isHasRequiredStaff()) {
                    noSettingDayCount++;
                }

                if (day.isShortage()) {
                    shortageDayCount++;
                }

                if (day.getMaxShortageCount() > maxShortageCount) {
                    maxShortageCount = day.getMaxShortageCount();
                }

                if (day.getShortageSlotCount() > mostDangerousSlotCount) {
                    mostDangerousSlotCount = day.getShortageSlotCount();
                    mostDangerousDate = day.getDate();
                }
            }
        }

        // 前月・次月リンク用の年月を作る
        YearMonth previousMonth = targetMonth.minusMonths(1);
        YearMonth nextMonth = targetMonth.plusMonths(1);

        // HTMLにカレンダー用データを渡す
        model.addAttribute("calendarWeeks", calendarWeeks);
        model.addAttribute("targetMonth", targetMonth);

        // HTMLに前月・次月リンク用データを渡す
        model.addAttribute("previousYear", previousMonth.getYear());
        model.addAttribute("previousMonth", previousMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());

        // HTMLに集計値を渡す
        model.addAttribute("shortageDayCount", shortageDayCount);
        model.addAttribute("noSettingDayCount", noSettingDayCount);
        model.addAttribute("regularHolidayCount", regularHolidayCount);
        model.addAttribute("temporaryClosureCount", temporaryClosureCount);
        model.addAttribute("maxShortageCount", maxShortageCount);
        model.addAttribute("mostDangerousDate", mostDangerousDate);

        return "shortages/calendar";
    }

    /**
     * 不足状況カレンダー画面からシフトを自動作成する処理
     *
     * 画面の「この月のシフトを自動作成」ボタンが押されたときに動く
     *
     * 処理の流れ：
     * 1. HTMLのフォームから year と month を受け取る
     * 2. ShiftCreationService にシフト作成処理を依頼する
     * 3. 作成後、同じ年月の不足状況カレンダーへ戻る
     *
     * 注意：
     * ShiftCreationService側では、同じ月の作成済みシフトを一度削除してから
     * 新しく作り直す処理になっている
     */
    @PostMapping("/shortages/calendar/create")
    public String createShift(
            @RequestParam Integer year,
            @RequestParam Integer month) {

        // 指定された年月のシフトを自動作成する
        shiftCreationService.createMonthlyShift(year, month);

        /*
         * 自動作成が終わったら、同じ年月の不足状況カレンダーに戻る
         *
         * 例：
         * /shortages/calendar?year=2026&month=7
         */
        return "redirect:/shortages/calendar?year=" + year + "&month=" + month;
    }

    /**
     * 不足状況の一覧を作成するメソッド
     *
     * 必要人数データを30分単位に分けて、
     * その30分枠を作成済みシフトが何人分カバーしているかを数える
     *
     * ここでは勤務希望ではなく、作成済みシフト結果を使う
     */
    private List<ShortageRow> createShortageRows(List<RequiredStaff> requiredStaffList,
                                                 List<ShiftAssignment> shiftAssignments) {

        // 不足状況の1行ずつを入れるリスト
        List<ShortageRow> shortageRows = new ArrayList<>();

        // 必要人数データを1件ずつ確認する
        for (RequiredStaff requiredStaff : requiredStaffList) {

            // 必要時間帯の開始時刻から確認を始める
            LocalTime slotStart = requiredStaff.getStartTime();

            /*
             * 必要時間帯を30分ごとに分けて確認する
             */
            while (slotStart.isBefore(requiredStaff.getEndTime())) {

                // 30分後を終了時刻にする
                LocalTime slotEnd = slotStart.plusMinutes(30);

                // 必要時間の終了時刻を超えないようにする
                if (slotEnd.isAfter(requiredStaff.getEndTime())) {
                    slotEnd = requiredStaff.getEndTime();
                }

                // この30分枠をカバーしている作成済みシフトの人数
                int assignedCount = 0;

                // 作成済みシフトを1件ずつ確認する
                for (ShiftAssignment shiftAssignment : shiftAssignments) {

                    // 必要人数と作成済みシフトの日付が同じか
                    boolean sameDate = shiftAssignment.getWorkDate().equals(requiredStaff.getWorkDate());

                    // 作成済みシフトが、この30分枠の開始時刻までに始まっているか
                    boolean coversSlotStart = !shiftAssignment.getStartTime().isAfter(slotStart);

                    // 作成済みシフトが、この30分枠の終了時刻まで働いているか
                    boolean coversSlotEnd = !shiftAssignment.getEndTime().isBefore(slotEnd);

                    // 同じ日付で、この30分枠を最初から最後までカバーしていれば人数に含める
                    if (sameDate && coversSlotStart && coversSlotEnd) {
                        assignedCount++;
                    }
                }

                // この30分枠の不足状況を1行分として作成する
                ShortageRow row = new ShortageRow(
                        requiredStaff.getWorkDate(),
                        slotStart,
                        slotEnd,
                        requiredStaff.getRequiredCount(),
                        assignedCount
                );

                // 作成した行をリストに追加する
                shortageRows.add(row);

                // 次の30分枠へ進む
                slotStart = slotEnd;
            }
        }

        return shortageRows;
    }

    /**
     * カレンダー表示用に、1か月分の日付を週ごとにまとめるメソッド
     */
    private List<List<ShortageCalendarDay>> createCalendarWeeks(
            YearMonth targetMonth,
            Map<LocalDate, List<ShortageRow>> rowsByDate,
            Set<LocalDate> datesWithRequiredStaff,
            Set<Integer> regularHolidayDayOfWeeks,
            Map<LocalDate, String> temporaryClosureReasons
    ) {
        List<List<ShortageCalendarDay>> calendarWeeks = new ArrayList<>();

        LocalDate firstDay = targetMonth.atDay(1);

        /*
         * Javaの曜日は、
         * 月曜=1、火曜=2、...、日曜=7
         *
         * 日曜始まりのカレンダーにしたいので、
         * 日曜だけ0になるように % 7 を使う
         */
        int blankCount = firstDay.getDayOfWeek().getValue() % 7;

        List<ShortageCalendarDay> week = new ArrayList<>();

        // 月初より前の空白マスを追加する
        for (int i = 0; i < blankCount; i++) {
            week.add(null);
        }

        // 1日から月末まで、1日ずつカレンダーに追加する
        for (int day = 1; day <= targetMonth.lengthOfMonth(); day++) {
            LocalDate date = targetMonth.atDay(day);
            List<ShortageRow> rows = rowsByDate.getOrDefault(date, new ArrayList<>());

            boolean hasRequiredStaff = datesWithRequiredStaff.contains(date);
            boolean regularHoliday = regularHolidayDayOfWeeks.contains(date.getDayOfWeek().getValue());
            boolean temporaryClosure = temporaryClosureReasons.containsKey(date);
            String temporaryClosureReason = temporaryClosureReasons.get(date);

            int shortageSlotCount = 0;
            int maxShortageCount = 0;

            // その日の不足コマ数と最大不足人数を数える
            for (ShortageRow row : rows) {
                if (row.isShortage()) {
                    shortageSlotCount++;
                }

                if (row.getShortageCount() > maxShortageCount) {
                    maxShortageCount = row.getShortageCount();
                }
            }

            // 1日分のカレンダー情報を作成する
            week.add(new ShortageCalendarDay(
                    date,
                    hasRequiredStaff,
                    regularHoliday,
                    temporaryClosure,
                    temporaryClosureReason,
                    shortageSlotCount,
                    maxShortageCount
            ));

            // 7日分たまったら、1週間として追加する
            if (week.size() == 7) {
                calendarWeeks.add(week);
                week = new ArrayList<>();
            }
        }

        // 最後の週が7日未満なら、空白マスで埋める
        if (!week.isEmpty()) {
            while (week.size() < 7) {
                week.add(null);
            }

            calendarWeeks.add(week);
        }

        return calendarWeeks;
    }
}