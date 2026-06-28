# Keep JNA classes and its native library load classes
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }
-dontwarn com.sun.jna.**

# Keep UniFFI generated bindings for xeton_core
-keep class com.xeton.xeton_core_ffi.** { *; }
-keep class com.xeton.downloader.core.xeton_core_ffi.** { *; }
