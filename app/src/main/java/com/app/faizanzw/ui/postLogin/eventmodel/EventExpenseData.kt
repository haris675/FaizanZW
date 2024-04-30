package com.app.faizanzw.ui.postLogin.eventmodel

import android.icu.text.Transliterator.Position
import com.app.faizanzw.ui.postLogin.fragments.search.ExpenseSearch

data class EventExpenseData(
    val position: Int,
    val data: ExpenseSearch
)
