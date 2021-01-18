import com.sinch.verification.Verification
import com.sinch.verification.model.VerificationMethodType
import com.sinch.verification.model.initiation.InitiationResponseData
import com.sinch.verification.network.auth.AppKeyAuthorizationMethod
import com.sinch.verification.process.config.VerificationMethodConfig
import com.sinch.verification.process.listener.InitiationListener
import com.sinch.verification.process.listener.VerificationListener
import com.sinch.verification.process.method.VerificationMethod

fun main(args: Array<String>) {
    Sample.runSample()
}

object Sample : VerificationListener, InitiationListener {

    private const val APP_KEY = "***"
    private var verification: Verification? = null

    fun runSample() {
        val phoneNumber = inputWithMessage("Enter number that needs to be verified")
        val method = inputVerificationMethod(asAutoSubmethodOptions = false)
        val verificationConfig = VerificationMethodConfig.Builder.instance
            .authorizationMethod(AppKeyAuthorizationMethod(appKey = APP_KEY))
            .verificationMethod(method)
            .number(phoneNumber)
            .build()

        verification = VerificationMethod.Builder.instance
            .verificationConfig(verificationConfig)
            .verificationListener(this)
            .initiationListener(this)
            .build()

        verification?.initiate()
    }

    override fun onVerified() {
        println("Successfully verified phone number!")
    }

    override fun onVerificationFailed(t: Throwable) {
        println("Error while verifying the code: ${t.localizedMessage}")
    }

    override fun onInitiated(data: InitiationResponseData) {
        if (data.method == VerificationMethodType.AUTO) {
            verifyCodeWithSpecificMethod()
        } else {
            verification?.verify(inputWithMessage("Enter received code"))
        }
    }

    override fun onInitializationFailed(t: Throwable) {
        println("Error while initiation verification: ${t.localizedMessage}")
    }

    private fun verifyCodeWithSpecificMethod() {
        val code = inputWithMessage("Enter received code")
        val method = inputVerificationMethod(asAutoSubmethodOptions = true)
        verification?.verify(code, method)
    }

    private fun inputWithMessage(msg: String): String {
        println(msg)
        return readLine().orEmpty()
    }

    private fun inputVerificationMethod(asAutoSubmethodOptions: Boolean): VerificationMethodType {
        var userMessage = "Enter verification method\n" +
                "1 - SMS\n" +
                "2 - Flashcall\n" +
                "3 - Callout\n"
        if (!asAutoSubmethodOptions) {
            userMessage += "4 - Seamless\n" +
                    "5 - Auto\n"
        }
        return when (inputWithMessage(userMessage)) {
            "1" -> VerificationMethodType.SMS
            "2" -> VerificationMethodType.FLASHCALL
            "3" -> VerificationMethodType.CALLOUT
            "4" -> VerificationMethodType.SEAMLESS
            "5" -> VerificationMethodType.AUTO
            else -> print("Method not known using SMS")
                .run { VerificationMethodType.SMS }
        }
    }

}