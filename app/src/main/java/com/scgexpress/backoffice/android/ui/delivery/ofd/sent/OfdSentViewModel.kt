package com.scgexpress.backoffice.android.ui.delivery.ofd.sent

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.os.SystemClock
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import com.scgexpress.backoffice.android.R
import com.scgexpress.backoffice.android.api.exception.NoConnectivityException
import com.scgexpress.backoffice.android.common.Event
import com.scgexpress.backoffice.android.common.LocationHelper
import com.scgexpress.backoffice.android.common.Utils
import com.scgexpress.backoffice.android.common.isTrackingId
import com.scgexpress.backoffice.android.model.*
import com.scgexpress.backoffice.android.preference.LoginPreference
import com.scgexpress.backoffice.android.repository.delivery.DeliveryLocalRepository
import com.scgexpress.backoffice.android.repository.delivery.DeliveryNetworkRepository
import com.scgexpress.backoffice.android.ui.delivery.ofd.cantsent.OfdCantSentViewModel.Companion.OFD_RETENTION_STATUS_ID
import com.scgexpress.backoffice.android.viewmodel.RxAndroidViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class OfdSentViewModel @Inject constructor(
    application: Application,
    private val repoNetwork: DeliveryNetworkRepository,
    private val repoLocal: DeliveryLocalRepository,
    private val loginPreference: LoginPreference
) : RxAndroidViewModel(application) {

    companion object {
        const val OFD_SENT_STATUS_CODE: String = "34"
        const val BUTTON_CLICKED_DELAY: Long = 1000
    }

    private val context: Context
        get() = getApplication()

    private val user: User
        get() = Utils.convertStringToUser(loginPreference.loginUser!!)

    private var _trackingId: MutableLiveData<String> = MutableLiveData()
    val trackingId: LiveData<String>
        get() = _trackingId

    private var _data: MutableLiveData<List<Any>> = MutableLiveData()
    val data: LiveData<List<Any>>
        get() = _data

    private var _dataNetwork: MutableLiveData<List<DeliveryOfdParcelResponse>> = MutableLiveData()
    val dataNetwork: LiveData<List<DeliveryOfdParcelResponse>>
        get() = _dataNetwork

    private var _scanData: MutableLiveData<List<Delivery>> = MutableLiveData()
    val scanData: LiveData<List<Delivery>>
        get() = _scanData

    private var _scanDataList: MutableLiveData<DeliveryOfdParcelList> = MutableLiveData()
    val scanDataList: LiveData<DeliveryOfdParcelList>
        get() = _scanDataList

    private var _manifestID: MutableLiveData<String> = MutableLiveData()
    val manifestID: LiveData<String>
        get() = _manifestID

    private var _codAmount: MutableLiveData<Double> = MutableLiveData()
    val codAmount: LiveData<Double>
        get() = _codAmount

    private val _snackbar = MutableLiveData<Event<String>>()
    val snackbar: LiveData<Event<String>>
        get() = _snackbar

    private val _warning = MutableLiveData<Event<String>>()
    val warning: LiveData<Event<String>>
        get() = _warning

    private val _codDialog = MutableLiveData<Event<Double>>()
    val codDialog: LiveData<Event<Double>>
        get() = _codDialog

    private var newItem: Delivery = Delivery()

    private var groupID: String = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var mLastClickTime: Long = 0

    private fun prepareData() {
        getDataNetwork()
    }

    private fun getDataNetwork() {
        addDisposable(repoNetwork.getManifestItemScanned(_manifestID.value!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it != null) {
                    _dataNetwork.value = it
                    genGroupID()
                }
            }) {
                genGroupID()
            })
    }

    fun scanOfd(trackingID: String) {

        if (trackingID.isEmpty()) {
            showWarning(context.getString(R.string.sentence_please_scan_your_tracking))
            return
        }
        if (!trackingID.isTrackingId()) {
            showWarning("'$trackingID' ${context.getString(R.string.sentence_is_not_tracking_ID_format)}")
            _trackingId.value = ""
            return
        }

        if (!checkAvailableTracking(trackingID)) {
            showWarning(context.getString(R.string.sentence_delivery_tracking_not_found))
            _trackingId.value = ""
            return
        }

        addDisposable(repoNetwork.getTracking(trackingID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it != null) {
                    updateData(it, false)
                } else {
                    showWarning(context.getString(R.string.sentence_scan_ofd_parcels_failed))
                }
                _trackingId.value = ""
            }) {
                if (it is NoConnectivityException) {
                    updateDataNoInternet(trackingID, false)
                    showSnackbar(context.getString(R.string.there_is_on_internet_connection))
                }
            })
    }

    private fun checkAvailableTracking(trackingID: String): Boolean {
        if (dataNetwork.value == null) return false
        for (item: DeliveryOfdParcelResponse in dataNetwork.value!!) {
            if (trackingID == item.trackingId && item.status == "7") {
                return true
            }
        }
        return false
    }

    private fun deleteOfd(item: Delivery) {
        item.deleted = true
        repoLocal.updateDelivery(item)
    }

    fun confirmOfdSent() {
        if (!checkLastClickTime()) return
        val deliveryParcel: ArrayList<DeliveryOfdParcel> = arrayListOf()
        for (data: Delivery in _scanData.value!!) {
            deliveryParcel.add(
                DeliveryOfdParcel(
                    trackingId = data.trackingNumber!!,
                    statusCode = data.statusID!!,
                    codCollected = data.codCollected!!,
                    datetimeInput = Utils.getServerDateTimeFormat(Utils.getCurrentTimestamp()),
                    recipientName = "",
                    latitude = latitude,
                    longitude = longitude
                )
            )
        }
        _scanDataList.value = DeliveryOfdParcelList(deliveryParcel)
    }

    private fun updateData(data: TrackingInfo, deleted: Boolean) {
        val codCollected = 0.0
        var codAmount = 0.0
        try {
            codAmount = data.codAmount.toDouble()
        } catch (ex: Exception) {
        }
        val item = Delivery(
            groupID, user.id, _manifestID.value!!,
            data.trackingNumber,
            data.senderCode, data.senderName, OFD_SENT_STATUS_CODE,
            codAmount, codCollected, data.dateCreated, deleted,
            true, Utils.getCurrentTimestamp()
        )
        newItem = item
        repoLocal.updateDelivery(item)

        if (data.trackingNumber.startsWith("11"))
            showCodDialog(codAmount)
    }

    private fun updateDataNoInternet(trackingID: String, deleted: Boolean) {
        repoLocal.updateDelivery(
            Delivery(
                groupID = groupID, userId = user.id, manifestID = _manifestID.value!!,
                trackingNumber = trackingID, statusID = OFD_SENT_STATUS_CODE, deleted = deleted,
                sync = false, timestamp = Utils.getCurrentTimestamp()
            )
        )
    }

    private fun updateCodCollected(item: Delivery, codCollected: Double) {
        item.codCollected = codCollected
        repoLocal.updateDelivery(item)
    }

    fun showSnackbar(msg: String) {
        _snackbar.value = Event(msg)
    }

    fun showWarning(msg: String) {
        _warning.value = Event(msg)
    }

    private fun showCodDialog(cod: Double) {
        _codDialog.value = Event(cod)
    }

    fun setManifestID(manifestID: String) {
        _manifestID.value = manifestID
        prepareData()
    }

    private fun getData() {
        addDisposable(repoLocal.getDelivery(user.id, _manifestID.value!!, groupID, OFD_RETENTION_STATUS_ID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it != null) {
                    _scanData.value = it
                    setCodAmount(it)
                    val d: ArrayList<Any> = arrayListOf(DeliveryOfdScanTitle("0"))
                    d.addAll(it)
                    _data.value = d
                }
            }) {
            })
    }

    private fun genGroupID(): String {
        if (groupID == "") {
            addDisposable(repoLocal.lastGroupID
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    groupID = if (it != null || it != "") {
                        (it.toInt() + 1).toString()
                    } else "1"
                    getData()
                }) {
                    groupID = "1"
                    getData()
                })
        }
        return groupID
    }

    private fun setCodAmount(data: List<Delivery>) {
        var codAmount = 0.0
        for (item: Delivery in data) {
            codAmount += item.codAmount!!
        }
        _codAmount.value = codAmount
    }

    fun checkExistTracking(trackingCode: String): Boolean {
        return scanData.value!!.any { l -> l.trackingNumber == trackingCode }
    }

    fun showDialogConfirmDeleteTracking(context: Context, item: Delivery) {
        val mBuilder = AlertDialog.Builder(context)
        mBuilder.setTitle(context.getString(R.string.delete_tracking))
        mBuilder.setMessage(context.getString(R.string.sentence_are_you_sure_you_want_to_delete_this_tracking))
        mBuilder.setPositiveButton(context.getString(R.string.yes)) { _, _ ->
            deleteOfd(item)
        }
        mBuilder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }

        val mDialog = mBuilder.create()
        mDialog.show()
    }

    fun showDialogCod(context: Context, codAmount: Double) {
        val mBuilder = AlertDialog.Builder(context)

        val input = EditText(context)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setSingleLine(true)
        mBuilder.setView(input)

        mBuilder.setTitle(context.getString(R.string.sentence_please_insert_cod_fee))
        mBuilder.setPositiveButton(context.getString(R.string.ok), null)
        mBuilder.setNegativeButton(context.getString(R.string.cancel), null)

        val mDialog = mBuilder.create()
        mDialog.setOnShowListener {
            val btnPos = mDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnPos.setOnClickListener {
                if (input.text.toString().trim() != "") {
                    if (input.text.toString().toDouble() <= codAmount) {
                        updateCodCollected(newItem, input.text.toString().toDouble())
                        mDialog.dismiss()
                    } else
                        Snackbar.make(
                            input, context.getString(R.string.sentence_please_insert_cod_fee_correctly),
                            Snackbar.LENGTH_SHORT
                        ).show()
                }
            }

            val btnNeg = mDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            btnNeg.setOnClickListener {
                deleteOfd(newItem)
                mDialog.dismiss()
            }
        }
        mDialog.setCancelable(false)
        mDialog.show()
    }

    fun getLocationHelper(mContext: Context): LocationHelper {
        return LocationHelper.getInstance(mContext)
    }

    private fun checkLastClickTime(): Boolean {
        if (SystemClock.elapsedRealtime() - mLastClickTime < BUTTON_CLICKED_DELAY) {
            return false
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return true
    }
}