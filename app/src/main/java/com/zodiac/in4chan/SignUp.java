package com.zodiac.in4chan;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.threeten.bp.Period.between;

public class SignUp extends AppCompatActivity {

    protected static LocalDate date,today;
    protected Button SignUp;
    protected TextView age;
    protected TextInputEditText Email,Pass,C_Pass,username;
    protected TextInputLayout til_email,til_username,til_password,til_cpassword;
    protected String email,pass,c_pass,user_name;
    protected static boolean REQUEST_ACCEPTED = true, REQUEST_DENIED=false;
    protected CheckBox Terms;
    protected FirebaseAuth mAuth;
    protected FirebaseFirestore db;
    protected CollectionReference cr;
    protected ProgressBar pb_s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        cr = db.collection("username");

        SignUp = findViewById(R.id.register);
        age = findViewById(R.id.age);
        Email = findViewById(R.id.email);
        Pass = findViewById(R.id.pass);
        C_Pass = findViewById(R.id.pass1);
        username = findViewById(R.id.username);
        Terms = findViewById(R.id.TermsConditions);
        pb_s = findViewById(R.id.pb_sign);
        pb_s.setVisibility(View.GONE);

        //TextInputLayout
        til_email = findViewById(R.id.til_email);
        til_username= findViewById(R.id.til_username);
        til_password = findViewById(R.id.til_password);
        til_cpassword = findViewById(R.id.til_cpassword);

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb_s.setVisibility(View.VISIBLE);
                SignUp.setVisibility(View.INVISIBLE);
                email = Email.getText().toString().trim();
                user_name = username.getText().toString().trim();
                pass = Pass.getText().toString().trim();
                c_pass = C_Pass.getText().toString().trim();
                check_uniqueness(user_name, new get_Query_result() {
                    @Override
                    public void onCallback(boolean value) {
                        if (checkAllEntries(pass, c_pass) && username_validity(user_name) && value) {
                            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        assert user != null;
                                        String uid = user.getUid();
                                        //saving to database...
                                        saveInfo(date, uid, user_name);
                                        //creating a sharedPreference
                                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("username", user_name);
                                        editor.putString("uid", uid);
                                        editor.apply();

                                        Toast.makeText(SignUp.this, "Registration successful",
                                                Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(SignUp.this, Login.class);
                                        startActivity(i);
                                        finish();
                                    } else {
                                        Toast.makeText(SignUp.this, "Network unavailable",
                                                Toast.LENGTH_LONG).show();
                                        pb_s.setVisibility(View.GONE);
                                        SignUp.setVisibility(View.VISIBLE);
                                    }
                                }

                            });
                        } else {
                            Toast.makeText(SignUp.this, "All fields necessary",
                                    Toast.LENGTH_LONG).show();
                            pb_s.setVisibility(View.GONE);
                            SignUp.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

        });

        setupHyperLink();
    }

    public void showDatePicker(View view){

        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        MaterialDatePicker<Long> picker = builder.build();
        picker.show(getSupportFragmentManager(),picker.toString());

        //Attaching Listeners
        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {

            @Override
            public void onPositiveButtonClick(Long selection) {

                TimeZone timeZoneUTC = TimeZone.getDefault();
                int offsetFromUTC = timeZoneUTC.getOffset(new Date().getTime()) * -1;
                Date dateFromCalender = new Date(selection + offsetFromUTC);

                Log.i("Date",dateFromCalender.toString());

                ZoneId zoneId = ZoneId.systemDefault();
                Instant instant = DateTimeUtils.toInstant(dateFromCalender);
                date = instant.atZone(zoneId).toLocalDate();

                setDate();
                Today();
                Log.i("Local date",date.toString());

            }
        });
    }

    @SuppressLint("SetTextI18n")
    protected void setDate(){
        if(date==null) {
            age.setText("Date of birth");
            age.setTextColor(getResources().getColor(R.color.grey, null));
        }else{
            age.setText(date.toString());
            age.setTextColor(getResources().getColor(R.color.white,null));
        }
    }

    private boolean checkAllEntries(String pass,String c_pass){
        removeError();
        if(!isEmail(Email))
        {
            til_email.setError("Invalid Email");

            return REQUEST_DENIED;
        }
        else if(isEmpty(username)){
            til_username.setError("Invalid Username");
            return REQUEST_DENIED;
        }
        else if(isEmpty(Pass)){

            til_password.setError("Set a password");
            return REQUEST_DENIED;
        }
        else if(isEmpty(C_Pass)){

            til_cpassword.setError("Confirm the password");
            return REQUEST_DENIED;
        }
        else if(!Tools.passwordValidator(pass)){
            Pass.setError("Password must be of minimum 8 characters. It can only include letters a-z, A-Z, numbers from 0-9 and only these '_@$*!?' special characters.");
            return REQUEST_DENIED;
        }
        else if(!pass.equals(c_pass))
        {
            til_cpassword.setError("Confirm password does not match");
            return REQUEST_DENIED;
        }
        else if(date==null){
            age.setError("Set Date of Birth");
            return REQUEST_DENIED;
        }
        else if(!((between(date,today).getYears()>18)||(between(date,today).getYears()==18)&&(between(date,today).getMonths()>0||between(date,today).getDays()>=1))){
            Toast.makeText(SignUp.this, "Your age is not above 18",
                    Toast.LENGTH_LONG).show();
            return REQUEST_DENIED;
        }
        else if(!Terms.isChecked())
        {
            Toast.makeText(SignUp.this, "Please accept our terms and conditions",
                    Toast.LENGTH_LONG).show();
            return REQUEST_DENIED;
        }
        else
        {
            return REQUEST_ACCEPTED;
        }

    }

    protected void removeError(){
        til_email.setError(null);
        til_username.setError(null);
        til_password.setError(null);
        til_cpassword.setError(null);
    }
    //to check is the email field is empty
    private boolean isEmail(EditText text) {
        CharSequence email = text.getText().toString();
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    //to check if any other field is empty or not
    private boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }

    private static void Today(){
        today = LocalDate.now(ZoneId.systemDefault());
        Log.i("Today",today.toString());
    }

    private boolean username_validity(String user_name){
        if(!(user_name.matches("^([a-zA-Z0-9_]){5,30}$")&&user_name.length()>=5)){
            username.setError("Username can only contain a-z, A-Z, 0-9,'_' and should have a length between 5 and 30 characters only");
            til_username.setError("Invalid username");
            return REQUEST_DENIED;
        }
        else{
            til_username.setError(null);
            return REQUEST_ACCEPTED;
        }
    }

    protected interface get_Query_result{
        void onCallback(boolean value);
    }

    protected void check_uniqueness(String user_name, final get_Query_result callback) {
        Query query = cr.whereArrayContains("user", user_name);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty())
                    callback.onCallback(true);
                else {
                    username.setError("Username already exists");
                    callback.onCallback(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("SignUP", e.getMessage());
            }
        });
    }

    protected void saveInfo(LocalDate date, String uid, String username){
        String dob = date.toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        List<String> fav_list = new ArrayList<>();
        fav_list.add("admin");
        Map<String, Object> data = new HashMap<>();
        data.put("DOB",dob);
        data.put("username",username);
        data.put("UID",uid);
        data.put("favourite",fav_list);
        data.put("profile_image","image");
        db.collection("users").document(uid).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("Data","Added Successfully");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Data", e.getMessage());
                    }
                });
        db.collection("username").document("user_name").update("user", FieldValue.arrayUnion(username)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("user","username added");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Error", e.getMessage());
                    }
                });
    }
    private void setupHyperLink(){
        CheckBox checkBox = findViewById(R.id.TermsConditions);
        checkBox.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
