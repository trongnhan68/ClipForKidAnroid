package com.nhannlt.kidmovie.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhannlt.kidmovie.R;
import com.nhannlt.kidmovie.lazylist.ImageLoader;

import java.util.List;

/**
 * Created by nhan on 5/28/2015.
 */
public class ListViewAdapter extends BaseAdapter {
    private List<VideoData> mVideos;
    private ImageLoader mImageLoader;
private Context mActivity;


    public ListViewAdapter(Context context, List<VideoData> videos) {
        this.mActivity = context;
        mVideos = videos;
        mImageLoader = new ImageLoader(mActivity);
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
        return mVideos.get(i).getYouTubeId().hashCode();
    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup container) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(
                    R.layout.list_item, container, false);
        }

        VideoData video = mVideos.get(position);
        ((TextView) convertView.findViewById(R.id.txtview_videoNameInItem))
                .setText(video.getTitle());
//            mImageFetcher.loadImage(video.getThumbUri(),
//                    (ImageView) convertView.findViewById(R.id.thumbnail));
        mImageLoader.DisplayImage(video.getThumbUri(),
                (ImageView) convertView.findViewById(R.id.thumbnail));

            /*if (mPlusClient.isConnected()) {
                ((PlusOneButton) convertView.findViewById(R.id.plus_button))
                        .initialize(video.getWatchUri(), null);
            }*/
        convertView.findViewById(R.id.main_target).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                       // mCallbacks.onVideoFavoriteSelected(mVideos.get(position));
                    }
                });
        return convertView;
    }
}
