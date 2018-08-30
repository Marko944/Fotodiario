package it.unipr.marco.fotodiario.activities;

import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import it.unipr.marco.fotodiario.R;
import it.unipr.marco.fotodiario.data.Album;
import it.unipr.marco.fotodiario.data.Picture;
import it.unipr.marco.fotodiario.dialogfragments.PictureDelete;
import it.unipr.marco.fotodiario.dialogfragments.PictureEditDesc;
import it.unipr.marco.fotodiario.dialogfragments.PictureInfo;
import it.unipr.marco.fotodiario.exceptions.PicturesNotFoundException;
import it.unipr.marco.fotodiario.interfaces.ISelectedData;
import it.unipr.marco.fotodiario.utility.PinchZoomPanView;
import it.unipr.marco.fotodiario.utility.StorageManager;
import it.unipr.marco.fotodiario.utility.Utility;
import it.unipr.marco.fotodiario.utility.Vars;

public class PictureView extends AppCompatActivity implements ISelectedData {

    private Picture picture;
    private StorageManager store;
    private ArrayList<Picture> pList;
    protected PinchZoomPanView pinchZoomPan;

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_view);

        Toolbar toolbar = findViewById(R.id.picture_view_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Foto");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        pList = new ArrayList<>();
        this.store = new StorageManager(getApplicationContext());
        try {
            pList = store.readPictureList(new Album(getIntent().getExtras().getString(Vars.ALBUM_NAME)));
        } catch(PicturesNotFoundException e) {
            //Nothing
        }
        for(Picture p : pList) {
            if(p.getFileName().equals(getIntent().getExtras().getString(Vars.PICTURE_NAME))) {
                this.picture = p.clone();
                this.picture.setAlbumParent(getIntent().getExtras().getString(Vars.ALBUM_NAME));
            }
        }
        this.pinchZoomPan = findViewById(R.id.pictureView);

        pinchZoomPan.loadImageOnCanvas(picture.getFileName(), picture.getAlbumParent());
    }

    /**
     * Aggiungo il menu alla toolbar
     * @param menu -
     * @return -
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_picture_view, menu);
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
            case R.id.pictureShare: {
                String picturePath = StorageManager.getBaseDirectory()+"/"
                        +picture.getAlbumParent()+"/"
                        +picture.getFileName()+".jpg";
                Uri uriPath = FileProvider.getUriForFile(this, "it.unipr.marco.fotodiario", new File(picturePath));
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uriPath);
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent, "Condividi Immagine..."));
                break;
            }
            case R.id.pictureAddDescription: {
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
            }
            case R.id.pictureInfo: {
                //Creo il bundle con i dati necessari a visualizzare le info
                Bundle bundle = new Bundle();
                bundle.putString(Vars.PICTURE_DESC, picture.getDesc());
                bundle.putString(Vars.PICTURE_NAME, picture.getFileName());
                bundle.putString(Vars.PICTURE_GPS_LATITUDE, picture.getLatitude()+"");
                bundle.putString(Vars.PICTURE_GPS_LONGITUDE, picture.getLongitude()+"");
                //Creo il fragment e gli attacco il bundle di parametri
                PictureInfo infoDialogFragment = new PictureInfo();
                infoDialogFragment.setArguments(bundle);
                //Onestamente non so cosa faccia
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                infoDialogFragment.show(ft, "Tag");
                break;
            }
            case R.id.pictureDelete: {
                //Creo il bundle con il nome dell'album che voglio eliminare
                Bundle bundle = new Bundle();
                bundle.putString(Vars.ALBUM_NAME, picture.getAlbumParent());
                bundle.putString(Vars.PICTURE_NAME, picture.getFileName());
                //Creo il fragment e gli attacco il bundle di parametri
                PictureDelete deleteDialogFragment = new PictureDelete();
                deleteDialogFragment.setArguments(bundle);
                //Onestamente non so cosa faccia
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                deleteDialogFragment.show(ft, "Tag");
                break;
            }
            case R.id.pictureMaps: {
                //Uri navigationIntentUri = Uri.parse("google.navigation:q=" + picture.getLatitude() + "," + picture.getLongitude());
                Uri navigationIntentUri = Uri.parse(
                        "geo:" + picture.getLatitude()+
                        "," + picture.getLongitude()+
                        "?q=" + picture.getLatitude()+
                        "," + picture.getLongitude()+
                        "("+picture.getFileName().replace(' ','+')+")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, navigationIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(mapIntent);
                }
                catch(ActivityNotFoundException ex) {
                    try {
                        Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, navigationIntentUri);
                        startActivity(unrestrictedIntent);
                    }
                    catch(ActivityNotFoundException innerEx) {
                        Toast.makeText(this, "Per favore installa Google Maps", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            }
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return (super.onOptionsItemSelected(item));
    }

    /**
     * Devo fare così altrimenti non fa essendo una subactivity
     */
    @Override
    public void onBackPressed() {
        setResultOk();
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
                ArrayList<Picture> pListTemp = (ArrayList<Picture>) pList.clone();
                int position = 0;
                for(Picture p : pList) {
                    if(p.getFileName().equals(getIntent().getExtras().getString(Vars.PICTURE_NAME))) {
                        position = pListTemp.indexOf(p);
                        pListTemp.remove(p);
                    }
                }
                pListTemp.add(position, picture);
                store.writePictureList(pListTemp, new Album(picture.getAlbumParent()));
                break;
            case Vars.FRAGMENT_PICTURE_INFO:
                break;
            case Vars.FRAGMENT_PICTURE_DELETE:
                setResultDelete();
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
     * Funzione ausiliaria per terminare con eliminazione
     */
    private void setResultDelete(){
        Intent intent = new Intent();
        intent.putExtra(Vars.ALBUM_NAME,picture.getAlbumParent());
        intent.putExtra(Vars.PICTURE_NAME,picture.getFileName());
        intent.putExtra(Vars.BOOL,"true");
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
