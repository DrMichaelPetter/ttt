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

/*
 * Created on 20.11.2007
 *
 * Author: Peter Ziewer, TU Munich, Germany - ziewer@in.tum.de
 */
package ttt.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.Control.Type;
import javax.swing.JFrame;

import ttt.Experimental;
import ttt.TTT;

import javax.sound.sampled.DataLine;


public class TargetDataLineMonitor implements TargetDataLine {
	private static final AudioFormat[] audioFormats = {
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 22050.0F, 16, 1, 2, 22050.0F, false),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 11025.0F, 16, 1, 2, 11025.0F, false),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0F, 16, 1, 2, 44100.0F, false),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, false) };
	public static void main(String[] args) throws Exception {
		TTT.setDebug(true);
		JFrame frame = new JFrame("Audio Volume Meter");
		AudioMonitorPanel amp = new AudioMonitorPanel(true);
        frame.add(amp);

        frame.pack();
        frame.setVisible(true);
		
		for (int i = 0; i < audioFormats.length; i++) {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormats[i]);
            if (AudioSystem.isLineSupported(info)) {
                // try {
                TargetDataLineMonitor targetDataLine = new TargetDataLineMonitor((TargetDataLine) AudioSystem.getLine(info),
                        amp);
                targetDataLine.open(audioFormats[i], targetDataLine.getBufferSize());
                break;
            }
        }
	}
	
    public TargetDataLineMonitor(TargetDataLine targetDataLine, AudioMonitorPanel volumeLevelComponent) {
        this.targetDataLine = targetDataLine;
        this.volumeLevelComponent = volumeLevelComponent;
    }

    private TargetDataLine targetDataLine;
    private float meanSampleValue = 0;
    private AudioMonitorPanel volumeLevelComponent;
    private int mutecounter=0;

    public int read(byte[] buffer, int offset, int len) {
        int i = targetDataLine.read(buffer, offset, len);

        while (offset < len) {
            // caculates the frame samples from the given byte array
            // http://www.jsresources.org/faq_audio.html#reconstruct_samples
            float sample = ((buffer[offset + 0] & 0xFF) | (buffer[offset + 1] << 8)) / 32768.0F;
            if (sample < 0)
                sample *= (-1);
            meanSampleValue += sample;
            offset += 2;
        }
        meanSampleValue /= (buffer.length / 2);

        if (volumeLevelComponent != null)
            volumeLevelComponent.setPeakPercentage(meanSampleValue * 2);
        ttt.TTT.debug("Sample: "+meanSampleValue*100);

        // (meanSampleValue<0.00000001) -- means absolute silence
        double threshold=Double.parseDouble(Experimental.Code.MICTHRESHOLD.get());
        if (meanSampleValue<threshold) mutecounter++;
        if (mutecounter>250){
        	mutecounter=0;
        	volumeLevelComponent.warnMuted();
        }

        return i;
    }

    public void open(AudioFormat format) throws LineUnavailableException {
        targetDataLine.open(format);
    }

    public void open(AudioFormat format, int bufferSize) throws LineUnavailableException {
        targetDataLine.open(format, bufferSize);
    }

    public int available() {
        return targetDataLine.available();
    }

    public void drain() {
        targetDataLine.drain();
    }

    public void flush() {
        targetDataLine.flush();
    }

    public int getBufferSize() {
        return targetDataLine.getBufferSize();
    }

    public AudioFormat getFormat() {
        return targetDataLine.getFormat();
    }

    public int getFramePosition() {
        return targetDataLine.getFramePosition();
    }

    public float getLevel() {
        return targetDataLine.getLevel();
    }

    public long getLongFramePosition() {
        return targetDataLine.getLongFramePosition();
    }

    public long getMicrosecondPosition() {
        return targetDataLine.getMicrosecondPosition();
    }

    public boolean isActive() {
        return targetDataLine.isActive();
    }

    public boolean isRunning() {
        return targetDataLine.isRunning();
    }

    public void addLineListener(LineListener listener) {
        targetDataLine.addLineListener(listener);
    }

    public void close() {
        targetDataLine.close();
    }

    public Control getControl(Type control) {
        return targetDataLine.getControl(control);
    }

    public Control[] getControls() {
        return targetDataLine.getControls();
    }

    public javax.sound.sampled.Line.Info getLineInfo() {
        return targetDataLine.getLineInfo();
    }

    public boolean isControlSupported(Type control) {
        return targetDataLine.isControlSupported(control);
    }

    public boolean isOpen() {
        return targetDataLine.isOpen();
    }

    public void open() throws LineUnavailableException {
        targetDataLine.open();
    }

    public void removeLineListener(LineListener listener) {
        targetDataLine.removeLineListener(listener);
    }

    public void start() {
        targetDataLine.start();
    }

    public void stop() {
        targetDataLine.stop();
    }
}
