package com.nhannlt.kidmovie.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.nhannlt.kidmovie.Constants;
import com.nhannlt.kidmovie.R;

import java.util.ArrayList;

/**
 * Created by nhan on 6/3/2015.
 */
public class ListMenuAdapter extends BaseAdapter {

    Context mContext;
    Callback mCallback;
    String location = "";
    ArrayList<String> listMenuItemName_VN , listMenuItemName_EN ;
    public ListMenuAdapter(Context mContext) {
        this.mContext = mContext;
        initString();
        try {
            this.mCallback = ((Callback) mContext);
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AdapterCallback.");
        }
    }

    public interface Callback {

        void onSwitch(boolean status);
        void onChooseAccount();
        void onChangeContent(String content);
        void onAbout();
        void onRemoveAd();
    }
   public void initString(){
       listMenuItemName_EN = new ArrayList<String>();
       listMenuItemName_VN = new ArrayList<String>();
       listMenuItemName_EN.add("Loop");
       listMenuItemName_EN.add("Accounts");
       listMenuItemName_EN.add("Remove Ad");
       listMenuItemName_EN.add("Content: English");
       listMenuItemName_EN.add("About");
       listMenuItemName_VN.add("Lặp");
       listMenuItemName_VN.add("Đăng Nhập");
       listMenuItemName_VN.add("Bỏ quảng cáo");
       listMenuItemName_VN.add("Nội dung:Tiếng Việt");
       listMenuItemName_VN.add("About");

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
        ArrayList<String> mListItem = new ArrayList<String>();
        SharedPreferences pre= mContext.getSharedPreferences("my_data", mContext.MODE_PRIVATE);
         location = pre.getString(Constants.PRE_LOCATION, "EN");
        if (location.equals("VN")) {
            mListItem.addAll(listMenuItemName_VN);

        } else {
            mListItem.addAll(listMenuItemName_EN);
        }

        switch (i) {
            case 0:
                ((TextView) convertView.findViewById(R.id.txtView_menuitem_name))
                        .setText(mListItem.get(0));
                ((Switch) convertView.findViewById(R.id.switch_replay)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                       mCallback.onSwitch(b);
                    }
                });


                break;
            case 1:
                ((TextView) convertView.findViewById(R.id.txtView_menuitem_name))
                        .setText(mListItem.get(1));
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
                        .setText(mListItem.get(2));
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
                        .setText(mListItem.get(3));
                ((Switch) convertView.findViewById(R.id.switch_replay)).setVisibility(View.GONE);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCallback.onChangeContent(location);
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

