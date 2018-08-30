package it.unipr.marco.fotodiario.utility;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import it.unipr.marco.fotodiario.data.Album;

import it.unipr.marco.fotodiario.R;

public class AlbumAdapter
        extends RecyclerView.Adapter<AlbumAdapter.ViewHolderAlbum>
        implements View.OnClickListener, View.OnLongClickListener {

    private ArrayList<Album> listAlbum;
    private View.OnClickListener listener;
    private View.OnLongClickListener longListener;

    public AlbumAdapter(ArrayList<Album> listAlbum) {
        this.listAlbum = listAlbum;
    }

    @NonNull
    @Override
    public ViewHolderAlbum onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout;
        if(Vars.album_visualization == Vars.LIST) {
            layout = R.layout.item_list_album;
        }
        else {
            layout = R.layout.item_grid_album;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent,false);

        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        return new ViewHolderAlbum(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderAlbum holder, int position) {
        holder.nome.setText(listAlbum.get(position).getName());
        if(Vars.album_visualization == Vars.LIST) {
            holder.desc.setText(listAlbum.get(position).getDesc());
        }
        //holder.img.setImageResource(listAlbum.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return listAlbum.size();
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

    class ViewHolderAlbum extends RecyclerView.ViewHolder{

        TextView nome, desc;
        //ImageView img;

        ViewHolderAlbum(View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.albumName);
            if(Vars.album_visualization == Vars.LIST) {
                desc = itemView.findViewById(R.id.albumDesc);
            }
            //img = itemView.findViewById(R.id.albumImg);
        }
    }
}
