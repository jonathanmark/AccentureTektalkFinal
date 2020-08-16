package org.tensorflow.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.tensorflow.demo.env.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameMechanicsActivity extends AppCompatActivity {
    private static final Logger LOGGER = new Logger();
    DatabaseReference databaseWord;
    public static List<String> Wordlist;
    final Random randomGenerator = new Random();
    public static String random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mechanics);
        databaseWord = FirebaseDatabase.getInstance().getReference("SavedWord");

        Wordlist = new ArrayList<>();


    }

    @Override
    protected void onStart() {
        super.onStart();
        final TextView textView = (TextView) findViewById(R.id.viewQuestion);
        databaseWord.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot wordSnapshot: snapshot.getChildren()){
                    String wS = wordSnapshot.child("wordSaved").getValue(String.class);
                    Wordlist.add(wS);

                }
                random = Wordlist.get(randomGenerator.nextInt(Wordlist.size()));

                textView.setText(random);
                LOGGER.i("NAMEEEEEEEEEEEEEEE " + random);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }



}