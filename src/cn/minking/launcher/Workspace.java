package cn.minking.launcher;
/**
 * 作者：      minking
 * 文件名称:    Workspace.java
 * 创建时间：    2013
 * 描述：  
 * 更新内容
 * ====================================================================================
 * 20140228: WORKSPACE文件
 * ====================================================================================
 */
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

public class Workspace extends DragableScreenView 
    implements SensorEventListener{

    private ContentResolver mResolver;
    private final WallpaperManager mWallpaperManager;
    private final LayoutInflater mInflater;
    private boolean mShowEditingIndicator = false;
    private Launcher mLauncher;
    private int mInEditingMode = 0;
    private int mOldTransitionType;
    private DragController mDragController;
    
    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mResolver = context.getContentResolver();
        mWallpaperManager = WallpaperManager.getInstance(context);
        mInflater = ((LayoutInflater)context.getSystemService("layout_inflater"));
        Resources localResources = getResources();
        String str = localResources.getString(R.string.home_indicator);
        FrameLayout.LayoutParams localLayoutParams 
            = new FrameLayout.LayoutParams(0, localResources.getDimensionPixelSize(R.dimen.slide_bar_height));
        if (!str.equals("bottom_point")) {
            if (!str.equals("top_point")) {
                if (str.equals("slider")) {
                    localLayoutParams.width = -1;
                    localLayoutParams.gravity = 80;
                    setSlideBarPosition(localLayoutParams);
                    mShowEditingIndicator = false;
                }
            }else {
                localLayoutParams.width = -2;
                localLayoutParams.gravity = 49;
                localLayoutParams.topMargin = localResources.getDimensionPixelSize(R.dimen.status_bar_height);
                setSeekBarPosition(localLayoutParams);
                mShowEditingIndicator = false;
            }
        }else {
            localLayoutParams.width = -2;
            localLayoutParams.gravity = 81;
            setSeekPointResource(R.drawable.workspace_seekpoint);
            setSeekBarPosition(localLayoutParams);
            mShowEditingIndicator = true;
        }
        setAnimationCacheEnabled(false);
        setMaximumSnapVelocity(6000);
        setClipChildren(false);
    }

    public Workspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
    }

    public void loadScreens(boolean flag){
        
    }

    @Override
    protected void onAttachedToWindow() {
        // TODO Auto-generated method stub
        super.onAttachedToWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onPinchOut(ScaleGestureDetector detector) {
        // TODO Auto-generated method stub
        super.onPinchOut(detector);
    }

    @Override
    public void onPinchIn(ScaleGestureDetector detector) {
        // TODO Auto-generated method stub
        super.onPinchIn(detector);
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        
    }

    private void unRegistAccelerometer() {
        SensorManager sensormanager = (SensorManager)getContext().getSystemService("sensor");
        if (sensormanager != null){
            sensormanager.unregisterListener(this);
        }
    }

    public void onDestory() {
        unRegistAccelerometer();
    }
    
    @Override
    protected boolean onRequestFocusInDescendants(int direction,
            Rect previouslyFocusedRect) {
        // TODO Auto-generated method stub
        return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
    }

    @Override
    protected void setCurrentScreenInner(int cur_screen) {
        // TODO Auto-generated method stub
        super.setCurrentScreenInner(cur_screen);
    }

    @Override
    public void snapToScreen(int screen) {
        // TODO Auto-generated method stub
        super.snapToScreen(screen);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.dispatchDraw(canvas);
    }
    
    private boolean isInNormalEditingMode() {
        boolean bFlag = false;
        if (mInEditingMode != 8) {
            bFlag = false;
        }else {
            bFlag = true;
        }
        return bFlag;
    }

    public void setDragController(DragController dragcontroller) {
        mDragController = dragcontroller;
    }
    
    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }
    
    public void onStart() {
        int i = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(mLauncher).getString("pref_key_transformation_type", String.valueOf(1))).intValue();
        if (!isInNormalEditingMode()) {
            setScreenTransitionType(i);
        }else {
            mOldTransitionType = i;
        }
    }
    
    public CellLayout getCurrentCellLayout(){
        return getCurrentCellScreen().getCellLayout();
    }
    
    public CellScreen getCurrentCellScreen(){
        return (CellScreen)getCurrentScreen();
    }
    
    void addInScreen(View view, long screenId, int cellX, int cellY, int spanX, int spanY){
        addInScreen(view, screenId, cellX, cellY, spanX, spanY, false);
    }

    void addInScreen(View view, long screenId, int cellX, int cellY, int spanX, int spanY, 
            boolean insert){
        
    }
    
}
