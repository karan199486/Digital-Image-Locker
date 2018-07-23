package com.example.karan.digitalimagelocker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserDashboardActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 100;
    String token = null;
    RecyclerView recyclerView;
    ArrayList<ListItem> list;
    MyAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);
        recyclerView = findViewById(R.id.recyclerview);
        token = getIntent().getStringExtra("token");
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        list =new ArrayList<>();
        getImageList();

    }

    public void showPopup(View v, final int position)
    {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.image_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch(item.getItemId())
                {
                    case R.id.popmnu_delete:
                        deleteImage(position);
                        return true;

                    case R.id.popmnu_save:
                        downloadImage(position);
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void downloadImage(final int position)
    {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading... \n 0/0 bytes");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        AndroidNetworking.download("https://faltutech.com/data/"+list.get(position).getlink().trim(),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(),
                list.get(position).getName().trim())
                .setTag("downloadTest")
                .setPriority(Priority.MEDIUM)
                .build()
                .setDownloadProgressListener(new DownloadProgressListener() {
                    @Override
                    public void onProgress(long bytesDownloaded, long totalBytes) {
                        progressDialog.setMessage("Downloading...\n"+bytesDownloaded+"/"+totalBytes);
                    }
                })
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        progressDialog.dismiss();
                        Toast.makeText(UserDashboardActivity.this,list.get(position).getName()+" is downloaded successfully",Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(ANError error) {
                        progressDialog.dismiss();
                        Toast.makeText(UserDashboardActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.usr_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.mnu_logout:
                    startActivity(new Intent(this,LoginActivity.class));
                    finish();
                return true;

            case R.id.mnu_refresh:
                list.clear();
                getImageList();
                return true;
            case R.id.mnu_uploadimage:
                selectImage();
                return true;
            default:

                return super.onOptionsItemSelected(item);
        }
    }


    private void getImageList()
    {
        //making list empty before
        list.clear();
        if(adapter!=null){
            adapter.notifyItemRangeRemoved(0,0);
            adapter.notifyDataSetChanged();
        }


        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request =new StringRequest(Request.Method.POST, Constants.url_getlistofimages, new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {
                Log.d("response",response);

                try {
                    /*
                }
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getInt("code")>5)
                    {*/
                        JSONArray jsonArray = new JSONArray(response);
                        Gson gson = new Gson();
                        for (int i = 0; i < jsonArray.length(); i++)
                        {
                            ListItem item = gson.fromJson(jsonArray.get(i).toString(),ListItem.class);
                            list.add(item);
                        }
                       adapter = new MyAdapter(UserDashboardActivity.this,list);
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                   /* }
                    else showlistError(jsonObject.getInt("code"));*/

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(UserDashboardActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("token",token);
                return params;

            }
        };
        queue.add(request);
    }

    private void showlistError(int code) {
        String msg;
        switch (code)
        {
            case 1: msg = "tokent is not valid";
                break;
            case 2: msg = "token is expired";
                break;
            case 3: msg = "sql error on server";
                break;
            case 4: msg = "token is not sent to server";
                break;
            case 5: msg = "no file is present";
                break;
            default: msg = "unknown error has occured with code "+code;

        }
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    private void uploadImage(String imagepath)
    {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("uploading ...");
        progressDialog.show();
        AndroidNetworking.upload(Constants.url_imageupload)
                .addMultipartFile("userfile", new File(imagepath))
                .addMultipartParameter("token", token)
                .setTag("uploadTest")
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {

                        progressDialog.setMessage("uploaded "+(bytesUploaded/1024)+" / "+(totalBytes/1024)+" KB");
                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();
                        // do anything with response
                        try {
                            if (response.getInt("code") == 5) {
                                Toast.makeText(UserDashboardActivity.this, "uploaded successfully", Toast.LENGTH_SHORT).show();
                                getImageList();
                            } else
                                Toast.makeText(UserDashboardActivity.this, "error code : " + response.getInt("code"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        progressDialog.dismiss();
                        // handle error
                        Toast.makeText(UserDashboardActivity.this, error.getErrorBody(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void selectImage()
    {
        /*Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i,REQUEST_CODE);*/

        Intent i = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK && data!=null)
        {
           Uri uri_image =  data.getData();
           String imagepath = getPath(this,uri_image);
           Log.d("imagepath","path is : "+imagepath);

           if(imagepath!=null)
           {
               uploadImage(imagepath);
           }
           else Toast.makeText(this, "Image doesnot exist", Toast.LENGTH_SHORT).show();
        }


    }

    public static String getPath(Context context, Uri uri )
    {
        /*String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;*/

        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        Cursor cursor = context.getContentResolver().query(uri,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        return picturePath;
    }


    public void deleteImage(final int position)
    {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete " + list.get(position).getName())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        final ProgressDialog progressDialog = new ProgressDialog(UserDashboardActivity.this);
                        progressDialog.setMessage("deletion in progress...");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        RequestQueue queue = Volley.newRequestQueue(UserDashboardActivity.this);
                        StringRequest request = new StringRequest(Request.Method.POST, Constants.url_imagedelete, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response)
                            {
                                progressDialog.dismiss();
                                try {
                                    JSONObject object = new JSONObject(response);
                                    if(object.getInt("code")==7)
                                    {
                                        //success
                                        Toast.makeText(UserDashboardActivity.this,"Image deleted Successfully",Toast.LENGTH_SHORT).show();
                                        list.remove(position);
                                        adapter.notifyItemRemoved(position);
                                        adapter.notifyItemRangeChanged(0,list.size());
                                    }
                                    else {
                                        //failure
                                        Toast.makeText(UserDashboardActivity.this,"Unable to delete image",Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(UserDashboardActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        })
                        {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> params = new HashMap<>();
                                params.put("token",token);
                                params.put("id",list.get(position).getId());
                                return params;
                            }
                        };

                        queue.add(request);
                    }
                })
                .setNegativeButton("No", null)
                .create();
        dialog.show();

    }
}
