package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.AsyncTask;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import org.json.JSONObject;

import cmu.xprize.ak_component.CAkQuestionBoard;
import cmu.xprize.ak_component.CAk_Component;
import cmu.xprize.ak_component.CAk_Data;
import cmu.xprize.robotutor.tutorengine.CObjectDelegate;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObjectImpl;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScriptable2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TInteger;
import cmu.xprize.sb_component.CSb_Scoreboard;
import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.ILogManager;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

/**
 * Created by jacky on 2016/7/6.
 */

public class TAkComponent extends CAk_Component implements ITutorObjectImpl, IDataSink {

    private CTutor mTutor;
    private CObjectDelegate mSceneObject;

    static final String TAG = "TAkComponent";

    public TAkComponent(Context context) {
        super(context);
    }

    public TAkComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TAkComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        mSceneObject = new CObjectDelegate(this);
        mSceneObject.init(context, attrs);

    }


//    **********************************************************
//    **********************************************************
//    *****************  Tutor Interface

    @Override
    public void UpdateValue(int value) {

        // update the Scope response variable  "<varname>.value"
        //
        mTutor.getScope().addUpdateVar(name() + ".value", new TInteger(value));

        boolean correct = true;

        reset();

        if(correct)
            mTutor.setAddFeature(TCONST.GENERIC_RIGHT);
        else
            mTutor.setAddFeature(TCONST.GENERIC_WRONG);
    }


    private void reset() {

        mTutor.setDelFeature(TCONST.GENERIC_RIGHT);
        mTutor.setDelFeature(TCONST.GENERIC_WRONG);
    }


    /**
     * Preprocess the data set
     *
     * @param data
     */
    @Override
    protected void updateDataSet(CAk_Data data) {

        // Let the compoenent process the new data set
        //
        super.updateDataSet(data);
    }



    /**
     *
     * @param dataSource
     */
    public void setDataSource(String dataSource) {

        // Ensure flags are reset so we don't trigger reset of the ALLCORRECCT flag
        // on the first pass.
        //
        reset();

        // We make the assumption that all are correct until proven wrong
        //
        mTutor.setAddFeature(TCONST.ALL_CORRECT);

        // TODO: globally make startWith type TCONST
        try {
            if (dataSource.startsWith(TCONST.SOURCEFILE)) {
                dataSource = dataSource.substring(TCONST.SOURCEFILE.length());

                String jsonData = JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutor.getTutorName() + "/" + TCONST.TASSETS + "/" + dataSource);
                // Load the datasource in the component module - i.e. the superclass
                loadJSON(new JSONObject(jsonData), null);

            } else if (dataSource.startsWith("db|")) {


            } else if (dataSource.startsWith("{")) {

                loadJSON(new JSONObject(dataSource), null);

            } else {
                throw (new Exception("BadDataSource"));
            }
        }
        catch (Exception e) {
            CErrorManager.logEvent(TAG, "Invalid Data Source for : " + name(), e, false);
        }
    }


    public void next() {

        // If wrong reset ALLCORRECT
        //
        if(mTutor.testFeatureSet(TCONST.GENERIC_WRONG)) {

            mTutor.setDelFeature(TCONST.ALL_CORRECT);
        }

        reset();

        if(dataExhausted())
            mTutor.setAddFeature(TCONST.FTR_EOD);

        super.next();


    }


    public void enable(Boolean enable) {
    }


    public void setButtonBehavior(String command) {
        mSceneObject.setButtonBehavior(command);
    }

    public void applyEventNode(String nodeName) {
        IScriptable2 obj = null;

        if(nodeName != null && !nodeName.equals("")) {
            try {
                obj = mTutor.getScope().mapSymbol(nodeName);
                obj.applyNode();

            } catch (Exception e) {
                // TODO: Manage invalid Behavior
                e.printStackTrace();
            }
        }
    }

    public void postQuestionBoard() {
        final CAkQuestionBoard questionBoard = this.questionBoard; //new CAkQuestionBoard(mContext);
        final PercentRelativeLayout percentLayout = (PercentRelativeLayout) getChildAt(0);

        int s = extraSpeed * 300;

        LayoutParams params = new LayoutParams(360, 80);
        params.addRule(CENTER_HORIZONTAL);
        percentLayout.addView(questionBoard, params);
        player.bringToFront();
        scoreboard.bringToFront();


        final AnimatorSet questionboardAnimator = CAnimatorUtil.configZoomIn(questionBoard, 3500,
                0, new LinearInterpolator(), 4f);
        ongoingAnimator.add(questionboardAnimator);
        ValueAnimator questionboardTranslationAnimator = ObjectAnimator.ofFloat(questionBoard,
                "y", getHeight() * 0.25f, getHeight() * 0.70f);
        questionboardAnimator.setDuration(5000-s);
        questionboardAnimator.setInterpolator(new LinearInterpolator());

        questionboardAnimator.playTogether(questionboardTranslationAnimator);

        questionboardAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                percentLayout.removeView(questionBoard);
                applyEventNode("NEXT");
                ongoingAnimator.remove(questionboardAnimator);
            }
        });

        questionboardAnimator.start();

        if(flag && teachFinger != null) {
            teachFinger.setVisibility(INVISIBLE);
            flag = false;
        }
    }

    public void postFinishLine() {
        int s = extraSpeed * 300;
        final PercentRelativeLayout percentLayout = (PercentRelativeLayout) getChildAt(0);

        final ImageView finishLine = new ImageView(mContext);
        LayoutParams params = new LayoutParams(getWidth()/3, getHeight()/10);
        params.addRule(CENTER_HORIZONTAL);
        finishLine.setImageResource(cmu.xprize.ak_component.R.drawable.finishline);

        percentLayout.addView(finishLine, params);
        player.bringToFront();
        scoreboard.bringToFront();

        final AnimatorSet finishLineAnimator = CAnimatorUtil.configZoomIn(finishLine, 3500,
                0, new LinearInterpolator(), 4f);

        ongoingAnimator.add(finishLineAnimator);

        ValueAnimator finishLineTranslationAnimator = ObjectAnimator.ofFloat(finishLine,
                "y", 0.25f * getHeight(), 0.9f * getHeight());
        finishLineAnimator.setDuration(5000-s);
        finishLineAnimator.setInterpolator(new LinearInterpolator());

        finishLineAnimator.playTogether(finishLineTranslationAnimator);

        finishLineAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                applyEventNode("NEXT");
                ongoingAnimator.remove(finishLineAnimator);
            }
        });

        finishLineAnimator.start();
    }

    public void judge(){
        reset();
        if(questionBoard.answerLane == player.lane){
            mTutor.setAddFeature(TCONST.GENERIC_RIGHT);
        }else {
            mTutor.setAddFeature(TCONST.GENERIC_WRONG);

        }
    }

    public void crash() {
        player.crash();
    }

    public void pause() {
        isRunning = false;
        for(Animator animator : ongoingAnimator)
            animator.pause();

    }

    public void resume() {
        isRunning = true;
        for(Animator animator : ongoingAnimator)
            animator.resume();
    }

    public void increaseScore() {
        mask.setVisibility(VISIBLE);
        Animator animator = scoreboard.reward(player.getX(), player.getY() - player.getHeight(), "+" + extraSpeed);
        LayoutParams params =  (LayoutParams) getLayoutParams();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                scoreboard.removeTextView();
                scoreboard.increase(extraSpeed);
                player.score += extraSpeed;

                new AnimateScoreboard().execute(scoreboard);
            }
        });
        animator.start();
    }

    public void decreaseScore() {
        mask.setVisibility(VISIBLE);
        Animator animator = scoreboard.reward(player.getX(), player.getY() - player.getHeight(), "-" + extraSpeed);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                scoreboard.removeTextView();
                scoreboard.decrease(extraSpeed);
                player.score -= extraSpeed;
                new AnimateScoreboard().execute(scoreboard);
            }
        });
        animator.start();
    }

    private class AnimateScoreboard extends AsyncTask<CSb_Scoreboard, Integer, CSb_Scoreboard> {
        @Override
        protected CSb_Scoreboard doInBackground(CSb_Scoreboard... params) {
            CSb_Scoreboard scoreboard = params[0];
            synchronized (scoreboard.lock) {
                while(!scoreboard.isAnimating){
                    try {
                        scoreboard.lock.wait();
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                scoreboard.isAnimating = false;
            }

            return scoreboard;
        }

        @Override
        protected void onPostExecute(CSb_Scoreboard scoreboard) {
            super.onPostExecute(scoreboard);
            applyEventNode("NEXT");
            mask.setVisibility(INVISIBLE);
        }


    }


    //**********************************************************
    //**********************************************************
    //*****************  Common Tutor Object Methods

    @Override
    public void onDestroy() {

    }

    @Override
    public void setName(String name) {
        mSceneObject.setName(name);
    }

    @Override
    public String name() {
        return mSceneObject.name();
    }

    @Override
    public void setParent(ITutorSceneImpl mParent) {
        mSceneObject.setParent(mParent);
    }

    @Override
    public void setTutor(CTutor tutor) {
        mTutor = tutor;
        mSceneObject.setTutor(tutor);
    }

    @Override
    public void postInflate() {}

    @Override
    public void setNavigator(ITutorGraph navigator) {
        mSceneObject.setNavigator(navigator);
    }

    @Override
    public void setLogManager(ILogManager logManager) {
        mSceneObject.setLogManager(logManager);
    }


    @Override
    public CObjectDelegate getimpl() {
        return mSceneObject;
    }

    @Override
    public void zoomInOut(Float scale, Long duration) {

    }

    @Override
    public void wiggle(String direction, Float magnitude, Long duration, Integer repetition) {

    }

    @Override
    public void setAlpha(Float alpha) {

    }

}
