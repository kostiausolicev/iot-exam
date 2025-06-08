package ru.guap.thing.robot

import ru.guap.thing.Connectable
import ru.guap.thing.robot.component.Servo

abstract class Robot : Connectable {
    private val servo1: Servo
    private val servo2: Servo
    private val servo3: Servo
    private val servo4: Servo
    private val servo5: Servo
    private val servo6: Servo?

    private var status: Boolean = false

    protected constructor(
        servo1: Servo,
        servo2: Servo,
        servo3: Servo,
        servo4: Servo,
        servo5: Servo,
        servo6: Servo?
    ) {
        this.servo1 = servo1
        this.servo2 = servo2
        this.servo3 = servo3
        this.servo4 = servo4
        this.servo5 = servo5
        this.servo6 = servo6
    }

    abstract fun type(): String
    abstract fun moveTo(x: Int, y: Int, z: Int)
    abstract fun grab()
    abstract fun turn(angle: Int)

    override fun connect() {
        this.status = true
    }

    override fun isConnected(): Boolean {
        return this.status
    }
}