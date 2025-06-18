package io.github.duzhaokun123.loader.acf;

import android.os.Build;

import androidx.annotation.RequiresApi;

@RequiresApi(Build.VERSION_CODES.P)
@SuppressWarnings("unused")
public class AppComponentFactory extends android.app.AppComponentFactory {
    static {
        try {
            Loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Error loading AppComponentFactory loader" ,e);
        }
    }
}
