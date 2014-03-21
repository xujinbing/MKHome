package cn.minking.launcher;
/**
 * 作者：      minking
 * 文件名称:    HotSeats.java
 * 创建时间：    2014-02-25
 * 描述：  
 * 更新内容
 * ====================================================================================
 * 20140225: 创建桌面的HOTSEAT， HOTSEAT支持长按拖动图标
 * ====================================================================================
 */
import java.util.ArrayList;
import java.util.Iterator;

import cn.minking.launcher.AllAppsList.RemoveInfo;
import cn.minking.launcher.DropTarget.DragObject;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class HotSeats extends LinearLayout
    implements android.view.View.OnLongClickListener, DragSource, DropTarget{
    
    private static int MAX_SEATS = -1;
    private static final ItemInfo PLACE_HOLDER_SEAT = new ItemInfo();
    private Context mContext;
    private final ItemInfo mCurrentSeats[];
    private DragController mDragController;
    private ItemInfo mDraggingItem;
    private boolean mIsLoading;
    private final boolean mIsReplaceSupported = true;
    private Launcher mLauncher;
    private int mLocation[];
    private final ItemInfo mSavedSeats[];
    
    // HOTSEAT 支持长按拖动图标
    public HotSeats(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContext = context;
        mIsLoading = true;
        mLocation = new int[2];
        MAX_SEATS = ResConfig.getHotseatCount();
        mCurrentSeats = new ItemInfo[MAX_SEATS];
        mSavedSeats = new ItemInfo[MAX_SEATS];
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    /**
     * 功能：  MAX个SEAT inflate layout/hotseat_button.xml的布局属性
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0; i < MAX_SEATS; i++) {
            LayoutInflater.from(mContext).inflate(R.layout.hotseat_button, this, true);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mLauncher.getDragLayer().getLocationInDragLayer(this, mLocation);
        mLauncher.getDragController().setDeleteRegion(new RectF(l, t, r, b));
    }

    /**
     * 功能：  设置HOTSEAT的Drag控制器
     * @param dragcontroller
     */
    public void setDragController(DragController dragcontroller){
        mDragController = dragcontroller;
    }

    public void setLauncher(Launcher launcher){
        mLauncher = launcher;
        for (int i = 0; i < getChildCount() - 1; i++) {
            ((HotSeatButton)getChildAt(i)).setLauncher(launcher);
        }
    }
    
    private int getSeatPosByX(int index, int max) {
        int pos = 0;
        if (max != 0){
            pos = Math.max(0, Math.min((index - mLocation[0] - getPaddingLeft()) / getSeatWidth(max), max - 1));
        }
        return pos;
    }

    private int getSeatWidth(int max) {
        int width;
        if (max != 0) {
            width = getWorkingWidth() / max;
        } else {
            width = getWorkingWidth();
        }
        return width;
    }

    private int getSeatsCount() {
        int count = 0;
        for (int i = 0; i < MAX_SEATS; i++){
            if (mSavedSeats[i] != mDraggingItem && mSavedSeats[i] != null){
                count++;
            }
        }
        return count;
    }

    private int getVisibleSeatsCount() {
        int vCount = 0;
        for (int i = 0; i < MAX_SEATS; i++){
            if (mSavedSeats[i] != mDraggingItem 
                && mSavedSeats[i] != PLACE_HOLDER_SEAT 
                && mSavedSeats[i] != null){
                vCount++;
            }
        }
        return vCount;
    }

    private int getWorkingWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private boolean isDropAllowed(int index, ItemInfo iteminfo) {
        int xPos = getSeatPosByX(index, getSeatsCount());
        boolean bDrop = false;
        if (mCurrentSeats[xPos] == null 
            || !(mCurrentSeats[xPos] instanceof FolderInfo) 
            || iteminfo.container <= 0L) {
            bDrop = true;
        } else {
            bDrop = false;
        }
        return bDrop;
    }

    private boolean isDropAllowed(DragSource dragsource, ItemInfo iteminfo) {
        boolean bDrop;
        if (!mIsReplaceSupported || dragsource == this) {
            bDrop = false;
        } else {
            bDrop = true;
        }
        if (mIsLoading || !bDrop && getVisibleSeatsCount() >= MAX_SEATS 
                || (iteminfo.itemType != 0 
                    && iteminfo.itemType != 1 
                    && iteminfo.itemType != 2)){
            bDrop = false;
        }
        return bDrop;
    }

    private boolean isDropAllowed(DropTarget.DragObject dragobject) {
        boolean flag = false;
        if (isDropAllowed(dragobject.dragSource, dragobject.dragInfo) 
            && isDropAllowed(dragobject.x, dragobject.dragInfo)) {
            flag = true;
        }
        return flag;
    }

    private void restoreSeats() {
        for (int i = 0; i < MAX_SEATS; i++) {
            ItemInfo iteminfo;
            if (mDraggingItem == mSavedSeats[i]) {
                iteminfo = null;
            } else {
                iteminfo = mSavedSeats[i];
            }
            setSeat(i, iteminfo);
        }
    }
    
    private void saveSeats() {
        saveSeats(true);
    }

    /**
     * 功能： 保存当前SEAT
     * @param flag
     */
    private void saveSeats(boolean flag) {
        ArrayList<ContentProviderOperation> arraylist = null;
        if (flag){
            arraylist = new ArrayList<ContentProviderOperation>();
        }
        int i = 0;
        int j = 0;
        
        // 先从CurrentSeat中读取Seat保存到SaveSeat中
        for (; i < MAX_SEATS; i++) {
            if (mCurrentSeats[i] != null && mCurrentSeats[i] != PLACE_HOLDER_SEAT) {
                mSavedSeats[j] = mCurrentSeats[i];
                mSavedSeats[j].cellX = j;
                if (arraylist != null){
                    arraylist.add(LauncherModel.getMoveItemOperation(mSavedSeats[j], 
                            LauncherSettings.Favorites.CONTAINER_HOTSEAT, 0, j, 0));
                }
                j++;
            }
        }
        
        if (arraylist != null && !arraylist.isEmpty()){
            LauncherModel.applyBatch(mContext, "cn.minking.launcher.settings", arraylist);
        }
        
        for (; j < MAX_SEATS; j++) {
            mSavedSeats[j] = null;
        }
        
        for (int k = 0; k < MAX_SEATS; k++) {
            setSeat(k, mSavedSeats[k]);
        }
        
    }
    
    /**
     * 功能： 给Item分配Seat位置
     */
    private void setSeat(int i, ItemInfo iteminfo){
        // i在范围之内，则当前位置上与Item不为同样的
        if ((i >= 0 || i < MAX_SEATS) && mCurrentSeats[i] != iteminfo){
            mCurrentSeats[i] = iteminfo;
            HotSeatButton hotseatbutton = (HotSeatButton)getChildAt(i);
            hotseatbutton.unbind(mDragController);
            if (iteminfo == null) {
                LinearLayout.LayoutParams layoutparams 
                    = (LinearLayout.LayoutParams)hotseatbutton.getLayoutParams();
                layoutparams.width = 0;
                layoutparams.weight = 0F;
                hotseatbutton.setLayoutParams(layoutparams);
            } else {
                if (iteminfo != PLACE_HOLDER_SEAT){
                    // 给Item分配图标
                    ItemIcon itemicon = mLauncher.createItemIcon(this, iteminfo);
                    itemicon.setCompactViewMode(true);
                    hotseatbutton.bind(itemicon, mDragController);
                }
                hotseatbutton.setTag(iteminfo);
                hotseatbutton.setOnLongClickListener(this);
                LinearLayout.LayoutParams layoutparams 
                    = (LinearLayout.LayoutParams)hotseatbutton.getLayoutParams();
                layoutparams.width = -1;
                layoutparams.weight = 1F;
                hotseatbutton.setLayoutParams(layoutparams);
            }
        }
    }
    
    private int setSeats(int index, ItemInfo iteminfo) {
        int k1 = -1;
        {
            
            int vCount = getVisibleSeatsCount();
            if (vCount != MAX_SEATS) {
                if (vCount != 0)
                {
                    int seatWidth = getSeatWidth(vCount);
                    int l;
                    if (!mIsReplaceSupported || mDraggingItem != null)
                        l = 0;
                    else
                        l = seatWidth / 4;
                    int j = 0;
                    do
                    {
                        if (j >= vCount + 1){}
                        int l1 = mLocation[0] + getPaddingLeft() + seatWidth * j + seatWidth / 2;
                        if (j < vCount && Math.abs(index - l1) < l)
                            break;
                        if (index <= l + (l1 - seatWidth) || index > l1 - l)
                        {
                            j++;
                        } else {
                            vCount = 0;
                            for (int i1 = 0; i1 < MAX_SEATS; i1++) {
                                if (i1 != j) {
                                    if (vCount < MAX_SEATS) {
                                        if (mDraggingItem != null 
                                            && mDraggingItem == mSavedSeats[vCount]) {
                                            vCount++;
                                        }
                                        setSeat(i1, mSavedSeats[vCount]);
                                        vCount++;
                                    }
                                } else {
                                    setSeat(i1, iteminfo);
                                }
                            }
                            if (k1 >= MAX_SEATS) {
                                k1 = -2;
                            }
                        }
                    } while (true);
                    k1 = j;
                } else {
                    k1 = 0;
                }
            } else {
                k1 = getSeatPosByX(index, vCount);
            }
        }
        if (k1 >= 0) {
            restoreSeats();
            setSeat(k1, iteminfo);
        }
        return k1;
    }
    
    @Override
    public boolean acceptDrop(DragObject dragobject) {
        return isDropAllowed(dragobject.dragSource, dragobject.dragInfo);
    }

    @Override
    public DropTarget getDropTargetDelegate(DragObject dragobject) {
        return null;
    }

    @Override
    public boolean isDropEnabled() {
        return true;
    }

    @Override
    public void onDragEnter(DragObject dragobject) {
    }

    @Override
    public void onDragExit(DragObject dragobject) {
        if (isDropAllowed(dragobject)){
            restoreSeats();
        }
    }

    @Override
    public void onDragOver(DragObject dragobject) {
        if (isDropAllowed(dragobject)){
            setSeats(dragobject.x, PLACE_HOLDER_SEAT);
        }
    }

    @Override
    public boolean onDrop(DragObject dragobject) {
        boolean flag;
        if (isDropAllowed(dragobject.x, dragobject.dragInfo)) {
            int pos = setSeats(dragobject.x, dragobject.dragInfo);
            if (pos != -1) {
                ItemInfo iteminfo;
                if (pos < 0) {
                    iteminfo = null;
                } else {
                    iteminfo = mSavedSeats[pos];
                }
                if (iteminfo != null) {
                    iteminfo.container = dragobject.dragInfo.container;
                    iteminfo.screenId = dragobject.dragInfo.screenId;
                    iteminfo.cellX = dragobject.dragInfo.cellX;
                    iteminfo.cellY = dragobject.dragInfo.cellY;
                    dragobject.dragInfo.cellX = pos;
                }
                
                saveSeats();
                
                if (mDraggingItem == null) {
                    LauncherModel.moveItemInDatabase(mContext, dragobject.dragInfo, 
                            LauncherSettings.Favorites.CONTAINER_HOTSEAT, 0, dragobject.dragInfo.cellX, 0);
                    if (iteminfo != null) {
                        Context context = mContext;
                        long container = iteminfo.container;
                        long screen = iteminfo.screenId;
                        int cx = iteminfo.cellX;
                        int cy = iteminfo.cellY;
                        LauncherModel.moveItemInDatabase(context, iteminfo, container, screen, cx, cy);
                        mLauncher.addItem(iteminfo, false);
                    }
                }
                flag = true;
            } else {
                flag = false;
            }
        } else {
            flag = false;
        }
        return flag;
    }

    @Override
    public void onDropCompleted(View view, DragObject dragobject, boolean flag) {
        mDraggingItem = null;
        if (flag) {
            saveSeats();
        } else {
            restoreSeats();
        }
    }

    /**
     * 功能：  判断是否为空的SEAT
     */
    boolean isEmptySeat(int i){
        boolean flag = false;
        if ((i < MAX_SEATS && i >= 0) 
                && (mCurrentSeats[i] == null || mCurrentSeats[i] == PLACE_HOLDER_SEAT)){
            flag = true;
        }
        return flag;
    }
    
    /**
     * 功能：  找到SEAT中的空位置
     */
    public int findEmptySeat(){
        int i = 0;
        for (i = 0; i < MAX_SEATS; i++) {
            if (isEmptySeat(i)) break;
        }
        if (i >= MAX_SEATS) {
            i = -1;
        }
        return i;
    }
    
    /**
     * 功能：  加载HOT SEAT 图标项目
     * @param iteminfo
     * @return
     */
    public boolean pushItem(ItemInfo iteminfo){
        boolean bFlag = false;
        
        if (!isEmptySeat(iteminfo.cellX)) {
            if(-1 == findEmptySeat()) return bFlag;
        }else {
            setSeat(iteminfo.cellX, iteminfo);
        }
        if (!mIsLoading) {
            saveSeats(false);
        }
        return bFlag;
    }
    
    /**
     * 功能：  开始BIND HOTSEAT， 将各BUTTON复位
     */
    public void startBinding(){
        for (int i = 0; i < MAX_SEATS; i++) {
            // 调用removeAllViewsInLayout()，清空以前的数据
            ((HotSeatButton)getChildAt(i)).removeAllViewsInLayout();
            mSavedSeats[i] = null;
            mCurrentSeats[i] = null;
        }
        mIsLoading = true;
    }
    
    /**
     * 功能：  完成 BIND HOTSEAT并保存
     */
    public void finishBinding(){
        saveSeats(false);
        mIsLoading = false;
    }
    
    public ItemIcon getItemIcon(FolderInfo folderinfo){
        Object object = (HotSeatButton)findViewWithTag(folderinfo);
        if (object == null || ((HotSeatButton)(object)).getChildCount() == 0)
            object = null;
        else
            object = (ItemIcon)((HotSeatButton)(object)).getChildAt(0);
        return (ItemIcon)object;
    }
    
    public void removeItems(ArrayList<RemoveInfo> arraylist){
        boolean flag = false;
        Iterator<RemoveInfo> iterator = arraylist.iterator();
        while (iterator.hasNext()) {
            RemoveInfo removeinfo = iterator.next();
            int i = 0;
            while (i < MAX_SEATS)  {
                if (mSavedSeats[i] != null){
                    if (mSavedSeats[i] instanceof FolderInfo) {
                            FolderInfo folderinfo = (FolderInfo)mSavedSeats[i];
                            folderinfo.removeItems(arraylist, mLauncher);
                            folderinfo.notifyDataSetChanged();
                    } else {
                        ComponentName componentname = ((ShortcutInfo)mSavedSeats[i]).intent.getComponent();
                        if (componentname != null && removeinfo.packageName.equals(componentname.getPackageName())) {
                            setSeat(i, null);
                            flag = true;
                        }
                    }
                }
                i++;
            }
        } 
        saveSeats(flag);
        return;
    }
    
    @Override
    public boolean onLongClick(View view) {
        boolean flag = false;
        if (!mIsLoading && mDraggingItem == null) {
            mDraggingItem = (ItemInfo)view.getTag();
            if (mDraggingItem != null) {
                if (!(mDraggingItem instanceof FolderInfo) || !((FolderInfo)mDraggingItem).opened) {
                    if (!mLauncher.isFolderShowing()) {
                        mDragController.startDrag(((HotSeatButton)view).getIcon(), this, mDraggingItem, DragController.DRAG_ACTION_COPY);
                        setSeat(mDraggingItem.cellX, PLACE_HOLDER_SEAT);
                        flag = true;
                    }
                }
            } 
        } 
        return flag;
    }
    
    
}