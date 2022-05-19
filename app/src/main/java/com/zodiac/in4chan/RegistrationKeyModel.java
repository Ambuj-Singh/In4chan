package com.zodiac.in4chan;

import com.google.gson.Gson;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class RegistrationKeyModel {
    IdentityKeyPair identityKeyPair;
    int registrationId;
    List<PreKeyRecord> preKeys;
    SignedPreKeyRecord signedPreKeyRecord;

    public RegistrationKeyModel(IdentityKeyPair identityKeyPair, int registrationId, List<PreKeyRecord> preKeys, SignedPreKeyRecord signedPreKeyRecord) {
        this.identityKeyPair = identityKeyPair;
        this.registrationId = registrationId;
        this.preKeys = preKeys;
        this.signedPreKeyRecord = signedPreKeyRecord;
    }

    public RegistrationKeyModel(String identityKeyPair, int registrationId, String[] preKeys, String signedPreKeyRecord) throws InvalidKeyException, IOException {
        this.identityKeyPair = new IdentityKeyPair(KeyUtils.decodeToByteArray(identityKeyPair));
        this.registrationId = registrationId;
        List<PreKeyRecord> preKeyRecords = new ArrayList<>();
        for(String item : preKeys) {
            preKeyRecords.add(new PreKeyRecord(KeyUtils.decodeToByteArray(item)));
        }
        this.preKeys = preKeyRecords;
        this.signedPreKeyRecord = new SignedPreKeyRecord(KeyUtils.decodeToByteArray(signedPreKeyRecord));
    }

    public IdentityKeyPair getIdentityKeyPair() {
        return identityKeyPair;
    }

    public String getIdentityKeyPairString() {
        return KeyUtils.encode(identityKeyPair.serialize());
    }

    public String getIdentityKeyPublicString() {
        return KeyUtils.encode(identityKeyPair.getPublicKey().serialize());
    }

    public void setIdentityKeyPair(IdentityKeyPair identityKey) {
        this.identityKeyPair = identityKey;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public List<PreKeyRecord> getPreKeys() {
        return preKeys;
    }

    public String getPreKeyIds() {
        List<String> preKeyList = new ArrayList<>();
        for (PreKeyRecord preKey : preKeys) {
            preKeyList.add(KeyUtils.encode(preKey.serialize()));
        }
        return new Gson().toJson(preKeyList);
    }

    public SignedPreKeyRecord getSignedPreKeyRecord() {
        return signedPreKeyRecord;
    }

    public String getSignedPreKeyRecordString() {
        return KeyUtils.encode(signedPreKeyRecord.serialize());
    }

    public void setSignedPreKeyRecord(SignedPreKeyRecord signedPreKeyRecord) {
        this.signedPreKeyRecord = signedPreKeyRecord;
    }

    public String getPublicIdentityKey() {

        return KeyUtils.encode(identityKeyPair.getPublicKey().serialize());
    }

    public String getSignedPreKeyPublicKey() {
        return KeyUtils.encode(signedPreKeyRecord.getKeyPair().getPublicKey().serialize());
    }

    public int getSignedPreKeyId() {
        return signedPreKeyRecord.getId();
    }

    public String getSignedPreKeyRecordSignature() {
        return KeyUtils.encode(signedPreKeyRecord.getSignature());
    }
}
