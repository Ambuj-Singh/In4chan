package com.zodiac.in4chan;

import android.app.Application;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.zodiac.in4chan.BackEnd.Services.DataContext;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.PreKeyStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.util.KeyHelper;
import org.whispersystems.libsignal.util.Medium;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class In4Chan extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance(Tools.firebaseURL).setPersistenceEnabled(true);
      /*  try {
            RegistrationKeyModel registrationKeyModel = generateKeys();
            DataContext dataContext = new DataContext(this);
            dataContext.setKeysAndRegistrationId(registrationKeyModel.getIdentityKeyPair(),registrationKeyModel.getRegistrationId());

        }
        catch (Exception e){
            Log.i("In4Chan_signal","error : "+e.getMessage());
        }*/
    }

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

