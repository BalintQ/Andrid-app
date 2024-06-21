import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var editText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.editText)

        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available on this device", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Create a PendingIntent for NFC events
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
    }

    override fun onResume() {
        super.onResume()

        // Enable NFC foreground dispatch to handle NFC events
        val intentFilters = arrayOf<IntentFilter>()
        val techList = arrayOf<Array<String>>()

        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFilters, techList)
    }

    override fun onPause() {
        super.onPause()

        // Disable NFC foreground dispatch
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Handle NFC intent
        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            processNfcIntent(intent)
        }
    }

    private fun processNfcIntent(intent: Intent) {
        val rawMessages: Array<Parcelable> = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            ?: return

        val messages = mutableListOf<NdefMessage>()

        for (rawMessage in rawMessages) {
            (rawMessage as NdefMessage).let {
                messages.add(it)
            }
        }

        if (messages.isEmpty()) {
            return
        }

        val record = messages[0].records[0]
        val payload = record.payload
        val payloadText = String(payload)

        // Display NFC content in EditText
        editText?.append(payloadText)

        // Check for newline character and simulate enter key press
        if (payloadText.endsWith("/n")) {
            editText?.append("\n") // Automatically add newline
        }
    }
}
