package co.edu.uniandes.vinilos.ui.common

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest
import kotlin.math.max

/**
 * Llena el espacio de la imagen alineándola arriba (en fotos de personas así se ven los
 * rostros y se recorta la parte inferior) y la vuelve transparente de forma gradual en la
 * parte de abajo ([fadeFraction] de la altura), para que se funda con el fondo de la
 * pantalla en vez de terminar en un borde recto.
 */
class FadeBottomTransformation(private val fadeFraction: Float = 0.55f) : BitmapTransformation() {

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val result = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        result.setHasAlpha(true)
        val canvas = Canvas(result)

        // Escala para cubrir todo el espacio, centrada en horizontal y pegada arriba.
        val scale = max(outWidth.toFloat() / toTransform.width, outHeight.toFloat() / toTransform.height)
        val matrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate((outWidth - toTransform.width * scale) / 2f, 0f)
        }
        canvas.drawBitmap(toTransform, matrix, Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG))

        val width = outWidth.toFloat()
        val height = outHeight.toFloat()
        val fadeStart = height * (1 - fadeFraction)
        val mask = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, fadeStart, 0f, height, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP)
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        canvas.drawRect(0f, fadeStart, width, height, mask)
        canvas.setBitmap(null)
        return result
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("$ID$fadeFraction".toByteArray(CHARSET))
    }

    override fun equals(other: Any?): Boolean = other is FadeBottomTransformation && other.fadeFraction == fadeFraction

    override fun hashCode(): Int = ID.hashCode() * 31 + fadeFraction.hashCode()

    private companion object {
        // Si cambia el recorte o el desvanecido, cambiar el ID invalida la caché de Glide.
        const val ID = "co.edu.uniandes.vinilos.ui.common.FadeBottomTransformation.topCrop"
    }
}
