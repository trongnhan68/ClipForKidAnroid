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
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.plus.PlusClient;
import com.nhannlt.kidmovie.lazylist.ImageLoader;


import com.nhannlt.kidmovie.util.VideoData;

import java.util.List;

/**
 * @author Ibrahim Ulukaya <ulukaya@google.com>
 *         <p/>
 *         Left side fragment showing user's uploaded YouTube videos.
 */
public class VideosListViewFragment extends Fragment implements ConnectionCallbacks,
        OnConnectionFailedListener {

    private static final String TAG = VideosListViewFragment.class.getName();
    private Callbacks mCallbacks;
    private ImageLoader mImageLoader;
    private PlusClient mPlusClient;
    private ListView mListView;

    public VideosListViewFragment() {
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
        View listView = inflater.inflate(R.layout.listview_fragment, container, false);
        mListView = (ListView) listView.findViewById(R.id.list_view);
        TextView emptyView = (TextView) listView.findViewById(android.R.id.empty);
        mListView.setEmptyView(emptyView);
        return listView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //setProfileInfo();
    }

    public void setVideos(List<VideoData> videos) {
        if (!isAdded()) {
            return;
        }

        mListView.setAdapter(new FavoriteVideoAdapter(videos));
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
        if (mListView.getAdapter() != null) {
            ((FavoriteVideoAdapter) mListView.getAdapter()).notifyDataSetChanged();
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
        mImageLoader = mCallbacks.onGetImageLoader();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
        mImageLoader = null;
    }

    public interface Callbacks {
        ImageLoader onGetImageLoader();

        void onVideoFavoriteSelected(VideoData video);

        void onConnected(String connectedAccountName);
    }
  public  void SetViewInvisible ()
  {

      getView().setVisibility(View.INVISIBLE);
  }
    private class FavoriteVideoAdapter extends BaseAdapter {
        private List<VideoData> mVideos;

        private FavoriteVideoAdapter(List<VideoData> videos) {
            mVideos = videos;
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

            /*if (mPlusClient.isConnected()) {
                ((PlusOneButton) convertView.findViewById(R.id.plus_button))
                        .initialize(video.getWatchUri(), null);
            }*/
            convertView.findViewById(R.id.main_target).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            mCallbacks.onVideoFavoriteSelected(mVideos.get(position));
                        }
                    });
            return convertView;
        }
    }
}
