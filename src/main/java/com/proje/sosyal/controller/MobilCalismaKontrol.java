package com.proje.sosyal.controller;

import com.proje.sosyal.dto.SkorDTO;
import com.proje.sosyal.model.Calisma;
import com.proje.sosyal.model.Ogrenci;
import com.proje.sosyal.service.CalismaServisi;
import com.proje.sosyal.service.OgrenciServisi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MobilCalismaKontrol {

    @Autowired
    private CalismaServisi calismaServisi;

    @Autowired
    private OgrenciServisi ogrenciServisi;

    // 1. ÇALIŞMA KAYDET
    @PostMapping("/calisma-kaydet")
    public Map<String, Object> calismaKaydet(@RequestParam Long kullaniciId, 
                                             @RequestParam int sure, 
                                             @RequestParam String ders) {
        Map<String, Object> cevap = new HashMap<>();
        try {
            Ogrenci ben = ogrenciServisi.idIleGetir(kullaniciId);
            calismaServisi.calismaKaydet(ben, sure, ders);
            cevap.put("basarili", true);
            cevap.put("mesaj", "Tebrikler! Çalışma kaydedildi.");
        } catch (Exception e) {
            cevap.put("basarili", false);
            cevap.put("mesaj", "Hata oluştu.");
        }
        return cevap;
    }

    // 2. LİDERLİK TABLOSU
    @GetMapping("/liderlik-tablosu")
    public List<SkorDTO> liderlikTablosu(@RequestParam Long kullaniciId) {
        Ogrenci ben = ogrenciServisi.idIleGetir(kullaniciId);
        return calismaServisi.yarisTablosunuGetir(ben);
    }
    
    // 3. ÇALIŞMA GEÇMİŞİNİ GETİR
    @GetMapping("/gecmis")
    public List<Map<String, Object>> calismaGecmisi(@RequestParam Long kullaniciId) {
        Ogrenci ben = ogrenciServisi.idIleGetir(kullaniciId);
        List<Map<String, Object>> liste = new ArrayList<>();
        
        if (ben != null) {
            List<Calisma> calismalar = ben.getCalismalar();
            
            if (calismalar != null) {
                // Listeyi tarihe göre sırala (En yeni en üstte)
                calismalar.sort(Comparator.comparing(Calisma::getTarih).reversed());

                // DÜZELTME: LocalDate olduğu için sadece gün/ay formatı kullanıyoruz. Saati kaldırdık.
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy");

                for (Calisma c : calismalar) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("ders", c.getDersAdi());
                    map.put("sure", c.getSureDakika());
                    
                    if (c.getTarih() != null) {
                        map.put("tarih", c.getTarih().format(dtf));
                    } else {
                        map.put("tarih", "-");
                    }
                    liste.add(map);
                }
            }
        }
        return liste;
    }
}