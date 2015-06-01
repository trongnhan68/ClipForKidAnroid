/*
 * Copyright (c) 2013 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.nhannlt.kidmovie;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.plus.PlusClient;
import com.nhannlt.kidmovie.lazylist.ImageLoader;

import com.nhannlt.kidmovie.util.ImageFetcher;
import com.nhannlt.kidmovie.util.ImageWorker;
import com.nhannlt.kidmovie.util.Utils;
import com.nhannlt.kidmovie.util.VideoData;

import java.util.List;

/**
 * @author Ibrahim Ulukaya <ulukaya@google.com>
 *         <p/>
 *         Left side fragment showing user's uploaded YouTube videos.
 */
public class VideosGridViewFragment extends Fragment implements ConnectionCallbacks,
        OnConnectionFailedListener {

    private static final String TAG = VideosGridViewFragment.class.getName();
    private Callbacks mCallbacks;
    private ImageLoader mImageLoader;
    private PlusClient mPlusClient;
    private GridView mGridView;
    public float widthOfGirdView;
    private boolean isLoading = false;
    ListVideoAdapter videoAdapter;
    public int currentFirstVisibleItem,currentVisibleItemCount,currentScrollState ;

    public VideosGridViewFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*mPlusClient = new PlusClient.Builder(getActivity(), this, this)
                .setScopes(Auth.SCOPES)
                .build();*/

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View listView = inflater.inflate(R.layout.list_fragment, container, false);
        mGridView = (GridView) listView.findViewById(R.id.grid_view);
        TextView emptyView = (TextView) listView.findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyView);
    return listView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //setProfileInfo();

    }

    public void setIsLoading (boolean isloading){

        this.isLoading = isloading;
    }
    public void setVideos(final List<VideoData> videos) {
        if (!isAdded()) {
            return;
        }
        if (videoAdapter == null) {
             videoAdapter = new ListVideoAdapter(videos);

            mGridView.setAdapter(videoAdapter);
            if (videoAdapter.getCount() < 3)
                isLoading = true;
        } else
        {
            videoAdapter.setVideos(videos);
            videoAdapter.notifyDataSetChanged();

        }


        mGridView.setOnScrollListener(new AbsListView.OnScrollListener(){
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if(firstVisibleItem + visibleItemCount >= totalItemCount && !isLoading) {
                    // End has been reached
                    Log.d("GridView scroll: ", ""+firstVisibleItem + " " + visibleItemCount + " "+totalItemCount);
                    mCallbacks.onLoadMore();
                    isLoading = true;
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState){
                Log.d("GridView scroll: ", ""+scrollState);
            }
        });


    }

    public void setProfileInfo() {
        /*if (!mPlusClient.isConnected() || mPlusClient.getCurrentPerson() == null) {
            ((ImageView) getView().findViewById(R.id.avatar))
                    .setImageDrawable(null);
            ((TextView) getView().findViewById(R.id.display_name))
                    .setText(R.string.not_signed_in);
        } else {
            Person currentPerson = mPlusClient.getCurrentPerson();
            if (currentPerson.hasImage()) {
                mImageFetcher.loadImage(currentPerson.getImage().getUrl(),
                        ((ImageView) getView().findViewById(R.id.avatar)));
            }
            if (currentPerson.hasDisplayName()) {
                ((TextView) getView().findViewById(R.id.display_name))
                        .setText(currentPerson.getDisplayName());
            }
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
       // mPlusClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
       // mPlusClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mGridView.getAdapter() != null) {
            ((ListVideoAdapter) mGridView.getAdapter()).notifyDataSetChanged();
        }

        //setProfileInfo();
       // mCallbacks.onConnected(mPlusClient.getAccountName());
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            Toast.makeText(getActivity(),
                    R.string.connection_to_google_play_failed, Toast.LENGTH_SHORT)
                    .show();

            Log.e(TAG,
                    String.format(
                            "Connection to Play Services Failed, error: %d, reason: %s",
                            connectionResult.getErrorCode(),
                            connectionResult.toString()));
            try {
                connectionResult.startResolutionForResult(getActivity(), 0);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, e.toString(), e);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new ClassCastException("Activity must implement callbacks.");
        }

        mCallbacks = (Callbacks) activity;
       // mImageFetcher = mCallbacks.onGetImageFetcher();
        mImageLoader =  mCallbacks.onGetImageLoader();

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
        mImageLoader= null;
        //mImageFetcher = null;

    }

    public interface Callbacks {
        ImageFetcher onGetImageFetcher();
        ImageLoader onGetImageLoader();
        void onVideoSelected(VideoData video);
        void onLoadMore();

    }

    private class ListVideoAdapter extends BaseAdapter {
        private List<VideoData> mVideos;

        private ListVideoAdapter(List<VideoData> videos) {
            mVideos = videos;

        }
        public void setVideos (List<VideoData> videos){

            this.mVideos = videos;

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
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.list_item, container, false);
            }


            VideoData video = mVideos.get(position);
            ((TextView) convertView.findViewById(R.id.txtview_videoNameInItem))
                    .setText(video.getTitle());
//            mImageFetcher.loadImage(video.getThumbUri(),
//                    (ImageView) convertView.findViewById(R.id.thumbnail));
            mImageLoader.DisplayImage(video.getThumbUri(),
                    (ImageView) convertView.findViewById(R.id.thumbnail));
            ((TextView) convertView.findViewById(R.id.txtView_videoDuratiion))
                    .setText(Utils.timeHumanReadable(video.getVideo().getContentDetails().getDuration()));
            try {
                final AbsListView.LayoutParams params = (AbsListView.LayoutParams) convertView.getLayoutParams();
                params.height = container.getWidth()/3-20;
                convertView.setLayoutParams(params);
            }

            catch(Exception ex) {}
            /*if (mPlusClient.isConnected()) {
                ((PlusOneButton) convertView.findViewById(R.id.plus_button))
                        .initialize(video.getWatchUri(), null);
            }*/
            convertView.findViewById(R.id.main_target).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            mCallbacks.onVideoSelected(mVideos.get(position));
                        }
                    });
            return convertView;
        }

    }
}
