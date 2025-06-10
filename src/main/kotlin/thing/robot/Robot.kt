package ru.guap.thing.robot

import kotlinx.coroutines.delay
import ru.guap.dto.DataDto
import ru.guap.thing.Device
import ru.guap.thing.robot.component.Servo

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

    suspend fun turn(angle: Int, callback: (suspend () -> Unit)? = null) {
        running = true
        delay(10_000)
        T = angle
        running = false
        callback?.invoke()
    }

    override fun toDataDto(n: Int): DataDto = DataDto(
        deviceName = deviceName(),
        n = n,
        m1 = servo1.m,
        m2 = servo2.m,
        m3 = servo3.m,
        m4 = servo4.m,
        m5 = servo5.m,
        m6 = servo6?.m,
        t1 = servo1.temperature,
        t2 = servo2.temperature,
        t3 = servo3.temperature,
        t4 = servo4.temperature,
        t5 = servo5.temperature,
        t6 = servo6?.temperature,
        l1 = servo1.l,
        l2 = servo2.l,
        l3 = servo3.l,
        l4 = servo4.l,
        l5 = servo5.l,
        l6 = servo6?.l,
        x = X,
        y = Y,
        t = T
    )

    override fun connect() {
        this.status = true
    }

    override fun isConnected(): Boolean {
        return this.status
    }
}