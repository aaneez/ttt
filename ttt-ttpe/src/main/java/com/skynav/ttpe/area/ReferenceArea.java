/*
 * Copyright 2014-15 Skynav, Inc. All rights reserved.
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

package com.skynav.ttpe.area;

import org.w3c.dom.Element;

import com.skynav.ttpe.geometry.Dimension;
import com.skynav.ttpe.geometry.Extent;
import com.skynav.ttpe.geometry.TransformMatrix;
import com.skynav.ttpe.geometry.WritingMode;
import com.skynav.ttpe.style.Visibility;

public class ReferenceArea extends PositionedBlockArea {

    private WritingMode wm;
    private TransformMatrix ctm;
    private double overflow;

    public ReferenceArea(Element e, double x, double y, double width, double height, WritingMode wm, TransformMatrix ctm, Visibility visibility) {
        super(e, x, y, width, height, visibility);
        assert wm != null;
        this.wm = wm;
        assert ctm != null;
        this.ctm = ctm;
    }

    @Override
    public ViewportArea getParent() {
        return (ViewportArea) super.getParent();
    }

    @Override
    public double getAvailable(Dimension dimension) {
        WritingMode wm = this.wm;
        if (wm == null)
            wm = WritingMode.LRTB;
        Extent ce = getContentExtent();
        if (wm.isHorizontal())
            return (dimension == Dimension.IPD) ? ce.getWidth() : ce.getHeight();
        else
            return (dimension == Dimension.IPD) ? ce.getHeight() : ce.getWidth();
    }

    public WritingMode getWritingMode() {
        return wm;
    }

    public int getBidiLevel() {
        WritingMode wm = this.wm;
        if (wm == null)
            wm = WritingMode.LRTB;
        if (wm == WritingMode.LRTB)
            return 0;
        else if (wm == WritingMode.RLTB)
            return 1;
        else
            return 0;
    }

    public TransformMatrix getCTM() {
        return ctm;
    }

    public void setOverflow(double overflow) {
        this.overflow = overflow;
    }

    public double getOverflow() {
        return overflow;
    }

}
