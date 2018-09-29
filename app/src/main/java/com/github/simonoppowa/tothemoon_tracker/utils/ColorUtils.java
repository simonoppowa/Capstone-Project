package com.github.simonoppowa.tothemoon_tracker.utils;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.widget.TextView;

import com.github.simonoppowa.tothemoon_tracker.R;

public class ColorUtils {

    public static int pickColorFromChange(Context context, double change) {

        if(change > 10) {
            return context.getResources().getColor(R.color.high_increase_color);
        }
        if(change > 0) {
            return context.getResources().getColor(R.color.low_increase_color);
        }
        if(change < -10) {
            return context.getResources().getColor(R.color.high_decrease_color);
        }
        if(change < 0) {
            return context.getResources().getColor(R.color.low_decrease_color);
        }

        return context.getResources().getColor(R.color.defaultTextColor);

    }

    public static Shader pickShaderFromChange(Context context, double change, TextView textView) {

        int textColor = pickColorFromChange(context, change);
        int shadeColor;

        if(change < 0) {
            shadeColor = context.getResources().getColor(R.color.high_decrease_shade_color);
        } else {
            shadeColor = context.getResources().getColor(R.color.high_increase_shade_color);
        }

        return new LinearGradient(0, 0, 0, textView.getTextSize(),
                new int[]{textColor,  shadeColor},
               null, Shader.TileMode.CLAMP);
    }
}
