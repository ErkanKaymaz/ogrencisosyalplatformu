package com.proje.sosyal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/mobil-test")
    public Map<String, String> testEt() {
        HashMap<String, String> cevap = new HashMap<>();
        cevap.put("mesaj", "Bağlantı Başarılı! 🚀");
        cevap.put("platform", "Spring Boot Sunucusu");
        return cevap;
    }
}