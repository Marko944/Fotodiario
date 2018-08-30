package it.unipr.marco.fotodiario.activities;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.ArrayList;

import it.unipr.marco.fotodiario.R;
import it.unipr.marco.fotodiario.data.Album;
import it.unipr.marco.fotodiario.exceptions.AlbumsNotFoundException;
import it.unipr.marco.fotodiario.dialogfragments.AlbumDelete;
import it.unipr.marco.fotodiario.interfaces.ISelectedData;
import it.unipr.marco.fotodiario.utility.AlbumAdapter;
import it.unipr.marco.fotodiario.utility.StorageManager;
import it.unipr.marco.fotodiario.utility.Vars;

public class MainActivity extends AppCompatActivity implements ISelectedData {

    private ArrayList<Album> listAlbum;
    private RecyclerView recyclerAlbum;
    private StorageManager store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new StorageManager(getApplicationContext());
        listAlbum = new ArrayList<>();
        //Android studio
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.main_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createAlbumIntent = new Intent(getApplicationContext(), AlbumCreateNew.class);
                startActivityForResult(createAlbumIntent, Vars.SUBACTIVITY_ALBUM_CREATE);
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            case R.id.mainChangeLayout:
                if(Vars.album_visualization == Vars.LIST) {
                    Vars.album_visualization = Vars.GRID;
                }
                else if(Vars.album_visualization == Vars.GRID) {
                    Vars.album_visualization = Vars.LIST;
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
        if(Vars.album_visualization == Vars.LIST)
            menu.findItem(R.id.mainChangeLayout).setIcon(R.drawable.ic_view_grid_white_24dp);
        if(Vars.album_visualization == Vars.GRID)
            menu.findItem(R.id.mainChangeLayout).setIcon(R.drawable.ic_view_list_white_24dp);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Funzione dell'Activity per ricevere informazioni dalle subactivities
     * @param requestCode -
     * @param resultCode -
     * @param data -
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        try {
            switch (requestCode) {
                case Vars.SUBACTIVITY_ALBUM_CREATE:
                case Vars.SUBACTIVITY_ALBUM_EDIT:
                    if (resultCode == RESULT_OK) {
                        listAlbum = store.readAlbumList();
                        buildRecycler();
                    }
                    break;
            }
        } catch (AlbumsNotFoundException e) {
            //Se arrivo qua ho fatto errori di programmazione o mi sono cancellato gli album tra il passaggio
            //della schermata precedente e quella attuale, non credo sia fattibile
            System.out.println("¯\\_(ツ)_/¯");
        }
    }

    /**
     * Funzione che gestice il disegno degli album a schermo con il recyclerView
     * E ha anche tutti gli action listener appartenenti alla lista/griglia
     */
    private void buildRecycler() {
        //Carico gli album
        loadAlbums();
        //Gestisco il layout e i vari action listeners
        recyclerAlbum = findViewById(R.id.main_recycler);
        if(Vars.album_visualization == Vars.LIST)
            recyclerAlbum.setLayoutManager(new LinearLayoutManager(this));
        else if(Vars.album_visualization == Vars.GRID)
            switch(listAlbum.size()) {
                default:
                    recyclerAlbum.setLayoutManager(new GridLayoutManager(this,2));
                    break;
            }
        AlbumAdapter adapter = new AlbumAdapter(listAlbum);
        /*
         * Gestore del click breve sull'album
         */
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), AlbumView.class);
                //Aggiungo il nome dell'album all'intent così che sappia cosa deve aprire
                i.putExtra(Vars.ALBUM_NAME, listAlbum.get(recyclerAlbum.getChildAdapterPosition(view)).getName());
                startActivity(i);
            }
        });
        /*
         * Gestore del click lungo sull'album
         */
        adapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                PopupMenu popup = new PopupMenu(MainActivity.this,view);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch(menuItem.getItemId()) {
                            /*
                             * Modifica dell'album selezionato
                             */
                            case R.id.albumEdit:
                                Intent i = new Intent(getApplicationContext(), AlbumEdit.class) ;
                                i.putExtra(Vars.ALBUM_NAME, listAlbum.get(recyclerAlbum.getChildAdapterPosition(view)).getName());
                                startActivityForResult(i, Vars.SUBACTIVITY_ALBUM_EDIT);
                                break;
                            /*
                             * Eliminazione dell'album selezionato
                             */
                            case R.id.albumRemove:
                                //Creo il bundle con il nome dell'album che voglio eliminare
                                Bundle bundle = new Bundle();
                                bundle.putString(Vars.ALBUM_NAME, listAlbum.get(recyclerAlbum.getChildAdapterPosition(view)).getName());
                                //Creo il fragment e gli attacco il bundle di parametri
                                AlbumDelete deleteDialogFragment = new AlbumDelete();
                                deleteDialogFragment.setArguments(bundle);
                                //Onestamente non so cosa faccia
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                deleteDialogFragment.show(ft, "Tag");
                                break;
                        }
                        return true;
                    }
                });
                popup.inflate(R.menu.menu_main_edit_remove);
                popup.show();
                return true;
            }
        });

        recyclerAlbum.setAdapter(adapter);
    }

    /**
     * Funzione data dall'implementazione dell'interfaccia ISelectData che permette di ricevere un messaggio
     * di ritorno da un fragment che invochi con
     * @param string parametro che il fragment ritorna
     */
    @Override
    public void onSelectedData(String string) {
        switch (string) {
            case Vars.FRAGMENT_ALBUM_DELETE:
                buildRecycler();
                break;
        }
    }

    /**
     * Funzione ausiliaria che chiede i permessi r/w e chiama lo store per leggere gli album
     */
    private void loadAlbums() {
        //55 char di descrizione
        StorageManager.checkAndAskForStoragePermission(this,getApplicationContext());
        try {
            listAlbum = store.readAlbumList();
        } catch(AlbumsNotFoundException e) {
            listAlbum = new ArrayList<>();
            Toast.makeText(getApplicationContext(),"Non ci sono album, inizia creandone uno",Toast.LENGTH_SHORT).show();
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
