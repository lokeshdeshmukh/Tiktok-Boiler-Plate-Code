

package com.loki.tiktok.videoTrimmer.interfaces;

import com.loki.tiktok.videoTrimmer.view.OptiRangeSeekBarView;

public interface OptiOnRangeSeekBarListener {
    void onCreate(OptiRangeSeekBarView rangeSeekBarView, int index, float value);

    void onSeek(OptiRangeSeekBarView rangeSeekBarView, int index, float value);

    void onSeekStart(OptiRangeSeekBarView rangeSeekBarView, int index, float value);

    void onSeekStop(OptiRangeSeekBarView rangeSeekBarView, int index, float value);
}
