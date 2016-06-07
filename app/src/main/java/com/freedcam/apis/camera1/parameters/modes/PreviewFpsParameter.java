/*
 *
 *     Copyright (C) 2015 Ingo Fuchs
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * /
 */

package com.freedcam.apis.camera1.parameters.modes;

import android.hardware.Camera.Parameters;

import com.freedcam.apis.KEYS;
import com.freedcam.apis.camera1.CameraHolder;

/**
 * Created by troop on 21.08.2014.
 */
public class PreviewFpsParameter extends  BaseModeParameter
{
    private CameraHolder cameraHolder;

    public PreviewFpsParameter(Parameters parameters,CameraHolder holder) {
        super(parameters, holder, KEYS.PREVIEW_FRAME_RATE, KEYS.PREVIEW_FRAME_RATE_VALUES);
        cameraHolder = holder;
    }



    @Override
    public void SetValue(String valueToSet, boolean setToCam)
    {
        super.SetValue(valueToSet, setToCam);
        if (setToCam) {
            cameraHolder.StopPreview();
            cameraHolder.StartPreview();
        }
    }

    @Override
    public String GetValue() {
        return super.GetValue();
    }

}