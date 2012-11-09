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
import com.kakapo.unity.message.interserver.ServerRegisterMessage;
import com.kakapo.unity.message.interserver.ServerSetStatus;
import com.kakapo.unity.message.interserver.ServerStatusListMessage;
import com.kakapo.unity.message.kempcodec.KempCodec;
import com.kakapo.unity.message.peer.CustomMessage;
import com.kakapo.unity.message.peer.TextMessage;
import com.kakapo.unity.message.peer.TextMessage2;
import com.kakapo.unity.message.server.StatusListMessage;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author prijo.pauly
 */
public class NewCodec {

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
    private final Pattern ServerSetStatus = Pattern.compile("Command: ServerSetStatus[\n]Group: (.*)[\n]Extension: (.*)[\n]Status: (.*)[\n]Override: (.*)(\n\n)");
    private final Pattern ServerStatusList = Pattern.compile("Command: ServerStatusList[\n]Group: (.*)[\n]ExtensionStatus: (.*)[\n](.*)( )(.*)(\n\n)");
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");

    public NewCodec() {
    }

    public static void main(String[] args) throws ParseException {
        String ToServer = "Command: Register\n"
                + "Group: 123@FRD Communication\n"
                + "Extension: DDD@drd.co.uk_172617\n"
                + "Override: True\n\n";
        NewCodec cod = new NewCodec();
        for (int i = 0; i < 10000000; i++) {
            cod.kempEcoder(cod.decodeKemp(ToServer));
        }
        System.out.println("Completed");
    }

    public Message decodeKemp(CharSequence messageFromClient) throws ParseException {
        Message result = null;
        Matcher _matcher = pattern.matcher(messageFromClient);
        if (_matcher.find()) {
            /*
             if ("Register".contentEquals(command)) {
             _matcher = Register.matcher(messageFromClient);
             if (_matcher.find()) {
             result = new RegisterMessage(_matcher.group(1), _matcher.group(2), Boolean.parseBoolean(_matcher.group(3)));
             }
             } else if ("Register2".contentEquals(command)) {
             _matcher = Register2.matcher(messageFromClient);
             if (_matcher.find()) {
             result = new RegisterMessage2(_matcher.group(1), _matcher.group(2), _matcher.group(3), _matcher.group(4));
             }
             } else if ("SetStatus".contentEquals(command)) {
             _matcher = SetStatus.matcher(messageFromClient);
             if (_matcher.find()) {
             result = new SetStatusMessage(_matcher.group(1), _matcher.group(1), Boolean.parseBoolean(_matcher.group(3)));
             }
             } else if ("AddScheduledStatus".contentEquals(command)) {
             _matcher = AddScheduledStatus.matcher(messageFromClient);
             if (_matcher.find()) {
             result = new AddScheduledStatusMessage(_matcher.group(1), new ScheduledStatus(_matcher.group(2), this.dateFormat.parse(_matcher.group(4)), this.dateFormat.parse(_matcher.group(5)), _matcher.group(3)));
             }
             } else if ("ListScheduledStatuses".contentEquals(command)) {
             _matcher = ListScheduledStatuses.matcher(messageFromClient);
             if (_matcher.find()) {
             result = new ListScheduledStatusesMessage(_matcher.group(1), this.dateFormat.parse(_matcher.group(2)), this.dateFormat.parse(_matcher.group(2)));
             }
             } else if ("ClearStatus".contentEquals(command)) {
             _matcher = ClearStatus.matcher(messageFromClient);
             if (_matcher.find()) {
             result = new ClearStatusMessage(_matcher.group(1));
             }
             } else if ("RemoveScheduledStatus".contentEquals(command)) {
             _matcher = RemoveScheduledStatus.matcher(messageFromClient);
             if (_matcher.find()) {
             result = new RemoveScheduledStatusMessage(_matcher.group(1), _matcher.group(2));
             }
             } else if ("Message".contentEquals(command)) {
             _matcher = Message.matcher(messageFromClient);
             if (_matcher.find()) {
             Set<String> extensions = new HashSet<String>();
             StringTokenizer tokenizer = new StringTokenizer(_matcher.group(4), " ,");
             while (tokenizer.hasMoreTokens()) {
             extensions.add(tokenizer.nextToken());
             }
             result = new TextMessage(_matcher.group(6), extensions, messageFromClient, _matcher.group(1), _matcher.group(2), Boolean.getBoolean(_matcher.group(3)));
             }
             } else if ("Message2".contentEquals(command)) {
             _matcher = Message2.matcher(messageFromClient);
             if (_matcher.find()) {
             Set<String> extensions = new HashSet<String>();
             StringTokenizer tokenizer = new StringTokenizer(_matcher.group(4), " ,");
             while (tokenizer.hasMoreTokens()) {
             extensions.add(tokenizer.nextToken());
             }
             result = new TextMessage2(_matcher.group(6), extensions, messageFromClient, _matcher.group(1), _matcher.group(2), _matcher.group(3));
             }
             } else if ("Custom".contentEquals(command)) {
             _matcher = Custom.matcher(messageFromClient);
             if (_matcher.find()) {
             Set<String> extensions = new HashSet<String>();
             StringTokenizer tokenizer = new StringTokenizer(_matcher.group(4), " ,");
             while (tokenizer.hasMoreTokens()) {
             extensions.add(tokenizer.nextToken());
             }
             result = new CustomMessage(_matcher.group(6), extensions, messageFromClient, _matcher.group(1), _matcher.group(2));
             }
             } else if ("ServerRegister".contentEquals(command)) {
             _matcher = ServerRegister.matcher(messageFromClient);
             if (_matcher.find()) {
             result = new ServerRegisterMessage(_matcher.group(1));
             }
             } else if ("ServerContactList".contentEquals(command)) {
             _matcher = ServerContactList.matcher(messageFromClient);
             if (_matcher.find()) {
             ContactAction action = new ContactAction("Add".contentEquals(_matcher.group(2)) ? ContactAction.Action.ADD : ContactAction.Action.REMOVE, _matcher.group(3));
             result = new ServerContactList(_matcher.group(1), action);
             } else {
             _matcher = ServerContactList2.matcher(messageFromClient);
             if (_matcher.find()) {
             ContactAction action = new ContactAction("Add".contentEquals(_matcher.group(2)) ? ContactAction.Action.ADD : ContactAction.Action.REMOVE, _matcher.group(3), _matcher.group(4));
             result = new ServerContactList(_matcher.group(1), action);
             }
             }
             } else if ("ServerMessage".contentEquals(command)) {
             _matcher = ServerMessage.matcher(messageFromClient);
             if (_matcher.find()) {
             } else {
             _matcher = ServerMessage2.matcher(messageFromClient);
             if (_matcher.find()) {
             //                        System.out.println(_matcher.group(1));
             System.out.println("Message2" + messageFromClient);
             }
             }
             } else if ("ServerSetStatus".contentEquals(command)) {
             _matcher = ServerSetStatus.matcher(messageFromClient);
             if (_matcher.find()) {
             //                    result = new ServerSetStatus(_matcher.group(1), _matcher.group(2), _matcher.group(3), Boolean.getBoolean(_matcher.group(4)));
             }
             } else if ("ServerStatusList".contentEquals(command)) {
             _matcher = ServerStatusList.matcher(messageFromClient);
             if (_matcher.find()) {
             //                    result = new ServerStatusListMessage(_matcher.group(1), new ExtensionStatus(_matcher.group(2), _matcher.group(3), this.dateFormat.parse(_matcher.group(4))));
             }
             }
             */
        }
        return result;

    }

    private CharSequence property(String line) {
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

    public String kempEcoder(Message result) throws ParseException {
        String command = result.getCommand();
        String messageToClient = null;
        if ("Message".contentEquals(command)) {
            TextMessage message = (TextMessage) result;
//            Set<String> extensions = message.getExtensions();
//            StringBuilder builder = new StringBuilder();
//            if (extensions.size() > 1) {
//                for (String extension : extensions) {
//                    builder.append(extension);
//                    builder.append(", ");
//                }
//            } else {
//                builder.append(extensions);
//            }
//            messageToClient = "Command: Message\nId: " + message.getId()
//                    + "\nSender: " + message.getSender()
//                    + "\nShare: " + message.isShare()
//                    + "\nExtensions: " + builder
//                    + "\nLength: " + message.getText().length()
//                    + "\n" + message.getText() + "\n\n";
//            System.out.println(""+message.getSender());
//            System.out.println(""+messageToClient);
            messageToClient = message.getInput().toString();
            System.out.println("" + messageToClient);
        } else if ("StatusList".contentEquals(command)) {
            //  System.out.println("Entered");
            StatusListMessage slm = (StatusListMessage) result;
//            for (ExtensionStatus es : slm.getStatuses()) {
//                //messageToClient = "Command: StatusList\nExtensionStatus: " + es.getExtension() + " " + es.getStatus() + " "+this.dateFormat.format(es.getSince())+"\n\n";
//               // System.out.println("" + messageToClient);
//            }
            // System.out.println("slm = " + slm.toString());
            // System.out.println("Exited");
            messageToClient = slm.toString();
        } else if ("Conctact".contentEquals(command)) {
//            ContactListTest con = (ContactListTest) result;
//            if (con.moreclients == true) {
//                messageToClient = con.command;
//                int k = con.contacts.size();
//                for (int i = 0; i < k && k != 1; i++) {
//                    messageToClient = messageToClient + "\n" + con.contacts.get(i);
//                }
//                messageToClient = messageToClient + "\n\n" + con.Status + "\n\n";
//            }
//        } else if ("ContactBroadcast".contentEquals(command)) {
////            ContactBroadcast con = (ContactBroadcast) result;
//            messageToClient = con.command + "\n" + "Add-Contact: " + con.name + "\n\n" + con.Status + "\n\n";
        } else if ("Register".contentEquals(command)) {
            RegisterMessage message = (RegisterMessage) result;
            messageToClient = message.toString();
        }
        return messageToClient;

    }
}
