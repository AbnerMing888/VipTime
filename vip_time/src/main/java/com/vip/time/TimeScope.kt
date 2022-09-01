package com.vip.time

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import java.io.Closeable
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 异步协程作用域
 */
@Suppress("unused", "MemberVisibilityCanBePrivate", "NAME_SHADOWING")
open class AndroidScope(
    lifecycleOwner: LifecycleOwner? = null,
    lifeEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY,
    val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : CoroutineScope, Closeable {
    init {
        lifecycleOwner?.lifecycle?.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (lifeEvent == event) cancel()
            }
        })
    }

    protected var catch: (AndroidScope.(Throwable) -> Unit)? = null
    protected var finally: (AndroidScope.(Throwable?) -> Unit)? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        catch(throwable)
    }

    val scopeGroup = exceptionHandler

    override val coroutineContext: CoroutineContext =
        dispatcher + exceptionHandler + SupervisorJob()


    open fun launch(block: suspend CoroutineScope.() -> Unit): AndroidScope {
        launch(EmptyCoroutineContext) {
            block()
        }.invokeOnCompletion {
            finally(it)
        }
        return this
    }

    protected open fun catch(e: Throwable) {
        catch?.invoke(this@AndroidScope, e) ?: handleError(e)
    }

    /**
     * @param e 如果发生异常导致作用域执行完毕, 则该参数为该异常对象, 正常结束则为null
     */
    protected open fun finally(e: Throwable?) {
        finally?.invoke(this@AndroidScope, e)
    }

    /**
     * 当作用域内发生异常时回调
     */
    open fun catch(block: AndroidScope.(Throwable) -> Unit = {}): AndroidScope {
        this.catch = block
        return this
    }

    /**
     * 无论正常或者异常结束都将最终执行
     */
    open fun finally(block: AndroidScope.(Throwable?) -> Unit = {}): AndroidScope {
        this.finally = block
        return this
    }


    /**
     * 错误处理
     */
    open fun handleError(e: Throwable) {

    }

    open fun cancel(cause: CancellationException? = null) {
        val job = coroutineContext[Job]
            ?: error("Scope cannot be cancelled because it does not have a job: $this")
        job.cancel(cause)
    }

    open fun cancel(
        message: String,
        cause: Throwable? = null
    ) = cancel(CancellationException(message, cause))

    override fun close() {
        cancel()
    }

}

/**
 * 异步作用域
 *
 * 该作用域生命周期跟随整个应用, 注意内存泄漏
 */
fun scope(
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    block: suspend CoroutineScope.() -> Unit
): AndroidScope {
    return AndroidScope(dispatcher = dispatcher).launch(block)
}

/**
 * AUTHOR:AbnerMing
 * INTRODUCE:定时带时间
 */
fun FragmentActivity.setInterval(end: Long, period: Long, block: Interval.(count: Long) -> Unit) {
    val interval = Interval(end, period, TimeUnit.SECONDS).life(this)
    interval.subscribe {
        block.invoke(this, count)
    }.finish {
        block.invoke(this, count)
    }.start()
}

/**
 * AUTHOR:AbnerMing
 * INTRODUCE:定时带时间
 */
fun Fragment.setInterval(end: Long, period: Long, block: Interval.(count: Long) -> Unit) {
    requireActivity().setInterval(end, period, block)
}


/**
 * AUTHOR:AbnerMing
 * INTRODUCE:倒计时不带时间
 */
fun FragmentActivity.setTimeOut(end: Long, block: () -> Unit) {
    val interval = Interval(end, 1, TimeUnit.SECONDS).life(this)
    interval.finish {
        block.invoke()
    }.start()
}

/**
 * AUTHOR:AbnerMing
 * INTRODUCE:倒计时不带时间
 */
fun Fragment.setTimeOut(end: Long, block: () -> Unit) {
    requireActivity().setTimeOut(end, block)
}

/**
 * AUTHOR:AbnerMing
 * INTRODUCE:倒计时带时间
 */

fun FragmentActivity.setTimeDown(
    end: Long,
    period: Long = 1,
    block: Interval.(count: Long) -> Unit
) {
    // 倒计时轮询器, 当[start]]比[end]值大, 且end不等于-1时, 即为倒计时
    val interval = Interval(0, period, TimeUnit.SECONDS, end).life(this)
    interval.subscribe {
        block.invoke(this, count)
    }.finish {
        block.invoke(this, count)
    }.start()
}

/**
 * AUTHOR:AbnerMing
 * INTRODUCE:倒计时带时间
 */
fun Fragment.setTimeDown(
    end: Long,
    period: Long = 1,
    block: Interval.(count: Long) -> Unit
) {
    requireActivity().setTimeDown(end, period, block)
}


/**
 * AUTHOR:AbnerMing
 * INTRODUCE:无限定时
 */
fun Context.setIntervalWireless(period: Long, block: (count: Long) -> Unit) {
    val interval = Interval(period, TimeUnit.SECONDS) // 每秒回调一次, 不会自动结束
    interval.subscribe {
        block.invoke(count)
    }.start()
}

/**
 * AUTHOR:AbnerMing
 * INTRODUCE:无限定时
 */
fun Fragment.setIntervalWireless(period: Long, block: (count: Long) -> Unit) {
    setIntervalWireless(period, block)
}


