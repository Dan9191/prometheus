package ru.dan.prometheus.controller

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.dan.prometheus.service.LoadService
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/load")
class LoadController(
    private val loadService: LoadService,
    private val meterRegistry: MeterRegistry
) {
    private val logger = LoggerFactory.getLogger(LoadController::class.java)

    @GetMapping("/cpu")
    fun cpuLoad(@RequestParam(defaultValue = "10") seconds: Int): String {
        logger.info("CPU load started for {} seconds", seconds)
        loadService.loadCpu(seconds)
        logger.info("CPU load finished")
        return "CPU load done for $seconds seconds"
    }

    @GetMapping("/memory")
    fun memoryLoad(@RequestParam(defaultValue = "200") mb: Int): String {
        logger.info("Memory load started: {} MB", mb)
        loadService.loadMemory(mb)
        logger.info("Memory load finished")
        return "Memory load done for $mb MB"
    }

    @GetMapping("/simulate")
    fun simulate(): String {
        logger.info("Document processing simulation started")
        val duration = loadService.simulateProcessing()
        meterRegistry.counter("documents_processed_total").increment()
        meterRegistry.timer("document_processing_duration").record(duration, TimeUnit.MILLISECONDS)
        logger.info("Document processed in {} ms", duration)
        return "Processed document in $duration ms"
    }
}