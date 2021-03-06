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

package freed.cam.ui.themesample.cameraui.childs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by troop on 13.06.2015.
 */
public class UiSettingsChildExit extends UiSettingsChild
{

    public UiSettingsChildExit(Context context) {
        super(context);
    }

    public UiSettingsChildExit(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (fragment_activityInterface != null)
                    fragment_activityInterface.closeActivity();
            }
        });
    }

    @Override
    public void onParameterValueChanged(String val) {

    }

    @Override
    public void onParameterIsSupportedChanged(boolean isSupported) {

    }

    @Override
    public void onParameterIsSetSupportedChanged(boolean isSupported) {

    }

    @Override
    public void onModuleChanged(String module) {
    }
}
