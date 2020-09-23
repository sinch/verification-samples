package com.sinch.sinchverification

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.sinch.logging.logger
import com.sinch.verification.core.VerificationInitData
import com.sinch.verification.core.internal.VerificationMethodType
import com.sinch.verification.core.verification.VerificationLanguage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val logger = logger()

    private val buttonToMethodMap by lazy {
        mapOf(
            smsButton.id to VerificationMethodType.SMS,
            flashcallButton.id to VerificationMethodType.FLASHCALL,
            calloutButton.id to VerificationMethodType.CALLOUT,
            seamlessButton.id to VerificationMethodType.SEAMLESS
        )
    }

    private val initData: VerificationInitData
        get() =
            VerificationInitData(
                usedMethod = buttonToMethodMap[methodToggle.checkedButtonId]
                    ?: VerificationMethodType.SMS,
                number = phoneInput.editText?.text.toString(),
                custom = customInput.editText?.text.toString(),
                reference = referenceInput.editText?.text.toString(),
                honourEarlyReject = honoursEarlyCheckbox.isChecked,
                acceptedLanguages = acceptedLanguagesInput?.editText?.text.toString().toLocaleList()
            )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initButton.setOnClickListener {
            checkFields()
        }
        phoneInput.editText?.addTextChangedListener {
            phoneInput.error = null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    private fun checkFields() {
        if (phoneInput.editText?.text.isNullOrEmpty()) {
            phoneInput.error = getString(R.string.phoneEmptyError)
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
