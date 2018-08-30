package it.unipr.marco.fotodiario.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import it.unipr.marco.fotodiario.R;
import it.unipr.marco.fotodiario.data.Picture;

public class PictureAdapter
        extends RecyclerView.Adapter<PictureAdapter.ViewHolderPicture>
        implements View.OnClickListener, View.OnLongClickListener {

    private ArrayList<Picture> listPicture;
    private View.OnClickListener listener;
    private View.OnLongClickListener longListener;

    public PictureAdapter(ArrayList<Picture> listPicture) {
        this.listPicture = listPicture;
    }

    @NonNull
    @Override
    public ViewHolderPicture onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout;
        if(Vars.picture_visualization == Vars.LIST) {
            layout = R.layout.item_list_picture;
        }
        else {
            layout = R.layout.item_grid_picture;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent,false);

        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        return new ViewHolderPicture(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderPicture holder, int position) {
        if(Vars.picture_visualization == Vars.LIST) {
            holder.nome.setText(listPicture.get(position).getFileName());
            holder.desc.setText(listPicture.get(position).getDesc());
        }

        Bitmap thumbnail;
        File thumbnailFile = new File(StorageManager.getBaseDirectory()+"/"
                +listPicture.get(position).getAlbumParent()+"/"
                +".thumb"
                +listPicture.get(position).getFileName()+".jpg");
        // Salvo la miniatura se non esiste
        if(!thumbnailFile.exists()) {
            createThumbnail(position, thumbnailFile);
        }
        // Carico la miniatura definitivamente
        thumbnail = BitmapFactory.decodeFile(thumbnailFile.getAbsolutePath());

        holder.img.setImageBitmap(thumbnail);
    }

    /**
     * Funzione creata per alleggerire il metodo di disegno che crea la miniatura
     * @param position parametro preso dall'@override
     * @param thumbnailFile miniatura da creare
     */
    private void createThumbnail(int position, File thumbnailFile) {
        File fullsizeImage = new File(StorageManager.getBaseDirectory()+"/"
                +listPicture.get(position).getAlbumParent()+"/"
                +listPicture.get(position).getFileName()+".jpg");
        // Now we crop the image
        Bitmap thumbnail = BitmapFactory.decodeFile(fullsizeImage.getAbsolutePath());
        thumbnail = cropToSquare(thumbnail);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(thumbnailFile);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * La funzione ausiliaria fa il crop dell'immagine e la scala alla dimensione di una miniatura
     * @param bitmap imput
     * @return output
     */
    private Bitmap cropToSquare(Bitmap bitmap){
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0)? 0: cropH;
        Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
        cropImg = Bitmap.createScaledBitmap(cropImg, 250, 250, false);

        return cropImg;
    }

    @Override
    public int getItemCount() {
        return listPicture.size();
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if(listener != null) {
            listener.onClick(view);
        }
    }

    public void setOnLongClickListener(View.OnLongClickListener longListener) {
        this.longListener = longListener;
    }

    @Override
    public boolean onLongClick(View view) {
        if(longListener != null) {
            longListener.onLongClick(view);
        }
        return true;
    }

    class ViewHolderPicture extends RecyclerView.ViewHolder{

        TextView nome, desc;
        ImageView img;

        ViewHolderPicture(View itemView) {
            super(itemView);
            if(Vars.picture_visualization == Vars.LIST) {
                nome = itemView.findViewById(R.id.pictureName);
                desc = itemView.findViewById(R.id.pictureDesc);
            }
            img = itemView.findViewById(R.id.pictureImage);
        }
    }
}
