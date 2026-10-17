# AlarmBoss 🚨

An advanced Android alarm application built using Jetpack Compose, Kotlin, and Material Design 3. AlarmBoss features strict challenges (Math, Lyrics, Barcodes, Reading, Pattern Lock and Word Scramble tasks) to ensure you wake up on time.

---

## 🎮 Progression

Every alarm you actually wake up to earns XP, so mornings become a game you can level up in.

- **XP and levels:** Easy, Medium and Strict alarms pay different XP, with bonuses for your streak and for finishing fast. Levels follow a square-root curve (level 2 at 50 XP, level 3 at 200 XP).
- **Wake-up streak:** consecutive days with a completed alarm. Miss a day and it restarts at 1.
- **Boss fights:** on every 7th day of a streak, Medium and Strict alarms swap their task for a three-stage boss fight (Math, then Sequence, then Maze) worth a flat 100 XP. Easy alarms stay one tap.
- **Adaptive difficulty:** Math and Maze tasks get harder as your level and streak grow (difficulty 1-10).
- **Stats tab:** level progress, streak, bosses defeated, records and a four-week wake-up calendar.

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

