package com.example.clinicrecord.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class DeepSeekClinicalRequest(
    val chiefComplaint: String,
    val presentIllness: String,
    val pulseDescription: String,
    val corePathogenesis: String
)

object DeepSeekClinicalApi {
    private const val ENDPOINT = "https://api.deepseek.com/chat/completions"

    suspend fun requestClinicalReference(
        apiKey: String,
        request: DeepSeekClinicalRequest
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            require(apiKey.isNotBlank()) { "DeepSeek API Key 未配置" }

            val body = JSONObject()
                .put("model", "deepseek-chat")
                .put(
                    "messages",
                    JSONArray()
                        .put(
                            JSONObject()
                                .put("role", "system")
                                .put(
                                    "content",
                                    "你是中医临床辅助参考工具。仅根据客观病史和四诊信息给出辨证思路参考，不能替代医生诊断，不直接下最终医嘱。"
                                )
                        )
                        .put(
                            JSONObject()
                                .put("role", "user")
                                .put("content", request.toPrompt())
                        )
                )
                .put("temperature", 0.2)

            val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 20_000
                readTimeout = 30_000
                doOutput = true
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("Content-Type", "application/json")
            }

            connection.outputStream.use { stream ->
                stream.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            val responseText = if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            if (connection.responseCode !in 200..299) {
                error("DeepSeek 请求失败：${connection.responseCode} $responseText")
            }

            JSONObject(responseText)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }
}

private fun DeepSeekClinicalRequest.toPrompt(): String {
    return """
        请基于以下四诊资料给出临床参考：
        主诉：$chiefComplaint
        现病史：$presentIllness
        舌脉：$pulseDescription
        已记录核心病机：$corePathogenesis

        请按“可能病机、辨证要点、治法参考、需进一步追问或观察”四部分回答。
    """.trimIndent()
}
