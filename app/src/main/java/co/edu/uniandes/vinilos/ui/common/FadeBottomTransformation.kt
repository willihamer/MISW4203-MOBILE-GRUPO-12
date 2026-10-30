package co.edu.uniandes.vinilos.ui.common

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import java.security.MessageDigest

/**
 * Recorta la imagen como centerCrop y la vuelve transparente de forma gradual en la parte
 * inferior ([fadeFraction] de la altura), para que se funda con el fondo de la pantalla
 * en vez de terminar en un borde recto.
 */
class FadeBottomTransformation(private val fadeFraction: Float = 0.55f) : BitmapTransformation() {

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val cropped = TransformationUtils.centerCrop(pool, toTransform, outWidth, outHeight)
        val width = cropped.width.toFloat()
        val height = cropped.height.toFloat()
        val fadeStart = height * (1 - fadeFraction)

        val result = pool.get(cropped.width, cropped.height, Bitmap.Config.ARGB_8888)
        result.setHasAlpha(true)
        val canvas = Canvas(result)
        canvas.drawBitmap(cropped, 0f, 0f, null)
        val mask = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, fadeStart, 0f, height, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP)
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        canvas.drawRect(0f, fadeStart, width, height, mask)
        canvas.setBitmap(null)

        if (cropped !== toTransform) pool.put(cropped)
        return result
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("$ID$fadeFraction".toByteArray(CHARSET))
    }

    override fun equals(other: Any?): Boolean = other is FadeBottomTransformation && other.fadeFraction == fadeFraction

    override fun hashCode(): Int = ID.hashCode() * 31 + fadeFraction.hashCode()

    private companion object {
        const val ID = "co.edu.uniandes.vinilos.ui.common.FadeBottomTransformation"
    }
}
