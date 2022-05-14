package com.zodiac.in4chan;

import
import android.app.Application;

public class In4Chan extends Application {

    public static RegistrationKeyModel generateKeys() throws InvalidKeyException, IOException {
        IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();
        int registrationId = KeyHelper.generateRegistrationId(false);
        SignedPreKeyRecord signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair,new Random().nextInt(Medium.MAX_VALUE - 1));
        List<PreKeyRecord> preKeys = KeyHelper.generatePreKeys(new Random().nextInt(Medium.MAX_VALUE - 101), 100);
        return new RegistrationKeyModel(
                identityKeyPair,
                registrationId,
                preKeys,
                signedPreKey
        );
    }


}

