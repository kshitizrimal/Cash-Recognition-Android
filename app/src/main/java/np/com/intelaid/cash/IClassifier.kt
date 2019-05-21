package np.com.intelaid.cash

import android.graphics.Bitmap

interface IClassifier {
    data class Recognition(
            var id: String = "", // A unique identifier for what has been recognized. Specific to the class, not the instance of the object.
            var title: String = "", // Display name for the recognition.
            var confidence: Float = 0F // A sortable score for how good the recognition is relative to others. Higher should be better.
    )  {
        override fun toString(): String {
            return "Rupee $title detected."
        }
    }
    fun recognizeImage(bitmap: Bitmap): List<Recognition>
    fun close()
}