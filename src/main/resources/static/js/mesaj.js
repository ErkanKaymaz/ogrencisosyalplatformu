let stompClient = null;
let seciliAliciId = null;
let seciliResimBase64 = null; // Resim verisini tutacak değişken

function baglan(benimId) {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function (frame) {
        console.log('Bağlandı: ' + frame);
        stompClient.subscribe('/topic/user-' + benimId, function (mesaj) {
            const m = JSON.parse(mesaj.body);
            // Mesajı göster (Basitlik için ID kontrolü yapmıyoruz, geliştirilebilir)
            ekranaYaz(m.metin, m.resimData, true);
        });
    });
}

function sohbetSec(element) {
    const aliciId = element.getAttribute("data-id");
    const aliciAdi = element.getAttribute("data-ad");

    // Aktif sınıfını güncelle (Kırmızı şerit için)
    document.querySelectorAll('.user-card').forEach(el => el.classList.remove('active'));
    element.classList.add('active');
    
    seciliAliciId = aliciId;
    document.getElementById("chat-header-name").innerHTML = `<span class="fw-bold text-burgundy">${aliciAdi}</span>`;
    document.getElementById("chat-box").innerHTML = ""; 
    
    fetch('/api/mesajlar/' + aliciId)
        .then(response => response.json())
        .then(mesajlar => {
            mesajlar.forEach(m => {
                const gelenMi = (m.gonderen.id != window.benimIdGlobal); 
                
                let resimSrc = null;
                if (m.resimBase64) {
                    resimSrc = "data:image/png;base64," + m.resimBase64;
                }
                ekranaYaz(m.mesajMetni, resimSrc, gelenMi);
            });
            scroolAsagi();
        });
}

// Resim seçilince çalışır
function resimSecildi(input) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            seciliResimBase64 = e.target.result; 
            document.getElementById("mesaj-input").placeholder = "Resim seçildi. Göndermek için tıkla...";
            document.getElementById("mesaj-input").focus();
        }
        reader.readAsDataURL(input.files[0]);
    }
}

function mesajGonder() {
    const girdi = document.getElementById("mesaj-input");
    const metin = girdi.value;

    if ((metin.trim() !== "" || seciliResimBase64 !== null) && seciliAliciId !== null) {
        const mesajVerisi = {
            'aliciId': seciliAliciId,
            'metin': metin,
            'resimData': seciliResimBase64 
        };
        
        stompClient.send("/app/sohbet", {}, JSON.stringify(mesajVerisi));
        
        // Kendi ekranıma yaz
        ekranaYaz(metin, seciliResimBase64, false);
        
        // Temizlik
        girdi.value = "";
        girdi.placeholder = "Bir mesaj yaz...";
        seciliResimBase64 = null; 
        document.getElementById("resim-input").value = ""; 
        
        scroolAsagi();
    } else if (seciliAliciId === null) {
        alert("Lütfen sohbet etmek istediğiniz bir kişiyi seçin.");
    }
}

function ekranaYaz(metin, resimSrc, gelenMi) {
    const kutu = document.getElementById("chat-box");
    const div = document.createElement("div");
    
    // --- İŞTE DÜZELTİLEN KISIM ---
    div.classList.add("msg"); // 'message' YERİNE 'msg' (CSS ile eşleşti)
    
    div.classList.add(gelenMi ? "incoming" : "outgoing");
    
    let icerikHTML = "";
    
    // Resim varsa ekle (Max Yükseklik Verildi)
    if (resimSrc) {
        icerikHTML += `<img src="${resimSrc}" style="max-height: 250px; width: auto; border-radius: 8px; margin-bottom: 5px; display: block;">`;
    }
    
    // Metin varsa ekle
    if (metin) {
        icerikHTML += `<span>${metin}</span>`;
    }
    
    div.innerHTML = icerikHTML;
    kutu.appendChild(div);
    scroolAsagi();
}

function scroolAsagi() {
    const kutu = document.getElementById("chat-box");
    if(kutu) kutu.scrollTop = kutu.scrollHeight;
}

document.addEventListener("DOMContentLoaded", function() {
    const input = document.getElementById("mesaj-input");
    if(input) {
        input.addEventListener("keypress", function(event) {
            if (event.key === "Enter") {
                mesajGonder();
            }
        });
    }
});