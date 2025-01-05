package ir.amirab.util

inline fun<Base,T:Base> T.ifThen(condition:Boolean, block:T.()->Base): Base {
    return if (condition){
        this.block()
    }else{
        this
    }
}