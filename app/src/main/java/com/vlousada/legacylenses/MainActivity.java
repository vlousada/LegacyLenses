package com.vlousada.legacylenses;

/*
 * Adapted sourcecode from com.github.obs1dium.bettermanual
 */


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;


import com.github.ma1co.pmcademo.app.BaseActivity;
import com.github.ma1co.pmcademo.app.Logger;

import com.github.ma1co.pmcademo.app.PlaybackActivity;
import com.github.obs1dium.CameraUtil;
import com.github.obs1dium.GridView;
import com.github.obs1dium.HistogramView;
import com.github.obs1dium.MinShutterActivity;
import com.github.obs1dium.OnSwipeTouchListener;
import com.github.obs1dium.PreviewNavView;
import com.github.obs1dium.SonyDrawables;

import com.sony.scalar.hardware.CameraEx;
import com.sony.scalar.hardware.CameraEx.ExifInfo;

//import com.github.ma1co.openmemories.framework.DisplayManager; //do not use until OpenMemories DeviceManager include getDisplayVideoRect().... Use scalar below
import com.sony.scalar.hardware.avio.DisplayManager;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends BaseActivity implements SurfaceHolder.Callback, View.OnClickListener, CameraEx.ShutterListener, CameraEx.ShutterSpeedChangeListener
{
    public static final boolean LOGGING_ENABLED = true;
    private static final int MESSAGE_TIMEOUT = 1000;

    private static String   m_cameraModel;

    private SurfaceHolder   m_surfaceHolder;
    private CameraEx        m_camera;
    private CameraEx.AutoPictureReviewControl m_autoReviewControl;

    private String          m_pictureID ="";
    private int             m_pictureReviewTime;

    private Preferences     m_prefs;

    private TextView        m_tvShutter;
    private TextView        m_tvISO;
    private TextView        m_tvExposureCompensation;
    private LinearLayout    m_lExposure;
    private TextView        m_tvExposure;
    private TextView        m_tvMagnification;
    private TextView        m_tvMsg;
    private HistogramView m_vHist;
    private TableLayout     m_lInfoBottom;
    private ImageView       m_ivDriveMode;
    private ImageView       m_ivMode;
    private GridView        m_vGrid;
    private TextView        m_tvHint;
    public static TextView  m_tvLog;

    // Legacy
    private ExifInfo        m_exifInfo;
    private TextView        m_tvLegacyLensName;
    private TextView        m_tvLegacyFocal;
    private TextView        m_tvLegacyAperture;
    private List<Integer>   m_legacyApertureList;
    private String          m_legacyName = "Lens Xpto";
    private int             m_curApert = 16; //current legacy Aperture
    private int             m_curFocal = 50; //current legacy Focal
    public static TextView  m_tvLegacySpecial;
    public static boolean   m_legacyZoom = false; //init Lens Type FIXED

    // ISO
    private int             m_curIso;
    private List<Integer>   m_supportedIsos;

    // Shutter speed
    private boolean         m_notifyOnNextShutterSpeedChange;

    // Exposure compensation
    private int             m_maxExposureCompensation;
    private int             m_minExposureCompensation;
    private int             m_curExposureCompensation;
    private float           m_exposureCompensationStep;

    // Preview magnification
    private List<Integer>   m_supportedPreviewMagnifications;
    private boolean         m_zoomLeverPressed;
    private int             m_curPreviewMagnification;
    private float           m_curPreviewMagnificationFactor;
    private Pair<Integer, Integer>  m_curPreviewMagnificationPos = new Pair<Integer, Integer>(0, 0);
    private int             m_curPreviewMagnificationMaxPos;
    private PreviewNavView m_previewNavView;

    enum DialMode { shutter, legacyaperture, iso, exposure, mode, drive, legacyfocal
    }
    private DialMode        m_dialMode;

    enum SceneMode { manual, aperture, shutter, other }
    private SceneMode       m_sceneMode;

    private final Handler   m_handler = new Handler();
    private final Runnable  m_hideMessageRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            m_tvMsg.setVisibility(View.GONE);
        }
    };

    private boolean         m_takingPicture;
    private boolean         m_shutterKeyDown;

    private boolean         m_haveTouchscreen;

    private static final int VIEW_FLAG_GRID         = 0x01;
    private static final int VIEW_FLAG_HISTOGRAM    = 0x02;
    private static final int VIEW_FLAG_EXPOSURE     = 0x04;
    private static final int VIEW_FLAG_MASK         = 0x07; // all flags combined
    private int              m_viewFlags;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceView.setOnTouchListener(new SurfaceSwipeTouchListener(this));
        m_surfaceHolder = surfaceView.getHolder();
        m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        m_tvMsg = (TextView)findViewById(R.id.tvMsg);

        m_tvShutter = (TextView)findViewById(R.id.tvShutter);
        m_tvShutter.setOnTouchListener(new ShutterSwipeTouchListener(this));

        m_tvISO = (TextView)findViewById(R.id.tvISO);
        m_tvISO.setOnTouchListener(new IsoSwipeTouchListener(this));

        m_tvExposureCompensation = (TextView)findViewById(R.id.tvExposureCompensation);
        m_tvExposureCompensation.setOnTouchListener(new ExposureSwipeTouchListener(this));
        m_lExposure = (LinearLayout)findViewById(R.id.lExposure);

        m_tvExposure = (TextView)findViewById(R.id.tvExposure);
        //noinspection ResourceType
        m_tvExposure.setCompoundDrawablesWithIntrinsicBounds(SonyDrawables.p_meteredmanualicon, 0, 0, 0);

        m_tvLog = (TextView)findViewById(R.id.tvLog);
        m_tvLog.setVisibility(LOGGING_ENABLED ? View.VISIBLE : View.GONE);

        m_vHist = (HistogramView)findViewById(R.id.vHist);

        m_tvMagnification = (TextView)findViewById(R.id.tvMagnification);

        m_lInfoBottom = (TableLayout)findViewById(R.id.lInfoBottom);

        m_previewNavView = (PreviewNavView)findViewById(R.id.vPreviewNav);
        m_previewNavView.setVisibility(View.GONE);

        m_ivDriveMode = (ImageView)findViewById(R.id.ivDriveMode);
        m_ivDriveMode.setOnClickListener(this);

        m_ivMode = (ImageView)findViewById(R.id.ivMode);
        m_ivMode.setOnClickListener(this);

        m_vGrid = (GridView)findViewById(R.id.vGrid);

        m_tvHint = (TextView)findViewById(R.id.tvHint);
        m_tvHint.setVisibility(View.GONE);

        //Legacy Textiew /Swipe
        m_tvLegacyLensName = (TextView)findViewById(R.id.tv_LensName);
        m_tvLegacyFocal = (TextView)findViewById(R.id.tv_LegacyFocal);
        m_tvLegacyFocal.setOnClickListener(m_legacyZoom ? this : null);

        m_tvLegacySpecial = (TextView)findViewById(R.id.tv_LegacySpecial);
        m_tvLegacyAperture = (TextView)findViewById(R.id.tv_LegacyAperture);
        m_tvLegacyAperture.setOnTouchListener(new ApertureSwipeTouchListener(this));


        setDialMode(DialMode.shutter);

        m_prefs = new Preferences(this);
        //IF NEED to reset SharedPreferences
        //m_prefs.clearPrefs();

        m_tvLegacyAperture.setText("F/" + "1.6"); //init Aperture Text
        m_tvLegacyLensName.setText("Lens Xpto"); //init Lens Name
        m_tvLegacyFocal.setText("50mm"); //init Lens focal

        m_exifInfo = new ExifInfo();

        m_cameraModel = getDeviceInfo().getModel().toString();

        //camera has touchscreen? FEATURES-KeyOne
        if (LLUtils.Model_FEATURES_Map.get(m_cameraModel)[1]== 1f)
            m_haveTouchscreen = true;
        else
            m_haveTouchscreen = false;
        //LLUtils.log(m_cameraModel);
        //init m_legacyZoom
        if (isZoomLegacy(m_prefs.getMinFocal(), m_prefs.getMaxFocal()))
            m_legacyZoom = true;


    }

    @Override
    protected void onResume()
    {


        super.onResume();

        String legacyLensName = "";
        String legacyLensAperture = "";
        String legacyLensSpecial = "";
        String legacyLensAll ="";
        String legacyLensFocal = "";

        m_camera = CameraEx.open(0, null);
        m_surfaceHolder.addCallback(this);
        m_camera.startDirectShutter();
        m_autoReviewControl = new CameraEx.AutoPictureReviewControl();
        m_camera.setAutoPictureReviewControl(m_autoReviewControl);


        // ENABLE picture review in ORDER to get fileInfo;
        m_pictureReviewTime = m_autoReviewControl.getPictureReviewTime();
        m_autoReviewControl.setPictureReviewInfoHist(false);
        m_autoReviewControl.setPictureReviewTime(2); //2secs
        m_autoReviewControl.setPictureReviewInfoListener(new CameraEx.PictureReviewInfoListener() {
            @Override
            public void onGetInfo(CameraEx.ReviewInfo reviewInfo, CameraEx cameraEx) {
                //Get some infos from "JUSTSAVED" photo
                String filePhoto = String.valueOf(reviewInfo.photo.dirNo)+ "-"+ String.valueOf(reviewInfo.photo.fileNo);
                if (m_takingPicture) {
                    m_pictureID = filePhoto;
                    m_takingPicture = false; //picture was already taken so waiting for new onShutter Listener
                    logLegacyExif();
                }
            }
        });

        m_vGrid.setVideoRect(getDisplayManager().getDisplayedVideoRect());
        //  if (m_videoRect != null)             m_vGrid.setVideoRect(m_videoRect);

        final Camera.Parameters params = m_camera.getNormalCamera().getParameters();
        final CameraEx.ParametersModifier paramsModifier = m_camera.createParametersModifier(params);

        // Exposure compensation
        m_maxExposureCompensation = params.getMaxExposureCompensation();
        m_minExposureCompensation = params.getMinExposureCompensation();
        m_exposureCompensationStep = params.getExposureCompensationStep();
        m_curExposureCompensation = params.getExposureCompensation();
        updateExposureCompensation(false);


        // Preview/Histogram
        m_camera.setPreviewAnalizeListener(new CameraEx.PreviewAnalizeListener()
        {
            @Override
            public void onAnalizedData(CameraEx.AnalizedData analizedData, CameraEx cameraEx)
            {
                if (analizedData != null && analizedData.hist != null && analizedData.hist.Y != null && m_vHist.getVisibility() == View.VISIBLE)
                    m_vHist.setHistogram(analizedData.hist.Y);
            }
        });

        // ISO
        m_camera.setAutoISOSensitivityListener(new CameraEx.AutoISOSensitivityListener()
        {
            @Override
            public void onChanged(int i, CameraEx cameraEx)
            {
                //LLUtils.log("AutoISOChanged " + String.valueOf(i) + "\n");
                m_tvISO.setText("\uE488 " + String.valueOf(i) + (m_curIso == 0 ? "(A)" : ""));
            }
        });

        // Shutter
        m_camera.setShutterSpeedChangeListener(this);
        m_camera.setShutterListener(this);

        // Exposure metering
        m_camera.setProgramLineRangeOverListener(new CameraEx.ProgramLineRangeOverListener()
        {
            @Override
            public void onAERange(boolean b, boolean b1, boolean b2, CameraEx cameraEx)
            {
                //LLUtils.log(String.format("onARRange b %b b1 %b b2 %b\n", Boolean.valueOf(b), Boolean.valueOf(b1), Boolean.valueOf(b2)));
            }

            @Override
            public void onEVRange(int ev, CameraEx cameraEx)
            {
                final String text;
                if (ev == 0)
                    text = "\u00B10.0";
                else if (ev > 0)
                    text = String.format("+%.1f", (float)ev / 3.0f);
                else
                    text = String.format("%.1f", (float)ev / 3.0f);
                m_tvExposure.setText(text);
                //LLUtils.log(String.format("onEVRange i %d %f\n", ev, (float)ev / 3.0f));
            }

            @Override
            public void onMeteringRange(boolean b, CameraEx cameraEx)
            {
                //LLUtils.log(String.format("onMeteringRange b %b\n", Boolean.valueOf(b)));
            }
        });


        //Apertures Stored
        String myAper = m_prefs.getMyApertures();
        m_legacyApertureList = new ArrayList<Integer>();
        for (String s : myAper.split(";"))
            m_legacyApertureList.add(Integer.parseInt(s));
        m_curApert = Integer.parseInt(m_prefs.getLegacyAperture().toString());
        //Focals Stored
        m_curFocal = Integer.parseInt(m_prefs.getLegacyFocal().toString());

        m_tvLegacyAperture.setText("F/" + Float.toString(m_curApert / 10f));
        m_tvLegacyFocal.setText(Integer.toString(m_curFocal)+"mm");

        m_supportedIsos = (List<Integer>)paramsModifier.getSupportedISOSensitivities();
        m_curIso = paramsModifier.getISOSensitivity();
        m_tvISO.setText(String.format("\uE488 %d", m_curIso));

        Pair<Integer, Integer> sp = paramsModifier.getShutterSpeed();
        updateShutterSpeed(sp.first, sp.second);

        m_supportedPreviewMagnifications = (List<Integer>)paramsModifier.getSupportedPreviewMagnification();
        m_camera.setPreviewMagnificationListener(new CameraEx.PreviewMagnificationListener()
        {
            @Override
            public void onChanged(boolean enabled, int magFactor, int magLevel, Pair coords, CameraEx cameraEx)
            {
                if (enabled)
                {
                    m_curPreviewMagnification = magLevel;
                    m_curPreviewMagnificationFactor = ((float)magFactor / 100.0f);
                    m_curPreviewMagnificationMaxPos = 1000 - (int)(1000.0f / m_curPreviewMagnificationFactor);
                    m_tvMagnification.setText(String.format("\uE012 %.2fx", (float)magFactor / 100.0f));
                    m_previewNavView.update(coords, m_curPreviewMagnificationFactor);
                }
                else
                {
                    m_previewNavView.update(null, 0);
                    m_curPreviewMagnification = 0;
                    m_curPreviewMagnificationMaxPos = 0;
                    m_curPreviewMagnificationFactor = 0;
                }
                togglePreviewMagnificationViews(enabled);
            }

            @Override
            public void onInfoUpdated(boolean b, Pair coords, CameraEx cameraEx)
            {
                // Useless?
                /*
                //LLUtils.log("onInfoUpdated b:" + String.valueOf(b) +
                               " x:" + coords.first + " y:" + coords.second + "\n");
                */
            }
        });


        loadDefaults();
        updateDriveModeImage();
        updateSceneModeImage();
        updateViewVisibility();


        // write EXIF on photos if camera supports it
        if (LLUtils.isOldLensAttached(m_camera)) {
            legacyLensName = m_tvLegacyLensName.getText().toString();
            legacyLensAperture = LLUtils.getApertureFromSV(m_tvLegacyAperture.getText().toString());
            legacyLensSpecial = m_tvLegacySpecial.getText().toString();
            legacyLensFocal = LLUtils.getFocalFromSV(m_tvLegacyFocal.getText().toString());
            if (legacyLensSpecial != "")  {
                legacyLensAll = legacyLensName + " & " + legacyLensSpecial;}
                else {
                legacyLensAll = legacyLensName;
            }

            //update EXIF tags if supported by camera API
            if (LLUtils.isEXIFSupported()) {

                m_exifInfo.writeMode = true;

                if (legacyLensAll != "" && legacyLensAll.matches("\\p{ASCII}*")) {
                    m_exifInfo.lensName = legacyLensAll + '\u0000';
                } else {
                    m_exifInfo.lensName = "----\u0000";
                }

                if (legacyLensFocal != "" && legacyLensFocal.length() > 0) {
                    m_exifInfo.focalLengthNumer = Integer.parseInt(legacyLensFocal) * 10;
                } else {
                    m_exifInfo.focalLengthNumer = 0;
                }
                m_exifInfo.focalLengthDenom = 10;


                try {
                    m_exifInfo.fNumberNumer = (int) (Double.parseDouble(legacyLensAperture) * 100.0D + 0.5D);
                } catch (Exception var1) {
                    m_exifInfo.fNumberNumer = 0;
                }
                //LLUtils.log("aperture: " + String.valueOf(m_exifInfo.fNumberNumer) + "\n");
                m_exifInfo.fNumberDenom = 100;

                m_exifInfo.fNumberMinNumer = (int) (Double.valueOf(0.0D).doubleValue() * 1000.0D);
                m_exifInfo.fNumberMinDenom = 1000;

                m_camera.setExifInfo(m_exifInfo);
                m_exifInfo.writeMode= false;

            }
        }


    }

    @Override
    protected void onPause()
    {
        super.onPause();

        saveDefaults();

        m_surfaceHolder.removeCallback(this);
        m_autoReviewControl.setPictureReviewTime(m_pictureReviewTime);
        m_camera.setAutoPictureReviewControl(null);
        m_camera.getNormalCamera().stopPreview();
        m_camera.release();
        m_camera = null;
    }



    private void saveDefaults()
    {
        final Camera.Parameters params = m_camera.getNormalCamera().getParameters();
        final CameraEx.ParametersModifier paramsModifier = m_camera.createParametersModifier(params);
        // Scene mode
        m_prefs.setSceneMode(params.getSceneMode());
        // Drive mode and burst speed
        m_prefs.setDriveMode(paramsModifier.getDriveMode());
        m_prefs.setBurstDriveSpeed(paramsModifier.getBurstDriveSpeed());
        // View visibility
        m_prefs.setViewFlags(m_viewFlags);

        m_prefs.setLegacyLensName(m_tvLegacyLensName.getText().toString());
        m_prefs.setLegacyFocal(String.valueOf(m_curFocal));
        m_prefs.setLegacyAperture(String.valueOf(m_curApert));

        // TODO: Dial mode
    }

    private void loadDefaults()
    {
        final Camera.Parameters params = m_camera.createEmptyParameters();
        final CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(params);
        // Scene mode
        final String sceneMode = m_prefs.getSceneMode();
        params.setSceneMode(sceneMode);
        // Drive mode and burst speed
        modifier.setDriveMode(m_prefs.getDriveMode());
        modifier.setBurstDriveSpeed(m_prefs.getBurstDriveSpeed());
        // Minimum shutter speed
        if (sceneMode.equals(CameraEx.ParametersModifier.SCENE_MODE_MANUAL_EXPOSURE))
            modifier.setAutoShutterSpeedLowLimit(-1);
        else
            modifier.setAutoShutterSpeedLowLimit(m_prefs.getMinShutterSpeed());
        // Disable self timer
        modifier.setSelfTimer(0);
        // Force aspect ratio to 3:2
        modifier.setImageAspectRatio(CameraEx.ParametersModifier.IMAGE_ASPECT_RATIO_3_2);
        // Apply
        m_camera.getNormalCamera().setParameters(params);
        // View visibility
        m_viewFlags = m_prefs.getViewFlags(VIEW_FLAG_GRID | VIEW_FLAG_HISTOGRAM);
        m_legacyName = m_prefs.getLegacyLensName();
        m_tvLegacyLensName.setText(m_legacyName);
        m_tvLegacyFocal.setText(m_prefs.getLegacyFocal() + "mm");
        m_tvLegacyAperture.setText("F/" + Float.toString(Integer.parseInt(m_prefs.getLegacyAperture()) / 10f));


        // TODO: Dial mode?
        setDialMode(DialMode.shutter);
    }


    private class SurfaceSwipeTouchListener extends OnSwipeTouchListener
    {
        public SurfaceSwipeTouchListener(Context context)
        {
            super(context);
        }

        @Override
        public boolean onScrolled(float distanceX, float distanceY)
        {
            if (m_curPreviewMagnification != 0)
            {
                m_curPreviewMagnificationPos = new Pair<Integer, Integer>(Math.max(Math.min(m_curPreviewMagnificationMaxPos, m_curPreviewMagnificationPos.first + (int)distanceX), -m_curPreviewMagnificationMaxPos),
                        Math.max(Math.min(m_curPreviewMagnificationMaxPos, m_curPreviewMagnificationPos.second + (int)distanceY), -m_curPreviewMagnificationMaxPos));
                m_camera.setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
                return true;
            }
            return false;
        }
    }

    private class ApertureSwipeTouchListener extends OnSwipeTouchListener
    {
        private int m_lastDistance;
        private int m_accumulatedDistance;

        public ApertureSwipeTouchListener(Context context)
        {
            super(context);
        }

        @Override
        public boolean onScrolled(float distanceX, float distanceY)
        {
            if (m_curApert != 0)
            {
                final int distance = (int)(Math.abs(distanceX) > Math.abs(distanceY) ? distanceX : -distanceY);
                if ((m_lastDistance > 0) != (distance > 0))
                    m_accumulatedDistance = distance;
                else
                    m_accumulatedDistance += distance;
                m_lastDistance = distance;
                if (Math.abs(m_accumulatedDistance) > 10)
                {
                    int ap = m_curApert;
                    for (int i = Math.abs(m_accumulatedDistance); i > 10; i -= 10)
                        ap = distance > 0 ? getPreviousFstop(ap) : getNextFstop(ap);
                    m_accumulatedDistance = 0;
                    if (ap != 0)
                    {
                        setLegacyAperture(ap);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private class ShutterSwipeTouchListener extends OnSwipeTouchListener
    {
        private int m_lastDistance;
        private int m_accumulatedDistance;

        public ShutterSwipeTouchListener(Context context)
        {
            super(context);
        }

        @Override
        public boolean onScrolled(float distanceX, float distanceY)
        {
            if (m_curIso != 0)
            {
                final int distance = (int)(Math.abs(distanceX) > Math.abs(distanceY) ? distanceX : -distanceY);
                if ((m_lastDistance > 0) != (distance > 0))
                    m_accumulatedDistance = distance;
                else
                    m_accumulatedDistance += distance;
                m_lastDistance = distance;
                if (Math.abs(m_accumulatedDistance) > 10)
                {
                    for (int i = Math.abs(m_accumulatedDistance); i > 10; i -= 10)
                    {
                        m_notifyOnNextShutterSpeedChange = true;
                        if (distance > 0)
                            m_camera.decrementShutterSpeed();
                        else
                            m_camera.incrementShutterSpeed();
                    }
                    m_accumulatedDistance = 0;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onClick()
        {
            if (m_sceneMode == SceneMode.aperture)
            {
                // Set minimum shutter speed
                startActivity(new Intent(getApplicationContext(), MinShutterActivity.class));
                return true;
            }
            if (m_dialMode == DialMode.legacyfocal)
            {
                // Set focal lenght
                startActivity(new Intent(getApplicationContext(), SetFocalLenghtActivity.class));
                return true;
            }

            return false;
        }
    }

    private class ExposureSwipeTouchListener extends OnSwipeTouchListener
    {
        private int m_lastDistance;
        private int m_accumulatedDistance;

        public ExposureSwipeTouchListener(Context context)
        {
            super(context);
        }

        @Override
        public boolean onScrolled(float distanceX, float distanceY)
        {
            if (m_curIso != 0)
            {
                final int distance = (int)(Math.abs(distanceX) > Math.abs(distanceY) ? distanceX : -distanceY);
                if ((m_lastDistance > 0) != (distance > 0))
                    m_accumulatedDistance = distance;
                else
                    m_accumulatedDistance += distance;
                m_lastDistance = distance;
                if (Math.abs(m_accumulatedDistance) > 10)
                {
                    for (int i = Math.abs(m_accumulatedDistance); i > 10; i -= 10)
                    {
                        if (distance > 0)
                            decrementExposureCompensation(true);
                        else
                            incrementExposureCompensation(true);
                    }
                    m_accumulatedDistance = 0;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onClick()
        {
            // Reset exposure compensation
            setExposureCompensation(0);
            return true;
        }
    }

    private class IsoSwipeTouchListener extends OnSwipeTouchListener
    {
        private int m_lastDistance;
        private int m_accumulatedDistance;

        public IsoSwipeTouchListener(Context context)
        {
            super(context);
        }

        @Override
        public boolean onScrolled(float distanceX, float distanceY)
        {
            if (m_curIso != 0)
            {
                final int distance = (int)(Math.abs(distanceX) > Math.abs(distanceY) ? distanceX : -distanceY);
                if ((m_lastDistance > 0) != (distance > 0))
                    m_accumulatedDistance = distance;
                else
                    m_accumulatedDistance += distance;
                m_lastDistance = distance;
                if (Math.abs(m_accumulatedDistance) > 10)
                {
                    int iso = m_curIso;
                    for (int i = Math.abs(m_accumulatedDistance); i > 10; i -= 10)
                        iso = distance > 0 ? getPreviousIso(iso) : getNextIso(iso);
                    m_accumulatedDistance = 0;
                    if (iso != 0)
                    {
                        setIso(iso);
                        showMessage(String.format("\uE488 %d", iso));
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onClick()
        {
            // Toggle manual / automatic ISO
            setIso(m_curIso == 0 ? getFirstManualIso() : 0);
            showMessage(m_curIso == 0 ? "Auto \uE488" : "Manual \uE488");
            return true;
        }
    }


    private void showMessage(String msg)
    {
        m_tvMsg.setText(msg);
        m_tvMsg.setVisibility(View.VISIBLE);
        m_handler.removeCallbacks(m_hideMessageRunnable);
        m_handler.postDelayed(m_hideMessageRunnable, MESSAGE_TIMEOUT);
    }




    private void setLegacyAperture(int ap)
    {
        m_curApert = ap;
        if (ap<100)
            m_tvLegacyAperture.setText("F/" + Float.toString(ap / 10f)); //with decimal
        else
            m_tvLegacyAperture.setText("F/" + Integer.toString(ap / 10)); //no decimal needed
    }

    private int getPreviousFstop(int current)
    {
        int previous = 0;
        for (Integer ap : m_legacyApertureList)
        {
            if (ap == current)
                return previous;
            else
                previous = ap;
        }
        return 0;
    }

    private int getNextFstop(int current)
    {
        boolean next = false;
        for (Integer ap : m_legacyApertureList)
        {
            if (next)
                return ap;
            else if (ap == current)
                next = true;
        }
        return current;
    }

    private void setIso(int iso)
    {
        //LLUtils.log("setIso: " + String.valueOf(iso) + "\n");
        m_curIso = iso;
        m_tvISO.setText(String.format("\uE488 %s", (iso == 0 ? "AUTO" : String.valueOf(iso))));
        Camera.Parameters params = m_camera.createEmptyParameters();
        m_camera.createParametersModifier(params).setISOSensitivity(iso);
        m_camera.getNormalCamera().setParameters(params);
    }

    private int getPreviousIso(int current)
    {
        int previous = 0;
        for (Integer iso : m_supportedIsos)
        {
            if (iso == current)
                return previous;
            else
                previous = iso;
        }
        return 0;
    }

    private int getNextIso(int current)
    {
        boolean next = false;
        for (Integer iso : m_supportedIsos)
        {
            if (next)
                return iso;
            else if (iso == current)
                next = true;
        }
        return current;
    }

    private int getFirstManualIso()
    {
        for (Integer iso : m_supportedIsos)
        {
            if (iso != 0)
                return iso;
        }
        return 0;
    }

    private void updateShutterSpeed(int n, int d)
    {
        final String text = CameraUtil.formatShutterSpeed(n, d);
        m_tvShutter.setText(text);
        if (m_notifyOnNextShutterSpeedChange)
        {
            showMessage(text);
            m_notifyOnNextShutterSpeedChange = false;
        }
    }

    private void setExposureCompensation(int value)
    {
        m_curExposureCompensation = value;
        Camera.Parameters params = m_camera.createEmptyParameters();
        params.setExposureCompensation(value);
        m_camera.getNormalCamera().setParameters(params);
        updateExposureCompensation(false);
    }

    private void decrementExposureCompensation(boolean notify)
    {
        if (m_curExposureCompensation > m_minExposureCompensation)
        {
            --m_curExposureCompensation;

            Camera.Parameters params = m_camera.createEmptyParameters();
            params.setExposureCompensation(m_curExposureCompensation);
            m_camera.getNormalCamera().setParameters(params);

            updateExposureCompensation(notify);
        }
    }

    private void incrementExposureCompensation(boolean notify)
    {
        if (m_curExposureCompensation < m_maxExposureCompensation)
        {
            ++m_curExposureCompensation;

            Camera.Parameters params = m_camera.createEmptyParameters();
            params.setExposureCompensation(m_curExposureCompensation);
            m_camera.getNormalCamera().setParameters(params);

            updateExposureCompensation(notify);
        }
    }

    private void updateExposureCompensation(boolean notify)
    {
        final String text;
        if (m_curExposureCompensation == 0)
            text = "\uEB18\u00B10.0";
        else if (m_curExposureCompensation > 0)
            text = String.format("\uEB18+%.1f", m_curExposureCompensation * m_exposureCompensationStep);
        else
            text = String.format("\uEB18%.1f", m_curExposureCompensation * m_exposureCompensationStep);
        m_tvExposureCompensation.setText(text);
        if (notify)
            showMessage(text);
    }

    private void updateSceneModeImage(String mode)
    {
        //LLUtils.log(String.format("updateSceneModeImage %s\n", mode));
        if (mode.equals(CameraEx.ParametersModifier.SCENE_MODE_MANUAL_EXPOSURE))
        {
            //noinspection ResourceType
            m_ivMode.setImageResource(SonyDrawables.s_16_dd_parts_osd_icon_mode_m);
            m_sceneMode = SceneMode.manual;
        }
        else if (mode.equals(CameraEx.ParametersModifier.SCENE_MODE_APERTURE_PRIORITY))
        {
            //noinspection ResourceType
            m_ivMode.setImageResource(SonyDrawables.s_16_dd_parts_osd_icon_mode_a);
            m_sceneMode = SceneMode.aperture;
        }
        else if (mode.equals(CameraEx.ParametersModifier.SCENE_MODE_SHUTTER_PRIORITY))
        {
            //noinspection ResourceType
            m_ivMode.setImageResource(SonyDrawables.s_16_dd_parts_osd_icon_mode_s);
            m_sceneMode = SceneMode.shutter;
        }
        else
        {
            //noinspection ResourceType
            m_ivMode.setImageResource(SonyDrawables.p_dialogwarning);
            m_sceneMode = SceneMode.other;
        }
    }

    private void updateViewVisibility()
    {
        m_vHist.setVisibility((m_viewFlags & VIEW_FLAG_HISTOGRAM) != 0 ? View.VISIBLE : View.GONE);
        m_vGrid.setVisibility((m_viewFlags & VIEW_FLAG_GRID) != 0 ? View.VISIBLE : View.GONE);
        m_lExposure.setVisibility((m_viewFlags & VIEW_FLAG_EXPOSURE) != 0 ? View.VISIBLE : View.GONE);
    }

    private void cycleVisibleViews()
    {
        if (++m_viewFlags > VIEW_FLAG_MASK)
            m_viewFlags = 0;
        updateViewVisibility();
    }

    private void updateSceneModeImage()
    {
        updateSceneModeImage(m_camera.getNormalCamera().getParameters().getSceneMode());
    }

    private void toggleSceneMode()
    {
        final String newMode;
        switch (m_sceneMode)
        {
            case manual:
                newMode = CameraEx.ParametersModifier.SCENE_MODE_APERTURE_PRIORITY;
                if (m_dialMode != DialMode.mode)
                    setDialMode(DialMode.legacyaperture);
                setMinShutterSpeed(m_prefs.getMinShutterSpeed());
                break;
            default:
                newMode = CameraEx.ParametersModifier.SCENE_MODE_MANUAL_EXPOSURE;
                if (m_dialMode != DialMode.mode)
                    setDialMode(DialMode.shutter);
                setMinShutterSpeed(-1);
                break;
        }
        setSceneMode(newMode);
    }

    private void toggleDriveMode()
    {
        final Camera normalCamera = m_camera.getNormalCamera();
        final CameraEx.ParametersModifier paramsModifier = m_camera.createParametersModifier(normalCamera.getParameters());
        final String driveMode = paramsModifier.getDriveMode();
        final String newMode;
        final String newBurstSpeed;
        if (driveMode.equals(CameraEx.ParametersModifier.DRIVE_MODE_SINGLE))
        {
            newMode = CameraEx.ParametersModifier.DRIVE_MODE_BURST;
            newBurstSpeed = CameraEx.ParametersModifier.BURST_DRIVE_SPEED_HIGH;
        }
        else if (driveMode.equals(CameraEx.ParametersModifier.DRIVE_MODE_BURST))
        {
            final String burstDriveSpeed = paramsModifier.getBurstDriveSpeed();
            if (burstDriveSpeed.equals(CameraEx.ParametersModifier.BURST_DRIVE_SPEED_LOW))
            {
                newMode = CameraEx.ParametersModifier.DRIVE_MODE_SINGLE;
                newBurstSpeed = burstDriveSpeed;
            }
            else
            {
                newMode = driveMode;
                newBurstSpeed = CameraEx.ParametersModifier.BURST_DRIVE_SPEED_LOW;
            }
        }
        else
        {
            // Anything else...
            newMode = CameraEx.ParametersModifier.DRIVE_MODE_SINGLE;
            newBurstSpeed = CameraEx.ParametersModifier.BURST_DRIVE_SPEED_HIGH;
        }

        final Camera.Parameters params = m_camera.createEmptyParameters();
        final CameraEx.ParametersModifier newParamsModifier = m_camera.createParametersModifier(params);
        newParamsModifier.setDriveMode(newMode);
        newParamsModifier.setBurstDriveSpeed(newBurstSpeed);
        m_camera.getNormalCamera().setParameters(params);

        updateDriveModeImage();
    }

    private void updateDriveModeImage()
    {
        final CameraEx.ParametersModifier paramsModifier = m_camera.createParametersModifier(m_camera.getNormalCamera().getParameters());
        final String driveMode = paramsModifier.getDriveMode();
        if (driveMode.equals(CameraEx.ParametersModifier.DRIVE_MODE_SINGLE))
        {
            //noinspection ResourceType
            m_ivDriveMode.setImageResource(SonyDrawables.p_drivemode_n_001);
        }
        else if (driveMode.equals(CameraEx.ParametersModifier.DRIVE_MODE_BURST))
        {
            final String burstDriveSpeed = paramsModifier.getBurstDriveSpeed();
            if (burstDriveSpeed.equals(CameraEx.ParametersModifier.BURST_DRIVE_SPEED_LOW))
            {
                //noinspection ResourceType
                m_ivDriveMode.setImageResource(SonyDrawables.p_drivemode_n_003);
            }
            else if (burstDriveSpeed.equals(CameraEx.ParametersModifier.BURST_DRIVE_SPEED_HIGH))
            {
                //noinspection ResourceType
                m_ivDriveMode.setImageResource(SonyDrawables.p_drivemode_n_002);
            }
        }

    }

    private void dumpList(List list, String name)
    {
        LLUtils.log(name);
        LLUtils.log(": ");
        if (list != null)
        {
            for (Object o : list)
            {
                LLUtils.log(o.toString());
                LLUtils.log(" ");
            }
        }
        else
            LLUtils.log("null");
        LLUtils.log("\n");
    }

    private void togglePreviewMagnificationViews(boolean magnificationActive)
    {
        m_previewNavView.setVisibility(magnificationActive ? View.VISIBLE : View.GONE);
        m_tvMagnification.setVisibility(magnificationActive ? View.VISIBLE : View.GONE);
        m_lInfoBottom.setVisibility(magnificationActive ? View.GONE : View.VISIBLE);
        m_vHist.setVisibility(magnificationActive ? View.GONE : View.VISIBLE);
         setRightViewVisibility(!magnificationActive);
    }

    private void setSceneMode(String mode)
    {
        Camera.Parameters params = m_camera.createEmptyParameters();
        params.setSceneMode(mode);
        m_camera.getNormalCamera().setParameters(params);
        updateSceneModeImage(mode);
    }


    private void setMinShutterSpeed(int speed)
    {
        final Camera.Parameters params = m_camera.createEmptyParameters();
        final CameraEx.ParametersModifier modifier = m_camera.createParametersModifier(params);
        modifier.setAutoShutterSpeedLowLimit(speed);
        m_camera.getNormalCamera().setParameters(params);
    }



    @Override
    public void onShutterSpeedChange(CameraEx.ShutterSpeedInfo shutterSpeedInfo, CameraEx cameraEx)
    {
        updateShutterSpeed(shutterSpeedInfo.currentShutterSpeed_n, shutterSpeedInfo.currentShutterSpeed_d);

    }


    @Override
    public void onShutter(int i, CameraEx cameraEx)
    {

        // i: 0 = success, 1 = canceled, 2 = error
       if (i==0) {
           m_takingPicture = true;
       }
        m_camera.cancelTakePicture();

    }



    // OnClickListener
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.ivDriveMode:
                toggleDriveMode();
                break;
            case R.id.ivMode:
                toggleSceneMode();
                break;
            case R.id.tv_LegacyFocal:
                startActivity(new Intent(getApplicationContext(), SetFocalLenghtActivity.class));
                break;
        }
    }





    private Pair<Integer, Integer> getCurrentShutterSpeed()
    {
        final Camera.Parameters params = m_camera.getNormalCamera().getParameters();
        final CameraEx.ParametersModifier paramsModifier = m_camera.createParametersModifier(params);
        return paramsModifier.getShutterSpeed();
    }



    private void setRightViewVisibility(boolean visibile)
    {
        final int visibility = visibile ? View.VISIBLE : View.GONE;
        m_ivDriveMode.setVisibility(visibility);
        m_ivMode.setVisibility(visibility);
    }

    private void logLegacyExif()
    {
        String legacyLensName = "";
        String legacyLensAperture = "";
        String legacyLensSpecial = "";
        String isoRatings = "";
        String shutterSpeed = "";
        String legacyLensAll ="";
        String legacyLensFocal = "";
        Calendar calendar = getDateTime().getCurrentTime();
        String recDate;

        legacyLensName = m_tvLegacyLensName.getText().toString();
        legacyLensAperture = m_tvLegacyAperture.getText().toString();
        legacyLensSpecial = m_tvLegacySpecial.getText().toString();
        legacyLensFocal = m_tvLegacyFocal.getText().toString();
        legacyLensAll = legacyLensName + " & " +legacyLensSpecial;
        shutterSpeed = m_tvShutter.getText().toString();
        isoRatings = "ISO " + LLUtils.getIsoFromSV(m_tvISO.getText().toString());

        recDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());

        //save infos into LOG.TXT
        Logger.exif(m_pictureID + ";" + recDate + ";" + shutterSpeed + ";" + legacyLensAperture + ";" + isoRatings + ";" + legacyLensName + ";" + legacyLensSpecial);

    }

    @Override
    protected boolean onLowerDialChanged(int value)
    {
        if (m_curPreviewMagnification != 0)
        {
            movePreviewHorizontal(value * (int)(500.0f / m_curPreviewMagnificationFactor));
            return true;
        }
        else
        {
            switch (m_dialMode)
            {
                case shutter:
                    if (value > 0)
                        m_camera.incrementShutterSpeed();
                    else
                        m_camera.decrementShutterSpeed();
                    break;

                case legacyaperture:
                    final int ap = value <= 0 ? getPreviousFstop(m_curApert) : getNextFstop(m_curApert);
                    if (ap != 0)
                        setLegacyAperture(ap);
                        //LLUtils.log(Integer.toString(ap)+'\n');
                        m_exifInfo.fNumberNumer = ap*10;
                        m_exifInfo.writeMode = true;
                        m_camera.setExifInfo(m_exifInfo);
                        m_exifInfo.writeMode = false;
                    break;

                case iso:
                    final int iso = value <= 0 ? getPreviousIso(m_curIso) : getNextIso(m_curIso);
                    if (iso != 0)
                        setIso(iso);
                    break;
                case exposure:
                    if (value <= 0)
                        decrementExposureCompensation(false);
                    else
                        incrementExposureCompensation(false);
                    break;

                case mode:
                case legacyfocal:
                case drive:
                    //quickly change to Shutter Mode (or Aperture) if Upper-dial used in above modes
                    if (m_sceneMode == SceneMode.manual)
                        setDialMode(DialMode.shutter);
            }
            return true;
        }
    }


    @Override
    protected boolean onUpperDialChanged(int value)
    {
        if (m_curPreviewMagnification != 0)
        {
            movePreviewHorizontal(value * (int)(500.0f / m_curPreviewMagnificationFactor));
            return true;
        }
        else
        {
            switch (m_dialMode)
            {
                case shutter:
                    if (value > 0)
                        m_camera.incrementShutterSpeed();
                    else
                        m_camera.decrementShutterSpeed();
                    break;

                case legacyaperture:
                    final int ap = value <= 0 ? getPreviousFstop(m_curApert) : getNextFstop(m_curApert);
                    if (ap != 0)
                        setLegacyAperture(ap);
                        m_exifInfo.fNumberNumer = ap*10;
                        m_exifInfo.writeMode = true;
                        m_camera.setExifInfo(m_exifInfo);
                        m_exifInfo.writeMode = false;
                    break;

                case iso:
                    final int iso = value <= 0 ? getPreviousIso(m_curIso) : getNextIso(m_curIso);
                    if (iso != 0)
                        setIso(iso);
                    break;
                case exposure:
                    if (value <= 0)
                        decrementExposureCompensation(false);
                    else
                        incrementExposureCompensation(false);
                    break;

                case mode:
                case legacyfocal:
                case drive:
                    //quickly change to Shutter Mode (or Aperture) if Upper-dial used in above modes
                    if (m_sceneMode == SceneMode.manual)
                        setDialMode(DialMode.shutter);
            }
            return true;
        }
    }

    private void setDialMode(DialMode newMode)
    {
        m_dialMode = newMode;
        m_tvShutter.setTextColor(newMode == DialMode.shutter ? Color.GREEN : Color.WHITE);
        m_tvISO.setTextColor(newMode == DialMode.iso ? Color.GREEN : Color.WHITE);
        m_tvExposureCompensation.setTextColor(newMode == DialMode.exposure ? Color.GREEN : Color.WHITE);
        m_tvLegacyAperture.setTextColor(newMode == DialMode.legacyaperture ? Color.GREEN : Color.WHITE);
        m_tvLegacyFocal.setTextColor(newMode == DialMode.legacyfocal ? Color.GREEN : Color.WHITE);

        if (newMode == DialMode.mode)
            m_ivMode.setColorFilter(Color.GREEN);
        else
            m_ivMode.setColorFilter(null);
        if (newMode == DialMode.drive)
            m_ivDriveMode.setColorFilter(Color.GREEN);
        else
            m_ivDriveMode.setColorFilter(null);
    }

    private void movePreviewVertical(int delta)
    {
        int newY = m_curPreviewMagnificationPos.second + delta;
        if (newY > m_curPreviewMagnificationMaxPos)
            newY = m_curPreviewMagnificationMaxPos;
        else if (newY < -m_curPreviewMagnificationMaxPos)
            newY = -m_curPreviewMagnificationMaxPos;
        m_curPreviewMagnificationPos = new Pair<Integer, Integer>(m_curPreviewMagnificationPos.first, newY);
        m_camera.setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
    }

    private void movePreviewHorizontal(int delta)
    {
        int newX = m_curPreviewMagnificationPos.first + delta;
        if (newX > m_curPreviewMagnificationMaxPos)
            newX = m_curPreviewMagnificationMaxPos;
        else if (newX < -m_curPreviewMagnificationMaxPos)
            newX = -m_curPreviewMagnificationMaxPos;
        m_curPreviewMagnificationPos = new Pair<Integer, Integer>(newX, m_curPreviewMagnificationPos.second);
        m_camera.setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
    }

    @Override
    protected boolean onEnterKeyUp()
    {
        return true;
    }

    @Override
    protected boolean onEnterKeyDown()
    {


        if (m_curPreviewMagnification != 0)
        {
            if (m_zoomLeverPressed = true) {

                //return to center in PreviewMagnification
                m_curPreviewMagnificationPos = new Pair<Integer, Integer>(0, 0);
                m_camera.setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
                return true;
            }
        }
        else if (m_dialMode == DialMode.iso)
        {
            // Toggle manual / automatic ISO
            setIso(m_curIso == 0 ? getFirstManualIso() : 0);
            return true;
        }
        else if (m_dialMode == DialMode.shutter && m_sceneMode == SceneMode.aperture)
        {
            // Set minimum shutter speed
            startActivity(new Intent(getApplicationContext(), MinShutterActivity.class));
            return true;
        }
        else if (m_dialMode == DialMode.exposure)
        {
            // Reset exposure compensation
            setExposureCompensation(0);
            return true;
        }
        else if (m_dialMode == DialMode.mode)
        {
            toggleSceneMode();
            return true;
        }
        else if (m_dialMode == DialMode.drive)
        {
            toggleDriveMode();
            return true;
        }
        else  if (m_dialMode == DialMode.legacyfocal)
        {
            // Set focal lenght
            startActivity(new Intent(getApplicationContext(), SetFocalLenghtActivity.class));
            return true;
        }
        return false;
    }

    @Override
    protected boolean onUpKeyDown()
    {
        return true;
    }

    @Override
    protected boolean onUpKeyUp()
    {
        if (m_curPreviewMagnification != 0)
        {
            movePreviewVertical((int)(-500.0f / m_curPreviewMagnificationFactor));
            return true;
        }
        else
        {
            // Toggle visibility of some views
            cycleVisibleViews();
            return true;
        }
    }

    @Override
    protected boolean onDownKeyDown()
    {
        return true;
    }

    @Override
    protected boolean onDownKeyUp()
    {
        if (m_curPreviewMagnification != 0)
        {
            movePreviewVertical((int)(500.0f / m_curPreviewMagnificationFactor));
            return true;
        }
        else
        {
            switch (m_dialMode)
            {
                case shutter:
                        setDialMode(DialMode.legacyaperture);
                        break;

                case legacyaperture:
                    setDialMode(DialMode.iso);
                    break;
                case iso:
                    setDialMode(DialMode.exposure);
                    break;
                case exposure:
                    setDialMode(m_haveTouchscreen ? DialMode.shutter : DialMode.mode);
                    break;
                case mode:
                    setDialMode(DialMode.drive);
                    break;
                case drive:
                    setDialMode(m_legacyZoom ? DialMode.legacyfocal : DialMode.shutter);
                    break;
                case legacyfocal:
                    setDialMode(DialMode.shutter);
                    break;
            }
            return true;
        }
    }

    @Override
    protected boolean onLeftKeyDown()
    {
        return true;
    }

    @Override
    protected boolean onLeftKeyUp()
    {
        if (m_curPreviewMagnification != 0)
        {
            movePreviewHorizontal((int)(-500.0f / m_curPreviewMagnificationFactor));
            return true;
        }
        else if (m_prefs.getSettingUseLeftRightKeys().equalsIgnoreCase("YES"))
            //return to Shutter DialMode
            setDialMode(DialMode.shutter);
        return false;

    }

    @Override
    protected boolean onRightKeyDown()
    {
        return true;
    }

    @Override
    protected boolean onRightKeyUp()
    {
        if (m_curPreviewMagnification != 0)
        {
            movePreviewHorizontal((int)(500.0f / m_curPreviewMagnificationFactor));
            return true;
        }
        else if (m_prefs.getSettingUseLeftRightKeys().equalsIgnoreCase("YES"))
            //Set to ISO DialMode
            setDialMode(DialMode.iso);
        return false;
    }

    @Override
    protected boolean onShutterKeyUp()
    {
        m_shutterKeyDown = false;
        return true;
    }

    @Override
    protected boolean onShutterKeyDown()
    {

        m_shutterKeyDown = true;
        return true;
    }

    @Override
    protected boolean onPlayKeyDown()
    {
        // Playback activity
        startActivity(new Intent(getApplicationContext(), PlaybackActivity.class));
        return true;
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final int scanCode = event.getScanCode();
        //LLUtils.log("ScanCode: " + String.valueOf(scanCode) + "\n");

        if (scanCode == 610 && !m_zoomLeverPressed) {
            // zoom lever tele
            m_zoomLeverPressed = true;
            if (m_curPreviewMagnification == 0) {
                m_curPreviewMagnification = 100;
            } else
                m_curPreviewMagnification = 200;
            m_camera.setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
            return true;
        } else if (scanCode == 611 && !m_zoomLeverPressed) {
            // zoom lever wide
            m_zoomLeverPressed = true;
            if (m_curPreviewMagnification == 200) {
                m_curPreviewMagnification = 100;
                m_camera.setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
            } else {
                m_curPreviewMagnification = 0;
                m_camera.stopPreviewMagnification();
            }
            return true;
        } else if (scanCode == 645) {
            // zoom lever returned to neutral position
            m_zoomLeverPressed = false;
            return true;


        }   // KEY-ONE: Preview Magnification
           else if (scanCode == LLUtils.Model_FUNCTIONKEYS_Map.get(m_cameraModel)[0]) {

            m_zoomLeverPressed = true;
            if ((m_curPreviewMagnification == 0) || (m_curPreviewMagnification == 200))
                m_curPreviewMagnification = 100;
            else
                m_curPreviewMagnification = 200;
                m_camera.setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
                return true;

        }  //KEY-TWO
           else if (scanCode == LLUtils.Model_FUNCTIONKEYS_Map.get(m_cameraModel)[1]) {
            // start intent Menu Activity
            m_zoomLeverPressed = false;
            m_curPreviewMagnification = 0;
            m_camera.stopPreviewMagnification();
            startActivity(new Intent(getApplicationContext(), MenuActivity.class));
            return true;
        }


        //KEY-THREE
        //....

        //KEY-PLAYBACK

        return super.onKeyDown(keyCode, event);

    };




    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        try
        {
            Camera cam = m_camera.getNormalCamera();
            cam.setPreviewDisplay(holder);
            cam.startPreview();
        }
        catch (IOException e)
        {
            m_tvMsg.setText("Error starting preview!");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        //LLUtils.log(String.format("surfaceChanged width %d height %d\n", width, height));

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}


    @Override
    public void onDisplayChanged(String device){
        //super.getDisplayManager().
       // LLUtils.log("Display changed! " + device + "\n");
        //redraw GridView to be adjusted to the new screen dimensions
        DisplayManager.VideoRect vr = getDisplayManager().getDisplayedVideoRect();
        //LLUtils.log(device + " ->pxRight: " + Integer.toString(vr.pxRight) + "\n");
        m_vGrid.setVideoRect(vr);



    }


    @Override
    protected void setColorDepth(boolean highQuality)
    {
        super.setColorDepth(false);
    }


    protected boolean doMagnifByZoomLevel (int code){
        if (code == 610 && !m_zoomLeverPressed)
        {
            // zoom lever tele
            m_zoomLeverPressed = true;
            if (m_curPreviewMagnification == 0)
            {
                m_curPreviewMagnification = 100;
            }
            else
                m_curPreviewMagnification = 200;
            m_camera.setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
            return true;
        }
        else if (code == 611 && !m_zoomLeverPressed)
        {
            // zoom lever wide
            m_zoomLeverPressed = true;
            if (m_curPreviewMagnification == 200)
            {
                m_curPreviewMagnification = 100;
                m_camera.setPreviewMagnification(m_curPreviewMagnification, m_curPreviewMagnificationPos);
            }
            else
            {
                m_curPreviewMagnification = 0;
                m_camera.stopPreviewMagnification();
            }
            return true;
        }
        else if (code == 645)
        {
            // zoom lever returned to neutral position
            m_zoomLeverPressed = false;
            return true;
        }
        return false;
    }

    protected boolean isZoomLegacy (String minFocal, String maxFocal)
    {
        boolean test = false;

        if (Integer.valueOf(maxFocal) > (Integer.valueOf(minFocal)))
            test = true;

        return test;
    }

}
