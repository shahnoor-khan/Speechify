package com.example.stt


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.preference.PreferenceManager

import android.text.Html
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.stt.asr.CloudSpeechSessionParams
import com.example.stt.asr.CloudSpeechStreamObserverParams
import com.example.stt.asr.RepeatingRecognitionSession
import com.example.stt.asr.SafeTranscriptionResultFormatter
import com.example.stt.asr.SpeechRecognitionModelOptions
import com.example.stt.asr.SpeechRecognitionModelOptions.SpecificModel.DICTATION_DEFAULT
import com.example.stt.asr.SpeechRecognitionModelOptions.SpecificModel.VIDEO
import com.example.stt.asr.TranscriptionResultFormatterOptions
import com.example.stt.asr.TranscriptionResultFormatterOptions.TranscriptColoringStyle.NO_COLORING
import com.example.stt.asr.TranscriptionResultUpdatePublisher
import com.example.stt.asr.TranscriptionResultUpdatePublisher.ResultSource
import com.example.stt.asr.cloud.CloudSpeechSessionFactory


class MainActivity : AppCompatActivity() {
    private var currentLanguageCodePosition = 0
    private var currentLanguageCode: String? = null
    private var audioRecord: AudioRecord? = null
    private val buffer = ByteArray(BYTES_PER_SAMPLE * CHUNK_SIZE_SAMPLES)

    // This class was intended to be used from a thread where timing is not critical (i.e. do not
    // call this in a system audio callback). Network calls will be made during all of the functions
    // that RepeatingRecognitionSession inherits from SampleProcessorInterface.
    private var recognizer: RepeatingRecognitionSession? = null
    private var networkChecker: NetworkConnectionChecker? = null
    private var transcript: TextView? = null
    private val transcriptUpdater: TranscriptionResultUpdatePublisher =
        TranscriptionResultUpdatePublisher { formattedTranscript, _ ->
            runOnUiThread {
                transcript?.text = formattedTranscript.toString()
            }
        }
    private val readMicData = Runnable {
        if (audioRecord!!.state != AudioRecord.STATE_INITIALIZED) {
            return@Runnable
        }
        recognizer?.init(CHUNK_SIZE_SAMPLES)
        while (audioRecord!!.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord!!.read(
                buffer,
                0,
                CHUNK_SIZE_SAMPLES * BYTES_PER_SAMPLE
            )
            recognizer!!.processAudioBytes(buffer)
        }
        recognizer?.stop()
    }

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        transcript = findViewById(R.id.transcript)
        initLanguageLocale()
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)  !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        } else {
            showAPIKeyDialog()
        }
    }

    override fun onStop() {
        super.onStop()
        if (audioRecord != null) {
            audioRecord!!.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (recognizer != null) {
            recognizer!!.unregisterCallback(transcriptUpdater)
            networkChecker!!.unregisterNetworkCallback()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_RECORD_AUDIO -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showAPIKeyDialog()
                } else {
                    // This should nag user again if they launch without the permissions.
                    Toast.makeText(
                        this,
                        "This app does not work without the Microphone permission.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    finish()
                }
                return
            }

            else -> {}
        }
    }

    private fun initLanguageLocale() {
        // The default locale is en-US.
        currentLanguageCode = "en-US"
        currentLanguageCodePosition = 22
    }

    private fun constructRepeatingRecognitionSession() {
        val options: SpeechRecognitionModelOptions = SpeechRecognitionModelOptions.newBuilder()
            .setLocale(currentLanguageCode) // As of 7/18/19, Cloud Speech's video model supports en-US only.
            .setModel(if (currentLanguageCode == "en-US") VIDEO else DICTATION_DEFAULT)
            .build()
        val cloudParams: CloudSpeechSessionParams = CloudSpeechSessionParams.newBuilder()
            .setObserverParams(
                CloudSpeechStreamObserverParams.newBuilder().setRejectUnstableHypotheses(false)
            )
            .setFilterProfanity(true)
            .setEncoderParams(
                CloudSpeechSessionParams.EncoderParams.newBuilder()
                    .setEnableEncoder(true)
            ).build()
//        networkChecker = NetworkConnectionChecker(this)
//        networkChecker!!.registerNetworkCallback()

        // There are lots of options for formatting the text. These can be useful for debugging
        // and visualization, but it increases the effort of reading the transcripts.
        val formatterOptions: TranscriptionResultFormatterOptions =
            TranscriptionResultFormatterOptions.newBuilder()
                .setTranscriptColoringStyle(NO_COLORING)
                .build()
        val recognizerBuilder: RepeatingRecognitionSession.Builder =
            RepeatingRecognitionSession.newBuilder()
                .setSpeechSessionFactory(CloudSpeechSessionFactory(cloudParams, getApiKey(this)))
                .setSampleRateHz(SAMPLE_RATE)
                .setTranscriptionResultFormatter(SafeTranscriptionResultFormatter(formatterOptions))
                .setSpeechRecognitionModelOptions(options)

        recognizer = recognizerBuilder.build()
        recognizer?.registerCallback(transcriptUpdater, ResultSource.WHOLE_RESULT)
    }

    @SuppressLint("MissingPermission")
    private fun startRecording() {
        if (audioRecord == null) {
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.RECORD_AUDIO
//                ) !== PackageManager.PERMISSION_GRANTED
//            ) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return
//            }
            audioRecord = AudioRecord(
                MIC_SOURCE,
                SAMPLE_RATE,
                MIC_CHANNELS,
                MIC_CHANNEL_ENCODING,
                CHUNK_SIZE_SAMPLES * BYTES_PER_SAMPLE
            )
        }
        audioRecord!!.startRecording()
        Thread(readMicData).start()
    }

    /** The API won't work without a valid API key. This prompts the user to enter one.  */
    private fun showAPIKeyDialog() {
        val contentLayout =
            getLayoutInflater().inflate(R.layout.api_key_message, null) as LinearLayout
        val linkView = contentLayout.findViewById<TextView>(R.id.api_key_link_view)
        linkView.text = Html.fromHtml(getString(R.string.api_key_doc_link))
        linkView.movementMethod = LinkMovementMethod.getInstance()
        val keyInput = contentLayout.findViewById<EditText>(R.id.api_key_input)
        keyInput.inputType = InputType.TYPE_CLASS_TEXT
        keyInput.setText(getApiKey(this))
        val selectLanguageView = contentLayout.findViewById<TextView>(R.id.language_locale_view)
        selectLanguageView.text = Html.fromHtml(getString(R.string.select_language_message))
        selectLanguageView.movementMethod = LinkMovementMethod.getInstance()
        val languagesList: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            getResources().getStringArray(R.array.languages)
        )
        val sp = contentLayout.findViewById<Spinner>(R.id.language_locale_spinner)
        sp.adapter = languagesList
        sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                handleLanguageChanged(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        sp.setSelection(currentLanguageCodePosition)
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle(getString(R.string.api_key_message))
            .setView(contentLayout)
            .setPositiveButton(
                getString(android.R.string.ok)
            ) { dialog, which ->
                saveApiKey(
                    this,
                    keyInput.text.toString().trim { it <= ' ' })
                constructRepeatingRecognitionSession()
                startRecording()
            }
            .show()
    }

    /** Handles selecting language by spinner.  */
    private fun handleLanguageChanged(itemPosition: Int) {
        currentLanguageCodePosition = itemPosition
        currentLanguageCode =
            getResources().getStringArray(R.array.language_locales).get(itemPosition)
    }

    companion object {
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
        private const val MIC_CHANNELS = AudioFormat.CHANNEL_IN_MONO
        private const val MIC_CHANNEL_ENCODING = AudioFormat.ENCODING_PCM_16BIT
        private const val MIC_SOURCE = MediaRecorder.AudioSource.VOICE_RECOGNITION
        private const val SAMPLE_RATE = 16000
        private const val CHUNK_SIZE_SAMPLES = 1280
        private const val BYTES_PER_SAMPLE = 2
        private const val SHARE_PREF_API_KEY = "api_key"

        /** Saves the API Key in user shared preference.  */
        private fun saveApiKey(context: Context, key: String) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(SHARE_PREF_API_KEY, key)
                .commit()
        }

        /** Gets the API key from shared preference.  */
        private fun getApiKey(context: Context): String? {
            return PreferenceManager.getDefaultSharedPreferences(context).getString(
                SHARE_PREF_API_KEY, ""
            )
        }
    }
}
