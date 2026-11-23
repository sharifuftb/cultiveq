package com.example.data.auth

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ITERATIONS = 12_000
    private const val KEY_LENGTH_BITS = 256
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    fun hashPassword(password: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = pbkdf2(password.toCharArray(), salt)
        return "${salt.toHex()}:${hash.toHex()}"
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 2) return false
        val salt = parts[0].fromHex() ?: return false
        val expected = parts[1].fromHex() ?: return false
        val actual = pbkdf2(password.toCharArray(), salt)
        return MessageDigest.isEqual(expected, actual)
    }

    private fun pbkdf2(chars: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(chars, salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.fromHex(): ByteArray? {
        if (length % 2 != 0) return null
        return try {
            chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        } catch (_: NumberFormatException) {
            null
        }
    }
}
