package it.unipr.marco.fotodiario.activities;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import it.unipr.marco.fotodiario.R;
import it.unipr.marco.fotodiario.data.Album;
import it.unipr.marco.fotodiario.data.Picture;
import it.unipr.marco.fotodiario.dialogfragments.PictureDelete;
import it.unipr.marco.fotodiario.exceptions.AlbumsNotFoundException;
import it.unipr.marco.fotodiario.exceptions.PicturesNotFoundException;
import it.unipr.marco.fotodiario.interfaces.ISelectedData;
import it.unipr.marco.fotodiario.utility.PictureAdapter;
import it.unipr.marco.fotodiario.utility.StorageManager;
import it.unipr.marco.fotodiario.utility.Utility;
import it.unipr.marco.fotodiario.utility.Vars;

public class AlbumView extends AppCompatActivity
    implements ISelectedData {

    private ArrayList<Picture> listPicture;
    private RecyclerView recyclerPicture;
    private Album album;
    private StorageManager store;
    private Picture lastPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);

        Toolbar toolbar = findViewById(R.id.album_view_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        album = new Album(getIntent().getExtras().getString(Vars.ALBUM_NAME));
        store = new StorageManager(getApplicationContext());

        FloatingActionButton fab = findViewById(R.id.pictureAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PackageManager pm = getPackageManager();
                final boolean deviceHasCameraFlag = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
                if(!deviceHasCameraFlag)
                    Toast.makeText(getApplicationContext(),"Serve una fotocamera per continuare", Toast.LENGTH_SHORT).show();
                else {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            lastPicture = new Picture(new Date(), album.getName());
                            photoFile = store.createTempImageFile(album,lastPicture);
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                                    "it.unipr.marco.fotodiario",
                                    photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, Vars.SUBACTIVITY_IMAGE_CAPTURE);
                        }
                    }

                }
            }
        });
        buildRecycler();
    }

    /**
     * Funzione che aggiunge il menu all'Actionbar
     * @param menu -
     * @return -
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album_view, menu);
        return true;
    }

    /**
     * Funzione che gestisce il menu dell'actionbar
     * @param item -
     * @return -
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
            case R.id.albumViewChangeLayout:
                if(Vars.picture_visualization == Vars.LIST) {
                    Vars.picture_visualization = Vars.GRID;
                }
                else if(Vars.picture_visualization == Vars.GRID) {
                    Vars.picture_visualization = Vars.LIST;
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        invalidateOptionsMenu();
        buildRecycler();

        return super.onOptionsItemSelected(item);
    }

    /**
     * Funzione che viene chiamata da invalidateOptionsMenu() e alla creazione dell'activity
     * che permette di cambiare le icone della action bar
     * @param menu -
     * @return -
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(Vars.picture_visualization == Vars.LIST)
            menu.findItem(R.id.albumViewChangeLayout).setIcon(R.drawable.ic_view_grid_white_24dp);
        if(Vars.picture_visualization == Vars.GRID)
            menu.findItem(R.id.albumViewChangeLayout).setIcon(R.drawable.ic_view_list_white_24dp);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Funzione che gestisce il rientro da una subactivity
     * @param requestCode -
     * @param resultCode -
     * @param data -
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case Vars.SUBACTIVITY_IMAGE_CAPTURE:
                if(resultCode == RESULT_OK) {
                    //Toast.makeText(getApplicationContext(),"Foto salvata con successo", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), PictureNew.class);
                    //Aggiungo le variabili necessarie all'intent
                    i.putExtra(Vars.ALBUM_NAME, lastPicture.getAlbumParent());
                    i.putExtra(Vars.PICTURE_FILE_NAME_TEMP, store.getTempName());
                    startActivityForResult(i,Vars.SUBACTIVITY_IMAGE_SAVE);
                }
                break;
            case Vars.SUBACTIVITY_IMAGE_SAVE:
                buildRecycler();
                break;
            case Vars.SUBACTIVITY_IMAGE_VIEW:
                if(data == null)
                    System.out.println("DATA IS NULL");
                if(data.getExtras() == null)
                    System.out.println("EXTRAS IS NULL");
                if(resultCode == RESULT_CANCELED && data.getExtras().containsKey(Vars.BOOL)) {
                    ArrayList<Picture> pListTemp = (ArrayList<Picture>) listPicture.clone();
                    for(Picture p : listPicture) {
                        if(p.getFileName().equals(data.getExtras().getString(Vars.PICTURE_NAME))) {
                            store.deleteImageFile(album, p);
                            pListTemp.remove(p);
                        }
                    }
                    store.writePictureList(pListTemp, album);
                }
                buildRecycler();
                break;
        }
    }

    /**
     * Funzione che gestice il disegno delle immagini a schermo con il recyclerView
     * E ha anche tutti gli action listener appartenenti alla lista/griglia
     */
    private void buildRecycler() {
        //Carico le immagini
        loadPictures();
        //Gestisco il layout e i vari action listeners
        recyclerPicture = findViewById(R.id.album_view_recycler);
        if(Vars.picture_visualization == Vars.LIST)
            recyclerPicture.setLayoutManager(new LinearLayoutManager(this));
        else if(Vars.picture_visualization == Vars.GRID) {
            if(Utility.getScreenOrientation(getWindowManager().getDefaultDisplay()) == Configuration.ORIENTATION_PORTRAIT)
                recyclerPicture.setLayoutManager(new GridLayoutManager(this,3));
            if(Utility.getScreenOrientation(getWindowManager().getDefaultDisplay()) == Configuration.ORIENTATION_LANDSCAPE)
                recyclerPicture.setLayoutManager(new GridLayoutManager(this,5));

        }

        PictureAdapter adapter = new PictureAdapter(listPicture);
        /*
         * Gestore del click breve sull'immagine
         */
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), PictureView.class);
                //Aggiungo il nome dell'album all'intent così che sappia cosa deve aprire
                i.putExtra(Vars.ALBUM_NAME, listPicture.get(recyclerPicture.getChildAdapterPosition(view)).getAlbumParent());
                i.putExtra(Vars.PICTURE_NAME, listPicture.get(recyclerPicture.getChildAdapterPosition(view)).getFileName());
                startActivityForResult(i, Vars.SUBACTIVITY_IMAGE_VIEW);
            }
        });
        /*
         * Gestore del click lungo sull'album
         */
        adapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                PopupMenu popup = new PopupMenu(AlbumView.this,view);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch(menuItem.getItemId()) {
                            //Eliminazione della foto selezionata
                            case R.id.pictureRemove:
                                //Mi segno quale Picture eventualmente cancellare
                                lastPicture = listPicture.get(recyclerPicture.getChildAdapterPosition(view)).clone();
                                //Creo il bundle con il nome dell'album che voglio eliminare
                                Bundle bundle = new Bundle();
                                bundle.putString(Vars.ALBUM_NAME, listPicture.get(recyclerPicture.getChildAdapterPosition(view)).getAlbumParent());
                                bundle.putString(Vars.PICTURE_NAME, listPicture.get(recyclerPicture.getChildAdapterPosition(view)).getFileName());
                                //Creo il fragment e gli attacco il bundle di parametri
                                PictureDelete deleteDialogFragment = new PictureDelete();
                                deleteDialogFragment.setArguments(bundle);
                                //Onestamente non so cosa faccia
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                deleteDialogFragment.show(ft, "Tag");
                                break;
                        }
                        return true;
                    }
                });
                popup.inflate(R.menu.menu_album_view_remove);
                popup.show();
                return true;
            }
        });

        recyclerPicture.setAdapter(adapter);
    }

    /**
     * Funzione data dall'implementazione dell'interfaccia ISelectData che permette di ricevere un messaggio
     * di ritorno da un fragment che invochi con
     * @param string parametro che il fragment ritorna
     */
    @Override
    public void onSelectedData(String string) {
        switch (string) {
            //Mi ero segnato la Picture al momento della pressione del delete, qua ci entro solo se clicco
            //sì nel fragment che mi si era aperto poco fa
            case Vars.FRAGMENT_PICTURE_DELETE:
                ArrayList<Picture> pListTemp = (ArrayList<Picture>) listPicture.clone();
                for(Picture p : listPicture) {
                    if(p.getFileName().equals(lastPicture.getFileName())) {
                        pListTemp.remove(p);
                        store.deleteImageFile(album, p);
                    }
                }
                store.writePictureList(pListTemp, album);
                buildRecycler();
                break;
        }
    }
    /**
     * Funzione ausiliaria che chiede i permessi r/w e chiama lo store per leggere gli album
     */
    private void loadPictures() {
        //55 char di descrizione
        StorageManager.checkAndAskForStoragePermission(this,getApplicationContext());
        listPicture = new ArrayList<>();
        try {
            listPicture = store.readPictureList(album);
        } catch(PicturesNotFoundException e) {
            listPicture = new ArrayList<>();
            Toast.makeText(getApplicationContext(),"Non ci sono immagini, inizia creandone una",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Funzione necessaria in quanto si fanno richieste di permessi un questa schermata
     * @param requestCode -
     * @param permissions -
     * @param grantResults -
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Vars.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    Toast.makeText(this, R.string.errorNoStoragePermission, Toast.LENGTH_LONG).show();
                //Se al primo avvio non ho i permessi e li do essendo asincrona la richiesta si è già persa
                //per strada la funzione loadAlbums() per cui tanto vale ricaricare tutto e via
                buildRecycler();
            }
        }
    }
}
