package com.proje.sosyal.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Lazy;
// --- YENİ EKLENEN İMPORTLAR (Bunlar Web'i Bozmaz) ---
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
// ----------------------------------------------------
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proje.sosyal.model.Ogrenci;
import com.proje.sosyal.model.TakipIstegi;
import com.proje.sosyal.repository.OgrenciRepository;
import com.proje.sosyal.repository.TakipIstegiRepository;

@Service
public class OgrenciServisi implements UserDetailsService {

    private final OgrenciRepository ogrenciDeposu;
    private final PasswordEncoder passwordEncoder;
    private final TakipIstegiRepository istekRepo;

    public OgrenciServisi(OgrenciRepository ogrenciDeposu, @Lazy PasswordEncoder passwordEncoder, TakipIstegiRepository istekRepo) {
        this.ogrenciDeposu = ogrenciDeposu;
        this.passwordEncoder = passwordEncoder;
        this.istekRepo = istekRepo;
    }

    // =================================================================
    //  WEB VE MOBİL İÇİN GİRİŞ FONKSİYONU (ESKİ HALİ - DOKUNULMADI)
    // =================================================================
    @Override
    public UserDetails loadUserByUsername(String kullaniciAdi) throws UsernameNotFoundException {
        Ogrenci ogrenci = ogrenciDeposu.findByKullaniciAdi(kullaniciAdi)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı"));

        return new User(
                ogrenci.getKullaniciAdi(),
                ogrenci.getSifre(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + ogrenci.getRol()))
        );
    }
    
    // =================================================================
    //  YENİ EKLEME: SİSTEMDEKİ AKTİF KULLANICIYI BULMA
    //  (Bu kod Web girişinde de Mobil girişinde de çalışır, bozmaz)
    // =================================================================
    public Ogrenci getAktifOgrenci() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String kadi = auth.getName(); 
        return ogrenciDeposu.findByKullaniciAdi(kadi).orElse(null);
    }

    // =================================================================
    //  SENİN ESKİ FONKSİYONLARININ HEPSİ BURADA (AYNEN KORUNDU)
    // =================================================================

    public void konumGuncelle(Ogrenci ogrenci, Double enlem, Double boylam) {
        ogrenci.setEnlem(enlem);
        ogrenci.setBoylam(boylam);
        ogrenciDeposu.save(ogrenci);
    }

    public void kayitOl(Ogrenci ogrenci) {
        ogrenci.setSifre(passwordEncoder.encode(ogrenci.getSifre()));
        ogrenciDeposu.save(ogrenci);
    }

    public boolean kullaniciAdiVarMi(String kAdi) {
        return ogrenciDeposu.findByKullaniciAdi(kAdi).isPresent();
    }

    public Ogrenci mevcutOgrenciyiGetir(String kullaniciAdi) {
        return ogrenciDeposu.findByKullaniciAdi(kullaniciAdi).orElse(null);
    }

    public Ogrenci idIleGetir(Long id) {
        return ogrenciDeposu.findById(id).orElse(null);
    }

    public void guncelle(Ogrenci ogrenci) {
        ogrenciDeposu.save(ogrenci); 
    }
 
    @Transactional
    public void takipEt(Long takipEdenId, Long takipEdilecekId) {
        Optional<Ogrenci> takipEden = ogrenciDeposu.findById(takipEdenId);
        Optional<Ogrenci> takipEdilecek = ogrenciDeposu.findById(takipEdilecekId);

        if (takipEden.isPresent() && takipEdilecek.isPresent()) {
            Ogrenci eden = takipEden.get();
            Ogrenci edilen = takipEdilecek.get();

            if (eden.getTakipEdilenler().contains(edilen)) return; 
            if (istekRepo.existsByIstekAtanAndIstekAlan(eden, edilen)) return; 

            TakipIstegi istek = new TakipIstegi();
            istek.setIstekAtan(eden);
            istek.setIstekAlan(edilen);
            istek.setTarih(java.time.LocalDateTime.now());
            istekRepo.save(istek);
        }
    }

    @Transactional
    public void istekKabul(Long istekId) {
        TakipIstegi istek = istekRepo.findById(istekId).orElse(null);
        if(istek != null) {
            Ogrenci atan = istek.getIstekAtan();
            Ogrenci alan = istek.getIstekAlan();
            atan.getTakipEdilenler().add(alan);
            ogrenciDeposu.save(atan);
            istekRepo.delete(istek);
        }
    }

    @Transactional
    public void istekReddet(Long istekId) {
        istekRepo.deleteById(istekId);
    }
    
    public List<TakipIstegi> gelenIstekleriGetir(Ogrenci ben) {
        return istekRepo.findByIstekAlan(ben);
    }

    public void profilGuncelle(Ogrenci ogrenci, String yeniAd, String yeniBolum, String yeniSifre) {
        ogrenci.setAdSoyad(yeniAd);
        ogrenci.setBolum(yeniBolum);
        if (yeniSifre != null && !yeniSifre.trim().isEmpty()) {
            ogrenci.setSifre(passwordEncoder.encode(yeniSifre));
        }
        ogrenciDeposu.save(ogrenci);
    }

    @Transactional
    public void ogrenciyiSil(Long id) {
        ogrenciDeposu.begenileriSil(id);
        ogrenciDeposu.takipleriSil(id);
        ogrenciDeposu.yorumlariSil(id);
        ogrenciDeposu.bildirimleriSil(id);
        ogrenciDeposu.mesajlariSil(id);
        ogrenciDeposu.notlariSil(id);
        ogrenciDeposu.deleteById(id);
    }

    public void takibiBirak(Long benId, Long hedefId) {
        Ogrenci ben = ogrenciDeposu.findById(benId).orElse(null);
        Ogrenci hedef = ogrenciDeposu.findById(hedefId).orElse(null);
        if (ben != null && hedef != null) {
            ben.getTakipEdilenler().remove(hedef);
            ogrenciDeposu.save(ben);
        }
    }

    public void takipciyiCikar(Long benId, Long hedefTakipciId) {
        Ogrenci ben = ogrenciDeposu.findById(benId).orElse(null);
        Ogrenci takipci = ogrenciDeposu.findById(hedefTakipciId).orElse(null);
        if (ben != null && takipci != null) {
            takipci.getTakipEdilenler().remove(ben);
            ogrenciDeposu.save(takipci);
        }
    }
}