package ru.fivegst.speedtest.backend

class IperfException(message: String) : Exception(message) {
    constructor(message: String, errno: Int) : this("$message (errno = $errno)")
}
