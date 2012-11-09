package com.kakapo.unity.message.kempcodec;

import com.kakapo.unity.message.ContactAction;
import com.kakapo.unity.message.ExtensionStatus;
import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.ScheduledStatus;
import com.kakapo.unity.message.client.AddScheduledStatusMessage;
import com.kakapo.unity.message.client.ClearStatusMessage;
import com.kakapo.unity.message.client.ListScheduledStatusesMessage;
import com.kakapo.unity.message.client.RegisterMessage;
import com.kakapo.unity.message.client.RegisterMessage2;
import com.kakapo.unity.message.client.RemoveScheduledStatusMessage;
import com.kakapo.unity.message.client.SetStatusMessage;
import com.kakapo.unity.message.interserver.ServerContactList;
import com.kakapo.unity.message.interserver.ServerIM;
import com.kakapo.unity.message.interserver.ServerRegisterMessage;
import com.kakapo.unity.message.interserver.ServerSetStatus;
import com.kakapo.unity.message.interserver.ServerStatusListMessage;
import com.kakapo.unity.message.kempcodec.legacy.AppendableBuffers;
import com.kakapo.unity.message.peer.CustomMessage;
import com.kakapo.unity.message.peer.TextMessage;
import com.kakapo.unity.message.peer.TextMessage2;
import com.kakapo.unity.message.server.ContactList2Message;
import com.kakapo.unity.message.server.ContactListMessage;
import com.kakapo.unity.message.server.ScheduledStatusListMessage;
import com.kakapo.unity.message.server.StatusListMessage;
import com.kakapo.unity.message.server.error.ErrorMessage;
import com.kakapo.unity.message.server.error.InternalErrorMessage;
import com.kakapo.unity.message.server.error.OverrideMessage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author prijo.pauly
 */
public class PrijosCodec implements MessageCodec {

    private final Pattern pattern = Pattern.compile("(.*)[\\r]?[\\n]?");
    private final Pattern Register = Pattern.compile("Command: Register[\n]Group: (.*)[\n]Extension: (.*)[\n]Override: (.*)(\n\n)");
    private final Pattern Register2 = Pattern.compile("Command: Register2[\n]Group: (.*)[\n]LoginID: (.*)[\n]CheckSum: (.*)[\n]ProductName: (.*)(\n\n)");
    private final Pattern SetStatus = Pattern.compile("Command: SetStatus[\n]Extension: (.*)[\n]Status: (.*)[\n]Override: (.*)(\n\n)");
    private final Pattern AddScheduledStatus = Pattern.compile("Command: AddScheduledStatus[\n]Extension: (.*)[\n]Status: (.*)[\n]Id: (.*)[\n]Start: (.*)[\n]End: (.*)(\n\n)");
    private final Pattern ListScheduledStatuses = Pattern.compile("Command: ListScheduledStatuses[\n]Extension: (.*)[\n]Start: (.*)[\n]End: (.*)(\n\n)");
    private final Pattern ClearStatus = Pattern.compile("Command: ClearStatus[\n]Extension: (.*)(\n\n)");
    private final Pattern RemoveScheduledStatus = Pattern.compile("Command: RemoveScheduledStatus[\n]Extension: (.*)[\n]Id: (.*)(\n\n)");
    private final Pattern Message = Pattern.compile("Command: Message[\n]Id: (.*)[\n]Sender: (.*)[\n]Share: (.*)[\n]Extensions: (.*)[\n]Length: (.*)[\n](.*)(\n\n)");
    private final Pattern Message2 = Pattern.compile("Command: Message2[\n]Id: (.*)[\n]Sender: (.*)[\n]DateTime: (.*)[\n]Extensions: (.*)[\n]Length: (.*)[\n](.*)(\n\n)");
    private final Pattern Custom = Pattern.compile("Command: Custom[\n]Id: (.*)[\n]Sender: (.*)[\n]DateTime: (.*)[\n]Extensions: (.*)[\n]Length: (.*)[\n](.*)(\n\n)");
    private final Pattern ServerRegister = Pattern.compile("Command: ServerRegister[\n]ServerName: (.*)(\n\n)");
    private final Pattern ServerContactList = Pattern.compile("Command: ServerContactList[\n]Group: (.*)[\n](Add||Remove)-Contact: (.*)(\n\n)");
    private final Pattern ServerContactList2 = Pattern.compile("Command: ServerContactList[\n]Group: (.*)[\n](Add||Remove)-Contact: (.*) (.*)(\n\n)");
    private final Pattern ServerMessage = Pattern.compile("Command: ServerMessage[\n]Group: (.*)[\n]Command: Message[\n]Id: (.*)[\n]Sender: (.*)[\n]Share: (.*)[\n]Extensions: (.*)[\n]Length: (.*)[\n](.*)(\n\n)");
    private final Pattern ServerMessage2 = Pattern.compile("Command: ServerMessage[\n]Group: (.*)[\n]Command: (Message2||Custom)[\n]Id: (.*)[\n]Sender: (.*)[\n]DateTime: (.*)[\n]Extensions: (.*)[\n]Length: (.*)[\n](.*)(\n\n)");
    private final Pattern ServerSetStatus = Pattern.compile("Command: ServerSetStatus[\n]Group: (.*)[\n]Extension: (.*)[\n]Status: (.*)( )(.*)[\n]Override: (.*)(\n\n)");
    private final Pattern ServerStatusList = Pattern.compile("Command: ServerStatusList[\n]Group: (.*)[\n](ExtensionStatus: (.*) (.*) (.*)[\n])+(\n)");
    private final Pattern ExtensionStatus = Pattern.compile("ExtensionStatus: (.*) (.*) (.*)[\n]");
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
    private static final String DEFAULT_STATUS = "none";

    public PrijosCodec() {
    }

    public static void main(String[] args) {
//        String ToServer = "Command: Register\n"
//                + "Group: DRD Communication\n"
//                + "Extension: ChrisTutt@drd.co.uk\n"
//                + "Override: true\n\n";
        PrijosCodec cod = new PrijosCodec();
        String ToServer=cod.fromClient();
        //ServerStatusListMessage msg=(ServerStatusListMessage)cod.decodeKemp(ToServer);
        cod.encode(cod.decode(ToServer).message,new StringBuilder());
//        for (int i = 0; i < 10000000; i++) {
//            cod.kempEcoder(cod.decodeKemp(ToServer));
//        }
        //  System.out.println("Completed"+msg.toString());
    }

    private String fromClient() {
        String fromClient = "";
        try {
            StringBuilder obj = new StringBuilder();
            FileInputStream fstream = new FileInputStream("text.txt");
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                obj = (new StringBuilder().append(strLine));
                fromClient = fromClient.concat(obj.toString() + "\n");
            }
            in.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return fromClient;
    }

    @Override
    public DecodeResult decode(CharSequence messageFromClientOrServer) {
        Message decodedMessageObj = null;
        try {
            Matcher _matcher = pattern.matcher(messageFromClientOrServer);
            if (_matcher.find()) {
                CharSequence command = property(_matcher.group());
                try {
                    PrijosCodec.Client_Message current_Message = PrijosCodec.Client_Message.valueOf(command.toString());
                    switch (current_Message) {
                        case Register:
                            _matcher = Register.matcher(messageFromClientOrServer);
                            if (_matcher.find()) {
                                decodedMessageObj = new RegisterMessage(_matcher.group(1), _matcher.group(2), Boolean.parseBoolean(_matcher.group(3)));
                                System.out.println("Register Message");
                            } else {
                                // //throw Invalid KEMP format throw 
                                System.out.println("InValid Register Message");
                            }
                            break;
                        case Register2:
                            _matcher = Register2.matcher(messageFromClientOrServer);
                            if (_matcher.find()) {
                                decodedMessageObj = new RegisterMessage2(_matcher.group(1), _matcher.group(2), _matcher.group(3), _matcher.group(4));
                            } else {
                                //throw Invalid KEMP format throw 
                            }
                            break;
                        case SetStatus:
                            _matcher = SetStatus.matcher(messageFromClientOrServer);
                            if (_matcher.find()) {
                                decodedMessageObj = new SetStatusMessage(_matcher.group(1), _matcher.group(2), Boolean.parseBoolean(_matcher.group(3)));
                            } else {
                                //throw Invalid KEMP format throw 
                            }
                            break;
                        case AddScheduledStatus:
                            _matcher = AddScheduledStatus.matcher(messageFromClientOrServer);
                            if (_matcher.find()) {
                                decodedMessageObj = new AddScheduledStatusMessage(_matcher.group(1), new ScheduledStatus(_matcher.group(2), this.dateFormat.parse(_matcher.group(4)), this.dateFormat.parse(_matcher.group(5)), _matcher.group(3)));
                            } else {
                                //throw Invalid KEMP format throw 
                            }
                            break;
                        case ClearStatus:
                            _matcher = ClearStatus.matcher(messageFromClientOrServer);
                            if (_matcher.find()) {
                                decodedMessageObj = new ClearStatusMessage(_matcher.group(1));
                            } else {
                                //throw Invalid KEMP format throw 
                            }
                            break;
                        case RemoveScheduledStatus:
                            _matcher = RemoveScheduledStatus.matcher(messageFromClientOrServer);
                            if (_matcher.find()) {
                                decodedMessageObj = new RemoveScheduledStatusMessage(_matcher.group(1), _matcher.group(2));
                            } else {
                                //throw Invalid KEMP format throw 
                            }
                            break;
                        case ListScheduledStatuses:
                            _matcher = ListScheduledStatuses.matcher(messageFromClientOrServer);
                            if (_matcher.find()) {
                                decodedMessageObj = new ListScheduledStatusesMessage(_matcher.group(1), this.dateFormat.parse(_matcher.group(2)), this.dateFormat.parse(_matcher.group(2)));
                            }
                            break;
                    }
                } catch (IllegalArgumentException notClientMessageException) {
                    try {
                        PrijosCodec.Peer_Message current_Message = PrijosCodec.Peer_Message.valueOf(command.toString());
                        switch (current_Message) {
                            case Message:
                                _matcher = Message.matcher(messageFromClientOrServer);
                                if (_matcher.find()) {
                                    Set<String> extensions = new HashSet<String>();
                                    StringTokenizer tokenizer = new StringTokenizer(_matcher.group(4), " ,");
                                    while (tokenizer.hasMoreTokens()) {
                                        extensions.add(tokenizer.nextToken());
                                    }
                                    decodedMessageObj = new TextMessage(_matcher.group(6), extensions, messageFromClientOrServer, _matcher.group(1), _matcher.group(2), Boolean.getBoolean(_matcher.group(3)));
                                } else {
                                    //throw Invalid KEMP format throw 
                                }
                                break;
                            case Message2:
                                _matcher = Message2.matcher(messageFromClientOrServer);
                                if (_matcher.find()) {
                                    Set<String> extensions = new HashSet<String>();
                                    StringTokenizer tokenizer = new StringTokenizer(_matcher.group(4), " ,");
                                    while (tokenizer.hasMoreTokens()) {
                                        extensions.add(tokenizer.nextToken());
                                    }
                                    decodedMessageObj = new TextMessage2(_matcher.group(6), extensions, messageFromClientOrServer, _matcher.group(1), _matcher.group(2), _matcher.group(3));
                                } else {
                                    //throw Invalid KEMP format throw 
                                }
                                break;
                            case Custom:
                                _matcher = Custom.matcher(messageFromClientOrServer);
                                if (_matcher.find()) {
                                    Set<String> extensions = new HashSet<String>();
                                    StringTokenizer tokenizer = new StringTokenizer(_matcher.group(4), " ,");
                                    while (tokenizer.hasMoreTokens()) {
                                        extensions.add(tokenizer.nextToken());
                                    }
                                    decodedMessageObj = new CustomMessage(_matcher.group(6), extensions, messageFromClientOrServer, _matcher.group(1), _matcher.group(2));
                                } else {
                                    //throw Invalid KEMP format throw 
                                }
                                break;
                        }
                    } catch (IllegalArgumentException notPeerMessageException) {
                        try {
                            PrijosCodec.InterServer_Message current_Message = PrijosCodec.InterServer_Message.valueOf(command.toString());
                            switch (current_Message) {
                                case ServerRegister:
                                    _matcher = ServerRegister.matcher(messageFromClientOrServer);
                                    if (_matcher.find()) {
                                        decodedMessageObj = new ServerRegisterMessage(_matcher.group(1));
                                    } else {
                                        //throw Invalid KEMP format throw 
                                    }
                                    break;
                                case ServerContactList:
                                    _matcher = ServerContactList.matcher(messageFromClientOrServer);
                                    if (_matcher.find()) {
//                                        System.out.println(_matcher.group(3));
                                        ContactAction action = new ContactAction("Add".contentEquals(_matcher.group(2)) ? ContactAction.Action.ADD : ContactAction.Action.REMOVE, _matcher.group(3));
                                        decodedMessageObj = new ServerContactList(_matcher.group(1), action);
                                    } else {
                                        _matcher = ServerContactList2.matcher(messageFromClientOrServer);
                                        if (_matcher.find()) {
                                            ContactAction action = new ContactAction("Add".contentEquals(_matcher.group(2)) ? ContactAction.Action.ADD : ContactAction.Action.REMOVE, _matcher.group(3), _matcher.group(4));
                                            decodedMessageObj = new ServerContactList(_matcher.group(1), action);
                                        } else {
                                            //throw Invalid KEMP format throw 
                                        }
                                    }
                                    break;
                                case ServerMessage:
                                    _matcher = ServerMessage.matcher(messageFromClientOrServer);
                                    if (_matcher.find()) {
                                        Set<String> extensions = new HashSet<String>();
                                        StringTokenizer tokenizer = new StringTokenizer(_matcher.group(4), " ,");
                                        while (tokenizer.hasMoreTokens()) {
                                            extensions.add(tokenizer.nextToken());
                                        }
                                        decodedMessageObj = new ServerIM(_matcher.group(1), new TextMessage(_matcher.group(7), extensions, messageFromClientOrServer, _matcher.group(2), _matcher.group(3), Boolean.getBoolean(_matcher.group(4))));
                                    } else {
                                        _matcher = ServerMessage2.matcher(messageFromClientOrServer);
                                        if (_matcher.find()) {
                                            Set<String> extensions = new HashSet<String>();
                                            StringTokenizer tokenizer = new StringTokenizer(_matcher.group(4), " ,");
                                            while (tokenizer.hasMoreTokens()) {
                                                extensions.add(tokenizer.nextToken());
                                            }
                                            if ("Message2".contentEquals(_matcher.group(2))) {
                                                decodedMessageObj = new ServerIM(_matcher.group(1), new TextMessage2(_matcher.group(7), extensions, messageFromClientOrServer, _matcher.group(2), _matcher.group(3), _matcher.group(4)));
                                            } else if ("Custom".contentEquals(_matcher.group(2))) {
                                            }
                                        } else {
                                            //throw Invalid KEMP format throw 
                                        }
                                    }
                                    break;
                                case ServerStatusList:
                                    _matcher = ServerStatusList.matcher(messageFromClientOrServer);
                                    if (_matcher.find()) {
                                        _matcher = ServerStatusList.matcher(messageFromClientOrServer);
                                        if (_matcher.find()) {
                                            ServerStatusListMessage ssl = new ServerStatusListMessage(_matcher.group(1));
                                            int lastMatched = 0, totalLength = _matcher.end();
                                            _matcher = ExtensionStatus.matcher(messageFromClientOrServer);
                                            while (lastMatched != (totalLength - 1)) {
                                                if (_matcher.find(lastMatched)) {
                                                    lastMatched = _matcher.end();
                                                    ssl.addStatus(_matcher.group(1), _matcher.group(2), this.dateFormat.parse(_matcher.group(3)));
                                                }
                                            }
                                            decodedMessageObj = ssl;
                                        }
                                    } else {
                                        //throw Invalid KEMP format throw 
                                    }
                                    break;
                                case ServerSetStatus:
                                    _matcher = ServerSetStatus.matcher(messageFromClientOrServer);
                                    if (_matcher.find()) {
                                        decodedMessageObj = new ServerSetStatus(_matcher.group(1), _matcher.group(2), _matcher.group(3), this.dateFormat.parse(_matcher.group(5)), Boolean.getBoolean(_matcher.group(6)));
                                    } else {
                                        //throw Invalid KEMP format throw 
                                    }
                                    break;
                                default:
                                    throw new AssertionError(current_Message.name());
                            }
                        } catch (IllegalArgumentException notInterServer_MessageException) {
                            System.out.println(" Command not found!");
                        }
                    }
                }
            } else {
                //Unknown Message format
                System.out.println("Unknown Message format");
            }
        } catch (ParseException | AssertionError e) {
            //parsing exception
            // Invalid kemp format exception
            System.out.println("Error while decoding KEMP" + e);

        }
        int position = messageFromClientOrServer.length();
        DecodeResult objDecodeResult = new DecodeResult(decodedMessageObj, position);
        return objDecodeResult;
    }

    private CharSequence property(CharSequence line) {
        int length = line.length();
        CharSequence command = null;
        for (int i = 0; i < length; i++) {
            if ((line.charAt(i) != ':') || (line.charAt(i + 1) != ' ')) {
                continue;
            }
            command = line.subSequence(i + 2, length - 1);
            //  System.out.println("" + command);
            return command;
        }
        return command;
    }

    @Override
    public void encode(Message paramMessage, Appendable original) {
        Appendable output = original;

        try {
            PrijosCodec.Peer_Message current_Message = PrijosCodec.Peer_Message.valueOf(paramMessage.getCommand());
            switch (current_Message) {

                case Message:

                    try {
                        output.append(((TextMessage) paramMessage).getInput());
                    } catch (IOException ex) {
                        Logger.getLogger(PrijosCodec.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case Message2:
                    try {
                        output.append(((TextMessage2) paramMessage).getInput());
                    } catch (IOException ex) {
                        Logger.getLogger(PrijosCodec.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case Custom:
                    try {
                        output.append(((CustomMessage) paramMessage).getInput());
                    } catch (IOException ex) {
                        Logger.getLogger(PrijosCodec.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
            }
        } catch (IllegalArgumentException notPeer_MessageException) {
            try {
                appendProperty("Command", paramMessage.getCommand(), output);
                PrijosCodec.Server_Message current_Message = PrijosCodec.Server_Message.valueOf(paramMessage.getCommand());
                switch (current_Message) {

                    case ContactList:
                        ContactListMessage clm = (ContactListMessage) paramMessage;
                        Collection<ContactAction> actions = clm.getActions();
                        for (ContactAction action : actions) {
                            String name = action.getAction().equals(ContactAction.Action.ADD) ? "Add-Contact" : "Remove-Contact";
                            appendProperty(name, action.getExtension(), output);
                        }
                        break;
                    case StatusList:
                        StatusListMessage slm = (StatusListMessage) paramMessage;
                        for (ExtensionStatus es : slm.getStatuses()) {
                            appendProperty("ExtensionStatus", new StringBuilder().append(es.getExtension()).append(" ").append(es.getStatus() == null ? DEFAULT_STATUS : new StringBuilder().append(es.getStatus()).append(" ").append(this.dateFormat.format(es.getSince())).toString()).toString(), output);
                        }
                        break;
                    case ScheduledStatusList:
                        ScheduledStatusListMessage sslm = (ScheduledStatusListMessage) paramMessage;
                        for (ScheduledStatus ss : sslm.getStatuses()) {
                            appendProperty("ScheduledStatus", new StringBuilder().append(ss.getName()).append(" ").append(ss.getId()).append(" ").append(this.dateFormat.format(ss.getStart())).append(" ").append(this.dateFormat.format(ss.getEnd())).toString(), output);
                        }
                        break;
//                  case Error:
//                        Considered below
//                        break;
                    case ContactList2:
                        ContactList2Message clm2 = (ContactList2Message) paramMessage;
                        Collection<ContactAction> actions2 = clm2.getActions();
                        for (ContactAction action : actions2) {
                            String name = (action.getAction().equals(ContactAction.Action.ADD) ? "Add-Contact" : "Remove-Contact");
                            appendProperty(name, action.getExtension() + " " + action.getProduct(), output);
                        }
                        break;
                    default:
                        throw new AssertionError();
                }

            } catch (IllegalArgumentException notServer_MessageException) {
                try {
                    appendProperty("Command", paramMessage.getCommand(), output);
                    PrijosCodec.InterServer_Message current_Message = PrijosCodec.InterServer_Message.valueOf(paramMessage.getCommand());
                    switch (current_Message) {
                        case ServerRegister:
                            appendProperty("ServerName", ((ServerRegisterMessage) paramMessage).getServerName(), output);
                            break;
                        case ServerContactList:
                            ServerContactList scl = (ServerContactList) paramMessage;
                            appendProperty("Group", scl.getGroup(), output);
                            ContactAction action = scl.getActions();
                            String name = (action.getAction().equals(ContactAction.Action.ADD) ? "Add-Contact" : "Remove-Contact");
                            appendProperty(name, action.getExtension() + action.getProduct() != null ? " " + action.getProduct() :"", output);
                            break;
                        case ServerMessage:
                            ServerIM message = (ServerIM) paramMessage;
                            appendProperty("Group", message.getGroup(), output);
                            try {
                                output.append(message.getPeerMessage().getInput());
                            } catch (IOException ex) {
                                Logger.getLogger(PrijosCodec.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            break;
                        case ServerStatusList:
                            ServerStatusListMessage sslm = (ServerStatusListMessage) paramMessage;
                            appendProperty("Group", sslm.getGroup(), output);
                            for (ExtensionStatus es : sslm.getStatuses()) {
                                appendProperty("ExtensionStatus", new StringBuilder().append(es.getExtension()).append(" ").append(es.getStatus() == null ? DEFAULT_STATUS : new StringBuilder().append(es.getStatus()).append(" ").append(this.dateFormat.format(es.getSince())).toString()).toString(), output);
                            }
                            break;
                        case ServerSetStatus:
                            ServerSetStatus sssm = (ServerSetStatus) paramMessage;
                            appendProperty("Group", sssm.getGroup(), output);
                            appendProperty("ExtensionStatus", sssm.getExtension() + " " + sssm.getStatus().getName() + " " + this.dateFormat.format(sssm.getStatus().getStart()), output);
                            appendProperty("Override", Boolean.toString(sssm.getStatus().isOverride()), output);
                            break;
                        default:
                            throw new AssertionError();
                    }

                } catch (IllegalArgumentException notInterServer_MessageException) {
                    try {
                        appendProperty("Command", paramMessage.getCommand(), output);
                        PrijosCodec.Error_Message current_Message = PrijosCodec.Error_Message.valueOf(paramMessage.getCommand());
                        switch (current_Message) {
                            case INTERNAL_ERROR:
                                InternalErrorMessage em = (InternalErrorMessage) paramMessage;
                                appendProperty("Number", Integer.toString(em.getNumber()), output);
                                appendProperty("Description", em.getKey(), output);
                                appendProperty("AlertUser", Boolean.toString(em.isAlert()), output);
                                break;
                            case OVERRIDE:
                                OverrideMessage em1 = (OverrideMessage) paramMessage;
                                appendProperty("Number", Integer.toString(em1.getNumber()), output);
                                appendProperty("Description", em1.getKey(), output);
                                appendProperty("AlertUser", Boolean.toString(em1.isAlert()), output);
                                break;
                            default:
                                throw new AssertionError();
                        }
                    } catch (IllegalArgumentException notError_MessageException) {
                        System.out.println(" Message object not known!" + notError_MessageException);
                    }
                }
            }
        }
        try {
            output.append('\n');
            System.out.println(""+output.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void appendProperty(CharSequence name, CharSequence value, Appendable output) {
        assert (!value.toString().contains("\n"));
        try {
            output.append(name);
            output.append(": ");
            output.append(value);
            output.append('\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * All message types
     */
    public static enum Message_Types {

        KeepAlive_Message,
        Peer_Message,
        Client_Message,
        Server_Message,
        Error_Message,
        InterServer_Message;
    }

    /**
     * KeepAlive messages
     */
    public static enum KeepAlive_Message {

        KeepAlive;
        //From client to server
        //From server to server
        //Used to keep the TCP connection active, should be ignored by the server
    }

    /**
     * All the Server to Server messages
     */
    public static enum InterServer_Message {

        ServerRegister,
        //From server to server
        //Used to register one server with another server.

        ServerContactList,
        //From server to server
        //Used to inform other servers [which then inform connected clients] that a user has registered or deregistered on the server.

        ServerMessage,
        //From server to server
        //Used to relay instant messages between clients connected to separate servers

        ServerStatusList,
        //From server to server
        //Used to inform other servers [which then inform connected clients] that the scheduled status for a connected client has changed.

        ServerSetStatus;
        //From server to server
        //Used to inform other servers [which then inform connected clients] that a connected client has set their ad-hoc status.
    }

    /**
     * All the Peer messages
     */
    public static enum Peer_Message {

        Message,
        //From client to server
        //From server to client
        //Send an instant message to one or more users

        Message2,
        //From client to server
        //From server to client
        //Used by clients using the new command
        //set to send instant messages, which are
        //then relayed through all servers.

        Custom;
        //From client to server
        //From server to client
        //Send an instant message to one or more users
    }

    /**
     * All the Client to Server messages
     */
    public static enum Client_Message {

        Register,
        //From client to server
        //Used to register a connection with user details such as user id and group name.

        Register2,
        //From client to server
        //Used by clients using the new command set to register on the server

        SetStatus,
        //From client to server
        //Set the ad-hoc status for a specified user

        AddScheduledStatus,
        //From client to server
        //Add a scheduled status for a specified user

        ClearStatus,
        //From client to server
        //Clear an ad-hoc or scheduled status for a specified user

        RemoveScheduledStatus,
        //From client to server
        //Clear a scheduled status for a specified user [used only in existing client app]

        ListScheduledStatuses;
        //From client to server
        //Requests all scheduled statuses for the specified user and date range
    }

    /**
     * All the Server to Client messages
     */
    public static enum Server_Message {

        ContactList,
        //From server to client
        //Returns the “online” status of one or more users

        StatusList,
        //From server to client
        //Returns the current presence status of one of more users.

        ScheduledStatusList,
        //From server to client
        //Response to the ListScheduledStatuses command. Lists all scheduled statuses that meet the params given in the command.

        Error,
        //From server to client
        //Used to inform a client that an error has occurred.

        ContactList2;
        //From server to client
        //Used to inform connected clients using
        //the new command set when another user
        //registers or deregisters.
    }

    /**
     * All Server.Error messages
     */
    public static enum Error_Message {

        INTERNAL_ERROR,
        OVERRIDE;
    }
}
