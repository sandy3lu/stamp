package com.yunjing.eurekaclient2.common.asn1;

import org.bouncycastle.asn1.*;

/*
    SES_SealInfo::=SEQUENCE{
    header  SES_Header
    esID    IA5String
    property  SES_ESPropertyInfo
    picture   SES_ESPictrueInfo
    extDatas  EXPLICIT ExtensionDatas OPTIONAL
    }

 */
public class SESSealInfo extends ASN1Object {

    private  SESHeader header;
    private DERIA5String esID;
    private SESESPropertyInfo property;
    private SESESPictrueInfo picture;
    private ExtensionDatas extDatas;

    public SESSealInfo( SESHeader header, DERIA5String esID, SESESPropertyInfo property, SESESPictrueInfo picture, ExtensionDatas extDatas){
        this.header = header;
        this.esID = esID;
        this.property = property;
        this.picture = picture;
        if(extDatas!=null) {
            this.extDatas = extDatas;
        }
    }

    public SESSealInfo(ASN1Sequence seq){

        header = SESHeader.getInstance(seq.getObjectAt(0));
        esID = DERIA5String.getInstance(seq.getObjectAt(1));
        property = SESESPropertyInfo.getInstance(seq.getObjectAt(2));
        picture = SESESPictrueInfo.getInstance(seq.getObjectAt(3));;
        if(seq.size()>4) {
            extDatas = ExtensionDatas.getInstance(seq.getObjectAt(4));
        }
    }

    public static SESSealInfo getInstance(
            ASN1TaggedObject obj,
            boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static SESSealInfo getInstance(
            Object obj)
    {
        if (obj instanceof SESSignature)
        {
            return (SESSealInfo)obj;
        }
        else if (obj != null)
        {
            return new SESSealInfo(ASN1Sequence.getInstance(obj));
        }

        return null;
    }


    public DERIA5String getEsID() {
        return esID;
    }

    public ExtensionDatas getExtDatas() {
        return extDatas;
    }

    public SESESPictrueInfo getPicture() {
        return picture;
    }

    public SESESPropertyInfo getProperty() {
        return property;
    }

    public SESHeader getHeader() {
        return header;
    }
    @Override
    public ASN1Primitive toASN1Primitive(){

        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(header);
        v.add(esID);
        v.add(property);
        v.add(picture);
        if(extDatas!=null) {
            v.add(extDatas);
        }
        return new DERSequence(v);

    }

}
