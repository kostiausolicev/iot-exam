package ru.guap.thing.robot.component

class Servo {
    var maxTemperature: Int = 65
    var temperature: Int = 20
        set(value) = if (value < maxTemperature) field = value else throw Exception()
    var angle: Int = 0
}