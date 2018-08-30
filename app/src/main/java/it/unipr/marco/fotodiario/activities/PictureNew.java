package it.unipr.marco.fotodiario.activities;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import it.unipr.marco.fotodiario.R;
import it.unipr.marco.fotodiario.data.Album;
import it.unipr.marco.fotodiario.data.Picture;
import it.unipr.marco.fotodiario.dialogfragments.PictureEditDesc;
import it.unipr.marco.fotodiario.exceptions.PicturesNotFoundException;
import it.unipr.marco.fotodiario.interfaces.ISelectedData;
import it.unipr.marco.fotodiario.utility.PinchZoomPanView;
import it.unipr.marco.fotodiario.utility.StorageManager;
import it.unipr.marco.fotodiario.utility.Utility;
import it.unipr.marco.fotodiario.utility.Vars;

public class PictureNew extends AppCompatActivity implements ISelectedData{

    private Picture picture;
    private StorageManager store;
    protected PinchZoomPanView pinchZoomPan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_new);

        Toolbar toolbar = findViewById(R.id.picture_new_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                store.deleteTempImageFile(new Album(picture.getAlbumParent()));
                onBackPressed();
                setResultError();
            }
        });

        this.store = new StorageManager(getApplicationContext());
        this.picture = new Picture(new Date(), getIntent().getExtras().getString(Vars.ALBUM_NAME));
        this.store.setTempName(getIntent().getExtras().getString(Vars.PICTURE_FILE_NAME_TEMP));
        this.pinchZoomPan = findViewById(R.id.pictureNew);

        pinchZoomPan.loadImageOnCanvas(store.getTempName(), picture.getAlbumParent());

        locationService();
    }

    /**
     * Aggiungo il menu alla toolbar
     * @param menu -
     * @return -
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_picture_new, menu);
        return true;
    }

    /**
     * Controllo i pulsanti della toolbar
     * @param item -
     * @return -
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.pictureAddDescription:
                //Creo il bundle con i dati necessari ad aggiungere la descrizione
                Bundle bundle = new Bundle();
                bundle.putString(Vars.PICTURE_DESC, picture.getDesc());
                //Creo il fragment e gli attacco il bundle di parametri
                PictureEditDesc descriptionDialogFragment = new PictureEditDesc();
                descriptionDialogFragment.setArguments(bundle);
                //Onestamente non so cosa faccia
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                descriptionDialogFragment.show(ft, "Tag");
                break;
            case R.id.pictureSave:
                store.createImageFile(new Album(picture.getAlbumParent()),picture);
                ArrayList<Picture> listPictures;
                try {
                    listPictures = store.readPictureList(new Album(picture.getAlbumParent()));
                    listPictures.add(picture);
                    store.writePictureList(listPictures, new Album(picture.getAlbumParent()));
                } catch (PicturesNotFoundException e) {
                    listPictures = new ArrayList<>();
                    listPictures.add(picture);
                    store.writePictureList(listPictures, new Album(picture.getAlbumParent()));
                }
                setResultOk();
                break;
            case android.R.id.home:
                store.deleteTempImageFile(new Album(picture.getAlbumParent()));
                onBackPressed();
                setResultError();
                break;
        }
        return (super.onOptionsItemSelected(item));
    }

    /**
     * Funzione data dall'implementazione dell'interfaccia ISelectData che permette di ricevere un messaggio
     * di ritorno da un fragment che invochi con
     * @param string parametro che il fragment ritorna
     */
    @Override
    public void onSelectedData(String string) {
        switch (string) {
            case Vars.FRAGMENT_PICTURE_ADD_DESC:
                picture.setDesc(Utility.utility);
                break;
        }
    }

    /**
     * Funzione ausiliaria per terminare con successo
     */
    private void setResultOk(){
        Intent intent = new Intent();
        intent.putExtra(Vars.ALBUM_NAME,picture.getAlbumParent());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Funzione ausiliaria per terminare con fallimento
     */
    private void setResultError(){
        Intent intent = new Intent();
        intent.putExtra(Vars.ALBUM_NAME, picture.getAlbumParent());
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    /**
     * Prendo le coordinate GPS
     */
    private void locationService() {
        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Location Changes", location.toString());
                picture.setLatitude((float)location.getLatitude());
                picture.setLongitude((float)location.getLongitude());
                Log.d("DEBUG", "Lat: "+picture.getLatitude()+"Long: "+picture.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Status Changed", String.valueOf(status));
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Provider Enabled", provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Provider Disabled", provider);
            }
        };

        // Now first make a criteria with your requirements
        // this is done to save the battery life of the device
        // there are various other other criteria you can search for..
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        // Now create a location manager
        final LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // This is the Best And IMPORTANT part
        final Looper looper = null;

        if(StorageManager.checkAndAskForGpsPermission(this,getApplicationContext())) {
            locationManager.requestSingleUpdate(criteria, locationListener, looper);
            try{ Thread.sleep(2000); }catch (Exception e){/*Nothing*/}
        } else {
            Toast.makeText(getApplicationContext(), R.string.errorNoGpsPermissions, Toast.LENGTH_SHORT).show();
        }
    }
}
