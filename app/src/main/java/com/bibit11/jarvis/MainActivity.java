package com.bibit11.jarvis;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.graphics.Color;
import java.util.*;
import java.text.SimpleDateFormat;

public class MainActivity extends Activity {
    LinearLayout root;
    TextView output, status;
    Button mic;
    SpeechRecognizer recognizer;
    TextToSpeech tts;
    boolean ttsReady = false;

    int bg = Color.rgb(12,12,16);
    int green = Color.rgb(0,255,136);

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24,36,24,24);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(bg);
        setContentView(root);

        TextView title = new TextView(this);
        title.setText("J.A.R.V.I.S");
        title.setTextColor(green);
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);
        title.setLetterSpacing(0.15f);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("PERSONAL AI ASSISTANT");
        sub.setTextColor(Color.LTGRAY);
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub);

        status = new TextView(this);
        status.setText("● SYSTEM ONLINE");
        status.setTextColor(green);
        status.setTextSize(14);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0,40,0,20);
        root.addView(status);

        output = new TextView(this);
        output.setText("Welcome, Boss.\n\nI am JARVIS. Tap the microphone and speak.");
        output.setTextColor(Color.WHITE);
        output.setTextSize(18);
        output.setGravity(Gravity.CENTER);
        output.setPadding(16,30,16,30);
        root.addView(output, new LinearLayout.LayoutParams(-1,0,1));

        mic = new Button(this);
        mic.setText("🎙  TALK TO JARVIS");
        mic.setTextSize(16);
        mic.setTextColor(Color.BLACK);
        mic.setBackgroundTintList(android.content.res.ColorStateList.valueOf(green));
        root.addView(mic, new LinearLayout.LayoutParams(-1,60));
        mic.setOnClickListener(v -> listen());

        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                ttsReady = true;
                tts.setLanguage(Locale.getDefault());
            }
        });

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new android.speech.RecognitionListener() {
                public void onReadyForSpeech(Bundle b) { status.setText("● LISTENING..."); }
                public void onBeginningOfSpeech() {}
                public void onRmsChanged(float r) {}
                public void onBufferReceived(byte[] b) {}
                public void onEndOfSpeech() { status.setText("● PROCESSING..."); }
                public void onError(int e) {
                    status.setText("● READY");
                    output.setText("Couldn't hear that. Please try again.");
                }
                public void onResults(Bundle b) {
                    ArrayList<String> words = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (words != null && !words.isEmpty()) respond(words.get(0));
                    status.setText("● SYSTEM ONLINE");
                }
                public void onPartialResults(Bundle b) {}
                public void onEvent(int a, Bundle b) {}
            });
        }
    }

    void listen() {
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 10);
            return;
        }
        if (recognizer == null) {
            output.setText("Speech recognition is not available on this device.");
            return;
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to JARVIS");
        recognizer.startListening(intent);
    }

    void respond(String command) {
        String q = command.toLowerCase(Locale.ROOT);
        String answer;

        if (q.contains("hello") || q.contains("hi jarvis")) {
            answer = "Hello Boss. How can I help you?";
        } else if (q.contains("your name") || q.contains("who are you")) {
            answer = "I am JARVIS, your personal assistant.";
        } else if (q.contains("time")) {
            answer = "The time is " + new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        } else if (q.contains("date") || q.contains("day")) {
            answer = "Today is " + new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(new Date());
        } else if (q.contains("youtube")) {
            answer = "Opening YouTube, Boss.";
            openUrl("https://www.youtube.com");
        } else if (q.contains("google")) {
            answer = "Opening Google, Boss.";
            openUrl("https://www.google.com");
        } else if (q.contains("how are you")) {
            answer = "I am working perfectly, Boss.";
        } else if (q.contains("thank")) {
            answer = "You're welcome, Boss.";
        } else {
            answer = "You said: " + command + ". I am ready for your next command.";
        }

        output.setText("YOU: " + command + "\n\nJARVIS: " + answer);
        if (ttsReady) tts.speak(answer, TextToSpeech.QUEUE_FLUSH, null, "jarvis_reply");
    }

    void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)));
        } catch (Exception e) {
            output.setText("Unable to open link.");
        }
    }

    @Override
    public void onDestroy() {
        if (recognizer != null) recognizer.destroy();
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }
}
