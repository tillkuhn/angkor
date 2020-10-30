package net.timafe.angkor

import org.junit.jupiter.api.Test
import java.security.KeyPairGenerator
import java.nio.file.Files
import java.nio.file.Paths
import java.security.*

/**
 * See https://stackoverflow.com/questions/11410770/load-rsa-public-key-from-file
 * See https://kodejava.org/how-to-create-a-digital-signature-and-sign-data/
 * See https://gist.github.com/destan/b708d11bd4f403506d6d5bb5fe6a82c5
 */
class GenerateDigitalSignature {

    // @Test // currently only for experimenting
    fun testkey() {
        try {
            // Get instance and initialize a KeyPairGenerator object.
            val keyGen = KeyPairGenerator.getInstance("DSA", "SUN")
            val random: SecureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN")
            keyGen.initialize(1024, random)

            // Get a PrivateKey from the generated key pair.
            val keyPair = keyGen.generateKeyPair()
            val privateKey = keyPair.private

            // Get an instance of Signature object and initialize it.
            val signature: Signature = Signature.getInstance("SHA1withDSA", "SUN")
            signature.initSign(privateKey)

            // Supply the data to be signed to the Signature object
            // using the update() method and generate the digital
            // signature.
            val bytes: ByteArray = Files.readAllBytes(Paths.get("/tmp/README"))
            signature.update(bytes)
            val digitalSignature: ByteArray = signature.sign()

            // Save digital signature and the public key to a file.
            Files.write(Paths.get("/tmp/signature"), digitalSignature)
            Files.write(Paths.get("/tmp/publickey"), keyPair.public.encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
