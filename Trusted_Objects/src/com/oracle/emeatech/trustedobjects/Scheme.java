package com.oracle.emeatech.trustedobjects;

import com.oracle.emeatech.trustedobjects.keys.KeySize;

/**
 * A scheme represents the meta data required to perform
 * signing / encryption / decryption operations
 * @author ewan
 */
public interface Scheme
{

    /**
     * Returns the name of the algorithm to be used for
     * asymmetric (public/private key) encryption and decryption
     * operations by this scheme.
     * @return the algorithm name
     */
    public String getAsymmetricCipherAlgorithm();

    /**
     * Returns the name of the algorithm to be used for
     * symmetric (secret key) encryption and decryption
     * operations by this scheme.
     * @return the algorithm name
     */
    public String getSymmetricCipherAlgorithm();

    /**
     * Returns the name of the algorithm to be used for
     * asymmetric (public/private key) signing and verification
     * operations by this scheme.
     * @return the algorithm name
     */
    public String getSignatureAlgorithm();

    /**
     * Returns the name of the secret (symmetric) key type
     * to be used by this scheme.
     * @return the name of the key type
     */
    public String getSecretKeyType();

    /**
     * Returns the name of the secret (symmetric) key type (i.e. algorithm)
     * to be used by this scheme.
     * @return the name of the key type
     */
    public KeySize getSecretKeySize();

    /**
     * Returns the name of the asymmetric (public/private) key type
     * (i.e. algorithm) to be used by this scheme.
     * @return the name of the key type
     */
    public String getAsymmetricKeyType();

}
