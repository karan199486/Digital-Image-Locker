package com.example.karan.digitalimagelocker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    Context context;
    ArrayList<ListItem> list;
    public MyAdapter(Context c, ArrayList<ListItem> listItems)
    {
        context = c;
        list = listItems;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int pos) {
        final int position = pos;
        holder.imagename.setText(list.get(position).getName().trim());
        /*Picasso.get()
                .load("https://faltutech.com/data/"+list.get(position).getlink().trim())
                .error(R.drawable.errorimage)
                .into(holder.imageView);*/
        Picasso.with(context)
                .load("https://faltutech.com/data/"+list.get(position).getlink().trim())
                .into(holder.imageView);

       /* holder.btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(context);
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
                                Toast.makeText(context,list.get(position).getName()+"is downloaded successfully",Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onError(ANError error) {
                                progressDialog.dismiss();
                            }
                        });
            }
        });
        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog  = new AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to delete "+list.get(position).getName())
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((UserDashboardActivity)context).deleteImage(position);
                            }
                        })
                        .setNegativeButton("No",null)
                        .create();
                dialog.show();
            }
        });*/

       holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
           @Override
           public boolean onLongClick(View v) {
               ((UserDashboardActivity)context).showPopup(holder.imageView,position);
               return true;
           }
       });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView imagename;
        ImageView imageView;
//        ImageButton btn_download,btn_delete;
        public MyViewHolder(View itemView) {
            super(itemView);

            imagename = itemView.findViewById(R.id.txt_imgname);
            imageView = itemView.findViewById(R.id.imageview);
            /*btn_delete = itemView.findViewById(R.id.btn_img_delete);
            btn_download = itemView.findViewById(R.id.btn_img_dwnload);*/
        }
    }
}
