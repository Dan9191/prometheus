package ru.dan.prometheus.controller

import edu.stanford.nlp.pipeline.CoreDocument
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.dan.prometheus.model.SentimentRequest
import ru.dan.prometheus.model.SentimentResponse
import java.util.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api")
class SentimentController(
    private val meterRegistry: MeterRegistry
) {

    private val pipeline by lazy {
        Properties().apply {
            setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,sentiment")
        }.let { StanfordCoreNLP(it) }
    }

    @PostMapping("/sentiment")
    fun analyze(@RequestBody request: SentimentRequest): SentimentResponse {
        // Общий счётчик запросов
        meterRegistry.counter("sentiment_requests_total").increment()

        val text = request.text.trim()
        if (text.isBlank()) {
            logger.info { "Received empty text for sentiment analysis" }
            meterRegistry.counter("sentiment_neutral_total").increment()
            return SentimentResponse("Neutral")
        }

        val document = CoreDocument(text)
        pipeline.annotate(document)
        val sentences = document.sentences()

        if (sentences.isEmpty()) {
            meterRegistry.counter("sentiment_neutral_total").increment()
            return SentimentResponse("Neutral")
        }

        val sentiments = sentences.map { it.sentiment().toSimpleSentiment() }

        val mainSentiment = sentiments
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }?.key ?: "Neutral"

        logger.info { "Detected sentiment: $mainSentiment" }

        // Инкремент нужного счётчика
        when (mainSentiment) {
            "Positive" -> meterRegistry.counter("sentiment_positive_total").increment()
            "Neutral"  -> meterRegistry.counter("sentiment_neutral_total").increment()
            "Negative" -> meterRegistry.counter("sentiment_negative_total").increment()
        }

        return SentimentResponse(mainSentiment)
    }
}

private fun String.toSimpleSentiment(): String = when (this) {
    "VeryPositive", "Positive" -> "Positive"
    "Neutral" -> "Neutral"
    "Negative", "VeryNegative" -> "Negative"
    else -> "Neutral"
}