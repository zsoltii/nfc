package hu.zsoltii.android.nfc.service.nfc;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.androidannotations.annotations.EBean;

@EBean
public class NfcService {

    private static final char[] HEXARRAY = "0123456789ABCDEF".toCharArray();

    public void readNfcData(Intent intent, LinearLayout nfcRecordsView) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        nfcRecordsView.removeAllViews();
        serial(intent, nfcRecordsView);

        String[] techList = tag.getTechList();
        for (int i = 0; i < techList.length; i++) {
            String tech = techList[i];

            if ("android.nfc.tech.IsoDep".equals(tech)) {
                isoDep(tag, nfcRecordsView);
            }
            if ("android.nfc.tech.NfcA".equals(tech)) {
                nfcA(tag, nfcRecordsView);
            }
            if ("android.nfc.tech.Ndef".equals(tech)) {
                nfcNdefMessages(tag, intent, nfcRecordsView);
            }
            //TODO other nfc tech support
        }
    }

    private void serial(Intent intent, LinearLayout nfcRecordsView) {
        StringBuilder builder = new StringBuilder();
        builder.append("serial number: ");
        builder.append(bytesToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)));
        TextView textView = new TextView(nfcRecordsView.getContext());
        textView.setText(builder.toString());
        nfcRecordsView.addView(textView);
    }

    private void nfcA(Tag tag, LinearLayout nfcRecordsView) {
        NfcA nfcA = NfcA.get(tag);
        StringBuilder builder = new StringBuilder();
        builder.append("ATQA: ");
        builder.append(bytesToHexString(nfcA.getAtqa()));
        builder.append("\nSAK: ");
        builder.append(nfcA.getSak());
        TextView textView = new TextView(nfcRecordsView.getContext());
        textView.setText(builder.toString());
        nfcRecordsView.addView(textView);
    }

    private void isoDep(Tag tag, LinearLayout nfcRecordsView) {
        IsoDep isoDep = IsoDep.get(tag);
        StringBuilder builder = new StringBuilder();
        if (isoDep.getHiLayerResponse() != null) {
            builder.append("High layer response: ");
            builder.append(new String(isoDep.getHiLayerResponse()));
        }
        if (isoDep.getHistoricalBytes() != null) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append("Historical bytes: ");
            builder.append(bytesToHexString(isoDep.getHistoricalBytes()));
        }
        if (builder.length() > 0) {
            TextView textView = new TextView(nfcRecordsView.getContext());
            textView.setText(builder.toString());
            nfcRecordsView.addView(textView);
        }
    }

    private void nfcNdefMessages(Tag tag, Intent intent, LinearLayout nfcRecordsView) {
        Ndef ndef = Ndef.get(tag);
        NdefMessage ndefMessage = ndef.getCachedNdefMessage();

        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord ndefRecord : records) {
            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                try {
                    StringBuilder builder = new StringBuilder();
                    builder.append("serial number: ");
                    builder.append(bytesToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)));
                    builder.append("\npayload: ");
                    builder.append(readNdefText(ndefRecord));
                    TextView textView = new TextView(nfcRecordsView.getContext());
                    textView.setText(builder.toString());
                    nfcRecordsView.addView(textView);
                } catch (UnsupportedEncodingException e) {
                    Log.e(getClass().getSimpleName(), "Unsupported Encoding", e);
                }
            }
        }
    }

    private static String readNdefText(NdefRecord record) throws UnsupportedEncodingException {
        byte[] payload = record.getPayload();
        String characterEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0063;
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, characterEncoding);
    }

    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEXARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEXARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
