# Keep JNA classes and its native library load classes
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }
-dontwarn com.sun.jna.**

# Keep UniFFI generated bindings for xeton_core
-keep class ir.amirab.xeton_core_ffi.** { *; }
-keep class ir.amirab.downloader.core.xeton_core_ffi.** { *; }
