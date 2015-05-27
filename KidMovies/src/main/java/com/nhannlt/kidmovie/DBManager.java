package com.nhannlt.kidmovie;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.google.api.services.youtube.model.Video;
import com.nhannlt.kidmovie.util.VideoData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by nhan on 5/26/2015.
 */
public class DBManager {
    SQLiteDatabase database = null;
    Activity mActivity;
  DBManager(Context activity){
   //this.database = database;
      this.mActivity = (Activity)activity;
  };
    public boolean doCreateDb() {
        database = mActivity.openOrCreateDatabase(
                "kidmovies.db",
                mActivity.MODE_PRIVATE,
                null);
        if(database!= null) return true;
        else return false;
    }

    public void doDeleteDb() {
        String msg = "";
        if (mActivity.deleteDatabase("kidmovies.db") == true) {
            msg = "Delete database [kidmovies.db] is successful";
        } else {
            msg = "Delete database [kidmovies.db] is failed";
        }
        Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
    }

    public void doCreateTable() {
       if( doCreateDb())

        doCreateFavoriteTable();
    }

    public void doCreateFavoriteTable() {
        String sql = "CREATE TABLE tblFavorite (" +
                "videoId TEXT PRIMARY KEY," +
                "videoName TEXT," +
                "videoUrl TEXT," +
                "videoDuration TEXT," +
                "videoPosition INTEGER )";
      try {
          database.execSQL(sql);
      } catch (Exception ex) {
          Log.d("db","failes");
      }
    }

    public void doDeleteRecordTable(String videoId) {
        if (videoId != null) {
            database.delete("tblFavorite", null, null);

            database.delete("tblFavorite",
                    "videoId=?",
                    new String[]{videoId});
        }
    }
    public FavoriteData loadVideoByID(String videoId) {
        Cursor c = database.query("tblFavorite",
                null, "videoId=?",
                new String[]{videoId}, null, null, null);
        c.moveToFirst();
        FavoriteData mFavoriteVideo = new FavoriteData();

            FavoriteData mFavoriteData = new FavoriteData();
            mFavoriteData.setVideoId(c.getString(0));
            mFavoriteData.setVideoName(c.getString(1));
            mFavoriteData.setVideoUrl(c.getString(2));
            mFavoriteData.setVideoDuration(c.getString(3));
            mFavoriteData.setVideoPosition(c.getInt(4));
            //mListVideos.add(mFavoriteData);

        // Toast.makeText(this, data, Toast.LENGTH_LONG).show();
        c.close();
        return mFavoriteData;
    }
    public ArrayList<FavoriteData> loadallVideos() {
        Cursor c = database.query("tblFavorite",
                null, null, null, null, null, "videoPosition");
        c.moveToFirst();
        ArrayList<FavoriteData> mListVideos = new ArrayList<FavoriteData>();
        while (c.isAfterLast() == false) {
            FavoriteData mFavoriteData = new FavoriteData();
            mFavoriteData.setVideoId(c.getString(0));
            mFavoriteData.setVideoName(c.getString(1));
            mFavoriteData.setVideoUrl(c.getString(2));
            mFavoriteData.setVideoDuration(c.getString(3));
            mFavoriteData.setVideoPosition(c.getInt(4));
            mListVideos.add(mFavoriteData);
            c.moveToNext();
        }
       // Toast.makeText(this, data, Toast.LENGTH_LONG).show();
        c.close();
     return mListVideos;
    }

    public boolean doInsertRecord(FavoriteData mVideo) {
        ContentValues values = new ContentValues();
        values.put("videoId", mVideo.getVideoId());
        values.put("videoUrl", mVideo.getVideoUrl());
        values.put("videoName", mVideo.getVideoName());
        values.put("videoDuration", mVideo.getVideoDuration());
        values.put("videoPosition", mVideo.getVideoPosition());

        String msg = "";
        if (database.insert("tblFavorite", null, values) == -1) {
            msg = "Failed to insert record";
            Log.d("DATABASE",msg);
            FavoriteData mVideoExist = new FavoriteData();
             mVideoExist = loadVideoByID(mVideo.getVideoId());
             mVideoExist.setVideoPosition(mVideoExist.getVideoPosition()+1);
            return  updatePosition(mVideoExist);

        } else {
            msg = "insert record is successful";
            Log.d("DATABASE",msg);
            return  true;
        }

    }

    public boolean updatePosition(FavoriteData mFavoriteData) {
        ContentValues values = new ContentValues();
        values.put("videoPosition", mFavoriteData.getVideoPosition());
        String msg = "";
        int ret = database.update("tblFavorite", values,
                "videoId=?", new String[]{mFavoriteData.getVideoId()});
        if (ret == 0) {
            msg = "Failed to update";
            Log.d("DATABASE",msg);
            return  false;
        } else {
            msg = "updating is successful";
            Log.d("DATABASE",msg);
            return  true;
        }
 //       Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}