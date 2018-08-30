package it.unipr.marco.fotodiario.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class PinchZoomPanView extends View {

    private Bitmap mBitmap;
    private int mImageHeight;
    private int mImageWidth;

    private float mPositionX;
    private float mPositionY;
    private float mLastTouchX;
    private float mLastTouchY;

    private int mActivePointerID = Vars.INVALID_POINTER_ID; //Fatto per il fix al multitouch mentre trascini

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.0f;
    private final static float mMinZoom = 1.0f;
    private final static float mMaxZoom = 4.0f;

    public PinchZoomPanView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Lo ScaleGestureDetector deve ispezionare ogni OnTouchEvent
        mScaleDetector.onTouchEvent(event);

        final int action = event.getAction();

        switch(action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN: {

                final float x = event.getX();
                final float y = event.getY();

                //Memorizzo la posizione per fare lo spostamento quando muoverò il dito
                mLastTouchX = x;
                mLastTouchY = y;

                //Memorizzo l'id del pointer per il fix al multitouch mentre trascini
                mActivePointerID = event.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE: {

                //Prendo l'id del pointer per il fix al multitouch mentre trascini
                final int pointerIndex = event.findPointerIndex(mActivePointerID);
                //Prendo la posizione di dove devo mettere l'immagine in base al pointer attivo
                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);

                //if(!mScaleDetector.isInProgress()) {

                    //Calcolo lo spostamento dell'immagine
                    final float distanceX = x - mLastTouchX;
                    final float distanceY = y - mLastTouchY;

                    //Sposto l'immagine
                    mPositionX += distanceX;
                    mPositionY += distanceY;

                    //Ridisegno l'immagine
                    invalidate();
                //}

                //Memorizzo l'attuale posizione per il prossimo ACTION_MOVE
                mLastTouchX = x;
                mLastTouchY = y;
                break;
            }
            case MotionEvent.ACTION_UP: {
                //Fatto per il fix al multitouch mentre trascini
                mActivePointerID = Vars.INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                //Fatto per il fix al multitouch mentre trascini
                mActivePointerID = Vars.INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                //Estraggo l'index del pointer che ha lasciato lo schermo
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerID = event.getPointerId(pointerIndex);
                if(pointerID == mActivePointerID) {
                    //Il puntatore attivo ha lasciato lo schermo
                    //quindi ne dobbiamo scegliere un altro e aggiustare i calcoli
                    //per il fix al multitouch mentre trascini
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = event.getX(newPointerIndex);
                    mLastTouchY = event.getY(newPointerIndex);
                    mActivePointerID = event.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mBitmap != null) {
            canvas.save();

            //Blocco il pan in modo che non puoi fare uscire la foto dallo schermo
            if((mPositionX * -1) < 0)
                mPositionX = 0;
            else if((mPositionX * -1) > mImageWidth * mScaleFactor - getWidth())
                mPositionX = (mImageWidth * mScaleFactor - getWidth()) * -1;
            if((mPositionY * -1) < 0)
                mPositionY = 0;
            else if((mPositionY * -1) > mImageHeight * mScaleFactor - getHeight())
                mPositionY = (mImageHeight * mScaleFactor - getHeight()) * -1;
            if((mImageHeight * mScaleFactor) <getHeight())
                mPositionY = 0;
            //Abilito il pan
            canvas.translate(mPositionX, mPositionY);
            //Abilito lo zoom
            canvas.scale(mScaleFactor, mScaleFactor);
            //Disegno l'immagine
            canvas.drawBitmap(mBitmap, 0, 0, null);
            canvas.restore();
        }
    }

    public void loadImageOnCanvas(String pictureNameTemp, String albumName) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(StorageManager.getBaseDirectory()+"/"
                +albumName+"/"
                +pictureNameTemp+".jpg", bmOptions);

        float aspectRatio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mImageWidth = displayMetrics.widthPixels;
        mImageHeight = Math.round(mImageWidth * aspectRatio);
        mBitmap = Bitmap.createScaledBitmap(bitmap, mImageWidth, mImageHeight, false);
        invalidate();//Chiamo onDraw
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            float variation = scaleGestureDetector.getScaleFactor();
            // Scalo l'immagine
            mScaleFactor *= variation;
            // Impedisco che l'immagine diventi troppo grande o troppo piccola
            mScaleFactor = Math.max(mMinZoom, Math.min(mScaleFactor, mMaxZoom));
            // Aggiusto lo zoom nel punto in cui faccio il pinch
            final float centerX = scaleGestureDetector.getFocusX();
            final float centerY = scaleGestureDetector.getFocusY();
            float diffX = centerX - centerX*variation;
            float diffY = centerY - centerY*variation;
            mPositionX += diffX;
            mPositionY += diffY;
            // Ridisegno
            invalidate();
            return true;
        }
    }
}
