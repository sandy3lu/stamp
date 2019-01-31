package com.yunjing.eurekaclient2.common.asn1;

import org.bouncycastle.asn1.*;

/**
ASN.1定义为：
 SES_ESPropertyInfo::=SEQUENCE{
        Type  INTEGER
        Name  UTF8String
        certListType  INTEGER
        certList  SEQUENCE OF cert
        createDate  UTCTIME
        validStart   UTCTIME
        validEnd    UTCTIME
   }
 */
public class SESESPropertyInfo extends ASN1Object {
    private ASN1Integer type;
    private DERUTF8String name;
    private ASN1Integer certListType;
    private ASN1Sequence certList;
    private ASN1UTCTime createDate;
    private ASN1UTCTime validStart;
    private ASN1UTCTime validEnd;

    public SESESPropertyInfo(ASN1Integer type,DERUTF8String name, ASN1Integer certListType,ASN1Sequence certList, ASN1UTCTime createDate, ASN1UTCTime validStart, ASN1UTCTime validEnd){
        this.type = type;
        this.name = name;
        this.certListType = certListType;
        this.certList =certList;
        this.createDate = createDate;
        this.validStart = validStart;
        this.validEnd = validEnd;
    }

    public SESESPropertyInfo( ASN1Sequence    seq){
        int index = 0;
        type = ASN1Integer.getInstance(seq.getObjectAt(index));
        index++;
        name = DERUTF8String.getInstance(seq.getObjectAt(index));
        index++;
        certListType = ASN1Integer.getInstance(seq.getObjectAt(index));
        index++;
        certList =(ASN1Sequence)(seq.getObjectAt(index));
        index++;
        createDate = ASN1UTCTime.getInstance(seq.getObjectAt(index));
        index++;
        validStart = ASN1UTCTime.getInstance(seq.getObjectAt(index));
        index++;
        validEnd =  ASN1UTCTime.getInstance(seq.getObjectAt(index));
    }

    public static SESESPropertyInfo getInstance(
            ASN1TaggedObject obj,
            boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static SESESPropertyInfo getInstance(
            Object obj)
    {
        if (obj instanceof SESSignature)
        {
            return (SESESPropertyInfo)obj;
        }
        else if (obj != null)
        {
            return new SESESPropertyInfo(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    public ASN1Integer getType() {
        return type;
    }
    public ASN1Integer getCertListType() {
        return certListType;
    }

    public ASN1Sequence getCertList() {
        return certList;
    }

    public ASN1UTCTime getCreateDate() {
        return createDate;
    }

    public ASN1UTCTime getValidEnd() {
        return validEnd;
    }

    public ASN1UTCTime getValidStart() {
        return validStart;
    }

    public DERUTF8String getName() {
        return name;
    }

    @Override
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(type);
        v.add(name);
        v.add(certListType);
        v.add(certList);
        v.add(createDate);
        v.add(validStart);
        v.add(validEnd);
        return new DERSequence(v);
    }
}
