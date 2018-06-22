package uk.aurorastudio.soundtimer.util

import android.content.Context
import android.preference.PreferenceManager
import java.security.AccessControlContext

class PrefUtil {
    companion object {
        private const val SOUND_VALUE_ID = "uk.aurorastudio.soundtimer.sound_value"

        fun getSoundValue(context: Context): Int{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getInt(SOUND_VALUE_ID, 0)
        }

        fun setSoundValue(value: Int, context: Context){
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putInt(SOUND_VALUE_ID, value)
            editor.apply()
        }
    }
}