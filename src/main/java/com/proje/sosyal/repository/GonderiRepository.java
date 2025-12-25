package com.proje.sosyal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proje.sosyal.model.Gonderi;
import com.proje.sosyal.model.Ogrenci;

public interface GonderiRepository extends JpaRepository<Gonderi, Long> {
    
    // Ana Sayfa Akışı İçin (Ben + Takip Ettiklerim)
    List<Gonderi> findByOgrenciInOrOgrenciOrderByTarihDesc(List<Ogrenci> takipEdilenler, Ogrenci ben);
    
    // Profil Sayfası İçin (Sadece Ben)
    List<Gonderi> findByOgrenciOrderByTarihDesc(Ogrenci ogrenci);
    
    // --- İŞTE EKSİK OLAN METOD (ADMİN İÇİN) ---
    List<Gonderi> findAllByOrderByTarihDesc();
}