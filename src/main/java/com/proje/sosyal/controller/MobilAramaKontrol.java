package com.proje.sosyal.controller;

import com.proje.sosyal.model.Ogrenci;
import com.proje.sosyal.repository.OgrenciRepository;
import com.proje.sosyal.service.OgrenciServisi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MobilAramaKontrol {

    @Autowired
    private OgrenciRepository ogrenciRepository;

    @Autowired
    private OgrenciServisi ogrenciServisi;

    @Autowired
    private com.proje.sosyal.repository.TakipIstegiRepository istekRepo; // Takip isteklerini kontrol etmek için

    // 1. ÖĞRENCİ ARAMA
    @GetMapping("/ara")
    public List<Map<String, Object>> ogrenciAra(@RequestParam String sorgu, @RequestParam Long kullaniciId) {
        Ogrenci ben = ogrenciServisi.idIleGetir(kullaniciId);
        List<Map<String, Object>> sonucListesi = new ArrayList<>();

        if (sorgu == null || sorgu.trim().isEmpty()) {
            return sonucListesi;
        }

        // İsime veya bölüme göre filtrele
        List<Ogrenci> bulunanlar = ogrenciRepository.findAll().stream()
                .filter(o -> !o.getId().equals(ben.getId())) // Kendimi getirme
                .filter(o -> (o.getAdSoyad() != null && o.getAdSoyad().toLowerCase().contains(sorgu.toLowerCase())) || 
                             (o.getBolum() != null && o.getBolum().toLowerCase().contains(sorgu.toLowerCase())))
                .collect(Collectors.toList());

        // Benim istek attığım kişilerin listesi
        List<Long> istekAttiklarim = istekRepo.findAll().stream()
                .filter(istek -> istek.getIstekAtan().getId().equals(ben.getId()))
                .map(istek -> istek.getIstekAlan().getId())
                .collect(Collectors.toList());

        for (Ogrenci o : bulunanlar) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", o.getId());
            map.put("adSoyad", o.getAdSoyad());
            map.put("bolum", o.getBolum());
            
            if (o.getProfilResmi() != null) {
                map.put("profilResmi", o.getProfilResmiBase64());
            }

            // DURUM KONTROLÜ
            if (ben.getTakipEdilenler().contains(o)) {
                map.put("durum", "TAKIP_EDILIYOR");
            } else if (istekAttiklarim.contains(o.getId())) {
                map.put("durum", "ISTEK_GONDERILDI");
            } else {
                map.put("durum", "TAKIP_ET");
            }

            sonucListesi.add(map);
        }

        return sonucListesi;
    }

    // 2. TAKİP ETME (VEYA İSTEK ATMA)
    @PostMapping("/takip-et")
    public Map<String, Object> takipEt(@RequestParam Long benId, @RequestParam Long hedefId) {
        Map<String, Object> cevap = new HashMap<>();
        try {
            ogrenciServisi.takipEt(benId, hedefId);
            cevap.put("basarili", true);
            cevap.put("mesaj", "İstek gönderildi");
        } catch (Exception e) {
            cevap.put("basarili", false);
            cevap.put("mesaj", "Hata oluştu");
        }
        return cevap;
    }
    
    // 3. TAKİBİ BIRAKMA
    @PostMapping("/takip-birak")
    public Map<String, Object> takibiBirak(@RequestParam Long benId, @RequestParam Long hedefId) {
        Map<String, Object> cevap = new HashMap<>();
        try {
            ogrenciServisi.takibiBirak(benId, hedefId);
            cevap.put("basarili", true);
        } catch (Exception e) {
            cevap.put("basarili", false);
        }
        return cevap;
    }
}