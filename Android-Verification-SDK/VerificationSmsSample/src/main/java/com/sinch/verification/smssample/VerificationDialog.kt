package com.sinch.verification.smssample

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.sinch.verification.core.VerificationInitData
import com.sinch.verification.core.internal.Verification
import com.sinch.verification.core.verification.VerificationEvent
import com.sinch.verification.core.verification.response.VerificationListener
import com.sinch.verification.sms.SmsVerificationMethod
import com.sinch.verification.sms.config.SmsVerificationConfig
import com.sinch.verification.sms.initialization.SmsInitializationListener
import com.sinch.verification.sms.initialization.SmsInitiationResponseData
import com.sinch.verification.smssample.databinding.DialogVerificationBinding
import java.util.*

class VerificationDialog : DialogFragment(), VerificationListener {

    companion object {
        private const val DATA_TAG = "data"
        fun newInstance(initData: VerificationInitData) = VerificationDialog().apply {
            arguments = Bundle().apply { putParcelable(DATA_TAG, initData) }
        }
    }

    private var _binding: DialogVerificationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val app: VerificationSampleApp get() = activity?.application as VerificationSampleApp
    private val initData by lazy {
        arguments?.get(DATA_TAG) as VerificationInitData
    }

    private val initListener = object : SmsInitializationListener {
        override fun onInitializationFailed(t: Throwable) {
            showErrorWithMessage(t.message.orEmpty())
        }

        override fun onInitiated(data: SmsInitiationResponseData) {}
    }

    private lateinit var verification: Verification

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            isCancelable = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //Normally we would implement all the logic inside View Model but for simplicity we will keep it here.
        verification = SmsVerificationMethod.Builder().config(
            SmsVerificationConfig.Builder().globalConfig(app.globalConfig)
                .number(initData.number)
                .custom(initData.custom)
                .honourEarlyReject(initData.honourEarlyReject)
                .reference(initData.reference)
                .acceptedLanguages(initData.acceptedLanguages)
                .build()
        )
            .initializationListener(initListener)
            .verificationListener(this)
            .build()
            .also { it.initiate() }

        binding.verifyButton.setOnClickListener {
            verification.verify(binding.codeInput.editText?.text.toString())
        }
        binding.quitButton.setOnClickListener {
            verification.stop()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onVerified() {
        binding.progressBar.hide()
        binding.messageText.apply {
            setTextColor(ContextCompat.getColor(app, R.color.green))
            text = getString(R.string.successfullyVerified)
            binding.quitButton.text = getString(R.string.close)
            binding.codeInput.visibility = View.GONE
            binding.verifyButton.visibility = View.GONE
        }
    }

    override fun onVerificationFailed(t: Throwable) {
        showErrorWithMessage(t.message.orEmpty())
    }

    override fun onVerificationEvent(event: VerificationEvent) {   }

    private fun showErrorWithMessage(text: String) {
        binding.progressBar.hide()
        binding.messageText.apply {
            setTextColor(ContextCompat.getColor(app, R.color.red))
            this.text =
                String.format(Locale.US, getString(R.string.verificationFailedPlaceholder), text)
        }
    }

}