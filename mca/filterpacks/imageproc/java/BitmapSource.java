/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package android.filterpacks.imageproc;

import android.content.Context;
import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.FrameManager;
import android.filterfw.core.GenerateFinalPort;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.KeyValueMap;
import android.filterfw.core.MutableFrameFormat;
import android.filterfw.core.NativeFrame;
import android.filterfw.format.ImageFormat;
import android.graphics.Bitmap;

/**
 * @hide
 */
public class BitmapSource extends Filter {

    @GenerateFinalPort(name = "target")
    String mTargetString;

    @GenerateFieldPort(name = "bitmap")
    private Bitmap mBitmap;

    @GenerateFieldPort(name = "repeatFrame", hasDefault = true)
    boolean mRepeatFrame = false;

    private int mTarget;
    private Frame mImageFrame;

    public BitmapSource(String name) {
        super(name);
    }


    @Override
    public void setupPorts() {
        // Setup output format
        mTarget = FrameFormat.readTargetString(mTargetString);
        FrameFormat outputFormat = ImageFormat.create(ImageFormat.COLORSPACE_RGBA, mTarget);

        // Add output port
        addOutputPort("image", outputFormat);
    }

    public void loadImage(FilterContext filterContext) {
        FrameFormat outputFormat = ImageFormat.create(mBitmap.getWidth(),
                                                      mBitmap.getHeight(),
                                                      ImageFormat.COLORSPACE_RGBA,
                                                      mTarget);
        mImageFrame = filterContext.getFrameManager().newFrame(outputFormat);
        mImageFrame.setBitmap(mBitmap);
        mBitmap = null;
    }

    @Override
    public void fieldPortValueUpdated(String name, FilterContext context) {
        // Clear image (to trigger reload) in case parameters have been changed
        if (name.equals("bitmap") || name.equals("target")) {
            if (mImageFrame != null) {
                mImageFrame.release();
                mImageFrame = null;
            }
        }
    }

    @Override
    public void process(FilterContext context) {
        if (mImageFrame == null) {
            loadImage(context);
        }

        pushOutput("image", mImageFrame);

        if (!mRepeatFrame) {
            closeOutputPort("image");
        }
    }

    @Override
    public void tearDown(FilterContext env) {
        if (mImageFrame != null) {
            mImageFrame.release();
            mImageFrame = null;
        }
    }
}