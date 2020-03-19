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

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import ttt.Constants;
import ttt.TTT;
import ttt.record.Recording;
/**
 * JavaSoundPlayer
 * 
 * alternative audio player, that does not rely on the JMF anymore,
 * enabling a smaller footprint and maybe a tiny web player one day?
 * 
 * depends on JLayer's MP3SPI for Java sound ( http://www.javazoom.net/mp3spi/mp3spi.html )
 * 
 * @author Michael Petter
 */
public class JavaSoundPlayer implements AudioVideoPlayer {

    /*
     * java -cp dist/ttt.jar:mp3/tritonus_share.jar:mp3/jl1.0.1.jar:mp3/mp3spi1.9.5.jar ttt.audio.JavaSoundPlayer test/Compilerbau_2015_07_13/Compilerbau_2015_07_13.mp3
     */
    public static void main(String[] args) {
        System.out.println("TestRoutine");
        JavaSoundPlayer jsp = new JavaSoundPlayer(args[0], null);
        jsp.play();
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private File audio;
    private Recording recording;
    private Clip audioClip;
    private BooleanControl mute;
    private FloatControl mastergain;

    public JavaSoundPlayer(String filename, Recording recording) {
        this.recording = recording;
        try {
            audio = Constants.getExistingFile(filename, Constants.AUDIO_FILE);
            AudioInputStream istream = AudioSystem.getAudioInputStream(audio);
            if (audio.getName().toLowerCase().endsWith(".mp3"))
            {
                TTT.verbose("loading and decoding MP3");
                AudioFormat baseFormat = istream.getFormat();
                AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
                    baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                istream = AudioSystem.getAudioInputStream(decodedFormat, istream);
            }
            
            audioClip = AudioSystem.getClip();
            audioClip.open(istream);
            TTT.verbose("Audio loaded: " + audio.getName());
            TTT.verbose("  length: "+getAudioDuration()/1000.0/60.0);
            TTT.verbose("available Sound controls:");
            for (var c:audioClip.getControls()){
                TTT.verbose(c.getType().toString());
            }
            for(var m:AudioSystem.getMixerInfo()){
            TTT.verbose(m.getDescription());
            for (var c:AudioSystem.getMixer(m).getControls()){
                TTT.verbose(c.getType().toString());
            }}


            mute = (BooleanControl)audioClip.getControl(BooleanControl.Type.MUTE);
            mastergain = (FloatControl)audioClip.getControl(FloatControl.Type.MASTER_GAIN);
            TTT.verbose("Gain range "+mastergain.getMinimum()+" - "+mastergain.getMaximum());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getAudioFilename() throws IOException {
        return audio.getCanonicalPath();
    }

    public void close() {
        audioClip.close();
    };

    public int getAudioDuration() {
        return (int) (audioClip.getMicrosecondLength() / 1000l);
    };

    // no replay offset here
    public void setReplayOffset(int msec) {
    };
    public int getReplayOffset() {
        return 0;
    };

    public int getTime() {
        return (int)(audioClip.getMicrosecondPosition() / 1000l);
    };
    public void setTime(int time) {
        audioClip.setMicrosecondPosition(time*1000l);
    };

    public void pause() {
        audioClip.stop();
    };

    public void play() {
        audioClip.start();
    };

    public int getVolumeLevel() {
        return (int)(Math.pow(10,mastergain.getValue()/20.0)*100);
    };

    public void setVolumeLevel(int volume) {
        float gain = volume/100.0f;
        mastergain.setValue((float)(20*Math.log10(gain)));
    };

    public boolean getMute() {
        return mute.getValue();
    };

    public void setMute(boolean mute) {
        this.mute.setValue(mute);
    };
}
