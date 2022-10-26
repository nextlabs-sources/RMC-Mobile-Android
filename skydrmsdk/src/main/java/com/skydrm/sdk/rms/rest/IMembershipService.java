package com.skydrm.sdk.rms.rest;

import com.skydrm.sdk.exception.RmsRestAPIException;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

public interface IMembershipService {
    /**
     * @param dhClientPublicKey X.509 can be get by api:generateDHKeyPair
     * @return List<X509Certificate>  certificates
     * @throws Exception
     */
    List<X509Certificate> membership(byte[] dhClientPublicKey) throws CertificateException, RmsRestAPIException;

    /**
     * Calculate the DH-AgreementKey with client-side private key and server-side public key
     *
     * @param dhClientPrivateKey PKCS#8 format
     * @param dhServerPublicKey  .X509
     * @return
     * @throws Exception
     */
    byte[] calcDHAgreementKey(byte[] dhClientPrivateKey, byte[] dhServerPublicKey) throws Exception;

    KeyPair generateDHKeyPair(BigInteger p, BigInteger g) throws Exception;
}
