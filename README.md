# AlarmBoss 🚨

An advanced Android alarm application built using Jetpack Compose, Kotlin, and Material Design 3. AlarmBoss features strict challenges (Math, Lyrics, Barcodes, and Reading tasks) to ensure you wake up on time.

---

## 🚀 Automated Release Pipeline

This project uses **GitHub Actions** to automatically build and compile the application into a production-ready Android APK whenever a new version tag is published. 

### How to Trigger a New Production Build

Because the automation script is configured to look for version tags (e.g., `v1.0.0`), standard code pushes to the `main` branch will **not** trigger an APK build. 

When you are ready to publish a new version of the app, open your Command Prompt (CMD) and execute the following two commands:

#### 1. Create a Local Version Tag
Create a signed version tag on your current local branch:
```bash
git tag -a v1.0.0 -m "First automated release"
```
*(Feel free to update `v1.0.0` to your current version number, such as `v1.0.1` or `v2.0.0`)*

#### 2. Push the Tag to GitHub
Push the new tag to your remote GitHub repository to wake up the build runner:
```bash
git push origin v1.0.0
```

---

### ⏱️ What Happens Next?

1. **Automation Trigger:** GitHub will immediately detect the new `v*` tag and spin up an isolated virtual environment to compile your code.
2. **Track the Progress:** Navigate to your repository page on the GitHub website and click the **Actions** tab at the top. You will see a live workflow log showing your Gradle environment compiling the APK.
3. **Download the App:** Once the build successfully completes, a brand new production **Release** will instantly appear on your GitHub dashboard. Your compiled `app-release.apk` file will be attached and ready for download onto any Android device!
