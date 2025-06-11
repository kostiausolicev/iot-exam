package ru.guap.thing.robot

import kotlinx.coroutines.delay
import ru.guap.dto.DataDto
import ru.guap.dto.DataPhysDto
import ru.guap.dto.Threshold
import ru.guap.dto.ThresholdsDto
import ru.guap.thing.Device
import ru.guap.thing.robot.component.Servo
import java.time.LocalDateTime

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
        protected set(value) {
            servo1.temperature += 10
            servo1.l += 10
            servo1.angle = value
            servo2.temperature -= 5
            field = value
        }
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

    fun saveThresholds(dto: ThresholdsDto) {
        servo1.apply {
            maxM = dto.m1.critMax
            minM = dto.m1.critMin
            warnMinM = dto.m1.warnMin
            warnMaxM = dto.m1.warnMax

            maxTemperature = dto.t1.critMax
            minTemperature = dto.t1.critMin
            warnMaxTemperature = dto.t1.warnMax
            warnMinTemperature = dto.t1.warnMin

            maxL = dto.l1.critMax
            minL = dto.l1.critMin
            warnMaxL = dto.l1.warnMax
            warnMinL = dto.l1.warnMin
        }
        servo2.apply {
            maxM = dto.m2.critMax
            minM = dto.m2.critMin
            warnMinM = dto.m2.warnMin
            warnMaxM = dto.m2.warnMax

            maxTemperature = dto.t2.critMax
            minTemperature = dto.t2.critMin
            warnMaxTemperature = dto.t2.warnMax
            warnMinTemperature = dto.t2.warnMin

            maxL = dto.l2.critMax
            minL = dto.l2.critMin
            warnMaxL = dto.l2.warnMax
            warnMinL = dto.l2.warnMin
        }
        servo3.apply {
            maxM = dto.m3.critMax
            minM = dto.m3.critMin
            warnMinM = dto.m3.warnMin
            warnMaxM = dto.m3.warnMax

            maxTemperature = dto.t3.critMax
            minTemperature = dto.t3.critMin
            warnMaxTemperature = dto.t3.warnMax
            warnMinTemperature = dto.t3.warnMin

            maxL = dto.l3.critMax
            minL = dto.l3.critMin
            warnMaxL = dto.l3.warnMax
            warnMinL = dto.l3.warnMin
        }
        servo4.apply {
            maxM = dto.m4.critMax
            minM = dto.m4.critMin
            warnMinM = dto.m4.warnMin
            warnMaxM = dto.m4.warnMax

            maxTemperature = dto.t4.critMax
            minTemperature = dto.t4.critMin
            warnMaxTemperature = dto.t4.warnMax
            warnMinTemperature = dto.t4.warnMin

            maxL = dto.l4.critMax
            minL = dto.l4.critMin
            warnMaxL = dto.l4.warnMax
            warnMinL = dto.l4.warnMin
        }
        servo5.apply {
            maxM = dto.m5.critMax
            minM = dto.m5.critMin
            warnMinM = dto.m5.warnMin
            warnMaxM = dto.m5.warnMax

            maxTemperature = dto.t5.critMax
            minTemperature = dto.t5.critMin
            warnMaxTemperature = dto.t5.warnMax
            warnMinTemperature = dto.t5.warnMin

            maxL = dto.l5.critMax
            minL = dto.l5.critMin
            warnMaxL = dto.l5.warnMax
            warnMinL = dto.l5.warnMin
        }
        servo6?.apply {
            maxM = dto.m6.critMax
            minM = dto.m6.critMin
            warnMinM = dto.m6.warnMin
            warnMaxM = dto.m6.warnMax

            maxTemperature = dto.t6.critMax
            minTemperature = dto.t6.critMin
            warnMaxTemperature = dto.t6.warnMax
            warnMinTemperature = dto.t6.warnMin

            maxL = dto.l6.critMax
            minL = dto.l6.critMin
            warnMaxL = dto.l6.warnMax
            warnMinL = dto.l6.warnMin
        }
    }

    fun getThresholds(): ThresholdsDto = ThresholdsDto(
        m1 = Threshold(
            warnMin = servo1.warnMinM,
            warnMax = servo1.warnMaxM,
            critMin = servo1.minM,
            critMax = servo1.maxM
        ),
        m2 = Threshold(
            warnMin = servo2.warnMinM,
            warnMax = servo2.warnMaxM,
            critMin = servo2.minM,
            critMax = servo2.maxM
        ),
        m3 = Threshold(
            warnMin = servo3.warnMinM,
            warnMax = servo3.warnMaxM,
            critMin = servo3.minM,
            critMax = servo3.maxM
        ),
        m4 = Threshold(
            warnMin = servo4.warnMinM,
            warnMax = servo4.warnMaxM,
            critMin = servo4.minM,
            critMax = servo4.maxM
        ),
        m5 = Threshold(
            warnMin = servo5.warnMinM,
            warnMax = servo5.warnMaxM,
            critMin = servo5.minM,
            critMax = servo5.maxM
        ),
        m6 = Threshold(
            warnMin = servo6?.warnMinM ?: 0,
            warnMax = servo6?.warnMaxM ?: 0,
            critMin = servo6?.minM ?: 0,
            critMax = servo6?.maxM ?: 0
        ),
        t1 = Threshold(
            warnMin = servo1.warnMinTemperature,
            warnMax = servo1.warnMaxTemperature,
            critMin = servo1.minTemperature,
            critMax = servo1.maxTemperature
        ),
        t2 = Threshold(
            warnMin = servo2.warnMinTemperature,
            warnMax = servo2.warnMaxTemperature,
            critMin = servo2.minTemperature,
            critMax = servo2.maxTemperature
        ),
        t3 = Threshold(
            warnMin = servo3.warnMinTemperature,
            warnMax = servo3.warnMaxTemperature,
            critMin = servo3.minTemperature,
            critMax = servo3.maxTemperature
        ),
        t4 = Threshold(
            warnMin = servo4.warnMinTemperature,
            warnMax = servo4.warnMaxTemperature,
            critMin = servo4.minTemperature,
            critMax = servo4.maxTemperature
        ),
        t5 = Threshold(
            warnMin = servo5.warnMinTemperature,
            warnMax = servo5.warnMaxTemperature,
            critMin = servo5.minTemperature,
            critMax = servo5.maxTemperature
        ),
        t6 = Threshold(
            warnMin = servo6?.warnMinTemperature ?: 0,
            warnMax = servo6?.warnMaxTemperature ?: 0,
            critMin = servo6?.minTemperature ?: 0,
            critMax = servo6?.maxTemperature ?: 0
        ),
        l1 = Threshold(
            warnMin = servo1.warnMinL,
            warnMax = servo1.warnMaxL,
            critMin = servo1.minL,
            critMax = servo1.maxL
        ),
        l2 = Threshold(
            warnMin = servo2.warnMinL,
            warnMax = servo2.warnMaxL,
            critMin = servo2.minL,
            critMax = servo2.maxL
        ),
        l3 = Threshold(
            warnMin = servo3.warnMinL,
            warnMax = servo3.warnMaxL,
            critMin = servo3.minL,
            critMax = servo3.maxL
        ),
        l4 = Threshold(
            warnMin = servo4.warnMinL,
            warnMax = servo4.warnMaxL,
            critMin = servo4.minL,
            critMax = servo4.maxL
        ),
        l5 = Threshold(
            warnMin = servo5.warnMinL,
            warnMax = servo5.warnMaxL,
            critMin = servo5.minL,
            critMax = servo5.maxL
        ),
        l6 = Threshold(
            warnMin = servo6?.warnMinL ?: 0,
            warnMax = servo6?.warnMaxL ?: 0,
            critMin = servo6?.minL ?: 0,
            critMax = servo6?.maxL ?: 0
        )
    )

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

    override fun toDataPsycDto(n: Int): DataPhysDto = DataPhysDto(
        deviceName = deviceName(),
        n = n,
        m1 = servo1.getPsycM(),
        m2 = servo2.getPsycM(),
        m3 = servo3.getPsycM(),
        m4 = servo4.getPsycM(),
        m5 = servo5.getPsycM(),
        m6 = servo6?.getPsycM(),
        t1 = servo1.getPsycTemperature(),
        t2 = servo2.getPsycTemperature(),
        t3 = servo3.getPsycTemperature(),
        t4 = servo4.getPsycTemperature(),
        t5 = servo5.getPsycTemperature(),
        t6 = servo6?.getPsycTemperature(),
        l1 = servo1.getPsycL(),
        l2 = servo2.getPsycL(),
        l3 = servo3.getPsycL(),
        l4 = servo4.getPsycL(),
        l5 = servo5.getPsycL(),
        l6 = servo6?.getPsycL(),
        x = X,
        y = Y,
        t = T,
        timestamp = LocalDateTime.now(),
        deviceId = id
    )

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