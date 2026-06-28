package com.xeton.util.desktop.poweraction

interface PowerAction {
    fun initiate(config: PowerActionConfig): Boolean
}
