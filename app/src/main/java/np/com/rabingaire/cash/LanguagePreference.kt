package np.com.rabingaire.cash

import android.content.Context

class LanguagePreference(context: Context) {
    private val preferenceName = "LanguagePreference"
    private val preferenceLanguage = "nepaliLanguage"
    private val preference = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun isNepaliLanguage(): Boolean {
        return preference.getBoolean(preferenceLanguage, false)
    }

    fun setNepaliLanguage() {
        val editor = preference.edit()
        if(isNepaliLanguage()) {
            editor.putBoolean(preferenceLanguage, false)
        } else {
            editor.putBoolean(preferenceLanguage, true)
        }
        editor.apply()
    }
}