package com.lethe_river.dokusyonow;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import android.util.Base64;
//import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class AmazonAPI {

    private static final String UTF8_CHARSET = "UTF-8";
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String REQUEST_URI = "/onca/xml";
    private static final String REQUEST_METHOD = "GET";
    private static final String endpoint = "ecs.amazonaws.jp";

    private static String awsAccessKeyId;
    private static String awsSecretKey;
    private static String associateTag;

    public static void setKeys(String accessKeyId, String secretKey, String associateTag) {
        AmazonAPI.awsAccessKeyId = accessKeyId;
        AmazonAPI.awsSecretKey = secretKey;
        AmazonAPI.associateTag = associateTag;
    }

    public static Document getDocument(String title) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("AssociateTag", associateTag);
        params.put("IdType", "ISBN");
        params.put("Service", "AWSECommerceService");
        params.put("Operation", "ItemSearch");
        params.put("Title", title);
        params.put("ResponseGroup", "Large");
        params.put("SearchIndex", "Books");

        return getDocument(params);
    }

    public static Document getDocument(long isbn) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("AssociateTag", associateTag);
        params.put("IdType", "ISBN");
        params.put("Service", "AWSECommerceService");
        params.put("Operation", "ItemLookup");
        params.put("ItemId", Long.toString(isbn));
        params.put("ResponseGroup", "Large");
        params.put("SearchIndex", "Books");

        return getDocument(params);
    }

    private static Document getDocument(Map<String, String> params) {
        Document doc = null;

        String requestUrl = null;

        requestUrl = sign(params);
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection urlConn = (HttpURLConnection) url
                    .openConnection();
            urlConn.setRequestMethod("GET");
            urlConn.setInstanceFollowRedirects(false);
            urlConn.setRequestProperty("Accept-Language", "ja;q=0.7,en;q=0.3");

            urlConn.connect();

            DocumentBuilder documentBuilder = DocumentBuilderFactory
                    .newInstance().newDocumentBuilder();
            doc = documentBuilder.parse(urlConn.getInputStream());
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
        return doc;
    }

    public static String sign(Map<String, String> params) {
        params.put("AWSAccessKeyId", awsAccessKeyId);
        params.put("Timestamp", timestamp());

        SortedMap<String, String> sortedParamMap = new TreeMap<String, String>(
                params);
        String canonicalQS = canonicalize(sortedParamMap);
        String toSign = REQUEST_METHOD + "\n" + endpoint + "\n" + REQUEST_URI
                + "\n" + canonicalQS;

        String hmac = hmac(toSign);
        String sig = percentEncodeRfc3986(hmac);
        String url = "http://" + endpoint + REQUEST_URI + "?" + canonicalQS
                + "&Signature=" + sig;

        return url;
    }

    private static String hmac(String stringToSign) {
        String signature = null;
        byte[] data;
        byte[] rawHmac;
        try {
            data = stringToSign.getBytes(UTF8_CHARSET);
            byte[] secretyKeyBytes = awsSecretKey.getBytes(UTF8_CHARSET);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretyKeyBytes,
                    HMAC_SHA256_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(secretKeySpec);
            rawHmac = mac.doFinal(data);
//          Base64 encoder = new Base64(0);
//          signature = new String(encoder.encode(rawHmac));
            signature = new String(Base64.encode(rawHmac, Base64.DEFAULT));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(UTF8_CHARSET + " is unsupported!", e);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return signature;
    }

    private static String timestamp() {
        String timestamp = null;
        Calendar cal = Calendar.getInstance();
        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dfm.setTimeZone(TimeZone.getTimeZone("GMT"));
        timestamp = dfm.format(cal.getTime());
        return timestamp;
    }

    private static String canonicalize(SortedMap<String, String> sortedParamMap) {
        if (sortedParamMap.isEmpty()) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        Iterator<Map.Entry<String, String>> iter = sortedParamMap.entrySet()
                .iterator();

        while (iter.hasNext()) {
            Map.Entry<String, String> kvpair = iter.next();
            buffer.append(percentEncodeRfc3986(kvpair.getKey()));
            buffer.append("=");
            buffer.append(percentEncodeRfc3986(kvpair.getValue()));
            if (iter.hasNext()) {
                buffer.append("&");
            }
        }
        String cannoical = buffer.toString();
        return cannoical;
    }

    private static String percentEncodeRfc3986(String s) {
        String out;
        try {
            out = URLEncoder.encode(s, UTF8_CHARSET).replace("+", "%20")
                    .replace("*", "%2A").replace("%7E", "~");
            out = out.replace("%0A", ""); // なぜか改行が入る
        } catch (UnsupportedEncodingException e) {
            out = s;
        }
        return out;
    }
}
