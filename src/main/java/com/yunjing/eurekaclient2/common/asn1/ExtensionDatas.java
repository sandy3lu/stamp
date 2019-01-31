package com.yunjing.eurekaclient2.common.asn1;

import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * The type Extension datas.
 * ExtensionDatas::=SEQUENCE SIZE (O..MAX) OF ExtData
 *     ExtData::=SEQUENCE{
 *     extnID     OBJECT IDENTIFIER
 *     critical     BOOLEAN DEFAULT FALSE
 *     extnValue   OCTET STRING
 *
 * @author lurong
 */
public class ExtensionDatas extends ASN1Object{
    private Hashtable extensions = new Hashtable();
    private Vector ordering = new Vector();

    public static ExtensionDatas getInstance(
            ASN1TaggedObject obj,
            boolean explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static ExtensionDatas getInstance(
            Object obj)
    {
        if (obj instanceof ExtensionDatas)
        {
            return (ExtensionDatas)obj;
        }
        else if (obj != null)
        {
            return new ExtensionDatas(ASN1Sequence.getInstance(obj));
        }

        return null;
    }



    /**
     * Constructor from ASN1Sequence.
     * <p>
     * The extensions are a list of constructed sequences, either with (OID, OctetString) or (OID, Boolean, OctetString)
     * </p>
     */
    private ExtensionDatas(
            ASN1Sequence seq)
    {
        Enumeration e = seq.getObjects();

        while (e.hasMoreElements())
        {
            ExtData ext = ExtData.getInstance(e.nextElement());

            if (extensions.containsKey(ext.getExtnID()))
            {
                throw new IllegalArgumentException("repeated extension found: " + ext.getExtnID());
            }

            extensions.put(ext.getExtnID(), ext);
            ordering.addElement(ext.getExtnID());
        }
    }

    /**
     * Base Constructor
     *
     * @param extension a single extension.
     */
    public ExtensionDatas(
            ExtData extension)
    {
        this.ordering.addElement(extension.getExtnID());
        this.extensions.put(extension.getExtnID(), extension);
    }

    /**
     * Base Constructor
     *
     * @param extensions an array of extensions.
     */
    public ExtensionDatas(
            ExtData[] extensions)
    {
        for (int i = 0; i != extensions.length; i++)
        {
            ExtData ext = extensions[i];

            this.ordering.addElement(ext.getExtnID());
            this.extensions.put(ext.getExtnID(), ext);
        }
    }

    /**
     * return an Enumeration of the extension field's object ids.
     */
    public Enumeration oids()
    {
        return ordering.elements();
    }

    /**
     * return the extension represented by the object identifier
     * passed in.
     *
     * @return the extension if it's present, null otherwise.
     */
    public ExtData getExtension(
            ASN1ObjectIdentifier oid)
    {
        return (ExtData)extensions.get(oid);
    }

    /**
     * return the parsed value of the extension represented by the object identifier
     * passed in.
     *
     * @return the parsed value of the extension if it's present, null otherwise.
     */
    public ASN1Encodable getExtensionParsedValue(ASN1ObjectIdentifier oid)
    {
        ExtData ext = this.getExtension(oid);

        if (ext != null)
        {
            return ext.getParsedValue();
        }

        return null;
    }

    @Override
    public ASN1Primitive toASN1Primitive()
    {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        Enumeration e = ordering.elements();

        while (e.hasMoreElements())
        {
            ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier)e.nextElement();
            ExtData ext = (ExtData)extensions.get(oid);

            vec.add(ext);
        }

        return new DERSequence(vec);
    }
}

