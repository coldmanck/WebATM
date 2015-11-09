package coldmanck.webatm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.Toast;


import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Spinner spinner;
    private Button submitButton, exitButton;
    private EditText etCash, etBankNo;
    private TextView tvMsg;
    private TextView tickTV;
    private CountDownTimer countDownTimer;
    private CheckBox cb;
    private ArrayAdapter<String> choiceList;
    private String[] info = new String[3];
    private String[] choices = {"deposit", "withdraw", "transfer", "view_history", "log_out"};
    private String nowChoice = "deposit";   // default "deposit"
    public static final String EXTRA_ACCOUNT_MESSAGE = "coldmanck.webatm.ACCOUNT";
    private ArrayList<String> cashHistory = new ArrayList<>();

    private BufferedReader in;
    private PrintWriter out;

    private String nowCash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String[] message = intent.getStringArrayExtra(LoginActivity.EXTRA_MESSAGE);
        tvMsg = (TextView) findViewById(R.id.main_text);
        for(int i = 0; i < message.length; i++) {
            tvMsg.append(message[i] + ", ");
            info[i] = message[i];
        }
        tvMsg.append("Note you cannot deposit/withdraw/transfer more than 60,000 at one time.\n");

        // I/O
        try {
            in = new BufferedReader(new InputStreamReader(LoginActivity.socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(LoginActivity.socket.getOutputStream())), true);
        }
        catch(IOException ex){
            Toast.makeText(MainActivity.this, ex.toString(), Toast.LENGTH_SHORT).show();
        }

        // Spinner
        spinner = (Spinner)findViewById(R.id.my_spinner);
        choiceList = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, choices);
        spinner.setAdapter(choiceList);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Toast.makeText(MainActivity.this, "你選的是" + choices[position], Toast.LENGTH_SHORT).show();
                nowChoice = choices[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        // Cash input
        etCash = (EditText) findViewById(R.id.cash_edittext);
        etCash.setHint(R.string.cash_hint);

        // Bank number input
        etBankNo = (EditText) findViewById(R.id.bank_number);
        etBankNo.setHint(R.string.bank_number_hint);

        // Count down
        tickTV = (TextView) findViewById(R.id.count_down);
        countDownTimer = new CountDownTimer(180000, 1000) {

            public void onTick(long millisUntilFinished) {
                tickTV.setText("After " + millisUntilFinished / 1000 + "s you will log out automatically.");
            }

            public void onFinish() {
//                moveTaskToBack(true);
                System.exit(0);
            }
        };
        countDownTimer.start();

        // Exit button
        exitButton = (Button) findViewById(R.id.exit_button);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
            }
        });

        // Submit button
        submitButton = (Button) findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countDownTimer.start();

                Thread thread = new Thread(new HandleNetwork());
                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException ex) {
                }

                if (nowChoice.equals("deposit") || nowChoice.equals("withdraw")
                        || nowChoice.equals("transfer")) {
                    if (nowCash.equals("-1"))
                        tvMsg.append("\nError!");
                    else {
                        tvMsg.append("\nNow cash: " + nowCash);
                        cashHistory.add(nowCash);
                    }
                } else if (nowChoice.equals("view_history")) {
                    Intent intent2History = new Intent(MainActivity.this, LineGraphActivity.class);
                    intent2History.putExtra(EXTRA_ACCOUNT_MESSAGE, cashHistory);
                    startActivity(intent2History);
                } else if (nowChoice.equals("log_out")) {
                    System.exit(0);
                }
            }
        });



//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    public class HandleNetwork implements Runnable{


        @Override
        public void run(){
            if(Integer.valueOf(etCash.getText().toString()) >= 60000){
                nowCash = "-1";
            }
            else if (nowChoice.equals("deposit") || nowChoice.equals("withdraw")) {
                try {
                    LoginActivity.out.println(nowChoice);
                    LoginActivity.out.println(etCash.getText());

                    nowCash = LoginActivity.in.readLine();
                }
                catch(IOException ex){}
            }
            else if(nowChoice.equals("transfer") && !etBankNo.getText().toString().equals("")){
                cb = (CheckBox) findViewById(R.id.if_internal);
                try {
                    LoginActivity.out.println(nowChoice);
                    LoginActivity.out.println(etCash.getText());
                    LoginActivity.out.println(etBankNo.getText());

                    if(cb.isChecked())
                        LoginActivity.out.println("true");
                    else
                        LoginActivity.out.println("false");

                    nowCash = LoginActivity.in.readLine();
                }
                catch(IOException ex){}
            }
            else if(nowChoice.equals("view_history")){
                try {
                    LoginActivity.out.println(nowChoice);
                    int size = Integer.valueOf(LoginActivity.in.readLine());

//                    for (int i = 0; i < size; i++)
//                        cashHistory.add(LoginActivity.in.readLine());
                }
                catch(IOException ex){ }
            }
            else if(nowChoice.equals("log_out")) {
                LoginActivity.out.println(nowChoice);
            }
        }

    }

}
