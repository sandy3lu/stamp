package com.yunjing.eurekaclient2.common.utils;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

public class CryptoUtil {

    public static String getDigest(byte[] contents){
        SM3Digest sm3Digest = new SM3Digest();
        sm3Digest.reset();
        sm3Digest.update(contents,0,contents.length);
        byte[] out = new byte[sm3Digest.getDigestSize()];
        sm3Digest.doFinal(out,0);
        return ByteUtils.toHexString(out);
    }
}
