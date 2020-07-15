

package com.loki.tiktok.videoTrimmer.interfaces;

import android.net.Uri;

public interface OptiOnTrimVideoListener {

    void onTrimStarted(int startPosition, int endPosition);

    void getResult(final Uri uri);

    void cancelAction();

    void onError(final String message);
}
