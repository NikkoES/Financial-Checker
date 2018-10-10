package com.luckynineapps.financialchecker.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.luckynineapps.financialchecker.R;
import com.luckynineapps.financialchecker.activity.common.SpendingActivity;
import com.luckynineapps.financialchecker.model.Spending;
import com.luckynineapps.financialchecker.utils.CurrencyEditText;

import java.util.List;

public class SpendingAdapter extends RecyclerView.Adapter<SpendingAdapter.ListMenuViewHolder> {

    private Context context;
    private final List<Spending> listMenu;

    public SpendingAdapter(Context context, List<Spending> listMenu) {
        this.context = context;
        this.listMenu = listMenu;
    }

    @Override
    public ListMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_menu, null, false);

        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mItemView.setLayoutParams(layoutParams);

        return new ListMenuViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(ListMenuViewHolder holder, int position) {
        final Spending mCurrent = listMenu.get(position);
        holder.judul.setText(mCurrent.description);
        holder.harga.setText(CurrencyEditText.currencyFormat(Long.parseLong(mCurrent.nominal)));
        Glide.with(context).
                load("").
                placeholder(R.drawable.pengeluaranlainnya).
                into(holder.imgMenu);
        holder.imgPen.setImageResource(R.drawable.redpen);
    }

    @Override
    public int getItemCount() {
        return listMenu.size();
    }

    public class ListMenuViewHolder extends RecyclerView.ViewHolder {
        private TextView judul,harga;
        private ImageView imgMenu,imgPen;

        final SpendingAdapter mAdapter;

        private InterstitialAd interstitialAd;

        public ListMenuViewHolder(View itemView, SpendingAdapter adapter) {
            super(itemView);
            judul = itemView.findViewById(R.id.tv_judul);
            harga = itemView.findViewById(R.id.tv_harga);
            imgMenu = itemView.findViewById(R.id.iv_menu);
            imgPen = itemView.findViewById(R.id.iv_pen);
            this.mAdapter = adapter;

            MobileAds.initialize(context, "ca-app-pub-3940256099942544~3347511713");
            interstitialAd = new InterstitialAd(context);
            interstitialAd.setAdUnitId(context.getString(R.string.ad_id_interstitial));
            interstitialAd.loadAd(new AdRequest.Builder().build());
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    showInputDialog();
                }
            });

            imgPen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getAdapterPosition()==2){
                        if (interstitialAd != null && interstitialAd.isLoaded()) {
                            interstitialAd.show();
                        }
                        else {
                            showInputDialog();
                        }
                    }
                    else {
                        showInputDialog();
                    }
                }
            });

        }

        public void showInputDialog() {

            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(context);
            alertDialogBuilder.setView(promptView);

            final EditText etKeterangan = (EditText) promptView.findViewById(R.id.et_keterangan);
            final EditText etNominal = (EditText) promptView.findViewById(R.id.et_nominal);

            new CurrencyEditText(etNominal);

            final Spending mCurrent = listMenu.get(getAdapterPosition());
            etKeterangan.setText(mCurrent.description);
            etNominal.setText(mCurrent.nominal);

            final Long idCurrent = mCurrent.getId();
            Log.e("ID", ""+idCurrent);

            final Spending spending = new Select().from(Spending.class).where("id = ?", idCurrent).executeSingle();

            alertDialogBuilder.setCancelable(true)
                    .setPositiveButton(context.getString(R.string.simpan), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            spending.description = etKeterangan.getText().toString();
                            spending.nominal = etNominal.getText().toString().replace(".", "");
                            spending.save();

                            judul.setText(etKeterangan.getText());
                            harga.setText(String.valueOf(CurrencyEditText.currencyFormat(Long.parseLong(etNominal.getText().toString().replace(".", "")))));
                            notifyDataSetChanged();

                            ((Activity)context).finish();
                            context.startActivity(new Intent(context, SpendingActivity.class));
                        }
                    })
                    .setNeutralButton(context.getString(R.string.hapus), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Spending.delete(Spending.class, idCurrent);

                            listMenu.remove(getAdapterPosition());
                            notifyDataSetChanged();

                            ((Activity)context).finish();
                            context.startActivity(new Intent(context, SpendingActivity.class));
                        }
                    })
                    .setNegativeButton(context.getString(R.string.batal), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            android.app.AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }

    }
}