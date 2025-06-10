package ru.guap.thing.robot

import kotlinx.coroutines.delay
import ru.guap.thing.Device
import ru.guap.thing.robot.component.Servo
import kotlin.math.pow
import kotlin.math.sqrt

abstract class Robot : Device {
    private val servo1: Servo
    private val servo2: Servo
    private val servo3: Servo
    private val servo4: Servo
    private val servo5: Servo
    private val servo6: Servo?

    var X: Int = 0
        protected set
    var Y: Int = 0
        protected set
    var T: Int = 0
        protected set
    var grab: Boolean = false

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

    fun grab(grab: Boolean) {
        this.grab = grab
    }

    suspend fun moveTo(x: Int?, y: Int?, callback: (suspend () -> Unit)? = null) {
        running = true
        delay(10_000)
        X = x ?: X
        Y = y ?: Y
        running = false
        callback?.invoke()
    }

    fun turn(angle: Int) {

    }

    fun toDto() {

    }

    override fun connect() {
        this.status = true
    }

    override fun isConnected(): Boolean {
        return this.status
    }
}