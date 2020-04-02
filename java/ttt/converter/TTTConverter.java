package ttt.converter;

import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.util.zip.DeflaterOutputStream;
import java.io.ByteArrayOutputStream;
import java.awt.Rectangle;
import ttt.messages.HextileMessage;
import ttt.messages.Message;
import java.util.ArrayList;
import ttt.record.Recording;
import java.io.File;
import java.io.IOException;

public class TTTConverter
{
    public static void main(final String[] arguments) throws IOException {
        if (arguments.length < 0) {
            printHelp();
            return;
        }
        final String s = arguments[0];
        switch (s) {
            case "-h": {
                printHelp();
                break;
            }
            default: {
                if (arguments.length < 2) {
                    System.out.println("Wrong number of inputs");
                    break;
                }
                final String from = arguments[0];
                final String to = arguments[1];
                convertRecording(from, to);
                break;
            }
        }
    }
    
    private static void printHelp() {
        System.out.println("Help for TTTConverter:");
        System.out.println("'TTTConverter -h' for help");
        System.out.println("'TTTConverter from.ttt to.ttt' to convert the file 'from.ttt' and save it in the file 'to.ttt'");
    }
    
    private static void convertRecording(final String from, final String to) throws IOException {
        final Recording recording = new Recording(new File(from), false);
        final ArrayList<FullFrameContainer> containerList = createContainer(recording);
        compressData(containerList);
        writeFile(compressHeader(recording, containerList), containerList, to);
    }
    
    private static ArrayList<FullFrameContainer> createContainer(final Recording recording) {
        final ArrayList<FullFrameContainer> containerList = new ArrayList<FullFrameContainer>();
        FullFrameContainer actualContainer = new FullFrameContainer();
        containerList.add(actualContainer);
        for (final Message message : recording.getMessages().getMessages()) {
            if (message.getEncoding() != 5) {
                actualContainer.addMessage(message);
            }
            else {
                final Rectangle bounds = ((HextileMessage)message).getBounds();
                if (bounds.width == recording.getProtocolPreferences().framebufferWidth && bounds.height == recording.getProtocolPreferences().framebufferHeight) {
                    actualContainer = new FullFrameContainer();
                    containerList.add(actualContainer);
                }
                actualContainer.addMessage(message);
            }
        }
        return containerList;
    }
    
    private static void compressData(final ArrayList<FullFrameContainer> containerList) throws IOException {
        int offset = 0;
        for (final FullFrameContainer container : containerList) {
            container.setOffset(offset);
            System.out.println("deflated container, offset: " + offset);
            offset += container.writeMessages();
        }
    }
    
    private static byte[] compressHeader(final Recording recording, final ArrayList<FullFrameContainer> containerList) throws IOException {
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(new DeflaterOutputStream(data));
        recording.writeInit(out);
        out.writeLong(recording.getProtocolPreferences().starttime);
        recording.writeExtensions(out);
        out.writeInt(containerList.size());
        for (final FullFrameContainer container : containerList) {
            container.writeFullFrameHeader(out);
        }
        out.close();
        data.close();
        return data.toByteArray();
    }
    
    private static void writeFile(final byte[] header, final ArrayList<FullFrameContainer> containerList, final String to) throws IOException {
        final FileOutputStream fileOut = new FileOutputStream(to);
        final DataOutputStream out = new DataOutputStream(fileOut);
        out.write("TTT 001.002\n".getBytes());
        out.writeInt(header.length + 16);
        out.write(header);
        for (final FullFrameContainer container : containerList) {
            container.writeData(out);
        }
        System.out.println("written file " + to);
        out.close();
    }
}