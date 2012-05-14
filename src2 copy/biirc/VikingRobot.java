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
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/*
 * This class maintains a java.awt.Robot to emulate keyboard/mouse input.
 * Viking calls methods in this class to perform keyboard/mouse commands.
 */
public class VikingRobot {

    //Our Robot
    private Robot robot;
    //Stores a mapping of characters and special key names to their KeyCodes.
    private static final HashMap<Object, Integer> mKeyboardTable = new HashMap();
    //Stores a mapping of mouse button names to their button masks.
    private static final HashMap<String, Integer> mMouseTable = new HashMap();
    //List of currently pressed keys, for use with releasePressedKeys()
    private ArrayList<Integer> mPressedKeyCodes = new ArrayList();

    //Set up our tables
    static {
        mKeyboardTable.put('A', KeyEvent.VK_A);
        mKeyboardTable.put('B', KeyEvent.VK_B);
        mKeyboardTable.put('C', KeyEvent.VK_C);
        mKeyboardTable.put('D', KeyEvent.VK_D);
        mKeyboardTable.put('E', KeyEvent.VK_E);
        mKeyboardTable.put('F', KeyEvent.VK_F);
        mKeyboardTable.put('G', KeyEvent.VK_G);
        mKeyboardTable.put('H', KeyEvent.VK_H);
        mKeyboardTable.put('I', KeyEvent.VK_I);
        mKeyboardTable.put('J', KeyEvent.VK_J);
        mKeyboardTable.put('K', KeyEvent.VK_K);
        mKeyboardTable.put('L', KeyEvent.VK_L);
        mKeyboardTable.put('M', KeyEvent.VK_M);
        mKeyboardTable.put('N', KeyEvent.VK_N);
        mKeyboardTable.put('O', KeyEvent.VK_O);
        mKeyboardTable.put('P', KeyEvent.VK_P);
        mKeyboardTable.put('Q', KeyEvent.VK_Q);
        mKeyboardTable.put('R', KeyEvent.VK_R);
        mKeyboardTable.put('S', KeyEvent.VK_S);
        mKeyboardTable.put('T', KeyEvent.VK_T);
        mKeyboardTable.put('U', KeyEvent.VK_U);
        mKeyboardTable.put('V', KeyEvent.VK_V);
        mKeyboardTable.put('W', KeyEvent.VK_W);
        mKeyboardTable.put('X', KeyEvent.VK_X);
        mKeyboardTable.put('Y', KeyEvent.VK_Y);
        mKeyboardTable.put('Z', KeyEvent.VK_Z);
        mKeyboardTable.put('0', KeyEvent.VK_0);
        mKeyboardTable.put('1', KeyEvent.VK_1);
        mKeyboardTable.put('2', KeyEvent.VK_2);
        mKeyboardTable.put('3', KeyEvent.VK_3);
        mKeyboardTable.put('4', KeyEvent.VK_4);
        mKeyboardTable.put('5', KeyEvent.VK_5);
        mKeyboardTable.put('6', KeyEvent.VK_6);
        mKeyboardTable.put('7', KeyEvent.VK_7);
        mKeyboardTable.put('8', KeyEvent.VK_8);
        mKeyboardTable.put('9', KeyEvent.VK_9);
        mKeyboardTable.put('!', KeyEvent.VK_EXCLAMATION_MARK);
        mKeyboardTable.put('@', KeyEvent.VK_AT);
        mKeyboardTable.put('#', KeyEvent.VK_NUMBER_SIGN);
        mKeyboardTable.put('$', KeyEvent.VK_DOLLAR);
        mKeyboardTable.put('^', KeyEvent.VK_CIRCUMFLEX);
        mKeyboardTable.put('&', KeyEvent.VK_AMPERSAND);
        mKeyboardTable.put('*', KeyEvent.VK_ASTERISK);
        mKeyboardTable.put('(', KeyEvent.VK_LEFT_PARENTHESIS);
        mKeyboardTable.put(')', KeyEvent.VK_RIGHT_PARENTHESIS);
        mKeyboardTable.put(' ', KeyEvent.VK_SPACE);
        mKeyboardTable.put('_', KeyEvent.VK_UNDERSCORE);
        mKeyboardTable.put('-', KeyEvent.VK_MINUS);
        mKeyboardTable.put('+', KeyEvent.VK_PLUS);
        mKeyboardTable.put('=', KeyEvent.VK_EQUALS);
        mKeyboardTable.put('[', KeyEvent.VK_OPEN_BRACKET);
        mKeyboardTable.put('{', KeyEvent.VK_BRACELEFT);
        mKeyboardTable.put(']', KeyEvent.VK_CLOSE_BRACKET);
        mKeyboardTable.put('}', KeyEvent.VK_BRACERIGHT);
        mKeyboardTable.put(';', KeyEvent.VK_SEMICOLON);
        mKeyboardTable.put('"', KeyEvent.VK_QUOTEDBL);
        mKeyboardTable.put('\'', KeyEvent.VK_QUOTE);
        mKeyboardTable.put('<', KeyEvent.VK_LESS);
        mKeyboardTable.put(',', KeyEvent.VK_COMMA);
        mKeyboardTable.put('>', KeyEvent.VK_GREATER);
        mKeyboardTable.put('.', KeyEvent.VK_PERIOD);
        mKeyboardTable.put('/', KeyEvent.VK_SLASH);
        mKeyboardTable.put('\\', KeyEvent.VK_BACK_SLASH);
        mKeyboardTable.put('`', KeyEvent.VK_BACK_QUOTE);
        mKeyboardTable.put('\n', KeyEvent.VK_ENTER);
        mKeyboardTable.put('\t', KeyEvent.VK_TAB);
        mKeyboardTable.put("TAB", KeyEvent.VK_TAB);
        mKeyboardTable.put("CONTROL", KeyEvent.VK_CONTROL);
        mKeyboardTable.put("SHIFT", KeyEvent.VK_SHIFT);
        mKeyboardTable.put("ALT", KeyEvent.VK_ALT);
        mKeyboardTable.put("OPTION", KeyEvent.VK_ALT);
        mKeyboardTable.put("COMMAND", KeyEvent.VK_META);
        mKeyboardTable.put("SPACE", KeyEvent.VK_SPACE);
        mKeyboardTable.put("ENTER", KeyEvent.VK_ENTER);
        mKeyboardTable.put("RETURN", KeyEvent.VK_ENTER);
        mKeyboardTable.put("ESCAPE", KeyEvent.VK_ESCAPE);
        mKeyboardTable.put("F1", KeyEvent.VK_F1);
        mKeyboardTable.put("F2", KeyEvent.VK_F2);
        mKeyboardTable.put("F3", KeyEvent.VK_F3);
        mKeyboardTable.put("F4", KeyEvent.VK_F4);
        mKeyboardTable.put("F5", KeyEvent.VK_F5);
        mKeyboardTable.put("F6", KeyEvent.VK_F6);
        mKeyboardTable.put("F7", KeyEvent.VK_F7);
        mKeyboardTable.put("F8", KeyEvent.VK_F8);
        mKeyboardTable.put("F9", KeyEvent.VK_F9);
        mKeyboardTable.put("F10", KeyEvent.VK_F10);
        mKeyboardTable.put("F11", KeyEvent.VK_F11);
        mKeyboardTable.put("F12", KeyEvent.VK_F12);
        mKeyboardTable.put("BACKSPACE", KeyEvent.VK_BACK_SPACE);
        mKeyboardTable.put("DELETE", KeyEvent.VK_DELETE);
        mKeyboardTable.put("HOME", KeyEvent.VK_HOME);
        mKeyboardTable.put("CAPSLOCK", KeyEvent.VK_CAPS_LOCK);
        mKeyboardTable.put("UP", KeyEvent.VK_UP);
        mKeyboardTable.put("DOWN", KeyEvent.VK_DOWN);
        mKeyboardTable.put("LEFT", KeyEvent.VK_LEFT);
        mKeyboardTable.put("RIGHT", KeyEvent.VK_RIGHT);
        mKeyboardTable.put("PAGEUP", KeyEvent.VK_PAGE_UP);
        mKeyboardTable.put("PAGEDOWN", KeyEvent.VK_PAGE_DOWN);
        mKeyboardTable.put("END", KeyEvent.VK_END);
        mKeyboardTable.put("PRINTSCREEN", KeyEvent.VK_PRINTSCREEN);
        mKeyboardTable.put("INSERT", KeyEvent.VK_INSERT);
        mKeyboardTable.put("NUMLOCK", KeyEvent.VK_NUM_LOCK);
        mKeyboardTable.put("SCROLLLOCK", KeyEvent.VK_SCROLL_LOCK);
        mKeyboardTable.put("WINDOWS", KeyEvent.VK_WINDOWS);

        mMouseTable.put("LEFT", MouseEvent.BUTTON1_MASK);
        mMouseTable.put("MIDDLE", MouseEvent.BUTTON2_MASK);
        mMouseTable.put("RIGHT", MouseEvent.BUTTON3_MASK);
    }

    //Initializes the Robot
    public VikingRobot() throws AWTException {
        robot = new Robot();
    }

    /*
     * Press and release a key combination.
     * This is equivalent to successively call holdKeyCombo(false) and releaseKeyCombo(false)
     * Returns a success or failure. */
    boolean pressKeyCombo(String keyCombo) {
        if (!validateKeyCombo(keyCombo))
            return false;
        holdKeyCombo(keyCombo, true);
        releaseKeyCombo(keyCombo, true);
        return true;
    }

    /* Press and hold key combination.
     * If validated is true, it assumes keyCombo is a valid keyStroke.
     * Otherwise checks it.
     * Returns a success or failure. */
    boolean holdKeyCombo(String keyCombo, boolean validated) {
        if (!validated)
            if (!validateKeyCombo(keyCombo))
                return false;
        StringTokenizer st = new StringTokenizer(keyCombo, "+");
        while (st.hasMoreTokens()) {
            Integer keyCode;
            String tok = st.nextToken();
            if (tok.length() == 1)
                keyCode = mKeyboardTable.get(tok.toUpperCase().charAt(0));
            else
                keyCode = mKeyboardTable.get(tok.toUpperCase());
            robot.keyPress(keyCode);
            mPressedKeyCodes.add(keyCode);
        }
        return true;
    }

    /* Release key combination.
     * If validated is true, it assumes keyCombo is a valid keyStroke.
     * Otherwise checks it.
     * Returns a success or failure. */
    boolean releaseKeyCombo(String keyCombo, boolean validated) {
        if (!validated)
            if (!validateKeyCombo(keyCombo))
                return false;
        StringTokenizer st = new StringTokenizer(keyCombo, "+");
        while (st.hasMoreTokens()) {
            Integer keyCode;
            String tok = st.nextToken();
            if (tok.length() == 1)
                keyCode = mKeyboardTable.get(tok.toUpperCase().charAt(0));
            else
                keyCode = mKeyboardTable.get(tok.toUpperCase());
            robot.keyRelease(keyCode);
            mPressedKeyCodes.remove(keyCode);
        }
        return true;
    }

    //Releases currently pressed keys.
    void releasePressedKeys() {
        Integer keyCodes[] = mPressedKeyCodes.toArray(new Integer[0]);
        for (Integer k : keyCodes) {
            robot.keyPress(k);
        }
        mPressedKeyCodes.clear();
    }

    //Checks if a keyCombo is a valid keystroke in the format key1+key2+...
    boolean validateKeyCombo(String keyCombo) {
        StringTokenizer st = new StringTokenizer(keyCombo, "+");
        while (st.hasMoreTokens()) {
            Integer keyCode;
            String tok = st.nextToken();
            if (tok.length() == 1)
                keyCode = mKeyboardTable.get(tok.toUpperCase().charAt(0));
            else
                keyCode = mKeyboardTable.get(tok.toUpperCase());
            if (keyCode == null)
                return false;
        }
        return true;
    }

    //Moves mouse to specified coordinates and returns new location.
    Point moveMouse(int x, int y) {
        robot.mouseMove(x, y);
        return MouseInfo.getPointerInfo().getLocation();
    }

    //Moves mouse relatively by specified amounts and returns new location.
    Point moveMouseRel(int relx, int rely) {
        Point mousePos = MouseInfo.getPointerInfo().getLocation();
        robot.mouseMove(mousePos.x + relx, mousePos.y + rely);
        return MouseInfo.getPointerInfo().getLocation();
    }

    /* Clicks specified mouse button.
     * Returns true on success, and false if the button is not valid
     */
    boolean mouseClick(String buttonStr) {
        buttonStr = buttonStr.toUpperCase();
        Integer button = mMouseTable.get(buttonStr);
        if (button == null)
            return false;
        //Ensures correct location of mouse. Weird bug causes mouse to
        //move to last position set by a Robot before pressing a button.
        Point mousePos = MouseInfo.getPointerInfo().getLocation();
        robot.mouseMove(mousePos.x, mousePos.y);
        robot.mousePress(button);
        robot.mouseRelease(button);
        return true;
    }

    //Press and hold a mouse button.
    boolean mousePress(String buttonStr) {
        Integer button = mMouseTable.get(buttonStr);
        if (button == null)
            return false;
        robot.mousePress(button);
        return true;
    }

    //Release mouse button
    boolean mouseRelease(String buttonStr) {
        Integer button = mMouseTable.get(buttonStr);
        if (button == null)
            return false;
        robot.mouseRelease(button);
        return true;
    }

    //Types a string.
    void typeString(String s) {
        char c[] = s.toCharArray();
        for (int i = 0; i < c.length; i++) {
            char orig = c[i];
            char ch = Character.toUpperCase(orig);
            if (orig == '~') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_BACK_QUOTE);
                robot.keyRelease(KeyEvent.VK_BACK_QUOTE);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '!') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_1);
                robot.keyRelease(KeyEvent.VK_1);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '@') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_2);
                robot.keyRelease(KeyEvent.VK_2);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '#') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_3);
                robot.keyRelease(KeyEvent.VK_3);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '$') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_4);
                robot.keyRelease(KeyEvent.VK_4);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '%') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_5);
                robot.keyRelease(KeyEvent.VK_5);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '^') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_6);
                robot.keyRelease(KeyEvent.VK_6);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '&') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_7);
                robot.keyRelease(KeyEvent.VK_7);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '*') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_8);
                robot.keyRelease(KeyEvent.VK_8);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '(') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_9);
                robot.keyRelease(KeyEvent.VK_9);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == ')') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_0);
                robot.keyRelease(KeyEvent.VK_0);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '_') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_MINUS);
                robot.keyRelease(KeyEvent.VK_MINUS);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '+') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_EQUALS);
                robot.keyRelease(KeyEvent.VK_EQUALS);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '{') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_OPEN_BRACKET);
                robot.keyRelease(KeyEvent.VK_OPEN_BRACKET);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '}') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_CLOSE_BRACKET);
                robot.keyRelease(KeyEvent.VK_CLOSE_BRACKET);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '|') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_BACK_SLASH);
                robot.keyRelease(KeyEvent.VK_BACK_SLASH);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == ':') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_SEMICOLON);
                robot.keyRelease(KeyEvent.VK_SEMICOLON);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '"') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_QUOTE);
                robot.keyRelease(KeyEvent.VK_QUOTE);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '<') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_COMMA);
                robot.keyRelease(KeyEvent.VK_COMMA);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '>') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_PERIOD);
                robot.keyRelease(KeyEvent.VK_PERIOD);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '?') {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_SLASH);
                robot.keyRelease(KeyEvent.VK_SLASH);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else if (orig == '\r')
                continue;
            else {
                if (Character.isUpperCase(orig))
                    robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress((Integer) mKeyboardTable.get(ch));
                robot.keyRelease((Integer) mKeyboardTable.get(ch));
                robot.keyRelease(KeyEvent.VK_SHIFT);
            }
        }
    }
}