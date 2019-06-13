package sh.hadi.bark

import android.app.Activity
import android.app.AlertDialog
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView

object Prompt {
    fun showInputDialog(activity: Activity, title: String, inputName: String? = null, defaultValue: String? = null, inputType: Int = InputType.TYPE_CLASS_TEXT, callback: (String) -> Unit) {
        Handler(Looper.getMainLooper()).post {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(title)

            val viewInflated = LayoutInflater.from(activity).inflate(R.layout.input_dialog, activity.findViewById(android.R.id.content), false)

            val input = viewInflated.findViewById(R.id.input) as EditText
            inputName?.let { input.hint = it }
            input.inputType = inputType
            defaultValue?.let { input.setText(it, TextView.BufferType.EDITABLE) }

            builder.setView(viewInflated)

            builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                callback(input.text.toString())
            }
            builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.cancel()
                callback("")
            }

            builder.show()
        }
    }

    fun showConfirmDialog(activity: Activity, title: String, message: String, confirmText: String? = null, cancelText: String? = null, callback: (Boolean) -> Unit) {
        Handler(Looper.getMainLooper()).post {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(title)
            builder.setMessage(message)

            if (confirmText != null) {
                builder.setPositiveButton(confirmText) { _, _ -> callback(true) }
            } else {
                builder.setPositiveButton(android.R.string.ok) { _, _ -> callback(true)  }
            }

            if (cancelText != null) {
                builder.setNegativeButton(cancelText) { _, _ -> callback(false)  }
            } else {
                builder.setNegativeButton(android.R.string.cancel) { _, _ -> callback(false)  }
            }

            builder.show()
        }
    }
}
