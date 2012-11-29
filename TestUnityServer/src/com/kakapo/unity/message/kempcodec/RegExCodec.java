package com.kakapo.unity.message.kempcodec;

import com.kakapo.unity.message.ContactAction;
import com.kakapo.unity.message.ExtensionStatus;
import com.kakapo.unity.message.KeepAliveMessage;
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
import com.kakapo.unity.message.peer.CustomMessage;
import com.kakapo.unity.message.peer.TextMessage;
import com.kakapo.unity.message.peer.TextMessage2;
import com.kakapo.unity.message.server.ContactList2Message;
import com.kakapo.unity.message.server.ContactListMessage;
import com.kakapo.unity.message.server.ScheduledStatusListMessage;
import com.kakapo.unity.message.server.StatusListMessage;
import com.kakapo.unity.message.server.error.ErrorMessage;
import com.kakapo.unity.message.server.error.InternalErrorMessage;
import com.kakapo.unity.message.server.error.Legacy.ExtensionInUseMessage;
import com.kakapo.unity.message.server.error.Legacy.MaxConnectionsMessage;
import com.kakapo.unity.message.server.error.Legacy.ShutDownMessage;
import com.kakapo.unity.message.server.error.OverrideMessage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
public class RegExCodec implements MessageCodec {

    private final Pattern pattern = Pattern.compile("(.*)[\\r]?[\\n]?");
    private final Pattern KeepAlive = Pattern.compile("^Command: KeepAlive(\n\n)$");
    private final Pattern AddContactList = Pattern.compile("^Command: ContactList[\n](Add-Contact: (.*)[\n])+(\n)");
    private final Pattern RemoveContactList = Pattern.compile("^Command: ContactList[\n](Remove-Contact: (.*)[\n])+(\n)");
    private final Pattern ScheduledStatusList = Pattern.compile("^Command: ScheduledStatusList[\n](ScheduledStatus: (.*) (.*) (.*) (.*)[\n])+(\n)");
    private final Pattern ScheduledStatus = Pattern.compile("ScheduledStatus: (.*) (.*) (.*) (.*)[\n]");
    private final Pattern AddContact = Pattern.compile("Add-Contact: (.*)[\n]");
    private final Pattern RemoveContact = Pattern.compile("Remove-Contact: (.*)[\n]");
    private final Pattern AddContactList2 = Pattern.compile("^Command: ContactList2[\n](Add-Contact: (.*) (.*)[\n])+(\n)");
    private final Pattern RemoveContactList2 = Pattern.compile("^Command: ContactList2[\n](Remove-Contact: (.*) (.*)[\n])+(\n)");
    private final Pattern AddContact2 = Pattern.compile("Add-Contact: (.*) (.*)[\n]");
    private final Pattern RemoveContact2 = Pattern.compile("Remove-Contact: (.*) (.*)[\n]");
    private final Pattern Error = Pattern.compile("^Command: Error[\n]Number: (.*)[\n]Description: (.*)[\n]AlertUser: (.*)(\n\n)$");
    private final Pattern Register = Pattern.compile("^Command: Register[\n]Group: (.*)[\n]Extension: (.*)[\n]Override: (.*)(\n\n)$");
    private final Pattern Register2 = Pattern.compile("^Command: Register2[\n]Group: (.*)[\n]LoginID: (.*)[\n]CheckSum: (.*)[\n]ProductName: (.*)(\n\n)$");
    private final Pattern SetStatus = Pattern.compile("^Command: SetStatus[\n]Extension: (.*)[\n]Status: (.*)[\n]Override: (.*)(\n\n)$");
    private final Pattern AddScheduledStatus = Pattern.compile("^Command: AddScheduledStatus[\n]Extension: (.*)[\n]Status: (.*)[\n]Id: (.*)[\n]Start: (.*)[\n]End: (.*)(\n\n)$");
    private final Pattern ListScheduledStatuses = Pattern.compile("^Command: ListScheduledStatuses[\n]Extension: (.*)[\n]Start: (.*)[\n]End: (.*)(\n\n)$");
    private final Pattern ClearStatus = Pattern.compile("^Command: ClearStatus[\n]Extension: (.*)(\n\n)$");
    private final Pattern RemoveScheduledStatus = Pattern.compile("^Command: RemoveScheduledStatus[\n]Extension: (.*)[\n]Id: (.*)(\n\n)$");
    private final Pattern Message = Pattern.compile("^Command: Message[\n]Id: (.*)[\n]Sender: (.*)[\n]Share: (.*)[\n]Extensions: (.*)[\n]Length: (.*)[\n](.*)(\n\n)$");
    private final Pattern Message2 = Pattern.compile("^Command: Message2[\n]Id: (.*)[\n]Sender: (.*)[\n]DateTime: (.*)[\n]Extensions: (.*)[\n]Length: (.*)[\n](.*)(\n\n)$");
    private final Pattern Custom = Pattern.compile("^Command: Custom[\n]Id: (.*)[\n]Sender: (.*)[\n]DateTime: (.*)[\n]Extensions: (.*)[\n]Length: (.*)[\n](.*)(\n\n)$");
    private final Pattern ServerRegister = Pattern.compile("^Command: ServerRegister[\n]ServerName: (.*)(\n\n)$");
    private final Pattern ServerContactList = Pattern.compile("^Command: ServerContactList[\n]Group: (.*)[\n](Add||Remove)-Contact: (.*)(\n\n)$");
    private final Pattern ServerContactList2 = Pattern.compile("^Command: ServerContactList[\n]Group: (.*)[\n](Add||Remove)-Contact: (.*) (.*)(\n\n)$");
    private final Pattern ServerMessage = Pattern.compile("^Command: ServerMessage[\n]Group: (.*)[\n]Command: Message[\n]Id: (.*)[\n]Sender: (.*)[\n]Share: (.*)[\n]Extensions: (.*)[\n]Length: (.*)[\n](.*)(\n\n)$");
    private final Pattern ServerMessage2 = Pattern.compile("^Command: ServerMessage[\n]Group: (.*)[\n]Command: (Message2||Custom)[\n]Id: (.*)[\n]Sender: (.*)[\n]DateTime: (.*)[\n]Extensions: (.*)[\n]Length: (.*)[\n](.*)(\n\n)$");
    private final Pattern ServerSetStatus = Pattern.compile("^Command: ServerSetStatus[\n]Group: (.*)[\n]ExtensionStatus: (.*) (.*) (.*)[\n]Override: (.*)(\n\n)$");
    private final Pattern ServerStatusList = Pattern.compile("^Command: ServerStatusList[\n]Group: (.*)[\n](ExtensionStatus: (.*) none[\n])+(\n)");
    private final Pattern StatusList = Pattern.compile("^Command: StatusList[\n](ExtensionStatus: (.*) none[\n])+(\n)");
    private final Pattern StatusList2 = Pattern.compile("^Command: StatusList[\n](ExtensionStatus: (.*) (.*) (.*)[\n])+(\n)");
    private final Pattern ServerStatusList2 = Pattern.compile("^Command: ServerStatusList[\n]Group: (.*)[\n](ExtensionStatus: (.*) (.*) (.*)[\n])+(\n)");
    private final Pattern ExtensionStatus = Pattern.compile("ExtensionStatus: (.*) none[\n]");
    private final Pattern ExtensionStatus2 = Pattern.compile("ExtensionStatus: (.*) (.*) (.*)[\n]");
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
    private final String DEFAULT_STATUS = "none";
    private final Logger LOG = Logger.getLogger(RegExCodec.class.getName());

    public RegExCodec() {
    }

    private String fromClient() {
        String fromClient = "";
        try {
            StringBuilder obj = new StringBuilder();
            FileInputStream fstream = new FileInputStream("AddScheduledStatus.txt");
            try (DataInputStream in = new DataInputStream(fstream)) {
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;

                //Read File Line By Line
                while ((strLine = br.readLine()) != null) {
                    // Print the content on the console
                    obj = (new StringBuilder().append(strLine));
                    fromClient = fromClient.concat(obj.toString() + "\n");
                }
                br = null;
                in.close();
                fstream.close();
            }
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
                    RegExCodec.Client_Message current_Message = RegExCodec.Client_Message.valueOf(command.toString());
                    //<editor-fold defaultstate="collapsed" desc="Switch Case Client_Message">
                    switch (current_Message) {
                        case Register:
                            _matcher = Register.matcher(messageFromClientOrServer);
                            if (_matcher.find()) {
                                decodedMessageObj = new RegisterMessage(_matcher.group(1), _matcher.group(2), Boolean.parseBoolean(_matcher.group(3)));
                            } else {
                                throw new IllegalStateException("Invalid KEMP format of Register Message");
                            }
                            break;
                        case Register2:
                            _matcher = Register2.matcher(messageFromClientOrServer);
                            if (_matcher.find()) {
                                decodedMessageObj = new RegisterMessage2(_matcher.group(1), _matcher.group(2), _matcher.group(3), _matcher.group(4));
                            } else {
                                throw new IllegalStateException("Invalid KEMP format of Register2 Message");
                            }
                            break;
                        case SetStatus:
                            _matcher = SetStatus.matcher(messageFromClientOrServer);
                            if (_matcher.find()) {
                                Boolean.parseBoolean(_matcher.group(3));
                                decodedMessageObj = new SetStatusMessage(_matcher.group(1), _matcher.group(2), Boolean.parseBoolean(_matcher.group(3)));
                            } else {
                                throw new IllegalStateException("Invalid KEMP format of SetStatus Message");
                            }
                            break;
                        case AddScheduledStatus:
                            _matcher = AddScheduledStatus.matcher(messageFromClientOrServer);
                            if (_matcher.find()) {
                                decodedMessageObj = new AddScheduledStatusMessage(_matcher.group(1), new ScheduledStatus(_matcher.group(2), this.dateFormat.parse(_matcher.group(4)), this.dateFormat.parse(_matcher.group(5)), _matcher.group(3)));
                            } else {
                                throw new IllegalStateException("Invalid KEMP format of AddScheduledStatus Message");
                            }
                            break;
                        case ClearStatus:
                            _matcher = ClearStatus.matcher(messageFromClientOrServer);
                            if (_matcher.find()) {
                                decodedMessageObj = new ClearStatusMessage(_matcher.group(1));
                            } else {
                                throw new IllegalStateException("Invalid KEMP format of ClearStatus Message");
                            }
                            break;
                        case RemoveScheduledStatus:
                            _matcher = RemoveScheduledStatus.matcher(messageFromClientOrServer);
                            if (_matcher.find()) {
                                decodedMessageObj = new RemoveScheduledStatusMessage(_matcher.group(2), _matcher.group(1));
                            } else {
                                throw new IllegalStateException("Invalid KEMP format of RemoveScheduledStatus Message");
                            }
                            break;
                        case ListScheduledStatuses:
                            _matcher = ListScheduledStatuses.matcher(messageFromClientOrServer);
                            if (_matcher.find()) {
                                decodedMessageObj = new ListScheduledStatusesMessage(_matcher.group(1), this.dateFormat.parse(_matcher.group(2)), this.dateFormat.parse(_matcher.group(3)));
                            } else {
                                throw new IllegalStateException("Invalid KEMP format of ListScheduledStatuses Message");
                            }
                            break;
                    }
                    //</editor-fold>
                    current_Message = null;
                } catch (IllegalArgumentException notClientMessageException) {
                    try {
                        RegExCodec.KeepAlive_Message current_Message = RegExCodec.KeepAlive_Message.valueOf(command.toString());
                        //<editor-fold defaultstate="collapsed" desc="Switch Case KeepAlive_Message">
                        switch (current_Message) {
                            case KeepAlive:
                                _matcher = KeepAlive.matcher(messageFromClientOrServer);
                                if (_matcher.find()) {
                                    decodedMessageObj = new KeepAliveMessage();
                                } else {
                                    throw new IllegalStateException("Invalid KEMP format of KeepAlive Message");
                                }
                                break;
                        }
                        //</editor-fold>
                        current_Message = null;
                    } catch (IllegalArgumentException notKeepAliveMessageException) {
                        try {
                            RegExCodec.Peer_Message current_Message = RegExCodec.Peer_Message.valueOf(command.toString());
                            //<editor-fold defaultstate="collapsed" desc="Switch Case Peer_Message">
                            switch (current_Message) {
                                case Message:
                                    _matcher = Message.matcher(messageFromClientOrServer);
                                    if (_matcher.find()) {
                                        Set<String> extensions = new HashSet<>();
                                        StringTokenizer tokenizer = new StringTokenizer(_matcher.group(4), " ,");
                                        while (tokenizer.hasMoreTokens()) {
                                            extensions.add(tokenizer.nextToken());
                                        }
                                        try {
                                            int k = Integer.parseInt(_matcher.group(5));
                                            if (k == _matcher.group(6).length()) {
                                                CharSequence text = _matcher.group(6);
                                                decodedMessageObj = new TextMessage(text, extensions, messageFromClientOrServer, _matcher.group(1), _matcher.group(2), Boolean.getBoolean(_matcher.group(3)));
                                            } else {
                                                throw new IllegalStateException("Message contains invalid data");
                                            }
                                        } catch (NumberFormatException e) {
                                            LOG.log(Level.WARNING, "Can not parse key{0}", e);

                                        }
                                    } else {
                                        throw new IllegalStateException("Invalid KEMP format of Message Message");
                                    }
                                    break;
                                case Message2:
                                    _matcher = Message2.matcher(messageFromClientOrServer);
                                    if (_matcher.find()) {
                                        Set<String> extensions = new HashSet<>();
                                        StringTokenizer tokenizer = new StringTokenizer(_matcher.group(4), " ,");
                                        while (tokenizer.hasMoreTokens()) {
                                            extensions.add(tokenizer.nextToken());
                                        }
                                        try {
                                            int k = Integer.parseInt(_matcher.group(5));
                                            if (k == _matcher.group(6).length()) {
                                                CharSequence text = _matcher.group(6);
                                                decodedMessageObj = new TextMessage2(_matcher.group(6), extensions, messageFromClientOrServer, _matcher.group(1), _matcher.group(2), _matcher.group(3));
                                            } else {
                                                throw new IllegalStateException("Message contains invalid data");
                                            }
                                        } catch (NumberFormatException e) {
                                            LOG.log(Level.WARNING, "Can not parse key {0}", e);
                                        }
                                    } else {
                                        throw new IllegalStateException("Invalid KEMP format of Message2 Message");
                                    }
                                    break;
                                case Custom:
//                                    _matcher = Custom.matcher(messageFromClientOrServer);
//                                    if (_matcher.find(0)) {
//                                        Set<String> extensions = new HashSet<>();
//                                        StringTokenizer tokenizer = new StringTokenizer(_matcher.group(4), " ,");
//                                        while (tokenizer.hasMoreTokens()) {
//                                            extensions.add(tokenizer.nextToken());
//                                        }
//                                        try {
//                                            int k = Integer.parseInt(_matcher.group(5));
//                                            if (k == _matcher.group(6).length()) {
//                                                CharSequence text = _matcher.group(6);
//                                                decodedMessageObj = new CustomMessage(_matcher.group(6), extensions, messageFromClientOrServer, _matcher.group(1), _matcher.group(2));
//                                            } else {
//                                                throw new IllegalStateException("Message contains invalid data");
//                                            }
//                                        } catch (NumberFormatException e) {
//                                            LOG.log(Level.WARNING, "Can not parse key {0}", e);
//                                        }
//                                    } else {
//                                        throw new IllegalStateException("Invalid KEMP format of Custom Message");
//                                    }
                                    break;
                            }
                            //</editor-fold>
                            current_Message = null;
                        } catch (IllegalArgumentException notPeerMessageException) {
                            try {
                                RegExCodec.InterServer_Message current_Message = RegExCodec.InterServer_Message.valueOf(command.toString());
                                //<editor-fold defaultstate="collapsed" desc="Switch Case InterServer_Message">
                                switch (current_Message) {
                                    case ServerRegister:
                                        _matcher = ServerRegister.matcher(messageFromClientOrServer);
                                        if (_matcher.find()) {
                                            decodedMessageObj = new ServerRegisterMessage(_matcher.group(1));
                                        } else {
                                            throw new IllegalStateException("Invalid KEMP format of ServerRegister Message");
                                        }
                                        break;
                                    case ServerContactList:
                                        _matcher = ServerContactList2.matcher(messageFromClientOrServer);
                                        if (_matcher.find()) {
                                            ContactAction action = new ContactAction("Add".contentEquals(_matcher.group(2)) ? ContactAction.Action.ADD : ContactAction.Action.REMOVE, _matcher.group(3), _matcher.group(4));
                                            decodedMessageObj = new ServerContactList(_matcher.group(1), action);
                                        } else {
                                            _matcher = ServerContactList.matcher(messageFromClientOrServer);
                                            if (_matcher.find()) {
                                                Logger.getLogger(this.getClass().toString()).log(Level.FINEST, "Matcher {0}",(_matcher.group(3)));
                                                ContactAction action = new ContactAction("Add".contentEquals(_matcher.group(2)) ? ContactAction.Action.ADD : ContactAction.Action.REMOVE, _matcher.group(3));
                                                decodedMessageObj = new ServerContactList(_matcher.group(1), action);
                                            } else {
                                                throw new IllegalStateException("Invalid KEMP format of ServerContactList Message");
                                            }
                                        }
                                        break;
                                    case ServerMessage:
                                        _matcher = ServerMessage.matcher(messageFromClientOrServer);
                                        if (_matcher.find()) {
                                            Set<String> extensions = new HashSet<>();
                                            StringTokenizer tokenizer = new StringTokenizer(_matcher.group(5), " ,");
                                            while (tokenizer.hasMoreTokens()) {
                                                extensions.add(tokenizer.nextToken());
                                            }
                                            try {
                                                int k = Integer.parseInt(_matcher.group(6));
                                                CharSequence text = _matcher.group(7);
                                                if (k == text.length()) {
                                                    decodedMessageObj = new ServerIM(_matcher.group(1), new TextMessage(text, extensions, null, _matcher.group(2), _matcher.group(3), Boolean.getBoolean(_matcher.group(4))));
                                                } else {
                                                    throw new IllegalStateException("Message contains invalid data");
                                                }
                                            } catch (NumberFormatException e) {
                                                LOG.log(Level.WARNING, "Can not parse key {0}", e);
                                            }
                                        } else {
                                            _matcher = ServerMessage2.matcher(messageFromClientOrServer);
                                            if (_matcher.find()) {
                                                Set<String> extensions = new HashSet<>();
                                                StringTokenizer tokenizer = new StringTokenizer(_matcher.group(6), " ,");
                                                while (tokenizer.hasMoreTokens()) {
                                                    extensions.add(tokenizer.nextToken());
                                                }
                                                try {
                                                    int k = Integer.parseInt(_matcher.group(7));
                                                    CharSequence text = _matcher.group(8);
                                                    if (k == text.length()) {
                                                        switch (_matcher.group(2)) {
                                                            case "Message2":
                                                                decodedMessageObj = new ServerIM(_matcher.group(1), new TextMessage2(_matcher.group(8), extensions, null, _matcher.group(3), _matcher.group(4), _matcher.group(5)));
                                                                break;
                                                            case "Custom":
                                                                decodedMessageObj = new ServerIM(_matcher.group(1), new CustomMessage(_matcher.group(8), extensions, null, _matcher.group(3), _matcher.group(4), _matcher.group(5)));
                                                                break;
                                                        }
                                                    } else {
                                                        throw new IllegalStateException("Message contains invalid data");
                                                    }
                                                } catch (NumberFormatException e) {
                                                    LOG.log(Level.WARNING, "Can not parse key{0}", e);
                                                }
                                            } else {
                                                throw new IllegalStateException("Invalid KEMP format of ServerMessage Message");
                                            }
                                        }
                                        break;
                                    case ServerStatusList:
                                        _matcher = ServerStatusList2.matcher(messageFromClientOrServer);
                                        if (_matcher.find()) {
                                            ServerStatusListMessage ssl = new ServerStatusListMessage(_matcher.group(1));
                                            int lastMatched = 0, totalLength = _matcher.end();
                                            _matcher = ExtensionStatus2.matcher(messageFromClientOrServer);
                                            while (lastMatched != (totalLength - 1)) {
                                                if (_matcher.find(lastMatched)) {
                                                    lastMatched = _matcher.end();
                                                    ssl.addStatus(_matcher.group(1), _matcher.group(2), this.dateFormat.parse(_matcher.group(3)));
                                                }
                                            }
                                            decodedMessageObj = ssl;
                                        } else {
                                            _matcher = ServerStatusList.matcher(messageFromClientOrServer);
                                            if (_matcher.find()) {
                                                ServerStatusListMessage ssl = new ServerStatusListMessage(_matcher.group(1));
                                                int lastMatched = 0, totalLength = _matcher.end();
                                                _matcher = ExtensionStatus.matcher(messageFromClientOrServer);
                                                while (lastMatched != (totalLength - 1)) {
                                                    if (_matcher.find(lastMatched)) {
                                                        lastMatched = _matcher.end();
                                                        ssl.addStatus(_matcher.group(1), null, null);
                                                    }
                                                }
                                                decodedMessageObj = ssl;
                                            } else {
                                                throw new IllegalStateException("Invalid KEMP format of ServerStatusList Message");
                                            }
                                        }
                                        break;
                                    case ServerSetStatus:
                                        _matcher = ServerSetStatus.matcher(messageFromClientOrServer);
                                        if (_matcher.find()) {
                                            decodedMessageObj = new ServerSetStatus(_matcher.group(1), _matcher.group(2), _matcher.group(3), this.dateFormat.parse(_matcher.group(4)), Boolean.getBoolean(_matcher.group(5)));
                                        } else {
                                            throw new IllegalStateException("Invalid KEMP format of ServerSetStatus Message");
                                        }
                                        break;
                                    default:
                                        throw new AssertionError(current_Message.name());
                                }
                                //</editor-fold>
                                current_Message = null;
                            } catch (IllegalArgumentException notInterServer_MessageException) {
                                try {
                                    RegExCodec.Server_Message current_Message = RegExCodec.Server_Message.valueOf(command.toString());
                                    //<editor-fold defaultstate="collapsed" desc="Switch Case Server_Message">
                                    switch (current_Message) {
                                        case ContactList:
                                            _matcher = AddContactList.matcher(messageFromClientOrServer);
                                            if (_matcher.find()) {
                                                Collection<ContactAction> actions = new ArrayList<>();
                                                int lastMatched = 0, totalLength = _matcher.end();
                                                _matcher = AddContact.matcher(messageFromClientOrServer);
                                                while (lastMatched != (totalLength - 1)) {
                                                    if (_matcher.find(lastMatched)) {
                                                        lastMatched = _matcher.end();
                                                        actions.add(new ContactAction(ContactAction.Action.ADD, _matcher.group(1)));
                                                    }
                                                }
                                                decodedMessageObj = new ContactListMessage(actions);
                                            } else {
                                                _matcher = RemoveContactList.matcher(messageFromClientOrServer);
                                                if (_matcher.find()) {
                                                    Collection<ContactAction> actions = new ArrayList<>();
                                                    int lastMatched = 0, totalLength = _matcher.end();
                                                    _matcher = RemoveContact.matcher(messageFromClientOrServer);
                                                    while (lastMatched != (totalLength - 1)) {
                                                        if (_matcher.find(lastMatched)) {
                                                            lastMatched = _matcher.end();
                                                            actions.add(new ContactAction(ContactAction.Action.REMOVE, _matcher.group(1)));
                                                        }
                                                    }
                                                } else {
                                                    throw new IllegalStateException("Invalid KEMP format of ServerSetStatus Message");
                                                }

                                            }
                                            break;
                                        case StatusList:
                                            _matcher = StatusList2.matcher(messageFromClientOrServer);
                                            if (_matcher.find()) {
                                                StatusListMessage sl = new StatusListMessage();
                                                int lastMatched = 0, totalLength = _matcher.end();
                                                _matcher = ExtensionStatus2.matcher(messageFromClientOrServer);
                                                while (lastMatched != (totalLength - 1)) {
                                                    if (_matcher.find(lastMatched)) {
                                                        lastMatched = _matcher.end();
                                                        sl.addStatus(_matcher.group(1), _matcher.group(2), this.dateFormat.parse(_matcher.group(3)));
                                                    }
                                                }
                                                decodedMessageObj = sl;
                                            } else {
                                                _matcher = StatusList.matcher(messageFromClientOrServer);
                                                if (_matcher.find()) {
                                                    StatusListMessage sl = new StatusListMessage();
                                                    int lastMatched = 0, totalLength = _matcher.end();
                                                    _matcher = ExtensionStatus.matcher(messageFromClientOrServer);
                                                    while (lastMatched != (totalLength - 1)) {
                                                        if (_matcher.find(lastMatched)) {
                                                            lastMatched = _matcher.end();
                                                            sl.addStatus(_matcher.group(1), null, null);
                                                        }
                                                    }
                                                    decodedMessageObj = sl;
                                                } else {
                                                    throw new IllegalStateException("Invalid KEMP format of ServerStatusList Message");
                                                }
                                            }
                                            break;
                                        case ScheduledStatusList:
                                            _matcher = ScheduledStatusList.matcher(messageFromClientOrServer);
                                            if (_matcher.find()) {
                                                Collection<ScheduledStatus> statuses = new ArrayList<>();
                                                int lastMatched = 0, totalLength = _matcher.end();
                                                _matcher = ScheduledStatus.matcher(messageFromClientOrServer);
                                                while (lastMatched != (totalLength - 1)) {
                                                    if (_matcher.find(lastMatched)) {
                                                        lastMatched = _matcher.end();
                                                        statuses.add(new ScheduledStatus(_matcher.group(1), this.dateFormat.parse(_matcher.group(3)), this.dateFormat.parse(_matcher.group(4)), _matcher.group(2)));
                                                    }
                                                }
                                                decodedMessageObj = new ScheduledStatusListMessage(statuses);
                                            } else {
                                                throw new IllegalStateException("Invalid KEMP format of ServerStatusList Message");
                                            }
                                            break;
                                        case Error:
                                            _matcher = Error.matcher(messageFromClientOrServer);
                                            if (_matcher.find()) {
                                                try {
                                                    int number = Integer.parseInt(_matcher.group(1));
                                                    switch (number) {
                                                        case 1:
                                                            decodedMessageObj = new InternalErrorMessage();
                                                            break;
                                                        case 2:
//                                                    decodedMessageObj = new MaxConnectionsMessage(readProperty("RedirectHostName", lines).toString());
                                                            break;
                                                        case 3:
                                                            decodedMessageObj = new MaxConnectionsMessage();
                                                            break;
                                                        case 4:
                                                            // No such error message
                                                            break;
                                                        case 5:
                                                            decodedMessageObj = new ExtensionInUseMessage();
                                                            break;
                                                        case 6:
                                                            decodedMessageObj = new OverrideMessage();
                                                            break;
                                                        case 7:
//                                                    decodedMessageObj = new ShutDownMessage(readProperty("RedirectHostName", lines).toString());
                                                            break;
                                                        case 8:
                                                            decodedMessageObj = new ShutDownMessage();
                                                    }
                                                } catch (NumberFormatException e) {
                                                    LOG.log(Level.WARNING, "Can not parse key{0}", e);
                                                }
                                            } else {
                                                throw new IllegalStateException("Invalid KEMP format of ServerSetStatus Message");
                                            }
                                            break;
                                        case ContactList2:
                                            _matcher = AddContactList2.matcher(messageFromClientOrServer);
                                            if (_matcher.find()) {
                                                Collection<ContactAction> actions = new ArrayList<>();
                                                int lastMatched = 0, totalLength = _matcher.end();
                                                _matcher = AddContact2.matcher(messageFromClientOrServer);
                                                while (lastMatched != (totalLength - 1)) {
                                                    if (_matcher.find(lastMatched)) {
                                                        lastMatched = _matcher.end();
                                                        actions.add(new ContactAction(ContactAction.Action.ADD, _matcher.group(1)));
                                                    }
                                                }
                                                decodedMessageObj = new ContactListMessage(actions);
                                            } else {
                                                _matcher = RemoveContactList2.matcher(messageFromClientOrServer);
                                                if (_matcher.find()) {
                                                    Collection<ContactAction> actions = new ArrayList<>();
                                                    int lastMatched = 0, totalLength = _matcher.end();
                                                    _matcher = RemoveContact2.matcher(messageFromClientOrServer);
                                                    while (lastMatched != (totalLength - 1)) {
                                                        if (_matcher.find(lastMatched)) {
                                                            lastMatched = _matcher.end();
                                                            actions.add(new ContactAction(ContactAction.Action.REMOVE, _matcher.group(1)));
                                                        }
                                                    }
                                                } else {
                                                    throw new IllegalStateException("Invalid KEMP format of ServerSetStatus Message");
                                                }

                                            }
                                            break;
                                        default:
                                            throw new AssertionError(current_Message.name());
                                    }
                                    //</editor-fold>
                                    current_Message = null;
                                } catch (IllegalArgumentException notServer_MessageException) {
                                    LOG.log(Level.WARNING, "Unknown Command{0}", notServer_MessageException);
                                }
                            }
                        }
                    }
                }
                command = null;
            } else {
                //Unknown Message format
                LOG.log(Level.WARNING, "Unknown Message format");
            }
            _matcher = null;
        } catch (IllegalStateException e) {
            if (e.toString().contains("Invalid KEMP")) {
                LOG.log(Level.WARNING, "Invalid KEMP format");
            } else {
                LOG.log(Level.WARNING, "Extra lines{0}", e);
            }
        } catch (NullPointerException e) {
            LOG.log(Level.WARNING, "First line of the message does not  follow the KEMP format{0}", e);
        } catch (Exception e) {
            System.err.println("Error while decoding KEMP" + e);
        }
        int position = messageFromClientOrServer.length();
        DecodeResult objDecodeResult = new DecodeResult(decodedMessageObj, position);


        decodedMessageObj = null;
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
            return command;
        }
        return command;
    }

    @Override
    public void encode(Message paramMessage, Appendable original) {
        try {
            RegExCodec.Peer_Message current_Message = RegExCodec.Peer_Message.valueOf(paramMessage.getCommand());
            //<editor-fold defaultstate="collapsed" desc="Switch Case - Peer Message">
            switch (current_Message) {
                case Message:
                    try {
                        CharSequence input = ((TextMessage) paramMessage).getInput();
                        if (input != null) {
                            original.append(input);
                        } else {
                            TextMessage tx = (TextMessage) paramMessage;
                            appendProperty("Command", tx.getCommand(), original);
                            appendProperty("Id", tx.getId(), original);
                            appendProperty("Sender", tx.getSender(), original);
                            appendProperty("Share", Boolean.toString(tx.isShare()), original);
                            writeExtensions(tx.getExtensions(), original);
                            appendFreeText(tx.getText(), original);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(RegExCodec.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case Message2:
                    try {
                        CharSequence input = ((TextMessage2) paramMessage).getInput();
                        if (input != null) {
                            original.append(((TextMessage2) paramMessage).getInput());
                        } else {
                            TextMessage2 tx = (TextMessage2) paramMessage;
                            appendProperty("Command", tx.getCommand(), original);
                            appendProperty("Id", tx.getId(), original);
                            appendProperty("Sender", tx.getSender(), original);
                            appendProperty("DateTime", tx.getDateTime(), original);
                            writeExtensions(tx.getExtensions(), original);
                            appendFreeText(tx.getText(), original);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(RegExCodec.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case Custom:
                    try {
                        original.append(((CustomMessage) paramMessage).getInput());
                    } catch (IOException ex) {
                        Logger.getLogger(RegExCodec.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
            }
            //</editor-fold>
            current_Message = null;
        } catch (IllegalArgumentException notPeer_MessageException) {
            try {
                appendProperty("Command", paramMessage.getCommand(), original);
                RegExCodec.KeepAlive_Message current_Message = RegExCodec.KeepAlive_Message.valueOf(paramMessage.getCommand());
                //<editor-fold defaultstate="collapsed" desc="Switch Case - KeepAlive Message">
                switch (current_Message) {
                    case KeepAlive:
                        //                        Only Command to append
                        break;
                }
                //</editor-fold>
                current_Message = null;
            } catch (IllegalArgumentException notKeepAlive_MessageException) {
                try {
                    RegExCodec.Server_Message current_Message = RegExCodec.Server_Message.valueOf(paramMessage.getCommand());
                    //<editor-fold defaultstate="collapsed" desc="Switch Case - Server Message">
                    switch (current_Message) {

                        case ContactList:
                            ContactListMessage clm = (ContactListMessage) paramMessage;
                            Collection<ContactAction> actions = clm.getActions();
                            for (ContactAction action : actions) {
                                String name = action.getAction().equals(ContactAction.Action.ADD) ? "Add-Contact" : "Remove-Contact";
                                appendProperty(name, action.getExtension(), original);
                            }
                            break;
                        case StatusList:
                            StatusListMessage slm = (StatusListMessage) paramMessage;
                            for (ExtensionStatus es : slm.getStatuses()) {
                                appendProperty("ExtensionStatus", new StringBuilder().append(es.getExtension()).append(" ").append(es.getStatus() == null ? DEFAULT_STATUS : new StringBuilder().append(es.getStatus()).append(" ").append(this.dateFormat.format(es.getSince())).toString()).toString(), original);
                            }
                            break;
                        case ScheduledStatusList:
                            ScheduledStatusListMessage sslm = (ScheduledStatusListMessage) paramMessage;
                            for (ScheduledStatus ss : sslm.getStatuses()) {
                                appendProperty("ScheduledStatus", new StringBuilder().append(ss.getName()).append(" ").append(ss.getId()).append(" ").append(this.dateFormat.format(ss.getStart())).append(" ").append(this.dateFormat.format(ss.getEnd())).toString(), original);
                            }
                            break;
                        case Error:
                            ErrorMessage em = (ErrorMessage) paramMessage;
                            appendProperty("Number", Integer.toString(em.getNumber()), original);
                            appendProperty("Description", em.getKey(), original);
                            appendProperty("AlertUser", Boolean.toString(em.isAlert()), original);
                            break;
                        case ContactList2:
                            ContactList2Message clm2 = (ContactList2Message) paramMessage;
                            Collection<ContactAction> actions2 = clm2.getActions();
                            for (ContactAction action : actions2) {
                                String name = (action.getAction().equals(ContactAction.Action.ADD) ? "Add-Contact" : "Remove-Contact");
                                appendProperty(name, new StringBuilder().append(action.getExtension()).append(" ").append(action.getProduct()), original);
                            }
                            break;
                    }
                    //</editor-fold>
                    current_Message = null;
                } catch (IllegalArgumentException notServer_MessageException) {
                    try {
                        RegExCodec.InterServer_Message current_Message = RegExCodec.InterServer_Message.valueOf(paramMessage.getCommand());
                        //<editor-fold defaultstate="collapsed" desc="Switch Case - InterServer Message">
                        switch (current_Message) {
                            case ServerRegister:
                                appendProperty("ServerName", ((ServerRegisterMessage) paramMessage).getServerName(), original);
                                break;
                            case ServerContactList:
                                ServerContactList scl = (ServerContactList) paramMessage;
                                appendProperty("Group", scl.getGroup(), original);
                                ContactAction action = scl.getActions();
                                String name = (action.getAction().equals(ContactAction.Action.ADD) ? "Add-Contact" : "Remove-Contact");
                                appendProperty(name, new StringBuilder().append(action.getExtension()).append((!action.getProduct().contentEquals("none") ? " " + action.getProduct() : "")), original);
                                break;
                            case ServerMessage:
                                ServerIM message = (ServerIM) paramMessage;
                                appendProperty("Group", message.getGroup(), original);
                                try {
                                    CharSequence input = message.getPeerMessage().getInput();
                                    if (input != null) {
                                        original.append(input.subSequence(0, (input.length() - 1)));
                                    } else {
                                        CharSequence command = message.getPeerMessage().getCommand();
                                        if ("Message".contentEquals(command)) {
                                            TextMessage tx = (TextMessage) message.getPeerMessage();
                                            appendProperty("Command", command, original);
                                            appendProperty("Id", tx.getId(), original);
                                            appendProperty("Sender", tx.getSender(), original);
                                            appendProperty("Share", Boolean.toString(tx.isShare()), original);
                                            writeExtensions(tx.getExtensions(), original);
                                            appendFreeText(tx.getText(), original);
                                        } else if ("Message2".contentEquals(command)) {
                                            TextMessage2 tx = (TextMessage2) message.getPeerMessage();
                                            appendProperty("Command", command, original);
                                            appendProperty("Id", tx.getId(), original);
                                            appendProperty("Sender", tx.getSender(), original);
                                            appendProperty("DateTime", tx.getDateTime(), original);
                                            writeExtensions(tx.getExtensions(), original);
                                            appendFreeText(tx.getText(), original);
                                        } else if ("Custom".contentEquals(command)) {
                                            TextMessage2 tx = (TextMessage2) message.getPeerMessage();
                                            appendProperty("Command", command, original);
                                            appendProperty("Id", tx.getId(), original);
                                            appendProperty("Sender", tx.getSender(), original);
                                            appendProperty("DateTime", tx.getDateTime(), original);
                                            writeExtensions(tx.getExtensions(), original);
                                            appendFreeText(tx.getText(), original);
                                        }
                                    }
                                } catch (IOException ex) {
                                    Logger.getLogger(RegExCodec.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                break;
                            case ServerStatusList:
                                ServerStatusListMessage sslm = (ServerStatusListMessage) paramMessage;
                                appendProperty("Group", sslm.getGroup(), original);
                                for (ExtensionStatus es : sslm.getStatuses()) {
                                    appendProperty("ExtensionStatus", new StringBuilder().append(es.getExtension()).append(" ").append(es.getStatus() == null ? DEFAULT_STATUS : new StringBuilder().append(es.getStatus()).append(" ").append(this.dateFormat.format(es.getSince())).toString()).toString(), original);
                                }
                                break;
                            case ServerSetStatus:
                                ServerSetStatus sssm = (ServerSetStatus) paramMessage;
                                appendProperty("Group", sssm.getGroup(), original);
                                appendProperty("ExtensionStatus", new StringBuilder().append(sssm.getExtension()).append(" ").append(sssm.getStatus().getName()).append(" ").append(this.dateFormat.format(sssm.getStatus().getStart())), original);
                                appendProperty("Override", Boolean.toString(sssm.getStatus().isOverride()), original);
                                break;

                        }
                        //</editor-fold>
                        current_Message = null;
                    } catch (IllegalArgumentException notInterServer_MessageException) {
                        try {
                            RegExCodec.Client_Message current_Message = RegExCodec.Client_Message.valueOf(paramMessage.getCommand());
                            //<editor-fold defaultstate="collapsed" desc="Switch Case - Client Message">
                            switch (current_Message) {
                                case Register:
                                    RegisterMessage rm = (RegisterMessage) paramMessage;
                                    appendProperty("Group", rm.getGroup(), original);
                                    appendProperty("Extension", rm.getExtension(), original);
                                    appendProperty("Override", Boolean.toString(rm.isOverride()), original);
                                    break;
                                case Register2:
                                    RegisterMessage2 rm2 = (RegisterMessage2) paramMessage;
                                    appendProperty("Group", rm2.getGroup(), original);
                                    appendProperty("LoginID", rm2.getLoginID(), original);
                                    appendProperty("CheckSum", rm2.getCheckSum(), original);
                                    appendProperty("ProductName", rm2.getProductName(), original);
                                    break;
                                case SetStatus:
                                    SetStatusMessage ssm = (SetStatusMessage) paramMessage;
                                    appendProperty("Extension", ssm.getExtension(), original);
                                    appendProperty("Status", ssm.getStatus().getName(), original);
                                    appendProperty("Override", Boolean.toString(ssm.getStatus().isOverride()), original);
                                    break;
                                case AddScheduledStatus:
                                    AddScheduledStatusMessage assm = (AddScheduledStatusMessage) paramMessage;
                                    appendProperty("Extension", assm.getExtension(), original);
                                    appendProperty("Status", assm.getScheduledStatus().getName(), original);
                                    appendProperty("Id", assm.getScheduledStatus().getId(), original);
                                    appendProperty("Start", this.dateFormat.format(assm.getScheduledStatus().getStart()), original);
                                    appendProperty("End", this.dateFormat.format(assm.getScheduledStatus().getEnd()), original);
                                    break;
                                case ClearStatus:
                                    appendProperty("Extension", ((ClearStatusMessage) paramMessage).getExtension(), original);
                                    break;
                                case RemoveScheduledStatus:
                                    RemoveScheduledStatusMessage rssm = (RemoveScheduledStatusMessage) paramMessage;
                                    appendProperty("Extension", rssm.getExtension(), original);
                                    appendProperty("Extension", rssm.getId(), original);
                                    break;
                                case ListScheduledStatuses:
                                    ListScheduledStatusesMessage lssm = (ListScheduledStatusesMessage) paramMessage;
                                    appendProperty("Extension", lssm.getExtension(), original);
                                    appendProperty("Start", this.dateFormat.format(lssm.getStart()), original);
                                    appendProperty("End", this.dateFormat.format(lssm.getEnd()), original);
                                    break;
                                default:
                                    throw new AssertionError(current_Message.name());
                            }
                            //</editor-fold>
                            current_Message = null;
                        } catch (IllegalArgumentException notClient_MessageException) {
                            LOG.log(Level.WARNING, " Message object not known!{0}", notClient_MessageException);
                        }
                    }
                }
            }
        }
        try {
            original.append('\n');
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

    private void writeExtensions(Set<String> extensions, Appendable output) {
        try {
            StringBuilder builder = new StringBuilder();
            for (String extension : extensions) {
                builder.append(extension);
                builder.append(",");
                extension = null;
            }
            appendProperty("Extensions", builder.deleteCharAt(builder.lastIndexOf(",")).toString().trim(), output);
            builder = null;
            extensions = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void appendFreeText(CharSequence text, Appendable output) {
        try {
            appendProperty("Length", Integer.toString(text.length()), output);
            output.append(text);
            output.append('\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            text = null;
            output = null;
        }
    }

    /**
     * All message types
     */
    public enum Message_Types {

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
    public enum KeepAlive_Message {

        KeepAlive;
        //From client to server
        //From server to server
        //Used to keep the TCP connection active, should be ignored by the server
    }

    /**
     * All the Server to Server messages
     */
    public enum InterServer_Message {

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
    public enum Peer_Message {

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
    public enum Client_Message {

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
    public enum Server_Message {

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
    public enum Error_Message {

        INTERNAL_ERROR,
        OVERRIDE;
    }
}
