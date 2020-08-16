package org.tensorflow.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.List;

public class Bookmark extends AppCompatActivity {
    ListView listViewWord;

    DatabaseReference databaseWord;
    private int countWord=0;

    public static List<SavedWord> Wordlist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        databaseWord = FirebaseDatabase.getInstance().getReference("SavedWord");

        listViewWord = (ListView) findViewById(R.id.listWord);

        Wordlist = new ArrayList<>();

    }

    @Override
    protected void onStart() {
        super.onStart();
        final TextView textViews = (TextView) findViewById(R.id.saveCountWords);
        databaseWord.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot wordSnapshot: snapshot.getChildren()){
                    SavedWord savedWord = wordSnapshot.getValue(SavedWord.class);
                    Wordlist.add(savedWord);
                }
                if(snapshot.exists()){
                    countWord=(int) snapshot.getChildrenCount();
                    textViews.setText(Integer.toString(countWord) + " Saved Word");
                } else {
                    textViews.setText("0 Word");
                }

                WordList adapter = new WordList(Bookmark.this, Wordlist);
                listViewWord.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}