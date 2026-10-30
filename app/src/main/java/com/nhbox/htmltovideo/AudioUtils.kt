package com.nhbox.htmltovideo

import org.json.JSONArray
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.tanh

/** WAV padhna, 48 kHz mono banana, silence trim karna aur poori timeline ka audio mix banana. */
object AudioUtils {
    const val SR = 48000

    /** WAV (16-bit PCM) ko mono ShortArray + sample rate mein badalta hai. */
    fun readWav(f: File): Pair<ShortArray, Int>? = readWavBytes(f.readBytes())

    fun readWavBytes(b: ByteArray): Pair<ShortArray, Int>? {
        if (b.size < 44) return null
        if (String(b, 0, 4, Charsets.US_ASCII) != "RIFF") return null
        val bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN)
        var p = 12
        var ch = 1
        var sr = 22050
        var bits = 16
        var dataOff = -1
        var dataLen = 0
        while (p + 8 <= b.size) {
            val id = String(b, p, 4, Charsets.US_ASCII)
            val sz = bb.getInt(p + 4)
            if (id == "fmt ") {
                ch = bb.getShort(p + 10).toInt()
                sr = bb.getInt(p + 12)
                bits = bb.getShort(p + 22).toInt()
            }
            if (id == "data") {
                dataOff = p + 8
                dataLen = if (sz < 0 || dataOff + sz > b.size) b.size - dataOff else sz
                break
            }
            if (sz < 0) break
            p += 8 + sz + (sz and 1)
        }
        if (dataOff < 0 || bits != 16 || ch < 1) return null
        val n = dataLen / 2 / ch
        if (n <= 0) return null
        val out = ShortArray(n)
        for (i in 0 until n) {
            var sum = 0
            for (c in 0 until ch) sum += bb.getShort(dataOff + (i * ch + c) * 2).toInt()
            out[i] = (sum / ch).toShort()
        }
        return Pair(out, sr)
    }

    fun resample(src: ShortArray, sr: Int, dst: Int): ShortArray {
        if (sr == dst) return src
        val n = (src.size.toLong() * dst / sr).toInt()
        val o = ShortArray(n)
        val r = sr.toDouble() / dst
        for (i in 0 until n) {
            val x = i * r
            val i0 = x.toInt()
            val f = x - i0
            val a = src[minOf(i0, src.size - 1)].toInt()
            val b = src[minOf(i0 + 1, src.size - 1)].toInt()
            o[i] = (a + (b - a) * f).toInt().toShort()
        }
        return o
    }

    /** Shuru aur end ki khamoshi hatata hai taaki timing sahi rahe. */
    fun trimSilence(s: ShortArray): ShortArray {
        val th = 300
        var a = 0
        while (a < s.size && Math.abs(s[a].toInt()) < th) a++
        var e = s.size - 1
        while (e > a && Math.abs(s[e].toInt()) < th) e--
        if (a >= e) return s
        val from = maxOf(0, a - SR / 20)
        val to = minOf(s.size, e + SR / 10)
        return s.copyOfRange(from, to)
    }

    /** Sab clips ko unke time par rakhkar ek mono 48 kHz track banata hai. */
    fun mix(totalSec: Double, ev: JSONArray, clips: Map<String, ShortArray>): ShortArray {
        val n = Math.ceil(totalSec * SR).toInt()
        val acc = IntArray(n)
        for (i in 0 until ev.length()) {
            val e = ev.getJSONObject(i)
            val c = clips[e.getString("key")] ?: continue
            val s = Math.round(e.getDouble("at") * SR).toInt()
            for (j in c.indices) {
                val p = s + j
                if (p >= n) break
                acc[p] += c[j].toInt()
            }
        }
        var peak = 1
        for (v in acc) {
            val a = Math.abs(v)
            if (a > peak) peak = a
        }
        val g = minOf(2.5, 26000.0 / peak)
        return ShortArray(n) { (acc[it] * g).toInt().coerceIn(-32768, 32767).toShort() }
    }

    /**
     * v3: voice-over + music + sound effects ko ek hi track mein milata hai.
     * Har stem ki apni volume (1.0 = 100%), upar se master. Auto-duck ON ho to voice ke time
     * music aur SFX apne aap halke ho jate hain. Aakhir mein soft limiter lagta hai taaki awaaz phate nahi.
     * Voice normalization pehle jaisi hi hai (mix() wali), isliye purane (sirf voice) projects ki awaaz same rehti hai.
     */
    fun mixStems(
        totalSec: Double,
        ev: JSONArray?,
        clips: Map<String, ShortArray>,
        music: ShortArray?,
        sfx: ShortArray?,
        master: Float,
        voiceVol: Float,
        musicVol: Float,
        sfxVol: Float,
        duck: Boolean
    ): ShortArray {
        val n = Math.ceil(totalSec * SR).toInt()

        // 1) voice track (normalize)
        val voice = FloatArray(n)
        var hasVoice = false
        if (ev != null) {
            val acc = IntArray(n)
            for (i in 0 until ev.length()) {
                val e = ev.getJSONObject(i)
                val c = clips[e.getString("key")] ?: continue
                val s = Math.round(e.getDouble("at") * SR).toInt()
                for (j in c.indices) {
                    val p = s + j
                    if (p >= n) break
                    if (p >= 0) acc[p] += c[j].toInt()
                }
                hasVoice = true
            }
            var peak = 1
            for (v in acc) {
                val a = Math.abs(v)
                if (a > peak) peak = a
            }
            val g = minOf(2.5, 26000.0 / peak).toFloat() / 32768f
            for (i in 0 until n) voice[i] = acc[i] * g
        }

        // 2) duck envelope (10 ms blocks, tez attack, dheema release)
        val blk = SR / 100
        val nb = n / blk + 2
        val env = FloatArray(nb + 1)
        if (duck && hasVoice) {
            var cur = 0f
            for (b in 0 until nb) {
                var pk = 0f
                val from = b * blk
                val to = minOf(n, from + blk)
                for (i in from until to) {
                    val a = Math.abs(voice[i])
                    if (a > pk) pk = a
                }
                val target = if (pk > 0.02f) 1f else 0f
                cur += (target - cur) * (if (target > cur) 0.35f else 0.02f)
                env[b] = cur
            }
            env[nb] = cur
        }

        // 3) mix + soft limiter
        val out = ShortArray(n)
        for (i in 0 until n) {
            val b = i / blk
            val f = (i - b * blk) / blk.toFloat()
            val e = env[b] + (env[b + 1] - env[b]) * f
            var x = voice[i] * voiceVol
            if (music != null && i < music.size) x += music[i] / 32768f * musicVol * (1f - 0.55f * e)
            if (sfx != null && i < sfx.size) x += sfx[i] / 32768f * sfxVol * (1f - 0.30f * e)
            x *= master
            out[i] = softLimit(x)
        }
        return out
    }

    private fun softLimit(x: Float): Short {
        val th = 0.85f
        val a = Math.abs(x)
        val y = if (a <= th) a else th + (1f - th) * tanh((a - th) / (1f - th))
        val v = ((if (x < 0f) -y else y) * 32767f).toInt().coerceIn(-32768, 32767)
        return v.toShort()
    }
}
