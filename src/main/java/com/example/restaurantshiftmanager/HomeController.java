package com.example.restaurantshiftmanager;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// 画面を表示するコントローラー
@Controller
public class HomeController {

    // 「/」ブラウザの１番最初のページにアクセスされたらこのメソッドを動かす
    @GetMapping("/")
    // return index → index.htmlを表示する
    public String index() {
        return "index";
    }
}