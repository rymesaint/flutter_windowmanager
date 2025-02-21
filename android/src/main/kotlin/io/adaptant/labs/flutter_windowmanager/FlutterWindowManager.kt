package io.adaptant.labs.flutter_windowmanager

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class FlutterWindowManager : FlutterPlugin, MethodCallHandler, ActivityAware {
    private var activity: Activity? = null
    private lateinit var channel : MethodChannel

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_windowmanager")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    }

    private fun validLayoutParam(flag: Int): Boolean {
        return when (flag) {
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
            WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_SCALED,
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
            WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH -> true
            WindowManager.LayoutParams.FLAG_BLUR_BEHIND -> Build.VERSION.SDK_INT < 15
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD -> Build.VERSION.SDK_INT in 5 until 26
            WindowManager.LayoutParams.FLAG_DITHER -> Build.VERSION.SDK_INT < 17
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS -> Build.VERSION.SDK_INT >= 21
            WindowManager.LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR -> Build.VERSION.SDK_INT >= 22
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN -> Build.VERSION.SDK_INT >= 18
            WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE -> Build.VERSION.SDK_INT >= 19
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED -> Build.VERSION.SDK_INT < 27
            WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING -> Build.VERSION.SDK_INT < 20
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS -> Build.VERSION.SDK_INT >= 19
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON -> Build.VERSION.SDK_INT < 27
            else -> false
        }
    }

    private fun validLayoutParams(result: Result, flags: Int): Boolean {
        for (i in 0 until Int.SIZE_BITS) {
            val flag = 1 shl i
            if ((flags and flag) != 0 && !validLayoutParam(flag)) {
                result.error("FlutterWindowManagerPlugin", "Invalid flag: ${flag.toString(16)}", null)
                return false
            }
        }
        return true
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        val flags: Int? = call.argument<Int>("flags")

        if (activity == null) {
            result.error("FlutterWindowManager", "Current activity is null", null)
            return
        }

        if (flags == null) {
            result.error("FlutterWindowManager", "Flags argument is missing", null)
            return
        }

        if (!validLayoutParams(result, flags)) return

        when (call.method) {
            "addFlags" -> {
                activity?.window?.addFlags(flags)
                result.success(true)
            }
            "clearFlags" -> {
                activity?.window?.clearFlags(flags)
                result.success(true)
            }
            "hideOverlayWindows" -> {
                if (Build.VERSION.SDK_INT >= 30) {
                    activity?.window?.setHideOverlayWindows(true)
                    result.success(true)
                } else {
                    result.error("FlutterWindowManagerPlugin", "setHideOverlayWindows() not available below API 30", null)
                }
            }
            else -> result.notImplemented()
        }
    }

    override fun onAttachedToActivity(@NonNull binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(@NonNull binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }
}
