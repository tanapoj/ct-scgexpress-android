package com.scgexpress.backoffice.android.ui.delivery.ofd.cantsent

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.integration.android.IntentIntegrator
import com.scgexpress.backoffice.android.R
import com.scgexpress.backoffice.android.common.Const.PARAMS_TRACKING_ID
import com.scgexpress.backoffice.android.common.listener.DrawableClickListener
import com.scgexpress.backoffice.android.common.showWarningDialog
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.fragment_ofd_cant_sent.*
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 *
 */
class OfdCantSentFragment : Fragment(), HasSupportFragmentInjector {

    companion object {
        private lateinit var viewModel: OfdCantSentViewModel

        fun newInstance(vm: OfdCantSentViewModel, trackingID: String): OfdCantSentFragment {
            viewModel = vm
            return OfdCantSentFragment().also {
                val args = Bundle()
                args.putString(PARAMS_TRACKING_ID, trackingID)
                it.arguments = args
            }
        }
    }

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    private lateinit var rootView: View


    private val adapter: OfdCantSentAdapter by lazy {
        OfdCantSentAdapter(viewModel)
    }

    private var manifestID = ""

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentInjector
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)

        if (context is OfdCantSentActivity) {
        } else {
            throw IllegalStateException("This fragment must be use in conjunction with " + OfdCantSentActivity::class.java.simpleName)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_ofd_cant_sent, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initRecyclerView()
        initButton()
        observeData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)?.let { it ->
            it.contents?.let {
                edtScanTracking.setText(it)
                checkExistTracking(it)
            }
        }
    }

    private fun initRecyclerView() {
        rvItems.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvItems.adapter = adapter

        edtRetentionReason.requestFocus()
    }

    private fun observeData() {
        viewModel.data.observe(this, Observer {
            if (it == null) return@Observer
            txtRegistered.text = (it.size - 1).toString()
            adapter.data = it
            edtScanTracking.text.clear()
            edtScanTracking.requestFocus()
        })

        viewModel.retentionReason.observe(this, Observer {
            if (it == null) return@Observer
            edtRetentionReason.setText("${it.name}")
            if (viewModel.isOther)
                edtNote.requestFocus()
            else edtScanTracking.requestFocus()
        })

        viewModel.manifestID.observe(this, Observer { it ->
            if (it != null) {
                manifestID = it
            }
        })

        viewModel.snackbar.observe(this, Observer { it ->
            if (it != null) {
                it.getContentIfNotHandled()?.let {
                    // Only proceed if the event has never been handled
                    Snackbar.make(rootView, it, Snackbar.LENGTH_SHORT).show()
                }
            }
        })

        viewModel.warning.observe(this, Observer { it ->
            if (it != null) {
                it.getContentIfNotHandled()?.let {
                    // Only proceed if the event has never been handled
                    activity!!.showWarningDialog(it)
                }
            }
        })

        viewModel.finish.observe(this, Observer { it ->
            if (it != null) {
                it.getContentIfNotHandled()?.let {
                    // Only proceed if the event has never been handled
                    if (it) {
                        activity!!.onBackPressed()
                    }
                }
            }
        })

        viewModel.trackingId.observe(this, Observer {
            edtScanTracking.setText(it)
        })

        viewModel.getLocationHelper(context!!).observe(this, Observer<Location> { location ->
            if (location == null) return@Observer
            viewModel.latitude = location.latitude
            viewModel.longitude = location.longitude
        })
    }

    private fun initButton() {
        edtRetentionReason.keyListener = null
        edtRetentionReason.setOnClickListener {
            viewModel.showDialogRetention(edtScanTracking.context)
        }

        edtScanTracking.setOnFocusChangeListener { _, hasFocus ->
            run {
                if (hasFocus) checkReason()
            }
        }

        edtScanTracking.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                checkExistTracking(edtScanTracking.text.toString())
                return@OnKeyListener true
            }
            false
        })

        edtScanTracking.setOnTouchListener(object : DrawableClickListener.RightDrawableClickListener(edtScanTracking) {
            override fun onDrawableClick(): Boolean {
                return if (checkReason()) {
                    IntentIntegrator(activity).run {
                        initiateScan()
                    }
                    true
                } else false
            }
        })

        btnConfirm.setOnClickListener {
            if (adapter.data.size > 1) {
                viewModel.confirmOfdRetention()
            } else
                viewModel.showWarning(getString(R.string.sentence_please_scan_your_tracking))
        }
    }

    private fun checkExistTracking(trackingId: String) {
        if (viewModel.checkExistTracking(trackingId)) {
            viewModel.showWarning(getString(R.string.sentence_this_tracking_has_been_scanned))
            edtScanTracking.setText("")
            edtScanTracking.requestFocus()
        } else {
            viewModel.scanOfdRetention(trackingId, edtNote.text.toString())
        }
    }

    private fun checkReason(): Boolean {
        if (viewModel.retentionReason.value!!.id == "") {
            viewModel.showWarning(getString(R.string.sentence_retention_please_insert_the_reason))
            edtRetentionReason.requestFocus()
            return false
        }

        if (viewModel.isOther && edtNote.text.toString() == "") {
            viewModel.showWarning(getString(R.string.sentence_retention_please_insert_note))
            edtNote.requestFocus()
            return false
        }
        return true
    }
}

