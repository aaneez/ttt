/*
 * Copyright 2016 Skynav, Inc. All rights reserved.
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

package com.skynav.ttv.verifier.ttml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import org.xml.sax.Locator;

import com.skynav.ttv.model.Model;
import com.skynav.ttv.model.value.Image;
import com.skynav.ttv.util.IOUtil;
import com.skynav.ttv.util.Location;
import com.skynav.ttv.util.Reporter;
import com.skynav.ttv.verifier.ImageVerifier;
import com.skynav.ttv.verifier.VerifierContext;

import com.xfsi.xav.test.Test;
import com.xfsi.xav.test.TestInfo;
import com.xfsi.xav.test.TestManager;
import com.xfsi.xav.util.Error;
import com.xfsi.xav.util.MimeType;
import com.xfsi.xav.util.Progress;
import com.xfsi.xav.util.Result;
import com.xfsi.xav.validation.images.jpeg.JpegValidator;
import com.xfsi.xav.validation.images.png.PngValidator;
import com.xfsi.xav.validation.util.AbstractTestInfo;
import com.xfsi.xav.validation.util.AbstractTestManager;

public class TTML2ImageVerifier implements ImageVerifier {

    private Model model;

    public TTML2ImageVerifier(Model model) {
        populate(model);
    }

    public boolean verify(Image image, Location location, VerifierContext context) {
        boolean failed = false;
        Reporter reporter = context.getReporter();
        Locator locator = location.getLocator();
        MimeType[] mimeType = new MimeType[1];
        if (!sniffImage(image, mimeType, location, context)) {
            reporter.logError(reporter.message(locator, "*KEY*", "Unable to determine image type."));
            failed = true;
        } else {
            MimeType mt = mimeType[0];
            assert mt != null;
            if (!isSupportedMimeType(mt)) {
                reporter.logError(reporter.message(locator, "*KEY*", "Image type ''{0}'' is not supported.", mt.getType()));
                failed = true;
            }
            if (!failed && !verifyImage(image, mt, location, context))
                failed = true;
        }
        return !failed;
    }

    private void populate(Model model) {
        this.model = model;
    }

    private static final short[]        pngSignature            =
        new short[] { 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
    private static final MimeType       pngType                 =
        new MimeType(MimeType.IMAGE_PNG_TYPE);
    private static final short[]        jpgSignature            =
        new short[] { 0xFF, 0xD8, 0xFF, 0xE0,   -1,   -1, 0x4A, 0x46, 0x49, 0x46, 0x00 };
    private static final MimeType       jpgType                 =
        new MimeType(MimeType.IMAGE_JPG_TYPE);
    private static final MimeType       unknownType             =
        new MimeType();
    private static final Signature[]    signatures              =
    {
        new Signature(pngSignature, pngType.getType()),
        new Signature(jpgSignature, jpgType.getType()),
    };
    private static final int            signatureLengthMaximum;

    static {
        int saLenMax = 0;
        for (Signature s : signatures) {
            short[] sa = s.getSignature();
            int saLen = sa.length;
            if (saLen > saLenMax)
                saLenMax = saLen;
        }
        signatureLengthMaximum = saLenMax;
    }

    private boolean sniffImage(Image image, MimeType[] outputType, Location location, VerifierContext context) {
        boolean failed = false;
        Reporter reporter = context.getReporter();
        MimeType mt = unknownType;
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(image.getURI().toURL().openStream());
            byte[] buf = new byte[signatureLengthMaximum];
            int nb = IOUtil.readCompletely(bis, buf);
            for (Signature s : signatures) {
                MimeType mtSniffed = sniffImage(buf, nb, s.getSignature(), s.getType());
                if (mtSniffed != null) {
                    mt = mtSniffed;
                    break;
                }
            }
        } catch (MalformedURLException e) {
            reporter.logError(e);
            failed = true;
        } catch (IOException e) {
            reporter.logError(e);
            failed = true;
        } finally {
            IOUtil.closeSafely(bis);
        }
        if (mt != null) {
            if ((outputType != null) && (outputType.length > 0))
                outputType[0] = mt;
        }
        return !failed;
    }

    private MimeType sniffImage(byte[] buf, int len, short[] signature, String type) {
        if (matchAtStart(signature, buf))
            return new MimeType(type);
        else
            return null;
    }

    private boolean matchAtStart(short[] b1, byte[] b2) {
        if (b1 != null && b2 != null) {
            // Need to have sniffable array as long as signature
            if (b2.length >= b1.length) {
                for (int i = 0; i < b1.length; i++) {
                    // Negative values matches all bytes
                    if (b1[i] < 0)
                        continue;
                    else {
                        short b2s = (short) (0xFF & b2[i]);
                        if (b1[i] != b2s)
                            return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean isSupportedMimeType(MimeType mt) {
        String[] components = mt.getType().split(";");
        String t = (components.length > 0) ? components[0] : null;
        String p = (components.length > 1) ? components[1] : null;
        if (t != null)
            t = t.trim();
        if (p != null)
            p = p.trim();
        return model.isSupportedResourceType(t, p);
    }

    private boolean verifyImage(Image image, MimeType mimeType, Location location, VerifierContext context) {
        Reporter reporter = context.getReporter();
        Locator locator = location.getLocator();
        Test t = getImageValidator(mimeType);
        if (t != null) {
            BufferedInputStream bis = null;
            try {
                TestInfo ti = new TestInfoAdapter(image, mimeType, location);
                TestManager tm = new TestManagerAdapter(context);
                URI uri = image.getURI();
                bis = new BufferedInputStream(uri.toURL().openStream());
                ti.setResourceStream(bis);
                reporter.logInfo(reporter.message("*KEY*", "Verifying image ''{0}'' as ''{1}''.", getImageName(uri), mimeType.toString()));
                Result r = t.run(tm, ti);
                if (r.isFailure())
                    return false;
                else {
                    int w = (Integer) r.getState("width");
                    int h = (Integer) r.getState("height");
                    image.setExtent(w, h);
                    return true;
                }
            } catch (Exception e) {
                reporter.logError(e);
                return false;
            } finally {
                IOUtil.closeSafely(bis);
            }
        } else {
            reporter.logError(reporter.message(locator, "*KEY*", "No image validator for ''{0}''.", mimeType.toString()));
            return false;
        }
    }

    private String getImageName(URI uri) {
        String p = uri.getPath();
        int i = p.lastIndexOf('/');
        if (i >= 0)
            p = p.substring(i + 1);
        return p;
    }

    private Test getImageValidator(MimeType mimeType) {
        if (mimeType.equals(pngType))
            return new PngValidator();
        else if (mimeType.equals(jpgType))
            return new JpegValidator();
        else
            return null;
    }

    private static class Signature {
        private short[] signature;
        private String type;
        Signature(short[] signature, String type) {
            this.signature = signature;
            this.type = type;
        }
        short[] getSignature() {
            return signature;
        }
        String getType() {
            return type;
        }
    }

    private static class TestInfoAdapter extends AbstractTestInfo {
        @SuppressWarnings("unused")
        private Image image;
        @SuppressWarnings("unused")
        private MimeType mimeType;
        private Location location;
        TestInfoAdapter(Image image, MimeType mimeType, Location location) {
            this.image = image;
            this.mimeType = mimeType;
            this.location = location;
        }
        Location getLocation() {
            return location;
        }
    }

    private static class TestManagerAdapter extends AbstractTestManager {
        private VerifierContext context;
        TestManagerAdapter(VerifierContext context) {
            this.context = context;
        }
        public void reportError(TestInfo ti, Error error) {
            Error.Severity s = error.getSeverity();
            if (s.isSevereAs(Error.Severity.ERROR_SEVERITY)) {
                Reporter reporter = context.getReporter();
                reporter.logError(reporter.message(getLocation(ti).getLocator(), "*KEY*", error.getMessage()));
            }
        }
        public void reportProgress(TestInfo ti, Progress progress) {
        }
        private Location getLocation(TestInfo ti) {
            if ((ti != null) && (ti instanceof TestInfoAdapter))
                return ((TestInfoAdapter) ti).getLocation();
            else
                return new Location();
        }
    }

}
