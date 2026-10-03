# DeployForge

**Web Tabanlı CI/CD Pipeline Yönetim, Otomatik Dağıtım ve Analiz Platformu**

DeployForge, bir GitHub deposundaki kod değişikliğinin test edilmesinden Docker imajının hedef sunucuda çalıştırılmasına kadar olan süreci tek panelden izlemeyi amaçlayan bitirme projesidir. Temel teknoloji tercihleri Spring Boot, PostgreSQL, GitHub Webhooks, Docker ve GitHub Actions'tır.

### Problem ve amaç

Küçük projelerde GitHub Actions çalışmaları, Docker imajları ve sunucudaki uygulama durumu farklı yerlerden takip edilir. Bir dağıtım başarısız olduğunda hangi commit'in hangi imajı ürettiğini, hangi adımın hata verdiğini ve hedef ortamda hangi sürümün çalıştığını bulmak zorlaşır.

DeployForge'un amacı yeni bir genel amaçlı CI motoru yazmak değil, mevcut araçların sonuçlarını birleştirmek ve kontrollü dağıtımı yönetmektir. Öğrenci projesi kapsamında hedef, bir depo ve bir hedef sunucu üzerinden uçtan uca gösterilebilen, daha sonra birden fazla projeye genişletilebilen bir sistemdir.

Başlıca kullanıcı, kendi uygulamasını yöneten geliştiricidir. Değerlendirme sırasında sistemin olay takibi, yetkilendirme, hata yönetimi ve dağıtım güvenilirliği somut senaryolarla gösterilebilmelidir.

### Sorumluluk paylaşımı

- **GitHub Actions:** Kaynak kodu checkout eder, test eder, imajı üretir ve registry'ye gönderir. DeployForge kaynak kod derlemez.
- **Webhook modülü:** Ham istek gövdesinin imzasını doğrular, olay türünü ve izin verilen depoyu kontrol eder, teslimatı kalıcılaştırır.
- **Pipeline modülü:** GitHub çalışma kimliği ve yeniden deneme numarası üzerinden çalışma kaydını günceller. Gerekli bilgileri GitHub API'den doğrular.
- **Dağıtım modülü:** Uygunluk ve yetki kontrolü yapar; aynı ortamda eşzamanlı dağıtımı önler; işi arka planda yürütür.
- **Analiz modülü:** Kayıtlı sonuçlardan özet üretir. GitHub Actions dışındaki geliştirme süresini ölçmez.
- **Güvenlik modülü:** Panel oturumunu, rollerini ve işlem yetkilerini yönetir.

### Olay ve dağıtım akışı

1. Geliştirici izin verilen dala commit gönderir; Actions workflow'u başlar.
2. Workflow testleri çalıştırır ve başarılıysa imajı GHCR'a gönderir. İmaj commit etiketiyle ilişkilendirilir; değişmez digest sonucu kaydedilir.
3. GitHub, `workflow_run` olayını DeployForge'a yollar. Backend, `X-Hub-Signature-256` değerini ham gövde üzerinden HMAC-SHA256 ile kontrol eder.
4. Geçerli olay ve teslimat kimliği aynı işlemde kaydedilir. Kayıt gerçekleşmeden başarı yanıtı verilmez. Geçici veritabanı hatası yeniden teslimata elverişli hata yanıtı üretir.
5. Arka plan işleyicisi GitHub API'den çalışma sonucunu doğrular; imaj bilgisini yetkili workflow çıktısından edinir ve güvenilir depoyla eşleştirir.
6. Yönetici dağıtımı başlatır. Backend depoyu, dalı, workflow kimliğini, commit'i, sonucu ve imaj digest'ini yeniden doğrular.
7. Hedefte kurulu sabit dağıtım betiği imajı digest ile çeker, servisi günceller ve süre sınırlı sağlık kontrolü yapar.
8. Başarılı sonuç ve çalışan digest kaydedilir. Başarısızlıkta önceki digest'e geri dönüş denenir; geri dönüşün sonucu ayrıca kaydedilir.

Webhook isteğinde SSH veya uzun API çağrıları yürütülmez. Başlangıçta mesaj kuyruğu yerine PostgreSQL'de kalıcı iş kayıtları ve sınırlı bir arka plan işleyicisi yeterlidir. İşler zaman aşımı ve sahiplik süresiyle tutulmalı; uygulama yeniden başladığında yarım kalan işlerin hedef durumu kontrol edilmelidir. Dağıtım komutu körlemesine tekrar edilmemelidir.

### Kullanım senaryosu

Hedeflenen demo, küçük bir örnek Spring Boot uygulaması üzerinden yürütülür:

1. Yönetici panelde test deposunu ve staging ortamını tanımlar.
2. Geçerli bir commit Actions testlerini geçirir; pipeline panelde başarılı görünür.
3. Yönetici ilgili çalışma için dağıtım ister; panel sıraya alınma, yürütülme ve son durumu gösterir.
4. Hedef uygulamanın sağlık kontrolü geçer; panel çalışan sürümü commit ve digest ile ilişkilendirir.
5. Bozuk test içeren commit dağıtım adayına dönüşmez.
6. Sağlık kontrolünü bozan bir sürüm başarısız dağıtım olarak görünür; geri dönüş denenir ve sonucu gösterilir.
7. Aynı webhook yeniden gönderildiğinde tek mantıksal kayıt oluşur.

GitHub yeniden çalıştırmaları ayrı run attempt olarak ele alınır. Eksik, gecikmiş veya sırası değişmiş olaylar mevcut kaydı eski bir duruma düşürmemelidir. GitHub API kesintileri panelde belirsiz/yeniden denenecek olarak görünür; başarı varsayılmaz.


