package com.developer.abhinav_suthar.gallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("About");

        functions();
    }

    private void functions(){

        CardView feedback = findViewById(R.id.about_feedback);
        CardView improve = findViewById(R.id.about_improve);
        CardView aboutMe = findViewById(R.id.about_about_me);
        ImageView call = findViewById(R.id.about_callMe);
        ImageView whatsApp = findViewById(R.id.about_whatsAppMe);

        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mail = new Intent(Intent.ACTION_SENDTO);
                mail.putExtra(Intent.EXTRA_EMAIL, new String[]{"abhinav.suthar.50@gmail.com"});
                mail.putExtra(Intent.EXTRA_SUBJECT, "Gallery App FeedBack");
                mail.putExtra(Intent.EXTRA_TEXT, "Hi Abhinav\nI will tell you something about your app.\n");
                mail.setData(Uri.parse("mailto:abhinav.suthar.50@gmail.com"));
                startActivity(Intent.createChooser(mail, "Send with Gmail"));
            }
        });
        improve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mail = new Intent(Intent.ACTION_SENDTO);
                mail.putExtra(Intent.EXTRA_EMAIL, new String[]{"abhinav.suthar.50@gmail.com"});
                mail.putExtra(Intent.EXTRA_SUBJECT, "Gallery App Suggestion");
                mail.putExtra(Intent.EXTRA_TEXT, "Hi Abhinav\nHere is my suggestion/improvement\n");
                mail.setData(Uri.parse("mailto:abhinav.suthar.50@gmail.com"));
                startActivity(Intent.createChooser(mail, "Send with Gmail"));
            }
        });
        aboutMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(About.this, R.style.AnimDialog);
                builder.setTitle("About Me ! ! !")
                        .setMessage("My name is Abhinav Suthar\nI am studying at IIT Jodhpur\n" +
                                "abhinav.suthar.50@gmail.com\n+917240505099")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+917240505099")));
            }
        });
        whatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=917240505099")));
            }
        });

        findViewById(R.id.about_tool_whatsAppMe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = findViewById(R.id.about_tool_whatsAppMe_num);
                String s = "https://api.whatsapp.com/send?phone=" + textView.getText().toString();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(s)));
            }
        });
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ((item.getItemId())==(android.R.id.home)) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
