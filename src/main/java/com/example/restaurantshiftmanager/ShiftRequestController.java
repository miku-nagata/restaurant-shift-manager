package com.example.restaurantshiftmanager;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
public class ShiftRequestController {

    private final ShiftRequestRepository shiftRequestRepository;
    private final EmployeeRepository employeeRepository;

    public ShiftRequestController(
            ShiftRequestRepository shiftRequestRepository,
            EmployeeRepository employeeRepository) {

        this.shiftRequestRepository = shiftRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * 登録されている勤務希望の一覧を表示する
     */
    @GetMapping("/shift-requests")
    public String list(Model model) {

        List<ShiftRequest> shiftRequests =
                shiftRequestRepository.findAll();

        model.addAttribute("shiftRequests", shiftRequests);

        return "shift-requests/list";
    }

    /**
     * 勤務希望の新規登録画面を表示する
     */
    @GetMapping("/shift-requests/new")
    public String newForm(Model model) {

        model.addAttribute(
                "employees",
                employeeRepository.findAll()
        );

        model.addAttribute(
                "timeOptions",
                createTimeOptions()
        );

        return "shift-requests/form";
    }

    /**
     * 新しい勤務希望を登録する
     */
    @PostMapping("/shift-requests")
    public String create(
            @RequestParam Long employeeId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate workDate,

            /*
             * 休み希望の場合は時間を入力しないため、
             * required = false にする
             */
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime startTime,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime endTime,

            @RequestParam String requestType,
            Model model) {

        /*
         * 休み希望では勤務時間を使用しないため、
         * 開始時間と終了時間をnullに統一する
         */
        if (isDayOffRequest(requestType)) {
            startTime = null;
            endTime = null;
        }

        String errorMessage = validateShiftRequest(
                null,
                employeeId,
                workDate,
                startTime,
                endTime,
                requestType
        );

        /*
         * 入力内容に問題があった場合は、
         * 入力画面をもう一度表示する
         */
        if (errorMessage != null) {

            model.addAttribute(
                    "errorMessage",
                    errorMessage
            );

            model.addAttribute(
                    "employees",
                    employeeRepository.findAll()
            );

            model.addAttribute(
                    "timeOptions",
                    createTimeOptions()
            );

            model.addAttribute(
                    "selectedEmployeeId",
                    employeeId
            );

            model.addAttribute(
                    "workDate",
                    workDate
            );

            /*
             * 時間がnullのときにtoString()を呼ぶと
             * エラーになるため、nullを確認する
             */
            model.addAttribute(
                    "selectedStartTime",
                    startTime != null
                            ? startTime.toString()
                            : null
            );

            model.addAttribute(
                    "selectedEndTime",
                    endTime != null
                            ? endTime.toString()
                            : null
            );

            model.addAttribute(
                    "selectedRequestType",
                    requestType
            );

            return "shift-requests/form";
        }

        Employee employee =
                employeeRepository.findById(employeeId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "従業員が見つかりません: "
                                                + employeeId
                                )
                        );

        ShiftRequest shiftRequest =
                new ShiftRequest(
                        employee,
                        workDate,
                        startTime,
                        endTime,
                        requestType
                );

        shiftRequestRepository.save(shiftRequest);

        return "redirect:/shift-requests";
    }

    /**
     * 勤務希望の編集画面を表示する
     */
    @GetMapping("/shift-requests/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            Model model) {

        ShiftRequest shiftRequest =
                shiftRequestRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "勤務希望が見つかりません: "
                                                + id
                                )
                        );

        model.addAttribute(
                "shiftRequest",
                shiftRequest
        );

        model.addAttribute(
                "employees",
                employeeRepository.findAll()
        );

        model.addAttribute(
                "timeOptions",
                createTimeOptions()
        );

        return "shift-requests/edit";
    }

    /**
     * 勤務希望を更新する
     */
    @PostMapping("/shift-requests/{id}/edit")
    public String update(
            @PathVariable Long id,

            @RequestParam Long employeeId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate workDate,

            /*
             * 編集時も休み希望では時間を入力しないため、
             * required = false にする
             */
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime startTime,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime endTime,

            @RequestParam String requestType,
            Model model) {

        /*
         * 休み希望では勤務時間を使用しないため、
         * 開始時間と終了時間をnullに統一する
         */
        if (isDayOffRequest(requestType)) {
            startTime = null;
            endTime = null;
        }

        Employee employee =
                employeeRepository.findById(employeeId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "従業員が見つかりません: "
                                                + employeeId
                                )
                        );

        ShiftRequest shiftRequest =
                shiftRequestRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "勤務希望が見つかりません: "
                                                + id
                                )
                        );

        String errorMessage = validateShiftRequest(
                id,
                employeeId,
                workDate,
                startTime,
                endTime,
                requestType
        );

        /*
         * 入力内容に問題があった場合は、
         * 入力された内容を編集画面へ戻す
         */
        if (errorMessage != null) {

            shiftRequest.setEmployee(employee);
            shiftRequest.setWorkDate(workDate);
            shiftRequest.setStartTime(startTime);
            shiftRequest.setEndTime(endTime);
            shiftRequest.setRequestType(requestType);

            model.addAttribute(
                    "errorMessage",
                    errorMessage
            );

            model.addAttribute(
                    "shiftRequest",
                    shiftRequest
            );

            model.addAttribute(
                    "employees",
                    employeeRepository.findAll()
            );

            model.addAttribute(
                    "timeOptions",
                    createTimeOptions()
            );

            return "shift-requests/edit";
        }

        /*
         * 入力内容に問題がなければ、
         * データを書き換えて保存する
         */
        shiftRequest.setEmployee(employee);
        shiftRequest.setWorkDate(workDate);
        shiftRequest.setStartTime(startTime);
        shiftRequest.setEndTime(endTime);
        shiftRequest.setRequestType(requestType);

        shiftRequestRepository.save(shiftRequest);

        return "redirect:/shift-requests";
    }

    /**
     * 勤務希望を削除する
     */
    @PostMapping("/shift-requests/{id}/delete")
    public String delete(@PathVariable Long id) {

        shiftRequestRepository.deleteById(id);

        return "redirect:/shift-requests";
    }

    /**
     * 勤務希望の入力内容と重複を確認する
     */
    private String validateShiftRequest(
            Long currentRequestId,
            Long employeeId,
            LocalDate workDate,
            LocalTime startTime,
            LocalTime endTime,
            String requestType) {

        // 今回の登録内容が休み希望か確認する
        boolean isDayOff =
                isDayOffRequest(requestType);

        /*
         * 出勤希望の場合だけ、
         * 開始時間と終了時間を必須にする
         */
        if (!isDayOff) {

            if (startTime == null || endTime == null) {
                return "出勤希望の場合は、開始時刻と終了時刻を入力してください。";
            }

            if (!startTime.isBefore(endTime)) {
                return "開始時刻は終了時刻より前にしてください。";
            }
        }

        List<ShiftRequest> shiftRequests =
                shiftRequestRepository.findAll();

        for (ShiftRequest existingRequest : shiftRequests) {

            /*
             * 編集時は、現在編集中の勤務希望自身を
             * 重複確認の対象から除外する
             */
            if (currentRequestId != null
                    && existingRequest.getId()
                    .equals(currentRequestId)) {

                continue;
            }

            boolean sameEmployee =
                    existingRequest
                            .getEmployee()
                            .getId()
                            .equals(employeeId);

            boolean sameDate =
                    existingRequest
                            .getWorkDate()
                            .equals(workDate);

            /*
             * 従業員または日付が違う場合は、
             * 次の勤務希望を確認する
             */
            if (!sameEmployee || !sameDate) {
                continue;
            }

            boolean existingIsDayOff =
                    isDayOffRequest(
                            existingRequest.getRequestType()
                    );

            /*
             * 同じ日に休み希望と出勤希望があると矛盾するため、
             * どちらかが休み希望なら登録しない
             */
            if (isDayOff || existingIsDayOff) {
                return "同じ従業員の同じ日付に勤務希望または休み希望が登録されています。";
            }

            /*
             * ここからは、今回も既存データも
             * 出勤希望の場合の確認
             */
            if (existingRequest.getStartTime() == null
                    || existingRequest.getEndTime() == null) {

                return "同じ従業員の同じ日付に勤務希望が登録されています。";
            }

            boolean overlaps =
                    startTime.isBefore(
                            existingRequest.getEndTime()
                    )
                            && endTime.isAfter(
                            existingRequest.getStartTime()
                    );

            if (overlaps) {
                return "同じ従業員の同じ日付・時間帯に勤務希望が登録されています。";
            }
        }

        return null;
    }

    /**
     * 希望の種類が休み希望か確認する
     */
    private boolean isDayOffRequest(String requestType) {

        /*
         * HTMLのvalueが「休み希望」の場合と
         * 「OFF」の場合の両方に対応する
         */
        return "休み希望".equals(requestType)
                || "OFF".equals(requestType);
    }

    /**
     * 勤務時間の選択肢を作成する
     */
    private List<String> createTimeOptions() {

        return List.of(
                "09:00", "09:30",
                "10:00", "10:30",
                "11:00", "11:30",
                "12:00", "12:30",
                "13:00", "13:30",
                "14:00", "14:30",
                "15:00", "15:30",
                "16:00", "16:30",
                "17:00", "17:30",
                "18:00", "18:30",
                "19:00", "19:30",
                "20:00", "20:30",
                "21:00", "21:30",
                "22:00"
        );
    }
}