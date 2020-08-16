/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.demo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.tensorflow.demo.OverlayView.DrawCallback;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.tracking.MultiBoxTracker;
import org.tensorflow.demo.tracking.ObjectTracker;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {

  public TextView translatedTv;
  private String originalText;
  public static String translatedText;
  Translate translate;
  Button SavedWord;

  DatabaseReference databaseWord;

  private static final Logger LOGGER = new Logger();



  private static final int TF_OD_API_INPUT_SIZE = 300;
  private static final String TF_OD_API_MODEL_FILE =
      "file:///android_asset/ssd_mobilenet_v1_android_export.pb";
  private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco_labels_list.txt"; ///////////////LABELLLLL


  // Which detection model to use: by default uses Tensorflow Object Detection API frozen

  // or YOLO.
  private enum DetectorMode {
    TF_OD_API;
  }
  private static final DetectorMode MODE = DetectorMode.TF_OD_API;

  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.6f;
  private static final float MINIMUM_CONFIDENCE_MULTIBOX = 0.1f;


  private static final boolean MAINTAIN_ASPECT = MODE == DetectorMode.TF_OD_API;

  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;

  private Integer sensorOrientation;

  private Classifier detector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;

  private byte[] luminanceCopy;

  boolean startCapture = false;
  Button btConvert;
  TextToSpeech textToSpeech;
  public static String Namestringsss;
  public static String chosenLanguagee;
  public static String apiLanguagee;


  private BorderedText borderedText;
  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);

    int cropSize = TF_OD_API_INPUT_SIZE;
   if (MODE == DetectorMode.TF_OD_API) {
      try {
        detector = TensorFlowObjectDetectionAPIModel.create(
            getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        cropSize = TF_OD_API_INPUT_SIZE; //CLASSIFICATION
      } catch (final IOException e) {
        LOGGER.e(e, "Exception initializing classifier!");
        Toast toast =
            Toast.makeText(
                getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
        toast.show();
        finish();
      }
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

    frameToCropTransform =
        ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            cropSize, cropSize,
            sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
        new DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            tracker.draw(canvas);
            if (isDebug()) {
              tracker.drawDebug(canvas);
            }
          }
        });

    addCallback(
        new DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            if (!isDebug()) {
              return;
            }
            final Bitmap copy = cropCopyBitmap;
            if (copy == null) {
              return;
            }

            final int backgroundColor = Color.argb(100, 0, 0, 0);
            canvas.drawColor(backgroundColor);

            final Matrix matrix = new Matrix();
            final float scaleFactor = 2;
            matrix.postScale(scaleFactor, scaleFactor);
            matrix.postTranslate(
                canvas.getWidth() - copy.getWidth() * scaleFactor,
                canvas.getHeight() - copy.getHeight() * scaleFactor);
            canvas.drawBitmap(copy, matrix, new Paint());

          }
        });
  }

  public void Capture(View Button){
    if(!startCapture){
      startCapture = true;
    } else {
      startCapture = false;
    }

  }

  OverlayView trackingOverlay;

  @Override
  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    byte[] originalLuminance = getLuminance();

    SavedWord = (Button) findViewById(R.id.save);
    databaseWord = FirebaseDatabase.getInstance().getReference("SavedWord");
    SavedWord.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addWord();
      }
    });


    translatedTv = (TextView) findViewById(R.id.translatedTv);

    tracker.onFrame(
        previewWidth,
        previewHeight,
        getLuminanceStride(),
        sensorOrientation,
        originalLuminance,
        timestamp);
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;
    LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    if (luminanceCopy == null) {
      luminanceCopy = new byte[originalLuminance.length];
    }
    System.arraycopy(originalLuminance, 0, luminanceCopy, 0, originalLuminance.length);
    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }


    runInBackground(
        new Runnable() {
          @Override
          public void run() {
            textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
              @Override
              public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                  int lang = textToSpeech.setLanguage(Locale.getDefault());
                }

              }
            });
            LOGGER.i("Running detection on image " + currTimestamp);

            final long startTime = SystemClock.uptimeMillis();
            final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);

            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
            final Canvas canvas = new Canvas(cropCopyBitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(2.0f);

            float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
            switch (MODE) {
              case TF_OD_API:
                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                break;

            }

            final List<Classifier.Recognition> mappedRecognitions =
                new LinkedList<Classifier.Recognition>();

            for (final Classifier.Recognition result : results) {
              final RectF location = result.getLocation();
              if (location != null && result.getConfidence() >= minimumConfidence) {
                canvas.drawRect(location, paint);

                cropToFrameTransform.mapRect(location);
                result.setLocation(location);
                if(startCapture == true) {

                  StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                  StrictMode.setThreadPolicy(policy);

                  try (InputStream is = getResources().openRawResource(R.raw.credentials)) {

                    //Get credentials:
                    final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);

                    //Set credentials and get translate service:
                    TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build();
                    translate = translateOptions.getService();

                  } catch (IOException ioe) {
                    ioe.printStackTrace();

                  }
                  String s3 = ChooseLanguage.getC3();
                  String s2 = ChooseLanguage.getC2();
                  String s1 = TensorFlowObjectDetectionAPIModel.getC1();
                  Namestringsss = s1;
                  apiLanguagee = s2;
                  chosenLanguagee = s3;
                  Translation translation = translate.translate(s1, Translate.TranslateOption.targetLanguage(s2), Translate.TranslateOption.model("base"));
                  translatedText = translation.getTranslatedText();


                  //Translated text and original text are set to TextViews:
                  translatedTv.setText(translatedText);
                  //LOGGER.i("PAPAMOOOOO " + SYMBOL);
                  
                  mappedRecognitions.add(result);
                  Button customButton = findViewById(R.id.custom_button);
                  customButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      Toast.makeText(DetectorActivity.this, "click", Toast.LENGTH_LONG).show();
                    }
                  });

                  btConvert = findViewById(R.id.custom_button);
                  btConvert.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                      String s1 = TensorFlowObjectDetectionAPIModel.getC1();
                      int speech = textToSpeech.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null);

                    }
                  });


                }

              }

            }

            tracker.trackResults(mappedRecognitions, luminanceCopy, currTimestamp);

            trackingOverlay.postInvalidate();


            requestRender();
            computingDetection = false;
          }
        });

  }



  private void addWord(){

    final String wordSaved = Namestringsss; //english word saved to database
    final String id = databaseWord.push().getKey(); // id
    final String language = chosenLanguagee;
    final String languageApi = apiLanguagee;

    if(wordSaved != null && language != null){
      databaseWord.addListenerForSingleValueEvent(new ValueEventListener() {

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {

          if (snapshot.exists()) {
            Toast.makeText(DetectorActivity.this, "Translated word already saved to Bookmark", Toast.LENGTH_LONG).show();

          }else {
            SavedWord savedword = new SavedWord(id, wordSaved, language, languageApi);// saving to database
            databaseWord.child(id).setValue(savedword);
            Toast.makeText(DetectorActivity.this, "Word Added", Toast.LENGTH_LONG).show();

          }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
      });

    } else {
      Toast.makeText(DetectorActivity.this, "Focus your Camera to Identify Object", Toast.LENGTH_LONG).show();
    }


  }

  public static String getTranslatedText() {
    String c1;
    c1 = translatedText;
    return c1;
  }



  @Override
  protected int getLayoutId() {
    return R.layout.camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  @Override
  public void onSetDebug(final boolean debug) {
    detector.enableStatLogging(debug);
  }
}
