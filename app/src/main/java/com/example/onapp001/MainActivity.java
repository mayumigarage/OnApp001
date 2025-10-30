package com.example.onapp001;


import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;

import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {


    private GeckoSession session;

    private EditText urlBar;
    private ImageButton backButton;
    private ImageButton forwardButton;

    private final List<String> historyList = new ArrayList<>();
    private int currentIndex = -1;

    private String currentUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GeckoView geckoView = findViewById(R.id.geckoView);
        urlBar = findViewById(R.id.urlBar);
        backButton = findViewById(R.id.backButton);
        forwardButton = findViewById(R.id.forwardButton);

        GeckoRuntime runtime = GeckoRuntime.create(this);
        session = new GeckoSession();

        // 🦊 ProgressDelegate を設定
        session.setProgressDelegate(new GeckoSession.ProgressDelegate() {

            @Override
            public void onPageStart(@NonNull GeckoSession session, @NonNull String url) {
                currentUrl = url;
                updateHistory(url);
                runOnUiThread(() -> urlBar.setText(url));
            }

            @Override
            public void onPageStop(@NonNull GeckoSession session, boolean success) {
                // ページ読み込み完了時にも呼ばれる
            }

            @Override
            public void onProgressChange(@NonNull GeckoSession session, int progress) {
                // 読み込み進行中
            }
        });

        session.open(runtime);
        geckoView.setSession(session);

        loadUrl("https://www.google.com");

        backButton.setOnClickListener(v -> {
            if (canGoBack()) {
                currentIndex--;
                loadUrl(historyList.get(currentIndex));
                updateNavButtons();
            }
        });

        forwardButton.setOnClickListener(v -> {
            if (canGoForward()) {
                currentIndex++;
                loadUrl(historyList.get(currentIndex));
                updateNavButtons();
            }
        });

        urlBar.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                String url = urlBar.getText().toString();
                if (!url.startsWith("http")) {
                    url = "https://" + url;
                }
                loadUrl(url);
                return true;
            }
            return false;
        });
    }

    private void loadUrl(String url) {
        session.loadUri(url);
    }

    private void updateHistory(String url) {
        if (currentIndex == -1 || !url.equals(historyList.get(currentIndex))) {
            while (historyList.size() > currentIndex + 1) {
                historyList.remove(historyList.size() - 1);
            }
            historyList.add(url);
            currentIndex = historyList.size() - 1;
        }
        updateNavButtons();
    }

    private boolean canGoBack() {
        return currentIndex > 0;
    }

    private boolean canGoForward() {
        return currentIndex < historyList.size() - 1;
    }

    private void updateNavButtons() {
        backButton.setEnabled(canGoBack());
        forwardButton.setEnabled(canGoForward());
    }





    @Override
    protected void onStart() {
        super.onStart();
        session.setActive(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        session.setActive(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        session.close();
    }
}
