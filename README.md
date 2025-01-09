
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

[AB Download Manager](https://abdownloadmanager.com) is a desktop app which lets you manage and organize your download files better than before

> [!CAUTION]
> This is a forked project from [amir1376/ab-download-manager](https://github.com/amir1376/ab-download-manager), aiming to provide the latest **macos** package of AB Download Manager, you can download from [GitHub Releases](https://github.com/peanut996/ab-download-manager-macos/releases/latest), but it is not recommended to use it, take the responsibility of using it at your own risk!

#### Application Signing

*AB Download Manager* is not signed (due to its costs and me not owning supported Apple device) so you will need to use a workaround for the first run. The workaround
depends on if you're running an Intel or Apple Silicon chip.

- **Intel Chips**: [Open a Mac app from an unidentified developer](https://support.apple.com/guide/mac-help/open-a-mac-app-from-an-unidentified-developer-mh40616/mac).
- **Apple Chips**: Open a terminal and run this command:

```bash
sudo xattr -r -d com.apple.quarantine /Applications/ABDownloadManager.app
```

## Features

- ⚡️ Faster Download Speed
- ⏰ Queues and Schedulers
- 🌐 Browser Extensions
- 💻 Multiplatform (Windows / Linux for now)
- 🌙 Multiple Themes (Dark/Light) with modern UI
- ❤️ Free and Open Source

Please visit [Project Website](https://abdownloadmanager.com) for more info

## Installation

[Download Instructions](https://abdownloadmanager.com)

[GitHub Releases](https://github.com/amir1376/ab-download-manager/releases/latest)

in order to download and install the app

### installation script (Linux)

```bash
bash <(curl -fsSL https://raw.githubusercontent.com/amir1376/ab-download-manager/master/scripts/install.sh)
```

### winget or scoop (for Windows)

**winget**:

```bash
winget install amir1376.ABDownloadManager
```

**scoop**:

```bash
scoop install extras/abdownloadmanager
```

## Uninstall

perform below command to uninstall

```bash
bash <(curl -fsSL https://raw.githubusercontent.com/amir1376/ab-download-manager/master/scripts/uninstall.sh)
```

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
**lots of features** are on the way!.

**But**, in the meantime you may face **Bugs or Problems**. so.
Please report them (by [Community chat](#community) or `GitHub Issues`) to me,And I'll do my best to fix them ASAP

## Community

You can join to our [Telegram Group](https://t.me/abdownloadmanager_discussion) to

- Report problems
- Suggest features
- Get help about the app

## Repositories And Source Code

There are multiple repositories related to the **AB Download Manager** project

| Repository                                                                                 | Description                                                                   |
|--------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------|
| [Main Application](https://github.com/amir1376/ab-download-manager) (You are here)         | Contains the  **Application** that runs on your  **device**                   |
| [Browser Integration](https://github.com/amir1376/ab-download-manager-browser-integration) | Contains the **Browser Extension** to be installed on your  **browser**       |
| [Website](https://github.com/amir1376/ab-download-manager-website)                         | Contains the **AB Download Manager** [website](https://abdownloadmanager.com) |

I spent a lot of time to create this project.

If you like my work, Please consider giving it a ⭐ Thanks ❤️

## Bug Report

If you see bugs in the source code! please report them in the `GitHub Issues` section

## Build From Source

to compile and test desktop app on your local machine
follow these steps.

1. Clone the project
2. Install the [JBR](https://github.com/JetBrains/JetBrainsRuntime/releases)
3. cd into the project, open your terminal and execute the following commands
4. select which way you want to compile the app
<details>
<summary>Packaged (msi,deb at the moment)</summary>

```bash
./gradlew

./gradlew packageReleaseDistributionForCurrentOS
```

This will create an installer package for your **current OS**, so you can install it on your own

>Note: you will get error if your OS does not support any of above package types in this case you should compile it `without packaging`

</details>

<details>
<summary>Without Package</summary>

In case you don't want to package it or your OS does not support those package types you can use this command to compile the app without packaging it
```bash
./gradlew

./gradlew createReleaseDistributable
```
It will create an output folder

>NOTE: this is not packaged you may package it yourself manually, or just simply run it!

>I suggest you to move the output somewhere else if you want to run it directly

</details>

The output will be created in

```
<project_dir>/desktop/app/build/compose/binaries/main-release/
```


> **Note**. This project is compiled and published by GitHub actions [here](./.github/workflows/publish.yml), so if you
> faced any problem you can check that too

## Contribution

Contributions to this project are very welcome!

If you want to contribute to this project, please read [Contributing Guide](CONTRIBUTING.md) first.

Let's make a better Download Manager together ❤️

## Support the Project

If you'd like to support the project, you can find details on how to donate in the [DONATE.md](DONATE.md) file.
