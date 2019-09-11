package cn.missfresh.flutter_hybrid

import android.os.Handler
import cn.missfresh.flutter_hybrid.interfaces.IContainerLifecycle
import cn.missfresh.flutter_hybrid.interfaces.IFlutterViewContainer

/**
 * Created by sjl
 * on 2019-09-02
 */
class ContainerLifecycleManager : IContainerLifecycle {

    private val mContainer: IFlutterViewContainer
    val mUniqueId: String

    private var mContainerStatus = ContainerLifecycleEnum.STATE_UN_KNOW
    private val mProxy = LifecycleProxy()

    private val mHandler = Handler()

    constructor(container: IFlutterViewContainer) {
        mContainer = container
        mUniqueId = System.currentTimeMillis().toString() + "_" + hashCode()
    }

    override fun onCreate() {
        mContainerStatus = ContainerLifecycleEnum.STATE_CREATED
        mContainer.getFHFlutterView().resumeFlutterView()
        mProxy.create()
    }

    override fun onAppear() {
        mContainerStatus = ContainerLifecycleEnum.STATE_APPEAR
        mContainer.getFHFlutterView().resumeFlutterView()
        mProxy.appear()
    }

    override fun onDisappear() {
        mProxy.disappear()
        mContainerStatus = ContainerLifecycleEnum.STATE_DISAPPEAR

        /**
         * If current container is finishing, we should call destroy flutter page early.
         */
        if (mContainer.isFinishing()) {
            mHandler.post { mProxy.destroy() }
        }
    }

    override fun onDestroy() {
        mProxy.destroy()
        mContainerStatus = ContainerLifecycleEnum.STATE_DESTROYED
    }

    override fun getContainer(): IFlutterViewContainer {
        return mContainer
    }

    override fun getState(): Int {
        return mContainerStatus.status
    }

    override fun containerId(): String {
        return mUniqueId
    }

    private inner class LifecycleProxy {
        private var containerStatus = ContainerLifecycleEnum.STATE_UN_KNOW

        fun create() {
            if (containerStatus == ContainerLifecycleEnum.STATE_UN_KNOW) {

                FlutterHybridPlugin.instance.lifecycleMessager().invokeMethod(
                        "nativePageDidInit", mContainer.getContainerName(),
                        mContainer.getContainerParams(), mUniqueId)

                Logger.e("LifecycleProxy nativePageDidInit")

                containerStatus = ContainerLifecycleEnum.STATE_CREATED

                FlutterHybridPlugin.instance.lifecycleMessager().invokeMethod(
                        "nativePageWillAppear", mContainer.getContainerName(),
                        mContainer.getContainerParams(), mUniqueId)
            }
        }

        fun appear() {
            mHandler.postDelayed({

                FlutterHybridPlugin.instance.lifecycleMessager().invokeMethod(
                        "nativePageDidAppear", mContainer.getContainerName(),
                        mContainer.getContainerParams(), mUniqueId)

            }, 10)
            Logger.e("LifecycleProxy nativePageDidAppear")
            containerStatus = ContainerLifecycleEnum.STATE_APPEAR
        }

        fun disappear() {
            if (containerStatus < ContainerLifecycleEnum.STATE_DISAPPEAR) {

                FlutterHybridPlugin.instance.lifecycleMessager().invokeMethod(
                        "nativePageWillDisappear", mContainer.getContainerName(),
                        mContainer.getContainerParams(), mUniqueId)


                FlutterHybridPlugin.instance.lifecycleMessager().invokeMethod(
                        "nativePageDidDisappear", mContainer.getContainerName(),
                        mContainer.getContainerParams(), mUniqueId)

                Logger.e("LifecycleProxy nativePageWillDisappear and nativePageDidDisappear")
                containerStatus = ContainerLifecycleEnum.STATE_DISAPPEAR
            }
        }

        fun destroy() {
            if (containerStatus < ContainerLifecycleEnum.STATE_DESTROYED) {

                FlutterHybridPlugin.instance.lifecycleMessager().invokeMethod(
                        "nativePageWillDealloc", mContainer.getContainerName(),
                        mContainer.getContainerParams(), mUniqueId)

                Logger.e("LifecycleProxy nativePageWillDealloc")
                containerStatus = ContainerLifecycleEnum.STATE_DESTROYED
            }
        }
    }
}