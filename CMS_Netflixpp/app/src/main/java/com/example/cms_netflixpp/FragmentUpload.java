package com.example.cms_netflixpp;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class FragmentUpload extends Fragment {
    final static String uploadURL = "http://{ip}:{port}/upload/movie";

    TextView txt_pathShow;
    TextView txt_thumbnail_path;
    Button btn_videoPicker;
    Button btn_thumbnailPicker;
    Button btn_send;
    Intent myFileIntent;
    Intent myThumbnailIntent;
    EditText txt_title;
    String filePathv, filePatht;
    public static String thumb = null;
    View view;

    private static final int PICK_FROM_GALLERY = 1;
    @SuppressLint("MissingInflatedId")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_upload, container, false);
        txt_pathShow = view.findViewById(R.id.txt_path);
        txt_thumbnail_path = view.findViewById(R.id.thumbnail_path);
        btn_videoPicker = view.findViewById(R.id.uploadv);
        btn_thumbnailPicker = view.findViewById(R.id.uploadt);
        btn_send = view.findViewById(R.id.send);
        txt_title = view.findViewById(R.id.fileName);

        btn_videoPicker.setOnClickListener(view -> {
            myFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
            myFileIntent.setType("video/*");
            try {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
                } else {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    myFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    myFileIntent.setType("video/*");
                    startActivityForResult(galleryIntent, 10);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btn_thumbnailPicker.setOnClickListener(view -> {
            myThumbnailIntent = new Intent(Intent.ACTION_GET_CONTENT);
            myThumbnailIntent.setType("image/*");
            try {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
                } else {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    myThumbnailIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    myThumbnailIntent.setType("image/*");
                    startActivityForResult(galleryIntent, 20);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btn_send.setOnClickListener(view -> {
            String title = txt_title.getText().toString();
            assert getArguments() != null;
            if(!title.equals("")){
                UploadThread uploadThread = new UploadThread(getArguments().getString("user"), getArguments().getString("pass"), title, filePathv, requireActivity().getApplicationContext());
                uploadThread.start();
            }else{
                Toast.makeText(getContext(), "Video title cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Context context = requireContext().getApplicationContext();
                    String path = uri.getPath();
                    txt_pathShow.setText(path);
                    videoSend(context, uri);
                }
                break;

            case 20:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Context context = requireContext().getApplicationContext();
                    String path = uri.getPath();
                    txt_thumbnail_path.setText(path);
                    thumb = thumbnailSend(context);
                    System.out.println(txt_thumbnail_path.getText());
                }
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

    public void videoSend(@NonNull Context context, @NonNull Uri uri) {
        final ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null)
            return;

        filePathv = context.getApplicationInfo().dataDir + File.separator + "video.mp4";
        System.out.println(filePathv);
        File file = new File(filePathv);
        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            if (inputStream == null)
                return;
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[20971520];
            int len;
            while ((len = inputStream.read(buf)) > 0)
                outputStream.write(buf, 0, len);
            outputStream.close();
            inputStream.close();
        } catch (IOException ignore) {
        }
    }

    @Nullable
    public String thumbnailSend(@NonNull Context context) {
        final ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null)
            return null;
        return filePatht = context.getApplicationInfo().dataDir + File.separator + "thumbnail.png";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    myFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    myFileIntent.setType("video/");
                    startActivityForResult(galleryIntent, 10);
                } else {
                    Toast.makeText(getContext(), "Unable to access gallery.", Toast.LENGTH_SHORT).show();
                }
                break;

            case 20:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    myThumbnailIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    myThumbnailIntent.setType("image/");
                    startActivityForResult(galleryIntent, 20);
                } else {
                    Toast.makeText(getContext(), "Unable to access gallery.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}