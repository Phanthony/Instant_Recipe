package com.phanthony.instantrecipe.extensions

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.phanthony.instantrecipe.R

fun Fragment.getErrorDialog(errorMessage: String, context: Context): AlertDialog.Builder{
    return AlertDialog.Builder(context).apply {
        setTitle(getString(R.string.error))
        setMessage(errorMessage)
        setPositiveButton(getString(R.string.Ok)) { dialog : DialogInterface, _: Int ->
            dialog.dismiss()
        }
    }
}