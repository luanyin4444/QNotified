package me.singleneuron.hook

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import me.singleneuron.base.BaseDelayableConditionalHookAdapter
import me.singleneuron.base.BaseDelayableHookAdapter
import me.singleneuron.util.NoAppletUtil
import me.singleneuron.util.QQVersion
import nil.nadph.qnotified.util.LicenseStatus
import nil.nadph.qnotified.util.Utils

object NoApplet : BaseDelayableConditionalHookAdapter("noapplet") {

    override val condition: () -> Boolean = {Utils.getHostVersionCode()>=QQVersion.QQ_8_0_0}

    override fun doInit(): Boolean {
        try {

            //val jumpActivityClass = Class.forName("com.tencent.mobileqq.activity.JumpActivity")
            Utils.logd("NoApplet inited")
            XposedBridge.hookAllMethods(Activity::class.java, "getIntent", object : XposedMethodHookAdapter() {
                override fun afterMethod(param: MethodHookParam?) {
                    if (param!!.thisObject::class.java.simpleName != "JumpActivity") return
                    //Utils.logd("NoApplet started: "+param.thisObject::class.java.simpleName)
                    val originIntent = param.result as Intent
                    /*Utils.logd("NoApplet getIntent: $originIntent")
                    Utils.logd("NoApplet getExtra: ${originIntent.extras}")*/
                    val originUri = originIntent.data
                    val schemeUri = originUri.toString()
                    if (!schemeUri.contains("mini_program")) return
                    Utils.logd("transfer applet intent: $schemeUri")
                    val processScheme = NoAppletUtil.removeMiniProgramNode(schemeUri)
                    val newScheme = NoAppletUtil.replace(processScheme, "req_type", "MQ==")
                    val newUri = Uri.parse(newScheme)
                    originIntent.data = newUri
                    originIntent.component = null
                    param.result = originIntent
                }
            })
        } catch (e: Exception) {
            Utils.log(e)
            return false
        }
        return true
    }

}