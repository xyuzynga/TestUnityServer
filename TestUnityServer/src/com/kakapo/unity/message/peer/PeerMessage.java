package com.kakapo.unity.message.peer;

import com.kakapo.unity.message.Message;
import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;

public abstract class PeerMessage extends Message {

    private final Set<String> _extensions;
    private final CharSequence _input;
    private final CharSequence _id;
    private final CharSequence _sender;

    public PeerMessage(String command, CharSequence id, CharSequence sender, Set<String> extensions, CharSequence input) {
        super(command);
        this._id = id;
        this._sender = sender;
        this._extensions = extensions;
        this._input = input;
    }

    public Set<String> getExtensions() {
        return this._extensions;
    }

    public CharSequence getInput() {
        return this._input;
    }

    public CharSequence getId() {
        return this._id;
    }

    public CharSequence getSender() {
        return this._sender;
    }
    
    /**
     *To ensure getEncodedMessage() is implemented in subclasses
     * @return ChannelBuffer
     */
    public abstract ChannelBuffer getEncodedMessage();
}