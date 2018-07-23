package com.example.karan.digitalimagelocker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

public class LoginActivity extends AppCompatActivity {

    EditText edttxt_username, edttxt_password;
    Button btn_login;
    TextView txt_gotoregisteration;
    private String url = "https://faltutech.com/data/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edttxt_password = findViewById(R.id.login_edittxt_password);
        edttxt_username = findViewById(R.id.login_edittxt_username);
        btn_login = findViewById(R.id.btn_login);
        txt_gotoregisteration = findViewById(R.id.txt_registeruser);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticateFromServer();
            }
        });

        txt_gotoregisteration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this,RegistrationActivity.class);
                startActivity(i);
                finish();
            }
        });


    }

    private void authenticateFromServer()
    {
        final String username = edttxt_username.getText().toString().trim();
        final String password = edttxt_password.getText().toString().trim();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("contacting server...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {
                progressDialog.dismiss();
                try {
                    JSONObject object = new JSONObject(response);
                    if(object.getInt("code")==1)
                    {
                        //success
                        String token = object.getString("token");
                        Intent i = new Intent(LoginActivity.this,UserDashboardActivity.class);
                        i.putExtra("token",token);
                        startActivity(i);
                        Toast.makeText(LoginActivity.this,"login successful",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else {
                        //failure
                        Toast.makeText(LoginActivity.this,"login failed",Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("username",username);
                params.put("password",password);
                return params;
            }
        };

        queue.add(request);
    }
}
