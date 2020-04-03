// TeleTeachingTool - Presentation Recording With Automated Indexing
//
// Copyright (C) 2003-2008 Peter Ziewer - Technische Universität München
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


package ttt.postprocessing.podcast;


import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ProgressMonitor;
import javax.swing.Timer;

import ttt.Constants;
import ttt.TTT;
import ttt.audio.Exec;
import ttt.record.Recording;

/**
 * Creates MP4 podcast from recording object using <a href="http://ffmpeg.org">ffmpeg</a> and <a href="http://gpac.sourceforge.net">MP4Box</a>. <br>  
 * Created on 16. August 2009, 17:25
 * @author Christof Angermueller 
 * 
 */
public class PodcastCreator {

	private static final int RESOLUTION_WIDTH = 480;
	private static final int RESOLUTION_HEIGTH = 320;
	private static final String FFMPEG = "ffmpeg";
	private static final String MP4BOX = "MP4Box";

	private static final double FRAMES_PER_SEC = 1;
	
	
	public static void main(String[] args) throws Exception {
		
		if (args.length == 0) {
			System.out.println("PodcastCreator filename.ttt ");
			System.out.println("PodcastCreator filename.ttt [-originalsize| - size width[xheight]] [-debug] [-crop widthxheight+xxy]");
			return;
		}
    TTT.verbose=false;
    Recording recording = new Recording(new File(args[0]), false);
    TTT.verbose=false;
    int width = recording.prefs.framebufferWidth;
    int height = recording.prefs.framebufferHeight;
    int cropx=0,cropy=0;
    int cropw=width;
    int croph=height;
    
    if (args.length == 1) {
        PodcastCreator.createPodcast(recording, true);
        return;
    }
    if (args.length > 1) {
        args = java.util.Arrays.copyOfRange(args,1,args.length);
        int i = 0;
        for (String param:args){
            i++;
            if (!param.startsWith("-")) continue;
            switch (param) {
            case "-originalsize":
                cropw=width = recording.prefs.framebufferWidth;
                height = recording.prefs.framebufferHeight;
                cropx=cropy=0;
                if (height%2==1) height++;
                croph=height;
                break;
            case "-size":
                String [] res = args[i].split("x");
				cropw=width=Integer.parseInt(res[0]);
				if (res.length==1){
					height=(int)(width * recording.prefs.framebufferHeight)/recording.prefs.framebufferWidth;
				}
				else
                	height=Integer.parseInt(res[1]);
                cropx=cropy=0;
                if (height%2==1) height++;
                croph=height;
                break;
            case "-debug":
                TTT.verbose=true;
                TTT.setDebug(true);
                break;
            case "-crop":
                String [] crop = args[i].split("\\+");
                String [] reso = crop[0].split("x");
                cropw=Integer.parseInt(reso[0]);
                croph=Integer.parseInt(reso[1]);
                if (height%2==1) height--;
                String [] offset = crop[1].split("x");
                cropx=Integer.parseInt(offset[0]);
                cropy=Integer.parseInt(offset[1]);
                break;
            default:
                System.out.println("unknown parameter: "+param);
                break;
            }
        }
        System.out.println("Generating podcast with dimensions "+cropw+"x"+croph+" @"+cropx+"/"+cropy+" from recording "+width+"x"+height);
        PodcastCreator.createPodcast(recording,width,height,FRAMES_PER_SEC,true,false,cropw,croph,cropx,cropy);
    }

	}


	/**
	 * Checks whether it is possible to create a podcast
	 * @param recording
	 * @return True if ffmpeg, MP4Box, and an audio file is available for creating a podcast
	 * @throws IOException
	 */
	public static boolean isCreationPossible(Recording recording) throws IOException {
		return Exec.getCommand(FFMPEG) != null && Exec.getCommand(MP4BOX) != null && (recording.getExistingFileBySuffix(new String[] {"wav","mp3","mp2"}).exists());
	}

	/**
	 * Creates podcast using default parameters
	 * @param recording
	 * @param batch
	 * @return True: Podcast created successfully.<br>False: Canceled by user.
	 * @throws Exception
	 */
	public static boolean createPodcast(Recording recording, boolean batch) throws Exception {
      return createPodcast(recording, RESOLUTION_WIDTH, RESOLUTION_HEIGTH, FRAMES_PER_SEC, batch, true,RESOLUTION_WIDTH,RESOLUTION_HEIGTH,0,0);
	}
	
	
	/**
	 * Creates podcast using default parameters
	 * @param recording
	 * @param batch
	 * @return True: Podcast created successfully.<br>False: Canceled by user.
	 * @throws Exception
	 */
	public static boolean createPodcast(Recording recording, boolean batch,boolean ShowProgressmonitor) throws Exception {
		return createPodcast(recording, RESOLUTION_WIDTH, RESOLUTION_HEIGTH, batch, ShowProgressmonitor);
	}
	
	/**
	 * Creates podcast using default parameters
	 * @param recording
	 * @param batch
	 * @return True: Podcast created successfully.<br>False: Canceled by user.
	 * @throws Exception
	 */
	public static boolean createPodcast(Recording recording, int resolutionWidth, int resolutionHeight, boolean batch,boolean ShowProgressmonitor) throws Exception {
      return createPodcast(recording, resolutionWidth, resolutionHeight, FRAMES_PER_SEC, batch, ShowProgressmonitor,resolutionWidth,resolutionHeight,0,0);
	}
	
	static boolean windows=System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
	private static String file2str(String filename) {
		if (!windows) return filename.replace(" ", "\\ ");
		return "\""+filename+"\"";
	}
	
	/**
	 * Creates podcast
	 * @param recording
	 * @param resolutionWidth Podcast width
	 * @param resolutionHeight Podcast heigth
	 * @param framesPerSec Frames per second
	 * @param batch
   * @param croppedw cropped frame width
   * @param croppedh cropped frame height
   * @param croppedx cropped frame x offset
   * @param croppedy cropped frame y offset
	 * @return True: Podcast created successfully.<br>False: Canceled by user
	 * @throws Exception
	 */
    public static boolean createPodcast(Recording recording, int resolutionWidth, int resolutionHeight, double framesPerSec, boolean batch, final boolean ShowProgressmonitor,int croppedw,int croppedh, int croppedx,int croppedy) throws Exception {
		if(TTT.verbose){
		System.out.println("----------------------------------------------");
		System.out.println("PodcastCreator");
		System.out.println("----------------------------------------------");
		System.out.println("Creating mp4 podcast");
		}
		//check whether the necessary applications are available
		String ffmpegCmd = Exec.getCommand(FFMPEG);
		if (ffmpegCmd == null) {
			throw new IOException("ffmpeg not found");
		}
		String mp4BoxCmd = Exec.getCommand(MP4BOX);
		if (mp4BoxCmd == null) {
			throw new IOException("MP4Box not found");
		}
		//get audio file
		File audioFile = recording.getExistingFileBySuffix(new String[] {"wav","mp3","mp2"});
		if (audioFile.exists() == false) {
			throw new IOException("No audio file found");
		}
		
		//initialization
		long startTime = System.currentTimeMillis();
		File outMovieFile = File.createTempFile("tmpOutMovie",".mp4");	//final output
		File outMovieTmpFile = File.createTempFile("tmpOutMovie",".mp4");	//temporary output for joined window movies
		File windowMovieFile = File.createTempFile("tmpWindowMovie", ".mp4");	//window movie created from png file		
		File windowImageFile = File.createTempFile("tmpWindowImage", ".png");	//image of window movie
		double frameLength = (double)1000/framesPerSec;
		double outMovieLength = 0;	//current length of outMovieFile
		int vFrames;	//number of video frames of window movie
		int i = 0;
		int j;
		//TODO the handling of the ProgressMonitor is INSANELY dirty
		final ProgressMonitor progressMonitor;
		if(ShowProgressmonitor){
			progressMonitor = new ProgressMonitor(TTT.getRootComponent(), null, "building podcast video stream", 0, recording.messages.size());	//time per frame is roughly the same for video and audio encoding
		}else{
			progressMonitor = null;
		}
		
		
		final Exec exec = new Exec();
		if(TTT.verbose){
		System.out.println("Building podcast video stream from messages");
		}
		recording.whiteOut();
		while (i < recording.messages.size()) {
		
			//draw all messages of the next frame
			while (i < recording.messages.size() && recording.messages.get(i).getTimestamp() - outMovieLength <= frameLength) {
				recording.deliverMessage(recording.messages.get(i++));
			
			}
			//the number of video frames depends on the timestamp of the succeeding message
			//if the next message occurs in x frames relative to the current frame, the next window lasts x-1 frames because nothing happens   
			if (i < recording.messages.size()) {
				vFrames = (int)((recording.messages.get(i).getTimestamp() - outMovieLength) / frameLength); 
			} else {
				vFrames = (int)((recording.getDuration() - outMovieLength) / frameLength);
				if (vFrames == 0) {
					vFrames = 1;
				}
			}
			outMovieLength += vFrames * frameLength;
			
			if (ShowProgressmonitor && !batch && i < recording.messages.size()) {
				progressMonitor.setProgress(i);
			}
			//create window movie using ffmpeg
			//write scaled window image
			ImageIO.write(ImageCreator.getScaledInstance(recording.getGraphicsContext().getScreenshot(), resolutionWidth, resolutionHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true), "png", windowImageFile);
			if (!TTT.debug) windowMovieFile.delete();
			exec.createListenerStream();
			j = exec.exec(new String[] {
                ffmpegCmd,
                "-loop", "1",
                "-r", String.valueOf(framesPerSec),
                "-pix_fmt", "rgb24",
                "-i", file2str(windowImageFile.getPath()),
                "-vcodec", "mpeg4",
                "-vframes", String.valueOf(vFrames),
                "-s",
                croppedw + "x" + croppedh,
                "-y",
                "-vf",
                "crop="+croppedw+":"+croppedh+":"+croppedx+":"+croppedy,
                "-b:v", String.valueOf(34)+"k",
                file2str(windowMovieFile.getPath())
            });
			
			if (j != 0 || windowMovieFile.length() == 0) {
				//error while creating window movie
				if (!TTT.debug) {
				windowMovieFile.delete();
				outMovieFile.delete();
				outMovieTmpFile.delete();
				windowImageFile.delete();
				}
				TTT.verbose("Unable to create window movie using ffmpeg:");
				TTT.verbose(exec.getListenerStream());
				throw new IOException("unable to create window movie using ffmpeg");
			}
			if (ShowProgressmonitor & !batch && progressMonitor.isCanceled()) {
				//canceled by user
				windowMovieFile.delete();
				outMovieFile.delete();
				outMovieTmpFile.delete();
				windowImageFile.delete();
				if(ShowProgressmonitor){
				progressMonitor.close();}
				if(TTT.verbose){
				System.out.println("Canceled by user");}
				windowMovieFile.delete();
				outMovieFile.delete();
				outMovieTmpFile.delete();
				return false;
			}
			if (outMovieFile.length() == 0) {
				
				TTT.debug(outMovieFile.getAbsolutePath() + " has size "+outMovieFile.length());
				TTT.debug("First frame assumed; skipping concatenation");
				TTT.debug(windowMovieFile.getAbsolutePath() + " has size "+windowMovieFile.length());
					
				
				//the first window movie can be renamed directly to output movie.
				//NOTE: MP4Box uses fps=1 for the container format when vFrames=1 whereby the container frame rate and codec frame rate can differ when using frameRate != 1. That causes a wrong synchronized video and audio stream
				outMovieTmpFile.delete();	//For renaming files on a windows system, the destination file may not exist
				windowMovieFile.renameTo(outMovieTmpFile);
			} else {
				//append the created window movie (windowMovieFile) to the output movie (outMovieFile) using MP4Box
				//NOTE: appending slideMovieFile to outMovieFile directly via "MP4Box -cat slideMovieFile.getPath() outMovieFile.getPath()" causes renaming problems in some cases. Thus outMovieTmpFile is used
				exec.createListenerStream();
				String [] line = new String[] { 
						mp4BoxCmd, 
						"-cat", 
						windowMovieFile.getPath(), 
						outMovieFile.getPath(), 
						"-out", 
						outMovieTmpFile.getPath()
						};
				j = exec.exec(line);
				if (j != 0 || outMovieTmpFile.length() == 0) {
					//error while appending the slideMovie to the output file
					if (!TTT.debug) {
					windowMovieFile.delete();
					outMovieFile.delete();
					outMovieTmpFile.delete();
					windowImageFile.delete();
					}
					String cmdline="";
					for (String s:line) cmdline+=s+" ";
					
						TTT.verbose("Unable join slide movies using the command:");
						TTT.verbose(cmdline);
						TTT.verbose(exec.getListenerStream());
					throw new IOException("unable join slide movies using \n"+cmdline);
				}
			}
			//replace outMovieFile by outMovieFileTmp
			if (!TTT.debug) outMovieFile.delete();
			if (i < recording.messages.size()) {
				outMovieFile.delete();
				outMovieTmpFile.renameTo(outMovieFile);
				TTT.debug("renamed tmp to outfile");
			}
		}
		if (!TTT.debug) {
			windowMovieFile.delete();
			outMovieFile.delete();
			windowImageFile.delete();
		}
		
		if(TTT.verbose){
		//audio encoding with ffmpeg. The audio stream must be converted via aac to achieve ipod compatibility
		System.out.println("Adding audio stream to podcast");
		}
		Timer timer = null;
		if (!batch) {			
			//the progress of the progress monitor is determined by the frame value ("frame= ") of the ffmpeg output
			final int nFrames = recording.getDuration()/1000;
			if(ShowProgressmonitor){
			progressMonitor.setNote("adding audio stream to podcast");
			progressMonitor.setMaximum(nFrames);
			progressMonitor.setProgress(0);
			}
			timer = new Timer(1000, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (ShowProgressmonitor && progressMonitor.isCanceled()) {
						exec.abort();
					}
					//get the value after "frame=" of the last line
					String[] lines = exec.getListenerStream().toString().split("\n");
					Scanner scanner = new Scanner(lines[lines.length-1]);
					scanner.useDelimiter("[ ]+");
					if (scanner.findInLine("frame=") != null && scanner.hasNextInt()){
						int i = scanner.nextInt();
						//if(TTT.verbose){
						// System.out.println("   Frame (" + i + "/" + nFrames + ")");
						//}
						if(ShowProgressmonitor){
						progressMonitor.setProgress(i);}
					}
					exec.getListenerStream().reset();
				}
			});				
			timer.start();
		}
		exec.createListenerStream();
		outMovieFile = recording.getFileBySuffix("mp4");
		String[] line = new String[] {
				ffmpegCmd,
				"-i", file2str(audioFile.getPath()),
				"-i", file2str(outMovieTmpFile.getPath()),
				"-strict","experimental",
				"-c:a","aac",
				"-b:a", "32k",
				"-b:v", "32k",
				"-y",
				file2str(outMovieFile.getPath())
			};
    
    	j=exec.exec(line);
    	
		
		if (!TTT.debug) outMovieTmpFile.delete();	
		if (!batch) {
			timer.stop();			
			if (ShowProgressmonitor&&progressMonitor.isCanceled()) {
				//canceled by user
				windowMovieFile.delete();
				outMovieFile.delete();
				outMovieTmpFile.delete();
				windowImageFile.delete();
				if(TTT.verbose){
				System.out.println("Canceled by user");
				}
				return false;
			}
			if(ShowProgressmonitor){
			progressMonitor.close();}
		}
		if (j != 0 || outMovieFile.length() == 0) {
			//error while adding audio stream
			String cmdline="";
			for (String s:line) cmdline+=s+" ";
			if(TTT.verbose){
			System.err.println("Unable add audio stream using the command:");
			System.err.println(cmdline);
			System.out.println(exec.getListenerStream());
			}else
				outMovieFile.delete();
			throw new IOException("unable to add audio stream via \n"+cmdline);
		}
		if(TTT.verbose){
		System.out.println("Podcast created in " + Constants.getStringFromTime((int)(System.currentTimeMillis()-startTime)));
		System.out.println("----------------------------------------------");
		}
		return true;
	}	
	
} 
