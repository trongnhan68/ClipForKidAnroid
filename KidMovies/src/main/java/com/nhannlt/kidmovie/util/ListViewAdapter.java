package com.nhannlt.kidmovie.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nhannlt.kidmovie.FavoriteData;
import com.nhannlt.kidmovie.R;
import com.nhannlt.kidmovie.lazylist.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nhan on 5/28/2015.
 */
public class ListViewAdapter extends BaseAdapter {
    private List<FavoriteData> mVideos = new ArrayList<FavoriteData>();
    private ImageLoader mImageLoader;
private Context mActivity;
private  CallbackFavoriteView callback;
private int heightScreen;
    public ListViewAdapter(Context context, List<FavoriteData> videos,int heightScreen) {
        this.mActivity = context;
        this.mVideos = videos;
        this.heightScreen = heightScreen;
        mImageLoader = new ImageLoader(this.mActivity);
        //this.callback = mActivity.get
        //mActivity.
        try {
            this.callback = ((CallbackFavoriteView)context);
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AdapterCallback.");
        }


    }
    public  void setVideos(List<FavoriteData> videos){

        this.mVideos = videos;
    }
    public interface CallbackFavoriteView {
        void onDeleteItem(String videoId);
        void onVideoFavoriteSelected(FavoriteData video);

    }
    @Override
    public int getCount() {
        return mVideos.size();
    }

    @Override
    public Object getItem(int i) {
        return mVideos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mVideos.get(i).getVideoId().hashCode();
    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup container) {
        ViewHolderItem viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(
                    R.layout.item_favorite, container, false);
            viewHolder = new ViewHolderItem();
            viewHolder.textViewDuration = (TextView) convertView.findViewById(R.id.txtView_videoDuratiion);
            viewHolder.textViewVideoName = (TextView) convertView.findViewById(R.id.txtview_videoNameInItem);
            viewHolder.thumbnail =  (ImageView) convertView.findViewById(R.id.thumbnail);
            viewHolder.btnDelete = (ImageButton) convertView.findViewById(R.id.btn_delete);

            convertView.setTag(viewHolder);
        } else {

            viewHolder = (ViewHolderItem) convertView.getTag();

        }
        try {
            final AbsListView.LayoutParams params = (AbsListView.LayoutParams) convertView.getLayoutParams();
            params.height = this.heightScreen/3;
            convertView.setLayoutParams(params);
        }

        catch(Exception ex) {}
        final FavoriteData video = mVideos.get(position);

        viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onDeleteItem(video.getVideoId());
            }
        });
        viewHolder.textViewVideoName
                .setText(video.getVideoName());

        mImageLoader.DisplayImage(video.getVideoUrl(),
                viewHolder.thumbnail);

        viewHolder.textViewDuration
                .setText(Utils.timeHumanReadable(video.getVideoDuration()));
        convertView.findViewById(R.id.main_target).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        callback.onVideoFavoriteSelected(mVideos.get(position));
                    }
                });
        return convertView;
    }

    static class ViewHolderItem {

        TextView textViewDuration;
        TextView textViewVideoName;
        ImageView thumbnail;
        ImageButton btnDelete;

    }

}

