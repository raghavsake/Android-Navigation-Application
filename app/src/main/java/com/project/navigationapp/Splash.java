package com.project.navigationapp;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Splash extends AppCompatActivity {

    String[] PERMISSIONS_REQUIRED = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    int index=0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        requestPermissions(index);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            for (int grantResult : grantResults) {
                if (grantResult != PERMISSION_GRANTED) {
                    requestPermissions(index);
                    break;
                }
            }
        } else {
            requestPermissions(index);
        }

        if(index+1==PERMISSIONS_REQUIRED.length)
            redirect();
        else
            requestPermissions(index+1);
    }

    void requestPermissions(int i) {
        try{
            String s= PERMISSIONS_REQUIRED[i];
            if (ContextCompat.checkSelfPermission(this, PERMISSIONS_REQUIRED[i]) != PERMISSION_GRANTED)
                checkPermission(s,100+i);
            else{
                index+=1;
                requestPermissions(index);
            }
        }catch(ArrayIndexOutOfBoundsException e)
        {
            redirect();
        }

    }

    public void checkPermission(String permission, int requestCode)
    {
        if(ContextCompat.checkSelfPermission(Splash.this,permission)== PERMISSION_DENIED)
            ActivityCompat.requestPermissions(Splash.this,new String[]{permission},requestCode);
        else
            return;
    }


    private void redirect() {
        Thread t=new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    sleep(1000);
                    Intent i = new Intent(Splash.this, HomeActivity.class);
                    startActivity(i);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }
}


