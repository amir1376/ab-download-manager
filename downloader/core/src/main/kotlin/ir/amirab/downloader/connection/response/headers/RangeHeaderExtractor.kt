package ir.amirab.downloader.connection.response.headers

import ir.amirab.downloader.connection.response.ResponseInfo

data class ContentRangeValue(
    val range: LongRange?,
    val fullSize: Long?,
)

fun ResponseInfo.getContentRange(): ContentRangeValue?{
    val value=responseHeaders["content-range"]?:return null
    val actualValue=runCatching {
        value.substring("bytes ".length)
    }.getOrNull()?:return null
    if (actualValue.isBlank()){
        return null
    }
    var from:Long?=null
    var to:Long?=null
    var size:Long?=null
    val (rangeString,sizeString)=actualValue.split("/")
    if (rangeString!="*"){
        rangeString.split("-").map {
            it.toLong()
        }.let {
            from=it[0]
            to=it[1]
        }
    }
    if (sizeString!="*"){
        size=sizeString.toLong()
    }

    return ContentRangeValue(
        range = from?.let {f->
            to?.let {t->
                f..t
            }
        },
        fullSize=size
    )

}