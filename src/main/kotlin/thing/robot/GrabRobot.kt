package ru.guap.thing.robot

import ru.guap.thing.robot.component.Servo

class GrabRobot(override var id: Int) : Robot(
    servo1 = Servo(),
    servo2 = Servo(),
    servo3 = Servo(),
    servo4 = Servo(),
    servo5 = Servo(),
    servo6 = Servo()
) {
    override fun moveTo(x: Int, y: Int, z: Int) {
        TODO("Not yet implemented")
    }

    override fun grab() {
        TODO("Not yet implemented")
    }

    override fun turn(angle: Int) {
        TODO("Not yet implemented")
    }

    override fun deviceName(): String = "Grap Robot: Applied Robotics AR-RTK-ML-01"
}