package com.proje.sosyal.controller;

import com.proje.sosyal.model.Gonderi;
import com.proje.sosyal.model.Ogrenci;
import com.proje.sosyal.model.TakipIstegi;
import com.proje.sosyal.service.GonderiServisi;
import com.proje.sosyal.service.OgrenciServisi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MobilProfilKontrol {
	@Autowired
    private com.proje.sosyal.repository.OgrenciRepository ogrenciRepository; // En başa ekle
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder; // En başa ekle
    @Autowired
    private GonderiServisi gonderiServisi;
    
    @Autowired
    private OgrenciServisi ogrenciServisi;

    // 1. PROFİL BİLGİSİ GETİR
    @GetMapping("/profil/{id}")
    public Map<String, Object> profilBilgisiGetir(@PathVariable Long id) {
        Map<String, Object> cevap = new HashMap<>();
        
        try {
            Ogrenci ben = ogrenciServisi.idIleGetir(id);

            if (ben == null) {
                cevap.put("hata", "Öğrenci bulunamadı");
                return cevap;
            }

            // Temel Bilgiler
            cevap.put("adSoyad", ben.getAdSoyad() != null ? ben.getAdSoyad() : "İsimsiz");
            cevap.put("bolum", ben.getBolum() != null ? ben.getBolum() : "-");
            if (ben.getProfilResmi() != null) {
                cevap.put("profilResmi", ben.getProfilResmiBase64());
            }

            // İstatistikler
            cevap.put("takipciSayisi", ben.getTakipciler() != null ? ben.getTakipciler().size() : 0);
            cevap.put("takipEdilenSayisi", ben.getTakipEdilenler() != null ? ben.getTakipEdilenler().size() : 0);
            cevap.put("gonderiSayisi", ben.getGonderiler() != null ? ben.getGonderiler().size() : 0);

            // --- İSTEKLER ---
            List<Map<String, Object>> istekListesi = new ArrayList<>();
            List<TakipIstegi> gelenIstekler = ogrenciServisi.gelenIstekleriGetir(ben);
            
            if (gelenIstekler != null) {
                for (TakipIstegi istek : gelenIstekler) {
                    Map<String, Object> iMap = new HashMap<>();
                    iMap.put("id", istek.getId());
                    if(istek.getIstekAtan() != null) {
                        iMap.put("istekAtanAd", istek.getIstekAtan().getAdSoyad());
                        iMap.put("istekAtanId", istek.getIstekAtan().getId());
                        if(istek.getIstekAtan().getProfilResmi() != null) {
                            iMap.put("istekAtanResim", istek.getIstekAtan().getProfilResmiBase64());
                        }
                    }
                    istekListesi.add(iMap);
                }
            }
            cevap.put("istekler", istekListesi);

            // --- GÖNDERİLER ---
            List<Map<String, Object>> gonderilerListesi = new ArrayList<>();
            List<Gonderi> orjinalGonderiler = ben.getGonderiler();
            
            if (orjinalGonderiler != null) {
                for (int i = orjinalGonderiler.size() - 1; i >= 0; i--) {
                    Gonderi g = orjinalGonderiler.get(i);
                    Map<String, Object> gMap = new HashMap<>();
                    gMap.put("id", g.getId());
                    gMap.put("metin", g.getMetin());
                    gMap.put("begeniSayisi", g.getBegeniSayisi());
                    if (g.getResim() != null) {
                        gMap.put("gonderiResim", g.getResimBase64());
                    }
                    gonderilerListesi.add(gMap);
                }
            }
            cevap.put("gonderilerim", gonderilerListesi);

        } catch (Exception e) {
            e.printStackTrace();
            cevap.put("hata", "Sunucu hatası: " + e.getMessage());
        }

        return cevap;
    }
 // --- YENİ: PROFİL GÜNCELLEME ---
    @PostMapping("/profil-guncelle")
    public Map<String, Object> profilGuncelle(
            @RequestParam Long id,
            @RequestParam(required = false) String adSoyad,
            @RequestParam(required = false) String bolum,
            @RequestParam(required = false) String kullaniciAdi,
            @RequestParam(required = false) String yeniSifre,
            @RequestParam(value = "resim", required = false) org.springframework.web.multipart.MultipartFile resim) {
        
        Map<String, Object> cevap = new HashMap<>();
        try {
            Ogrenci ben = ogrenciServisi.idIleGetir(id);
            if (ben == null) {
                cevap.put("basarili", false);
                cevap.put("mesaj", "Kullanıcı bulunamadı");
                return cevap;
            }

            // 1. Kullanıcı Adı Kontrolü (Eğer değiştirdiyse ve başkası kullanıyorsa)
            if (kullaniciAdi != null && !kullaniciAdi.equals(ben.getKullaniciAdi())) {
                if (ogrenciRepository.findByKullaniciAdi(kullaniciAdi).isPresent()) {
                    cevap.put("basarili", false);
                    cevap.put("mesaj", "Bu kullanıcı adı dolu!");
                    return cevap;
                }
                ben.setKullaniciAdi(kullaniciAdi);
            }

            // 2. Diğer Bilgiler
            if (adSoyad != null && !adSoyad.isEmpty()) ben.setAdSoyad(adSoyad);
            if (bolum != null && !bolum.isEmpty()) ben.setBolum(bolum);

            // 3. Şifre (Sadece dolu geldiyse değiştir)
            if (yeniSifre != null && !yeniSifre.isEmpty()) {
                ben.setSifre(passwordEncoder.encode(yeniSifre));
            }

            // 4. Resim
            if (resim != null && !resim.isEmpty()) {
                ben.setProfilResmi(resim.getBytes());
            }

            ogrenciRepository.save(ben);
            cevap.put("basarili", true);
            cevap.put("mesaj", "Profil güncellendi!");

        } catch (Exception e) {
            e.printStackTrace();
            cevap.put("basarili", false);
            cevap.put("mesaj", "Hata: " + e.getMessage());
        }
        return cevap;
    }
    // 2. GÖNDERİ SİL
    @PostMapping("/gonderi-sil")
    public Map<String, Object> gonderiSil(@RequestParam Long gonderiId, @RequestParam Long kullaniciId) {
        Map<String, Object> cevap = new HashMap<>();
        try {
            Ogrenci ben = ogrenciServisi.idIleGetir(kullaniciId);
            gonderiServisi.gonderiSil(gonderiId, ben);
            cevap.put("basarili", true);
        } catch (Exception e) { cevap.put("basarili", false); }
        return cevap;
    }

    // 3. TAKİPÇİYİ LİSTEDEN ÇIKAR
    @PostMapping("/takipci-cikar")
    public Map<String, Object> takipciCikar(@RequestParam Long benId, @RequestParam Long hedefId) {
        Map<String, Object> cevap = new HashMap<>();
        try {
            ogrenciServisi.takipciyiCikar(benId, hedefId);
            cevap.put("basarili", true);
        } catch (Exception e) { cevap.put("basarili", false); }
        return cevap;
    }

    // --- DİKKAT: "takip-birak" METODUNU BURADAN SİLDİM ÇÜNKÜ MobilAramaKontrol'DE VAR ---

    // 4. İSTEK KABUL ET
    @PostMapping("/istek-kabul")
    public Map<String, Object> istekKabul(@RequestParam Long istekId) {
        Map<String, Object> cevap = new HashMap<>();
        try { ogrenciServisi.istekKabul(istekId); cevap.put("basarili", true); } catch (Exception e) { cevap.put("basarili", false); }
        return cevap;
    }

    // 5. İSTEK REDDET
    @PostMapping("/istek-reddet")
    public Map<String, Object> istekReddet(@RequestParam Long istekId) {
        Map<String, Object> cevap = new HashMap<>();
        try { ogrenciServisi.istekReddet(istekId); cevap.put("basarili", true); } catch (Exception e) { cevap.put("basarili", false); }
        return cevap;
    }

    // 6. TAKİPÇİ LİSTESİNİ GETİR
    @GetMapping("/profil/{id}/takipciler")
    public List<Map<String, Object>> takipcileriGetir(@PathVariable Long id) {
        Ogrenci ben = ogrenciServisi.idIleGetir(id);
        List<Map<String, Object>> liste = new ArrayList<>();
        if(ben != null && ben.getTakipciler() != null) {
            for (Ogrenci k : ben.getTakipciler()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", k.getId());
                map.put("adSoyad", k.getAdSoyad());
                map.put("bolum", k.getBolum());
                if(k.getProfilResmi() != null) map.put("profilResmi", k.getProfilResmiBase64());
                liste.add(map);
            }
        }
        return liste;
    }

    // 7. TAKİP EDİLENLERİ GETİR
    @GetMapping("/profil/{id}/takip-edilenler")
    public List<Map<String, Object>> takipEdilenleriGetir(@PathVariable Long id) {
        Ogrenci ben = ogrenciServisi.idIleGetir(id);
        List<Map<String, Object>> liste = new ArrayList<>();
        if(ben != null && ben.getTakipEdilenler() != null) {
            for (Ogrenci k : ben.getTakipEdilenler()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", k.getId());
                map.put("adSoyad", k.getAdSoyad());
                map.put("bolum", k.getBolum());
                if(k.getProfilResmi() != null) map.put("profilResmi", k.getProfilResmiBase64());
                liste.add(map);
            }
        }
        return liste;
    }
}