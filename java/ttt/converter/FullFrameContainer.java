package ttt.converter;

import java.util.zip.DeflaterOutputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import ttt.messages.Message;
import java.util.ArrayList;

class FullFrameContainer
{
    private ArrayList<Message> messages;
    private ByteArrayOutputStream data;
    private int offset;
    
    FullFrameContainer() {
        this.messages = new ArrayList<Message>();
    }
    
    void addMessage(final Message message) {
        this.messages.add(message);
    }
    
    void setOffset(final int offset) {
        this.offset = offset;
    }
    
    int getOffset() {
        return this.offset;
    }
    
    void writeFullFrameHeader(final DataOutputStream os) throws IOException {
        if (this.messages.size() <= 0) {
            return;
        }
        os.writeInt(this.messages.get(0).getTimestamp());
        os.writeInt(this.offset);
    }
    
    int writeMessages() throws IOException {
        int lastTimestamp = -1;
        this.data = new ByteArrayOutputStream();
        final DataOutputStream os = new DataOutputStream(new DeflaterOutputStream(this.data));
        for (final Message message : this.messages) {
            final int timestamp = message.getTimestamp();
            if (timestamp == lastTimestamp) {
                message.write(os, -1);
            }
            else {
                message.write(os, timestamp);
            }
            lastTimestamp = timestamp;
        }
        os.close();
        this.data.close();
        return this.data.size();
    }
    
    void writeData(final DataOutputStream os) throws IOException {
        os.write(this.data.toByteArray());
    }
}