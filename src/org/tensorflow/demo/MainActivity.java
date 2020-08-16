package org.tensorflow.demo;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button learnBtn;
    private Button bookmarkBtn;
    private Button gameBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameBtn = (Button) findViewById(R.id.btnGame);
        gameBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openGameAct();
            }
        });

        learnBtn = (Button) findViewById(R.id.button);
        learnBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openDetectorAct();
            }
        });

        bookmarkBtn = (Button) findViewById(R.id.buttonBookmark);
        bookmarkBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openBookmarkAct();
            }
        });
    }

    private void openDetectorAct() {
        Intent open = new Intent(this, ChooseLanguage.class);
        startActivity(open);
    }
    private void openBookmarkAct() {
        Intent opens = new Intent(this, Bookmark.class);
        startActivity(opens);
    }
    private void openGameAct() {
        Intent openss = new Intent(this, GameActivity.class);
        startActivity(openss);
    }

}