/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.media.control.mgcp.pkg.au;

import java.util.Map;

import org.mobicents.media.control.mgcp.pkg.AbstractMgcpSignal;
import org.mobicents.media.control.mgcp.pkg.SignalType;
import org.mobicents.media.server.spi.dtmf.DtmfDetector;
import org.mobicents.media.server.spi.player.Player;

import com.google.common.base.Optional;

/**
 * Plays a prompt and collects DTMF digits entered by a user.
 * 
 * <p>
 * If no digits are entered or an invalid digit pattern is entered, the user may be reprompted and given another chance to enter
 * a correct pattern of digits. The following digits are supported: 0-9, *, #, A, B, C, D.
 * </p>
 * 
 * <p>
 * By default PlayCollect does not play an initial prompt, makes only one attempt to collect digits, and therefore functions as
 * a simple Collect operation.<br>
 * Various special purpose keys, key sequences, and key sets can be defined for use during the PlayCollect operation.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class PlayCollect extends AbstractMgcpSignal {

    // Media Components
    private final Player player;
    private final DtmfDetector detector;

    // Playlists
    private final Playlist initialPrompt;
    private final Playlist reprompt;
    private final Playlist noDigitsReprompt;
    private final Playlist failureAnnouncement;
    private final Playlist successAnnouncement;

    private final boolean nonInterruptibleAudio;
    private final boolean clearDigitBuffer;
    private final int minDigits;
    private final int maxDigits;
    private final String digitPattern;
    private final int firstDigitTimer;
    private final int interDigitTimer;
    private final int extraDigitTimer;
    private final String restartKey;

    public PlayCollect(Player player, DtmfDetector detector, Map<String, String> parameters) {
        super(AudioPackage.PACKAGE_NAME, "pc", SignalType.TIME_OUT, parameters);

        // Media Components
        this.player = player;
        this.detector = detector;

        // Playlists
        this.initialPrompt = new Playlist(getInitialPrompt(), 1);
        this.reprompt = new Playlist(getReprompt(), 1);
        this.noDigitsReprompt = new Playlist(getNoDigitsReprompt(), 1);
        this.failureAnnouncement = new Playlist(getFailureAnnouncement(), 1);
        this.successAnnouncement = new Playlist(getSuccessAnnouncement(), 1);

        this.nonInterruptibleAudio = getNonInterruptibleAudio();
        this.clearDigitBuffer = getClearDigitBuffer();
        this.minDigits = getMinimumDigits();
        this.maxDigits = getMaximumDigits();
        this.digitPattern = getDigitPattern();
        this.firstDigitTimer = getFirstDigitTimer();
        this.interDigitTimer = getInterDigitTimer();
        this.extraDigitTimer = getExtraDigitTimer();
    }

    /**
     * The initial announcement prompting the user to either enter DTMF digits or to speak.
     * <p>
     * Consists of one or more audio segments.<br>
     * If not specified (the default), the event immediately begins digit collection or recording.
     * </p>
     * 
     * @return The array of audio prompts. Array will be empty if none is specified.
     */
    private String[] getInitialPrompt() {
        return Optional.fromNullable(getParameter(SignalParameters.INITIAL_PROMPT.symbol())).or("").split(",");
    }

    /**
     * Played after the user has made an error such as entering an invalid digit pattern or not speaking.
     * <p>
     * Consists of one or more audio segments. <b>Defaults to the Initial Prompt.</b>
     * </p>
     * 
     * @return The array of audio prompts. Array will be empty if none is specified.
     */
    private String[] getReprompt() {
        String segments = Optional.fromNullable(getParameter(SignalParameters.REPROMPT.symbol())).or("");
        if (segments.isEmpty()) {
            segments = Optional.fromNullable(getParameter(SignalParameters.INITIAL_PROMPT.symbol())).or("");
        }
        return segments.split(",");
    }

    /**
     * Played after the user has failed to enter a valid digit pattern during a PlayCollect event.
     * <p>
     * Consists of one or more audio segments. <b>Defaults to the Reprompt.</b>
     * </p>
     * 
     * @return The array of audio prompts. Array will be empty if none is specified.
     */
    private String[] getNoDigitsReprompt() {
        String segments = Optional.fromNullable(getParameter(SignalParameters.NO_DIGITS_REPROMPT.symbol())).or("");
        if (segments.isEmpty()) {
            segments = Optional.fromNullable(getParameter(SignalParameters.REPROMPT.symbol())).or("");
        }
        return segments.split(",");
    }

    /**
     * Played when all data entry attempts have failed.
     * <p>
     * Consists of one or more audio segments. No default.
     * </p>
     * 
     * @return The array of audio prompts. Array will be empty if none is specified.
     */
    private String[] getFailureAnnouncement() {
        return Optional.fromNullable(getParameter(SignalParameters.FAILURE_ANNOUNCEMENT.symbol())).or("").split(",");
    }

    /**
     * Played when all data entry attempts have succeeded.
     * <p>
     * Consists of one or more audio segments. No default.
     * </p>
     * 
     * @return The array of audio prompts. Array will be empty if none is specified.
     */
    private String[] getSuccessAnnouncement() {
        return Optional.fromNullable(getParameter(SignalParameters.FAILURE_ANNOUNCEMENT.symbol())).or("").split(",");
    }

    /**
     * If set to true, initial prompt is not interruptible by either voice or digits.
     * <p>
     * <b>Defaults to false.</b> Valid values are the text strings "true" and "false".
     * </p>
     * 
     * @return
     */
    private boolean getNonInterruptibleAudio() {
        String value = Optional.fromNullable(getParameter(SignalParameters.NON_INTERRUPTIBLE_PLAY.symbol())).or("false");
        return Boolean.parseBoolean(value);
    }

    /**
     * If set to true, clears the digit buffer before playing the initial prompt.
     * <p>
     * <b>Defaults to false.</b> Valid values are the text strings "true" and "false".
     * </p>
     * 
     * @return
     */
    private boolean getClearDigitBuffer() {
        String value = Optional.fromNullable(getParameter(SignalParameters.CLEAR_DIGIT_BUFFER.symbol())).or("false");
        return Boolean.parseBoolean(value);
    }

    /**
     * The minimum number of digits to collect.
     * <p>
     * <b>Defaults to one.</b> This parameter should not be specified if the Digit Pattern parameter is present.
     * </p>
     * 
     * @return
     */
    private int getMinimumDigits() {
        String value = Optional.fromNullable(getParameter(SignalParameters.MINIMUM_NUM_DIGITS.symbol())).or("1");
        return Integer.parseInt(value);
    }

    /**
     * The maximum number of digits to collect.
     * <p>
     * <b>Defaults to one.</b> This parameter should not be specified if the Digit Pattern parameter is present.
     * </p>
     * 
     * @return
     */
    private int getMaximumDigits() {
        String value = Optional.fromNullable(getParameter(SignalParameters.MAXIMUM_NUM_DIGITS.symbol())).or("1");
        return Integer.parseInt(value);
    }

    /**
     * A legal digit map as described in section 7.1.14 of the Megaco protocol using the DTMF mappings associated with the
     * Megaco DTMF Detection Package described in the Megaco protocol document.
     * <p>
     * <b>This parameter should not be specified if one or both of the Minimum # Of Digits parameter and the Maximum Number Of
     * Digits parameter is present.</b>
     * </p>
     * 
     * @return The digit pattern or an empty String if not specified.
     */
    private String getDigitPattern() {
        return Optional.fromNullable(getParameter(SignalParameters.DIGIT_PATTERN.symbol())).or("");
    }

    /**
     * The amount of time allowed for the user to enter the first digit.
     * <p>
     * Specified in units of 100 milliseconds. <b>Defaults to 50 (5 seconds).</b>
     * </p>
     * 
     * @return
     */
    private int getFirstDigitTimer() {
        String value = Optional.fromNullable(getParameter(SignalParameters.FIRST_DIGIT_TIMER.symbol())).or("50");
        return Integer.parseInt(value);
    }

    /**
     * The amount of time allowed for the user to enter each subsequent digit.
     * <p>
     * Specified units of 100 milliseconds seconds. <b>Defaults to 30 (3 seconds).</b>
     * </p>
     * 
     * @return
     */
    private int getInterDigitTimer() {
        String value = Optional.fromNullable(getParameter(SignalParameters.INTER_DIGIT_TIMER.symbol())).or("30");
        return Integer.parseInt(value);
    }

    /**
     * The amount of time to wait for a user to enter a final digit once the maximum expected amount of digits have been
     * entered.
     * <p>
     * Typically this timer is used to wait for a terminating key in applications where a specific key has been defined to
     * terminate input.
     * </p>
     * <p>
     * Specified in units of 100 milliseconds. </b>If not specified, this timer is not activated.</b>
     * </p>
     * 
     * @return
     */
    private int getExtraDigitTimer() {
        String value = Optional.fromNullable(getParameter(SignalParameters.EXTRA_DIGIT_TIMER.symbol())).or("");
        return Integer.parseInt(value);
    }

    /**
     * Defines a key sequence consisting of a command key optionally followed by zero or more keys. This key sequence has the
     * following action: discard any digits collected or recording in progress, replay the prompt, and resume digit collection
     * or recording.
     * <p>
     * <b>No default.</b> An application that defines more than one command key sequence, will typically use the same command
     * key for all command key sequences.
     * <p>
     * If more than one command key sequence is defined, then all key sequences must consist of a command key plus at least one
     * other key.
     * </p>
     * 
     * @return
     */
    private String getRestartKey() {
        return Optional.fromNullable(getParameter(SignalParameters.RESTART_KEY.symbol())).or("");
    }

    /**
     * Defines a key sequence consisting of a command key optionally followed by zero or more keys. This key sequence has the
     * following action: discard any digits collected or recordings in progress and resume digit collection or recording.
     * <p>
     * <b>No default.</b>
     * </p>
     * An application that defines more than one command key sequence, will typically use the same command key for all command
     * key sequences.
     * </p>
     * <p>
     * If more than one command key sequence is defined, then all key sequences must consist of a command key plus at least one
     * other key.
     * </p>
     * 
     * @return
     */
    private String getReinputKey() {
        return Optional.fromNullable(getParameter(SignalParameters.REINPUT_KEY.symbol())).or("");
    }

    /**
     * Defines a key sequence consisting of a command key optionally followed by zero or more keys. This key sequence has the
     * following action: terminate the current event and any queued event and return the terminating key sequence to the call
     * processing agent.
     * <p>
     * <b> No default.</b> An application that defines more than one command key sequence, will typically use the same command
     * key for all command key sequences.
     * <p>
     * If more than one command key sequence is defined, then all key sequences must consist of a command key plus at least one
     * other key.
     * </p>
     * 
     * @return
     */
    private String getReturnKey() {
        return Optional.fromNullable(getParameter(SignalParameters.RETURN_KEY.symbol())).or("");
    }

    /**
     * Defines a key with the following action. Stop playing the current announcement and resume playing at the beginning of the
     * first, last, previous, next, or the current segment of the announcement.
     * <p>
     * <b>No default. The actions for the position key are fst, lst, prv, nxt, and cur.</b>
     * </p>
     * 
     * @return
     */
    private String getPositionKey() {
        return Optional.fromNullable(getParameter(SignalParameters.POSITION_KEY.symbol())).or("");
    }

    /**
     * Defines a key with the following action. Terminate playback of the announcement.
     * <p>
     * <b>No default.</b>
     * </p>
     * 
     * @return
     */
    private String getStopKey() {
        return Optional.fromNullable(getParameter(SignalParameters.STOP_KEY.symbol())).or("");
    }

    /**
     * Defines a set of keys that are acceptable as the first digit collected. This set of keys can be specified to interrupt a
     * playing announcement or to not interrupt a playing announcement.
     * <p>
     * <b>The default key set is 0-9. The default behavior is to interrupt a playing announcement when a Start Input Key is
     * pressed.</b>
     * </p>
     * <p>
     * This behavior can be overidden for the initial prompt only by using the ni (Non-Interruptible Play) parameter.
     * Specification is a list of keys with no separators, e.g. 123456789#.
     * </p>
     * 
     * @return
     */
    private String getStartInputKeys() {
        return Optional.fromNullable(getParameter(SignalParameters.START_INPUT_KEY.symbol())).or("0-9");
    }

    /**
     * Specifies a key that signals the end of digit collection or voice recording.
     * <p>
     * <b>The default end input key is the # key.</b> To specify that no End Input Key be used the parameter is set to the
     * string "null".
     * <p>
     * <b>The default behavior not to return the End Input Key in the digits returned to the call agent.</b> This behavior can
     * be overidden by the Include End Input Key (eik) parameter.
     * </p>
     * 
     * @return
     */
    private String getEndInputKey() {
        return Optional.fromNullable(getParameter(SignalParameters.END_INPUT_KEY.symbol())).or("#");
    }

    /**
     * By default the End Input Key is not included in the collected digits returned to the call agent. If this parameter is set
     * to "true" then the End Input Key will be returned with the collected digits returned to the call agent.
     * <p>
     * <b>Default is "false".</b>
     * </p>
     * 
     * @return
     */
    private boolean getIncludeEndInputKey() {
        String value = Optional.fromNullable(getParameter(SignalParameters.INCLUDE_END_INPUT_KEY.symbol())).or("false");
        return Boolean.parseBoolean(value);
    }

    /**
     * The number of attempts the user needed to enter a valid digit pattern or to make a recording.
     * <p>
     * <b>Defaults to 1.</b> Also used as a return parameter to indicate the number of attempts the user made.
     * </p>
     * 
     * @return
     */
    private int getNumberOfAttempts() {
        String value = Optional.fromNullable(getParameter(SignalParameters.NUMBER_OF_ATTEMPTS.symbol())).or("1");
        return Integer.parseInt(value);
    }

    @Override
    protected boolean isParameterSupported(String name) {
        // Check if parameter is valid
        SignalParameters parameter = SignalParameters.fromSymbol(name);
        if (parameter == null) {
            return false;
        }

        // Check if parameter is supported
        switch (parameter) {
            case INITIAL_PROMPT:
            case REPROMPT:
            case NO_DIGITS_REPROMPT:
            case FAILURE_ANNOUNCEMENT:
            case SUCCESS_ANNOUNCEMENT:
            case NON_INTERRUPTIBLE_PLAY:
            case SPEED:
            case VOLUME:
            case CLEAR_DIGIT_BUFFER:
            case MAXIMUM_NUM_DIGITS:
            case MINIMUM_NUM_DIGITS:
            case DIGIT_PATTERN:
            case FIRST_DIGIT_TIMER:
            case INTER_DIGIT_TIMER:
            case EXTRA_DIGIT_TIMER:
            case RESTART_KEY:
            case REINPUT_KEY:
            case RETURN_KEY:
            case POSITION_KEY:
            case STOP_KEY:
            case START_INPUT_KEY:
            case END_INPUT_KEY:
            case INCLUDE_END_INPUT_KEY:
            case NUMBER_OF_ATTEMPTS:
                return true;

            default:
                return false;
        }
    }

    @Override
    public void execute() {
        // TODO Auto-generated method stub

    }

    @Override
    public void cancel() {
        // TODO Auto-generated method stub

    }

}
