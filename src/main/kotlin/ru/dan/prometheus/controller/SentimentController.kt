package ru.dan.prometheus.controller

import edu.stanford.nlp.pipeline.CoreDocument
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*
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
            meterRegistry.counter("sentiment_requests_empty").increment()
            return SentimentResponse("Neutral")
        }

        logger.info { "Analyzing sentiment for text (length=${text.length}): \"$text\"" }

        val document = CoreDocument(text)
        pipeline.annotate(document)

        val sentences = document.sentences()
        if (sentences.isEmpty()) {
            meterRegistry.counter("sentiment_result", "value", "Neutral").increment()
            return SentimentResponse("Neutral")
        }

        val allSentiments = sentences.mapNotNull { it.sentiment() }
        val mainSentiment = allSentiments
            .maxByOrNull { it.toSentimentScore() } ?: "Neutral"

        logger.info { "Detected main sentiment: $mainSentiment (from ${allSentiments.size} sentence(s))" }

        meterRegistry.counter("sentiment_result", "value", mainSentiment).increment()
        meterRegistry.counter("sentiment_sentences_total").increment(allSentiments.size.toDouble())


        allSentiments.forEach { sentiment ->
            meterRegistry.counter("sentiment_per_sentence", "value", sentiment).increment()
        }

        return SentimentResponse(mainSentiment)
    }
}

private fun String.toSentimentScore(): Int = when (this) {
    "VeryPositive" -> 4
    "Positive"     -> 3
    "Neutral"      -> 2
    "Negative"     -> 1
    "VeryNegative" -> 0
    else           -> 2
}