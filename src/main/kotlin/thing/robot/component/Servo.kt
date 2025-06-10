package ru.guap.thing.robot.component

class Servo {
    var maxTemperature: Int = 65
    var temperature: Int = 20
        set(value) = if (value < maxTemperature) field = value else throw Exception("Слишком высокая температура. Перегрев")
    var angle: Int = 0
        set(value) = if (value in -90..90) field = value else throw Exception("Угол должен быть в пределах -90..90 градусов")
    var m: Int = 0 // Энкодер
    var l: Int = 0 // Нагрузка
}