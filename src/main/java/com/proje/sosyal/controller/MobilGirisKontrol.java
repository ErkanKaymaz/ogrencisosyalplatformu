package com.proje.sosyal.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.proje.sosyal.model.Ogrenci;
import com.proje.sosyal.service.OgrenciServisi;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class MobilGirisKontrol {

    @Autowired
    private OgrenciServisi ogrenciServisi;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/giris")
    public Map<String, Object> girisYap(@RequestBody Map<String, String> bilgiler) {
        String kadi = bilgiler.get("kullaniciAdi");
        String sifre = bilgiler.get("sifre");

        Map<String, Object> cevap = new HashMap<>();
        
        // DİKKAT: Servisindeki metodun adı "mevcutOgrenciyiGetir" mi yoksa "getKullaniciAdi" mı?
        // Senin attığın OgrenciServisi kodunda "mevcutOgrenciyiGetir" vardı.
        Ogrenci ogrenci = ogrenciServisi.mevcutOgrenciyiGetir(kadi);

        if (ogrenci != null && passwordEncoder.matches(sifre, ogrenci.getSifre())) {
            cevap.put("basarili", true);
            cevap.put("kullaniciId", ogrenci.getId());
            
            // *** İŞTE BU SATIR ÇOK ÖNEMLİ ***
            // Veritabanındaki rol bilgisini mobile gönderiyoruz.
            String rol = (ogrenci.getRol() != null) ? ogrenci.getRol() : "USER";
            cevap.put("rol", rol); 
            // *******************************
            
            cevap.put("mesaj", "Giriş Başarılı");
        } else {
            cevap.put("basarili", false);
            cevap.put("mesaj", "Kullanıcı adı veya şifre hatalı!");
        }
        return cevap;
    }
}