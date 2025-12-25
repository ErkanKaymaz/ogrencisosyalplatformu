package com.proje.sosyal.controller;

import com.proje.sosyal.model.Bildirim;
import com.proje.sosyal.model.Ogrenci;
import com.proje.sosyal.repository.BildirimRepository;
import com.proje.sosyal.service.OgrenciServisi; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bildirimler")
public class BildirimController {

    @Autowired
    private BildirimRepository bildirimRepository;

    @Autowired
    private OgrenciServisi ogrenciServisi;

    // DEĞİŞİKLİK BURADA: Artık ID'yi parametre olarak alıyoruz.
    @GetMapping
    public List<Bildirim> getBildirimler(@RequestParam Long kullaniciId) {
        // ID ile öğrenciyi bul
        Ogrenci ogrenci = ogrenciServisi.idIleGetir(kullaniciId);
        if (ogrenci == null) return List.of(); // Öğrenci yoksa boş liste dön
        
        return bildirimRepository.findByOgrenciOrderByTarihDesc(ogrenci);
    }

    @DeleteMapping("/{id}")
    public void bildirimiSil(@PathVariable Long id) {
        bildirimRepository.deleteById(id);
    }
}