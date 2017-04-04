package cmu.xprize.asm_component;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Gravity;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.util.ArrayList;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.util.IEvent;
import cmu.xprize.util.IEventListener;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;


public class CAsm_Component extends LinearLayout implements ILoadableObject, IEventListener {


    private Context mContext;

    protected String mDataSource;
    private int _dataIndex;

    protected int[] numbers;

    //current digit
    protected int digitIndex;
    protected int numSlots;

    //corValue is the correct result
    //corDigit is current correct digit
    protected Integer corDigit;
    protected Integer corValue;
    protected String operation;
    protected String curImage;
    //used for addition
    protected String curStrategy;

    protected boolean dotbagsVisible = true;

    //used to show:
    //carrying in addition
    //borrowing in subtraction
    //repeated addition in multiplication
    protected Integer overheadVal = null;
    protected CAsm_Text overheadText = null;
    protected CAsm_Text overheadTextSupplement = null;
    protected int curOverheadCol = -1;

    //the number of Alleys
    protected int numAlleys = 0;

    private float scale = getResources().getDisplayMetrics().density;
    protected int alleyMargin = (int) (ASM_CONST.alleyMargin * scale);

    //if user is writing, stop the timer used to show dotbags
    //use TimeStamp to judge if it is the time to show the dotbags
    protected  boolean isWriting = false;
    protected boolean hasShown = false;
    protected long startTime;

//    Arithmetic problems will start with the
    protected int               placeValIndex;
    protected String[]          chimes = ASM_CONST.CHIMES[placeValIndex];
    protected String[]          twoRowschimes = new String[20];
    protected String            currentChime;

    protected ArrayList<CAsm_Alley> allAlleys = new ArrayList<>();

    protected IDotMechanics mechanics = new CAsm_MechanicBase();

    // TODO: wrap in LetterBox
    //protected CAsm_LetterBoxLayout Scontent;

    // json loadable
    public CAsm_Data[] dataSource;

    //Writing
    protected CAsm_Popup mPopup;
    protected CAsm_Popup mPopupSupplement;
    private boolean hasTwoPopup = false;

    private boolean clickPaused = false;
    protected int overheadCorrect = ASM_CONST.NO_INPUT;
    protected int resultCorrect = ASM_CONST.NO_INPUT;
    protected String curNode = "";

    static final String TAG = "CAsm_Component";

    public CAsm_Component(Context context) {

        super(context);
        init(context, null);
    }

    public CAsm_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CAsm_Component(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {

        setOrientation(VERTICAL);


        //inflate(getContext(), R.layout.asm_container, this);

        mContext = context;

        if (attrs != null) {

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RoboTutor,
                    0, 0);

            try {
                mDataSource = a.getString(R.styleable.RoboTutor_dataSource);
            } finally {
                a.recycle();
            }
        }

        // Get the letterboxed game container
        //
        //Scontent = (CAsm_LetterBoxLayout) findViewById(R.id.Scontent);
        //Scontent.setOnClickListener(this);
        mPopup = new CAsm_Popup(mContext);
        mPopupSupplement = new CAsm_Popup(mContext);
    }

    public void setDataSource(CAsm_Data[] _dataSource) {

        dataSource = _dataSource;
        _dataIndex = 0;
    }

    public void setDotBagsVisible(Boolean _dotbagsVisible, int curDigitIndex) {
        if (!operation.equals("x"))
            setDotBagsVisible(_dotbagsVisible, curDigitIndex, 0);
    }

    public void setDotBagsVisible(Boolean _dotbagsVisible, int curDigitIndex, int startRow) {
            if (curDigitIndex != digitIndex) return;
            if (System.currentTimeMillis() - startTime < 3000 && _dotbagsVisible) return;
            if (operation.equals("x") && !_dotbagsVisible) return;

            if (_dotbagsVisible && !hasShown && !isWriting) {
                if(curOverheadCol >= 0)
                    if((allAlleys.get(curOverheadCol).getTextLayout().getTextLayout(digitIndex).getText(0).getText().equals("")
                            || allAlleys.get(curOverheadCol).getTextLayout().getTextLayout(digitIndex).getText(0).getCurrentTextColor() == Color.RED) && curOverheadCol > 9) {
                        mechanics.highlightOverheadOrResult(ASM_CONST.HIGHLIGHT_OVERHEAD);
                        return;
                    } else if(allAlleys.get(curOverheadCol).getTextLayout().getTextLayout(digitIndex).getText(1).getText().equals("")
                            || allAlleys.get(curOverheadCol).getTextLayout().getTextLayout(digitIndex).getText(1).getCurrentTextColor() == Color.RED) {
                        mechanics.highlightOverheadOrResult(ASM_CONST.HIGHLIGHT_OVERHEAD);
                        return;
                    } else
                        curOverheadCol = -1;

                hasShown = true;

                int delayTime = 0;
                startRow = startRow >= 0? startRow : 0;
                int lastRow = operation.equals("x") ? startRow + 3 : allAlleys.size();
                for (int i = startRow; i < lastRow; i++) {
                    final CAsm_Alley curAlley = allAlleys.get(i);
                    final int _curDigitIndex = curDigitIndex;
                    if (curAlley.getDotBag().getVisibility() != VISIBLE)
                        delayTime = wiggleDigitAndDotbag(curAlley, delayTime, _curDigitIndex, startRow);
                }

                if (!dotbagsVisible)
                    mechanics.preClickSetup();

            } else if(!_dotbagsVisible){
                for (int alley = 0; alley < allAlleys.size(); alley++)
                    allAlleys.get(alley).getDotBag().setVisibility(INVISIBLE);
            } else
                return;

            dotbagsVisible = _dotbagsVisible;
    }

    public void setDotBagsVisible(Boolean _dotbagsVisible) {
        setDotBagsVisible(_dotbagsVisible, digitIndex);
    }

    public int wiggleDigitAndDotbag(final CAsm_Alley curAlley, int delayTime, final int curDigitIndex, int startRow) {
        final CAsm_DotBag curDB = curAlley.getDotBag();
        final CAsm_TextLayout curTextLayout;
        if (operation.equals("x")) curTextLayout = curAlley.getTextLayout().getTextLayout(numSlots-1);
        else curTextLayout = curAlley.getTextLayout().getTextLayout(digitIndex);
        CAsm_Text curText = curTextLayout.getText(1);

        if (!curText.getText().equals("") && !curText.getIsStruck()) {
            clickPaused = true;
            Handler h = new Handler();

            //wiggle operator
            if ((allAlleys.indexOf(curAlley) == ASM_CONST.OPERATION - 1 && !operation.equals("x"))) {
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(curDigitIndex != digitIndex) return;
                        curAlley.getTextLayout().getTextLayout(0).getText(1).wiggle(300, 1, 0, .5f);
                    }
                }, delayTime);
                delayTime += 1000;
            } else if (allAlleys.indexOf(curAlley) == startRow + 1 && operation.equals("x")) {
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(curDigitIndex != digitIndex) return;
                        curAlley.getTextLayout().getTextLayout(numSlots - 1).getText(0).wiggle(300, 1, 0, .3f);
                    }
                }, delayTime);
                delayTime += 1000;
            }

            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (curDigitIndex != digitIndex) return;
                    clickPaused = false;
                    curDB.setVisibility(VISIBLE);
                    curDB.wiggle(300, 1, 0, .05f);
                    if (!operation.equals("x")) curTextLayout.getText(0).wiggle(300, 1, 0, .3f);
                    curTextLayout.getText(1).wiggle(300, 1, 0, .3f);
                }
            }, delayTime);
            delayTime += 1000;
        } else if (!curText.getIsStruck())
            curDB.setVisibility(VISIBLE);

        return delayTime;
    }

    public void next() {
        isWriting = false;
        hasShown = false;
        curOverheadCol = -1;

        try {
            if (dataSource != null) {
                updateDataSet(dataSource[_dataIndex]);

                _dataIndex++;
            } else {
                CErrorManager.logEvent(TAG, "Error no DataSource : ", null, false);
            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, false);
        }

        mechanics.next();

    }

    public void nextDigit() {

        digitIndex--;
        isWriting = false;
        hasShown = false;
        startTime = System.currentTimeMillis();

        mechanics.nextDigit();

        if(operation.equals("x")) {
            corDigit = Integer.valueOf(CAsm_Util.intToDigits(corValue, numSlots-2)[digitIndex]);
            if(corDigit.equals(allAlleys.get(ASM_CONST.RESULT_OR_ADD_MULTI_PART1 - 1).getTextLayout().getDigit(digitIndex)))
                nextDigit();
        } else
            corDigit = Integer.valueOf(CAsm_Util.intToDigits(corValue, numSlots)[digitIndex]);
    }

    public boolean dataExhausted() {
        return (_dataIndex >= dataSource.length);
    }

    protected void updateDataSet(CAsm_Data data) {
        // TODO: talk about whether this should be part of base mechanics
        readInData(data);

        numSlots = CAsm_Util.maxDigits(numbers) + 1;
        digitIndex = numSlots;

        if (operation != null && operation.equals("x")) {
            alleyMargin = (int) (ASM_CONST.alleyMarginMul * scale);
            updateAllAlleyForMultiplication();
        } else {
            alleyMargin = (int) (ASM_CONST.alleyMargin * scale);
            updateAllAlleyForAddSubtract();
        }

        setMechanics();
        setSound();

    }

    private void updateAllAlleyForAddSubtract() {
        int val, id;
        boolean clickable = true;

        updateAlley(0, 0, ASM_CONST.ANIMATOR3, operation, false); // animator alley
        updateAlley(1, 0, ASM_CONST.ANIMATOR2, operation, false); // animator alley
        updateAlley(2, 0, ASM_CONST.ANIMATOR1, operation, false); // animator alley
        updateAlley(3, 0, ASM_CONST.OVERHEAD, operation, true); // carry/borrow alley

        // update alleys
        for (int i = 0; i < numbers.length; i++) {

            val = numbers[i];

            if (i == numbers.length - 2) {
                id = ASM_CONST.OPERATION;
            } else if (i == numbers.length - 1) {
                id = ASM_CONST.RESULT;
                val = 0;
                clickable = false;
            } else {
                id = ASM_CONST.REGULAR;
            }

            updateAlley(i + 4, val, id, operation, clickable);
        }

        // delete extra alleys
        int delta = numAlleys - (numbers.length + 4);

        if (delta > 0) {
            for (int i = 0; i < delta; i++) {
                delAlley();
            }
        }
    }

    private void updateAllAlleyForMultiplication() {
        numSlots += 2;

        // update alleys
        updateAlley(0, numbers[0], ASM_CONST.REGULAR_MULTI, operation, false);
        updateAlley(1, numbers[1], ASM_CONST.OPERATION_MULTI, operation, false);
        updateAlley(2, numbers[2], ASM_CONST.RESULT_OR_ADD_MULTI_PART1, operation, false);

        //Spare space to show repeated addition
        for (int i = 3; i < 17; i++)
            updateAlley(i, 0, i+1, operation, false);

        // delete extra alleys
        int delta = numAlleys - (numbers.length + 14);

        if (delta > 0) {
            for (int i = 0; i < delta; i++) {
                delAlley();
            }
        }
    }

    private void readInData(CAsm_Data data) {

        numbers = data.dataset;
        curImage = data.image;
        corValue = numbers[numbers.length - 1];
        operation = data.operation;

        //set default strategy as "count_up"
        if (data.strategy.equals("")) curStrategy = ASM_CONST.STRATEGY_COUNT_UP;
        else curStrategy = data.strategy;

    }

    private void setSound() {
        switch (operation) {
            case "+":
//                result dotbag will be the only one playing sound
                allAlleys.get(allAlleys.size() - 1).getDotBag().setIsAudible(true);
                break;
            case "-":
//                minuend dotbag is the only one that plays
                allAlleys.get(3).getDotBag().setIsAudible(true);
                break;
        }
    }

    private void setMechanics() {

        if ((mechanics.getOperation()).equals(operation)) {
            return; // no need to change mechanics
        }

        switch(operation) {

            case "+":
                mechanics = new CAsm_MechanicAdd(this);
                break;
            case "-":
                mechanics = new CAsm_MechanicSubtract(this);
                break;
            case "x":
                mechanics = new CAsm_MechanicMultiply(this);
                break;
        }
    }

    private void updateAlley(int index, int val, int id, String operation, boolean clickable) {
        if (index + 1 > numAlleys)
            addAlley(index, val, id, operation, clickable);
        else {
            CAsm_Alley currAlley = allAlleys.get(index);
            currAlley.update(val, curImage, id, operation, clickable, numSlots);
        }
    }

    private CAsm_Alley addAlley(int index, int val, int id, String operation, boolean clickable) {

        CAsm_Alley newAlley = new CAsm_Alley(mContext);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, alleyMargin);
        newAlley.setLayoutParams(lp);

        newAlley.update(val, curImage, id, operation, clickable, numSlots);

        //Scontent.addView(newAlley, index);
        addView(newAlley, index);
        allAlleys.add(index, newAlley);

        numAlleys++;

        return newAlley;
    }

    public void nextPlaceValue() {
        placeValIndex++;
        chimes = ASM_CONST.CHIMES[placeValIndex % 4 + 1];
        String [] firstRowChimes = chimes;
        String [] secondRowChimes = ASM_CONST.CHIMES[(placeValIndex - 1) % 4 + 1];
        for(int i = 0;i < 10; i++){
            twoRowschimes[i]  = firstRowChimes[i];
        }
        for(int i = 10;i< 19; i++){
            twoRowschimes[i]  = secondRowChimes[i-10+1];
        }
        for (CAsm_Alley alley: allAlleys) {
            CAsm_DotBag dotBag = alley.getDotBag();
            if(dotBag.getRows() == 1){
                dotBag.setChimes(chimes);
                dotBag.setChimeIndex(-1);
            }else{
                dotBag.setChimes(twoRowschimes);
                dotBag.setChimeIndex(-1);
            }
        }
    }

    public void playChime() {

    }

    public void resetPlaceValue() {
        placeValIndex = -1;
    }

    private void delAlley() {

        int index = numAlleys - 1;
        CAsm_Alley toRemove = allAlleys.get(index);

        toRemove.removeAllViews();
        //Scontent.removeView(toRemove);
        removeView(toRemove);
        allAlleys.remove(index);

        numAlleys--;

    }

    public boolean isWholeCorrect() {

        int ans;
        if (operation.equals("x"))
            ans = allAlleys.get(ASM_CONST.RESULT_OR_ADD_MULTI_PART1 - 1).getNum();
        else
            ans = allAlleys.get(numAlleys - 1).getNum();

        return corValue.equals(ans);

    }

    public boolean isDigitCorrect() {

        overheadCorrect = ASM_CONST.NO_INPUT;
        resultCorrect = ASM_CONST.NO_INPUT;
        boolean isOverheadCorrect, bottomCorrect;

        CAsm_TextLayout textLayout;
        if(operation.equals("x"))
            textLayout = allAlleys.get(ASM_CONST.RESULT_OR_ADD_MULTI_PART1 - 1).getTextLayout();
        else
            textLayout = allAlleys.get(numAlleys - 1).getTextLayout();

        //For multiplication, user can change the order of writing result.
        //e.g. If the result is 123, user input 1 first. We need to confirm the “1” is a correct input.
        if(operation.equals("x")) checkOtherBottomCorrect(textLayout);

        // first check bottom answer
        bottomCorrect = corDigit.equals(textLayout.getDigit(digitIndex));

        if (!bottomCorrect && textLayout.getDigit(digitIndex) != null) {
            wrongDigit(textLayout.getTextLayout(digitIndex).getText(1));
            if (!(operation.equals("x") && resultCorrect == ASM_CONST.ALL_INPUT_RIGHT))
                resultCorrect = ASM_CONST.NOT_ALL_INPUT_RIGHT;
        }

        // now check overhead answer
        if (overheadVal != null && overheadVal <= corValue) {
            if (overheadVal < 10)
                isOverheadCorrect = overheadVal.equals(overheadText.getDigit());
            else if (overheadTextSupplement.getDigit() == null || overheadText.getDigit() == null)
                isOverheadCorrect = false;
            else
                isOverheadCorrect = overheadVal.equals(overheadTextSupplement.getDigit() * 10 + overheadText.getDigit());

            if (isOverheadCorrect) {
                mechanics.correctOverheadText();
                overheadCorrect = ASM_CONST.ALL_INPUT_RIGHT;
            } else if (overheadVal < 10 ) {
                if (overheadText.getDigit() != null) {
                    wrongDigit(overheadText);
                    overheadCorrect = ASM_CONST.NOT_ALL_INPUT_RIGHT;
                } else
                    overheadCorrect = ASM_CONST.NO_INPUT;
            } else {
                boolean allRight = true, allEmpty = true;

                if (overheadTextSupplement.getDigit() != null) {
                    if (overheadTextSupplement.getDigit() != overheadVal / 10) {
                        wrongDigit(overheadTextSupplement);
                        overheadCorrect = ASM_CONST.NOT_ALL_INPUT_RIGHT;
                        allRight = false;
                    } else
                        overheadTextSupplement.cancelResult();
                    allEmpty = false;
                }

                if (overheadText.getDigit() != null) {
                    if (overheadText.getDigit() != overheadVal % 10) {
                        wrongDigit(overheadText);
                        overheadCorrect = ASM_CONST.NOT_ALL_INPUT_RIGHT;
                        allRight = false;
                    } else
                        overheadText.cancelResult();
                    allEmpty = false;
                }

                if (allRight) overheadCorrect = ASM_CONST.ALL_INPUT_RIGHT;
                if (allEmpty) overheadCorrect = ASM_CONST.NO_INPUT;
            }
        }

        return bottomCorrect;
    }

    public void checkOtherBottomCorrect(CAsm_TextLayout textLayout) {
        int otherBottomCorrect = 0;
        for(int i = 1; i < digitIndex; i++) {
            Integer curDigit = Integer.valueOf(CAsm_Util.intToDigits(corValue, numSlots-2)[i]);
            Integer digit = textLayout.getDigit(i);
            if(digit != null && !digit.equals(""))
                otherBottomCorrect = curDigit.equals(textLayout.getDigit(i))? 1 : 2;
            if(otherBottomCorrect == 0 && i+ 1 == digitIndex)
                break;
            if(otherBottomCorrect == 1) {
                textLayout.getTextLayout(i).getText(1).reset();
                resultCorrect = ASM_CONST.ALL_INPUT_RIGHT;
                i = digitIndex;
            } else if(otherBottomCorrect == 2) {
                wrongDigit(textLayout.getTextLayout(i).getText(1));
                resultCorrect = ASM_CONST.NOT_ALL_INPUT_RIGHT;
                i = digitIndex;
            }
        }
    }

    public void wrongDigit(final CAsm_Text t) {
            //Indicates that the digit the user entered is wrong w/ red text.
            t.setTextColor(Color.RED);
            clickPaused = true;
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    t.setText("");
                    t.setTextColor(Color.BLACK);
                    clickPaused = false;
                    setDotBagsVisible(true, digitIndex, mechanics.getCurRow()-2);
                }
            }, 1500);
    }

    public boolean getClickPaused() {return clickPaused;}

    public void highlightText(final CAsm_Text t) {
        //Useful to highlight individual Text-fields to call importance to them.
        int colorStart = Color.YELLOW;
        int colorEnd = Color.TRANSPARENT;
        ValueAnimator v = ValueAnimator.ofObject(new ArgbEvaluator(),colorStart,colorEnd);
        v.setDuration(1250);
        v.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                t.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        v.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                if (t.isWritable) {
                    //For multiplication, user could choose the order of writing result digits
                    if(operation.equals("x")) {
                        CAsm_TextLayout resultTextLayout;
                        resultTextLayout = allAlleys.get(ASM_CONST.RESULT_OR_ADD_MULTI_PART1 - 1).getTextLayout();
                        for(int i = 1; i <= digitIndex; i++) {
                            if(resultTextLayout.getTextLayout(i).getText(1).isWritable)
                                resultTextLayout.getTextLayout(i).getText(1).setResult();
                        }
                    }
                    t.setResult();
                }
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                if (t.isWritable) {
                    //For multiplication, user could choose the order of writing result digits
                    if(operation.equals("x")) {
                        CAsm_TextLayout resultTextLayout;
                        resultTextLayout = allAlleys.get(ASM_CONST.RESULT_OR_ADD_MULTI_PART1 - 1).getTextLayout();
                        for(int i = 1; i <= digitIndex; i++) {
                            if(resultTextLayout.getTextLayout(i).getText(1).isWritable)
                                resultTextLayout.getTextLayout(i).getText(1).setResult();
                        }
                    } else
                        t.setResult();
                }
            }
        });
        v.start();
    }

    public void highlightCurrentColumn() {
        //Highlights user's active column.
        for (CAsm_Alley alley: allAlleys) {
            try {
                CAsm_Text text = alley.getTextLayout().getTextLayout(digitIndex).getText(1);
                if (text.getDigit() != null || text.isWritable) {highlightText(text); }
            } catch (NullPointerException e) { continue;}
        }
    }

    public void updateText(CAsm_Text t1, CAsm_Text t2, boolean isClickingBorrowing) {
        isWriting = true;
        if (!mPopup.isActive && !mPopupSupplement.isActive) {
            ArrayList<IEventListener> listeners = new ArrayList<>();
            listeners.add(t2);
            listeners.add(this);
            mPopup.showAtLocation(this, Gravity.LEFT, 10, 10);
            mPopup.enable(true, listeners);

            if(isClickingBorrowing)
                mPopup.update(t2, 120, -300, 300, 300);
            else
                mPopup.update(t2, 60, 0, 300, 300);

            mPopup.isActive = true;

            if (t1 != null) {
                hasTwoPopup = true;
                listeners = new ArrayList<>();
                listeners.add(t1);
                listeners.add(this);

                mPopupSupplement.showAtLocation(this, Gravity.LEFT, 10, 10);
                mPopupSupplement.enable(true, listeners);

                if (isClickingBorrowing) {
                    mPopup.update(t2, 420, -300, 300, 300);
                    mPopupSupplement.update(t2, 120, -300, 300, 300);
                } else {
                    mPopup.update(t2, 360, 0, 300, 300);
                    mPopupSupplement.update(t2, 60, 0, 300, 300);
                }

                mPopupSupplement.isActive = true;
            }
        }
    }

    public void exitWrite() {

        mPopup.isActive = false;
        mPopup.enable(false,null);
        mPopup.dismiss();

        mPopupSupplement.isActive = false;
        mPopupSupplement.enable(false,null);
        mPopupSupplement.dismiss();

        resetHesitationTimer(3000);
    }

    public void resetHesitationTimer(int delayTime) {
        if(isWriting && !hasShown) {
            startTime = System.currentTimeMillis();
            isWriting = false;

            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setDotBagsVisible(true, digitIndex, mechanics.getCurRow()-2);
                }
            }, delayTime);
        }
    }

    public void onEvent(IEvent event) {

        if (!hasTwoPopup) {
            mPopup.reset();
            mPopupSupplement.reset();
        } else if (!overheadText.getText().equals("") && overheadTextSupplement.getText().equals("")) {
            mPopup.reset();
            hasTwoPopup = false;
        } else if (overheadText.getText().equals("") && !overheadTextSupplement.getText().equals("")) {
            mPopupSupplement.reset();
            hasTwoPopup = false;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            mechanics.handleClick();
        }
        return true;
    }

    /**
     * Load the data source
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {

        JSON_Helper.parseSelf(jsonData, this, CClassMap.classMap, scope);
        _dataIndex = 0;

    }

    public void addMapToTutor(String key, String value) {
    }

    public void delAddFeature(String delFeature, String addFeature) {
    }

    public void applyEventNode(String nodeName) {

    }

}