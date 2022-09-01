@file:OptIn(ObsoleteCoroutinesApi::class)
@file:Suppress("MemberVisibilityCanBePrivate")

package com.vip.time

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.TickerMode
import kotlinx.coroutines.channels.ticker
import java.io.Closeable
import java.io.Serializable
import java.util.concurrent.TimeUnit

/**
 * 创建一个会自动结束的轮循器
 *
 * 操作
 * 1. 开启 [start] 只有在闲置状态下才可以开始
 * 2. 停止 [stop]
 * 3. 暂停 [pause]
 * 4. 继续 [resume]
 * 5. 重置 [reset] 重置不会导致轮循器停止
 * 6. 开关 [switch] 开启|暂停切换
 * 7. 生命周期 [life]
 *
 * 函数回调: 允许多次订阅同一个轮循器
 * 1. 每个事件 [subscribe]
 * 2. 停止或者结束 [finish]
 *
 * @param end 结束值, -1 表示永远不结束
 * @param period 计时器间隔
 * @param unit 计时器单位
 * @param initialDelay 第一次事件的间隔时间, 默认0
 * @param start 开始值, 当[start]]比[end]值大, 且end不等于-1时, 即为倒计时, 反之正计时
 */
open class Interval(
    var end: Long,
    private val period: Long,
    private val unit: TimeUnit,
    private val start: Long = 0,
    private val initialDelay: Long = 0
) : Serializable, Closeable {

    /**
     * 创建一个不会自动结束的轮询器/计时器
     *
     * @param period 间隔时间
     * @param unit 时间单位
     * @param initialDelay 初次间隔时间, 默认为0即立即开始
     */
    constructor(
        period: Long,
        unit: TimeUnit,
        initialDelay: Long = 0
    ) : this(-1, period, unit, 0, initialDelay)

    private val subscribeList: MutableList<Interval.(Long) -> Unit> = mutableListOf()
    private val finishList: MutableList<Interval.(Long) -> Unit> = mutableListOf()
    private var countTime = 0L
    private var delay = 0L
    private var scope: AndroidScope? = null
    private lateinit var ticker: ReceiveChannel<Unit>

    /** 轮循器的计数 */
    var count = start

    /** 轮循器当前状态 */
    var state = IntervalStatus.STATE_IDLE
        private set

    // <editor-fold desc="回调">

    /**
     * 订阅轮循器
     * 每次轮循器计时都会调用该回调函数
     * 轮循器完成时会同时触发回调[block]和[finish]
     */
    fun subscribe(block: Interval.(Long) -> Unit) = apply {
        subscribeList.add(block)
    }

    /**
     * 轮循器完成时回调该函数
     * @see stop 执行该函数也会回调finish
     * @see cancel 使用该函数取消轮询器不会回调finish
     */
    fun finish(block: Interval.(Long) -> Unit) = apply {
        finishList.add(block)
    }

    // </editor-fold>

    // <editor-fold desc="操作">

    /**
     * 开始
     * 如果当前为暂停状态将重新开始轮询
     */
    fun start() = apply {
        if (state == IntervalStatus.STATE_ACTIVE) return this
        state = IntervalStatus.STATE_ACTIVE
        count = start
        launch()
    }

    /**
     * 停止
     */
    fun stop() {
        if (state == IntervalStatus.STATE_IDLE) return
        scope?.cancel()
        state = IntervalStatus.STATE_IDLE
        finishList.forEach {
            it.invoke(this, count)
        }
    }

    /**
     * 取消
     * 区别于[stop]并不会执行[finish]
     */
    fun cancel() {
        if (state == IntervalStatus.STATE_IDLE) return
        scope?.cancel()
        state = IntervalStatus.STATE_IDLE
    }

    /** 等于[cancel] */
    override fun close() = cancel()

    /**
     * 切换轮循器开始或结束
     * 假设轮询器为暂停[IntervalStatus.STATE_PAUSE]状态将继续运行[resume]
     */
    fun switch() {
        when (state) {
            IntervalStatus.STATE_ACTIVE -> stop()
            IntervalStatus.STATE_IDLE -> start()
            IntervalStatus.STATE_PAUSE -> resume()
        }
    }

    /**
     * 暂停
     */
    fun pause() {
        if (state != IntervalStatus.STATE_ACTIVE) return
        scope?.cancel()
        state = IntervalStatus.STATE_PAUSE
        delay = System.currentTimeMillis() - countTime
    }

    /**
     * 继续
     * 要求轮循器为暂停状态[IntervalStatus.STATE_PAUSE], 否则无效
     */
    fun resume() {
        if (state != IntervalStatus.STATE_PAUSE) return
        state = IntervalStatus.STATE_ACTIVE
        launch(delay)
    }

    /**
     * 重置
     */
    fun reset() {
        count = start
        delay = unit.toMillis(initialDelay)
        scope?.cancel()
        if (state == IntervalStatus.STATE_ACTIVE) launch()
    }

    /**
     * 绑定生命周期, 在指定生命周期发生时取消轮循器
     * @param lifecycleOwner 默认在销毁时取消轮循器
     * @param lifeEvent 销毁的时机, 默认为 ON_STOP 界面停止时停止轮循器
     *
     * Fragment的显示/隐藏不会调用onDestroy, 故轮循器默认是在ON_STOP停止, 如果你设置ON_DESTORY请考虑Fragment的情况下
     */
    fun life(
        lifecycleOwner: LifecycleOwner,
        lifeEvent: Lifecycle.Event = Lifecycle.Event.ON_STOP
    ) = apply {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (lifeEvent == event) cancel()
            }
        })
    }

    private fun launch(delay: Long = unit.toMillis(initialDelay)) {
        scope = scope {

            ticker = ticker(unit.toMillis(period), delay, mode = TickerMode.FIXED_DELAY)

            for (unit in ticker) {

                subscribeList.forEach {
                    it.invoke(this@Interval, count)
                }

                if (end != -1L && count == end) {
                    scope?.cancel()
                    state = IntervalStatus.STATE_IDLE
                    finishList.forEach {
                        it.invoke(this@Interval, count)
                    }
                }

                if (end != -1L && start > end) count-- else count++
                countTime = System.currentTimeMillis()
            }
        }
    }

}

