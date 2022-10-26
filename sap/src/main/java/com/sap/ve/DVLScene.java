/*
 (C) 2015 SAP SE or an SAP affiliate company. All rights reserved.
*/
package com.sap.ve;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.sap.ve.DVLTypes.*;

import java.lang.Object;
import java.security.Provider;
import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.security.Security;

import android.util.Log;

public class DVLScene
{
    private long m_handle = 0;
    private Context m_context;
    public static Context m_static_context = null;

    public DVLScene(long handle, Context context)
    {
        m_handle = handle;
        m_context = context;
        m_static_context = context;
    }

    public static String GetTempFilePath()
    {
        String tempFilePath = "";
        if(m_static_context != null) {
            tempFilePath = m_static_context.getCacheDir().getAbsolutePath() + "/temp.tmp";
        }
        return tempFilePath;
    }

    // system stuff

    public long getHandle()
    {
        return m_handle;
    }

    public DVLRESULT RetrieveSceneInfo(int flags, SDVLSceneInfo sceneInfo)
    {
        return DVLRESULT.fromInt(nativeRetrieveSceneInfo(m_handle, flags, sceneInfo));
    }

    public DVLRESULT RetrieveNodeInfo(long id, int nodeInfoFlags, SDVLNodeInfo nodeInfo)
    {
        nodeInfo.nodeID = id;
        return DVLRESULT.fromInt(nativeRetrieveNodeInfo(m_handle, id, nodeInfoFlags, nodeInfo));
    }

    public DVLRESULT RetrieveMetadata(long id, java.util.ArrayList<SDVLMetadataNameValuePair> metadata)
    {
        return DVLRESULT.fromInt(nativeRetrieveMetadata(m_handle, id, metadata));
    }

    public DVLRESULT RetrieveVEIDs(long id, java.util.ArrayList<SDVLMetadataNameValuePair> VEIDs)
    {
        return DVLRESULT.fromInt(nativeRetrieveVEIDs(m_handle, id, VEIDs));
    }

    public DVLRESULT RetrieveThumbnail(long stepId, SDVLImage image)
    {
        return DVLRESULT.fromInt(nativeRetrieveThumbnail(m_handle, stepId, image));
    }

    public DVLRESULT RetrieveProcedures(SDVLProceduresInfo proceduresInfo)
    {
        return DVLRESULT.fromInt(nativeRetrieveProcedures(m_handle, proceduresInfo));
    }

    public DVLRESULT BuildPartsList(int maxParts, int maxNodesInSinglePart, int maxPartNameLength,
            DVLPARTSLISTTYPE type, DVLPARTSLISTSORT sort, long idConsumedStep, java.lang.String substring, SDVLPartsListInfo info)
    {
        return DVLRESULT.fromInt(nativeBuildPartsList(m_handle, maxParts, maxNodesInSinglePart, maxPartNameLength,
                type.ordinal(), sort.ordinal(), idConsumedStep, substring, info));
    }

    public DVLRESULT FindNodes(DVLFINDNODETYPE type, DVLFINDNODEMODE mode, java.lang.String str, SDVLNodeIDsArrayInfo info)
    {
        return DVLRESULT.fromInt(nativeFindNodes(m_handle, type.ordinal(), mode.ordinal(), str, info));
    }

    public int GetTotalSelectedNodesCount()
    {
        return nativeGetTotalSelectedNodesCount(m_handle);
    }

    public int GetVisibleSelectedNodesCount()
    {
        return nativeGetVisibleSelectedNodesCount(m_handle);
    }

    public int GetHiddenSelectedNodesCount()
    {
        return nativeGetHiddenSelectedNodesCount(m_handle);
    }

    public void PerformAction(DVLSCENEACTION action)
    {
        nativePerformAction(m_handle, action.ordinal());
    }

    public DVLRESULT ActivateStep(long id, boolean fromTheBeginning, boolean continueToTheNext)
    {
        return DVLRESULT.fromInt(nativeActivateStep(m_handle, id, fromTheBeginning, continueToTheNext));
    }

    public DVLRESULT PauseCurrentStep()
    {
        return DVLRESULT.fromInt(nativePauseCurrentStep(m_handle));
    }

    public DVLRESULT ChangeNodeFlags(long nodeId, int nodeFlags, int flagOp)
    {
        return DVLRESULT.fromInt(nativeChangeNodeFlags(m_handle, nodeId, nodeFlags, flagOp));
    }

    public DVLRESULT SetNodeOpacity(long nodeId, float opacity)
    {
        return DVLRESULT.fromInt(nativeSetNodeOpacity(m_handle, nodeId, opacity));
    }

    public DVLRESULT SetNodeHighlightColor(long nodeId, int color)
    {
        return DVLRESULT.fromInt(nativeSetNodeHighlightColor(m_handle, nodeId, color));
    }

    public DVLRESULT GetNodeWorldMatrix(long nodeId, SDVLMatrix mat)
    {
        return DVLRESULT.fromInt(nativeGetNodeWorldMatrix(m_handle, nodeId, mat));
    }

    public DVLRESULT SetNodeWorldMatrix(long nodeId, SDVLMatrix mat)
    {
        return DVLRESULT.fromInt(nativeSetNodeWorldMatrix(m_handle, nodeId, mat));
    }

    public DVLRESULT Execute(DVLEXECUTE type, String str)
    {
        if (type.equals(DVLEXECUTE.DYNAMICLABELS))
        {
            DVLRESULT res = ExecuteDynamicLabels(str);
            nativeUpdateDynamicLabels(m_handle);
            return res;
        }

        return DVLRESULT.fromInt(nativeExecute(m_handle, type.ordinal(), str));
    }

    public void Release()
    {
        nativeRelease(m_handle);
    }

    public class DynamicLabel
    {
        String id;
        String name;
        String text;
        Bitmap image;
        Typeface font;
        float posX, posY;
        float sizeX, sizeY;
        float anchorX, anchorY;
        float fontSize;
        float opacity;
        float marginX, marginY;
        int alignmentX, alignmentY;
        int textColor;
        int bgColor;
        int frameColor;
        int poiColorIndex;
        int poiColor;
        float frameThickness;
        float frameRadius;

        CharSequence csList[];
        TextPaint textPaint;
        boolean PrepareText(float width, float height, boolean cropLabel)
        {
            if (text == null)
                return false;

            width -= marginX * 2.f + frameThickness;
            height -= marginY * 2.f + frameThickness;
            if (width <= 0.f || height <= 0.f)
                return false;

            textPaint = new TextPaint();
            textPaint.setAntiAlias(true);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setColor(textColor | 0xFF000000);
            textPaint.setTypeface(font != null ? font : Typeface.DEFAULT);
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTextSize(fontSize);

            String split[] = text.split("\n");
            float fontSpacing = textPaint.getFontSpacing();
            int numLines = Math.min(split.length, (int)(height / fontSpacing));
            csList = new CharSequence[numLines];

            float maxLineWidth = 0.f;
            for (int i = 0; i < numLines; i++)
            {
                csList[i] = android.text.TextUtils.ellipsize(split[i], textPaint, width, android.text.TextUtils.TruncateAt.END);
                if (cropLabel)
                    maxLineWidth = Math.max(maxLineWidth, textPaint.measureText(csList[i], 0, csList[i].length()));
            }

            if (cropLabel)
            {
                sizeX = (float) Math.ceil(maxLineWidth + marginX * 2.f + frameThickness);
                sizeY = (float) Math.ceil(numLines * fontSpacing + marginY * 2.f + frameThickness);
            }

            return true;
        }
    }

    public class EncryptionHandler
    {
        final int  kCCKeySizeAES128 = 16;
        SecretKey mKey = null;

        byte[] key;
        public boolean DeriveKey(byte [] salt, char [] password)
        {

            PBEKeySpec spec = new PBEKeySpec(password, salt, 10000, kCCKeySizeAES128*8 );

            String tag = "EncryptionHandler:DeriveKey: ";
            try {

                SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2withHmacSHA256");

                mKey = f.generateSecret(spec);

                key = mKey.getEncoded();
            }
            catch (java.security.NoSuchAlgorithmException e)
            {
                mKey = null;
                Log.w(tag, e.toString());
                return false;
            }
            catch (java.security.spec.InvalidKeySpecException e)
            {
                mKey = null;
                Log.w(tag, e.toString());
                return false;
            }
            return true;
        }


        public byte[] Decrypt(byte [] bufIn, byte[] ivTextBytes)
        {
            String tag = "EncryptionHandler:Decrypt: ";
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                IvParameterSpec ivParameterSpec = new IvParameterSpec(ivTextBytes);
                cipher.init(Cipher.DECRYPT_MODE, mKey, ivParameterSpec);
                return cipher.doFinal(bufIn);
            }
            catch (java.security.NoSuchAlgorithmException e)
            {
                Log.w(tag, e.toString());
            }
            catch (javax.crypto.NoSuchPaddingException e)
            {
                Log.w(tag, e.toString());
            }
            catch (java.security.InvalidKeyException e)
            {
                Log.w(tag, e.toString());
            }
            catch (java.security.InvalidAlgorithmParameterException e)
            {
                Log.w(tag, e.toString());
            }
            catch (javax.crypto.IllegalBlockSizeException e)
            {
                Log.w(tag, e.toString());
            }
            catch (javax.crypto.BadPaddingException e)
            {
                Log.w(tag, e.toString());
            }
            return null;
        }
    }

    private static String getStringAttribute(NamedNodeMap attributes, String name)
    {
        Node node = attributes.getNamedItem(name);
        return node != null ? node.getNodeValue() : null;
    }

    private static float getFloatAttribute(NamedNodeMap attributes, String name, float defaultValue)
    {
        String str = getStringAttribute(attributes, name);
        return (str != null) ? Float.valueOf(str) : defaultValue;
    }

    private static int getIntAttribute(NamedNodeMap attributes, String name, int defaultValue)
    {
        String str = getStringAttribute(attributes, name);
        return (str != null) ? Integer.parseInt(str) : defaultValue;
    }

    private static int getColorAttribute(NamedNodeMap attributes, String name, int defaultValue)
    {
        String color = getStringAttribute(attributes, name);
        return (color != null) && (color.length() == 6) ? (Integer.parseInt(color, 16) | 0xFF000000) : defaultValue;
    }

    private static float[] getFloat2Attribute(NamedNodeMap attributes, String name, float x, float y)
    {
        float res[] = {x, y};
        String str = getStringAttribute(attributes, name);
        if (str != null)
        {
            String[] values = str.split(",");
            if (values.length == 2)
            {
                res[0] = Float.valueOf(values[0]);
                res[1] = Float.valueOf(values[1]);
            }
        }

        return res;
    }

    private static int[] getInt2Attribute(NamedNodeMap attributes, String name, int x, int y)
    {
        int res[] = {x, y};
        String str = getStringAttribute(attributes, name);
        if (str != null)
        {
            String[] values = str.split(",");
            if (values.length == 2)
            {
                res[0] = Integer.parseInt(values[0]);
                res[1] = Integer.parseInt(values[1]);
            }
        }

        return res;
    }

    public DVLRESULT ExecuteDynamicLabels(String xml)
    {
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//          try {
//              factory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
//              factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
//              factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
//              factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
//          } catch (javax.xml.parsers.ParserConfigurationException e) {
//          }

//          factory.setNamespaceAware(true);
//          final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//          factory.setValidating(true);
//          final Schema schema = sf.newSchema(new StreamSource(getClass().getResourceAsStream(SCHEMA_PATH)));
//          factory.setSchema(schema);

            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF8")));
        } catch (java.lang.Throwable e) {
            e.printStackTrace();
            Log.w("ExecuteDynamicLabels", e.getMessage());
            return DVLRESULT.BADFORMAT;
        }

        try {
            boolean isCGMScene = nativeIsCGMScene(m_handle);

            DisplayMetrics metrics = new DisplayMetrics();
            ((WindowManager) m_context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);

            ArrayList<Integer> poiColors = new ArrayList<Integer>();
            Element root = doc.getDocumentElement();
            boolean cropLabel = !isCGMScene;
            if (!isCGMScene)
            {
                String strLabelCrop = root.getAttribute("label-crop");
                if (strLabelCrop != null && (strLabelCrop.equalsIgnoreCase("false") || strLabelCrop.equals("0")))
                    cropLabel = false;
            }

            Node node = root.getFirstChild();
            while (node != null)
            {
                if (node.getNodeName().equals("dynamic-label"))
                {
                    DynamicLabel dl = new DynamicLabel();
                    NamedNodeMap attributes = node.getAttributes();

                    dl.id = getStringAttribute(attributes, "id");
                    dl.name = getStringAttribute(attributes, "name");
                    if ((dl.id == null) && (dl.name == null))
                        return DVLRESULT.BADFORMAT;

                    dl.text = getStringAttribute(attributes, "text");

                    String fontFamily = getStringAttribute(attributes, "font");
                    dl.font = fontFamily != null ? Typeface.create(fontFamily, Typeface.NORMAL) : Typeface.DEFAULT;

                    dl.image = null;
                    String image = getStringAttribute(attributes, "image");
                    if (image != null)
                    {
                        byte imageData[] = Base64.decode(image, 0);
                        if (imageData != null && imageData.length > 0)
                            dl.image = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    }

                    dl.fontSize = getFloatAttribute(attributes, "font-size", 12.f) * metrics.xdpi / 72.f;

                    dl.textColor = getColorAttribute(attributes, "text-color", 0xFFFFFFFF);
                    dl.bgColor = getColorAttribute(attributes, "bg-color", 0x00000000);
                    dl.frameColor = getColorAttribute(attributes, "frame-color", 0x00000000);

                    dl.opacity = getFloatAttribute(attributes, "opacity", dl.image != null ? 1.f : (dl.bgColor & 0xFF000000) != 0 ? 0.5f : 0.f);
                    if (dl.image == null)
                        dl.bgColor = (dl.bgColor & 0xFFFFFF) | ((int)(dl.opacity * 0xFF) << 24);

                    float pos[] = getFloat2Attribute(attributes, "position", 0.f, 0.f);
                    dl.posX = pos[0];
                    dl.posY = pos[1];

                    if (isCGMScene)
                    {
                        float size[] = getFloat2Attribute(attributes, "size", 1.f, 1.f);
                        dl.sizeX = size[0];
                        dl.sizeY = size[1];
                        dl.frameRadius = 0.f;
                    }
                    else
                    {
                        float size[] = getFloat2Attribute(attributes, "size", 4.f, 3.f);
                        dl.sizeX = size[0] * metrics.xdpi / 2.54f;
                        dl.sizeY = size[1] * metrics.ydpi / 2.54f;
                        dl.frameRadius = (dl.image == null) ? 10.f * metrics.density : 0.f;
                    }

                    float anchor[] = getFloat2Attribute(attributes, "pivot-point", 0.5f, 0.5f);
                    dl.anchorX = anchor[0];
                    dl.anchorY = anchor[1];

                    float margin[] = getFloat2Attribute(attributes, "margin", 0.f, 0.f);
                    dl.marginX = margin[0] * metrics.density;
                    dl.marginY = margin[1] * metrics.density;

                    int alignment[] = getInt2Attribute(attributes, "alignment", 0, 0);
                    dl.alignmentX = alignment[0];
                    dl.alignmentY = alignment[1];

                    dl.frameThickness = (dl.frameColor & 0xFF000000) != 0 ? 2.f * metrics.density : 0.f;

                    dl.poiColorIndex = getIntAttribute(attributes, "poi-color", 0);
                    dl.poiColor = dl.poiColorIndex < poiColors.size() ? poiColors.get(dl.poiColorIndex) : 0xFFFFFFFF;

                    if (cropLabel && dl.image == null)
                        dl.PrepareText(dl.sizeX, dl.sizeY, true);

                    DVLRESULT res = DVLRESULT.fromInt(nativeSetDynamicLabel(m_handle, dl.id, dl.name, dl));
                    if (res.Failed())
                        return res;
                }
                else if (!isCGMScene)
                {
                    if (node.getNodeName().equals("poi-color"))
                    {
                        String color = node.getTextContent();
                        if ((color != null) && (color.length() == 6))
                            poiColors.add(Integer.parseInt(color, 16) | 0xFF000000);
                    }
                    else if (node.getNodeName().equals("poi-icon"))
                    {
                        NamedNodeMap attributes = node.getAttributes();
                        String image = getStringAttribute(attributes, "image");
                        if (image != null)
                        {
                            byte imageData[] = Base64.decode(image, 0);
                            if (imageData != null && imageData.length > 0)
                            {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                                if (bitmap != null)
                                {
                                    float size[] = getFloat2Attribute(attributes, "size", bitmap.getWidth() / metrics.density, bitmap.getHeight() / metrics.density);
                                    nativeSetPOIIcon(m_handle, size[0] * metrics.density, size[1] * metrics.density, bitmap);
                                }
                            }
                        }
                    }
                }

                node = node.getNextSibling();
            }
        } catch (Throwable e) {
            Log.w("LoadDynamicLabels", e.getMessage());
            return DVLRESULT.FAIL;
        }

        return DVLRESULT.OK;
    }

    // native stuff

    // static private native void nativeRelease(long hScene);
    static private native int nativeRetrieveSceneInfo(long hScene, int flags, Object sceneInfo);
    static private native int nativeRetrieveNodeInfo(long hScene, long id, int flags, Object nodeInfo);
    static private native int nativeRetrieveMetadata(long hScene, long id, Object metadata);
    static private native int nativeRetrieveVEIDs(long hScene, long id, Object VEIDs);
    static private native int nativeRetrieveThumbnail(long hScene, long stepId, Object image);
    static private native int nativeRetrieveProcedures(long hScene, Object proceduresInfo);
    static private native int nativeBuildPartsList(long hScene, int maxParts, int maxNodesInSinglePart, int maxPartNameLength,
            int partsListType, int partsListSort, long idConsumedStep, java.lang.String substr, Object info);
    static private native int nativeFindNodes(long hScene, int type, int mode, java.lang.String str, Object info);
    static private native int nativeGetTotalSelectedNodesCount(long hScene);
    static private native int nativeGetVisibleSelectedNodesCount(long hScene);
    static private native int nativeGetHiddenSelectedNodesCount(long hScene);
    static private native void nativePerformAction(long hScene, int action);
    static private native int nativeActivateStep(long hScene, long id, boolean bFromTheBeginning, boolean bContinueToTheNext);
    static private native int nativePauseCurrentStep(long hScene);
    static private native int nativeChangeNodeFlags(long hScene, long nodeId, int nodeFlags, int flagOp);
    static private native int nativeSetNodeOpacity(long hScene, long nodeId, float opacity);
    static private native int nativeSetNodeHighlightColor(long hScene, long nodeId, int color);
    static private native int nativeGetNodeWorldMatrix(long hScene, long nodeId, Object mat);
    static private native int nativeSetNodeWorldMatrix(long hScene, long nodeId, Object mat);
    static private native int nativeExecute(long hScene, int type, String str);
    static private native int nativeSetDynamicLabel(long hScene, String id, String name, Object objDynamicLabel);
    static private native void nativeUpdateDynamicLabels(long hScene);
    static private native boolean nativeIsCGMScene(long hScene);
    static private native int nativeSetPOIIcon(long hScene, float width, float height, Object bitmap);
    static private native void nativeRelease(long hScene);
}
