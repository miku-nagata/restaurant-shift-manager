package com.example.restaurantshiftmanager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Component
public class DemoDataInitializer implements CommandLineRunner {

        private final EmployeeRepository employeeRepository;
        private final ShiftRequestRepository shiftRequestRepository;

        // Renderの環境変数 DEMO_DATA_INIT の値を受け取る
        // 設定されていなければ false
        @Value("${DEMO_DATA_INIT:false}")
        private boolean demoDataInit;

        public DemoDataInitializer(
                        EmployeeRepository employeeRepository,
                        ShiftRequestRepository shiftRequestRepository) {
                this.employeeRepository = employeeRepository;
                this.shiftRequestRepository = shiftRequestRepository;
        }

        @Override
        public void run(String... args) {

                System.out.println("DEMO_DATA_INIT = " + demoDataInit);

                // デモデータ投入が無効なら何もしない
                if (!demoDataInit) {
                        System.out.println("デモデータ投入はOFFです。");
                        return;
                }

                // すでに登録されている従業員名を取得
                Set<String> existingNames = employeeRepository.findAll()
                                .stream()
                                .map(Employee::getName)
                                .collect(Collectors.toCollection(HashSet::new));

                // デモ従業員
                saveIfMissing(
                                existingNames,
                                new Employee("佐藤 美咲", "正社員", "上級",
                                                1300, 176, null));

                saveIfMissing(
                                existingNames,
                                new Employee("田中 健太", "正社員", "中級",
                                                1250, 176, null));

                saveIfMissing(
                                existingNames,
                                new Employee("山本 結衣", "パート", "中級",
                                                1100, 100, 100000));

                saveIfMissing(
                                existingNames,
                                new Employee("松本 彩", "パート", "上級",
                                                1150, 120, 120000));

                saveIfMissing(
                                existingNames,
                                new Employee("高橋 翔", "アルバイト", "初級",
                                                1050, 80, 80000));

                saveIfMissing(
                                existingNames,
                                new Employee("小林 葵", "アルバイト", "中級",
                                                1080, 90, 90000));

                saveIfMissing(
                                existingNames,
                                new Employee("伊藤 蓮", "アルバイト", "初級",
                                                1050, 70, 70000));

                saveIfMissing(
                                existingNames,
                                new Employee("渡辺 凛", "パート", "中級",
                                                1100, 100, 100000));

                System.out.println("デモ従業員データの登録が完了しました。");
                Map<String, Employee> employees = employeeRepository.findAll()
                                .stream()
                                .collect(Collectors.toMap(
                                                Employee::getName,
                                                employee -> employee,
                                                (employee1, employee2) -> employee1));
                                                 createDemoShiftRequests(employees);

                System.out.println("デモ勤務希望データの登録が完了しました。");

        }

        private void saveIfMissing(Set<String> existingNames, Employee employee) {

                // 同じ名前がすでに存在する場合は登録しない
                if (existingNames.contains(employee.getName())) {
                        return;
                }

                employeeRepository.save(employee);
                existingNames.add(employee.getName());
        }

        private void createDemoShiftRequests(Map<String, Employee> employees) {

                LocalDate startDate = LocalDate.of(2026, 8, 1);
                LocalDate endDate = LocalDate.of(2026, 8, 31);
            
                for (LocalDate date = startDate;
                     !date.isAfter(endDate);
                     date = date.plusDays(1)) {
            
                    // 火曜日は定休日
                    if (date.getDayOfWeek() == DayOfWeek.TUESDAY) {
                        continue;
                    }
            
                    // 8月19日は臨時休業
                    if (date.equals(LocalDate.of(2026, 8, 19))) {
                        continue;
                    }
            
                    // あえて人員不足になる日
                    if (date.equals(LocalDate.of(2026, 8, 6))) {
            
                        saveShiftRequest(
                                employees.get("佐藤 美咲"),
                                date,
                                LocalTime.of(9, 0),
                                LocalTime.of(17, 0)
                        );
            
                        saveShiftRequest(
                                employees.get("山本 結衣"),
                                date,
                                LocalTime.of(9, 0),
                                LocalTime.of(15, 0)
                        );
            
                        saveShiftRequest(
                                employees.get("高橋 翔"),
                                date,
                                LocalTime.of(11, 0),
                                LocalTime.of(17, 0)
                        );
            
                        continue;
                    }
            
                    // 土曜日・日曜日
                    if (date.getDayOfWeek() == DayOfWeek.SATURDAY
                            || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            
                        saveShiftRequest(
                                employees.get("佐藤 美咲"),
                                date,
                                LocalTime.of(9, 0),
                                LocalTime.of(17, 0)
                        );
            
                        saveShiftRequest(
                                employees.get("田中 健太"),
                                date,
                                LocalTime.of(9, 0),
                                LocalTime.of(17, 0)
                        );
            
                        saveShiftRequest(
                                employees.get("山本 結衣"),
                                date,
                                LocalTime.of(9, 0),
                                LocalTime.of(15, 0)
                        );
            
                        saveShiftRequest(
                                employees.get("松本 彩"),
                                date,
                                LocalTime.of(9, 0),
                                LocalTime.of(15, 0)
                        );
            
                        saveShiftRequest(
                                employees.get("小林 葵"),
                                date,
                                LocalTime.of(11, 0),
                                LocalTime.of(17, 0)
                        );
            
                        saveShiftRequest(
                                employees.get("渡辺 凛"),
                                date,
                                LocalTime.of(9, 0),
                                LocalTime.of(15, 0)
                        );
            
                        continue;
                    }
            
                    // 月・水・木・金
                    saveShiftRequest(
                            employees.get("佐藤 美咲"),
                            date,
                            LocalTime.of(9, 0),
                            LocalTime.of(17, 0)
                    );
            
                    saveShiftRequest(
                            employees.get("田中 健太"),
                            date,
                            LocalTime.of(9, 0),
                            LocalTime.of(17, 0)
                    );
            
                    saveShiftRequest(
                            employees.get("山本 結衣"),
                            date,
                            LocalTime.of(9, 0),
                            LocalTime.of(15, 0)
                    );
            
                    saveShiftRequest(
                            employees.get("松本 彩"),
                            date,
                            LocalTime.of(9, 0),
                            LocalTime.of(15, 0)
                    );
            
                    saveShiftRequest(
                            employees.get("高橋 翔"),
                            date,
                            LocalTime.of(11, 0),
                            LocalTime.of(17, 0)
                    );
                }
            }

            private void saveShiftRequest(
                Employee employee,
                LocalDate workDate,
                LocalTime startTime,
                LocalTime endTime
        ) {
        
            if (employee == null) {
                return;
            }
        
            // 同じ従業員・同じ日の勤務希望がすでにあれば追加しない
            if (shiftRequestRepository.existsByEmployeeAndWorkDate(
                    employee,
                    workDate
            )) {
                return;
            }
        
            ShiftRequest shiftRequest = new ShiftRequest(
                    employee,
                    workDate,
                    startTime,
                    endTime,
                    "出勤希望"
            );
        
            shiftRequestRepository.save(shiftRequest);
        }
}