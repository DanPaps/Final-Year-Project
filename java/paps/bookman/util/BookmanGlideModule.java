package paps.bookman.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

/**
 * Daniel Pappoe
 * bookman-android
 */

@GlideModule
public class BookmanGlideModule extends AppGlideModule {
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    @SuppressLint("CheckResult")
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        RequestOptions defaultOptions = new RequestOptions();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            defaultOptions.format((activityManager.isLowRamDevice()) ? DecodeFormat.PREFER_RGB_565 : DecodeFormat.PREFER_ARGB_8888);
            defaultOptions.disallowHardwareConfig();
            builder.setDefaultRequestOptions(defaultOptions);
        }
    }
}
