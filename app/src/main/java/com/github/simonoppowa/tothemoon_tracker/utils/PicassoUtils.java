package com.github.simonoppowa.tothemoon_tracker.utils;

import com.github.simonoppowa.tothemoon_tracker.activities.MainActivity;

public class PicassoUtils {

    public static String getFullCoinImageUrl(String part) {

        return MainActivity.CRYPTOCOMPARE_BASE_URL + part;
    }

    public static String getFullCoinImageUrlSmall(String part) {
        return getFullCoinImageUrl(part) + "?width=50";
    }
}
