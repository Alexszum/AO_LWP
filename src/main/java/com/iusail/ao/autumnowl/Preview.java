package com.iusail.ao.autumnowl;


import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

public class Preview extends Activity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Intent intent = new Intent();
            intent.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        String pkg = LiveWallpaperService.class.getPackage().getName();
        String cls = LiveWallpaperService.class.getCanonicalName();
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT ,new ComponentName(pkg, cls));
        startActivityForResult(intent,0);
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        finish();
    }

}
