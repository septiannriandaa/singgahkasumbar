package com.example.wisatasumbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wisatasumbar.Model.Wisata;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterClass extends RecyclerView.Adapter<AdapterClass.MyViewHolder>{
    ArrayList<Wisata> list;
    private OnClickListener mListener;
    public interface OnClickListener{
        void onItemClick(int position);
    }
    public void setOnItemClickListener(OnClickListener listener){
        mListener = listener;
    }
    public AdapterClass(ArrayList<Wisata> list){
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_holder,viewGroup,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
        holder.lokasi.setText(list.get(i).getLokasi());
        holder.namaWisata.setText(list.get(i).getNamaWisata());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView lokasi,namaWisata;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            lokasi = itemView.findViewById(R.id.txt_lokasi);
            namaWisata = itemView.findViewById(R.id.txt_namaWisata);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
