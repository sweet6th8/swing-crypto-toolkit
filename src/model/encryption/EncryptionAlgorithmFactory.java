package model.encryption;

// Import specific algorithm classes
import model.encryption.AESEncryption;
import model.encryption.RSAEncryption;
import model.encryption.CaesarCipher;
import model.encryption.VigenereCipher;
// TODO: Add imports for DESede, ChaCha20Poly1305 when implemented

/**
 * Factory class to create instances of EncryptionAlgorithm.
 */
public class EncryptionAlgorithmFactory {

    /**
     * Creates an EncryptionAlgorithm instance for key generation.
     * Mode and padding are not typically required for key generation itself,
     * so they might be null or default values here.
     *
     * @param algorithm The name of the algorithm (e.g., "AES", "RSA").
     * @param keySize   The desired key size.
     * @return An instance of the specified EncryptionAlgorithm.
     * @throws IllegalArgumentException if the algorithm is unsupported or key size
     *                                  is invalid.
     */
    public static EncryptionAlgorithm createAlgorithmForKeyGen(String algorithm, int keySize) {
        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm name cannot be null.");
        }

        switch (algorithm.toUpperCase()) {
            case "AES":
                // For key gen, mode/padding don't matter, use defaults or null
                return new AESEncryption(null, null, keySize);
            case "RSA":
                // For key gen, padding doesn't matter
                return new RSAEncryption(keySize);
            // TODO: Add cases for DESede, ChaCha20Poly1305
            // case "DESEDE":
            // return new DESedeEncryption(null, null, keySize);
            // case "CHACHA20POLY1305":
            // // Key size is fixed for ChaCha20
            // if (keySize != 256) {
            // throw new IllegalArgumentException("ChaCha20Poly1305 requires a 256-bit
            // key.");
            // }
            // return new ChaCha20Poly1305Encryption(null, null);
            case "CAESAR":
                return new CaesarCipher(); // Key size is irrelevant
            case "VIGENERE":
                return new VigenereCipher(); // Key size is irrelevant
            default:
                throw new IllegalArgumentException("Unsupported algorithm for key generation: " + algorithm);
        }
    }

    /**
     * Creates an EncryptionAlgorithm instance for encryption/decryption operations.
     *
     * @param algorithm The name of the algorithm (e.g., "AES", "RSA", "Caesar").
     * @param mode      The encryption mode (e.g., "CBC", "ECB"). Can be null for
     *                  algorithms that don't use modes.
     * @param padding   The padding scheme (e.g., "PKCS5Padding", "NoPadding"). Can
     *                  be null for algorithms that don't use padding.
     * @param keySize   The key size (may not be relevant for all algorithms or
     *                  operations).
     * @return An instance of the specified EncryptionAlgorithm configured for
     *         operations.
     * @throws IllegalArgumentException if the algorithm or parameters are
     *                                  unsupported/invalid.
     */
    public static EncryptionAlgorithm createAlgorithmForOperation(String algorithm, String mode, String padding,
            int keySize) {
        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm name cannot be null.");
        }
        // Basic validation, more specific validation happens in constructors
        switch (algorithm.toUpperCase()) {
            case "AES":
                return new AESEncryption(mode, padding, keySize);
            case "RSA":
                // RSA constructor might need adjustment if padding is passed differently
                return new RSAEncryption(keySize, padding);
            // TODO: Add cases for DESede, ChaCha20Poly1305
            // case "DESEDE":
            // return new DESedeEncryption(mode, padding, keySize);
            // case "CHACHA20POLY1305":
            // return new ChaCha20Poly1305Encryption(mode, padding);
            case "CAESAR":
                // Mode/padding/keySize are not used by Caesar class itself
                // Shift needs to be set separately if not default
                return new CaesarCipher();
            case "VIGENERE":
                // Mode/padding/keySize are not used by Vigenere class itself
                // Keyword needs to be set separately if not default
                return new VigenereCipher();
            default:
                throw new IllegalArgumentException("Unsupported algorithm for operation: " + algorithm);
        }
    }
}