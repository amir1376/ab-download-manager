@file:OptIn(ExperimentalUnsignedTypes::class)
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.abdownloadmanager.shared.utils

import java.net.NetworkInterface

fun getSubnet(): List<SubnetAddress> {
    return runCatching {
        NetworkInterface.getNetworkInterfaces()
            .toList()
            .flatMap { networkInterface ->
                networkInterface.interfaceAddresses
            }.filter {
                !it.address.isLinkLocalAddress
                        && !it.address.isLoopbackAddress
                        && it.networkPrefixLength > 0
            }.mapNotNull {
                val ip = ByteIp(it.address.address)
                SubnetAddress(
                    ip,
                    it.networkPrefixLength
                )
            }
    }.getOrElse {
        emptyList()
    }
}

abstract class Ip {
    abstract val intIp: UInt
    abstract val byteIp: UByteArray

    fun toByteIp() = ByteIp(byteIp)
    fun toIntIp() = IntIp(intIp)
    override fun toString(): String {
        return byteIp.map { it }.joinToString(".")
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Ip) {
            return other.intIp == intIp
        }
        return false
    }

    override fun hashCode(): Int {
        return intIp.hashCode()
    }
}

class IntIp(
    ip: UInt
) : Ip() {
    override val intIp by lazy { ip }

    override val byteIp by lazy {
        UByteArray(4) {
            val bitCount = (3 - it) * 8
            (ip shr bitCount).toUByte()
        }
    }
}

@JvmName("ByteIpFromByteArrayVararg")
fun ByteIp(ip: ByteArray): ByteIp {
    return ByteIp(
        ip.map { it.toUByte() }.toUByteArray()
    )
}

@JvmName("ByteIpFromByteVararg")
fun ByteIp(vararg ip: Byte): ByteIp {
    return ByteIp(ip)
}

@JvmName("ByteIpFromUByteVararg")
fun ByteIp(vararg ip: UByte): ByteIp {
    return ByteIp(ip)
}

@JvmName("ByteIpFromIntVararg")
fun ByteIp(vararg ip: Int): ByteIp {
    return ByteIp(
        ip.map {
            it.toUByte()
        }.toUByteArray()
    )
}

class ByteIp(
    ip: UByteArray
) : Ip() {
    init {
        require(ip.size == 4)
    }

    override val intIp: UInt by lazy {
        ip.foldIndexed(0u) { index, acc, byte ->
            val int = byte.toUInt()
            val bitCount = ((3 - index) * 8)
            val shifted = int shl bitCount
            acc.or(shifted)
        }
    }
    override val byteIp by lazy { ip }
}


class SubnetAddress(
    val ip: Ip,
    val prefix: Short
) {


    override fun toString(): String {
        return "$network/$prefix"
    }


    val mask: UInt by lazy {
        val full = UInt.MAX_VALUE
        full shl (32 - prefix)
    }
    val network: Ip by lazy {
        IntIp(ip.intIp and mask)
    }

    val broadcast: Ip by lazy {
        IntIp(network.intIp or mask.inv())
    }

    private val subnetIntRange = ((network.intIp + 1u) until broadcast.intIp)
    val subnetRangeSize by lazy {
        (subnetIntRange.last - subnetIntRange.first)
    }

    val subnetIpRange by lazy {
        subnetIntRange.asSequence().map {
            IntIp(it)
        }
    }
}
