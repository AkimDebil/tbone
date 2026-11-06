package com.example.data.model

import java.net.URLEncoder

data class RecommendedWork(
    val title: String,
    val composer: String,
    val whyListen: String
) {
    fun getYouTubeSearchUrl(): String {
        val query = "$composer $title classical performance"
        return "https://www.youtube.com/results?search_query=" + URLEncoder.encode(query, "UTF-8")
    }
}

data class FamousRecording(
    val performer: String,
    val orchestraOrAccompanist: String,
    val highlight: String
) {
    fun getYouTubeSearchUrl(composer: String, pieceTitle: String): String {
        val query = "$composer $pieceTitle $performer classical performance"
        return "https://www.youtube.com/results?search_query=" + URLEncoder.encode(query, "UTF-8")
    }
}

data class RepertoireAnalysis(
    val pieceTitle: String,
    val composer: String,
    val instrument: String,
    val eraAndStyle: String,
    val technicalTips: List<String>,
    val recommendedListening: List<RecommendedWork>,
    val famousRecordings: List<FamousRecording>
) {
    fun getGeneralYouTubeSearchUrl(): String {
        val query = "$composer $pieceTitle $instrument classical performance"
        return "https://www.youtube.com/results?search_query=" + URLEncoder.encode(query, "UTF-8")
    }
}
