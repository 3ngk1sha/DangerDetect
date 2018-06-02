package com.unipi.vnikolis.unipismartalert.sms;

import android.annotation.SuppressLint;
import android.telephony.SmsManager;
import java.util.ArrayList;

/**
 * Αποστολή SMS
 */

@SuppressLint("Registered")
public class SendSMS {

    /**
     * Μήνυμα SOS
     * @param phones Λίστα τηλεφώνικου καταλόγου παραληπτών
     */
    public void SOS_SMS(ArrayList<String> phones, double longitude, double latitude)
    {
        String textMessage = "Βρίσκομαι στην τοποθεσία με γεωγραφικό μήκος : " + longitude + " και γεωγραφικό πλάτος : " + latitude + " και χρειάζομαι βοήθεια";
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> parts =smsManager.divideMessage(textMessage);

        for(String number : phones)
            {
                smsManager.sendMultipartTextMessage(number,null, parts, null, null);
            }
    }

    /**
     * Μήνυμα ακύρωσης
     * @param phones Λίστα τηλεφώνικου καταλόγου παραληπτών
     */
    public void ABORT_SMS(ArrayList<String> phones)
    {
        SmsManager smsManager = SmsManager.getDefault();
        String textMessage = "Άκυρος ο Συναγερμός. Όλα καλά";
        for(String number : phones)
        {
            smsManager.sendTextMessage(number,null,textMessage,null,null);
        }
    }
}
