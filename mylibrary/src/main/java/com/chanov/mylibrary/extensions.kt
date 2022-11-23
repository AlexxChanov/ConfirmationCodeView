package com.chanov.mylibrary

fun <T> T?.orDefault(defaultValue: T): T = this ?: defaultValue
