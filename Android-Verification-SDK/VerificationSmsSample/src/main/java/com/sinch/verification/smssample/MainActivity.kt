package com.sinch.verification.smssample

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import com.sinch.verification.core.VerificationInitData
import com.sinch.verification.core.internal.VerificationMethodType
import com.sinch.verification.core.verification.VerificationLanguage
import com.sinch.verification.smssample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        const val PERMISSION_REQUEST_CODE = 5
    }

    private val initData: VerificationInitData
        get() =
            VerificationInitData(
                usedMethod = VerificationMethodType.SMS,
                number = binding.phoneInput.editText?.text.toString(),
                custom = binding.customInput.editText?.text.toString(),
                reference = binding.referenceInput.editText?.text.toString(),
                honourEarlyReject = binding.honoursEarlyCheckbox.isChecked,
                acceptedLanguages = binding.acceptedLanguagesInput?.editText?.text.toString().toLocaleList()
            )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.initButton.setOnClickListener {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE
                ), PERMISSION_REQUEST_CODE
            )
        }
        binding.phoneInput.editText?.addTextChangedListener {
            binding.phoneInput.error = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //We simply proceed with the verification
        checkFields()
    }

    private fun checkFields() {
        if (binding.phoneInput.editText?.text.isNullOrEmpty()) {
            binding.phoneInput.error = getString(R.string.phoneEmptyError)
        } else {
            VerificationDialog.newInstance(initData)
                .apply {
                    show(supportFragmentManager, "dialog")
                }
        }
    }

    private fun String.toLocaleList() = split(",")
        .filter { it.contains("-") }
        .map { it.split("-") }
        .map { VerificationLanguage(it[0], it[1]) }

}
