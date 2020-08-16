package org.tensorflow.demo;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class ChooseLanguage extends AppCompatActivity {
    String SYMBOL;
    public static String[] languages={"Select Language", "Afrikaans", "Albanian", "Amharic", "Arabic", "Armenian", "Azerbaijani",
            "Basque", "Belarusian", "Bengali", "Bosnian", "Bulgarian",
            "Catalan", "Cebuano", "Chinese (simplified)", "Chinese (Traditional)", "Corsican", "Croatian","Czech",
            "Danish", "Dutch",
            "English", "Esperanto", "Estonian",
            "Finnish", "French", "Frisian",
            "Galician", "Georgian", "German", "Greek", "Gujarati",
            "Haitian Creole", "Hausa", "Hawaiian", "Hebrew", "Hindi", "Hmong", "Hungarian",
            "Icelandic", "Igbo", "Indonesian", "Irish", "Italian",
            "Japanese", "Javanese",
            "Kannada", "Kazakh", "Khmer", "Kinyarwanda", "Korean", "Kurdish", "Kyrgyz",
            "Lao", "Latin", "Latvian","Lithuanian", "Luxembourgish",
            "Macedonian", "Malagsy", "Malay", "Malayalam", "Maltese", "Maori", "Marathi", "Mongolian", "Myanmar(Burmese)",
            "Nepali", "Norwegian","Nyanja(Chichewa)",
            "Odia(Oriya)",
            "Pashto","Persian","Polish","Portugese(Portugal,Brazil)", "Punjabi",
            "Romanian", "Russian",
            "Samoan", "Scots Gaelic", "Serbian", "Sesotho", "Shona", "Sindhi", "Sinhala(Sinhalese)", "Slovak", "Slovenian", "Somali", "Spanish", "Sundanese", "Swahili", "Swedish",
            "Tagalog(Filipino)", "Tajik", "Tamil", "Tatar", "Telugu", "Thai", "Turkish", "Turkmen",
            "Ukranian", "Urdu", "Uyghur", "Uzbek",
            "Vietnamese",
            "Welsh",
            "Xhosa",
            "Yiddish","Yoruba",
            "Zulu"
    };
    public static String[] langsym={"","af", "sq", "am", "ar", "hy", "az",
            "eu", "be", "bn", "bs", "bg",
            "ca", "ceb", "zh", "zh-TW", "co", "hr","cs",
            "da", "nl",
            "en", "eo", "et",
            "fi", "fr", "fy",
            "gl", "ka", "de", "el", "gu",
            "ht", "ha", "haw", "he", "hi", "hmn", "hu",
            "is", "ig", "id", "ga", "it",
            "ja", "jv",
            "kn", "kk", "km", "rw", "ko", "ku", "ky",
            "lo", "la", "lv","lt", "lb",
            "mk", "mg", "ms", "ml", "mt", "mi", "mr", "mn", "my",
            "ne", "no","ny",
            "or",
            "ps","fa","pl","pt", "pa",
            "ro", "ru",
            "sm", "gd", "sr", "st", "sn", "sd", "si", "sk", "sl", "so", "es", "su", "sw", "sv",
            "tl", "tg", "ta", "tt", "te", "th", "tr", "tk",
            "uk", "ur", "ug", "uz",
            "vi",
            "cy",
            "xh",
            "yi","yo",
            "zu"
    };
    public static String NameStrings;
    public static String chosenLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_language);


        final Spinner spinner=findViewById(R.id.spinner);
        ArrayAdapter<String> languageAdapter= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(languageAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                              @Override
                                              public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                 if(adapterView.getItemAtPosition(i).equals("Select Language"))
                                                 {
                                                     Toast.makeText(adapterView.getContext(), "Please Choose a Language", Toast.LENGTH_LONG).show();

                                                 } else {
                                                     Toast.makeText(adapterView.getContext(), "Selected Language: " + languages[i], Toast.LENGTH_LONG).show();
                                                     SYMBOL = langsym[i];
                                                     NameStrings = SYMBOL;
                                                     chosenLanguage = languages[i];
                                                     Button learnBtns = (Button) findViewById(R.id.button2);

                                                     learnBtns.setOnClickListener(new View.OnClickListener(){
                                                         @Override
                                                         public void onClick(View view) {
                                                             openDetectorAct();
                                                         }
                                                     });
                                                 }

                                              }

                                              @Override
                                              public void onNothingSelected(AdapterView<?> adapterView) {

                                              }
                                          });


    }

    private void openDetectorAct() {
        Intent opens = new Intent(this, DetectorActivity.class);
        startActivity(opens);
    }

    public static String getC2() {
        String c2;
        c2 = NameStrings;
        return c2;
    }

    public static String getC3(){
        String c3;
        c3 = chosenLanguage;
        return c3;
    }

}