package com.app.faizanzw.utils

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.Uri
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.app.faizanzw.R
import com.app.faizanzw.database.DBBranch
import com.app.faizanzw.database.DBEmployee
import com.app.faizanzw.database.DBExpenseType
import com.app.faizanzw.database.DBTaskType
import com.app.faizanzw.ui.postLogin.fragments.model.DataItem
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.gun0912.tedpermission.provider.TedPermissionProvider.context
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.aviran.cookiebar2.CookieBar
import java.io.File
import java.io.FileOutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


object Extension {

    fun String.getDataOrNA(): String {
        return if (this.isEmpty())
            "NA"
        else
            this
    }

    fun String.formatedDateForHome(): String? {
        val dtStart = this
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        try {
            val date: Date = format.parse(dtStart)
            val newFormate = SimpleDateFormat("dd/MM/yyyy\nhh:mm a", Locale.ENGLISH)
            System.out.println(date)
            return newFormate.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }
    }

    /*fun Long.timeAgo(context: Context): String {
        return DateUtils.getRelativeDateTimeString(
            context,
            Date(this).time * 1000,
            System.currentTimeMillis(),
            DateUtils.DAY_IN_MILLIS, 0
        ).toString()
    }*/

    fun String.formatedDateForHistory(): String? {
        val dtStart = this
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        try {
            val date: Date = format.parse(dtStart)
            val newFormate = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH)
            System.out.println(date)
            return newFormate.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }
    }


    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS
    fun Long.getTimeAgo(ctx: Context?): String? {
        var time = this
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000
        }
        val now: Long = System.currentTimeMillis()
        if (time > now || time <= 0) {
            return null
        }

        val diff = now - time
        return if (diff < MINUTE_MILLIS) {
            "just now"
        } else if (diff < 2 * MINUTE_MILLIS) {
            "a minute ago"
        } else if (diff < 50 * MINUTE_MILLIS) {
            "${diff / MINUTE_MILLIS} minutes ago"
        } else if (diff < 90 * MINUTE_MILLIS) {
            "an hour ago"
        } else if (diff < 24 * HOUR_MILLIS) {
            "${diff / HOUR_MILLIS} hours ago"
        } else if (diff < 48 * HOUR_MILLIS) {
            "yesterday"
        } else {
            "${diff / DAY_MILLIS} days ago"
        }
    }


    var EmojiExcludeFilter =
        InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                val type = Character.getType(source[i])
                if (type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt()) {
                    return@InputFilter ""
                }
            }
            null
        }

    var spaceFilter =
        InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (Character.isWhitespace(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }

    fun EditText.setFilters(count: Int) {
        this.filters = arrayOf<InputFilter>(
            spaceFilter,
            EmojiExcludeFilter,
            InputFilter.LengthFilter(count)
        )
    }

    fun EditText.setFiltersWithoutSpace(count: Int) {
        this.filters = arrayOf<InputFilter>(
            EmojiExcludeFilter,
            InputFilter.LengthFilter(count)
        )
    }

    fun CharSequence.isEmailValid(): Boolean =
        !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    fun CharSequence.isPhoneNumberValid(): Boolean =
        !TextUtils.isEmpty(this) && Pattern.compile("^(\\+44\\s?7\\d{3}|\\(?07\\d{3}\\)?)\\s?\\d{3}\\s?\\d{3}\$")
            .matcher(this).matches()

    fun CharSequence.isPasswordValid(): Boolean =
        !TextUtils.isEmpty(this) && this.length >= 8

    fun Int.asColor(context: Context) = ContextCompat.getColor(context, this)
    fun Int.asDrawable(context: Context) = ContextCompat.getDrawable(context, this)

    /*fun JsonElement.getMessage(): String {
        return this.asJsonObject.get(Constants.MESSAGE).asString
    }

    fun JsonElement.getStatus(): Int {
        return this.asJsonObject.get(Constants.STATUS).asInt
    }*/

    fun Uri.getDuration(context: Context): Long {
        val retriever = MediaMetadataRetriever();
//use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(context, this)
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if (time != null)
            return time.toLong()
        retriever.release()
        return 0
    }

    fun FragmentActivity.makeDelayInEnable(view: View) {
        this.lifecycleScope.launch {
            view.isEnabled = false
            delay(1000)
            view.isEnabled = true
        }
    }

    fun Activity.setStatusBarColor() {
        val window: Window = this.getWindow()
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setStatusBarColor(R.color.color_primary.asColor(this))
    }

    fun Activity.errorSnackBar(
        message: String
    ) {
        CookieBar.build(this)
            .setMessage(message)
            .setBackgroundColor(R.color.light_red)
            .setMessageColor(R.color.white)
            .show()
    }

    fun Activity.successSnackBar(
        message: String
    ) {
        CookieBar.build(this)
            .setMessage(message)
            .setBackgroundColor(R.color.color_primary)
            .setMessageColor(R.color.white)
            .show()
    }

    fun Activity.setFullScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    fun Context.isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

    inline fun View.debouncedOnClick(
        debounceTill: Long = 1200,
        crossinline onClick: (v: View) -> Unit
    ) {
        this.setOnClickListener(object : DebouncedOnClickListener(debounceTill) {
            override fun onDebouncedClick(v: View) {
                onClick(v)
            }
        })
    }

    fun <T> Activity.cleaereTopredirectToActivity(activity: Class<T>) {
        this.finish()
        val intent = Intent(this, activity)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    fun JsonElement.getMessage(): String {
        return this.asJsonObject.get(Constants.MESSAGE).asString
    }

    fun JsonElement.getDataMessage(): JsonObject {
        return this.asJsonObject.get(Constants.DATA).asJsonObject
    }

    fun JsonElement.getDataArray(): JsonArray {
        return this.asJsonObject.get(Constants.DATA).asJsonArray
    }

    fun JsonElement.getStatus(): Boolean {
        return this.asJsonObject.get(Constants.SUCCESS).asBoolean
    }

    fun JsonElement.getStatusCode(): Int {
        return this.asJsonObject.get("status").asInt
    }

    fun String.getRequestBody(): RequestBody {
        return RequestBody.create("multipart/form-data".toMediaTypeOrNull(), this)
    }

    fun String.getImageBody(key: String): MultipartBody.Part? {
        var fileBody: MultipartBody.Part? = null
        if (this != null && !this.isEmpty()) {
            val file = File(this)
            Log.e("path : ", "${file.absolutePath} and ${file.extension}")
            val requestFile: RequestBody =
                RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
            fileBody = MultipartBody.Part.createFormData(
                key,
                file.name /*+ "." + file.extension*/,
                requestFile
            )
        }
        return fileBody
    }

    fun Context.currentDateInString(): String {
        val c = Calendar.getInstance().time
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        return simpleDateFormat.format(c)
    }

    fun Activity.getFromDate(
        fromDate: Boolean,
        callBack: (value: Boolean, date: String) -> Unit
    ) {
        val c = Calendar.getInstance()
        var mYear = c.get(Calendar.YEAR)
        var mMonth = c.get(Calendar.MONTH)
        var mDay = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                var strDay = dayOfMonth.toString()
                var strMonth = (monthOfYear + 1).toString()

                if (dayOfMonth <= 9) strDay = "0" + dayOfMonth

                if ((monthOfYear + 1) <= 9) strMonth = "0" + (monthOfYear + 1)


                /*if (fromDate) {
                    binding.edtStartDate.setTitle(strDay + "/" + strMonth + "/" + year)
                } else {
                    binding.edtEndDate.setTitle(strDay + "/" + strMonth + "/" + year)
                }*/

                callBack(fromDate, strDay + "/" + strMonth + "/" + year)

                //startdate = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year;
            },
            mYear,
            mMonth,
            mDay
        )
        datePickerDialog.show()
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        /* if (fromDate)
         {

         }*/
    }

    fun String.convertTodate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val date = sdf.parse(this)
        val another = SimpleDateFormat("dd/MM/yyyy")
        return another.format(date)
    }

    fun showLogoutPopUp(
        context: Activity?,
        callFunction: () -> Unit
    ) {
        try {
            val mBuilder = AlertDialog.Builder(
                context!!, R.style.dialogtheme2
            )

            mBuilder.setTitle("Sign Out")
            mBuilder.setMessage("Are you sure want to Sign Out?")
            mBuilder.setPositiveButton(
                "YES"
            ) { dialog, which ->
                dialog.dismiss()
                callFunction()
            }
            mBuilder.setNegativeButton(
                "NO"
            ) { dialog, which -> dialog.dismiss() }
            val mDialog = mBuilder.create()
            mDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun ArrayList<DataItem>.findPosition(term: String): Int {
        if (term.isNotEmpty()) {
            val position = this.indexOfFirst { item ->
                item.comboID.equals(term)
            }
            if (position > 0)
                return position
            return 0
        }
        return 0
    }

    fun ArrayList<DBTaskType>.findTaskTypePosition(term: String): Int {
        if (term.isNotEmpty()) {
            val position = this.indexOfFirst { item ->
                item.comboID.equals(term.toInt())
            }
            if (position > 0)
                return position
            return 0
        }
        return 0
    }

    fun ArrayList<DBExpenseType>.findExpenseType(term: String): Int {
        if (term.isNotEmpty()) {
            val position = this.indexOfFirst { item ->
                item.comboID.equals(term.toInt())
            }
            if (position > 0)
                return position
            return 0
        }
        return 0
    }

    fun ArrayList<DBBranch>.findBranch(term: String): Int {
        if (term.isNotEmpty()) {
            val position = this.indexOfFirst { item ->
                item.comboID.equals(term.toInt())
            }
            if (position > 0)
                return position
            return 0
        }
        return 0
    }

    fun ArrayList<DBEmployee>.findEmployee(term: String): Int {
        if (term.isNotEmpty()) {
            val position = this.indexOfFirst { item ->
                item.userMasterKey.equals(term.toInt())
            }
            if (position > 0)
                return position
            return 0
        }
        return 0
    }

    fun ArrayList<DataItem>.findValue(term: String): String {
        if (term.isNotEmpty()) {
            val position = this.find {
                it.comboID.equals(term)
            }
            if (position != null)
                return position.comboValue
            return ""
        }
        return ""
    }

    fun ArrayList<DBBranch>.findBranchValue(term: String): String {
        if (term.isNotEmpty()) {
            val position = this.find {
                it.comboID.equals(term.toInt())
            }
            if (position != null)
                return position.comboValue!!
            return ""
        }
        return ""
    }

    fun ArrayList<String>.findStringListPosition(term: String): Int {
        if (term.isNotEmpty()) {
            val position = this.indexOfFirst { item ->
                item.equals(term)
            }
            if (position > 0)
                return position
            return 0
        }
        return 0
    }

    @Throws(RuntimeException::class)
    fun saveFile(context: Activity, data: ByteArray?, fileName: String?): String {
        val destinyDir = context.cacheDir
        if (!destinyDir.exists() && !destinyDir.mkdirs()) {
            return ""
        }
        val mainPicture: File? = File(destinyDir, fileName)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mainPicture)
            fos.write(data)
            fos.close()
            if (mainPicture != null)
                return mainPicture.absolutePath
            return ""
        } catch (e: java.lang.Exception) {
            context.errorSnackBar("Error in converting byte to file")
            return ""
        }
    }

    fun EditText.setTextWatcher(view: TextInputLayout) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                view.error = ""
            }
        })
    }

    fun openImageFileInExternal(file: File) {
        val newIntent = Intent(Intent.ACTION_VIEW)
        val mimeType: String = "pdf"
        newIntent.setDataAndType(Uri.fromFile(file), mimeType)
        newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(newIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No handler for this type of file.", Toast.LENGTH_LONG).show()
        }
    }

}