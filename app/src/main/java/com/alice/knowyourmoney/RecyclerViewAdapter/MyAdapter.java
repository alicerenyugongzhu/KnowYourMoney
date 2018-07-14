package com.alice.knowyourmoney.RecyclerViewAdapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alice.knowyourmoney.R;
import com.alice.knowyourmoney.database.AccountComment;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    List<AccountComment> ac = new ArrayList<AccountComment>();
    private MyItemClickListener mItemClickListener;


    public MyAdapter(List<AccountComment> ac){
        this.ac = ac;
    }

    public void setData(List<AccountComment> ac){
        this.ac = ac;
    }

    public void DataReloadAll(){
        notifyDataSetChanged();
    }

    public void DataReload(){
        //this.ac.add(acItem);
        notifyItemInserted(this.ac.size());
        //notifyItemInserted(this.ac.size());
    }

    public interface MyItemClickListener {
        void onItemClick(View view, int position);
        boolean onItemLongClick(View view, int position);
    }

    public void SetItemClickListener(MyItemClickListener listener){
        this.mItemClickListener = listener;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_account, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Log.d("alice_debug", "position is " + position);
        Log.d("alice_debug", "Date is this position is " + ac.get(position).getDate());
        holder.rdReason.setText(ac.get(position).getReason());
        holder.rdPrice.setText(Float.toString(ac.get(position).getPrice()));
        holder.rdDate.setText(ac.get(position).getDate());

    }

    @Override
    public int getItemCount() {
        return ac.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        TextView rdReason;
        TextView rdPrice;
        TextView rdDate;

        public ViewHolder(View view){
            super(view);
            rdReason = (TextView)view.findViewById(R.id.recordReason);
            rdPrice = (TextView)view.findViewById(R.id.recordPrice);
            rdDate = (TextView)view.findViewById(R.id.recordDate);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

        }

        @Override
        public boolean onLongClick(View v) {
            if(mItemClickListener != null){
                mItemClickListener.onItemLongClick(v, getLayoutPosition());
                return true;
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            if(mItemClickListener != null){
                mItemClickListener.onItemClick(v, getLayoutPosition());
            }
        }
    }

}
