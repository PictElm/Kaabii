package com.patatos.sac.kaabii

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View

class SetWallpaperActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.main)
    }

    fun onClick(view: View) {
        Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).also {
            it.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this, MyWallpaperService::class.java)
            )
            this.startActivity(it)
        }
    }

}
