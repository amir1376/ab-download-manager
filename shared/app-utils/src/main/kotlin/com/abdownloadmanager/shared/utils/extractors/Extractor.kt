package com.abdownloadmanager.shared.utils.extractors

interface Extractor<in Input,out Output>{
    fun extract(input:Input):Output
}