package co.edu.uniandes.vinilos.ui.common

import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders

private const val BROWSER_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

/**
 * Modelo de Glide para una imagen remota del backend. Servidores como Wikimedia Commons
 * devuelven HTTP 403 si el User-Agent es el por defecto, así que se envía uno de navegador.
 * Una URL vacía da null para que Glide muestre el placeholder.
 */
fun remoteImage(url: String): GlideUrl? =
    if (url.isBlank()) {
        null
    } else {
        GlideUrl(url, LazyHeaders.Builder().addHeader("User-Agent", BROWSER_USER_AGENT).build())
    }
