package dev.johnoreilly.peopleinspace

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
