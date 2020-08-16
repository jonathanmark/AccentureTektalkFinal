package org.tensorflow.demo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class WordList extends ArrayAdapter<SavedWord> {

    private Activity context;
    private List<SavedWord> wordList;
    Translate translate;
    DatabaseReference databaseWord;
    public String translatedText;
    public String transtext;
    public static String f1;


    public WordList(Activity context, List<SavedWord> wordList)
    {
        super(context, R.layout.activity_list_layout, wordList);
        this.context = context;
        this.wordList = wordList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.activity_list_layout, null, true);
        Button btnDelete = (Button) listViewItem.findViewById(R.id.buttondelete);
        TextView textviewWord = (TextView) listViewItem.findViewById(R.id.textViewWord);
        TextView textviewLang = (TextView) listViewItem.findViewById(R.id.textViewLanguage);

        databaseWord = FirebaseDatabase.getInstance().getReference("SavedWord");



        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try (InputStream is = getContext().getResources().openRawResource(R.raw.credentials)) {

            //Get credentials:
            final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);

            //Set credentials and get translate service:
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build();
            translate = translateOptions.getService();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        SavedWord savedWord = wordList.get(position);

        String s1 = savedWord.getWordSaved();
        f1 = s1;
        String s2 = savedWord.getApiLanguage();
        final String wordID = savedWord.getWordId();
        Translation translation = translate.translate(s1, Translate.TranslateOption.targetLanguage(s2), Translate.TranslateOption.model("base"));

        translatedText = translation.getTranslatedText();
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteWord(wordID);

                context.recreate();

            }
        });

        textviewWord.setText(translatedText + " - " + s1);
        textviewLang.setText("Language: "+ savedWord.getLanguageSaved());



        return listViewItem;
    }

    private void deleteWord(String wordID) {

        DatabaseReference deleteWord = FirebaseDatabase.getInstance().getReference("SavedWord").child(wordID);

        deleteWord.removeValue();

        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.activity_list_layout, null, true);


        Toast.makeText(context, "Word Deleted", Toast.LENGTH_SHORT).show();

    }

    public static String getF1() {
        return f1;
    }
}
