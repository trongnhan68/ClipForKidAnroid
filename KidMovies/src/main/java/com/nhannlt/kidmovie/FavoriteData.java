package com.nhannlt.kidmovie;

/**
 * Created by nhan on 5/26/2015.
 */
public class FavoriteData {
    String videoId="";
    String videoUrl="";
    int videoPosition=0;
String videoName="";
    String videoDuration="";

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }



    public String getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(String videoDuration) {
        this.videoDuration = videoDuration;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getVideoPosition() {
        return videoPosition;
    }

    public void setVideoPosition(int videoPosition) {
        this.videoPosition = videoPosition;
    }
}
