package ru.guap.thing.robot

import ru.guap.thing.robot.component.Servo

class VacuumRobot : Robot(
    servo1 = Servo(),
    servo2 = Servo(),
    servo3 = Servo(),
    servo4 = Servo(),
    servo5 = Servo(),
    servo6 = null
) {
    override fun type(): String {
        return "Applied Robotics AR-RTK-PL-01"
    }

    override fun moveTo(x: Int, y: Int, z: Int) {
        TODO("Not yet implemented")
    }

    override fun grab() {
        TODO("Not yet implemented")
    }

    override fun turn(angle: Int) {
        TODO("Not yet implemented")
    }
}