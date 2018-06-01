package com.unipi.vnikolis.unipismartalert;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The backend code for Login Activity
 */
public class LoginForm extends AppCompatActivity {
    Database myDb;
    TextView UserName, Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);

        UserName = findViewById(R.id.UserName);
        Password = findViewById(R.id.Password);
        myDb = new Database(this);
    }

    /**
     * Ταυτοποίηση του τρέχων χρήστη
     * @param view view
     */
    public void login(View view)
    {
        //διαβασε απο τη βάση τα στοιχεία
        Cursor res = myDb.getUser(UserName.getText().toString(), Password.getText().toString());
        if(res.getCount() <= 0) //εαν δεν υπάρχει χρήστης
        {
            Toast.makeText(LoginForm.this, "Λάθος Στοιχεία", Toast.LENGTH_LONG).show();
        }
        else //εαν βρεθεί ο χρήστης
        {
            SendSMS sms = new SendSMS();
            sms.ABORT_SMS(myDb.getPhones());
            Toast.makeText(LoginForm.this, "Το μήνυμα εστάλλει επιτυχώς", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(LoginForm.this, MainActivity.class);
            startActivity(intent);
        }
    }
}
