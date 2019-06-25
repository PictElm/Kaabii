package com.patatos.sac.kaabii

import android.os.Bundle
import android.preference.PreferenceActivity

class MyPreferencesActivity : PreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.addPreferencesFromResource(R.xml.mypreferences)
    }

}
