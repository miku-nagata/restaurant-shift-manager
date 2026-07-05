package com.example.restaurantshiftmanager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * シフトを自動作成するためのServiceクラス
 * Serviceクラスは、
 * Controllerに直接書くには長くなりそうな処理や、アプリの中心になる処理を書く
 * 今回は、「必要人数」と「勤務希望」をもとに、「作成済みシフト」を作成
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
     * 処理の流れ：
     * 1. 対象月の開始日と終了日を作る
     * 2. すでに作成済みのシフトを削除する
     * 3. 対象月の必要人数を取得する
     * 4. 対象月の勤務希望を取得する
     * 5. 必要人数ごとに、出勤希望者を探して割り当てる
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
         * すでに作成済みのシフト結果を削除
         *
         * 例：
         * 7月分を自動作成する
         * → 既存の7月分シフトを削除
         * → 新しく7月分シフトを作成
         */
        shiftAssignmentRepository.deleteByWorkDateBetween(startDate, endDate);

        // 対象月の必要人数を取得
        List<RequiredStaff> requiredStaffList =
                requiredStaffRepository.findByWorkDateBetween(startDate, endDate);

        // 対象月の勤務希望を取得
        List<ShiftRequest> shiftRequests =
                shiftRequestRepository.findByWorkDateBetween(startDate, endDate);
        // 確認用：対象月の必要人数データが何件取れているか表示します。
        System.out.println("必要人数データ件数: " + requiredStaffList.size());

// 確認用：対象月の勤務希望データが何件取れているか表示します。
        System.out.println("勤務希望データ件数: " + shiftRequests.size());

        /*
         * 必要人数データを日付順・開始時刻順に並べる
         *
         * 並べておくと、
         * 7月1日 10:00〜15:00
         * 7月1日 17:00〜22:00
         * 7月2日 10:00〜15:00
         * のように、自然な順番で処理できる
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
         * 必要人数の時間帯ごとに、シフトを作成
         *
         * 例：
         * 7月10日 10:00〜15:00 に2人必要
         * → その時間に出勤希望を出している人を探す
         * → 2人まで割り当てる
         */
        for (RequiredStaff requiredStaff : sortedRequiredStaffList) {

            // 必要人数が未入力、または0以下なら処理しない
            if (requiredStaff.getRequiredCount() == null || requiredStaff.getRequiredCount() <= 0) {
                continue;
            }

            /*
             * この必要人数データに合う勤務希望を探す
             *
             * 条件：
             * 1. 同じ日付であること
             * 2. requestType が「出勤希望」であること
             * 3. 勤務希望の開始時刻が、必要時間の開始時刻以前であること
             * 4. 勤務希望の終了時刻が、必要時間の終了時刻以後であること
             *
             * つまり、
             * 「必要な時間帯をすべてカバーできる人」を候補にする
             */
            List<ShiftRequest> candidates = shiftRequests
                    .stream()
                    .filter(shiftRequest -> shiftRequest.getWorkDate().equals(requiredStaff.getWorkDate()))
                    .filter(shiftRequest -> "出勤希望".equals(shiftRequest.getRequestType()))
                    .filter(shiftRequest -> !shiftRequest.getStartTime().isAfter(requiredStaff.getStartTime()))
                    .filter(shiftRequest -> !shiftRequest.getEndTime().isBefore(requiredStaff.getEndTime()))
                    .sorted(Comparator.comparing(shiftRequest -> shiftRequest.getEmployee().getName()))
                    .toList();
            // 確認用：この必要人数に対して、候補者が何人いるか表示します。
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
             * Setは、同じ値を重複して入れられないコレクション
             */
            Set<Long> assignedEmployeeIds = new HashSet<>();

            // 何人割り当てたかを数える変数
            int assignedCount = 0;

            /*
             * 候補者を必要人数分だけシフトに入れる
             */
            for (ShiftRequest candidate : candidates) {

                // 必要人数に達したら、それ以上は割り当てない
                if (assignedCount >= requiredStaff.getRequiredCount()) {
                    break;
                }

                // 候補者の従業員IDを取得
                Long employeeId = candidate.getEmployee().getId();

                // すでに同じ時間帯に割り当て済みならスキップ
                if (assignedEmployeeIds.contains(employeeId)) {
                    continue;
                }

                /*
                 * シフト結果を作成
                 *
                 * 時間は勤務希望ではなく、必要人数で設定された時間帯に合わせる
                 *
                 * 例：
                 * 必要人数：10:00〜15:00
                 * 勤務希望：09:00〜17:00
                 * 作成結果：10:00〜15:00
                 */
                ShiftAssignment shiftAssignment = new ShiftAssignment(
                        requiredStaff.getWorkDate(),
                        requiredStaff.getStartTime(),
                        requiredStaff.getEndTime(),
                        candidate.getEmployee()
                );

                // 作成したシフト結果をデータベースに保存
                shiftAssignmentRepository.save(shiftAssignment);

                // この従業員を割り当て済みとして記録
                assignedEmployeeIds.add(employeeId);

                // 割り当て人数を1人増やす
                assignedCount++;
            }
        }
    }
}