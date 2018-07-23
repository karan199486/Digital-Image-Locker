package com.example.karan.digitalimagelocker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        eusername = findViewById(R.id.regusr_uname);
        eemail = findViewById(R.id.regusr_email);
        ephone = findViewById(R.id.regusr_mobile);
        epass = findViewById(R.id.regusr_password);
        econfirmpass = findViewById(R.id.regusr_confpassword);
        btn_register = findViewById(R.id.regusr_btn_submit);
        txt_alreadyreg = findViewById(R.id.regusr_txt_alreadyreg);

        btn_register.setOnClickListener(this);
        txt_alreadyreg.setOnClickListener(this);
    }

    EditText eusername, eemail, ephone, epass, econfirmpass;
    Button btn_register;
    TextView txt_alreadyreg;

    String username, email, phoneno, pass, confpass;
    String registerationUrl = "https://faltutech.com/data/register.php";



    @Override
    public void onClick(View v)
    {
        if(v == btn_register)
        {
            username = eusername.getText().toString().trim();
            email = eemail.getText().toString().trim();
            phoneno = ephone.getText().toString().trim();
            pass = epass.getText().toString().trim();
            confpass = econfirmpass.getText().toString().trim();

            if(!checkIfEmpty() && isPassEqual())
            {
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("contacting server...");
                progressDialog.show();

                RequestQueue requestQueue = Volley.newRequestQueue(this);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, registerationUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.d("karan",response);
                        try {
                            JSONObject object = new JSONObject(response);
                            if(object.getInt("code")==8)
                            {
                                Toast.makeText(RegistrationActivity.this,"registration successful",Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(RegistrationActivity.this,UserDashboardActivity.class);
                                i.putExtra("token",object.getString("token"));
                                startActivity(i);
                                finish();
                            }
                            else displayError(object.getInt("code"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //result from server

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(RegistrationActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                })
                {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> params = new HashMap<>();
                        params.put("username",username);
                        params.put("email",email);
                        params.put("mobile",phoneno);
                        params.put("password",pass);
                        return params;
                    }
                };

                requestQueue.add(stringRequest);
            }
            else if(checkIfEmpty())Toast.makeText(this,"please fill all fields",Toast.LENGTH_SHORT).show();
            else if(!isPassEqual())Toast.makeText(this,"password mismatch",Toast.LENGTH_SHORT).show();
        }
        else if(v == txt_alreadyreg)
        {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void displayError(int code)
    {
        String message = null;
        switch (code)
        {
            case 1 : message = "invalid email"; break;
            case 2 : message = "Password strength is not good (Requires atleast 8 char long and one upper case, one lower case , a digit, and a special character from ~!@#$&* )"; break;
            case 3 : message = "invalid phoneno"; break;
            case 4 : message = "invalid username"; break;
            case 5 : message = "email already registered"; break;
            case 6 : message = "mobile already registered"; break;
            case 7 : message = "username already registered";

        }
        Toast.makeText(RegistrationActivity.this,message,Toast.LENGTH_SHORT).show();
    }

    private boolean checkIfEmpty(){
        return username.equals("") || email.equals("") || phoneno.equals("") || pass.equals("") || confpass.equals("");
    }

    private boolean isPassEqual(){
        return pass.equals(confpass);
    }
}
