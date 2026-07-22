package com.babytracker.core.ai.config

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

class AiDeviceKeyStore {
    companion object {
        private const val KEY_ALIAS = "baby_tracker_ai_device_key_v1"
        private const val KEYSTORE = "AndroidKeyStore"
    }

    fun publicKeyBase64(): String {
        ensureKeyPair()
        val certificate = keyStore().getCertificate(KEY_ALIAS)
            ?: error("AI 设备公钥不可用")
        return Base64.encodeToString(certificate.publicKey.encoded, Base64.NO_WRAP)
    }

    fun decrypt(envelope: String): String {
        ensureKeyPair()
        val privateKey = keyStore().getKey(KEY_ALIAS, null)
            ?: error("AI 设备私钥不可用")
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        val oaep = OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT,
        )
        cipher.init(Cipher.DECRYPT_MODE, privateKey, oaep)
        return cipher.doFinal(Base64.decode(envelope, Base64.DEFAULT)).toString(Charsets.UTF_8)
    }

    private fun ensureKeyPair() {
        if (keyStore().containsAlias(KEY_ALIAS)) return
        val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_DECRYPT,
        )
            .setKeySize(2_048)
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setUserAuthenticationRequired(false)
            .build()
        generator.initialize(spec)
        generator.generateKeyPair()
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
}
