# Icon Changer

برنامه اندروید برای ساخت میانبرهای سفارشی روی صفحه اصلی، با آیکون و نام دلخواه.

## امکانات
- فهرست همه برنامه‌های نصب‌شده
- انتخاب آیکون از گالری یا از بسته آیکون پیکسلی
- تغییر نام میانبر
- ساخت میانبر روی صفحه اصلی

## بسته آیکون پیکسلی
آیکون‌های Discord، Reddit، Twitter، Snapchat، WhatsApp، Twitch، YouTube، Facebook و Instagram. اگر برنامه‌ای از این‌ها نصب باشد، آیکون پیکسلی‌اش خودکار انتخاب می‌شود.

فایل‌های آیکون باید با این نام‌ها در مسیر `app/src/main/res/drawable-nodpi/` باشند:
`pix_discord.png`, `pix_reddit.png`, `pix_twitter.png`, `pix_snapchat.png`, `pix_whatsapp.png`, `pix_twitch.png`, `pix_youtube.png`, `pix_facebook.png`, `pix_instagram.png`

## ساختار
- `app/src/main/java/com/example/iconchanger/MainActivity.kt`: کد برنامه
- `app/src/main/AndroidManifest.xml`: مانیفست
- `app/src/main/res/drawable-nodpi/`: آیکون‌ها

## ساخت پروژه
یک پروژه Empty Activity با Compose در Android Studio بساز (Package name: `com.example.iconchanger`، Minimum SDK 26) و فایل‌های بالا را جایگزین فایل‌های پروژه کن.

## محدودیت
آیکون اصلی برنامه‌ها عوض نمی‌شود. یک میانبر جدید با آیکون دلخواه ساخته می‌شود.
