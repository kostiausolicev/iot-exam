package ru.guap.thing.robot.component

class Servo {
    var maxTemperature: Int = 65
    var minTemperature: Int = 10

    var maxAngle = 90
    var minAngle = -90

    var maxM = 100
    var minM = 10

    var maxL = 100
    var minL = 10

    var warnMaxTemperature: Int = 65
    var warnMinTemperature: Int = 10

    var warnMaxAngle = 90
    var warnMinAngle = -90

    var warnMaxM = 100
    var warnMinM = 10

    var warnMaxL = 100
    var warnMinL = 10

    var temperature: Int = 20
        set(value) =
            if (value in minTemperature..maxTemperature) field = value
            else throw Exception("Не подходящая температура")
    var angle: Int = 0
        set(value) =
            if (value in minAngle..maxAngle) field = value
            else throw Exception("Угол должен быть в пределах -90..90 градусов")
    var m: Int = 0 // Энкодер
    var l: Int = 0 // Нагрузка

    fun getPsycTemperature() = (minTemperature + (minTemperature.toDouble() / maxTemperature.toDouble()) * (maxTemperature - minTemperature)).toInt()
    fun getPsycM() = (minM + (minM.toDouble() / maxM.toDouble()) * (maxM - minM)).toInt()
    fun getPsycL() = (minL + (minL.toDouble() / maxL.toDouble()) * (maxL - minL)).toInt()
}