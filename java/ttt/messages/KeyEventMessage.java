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
 * Created on 11.01.2006
 *
 * Author: Peter Ziewer, TU Munich, Germany - ziewer@in.tum.de
 */
package ttt.messages;

import java.io.IOException;
import java.io.OutputStream;

import ttt.Constants;

public class KeyEventMessage extends UserInputMessage {
    private byte[] buffer = new byte[8];

    public KeyEventMessage(int keysym, boolean down) {
        buffer[0] = (byte) Constants.KeyboardEvent;
        buffer[1] = (byte) (down ? 1 : 0);
        buffer[2] = (byte) 0;
        buffer[3] = (byte) 0;
        buffer[4] = (byte) ((keysym >> 24) & 0xff);
        buffer[5] = (byte) ((keysym >> 16) & 0xff);
        buffer[6] = (byte) ((keysym >> 8) & 0xff);
        buffer[7] = (byte) (keysym & 0xff);
    }

    public void writeRFB(OutputStream out) throws IOException {
        out.write(buffer);
    }
}
