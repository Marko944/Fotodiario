package it.unipr.marco.fotodiario.utility;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import it.unipr.marco.fotodiario.data.Album;
import it.unipr.marco.fotodiario.data.Picture;
import it.unipr.marco.fotodiario.exceptions.AlbumAlreadyExistsException;
import it.unipr.marco.fotodiario.exceptions.AlbumsNotFoundException;
import it.unipr.marco.fotodiario.exceptions.PicturesNotFoundException;

public class StorageManager {

    private ContextWrapper contextWrapper;
    private String tempName;

    /**
     * Costruttore che necessita del contesto per avere accesso al path interno
     * */
    public StorageManager(Context c) {
        this.contextWrapper = new ContextWrapper(c);
    }

    public String getTempName() { return tempName; }
    public void setTempName(String s) {this.tempName = s; }

    /**
     * La funzione crea il file temporaneo e ne salva il nome internamente in modo da poterlo rinominare
     * in seguito o eventualmente cancellarlo
     * @param album Album a cui ci si riferisce
     * @param picture Immagine da salvare
     * @return -
     * @throws IOException -
     */
    public File createTempImageFile(Album album, Picture picture) throws IOException {
        String imageFileName = picture.getFileName();
        File storageDir = getAlbumFolder(album);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        String[]  temp = image.toString().split("/");
        this.tempName = temp[temp.length-1];
        temp = this.tempName.split("\\.");
        this.tempName = temp[0];
        return image;
    }

    /**
     * La funziona cancella il file temporaneo se l'utente non vuole salvarlo
     * @param album -
     */
    public void deleteTempImageFile(Album album) {
        String sTarget = getAlbumFolder(album)+"/"+this.tempName+".jpg";
        deleteRecursive(new File(sTarget));
    }

    /**
     * La funzione cancella l'immagine e la sua miniatura
     * @param a Album in cui si trova l'immagine
     * @param p Picture
     */
    public void deleteImageFile(Album a, Picture p) {
        String sTarget = getAlbumFolder(a)+"/"+p.getFileName()+".jpg";
        String sTargetThumb = getAlbumFolder(a)+"/.thumb"+p.getFileName()+".jpg";
        deleteRecursive(new File(sTarget));
        deleteRecursive(new File(sTargetThumb));
    }

    /**
     * La funzione crea il vero file immagine e ci aggiunge le informazioni exif
     * @param album -
     * @param picture -
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void createImageFile(Album album, Picture picture) {
        //rename
        File oldFile = new File(getAlbumFolder(album)+"/"+this.tempName+".jpg");
        oldFile.renameTo(new File(getAlbumFolder(album)+"/"+picture.getFileName()+".jpg"));
    }

    /**
     * La funzione cancella un intero album, si suppone fortemente che non venga chiamata a caso
     * @param albumName nome dell'album da cancellare
     */
    public void deleteAlbum(String albumName) {
        try {
            ArrayList<Album> listAlbum = readAlbumList();
            ArrayList<Album> listAlbumEdited = readAlbumList();
            for(Album a : listAlbum) {
                if(a.equals(new Album(albumName))) {
                    listAlbumEdited.remove(a);
                }
            }
            writeAlbumList(listAlbumEdited);
            deleteRecursive(new File(getBaseDirectory()+"/"+albumName));
        } catch(AlbumsNotFoundException e) {/*Do nothing*/}
    }

    /**
     * La funzione crea una cartella con il nome che riceve
     * @param albumName nome dell'album da creare
     */
    public void createAlbumFolder(String albumName) throws AlbumAlreadyExistsException{
        File albumFolder = getAlbumFolder(new Album(albumName));
        if (!albumFolder.exists()) {
            if(albumFolder.mkdirs()){
                Log.i("DEBUG", "La cartella "+albumFolder+" è stata creata.");
            }else{
                Log.i("DEBUG", "La catella "+albumFolder+" non è stata creata.");
                throw new AlbumAlreadyExistsException("Errore generico nella creazione cartella");
            }
        } else{
            Log.i("DEBUG", "La cartella "+albumFolder+" esiste già.");
            throw new AlbumAlreadyExistsException("La cartella esiste già");
        }
    }

    /**
     * La funzione rinomina un album
     * @param albumName nuovo nome
     * @param albumNameOld vecchio nome
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void editAlbumFolder(String albumName, String albumNameOld) throws AlbumAlreadyExistsException{
        File albumFolder = getAlbumFolder(new Album(albumName));
        File albumFolderOld = getAlbumFolder(new Album(albumNameOld));
        if (!albumFolder.exists()) {
            albumFolderOld.renameTo(albumFolder);
        } else{
            Log.i("DEBUG", "La cartella "+albumFolder+" esiste già.");
            throw new AlbumAlreadyExistsException("La cartella esiste già");
        }
    }

    /**
     * La funzione scrive il file albums.txt che contiene i dati degli album nel parametro di ingresso
     * @param listAlbum lista degli album
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void writeAlbumList(ArrayList<Album> listAlbum) {
        String fileContents = fromArrayListToStringAlbum(listAlbum);
        File outputFile = new File(getBaseDirectory() + "/" + "albums.txt");
        try{
            if(outputFile.exists()) {
                Log.i("DEBUG", "Il file albums.txt esiste già, lo aggiorno");
                outputFile.delete();
            }
                if(outputFile.createNewFile()) {
                    FileOutputStream fos = new FileOutputStream(outputFile);
                    fos.write(fileContents.getBytes());
                    fos.close();
                }
        } catch (IOException e ){
            Log.w("ERROR", e.toString());
        }
    }

    /**
     * La funzione legge il file albums.txt e restituisce la sua struttura dati
     * @return ArrayList<Album> preso dal file di testo
     * @throws AlbumsNotFoundException eccezione custom
     */
    public ArrayList<Album> readAlbumList() throws AlbumsNotFoundException{
        File inputFile = new File(getBaseDirectory() + "/" + "albums.txt");
        String in;
        try{
            if(inputFile.exists()){
                FileInputStream fis = new FileInputStream(inputFile);
                StringBuilder fileContent = new StringBuilder("");
                byte[] buffer = new byte[1024];
                int n;
                while ((n = fis.read(buffer)) != -1) {
                    fileContent.append(new String(buffer, 0, n));
                }
                in = fileContent.toString();
                fis.close();
            }
            else{
                throw new AlbumsNotFoundException("Il file albums.txt non esiste");
            }
        } catch (IOException e){
            Log.w("ERROR", e.toString());
            throw new AlbumsNotFoundException("Errore generico di lettura del file albums.txt");
        }
        if(in.isEmpty()) {
            deleteRecursive(inputFile);
            throw new AlbumsNotFoundException("Il file albums.txt era vuoto per qualche motivo");
        }

        return fromStringToArrayListAlbum(in);
    }

    /**
     * La funzione scrive il file pictures.txt che contiene i dati delle foto
     * @param listPicture lista delle immagini
     * @param album nome dell'album
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void writePictureList(ArrayList<Picture> listPicture, Album album) {
        String fileContents = fromArrayListToStringPicture(listPicture);
        File outputFile = new File(getAlbumFolder(album)+"/"+"pictures.txt");
        try{
            if(outputFile.exists()) {
                Log.i("DEBUG", "Il file pictures.txt esiste già, lo aggiorno");
                outputFile.delete();
            }
            if(outputFile.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(outputFile);
                fos.write(fileContents.getBytes());
                fos.close();
            }
        } catch (IOException e ){
            Log.w("ERROR", e.toString());
        }
    }

    /**
     * La funzione legge il file descrittore della lista delle foto
     * @param album Da quale album leggere le foto
     * @return -
     * @throws PicturesNotFoundException -
     */
    public ArrayList<Picture> readPictureList(Album album) throws PicturesNotFoundException {
        File inputFile = new File(getAlbumFolder(album)+"/"+"pictures.txt");
        String in;
        try {
            if(inputFile.exists()){
                FileInputStream fis = new FileInputStream(inputFile);
                StringBuilder fileContent = new StringBuilder("");
                byte[] buffer = new byte[1024];
                int n;
                while ((n = fis.read(buffer)) != -1) {
                    fileContent.append(new String(buffer, 0, n));
                }
                in = fileContent.toString();
                fis.close();
            }
            else{
                throw new PicturesNotFoundException("Il file pictures.txt non esiste");
            }
        } catch (IOException e) {
            Log.w("ERROR", e.toString());
            throw new PicturesNotFoundException("Errore generico di lettura del file "+getAlbumFolder(album)+"pictures.txt");
        }
        if(in.isEmpty()) {
            deleteRecursive(inputFile);
            throw new PicturesNotFoundException("Il file pictures.txt era vuoto per qualche motivo");
        }
        return fromStringToArrayListPicture(in, album.getName());
    }

    /*
     *  =======================INIZIO METODI UTILITY PRIVATI DINAMICI===============================
     */

    /**
     * La funzione ritorna il file della cartella dell'album
     * @param album di cui volere la cartella
     * @return File dell'album
     */
    private File getAlbumFolder(Album album) {
        return new File(getBaseDirectory()+"/"+album.getName());
    }

    /**
     * Funzione ausiliaria che cancella un file o una cartella con tutto il suo contenuto
     * @param fileOrDirectory file o directory dalla quale cancellare tutto
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    /**
     * La funzione scrive l'arraylist di Album come una stringa
     * @param listAlbum lista degli album
     * @return String che contiene nome \n desc \n data
     */
    private String fromArrayListToStringAlbum(ArrayList<Album> listAlbum) {
        StringBuilder out = new StringBuilder();
        for (Album album : listAlbum) {
            out.append(album.getName()).append("\n");
            out.append(album.getDesc()).append("\n");
            out.append(album.getDate()).append("\n");
        }
        return out.toString();
    }

    /**
     * La funzione prende una stringa e restituisce la struttura dati degli album facendo il parsing
     * @param in Stringa da parsare
     * @return ArrayList di Album parsati
     */
    @SuppressWarnings("deprecation")
    private ArrayList<Album> fromStringToArrayListAlbum(String in) {
        ArrayList<Album> listAlbum = new ArrayList<>();
        String pezzi[] = in.split("\n");
        for(int i=0; i<pezzi.length;) {
            listAlbum.add(new Album(pezzi[i],pezzi[i+1],new Date(pezzi[i+2])));
            i+=3;
        }
        return listAlbum;
    }

    /**
     * La funzione scrive l'arraylist di Picture come una stringa
     * @param listPicture lista delle foto
     * @return String che contiene nome \n desc \n data
     */
    private String fromArrayListToStringPicture(ArrayList<Picture> listPicture) {
        StringBuilder out = new StringBuilder();
        for (Picture picture : listPicture) {
            out.append(picture.getFileName()).append("\n");
            out.append(picture.getDesc()).append("\n");
            out.append(picture.getLatitude()).append("\n");
            out.append(picture.getLongitude()).append("\n");
        }
        return out.toString();
    }

    /**
     * La funzione prende una stringa e restituisce la struttura dati delle foto facendo il parsing
     * @param in Stringa da parsare
     * @return ArrayList di Picture parsati
     */
    private ArrayList<Picture> fromStringToArrayListPicture(String in, String album) {
        ArrayList<Picture> listPicture = new ArrayList<>();
        String pezzi[] = in.split("\n");
        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        for(int i=0; i<pezzi.length;) {
            try {
                Picture p = new Picture(df.parse(pezzi[i]), album);
                p.setDesc(pezzi[i+1]);
                p.setLatitude(Float.parseFloat(pezzi[i+2]));
                p.setLongitude(Float.parseFloat(pezzi[i+3]));
                listPicture.add(p);
                i=i+4;
            } catch (ParseException e) {
                //Do nothing
            }
        }
        return listPicture;
    }

    /*
    *  =======================INIZIO METODI UTILITY STATICI=========================================
    */

    /**
     *  La funzione è ausiliaria
     * @return path della cartella DCIM + /AlbumsPhotoVideo
     */
    public static File getBaseDirectory(){
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        String fullPathDCIM = file.getAbsolutePath();
        return new File(fullPathDCIM+"/AlbumsPhotoVideo");
    }

    /**
     * La funzione controlla se si hanno i permessi di scrittura, in caso negativo li chiede
     * è premura del chiamante controllare tramite onRequestPermissionsResult se i permessi
     * sono stati dati o meno
     * @param a Activity che effettua la richiesta
     * @param c Contesto dell'app
     */
    public static void checkAndAskForStoragePermission(Activity a, Context c) {
        if(!isExternalStorageMounted())
            return;
        if(!checkPermission(c, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(a,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Vars.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    /**
     * La funzione controlla se si hanno i permessi di scrittura, in caso negativo li chiede
     * è premura del chiamante controllare tramite onRequestPermissionsResult se i permessi
     * sono stati dati o meno
     * @param a Activity che effettua la richiesta
     * @param c Contesto dell'app
     */
    public static boolean checkAndAskForGpsPermission(Activity a, Context c) {
        if(!checkPermission(c, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(a,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Vars.PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
        return checkPermission(c, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /**
     * Check se esiste la scheda SD Esterna (interna)
     * @return true se esiste la scheda sd interna, false altrimenti
     */
    private static boolean isExternalStorageMounted(){
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Checker di permessi
     * @param permission permesso da controllare
     * @return true se si ha il permesso, false altrimenti
     */
    private static boolean checkPermission(Context c, String permission){
        int check = ContextCompat.checkSelfPermission(c, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }
}
