package it.unipr.marco.fotodiario.dialogfragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import it.unipr.marco.fotodiario.interfaces.ISelectedData;
import it.unipr.marco.fotodiario.utility.StorageManager;
import it.unipr.marco.fotodiario.utility.Utility;
import it.unipr.marco.fotodiario.utility.Vars;

public class PictureInfo extends DialogFragment {

    //Serve per il passaggio di parametri dal fragment all'activity
    private ISelectedData mCallback;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState){
        //Prendo i parametri che ricevo alla creazione del fragment
        final Bundle bundle = getArguments();
        final StorageManager store = new StorageManager(getActivity().getApplicationContext());
        String out =
                "Data: " + bundle.getString(Vars.PICTURE_NAME)+
                "\nDescrizione: " + bundle.getString(Vars.PICTURE_DESC)+
                "\nLatitudine: " + bundle.getString(Vars.PICTURE_GPS_LATITUDE)+
                "\nLongitudine: " + bundle.getString(Vars.PICTURE_GPS_LONGITUDE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(out);
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //Funzione magica per passare una string all'Activity chiamante
                mCallback.onSelectedData(Vars.FRAGMENT_PICTURE_INFO);
            }
        });

        return builder.create();
    }

    /**
     * Funzione che sfrutta l'interfaccia ISelectData per passare parametri all'activity chiamante
     * Non va personalizzata! Prendila così com'è
     * @param activity -
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (ISelectedData) activity;
        }
        catch (ClassCastException e) {
            Log.d("Error", "Activity doesn't implement the ISelectedData interface");
        }
    }
}
