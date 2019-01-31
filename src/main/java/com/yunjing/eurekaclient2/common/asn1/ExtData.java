package com.yunjing.eurekaclient2.common.asn1;


import org.bouncycastle.asn1.*;

import java.io.IOException;

public class ExtData extends ASN1Object {
    private ASN1ObjectIdentifier extnID;
    /** default, DER will not encoding*/
    private ASN1Boolean critial = ASN1Boolean.FALSE;
    private ASN1OctetString extnValue;

    public ExtData(ASN1ObjectIdentifier extnID, ASN1Boolean critial,ASN1OctetString extnValue ){
        this.extnID = extnID;
        this.critial = critial;
        this.extnValue = extnValue;
    }

    public ExtData(ASN1ObjectIdentifier extnID, ASN1OctetString extnValue ){
        this.extnID = extnID;
        this.extnValue = extnValue;
    }

    public ExtData(ASN1Sequence seq){
        extnID = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0));
        if(seq.size()>2){
            critial = ASN1Boolean.getInstance(seq.getObjectAt(1));
        }
        extnValue = ASN1OctetString.getInstance(seq.getObjectAt(seq.size()-1));

    }

    public static ExtData getInstance(
            ASN1TaggedObject obj,
            boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static ExtData getInstance(
            Object obj)
    {
        if (obj instanceof ExtData)
        {
            return (ExtData)obj;
        }
        else if (obj != null)
        {
            return new ExtData(ASN1Sequence.getInstance(obj));
        }

        return null;
    }


    public ASN1Boolean getCritial() {
        return critial;
    }

    public ASN1ObjectIdentifier getExtnID() {
        return extnID;
    }

    public ASN1OctetString getExtnValue() {
        return extnValue;
    }

    public ASN1Encodable getParsedValue()
    {
        return convertValueToObject(this);
    }

    /**
     * Convert the value of the passed in extension to an object
     * @param ext the extension to parse
     * @return the object the value string contains
     * @exception IllegalArgumentException if conversion is not possible
     */
    private static ASN1Primitive convertValueToObject(
            ExtData ext)
            throws IllegalArgumentException
    {
        try
        {
            return ASN1Primitive.fromByteArray(ext.getExtnValue().getOctets());
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("can't convert extension: " +  e);
        }
    }

    @Override
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(extnID);
        if(critial.isTrue()) {
            v.add(critial);
        }
        v.add(extnValue);

        return new DERSequence(v);
    }
}



