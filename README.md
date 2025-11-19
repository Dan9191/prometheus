## Prometheus Demo – нагрузочный сервис

Простое Spring Boot-приложение, которое умеет:

* нагружать CPU
* «съедать» память
* имитировать обработку документов с метриками (успех/ошибка, длительность)

Всё это экспортируется в **Prometheus** и готово к сбору Grafana-дашбордами.

### Стек

- Kotlin 1.9.25
- Spring Boot 3.5.7
- Micrometer → Prometheus
- SLF4J + JSON-логи

### Как запустить

```bash
./gradlew bootRun
```
### или
```
docker build -t prometheus-demo .
docker run -p 8066:8066 prometheus-demo
```

#### Получение всех записей студента
```http
POST /api/sentiment

Content-Type: application/json

{
  "text": "i love ice cream",
}
```

##### Ответ (200 OK):
```json
{
  "sentiment": "Neutral"
}
```

## Метрики Prometheus
### Общее количество обработанных документов
documents_processed_total

### Успешные
documents_processed_success

### Ошибки
documents_processed_failure

### Таймер длительности
document_processing_duration_seconds