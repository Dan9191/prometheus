package ru.dan.prometheus.service

import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class LoadService {

    fun loadCpu(seconds: Int) {
        val end = System.currentTimeMillis() + (seconds * 1000)
        while (System.currentTimeMillis() < end) {
            Math.sqrt(Random.nextDouble()) // бессмысленные вычисления для загрузки CPU
        }
    }

    fun loadMemory(mb: Int) {
        val size = mb * 1024 * 1024 / 8
        val dummy = DoubleArray(size)
        repeat(1000) {
            dummy[Random.nextInt(dummy.size)] = Random.nextDouble()
        }
    }

    fun simulateProcessing(): Long {
        val start = System.currentTimeMillis()
        Thread.sleep(Random.nextLong(50, 200))
        return System.currentTimeMillis() - start
    }
}