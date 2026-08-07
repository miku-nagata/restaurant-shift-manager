package com.example.restaurantshiftmanager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;


@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/h2-console/**", "/login").permitAll()
                
                        // 従業員一覧は管理者・デモユーザーが閲覧できる
                        .requestMatchers(HttpMethod.GET, "/employees")
                        .hasAnyRole("ADMIN", "DEMO")

                        // 従業員の登録・編集・削除は管理者のみ
                        .requestMatchers("/employees", "/employees/**")
                        .hasRole("ADMIN")

                        // 必要人数一覧は管理者・デモユーザーが閲覧できる
                        .requestMatchers(HttpMethod.GET, "/required-staff")
                        .hasAnyRole("ADMIN", "DEMO")

                        // 必要人数設定は管理者のみ
                        .requestMatchers("/required-staff", "/required-staff/**")
                        .hasRole("ADMIN")

                        // 不足状況は管理者・デモユーザーが閲覧できる
                        .requestMatchers(HttpMethod.GET, "/shortages", "/shortages/**")
                        .hasAnyRole("ADMIN", "DEMO")

                        // シフト自動作成だけはデモユーザーも実行できる
                        .requestMatchers(HttpMethod.POST, "/shortages/calendar/create")
                        .hasAnyRole("ADMIN", "DEMO")

                        // それ以外の変更操作は管理者のみ
                        .requestMatchers("/shortages", "/shortages/**")
                        .hasRole("ADMIN")

                        // 曜日別必要人数パターン一覧は管理者・デモユーザーが閲覧できる
                        .requestMatchers(HttpMethod.GET, "/required-staff-patterns")
                        .hasAnyRole("ADMIN", "DEMO")

                        // 必要人数パターンの登録・編集・削除・月への反映は管理者のみ
                        .requestMatchers("/required-staff-patterns", "/required-staff-patterns/**")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // 管理者
        UserDetails adminUser = User.builder()
        .username("admin")
        .password(passwordEncoder.encode("password"))
        .roles("ADMIN")
        .build();

        // 一般スタッフ
        UserDetails staffUser = User.builder()
        .username("staff")
        .password(passwordEncoder.encode("password"))
        .roles("STAFF")
        .build();

        // デモユーザー
        UserDetails demoUser = User.builder()
            .username("demo")
            .password(passwordEncoder.encode("demo"))
            .roles("DEMO")
            .build();

        return new InMemoryUserDetailsManager(
            adminUser,
            staffUser,
            demoUser
    );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}