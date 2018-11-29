package hu.zsoltii.android.nfc;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import hu.zsoltii.android.nfc.service.nfc.NfcService;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

@EActivity(R.layout.nfc)
public class NfcActivity extends Activity {

    public static final String MIME_TEXT_PLAIN = "text/plain";

    NfcAdapter nfcAdapter;

    @ViewById
    TextView explanation;

    @ViewById
    LinearLayout nfcRecordsView;

    @Bean
    NfcService nfcService;

    @StringRes
    String nfcNotSupported;

    @AfterViews
    void afterViews() {

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, nfcNotSupported, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!nfcAdapter.isEnabled()) {
            explanation.setText(R.string.nfcDisabled);
        } else {
            explanation.setText(R.string.explanation);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(getClass().getSimpleName(), "IntentAction (onNewIntent): " + getIntent().getAction());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getClass().getSimpleName(), "onResume: action-" + getIntent().getAction());
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction()) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()) && MIME_TEXT_PLAIN.equals(getIntent().getType())) {
            nfcService.readNfcData(getIntent(), nfcRecordsView);
        }
    }
}
