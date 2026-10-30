# HTML2Video v3.1

## v3.1 fix
- **Export video mein background ka zigzag light flicker theek kiya.** Page ka `#grain` (film grain) overlay har 0.1 second mein poori screen par
  zigzag jump karta tha; export mein har frame par noise ki position badalti thi. Ab export ke dauran adapter grain animation band rakhta hai
  (static grain rehta hai) aur export khatam hote hi wapas chalu ho jata hai. Preview/browser par page jaisa tha waisa hi rehta hai.

---

# HTML2Video v3 (Android Studio project)

## v3 mein naya kya hai
- **MovieBox jaisi scene-based HTML ab load hoti hai.** Pehle "NH API nahi mili" aata tha, kyunki woh HTML canvas nahi, DOM se bani hai.
  Ab app `assets/h2v_dom.js` adapter se us HTML ko `NH` API de deti hai (jin HTML mein `scenes`, `CAPTIONS`, `render()`, `TOTAL` hota hai).
  Canvas wali HTML (jaise Build tutorial) par adapter kuch nahi karta, woh pehle jaisi hi chalti hai.
- **Voice-over + music + sound effects ek hi MP4 mein.** HTML ka apna sound design (SFX aur background music) offline render hota hai
  aur Sarvam / Google / Phone voice-over ke saath mix hota hai.
- **Voice ke hisaab se timeline.** Har caption ki awaaz jitni lambi ho, utna us hisse ka visual slow ho jata hai, taaki awaaz aur visual sync rahein
  (isliye video 2:30 se lambi ho sakti hai).
- **Volume card:** Master, Voice-over, Music, Sound effects sliders (0 se 200%), **Auto-duck** (voice ke time music/SFX halke) aur Reset.
  Volume yaad rehti hai. Preview mein music + SFX ki volume turant badalti hai; final mix export mein lagta hai.
- Naye sample buttons: **Build tutorial** aur **MovieBox promo**.
- DOM HTML ka frame WebView se seedha 1920x1080 Bitmap mein capture hota hai (JPEG ke bina). Capture CPU par hota hai, isliye DOM HTML ke liye
  **30 fps** chuno (60 fps bahut slow hoga). Export ke dauran screen on rakho aur app ko foreground mein rakho.
- Purane sab features waise hi hain (Player/Editor, 9:16, Sarvam, Google, Phone TTS, Studio, Cancel, Share).

## DOM HTML ke liye rule (MovieBox style)
Page mein ye hona chahiye: `scenes` (har `section.scene` par `data-start`/`data-end`), `CAPTIONS` (scene id -> `[[time, text], ...]`),
`render()` (time se page ki state), `TOTAL`, aur `t`. Sound ke liye page ka `sfx`, `sfxReveal`, `CUES` use hota hai (agar ho).
Canvas wali HTML ke liye purana `window.NH` rule hi chalta hai (neeche).

---

# HTML2Video v2 (Android Studio project)

## v2 mein naya kya hai
- Premium dark UI: cards, gradient buttons, chips, progress bar aur ETA.
- **Player / Editor mode:** Player mein sirf 16:9 video canvas dikhta hai (pehle video player kat jata tha). Editor mein poora page full screen khulta hai, jahan marks aur text edit kar sakte ho.
- **Preview button:** video seedha app mein chalakar dekho.
- **Screenshot Studio** app ke andar bundled hai (zip ya screenshots load karo, marks lagao).
- Studio ke andar file chooser, mic record aur "Save HTML" ab kaam karte hain (downloads `Download/HTML2Video` mein jate hain).
- 30 ya 60 fps chuno. Pichla project ek tap mein dobara khulta hai. Export ke baad Share button.
- **9:16 Portrait (naya):** Export card mein "16:9 Landscape / 9:16 Portrait" chuno. 9:16 mein player screen bhi vertical ho jati hai aur MP4 **1080x1920** banti hai.
  - **Fit (blur background):** poora 16:9 frame beech mein, upar-neeche blur background (preview mein black bars dikhte hain, export mein blur lagta hai).
  - **Fill (crop):** poori vertical screen bharta hai, frame ke beech ka hissa rehta hai (side kat jati hai).
  - Choice yaad rehti hai. 16:9 export pehle jaisa hi hai.
- Purane sab features (Sarvam, Google, phone TTS, bina awaaz, Cancel, TTS settings) waise hi hain.


HTML animation ko **1920x1080, 60 fps MP4** video mein badalne wali Android app.
Awaaz phone ke apne Hindi TTS se seedha file mein banti hai, isliye "audio capture" wali koi dikkat nahi aati.

## Kaise kholein
1. Android Studio mein **File > Open** karke ye `HTML2Video` folder chuno.
2. Gradle sync hone do (internet chahiye). JDK 17 chahiye (Android Studio ke saath aata hai).
3. Phone (Android 10 ya upar) par Run karo. Code On the Go mein bhi build ho sakta hai.

## Kaise use karein
1. App kholo. **Sample (Firebase tutorial) kholo** dabao ya **HTML file chuno** se apni HTML do.
2. "Hindi AI awaaz" ON rakho.
3. **Export 1080p 60fps MP4** dabao. Progress aur baaki time dikhega.
4. Ho jane par video **Movies/HTML2Video** folder mein save hota hai. "Video kholo" se dekh sakte ho.

Export ke dauran screen on rakho aur app ko foreground mein rakho.
60 fps ke 7000+ frames banane mein kuch minute lagte hain (phone par depend karta hai).

## Free natural awaaz: Sarvam AI (recommended)
1. dashboard.sarvam.ai par sign up karo aur **API key** lo (signup par free credits milte hain; amount dashboard par dikhta hai).
2. App mein **"Sarvam AI: natural Hindi"** chuno, key daalo. Speaker (chhote letters mein): `ritu`, `shubh`, `priya`, `kavya`, `neha`, `rahul` ...
3. Export karo. Ek tutorial ke liye kuch hazaar characters lagte hain, to free credits mein kaafi videos ban jate hain.
Key sirf aapke phone mein save hoti hai. Kisi ko share mat karo.

## Google Cloud (optional, internet + API key)
Phone ki TTS awaaz thodi robotic hoti hai. Zyada natural awaaz ke liye app mein
**"Natural insaani awaaz (Google Cloud)"** chuno:
1. console.cloud.google.com par project banao, **Cloud Text-to-Speech API** enable karo, aur ek **API key** banao
   (billing account jodna padta hai; free monthly quota aam tutorial ke liye kaafi hota hai, limits Google ki pricing page par dekho).
2. API key app ke "Google Cloud API key" box mein daalo. Voice name: `hi-IN-Neural2-A` (female), `hi-IN-Neural2-B` (male),
   `hi-IN-Neural2-C` (male), `hi-IN-Neural2-D` (female). Naye Chirp3-HD voices Google ki voice list mein dekh sakte ho.
3. Export karo. Key sirf aapke phone mein save hoti hai. Apni key kisi ko share mat karo aur key ko sirf Text-to-Speech API tak restrict karo.

## Hindi voice nahi mili to (phone TTS)
Phone Settings > System > Language > Text-to-speech output > Google > Install voice data > Hindi.
Ya app ka "TTS settings kholo" button dabao.

## HTML ke liye zaroori rule
Aapki HTML mein ye `window.NH` object hona chahiye (master prompt ka template ise already deta hai):
- `NH.lang` (jaise "hi-IN")
- `NH.narration()` : JSON string, har step ka `{a, b}` text
- `NH.setDurations(json)` : awaaz ki length lekar timeline banata hai, `{total, events}` lauta ta hai
- `NH.run(fps)` : frames banata hai aur `Android.pushFrame(dataUrl)` ko bhejta hai
  (aakhir mein `Android.done()`, error par `Android.fail(msg)`)

## Files
- `app/src/main/java/com/nhbox/htmltovideo/MainActivity.kt` : screen, WebView, JS bridge, save
- `TtsRenderer.kt` : Hindi awaaz ko WAV mein banata hai
- `AudioUtils.kt` : WAV padhna, 48 kHz mono, mixing
- `VideoExporter.kt` : H.264 (1080p60) + AAC ko MP4 mein likhta hai
- `app/src/main/assets/sample_firebase.html` : test ke liye sample
- `app/src/main/assets/h2v_dom.js` : (v3) DOM HTML ke liye NH adapter (narration, timeline, music + SFX render, frame stepping)
- `app/src/main/assets/moviebox_promo.html`, `build_tutorial_video.html` : (v3) sample HTML
