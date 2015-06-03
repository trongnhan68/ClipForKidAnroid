package com.nhannlt.kidmovie.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.nhannlt.kidmovie.R;

/**
 * Created by nhan on 6/3/2015.
 */
public class ListMenuAdapter extends BaseAdapter {

    Context mContext;
    Callback mCallback;

    public ListMenuAdapter(Context mContext) {
        this.mContext = mContext;
        try {
            this.mCallback = ((Callback) mContext);
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AdapterCallback.");
        }
    }

    public interface Callback {

        void onSwitch(boolean status);
        void onChooseAccount();
        void onChangeContent();
        void onAbout();
        void onRemoveAd();
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Object getItem(int i) {

        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup container) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_menu, container, false);
        }

        switch (i) {
            case 0:
                ((TextView) convertView.findViewById(R.id.txtView_menuitem_name))
                        .setText("Loop");
                ((Switch) convertView.findViewById(R.id.switch_replay)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                       mCallback.onSwitch(b);
                    }
                });


                break;
            case 1:
                ((TextView) convertView.findViewById(R.id.txtView_menuitem_name))
                        .setText("Accounts");
                ((Switch) convertView.findViewById(R.id.switch_replay)).setVisibility(View.GONE);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCallback.onChooseAccount();
                    }
                });
                break;
            case 2:
                ((TextView) convertView.findViewById(R.id.txtView_menuitem_name))
                        .setText("Remove Ad");
                ((Switch) convertView.findViewById(R.id.switch_replay)).setVisibility(View.GONE);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCallback.onRemoveAd();
                    }
                });
                break;
            case 3:
                ((TextView) convertView.findViewById(R.id.txtView_menuitem_name))
                        .setText("Content");
                ((Switch) convertView.findViewById(R.id.switch_replay)).setVisibility(View.GONE);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCallback.onChangeContent();
                    }
                });
                break;

            case 4:
                ((TextView) convertView.findViewById(R.id.txtView_menuitem_name))
                        .setText("About");
                ((Switch) convertView.findViewById(R.id.switch_replay)).setVisibility(View.GONE);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCallback.onAbout();
                    }
                });
                break;

        }

        return convertView;
    }
}

