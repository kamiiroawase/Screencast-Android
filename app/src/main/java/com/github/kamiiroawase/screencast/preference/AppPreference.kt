package com.github.kamiiroawase.screencast.preference

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import androidx.core.content.edit

class AppPreference(private val context: Application) {
    companion object {
        private var INSTANCE: AppPreference? = null

        fun getInstance(): AppPreference {
            return INSTANCE!!
        }

        fun init(context: Application) {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = AppPreference(context)
                }
            }
        }
    }

    fun getSettingsPreferences(): SharedPreferences {
        return context.getSharedPreferences(
            "fcd08245-c8fd-4bbf-be42-5ee99c7ad0c1",
            Context.MODE_PRIVATE
        )
    }

    fun getSettingsAudioRecordSwitch(): String {
        return getSettingsPreferences().getString("audio_record_switch", "0")!!
    }

    fun setSettingsAudioRecordSwitch(value: String) {
        getSettingsPreferences().edit { putString("audio_record_switch", value) }
    }

    fun getSettingsLupingzhenshu(): String {
        return getSettingsPreferences().getString("lupingzhenshu", "30")!!
    }

    fun setSettingsLupingzhenshu(value: String) {
        getSettingsPreferences().edit { putString("lupingzhenshu", value) }
    }

    fun getSettingsLupingfenbianlv(): String {
        return getSettingsPreferences().getString("lupingfenbianlv", "0")!!
    }

    fun setSettingsLupingfenbianlv(value: String) {
        getSettingsPreferences().edit { putString("lupingfenbianlv", value) }
    }

    fun getSettingsLupingquyu(): Rect? {
        val top = getSettingsPreferences().getString("lupingquyu_top", "-1")!!.toInt()
        val left = getSettingsPreferences().getString("lupingquyu_left", "-1")!!.toInt()
        val right = getSettingsPreferences().getString("lupingquyu_right", "-1")!!.toInt()
        val bottom = getSettingsPreferences().getString("lupingquyu_bottom", "-1")!!.toInt()

        if (top < 0 || left < 0 || right < 0 || bottom < 0) {
            return null
        }

        return Rect(left, top, right, bottom)
    }

    fun setSettingsLupingquyu(value: Rect? = null) {
        getSettingsPreferences().edit {
            if (value != null) {
                putString("lupingquyu_bottom", value.bottom.toString())
                putString("lupingquyu_right", value.right.toString())
                putString("lupingquyu_left", value.left.toString())
                putString("lupingquyu_top", value.top.toString())
            } else {
                putString("lupingquyu_bottom", "-1")
                putString("lupingquyu_right", "-1")
                putString("lupingquyu_left", "-1")
                putString("lupingquyu_top", "-1")
            }
        }
    }

    fun getSettingsLupinghuazhi(): String {
        return getSettingsPreferences().getString("lupinghuazhi", "8")!!
    }

    fun setSettingsLupinghuazhi(value: String) {
        getSettingsPreferences().edit { putString("lupinghuazhi", value) }
    }

    fun getSettingsLupingfangxiang(): String {
        return getSettingsPreferences().getString("lupingfangxiang", "1")!!
    }

    fun setSettingsLupingfangxiang(value: String) {
        getSettingsPreferences().edit { putString("lupingfangxiang", value) }
    }

    fun getSettingsXuanfuqiuSwitch(): String {
        return getSettingsPreferences().getString("xuanfuqiu_switch", "0")!!
    }

    fun setSettingsXuanfuqiuSwitch(value: String) {
        getSettingsPreferences().edit { putString("xuanfuqiu_switch", value) }
    }

    fun getSettingsBaocunmulu(): String {
        return getSettingsPreferences().getString("baocunmulu", "")!!
    }

    fun setSettingsBaocunmulu(value: String) {
        getSettingsPreferences().edit { putString("baocunmulu", value) }
    }

    fun getSettingsXuanfuqiuLocation(): Pair<Float, Float> {
        return Pair(
            getSettingsPreferences().getFloat("xuanfuqiu_location_x", 9999f),
            getSettingsPreferences().getFloat("xuanfuqiu_location_y", 600f)
        )
    }

    fun setSettingsXuanfuqiuLocation(x: Float, y: Float) {
        getSettingsPreferences().edit {
            putFloat("xuanfuqiu_location_x", x)
            putFloat("xuanfuqiu_location_y", y)
        }
    }
}