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
import com.kakapo.unity.message.client.RemoveExtensionMessage;
import com.kakapo.unity.message.client.RemoveScheduledStatusMessage;
import com.kakapo.unity.message.client.SetStatusMessage;
import com.kakapo.unity.message.interserver.ServerContactList;
import com.kakapo.unity.message.interserver.ServerIM;
import com.kakapo.unity.message.interserver.ServerRegisterMessage;
import com.kakapo.unity.message.interserver.ServerSetStatus;
import com.kakapo.unity.message.interserver.ServerStatusListMessage;
import com.kakapo.unity.message.kempcodec.legacy.*;
import com.kakapo.unity.message.peer.CustomMessage;
import com.kakapo.unity.message.peer.TextMessage;
import com.kakapo.unity.message.peer.TextMessage2;
import com.kakapo.unity.message.server.ContactList2Message;
import com.kakapo.unity.message.server.ContactListMessage;
import com.kakapo.unity.message.server.ScheduledStatusListMessage;
import com.kakapo.unity.message.server.StatusListMessage;
import com.kakapo.unity.message.server.error.ErrorMessage;
import com.kakapo.unity.message.server.error.InternalErrorMessage;
import com.kakapo.unity.message.server.error.Legacy.RedirectErrorMessage;
import com.kakapo.unity.message.server.error.OverrideMessage;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class LegacyPatternCodec
        implements MessageCodec {

    private final Logger logger = Logger.getLogger(SimpleMessageCodec.class.getName());
    private final String DEFAULT_STATUS = "none";
    private final ResourceBundle _messages;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
    private final Pattern BLANK_LINE_PATTERN = Pattern.compile("(?:\\r?\\n){2,}", 32);

    public LegacyPatternCodec() {
        this._messages = ResourceBundle.getBundle("messages");
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private CharSequence readProperty(String name, LineReader lines) {
        Property property = new Property(lines.next());
        if ((property.name == null) || (!name.contentEquals(property.name))) {
            throw new IllegalArgumentException(new StringBuilder().append("Expected ").append(name).append(" but read ").append(property.name).toString());
        }
        return property.value;
    }

    @Override
    public synchronized MessageCodec.DecodeResult decode(CharSequence encoded) {

        LineReader lines = new LineReader(encoded);
        boolean complete = false;
        Message result = null;
        try {
            CharSequence commandLine = lines.next();
            Property property = new Property(commandLine);
            if ((property.name == null) || (!"Command".contentEquals(property.name))) {
                throw new IllegalArgumentException(new StringBuilder().append("No command found for line ").append(commandLine).toString());
            }
            if ("KeepAlive".contentEquals(property.value)) {
                result = new KeepAliveMessage();
                complete = checkMessageEnd(lines);
            } else if ("Register".contentEquals(property.value)) {
                CharSequence group = readProperty("Group", lines);
                CharSequence extension = readProperty("Extension", lines);
                boolean override = Boolean.parseBoolean(readProperty("Override", lines).toString());
                result = new RegisterMessage(group, extension, override);
                complete = checkMessageEnd(lines);
            } else if ("Register2".contentEquals(property.value)) {
                CharSequence group = readProperty("Group", lines);
                CharSequence loginId = readProperty("LoginID", lines);
                CharSequence checkSum = readProperty("CheckSum", lines);
                CharSequence productName = readProperty("ProductName", lines);
                result = new RegisterMessage2(group, loginId, checkSum, productName);
                complete = checkMessageEnd(lines);
            } else if ("Message".contentEquals(property.value)) {
                CharSequence id = readProperty("Id", lines);
                String sender = readProperty("Sender", lines).toString();
                boolean share = Boolean.parseBoolean(readProperty("Share", lines).toString());
                Set<String> extensions = readExtensions(lines);
                int length = Integer.parseInt(readProperty("Length", lines).toString());
                CharSequence text = readFreeText(lines, encoded, length);
                complete = checkMessageEnd(lines);
                result = new TextMessage(text, extensions, encoded.subSequence(0, lines.position()), id, sender, share, length);
            } else if ("Message2".contentEquals(property.value)) {
                CharSequence id = readProperty("Id", lines);
                CharSequence sender = readProperty("Sender", lines);
                CharSequence dateTime = readProperty("DateTime", lines);
                Set<String> extensions = readExtensions(lines);
                int length = Integer.parseInt(readProperty("Length", lines).toString());
                CharSequence text = readFreeText(lines, encoded, length);
                complete = checkMessageEnd(lines);
                result = new TextMessage2(text, extensions, encoded.subSequence(0, lines.position()), id, sender, dateTime, length);
            } else if ("Custom".contentEquals(property.value)) {
                CharSequence id = readProperty("Id", lines);
                CharSequence sender = readProperty("Sender", lines);
                CharSequence dateTime = readProperty("DateTime", lines);
                Set<String> extensions = readExtensions(lines);
                int length = Integer.parseInt(readProperty("Length", lines).toString());
                CharSequence text = readFreeText(lines, encoded, length);
                complete = checkMessageEnd(lines);
                result = new CustomMessage(text, extensions, encoded.subSequence(0, lines.position()), id, sender, dateTime, length);
            } else if ("SetStatus".contentEquals(property.value)) {
                String extension = readProperty("Extension", lines).toString();
                String status = readProperty("Status", lines).toString();
                boolean override = Boolean.parseBoolean(readProperty("Override", lines).toString());
                result = new SetStatusMessage(extension, status, override);
                complete = checkMessageEnd(lines);
            } else if ("ClearStatus".contentEquals(property.value)) {
                String extension = readProperty("Extension", lines).toString();
                result = new ClearStatusMessage(extension);
                complete = checkMessageEnd(lines);
            } else if ("AddScheduledStatus".contentEquals(property.value)) {
                String extension = readProperty("Extension", lines).toString();
                String status = readProperty("Status", lines).toString();
                String id = readProperty("Id", lines).toString();
                Date start = this.dateFormat.parse(readProperty("Start", lines).toString());
                Date end = this.dateFormat.parse(readProperty("End", lines).toString());
                result = new AddScheduledStatusMessage(extension, new ScheduledStatus(status, start, end, id));
                complete = checkMessageEnd(lines);
            } else if ("ListScheduledStatuses".contentEquals(property.value)) {
                String extension = readProperty("Extension", lines).toString();
                Date start = this.dateFormat.parse(readProperty("Start", lines).toString());
                Date end = this.dateFormat.parse(readProperty("End", lines).toString());
                result = new ListScheduledStatusesMessage(extension, start, end);
                complete = checkMessageEnd(lines);
            } else if ("RemoveExtension".contentEquals(property.value)) {
                String extension = readProperty("Extension", lines).toString();
                result = new RemoveExtensionMessage(extension);
                complete = checkMessageEnd(lines);
            } else if ("RemoveScheduledStatus".contentEquals(property.value)) {
                String extension = readProperty("Extension", lines).toString();
                String id = readProperty("Id", lines).toString();
                result = new RemoveScheduledStatusMessage(id, extension);
                complete = checkMessageEnd(lines);
            } else if ("ContactList".contentEquals(property.value)) {
                List<ContactAction> actions = new ArrayList<ContactAction>();
                CharSequence line;
                while ((line = lines.next()).length() > 0) {
                    Property actionProperty = new Property(line);
                    String extension = actionProperty.value.toString();
                    if ("Add-Contact".contentEquals(actionProperty.name)) {
                        actions.add(new ContactAction(ContactAction.Action.ADD, extension));
                    } else {
                        actions.add(new ContactAction(ContactAction.Action.REMOVE, extension));
                    }
                }
                result = new ContactListMessage(actions);
                complete = true;
            } else if ("ContactList2".contentEquals(property.value)) {
                List<ContactAction> actions = new ArrayList<ContactAction>();
                CharSequence line;
                while ((line = lines.next()).length() > 0) {
                    Property actionProperty = new Property(line);
                    String[] parts = actionProperty.value.toString().split(" ");
                    if (parts.length == 2) {
                        if ("Add-Contact".contentEquals(actionProperty.name)) {
                            actions.add(new ContactAction(ContactAction.Action.ADD, parts[0], parts[1]));
                        } else {
                            actions.add(new ContactAction(ContactAction.Action.REMOVE, parts[0], parts[1]));
                        }
                    } else {
                        if ("Add-Contact".contentEquals(actionProperty.name)) {
                            actions.add(new ContactAction(ContactAction.Action.ADD, parts[0]));
                        } else {
                            actions.add(new ContactAction(ContactAction.Action.REMOVE, parts[0]));
                        }
                    }
                }
                result = new ContactList2Message(actions);
                complete = true;
            } else if ("StatusList".contentEquals(property.value)) {
                StatusListMessage message = new StatusListMessage();
                CharSequence line;
                while ((line = lines.next()).length() > 0) {
                    Property actionProperty = new Property(line);
                    String[] parts = actionProperty.value.toString().split(" ");
                    if (parts.length == 3) {
                        message.addStatus(parts[0], parts[1], this.dateFormat.parse(parts[2].trim()));
                    } else {
                        message.addStatus(parts[0], null, null);
                    }
                }
                result = message;
                complete = true;
            } else if ("ScheduledStatusList".contentEquals(property.value)) {
                List<ScheduledStatus> actions = new ArrayList<ScheduledStatus>();
                CharSequence line;
                while ((line = lines.next()).length() > 0) {
                    Property actionProperty = new Property(line);
                    String[] parts = actionProperty.value.toString().split(" ");
                    actions.add(new ScheduledStatus(parts[0], this.dateFormat.parse(parts[2]), this.dateFormat.parse(parts[3].trim()), parts[1]));
                }

                result = new ScheduledStatusListMessage(actions);
                complete = true;
            } else if ("Error".contentEquals(property.value)) {
                int number = Integer.parseInt(readProperty("Number", lines).toString());
                switch (number) {
                    case 1:
                        result = new InternalErrorMessage();
                        break;
                    case 2:
                        //  result = new MaxConnectionsMessage(readProperty("RedirectHostName", lines).toString());
                        break;
                    case 3:
                        //    result = new MaxConnectionsMessage();
                        break;
                    case 5:
                        //  result = new ExtensionInUseMessage();
                        break;
                    case 6:
                        result = new OverrideMessage();
                        break;
                    case 7:
//                        result = new ShutDownMessage(readProperty("RedirectHostName", lines).toString());
                        break;
                    case 8:
                    //    result = new ShutDownMessage();
                    case 4:
                }

                while (lines.next().length() > 0);
                complete = true;
            } else if ("ServerRegister".contentEquals(property.value)) {
                CharSequence group = readProperty("ServerName", lines);
                result = new ServerRegisterMessage(group);
                complete = checkMessageEnd(lines);
            } else if ("ServerContactList".contentEquals(property.value)) {
                CharSequence group = readProperty("Group", lines);
                ContactAction contactAction;
                Property actionProperty = new Property(lines.next());
                String[] parts = actionProperty.value.toString().split(" ");
                if (parts.length == 2) {
                    if ("Add-Contact".contentEquals(actionProperty.name)) {
                        contactAction = new ContactAction(ContactAction.Action.ADD, parts[0], parts[1]);
                    } else {
                        contactAction = new ContactAction(ContactAction.Action.REMOVE, parts[0], parts[1]);
                    }
                } else {
                    if ("Add-Contact".contentEquals(actionProperty.name)) {
                        contactAction = new ContactAction(ContactAction.Action.ADD, actionProperty.value.toString());
                    } else {
                        contactAction = new ContactAction(ContactAction.Action.REMOVE, actionProperty.value.toString());
                    }
                }
                lines.next();
                result = new ServerContactList(group, contactAction);
                complete = true;
            } else if ("ServerStatusList".contentEquals(property.value)) {
                CharSequence group = readProperty("Group", lines);
                ServerStatusListMessage message = new ServerStatusListMessage(group.toString());
                CharSequence line;
                while ((line = lines.next()).length() > 0) {
                    Property actionProperty = new Property(line);
                    String[] parts = actionProperty.value.toString().split(" ");
                    if (parts.length == 3) {
                        message.addStatus(parts[0], parts[1], this.dateFormat.parse(parts[2].trim()));
                    } else {
                        message.addStatus(parts[0], null, null);
                    }
                }
                result = message;
                complete = true;
            } else if ("ServerSetStatus".contentEquals(property.value)) {
                CharSequence group = readProperty("Group", lines);
                String ExtensionStatus = readProperty("ExtensionStatus", lines).toString();
                String[] parts = ExtensionStatus.split(" ");
                boolean override = Boolean.parseBoolean(readProperty("Override", lines).toString());
                if (parts.length == 3) {
                    result = new ServerSetStatus(group.toString(), parts[0], parts[1], this.dateFormat.parse(parts[2].trim()), override);
                }
                lines.next();
                complete = true;
            } else if ("ServerMessage".contentEquals(property.value)) {
                CharSequence group = readProperty("Group", lines);
                String command = readProperty("Command", lines).toString();
                switch (command) {
                    case "Message": {
                        CharSequence id = readProperty("Id", lines);
                        CharSequence sender = readProperty("Sender", lines);
                        boolean share = Boolean.parseBoolean(readProperty("Share", lines).toString());
                        Set<String> extensions = readExtensions(lines);
                        int length = Integer.parseInt(readProperty("Length", lines).toString());
                        CharSequence text = readFreeText(lines, encoded, length);
                        complete = checkMessageEnd(lines);
                        result = new ServerIM(group, new TextMessage(text, extensions, null, id, sender, share, length));
                        break;
                    }
                    case "Message2": {
                        CharSequence id = readProperty("Id", lines);
                        CharSequence sender = readProperty("Sender", lines);
                        CharSequence dateTime = readProperty("DateTime", lines);
                        Set<String> extensions = readExtensions(lines);
                        int length = Integer.parseInt(readProperty("Length", lines).toString());
                        CharSequence text = readFreeText(lines, encoded, length);
                        complete = checkMessageEnd(lines);
                        result = new ServerIM(group, new TextMessage2(text, extensions, null, id, sender, dateTime, length));
                        break;
                    }
                    case "Custom": {
                        CharSequence id = readProperty("Id", lines);
                        CharSequence sender = readProperty("Sender", lines);
                        CharSequence dateTime = readProperty("DateTime", lines);
                        Set<String> extensions = readExtensions(lines);
                        int length = Integer.parseInt(readProperty("Length", lines).toString());
                        CharSequence text = readFreeText(lines, encoded, length);
                        complete = checkMessageEnd(lines);
                        result = new ServerIM(group, new CustomMessage(text, extensions, null, id, sender, dateTime, length));
                        break;
                    }
                }
            } else {
                throw new IllegalArgumentException(new StringBuilder().append("Unknown command: ").append(property.value).toString());
            }
        } catch (Exception e) {
            String text = encoded.toString();

            if (BLANK_LINE_PATTERN.matcher(text).find()) {
                throw new IllegalArgumentException(new StringBuilder().append("Problem with message: ").append(text).append(" at ").append(text.substring(lines.position())).toString(), e);
            }

            return null;
        }

        if (complete) {
            return new MessageCodec.DecodeResult(result, lines.position());
        }

        return null;
    }

    private boolean checkMessageEnd(LineReader lines) {
        if (lines.hasNext()) {
            CharSequence line = lines.next();
            if (line.length() > 0) {
                throw new IllegalStateException(new StringBuilder().append("Found extra line at end of message: ").append(line.toString()).toString());
            }
            return true;
        }

        return false;
    }

    private CharSequence readFreeText(LineReader lines, CharSequence input, int length) {

        CharSequence sequence = input.subSequence(lines.position(), lines.position() + length);

        lines.position(lines.position() + length);
        lines.next();
        return sequence;
    }

    private Set<String> readExtensions(LineReader lines) {
        Property property = new Property(lines.next());
        assert ("Extensions".contentEquals(property.name));
        Set<String> extensions = new HashSet<String>();
        StringTokenizer tokenizer = new StringTokenizer(property.value.toString(), " ,");
        while (tokenizer.hasMoreTokens()) {
            extensions.add(tokenizer.nextToken());
        }
        return extensions;
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

    @Override
    public synchronized void encode(Message message, Appendable original) {
        Appendable output = original;

//
//    if (logger.isLoggable(Level.FINE))
//    { 
//      output = new StringBuilder();
//    }   
        appendProperty("Command", message.getCommand(), output);

        if (!(message instanceof KeepAliveMessage)) {
            if ((message instanceof RegisterMessage)) {
                RegisterMessage rm = (RegisterMessage) message;
                appendProperty("Group", rm.getGroup(), output);
                appendProperty("Extension", rm.getExtension(), output);
                appendProperty("Override", Boolean.toString(rm.isOverride()), output);
            } else if ((message instanceof RegisterMessage2)) {
                RegisterMessage2 rm2 = (RegisterMessage2) message;
                appendProperty("Group", rm2.getGroup(), output);
                appendProperty("LoginID", rm2.getLoginID(), output);
                appendProperty("CheckSum", rm2.getCheckSum(), output);
                appendProperty("ProductName", rm2.getProductName(), output);
            } else if ((message instanceof SetStatusMessage)) {
                SetStatusMessage usm = (SetStatusMessage) message;
                appendProperty("Extension", usm.getExtension(), output);
                appendProperty("Status", usm.getStatus().getName(), output);
                appendProperty("Override", Boolean.toString(usm.getStatus().isOverride()), output);
            } else if ((message instanceof ClearStatusMessage)) {
                ClearStatusMessage csm = (ClearStatusMessage) message;
                appendProperty("Extension", csm.getExtension(), output);
            } else if ((message instanceof AddScheduledStatusMessage)) {
                AddScheduledStatusMessage assm = (AddScheduledStatusMessage) message;
                ScheduledStatus ss = assm.getScheduledStatus();
                appendProperty("Extension", assm.getExtension(), output);
                appendProperty("Status", ss.getName(), output);
                appendProperty("Id", ss.getId(), output);
                appendProperty("Start", this.dateFormat.format(ss.getStart()), output);
                appendProperty("End", this.dateFormat.format(ss.getEnd()), output);
            } else if ((message instanceof ListScheduledStatusesMessage)) {
                ListScheduledStatusesMessage lssm = (ListScheduledStatusesMessage) message;
                appendProperty("Extension", lssm.getExtension(), output);
                appendProperty("Start", this.dateFormat.format(lssm.getStart()), output);
                appendProperty("End", this.dateFormat.format(lssm.getEnd()), output);
            } else if ((message instanceof RemoveScheduledStatusMessage)) {
                RemoveScheduledStatusMessage rssm = (RemoveScheduledStatusMessage) message;
                appendProperty("Extension", rssm.getExtension(), output);
                appendProperty("Id", rssm.getId(), output);
            } else if ((message instanceof ContactListMessage)) {
                ContactListMessage clm = (ContactListMessage) message;
                Collection<ContactAction> actions = clm.getActions();
                for (ContactAction action : actions) {
                    String name = action.getAction().equals(ContactAction.Action.ADD) ? "Add-Contact" : "Remove-Contact";
                    appendProperty(name, action.getExtension(), output);
                }
            } else if ((message instanceof ContactList2Message)) {
                ContactList2Message clm2 = (ContactList2Message) message;
                Collection<ContactAction> actions = clm2.getActions();
                for (ContactAction action : actions) {
                    String name = action.getAction().equals(ContactAction.Action.ADD) ? "Add-Contact" : "Remove-Contact";
                    if (action.getProduct() != null) {
                        appendProperty(name, action.getExtension() + (action.getProduct().contentEquals("none") ? "" : " " + action.getProduct()), output);
                    } else {
                        appendProperty(name, action.getExtension(), output);
                    }
                }
            } else if ((message instanceof TextMessage)) {
                TextMessage tm = (TextMessage) message;
                appendProperty("Id", tm.getId(), output);
                appendProperty("Sender", tm.getSender(), output);
                appendProperty("Share", Boolean.toString(tm.isShare()), output);
                writeExtensions(tm.getExtensions(), output);
                appendFreeText(tm.getText(), output, tm.getLength());
            } else if ((message instanceof TextMessage2)) {
                TextMessage2 tm2 = (TextMessage2) message;
                appendProperty("Id", tm2.getId(), output);
                appendProperty("Sender", tm2.getSender(), output);
                appendProperty("DateTime", tm2.getDateTime(), output);
                writeExtensions(tm2.getExtensions(), output);
                appendFreeText(tm2.getText(), output, tm2.getLength());
            } else if ((message instanceof CustomMessage)) {
                CustomMessage cm = (CustomMessage) message;
                appendProperty("Id", cm.getId(), output);
                appendProperty("Sender", cm.getSender(), output);
                appendProperty("DateTime", cm.getDateTime(), output);
                writeExtensions(cm.getExtensions(), output);
                appendFreeText(cm.getText(), output, cm.getLength());
            } else if ((message instanceof ErrorMessage)) {
                ErrorMessage em = (ErrorMessage) message;
                appendProperty("Number", Integer.toString(em.getNumber()), output);
                appendProperty("Description", this._messages.getString(em.getKey()), output);
                appendProperty("AlertUser", Boolean.toString(em.isAlert()), output);
                if ((message instanceof RedirectErrorMessage)) {
                    String redirect = ((RedirectErrorMessage) message).getRedirect();
                    if (redirect != null) {
                        appendProperty("RedirectHostName", redirect, output);
                    }
                }
            } else if ((message instanceof StatusListMessage)) {
                StatusListMessage slm = (StatusListMessage) message;

                for (ExtensionStatus es : slm.getStatuses()) {
                    appendProperty("ExtensionStatus", new StringBuilder().append(es.getExtension()).append(" ").append(es.getStatus() == null ? DEFAULT_STATUS : new StringBuilder().append(es.getStatus()).append(" ").append(this.dateFormat.format(es.getSince())).toString()).toString(), output);
                }

            } else if ((message instanceof ScheduledStatusListMessage)) {
                ScheduledStatusListMessage sslm = (ScheduledStatusListMessage) message;
                for (ScheduledStatus ss : sslm.getStatuses()) {
                    appendProperty("ScheduledStatus", new StringBuilder().append(ss.getName()).append(" ").append(ss.getId()).append(" ").append(this.dateFormat.format(ss.getStart())).append(" ").append(this.dateFormat.format(ss.getEnd())).toString(), output);
                }

            } else if ((message instanceof ServerRegisterMessage)) {
                ServerRegisterMessage srm = (ServerRegisterMessage) message;
                appendProperty("ServerName", srm.getServerName(), output);
            } else if ((message instanceof ServerContactList)) {
                ServerContactList scl = (ServerContactList) message;
                appendProperty("Group", scl.getGroup(), output);
                ContactAction action = scl.getActions();
                String name = action.getAction().equals(ContactAction.Action.ADD) ? "Add-Contact" : "Remove-Contact";
                if (action.getProduct() != null) {
                    appendProperty(name, action.getExtension() + (action.getProduct().contentEquals("none") ? "" : " " + action.getProduct()), output);
                } else {
                    appendProperty(name, action.getExtension(), output);
                }
            } else if ((message instanceof ServerIM)) {
                ServerIM sim = (ServerIM) message;
                appendProperty("Group", sim.getGroup(), output);
                if (((ServerIM) message).getPeerMessage() instanceof TextMessage2) {
                    TextMessage2 tm2 = (TextMessage2) sim.getPeerMessage();
                    appendProperty("Command", tm2.getCommand(), output);
                    appendProperty("Id", tm2.getId(), output);
                    appendProperty("Sender", tm2.getSender(), output);
                    appendProperty("DateTime", tm2.getDateTime(), output);
                    writeExtensions(tm2.getExtensions(), output);
                    appendFreeText(tm2.getText(), output, tm2.getLength());
                } else if (((ServerIM) message).getPeerMessage() instanceof TextMessage) {
                    TextMessage tm = (TextMessage) sim.getPeerMessage();
                    appendProperty("Command", tm.getCommand(), output);
                    appendProperty("Id", tm.getId(), output);
                    appendProperty("Sender", tm.getSender(), output);
                    appendProperty("Share", Boolean.toString(tm.isShare()), output);
                    writeExtensions(tm.getExtensions(), output);
                    appendFreeText(tm.getText(), output, tm.getLength());
                } else if (((ServerIM) message).getPeerMessage() instanceof CustomMessage) {
                    CustomMessage cm = (CustomMessage) sim.getPeerMessage();
                    appendProperty("Command", cm.getCommand(), output);
                    appendProperty("Id", cm.getId(), output);
                    appendProperty("Sender", cm.getSender(), output);
                    appendProperty("DateTime", cm.getDateTime(), output);
                    writeExtensions(cm.getExtensions(), output);
                    appendFreeText(cm.getText(), output, cm.getLength());
                }
            } else if ((message instanceof ServerStatusListMessage)) {
                ServerStatusListMessage sslm = (ServerStatusListMessage) message;
                appendProperty("Group", sslm.getGroup(), output);
                for (ExtensionStatus es : sslm.getStatuses()) {
                    appendProperty("ExtensionStatus", new StringBuilder().append(es.getExtension()).append(" ").append(es.getStatus() == null ? DEFAULT_STATUS : new StringBuilder().append(es.getStatus()).append(" ").append(this.dateFormat.format(es.getSince())).toString()).toString(), output);
                }
            } else if ((message instanceof ServerSetStatus)) {
                ServerSetStatus sss = (ServerSetStatus) message;
                appendProperty("Group", sss.getGroup(), output);
                appendProperty("ExtensionStatus", new StringBuilder().append(sss.getExtension()).append(" ").append(sss.getStatus().getName()).append(" ").append(this.dateFormat.format(sss.getStatus().getStart())), output);
                appendProperty("Override", Boolean.toString(sss.getStatus().isOverride()), output);
            } else {
                throw new RuntimeException(new StringBuilder().append("Do not know how to encode message ").append(message).toString());
            }
        }
        try {
            output.append('\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


//    if (logger.isLoggable(Level.FINE))
//    {
//        System.out.println("Logging .............");
//      logger.fine(output.toString());
//      try
//      {
//        original.append(output.toString());
//      }
//      catch (IOException e)
//      {
//        throw new RuntimeException(e);
//      }
//    }



    }

    synchronized void encodeKemp1(Message message, Appendable original) {

        Appendable output = original;

        if ((message instanceof KeepAliveMessage)) {
            appendProperty("Command", message.getCommand(), output);
        } else if ((message instanceof ContactListMessage)) {
            ContactListMessage clm = (ContactListMessage) message;
            Collection<ContactAction> actions = clm.getActions();
            appendProperty("Command", clm.getCommand(), output);
            for (ContactAction action : actions) {
                String name = action.getAction().equals(ContactAction.Action.ADD) ? "Add-Contact" : "Remove-Contact";
                appendProperty(name, action.getExtension(), output);
            }
        } else if ((message instanceof ContactList2Message)) {
            appendProperty("Command", "ContactList", output);
            ContactList2Message clm2 = (ContactList2Message) message;
            for (ContactAction action : clm2.getActions()) {
                String name = action.getAction().equals(ContactAction.Action.ADD) ? "Add-Contact" : "Remove-Contact";
                appendProperty(name, action.getExtension(), output);
            }
        } else if ((message instanceof TextMessage)) {
            TextMessage tm = (TextMessage) message;
            appendProperty("Command", tm.getCommand(), output);
            appendProperty("Id", tm.getId(), output);
            appendProperty("Sender", tm.getSender(), output);
            appendProperty("Share", Boolean.toString(tm.isShare()), output);
            writeExtensions(tm.getExtensions(), output);
            appendFreeText(tm.getText(), output, tm.getLength());
        } else if ((message instanceof TextMessage2)) {
            TextMessage2 tm2 = (TextMessage2) message;
            appendProperty("Command", "Message", output);
            appendProperty("Id", tm2.getId(), output);
            appendProperty("Sender", tm2.getSender(), output);
            appendProperty("Share", "true", output);
            writeExtensions(tm2.getExtensions(), output);
            appendFreeText(tm2.getText(), output, tm2.getLength());
        } else if ((message instanceof CustomMessage)) {
            CustomMessage cm = (CustomMessage) message;
            appendProperty("Command", cm.getCommand(), output);
            appendProperty("Id", cm.getId(), output);
            appendProperty("Sender", cm.getSender(), output);
            appendProperty("Share", "true", output);
            writeExtensions(cm.getExtensions(), output);
            appendFreeText(cm.getText(), output, cm.getLength());
        } else if ((message instanceof ErrorMessage)) {
            ErrorMessage em = (ErrorMessage) message;
            appendProperty("Command", em.getCommand(), output);
            appendProperty("Number", Integer.toString(em.getNumber()), output);
            appendProperty("Description", this._messages.getString(em.getKey()), output);
            appendProperty("AlertUser", Boolean.toString(em.isAlert()), output);
        } else if ((message instanceof StatusListMessage)) {
            StatusListMessage slm = (StatusListMessage) message;
            appendProperty("Command", slm.getCommand(), output);
            for (ExtensionStatus es : slm.getStatuses()) {
                appendProperty("ExtensionStatus", new StringBuilder().append(es.getExtension()).append(" ").append(es.getStatus() == null ? DEFAULT_STATUS : new StringBuilder().append(es.getStatus()).append(" ").append(this.dateFormat.format(es.getSince())).toString()).toString(), output);
            }
        } else if ((message instanceof ScheduledStatusListMessage)) {
            ScheduledStatusListMessage sslm = (ScheduledStatusListMessage) message;
            appendProperty("Command", sslm.getCommand(), output);
            for (ScheduledStatus ss : sslm.getStatuses()) {
                appendProperty("ScheduledStatus", new StringBuilder().append(ss.getName()).append(" ").append(ss.getId()).append(" ").append(this.dateFormat.format(ss.getStart())).append(" ").append(this.dateFormat.format(ss.getEnd())).toString(), output);
            }
        } else if ((message instanceof ServerRegisterMessage)) {
            ServerRegisterMessage srm = (ServerRegisterMessage) message;
            appendProperty("Command", srm.getCommand(), output);
            appendProperty("ServerName", srm.getServerName(), output);
        } else if ((message instanceof ServerContactList)) {
            ServerContactList scl = (ServerContactList) message;
            appendProperty("Command", scl.getCommand(), output);
            appendProperty("Group", scl.getGroup(), output);
            ContactAction action = scl.getActions();
            String name = action.getAction().equals(ContactAction.Action.ADD) ? "Add-Contact" : "Remove-Contact";
            if (action.getProduct() != null) {
                appendProperty(name, action.getExtension() + (action.getProduct().contentEquals("none") ? "" : " " + action.getProduct()), output);
            } else {
                appendProperty(name, action.getExtension(), output);
            }
        } else if ((message instanceof ServerIM)) {
            ServerIM sim = (ServerIM) message;
            appendProperty("Command", sim.getCommand(), output);
            appendProperty("Group", sim.getGroup(), output);
            if (((ServerIM) message).getPeerMessage() instanceof TextMessage2) {
                TextMessage2 tm2 = (TextMessage2) sim.getPeerMessage();
                appendProperty("Command", tm2.getCommand(), output);
                appendProperty("Id", tm2.getId(), output);
                appendProperty("Sender", tm2.getSender(), output);
                appendProperty("DateTime", tm2.getDateTime(), output);
                writeExtensions(tm2.getExtensions(), output);
                appendFreeText(tm2.getText(), output, tm2.getLength());
            } else if (((ServerIM) message).getPeerMessage() instanceof TextMessage) {
                TextMessage tm = (TextMessage) sim.getPeerMessage();
                appendProperty("Command", tm.getCommand(), output);
                appendProperty("Id", tm.getId(), output);
                appendProperty("Sender", tm.getSender(), output);
                appendProperty("Share", Boolean.toString(tm.isShare()), output);
                writeExtensions(tm.getExtensions(), output);
                appendFreeText(tm.getText(), output, tm.getLength());
            } else if (((ServerIM) message).getPeerMessage() instanceof CustomMessage) {
                CustomMessage cm = (CustomMessage) sim.getPeerMessage();
                appendProperty("Command", cm.getCommand(), output);
                appendProperty("Id", cm.getId(), output);
                appendProperty("Sender", cm.getSender(), output);
                appendProperty("DateTime", cm.getDateTime(), output);
                writeExtensions(cm.getExtensions(), output);
                appendFreeText(cm.getText(), output, cm.getLength());
            }
        } else if ((message instanceof ServerStatusListMessage)) {
            ServerStatusListMessage sslm = (ServerStatusListMessage) message;
            appendProperty("Command", sslm.getCommand(), output);
            appendProperty("Group", sslm.getGroup(), output);
            for (ExtensionStatus es : sslm.getStatuses()) {
                appendProperty("ExtensionStatus", new StringBuilder().append(es.getExtension()).append(" ").append(es.getStatus() == null ? DEFAULT_STATUS : new StringBuilder().append(es.getStatus()).append(" ").append(this.dateFormat.format(es.getSince())).toString()).toString(), output);
            }
        } else if ((message instanceof ServerSetStatus)) {
            ServerSetStatus sss = (ServerSetStatus) message;
            appendProperty("Command", sss.getCommand(), output);
            appendProperty("Group", sss.getGroup(), output);
            appendProperty("ExtensionStatus", new StringBuilder().append(sss.getExtension()).append(" ").append(sss.getStatus().getName()).append(" ").append(this.dateFormat.format(sss.getStatus().getStart())), output);
            appendProperty("Override", Boolean.toString(sss.getStatus().isOverride()), output);
        } else {
            throw new RuntimeException(new StringBuilder().append("Could not identify as KEMP1 encoded Legacy Client message ").append(message).toString());
        }
        try {
            output.append('\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized void encodeKemp2(Message message, Appendable original) {
        Appendable output = original;

        if ((message instanceof KeepAliveMessage)) {
            appendProperty("Command", message.getCommand(), output);
        } else if ((message instanceof ContactListMessage)) {
            ContactListMessage clm = (ContactListMessage) message;
            Collection<ContactAction> actions = clm.getActions();
            appendProperty("Command", clm.getCommand(), output);
            for (ContactAction action : actions) {
                String name = action.getAction().equals(ContactAction.Action.ADD) ? "Add-Contact" : "Remove-Contact";
                appendProperty(name, action.getExtension(), output);
            }
        } else if ((message instanceof ContactList2Message)) {
            appendProperty("Command", "ContactList", output);
            ContactList2Message clm2 = (ContactList2Message) message;
            for (ContactAction action : clm2.getActions()) {
                String name = action.getAction().equals(ContactAction.Action.ADD) ? "Add-Contact" : "Remove-Contact";
                appendProperty(name, action.getExtension(), output);
            }
        } else if ((message instanceof TextMessage)) {
            TextMessage tm = (TextMessage) message;
            appendProperty("Command", tm.getCommand(), output);
            appendProperty("Id", tm.getId(), output);
            appendProperty("Sender", tm.getSender(), output);
            appendProperty("Share", Boolean.toString(tm.isShare()), output);
            writeExtensions(tm.getExtensions(), output);
            appendFreeText(tm.getText(), output, tm.getLength());
        } else if ((message instanceof TextMessage2)) {
            TextMessage2 tm2 = (TextMessage2) message;
            appendProperty("Command", "Message", output);
            appendProperty("Id", tm2.getId(), output);
            appendProperty("Sender", tm2.getSender(), output);
            appendProperty("Share", "true", output);
            writeExtensions(tm2.getExtensions(), output);
            appendFreeText(tm2.getText(), output, tm2.getLength());
        } else if ((message instanceof CustomMessage)) {
            CustomMessage cm = (CustomMessage) message;
            appendProperty("Command", cm.getCommand(), output);
            appendProperty("Id", cm.getId(), output);
            appendProperty("Sender", cm.getSender(), output);
            appendProperty("Share", "true", output);
            writeExtensions(cm.getExtensions(), output);
            appendFreeText(cm.getText(), output, cm.getLength());
        } else if ((message instanceof ErrorMessage)) {
            ErrorMessage em = (ErrorMessage) message;
            appendProperty("Command", em.getCommand(), output);
            appendProperty("Number", Integer.toString(em.getNumber()), output);
            appendProperty("Description", this._messages.getString(em.getKey()), output);
            appendProperty("AlertUser", Boolean.toString(em.isAlert()), output);
        } else if ((message instanceof StatusListMessage)) {
            StatusListMessage slm = (StatusListMessage) message;
            appendProperty("Command", slm.getCommand(), output);
            for (ExtensionStatus es : slm.getStatuses()) {
                appendProperty("ExtensionStatus", new StringBuilder().append(es.getExtension()).append(" ").append(es.getStatus() == null ? DEFAULT_STATUS : new StringBuilder().append(es.getStatus()).append(" ").append(this.dateFormat.format(es.getSince())).toString()).toString(), output);
            }
        } else if ((message instanceof ScheduledStatusListMessage)) {
            ScheduledStatusListMessage sslm = (ScheduledStatusListMessage) message;
            appendProperty("Command", sslm.getCommand(), output);
            for (ScheduledStatus ss : sslm.getStatuses()) {
                appendProperty("ScheduledStatus", new StringBuilder().append(ss.getName()).append(" ").append(ss.getId()).append(" ").append(this.dateFormat.format(ss.getStart())).append(" ").append(this.dateFormat.format(ss.getEnd())).toString(), output);
            }
        } else if ((message instanceof ServerRegisterMessage)) {
            ServerRegisterMessage srm = (ServerRegisterMessage) message;
            appendProperty("Command", srm.getCommand(), output);
            appendProperty("ServerName", srm.getServerName(), output);
        } else if ((message instanceof ServerContactList)) {
            ServerContactList scl = (ServerContactList) message;
            appendProperty("Command", scl.getCommand(), output);
            appendProperty("Group", scl.getGroup(), output);
            ContactAction action = scl.getActions();
            String name = action.getAction().equals(ContactAction.Action.ADD) ? "Add-Contact" : "Remove-Contact";
            if (action.getProduct() != null) {
                appendProperty(name, action.getExtension() + (action.getProduct().contentEquals("none") ? "" : " " + action.getProduct()), output);
            } else {
                appendProperty(name, action.getExtension(), output);
            }
        } else if ((message instanceof ServerIM)) {
            ServerIM sim = (ServerIM) message;
            appendProperty("Command", sim.getCommand(), output);
            appendProperty("Group", sim.getGroup(), output);
            if (((ServerIM) message).getPeerMessage() instanceof TextMessage2) {
                TextMessage2 tm2 = (TextMessage2) sim.getPeerMessage();
                appendProperty("Command", tm2.getCommand(), output);
                appendProperty("Id", tm2.getId(), output);
                appendProperty("Sender", tm2.getSender(), output);
                appendProperty("DateTime", tm2.getDateTime(), output);
                writeExtensions(tm2.getExtensions(), output);
                appendFreeText(tm2.getText(), output, tm2.getLength());
            } else if (((ServerIM) message).getPeerMessage() instanceof TextMessage) {
                TextMessage tm = (TextMessage) sim.getPeerMessage();
                appendProperty("Command", tm.getCommand(), output);
                appendProperty("Id", tm.getId(), output);
                appendProperty("Sender", tm.getSender(), output);
                appendProperty("Share", Boolean.toString(tm.isShare()), output);
                writeExtensions(tm.getExtensions(), output);
                appendFreeText(tm.getText(), output, tm.getLength());
            } else if (((ServerIM) message).getPeerMessage() instanceof CustomMessage) {
                CustomMessage cm = (CustomMessage) sim.getPeerMessage();
                appendProperty("Command", cm.getCommand(), output);
                appendProperty("Id", cm.getId(), output);
                appendProperty("Sender", cm.getSender(), output);
                appendProperty("DateTime", cm.getDateTime(), output);
                writeExtensions(cm.getExtensions(), output);
                appendFreeText(cm.getText(), output, cm.getLength());
            }
        } else if ((message instanceof ServerStatusListMessage)) {
            ServerStatusListMessage sslm = (ServerStatusListMessage) message;
            appendProperty("Command", sslm.getCommand(), output);
            appendProperty("Group", sslm.getGroup(), output);
            for (ExtensionStatus es : sslm.getStatuses()) {
                appendProperty("ExtensionStatus", new StringBuilder().append(es.getExtension()).append(" ").append(es.getStatus() == null ? DEFAULT_STATUS : new StringBuilder().append(es.getStatus()).append(" ").append(this.dateFormat.format(es.getSince())).toString()).toString(), output);
            }
        } else if ((message instanceof ServerSetStatus)) {
            ServerSetStatus sss = (ServerSetStatus) message;
            appendProperty("Command", sss.getCommand(), output);
            appendProperty("Group", sss.getGroup(), output);
            appendProperty("ExtensionStatus", new StringBuilder().append(sss.getExtension()).append(" ").append(sss.getStatus().getName()).append(" ").append(this.dateFormat.format(sss.getStatus().getStart())), output);
            appendProperty("Override", Boolean.toString(sss.getStatus().isOverride()), output);
        } else {
            throw new RuntimeException(new StringBuilder().append("Could not identify as KEMP1/KEMP2 encoded New Client message ").append(message).toString());
        }
        try {
            output.append('\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized void encodeServer(Message message, Appendable original) {
        Appendable output = original;
        if ((message instanceof KeepAliveMessage)) {
            appendProperty("Command", message.getCommand(), output);
        } else if ((message instanceof ServerRegisterMessage)) {
            ServerRegisterMessage srm = (ServerRegisterMessage) message;
            appendProperty("Command", srm.getCommand(), output);
            appendProperty("ServerName", srm.getServerName(), output);
        } else if ((message instanceof ServerContactList)) {
            ServerContactList scl = (ServerContactList) message;
            appendProperty("Command", scl.getCommand(), output);
            appendProperty("Group", scl.getGroup(), output);
            ContactAction action = scl.getActions();
            String name = action.getAction().equals(ContactAction.Action.ADD) ? "Add-Contact" : "Remove-Contact";
            if (action.getProduct() != null) {
                appendProperty(name, action.getExtension() + (action.getProduct().contentEquals("none") ? "" : " " + action.getProduct()), output);
            } else {
                appendProperty(name, action.getExtension(), output);
            }
        } else if ((message instanceof ServerIM)) {
            ServerIM sim = (ServerIM) message;
            appendProperty("Command", sim.getCommand(), output);
            appendProperty("Group", sim.getGroup(), output);
            if (((ServerIM) message).getPeerMessage() instanceof TextMessage2) {
                TextMessage2 tm2 = (TextMessage2) sim.getPeerMessage();
                appendProperty("Command", tm2.getCommand(), output);
                appendProperty("Id", tm2.getId(), output);
                appendProperty("Sender", tm2.getSender(), output);
                appendProperty("DateTime", tm2.getDateTime(), output);
                writeExtensions(tm2.getExtensions(), output);
                appendFreeText(tm2.getText(), output, tm2.getLength());
            } else if (((ServerIM) message).getPeerMessage() instanceof TextMessage) {
                TextMessage tm = (TextMessage) sim.getPeerMessage();
                appendProperty("Command", tm.getCommand(), output);
                appendProperty("Id", tm.getId(), output);
                appendProperty("Sender", tm.getSender(), output);
                appendProperty("Share", Boolean.toString(tm.isShare()), output);
                writeExtensions(tm.getExtensions(), output);
                appendFreeText(tm.getText(), output, tm.getLength());
            } else if (((ServerIM) message).getPeerMessage() instanceof CustomMessage) {
                CustomMessage cm = (CustomMessage) sim.getPeerMessage();
                appendProperty("Command", cm.getCommand(), output);
                appendProperty("Id", cm.getId(), output);
                appendProperty("Sender", cm.getSender(), output);
                appendProperty("DateTime", cm.getDateTime(), output);
                writeExtensions(cm.getExtensions(), output);
                appendFreeText(cm.getText(), output, cm.getLength());
            }
        } else if ((message instanceof ServerStatusListMessage)) {
            ServerStatusListMessage sslm = (ServerStatusListMessage) message;
            appendProperty("Command", sslm.getCommand(), output);
            appendProperty("Group", sslm.getGroup(), output);
            for (ExtensionStatus es : sslm.getStatuses()) {
                appendProperty("ExtensionStatus", new StringBuilder().append(es.getExtension()).append(" ").append(es.getStatus() == null ? DEFAULT_STATUS : new StringBuilder().append(es.getStatus()).append(" ").append(this.dateFormat.format(es.getSince())).toString()).toString(), output);
            }
        } else if ((message instanceof ServerSetStatus)) {
            ServerSetStatus sss = (ServerSetStatus) message;
            appendProperty("Command", sss.getCommand(), output);
            appendProperty("Group", sss.getGroup(), output);
            appendProperty("ExtensionStatus", new StringBuilder().append(sss.getExtension()).append(" ").append(sss.getStatus().getName()).append(" ").append(this.dateFormat.format(sss.getStatus().getStart())), output);
            appendProperty("Override", Boolean.toString(sss.getStatus().isOverride()), output);
        } else {
            throw new RuntimeException(new StringBuilder().append("Could not identify as KEMP2 encoded InterServer message ").append(message).toString());
        }
        try {
            output.append('\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void appendFreeText(CharSequence text, Appendable output, int length) {
        try {
            appendProperty("Length", Integer.toString(length), output);
            output.append(text);
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
                builder.append(", ");
            }
            appendProperty("Extensions", builder, output);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class Property {

        CharSequence name;
        CharSequence value;

        public Property(CharSequence line) {
            int length = line.length();
            for (int i = 0; i < length; i++) {
                if ((line.charAt(i) != ':') || (line.charAt(i + 1) != ' ')) {
                    continue;
                }
                this.name = line.subSequence(0, i);
                this.value = line.subSequence(i + 2, length);
                return;
            }
        }
    }
}