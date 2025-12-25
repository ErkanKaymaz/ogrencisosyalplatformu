package com.proje.sosyal.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.proje.sosyal.model.Ogrenci;
import com.proje.sosyal.service.GonderiServisi;
import com.proje.sosyal.service.OgrenciServisi;

@Controller // BU ÇOK ÖNEMLİ! YOKSA SAYFA AÇILMAZ.
public class ProfilKontrol {

    private final OgrenciServisi ogrenciServisi;
    private final GonderiServisi gonderiServisi;

    public ProfilKontrol(OgrenciServisi ogrenciServisi, GonderiServisi gonderiServisi) {
        this.ogrenciServisi = ogrenciServisi;
        this.gonderiServisi = gonderiServisi;
    }

    @GetMapping("/profil")
    public String profilSayfasi(Model model, Principal principal) {
        Ogrenci ben = ogrenciServisi.mevcutOgrenciyiGetir(principal.getName());
        model.addAttribute("ben", ben);
        model.addAttribute("gonderilerim", gonderiServisi.ogrencininGonderileriniGetir(ben));
        
        // YENİ: Gelen istekleri de gönder
        model.addAttribute("istekler", ogrenciServisi.gelenIstekleriGetir(ben));
        
        return "profil";
    }

    // YENİ METODLAR
    @GetMapping("/istek-kabul/{id}")
    public String istekKabul(@PathVariable Long id) {
        ogrenciServisi.istekKabul(id);
        return "redirect:/profil";
    }

    @GetMapping("/istek-reddet/{id}")
    public String istekReddet(@PathVariable Long id) {
        ogrenciServisi.istekReddet(id);
        return "redirect:/profil";
    }

    // TAKİBİ BIRAKMA
    @GetMapping("/takip-birak/{id}")
    public String takibiBirak(@PathVariable Long id, Principal principal) {
        Ogrenci ben = ogrenciServisi.mevcutOgrenciyiGetir(principal.getName());
        ogrenciServisi.takibiBirak(ben.getId(), id);
        return "redirect:/profil";
    }

    // TAKİPÇİYİ ÇIKARMA
    @GetMapping("/takipci-cikar/{id}")
    public String takipciCikar(@PathVariable Long id, Principal principal) {
        Ogrenci ben = ogrenciServisi.mevcutOgrenciyiGetir(principal.getName());
        ogrenciServisi.takipciyiCikar(ben.getId(), id);
        return "redirect:/profil";
    }
    @GetMapping("/profil/gonderi-sil/{id}")
    public String gonderiSil(@PathVariable Long id, Principal principal) {
        Ogrenci ben = ogrenciServisi.mevcutOgrenciyiGetir(principal.getName());
        gonderiServisi.gonderiSil(id, ben);
        return "redirect:/profil";
    }
 // YORUM SİL (Hangi gönderiden ve hangi yorum)
    @GetMapping("/profil/yorum-sil/{gonderiId}/{yorumId}")
    public String yorumSil(@PathVariable Long gonderiId, @PathVariable Long yorumId, Principal principal) {
        Ogrenci ben = ogrenciServisi.mevcutOgrenciyiGetir(principal.getName());
        gonderiServisi.yorumSil(gonderiId, yorumId, ben);
        return "redirect:/profil";
    }
}