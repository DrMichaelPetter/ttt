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
 * Created on 16.03.2006
 *
 * Author: Peter Ziewer, TU Munich, Germany - ziewer@in.tum.de
 */
package ttt.player;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;


import ttt.Constants;
import ttt.RfbProtocol;
import ttt.TTT;
import ttt.gui.GraphicsContext;
import ttt.messages.Annotation;
import ttt.messages.CursorPositionMessage;
import ttt.messages.DeleteAllAnnotation;
import ttt.messages.DeleteAnnotation;
import ttt.messages.FreehandAnnotation;
import ttt.messages.HighlightAnnotation;
import ttt.messages.LineAnnotation;
import ttt.messages.RectangleAnnotation;
import ttt.messages.TextAnnotation;
import ttt.messages.WhiteboardMessage;

public class PaintListener extends RFBKeyAndMouseListener implements KeyEventDispatcher{

	static int  instancecount = 0;
    GraphicsContext graphicsContext;
    private PaintControls paintControls;
    private Player player;
    

    public PaintListener(GraphicsContext graphicsContext, Player player) {
        super(graphicsContext);
    	this.player=player;
        this.graphicsContext = graphicsContext;         
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
         
    }

    void setPaintControls(PaintControls controls) {    	  
    	paintControls = controls;    
    }

    // temporary annotation
    private Annotation annotation;
    
    // true when text is edited
	private boolean textMode = false;
	
    // enable/disable painting
    private boolean activated;

    // set cursor shapes
    static Cursor paintCursor;
    static {
    	
        Image cursorImage = Constants.getIcon("PaintCursor24.png").getImage();
       
        // compare sizes of image and system default
        Dimension pngDimension = new Dimension(cursorImage.getWidth(null), cursorImage.getHeight(null));
        Dimension dimension = Toolkit.getDefaultToolkit().getBestCursorSize(pngDimension.width, pngDimension.height);

        // adjust cursor size if needed
        if (dimension.equals(pngDimension) || dimension.width == 0 || dimension.height == 0) {
            // no adjustment needed
        
        } else {
            // adjust cursor size
        	/*
            BufferedImage image = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_4BYTE_ABGR);
            image.createGraphics().drawImage(cursorImage, 0, 0, null);
            cursorImage = image;
            */
        }

        // set cursor shape
        paintCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(3, 19), "Paint Cursor");
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
        player.indicateOverlay(activated);
        
        // set cursor
        if (activated) {

            // TODO: remove nasty hack
            // paint cursor
            ((RfbProtocol) graphicsContext).setCustomCursor(paintCursor);

            // no soft cursor
            graphicsContext.showSoftCursor(false);
        } else {
            // terminate (if needed)
            finishPainting();

            // reset default system cursor
            // TODO: nasty hack
            ((RfbProtocol) graphicsContext).setCustomCursor(null);

            // set position for soft cursor
            Point position = MouseInfo.getPointerInfo().getLocation();
            Point diff = graphicsContext.getLocationOnScreen();
            // NOTE: could be done local, but without notifying recorder and clients
            writeMessage(new CursorPositionMessage(0, position.x - diff.x, position.y - diff.y));

            // show soft cursor (if available) or default (otherwise)
            graphicsContext.showSoftCursor(true);
        }
    }

    private int paintMode = Constants.AnnotationFreehand;

    public void setPaintMode(int paintMode) {
    	// end text input if not yet finished
    	if(textMode) finishPainting();
        if (paintMode == Constants.AnnotationDeleteAll)
            // delete all is no permanent mode
            writeMessage(new DeleteAllAnnotation(0));
        else
            // set mode
            this.paintMode = paintMode;
    }

    public int togglePaintMode() {
        // switch to next next mode
        if (paintMode == Constants.AnnotationText)
            paintMode = Constants.AnnotationFreehand;
        else if (paintMode == Constants.AnnotationFreehand)
            paintMode = Constants.AnnotationHighlight;
        else if (paintMode == Constants.AnnotationHighlight)
            paintMode = Constants.AnnotationLine;
        else if (paintMode == Constants.AnnotationLine)
            paintMode = Constants.AnnotationRectangle;
        else if (paintMode == Constants.AnnotationRectangle)
            paintMode = Constants.AnnotationDelete;
        else if (paintMode == Constants.AnnotationDelete)
            paintMode = Constants.AnnotationText;

        // update GUI
        if (paintControls != null)
            paintControls.selectPaintModeButton(paintMode);

        return paintMode;
    }

    private int color = Annotation.Red;

    public void setColor(int color) {
        this.color = color;
    }

    public int toggleColor() {
        // switch to next next color
        if (color == Annotation.Red)
            color = Annotation.Blue;
        else if (color == Annotation.Blue)
            color = Annotation.Green;
        else if (color == Annotation.Green)
            color = Annotation.Yellow;
        else if (color == Annotation.Yellow)
            color = Annotation.Black;
        else if (color == Annotation.Black)
            color = Annotation.Red;

        // update GUI
        if (paintControls != null)
            paintControls.selectColorButton(color);

        return color;
    }

    // /////////////////////////////////////////////////////
    // whiteboard controls
    // /////////////////////////////////////////////////////

    private boolean whiteboardEnabled;

    public void toggleWhiteboard() {
        enableWhiteboard(!whiteboardEnabled);
    }

    public void enableWhiteboard(boolean enable) {
        if (enable && currentWhiteboardPage == 0)
            currentWhiteboardPage = 1;
        writeMessage(new WhiteboardMessage(0, enable ? currentWhiteboardPage : 0, graphicsContext.prefs));
        whiteboardEnabled = enable;

        // always activate painting mode during whiteboard mode
        if (whiteboardEnabled && !activated)
            setActivated(true);
    }

    private int currentWhiteboardPage = 0;
    private int nextFreeWhiteboardNumber = 1;

    public void enableWhiteboard(int page) {
        if (page < 0)
            page = 0;
        if (page > 127)
            page = 127;
        writeMessage(new WhiteboardMessage(0, page, graphicsContext.prefs));
        whiteboardEnabled = page > 0;

        // always activate painting mode during whiteboard mode
        if (whiteboardEnabled && !activated)
            setActivated(true);

        currentWhiteboardPage = page;
        nextFreeWhiteboardNumber = Math.max(nextFreeWhiteboardNumber, page+1);

        paintControls.setWhiteboardSelected(page > 0);
    }

    public void nextWhiteboard() {
        enableWhiteboard(currentWhiteboardPage + 1);
    }

    public void previousWhiteboard() {
        enableWhiteboard(currentWhiteboardPage - 1);
    }
    
    //MODMSG
    public void newWhiteboardPage() {
    	enableWhiteboard(nextFreeWhiteboardNumber);
    }

    // /////////////////////////////////////////////////////
    // Handle mouse events.
    // /////////////////////////////////////////////////////
    public void mousePressed(MouseEvent event) {
        if (!areCoordinatesValid(event))
            return;

        // create and write temporary annotation
        if (textMode) {
        	finishPainting();
        } else if (activated && !alt_tab) {
            set(event.getX(), event.getY());
            writeMessage(annotation);
        } else
            super.mousePressed(event);
      
    }

    public void mouseReleased(MouseEvent event) {
        if (!areCoordinatesValid(event))
            return;
        
        // write final annotation
        if (!textMode && activated && !alt_tab)
            finishPainting();
        else
            super.mouseReleased(event);
    }

    private void finishPainting() {
    	if(textMode) {
    		textMode = false;
    		// if text annotation is still empty, discard it
    		if (((TextAnnotation)annotation).isEmpty()) {
    			annotation = null;
    		} else {
    			((TextAnnotation)annotation).trim();
    		}
    	}
        if (annotation != null) {
            // NOTE: reset last coordinates not needed - same as last dragged event
            annotation.temporary = false;
            // force consumer to reset timestamp
            annotation.setTimestamp(0);
            writeMessage(annotation);
            annotation = null;
        }
    }

    public void mouseMoved(MouseEvent event) {
        if (!areCoordinatesValid(event))
            return;

        if (activated && !alt_tab) {
            // TODO: to move or not to move?
        } else
            super.mouseMoved(event);
    }

    public void mouseDragged(MouseEvent event) {
        if (!areCoordinatesValid(event))
            return;

        // fix coordinates and write temporary annotation
        if (activated && !alt_tab) {
            reset(event.getX(), event.getY());
            writeMessage(annotation);
        } else
            super.mouseDragged(event);
    }

    private boolean restoreCursor;

    private boolean areCoordinatesValid(MouseEvent event) {
        boolean areCoordinatesValid = !(event.getX() < 0 || event.getX() > graphicsContext.prefs.framebufferWidth
                || event.getY() < 0 || event.getY() > graphicsContext.prefs.framebufferHeight);

        if (!areCoordinatesValid && !restoreCursor) {
            // terminate painting
            finishPainting();

            // show cursor outside
            restoreCursor = true;

            // TODO: nasty hack
            ((RfbProtocol) graphicsContext).setCustomCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        } else if (areCoordinatesValid && restoreCursor) {
            // restore cursor inside
            if (activated)
                ((RfbProtocol) graphicsContext).setCustomCursor(paintCursor);
            else
                ((RfbProtocol) graphicsContext).setCustomCursor(null);
            restoreCursor = false;
        }

        return areCoordinatesValid;
    }

    // initiates new temporary annotation
    private void set(int x, int y) {
    
        switch (paintMode) {
        case Constants.AnnotationText:
        	annotation = new TextAnnotation(0, color, x, y, 0, "");
        	annotation.temporary = true;   
        	textMode = true;   	
        	break;
        case Constants.AnnotationHighlight:
            annotation = new HighlightAnnotation(0, color + 3, x, y, x, y);
            annotation.temporary = true;
            break;
        case Constants.AnnotationRectangle:
            annotation = new RectangleAnnotation(0, color, x, y, x, y);
            annotation.temporary = true;
            break;
        case Constants.AnnotationLine:
            annotation = new LineAnnotation(0, color, x, y, x, y);
            annotation.temporary = true;
            break;
        case Constants.AnnotationFreehand:
            annotation = new FreehandAnnotation(0, color);
            ((FreehandAnnotation) annotation).addPoint(x, y);
            annotation.temporary = true;
            break;
        case Constants.AnnotationDelete:
            annotation = new DeleteAnnotation(0, x, y);
        }

    }

    // updates temporary annotation
    private void reset(int x, int y) {
        // fix if startpoint was outside (can happen during fullscreen)
        if (annotation == null) {
            set(x, y);
            return;
        }

        switch (paintMode) {
        case Constants.AnnotationHighlight:
            ((HighlightAnnotation) annotation).setEndPoint(x, y);
            break;
        case Constants.AnnotationRectangle:
            ((RectangleAnnotation) annotation).setEndPoint(x, y);
            break;
        case Constants.AnnotationLine:
            if (annotation instanceof LineAnnotation) {
                Point point = ((LineAnnotation) annotation).getStartPoint();
                // set horizontal or vertical line depending on x-delta and y-delta
                if (Math.abs(point.x - x) > Math.abs(point.y - y))
                    // reset y
                    y = point.y;
                else
                    // reset x
                    x = point.x;
            }
            ((LineAnnotation) annotation).setEndPoint(x, y);
            break;
        case Constants.AnnotationFreehand:
            ((FreehandAnnotation) annotation).addPoint(x, y);
            break;
        case Constants.AnnotationDelete:
            annotation = new DeleteAnnotation(0, x, y);
        }
    }

    @Override
    public void keyTyped(KeyEvent evt) {
    	super.keyTyped(evt);		
    }
    
    
    public void triggerPageDown() {
    	
    	super.keyPressed(new KeyEvent(player,KeyEvent.KEY_PRESSED,System.currentTimeMillis(),0,KeyEvent.VK_PAGE_DOWN,KeyEvent.CHAR_UNDEFINED));
    }
    
    public void triggerPageUp() {
    	super.keyPressed(new KeyEvent(player,KeyEvent.KEY_PRESSED,System.currentTimeMillis(),0,KeyEvent.VK_PAGE_UP,KeyEvent.CHAR_UNDEFINED));
    }
    
    @Override
    public void keyPressed(KeyEvent evt) {

        // TODO: add option panel to set keys
    	if (!textMode) { 
	        switch (evt.getKeyCode()) {
	        case KeyEvent.VK_PAGE_UP:
	        case KeyEvent.VK_PAGE_DOWN:
	        case KeyEvent.VK_RIGHT:
	        case KeyEvent.VK_LEFT:
	        case KeyEvent.VK_UP:
	        case KeyEvent.VK_DOWN:
	            // Automatically delete annotations
	            writeMessage(new DeleteAllAnnotation(0));
	            break;
	
	        case KeyEvent.VK_F9:
	            // switch paint color (rotate)
	            if (!activated) {
	                setActivated(true);
	                paintControls.updateActivatedState(activated);
	            }
	            toggleColor();
	            // consume key
	            return;
	
	        case KeyEvent.VK_F10:
	            // switch paint mode (rotate)
	            if (!activated) {
	                setActivated(true);
	                paintControls.updateActivatedState(activated);
	            }
	            togglePaintMode();
	            // consume key
	            return;
	
	        case KeyEvent.VK_F12:
	            // activate / deactivte painting
	            setActivated(!activated);
	            paintControls.updateActivatedState(activated);
	            // consume key
	            return;
	        }
    	}
        super.keyPressed(evt);
    }

    @Override
    public void keyReleased(KeyEvent evt) {
        // filter paint control keys of ttt
        // TODO: option to set keys
    	if (!textMode) {
	        switch (evt.getKeyCode()) {
	        case KeyEvent.VK_F9:
	        case KeyEvent.VK_F10:
	        case KeyEvent.VK_F12:
	            // consume key
	            return;
	        }
    	}
        super.keyReleased(evt);
    }

	@Override
	public boolean dispatchKeyEvent(KeyEvent evt) {
		/* this method is called 3 times
		 * one time for the keytyped event with the event id = 400
		 * one time for the keypressed event with the event id = 401
		 * and the third time with keyreleased event with the id = 402
		 * */	
		if (textMode) {	
		if(evt.getID() == 400){					
				if(evt.getKeyChar() != KeyEvent.CHAR_UNDEFINED &&
						(evt.getKeyChar() > 31 || evt.getKeyChar() == KeyEvent.VK_ENTER)) {
					// add non special characters to text annotation		
					
					((TextAnnotation)annotation).addChar(evt.getKeyChar());
					graphicsContext.refresh();
					evt.consume();
				}
				if (evt.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
					// delete last character when backspace pressed
					((TextAnnotation)annotation).deleteLastChar();
					graphicsContext.refresh();
					evt.consume();
				}					
	    	}	
			return true;
		}				
		return false;
	}

	public int getPaintMode() {
		return this.paintMode;
		
	}
	
}

