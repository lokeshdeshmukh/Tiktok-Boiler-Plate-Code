

package com.loki.tiktok.interfaces

import com.loki.tiktok.fragments.BaseCreatorDialogFragment
import java.io.File

interface DialogueHelper {
    fun setHelper(helper: BaseCreatorDialogFragment.CallBacks)
    fun setMode(mode: Int)
    fun setFilePathFromSource(file: File)
    fun setDuration(duration: Long)
}
