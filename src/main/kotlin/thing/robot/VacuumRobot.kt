package ru.guap.thing.robot

import ru.guap.thing.robot.component.Servo

class VacuumRobot(override var id: Int) : Robot(
    servo1 = Servo(),
    servo2 = Servo(),
    servo3 = Servo(),
    servo4 = Servo(),
    servo5 = Servo(),
    servo6 = null
) {

    override fun deviceName(): String = "Vacuum Robot: Applied Robotics AR-RTK-PL-01"
}