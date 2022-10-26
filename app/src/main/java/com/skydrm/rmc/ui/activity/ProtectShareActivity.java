package com.skydrm.rmc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.DownloadManager;
import com.skydrm.rmc.engine.ExecuteCommandImpl;
import com.skydrm.rmc.engine.FileOperation;
import com.skydrm.rmc.engine.Render.RenderHelper;
import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.engine.enumData.FileFrom;
import com.skydrm.rmc.engine.eventBusMsg.ChangeExpiryDateEvent;
import com.skydrm.rmc.engine.eventBusMsg.CommandOperateEvent;
import com.skydrm.rmc.engine.eventBusMsg.HomePageToSharePageEvent;
import com.skydrm.rmc.engine.eventBusMsg.MorePageToProtectPageEvent;
import com.skydrm.rmc.engine.eventBusMsg.MorePageToSharePageEvent;
import com.skydrm.rmc.engine.eventBusMsg.MyVaultFileShareEvent;
import com.skydrm.rmc.engine.eventBusMsg.ProtectCompleteNotifyEvent;
import com.skydrm.rmc.engine.eventBusMsg.ShareCompleteNotifyEvent;
import com.skydrm.rmc.engine.eventBusMsg.ViewPageToProtectPageEvent;
import com.skydrm.rmc.engine.eventBusMsg.ViewPageToSharePageEvent;
import com.skydrm.rmc.engine.intereface.IAddComplete;
import com.skydrm.rmc.engine.intereface.IExecuteCommand;
import com.skydrm.rmc.engine.intereface.IProtectComplete;
import com.skydrm.rmc.engine.intereface.IShareComplete;
import com.skydrm.rmc.engine.watermark.EditWatermarkHelper;
import com.skydrm.rmc.engine.watermark.WatermarkSetInvalidEvent;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.exceptions.ExceptionDialog;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.exception.FileUploadException;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.common.ActivityManager;
import com.skydrm.rmc.ui.myspace.myvault.model.adapter.RightsAdapter;
import com.skydrm.rmc.ui.myspace.myvault.view.task.GetMyVaultMetadataTask;
import com.skydrm.rmc.ui.widget.customcontrol.CommentWidget;
import com.skydrm.rmc.ui.widget.customcontrol.EditWatermarkWidget;
import com.skydrm.rmc.ui.widget.customcontrol.FlowLayout;
import com.skydrm.rmc.ui.widget.customcontrol.SoftKeyBoardStatusView;
import com.skydrm.rmc.utils.PatchedTextView;
import com.skydrm.rmc.utils.commonUtils.CalenderUtils;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ExpiryChecker;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.rest.myVault.MyVaultMetaDataResult;
import com.skydrm.sdk.rms.rest.myVault.MyVaultUploadFileResult;
import com.skydrm.sdk.rms.rest.user.User;
import com.skydrm.sdk.rms.types.RemoteViewResult2;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by aning on 5/5/2017.
 * <p>
 * The activity used to handle protect or share file.
 * share way include:
 * (1) file info --> share
 * (2) command --> share
 * (3) home slid --> share
 */

@Deprecated
public class ProtectShareActivity extends BaseActivity {
    private static final DevLog log = new DevLog(ProtectShareActivity.class.getSimpleName());
    private static final DateFormat sDF = new SimpleDateFormat("EEEE,MMMM d,yyyy");
    private static final String EXPIRY_NEVER_EXPIRE = "Never Expire";
    private static final String EXPIRY_RELATIVE = "Relative";
    private static final String EXPIRY_DATE_RANGE = "Date Range";
    private static final String EXPIRY_ABSOLUTE_DATE = "Absolute Date";
    private static int CONTACT_EMAIL_REQUEST_CODE = 0725;
    // bind controls
    @BindView(R.id.root_view)
    ScrollView mRootView;
    @BindView(R.id.imageView_back)
    ImageView mImageViewBack;
    @BindView(R.id.operate_button)
    Button mOperateButton;
    @BindView(R.id.tv_first)
    PatchedTextView mTvTitle;
    @BindView(R.id.tv_second)
    PatchedTextView mTvFileName;
    // only for share view, will set Gone if is protect view.
    @BindView(R.id.share_with_layout)
    RelativeLayout mShareWithLayout;
    @BindView(R.id.flowLayout)
    FlowLayout mFlowLayout;
    @BindView(R.id.et_email_address)
    EditText mEditText;
    @BindView(R.id.specify_rights_layout)
    RelativeLayout mRlSpecifyRightsLayout;
    @BindView(R.id.view_rights_layout)
    RelativeLayout mRlViewRightsLayout;
    @BindView(R.id.Rl_content_layout)
    RelativeLayout mRlContentLayout;

    @BindView(R.id.no_rights_tip)
    TextView mTvRightsTip;
    @BindView(R.id.steward_tip)
    TextView mTvStewardRightsTip;
    // toggle buttons
    @BindView(R.id.toggle_view)
    SwitchCompat toggleView;
    @BindView(R.id.toggle_print)
    SwitchCompat togglePrint;
    @BindView(R.id.toggle_edit)
    SwitchCompat toggleEdit;

    @BindView(R.id.toggle_share)
    SwitchCompat toggleShare;
    @BindView(R.id.toggle_download)
    SwitchCompat toggleDownload;
    @BindView(R.id.toggle_watermark)
    SwitchCompat toggleWatermark;
    @BindView(R.id.toggle_validity)
    SwitchCompat toggleValidity;
    // rights textView
    @BindView(R.id.text_view)
    TextView textView;
    @BindView(R.id.text_print)
    TextView textPrint;
    @BindView(R.id.text_edit)
    TextView textEdit;

    @BindView(R.id.text_share)
    TextView textShare;
    @BindView(R.id.text_download)
    TextView textDownload;
    @BindView(R.id.text_watermark)
    TextView textWatermark;
    @BindView(R.id.text_validity)
    TextView textValidity;
    @BindView(R.id.text_never_expire)
    TextView textNeverExpire;
    @BindView(R.id.comment_widget)
    CommentWidget commentWidget;
    @BindView(R.id.softKeyBoardStatusView)
    SoftKeyBoardStatusView softKeyBoardStatusView;

    /*********** Begin watermark ***************/
    @BindView(R.id.change_watermark_layout)
    RelativeLayout watermarkChangeLayout;
    @BindView(R.id.watermark_value)
    TextView watermark;
    @BindView(R.id.change_label)
    TextView change;
    EditWatermarkWidget editWatermarkWidget;
    private String watermarkValue = "";
    private Button btnEditWatermarkCancel;
    private Button btnEditWatermarkOk;
    /*********** End watermark ***************/

    /*********** Begin expiry date ***************/
    @BindView(R.id.change_never_layout)
    LinearLayout changeNeverDate;
    @BindView(R.id.change_common_layout)
    LinearLayout changeCommonDate;
    @BindView(R.id.change_absolute_layout)
    LinearLayout changeAbsoluteDate;
    @BindView(R.id.from_day)
    TextView fromDay;
    @BindView(R.id.from_month)
    TextView fromMonth;
    @BindView(R.id.from_week)
    TextView fromWeek;
    @BindView(R.id.to_day)
    TextView toDay;
    @BindView(R.id.to_month)
    TextView toMonth;
    @BindView(R.id.to_week)
    TextView toWeek;
    @BindView(R.id.count_day)
    TextView countDay;
    // Absolute date
    @BindView(R.id.abs_to_day)
    TextView absToDay;
    @BindView(R.id.abs_to_month)
    TextView absToMonth;
    @BindView(R.id.abs_to_week)
    TextView absToWeek;
    @BindView(R.id.abs_count_day)
    TextView absCountDay;

    // calendar
    private Calendar absoluteEndDate;
    private Calendar rangeStartDate;
    private Calendar rangeEndDate;
    // for expiry date
    private User.IExpiry iExpiry;
    // record whether is expired
    private boolean bIsExpired = false;
    /*********** End expiry date ***************/

    private RightsAdapter mRightsAdapter;

    // display validity layout
    @BindView(R.id.validity_layout)
    LinearLayout validityLayout;
    @BindView(R.id.validity_content)
    TextView validityContent;

    // download progress layout
    @BindView(R.id.download_progress_of_protectshare_activity)
    RelativeLayout progressLayout;

    private TextView mProgressValue;
    private ProgressBar mProgressBar;
    // download callback of DownloadManager.
    private DownloadManager.IDownloadCallBack mDownloadCallback;

    private Context mContext;
    // flow layout used to hold wrap emails
    private List<String> mEmailList = new ArrayList<>();
    private INxFile mClickFileItem;
    private INxlFileFingerPrint mINxlFileFingerPrint;
    private File mWorkingFile;
    private String mFileName;
    // protect or share
    private CmdOperate mCmdOperate;
    private boolean bIsNxl = false;
    // for command Add dest folder(upload normal file into this)
    private INxFile mDestFolder;
    // need this when add file
    private BoundService mBoundService;

    private IExecuteCommand mIExecuteCommand;
    // for myVault view file info
    private INxlFile mFileBase;
    private MyVaultMetaDataResult mMyVaultMetaData;
    // file from
    private FileFrom mFileFrom;
    // remote view result data
    private RemoteViewResult2.ResultsBean mRemoteViewResultBean;
    // the rights value
    private int mPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protectshare3);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        mContext = this;
        mIExecuteCommand = new ExecuteCommandImpl();
        initView();
    }

    // bind listener for controls
    @OnClick({R.id.imageView_back, R.id.operate_button, R.id.softKeyBoardStatusView,
            R.id.text_view, R.id.text_print, R.id.text_share, R.id.text_download, R.id.text_watermark, R.id.text_validity})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_back:
                finish();
                break;
            case R.id.operate_button:
                if (mCmdOperate == CmdOperate.SHARE || mCmdOperate == CmdOperate.COMMAND_ADD_SHARE) {
                    doShare();
                } else if (mCmdOperate == CmdOperate.PROTECT || mCmdOperate == CmdOperate.COMMAND_ADD_PROTECT) {
                    doProtect();
                }
                break;
            // used to hide the soft keyboard when user click other space field.
            case R.id.softKeyBoardStatusView:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                break;
            // textView rights
            case R.id.text_view:
                // note: for view, is enable in default, and can't edit this rights.
                break;
            case R.id.text_print:
                togglePrint.setChecked(!togglePrint.isChecked());
                break;
            case R.id.text_edit:
                toggleEdit.setChecked(!toggleEdit.isChecked());
                break;
            case R.id.text_share:
                toggleShare.setChecked(!toggleShare.isChecked());
                break;
            case R.id.text_download:
                toggleDownload.setChecked(!toggleDownload.isChecked());
                break;
            case R.id.text_watermark:
                toggleWatermark.setChecked(!toggleWatermark.isChecked());
                break;
            case R.id.text_validity:
                // note: for view, is enable in default, and can't edit this rights.
                break;
            default:
                break;
        }
    }

    /**
     * eventBus message handler for view page protect
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onViewPageEventHandler(ViewPageToProtectPageEvent eventMsg) {
        mWorkingFile = eventMsg.getWorkingFile();
        mClickFileItem = eventMsg.getClickFileItem();
        mCmdOperate = eventMsg.getCmdOperate();
        bIsNxl = eventMsg.isbNxlFile();
    }

    /**
     * eventBus message handler for view page share
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onViewPageEventHandler(ViewPageToSharePageEvent eventMsg) {
        mWorkingFile = eventMsg.getWorkingFile();
        mClickFileItem = eventMsg.getClickFileItem();
        mCmdOperate = eventMsg.getCmdOperate();
        bIsNxl = eventMsg.isbNxlFile();
    }

    /**
     * eventBus message handler for more page share
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMorePageEventHandler(MorePageToSharePageEvent eventMsg) {
        mFileBase = eventMsg.getIMyVaultFileEntry();
        mWorkingFile = eventMsg.getWorkingFile();
        mClickFileItem = eventMsg.getClickFileItem();
        mINxlFileFingerPrint = eventMsg.getFileFingerPrint();
        mCmdOperate = eventMsg.getCmdOperate();
        bIsNxl = eventMsg.isbNxlFile();
        mRemoteViewResultBean = eventMsg.getRemoteViewResultBean();
    }

    /**
     * eventBus message handler for more page protect
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMorePageEventHandler(MorePageToProtectPageEvent eventMsg) {
        mWorkingFile = eventMsg.getWorkingFile();
        mClickFileItem = eventMsg.getClickFileItem();
        mCmdOperate = eventMsg.getCmdOperate();
        bIsNxl = eventMsg.isbNxlFile();
    }

    /**
     * eventBus message handler for home page share.
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onHomePageEventHandler(HomePageToSharePageEvent eventMsg) {
        mClickFileItem = eventMsg.getClickFileItem();
        mCmdOperate = eventMsg.getCmdOperate();
    }

    /**
     * eventBus message handler for Command Add -> Protect or Share
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onCommandAddEventHandler(CommandOperateEvent.CommandAddMsg eventMsg) {
        mWorkingFile = eventMsg.getWorkingFile();
        mCmdOperate = eventMsg.getCmdOperate();
        mBoundService = eventMsg.getBoundService();
        mDestFolder = eventMsg.getParentFolder();
    }

    /**
     * eventBus message handler for myVault file share ------> will not access this
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMyVaultFileEventHandler(MyVaultFileShareEvent eventMsg) {
        // for myVault view file info
        mFileBase = eventMsg.getMyVaultFileEntry();
        mMyVaultMetaData = eventMsg.getMyVaultMetaData();
        mCmdOperate = eventMsg.getCmdOperate();
        mFileFrom = eventMsg.getFileFrom();
        bIsNxl = eventMsg.isbNxlFile();
        mRemoteViewResultBean = eventMsg.getRemoteViewResultBean();
    }

    /**
     * eventBus message handler for Edit watermark if is set empty.
     *
     * @param eventMsg
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEditWatermarkSetEmpty(WatermarkSetInvalidEvent eventMsg) {
        if (eventMsg.bSetInvalid) {
            btnEditWatermarkOk.setEnabled(false);
        } else {
            btnEditWatermarkOk.setEnabled(true);
        }
    }

    /**
     * eventBus message for change expiry date.
     *
     * @param eventMsg
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onChangeExpiryDateEvent(ChangeExpiryDateEvent eventMsg) {
        iExpiry = eventMsg.iExpiry;
        displayExpiryDate();
    }

    private void initView() {
        // file name
        if (mClickFileItem != null) {
            mFileName = RenderHelper.isGoogleFile(mClickFileItem) ? RenderHelper.getGoogleExportFileName(mClickFileItem) : mClickFileItem.getName();
            mTvFileName.setText(mFileName);
        } else if (mWorkingFile != null) {
            mFileName = mWorkingFile.getName();
            mTvFileName.setText(mFileName);
        } else if (mFileBase != null) { // myVault
            mFileName = mFileBase.getName();
            mTvFileName.setText(mFileName);
        }

        // init rights layout
        initRightsView();

        // init protect or share sub view.
        initSubView();

        // init change watermark
        initWatermarkChange();

        // init change expire
        initExpireChange();

        // whether switch into "share repo file" mode for share operate.
        if (mCmdOperate == CmdOperate.SHARE && isSwitchToShareRepoFileModel()) {
            // for myDrive all normal files
            if (!isMyDriveNxlFile()) {
                return;
            }
            // simple remote view file,(but slid and command share still need to download to get rights for myDrive nxl file)
            if ((RenderHelper.isNeedSimpleRemoteView(mFileName) && mFileFrom == FileFrom.FILE_FROM_VIEW_PAGE)) {
                return;
            }
        }

        // look at the file if is in local, if not try get it.
        if (mWorkingFile == null) {
            // get file
            if ((mFileFrom != null && mFileFrom == FileFrom.FILE_FROM_MYVAULT) // the menu command operate "view file info" of myVault.
                    || (mFileBase != null && mRemoteViewResultBean != null)) { // myVault simple remote view
                // "share local file" model for myVault (may be deprecated for this model).
                tryGetMyVaultFile();
            } else {
                tryGetFile();
            }
        }
    }

    private void initExpiryChange(TextView change, final String selectItem) {
        change.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ExpiryWindow.class);
                // post expiry value to ExpiryWindow to change
                EventBus.getDefault().postSticky(new ChangeExpiryDateEvent(iExpiry));
                intent.putExtra("select_item", selectItem);
                intent.putExtra("use_default", false);
                startActivity(intent);
            }
        });
    }

    /**
     * Display expiry date
     */
    private void displayExpiryDate() {

        switch (iExpiry.getOption()) {
            case 0: // Never expiry
                changeAbsoluteDate.setVisibility(View.GONE);
                changeCommonDate.setVisibility(View.GONE);
                changeNeverDate.setVisibility(View.VISIBLE);
                // change operate
                TextView neverChange = (TextView) findViewById(R.id.never_change);
                initExpiryChange(neverChange, EXPIRY_NEVER_EXPIRE);

                break;
            case 1: // relative
                changeNeverDate.setVisibility(View.GONE);
                changeAbsoluteDate.setVisibility(View.GONE);
                changeCommonDate.setVisibility(View.VISIBLE);
                // set data
                User.IRelative relative = (User.IRelative) iExpiry;
                updateRelativeEndDate(relative.getYear(), relative.getMonth(), relative.getDay(), relative.getWeek());

                // change operate
                TextView relativeChange = (TextView) findViewById(R.id.change);
                initExpiryChange(relativeChange, EXPIRY_RELATIVE);

                break;
            case 2: // absolute
                changeNeverDate.setVisibility(View.GONE);
                changeCommonDate.setVisibility(View.GONE);
                changeAbsoluteDate.setVisibility(View.VISIBLE);

                // set the "Expire on" text style
                TextView expireOn = (TextView) findViewById(R.id.expire_on);
                Spannable spannable = new SpannableString(mContext.getResources().getString(R.string.Rights_will_expire_on));
                spannable.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green_light)), 12, 21, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                spannable.setSpan(new StyleSpan(Typeface.BOLD), 12, 21, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                expireOn.setText(spannable);

                User.IAbsolute absolute = (User.IAbsolute) iExpiry;
                // end date
                long date = absolute.endDate();
                absoluteEndDate = Calendar.getInstance(Locale.getDefault());
                absoluteEndDate.setTime(new Date(date));
                updateAbsoluteEndDateText(absoluteEndDate);
                // change operate
                TextView absChange = (TextView) findViewById(R.id.abs_change);
                initExpiryChange(absChange, EXPIRY_ABSOLUTE_DATE);

                break;
            case 3: // date range
                changeNeverDate.setVisibility(View.GONE);
                changeAbsoluteDate.setVisibility(View.GONE);
                changeCommonDate.setVisibility(View.VISIBLE);

                // set data --- convert mills to Calendar
                User.IRange range = (User.IRange) iExpiry;
                // start date
                long startDate = range.startDate();
                rangeStartDate = Calendar.getInstance(Locale.getDefault());
                rangeStartDate.setTime(new Date(startDate));
                // end date
                long endDate = range.endDate();
                rangeEndDate = Calendar.getInstance(Locale.getDefault());
                rangeEndDate.setTime(new Date(endDate));

                updateRelativeOrRangeEndDateText(rangeStartDate, rangeEndDate);
                // change operate
                TextView rangChange = (TextView) findViewById(R.id.change);
                initExpiryChange(rangChange, EXPIRY_DATE_RANGE);

                break;
            default:
                break;
        }
    }

    // update Relative date.
    private void updateRelativeEndDate(int growthYears, int growthMonths, int growthDays, int growthWeeks) {
        Calendar calendar = Calendar.getInstance();
        //Get current year,month and day ,as for week(translate it to days to calc)
        int curYears = calendar.get(Calendar.YEAR);
        int curMonths = calendar.get(Calendar.MONTH);
        int curDays = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.set(Calendar.YEAR, curYears + growthYears);
        calendar.set(Calendar.MONTH, curMonths + growthMonths);
        calendar.set(Calendar.DAY_OF_MONTH, curDays + growthDays + growthWeeks * 7 - 1);
        calendar.set(Calendar.AM_PM, 1);
        calendar.set(Calendar.HOUR, calendar.getActualMaximum(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        //update date show TextView
        updateRelativeOrRangeEndDateText(Calendar.getInstance(), calendar);
    }

    private void updateRelativeOrRangeEndDateText(Calendar startDate, Calendar endDate) {
        //for from side
        fromMonth.setText(CalenderUtils.getMonthLabel(startDate));
        fromWeek.setText(CalenderUtils.getWeekLabel(startDate) + " " + startDate.get(Calendar.YEAR));
        fromDay.setText(String.valueOf(startDate.get(Calendar.DAY_OF_MONTH)));
        //for to side
        toMonth.setText(CalenderUtils.getMonthLabel(endDate));
        toWeek.setText(CalenderUtils.getWeekLabel(endDate) + " " + endDate.get(Calendar.YEAR));
        toDay.setText(String.valueOf(endDate.get(Calendar.DAY_OF_MONTH)));
        countDay.setText(String.valueOf(CalenderUtils.countDays(startDate, endDate)));
        countDay.append(" days");
    }

    private void updateAbsoluteEndDateText(Calendar endDate) {
        absToMonth.setText(CalenderUtils.getMonthLabel(endDate));
        absToWeek.setText(CalenderUtils.getWeekLabel(endDate) + " " + endDate.get(Calendar.YEAR));
        absToDay.setText(String.valueOf(endDate.get(Calendar.DAY_OF_MONTH)));
        absCountDay.setText(String.valueOf(CalenderUtils.countDays(Calendar.getInstance(), endDate)));
        absCountDay.append(" days");
    }

    /**
     * Get Expiry from heartbeat, so may be a little delayed.
     */
    private void tryGetExpiry() {
        iExpiry = SkyDRMApp.getInstance().getSession().getUserPreference().getExpiry();
    }

    /**
     * Init expire change
     */
    private void initExpireChange() {

        tryGetExpiry();
        // display the default expiry layout.
        displayExpiryDate();

    }

    /**
     * Init watermark change.
     */
    private void initWatermarkChange() {
        watermarkValue = SkyDRMApp.getInstance().getSession().getUserPreference().getWatermarkValue();

        toggleWatermark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    watermarkChangeLayout.setVisibility(View.VISIBLE);
                    // display watermark
                    watermark.setText(""); // reset
                    EditWatermarkHelper.string2imageSpanForDisplay(mContext, watermarkValue, watermark);

                    // set underline
                    change.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                } else {
                    watermarkChangeLayout.setVisibility(View.GONE);
                }
            }
        });

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                RelativeLayout layout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.change_watermark_value_layout, null);
                builder.setView(layout);
                final AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();

                editWatermarkWidget = (EditWatermarkWidget) layout.findViewById(R.id.edit_watermark_widget);

                // cancel button
                btnEditWatermarkCancel = (Button) layout.findViewById(R.id.btn_cancel);
                btnEditWatermarkCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                // ok button
                btnEditWatermarkOk = (Button) layout.findViewById(R.id.btn_ok);
                btnEditWatermarkOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get the edited watermark value
                        EditText editText = editWatermarkWidget.getEditText();

                        // prevent user input empty watermark.
                        if (editText.getEditableText().toString().trim().equals("")) {
                            ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.watermark_can_not_is_empty));
                            return;
                        }

                        watermarkValue = EditWatermarkHelper.imageSpan2StringEx(editText);
                        // display watermark
                        watermark.setText(""); // reset
                        EditWatermarkHelper.string2imageSpanForDisplay(mContext, watermarkValue, watermark);

                        dialog.dismiss();
                    }
                });
            }
        });
    }

    public String getWatermarkValue() {
        return watermarkValue;
    }

//    public void showSoftInput(View view) {
//        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//        if (inputMethodManager != null) {
//            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
//        }
//    }

    /**
     * Judge whether switch to "ShareRepoFile" model to share, the default use "ShareLocalFile" to share.
     */
    private boolean isSwitchToShareRepoFileModel() {

        // myVault file
        if ((mFileFrom != null && mFileFrom == FileFrom.FILE_FROM_MYVAULT) // the menu command operate "view file info" of myVault.
                || mFileBase != null) {
            return true;
        }

        // myDrive file
        if (mClickFileItem != null && mClickFileItem.getService().alias.equals(BoundService.MYDRIVE)) {
            return true;
        }

        return false;
    }

    /**
     * Judge whether is myDrive nxl file.
     * Note: for myDrive nxl file, we can still use "share Repo file" api to share, but still need to download to get rights firstly,
     * then to share, but which case don't need to upload file again when share using "share repository file".
     */
    private boolean isMyDriveNxlFile() {
        return mClickFileItem != null
                && mClickFileItem.getService().alias.equals(BoundService.MYDRIVE)
                && isNxlFile();
    }

    /**
     * Init rights layout according to whether is nxl file
     */
    private void initRightsView() {
        // init view rights
        if (isNxlFile()) {
            // load view rights layout
            mRlSpecifyRightsLayout.setVisibility(View.GONE);
            mRlViewRightsLayout.setVisibility(View.VISIBLE);
            // read and display the rights
            readNxlRights();
        } else {
            // init specify rights for normal file.
            initRightsToggle();
            // hide validity layout for normal file.
            validityLayout.setVisibility(View.GONE);
        }
    }

    private boolean isNxlFile() {
        if (mWorkingFile != null) {
            return RenderHelper.isNxlFile(mWorkingFile.getPath());
        } else if (mClickFileItem != null) {
            // initial judgement only by postfix(note: will judge again after downloading the file.)
            return mClickFileItem.getName().toLowerCase().endsWith(".nxl");
        }
        return bIsNxl;
    }

    // get simple remote view rights: remote view repo(myDrive), myVault & project.
    private void getSimpleRemoteViewRights() {
        if (mRemoteViewResultBean != null) {
            mPermissions = mRemoteViewResultBean.getPermissions();
            boolean isOwner = mRemoteViewResultBean.isOwner();
            Rights rights = new Rights();
            rights.setPermissions(mPermissions);
            rights.IntegerToRights();
            showNxlRights(rights.toList(), isOwner);
        }
    }

    private void readNxlRights() {

        // read rights for myVault
        if (mFileFrom != null && mFileFrom == FileFrom.FILE_FROM_MYVAULT) {  // when command operate "view file info" of myVault.
            if (mMyVaultMetaData != null) {
                List<String> rights = mMyVaultMetaData.getResults().getDetail().getRights();
                showNxlRights(rights, true);// myVault files belong to owner
            } else {
                // get rights from rms
                new GetMyVaultMetadataTask((MyVaultFile) mFileBase, new LoadTask.ITaskCallback<GetMyVaultMetadataTask.Result, Exception>() {
                    @Override
                    public void onTaskPreExecute() {

                    }

                    @Override
                    public void onTaskExecuteSuccess(GetMyVaultMetadataTask.Result results) {
                        MyVaultMetaDataResult result = results.result;
                        if (result == null) { // can't get rights.
                            mTvRightsTip.setVisibility(View.VISIBLE);
                            mTvStewardRightsTip.setVisibility(View.VISIBLE);
                            mTvRightsTip.setText(mContext.getResources().getString(R.string.read_rights_failed));
                        } else {
                            mMyVaultMetaData = result;
                            List<String> rights = mMyVaultMetaData.getResults().getDetail().getRights();
                            // myVault files must be owner.
                            showNxlRights(rights, true);
                        }
                    }

                    @Override
                    public void onTaskExecuteFailed(Exception e) {
                        ExceptionHandler.handleException(mContext, e);
                    }
                }).run();
            }
            return;
        }

        // read rights from simple remote view.
        if (mRemoteViewResultBean != null) {
            getSimpleRemoteViewRights();
            return;
        }

        //  read rights for other nxl files -- finger print has existed
        if (mINxlFileFingerPrint != null) {
            displayRights(mINxlFileFingerPrint);
            return;
        }

        // get rights from the nxl file header, display progressBar loading
        if (mWorkingFile != null) {
            final LinearLayout readRightsLoad = (LinearLayout) mRlViewRightsLayout.findViewById(R.id.read_rights_loading_layout);
            readRightsLoad.setVisibility(View.VISIBLE);
            // read rights for other nxl files -- get finger print
            FileOperation.readNxlFingerPrint(mContext, mWorkingFile, false, Constant.VIEW_TYPE_NORMAL, new FileOperation.IGetFingerPrintCallback() {
                @Override
                public void onGetFingerPrintFinished(INxlFileFingerPrint fingerPrint) {
                    // hide read rights loading progress
                    readRightsLoad.setVisibility(View.GONE);
                    mINxlFileFingerPrint = fingerPrint;
                    displayRights(fingerPrint);
                }
            });
        }

    }

    private void showNxlRights(List<String> rights, boolean isOwner) {
        // display rights
        GridView rightView = mRlViewRightsLayout.findViewById(R.id.rights_view);
        TextView tvStewardRightsTip = mRlViewRightsLayout.findViewById(R.id.steward_tip);

        mRightsAdapter = new RightsAdapter(this);
        rightView.setAdapter(mRightsAdapter);
        mRightsAdapter.showRights(rights);

        // display rights validity expiry tip in ui
        if (mMyVaultMetaData != null) {
            long startDate = mMyVaultMetaData.getResults().getDetail().getValidity().getStartDate();
            long endDate = mMyVaultMetaData.getResults().getDetail().getValidity().getEndDate();
            String content = "";
            if (startDate == 0) {
                if (endDate == 0) { // Never
                    validityContent.setText(getResources().getString(R.string.never_expire));
                    //bIsExpired = false;
                } else { // Relative or Absolute
                    content = getResources().getString(R.string.Until) + " " + sDF.format(new Date(endDate));
                    validityContent.setText(content);
                    // use Absolute to judge whether expire (Relative is equivalent Absolute basically)
                    //bIsExpired = new Expiry.Builder().absolute().setEndDate(endDate).build().isExpired();
                }
            } else {
                if (endDate != 0) { // Date range
                    content = sDF.format(new Date(startDate)) + " - " + sDF.format(new Date(endDate));
                    validityContent.setText(content);
                    //bIsExpired = new Expiry.Builder().range().setStartDate(startDate).setEndDate(endDate).build().isExpired();
                }
            }
            bIsExpired = !new ExpiryChecker().isValidate(startDate, endDate);
        } else if (mINxlFileFingerPrint != null) { // for myDrive simple remote view
            // display rights validity expiry tip in ui
            String expiry = mINxlFileFingerPrint.formatString();
            validityContent.setText(expiry);
            // share expired
            bIsExpired = mINxlFileFingerPrint.isExpired();
        }

        // owner has all rights tip info.
        if (isOwner) {
            tvStewardRightsTip.setVisibility(View.VISIBLE);
        }
    }

    private void displayRights(INxlFileFingerPrint fingerPrint) {
        GridView rightsView = mRlViewRightsLayout.findViewById(R.id.rights_view);
        TextView tvRightsTip = mRlViewRightsLayout.findViewById(R.id.no_rights_tip);
        TextView tvStewardRightsTip = mRlViewRightsLayout.findViewById(R.id.steward_tip);

        // read rights failed.
        if (fingerPrint == null) {
            tvRightsTip.setVisibility(View.VISIBLE);
            tvRightsTip.setText(mContext.getResources().getString(R.string.read_rights_failed));
            // disable
            mOperateButton.setEnabled(false);
            mEditText.setEnabled(false);
            commentWidget.getEditText().setEnabled(false);
            validityLayout.setVisibility(View.GONE);
            return;
        }

        mRightsAdapter = new RightsAdapter(this);
        rightsView.setAdapter(mRightsAdapter);
        mRightsAdapter.showRights(fingerPrint);

        if (fingerPrint.hasRights()) {
            // display rights validity expiry tip in ui
            String expiry = fingerPrint.formatString();
            validityContent.setText(expiry);
        } else {  // means have no any rights.
            tvRightsTip.setVisibility(View.VISIBLE);
            validityLayout.setVisibility(View.GONE);
        }
        // disable share button if don't have share right or rights expire.
        if (mCmdOperate == CmdOperate.SHARE || mCmdOperate == CmdOperate.COMMAND_SHARE_FROM_REPO || mCmdOperate == CmdOperate.COMMAND_ADD_SHARE) {
            if (!fingerPrint.hasShare() && !SkyDRMApp.getInstance().isStewardOf(fingerPrint.getOwnerID())) {
                mOperateButton.setEnabled(false);
                mEditText.setEnabled(false);
                commentWidget.getEditText().setEnabled(false);
            }
        }
        // record rights expired
        //bIsExpired = fingerPrint.isExpired();

        // tip steward have all rights
        if (SkyDRMApp.getInstance().isStewardOf(fingerPrint.getOwnerID()))
            tvStewardRightsTip.setVisibility(View.VISIBLE);
    }

    private void initRightsToggle() {
        mRlSpecifyRightsLayout.setVisibility(View.VISIBLE);
        // rights
        togglePrint.setChecked(false);
        toggleEdit.setChecked(false);
        toggleShare.setChecked(false);
        toggleDownload.setChecked(false);
        toggleWatermark.setChecked(false);
    }

    private void initSubView() {
        if (mCmdOperate == CmdOperate.SHARE || mCmdOperate == CmdOperate.COMMAND_ADD_SHARE) {
            initShare();
        } else if (mCmdOperate == CmdOperate.PROTECT || mCmdOperate == CmdOperate.COMMAND_ADD_PROTECT) {
            initProtect();
        }
    }

    // Add download progress view
    private void showProgress() {
        progressLayout.setVisibility(View.VISIBLE);
    }

    // init download progress.
    private void initProgress() {
        mProgressBar = (ProgressBar) progressLayout.findViewById(R.id.progress);
        mProgressValue = (TextView) progressLayout.findViewById(R.id.textView_progress);
    }

    private void tryGetFile() {
        initProgress();
        initDownload();
        mWorkingFile = DownloadManager.getInstance().tryGetFile(mContext, mClickFileItem, mProgressBar, mProgressValue, true, mDownloadCallback); // parameter: true  --- need opitimized
        // local file, then read rights directly
        if (mWorkingFile != null) {
            // do some special handler for nxl file
            bIsNxl = RenderHelper.isNxlFile(mWorkingFile.getPath());
            if (bIsNxl) {
                readNxlRights();
                if (mCmdOperate == CmdOperate.PROTECT || mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_REPO || mCmdOperate == CmdOperate.COMMAND_ADD_PROTECT) {
                    mOperateButton.setEnabled(false);
                }
            } else { // normal file
                if (mWorkingFile.getName().toLowerCase().endsWith(".nxl")) { // invalid nxl file
                    GenericError.showUI(this, mContext.getString(R.string.hint_msg_nxl_invalid_type), true, false, true, null);
                    mOperateButton.setEnabled(false);
                }
            }
        } else { // remote file
            beforeDownloadSetting();
        }
    }


    private void tryGetMyVaultFile() {
        MyVaultFile f = (MyVaultFile) mFileBase;
        String localPath = f.getLocalPath();
        if (localPath == null || localPath.isEmpty()) {
            //Download file
            FileOperation.downloadNxlFile(mContext, 1, mFileBase, mDownloadCallback);
        } else {
            readNxlRights();
        }
    }

    private void beforeDownloadSetting() {
        // hide rights layout
        if (isNxlFile()) {
            mRlViewRightsLayout.setVisibility(View.INVISIBLE);
            validityLayout.setVisibility(View.INVISIBLE);
        } else {
            mRlSpecifyRightsLayout.setVisibility(View.GONE);
        }
        // disable button
        mOperateButton.setEnabled(false);
        mEditText.setEnabled(false);
        commentWidget.getEditText().setEnabled(false);
        // show progress bar layout
        showProgress();
    }

    private void afterDownloadSetting() {
        // remove progress view
        progressLayout.setVisibility(View.GONE);
        // visible rights layout
        if (isNxlFile()) {
            mRlViewRightsLayout.setVisibility(View.VISIBLE);
            validityLayout.setVisibility(View.VISIBLE);
        } else {
            mRlSpecifyRightsLayout.setVisibility(View.VISIBLE);
        }
        // enable button
        mOperateButton.setEnabled(true);
        mEditText.setEnabled(true);
        commentWidget.getEditText().setEnabled(true);
    }

    // init downloader
    private void initDownload() {
        mDownloadCallback = new DownloadManager.IDownloadCallBack() {
            @Override
            public void onDownloadFinished(boolean taskStatus, String localPath, @Nullable FileDownloadException e) {
                afterDownloadSetting();
                // remove the downloader
                if (mClickFileItem != null) {
                    DownloadManager.getInstance().removeDownloader(mClickFileItem);
                }
                // will display the rights if is nxl file.
                if (taskStatus) {
                    mWorkingFile = new File(localPath);
                    bIsNxl = RenderHelper.isNxlFile(mWorkingFile.getPath());
                    if (bIsNxl) {
                        readNxlRights();
                        if (mCmdOperate == CmdOperate.PROTECT || mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_REPO || mCmdOperate == CmdOperate.COMMAND_ADD_PROTECT) {
                            mOperateButton.setEnabled(false);
                        }
                    } else { // normal file
                        if (mWorkingFile.getName().toLowerCase().endsWith(".nxl")) { // invalid nxl file
                            GenericError.showUI(ProtectShareActivity.this, mContext.getString(R.string.hint_msg_nxl_invalid_type), true, false, true, null);
                            mOperateButton.setEnabled(false);
                        }
                    }
                } else {
                    // exception handler
                    if (e != null) {
                        switch (e.getErrorCode()) {
                            case AuthenticationFailed:
                                SkyDRMApp.getInstance().getSession().sessionExpiredHandler(mContext);
                                break;
                            case NetWorkIOFailed:
                                ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.excep_network_unavailable));
                                downloadErrorHandler();
                                break;
                            case ExportedFileTooLarge:
                                ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.excep_export_google_file));
                                downloadErrorHandler();
                                break;
                            default: // For third party drive, since don't handle error exception (such as can't distinguish network IO exception and File Not Found(404) and so on),
                                // so now display this info roughly, will display more friendly info later.
                                ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.excep_download_failed));
                                downloadErrorHandler();
                                break;
                        }
                    }
                }
            }

            @Override
            public void onDownloadProgress(long value) {
                // for myVault download progress.
                if ((mFileFrom != null && mFileFrom == FileFrom.FILE_FROM_MYVAULT) // the menu command operate "view file info" of myVault.
                        || (mFileBase != null && mRemoteViewResultBean != null)) { // myVault simple remote view
                    String text = String.format(Locale.getDefault(), "%d", value) + "%";
                    mProgressValue.setText(text);
                    mProgressBar.setProgress((int) value);
                    return;
                }

                // for other drive, try download if the file is downloading
                if (DownloadManager.getInstance().tryGetDownloader(mClickFileItem) != null
                        && DownloadManager.getInstance().tryGetDownloader(mClickFileItem).isbIsDownloading()) {
                    if (mProgressBar != null) {
                        mProgressBar.setProgress((int) value);
                    }
                    if (mProgressValue != null) {
                        String text = String.format(Locale.getDefault(), "%d", value) + "%";
                        mProgressValue.setText(text);
                    }
                }
            }
        };
    }

    // handle download error, such as protect or share from home menu in the case that disconnect network
    private void downloadErrorHandler() {
        if (mCmdOperate == CmdOperate.PROTECT || mCmdOperate == CmdOperate.COMMAND_PROTECT_FROM_REPO) {
            toggleView.setEnabled(false);
            togglePrint.setEnabled(false);
            toggleEdit.setEnabled(false);
            toggleWatermark.setEnabled(false);
            toggleDownload.setEnabled(false);
            toggleShare.setEnabled(false);
        } else if (mCmdOperate == CmdOperate.SHARE || mCmdOperate == CmdOperate.COMMAND_SHARE_FROM_REPO) {
            mEditText.setEnabled(false);
            commentWidget.getEditText().setEnabled(false);
            if (!bIsNxl) {
                toggleView.setEnabled(false);
                togglePrint.setEnabled(false);
                toggleEdit.setEnabled(false);
                toggleWatermark.setEnabled(false);
                toggleDownload.setEnabled(false);
                toggleShare.setEnabled(false);
            }
        }
        mOperateButton.setEnabled(false);
    }

    private void initProtect() {
        mTvTitle.setText(getResources().getString(R.string.Protect));
        mOperateButton.setText(getResources().getString(R.string.Create_protected_file));
        mShareWithLayout.setVisibility(View.GONE);
    }

    private void initShare() {
        mEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Drawable drawable = mEditText.getCompoundDrawables()[2];
                if (drawable == null) return false;
                if (motionEvent.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (motionEvent.getX() > mEditText.getWidth() - mEditText.getPaddingLeft() - drawable.getIntrinsicWidth()) {
                    mEditText.setFocusableInTouchMode(false);
                    mEditText.setFocusable(false);
                    lunchContactPageWithResult(Constant.REQUEST_CODE_SELECT_EMAILS);
                } else {
                    mEditText.setFocusableInTouchMode(true);
                    mEditText.setFocusable(true);
                }
                return ProtectShareActivity.super.onTouchEvent(motionEvent);
            }
        });

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                CommonUtils.wrapEmail(mContext, s.toString(), false, mEmailList, mFlowLayout, mEditText);
            }
        });

        // handle enter key to wrap text user input
        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    CommonUtils.wrapEmail(mContext, mEditText.getText().append(" ").toString(), false, mEmailList, mFlowLayout, mEditText);
                }
                return false;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_CODE_SELECT_EMAILS && resultCode == RESULT_OK) {
          //  parseContactsParcelAndDisplay(data);
            Serializable serializableExtra = data.getSerializableExtra(Constant.SELECT_EMAIL_RESULT);
            if (serializableExtra instanceof HashSet) {
                HashSet contacts = (HashSet) serializableExtra;
                for (Object o : contacts) {
                    if (o instanceof String) {
                        CommonUtils.wrapEmail(mContext, (String) o, true, mEmailList, mFlowLayout, mEditText);
                    }
                }
            }
        }
    }
    private void parseContactsParcelAndDisplay(Intent data) {
        mFlowLayout.wrapEmailFromContact(data);
    }

    private void doShare() {
        if (mCmdOperate == CmdOperate.SHARE) {
            shareFile();
        } else if (mCmdOperate == CmdOperate.COMMAND_ADD_SHARE) {
            addFile();
        }
    }

    private void shareFile() {
        // wrap emails
        if (!TextUtils.isEmpty(mEditText.getText().toString())) {
            CommonUtils.wrapEmail(mContext, mEditText.getText().toString(), true, mEmailList, mFlowLayout, mEditText);
        }
        // get valid emails

        final List<String> listEmail = CommonUtils.getValidEmails(mContext,mEmailList);
        if (listEmail == null || listEmail.size() == 0) {
            return;
        }

        // judge share whether expired for nxl file
        if (bIsExpired) { // or directly catch rest api exception then give prompt.
            ToastUtil.showToast(mContext, getResources().getString(R.string.share_rights_expired));
            return;
        }

        String comment = commentWidget.getText().toString();
        // whether switch into "share repo file" mode for share operate.
        if (mCmdOperate == CmdOperate.SHARE && isSwitchToShareRepoFileModel()) {
            shareRepoFile(listEmail, comment);
            return;
        }

        // share local file
        Expiry expiry = buildExpiry();
        if (isExpired(iExpiry)) return;
        mIExecuteCommand.shareLocalFile(mContext,
                mWorkingFile,
                listEmail,
                getRights(),
                getObligations(),
                comment,
                expiry,
                new IShareComplete() {
                    @Override
                    public void onShareComplete(boolean result) {
                        afterShareComplete(result);
                    }
                });

    }

    /**
     * Share repository file
     */
    private void shareRepoFile(final List<String> listEmail, final String comment) {
        // for myVault file
        if (mFileBase != null) {
            Expiry expiry = buildExpiry();
            if (isExpired(iExpiry)) return;
            MyVaultFile f = (MyVaultFile) mFileBase;
            mIExecuteCommand.shareRepoFile(mContext,
                    f.getName(),
                    f.getRepoId(),
                    f.getPathId(),
                    f.getPathDisplay(),
                    getPermissions(),
                    listEmail,
                    comment,
                    null, // pass null for nxl file
                    expiry,
                    new IShareComplete() {
                        @Override
                        public void onShareComplete(boolean result) {
                            afterShareComplete(result);
                        }
                    });
        }

        // for myDrive
        if (mClickFileItem != null) {
            Expiry expiry = buildExpiry();
            if (isExpired(iExpiry)) return;
            BoundService bs = mClickFileItem.getService();
            mIExecuteCommand.shareRepoFile(mContext,
                    mClickFileItem.getName(),
                    bs.rmsRepoId,
                    mClickFileItem.getCloudPath(),
                    mClickFileItem.getDisplayPath(),
                    getPermissions(),
                    listEmail,
                    comment,
                    toggleWatermark.isChecked() ? watermarkValue : null, // need to judge watermark toggle if is checked
                    expiry,
                    new IShareComplete() {
                        @Override
                        public void onShareComplete(boolean result) {
                            afterShareComplete(result);
                        }
                    });
        }
    }

    /**
     * get permissions parameter for share repository file.
     * Note: include the watermark value
     */
    private int getPermissions() {
        int permission = -1;

        if (bIsNxl) { // myVault all files & myDrive simple remote view nxl files

            // for simple remote view of myVault and myDrive (case: remoteViewPage -> infoPage -> sharePage)
            if (mRemoteViewResultBean != null) {
                permission = mPermissions;
            }

            // for myVault
            if (mMyVaultMetaData != null) {
                List<String> list = mMyVaultMetaData.getResults().getDetail().getRights();
                Rights rights = new Rights();
                rights.listToRights(list);
                permission = rights.toInteger();
            }

            // for myDrive
            if (mINxlFileFingerPrint != null) {
                permission = mINxlFileFingerPrint.toInteger();
            }

        } else { // normal file in myDrive
            Rights rights = getRights();
            if (getObligations() != null) {
                rights.setWatermark(getObligations().hasWatermark());
            }
            permission = rights.toInteger();
        }

        return permission;
    }

    /**
     * Do some setting when share complete.
     */
    private void afterShareComplete(boolean result) {
        // hidden soft-keyboard
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            // the first para is windowToken, which can be any current view's window token.
            imm.hideSoftInputFromWindow(mOperateButton.getWindowToken(), 0);
        }

        if (result) {
            // notify refresh ui
            EventBus.getDefault().post(new ShareCompleteNotifyEvent());
            // popup succeed prompt window
            CommonUtils.popupShareSucceedTip(mContext, mFileName, mRootView, mEmailList, bIsNxl);
            // clear emails
            mEmailList.clear();
            // close the previous activity
            if (mCmdOperate == CmdOperate.COMMAND_ADD_SHARE) {
                ActivityManager.getDefault().finishActivityByClass(CmdOperateFileActivity.class);
            }
        }
    }

    private void doProtect() {
        if (mCmdOperate == CmdOperate.PROTECT) {
            protectFile();
        } else if (mCmdOperate == CmdOperate.COMMAND_ADD_PROTECT) {
            addFile();
        }
    }

    /**
     * Build expiry and write file header when protect or share
     *
     * @return
     */
    private Expiry buildExpiry() {

        // for share nxl file, will pass null
        if (iExpiry == null) {
            return null;
        }

        int option = iExpiry.getOption();
        switch (option) {
            case 0:
                return new Expiry.Builder().never().build();
            case 1:
                User.IRelative iRelative = (User.IRelative) iExpiry;
                return new Expiry.Builder().relative()
                        .setYear(iRelative.getYear())
                        .setMonth(iRelative.getMonth())
                        .setWeek(iRelative.getWeek())
                        .setDay(iRelative.getDay())
                        .build();
            case 2:
                User.IAbsolute iAbsolute = (User.IAbsolute) iExpiry;
                return new Expiry.Builder().absolute()
                        .setEndDate(iAbsolute.endDate())
                        .build();
            case 3:
                User.IRange iRange = (User.IRange) iExpiry;
                return new Expiry.Builder().range()
                        .setStartDate(iRange.startDate())
                        .setEndDate(iRange.endDate())
                        .build();
            default:
                throw new RuntimeException("Can not access this!");
        }
    }

    private void protectFile() {
        Expiry expiry = buildExpiry();
        if (isExpired(iExpiry)) return;
        mIExecuteCommand.protect(mContext,
                mWorkingFile,
                getRights(),
                getObligations(),
                expiry,
                mClickFileItem,
                new IProtectComplete() {
                    @Override
                    public void onProtectComplete(MyVaultUploadFileResult result) {
                        // popup succeed prompt window
                        if (result != null) {
                            // notify refresh ui
                            EventBus.getDefault().post(new ProtectCompleteNotifyEvent());
                            // popup succeed prompt window
                            CommonUtils.popupProtectSucceedTip(mContext, mRootView, mFileName);
                            // close the previous activity
                            if (mCmdOperate == CmdOperate.COMMAND_ADD_PROTECT) {
                                ActivityManager.getDefault().finishActivityByClass(CmdOperateFileActivity.class);
                            }
                        }
                    }
                });
    }

    private boolean isExpired(User.IExpiry expiry) {
        ExpiryChecker expiryChecker = new ExpiryChecker();
        if (!expiryChecker.isValidate(expiry)) {
            ExceptionDialog.showSimpleUI(mContext, getString(R.string.hint_msg_valid_period));
            return true;
        }
        return false;
    }

    private void addFile() {

        // for add-share, should check emails firstly.
        if (mCmdOperate == CmdOperate.COMMAND_ADD_SHARE) {
            // wrap emails
            if (!TextUtils.isEmpty(mEditText.getText().toString())) {
                CommonUtils.wrapEmail(mContext, mEditText.getText().toString(), true, mEmailList, mFlowLayout, mEditText);
            }
            // get valid emails
            final List<String> listEmail = CommonUtils.getValidEmails(mContext, mEmailList);
            if (listEmail == null || listEmail.size() == 0) {
                return;
            }
        }

        // do Add file
        mIExecuteCommand.AddFile(mContext,
                mBoundService,
                mDestFolder,
                mWorkingFile,
                new IAddComplete() {
                    @Override
                    public void onAddFileComplete(boolean taskStatus, @Nullable NXDocument uploadedDoc, FileUploadException e) {
                        if (taskStatus) {
                            ToastUtil.showToast(mContext, mContext.getString(R.string.upload_file_to_repository_success));
                            // then protect file or share file
                            if (uploadedDoc != null) {
                                if (mCmdOperate == CmdOperate.COMMAND_ADD_PROTECT) {
                                    mClickFileItem = uploadedDoc;
                                    protectFile();
                                } else if (mCmdOperate == CmdOperate.COMMAND_ADD_SHARE) {
                                    shareFile();
                                }
                            }
                            //// TODO: 5/14/2017 may need to notify ui to refresh
                        } else {
                            String errorMsg = mContext.getString(R.string.Upload_file_failed_no_period);
                            if (e != null) {
                                errorMsg += ", " + e.getMessage() + ".";
                            }
                            ToastUtil.showToast(mContext, errorMsg);
                        }
                    }
                });
    }

    /**
     * get rights from UI for normal file
     */
    private Rights getRights() {
        Rights rights = new Rights();

        if (toggleView.isChecked()) {
            rights.setView(true);
        }
        if (togglePrint.isChecked()) {
            rights.setPrint(true);
        }
        if (toggleEdit.isChecked()) {
            rights.setPrint(true);
        }
        if (toggleShare.isChecked()) {
            rights.setShare(true);
        }
        if (toggleDownload.isChecked()) {
            rights.setDownload(true);
        }
        // added for sharing local file
        if (toggleWatermark.isChecked()) {
            rights.setWatermark(true);
        }

        return rights;
    }

    /**
     * get obligations from UI for normal file, now only for overlay.
     */
    private Obligations getObligations() {
        Map<String, String> map = new HashMap<>();
        Obligations nxObligations = new Obligations();
        if (toggleWatermark.isChecked()) {
            map.put(Constant.RIGHTS_WATERMARK, watermarkValue);
        } else {
            map.put(Constant.RIGHTS_WATERMARK, null);
        }
        nxObligations.setObligation(map);

        return nxObligations;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(this);
    }
}
