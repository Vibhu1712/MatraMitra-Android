# मात्रा मित्र — Android app

A small offline Android app that wraps the Matra Mitra web page in a WebView.
The page and fonts are bundled in the app, so it works without internet.
Speech uses the phone's own text-to-speech engine.

- Targets Android 16 (API 36), as Google Play requires for new apps from 31 Aug 2026.
- Runs on Android 7.0 and newer.

## 1. Test build (APK for your own phone)

Every push to GitHub runs **Actions → Build APK**. Download the **matra-mitra-apk** artifact,
unzip it and install `app-debug.apk` on the phone.

## 2. Create your upload key (once, keep it safe forever)

Google Play needs every upload signed with the same private key. Create it on your computer.
`keytool` comes with Java or Android Studio
(Windows: `C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe`).

```
keytool -genkeypair -v -keystore upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

Choose a password and answer the name questions. Back up `upload.jks` and the password somewhere
safe (not in this repository). If you lose it you can ask Google to reset it, but it takes time.

Turn the key into text so GitHub can store it. Windows PowerShell:

```
[Convert]::ToBase64String([IO.File]::ReadAllBytes("upload.jks")) | Set-Content upload.b64
```

(Linux/macOS: `base64 -w0 upload.jks > upload.b64`)

## 3. Give GitHub the key as secrets

Repository → **Settings → Secrets and variables → Actions → New repository secret**. Add four:

| Name | Value |
|---|---|
| `KEYSTORE_BASE64` | the whole content of `upload.b64` |
| `KEYSTORE_PASSWORD` | the keystore password |
| `KEY_ALIAS` | `upload` |
| `KEY_PASSWORD` | the key password (same as keystore password unless you set a different one) |

Then **Actions → Build APK → Run workflow**. The run now also produces
**matra-mitra-play-bundle** containing `app-release.aab`, which is the file you upload to Play.

## 4. Privacy policy page

Play needs a public privacy policy link. Edit the email in `docs/privacy-policy.html`, then turn on
**Settings → Pages → Deploy from a branch → main → /docs**. The link becomes
`https://vibhu1712.github.io/MatraMitra-Android/privacy-policy.html`.

## 5. Every update

Raise `versionCode` (and `versionName`) in `app/build.gradle`, push, and upload the new `.aab`.
Play rejects a bundle whose versionCode was already used.

## Store listing material

`play-store/` has the 512×512 icon, the 1024×500 feature graphic, five phone screenshots and
ready-to-paste listing text.

## Font licences

Baloo 2 and Tiro Devanagari Hindi are used under the SIL Open Font License; the licence texts are
in `app/src/main/assets/fonts/`.
