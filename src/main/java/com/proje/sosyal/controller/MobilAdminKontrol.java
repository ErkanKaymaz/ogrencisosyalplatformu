package com.proje.sosyal.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.proje.sosyal.model.Bildirim;
import com.proje.sosyal.model.Gonderi;
import com.proje.sosyal.model.Ogrenci;
import com.proje.sosyal.model.Yorum;
import com.proje.sosyal.repository.BildirimRepository;
import com.proje.sosyal.repository.GonderiRepository;
import com.proje.sosyal.repository.OgrenciRepository;
import com.proje.sosyal.repository.YorumRepository;
import com.proje.sosyal.service.OgrenciServisi;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class MobilAdminKontrol {

    @Autowired private OgrenciRepository ogrenciRepo;
    @Autowired private GonderiRepository gonderiRepo;
    @Autowired private YorumRepository yorumRepo;
    @Autowired private BildirimRepository bildirimRepo;
    @Autowired private OgrenciServisi ogrenciServisi;

    // 1. TÜM VERİLERİ GETİR (Dashboard İçin)
    @GetMapping("/veriler")
    public Map<String, Object> adminVerileriniGetir() {
        Map<String, Object> veri = new HashMap<>();

        // 1. Öğrenciler
        List<Map<String, Object>> ogrenciler = new ArrayList<>();
        for(Ogrenci o : ogrenciRepo.findAll()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", o.getId());
            map.put("adSoyad", o.getAdSoyad());
            map.put("kadi", o.getKullaniciAdi());
            map.put("rol", o.getRol());
            if(o.getProfilResmi() != null) map.put("resim", o.getProfilResmiBase64());
            ogrenciler.add(map);
        }

        // 2. Gönderiler
        List<Map<String, Object>> gonderiler = new ArrayList<>();
        for(Gonderi g : gonderiRepo.findAllByOrderByTarihDesc()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", g.getId());
            map.put("yazar", g.getOgrenci().getAdSoyad());
            map.put("metin", g.getMetin());
            if(g.getResim() != null) map.put("resim", g.getResimBase64());
            gonderiler.add(map);
        }

        // 3. Yorumlar
        List<Map<String, Object>> yorumlar = new ArrayList<>();
        for(Yorum y : yorumRepo.findAll()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", y.getId());
            map.put("yazar", y.getYazar().getAdSoyad());
            map.put("icerik", y.getIcerik());
            map.put("gonderiId", y.getGonderi().getId());
            yorumlar.add(map);
        }

        veri.put("ogrenciler", ogrenciler);
        veri.put("gonderiler", gonderiler);
        veri.put("yorumlar", yorumlar);
        return veri;
    }

    // 2. KULLANICI SİL
    @PostMapping("/kullanici-sil")
    public Map<String, Object> kullaniciSil(@RequestParam Long id) {
        Map<String, Object> cevap = new HashMap<>();
        try {
            ogrenciServisi.ogrenciyiSil(id);
            cevap.put("basarili", true);
        } catch (Exception e) {
            cevap.put("basarili", false);
        }
        return cevap;
    }

    // 3. GÖNDERİ SİL (Bildirimli)
    @PostMapping("/gonderi-sil")
    public Map<String, Object> gonderiSil(@RequestParam Long id, @RequestParam String sebep) {
        Map<String, Object> cevap = new HashMap<>();
        try {
            Gonderi gonderi = gonderiRepo.findById(id).orElse(null);
            if(gonderi != null) {
                Bildirim bildirim = new Bildirim();
                bildirim.setOgrenci(gonderi.getOgrenci());
                bildirim.setTarih(LocalDateTime.now());
                bildirim.setMesaj("YÖNETİCİ UYARISI: Gönderiniz kaldırıldı. Sebep: " + sebep);
                bildirimRepo.save(bildirim);
                
                gonderiRepo.delete(gonderi);
                cevap.put("basarili", true);
            }
        } catch (Exception e) {
            cevap.put("basarili", false);
        }
        return cevap;
    }

    // 4. YORUM SİL (Bildirimli)
    @PostMapping("/yorum-sil")
    public Map<String, Object> yorumSil(@RequestParam Long id, @RequestParam String sebep) {
        Map<String, Object> cevap = new HashMap<>();
        try {
            Yorum yorum = yorumRepo.findById(id).orElse(null);
            if(yorum != null) {
                Bildirim bildirim = new Bildirim();
                bildirim.setOgrenci(yorum.getYazar());
                bildirim.setTarih(LocalDateTime.now());
                bildirim.setMesaj("YÖNETİCİ UYARISI: Yorumunuz kaldırıldı. Sebep: " + sebep);
                bildirimRepo.save(bildirim);
                
                yorumRepo.delete(yorum);
                cevap.put("basarili", true);
            }
        } catch (Exception e) {
            cevap.put("basarili", false);
        }
        return cevap;
    }
}