package com.proje.sosyal.controller;

import java.time.LocalDateTime;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proje.sosyal.model.Bildirim;
import com.proje.sosyal.model.Gonderi;

import com.proje.sosyal.repository.BildirimRepository;
import com.proje.sosyal.repository.GonderiRepository;
import com.proje.sosyal.repository.OgrenciRepository;
import com.proje.sosyal.repository.YorumRepository; // BU EKLENDİ
import com.proje.sosyal.service.OgrenciServisi;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminKontrol {

    private final OgrenciRepository ogrenciRepo;
    private final GonderiRepository gonderiRepo;
    private final BildirimRepository bildirimRepo;
    private final OgrenciServisi ogrenciServisi;
    private final YorumRepository yorumRepo; // BU EKLENDİ

    // Constructor (Kurucu) Güncellendi
    public AdminKontrol(OgrenciRepository ogrenciRepo, GonderiRepository gonderiRepo, 
                        BildirimRepository bildirimRepo, OgrenciServisi ogrenciServisi, 
                        YorumRepository yorumRepo) {
        this.ogrenciRepo = ogrenciRepo;
        this.gonderiRepo = gonderiRepo;
        this.bildirimRepo = bildirimRepo;
        this.ogrenciServisi = ogrenciServisi;
        this.yorumRepo = yorumRepo;
    }

    @GetMapping
    public String adminPaneli(Model model) {
        model.addAttribute("tumOgrenciler", ogrenciRepo.findAll());
        model.addAttribute("tumGonderiler", gonderiRepo.findAllByOrderByTarihDesc());
        
        // YORUMLARI HTML'E GÖNDERİYORUZ
        model.addAttribute("tumYorumlar", yorumRepo.findAll()); 
        
        return "admin";
    }

    @GetMapping("/kullanici-sil/{id}")
    public String kullaniciSil(@PathVariable Long id) {
        ogrenciServisi.ogrenciyiSil(id);
        return "redirect:/admin";
    }

    @PostMapping("/gonderi-sil")
    public String gonderiSil(@RequestParam Long gonderiId, @RequestParam String sebep) {
        Gonderi gonderi = gonderiRepo.findById(gonderiId).orElse(null);
        
        if(gonderi != null) {
            // Bildirim oluştur
            Bildirim bildirim = new Bildirim();
            bildirim.setOgrenci(gonderi.getOgrenci());
            bildirim.setTarih(LocalDateTime.now());
            bildirim.setMesaj("UYARI: Bir gönderiniz yöneticiler tarafından kaldırıldı. Sebep: " + sebep);
            bildirimRepo.save(bildirim);
            
            // Gönderiyi sil
            gonderiRepo.delete(gonderi);
        }
        return "redirect:/admin";
    }

 // YORUM SİLME (GÜNCELLENDİ: Bildirimli ve Sebepli)
    @PostMapping("/yorum-sil")
    public String yorumSil(@RequestParam Long yorumId, @RequestParam String sebep) {
        com.proje.sosyal.model.Yorum yorum = yorumRepo.findById(yorumId).orElse(null);
        
        if(yorum != null) {
            // 1. Yorum Sahibine Bildirim Gönder
            Bildirim bildirim = new Bildirim();
            bildirim.setOgrenci(yorum.getYazar());
            bildirim.setTarih(LocalDateTime.now());
            
            // Yorumun içeriği boşsa (sadece resimse) "Görsel" yazsın
            String yorumOzeti = (yorum.getIcerik() != null && !yorum.getIcerik().isEmpty()) 
                                ? yorum.getIcerik() 
                                : "[Görsel Yorum]";
                                
            bildirim.setMesaj("UYARI: '" + yorumOzeti + "' içeriğindeki yorumunuz kaldırıldı. Sebep: " + sebep);
            bildirimRepo.save(bildirim);
            
            // 2. Yorumu Sil
            yorumRepo.delete(yorum);
        }
        return "redirect:/admin";
    }
}