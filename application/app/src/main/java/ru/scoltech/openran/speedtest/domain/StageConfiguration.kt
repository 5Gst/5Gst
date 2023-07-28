package ru.scoltech.openran.speedtest.domain

data class StageConfiguration(
    var name: String,
    var serverArgs: String,
    var deviceArgs: String,
) {
    companion object {
        val EMPTY = StageConfiguration("", "", "")
    }
}
