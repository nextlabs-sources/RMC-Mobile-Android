package com.skydrm.sdk.rms.rest.user;

import android.util.Base64;

import com.skydrm.sdk.Config;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.IMembershipService;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.DevLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class Membership extends RestAPI.RestServiceBase implements IMembershipService {

    public Membership(IRmUser user, OkHttpClient httpClient, Config config, DevLog log) {
        super(user, httpClient, config, log);
    }

    @Override
    public List<X509Certificate> membership(byte[] dhClientPublicKey) throws CertificateException, RmsRestAPIException {
        // prepare post data / json format
        JSONObject membershipJSON = new JSONObject();
        try {
            JSONObject parameters = new JSONObject();
            parameters.put("userId", user.getUserId());
            parameters.put("ticket", user.getTicket());
            parameters.put("membership", user.getMembershipId());
            parameters.put("publicKey", Base64.encodeToString(dhClientPublicKey, Base64.DEFAULT));
            membershipJSON.put("parameters", parameters);
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed prepare in membership-", RmsRestAPIException.ExceptionDomain.Common);
        }
        // request body
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                membershipJSON.toString());
        Request.Builder builder = new Request.Builder();
        setCommonParas(builder);
        Request request = builder
                .url(config.getMembershipURL())
                .put(body)
                .build();

        String responseString = executeNetRequest(request);
        log.v("membership:\n" + responseString);

        // pick out the .X509 certificates from the reponseString
        String certStr;
        try {
            JSONObject j = new JSONObject(responseString);
            if (!j.has("statusCode")) {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
            }
            int code = j.getInt("statusCode");
            if (code == 200) {
                if (j.has("results")) {
                    JSONObject results = j.getJSONObject("results");
                    certStr = results.getString("certficates");
                } else {
                    throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
                }
            } else if (code == 401 || code == 403) { // for this api, when ticket expires(such as change password), always return 403 not 401, why??
                throw new RmsRestAPIException("Authentication failed", RmsRestAPIException.ExceptionDomain.AuthenticationFailed, code);
            } else {
                throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common, code);
            }
        } catch (JSONException e) {
            throw new RmsRestAPIException("failed parse response", RmsRestAPIException.ExceptionDomain.Common);
        }

        List<X509Certificate> serverIssuedCertificates = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream stream = new ByteArrayInputStream(certStr.getBytes());
            Collection collection = cf.generateCertificates(stream);

            serverIssuedCertificates = new ArrayList<>();
            Iterator i = collection.iterator();
            while (i.hasNext()) {
                X509Certificate cert = (X509Certificate) i.next();
                //PublicKey pubKey = cert.getPublicKey();
                //String publicKey = Base64.encodeToString(pubKey.getEncoded(), Base64.DEFAULT);
                serverIssuedCertificates.add(cert);
            }
        } catch (CertificateException ce) {
            ce.printStackTrace();
            throw ce;
        }

        return serverIssuedCertificates;
    }

    @Override
    public byte[] calcDHAgreementKey(byte[] dhClientPrivateKey, byte[] dhServerPublicKey) throws Exception {
        // DH key spec
        KeyFactory dhKeyFactory = KeyFactory.getInstance("DH");
        // - server public key
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(dhServerPublicKey);
        PublicKey serverPublicKey = dhKeyFactory.generatePublic(keySpec);
        // - client private key
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(dhClientPrivateKey);
        PrivateKey clientPrivateKey = dhKeyFactory.generatePrivate(spec);
        // - calc agreement key
        KeyAgreement agreementKey = KeyAgreement.getInstance(dhKeyFactory.getAlgorithm());
        agreementKey.init(clientPrivateKey);
        agreementKey.doPhase(serverPublicKey, true);

        return agreementKey.generateSecret();
    }

    @Override
    public KeyPair generateDHKeyPair(BigInteger p, BigInteger g) throws Exception {
        DHParameterSpec dhParameterSpec = new DHParameterSpec(p, g);
        KeyPairGenerator generator = KeyPairGenerator.getInstance("DH");
        generator.initialize(dhParameterSpec, new SecureRandom());
        return generator.generateKeyPair();
    }
}
