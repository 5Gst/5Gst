package ru.fivegst.speedtest.domain

data class SpeedTestResult(
    val entries: List<Entry>,
) {
    data class Entry(
        val stageConfiguration: StageConfiguration,
        val measurementResult: String,
    )
}
