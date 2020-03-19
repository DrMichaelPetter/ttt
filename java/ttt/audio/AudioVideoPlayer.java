// TeleTeachingTool - Presentation Recording With Automated Indexing
//
// Copyright (C) 2003-2008 Peter Ziewer - Technische Universit�t M�nchen
// 
//    This file is part of TeleTeachingTool.
//
//    TeleTeachingTool is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    TeleTeachingTool is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with TeleTeachingTool.  If not, see <http://www.gnu.org/licenses/>.

package ttt.audio;

import java.awt.Component;
import java.io.IOException;


public interface AudioVideoPlayer {

    String getAudioFilename() throws IOException;
    void close();
    int getAudioDuration();

    default int getDuration() { return getAudioDuration(); };

    default String getVideoFilename() throws IOException {return null;};
    default Component getVideo() { return null; };
    default int getVideoDuration() { return getAudioDuration(); };
    default void setReplayRatio(double ratio) { };


    void setReplayOffset(int msec);
    int getReplayOffset();


    int getTime() ;
    void setTime(int time);

    void pause();
    void play();

    int getVolumeLevel();
    void setVolumeLevel(int volume);
    boolean getMute();
    void setMute(boolean mute);
}
