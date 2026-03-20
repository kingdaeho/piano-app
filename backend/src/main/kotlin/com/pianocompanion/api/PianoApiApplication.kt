package com.pianocompanion.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PianoApiApplication

fun main(args: Array<String>) {
    runApplication<PianoApiApplication>(*args)
}
