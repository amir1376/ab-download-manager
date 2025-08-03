{
  description = "A flake for AB Download Manager";
  inputs = {
    nixpkgs.url = "github:Nixos/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };
  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        version = "1.6.8";
        src = pkgs.fetchurl {
          url = "https://github.com/amir1376/ab-download-manager/releases/download/v${version}/ABDownloadManager_${version}_linux_x64.tar.gz";
          sha256 = "355be492ca0a4b852da0619782590c6359ff124686fb277a5c30c4452c9b2725";
        };
        runtimeDeps = with pkgs; [
# GUI and windowing
          gtk3
          gtk4
          glib
          gdk-pixbuf
          cairo
          pango
          atk
          
          # X11 and Wayland support
          xorg.libX11
          xorg.libXext
          xorg.libXi
          xorg.libXrender
          xorg.libXtst
          xorg.libXrandr
          xorg.libXfixes
          xorg.libXcomposite
          xorg.libXdamage
          xorg.libXcursor
          wayland
          
          # Audio and multimedia
          alsa-lib
          pulseaudio
          
          # System integration
          libnotify
          xdg-utils
          cups
          dbus
          systemd
          
          # Fonts and rendering
          fontconfig
          freetype
          dejavu_fonts
          liberation_ttf
          
          # OpenGL
          libGL
          libGLU
          mesa
          
          # Desktop integration
          gsettings-desktop-schemas
          hicolor-icon-theme
          
          # Java/JNA native libraries
          glibc
          gcc-unwrapped.lib
          zlib
          
          # Additional libraries that might be needed
          libpng
          libjpeg
          expat
          libffi
          libuuid
          nspr
          nss
        ];
        libPath = pkgs.lib.makeLibraryPath runtimeDeps;

        xdgDataDirs = pkgs.lib.makeSearchPathOutput "share" "XDG_DATA_DIRS" runtimeDeps;
# Comprehensive font configuration
        fontConf = pkgs.makeFontsConf {
          fontDirectories = with pkgs; [ 
            dejavu_fonts 
            liberation_ttf 
            freefont_ttf
            noto-fonts
            noto-fonts-cjk-sans
            noto-fonts-emoji
          ];
        };
      in
      {
        packages.default = pkgs.stdenv.mkDerivation rec {
          pname = "ab-download-manager";
          inherit version src;
          nativeBuildInputs = [
            pkgs.makeWrapper
            pkgs.autoPatchelfHook
          ];
          buildInputs = runtimeDeps;
          installPhase = ''
            runHook preInstall
            mkdir -p $out
            tar -xzf ${src} -C $out
            mv $out/ABDownloadManager/* $out
            rmdir $out/ABDownloadManager
            makeWrapper $out/bin/ABDownloadManager $out/bin/ab-download-manager \
              --set FONTCONFIG_FILE "${fontConf}/etc/fonts/fonts.conf" \
              --prefix LD_LIBRARY_PATH : "${libPath}" \
              --prefix XDG_DATA_DIRS : "${xdgDataDirs}:$out/share"
            mkdir -p $out/share/applications
            echo "[Desktop Entry]
            Name=AB Download Manager
            Comment=Manage and organize your download files better than before
            GenericName=Downloader
            Categories=Utility;Network;
            Exec=ab-download-manager
            Icon=ab-download-manager
            Terminal=false
            Type=Application
            StartupWMClass=com-abdownloadmanager-desktop-AppKt" > $out/share/applications/ab-download-manager.desktop
            mkdir -p $out/share/icons/hicolor/scalable/apps
            cp $out/lib/ABDownloadManager.png $out/share/icons/hicolor/scalable/apps/ab-download-manager.png
            runHook postInstall
          '';
          meta = with pkgs.lib; {
            description = "A download manager to manage and organize your download files better than before";
            homepage = "https://github.com/amir1376/ab-download-manager";
            license = licenses.asl20;
            maintainers = with maintainers; [ ];
            platforms = platforms.linux;
          };
        };
      }
    );
}
