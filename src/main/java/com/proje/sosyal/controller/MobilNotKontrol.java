package com.proje.sosyal.controller;

import com.proje.sosyal.model.Not;
import com.proje.sosyal.model.Ogrenci;
import com.proje.sosyal.service.NotServisi;
import com.proje.sosyal.service.OgrenciServisi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notlar")
public class MobilNotKontrol {

    @Autowired
    private NotServisi notServisi;

    @Autowired
    private OgrenciServisi ogrenciServisi;

    // 1. NOTLARI GETİR
    @GetMapping("/liste")
    public List<Map<String, Object>> notlariGetir(@RequestParam Long kullaniciId) {
        Ogrenci ben = ogrenciServisi.idIleGetir(kullaniciId);
        List<Map<String, Object>> liste = new ArrayList<>();
        
        if (ben != null) {
            List<Not> notlar = notServisi.notlariGetir(ben);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM HH:mm");

            for (Not not : notlar) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", not.getId());
                map.put("icerik", not.getIcerik());
                map.put("tamamlandi", not.isTamamlandi());
                map.put("tarih", not.getTarih().format(dtf)); // Örn: 12 May 14:30
                liste.add(map);
            }
        }
        return liste;
    }

    // 2. NOT EKLE
    @PostMapping("/ekle")
    public Map<String, Object> notEkle(@RequestParam Long kullaniciId, @RequestParam String icerik) {
        Map<String, Object> cevap = new HashMap<>();
        try {
            Ogrenci ben = ogrenciServisi.idIleGetir(kullaniciId);
            notServisi.notEkle(ben, icerik);
            cevap.put("basarili", true);
        } catch (Exception e) {
            cevap.put("basarili", false);
        }
        return cevap;
    }

    // 3. NOT SİL
    @PostMapping("/sil")
    public Map<String, Object> notSil(@RequestParam Long kullaniciId, @RequestParam Long notId) {
        Map<String, Object> cevap = new HashMap<>();
        try {
            Ogrenci ben = ogrenciServisi.idIleGetir(kullaniciId);
            notServisi.notSil(notId, ben);
            cevap.put("basarili", true);
        } catch (Exception e) {
            cevap.put("basarili", false);
        }
        return cevap;
    }

    // 4. DURUM DEĞİŞTİR (Tik at/kaldır)
    @PostMapping("/durum")
    public Map<String, Object> durumDegistir(@RequestParam Long kullaniciId, @RequestParam Long notId) {
        Map<String, Object> cevap = new HashMap<>();
        try {
            Ogrenci ben = ogrenciServisi.idIleGetir(kullaniciId);
            notServisi.durumDegistir(notId, ben);
            cevap.put("basarili", true);
        } catch (Exception e) {
            cevap.put("basarili", false);
        }
        return cevap;
    }
}