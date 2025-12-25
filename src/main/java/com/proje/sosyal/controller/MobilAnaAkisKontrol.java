package com.proje.sosyal.controller;

import com.proje.sosyal.model.Gonderi;
import com.proje.sosyal.repository.GonderiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MobilAnaAkisKontrol {
	@Autowired
    private com.proje.sosyal.repository.YorumRepository yorumRepository;
    @Autowired
    private com.proje.sosyal.service.OgrenciServisi ogrenciServisi; // Bunu sınıfın başına ekle

    @Autowired
    private com.proje.sosyal.service.GonderiServisi gonderiServisi; // Bunu sınıfın başına ekle
    @Autowired
    private GonderiRepository gonderiRepository;

    @GetMapping("/akis")
    public List<Map<String, Object>> tumGonderileriGetir(@org.springframework.web.bind.annotation.RequestParam Long kullaniciId) {
        
        // 1. Seni buluyoruz
        com.proje.sosyal.model.Ogrenci ben = ogrenciServisi.idIleGetir(kullaniciId);
        
        // 2. ORİJİNAL MANTIK: Sadece takip ettiklerini ve kendini getir
        // (Eskiden findAll vardı, o yüzden herkes geliyordu. Şimdi düzeldi.)
        List<Gonderi> gonderiler = gonderiServisi.takipEdilenlerinGonderileriniGetir(ben);
        
        List<Map<String, Object>> sonucListesi = new ArrayList<>();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd MMM HH:mm");

        for (Gonderi g : gonderiler) {
            Map<String, Object> gonderiMap = new HashMap<>();
            
            gonderiMap.put("id", g.getId());
            gonderiMap.put("metin", g.getMetin());
            if (g.getTarih() != null) gonderiMap.put("tarih", g.getTarih().format(format));
            
            gonderiMap.put("begeniSayisi", g.getBegeniSayisi());
            if (g.getResim() != null) gonderiMap.put("gonderiResim", g.getResimBase64());

            // Durum Kontrolleri
            boolean begendiMi = g.getBegenenler().stream().anyMatch(k -> k.getId().equals(kullaniciId));
            boolean benimGonderimMi = g.getOgrenci().getId().equals(kullaniciId);
            
            gonderiMap.put("begendiMi", begendiMi);
            gonderiMap.put("benimGonderimMi", benimGonderimMi);

            if (g.getOgrenci() != null) {
                gonderiMap.put("yazarAdSoyad", g.getOgrenci().getAdSoyad());
                gonderiMap.put("yazarBolum", g.getOgrenci().getBolum());
                if (g.getOgrenci().getProfilResmi() != null) {
                    gonderiMap.put("yazarResim", g.getOgrenci().getProfilResmiBase64());
                }
            }
            
            sonucListesi.add(gonderiMap);
        }

        return sonucListesi;
    }
    
 // 1. BEĞENİ YAPMA
    @org.springframework.web.bind.annotation.PostMapping("/begen")
    public Map<String, Object> begeniYap(
            @org.springframework.web.bind.annotation.RequestParam Long gonderiId,
            @org.springframework.web.bind.annotation.RequestParam Long kullaniciId) {
        
        Map<String, Object> cevap = new HashMap<>();
        try {
            com.proje.sosyal.model.Ogrenci ben = ogrenciServisi.idIleGetir(kullaniciId);
            gonderiServisi.begeniYap(gonderiId, ben);
            cevap.put("basarili", true);
        } catch (Exception e) {
            cevap.put("basarili", false);
        }
        return cevap;
    }

    // 2. YORUM LİSTESİNİ GETİR
    @GetMapping("/yorumlar/{gonderiId}")
    public List<Map<String, Object>> yorumlariGetir(@org.springframework.web.bind.annotation.PathVariable Long gonderiId) {
        List<Map<String, Object>> liste = new ArrayList<>();
        
        // Gönderiyi bul
        com.proje.sosyal.model.Gonderi g = gonderiRepository.findById(gonderiId).orElse(null);
        if (g != null) {
            for (com.proje.sosyal.model.Yorum y : g.getYorumlar()) {
                Map<String, Object> yMap = new HashMap<>();
                yMap.put("id", y.getId());
                yMap.put("icerik", y.getIcerik());
                yMap.put("yazarAd", y.getYazar().getAdSoyad());
                
                if (y.getYazar().getProfilResmi() != null) {
                    yMap.put("yazarResim", y.getYazar().getProfilResmiBase64());
                }
                liste.add(yMap);
            }
        }
        return liste;
    }

    // 3. YORUM YAPMA
    @org.springframework.web.bind.annotation.PostMapping("/yorum-yap")
    public Map<String, Object> yorumYap(
            @org.springframework.web.bind.annotation.RequestParam Long gonderiId,
            @org.springframework.web.bind.annotation.RequestParam Long kullaniciId,
            @org.springframework.web.bind.annotation.RequestParam String metin) {
        
        Map<String, Object> cevap = new HashMap<>();
        try {
            com.proje.sosyal.model.Ogrenci ben = ogrenciServisi.idIleGetir(kullaniciId);
            // Mobilden şimdilik resimsiz yorum atıyoruz (null gönderdik)
            gonderiServisi.yorumYap(gonderiId, ben, metin, null);
            cevap.put("basarili", true);
        } catch (Exception e) {
            cevap.put("basarili", false);
        }
        return cevap;
    }
    @PostMapping("/yorum-sil")
    public Map<String, Object> yorumSil(@RequestParam Long yorumId, @RequestParam Long kullaniciId) {
        Map<String, Object> cevap = new HashMap<>();
        try {
            // 1. Yorumu ve Silmek isteyen kişiyi bul
            com.proje.sosyal.model.Yorum yorum = yorumRepository.findById(yorumId).orElse(null);
            com.proje.sosyal.model.Ogrenci talepEden = ogrenciServisi.idIleGetir(kullaniciId);

            if (yorum != null && talepEden != null) {
                // MANTIK: Yorumu yazan kişi SİLEBİLİR OR gönderi sahibi SİLEBİLİR
                boolean yorumuYazanMi = yorum.getYazar().getId().equals(talepEden.getId());
                boolean gonderiSahibiMi = yorum.getGonderi().getOgrenci().getId().equals(talepEden.getId());

                if (yorumuYazanMi || gonderiSahibiMi) {
                    yorumRepository.delete(yorum);
                    cevap.put("basarili", true);
                    cevap.put("mesaj", "Yorum silindi.");
                } else {
                    cevap.put("basarili", false);
                    cevap.put("mesaj", "Bu yorumu silme yetkiniz yok.");
                }
            }
        } catch (Exception e) {
            cevap.put("basarili", false);
            cevap.put("mesaj", "Hata: " + e.getMessage());
        }
        return cevap;
    }
    @org.springframework.web.bind.annotation.PostMapping("/gonderi-paylas")
    public Map<String, Object> gonderiPaylas(
            @org.springframework.web.bind.annotation.RequestParam("kullaniciId") Long kullaniciId,
            @org.springframework.web.bind.annotation.RequestParam("metin") String metin,
            @org.springframework.web.bind.annotation.RequestParam(value = "resim", required = false) org.springframework.web.multipart.MultipartFile resim) {
        
        Map<String, Object> cevap = new HashMap<>();
        
        try {
            // 1. Öğrenciyi bul
            com.proje.sosyal.model.Ogrenci ogrenci = ogrenciServisi.idIleGetir(kullaniciId);
            
            if (ogrenci == null) {
                cevap.put("basarili", false);
                cevap.put("mesaj", "Kullanıcı bulunamadı.");
                return cevap;
            }

            // 2. Servis metodunu kullanarak kaydet (Web projesindeki servisinin aynısını kullanıyoruz)
            gonderiServisi.gonderiPaylas(metin, resim, ogrenci);

            cevap.put("basarili", true);
            cevap.put("mesaj", "Gönderi paylaşıldı!");
            
        } catch (Exception e) {
            // BU SATIRLARI EKLE Kİ HATAYI KONSOLDA GÖRELİM
            System.err.println("GÖNDERİ PAYLAŞMA HATASI: " + e.getMessage());
            e.printStackTrace(); 
            
            cevap.put("basarili", false);
            cevap.put("mesaj", "Hata: " + e.getMessage());
        }
        
        return cevap;
    }
}