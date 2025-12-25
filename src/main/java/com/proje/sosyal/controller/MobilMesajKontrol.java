package com.proje.sosyal.controller;

import com.proje.sosyal.model.Mesaj;
import com.proje.sosyal.model.Ogrenci;
import com.proje.sosyal.service.MesajServisi;
import com.proje.sosyal.service.OgrenciServisi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mesaj")
@CrossOrigin // Mobil erişim için ek güvenlik önlemi
public class MobilMesajKontrol {

    @Autowired
    private MesajServisi mesajServisi;

    @Autowired
    private OgrenciServisi ogrenciServisi;

    // 1. SOHBET GEÇMİŞİNİ GETİR
    @GetMapping("/gecmis")
    public List<Map<String, Object>> mesajGecmisi(@RequestParam Long benId, @RequestParam Long karsiId) {
        List<Map<String, Object>> sonuc = new ArrayList<>();
        
        try {
            // Null kontrolü
            if (benId == null || karsiId == null) return sonuc;

            List<Mesaj> mesajlar = mesajServisi.sohbetGecmisiniGetir(benId, karsiId);
            
            if (mesajlar == null) return sonuc;

            for (Mesaj m : mesajlar) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", m.getId());
                
                // Modelindeki isme göre (mesajMetni ise mesajMetni, icerik ise icerik)
                map.put("metin", m.getMesajMetni()); 
                
                if (m.getGonderen() != null) {
                    map.put("gonderenId", m.getGonderen().getId());
                }
                
                // Resim varsa ekle
                if (m.getResim() != null) {
                    map.put("resim", m.getResimBase64());
                }
                
                sonuc.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Hata olsa bile boş liste dön ki frontend çökmesin
        }
        return sonuc;
    }

    // 2. MESAJ GÖNDER
    @PostMapping("/gonder")
    public Map<String, Object> mesajGonder(@RequestParam Long gonderenId, 
                                           @RequestParam Long aliciId, 
                                           @RequestParam String metin) {
        Map<String, Object> cevap = new HashMap<>();
        try {
            Ogrenci gonderen = ogrenciServisi.idIleGetir(gonderenId);
            Ogrenci alici = ogrenciServisi.idIleGetir(aliciId);
            
            if (gonderen != null && alici != null) {
                // Servis metoduna null resim ile yolluyoruz (Şimdilik sadece metin)
                mesajServisi.mesajGonder(gonderen, alici, metin, null);
                cevap.put("basarili", true);
            } else {
                cevap.put("basarili", false);
                cevap.put("mesaj", "Kullanıcı bulunamadı");
            }
        } catch (Exception e) {
            e.printStackTrace();
            cevap.put("basarili", false);
            cevap.put("mesaj", "Sunucu hatası: " + e.getMessage());
        }
        return cevap;
    }

    // 3. ARKADAŞ LİSTESİNİ GETİR (YENİ EKLENEN KISIM)
    // chatList.tsx sayfasının çalışması için bu zorunlu!
    @GetMapping("/arkadaslar")
    public List<Map<String, Object>> arkadaslariGetir(@RequestParam Long kullaniciId) {
        Ogrenci ben = ogrenciServisi.idIleGetir(kullaniciId);
        List<Map<String, Object>> liste = new ArrayList<>();
        
        try {
            if (ben != null) {
                // Mantık: Ben birini takip ediyorum (o), O da beni takip ediyorsa -> Arkadaşızdır.
                for (Ogrenci o : ben.getTakipEdilenler()) {
                    if (o.getTakipEdilenler().contains(ben)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", o.getId());
                        map.put("adSoyad", o.getAdSoyad());
                        
                        if (o.getProfilResmi() != null) {
                            map.put("profilResmi", o.getProfilResmiBase64());
                        } else {
                            map.put("profilResmi", null);
                        }
                        
                        liste.add(map);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return liste;
    }
}