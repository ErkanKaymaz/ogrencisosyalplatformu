package com.proje.sosyal.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.proje.sosyal.model.Gonderi;
import com.proje.sosyal.model.Ogrenci;
import com.proje.sosyal.model.Yorum;
import com.proje.sosyal.repository.GonderiRepository;
import com.proje.sosyal.repository.YorumRepository; 

@Service
public class GonderiServisi {

    private final GonderiRepository gonderiDeposu;
    private final YorumRepository yorumDeposu; 


    public GonderiServisi(GonderiRepository gonderiDeposu, YorumRepository yorumDeposu) {
        this.gonderiDeposu = gonderiDeposu;
        this.yorumDeposu = yorumDeposu;
    }

    public void gonderiPaylas(String metin, MultipartFile resimDosyasi, Ogrenci ogrenci) {
        try {
            Gonderi gonderi = new Gonderi();
            gonderi.setMetin(metin);
            gonderi.setTarih(LocalDateTime.now());
            gonderi.setOgrenci(ogrenci);

            if (resimDosyasi != null && !resimDosyasi.isEmpty()) {
                gonderi.setResim(resimDosyasi.getBytes());
            }

            gonderiDeposu.save(gonderi);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Gonderi> takipEdilenlerinGonderileriniGetir(Ogrenci ogrenci) {
        return gonderiDeposu.findByOgrenciInOrOgrenciOrderByTarihDesc(ogrenci.getTakipEdilenler(), ogrenci);
    }
    
    public List<Gonderi> ogrencininGonderileriniGetir(Ogrenci ogrenci) {
        return gonderiDeposu.findByOgrenciOrderByTarihDesc(ogrenci);
    }

    public void begeniYap(Long gonderiId, Ogrenci begenenKisi) {
        Gonderi gonderi = gonderiDeposu.findById(gonderiId).orElse(null);
        if(gonderi != null) {
            if (gonderi.getOgrenci().getId().equals(begenenKisi.getId())) {
                return; 
            }
            if (gonderi.getBegenenler().contains(begenenKisi)) {
                gonderi.getBegenenler().remove(begenenKisi);
            } else {
                gonderi.getBegenenler().add(begenenKisi);
            }
            gonderiDeposu.save(gonderi);
        }
    }

    public void yorumYap(Long gonderiId, Ogrenci yazar, String icerik, MultipartFile resimDosyasi) {
        Gonderi gonderi = gonderiDeposu.findById(gonderiId).orElse(null);
        if(gonderi != null) {
            Yorum yorum = new Yorum();
            yorum.setGonderi(gonderi);
            yorum.setYazar(yazar);
            yorum.setIcerik(icerik);
            yorum.setTarih(LocalDateTime.now());
            
            if (resimDosyasi != null && !resimDosyasi.isEmpty()) {
                try {
                    yorum.setResim(resimDosyasi.getBytes());
                } catch (Exception e) { e.printStackTrace(); }
            }
            
            yorumDeposu.save(yorum);
        }
    }

   
    public void gonderiSil(Long gonderiId, Ogrenci talepEden) {
        Gonderi gonderi = gonderiDeposu.findById(gonderiId).orElse(null);
        if (gonderi != null && gonderi.getOgrenci().getId().equals(talepEden.getId())) {
            gonderiDeposu.delete(gonderi);
        }
    }

    public void yorumSil(Long gonderiId, Long yorumId, Ogrenci talepEden) {
  
        Yorum yorum = yorumDeposu.findById(yorumId).orElse(null);
        
        if (yorum != null) {

            Gonderi gonderi = yorum.getGonderi();
            

            if (gonderi.getOgrenci().getId().equals(talepEden.getId())) {
                
 
                yorumDeposu.delete(yorum);
            }
        }
    }
}