<div align="center">
  <a href="https://abdownloadmanager.com" target="_blank">
    <img width="180" src="assets/logo/app_logo_with_background.svg" alt="AB Download Manager Logo">
  </a>
</div>
<h1 align="center">AB Download Manager</h1>
<p align="center">
    <a href="https://github.com/amir1376/ab-download-manager/releases/latest"><img alt="GitHub Release" src="https://img.shields.io/github/v/release/amir1376/ab-download-manager?color=greenlight&label=latest%20release"></a>
    <a href="https://abdownloadmanager.com"><img alt="AB Download Manager Website" src="https://img.shields.io/badge/project-website-purple?&labelColor=gray"></a>
    <a href="https://t.me/abdownloadmanager_discussion"><img alt="Telegram Group" src="https://img.shields.io/badge/Telegram-Group-blue?logo=telegram&labelColor=gray"></a>
    <a href="https://t.me/abdownloadmanager"><img alt="Telegram Channel" src="https://img.shields.io/badge/Telegram-Channel-blue?logo=telegram&labelColor=gray"></a>
    <a href="https://crowdin.com/project/ab-download-manager"><img alt="Crowdin" src="https://badges.crowdin.net/ab-download-manager/localized.svg"></a>
</p>

<a href="https://abdownloadmanager.com" target="_blank">
    <img alt="AB Download Manager Banner" src="assets/banners/app_banner.png"/>
</a>


## Description

[AB Download Manager](https://abdownloadmanager.com) is a desktop app that helps you manage and organize your downloads more efficiently than ever before.

## Features

- ⚡️ Faster Download Speed
- ⏰ Queues and Schedulers
- 🌐 Browser Extensions
- 💻 Multiplatform (Windows / Linux / Mac)
- 🌙 Multiple Themes (Dark/Light) with modern UI
- ❤️ Free and Open Source

Please visit [Project Website](https://abdownloadmanager.com) for more info.

## Installation

### Download and Install the App

<a href="https://abdownloadmanager.com"><img src="https://img.shields.io/badge/Official%20Website-897BFF?logo=abdownloadmanager&logoColor=fff&style=flat-square" alt="Official Website" height="32" /></a>
<a href="https://github.com/amir1376/ab-download-manager/releases/latest"><img src="https://img.shields.io/badge/GitHub%20Releases-2a2f36?logo=github&logoColor=fff&style=flat-square" alt="GitHub Releases" height="32" /></a>

#### Installation script (Linux)

```bash
bash <(curl -fsSL https://raw.githubusercontent.com/amir1376/ab-download-manager/master/scripts/install.sh)
```

#### Winget or Scoop (for Windows)

**winget**:

```bash
winget install amir1376.ABDownloadManager
```

**scoop**:

```bash
scoop install extras/abdownloadmanager
```

#### Homebrew (for macOS)

```bash
brew tap amir1376/tap && brew install --cask ab-download-manager
```

### Browser Extensions

You can download the browser extension to integrate the app with your browser.

<p align="left">
<a href="https://addons.mozilla.org/firefox/addon/ab-download-manager/">
    <picture>
        <img alt="Chrome Extension" src="./assets/banners/firefox-extension.png" height="48">
    </picture>
</a>
<a href="https://chromewebstore.google.com/detail/bbobopahenonfdgjgaleledndnnfhooj">
    <picture>
        <source media="(prefers-color-scheme: dark)" srcset="./assets/banners/chrome-extension_dark.png" height="48">
        <source media="(prefers-color-scheme: light)" srcset="./assets/banners/chrome-extension_light.png" height="48">
        <img alt="Chrome Extension" src="./assets/banners/chrome-extension_light.png" height="48">
    </picture>
</a>
</p>

## Screenshots

<div align="center">
<picture>
  <source media="(prefers-color-scheme: dark)" srcset="./assets/screenshots/app-home_dark.png">
  <source media="(prefers-color-scheme: light)" srcset="./assets/screenshots/app-home_light.png">
  <img alt="App Home Section" src="./assets/screenshots/app-home_dark.png">
</picture>

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="./assets/screenshots/app-download_dark.png">
  <source media="(prefers-color-scheme: light)" srcset="./assets/screenshots/app-download_light.png">
  <img alt="App Download Section" src="./assets/screenshots/app-download_dark.png">
</picture>
</div>

## Project Status & Feedback

Please keep in mind that this project is in the beginning of its journey.
**Lots of features** are on the way!

**But**, in the meantime you may face **Bugs or Problems**. If you do, please report them to me via the [Community chat](#community) or through `GitHub Issues`, and I'll do my best to fix them ASAP.

## Community

You can join our [Telegram Group](https://t.me/abdownloadmanager_discussion) to:

- Report problems
- Suggest features
- Get help with the app

## Repositories And Source Code

There are multiple repositories related to the **AB Download Manager** project:

| Repository                                                                                 | Description                                                                   |
|--------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------|
| [Main Application](https://github.com/amir1376/ab-download-manager) (You are here)         | Contains the  **Application** that runs on your  **device**                   |
| [Browser Integration](https://github.com/amir1376/ab-download-manager-browser-integration) | Contains the **Browser Extension** to be installed on your  **browser**       |
| [Website](https://github.com/amir1376/ab-download-manager-website)                         | Contains the **AB Download Manager** [website](https://abdownloadmanager.com) |

I've spent a lot of time to create this project.

If you like my work, please consider giving it a ⭐ — thanks! ❤️

## Bug Report

If you notice any bugs in the source code, please report them via the `GitHub Issues` section.

## Build From Source

To compile and test the desktop app on your local machine,
follow these steps:

1. Clone the project.
2. Download and extract the [JBR](https://github.com/JetBrains/JetBrainsRuntime/releases), and make it available by either:
    
    - Adding it to your `PATH`, or
    - Setting the `JAVA_HOME` environment variable to its installation path.
  
3. Navigate to the project directory, open your terminal and execute the following command:

    ```bash
    ./gradlew createReleaseFolderForCi
    ```

4. The output will be available at:

    ```
    <project_dir>/build/ci-release
    ```

> **Note**. This project is compiled and published by GitHub actions [here](./.github/workflows/publish.yml), so if you
> faced any problem you can check that too.

## Translations

If you’d like to help translate AB Download Manager into another language, or improve existing translations, you can do
so on Crowdin. Here’s how:

- Visit the project in [Crowdin](https://crowdin.com/project/ab-download-manager)
- Please DO NOT submit translations via pull requests.
- If you want to add a new language, please see [this](https://github.com/amir1376/ab-download-manager/issues/144).

## Contribution

> ❌ **Important Notice:** The entire codebase is being completely rewritten. Pull requests are **not accepted** at this
> time, as incoming changes may be lost or conflict heavily with ongoing refactoring. Please wait until the refactor is
> complete before submitting any PRs.

Contributions to this project are very welcome!

If you want to contribute to this project, please read [Contributing Guide](CONTRIBUTING.md) first.

Let's make a better Download Manager together! ❤️

## Support the Project

If you'd like to support the project, you can find details on how to donate in the [DONATE.md](DONATE.md) file.
