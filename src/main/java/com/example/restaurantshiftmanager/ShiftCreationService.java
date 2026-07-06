package com.example.restaurantshiftmanager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * シフトを自動作成するためのServiceクラス
 * Serviceクラスは、
 * Controllerに直接書くには長くなりそうな処理や、アプリの中心になる処理を書く
 * 今回は、「必要人数」と「勤務希望」をもとに、「作成済みシフト」を作成する
 */
@Service
public class ShiftCreationService {

    /**
     * 勤務希望データを取得するためのRepository
     */
    private final ShiftRequestRepository shiftRequestRepository;

    /**
     * 必要人数データを取得するためのRepository
     */
    private final RequiredStaffRepository requiredStaffRepository;

    /**
     * 作成したシフト結果を保存するためのRepository
     */
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    /**
     * コンストラクタ
     * Springが自動で各Repositoryを渡す
     */
    public ShiftCreationService(
            ShiftRequestRepository shiftRequestRepository,
            RequiredStaffRepository requiredStaffRepository,
            ShiftAssignmentRepository shiftAssignmentRepository) {

        this.shiftRequestRepository = shiftRequestRepository;
        this.requiredStaffRepository = requiredStaffRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
    }

    /**
     * 指定された年月のシフトを自動作成
     *
     * 処理の流れ：
     * 1. 対象月の開始日と終了日を作る
     * 2. すでに作成済みのシフトを削除する
     * 3. 対象月の必要人数を取得する
     * 4. 対象月の勤務希望を取得する
     * 5. まず完全に入れる人を割り当てる
     * 6. 必要人数に足りない場合、1時間以内の不足なら部分的に割り当てる
     *
     * @param year シフトを作成したい年
     * @param month シフトを作成したい月
     */
    @Transactional
    public void createMonthlyShift(int year, int month) {

        // 指定された年月の1日を作る
        LocalDate startDate = LocalDate.of(year, month, 1);

        // 指定された月の末日を取得
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        /*
         * 同じ月のシフトを作り直すために、
         * すでに作成済みのシフト結果を削除する
         *
         * これがないと、作成ボタンを押すたびに
         * 同じ月のシフトが重複して増えてしまう
         */
        shiftAssignmentRepository.deleteByWorkDateBetween(startDate, endDate);

        // 対象月の必要人数を取得する
        List<RequiredStaff> requiredStaffList =
                requiredStaffRepository.findByWorkDateBetween(startDate, endDate);

        // 対象月の勤務希望を取得する
        List<ShiftRequest> shiftRequests =
                shiftRequestRepository.findByWorkDateBetween(startDate, endDate);

        // 確認用：対象月の必要人数データが何件取れているか表示する
        System.out.println("必要人数データ件数: " + requiredStaffList.size());

        // 確認用：対象月の勤務希望データが何件取れているか表示する
        System.out.println("勤務希望データ件数: " + shiftRequests.size());

        /*
         * 必要人数データを日付順・開始時刻順に並べる
         */
        List<RequiredStaff> sortedRequiredStaffList = requiredStaffList
                .stream()
                .sorted(
                        Comparator
                                .comparing(RequiredStaff::getWorkDate)
                                .thenComparing(RequiredStaff::getStartTime)
                )
                .toList();

        /*
         * 必要人数の時間帯ごとに、シフトを作成する
         */
        for (RequiredStaff requiredStaff : sortedRequiredStaffList) {

            // 必要人数が未入力、または0以下なら処理しない
            if (requiredStaff.getRequiredCount() == null || requiredStaff.getRequiredCount() <= 0) {
                continue;
            }

            /*
             * まず、必要時間を完全にカバーできる人を候補にする
             *
             * 例：
             * 必要人数：10:00〜15:00
             * 勤務希望：09:00〜17:00
             * → 完全にカバーできるので候補になる
             */
            List<ShiftCandidate> candidates = shiftRequests
                    .stream()
                    .filter(shiftRequest -> shiftRequest.getWorkDate().equals(requiredStaff.getWorkDate()))
                    .filter(shiftRequest -> "出勤希望".equals(shiftRequest.getRequestType()))
                    .filter(shiftRequest -> isFullCover(shiftRequest, requiredStaff))
                    .map(shiftRequest -> new ShiftCandidate(
                            shiftRequest,
                            requiredStaff.getStartTime(),
                            requiredStaff.getEndTime(),
                            0
                    ))
                    .sorted(Comparator.comparing(ShiftCandidate::getEmployeeName))
                    .toList();

            /*
             * 完全にカバーできる人だけでは必要人数に足りない場合、
             * 1時間以内の不足であれば、部分的に働ける人も候補に追加する
             *
             * 例：
             * 必要人数：10:00〜15:00
             * 勤務希望：10:00〜14:30
             * → 30分足りないが、10:00〜14:30は働けるので候補にする
             */
            if (candidates.size() < requiredStaff.getRequiredCount()) {

                List<ShiftCandidate> partialCandidates = shiftRequests
                        .stream()
                        .filter(shiftRequest -> shiftRequest.getWorkDate().equals(requiredStaff.getWorkDate()))
                        .filter(shiftRequest -> "出勤希望".equals(shiftRequest.getRequestType()))
                        .filter(shiftRequest -> !isFullCover(shiftRequest, requiredStaff))
                        .filter(shiftRequest -> hasOverlap(shiftRequest, requiredStaff))
                        .map(shiftRequest -> {
                            /*
                             * 実際に割り当てる開始時刻は、
                             * 必要開始時刻と希望開始時刻のうち、遅い方にする
                             *
                             * 例：
                             * 必要：10:00〜15:00
                             * 希望：11:00〜15:00
                             * → 実際の割り当て開始は11:00
                             */
                            LocalTime assignedStartTime = getLaterTime(
                                    shiftRequest.getStartTime(),
                                    requiredStaff.getStartTime()
                            );

                            /*
                             * 実際に割り当てる終了時刻は、
                             * 必要終了時刻と希望終了時刻のうち、早い方にする
                             *
                             * 例：
                             * 必要：10:00〜15:00
                             * 希望：10:00〜14:30
                             * → 実際の割り当て終了は14:30
                             */
                            LocalTime assignedEndTime = getEarlierTime(
                                    shiftRequest.getEndTime(),
                                    requiredStaff.getEndTime()
                            );

                            // 必要時間に対して何分足りないか計算する
                            long shortageMinutes = calculateShortageMinutes(shiftRequest, requiredStaff);

                            return new ShiftCandidate(
                                    shiftRequest,
                                    assignedStartTime,
                                    assignedEndTime,
                                    shortageMinutes
                            );
                        })
                        // 足りない時間が1時間以内の人だけ候補にする
                        .filter(candidate -> candidate.getShortageMinutes() <= 60)
                        .sorted(
                                Comparator
                                        .comparingLong(ShiftCandidate::getShortageMinutes)
                                        .thenComparing(ShiftCandidate::getEmployeeName)
                        )
                        .toList();

                /*
                 * 完全に入れる人を先に並べ、
                 * そのあとに部分的に入れる人を並べる
                 */
                candidates = java.util.stream.Stream
                        .concat(candidates.stream(), partialCandidates.stream())
                        .toList();
            }

            // 確認用：この必要人数に対して、候補者が何人いるか表示する
            System.out.println(
                    requiredStaff.getWorkDate()
                            + " "
                            + requiredStaff.getStartTime()
                            + "〜"
                            + requiredStaff.getEndTime()
                            + " 必要人数: "
                            + requiredStaff.getRequiredCount()
                            + " 候補者数: "
                            + candidates.size()
            );

            /*
             * 同じ従業員を同じ時間帯に重複して入れないためのSet
             *
             * Setは、同じ値を重複して入れられない
             */
            Set<Long> assignedEmployeeIds = new HashSet<>();

            // 何人割り当てたかを数える変数
            int assignedCount = 0;

            /*
             * 候補者を必要人数分だけシフトに入れる
             */
            for (ShiftCandidate candidate : candidates) {

                // 必要人数に達したら、それ以上は割り当てない
                if (assignedCount >= requiredStaff.getRequiredCount()) {
                    break;
                }

                // 候補者の従業員IDを取得する
                Long employeeId = candidate.getShiftRequest().getEmployee().getId();

                // すでに同じ時間帯に割り当て済みならスキップする
                if (assignedEmployeeIds.contains(employeeId)) {
                    continue;
                }

                /*
                 * シフト結果を作成する
                 *
                 * 完全にカバーできる人：
                 * → 必要人数の時間帯で保存
                 *
                 * 1時間以内だけ足りない人：
                 * → 勤務希望と必要時間が重なっている部分だけ保存
                 */
                ShiftAssignment shiftAssignment = new ShiftAssignment(
                        requiredStaff.getWorkDate(),
                        candidate.getAssignedStartTime(),
                        candidate.getAssignedEndTime(),
                        candidate.getShiftRequest().getEmployee()
                );

                // 作成したシフト結果をデータベースに保存する
                shiftAssignmentRepository.save(shiftAssignment);

                // この従業員を割り当て済みとして記録する
                assignedEmployeeIds.add(employeeId);

                // 割り当て人数を1人増やす
                assignedCount++;
            }
        }
    }

    /**
     * 勤務希望が、必要時間を完全にカバーしているか判定するメソッド
     *
     * 例：
     * 必要時間：10:00〜15:00
     * 希望時間：09:00〜17:00
     * → true
     *
     * 必要時間：10:00〜15:00
     * 希望時間：10:00〜14:30
     * → false
     */
    private boolean isFullCover(ShiftRequest shiftRequest, RequiredStaff requiredStaff) {
        return !shiftRequest.getStartTime().isAfter(requiredStaff.getStartTime())
                && !shiftRequest.getEndTime().isBefore(requiredStaff.getEndTime());
    }

    /**
     * 勤務希望と必要時間に、重なっている時間があるか判定するメソッド
     *
     * 例：
     * 必要時間：10:00〜15:00
     * 希望時間：14:00〜18:00
     * → 14:00〜15:00が重なるので true
     *
     * 必要時間：10:00〜15:00
     * 希望時間：16:00〜18:00
     * → 重ならないので false
     */
    private boolean hasOverlap(ShiftRequest shiftRequest, RequiredStaff requiredStaff) {

        LocalTime overlapStartTime = getLaterTime(
                shiftRequest.getStartTime(),
                requiredStaff.getStartTime()
        );

        LocalTime overlapEndTime = getEarlierTime(
                shiftRequest.getEndTime(),
                requiredStaff.getEndTime()
        );

        return overlapStartTime.isBefore(overlapEndTime);
    }

    /**
     * 必要時間に対して、勤務希望が何分足りないかを計算するメソッド
     *
     * 例：
     * 必要時間：10:00〜15:00
     * 希望時間：10:00〜14:30
     * → 後ろが30分足りないので 30
     *
     * 必要時間：10:00〜15:00
     * 希望時間：11:00〜15:00
     * → 前が60分足りないので 60
     */
    private long calculateShortageMinutes(ShiftRequest shiftRequest, RequiredStaff requiredStaff) {

        long shortageMinutes = 0;

        // 希望開始時刻が必要開始時刻より遅い場合、前半が足りない
        if (shiftRequest.getStartTime().isAfter(requiredStaff.getStartTime())) {
            shortageMinutes += Duration.between(
                    requiredStaff.getStartTime(),
                    shiftRequest.getStartTime()
            ).toMinutes();
        }

        // 希望終了時刻が必要終了時刻より早い場合、後半が足りない
        if (shiftRequest.getEndTime().isBefore(requiredStaff.getEndTime())) {
            shortageMinutes += Duration.between(
                    shiftRequest.getEndTime(),
                    requiredStaff.getEndTime()
            ).toMinutes();
        }

        return shortageMinutes;
    }

    /**
     * 2つの時刻のうち、遅い方を返すメソッド
     *
     * 例：
     * 10:00 と 11:00
     * → 11:00
     */
    private LocalTime getLaterTime(LocalTime time1, LocalTime time2) {
        if (time1.isAfter(time2)) {
            return time1;
        }

        return time2;
    }

    /**
     * 2つの時刻のうち、早い方を返すメソッド
     *
     * 例：
     * 15:00 と 14:30
     * → 14:30
     */
    private LocalTime getEarlierTime(LocalTime time1, LocalTime time2) {
        if (time1.isBefore(time2)) {
            return time1;
        }

        return time2;
    }

    /**
     * シフト作成時の候補者情報をまとめるためのクラス
     *
     * ShiftRequestだけだと、
     * 「実際に何時から何時まで割り当てるか」
     * 「何分足りない候補なのか」
     * を一緒に持てないため、このクラスを作る
     */
    private static class ShiftCandidate {

        /**
         * 元になった勤務希望データ
         */
        private final ShiftRequest shiftRequest;

        /**
         * 実際にシフトとして割り当てる開始時刻
         */
        private final LocalTime assignedStartTime;

        /**
         * 実際にシフトとして割り当てる終了時刻
         */
        private final LocalTime assignedEndTime;

        /**
         * 必要時間に対して何分足りないか
         * 完全にカバーできる場合は0
         */
        private final long shortageMinutes;

        public ShiftCandidate(
                ShiftRequest shiftRequest,
                LocalTime assignedStartTime,
                LocalTime assignedEndTime,
                long shortageMinutes) {

            this.shiftRequest = shiftRequest;
            this.assignedStartTime = assignedStartTime;
            this.assignedEndTime = assignedEndTime;
            this.shortageMinutes = shortageMinutes;
        }

        public ShiftRequest getShiftRequest() {
            return shiftRequest;
        }

        public LocalTime getAssignedStartTime() {
            return assignedStartTime;
        }

        public LocalTime getAssignedEndTime() {
            return assignedEndTime;
        }

        public long getShortageMinutes() {
            return shortageMinutes;
        }

        public String getEmployeeName() {
            return shiftRequest.getEmployee().getName();
        }
    }
}