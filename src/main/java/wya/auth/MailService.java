package wya.auth;

import wya.Util;
import wya.WyaLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

public class MailService {

    private static final String API_KEY = "key-828ab5648bb660b4216df72c4a0ef3e8";
    private static final String urlBase = "https://api.mailgun.net/v3/sandbox1f92deba576143d78338ccb7e14c2cbf.mailgun.org/messages";

    private MailService() {
    }

    public static void sendAuthEmail(String displayName, String pin) throws IOException, InterruptedException {
        String email = displayName + "@mit.edu";
        WyaLogger.d("auth code is " + pin);

        sendEmail(email, "Your Wya Confirmation Code", "Hi " + displayName + "! Your Wya verification code is: \n" + pin + "\n\nIf you run into any issues, feel free to email us at wya.devs@gmail.com.");
        WyaLogger.d("Sent mail successfully to " + email);
    }

    public static void sendEmail(String emailAddress, String subject, String content) throws IOException, InterruptedException {
        new File("temp").mkdirs();
        File emailFile = new File("temp", UUID.randomUUID().toString());

        FileOutputStream stream = new FileOutputStream(emailFile);
        PrintWriter writer = new PrintWriter(stream);

        writer.write("From: \"The Wya Team\" <wya.devs@gmail.com>\n" +
                "To: <" + emailAddress + ">\n" +
                "Subject: " + subject + "\n" +
                "\n" + content);

        writer.flush();
        stream.flush();
        stream.close();

        Process pr = Util.exec("curl --url 'smtps://smtp.gmail.com:465' --ssl-reqd " +
                "  --mail-from 'wya.devs@gmail.com' --mail-rcpt '" + emailAddress + "' " +
                "  --upload-file " + emailFile.getAbsolutePath() + " --user 'wya.devs@gmail.com:Whereyouat?'");

        int result = pr.waitFor();

        emailFile.delete();


        if (result != 0) {
            WyaLogger.d("failed to deliver mail! " + emailAddress);
            //throw new RuntimeException("failed to deliver mail");
        }
    }
}
