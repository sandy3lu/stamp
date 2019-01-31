package com.yunjing.eurekaclient2.common.asn1;

import org.bouncycastle.asn1.*;

public class CertDigestObj extends ASN1Object {
    private DERPrintableString type;
    private ASN1OctetString value;

    public CertDigestObj(DERPrintableString type, DEROctetString value){
        this.type = type;
        this.value = value;
    }

    private CertDigestObj( ASN1Sequence seq)
    {
        type = DERPrintableString.getInstance(seq.getObjectAt(0));
        value = DEROctetString.getInstance(seq.getObjectAt(1));

    }

    public static CertDigestObj getInstance(
            ASN1TaggedObject obj,
            boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static CertDigestObj getInstance(
            Object obj)
    {
        if (obj instanceof SESSignature)
        {
            return (CertDigestObj)obj;
        }
        else if (obj != null)
        {
            return new CertDigestObj(ASN1Sequence.getInstance(obj));
        }

        return null;
    }


    public DERPrintableString getType() {
        return type;
    }

    public ASN1OctetString getValue() {
        return value;
    }
    @Override
    public ASN1Primitive toASN1Primitive(){

        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(type);
        v.add(value);
        return new DERSequence(v);

    }
}
