package com.xeton.downloader.utils

import java.io.File
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption

actual object SparseFile : ISparseFile {
    override fun createSparseFile(file: File): Boolean {
        // [DELEGATED TO RUST]
        // Zero-copy sparse allocation is now handled natively via Rust core
        // (xeton_core/src/destination/mod.rs) using OS specific syscalls
        return false
    }

    override fun canWeCreateSparseFile(file: File): Boolean {
        // android doesn't tell us!
        return true
    }
}
