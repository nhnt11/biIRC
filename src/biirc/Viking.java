/*
 * biIRC - biIRC is an IRC Remote Control
 * 
 * Copyright (C) 2012 Nihanth Subramanya
 * 
 * biIRC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * biIRC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with biIRC.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package biirc;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class Viking implements Runnable {

    //Global variables
    //GUI manager
    private VikingWindow mVikingWindow;
    private Socket mServerSocket;
    private String mServer;
    private int mPort;
    private String mNick;
    private String mAltNick;
    /*
     * Stores the nick that is finally used though only for convenience during
     * initialization. Not used thereafter.
     */
    private String mFinalNick;
    private String mIdent;
    private String mNickPass;
    private boolean mGhost; //Whether or not to attempt ghosting
    private String mPassphrase; //Passphrase used to authenticate user to send commands
    private final String LOG = "--------------------------LOG--------------------------" + CRLF;
    private BufferedReader mServerReader;
    private BufferedWriter mServerWriter;
    private StringBuilder mLog;
    private boolean run = true; //Run flag for the server message monitor thread.
    private String mCurrentAuthorizedMaster; //Current authenticated user (format nick!~username@host)
    private String mCurrentAuthorizedNick; //Current authenticated user's nick
    private VikingRobot mRobot; //Emulates keyboard/mouse input
    private boolean robotNotSupported = false; //Flag in case input emulation not supported
    private Properties mProperties; //Loaded from config file
    //IRC commands etc
    private static final String PRIVMSG = "PRIVMSG ";
    private static final String NOTICE = " NOTICE ";
    private static final String NICK = "NICK ";
    private static final String USER = "USER ";
    private static final String QUIT = "QUIT ";
    private static final String PING = "PING ";
    private static final String PONG = "PONG ";
    private static final String CRLF = "\r\n";
    //Constants corresponding to commands
//    private static final int EXEC = 0;
//    private static final int KEY_HOLD = 1;
//    private static final int KEY_RELEASE = 2;
//    private static final int KEY_RELEASE_ALL = 3;
//    private static final int KEY_PRESS = 4;
//    private static final int TYPE = 5;
//    private static final int MOVE_MOUSE = 6;
//    private static final int MOVE_MOUSE_REL = 7;
//    private static final int PRESS_MOUSE = 8;
//    private static final int RELEASE_MOUSE = 9;
//    private static final int CLICK_MOUSE = 10;
//    private static final int COPY_FILE = 11;
//    private static final int MOVE_FILE = 12;
//    private static final int DELETE_FILE = 13;
//    private static final int GETCB = 14;
//    private static final int MACRO = 15;
//    private static final int ECHO = 16;
//    private static final int SLEEP = 17;
//    private static final int DUMPLOG = 18;
//    private static final int EXIT = 19;
//    private static final int HELP = 20;

    private enum CommandConstant {

        NULL, EXEC, KEY_HOLD, KEY_RELEASE, KEY_RELEASE_ALL, KEY_PRESS, TYPE,
        MOVE_MOUSE, MOVE_MOUSE_REL, PRESS_MOUSE, RELEASE_MOUSE, CLICK_MOUSE,
        COPY_FILE, MOVE_FILE, DELETE_FILE, GETCB, MACRO, ECHO, SLEEP, DUMPLOG,
        EXIT, HELP
    }
    /*
     * Hashmap used to convert user-inputed command string to one of the
     * constants defined above. This is so that command strings can easily be
     * changed if/when required.
     */
    private static final HashMap<String, CommandConstant> mCommands = new HashMap();
    /*
     * HashMap used to store syntax for each command. For consistency purposes.
     */
    private static final HashMap<CommandConstant, String> mCommandsSyntax = new HashMap();

    static {
        mCommands.put("exec", CommandConstant.EXEC);
        mCommands.put("holdkeys", CommandConstant.KEY_HOLD);
        mCommands.put("releasekeys", CommandConstant.KEY_RELEASE);
        mCommands.put("releaseallkeys", CommandConstant.KEY_RELEASE_ALL);
        mCommands.put("presskeys", CommandConstant.KEY_PRESS);
        mCommands.put("type", CommandConstant.TYPE);
        mCommands.put("movemouse", CommandConstant.MOVE_MOUSE);
        mCommands.put("movemouserel", CommandConstant.MOVE_MOUSE_REL);
        mCommands.put("pressmouse", CommandConstant.PRESS_MOUSE);
        mCommands.put("releasemouse", CommandConstant.RELEASE_MOUSE);
        mCommands.put("clickmouse",CommandConstant.CLICK_MOUSE);
        mCommands.put("copyfile", CommandConstant.COPY_FILE);
        mCommands.put("movefile", CommandConstant.MOVE_FILE);
        mCommands.put("deletefile", CommandConstant.DELETE_FILE);
        mCommands.put("getcb", CommandConstant.GETCB);
        mCommands.put("macro", CommandConstant.MACRO);
        mCommands.put("echo", CommandConstant.ECHO);
        mCommands.put("sleep", CommandConstant.SLEEP);
        mCommands.put("dumplog", CommandConstant.DUMPLOG);
        mCommands.put("exit", CommandConstant.EXIT);
        mCommands.put("help", CommandConstant.HELP);

        mCommandsSyntax.put(CommandConstant.EXEC, "Usage - exec [executable]");
        mCommandsSyntax.put(CommandConstant.KEY_HOLD, "Usage - holdkeys [keystroke]");
        mCommandsSyntax.put(CommandConstant.KEY_RELEASE, "Usage - releasekeys [keystroke]");
        mCommandsSyntax.put(CommandConstant.KEY_RELEASE_ALL, "Usage - releaseallkeys");
        mCommandsSyntax.put(CommandConstant.KEY_PRESS, "Usage - presskeys [keystroke]");
        mCommandsSyntax.put(CommandConstant.TYPE, "Usage - type [text]");
        mCommandsSyntax.put(CommandConstant.MOVE_MOUSE, "Usage - movemouse [x] [y]");
        mCommandsSyntax.put(CommandConstant.MOVE_MOUSE_REL, "Usage - movemouserel [relative x] [relative y]");
        mCommandsSyntax.put(CommandConstant.PRESS_MOUSE, "Usage - pressmouse [button]");
        mCommandsSyntax.put(CommandConstant.RELEASE_MOUSE, "Usage - releasemouse [button]");
        mCommandsSyntax.put(CommandConstant.CLICK_MOUSE, "Usage - clickmouse [button]");
        mCommandsSyntax.put(CommandConstant.COPY_FILE, "Usage - copyfile [file] [dest]");
        mCommandsSyntax.put(CommandConstant.MOVE_FILE, "Usage - movefile [file] [dest]");
        mCommandsSyntax.put(CommandConstant.DELETE_FILE, "Usage - deletefile [file]");
        mCommandsSyntax.put(CommandConstant.GETCB, "Usage - getcb");
        mCommandsSyntax.put(CommandConstant.MACRO, "Usage - macro [macroname] [arguments]");
        mCommandsSyntax.put(CommandConstant.ECHO, "Usage - echo [text]");
        mCommandsSyntax.put(CommandConstant.SLEEP, "Usage - sleep [millis]");
        mCommandsSyntax.put(CommandConstant.DUMPLOG, "Usage - dumplog");
        mCommandsSyntax.put(CommandConstant.EXIT, "Usage - exit");
        mCommandsSyntax.put(CommandConstant.HELP, "Usage - help");
    }
    //Used to send user input to various parts of the app. See class below for details
//    private UserInputBroadcaster mUserInputMon = new UserInputBroadcaster();
    //UserInputListener which quits or dumps the log on user request.
//    private UserInputListener mQuitLogListner = new UserInputListener() {
//
//        @Override
//        public void userInputObtained(String line) {
//            try {
//                if (line.equals("exit"))
//                    quit();
//                if (line.equals("log"))
//                    System.out.println(mLog.toString());
//            } catch (Exception e) {
//            }
//        }
//    };
    /*
     * UserInputListener that sends user input as an IRC command (in full form,
     * using PRIVMSG, NICK, etc and not "/msg" or "/nick") if said input starts
     * with ">"
     *
     * This is mainly for debug purposes but can also be used to change nick,
     * etc
     */
//    private UserInputListener mUserIRCClient = new UserInputListener() {
//
//        @Override
//        public void userInputObtained(String line) {
//            try {
//                if (line.startsWith(">"))
//                    writeToServer(line.substring(1) + CRLF);
//            } catch (Exception e) {
//                mVikingWindow.appendErrorToConsole(
//                        "Error communicating with server - " + e.getMessage());
//            }
//        }
//    };
    //Shell mode stuff
    //mShellProcess is the current running process that was exec()'d.
    private Process mShellProcess;
    /*
     * Three threads are used for shell mode - mOutputThread, mErrorThread, and
     * mTerminatorThread. Each has its own runnable, which is used to
     * reinitialize the thread every time a new command is executed. This is
     * because Java does not allow re-starting a thread without reinitializing.
     *
     * mOutputThread contiunously sends the user output from the process's
     * output stream.
     */
    private Thread mOutputThread;
    Runnable mOutputRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(mShellProcess.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    sendMessage(getCurrentAuthorizedNick(), line);
                    Thread.sleep(MESSAGE_SEND_DELAY);
                    synchronized (mShellLockObj) {
                        if (mShellProcessForceTerminated) {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    };
    //mErrorThread contiunously sends the user error output from the process's
    //error stream.
    private Thread mErrorThread;
    Runnable mErrorRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(mShellProcess.getErrorStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    sendMessage(getCurrentAuthorizedNick(), line);
                    Thread.sleep(MESSAGE_SEND_DELAY);
                    synchronized (mShellLockObj) {
                        if (mShellProcessForceTerminated) {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    };
    /*
     * mTerminatorThread waits for the process to terminate (on its own accord).
     * It then sends the user a process terminated message with the process's
     * exit value, and sets mShellProcess to null.
     */
    private Thread mTerminatorThread;
    Runnable mTerminatorRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                mShellProcess.waitFor();
                synchronized (mMessageListenerLock) {
                    mCurrentMessageListener = mCommandsListener;
                }
                while (mOutputThread.isAlive() || mErrorThread.isAlive()) {
                    Thread.sleep(20);
                }
                sendMessage(getCurrentAuthorizedNick(),
                        "Process terminated with exit value " + mShellProcess.exitValue());

            } catch (Exception e) {
            }
            mShellProcess = null;
        }
    };
    //Object used to synchronize operations between the three shell mode threads.
    private final Object mShellLockObj = new Object();
    /*
     * boolean which stores whether the user used "END SHELL" to force-terminate
     * executed process. This flag tells the three threads to stop when it is
     * set to true.
     */
    private boolean mShellProcessForceTerminated = false;
    private MessageListener mCommandsListener = new MessageListener() {

        @Override
        public void processMessage(String msg) {
            try {
                processPrivMsg(msg);
            } catch (Exception e) {
            }
        }
    };
    private MessageListener mShellListener = new MessageListener() {

        @Override
        public void processMessage(String msg) {
            try {
                writeToShellProcess(msg);
            } catch (Exception e) {
            }
        }
    };
    private MessageListener mCurrentMessageListener = mCommandsListener;
    private final Object mMessageListenerLock = new Object();
    //Amount of time to sleep before sending consecutive messages
    //in order to prevent flooding
    private static final int MESSAGE_SEND_DELAY = 1000;

    //Connect to server, set nick, and start main threads.
    @SuppressWarnings("SleepWhileInLoop")
    private void initialize() {
        try {
            //Start the GUI.
            mVikingWindow = new VikingWindow(this);
            mVikingWindow.setVisible(true);
            printGPLNotice();
            //Quit and log listener started before anything happens so that the user can
            //quit or view the log at anytime.
            //System.out.println("Type \"quit\" at any time to quit or \"log\" to view the log.");
//            mUserInputMon.addUserInputListener(mQuitLogListner);
//            mUserInputMon.start();
            mProperties = new Properties();
            try {
                mProperties.load(new FileReader(new File(
                        System.getProperty("user.home")
                        + System.getProperty("file.separator")
                        + "biIRC" + System.getProperty("file.separator")
                        + "biirccfg.txt")));
            } catch (FileNotFoundException e) {
                new ConfigDialog(mVikingWindow, this, mProperties).setVisible(true);
            }
            mServer = mProperties.getProperty("server");
            if (mServer == null || mServer.equals("")) {
                mVikingWindow.appendErrorToConsole("Server not specified in config file.");
            }
            try {
                mPort = Integer.parseInt(mProperties.getProperty("port"));
            } catch (NumberFormatException e) {
                mVikingWindow.appendErrorToConsole("Port invalid or not specified in config file.");
            }
            mNick = mProperties.getProperty("nick");
            if (mNick == null || mNick.equals("")) {
                mVikingWindow.appendErrorToConsole("Nick not specified in config file.");
            }
            mIdent = mProperties.getProperty("ident");
            if (mIdent == null || mIdent.equals("")) {
                mVikingWindow.appendErrorToConsole("Ident not specified in config file.");
            }
            mAltNick = mProperties.getProperty("altnick");
            mNickPass = mProperties.getProperty("nickpass");
            mGhost = Boolean.parseBoolean(mProperties.getProperty("ghost"));
            mPassphrase = mProperties.getProperty("passphrase");
            if (mPassphrase == null || mPassphrase.length() < 6) {
                mVikingWindow.appendErrorToConsole("Passphrase must be at least 6 characters long.");
            }

            //Connect to IRC server
            //Keep retrying till a connection is established
            mVikingWindow.appendToConsole(
                    "Connecting to host " + mServer + " on port " + mPort + "...");
            while (true) {
                try {
                    mServerSocket = new Socket(mServer, mPort);
                    break;
                } catch (Exception e) {
                    mVikingWindow.appendErrorToConsole("Error - " + e.getMessage());
                    mVikingWindow.appendToConsole("Retrying in 10 seconds...");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ee) {
                    }
                }
            }
            mVikingWindow.appendToConsole("Connected to server.");

            //Initialize log
            mLog = new StringBuilder();
            mLog.append(LOG);
            mVikingWindow.appendToLog(LOG);

            //Initialize input/output to server
            mServerWriter = new LoggedBufferedWriter(
                    new OutputStreamWriter(mServerSocket.getOutputStream()),
                    mLog,
                    mVikingWindow);
            mServerReader = new LoggedBufferedReader(
                    new InputStreamReader(mServerSocket.getInputStream()),
                    mLog,
                    mVikingWindow);

            //Set nick and ident, ghost if enabled
            mFinalNick = mNick;
            mVikingWindow.appendToConsole("Setting nick to " + mFinalNick);
            writeToServer(NICK + mFinalNick + CRLF);
            writeToServer(USER + mIdent + " 0 * : " + CRLF);
            //Watch for reply from server and take appropriate action
            String line;
            boolean altNickAttempted = false;
            outer:
            while ((line = mServerReader.readLine()) != null) {
                //Reply code 432 => invalid nick
                if (line.indexOf(" 432 ") > -1) {
                    mVikingWindow.appendErrorToConsole(
                            "Invalid nick. Enter a new one and try again.");
                }
                //Reply code 433 => Nick in use
                if (line.indexOf(" 433 ") > -1) {
                    mVikingWindow.appendErrorToConsole("Nick already in use.");
                    if (!altNickAttempted && mAltNick != null && !mAltNick.equals("")) {
                        altNickAttempted = true;
                        mFinalNick = mAltNick;
                        mVikingWindow.appendToConsole("Setting nick to " + mFinalNick);
                        writeToServer(NICK + mFinalNick + CRLF);
                        writeToServer(USER + mIdent + " 0 * : " + CRLF);
                        continue;
                    }
                    //Get a new nick.
                    mFinalNick = JOptionPane.showInputDialog(mVikingWindow,
                            "Nick already in use. Enter another one:");
                    mVikingWindow.appendToConsole("Setting nick to " + mFinalNick);
                    writeToServer(NICK + mFinalNick + CRLF);
                    writeToServer(USER + mIdent + " 0 * : " + CRLF);
//                    //Watch user input for a new nick.
//                    mVikingWindow.appendToConsole("Enter another nick...");
//                    final Object lock = new Object();
//                    mUserInputMon.addUserInputListener(new UserInputListener() {
//
//                        @Override
//                        public void userInputObtained(String line) {
//                            try {
//                                mUserInputMon.removeUserInputListener(this);
//                                mFinalNick = line;
//                                System.out.println("Setting nick to " + mFinalNick);
//                                writeToServer(NICK + mFinalNick + CRLF);
//                                writeToServer(USER + mIdent + " 0 * : " + CRLF);
//                                //Notify that a new nick has been inputed and is being processed
//                                synchronized (lock) {
//                                    lock.notify();
//                                }
//                            } catch (Exception e) {
//                            }
//                        }
//                    });
//                    //Wait on the lock object till the listener calls notify
//                    try {
//                        synchronized (lock) {
//                            lock.wait();
//                        }
//                    } catch (Exception e) {
//                    }
                    //A new nick has been chosen. Restart the loop to listen for server reply
                    continue;
                }
                //Code 004 => Nick successfully set.
                if (line.indexOf(" 004 ") > -1) {
                    mVikingWindow.appendToConsole("Nick set to " + mFinalNick + ".");
                    if (!mFinalNick.equals(mNick) && mGhost) {
                        mVikingWindow.appendToConsole("Attempting to ghost " + mNick + "...");
                        sendMessage("NickServ", "GHOST " + mNick + " " + mNickPass);
                    }
                    break;
                }
            }

            //Clear buffer of useless welcome messages, etc.
            while (mServerReader.ready()) {
                mServerReader.readLine();
            }

            //Identify with NickServ if a password is specified and the preferred nick worked.
            if (mNickPass != null & !mNickPass.equals("") && mNick.equals(mFinalNick)) {
                mVikingWindow.appendToConsole("Identifying for " + mFinalNick + "...");
                sendMessage("NickServ", "identify " + mNickPass);
            }

            //Prepare robot and set the robotNotSupported flag on failure
            try {
                mRobot = new VikingRobot();
            } catch (AWTException e) {
                robotNotSupported = true;
            }

            //Start thread to monitor for incoming messages
            new Thread(this).start();

            //Start accepting manual IRC commands
//            mUserInputMon.addUserInputListener(mUserIRCClient);
        } catch (IOException e) {
            mVikingWindow.appendErrorToConsole("An error occured - " + e.getMessage());
        }
    }

    //Prints copyright and warranty notice
    private void printGPLNotice() {
        mVikingWindow.appendToConsole("biIRC  Copyright (C) 2012  Nihanth Subramanya");
        mVikingWindow.appendToConsole("This program comes with ABSOLUTELY NO WARRANTY.");
        mVikingWindow.appendToConsole("This is free software, and you are welcome to redistribute it "
                + "under certain conditions.");
        mVikingWindow.appendToConsole("For details see http://www.gnu.org/licenses/gpl-3.0.txt");
        mVikingWindow.appendToConsole("");
    }

    Properties getProperties() {
        return mProperties;
    }

    private String getCurrentAuthorizedNick() {
        synchronized (this) {
            return mCurrentAuthorizedNick;
        }
    }

    //Send a quit message to the server, stop the main thread and exit
    void quit() {
        try {
            writeToServer(QUIT + CRLF);
            mServerSocket.close();
        } catch (Exception e) {
        }
        synchronized (this) {
            run = false;
        }
    }

    void restart() throws URISyntaxException, IOException {
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final File currentJar = new File(
                this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());

        /*
         * is it a jar file?
         */
        if (!currentJar.getName().endsWith(".jar")) {
            return;
        }

        /*
         * Build command: java -jar application.jar
         */
        final ArrayList<String> command = new ArrayList<String>();
        command.add(javaBin);
        command.add("-jar");
        command.add(currentJar.getPath());

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
        System.exit(0);
    }

    // Convenience method to send message to server.
    private void writeToServer(String msg) throws IOException {
        if (!msg.endsWith(CRLF)) {
            msg += CRLF; //precaution, not to be taken for granted
        }
        mServerWriter.write(msg);
        mServerWriter.flush();
    }

    /*
     * Processes message from server. Send PRIVMSG's to be further processed if
     * they are from mCurrentAuthorizedMaster Also displays NOTICEs from the
     * server. All other messages ignored.
     */
    private void processServerMessage(String line) throws IOException {
        if (line.startsWith(PING)) {
            writeToServer(PONG + line.substring(5) + CRLF);
            return;
        }
        StringTokenizer st = new StringTokenizer(line);
        String from = st.nextToken(": ");
        String nick = new StringTokenizer(from, "!").nextToken();
        String keyword = st.nextToken(" ");
        st.nextToken(":");
        String msg = st.nextToken("").substring(1);
        if (keyword.trim().equals(PRIVMSG.trim())) {
            if (!from.equals(mCurrentAuthorizedMaster)) {
                if (msg.equals(mPassphrase)) {
                    mCurrentAuthorizedMaster = from;
                    synchronized (this) {
                        mCurrentAuthorizedNick = nick;
                    }
                    sendMessage(nick,
                            "You (" + from + ") are now authorized to send commands.");
                }
            } else {
                mCurrentMessageListener.processMessage(msg);
            }
        } else if (keyword.trim().equals(NOTICE.trim())) {
            mVikingWindow.appendToConsole("Notice from " + nick + ": " + msg);
        }
    }

    /*
     * Sends message to the current shell process as input. returns false if no
     * process is currently running or true if message was successfully passed
     * to the process
     */
    private void writeToShellProcess(String msg) throws IOException {
        if (msg.equals("END SHELL")) {
            synchronized (mShellLockObj) {
                if (!mShellProcessForceTerminated);
                mShellProcessForceTerminated = true;
                mShellProcess.destroy();
            }
        }
        mShellProcess.getOutputStream().write((msg + CRLF).getBytes());
        mShellProcess.getOutputStream().flush();
    }

    /*
     * Process PRIVMSG from a user (processServerMessage() ensures the user is
     * authenticated) The first word of the message is taken as the keyword
     * using a StringTokenizer. The keyword is lookup()'d and the corresponding
     * action is taken.
     */
    private void processPrivMsg(String msg) throws IOException {
        StringTokenizer st = new StringTokenizer(msg, " ");
        String keyword = st.nextToken();
        CommandConstant command = lookup(keyword);
        switch (command) {
            case EXEC:
                if (msg.trim().length() < keyword.length() + 1) {
                    sendInvalidSyntaxMessage(command);
                } else {
                    try {
                        exec(msg.substring(keyword.length() + 1));
                    } catch (IOException e) {
                        e.printStackTrace(System.out);
                    }
                }
                break;
            case KEY_HOLD:
                if (st.countTokens() != 1) {
                    sendInvalidSyntaxMessage(command);
                } else {
                    sendMessage(getCurrentAuthorizedNick(), holdKeys(st.nextToken()));
                }
                break;
            case KEY_RELEASE:
                if (st.countTokens() != 1) {
                    sendInvalidSyntaxMessage(command);
                } else {
                    sendMessage(getCurrentAuthorizedNick(), releaseKeys(st.nextToken()));
                }
                break;
            case KEY_RELEASE_ALL:
                sendMessage(getCurrentAuthorizedNick(), releaseAllKeys());
                break;
            case KEY_PRESS:
                if (st.countTokens() != 1) {
                    sendInvalidSyntaxMessage(command);
                } else {
                    sendMessage(getCurrentAuthorizedNick(), keyPress(st.nextToken()));
                }
                break;
            case TYPE:
                if (msg.trim().length() < keyword.length() + 1) {
                    sendInvalidSyntaxMessage(command);
                } else {
                    sendMessage(getCurrentAuthorizedNick(), type(
                            msg.substring(keyword.length() + 1)));
                }
                break;
            case MOVE_MOUSE:
                if (st.countTokens() != 2) {
                    sendInvalidSyntaxMessage(command);
                } else {
                    sendMessage(getCurrentAuthorizedNick(), moveMouse(st.nextToken(),
                            st.nextToken()));
                }
                break;
            case MOVE_MOUSE_REL:
                if (st.countTokens() != 2) {
                    sendInvalidSyntaxMessage(command);
                } else {
                    sendMessage(getCurrentAuthorizedNick(),
                            moveMouseRel(st.nextToken(), st.nextToken()));
                }
                break;
            case PRESS_MOUSE:
                if (st.countTokens() != 1) {
                    sendInvalidSyntaxMessage(command);
                } else {
                    sendMessage(getCurrentAuthorizedNick(), pressMouse(st.nextToken()));
                }
                break;
            case RELEASE_MOUSE:
                if (st.countTokens() != 1) {
                    sendInvalidSyntaxMessage(command);
                } else {
                    sendMessage(getCurrentAuthorizedNick(), releaseMouse(st.nextToken()));
                }
                break;
            case CLICK_MOUSE:
                if (st.countTokens() != 1) {
                    sendInvalidSyntaxMessage(command);
                } else {
                    sendMessage(getCurrentAuthorizedNick(), clickMouse(st.nextToken()));
                }
                break;
            case COPY_FILE:
                if (st.countTokens() != 2) {
                    sendInvalidSyntaxMessage(command);
                } else {
                    sendMessage(getCurrentAuthorizedNick(),
                            copyFile(st.nextToken(), st.nextToken()));
                }
                break;
            case MOVE_FILE:
                if (st.countTokens() != 2) {
                    sendInvalidSyntaxMessage(command);
                } else {
                    sendMessage(getCurrentAuthorizedNick(),
                            moveFile(st.nextToken(), st.nextToken()));
                }
                break;
            case DELETE_FILE:
                if (st.countTokens() != 1) {
                    sendInvalidSyntaxMessage(command);
                } else {
                    sendMessage(getCurrentAuthorizedNick(), deleteFile(st.nextToken()));
                }
                break;
            case GETCB:
                sendMessage(getCurrentAuthorizedNick(), getClipBoard());
                break;
            case MACRO:
                if (st.countTokens() < 1) {
                    sendInvalidSyntaxMessage(command);
                    break;
                }
                String macro = st.nextToken();
                String args[];
                try {
                    String argline = st.nextToken("").trim();
                    if (argline.length() < 1) {
                        args = new String[0];
                    } else {
                        args = argline.split(" +");
                    }
                } catch (NoSuchElementException e) {
                    args = new String[0];
                }
                runMacro(macro, args);
                break;
            case ECHO:
                if (!st.hasMoreTokens()) {
                    sendInvalidSyntaxMessage(command);
                } else {
                    echo(st.nextToken("").trim());
                }
                break;
            case SLEEP:
                sleep(msg.substring(keyword.length() + 1));
                break;
            case DUMPLOG:
                dumpLog();
                break;
            case EXIT:
                sendMessage(getCurrentAuthorizedNick(),
                        "biIRC is now exiting and can receive no further commands");
                quit();
                System.exit(0);
                break;
            case HELP:
                sendHelpText();
                break;
            case NULL:
                sendMessage(getCurrentAuthorizedNick(), "Unrecognized command.");
                break;
        }
    }

    private void sendInvalidSyntaxMessage(CommandConstant command) throws IOException {
        sendMessage(getCurrentAuthorizedNick(), mCommandsSyntax.get(command));
    }

    /*
     * Executes a command. Starts the three shell threads and puts the app into
     * "shell mode" - all further commands are transferred as input to the shell
     * process until it terminates.
     */
    private void exec(String cmd) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        try {
            synchronized (mMessageListenerLock) {
                mCurrentMessageListener = mShellListener;
            }
            mShellProcessForceTerminated = false;
            mShellProcess = runtime.exec(cmd);
            mOutputThread = new Thread(mOutputRunnable);
            mOutputThread.start();
            mErrorThread = new Thread(mErrorRunnable);
            mErrorThread.start();
            mTerminatorThread = new Thread(mTerminatorRunnable);
            mTerminatorThread.start();
        } catch (Exception e) {
            sendMessage(getCurrentAuthorizedNick(),
                    "Error executing command \"" + cmd + "\" - " + e.getMessage());
        }

    }

    //Press and hold a keystroke
    private String holdKeys(String keyCombo) {
        if (keyCombo.trim().equals("")) {
            return mCommandsSyntax.get(CommandConstant.KEY_HOLD);
        }
        if (robotNotSupported) {
            return "Keyboard emulation not supported on your OS.";
        }
        return mRobot.holdKeyCombo(keyCombo, false)
                ? "Key combination " + keyCombo + " is now held"
                : "Invalid key combination";
    }

    //Release a keystroke
    private String releaseKeys(String keyCombo) {
        if (keyCombo.trim().equals("")) {
            return mCommandsSyntax.get(CommandConstant.KEY_RELEASE);
        }
        if (robotNotSupported) {
            return "Keyboard emulation not supported on your OS.";
        }
        return mRobot.releaseKeyCombo(keyCombo, false)
                ? "Key combination " + keyCombo + " is now released"
                : "Invalid key combination";
    }

    //Release all currently held keys
    private String releaseAllKeys() {
        if (robotNotSupported) {
            return "Keyboard emulation not supported on your OS.";
        }
        mRobot.releasePressedKeys();
        return "Successfully released all held keys.";
    }

    //Press and release a keystroke
    private String keyPress(String keyCombo) {
        if (keyCombo.trim().equals("")) {
            return mCommandsSyntax.get(CommandConstant.KEY_PRESS);
        }
        if (robotNotSupported) {
            return "Keyboard emulation not supported on your OS.";
        }
        return mRobot.pressKeyCombo(keyCombo)
                ? "Successfully pressed keystroke " + keyCombo
                : "Invalid key combination";
    }

    //Type some text
    private String type(String text) {
        if (robotNotSupported) {
            return "Keyboard emulation not supported on your OS.";
        }
        mRobot.typeString(text);
        return "Successfully typed \"" + text + "\".";
    }

    //Moves mouse to specified coordinates
    private String moveMouse(String X, String Y) {
        if (robotNotSupported) {
            return "Mouse emulation not supported on your OS.";
        }
        int x, y;
        try {
            x = Integer.parseInt(X);
            y = Integer.parseInt(Y);
        } catch (NumberFormatException e) {
            return mCommandsSyntax.get(CommandConstant.MOVE_MOUSE);
        }
        Point p = mRobot.moveMouse(x, y);
        return "Mouse position moved to " + p.x + ", " + p.y;
    }

    //Moves mouse relative to current coordinates by specified amount
    private String moveMouseRel(String relX, String relY) {
        if (robotNotSupported) {
            return "Mouse emulation not supported on your OS.";
        }
        int x, y;
        try {
            x = Integer.parseInt(relX);
            y = Integer.parseInt(relY);
        } catch (NumberFormatException e) {
            return mCommandsSyntax.get(CommandConstant.MOVE_MOUSE_REL);
        }
        Point p = mRobot.moveMouseRel(x, y);
        return "Mouse position moved to " + p.x + ", " + p.y;
    }

    //Press (and hold) a mouse button
    private String pressMouse(String button) {
        if (robotNotSupported) {
            return "Mouse emulation not supported on your OS.";
        }
        return mRobot.mousePress(button)
                ? button + " mouse button is now pressed"
                : mCommandsSyntax.get(CommandConstant.PRESS_MOUSE);
    }

    //Release a mouse button
    private String releaseMouse(String button) {
        if (robotNotSupported) {
            return "Mouse emulation not supported on your OS.";
        }
        return mRobot.mouseRelease(button)
                ? button + " mouse button released"
                : mCommandsSyntax.get(CommandConstant.RELEASE_MOUSE);
    }

    //Click a mouse button
    private String clickMouse(String button) {
        if (robotNotSupported) {
            return "Mouse emulation not supported on your OS.";
        }
        return mRobot.mouseClick(button)
                ? "Successfully " + button + " clicked the mouse"
                : mCommandsSyntax.get(CommandConstant.CLICK_MOUSE);
    }

    private String copyFile(String sourceStr, String destStr) throws IOException {
        File sourceFile = new File(sourceStr);
        if (!sourceFile.exists()) {
            return "File does not exist.";
        }
        File destFile = new File(
                destStr + System.getProperty("file.separator") + sourceFile.getName());
        if (!destFile.exists()) {
            destFile.getParentFile().mkdirs();
            destFile.createNewFile();
        } else {
            boolean replaceFile =
                    getInput("Destination already exists. Replace? (y/n)").equals("y");
            if (!replaceFile) {
                return "Copy operation aborted.";
            }
        }
        FileChannel source;
        FileChannel destination;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } catch (Exception e) {
            return e.getMessage();
        }
        return "File successfully copied.";
    }

    private String moveFile(String sourceStr, String destStr) throws IOException {
        File sourceFile = new File(sourceStr);
        if (!sourceFile.exists()) {
            return "File does not exist.";
        }
        File destFile = new File(
                destStr + System.getProperty("file.separator") + sourceFile.getName());
        if (!destFile.exists()) {
            destFile.getParentFile().mkdirs();
            destFile.createNewFile();
        } else {
            boolean replaceFile =
                    getInput("Destination already exists. Replace? (y/n)").equals("y");
            if (!replaceFile) {
                return "Move operation aborted.";
            }
        }
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
            sourceFile.delete();
            source.close();
            destination.close();
        } catch (Exception e) {
            source.close();
            destination.close();
            return e.getMessage();
        }
        return "File successfully copied.";
    }

    private String deleteFile(String sourceStr) throws IOException {
        File sourceFile = new File(sourceStr);
        if (!sourceFile.exists()) {
            return "File does not exist.";
        }
        if (getInput("Delete file \"" + sourceStr + "\"? (y/n)").equals("y")) {
            sourceFile.delete();
            return "File deleted.";
        }
        return "Delete aborted.";
    }

    //Returns current text contents of the system clipboard, or a failure message.
    private String getClipBoard() {
        try {
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(
                    DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException e) {
            return "There does not appear to be any text in the clipboard";
        } catch (IOException e) {
            return "Could not retrieve clipboard contents - " + e.getMessage();
        } catch (IllegalStateException e) {
            return "Clipboard unavailable - " + e.getMessage();
        }
    }

    //Runs specified macro from pre-saved file
    private void runMacro(String macroname, String args[]) throws IOException {
        try {
            File f = new File(System.getProperty("user.home")
                    + System.getProperty("file.separator")
                    + "biIRC" + System.getProperty("file.separator")
                    + "Macros" + System.getProperty("file.separator")
                    + macroname + ".xml");
            Macro macro = Macro.loadFromFile(f);
            String syntax = macro.getSyntax();
            ArrayList<String> argList = macro.getArgs();
            ArrayList<String> cmdList = macro.getCommands();
            String arguments[] = argList.toArray(new String[0]);
            if (args.length != arguments.length) {
                sendMessage(getCurrentAuthorizedNick(), "Usage - macro " + syntax);
                return;
            }
            for (String cmd : cmdList) {
                for (int j = 0; j < arguments.length; j++) {
                    cmd = cmd.replaceAll("%" + arguments[j] + "%", args[j]);
                }
                processPrivMsg(cmd);
            }
        } catch (FileNotFoundException e) {
            sendMessage(getCurrentAuthorizedNick(), "Macro file not found.");
        } catch (IOException e) {
            sendMessage(getCurrentAuthorizedNick(),
                    "Error reading from macro file - " + e.getMessage());
        } catch (Exception e) {
            mVikingWindow.appendErrorToConsole(e.getMessage());
        }
    }

    private void echo(String text) throws IOException {
        sendMessage(getCurrentAuthorizedNick(), text);
    }

    //Sleeps for specified amount of time. For use with macros.
    private void sleep(String timeMillis) throws IOException {
        try {
            int millis = Integer.parseInt(timeMillis);
            Thread.sleep(millis);
        } catch (NumberFormatException e) {
            sendMessage(getCurrentAuthorizedNick(), mCommandsSyntax.get(CommandConstant.SLEEP));
        } catch (InterruptedException e) {
            sendMessage(getCurrentAuthorizedNick(), "Interrupted while sleeping - " + e.getMessage());
        }
    }

    //Dumps log to a file
    private void dumpLog() throws IOException {
        String log = mLog.toString();
        File f = new File(System.getProperty("user.home")
                + System.getProperty("file.separator")
                + "biIRC" + System.getProperty("file.separator")
                + "Logs" + System.getProperty("file.separator")
                + new Date().toString() + ".txt");
        f.mkdirs();
        f.createNewFile();
        FileWriter fw = new FileWriter(f);
        fw.write(log);
        fw.flush();
        fw.close();
        sendMessage(getCurrentAuthorizedNick(), "Log successfully saved to " + f);
    }

    //Sends link to a help file
    private void sendHelpText() throws IOException {
        sendMessage(getCurrentAuthorizedNick(), "<help file link>");
    }

    private String getInput(String msg) throws IOException {
        sendMessage(getCurrentAuthorizedNick(), msg);
        final StringBuilder wrapper = new StringBuilder();
        MessageListener prevListener;
        synchronized (mMessageListenerLock) {
            prevListener = mCurrentMessageListener;
            mCurrentMessageListener = new MessageListener() {

                @Override
                public void processMessage(String msg) {
                    wrapper.append(msg);
                }
            };
        }
        String line;
        try {
            while (wrapper.toString().equals("") && (line = mServerReader.readLine()) != null) {
                try {
                    processServerMessage(line);
                } catch (Exception e) {
                    System.out.println(line);
                    e.printStackTrace(System.out);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        synchronized (mMessageListenerLock) {
            mCurrentMessageListener = prevListener;
        }
        return wrapper.toString();
    }

    //Convenience method to send a PRIVMSG to a recepient
    private void sendMessage(String to, String msg) throws IOException {
        writeToServer(PRIVMSG + to
                + " :" + msg + CRLF);
    }

    //Looksup a keyword and returns its representative constant
    private CommandConstant lookup(String key) {
        CommandConstant i = (CommandConstant) mCommands.get(key);
        return i == null ? CommandConstant.NULL : i;
    }

    //Continuously monitor server inputstream for messages and process them
    @Override
    public void run() {
        String line;
        try {
            while (run && (line = mServerReader.readLine()) != null) {
                try {
                    processServerMessage(line);
                } catch (Exception e) {
                    System.out.println(line);
                    e.printStackTrace(System.out);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    //Create a new Viking and initialize()
    public static void main(String[] args) throws IOException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        (new Viking()).initialize();
    }
}

//Convenience class which echoes all read lines to a StringBuilder log
class LoggedBufferedReader extends BufferedReader {

    private VikingWindow mVW;
    private StringBuilder mLog;

    public LoggedBufferedReader(InputStreamReader inputstream, StringBuilder log, VikingWindow vw) {
        super(inputstream);
        mLog = log;
        mVW = vw;
    }

    @Override
    public String readLine() throws IOException {
        String ret = super.readLine();
        mLog.append("INCOMING----->").append(ret);
        mVW.appendIncomingToLog(ret);
        return ret;
    }
}

//Convenience class which echoes all written strings to a StringBuilder log
class LoggedBufferedWriter extends BufferedWriter {

    private VikingWindow mVW;
    private StringBuilder mLog;

    public LoggedBufferedWriter(OutputStreamWriter outputstream, StringBuilder log, VikingWindow vw) {
        super(outputstream);
        mLog = log;
        mVW = vw;
    }

    @Override
    public void write(String s) throws IOException {
        super.write(s);
        mLog.append("OUTGOING----->").append(s);
        mVW.appendOutgoingToLog(s);
    }
}

interface MessageListener {

    void processMessage(String msg);
}

/*
 * This class monitors user input through System.in It maintains a list of all
 * listeners (added with addUserInputListener()) and sends user input to all of
 * them. No longer used since biIRC now has a GUI.
 */
//class UserInputBroadcaster implements Runnable {
//
//    private ArrayList<UserInputListener> mListeners;
//
//    public UserInputBroadcaster() {
//        mListeners = new ArrayList();
//    }
//
//    void addUserInputListener(UserInputListener listener) {
//        synchronized (this) {
//            mListeners.add(listener);
//        }
//    }
//
//    void removeUserInputListener(UserInputListener listener) {
//        synchronized (this) {
//            mListeners.remove(listener);
//        }
//    }
//
//    public void start() {
//        new Thread(this).start();
//    }
//
//    @Override
//    public void run() {
//        try {
//            BufferedReader br = new BufferedReader(
//                    new InputStreamReader(System.in));
//            String line;
//            while ((line = br.readLine()) != null) {
//                UserInputListener currentListeners[];
//                synchronized (this) {
//                    currentListeners = mListeners.toArray(
//                            new UserInputListener[0]);
//                }
//                for (UserInputListener l : currentListeners) {
//                    l.userInputObtained(line);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace(System.out);
//        }
//    }
//}
//
////Interface for a listener to be used with a UserInputBroadcaster
//interface UserInputListener {
//
//    public void userInputObtained(String input);
//}