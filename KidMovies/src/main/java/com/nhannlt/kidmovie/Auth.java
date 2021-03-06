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

import com.google.android.gms.common.Scopes;
import com.google.api.services.youtube.YouTubeScopes;

public class Auth {
    // Register an API key here: https://code.google.com/apis/console AIzaSyBufHMtVb_rBlOPKPkCETC1aGh2JkR03DM
    public static final String KEY = "AIzaSyDTbkkZZ3OscoG-P8LAdovOxLg6mbThaSU";
    //public static final String KEY = "1038678827019-s5ju7hgbp149bjdndos7jf3pjvvftakn.apps.googleusercontent.com";
    public static final String[] SCOPES = { YouTubeScopes.YOUTUBE, YouTubeScopes.YOUTUBE_READONLY};
}
