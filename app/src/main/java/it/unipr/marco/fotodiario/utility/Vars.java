package it.unipr.marco.fotodiario.utility;

public class Vars {
    //TODO studia se puoi/conviene metterle nelle sharedPreferences

    //Variabili per il pinchZoomPan
    public static final int INVALID_POINTER_ID = -1;

    //Variabili e codici statici della GUI
    public static final int LIST = 1;
    public static final int GRID = 2;
    public static int album_visualization = LIST;
    public static int picture_visualization = GRID;

    //Codici statici per gli intent
    public static final int SUBACTIVITY_ALBUM_CREATE = 1;
    public static final int SUBACTIVITY_ALBUM_EDIT = 2;
    public static final int SUBACTIVITY_IMAGE_VIEW = 3;
    public static final int SUBACTIVITY_IMAGE_CAPTURE = 4;
    public static final int SUBACTIVITY_IMAGE_SAVE = 5;

    public static final String FRAGMENT_ALBUM_DELETE = "ALBUM_DELETE";
    public static final String FRAGMENT_PICTURE_ADD_DESC = "PICTURE_ADD_DESC";
    public static final String FRAGMENT_PICTURE_DELETE = "FRAGMENT_PICTURE_DELETE";
    public static final String FRAGMENT_PICTURE_INFO = "FRAGMENT_PICTURE_INFO";
    public static final String ALBUM_NAME = "ALBUM_NAME";
    public static final String PICTURE_DESC = "ALBUM_DESC";
    public static final String PICTURE_FILE_NAME_TEMP = "PICTURE_FILE_NAME_TEMP";
    public static final String PICTURE_NAME = "PICTURE_NAME";
    public static final String PICTURE_GPS_LATITUDE = "PICTURE_GPS_LATITUDE";
    public static final String PICTURE_GPS_LONGITUDE = "PICTURE_GPS_LONGITUDE";
    public static final String BOOL = "BOOL";

    //Codici statici di permesso (belli sti letterali)
    public static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    public static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 2;
}
