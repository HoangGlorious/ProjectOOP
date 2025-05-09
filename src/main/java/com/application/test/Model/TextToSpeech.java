package com.application.test.Model;


import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class TextToSpeech {
    // Hàm TextToSpeech sử dụng Google Translate API
    public static void speak(String text) throws Exception {
        try {
            //Encode từ thành URL để thuận tiện cho việc gọi api.
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String urlStr = "https://translate.google.com/translate_tts?ie=UTF-8&tl=en&client=tw-ob&q=" + encodedText;

            //Tải xuống file audio thành 1 file tạm thời.
            File tmpFile = File.createTempFile("tts", ".wav");
            tmpFile.deleteOnExit();


            try (InputStream in = new URL(urlStr).openStream()) {
                Files.copy(in, tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // Chạy file audio bằng JavaFX MediaPlayer.
            Media media = new Media(tmpFile.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnError(() -> {
                System.err.println("MediaPlayer error: " + mediaPlayer.getError().getMessage());
            });
            mediaPlayer.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}