/*
 * Copyright 2013-2015 Skynav, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY SKYNAV, INC. AND ITS CONTRIBUTORS “AS IS” AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SKYNAV, INC. OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.skynav.ttv.verifier.util;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Locator;

import com.skynav.ttv.model.Model;
import com.skynav.ttv.model.value.ClockMode;
import com.skynav.ttv.model.value.ClockTime;
import com.skynav.ttv.model.value.OffsetTime;
import com.skynav.ttv.model.value.Time;
import com.skynav.ttv.model.value.TimeBase;
import com.skynav.ttv.model.value.TimeParameters;
import com.skynav.ttv.model.value.WallClockTime;
import com.skynav.ttv.model.value.impl.ClockTimeImpl;
import com.skynav.ttv.model.value.impl.OffsetTimeImpl;
import com.skynav.ttv.model.value.impl.WallClockTimeImpl;
import com.skynav.ttv.util.Location;
import com.skynav.ttv.util.Message;
import com.skynav.ttv.util.Reporter;
import com.skynav.ttv.verifier.VerifierContext;

public class Timing {

    public static boolean isCoordinate(String value, Location location, VerifierContext context, TimeParameters timeParameters, Time[] outputTime) {
        if (isClockTime(value, location, context, timeParameters, outputTime))
            return true;
        else if (isOffsetTime(value, location, context, timeParameters, outputTime))
            return true;
        else if (isWallClockTime(value, location, context, timeParameters, outputTime)) {
            if ((context == null) || context.getModel().isTTMLVersion(2))
                return true;
            else
                return false;
        } else
            return false;
    }

    public static void badCoordinate(String value, Location location, VerifierContext context, TimeParameters timeParameters) {
        if (maybeWallClockTime(value, location, context, timeParameters)) {
            Model model = context.getModel();
            if (model.isTTMLVersion(2))
                badWallClockTime(value, location, context, timeParameters);
            else {
                Reporter reporter = context.getReporter();
                reporter.logInfo(reporter.message(location.getLocator(), "*KEY*",
                    "Bad <timeExpression>, wall clock time not supported in TTML{0}.", model.getTTMLVersion()));
            }
        } else if (maybeClockTime(value, location, context, timeParameters))
            badClockTime(value, location, context, timeParameters);
        else
            badOffsetTime(value, location, context, timeParameters);
    }

    private static boolean maybeClockTime(String value, Location location, VerifierContext context, TimeParameters timeParameters) {
        return value.indexOf(':') >= 0;
    }

    public static boolean isDuration(String value, Location location, VerifierContext context, TimeParameters timeParameters, Time[] outputTime) {
        return isCoordinate(value, location, context, timeParameters, outputTime);
    }

    public static void badDuration(String value, Location location, VerifierContext context, TimeParameters timeParameters) {
        badCoordinate(value, location, context, timeParameters);
    }

    private static final Pattern clockTimePattern = Pattern.compile("(\\d{2,}):(\\d{2}):(\\d{2})(\\.\\d+|:\\d{2,}(?:\\.\\d+)?)?");
    public static boolean isClockTime(String value, Location location, VerifierContext context, TimeParameters timeParameters, Time[] outputTime) {
        Matcher m = clockTimePattern.matcher(value);
        if (m.matches()) {
            assert m.groupCount() >= 3;
            String hours = m.group(1);
            String minutes = m.group(2);
            String seconds = m.group(3);
            String frames = null;
            String subFrames = null;
            if (m.groupCount() > 3) {
                String remainder = m.group(4);
                if (remainder != null) {
                    if (remainder.indexOf(':') == 0) {
                        String[] parts = remainder.substring(1).split("\\.", 3);
                        if (parts.length > 0)
                            frames = parts[0];
                        if (parts.length > 1)
                            subFrames = parts[1];
                    } else
                        seconds += remainder;
                }
            }
            if ((timeParameters.getTimeBase() == TimeBase.CLOCK) && ((frames != null) || (subFrames != null)))
                return false;
            ClockTime t = new ClockTimeImpl(hours, minutes, seconds, frames, subFrames);
            if (t.getMinutes() > 59)
                return false;
            if (t.getSeconds() > 60.0)
                return false;
            if (t.getFrames() >= timeParameters.getFrameRate())
                return false;
            if (t.getSubFrames() >= timeParameters.getSubFrameRate())
                return false;
            if (outputTime != null)
                outputTime[0] = t;
            if (location != null) {
                if (frames != null)
                    updateUsage(context, location, OffsetTime.Metric.Frames);
            }
            return true;
        } else
            return false;
    }

    public static void badClockTime(String value, Location location, VerifierContext context, TimeParameters timeParameters) {
        Reporter reporter = context.getReporter();
        Locator locator = location.getLocator();
        assert value.indexOf(':') >= 0;
        String[] parts = value.split("\\:", 5);
        int numParts = parts.length;
        if (numParts > 0) {
            String hh = parts[0];
            if (hh.length() == 0)
                reporter.logInfo(reporter.message(locator, "*KEY*", "Bad <timeExpression>, hours part is empty in clock time."));
            else if (!Strings.isDigits(hh))
                reporter.logInfo(reporter.message(locator, "*KEY*", "Bad <timeExpression>, hours part ''{0}'' contains non-digit character in clock time.", hh));
            else if (hh.length() < 2)
                reporter.logInfo(reporter.message(locator, "*KEY*", "Bad <timeExpression>, hours part must contain two or more digits in clock time."));
        } else {
            reporter.logInfo(reporter.message(locator, "*KEY*", "Bad <timeExpression>, empty expression."));
        }
        if (numParts > 1) {
            String mm = parts[1];
            if (mm.length() == 0)
                reporter.logInfo(reporter.message(locator, "*KEY*", "Bad <timeExpression>, minutes part is empty in clock time."));
            else if (!Strings.isDigits(mm))
                reporter.logInfo(reporter.message(locator, "*KEY*", "Bad <timeExpression>, minutes part ''{0}'' contains non-digit character in clock time.", mm));
            else if (mm.length() < 2)
                reporter.logInfo(reporter.message(locator, "*KEY*", "Bad <timeExpression>, minutes part is missing digit(s), must contain two digits in clock time."));
            else if (mm.length() > 2)
                reporter.logInfo(reporter.message(locator, "*KEY*", "Bad <timeExpression>, minutes part contains extra digit(s), must contain two digits in clock time."));
            else if (Integer.parseInt(mm) >= 60)
                reporter.logInfo(reporter.message(locator, "*KEY*", "Bad <timeExpression>, minutes ''{0}'' must be less than 60.", mm));
        } else {
            reporter.logInfo(reporter.message(locator, "*KEY*", "Bad <timeExpression>, missing minutes and seconds parts in clock time."));
        }
        if (numParts > 2) {
            String ss = parts[2];
            if (ss.length() == 0)
                reporter.logInfo(reporter.message(locator, "*KEY*", "Bad <timeExpression>, seconds part is empty in clock time."));
            else if (Strings.containsDecimalSeparator(ss)) {
                String[] subParts = ss.split("\\.", 3);
                if (subParts.length > 0) {
                    String w = subParts[0];
                    if (w.length() == 0) {
                        reporter.logInfo(reporter.message(locator, "*KEY*",
                            "Bad <timeExpression>, seconds part whole sub-part is empty in clock time."));
                    } else if (!Strings.isDigits(w)) {
                        reporter.logInfo(reporter.message(locator, "*KEY*",
                            "Bad <timeExpression>, seconds part whole sub-part ''{0}'' contains non-digit character in clock time.", w));
                    } else if (w.length() < 2) {
                        reporter.logInfo(reporter.message(locator, "*KEY*",
                            "Bad <timeExpression>, seconds part is missing digit(s), must contain two digits in clock time."));
                    } else if (w.length() > 2) {
                        reporter.logInfo(reporter.message(locator, "*KEY*",
                            "Bad <timeExpression>, seconds part contains extra digit(s), must contain two digits in clock time."));
                    }
                }
                if (subParts.length > 1) {
                    String f = subParts[1];
                    if (f.length() == 0) {
                        reporter.logInfo(reporter.message(locator, "*KEY*",
                            "Bad <timeExpression>, seconds part fraction sub-part is empty in clock time."));
                    } else if (!Strings.isDigits(f)) {
                        reporter.logInfo(reporter.message(locator, "*KEY*",
                            "Bad <timeExpression>, seconds part fraction sub-part ''{0}'' contains non-digit character in clock time.", f));
                    }
                }
                if (subParts.length == 2) {
                    String w = subParts[0];
                    String f = subParts[1];
                    if (Strings.isDigits(w) && Strings.isDigits(f)) {
                        if (Double.parseDouble(ss) > 60.0) {
                            reporter.logInfo(reporter.message(locator, "*KEY*", "Bad <timeExpression>, seconds ''{0}'' must be less than 60.0.", ss));
                        }
                    }
                } else if (subParts.length > 2) {
                    StringBuffer sb = new StringBuffer();
                    for (int i = 2, n = subParts.length; i < n; ++i) {
                        sb.append('.');
                        sb.append(subParts[i]);
                    }
                    reporter.logInfo(reporter.message(locator, "*KEY*", "Bad <timeExpression>, seconds part contains extra sub-parts ''{0}''.", sb.toString()));
                }
            } else if (!Strings.isDigits(ss)) {
                reporter.logInfo(reporter.message(locator, "*KEY*",
                    "Bad <timeExpression>, seconds part ''{0}'' contains unexpected character (not digit or decimal separator) in clock time.", ss));
            } else if (ss.length() < 2) {
                reporter.logInfo(reporter.message(locator, "*KEY*",
                    "Bad <timeExpression>, seconds part is missing digit(s), must contain two digits in clock time."));
            } else if (ss.length() > 2) {
                reporter.logInfo(reporter.message(locator, "*KEY*",
                    "Bad <timeExpression>, seconds part contains extra digit(s), must contain two digits in clock time."));
            } else if (Integer.parseInt(ss) > 60) {
                reporter.logInfo(reporter.message(locator, "*KEY*",
                    "Bad <timeExpression>, seconds ''{0}'' must be less than 60.0.", ss));
            }
        } else {
            reporter.logInfo(reporter.message(locator, "*KEY*", "Bad <timeExpression>, missing seconds part in clock time."));
        }
        if (numParts > 3) {
            String ff = parts[3];
            int frames = 0;
            int subFrames = 0;
            if (ff.length() == 0)
                reporter.logInfo(reporter.message(locator, "*KEY*", "Bad <timeExpression>, frames part is empty in clock time."));
            else if (Strings.containsDecimalSeparator(ff)) {
                String[] subParts = ff.split("\\.", 3);
                if (subParts.length > 0) {
                    String w = subParts[0];
                    if (w.length() == 0) {
                        reporter.logInfo(reporter.message(locator, "*KEY*",
                            "Bad <timeExpression>, frames part whole sub-part is empty in clock time."));
                    } else if (!Strings.isDigits(w)) {
                        reporter.logInfo(reporter.message(locator, "*KEY*",
                            "Bad <timeExpression>, frames part whole sub-part ''{0}'' contains non-digit character in clock time.", w));
                    } else if (w.length() < 2) {
                        reporter.logInfo(reporter.message(locator, "*KEY*",
                            "Bad <timeExpression>, frames part whole sub-part is missing digit(s), must contain two or more digits in clock time."));
                    }
                }
                if (subParts.length > 1) {
                    String f = subParts[1];
                    if (f.length() == 0) {
                        reporter.logInfo(reporter.message(locator, "*KEY*",
                            "Bad <timeExpression>, frames part sub-frames sub-part is empty in clock time."));
                    } else if (!Strings.isDigits(f)) {
                        reporter.logInfo(reporter.message(locator, "*KEY*",
                            "Bad <timeExpression>, frames part sub-frames sub-part ''{0}'' contains non-digit character in clock time.", f));
                    }
                }
                if (subParts.length == 2) {
                    String w = subParts[0];
                    String f = subParts[1];
                    if (Strings.isDigits(w) && Strings.isDigits(f)) {
                        frames = Integer.parseInt(w);
                        subFrames = Integer.parseInt(f);
                    }
                } else if (subParts.length > 2) {
                    StringBuffer sb = new StringBuffer();
                    for (int i = 2, n = subParts.length; i < n; ++i) {
                        sb.append('.');
                        sb.append(subParts[i]);
                    }
                    reporter.logInfo(reporter.message(locator, "*KEY*",
                        "Bad <timeExpression>, frames part contains extra sub-parts ''{0}''.", sb.toString()));
                }
            } else if (!Strings.isDigits(ff)) {
                reporter.logInfo(reporter.message(locator, "*KEY*",
                    "Bad <timeExpression>, frames part ''{0}'' contains unexpected character (not digit or decimal separator) in clock time.", ff));
            } else if (ff.length() < 2) {
                reporter.logInfo(reporter.message(locator, "*KEY*",
                                                  "Bad <timeExpression>, frames part is missing digit(s), must contain two or more digits in clock time."));
            } else {
                frames = Integer.parseInt(ff);
            }
            if (ff.length() > 0) {
                if (timeParameters.getTimeBase() == TimeBase.CLOCK) {
                    reporter.logInfo(reporter.message(locator, "*KEY*",
                        "Bad <timeExpression>, frames part not permitted when using 'clock' time base."));
                }
                double frameRate = timeParameters.getFrameRate();
                if (frames >= frameRate) {
                    reporter.logInfo(reporter.message(locator, "*KEY*",
                        "Bad <timeExpression>, frames ''{0}'' must be less than frame rate {1}.", frames, frameRate));
                }
                double subFrameRate = timeParameters.getSubFrameRate();
                if (subFrames >= subFrameRate) {
                    reporter.logInfo(reporter.message(locator, "*KEY*",
                        "Bad <timeExpression>, sub-frames ''{0}'' must be less than sub-frame rate {1}.", subFrames, subFrameRate));
                }
            }
        }
        if (numParts > 4) {
            String uu = parts[4];
            if (uu.length() == 0) {
                reporter.logInfo(reporter.message(locator, "*KEY*",
                    "Bad <timeExpression>, unexpected empty part after seconds or frames part in clock time."));
            } else {
                reporter.logInfo(reporter.message(locator, "*KEY*",
                    "Bad <timeExpression>, unexpected part '':{0}'' after seconds or frames part in clock time.", uu));
            }
        }
    }

    private static final Pattern offsetTimePattern = Pattern.compile("(\\d+(?:\\.\\d+)?)(h|m|s|ms|f|t)");
    public static boolean isOffsetTime(String value, Location location, VerifierContext context, TimeParameters timeParameters, Time[] outputTime) {
        Matcher m = offsetTimePattern.matcher(value);
        if (m.matches()) {
            assert m.groupCount() == 2;
            String offset = m.group(1);
            String metric = m.group(2);
            OffsetTime t = new OffsetTimeImpl(offset, metric);
            if ((timeParameters.getTimeBase() == TimeBase.CLOCK) && (t.getMetric() == OffsetTime.Metric.Frames))
                return false;
            if (outputTime != null)
                outputTime[0] = t;
            if (location != null)
                updateUsage(context, location, t.getMetric());
            return true;
        } else
            return false;
    }

    private static void updateUsage(VerifierContext context, Location location, OffsetTime.Metric metric) {
        String key = "usage" + metric.name();
        @SuppressWarnings("unchecked")
        Set<Locator> usage = (Set<Locator>) context.getResourceState(key);
        if (usage == null) {
            usage = new java.util.HashSet<Locator>();
            context.setResourceState(key, usage);
        }
        usage.add(location.getLocator());
    }

    public static void badOffsetTime(String value, Location location, VerifierContext context, TimeParameters timeParameters) {
        Reporter reporter = context.getReporter();
        Locator locator = location.getLocator();
        int valueIndex = 0;
        int valueLength = value.length();
        char c;

        do {

            // whitespace before time count
            if (valueIndex == valueLength)
                break;
            c = value.charAt(valueIndex);
            if (Characters.isXMLSpace(c)) {
                while (Characters.isXMLSpace(c)) {
                    if (++valueIndex >= valueLength)
                        break;
                    c = value.charAt(valueIndex);
                }
                reporter.logInfo(reporter.message(locator, "*KEY*",
                    "Bad <timeExpression>, XML space padding not permitted before offset time."));
            }

            // time count (digit+)
            if (valueIndex == valueLength)
                break;
            c = value.charAt(valueIndex);
            if (Characters.isDigit(c)) {
                while (Characters.isDigit(c)) {
                    if (++valueIndex >= valueLength)
                        break;
                    c = value.charAt(valueIndex);
                }
            }

            // optional fraction (decimal separator)
            if (valueIndex == valueLength) {
                reporter.logInfo(reporter.message(locator, "*KEY*",
                    "Bad <timeExpression>, missing metric in integral offset time."));
                break;
            }
            if ((c != '.') && !Characters.isLetter(c)) {
                reporter.logInfo(reporter.message(locator, "*KEY*",
                    "Bad <timeExpression>, time count must contain digits followed by optional fraction then metric, got ''{0}'' in offset time.", c));
                break;
            }

            // optional fraction (digits)
            if (c == '.') {
                if (++valueIndex == valueLength) {
                    reporter.logInfo(reporter.message(locator, "*KEY*",
                        "Bad <timeExpression>, missing fraction part and metric."));
                    break;
                }
                c = value.charAt(valueIndex);
                if (Characters.isDigit(c)) {
                    while (Characters.isDigit(c)) {
                        if (++valueIndex >= valueLength)
                            break;
                        c = value.charAt(valueIndex);
                    }
                } else {
                    reporter.logInfo(reporter.message(locator, "*KEY*",
                        "Bad <timeExpression>, missing fraction part after decimal separator, must contain one or more digits."));
                    break;
                }
            }

            // metric
            if (valueIndex == valueLength) {
                reporter.logInfo(reporter.message(locator, "*KEY*", "Bad <timeExpression>, missing metric in non-integral offset time."));
                break;
            }
            StringBuffer sb = new StringBuffer();
            c = value.charAt(valueIndex);
            while (Characters.isLetter(c)) {
                sb.append(c);
                if (++valueIndex >= valueLength)
                    break;
                c = value.charAt(valueIndex);
            }
            if (sb.length() == 0) {
                reporter.logInfo(reporter.message(locator, "*KEY*",
                    "Bad <timeExpression>, unexpected character ''{0}'', expected metric in offset time.", c));
                break;
            } else {
                String metric = sb.toString();
                try {
                    OffsetTime.Metric m = OffsetTime.Metric.valueOfShorthand(metric);
                    if ((timeParameters.getTimeBase() == TimeBase.CLOCK) && (m == OffsetTime.Metric.Frames)) {
                        reporter.logInfo(reporter.message(locator, "*KEY*",
                            "Bad <timeExpression>, frames metric not permitted when using 'clock' time base."));
                    }
                } catch (IllegalArgumentException e) {
                    try {
                        OffsetTime.Metric.valueOfShorthand(metric.toLowerCase());
                        reporter.logInfo(reporter.message(locator, "*KEY*",
                            "Bad <timeExpression>, metric ''{0}'' must be lower case in offset time.", metric));
                    } catch (IllegalArgumentException ee) {
                        reporter.logInfo(reporter.message(locator, "*KEY*",
                            "Bad <timeExpression>, unknown metric ''{0}'' in offset time.", metric));
                    }
                    break;
                }
            }

            // whitespace after metric
            if (valueIndex == valueLength)
                break;
            c = value.charAt(valueIndex);
            if (Characters.isXMLSpace(c)) {
                while (Characters.isXMLSpace(c)) {
                    if (++valueIndex >= valueLength)
                        break;
                    c = value.charAt(valueIndex);
                }
                if (valueIndex == valueLength) {
                    reporter.logInfo(reporter.message(locator, "*KEY*",
                        "Bad <timeExpression>, XML space padding not permitted after offset time."));
                }
            }

            // unrecognized non-whitespace characters after offset time
            if (valueIndex != valueLength) {
                String remainder = value.substring(valueIndex);
                reporter.logInfo(reporter.message(locator, "*KEY*",
                    "Bad <timeExpression>, unrecognized characters ''{0}'' after offset time.", remainder));
            }

        } while (false);

    }

    private static final Pattern wallClockTimePattern = Pattern.compile("wallclock\\(\\s*([^\\)]*)\\s*\\)");
    private static boolean maybeWallClockTime(String value, Location location, VerifierContext context, TimeParameters timeParameters) {
        return wallClockTimePattern.matcher(value).matches();
    }

    public static boolean isWallClockTime(String value, Location location, VerifierContext context, TimeParameters timeParameters, Time[] outputTime) {
        Matcher m = wallClockTimePattern.matcher(value);
        if (m.matches()) {
            assert m.groupCount() == 1;
            String wallClockTime = m.group(1);
            if (isWallClockDateTime(wallClockTime, location, context, timeParameters, outputTime))
                return true;
            else if (isWallClockWallTime(wallClockTime, location, context, timeParameters, outputTime))
                return true;
            else if (isWallClockDate(wallClockTime, location, context, timeParameters, outputTime))
                return true;
            else
                return false;
        } else
            return false;
    }

    private static final Pattern wallClockDateTimePattern =
        Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2})(?::(\\d{2}(?:\\.\\d+)?))?");
    public static boolean isWallClockDateTime(String value, Location location, VerifierContext context, TimeParameters timeParameters, Time[] outputTime) {
        Matcher m = wallClockDateTimePattern.matcher(value);
        if (m.matches()) {
            assert m.groupCount() >= 5;
            String years = m.group(1);
            String months = m.group(2);
            String days = m.group(3);
            String hours = m.group(4);
            String minutes = m.group(5);
            String seconds = null;
            if (m.groupCount() > 5)
                seconds = m.group(6);
            if ((timeParameters.getTimeBase() != TimeBase.CLOCK) && (timeParameters.getClockMode() != ClockMode.UTC))
                return false;
            WallClockTime t = new WallClockTimeImpl(years, months, days, hours, minutes, seconds);
            if ((t.getMonths() < 1) || (t.getMonths() > 12))
                return false;
            if ((t.getDays() < 1) || (t.getDays() > 31))
                return false;
            if (t.getHours() > 23)
                return false;
            if (t.getMinutes() > 59)
                return false;
            if (t.getSeconds() > 60.0)
                return false;
            if (outputTime != null)
                outputTime[0] = t;
            return true;
        } else
            return false;
    }

    private static final Pattern wallClockWallTimePattern = Pattern.compile("(\\d{2}):(\\d{2})(?::(\\d{2}(?:\\.\\d+)?))?");
    public static boolean isWallClockWallTime(String value, Location location, VerifierContext context, TimeParameters timeParameters, Time[] outputTime) {
        Matcher m = wallClockWallTimePattern.matcher(value);
        if (m.matches()) {
            WallClockTime tBegin = getDocumentWallClockBegin(context);
            String years = Integer.toString(tBegin.getYears());
            String months = Integer.toString(tBegin.getMonths());
            String days = Integer.toString(tBegin.getDays());
            assert m.groupCount() >= 2;
            String hours = m.group(1);
            String minutes = m.group(2);
            String seconds = null;
            if (m.groupCount() > 2)
                seconds = m.group(3);
            if ((timeParameters.getTimeBase() != TimeBase.CLOCK) && (timeParameters.getClockMode() != ClockMode.UTC))
                return false;
            WallClockTime t = new WallClockTimeImpl(years, months, days, hours, minutes, seconds);
            if (t.getHours() > 23)
                return false;
            if (t.getMinutes() > 59)
                return false;
            if (t.getSeconds() > 60.0)
                return false;
            if (outputTime != null)
                outputTime[0] = t;
            return true;
        } else
            return false;
    }

    private static final Pattern wallClockDatePattern = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
    public static boolean isWallClockDate(String value, Location location, VerifierContext context, TimeParameters timeParameters, Time[] outputTime) {
        Matcher m = wallClockDatePattern.matcher(value);
        if (m.matches()) {
            assert m.groupCount() == 3;
            String years = m.group(1);
            String months = m.group(2);
            String days = m.group(3);
            String hours = null;
            String minutes = null;
            String seconds = null;
            if ((timeParameters.getTimeBase() != TimeBase.CLOCK) && (timeParameters.getClockMode() != ClockMode.UTC))
                return false;
            WallClockTime t = new WallClockTimeImpl(years, months, days, hours, minutes, seconds);
            if ((t.getMonths() < 1) || (t.getMonths() > 12))
                return false;
            if ((t.getDays() < 1) || (t.getDays() > 31))
                return false;
            if (outputTime != null)
                outputTime[0] = t;
            return true;
        } else
            return false;
    }

    public static void badWallClockTime(String value, Location location, VerifierContext context, TimeParameters timeParameters) {
        Reporter reporter = context.getReporter();
        Locator locator = location.getLocator();
        Message m;
        if (value.indexOf('T') >= 0) {          // date-time format contains 'T', while other forms do not
            m = reporter.message(locator, "*KEY*", "Bad date-time form of wall clock <timeExpression>.");
        } else if (value.indexOf('-') >= 0) {   // date format contains '-', but not 'T'
            m = reporter.message(locator, "*KEY*", "Bad date form of wall clock <timeExpression>.");
        } else if (value.indexOf(':') >= 0) {   // wall-time format contains ':', but not 'T' and not '-'
            m = reporter.message(locator, "*KEY*", "Bad wall-time form of wallclock time <timeExpression>.");
        } else {                                // no hint on intended format
            m = reporter.message(locator, "*KEY*", "Bad wall clock <timeExpression>.");
        }
        reporter.logInfo(m);
    }

    private static WallClockTime getDocumentWallClockBegin(VerifierContext context) {
        WallClockTime xwcb = (WallClockTime) context.getResourceState("externalWallClockBegin");
        if (xwcb != null)
            return xwcb;
        WallClockTime iwcb = (WallClockTime) context.getResourceState("internalWallClockBegin");
        if (iwcb != null)
            return iwcb;
        else
            return WallClockTimeImpl.utc();
    }

}
