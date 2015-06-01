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

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnFullscreenListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.nhannlt.kidmovie.lazylist.ImageLoader;
import com.nhannlt.kidmovie.util.ImageFetcher;
import com.nhannlt.kidmovie.util.ListViewAdapter;
import com.nhannlt.kidmovie.util.Upload;
import com.nhannlt.kidmovie.util.Utils;
import com.nhannlt.kidmovie.util.VideoData;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Ibrahim Ulukaya <ulukaya@google.com>
 *         <p/>
 *         Main activity class which handles authorization and intents.
 */
public class MainActivity extends Activity implements
        VideosGridViewFragment.Callbacks, PlayerStateChangeListener, OnFullscreenListener, ListViewAdapter.CallbackFavoriteView {
    // private static final int MEDIA_TYPE_VIDEO = 7;
    private static final String YOUTUBE_FRAGMENT_TAG = "youtube";
    public  String TAG = "MAIN";


    private YouTubePlayer mYouTubePlayer;
    private boolean mIsFullScreen = false;
    JSONArray myFirstVideos = new JSONArray();
    JSONArray myPlayLists_EN = new JSONArray();
    JSONArray myPlayLists_VN = new JSONArray();
    // String preference
    public static final String PRE_IN_TAB = "IN_TAB";
    public static final String PRE_REGISTED = "REGISTED";
    public static final String PRE_LOCATION = "LOCATION";
    // BaseURLString
    public static final String  BaseURLStringDropBox_1 ="https://www.dropbox.com/s/msp70rmarezsjyw/VideoJson.txt?dl=1";
//static NSString * const BaseURLStringDropBox_2 =@"https://www.dropbox.com/s/msp70rmarezsjyw/VideoJson.txt?dl=1";
public static final String  BaseURLStringGoogle ="https://drive.google.com/uc?export=download&id=0B45IYpZpvVu-NGFqQXhEZmhVbVE";
    public static final String  BaseURLStringGit ="https://raw.githubusercontent.com/trongnhan68/Kid-Video/master/VideoJson.txt";

    public static final String ACCOUNT_KEY = "accountName";
    public static final String MESSAGE_KEY = "message";
    public static final String YOUTUBE_ID = "youtubeId";
    public static final String YOUTUBE_WATCH_URL_PREFIX = "http://www.youtube.com/watch?v=";
    static final String REQUEST_AUTHORIZATION_INTENT = "com.nhannlt.kidmovie.RequestAuth";
    static final String REQUEST_AUTHORIZATION_INTENT_PARAM = "com.nhannlt.kidmovie.RequestAuth.param";
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    private static final int REQUEST_GMS_ERROR_DIALOG = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 2;
    private static final int REQUEST_AUTHORIZATION = 3;
    private static final int RESULT_PICK_IMAGE_CROP = 4;
    private static final int RESULT_VIDEO_CAP = 5;
    private static final int REQUEST_DIRECT_TAG = 6;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = new GsonFactory();
    GoogleAccountCredential credential;
    private ImageFetcher mImageFetcher;
    private ImageLoader mImageLoader;
    private String mChosenAccountName;
    private Uri mFileURI = null;
    private VideoData mVideoData;
    private UploadBroadcastReceiver broadcastReceiver;
    private VideosGridViewFragment mVideosGridViewFragment;
    private VideosListViewFragment mVideosListViewFragment;
    private ListView mListFavorite;
    private ImageButton btnList;
    private ImageButton btnMenu;
    private ImageButton btnMusic;
    private ImageButton btnStory;
    private ImageButton btnCartoon;
    private TextView txtVideoName;

    private LinearLayout linearListVideos;
    private String EN_PLAYLIST_ID_MUSIC = "";
    private String EN_PLAYLIST_ID_STORY = "";
    private String EN_PLAYLIST_ID_CARTOON = "";
    private String VN_PLAYLIST_ID_MUSIC = "";
    private String VN_PLAYLIST_ID_STORY = "";
    private String VN_PLAYLIST_ID_CARTOON = "";
    private String VN_PLAYLIST_ID_FAVORITE = "";
    private String EN_PLAYLIST_ID_FAVORITE = "";
    ListViewAdapter listAdapter;
    List<VideoData> EN_VIDEOS_MUSIC,EN_VIDEOS_STORY,EN_VIDEOS_CARTOON,VN_VIDEOS_MUSIC,VN_VIDEOS_STORY,VN_VIDEOS_CARTOON,VIDEOS_SEARCH ;
    String pageTokenSearch="", EN_PAGETOKEN_MUSIC = "", EN_PAGETOKEN_STORY = "", EN_PAGETOKEN_CARTOON = "",
    VN_PAGETOKEN_MUSIC = "", VN_PAGETOKEN_STORY = "", VN_PAGETOKEN_CARTOON = "";

    SearchView search;
    private DBManager mDBManager;

    DisplayMetrics displaymetrics = new DisplayMetrics();

    private int IN_TAB = Constants.tab_music; // TAB 1: MUSIC, TAB 2: STORY, TAB3 CARTOON.
    public int heightScreen;
    public int widthScreen;

    SQLiteDatabase database=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        SharedPreferences pre=getSharedPreferences("my_data", MODE_PRIVATE);
        SharedPreferences.Editor editPre=pre.edit();
        editPre.putString(PRE_LOCATION,"EN"); // dummy
        // init database
        mDBManager = new DBManager(this);
        mDBManager.doCreateTable();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        heightScreen = displaymetrics.heightPixels;
        widthScreen = displaymetrics.widthPixels;
        // Check to see if the proper keys and playlist IDs have been set up
        if (!isCorrectlyConfigured()) {
            setContentView(R.layout.developer_setup_required);
            showMissingConfigurations();
        } else {
            //super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
           //getActionBar().hide();



            credential = GoogleAccountCredential.usingOAuth2(
                    getApplicationContext(), Arrays.asList(Auth.SCOPES));
            // set exponential backoff policy
            credential.setBackOff(new ExponentialBackOff());

            if (savedInstanceState != null) {
                mChosenAccountName = savedInstanceState.getString(ACCOUNT_KEY);

            } else {
                loadAccount();
            }

            credential.setSelectedAccountName(mChosenAccountName);

            mVideosGridViewFragment = (VideosGridViewFragment) getFragmentManager()
                    .findFragmentById(R.id.list_fragment);
            mListFavorite = (ListView) this.findViewById(R.id.listview_favorite);


            listAdapter = new ListViewAdapter(this,mDBManager.loadallVideos(),heightScreen);

            mListFavorite.setAdapter(listAdapter);

            new JSONAsyncTask().execute(BaseURLStringGit);
            initView();
            onClick();
        }

    }

    /**
     * This method checks various internal states to figure out at startup time
     * whether certain elements have been configured correctly by the developer.
     * Checks that:
     * <ul>
     * <li>the API key has been configured</li>
     * <li>the playlist ID has been configured</li>
     * </ul>
     *
     * @return true if the application is correctly configured for use, false if
     * not
     */
    public static int getColorWithAlpha(int color, float ratio) {
        int newColor = 0;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        newColor = Color.argb(alpha, r, g, b);
        return newColor;
    }
    public void initView(){

         btnList = (ImageButton) this.findViewById(R.id.imgBtn_thuvien);
         btnMenu = (ImageButton) this.findViewById(R.id.imgBtn_menu);
        btnMusic = (ImageButton) this.findViewById(R.id.btn_music);
        btnCartoon = (ImageButton) this.findViewById(R.id.btn_cartoon);
        btnStory = (ImageButton) this.findViewById(R.id.btn_funny);
        txtVideoName = (TextView) this.findViewById(R.id.txtView_videoname_main);
        EN_VIDEOS_MUSIC = new ArrayList<VideoData>();
        EN_VIDEOS_STORY = new ArrayList<VideoData>();
        EN_VIDEOS_CARTOON = new ArrayList<VideoData>();
        VN_VIDEOS_MUSIC = new ArrayList<VideoData>();
        VN_VIDEOS_STORY = new ArrayList<VideoData>();
        VN_VIDEOS_CARTOON = new ArrayList<VideoData>();
        VIDEOS_SEARCH = new ArrayList<VideoData>();

        linearListVideos = (LinearLayout) this.findViewById(R.id.linear_listVideos);
// init Preference
        SharedPreferences pre=getSharedPreferences("my_data", MODE_PRIVATE);
        SharedPreferences.Editor editPre=pre.edit();
        editPre.putInt(PRE_IN_TAB, Constants.tab_music);
        txtVideoName.setMovementMethod(new ScrollingMovementMethod());
        search=(SearchView) findViewById(R.id.searchView);
        search.setBackgroundColor(getColorWithAlpha(Color.BLACK,0.5f));

        int searchTextViewId = search.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView searchTextView = (TextView) search.findViewById(searchTextViewId);
        searchTextView.setTextColor(Color.WHITE);

        search.setQueryHint("Search View");
        search.setOnSearchClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
           // search.setBackgroundColor(Color.BLACK);
          }
        });
        //*** setOnQueryTextFocusChangeListener ***
        search.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub

                Toast.makeText(getBaseContext(), String.valueOf(hasFocus),
                        Toast.LENGTH_SHORT).show();
            }
        });

        //*** setOnQueryTextListener ***
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO Auto-generated method stub

                SharedPreferences pre=getSharedPreferences("my_data", MODE_PRIVATE);
                SharedPreferences.Editor editPre=pre.edit();
                editPre.putInt(PRE_IN_TAB, Constants.tab_search);
                editPre.commit();

                 searchVideosByContent(query);
                Toast.makeText(getBaseContext(), query,
                        Toast.LENGTH_SHORT).show();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO Auto-generated method stub

                	Toast.makeText(getBaseContext(), newText, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }
    public void refreshView()
    {
        SharedPreferences pre=getSharedPreferences("my_data", MODE_PRIVATE);
         String location = pre.getString(PRE_LOCATION, "EN");
         int inTab = pre.getInt(PRE_IN_TAB, IN_TAB);
         switch (inTab) {
             case Constants.tab_music:
                 if (location.equals("VN")) {
                     btnMusic.setBackground(getApplication().getResources().getDrawable(R.drawable.vn_music_select));
                     btnStory.setBackground(getApplication().getResources().getDrawable(R.drawable.vn_story));
                     btnCartoon.setBackground(getApplication().getResources().getDrawable(R.drawable.vn_cartoon));
                  if (VN_VIDEOS_MUSIC.size() == 0)
                     loadData(VN_PLAYLIST_ID_MUSIC,VN_PAGETOKEN_MUSIC);
                     else mVideosGridViewFragment.setVideos(VN_VIDEOS_MUSIC);
                 } else
                 {
                     btnMusic.setBackground(getApplication().getResources().getDrawable(R.drawable.en_music_select));
                     btnStory.setBackground(getApplication().getResources().getDrawable(R.drawable.en_story));
                     btnCartoon.setBackground(getApplication().getResources().getDrawable(R.drawable.en_cartoon));

                     loadData(EN_PLAYLIST_ID_MUSIC,EN_PAGETOKEN_MUSIC);
                 }

                 break;
             case Constants.tab_story:
                 if (location.equals("VN")) {
                     btnMusic.setBackground(getApplication().getResources().getDrawable(R.drawable.vn_music));
                     btnStory.setBackground(getApplication().getResources().getDrawable(R.drawable.vn_story_select));
                     btnCartoon.setBackground(getApplication().getResources().getDrawable(R.drawable.vn_cartoon));

                     loadData(VN_PLAYLIST_ID_STORY,VN_PAGETOKEN_STORY);
                 } else
                 {
                     btnMusic.setBackground(getApplication().getResources().getDrawable(R.drawable.en_music));
                     btnStory.setBackground(getApplication().getResources().getDrawable(R.drawable.en_story_select));
                     btnCartoon.setBackground(getApplication().getResources().getDrawable(R.drawable.en_cartoon));
                     loadData(EN_PLAYLIST_ID_STORY,EN_PAGETOKEN_STORY);
                 }
                 break;
             case Constants.tab_cartoon:
                 if (location.equals("VN")) {
                     btnMusic.setBackground(getApplication().getResources().getDrawable(R.drawable.vn_music));
                     btnStory.setBackground(getApplication().getResources().getDrawable(R.drawable.vn_story));
                     btnCartoon.setBackground(getApplication().getResources().getDrawable(R.drawable.vn_cartoon_select));
                     loadData(VN_PLAYLIST_ID_CARTOON,VN_PLAYLIST_ID_CARTOON);
                 } else
                 {
                     btnMusic.setBackground(getApplication().getResources().getDrawable(R.drawable.en_music));
                     btnStory.setBackground(getApplication().getResources().getDrawable(R.drawable.en_story));
                     btnCartoon.setBackground(getApplication().getResources().getDrawable(R.drawable.en_cartoon_select));
                     loadData(EN_PLAYLIST_ID_CARTOON,EN_PAGETOKEN_CARTOON);
                 }
                 break;
             default:
                 break;
         }
    }
    public void onClick() {
         btnList.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (!btnList.isSelected()) {
                     showListVideo();
                     btnList.setSelected(true);
                     Log.d("Main Log","Show List View");
                 } else {
                     hideListVideo();
                     btnList.setSelected(false);
                     Log.d("Main Log","Hide List View");

                 }
             }
         });
          btnMusic.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  SharedPreferences pre=getSharedPreferences("my_data", MODE_PRIVATE);
                  SharedPreferences.Editor editPre=pre.edit();
                  editPre.putInt(PRE_IN_TAB,Constants.tab_music);
                  editPre.commit();
                  refreshView();
              }
          });
        btnStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pre=getSharedPreferences("my_data", MODE_PRIVATE);
                SharedPreferences.Editor editPre=pre.edit();
                editPre.putInt(PRE_IN_TAB,Constants.tab_story);
                editPre.commit();
                refreshView();
            }
        });
        btnCartoon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pre = getSharedPreferences("my_data", MODE_PRIVATE);
                SharedPreferences.Editor editPre = pre.edit();
                editPre.putInt(PRE_IN_TAB, Constants.tab_cartoon);
                editPre.commit();
                refreshView();
            }
        });
    }

    private void showListVideo() {

        //mVideosGridViewFragment.SetViewVisible();
        // linearListVideos.
        runOnUiThread(new Runnable() {
            public void run() {
                //* The Complete ProgressBar does not appear**/
                //linearListVideos.setVisibility(View.VISIBLE);
                linearListVideos.setVisibility(View.VISIBLE);
                linearListVideos.animate()
                        .translationY(0).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        btnList.setSelected(true);
                    }
                });

            }
        });

    }

    private void hideListVideo() {

        //mVideosGridViewFragment.SetViewInvisible();
        runOnUiThread(new Runnable() {
            public void run() {
                //* The Complete ProgressBar does not appear**/
                //linearListVideos.setVisibility(View.INVISIBLE);
                linearListVideos.animate()
                        .translationY(-linearListVideos.getHeight()).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        linearListVideos.setVisibility(View.INVISIBLE);
                        btnList.setSelected(false);
                    }
                });

            }
        });


    }
    public void panToVideo(final String youtubeId, final  String youtubeName) {

       if (youtubeName!=null) txtVideoName.setText(youtubeName);
        popPlayerFromBackStack();
        YouTubePlayerFragment playerFragment = YouTubePlayerFragment
                .newInstance();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.detail_container, playerFragment,
                        YOUTUBE_FRAGMENT_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null).commit();
        playerFragment.initialize(Auth.KEY,
                new YouTubePlayer.OnInitializedListener() {
                    @Override
                    public void onInitializationSuccess(
                            YouTubePlayer.Provider provider,
                            YouTubePlayer youTubePlayer, boolean b) {
                        youTubePlayer.loadVideo(youtubeId);
                        mYouTubePlayer = youTubePlayer;
                        youTubePlayer
                                .setPlayerStateChangeListener(MainActivity.this);
                        youTubePlayer
                                .setOnFullscreenListener(MainActivity.this);
                    }

                    @Override
                    public void onInitializationFailure(
                            YouTubePlayer.Provider provider,
                            YouTubeInitializationResult result) {
                        //  showErrorToast(result.toString());
                    }
                });
    }
    public boolean popPlayerFromBackStack() {
        if (mIsFullScreen) {
            mYouTubePlayer.setFullscreen(false);
            return false;
        }
        if (getFragmentManager().findFragmentByTag(YOUTUBE_FRAGMENT_TAG) != null) {
            getFragmentManager().popBackStack();
            return false;
        }
        return true;
    }
    private boolean isCorrectlyConfigured() {
        // This isn't going to internationalize well, but we only really need
        // this for the sample app.
        // Real applications will remove this section of code and ensure that
        // all of these values are configured.
        if (Auth.KEY.startsWith("Replace")) {
            return false;
        }
        return !Constants.UPLOAD_PLAYLIST.startsWith("Replace");
    }

    /**
     * This method renders the ListView explaining what the configurations the
     * developer of this application has to complete. Typically, these are
     * static variables defined in {@link Auth} and {@link Constants}.
     */
    private void showMissingConfigurations() {
        List<MissingConfig> missingConfigs = new ArrayList<MissingConfig>();

        // Make sure an API key is registered
        if (Auth.KEY.startsWith("Replace")) {
            missingConfigs
                    .add(new MissingConfig(
                            "API key not configured",
                            "KEY constant in Auth.java must be configured with your Simple API key from the Google API Console"));
        }

        // Make sure a playlist ID is registered
        if (Constants.UPLOAD_PLAYLIST.startsWith("Replace")) {
            missingConfigs
                    .add(new MissingConfig(
                            "Playlist ID not configured",
                            "UPLOAD_PLAYLIST constant in Constants.java must be configured with a Playlist ID to submit to. (The playlist ID typically has a prexix of PL)"));
        }

        // Renders a simple_list_item_2, which consists of a title and a body
        // element
        ListAdapter adapter = new ArrayAdapter<MissingConfig>(this,
                android.R.layout.simple_list_item_2, missingConfigs) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row;
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    row = inflater.inflate(android.R.layout.simple_list_item_2,
                            null);
                } else {
                    row = convertView;
                }

                TextView titleView = (TextView) row
                        .findViewById(android.R.id.text1);
                TextView bodyView = (TextView) row
                        .findViewById(android.R.id.text2);
                MissingConfig config = getItem(position);
                titleView.setText(config.title);
                bodyView.setText(config.body);
                return row;
            }
        };

        // Wire the data adapter up to the view
        ListView missingConfigList = (ListView) findViewById(R.id.missing_config_list);
        missingConfigList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (broadcastReceiver == null)
            broadcastReceiver = new UploadBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(
                REQUEST_AUTHORIZATION_INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver, intentFilter);
    }

    private void ensureFetcher() {
        if (mImageFetcher == null) {
            mImageFetcher = new ImageFetcher(this, 256, 256);
            mImageFetcher.addImageCache(getFragmentManager(),
                    new com.nhannlt.kidmovie.util.ImageCache.ImageCacheParams(this,
                            "cache"));
        }
    }
    private void ensureLoader() {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.getApplicationContext());

        }
    }
    private void loadAccount() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        mChosenAccountName = sp.getString(ACCOUNT_KEY, null);
        //if (mChosenAccountName==null) chooseAccount();
        invalidateOptionsMenu();
    }

    private void saveAccount() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        sp.edit().putString(ACCOUNT_KEY, mChosenAccountName).commit();
        new JSONAsyncTask().execute(BaseURLStringGit);
        //refreshView();
    }

    private void loadData(String playListId, String pageToken) {
        if (mChosenAccountName == null) {
            return;
        }
        if (mImageLoader!=null) {

            mImageLoader.clearCache();
        }
        loadVideosByPlayListId(playListId, 0,pageToken);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(
                    broadcastReceiver);
        }
        if (isFinishing()) {
            // mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                // loadData();
                break;
            case R.id.menu_accounts:
                chooseAccount();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GMS_ERROR_DIALOG:
                break;
            case RESULT_PICK_IMAGE_CROP:
                if (resultCode == RESULT_OK) {
                    mFileURI = data.getData();
                    if (mFileURI != null) {
                        Intent intent = new Intent(this, ReviewActivity.class);
                        intent.setData(mFileURI);
                        startActivity(intent);
                    }
                }
                break;

            case RESULT_VIDEO_CAP:
                if (resultCode == RESULT_OK) {
                    mFileURI = data.getData();
                    if (mFileURI != null) {
                        Intent intent = new Intent(this, ReviewActivity.class);
                        intent.setData(mFileURI);
                        startActivity(intent);
                    }
                }
                break;
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    haveGooglePlayServices();
                } else {
                    checkGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != Activity.RESULT_OK) {
                    chooseAccount();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null
                        && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(
                            AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mChosenAccountName = accountName;
                        credential.setSelectedAccountName(accountName);
                        saveAccount();
                    }
                }
                break;
            case REQUEST_DIRECT_TAG:
                if (resultCode == Activity.RESULT_OK && data != null
                        && data.getExtras() != null) {
                    String youtubeId = data.getStringExtra(YOUTUBE_ID);
                    if (youtubeId.equals(mVideoData.getYouTubeId())) {
                        directTag(mVideoData);
                    }
                }
                break;
        }
    }

    private void directTag(final VideoData video) {
        final Video updateVideo = new Video();
        VideoSnippet snippet = video
                .addTags(Arrays.asList(
                        Constants.DEFAULT_KEYWORD,
                        Upload.generateKeywordFromPlaylistId(Constants.UPLOAD_PLAYLIST)));
        updateVideo.setSnippet(snippet);
        updateVideo.setId(video.getYouTubeId());

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                YouTube youtube = new YouTube.Builder(transport, jsonFactory,
                        credential).setApplicationName(Constants.APP_NAME)
                        .build();
                try {
                    youtube.videos().update("snippet", updateVideo).execute();
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
                return null;
            }

        }.execute((Void) null);
        Toast.makeText(this,
                R.string.video_submitted_to_ytdl, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ACCOUNT_KEY, mChosenAccountName);
    }

    private void loadVideosByPlayListId(String playListId, final int type, final String pageTokenNext) {
                if (mChosenAccountName == null) {
                    chooseAccount();
                    return;
                }

                setProgressBarIndeterminateVisibility(true);
                new AsyncTask<String, Void, List<VideoData>>() {

                List<VideoData> videos = new ArrayList<VideoData>();

                    @Override
                    protected List<VideoData> doInBackground(String...playListId ) {

                        YouTube youtube = new YouTube.Builder(transport, jsonFactory,
                                credential).setApplicationName(Constants.APP_NAME)
                                .build();

                        try {


                            PlaylistItemListResponse pilr = youtube.playlistItems()
                                    .list("id,contentDetails")
                                    .setPlaylistId(playListId[0].toString()).setPageToken(pageTokenNext)
                                    .setMaxResults(20l).execute();
                            List<String> videoIds = new ArrayList<String>();

                            // Iterate over playlist item list response to get uploaded
                            // videos' ids.
                            for (PlaylistItem item : pilr.getItems()) {
                                videoIds.add(item.getContentDetails().getVideoId());
                            }

                            String pageToken = "";
                           if (pilr.getNextPageToken()!=null)
                            pageToken = pilr.getNextPageToken();


                            // Get details of uploaded videos with a videos list
                            // request.
                            VideoListResponse vlr = youtube.videos()
                                    .list("id,snippet,status,contentDetails")
                                    .setId(TextUtils.join(",", videoIds)).execute();

                            // Add only the public videos to the local videos list.
                            for (Video video : vlr.getItems()) {
                                if ("public".equals(video.getStatus()
                                        .getPrivacyStatus())) {
                                    VideoData videoData = new VideoData();
                                    videoData.setVideo(video);
                                    videos.add(videoData);
                                }
                            }

                            // Sort videos by title
                            Collections.sort(videos, new Comparator<VideoData>() {
                                @Override
                                public int compare(VideoData videoData,
                                                   VideoData videoData2) {
                                    return videoData.getTitle().compareTo(
                                            videoData2.getTitle());
                                }
                            });
                        if (type == 0 ) {
                            SharedPreferences pre = getSharedPreferences("my_data", MODE_PRIVATE);
                            String location = pre.getString(PRE_LOCATION, "EN");
                            int inTab = pre.getInt(PRE_IN_TAB, IN_TAB);
                            switch (inTab) {
                                case Constants.tab_music:
                                    if (location.equals("VN")) {
                                        VN_PAGETOKEN_MUSIC = pageToken;
                                        VN_VIDEOS_MUSIC.addAll(videos);
                                        return VN_VIDEOS_MUSIC;

                                    } else {
                                        EN_PAGETOKEN_MUSIC = pageToken;
                                        EN_VIDEOS_MUSIC.addAll(videos);
                                        return EN_VIDEOS_MUSIC;
                                    }

                                case Constants.tab_story:
                                    if (location.equals("VN")) {
                                        VN_PAGETOKEN_STORY = pageToken;
                                        VN_VIDEOS_STORY.addAll(videos);
                                        return VN_VIDEOS_STORY;
                                    } else {
                                        EN_PAGETOKEN_STORY = pageToken;
                                        EN_VIDEOS_STORY.addAll(videos);
                                        return EN_VIDEOS_STORY;
                                    }

                                case Constants.tab_cartoon:
                                    if (location.equals("VN")) {
                                        VN_PAGETOKEN_CARTOON = pageToken;
                                        VN_VIDEOS_CARTOON.addAll(videos);
                                        return VN_VIDEOS_CARTOON;
                                    } else {
                                        EN_PAGETOKEN_CARTOON = pageToken;
                                        EN_VIDEOS_CARTOON.addAll(videos);
                                        return EN_VIDEOS_CARTOON;
                                    }

                            }
                        }
                            return videos;

                        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
                            showGooglePlayServicesAvailabilityErrorDialog(availabilityException
                                    .getConnectionStatusCode());
                        } catch (UserRecoverableAuthIOException userRecoverableException) {
                            startActivityForResult(
                                    userRecoverableException.getIntent(),
                                    REQUEST_AUTHORIZATION);
                        } catch (IOException e) {
                            Utils.logAndShow(MainActivity.this, Constants.APP_NAME, e);
                        }
                        return null;
                    }

            @Override
            protected void onPostExecute(List<VideoData> videos) {
                setProgressBarIndeterminateVisibility(false);

                if (videos == null) {
                    return;
                }
            if (type == 0 ) { // get videos in playlist


                mVideosGridViewFragment.setIsLoading(false);
                mVideosGridViewFragment.setVideos(videos);
            } else {  // get favorite videos
                //mVideosListViewFragment.setVideos(videos);
                if (listAdapter!=null)
                    if (listAdapter.getCount() < 1) {
                List<FavoriteData> favoriteVideos = new ArrayList<FavoriteData>();
                for (int i=0;i<videos.size();i++) {
                    FavoriteData mDataFavorite = new FavoriteData();
                    mDataFavorite.setVideoId(videos.get(i).getYouTubeId());
                    mDataFavorite.setVideoName(videos.get(i).getTitle());
                    mDataFavorite.setVideoUrl(videos.get(i).getThumbUri());
                    mDataFavorite.setVideoDuration(videos.get(i).getVideo().getContentDetails().getDuration());
                    mDataFavorite.setVideoPosition(0);
                    favoriteVideos.add(mDataFavorite);
                    if (mDBManager.doInsertRecord(mDataFavorite)) {
                       //Log.d("add new video ", " " +mDataFavorite.getVideoName());
                    };

                }
                        List<FavoriteData> favoritevideoDB = new ArrayList<FavoriteData>();
                        favoritevideoDB = mDBManager.loadallVideos();
                            listAdapter.setVideos(favoritevideoDB);
                            listAdapter.notifyDataSetChanged();
                    }

                }}

        }.execute(playListId);
    }
    private void searchVideosByContent(final String content) {
        if (mChosenAccountName == null) {
            chooseAccount();
            return;
        }


        setProgressBarIndeterminateVisibility(true);
        new AsyncTask<String, Void, List<VideoData>>() {


            @Override
            protected List<VideoData> doInBackground(String...playListId ) {

                YouTube youtube = new YouTube.Builder(transport, jsonFactory,
                        credential).setApplicationName(Constants.APP_NAME)
                        .build();

                try {
                    List<VideoData> videos = new ArrayList<VideoData>();

                    SearchListResponse pilr = youtube.search()
                            .list("id").setQ(content).setPageToken(pageTokenSearch)
                            .setMaxResults(20l).execute();

                    List<String> videoIds = new ArrayList<String>();

                    // Iterate over playlist item list response to get uploaded
                    // videos' ids.
                    for (SearchResult item : pilr.getItems()) {
                        videoIds.add(item.getId().getVideoId());
                    }
                    if (pilr.getNextPageToken()!= null)
                    pageTokenSearch = pilr.getNextPageToken();
                    else pageTokenSearch = "";
                    // Get details of uploaded videos with a videos list
                    // request.
                    VideoListResponse vlr = youtube.videos()
                            .list("id,snippet,status,contentDetails")
                            .setId(TextUtils.join(",", videoIds)).execute();

                    // Add only the public videos to the local videos list.
                    for (Video video : vlr.getItems()) {
                        if ("public".equals(video.getStatus()
                                .getPrivacyStatus())) {
                            VideoData videoData = new VideoData();
                            videoData.setVideo(video);
                            videos.add(videoData);
                        }
                    }

                    // Sort videos by title
                    Collections.sort(videos, new Comparator<VideoData>() {
                        @Override
                        public int compare(VideoData videoData,
                                           VideoData videoData2) {
                            return videoData.getTitle().compareTo(
                                    videoData2.getTitle());
                        }
                    });

                            VIDEOS_SEARCH.addAll(videos);
                            return VIDEOS_SEARCH;


                } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
                    showGooglePlayServicesAvailabilityErrorDialog(availabilityException
                            .getConnectionStatusCode());
                } catch (UserRecoverableAuthIOException userRecoverableException) {
                    startActivityForResult(
                            userRecoverableException.getIntent(),
                            REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    Utils.logAndShow(MainActivity.this, Constants.APP_NAME, e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<VideoData> videos) {
                setProgressBarIndeterminateVisibility(false);
                Log.d("Size: ", "" + videos.size());
                if (VIDEOS_SEARCH.size() <= 20) {
                    mVideosGridViewFragment = new VideosGridViewFragment();

                }
                mVideosGridViewFragment.setIsLoading(false);
                mVideosGridViewFragment.setVideos(videos);
            }

        }.execute(content);
    }
    @Override
    public void onBackPressed() {
        // if (mDirectFragment.popPlayerFromBackStack()) {
        // super.onBackPressed();
        // }
    }

    @Override
    public ImageFetcher onGetImageFetcher() {
        ensureFetcher();
        return mImageFetcher;
    }
    @Override
    public ImageLoader onGetImageLoader() {
        ensureLoader();
        return mImageLoader;
    }

    @Override
    public void onVideoSelected(VideoData video) {
        mVideoData = video;
        panToVideo(video.getYouTubeId(),video.getTitle());

        hideListVideo();
        FavoriteData mFavoriteData = new FavoriteData();
        mFavoriteData.setVideoId(video.getYouTubeId());
        mFavoriteData.setVideoName(video.getTitle());
        mFavoriteData.setVideoUrl(video.getThumbUri());
        mFavoriteData.setVideoDuration(video.getVideo().getContentDetails().getDuration());
        mFavoriteData.setVideoPosition(0);
        if (mDBManager.doInsertRecord(mFavoriteData)) {

            List<FavoriteData> videos = new ArrayList<FavoriteData>();
            videos = mDBManager.loadallVideos();
            if (listAdapter!=null) {
                listAdapter.setVideos(videos);
                listAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onLoadMore() {
        SharedPreferences pre=getSharedPreferences("my_data", MODE_PRIVATE);
        String location = pre.getString(PRE_LOCATION, "EN");
        int inTab = pre.getInt(PRE_IN_TAB, IN_TAB);
        switch (inTab) {
            case Constants.tab_music:
                if (location.equals("VN")) {
                    loadVideosByPlayListId(VN_PLAYLIST_ID_MUSIC,0,VN_PAGETOKEN_MUSIC);
                } else {
                    loadVideosByPlayListId(EN_PLAYLIST_ID_MUSIC,0,EN_PAGETOKEN_MUSIC);
                }
                break;
            case Constants.tab_story:
                if (location.equals("VN")) {
                    loadVideosByPlayListId(VN_PLAYLIST_ID_STORY,0,VN_PAGETOKEN_STORY);
                } else {
                    loadVideosByPlayListId(EN_PLAYLIST_ID_STORY,0,EN_PAGETOKEN_STORY);
                }
                break;
            case Constants.tab_cartoon:
                if (location.equals("VN")) {
                    loadVideosByPlayListId(VN_PLAYLIST_ID_CARTOON,0,VN_PAGETOKEN_CARTOON);
                } else {
                    loadVideosByPlayListId(EN_PLAYLIST_ID_CARTOON,0,EN_PAGETOKEN_CARTOON);
                }
                break;
            case Constants.tab_search:
                searchVideosByContent("");

                break;
        }


    }


    public void pickFile(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, RESULT_PICK_IMAGE_CROP);
    }

    public void recordVideo(View view) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // Workaround for Nexus 7 Android 4.3 Intent Returning Null problem
        // create a file to save the video in specific folder (this works for
        // video only)
        // mFileURI = getOutputMediaFile(MEDIA_TYPE_VIDEO);
        // intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileURI);

        // set the video image quality to high
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        // start the Video Capture Intent
        startActivityForResult(intent, RESULT_VIDEO_CAP);
    }

    public void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, MainActivity.this,
                        REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     */
    private boolean checkGooglePlayServicesAvailable() {
        final int connectionStatusCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }

    private void haveGooglePlayServices() {
        // check if there is already an account selected
        if (credential.getSelectedAccountName() == null) {
            // ask user to choose account
            chooseAccount();
        }
    }

    private void chooseAccount() {
        startActivityForResult(credential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER);
    }

    @Override
    public void onFullscreen(boolean b) {

    }

    @Override
    public void onLoading() {

    }

    @Override
    public void onLoaded(String s) {

    }

    @Override
    public void onAdStarted() {

    }

    @Override
    public void onVideoStarted() {

    }

    @Override
    public void onVideoEnded() {

    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {

    }

    @Override
    public void onVideoFavoriteSelected(FavoriteData video) {
        //mVideoData = video;


        panToVideo(video.getVideoId(),video.getVideoName());
     if (mDBManager.updatePosition(video)) {

     };

        hideListVideo();

    }

    /**
     * Private class representing a missing configuration and what the developer
     * can do to fix the issue.
     */
    private class MissingConfig {

        public final String title;
        public final String body;

        public MissingConfig(String title, String body) {
            this.title = title;
            this.body = body;
        }
    }

    // public Uri getOutputMediaFile(int type)
    // {
    // // To be safe, you should check that the SDCard is mounted
    // if(Environment.getExternalStorageState() != null) {
    // // this works for Android 2.2 and above
    // File mediaStorageDir = new
    // File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
    // "SMW_VIDEO");
    //
    // // This location works best if you want the created images to be shared
    // // between applications and persist after your app has been uninstalled.
    //
    // // Create the storage directory if it does not exist
    // if (! mediaStorageDir.exists()) {
    // if (! mediaStorageDir.mkdirs()) {
    // Log.d(TAG, "failed to create directory");
    // return null;
    // }
    // }
    //
    // // Create a media file name
    // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
    // Locale.getDefault()).format(new Date());
    // File mediaFile;
    // if(type == MEDIA_TYPE_VIDEO) {
    // mediaFile = new File(mediaStorageDir.getPath() + File.separator +
    // "VID_"+ timeStamp + ".mp4");
    // } else {
    // return null;
    // }
    //
    // return Uri.fromFile(mediaFile);
    // }
    //
    // return null;
    // }

    private class UploadBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(REQUEST_AUTHORIZATION_INTENT)) {
                Log.d(TAG, "Request auth received - executing the intent");
                Intent toRun = intent
                        .getParcelableExtra(REQUEST_AUTHORIZATION_INTENT_PARAM);
                startActivityForResult(toRun, REQUEST_AUTHORIZATION);
            }
        }
    }
    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {

                //------------------>>
                HttpGet httppost = new HttpGet(urls[0]);
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = httpclient.execute(httppost);

                // StatusLine stat = response.getStatusLine();
                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);


                    JSONObject jsono = new JSONObject(data);

                    String version = jsono.getString("message");

                    myFirstVideos = jsono.getJSONArray("MyFistVideos");
                    myPlayLists_EN = jsono.getJSONArray("MyPlayLists_EN");
                    myPlayLists_VN = jsono.getJSONArray("MyPlayLists_VN");

                    return true;
                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {

                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {

                try {
                         JSONObject mObjectEN = myPlayLists_EN.getJSONObject(0);
                        JSONObject mObjectVN = myPlayLists_VN.getJSONObject(0);
                        EN_PLAYLIST_ID_MUSIC = mObjectEN.getString("playListId");
                        VN_PLAYLIST_ID_MUSIC = mObjectVN.getString("playListId");
                    //
                        mObjectEN = myPlayLists_EN.getJSONObject(1);
                        mObjectVN = myPlayLists_VN.getJSONObject(1);
                    EN_PLAYLIST_ID_STORY = mObjectEN.getString("playListId");
                    VN_PLAYLIST_ID_STORY = mObjectVN.getString("playListId");
                    //
                    mObjectEN = myPlayLists_EN.getJSONObject(2);
                    mObjectVN = myPlayLists_VN.getJSONObject(2);
                    EN_PLAYLIST_ID_CARTOON = mObjectEN.getString("playListId");
                    VN_PLAYLIST_ID_CARTOON = mObjectVN.getString("playListId");

                    mObjectEN = myPlayLists_EN.getJSONObject(3);
                    mObjectVN = myPlayLists_VN.getJSONObject(3);
                    VN_PLAYLIST_ID_FAVORITE = mObjectVN.getString("playListId");
                    EN_PLAYLIST_ID_FAVORITE =  mObjectEN.getString("playListId");

                    SharedPreferences pre=getSharedPreferences("my_data", MODE_PRIVATE);
                    SharedPreferences.Editor editPre=pre.edit();
                    editPre.putInt(PRE_IN_TAB, Constants.tab_music);
                    editPre.commit();
                    refreshView();


                    String location = pre.getString(PRE_LOCATION, "EN");

                    boolean isFirstTimeApp = pre.getBoolean("isFirstTimeApp",false);
                    if (!isFirstTimeApp) {
                        if (location.equals("VN")) {
                            loadVideosByPlayListId(VN_PLAYLIST_ID_FAVORITE, 1,"");
                            Log.d(TAG, "Load favorite VN");
                        } else {
                            loadVideosByPlayListId(EN_PLAYLIST_ID_FAVORITE, 1,"");
                            Log.d(TAG, "Load favorite EN");
                        }
                   editPre.putBoolean("isFirstTimeApp",true);
                    }

                } catch (JSONException ex) {

                    Log.d(TAG,"Load JSON  failed");
                }
            }
        }}
}
