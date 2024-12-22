package net.deathlksr.fuguribeta.utils.timing

class MSTimer {
    var time = -1L

    fun hasTimePassed(ms: Number) = System.currentTimeMillis() >= time + ms.toLong()

    fun hasTimeLeft(ms: Number) = ms.toLong() + time - System.currentTimeMillis()

    fun elapsedTime() = (System.currentTimeMillis() - time)

    fun reset() {
        time = System.currentTimeMillis()
    }

    fun zero() {
        time = -1L
    }
}
