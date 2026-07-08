package com.example.restaurantshiftmanager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MyShiftController {

    // 「/my-shifts」にアクセスされたときに、自分のシフト画面を表示する
    @GetMapping("/my-shifts")
    public String showMyShifts(Model model) {

        // 1. 表示する年月を決める
        YearMonth targetMonth = YearMonth.now();

        // 2. 画面に表示する仮のシフトデータを作る
        List<MyShiftItem> shiftList = List.of(
                new MyShiftItem(targetMonth.atDay(3), LocalTime.of(10, 0), LocalTime.of(15, 0)),
                new MyShiftItem(targetMonth.atDay(6), LocalTime.of(17, 0), LocalTime.of(22, 0)),
                new MyShiftItem(targetMonth.atDay(8), LocalTime.of(10, 0), LocalTime.of(15, 0)),
                new MyShiftItem(targetMonth.atDay(10), LocalTime.of(17, 0), LocalTime.of(22, 0))
        );

        // 3. カレンダー表示用のデータを作る
        List<MyShiftDay> calendarDays = createCalendarDays(targetMonth, shiftList);

        // 4. 今月の合計勤務時間を計算する
        String totalWorkHours = calculateTotalWorkHours(shiftList);

        // 5. 今日以降で一番近いシフトを探す
        MyShiftItem nextShift = findNextShift(shiftList);

        // 6. HTMLで使うデータをModelに入れる
        model.addAttribute("yearMonthLabel", targetMonth.getYear() + "年" + targetMonth.getMonthValue() + "月");
        model.addAttribute("workDayCount", shiftList.size() + "日");
        model.addAttribute("totalWorkHours", totalWorkHours);
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("shiftList", shiftList);

        // 7. 次の出勤予定があるかどうかで表示を分ける
        if (nextShift != null) {
            model.addAttribute("nextWorkDateLabel", nextShift.getShortDateLabel());
            model.addAttribute("nextWorkTimeLabel", nextShift.getTimeLabel());
        } else {
            model.addAttribute("nextWorkDateLabel", "予定なし");
            model.addAttribute("nextWorkTimeLabel", "今月の出勤予定はありません");
        }

        // 8. my-shifts.html を表示する
        return "my-shifts";
    }

    // カレンダー用のデータを作る処理
    private List<MyShiftDay> createCalendarDays(YearMonth targetMonth, List<MyShiftItem> shiftList) {

        // HTMLに渡すカレンダー用の日付リスト
        // 最終的に「空白マス」「通常の日」「出勤日」がここに入る
        List<MyShiftDay> calendarDays = new ArrayList<>();

        // シフトを日付で探しやすくするためのMap
        //
        // 例：
        // 3日 → 10:00〜15:00
        // 6日 → 17:00〜22:00
        //
        // Listのままだと毎回探すのが大変なので、
        // 「日にち」をキーにして取り出せる形にする
        Map<Integer, MyShiftItem> shiftMap = new HashMap<>();

        // shiftListの中身を、日付をキーにしたMapへ入れる
        for (MyShiftItem shift : shiftList) {
            shiftMap.put(shift.getDayOfMonth(), shift);
        }
        // 月初の曜日に合わせて、カレンダーの前に空白マスを作る
        //
        // このカレンダーは日曜始まり
        //
        // Javaの曜日番号は、月曜日=1、火曜日=2、...、日曜日=7
        //
        // 日曜始まりにしたいので、日曜日なら空白0個にする
        //
        // 例：
        // 1日が日曜日 → 空白0個
        // 1日が月曜日 → 日曜日の分だけ空白1個
        // 1日が火曜日 → 日・月の分だけ空白2個
        // 1日が水曜日 → 日・月・火の分だけ空白3個
        int blankDays = targetMonth.atDay(1).getDayOfWeek().getValue() % 7;

        // 月初より前の空白マスを追加する
        for (int i = 0; i < blankDays; i++) {
            calendarDays.add(MyShiftDay.blankDay());
        }

        // 1日から月末まで、1日ずつ確認する
        for (int day = 1; day <= targetMonth.lengthOfMonth(); day++) {

            // その日にシフトがあるかをMapから探す
            MyShiftItem shift = shiftMap.get(day);

            if (shift != null) {

                // シフトがある日
                // 日付と勤務時間をカレンダーに表示する
                calendarDays.add(MyShiftDay.workDay(day, shift.getTimeLabel()));

            } else {

                // シフトがない日
                // 日付だけをカレンダーに表示する
                calendarDays.add(MyShiftDay.normalDay(day));
            }
        }

        // カレンダーの最後の週が7日分になるように、後ろにも空白マスを追加する
        //
        // 例：
        // 最後の日が金曜日なら、土曜・日曜の空白マスを追加する
        while (calendarDays.size() % 7 != 0) {
            calendarDays.add(MyShiftDay.blankDay());
        }

        // 完成したカレンダー用データを返す
        return calendarDays;
    }

    // 合計勤務時間を計算する処理
    private String calculateTotalWorkHours(List<MyShiftItem> shiftList) {

        // 合計勤務時間を「分」で計算する
        // 例：5時間なら 300分 として足していく
        long totalMinutes = 0;

        // シフトを1件ずつ取り出す
        for (MyShiftItem shift : shiftList) {

            // その日の勤務時間を分で取得して、合計に足す
            totalMinutes += shift.getWorkMinutes();
        }

        // 合計分数を「時間」と「分」に分ける
        //
        // 例：
        // 300分 → 5時間0分
        // 330分 → 5時間30分
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        // ぴったり○時間の場合
        // 例：20時間0分 → 「20時間」と表示する
        if (minutes == 0) {
            return hours + "時間";
        }

        // ○時間○分の場合
        // 例：20時間30分 → 「20時間30分」と表示する
        return hours + "時間" + minutes + "分";
    }

    // 次の出勤予定を探す処理
    private MyShiftItem findNextShift(List<MyShiftItem> shiftList) {

        // 今日の日付を取得する
        LocalDate today = LocalDate.now();

        // シフト一覧を1件ずつ確認する
        for (MyShiftItem shift : shiftList) {

            // このシフトの出勤日を取り出す
            LocalDate workDate = shift.getWorkDate();

            // 出勤日が「今日」かどうか
            boolean isToday = workDate.isEqual(today);

            // 出勤日が「今日より後」かどうか
            boolean isFuture = workDate.isAfter(today);

            // 今日、または未来のシフトなら「次の出勤予定」として返す
            if (isToday || isFuture) {
                return shift;
            }
        }

        // 今日以降のシフトがなかった場合
        return null;
    }

    // カレンダーの1日分を表すクラス
    public static class MyShiftDay {

        // カレンダーの空白マスかどうか
        // 例：月の始まり前や、月末後の空白部分
        private final boolean blank;

        // カレンダーに表示する日付
        // 例：1日、2日、3日
        private final int dayOfMonth;

        // 出勤予定がある日かどうか
        private final boolean workDay;

        // 出勤時間の表示
        // 例：10:00〜15:00
        private final String shiftTimeLabel;

        // MyShiftDayのデータを作るためのコンストラクタ
        private MyShiftDay(boolean blank, int dayOfMonth, boolean workDay, String shiftTimeLabel) {
            this.blank = blank;
            this.dayOfMonth = dayOfMonth;
            this.workDay = workDay;
            this.shiftTimeLabel = shiftTimeLabel;
        }

        // カレンダーの空白マスを作る
        // 日付を表示しないマス
        public static MyShiftDay blankDay() {
            return new MyShiftDay(true, 0, false, "");
        }

        // 出勤予定がない普通の日を作る
        // 日付だけを表示する
        public static MyShiftDay normalDay(int dayOfMonth) {
            return new MyShiftDay(false, dayOfMonth, false, "");
        }

        // 出勤予定がある日を作る
        // 日付と勤務時間を表示する
        public static MyShiftDay workDay(int dayOfMonth, String shiftTimeLabel) {
            return new MyShiftDay(false, dayOfMonth, true, shiftTimeLabel);
        }

        // HTML側で「空白マスかどうか」を使うためのgetter
        public boolean isBlank() {
            return blank;
        }

        // HTML側で「日付」を表示するためのgetter
        public int getDayOfMonth() {
            return dayOfMonth;
        }

        // HTML側で「出勤日かどうか」を判定するためのgetter
        public boolean isWorkDay() {
            return workDay;
        }

        // HTML側で「出勤時間」を表示するためのgetter
        public String getShiftTimeLabel() {
            return shiftTimeLabel;
        }
    }


    // シフト一覧の1件分を表すクラス
    public static class MyShiftItem {

        // 出勤日
        // 例：2026-07-03
        private final LocalDate workDate;

        // 出勤開始時間
        // 例：10:00
        private final LocalTime startTime;

        // 出勤終了時間
        // 例：15:00
        private final LocalTime endTime;

        // MyShiftItemのデータを作るためのコンストラクタ
        public MyShiftItem(LocalDate workDate, LocalTime startTime, LocalTime endTime) {
            this.workDate = workDate;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        // 出勤日を返す
        public LocalDate getWorkDate() {
            return workDate;
        }

        // 日にちだけを返す
        // 例：2026-07-03 → 3
        public int getDayOfMonth() {
            return workDate.getDayOfMonth();
        }

        // 次の出勤カードに表示する短い日付
        // 例：7/3
        public String getShortDateLabel() {
            return workDate.getMonthValue() + "/" + workDate.getDayOfMonth();
        }

        // シフト一覧に表示する日付
        // 例：7月3日（金）
        public String getDateLabel() {
            return workDate.getMonthValue() + "月"
                    + workDate.getDayOfMonth() + "日（"
                    + getDayOfWeekLabel() + "）";
        }

        // 勤務時間の表示
        // 例：10:00〜15:00
        public String getTimeLabel() {
            return startTime + "〜" + endTime;
        }

        // 勤務時間を「分」で返す
        // 例：10:00〜15:00 → 300分
        public long getWorkMinutes() {
            return Duration.between(startTime, endTime).toMinutes();
        }

        // 勤務時間を画面表示用の文字にする
        // 例：300分 → 5時間
        public String getWorkHoursLabel() {

            // 勤務時間を分で取得する
            long minutes = getWorkMinutes();

            // 分を「時間」と「残りの分」に分ける
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;

            // ぴったり○時間の場合
            if (remainingMinutes == 0) {
                return hours + "時間";
            }

            // ○時間○分の場合
            return hours + "時間" + remainingMinutes + "分";
        }

        // 曜日を日本語1文字で返す
        // 例：MONDAY → 月
        private String getDayOfWeekLabel() {
            return switch (workDate.getDayOfWeek()) {
                case MONDAY -> "月";
                case TUESDAY -> "火";
                case WEDNESDAY -> "水";
                case THURSDAY -> "木";
                case FRIDAY -> "金";
                case SATURDAY -> "土";
                case SUNDAY -> "日";
            };
        }
    }
}