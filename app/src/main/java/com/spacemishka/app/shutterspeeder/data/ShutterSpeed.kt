package com.spacemishka.app.shutterspeeder.data

enum class ShutterSpeed(val speed: String, val microseconds: Long) {
    ONE_8000("1/8000", 125),
    ONE_4000("1/4000", 250),
    ONE_2000("1/2000", 500),
    ONE_1000("1/1000", 1000),
    ONE_500("1/500", 2000),
    ONE_250("1/250", 4000),
    ONE_125("1/125", 8000),
    ONE_60("1/60", 16667),
    ONE_30("1/30", 33333),
    ONE_15("1/15", 66667),
    ONE_8("1/8", 125000),
    ONE_2("1/2", 500000),
    ONE("1", 1000000);

    override fun toString(): String = speed
} 