package com.sinch.sinchverification

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.sinch.sinchverification.databinding.DialogVerificationBinding
import com.sinch.verification.all.BasicVerificationMethodBuilder.createVerification
import com.sinch.verification.all.CommonVerificationInitializationParameters
import com.sinch.verification.core.VerificationInitData
import com.sinch.verification.core.initiation.response.InitiationListener
import com.sinch.verification.core.initiation.response.InitiationResponseData
import com.sinch.verification.core.internal.Verification
import com.sinch.verification.core.verification.VerificationEvent
import com.sinch.verification.core.verification.response.VerificationListener
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

    private val initListener = object : InitiationListener<InitiationResponseData> {
        override fun onInitializationFailed(t: Throwable) {
            showErrorWithMessage(t.message.orEmpty())
        }

        override fun onInitiated(data: InitiationResponseData) {}
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
        AppSignatureHelper(app).appSignatures[0]
        verification = createVerification(
            commonVerificationInitializationParameters = CommonVerificationInitializationParameters(
                globalConfig = app.globalConfig,
                verificationInitData = initData,
                initiationListener = initListener,
                verificationListener = this
            )
        ).also { it.initiate() }
        binding.verifyButton.setOnClickListener {
            verification.verify(binding.codeInput.editText?.text.toString())
        }
        binding.quitButton.setOnClickListener {
            verification.stop()
            dismiss()
        }
        listOf(binding.codeInput, binding.verifyButton).forEach {
            it.visibility =
                if (initData.usedMethod.allowsManualVerification) View.VISIBLE else View.GONE
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

    override fun onVerificationEvent(event: VerificationEvent) {}

    private fun showErrorWithMessage(text: String) {
        binding.progressBar.hide()
        binding.messageText.apply {
            setTextColor(ContextCompat.getColor(app, R.color.red))
            this.text =
                String.format(Locale.US, getString(R.string.verificationFailedPlaceholder), text)
        }
    }

}