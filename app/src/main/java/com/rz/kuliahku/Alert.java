package com.rz.kuliahku;

import android.content.Context;
import android.graphics.Color;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Alert {
    private Context context;
    private SweetAlertDialog pDialog;

    public void setContext(Context context) {
        this.context = context;
    }

    public void successAlert(String description) {
        pDialog = new SweetAlertDialog(this.context, SweetAlertDialog.SUCCESS_TYPE);
        pDialog.setTitleText("Success!");
        pDialog.setContentText(description);
        pDialog.show();
    }

    public void errorAlert(String description) {
        pDialog = new SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE);
        pDialog.setTitleText("Error!");
        pDialog.setContentText(description);
        pDialog.show();
    }

    public void loadingAlert() {
        pDialog = new SweetAlertDialog(this.context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    public void hideAlert() {
        pDialog.hide();
    }
}
