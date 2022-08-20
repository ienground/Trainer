package net.ienlab.trainer.utils

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import net.ienlab.trainer.R

class MyBottomSheetDialog(context: Context, var cancel: Boolean = true): BottomSheetDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (context.resources.getBoolean(R.bool.is_tablet_landscape)) window?.setLayout(1200, ViewGroup.LayoutParams.MATCH_PARENT)
        behavior.peekHeight = 2000
        setCancelable(cancel)
    }
}