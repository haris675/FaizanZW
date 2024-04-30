package com.app.faizanzw.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import com.app.faizanzw.R
import com.app.faizanzw.databinding.DialogExpenseDetailBinding
import com.app.faizanzw.ui.postLogin.fragments.search.ExpenseSearch
import com.app.faizanzw.utils.Extension.convertTodate

object DialogUtils {

    fun showDetailDialog(
        context: Context,
        model: ExpenseSearch,
        selectedPosition: Int,
        onComplete: (position: Int) -> Unit, onInprogress: (position: Int) -> Unit
    ) {
        val dialog1 = Dialog(context, R.style.dialogtheme2)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog1.show()
        val binding: DialogExpenseDetailBinding = DialogExpenseDetailBinding.inflate(
            LayoutInflater.from(context),
            null,
            false
        )

        dialog1.setContentView(binding.root)
        dialog1.setCancelable(true)

        binding.txtTaskNo.text = model.tranNo
        binding.txtDate.text = model.trandate.convertTodate()


        binding.model = model

        binding.btnComplete.setOnClickListener {
            onComplete(selectedPosition)
            dialog1.dismiss()
        }
        binding.btnInProgress.setOnClickListener {
            onComplete(selectedPosition)
            dialog1.dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dialog1.dismiss()
        }
    }

}