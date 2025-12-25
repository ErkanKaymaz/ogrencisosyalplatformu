package com.proje.sosyal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proje.sosyal.model.Ogrenci;
import com.proje.sosyal.model.TakipIstegi;

public interface TakipIstegiRepository extends JpaRepository<TakipIstegi, Long> {
    // Bana gelen istekleri bul
    List<TakipIstegi> findByIstekAlan(Ogrenci istekAlan);
    
    // Zaten istek atmış mıyım kontrolü
    boolean existsByIstekAtanAndIstekAlan(Ogrenci atan, Ogrenci alan);
    
    // İsteği bulmak için
    TakipIstegi findByIstekAtanAndIstekAlan(Ogrenci atan, Ogrenci alan);
}