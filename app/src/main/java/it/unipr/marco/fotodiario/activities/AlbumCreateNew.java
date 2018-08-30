package it.unipr.marco.fotodiario.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import it.unipr.marco.fotodiario.R;
import it.unipr.marco.fotodiario.data.Album;
import it.unipr.marco.fotodiario.exceptions.AlbumAlreadyExistsException;
import it.unipr.marco.fotodiario.exceptions.AlbumsNotFoundException;
import it.unipr.marco.fotodiario.utility.StorageManager;
import it.unipr.marco.fotodiario.utility.Utility;
import it.unipr.marco.fotodiario.utility.Vars;

public class AlbumCreateNew extends AppCompatActivity
    implements View.OnClickListener {

    private EditText editName;
    private EditText editDesc;

    private ArrayList<Album> listAlbum;
    private StorageManager store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_create_new);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        store = new StorageManager(getApplicationContext());

        editName = findViewById(R.id.textAlbumName);
        editDesc = findViewById(R.id.textAlbumDesc);

        //Movimento con tab
        editName.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editDesc.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        try{
            listAlbum = store.readAlbumList();
        } catch(AlbumsNotFoundException e) {
            listAlbum = new ArrayList<>();
        }
    }

    /*
     * Ricordati che se modifichi qua sotto ti tocca anche cambiare AlbumEdit in quanto è copia 1:1
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonAlbumSave:
                StorageManager.checkAndAskForStoragePermission(this,getApplicationContext());
                //Il nome non deve essere vuoto
                String nomeAlbum = Utility.cleanFileName(editName.getText().toString());
                String descrizioneAlbum = editDesc.getText().toString();
                TextView labelErrore = findViewById(R.id.labelAlbumError);
                if(nomeAlbum.isEmpty()){
                    labelErrore.setTextColor(getResources().getColor(R.color.colorAccent));
                    labelErrore.setText(R.string.errorAlbumNameEmpty);
                }
                else{
                    labelErrore.setText("");
                    listAlbum.add(new Album(nomeAlbum, descrizioneAlbum, new Date()));
                    try{
                        store.createAlbumFolder(nomeAlbum);
                    } catch(AlbumAlreadyExistsException e) {
                        //Osservazione per il prof: android senza permessi in lettura espliciti su sdcard
                        //può leggere il nome delle cartelle in quanto qua dentro ci arrivo lo stesso anche
                        //senza avere i permessi di WRITE
                        labelErrore.setText(R.string.errorAlbumNameAlreadyExists);
                        return;
                    }
                    store.writeAlbumList(listAlbum);
                    setResultOk();
                }
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
                    setResultError();
            }
        }
    }

    /**
     * Funzione ausiliaria per terminare con successo
     */
    private void setResultOk(){
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Funzione ausiliaria per terminare con fallimento
     */
    private void setResultError(){
        setResult(RESULT_CANCELED);
        finish();
    }
}
