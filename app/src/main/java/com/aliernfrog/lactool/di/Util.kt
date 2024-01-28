package com.aliernfrog.lactool.di

import org.koin.mp.KoinPlatformTools

inline fun <reified T : Any> get(): T = KoinPlatformTools.defaultContext().get().get<T>()