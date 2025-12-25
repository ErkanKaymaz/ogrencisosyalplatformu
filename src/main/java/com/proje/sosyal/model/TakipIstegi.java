package com.proje.sosyal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class TakipIstegi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "istek_atan_id")
    private Ogrenci istekAtan;

    @ManyToOne
    @JoinColumn(name = "istek_alan_id")
    private Ogrenci istekAlan;

    private LocalDateTime tarih;

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Ogrenci getIstekAtan() { return istekAtan; }
    public void setIstekAtan(Ogrenci istekAtan) { this.istekAtan = istekAtan; }
    public Ogrenci getIstekAlan() { return istekAlan; }
    public void setIstekAlan(Ogrenci istekAlan) { this.istekAlan = istekAlan; }
    public LocalDateTime getTarih() { return tarih; }
    public void setTarih(LocalDateTime tarih) { this.tarih = tarih; }
}