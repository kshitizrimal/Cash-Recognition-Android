package np.com.intelaid.cash

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer

import android.os.Bundle
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.camerakit.CameraKitView
import java.util.*
import java.util.concurrent.Executors





class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    // Implementation of TextToSpeech Abstract class
    override fun onInit(status: Int) {
        textToSpeechStatus = status
        playInitialMessage(textToSpeechStatus)
    }

    companion object {
        private const val MODEL_PATH = "Cash.tflite"
        private const val LABEL_PATH = "labels.txt"
        private const val INPUT_SIZE = 224
    }

    private lateinit var classifier: Classifier
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var cameraView: CameraKitView
    private lateinit var resultOutput: TextView
    private lateinit var captureButton: Button
    private lateinit var tts: TextToSpeech
    private lateinit var builder: AlertDialog.Builder
    private lateinit var dialogView: View
    private lateinit var dialogMessage: TextView
    private lateinit var dialog: AlertDialog
    private lateinit var nepaliAudio: NepaliAudio
    private lateinit var audioPlayer: MediaPlayer
    private lateinit var languagePreference:LanguagePreference
    private  var textToSpeechStatus: Int = 0
    private  var result: String = ""

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initTensorFlowAndLoadModel()

        nepaliAudio = NepaliAudio(this)

        //Text to Speech
        tts = TextToSpeech(this, this)
        audioPlayer = nepaliAudio.initialMessageAudio()

        languagePreference = LanguagePreference(this)


        cameraView = findViewById(R.id.cameraView)
        resultOutput = findViewById(R.id.resultOutput)
        captureButton = findViewById(R.id.captureButton)

        // Dialog Box
        builder = AlertDialog.Builder(this)
        dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)
        dialogMessage = dialogView.findViewById(R.id.message)
        dialogMessage.text = "Computing..."
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()


        captureButton.setOnClickListener {
            dialog.show()
            cameraView.captureImage { _, photo ->
                Thread(Runnable {
                    onCaptureImage(photo)
                    resultOutput.post { resultOutput.text = result }
                }).start()
            }
        }

        cameraView.setOnTouchListener { _, event ->

            if (event.action == MotionEvent.ACTION_DOWN) {
                dialog.show()
                cameraView.captureImage { _, photo ->
                    Thread(Runnable {
                        onCaptureImage(photo)
                        resultOutput.post { resultOutput.text = result }
                    }).start()
                }
            }
            true
        }
    }

    override  fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            languagePreference.setNepaliLanguage()

            vibrate()

            playInitialMessage(textToSpeechStatus)

        }
        return true
    }

    private fun playInitialMessage(status: Int) {
        if(languagePreference.isNepaliLanguage()) {
            tts.stop() // stop tts if it's playing
            //Nepali audio
            audioPlayer = nepaliAudio.initialMessageAudio()
            audioPlayer.start()
        } else {
            if (status == TextToSpeech.SUCCESS) {
                audioPlayer.stop() // stop media player if it's playing
                audioPlayer.reset()
                // set US English as language for tts
                val result = tts!!.setLanguage(Locale.US)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "The Language specified is not supported!")
                } else {
                    tts!!.speak("Please Touch the screen to capture image.", TextToSpeech.QUEUE_FLUSH, null, "")
                }

            } else {
                Log.e("TTS", "Initilization Failed!")
            }
        }
    }

    private fun vibrate() {
        val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibratorService.vibrate(500)
    }

    private fun onCaptureImage(photo: ByteArray) {
        var bitmap = BitmapFactory.decodeByteArray(photo, 0, photo.size)
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)

        val results = classifier.recognizeImage(bitmap)
        playCashAudio(results)

    }

    private fun playCashAudio(results: List<IClassifier.Recognition>) {
        if(languagePreference.isNepaliLanguage()) {
            tts.stop()
            result = if (results.isNotEmpty()) {
                results[0].toString()
            } else {
                "Can't identify please try again !!!"
            }

            var audioResult = if (results.isNotEmpty()) {
                results[0].title
            } else {
                "Can't identify please try again !!!"
            }

            dialog.dismiss()

            vibrate() // vibrate to give user feedback
            audioPlayer = nepaliAudio.cashAudio(audioResult)
            audioPlayer.start()
        } else {
            audioPlayer.stop() // stop media player if it's playing
            audioPlayer.reset()

            result = if (results.isNotEmpty()) {
                results[0].toString()
            } else {
                "Can't identify please try again !!!"
            }

            dialog.dismiss()

            vibrate() // vibrate to give user feedback
            // speak the result
            tts!!.speak(result, TextToSpeech.QUEUE_FLUSH, null,"")
        }
    }

    override fun onResume() {
        super.onResume()
        cameraView.onResume()
    }

    override fun onPause() {
        super.onPause()
        cameraView.onPause()
    }

    override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
        executor.execute { classifier.close() }
        audioPlayer.release()
    }

    private fun initTensorFlowAndLoadModel() {
        executor.execute {
            try {
                classifier = Classifier.create(
                        assets,
                        MODEL_PATH,
                        LABEL_PATH,
                        INPUT_SIZE)
            } catch (e: Exception) {
                throw RuntimeException("Error initializing TensorFlow!", e)
            }
        }
    }
}
