#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-}"
[ -n "${VERSION}" ] || { echo "usage: $0 <version> [--arch x64|arm64|all]"; exit 1; }

REQ_ARCH="${2:-}"
REQ_VAL="${3:-}"

HOST_ARCH="$(uname -m)"
case "$HOST_ARCH" in
  x86_64|aarch64) ;;
  *) echo "unsupported host arch: $HOST_ARCH"; exit 2;;
esac

case "$REQ_ARCH" in
  "" ) TARGETS=("$HOST_ARCH") ;;
  --arch )
    case "$REQ_VAL" in
      x64) TARGETS=(x86_64) ;;
      arm64) TARGETS=(aarch64) ;;
      all) TARGETS=(x86_64 aarch64) ;;
      *) echo "invalid --arch value: $REQ_VAL (use x64|arm64|all)"; exit 2 ;;
    esac
    ;;
  * ) echo "usage: $0 <version> [--arch x64|arm64|all]"; exit 1 ;;
esac

NEED_OTHER=()
for T in "${TARGETS[@]}"; do
  [ "$T" = "$HOST_ARCH" ] || NEED_OTHER+=("$T")
done
if [ "${#NEED_OTHER[@]}" -gt 0 ]; then
  echo "error: requested arch(${NEED_OTHER[*]}) cannot be built on host $HOST_ARCH without cross toolchains/emulation (disallowed)."
  echo "hint: run this script on each native host, e.g.:"
  echo "  on x86_64: ./package-rhel.sh $VERSION --arch x64"
  echo "  on aarch64: ./package-rhel.sh $VERSION --arch arm64"
  exit 10
fi

if command -v dnf >/dev/null 2>&1; then
  sudo dnf -y install \
    java-21-openjdk java-21-openjdk-devel java-21-openjdk-jmods \
    unzip zip rpm-build rpmdevtools git which desktop-file-utils
elif command -v apt-get >/dev/null 2>&1; then
  sudo apt-get update -y
  sudo apt-get install -y \
    openjdk-21-jdk openjdk-21-jre-headless \
    unzip zip rpm git which desktop-file-utils
else
  echo "unsupported distro: need dnf or apt-get"; exit 3
fi

[ -f "./gradlew" ] || { echo "run in repo root"; exit 4; }
chmod +x ./gradlew

export JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")")"
export PATH="$JAVA_HOME/bin:$PATH"
export JAVA_TOOL_OPTIONS="-Djdk.tls.client.protocols=TLSv1.2"

TOPDIR="$HOME/rpmbuild"
if [ -d "$TOPDIR/BUILD" ]; then
  :
elif command -v rpmdev-setuptree >/dev/null 2>&1; then
  rpmdev-setuptree
else
  mkdir -p "$TOPDIR"/{BUILD,BUILDROOT,RPMS,SOURCES,SPECS,SRPMS}
fi

SOURCES="$TOPDIR/SOURCES"
SPECS="$TOPDIR/SPECS"
DIST="$(pwd)/dist"
mkdir -p "$SOURCES" "$SPECS" "$DIST"

for TARCH in "${TARGETS[@]}"; do
  echo "构建目标平台：$TARCH"

  ./gradlew --no-daemon clean
  ./gradlew --no-daemon desktop:app:createReleaseFolderForCi || ./gradlew --no-daemon createReleaseFolderForCi

  CAND1="desktop/app/build/compose/binaries/main-release/app/ABDownloadManager"
  CAND2="$(find desktop/app/build/compose/binaries -type d -path '*/app/ABDownloadManager' | head -n1 || true)"
  CAND3="$(find build/ci-release -type d -name ABDownloadManager | head -n1 || true)"
  if [ -d "$CAND1" ]; then ABDIR="$CAND1"
  elif [ -n "$CAND2" ] && [ -d "$CAND2" ]; then ABDIR="$CAND2"
  elif [ -n "$CAND3" ] && [ -d "$CAND3" ]; then ABDIR="$CAND3"
  else
    echo "ABDownloadManager directory not found"; exit 5
  fi

  BUNDLE_TGZ="$SOURCES/abdm-bundle-${VERSION}-${TARCH}.tar.gz"
  tar -C "$(dirname "$ABDIR")" -czf "$BUNDLE_TGZ" "$(basename "$ABDIR")"

  SPECFILE="$SPECS/ab-download-manager-${TARCH}.spec"
  cat > "$SPECFILE" <<'EOF'
Name:           ab-download-manager
Version:        VERSION_PLACEHOLDER
Release:        1%{?dist}
Summary:        AB Download Manager (Compose Desktop)
License:        Apache-2.0
URL:            https://github.com/amir1376/ab-download-manager

%global debug_package %{nil}
%global _enable_debug_packages 0
%global _debugsource_packages 0

%global _use_internal_dependency_generator 0
%global __requires_exclude .*
%global __provides_exclude .*
AutoReqProv:    no

Source0:        abdm-bundle-VERSION_PLACEHOLDER-ARCHFILE.tar.gz
BuildArch:      ARCHRPM

%global installdir /opt/%{name}
%global desktopfile %{name}.desktop
%global iconbase    %{name}.png

%description
AB Download Manager built from source with bundled runtime.

%prep
%setup -q -T -c
tar -xzf %{SOURCE0}

%build

%install
mkdir -p %{buildroot}%{installdir}
mkdir -p %{buildroot}%{installdir}/lib-compat
SRCDIR=$(find . -maxdepth 2 -type d -name ABDownloadManager | head -n1)
cp -a "$SRCDIR"/* %{buildroot}%{installdir}/
mkdir -p %{buildroot}%{_bindir}
cat > %{buildroot}%{_bindir}/abdm << 'EOT'
#!/usr/bin/env bash
export LD_LIBRARY_PATH="/opt/ab-download-manager/lib-compat:${LD_LIBRARY_PATH:-}"
exec "/opt/ab-download-manager/bin/ABDownloadManager" "$@"
EOT
chmod 0755 %{buildroot}%{_bindir}/abdm
mkdir -p %{buildroot}%{_datadir}/applications
cat > %{buildroot}%{_datadir}/applications/%{desktopfile} << 'EOT'
[Desktop Entry]
Type=Application
Name=AB Download Manager
Comment=Modern cross-platform download manager
Exec=abdm
Icon=ab-download-manager
Terminal=false
Categories=Network;Utility;
EOT
if [ -f "$SRCDIR/lib/ABDownloadManager.png" ]; then
  mkdir -p %{buildroot}%{_datadir}/icons/hicolor/256x256/apps
  install -m 0644 "$SRCDIR/lib/ABDownloadManager.png" %{buildroot}%{_datadir}/icons/hicolor/256x256/apps/%{iconbase}
fi

%post
if command -v update-desktop-database >/dev/null 2>&1; then
  update-desktop-database -q %{_datadir}/applications || true
fi

%postun
if command -v update-desktop-database >/dev/null 2>&1; then
  update-desktop-database -q %{_datadir}/applications || true
fi

%files
%{_bindir}/abdm
%{installdir}
%{installdir}/lib-compat
%{_datadir}/applications/%{desktopfile}
%{_datadir}/icons/hicolor/256x256/apps/%{iconbase}

%changelog
* Mon Sep 29 2025 Builder <builder@example.com> - VERSION_PLACEHOLDER-1
- Build from source on RHEL; disable auto deps/provides
EOF

  case "$TARCH" in
    x86_64) ARCHFILE="x86_64"; ARCHRPM="x86_64" ;;
    aarch64) ARCHFILE="aarch64"; ARCHRPM="aarch64" ;;
  esac
  sed -i "s/VERSION_PLACEHOLDER/${VERSION}/g" "$SPECFILE"
  sed -i "s/ARCHFILE/${ARCHFILE}/g" "$SPECFILE"
  sed -i "s/ARCHRPM/${ARCHRPM}/g" "$SPECFILE"

  rpmbuild -ba "$SPECFILE" --target "$TARCH"
done

find "$TOPDIR/RPMS" -type f -name "*.rpm" -exec cp -f {} "$DIST/" \;
echo "$DIST"
