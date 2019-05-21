package np.com.intelaid.cash
import android.content.Context
import android.media.MediaPlayer

class NepaliAudio {
    private lateinit var mp: MediaPlayer
    private var context: Context

    constructor(context: Context) {
       this.context = context
    }

    fun initialMessageAudio(): MediaPlayer {
        return MediaPlayer.create (context, R.raw.initial_message)
    }

    fun cashAudio(amount: String): MediaPlayer {
        return when(amount) {
            "five" -> MediaPlayer.create(context, R.raw.five)
            "ten" -> MediaPlayer.create(context, R.raw.ten)
            "twenty" -> MediaPlayer.create(context, R.raw.twenty)
            "fifty" -> MediaPlayer.create(context, R.raw.fifty)
            "hundred" -> MediaPlayer.create(context, R.raw.hundred)
            "fivehundred" -> MediaPlayer.create(context, R.raw.five_hundred)
            "thousand" -> MediaPlayer.create(context, R.raw.thousand)
            else -> {
                MediaPlayer.create(context, R.raw.error)
            }
        }
    }
}