# Stillwords — Release & Signing Guide

This guide describes how to configure signing, generate production builds, and release updates to Google Play for **Stillwords**.

---

## 1. Keystore Configuration

Release builds require a dedicated signing keystore. **Never commit keystores or passwords to version control.** Both `keystore.properties` and `*.jks` / `*.keystore` files are strictly ignored by `.gitignore`.

### A. Generating the Upload Keystore
Run the following `keytool` command in your terminal (included with JDK 17+):

```bash
keytool -genkey -v -keystore stillwords-upload-key.jks -alias stillwords-upload -keyalg RSA -keysize 2048 -validity 10000
```
When prompted, provide:
- Keystore password (store securely in a password manager)
- Name, organization, and location details
- Key password (can match keystore password)

### B. Configuring `keystore.properties`
Create a file named `keystore.properties` in the project root directory (`keystore.properties`):

```properties
storeFile=stillwords-upload-key.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=stillwords-upload
keyPassword=YOUR_KEY_PASSWORD
```

> **Important:**
> If `keystore.properties` is missing or incomplete, `./gradlew assembleRelease` and `./gradlew bundleRelease` will immediately fail with an explicit error message. Release builds never fall back to debug signing.

### C. Keystore Backup & Disaster Recovery
- Keep safe offline copies of `stillwords-upload-key.jks` and `keystore.properties` (e.g. encrypted cloud vault or offline backup drive).
- Loss of the upload key requires contacting Google Play Console support to reset the upload key.

---

## 2. Google Play App Signing
Stillwords is configured for Google Play App Signing:
1. You sign your Android App Bundle (`.aab`) with your local **upload key**.
2. Google Play verifies your upload key, strips the upload signature, and re-signs the delivered APKs with your secure **app signing key** managed by Google.
3. Enroll in Google Play App Signing when creating the app entry in the Google Play Console.

---

## 3. Building Release Artifacts

To compile and build an Android App Bundle (.aab) for Google Play:
```bash
./gradlew clean bundleRelease
```
The output bundle will be located at:
```text
app/build/outputs/bundle/release/app-release.aab
```

To build a standalone signed release APK for device testing:
```bash
./gradlew assembleRelease
```
The output APK will be located at:
```text
app/build/outputs/apk/release/app-release.apk
```

---

## 4. Release Checklist for Updates

For every update published to Google Play, follow this exact checklist:

1. **Increment `APP_VERSION_CODE` in `gradle.properties`:**
   - Must increase by at least 1 for every bundle uploaded to Play Console (e.g. `1` → `2`).
2. **Update `APP_VERSION_NAME` in `gradle.properties`:**
   - Update user-facing semantic version (e.g. `1.0.0` → `1.0.1`).
3. **Document changes in `CHANGELOG.md`:**
   - Add a new section with the version, date, and user-facing improvements under Added/Changed/Fixed.
4. **Run Pre-flight Checks:**
   ```bash
   ./gradlew clean testDebugUnitTest lintDebug
   ```
5. **Build the production bundle:**
   ```bash
   ./gradlew bundleRelease
   ```
6. **Upload to Play Console:**
   - Log into [Google Play Console](https://play.google.com/console).
   - Navigate to **Production** (or **Testing > Internal testing**).
   - Create a new release, upload `app/build/outputs/bundle/release/app-release.aab`.
   - Copy release notes from `CHANGELOG.md`.
   - Review and rollout.
