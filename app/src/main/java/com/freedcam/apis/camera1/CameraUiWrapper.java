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

package com.freedcam.apis.camera1;


import android.content.Context;
import android.os.Build.VERSION;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;

import com.freedcam.apis.KEYS;
import com.freedcam.apis.basecamera.AbstractCameraUiWrapper;
import com.freedcam.apis.basecamera.Size;
import com.freedcam.apis.basecamera.interfaces.I_Module;
import com.freedcam.apis.basecamera.interfaces.I_error;
import com.freedcam.apis.basecamera.modules.I_Callbacks.ErrorCallback;
import com.freedcam.apis.basecamera.modules.I_ModuleEvent;
import com.freedcam.apis.basecamera.parameters.I_ParametersLoaded;
import com.freedcam.apis.basecamera.parameters.modes.AbstractModeParameter.I_ModeParameterEvent;
import com.freedcam.apis.camera1.CameraHolder.Frameworks;
import com.freedcam.apis.camera1.cameraholder.CameraHolderLG;
import com.freedcam.apis.camera1.cameraholder.CameraHolderMTK;
import com.freedcam.apis.camera1.cameraholder.CameraHolderMotoX;
import com.freedcam.apis.camera1.modules.ModuleHandler;
import com.freedcam.apis.camera1.parameters.ParametersHandler;
import com.freedcam.apis.camera1.renderscript.FocusPeakProcessorAp1;
import com.freedcam.utils.AppSettingsManager;
import com.freedcam.utils.DeviceUtils.Devices;
import com.freedcam.utils.Logger;
import com.freedcam.utils.RenderScriptHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by troop on 16.08.2014.
 */
public class CameraUiWrapper extends AbstractCameraUiWrapper implements Callback, I_ParametersLoaded, ErrorCallback, I_ModuleEvent
{
    protected ExtendedSurfaceView preview;
    protected I_error errorHandler;
    private static String TAG = CameraUiWrapper.class.getSimpleName();
    public CameraHolder cameraHolder;
    public FocusPeakProcessorAp1 focusPeakProcessorAp1;
    boolean cameraRdy = false;

    @Override
    public String CameraApiName() {
        return AppSettingsManager.API_1;
    }

    public CameraUiWrapper(SurfaceView preview, TextureViewRatio previewTexture, Context context, AppSettingsManager appSettingsManager, RenderScriptHandler renderScriptHandler)
    {
        super(appSettingsManager);

        this.preview = (ExtendedSurfaceView)preview;
        //attache the callback to the Campreview
        preview.getHolder().addCallback(this);

        errorHandler = this;
        if (hasLGFramework())
            cameraHolder = new CameraHolderLG(this,appSettingsManager,Frameworks.LG);
        else if (isMotoExt())
            cameraHolder = new CameraHolderMotoX(this,appSettingsManager,Frameworks.MotoX);
        else if (isMTKDevice())
            cameraHolder = new CameraHolderMTK(this,appSettingsManager,Frameworks.MTK);
        else
            cameraHolder = new CameraHolder(this,appSettingsManager,Frameworks.Normal);
        super.cameraHolder = cameraHolder;
        cameraHolder.errorHandler = errorHandler;

        parametersHandler = new ParametersHandler(this,context,appSettingsManager);
        cameraHolder.SetParameterHandler(parametersHandler);
        parametersHandler.AddParametersLoadedListner(this);
        this.preview.ParametersHandler = parametersHandler;
        //parametersHandler.ParametersEventHandler.AddParametersLoadedListner(this.preview);
        moduleHandler = new ModuleHandler(cameraHolder,context,appSettingsManager);
        moduleHandler.moduleEventHandler.addListner(this);

        Focus = new FocusHandler(this);
        cameraHolder.Focus = Focus;
        if (VERSION.SDK_INT >= 18) {
            focusPeakProcessorAp1 = new FocusPeakProcessorAp1(previewTexture, this, context,renderScriptHandler);
            SetCameraChangedListner(focusPeakProcessorAp1);
        }
        else
            previewTexture.setVisibility(View.GONE);
        Logger.d(TAG, "Ctor done");
    }

    private boolean hasLGFramework()
    {
        try {
            Class c = Class.forName("com.lge.hardware.LGCamera");
            Logger.d(TAG, "Has Lg Framework");
            c = Class.forName("com.lge.media.CamcorderProfileEx");
            Logger.d(TAG, "Has Lg Framework");
            return true;

        } catch (ClassNotFoundException|NullPointerException|UnsatisfiedLinkError | ExceptionInInitializerError e) {

            Logger.d(TAG, "No LG Framework");
            return false;
        }
    }

    private boolean isMotoExt()
    {
        try {
            Class c = Class.forName("com.motorola.android.camera.CameraMotExt");
            Logger.d(TAG, "Has Moto Framework");
            c = Class.forName("com.motorola.android.media.MediaRecorderExt");
            Logger.d(TAG, "Has Moto Framework");
            return true;

        } catch (ClassNotFoundException|NullPointerException|UnsatisfiedLinkError | ExceptionInInitializerError e) {
            Logger.d(TAG, "No Moto Framework");
            return false;
        }

    }

    private boolean isMTKDevice()
    {
        try
        {
            Class camera = Class.forName("android.hardware.Camera");
            Method[] meths = camera.getMethods();
            Method app = null;
            for (Method m : meths)
            {
                if (m.getName().equals("setProperty"))
                    app = m;
            }
            if (app != null) {
                Logger.d(TAG,"MTK Framework found");
                return true;
            }
            Logger.d(TAG, "MTK Framework not found");
            return false;
        }
        catch (ClassNotFoundException|NullPointerException|UnsatisfiedLinkError | ExceptionInInitializerError e)
        {
            Logger.exception(e);
            Logger.d(TAG, "MTK Framework not found");
            return false;
        }
    }


    @Override
    public void StartCamera() {
        cameraHolder.OpenCamera(appSettingsManager.GetCurrentCamera());
        Logger.d(TAG, "opencamera");
    }

    @Override
    public void StopCamera() {
        Logger.d(TAG, "Stop Camera");
        cameraHolder.CloseCamera();
    }

    @Override
    public void StartPreview() {
        cameraHolder.StartPreview();
    }

    @Override
    public void StopPreview() {
        Logger.d(TAG, "Stop Preview");
        cameraHolder.StopPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        Logger.d(TAG, "surface created");
        PreviewSurfaceRdy = true;
        StartCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        PreviewSurfaceRdy =false;

        //StopPreview();
        StopCamera();
    }

    @Override
    public void ParametersLoaded()
    {
        parametersHandler.PictureSize.addEventListner(onPreviewSizeShouldChange);
        //parametersHandler.VideoSize.addEventListner(onPreviewSizeShouldChange);
    }

    @Override
    public void onError(int i)
    {
        errorHandler.OnError("Got Error from camera: " + i);
        try
        {
            cameraHolder.CloseCamera();
        }
        catch (Exception ex)
        {
           Logger.e(TAG,ex.getMessage());
        }
    }

    //this gets called when the cameraholder has open the camera
    @Override
    public void onCameraOpen(String message)
    {
        cameraRdy = true;
        super.onCameraOpen(message);
        ((ParametersHandler) parametersHandler).LoadParametersFromCamera();
        cameraHolder.SetErrorCallback(this);
        cameraHolder.SetSurface(preview.getHolder());
        cameraHolder.StartPreview();
        super.onCameraOpenFinish("");
    }

    @Override
    public void onCameraClose(String message)
    {
        cameraRdy = false;
        super.onCameraClose(message);
    }

    @Override
    public void onPreviewOpen(String message) {
        super.onPreviewOpen(message);

    }

    @Override
    public void onPreviewClose(String message) {
        super.onPreviewClose(message);
        cameraHolder.ResetPreviewCallback();
    }

    @Override
    public void onCameraError(String error) {
        super.onCameraError(error);
    }

    @Override
    public void onCameraStatusChanged(String status)
    {
        super.onCameraStatusChanged(status);
    }

    @Override
    public void onModuleChanged(I_Module module) {
        super.onModuleChanged(module);
    }

    @Override
    public void onCameraOpenFinish(String message) {
        super.onCameraOpenFinish(message);
    }

    @Override
    public void OnError(String error) {
        super.onCameraError(error);
    }

    I_ModeParameterEvent onPreviewSizeShouldChange = new I_ModeParameterEvent() {

        @Override
        public void onValueChanged(String val)
        {
            if(moduleHandler.GetCurrentModuleName().equals(KEYS.MODULE_PICTURE)
                    || moduleHandler.GetCurrentModuleName().equals(KEYS.MODULE_HDR)
                    || moduleHandler.GetCurrentModuleName().equals(KEYS.MODULE_INTERVAL)
                    || moduleHandler.GetCurrentModuleName().equals(KEYS.MODULE_STACKING))
            {
                Size sizefromCam = new Size(parametersHandler.PictureSize.GetValue());
                List<Size> sizes = new ArrayList<>();
                String[] stringsSizes = parametersHandler.PreviewSize.GetValues();
                for (String s : stringsSizes) {
                    sizes.add(new Size(s));
                }
                final Size size = getOptimalPreviewSize(sizes, sizefromCam.width, sizefromCam.height);
                Logger.d(TAG, "set size to " + size.width + "x" + size.height);

                parametersHandler.PreviewSize.SetValue(size.width + "x" + size.height, true);
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (preview != null)
                            preview.setAspectRatio(size.width, size.height);
                        if (focusPeakProcessorAp1 != null)
                            focusPeakProcessorAp1.SetAspectRatio(size.width,size.height);
                    }
                });

            }
            else if (moduleHandler.GetCurrentModuleName().equals(KEYS.MODULE_VIDEO))
            {
                Size sizefromCam = new Size("1920x1080");

                List<Size> sizes = new ArrayList<>();
                String[] stringsSizes = parametersHandler.PreviewSize.GetValues();
                for (String s : stringsSizes) {
                    sizes.add(new Size(s));
                }
                final Size size = getOptimalPreviewSize(sizes, sizefromCam.width, sizefromCam.height);
                Logger.d(TAG, "set size to " + size.width + "x" + size.height);
                parametersHandler.PreviewSize.SetValue(size.width + "x" + size.height, true);
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (preview != null)
                            preview.setAspectRatio(size.width, size.height);
                        if (focusPeakProcessorAp1 != null)
                            focusPeakProcessorAp1.SetAspectRatio(size.width,size.height);
                    }
                });

            }
        }

        @Override
        public void onIsSupportedChanged(boolean isSupported) {

        }

        @Override
        public void onIsSetSupportedChanged(boolean isSupported) {

        }

        @Override
        public void onValuesChanged(String[] values) {

        }

        @Override
        public void onVisibilityChanged(boolean visible) {

        }
    };

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        double ASPECT_TOLERANCE = 0.2;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        // Try to find an size match aspect ratio and size
        for (Size size : sizes)
        {
            if(appSettingsManager.getDevice() == Devices.ZTE_ADV) {
                if (size.width <= 1440 && size.height <= 1080 && size.width >= 640 && size.height >= 480) {
                    double ratio = (double) size.width / size.height;
                    if (ratio < targetRatio + ASPECT_TOLERANCE && ratio > targetRatio - ASPECT_TOLERANCE) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - h);
                        break;
                    }

                }
            }
            else {
                if (size.width <= 1280 && size.height <= 720 && size.width >= 640 && size.height >= 480) {
                    double ratio = (double) size.width / size.height;
                    if (ratio < targetRatio + ASPECT_TOLERANCE && ratio > targetRatio - ASPECT_TOLERANCE) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - h);
                        break;
                    }

                }
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes)
            {
                if(appSettingsManager.getDevice() == Devices.ZTE_ADV) {
                    if (size.width <= 1440 && size.height <= 1080 && size.width >= 640 && size.height >= 480) {
                        if (Math.abs(size.height - h) < minDiff) {
                            optimalSize = size;
                            minDiff = Math.abs(size.height - h);
                        }
                    }
                }
                else {
                    if (size.width <= 1280 && size.height <= 720 && size.width >= 640 && size.height >= 480) {
                        if (Math.abs(size.height - h) < minDiff) {
                            optimalSize = size;
                            minDiff = Math.abs(size.height - h);
                        }
                    }
                }
            }
        }
        Logger.d(TAG, "Optimal preview size " + optimalSize.width + "x" + optimalSize.height);
        return optimalSize;
    }

    @Override
    public void ModuleChanged(String module)
    {
        onPreviewSizeShouldChange.onValueChanged("");
    }

    @Override
    public int getMargineLeft() {
        return preview.getLeft();
    }

    @Override
    public int getMargineRight() {
        return preview.getRight();
    }

    @Override
    public int getMargineTop() {
        return preview.getTop();
    }

    @Override
    public int getPreviewWidth() {
        return preview.getWidth();
    }

    @Override
    public int getPreviewHeight() {
        return preview.getHeight();
    }

    @Override
    public SurfaceView getSurfaceView() {
        return preview;
    }
}