package com.sinch.sinchverification

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import com.sinch.sinchverification.databinding.ActivityMainBinding
import com.sinch.verification.core.VerificationInitData
import com.sinch.verification.core.internal.VerificationMethodType
import com.sinch.verification.core.verification.VerificationLanguage

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        const val PERMISSION_REQUEST_CODE = 5
    }

    private val buttonToMethodMap by lazy {
        mapOf(
            binding.smsButton.id to VerificationMethodType.SMS,
            binding.flashcallButton.id to VerificationMethodType.FLASHCALL,
            binding.calloutButton.id to VerificationMethodType.CALLOUT,
            binding.seamlessButton.id to VerificationMethodType.SEAMLESS
        )
    }

    private val initData: VerificationInitData
        get() =
            VerificationInitData(
                usedMethod = selectedVerificationMethod,
                number = binding.phoneInput.editText?.text.toString(),
                custom = binding.customInput.editText?.text.toString(),
                reference = binding.referenceInput.editText?.text.toString(),
                honourEarlyReject = binding.honoursEarlyCheckbox.isChecked,
                acceptedLanguages = binding.acceptedLanguagesInput?.editText?.text.toString()
                    .toLocaleList()
            )

    private val selectedVerificationMethod: VerificationMethodType
        get() = buttonToMethodMap[binding.methodToggle.checkedButtonId]
            ?: VerificationMethodType.SMS

    private val requestedPermissions: Array<String>
        get() {
            val optionalPerms = arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE
            )
            return if (selectedVerificationMethod == VerificationMethodType.FLASHCALL) {
                optionalPerms + Manifest.permission.READ_CALL_LOG
            } else {
                optionalPerms
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.initButton.setOnClickListener {
            ActivityCompat.requestPermissions(this, requestedPermissions, PERMISSION_REQUEST_CODE)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
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
