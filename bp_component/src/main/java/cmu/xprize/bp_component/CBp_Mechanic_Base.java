//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//*********************************************************************************

package cmu.xprize.bp_component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.util.CAnimatorUtil;
import cmu.xprize.util.CErrorManager;
import cmu.xprize.util.TCONST;

public class CBp_Mechanic_Base implements IBubbleMechanic, View.OnTouchListener, View.OnClickListener {

    protected Context               mContext;
    protected CBP_Component         mParent;
    protected boolean               mInitialized = false;


    private final Handler           mainHandler = new Handler(Looper.getMainLooper());
    private HashMap                 queueMap    = new HashMap();
    protected boolean               _enabled    = true;
    private boolean                 _qDisabled  = false;

    private boolean                 _watchable    = true;
    private int[]                   _screenCoord  = new int[2];
    private Boolean                 _touchStarted = false;
    private long                    _time;
    private long                    _prevTime;

    private LocalBroadcastManager   bManager;

    protected boolean               _animationStarted = false;
    protected boolean               _stimulusShown    = false;
    protected boolean               _stimulusAnimated = false;
    protected boolean               _stimulusMoved    = false;

    protected float                 _alpha      = 0.80f;
    protected float[]               _scaleRange = {0.85f, 1.3f};
    //protected float[]               _scaleRange = {1f, 1f};

    protected CBp_Data              _currData;

    protected int                         _bubbleIntrinsicRadius;
    protected CBubble[]                   SBubbles;
    protected CBubbleStimulus             SbubbleStumulus;

    protected HashMap<Animator, CBubble>  inflators   = new HashMap<Animator, CBubble>();
    protected HashMap<Animator, CBubble>  translators = new HashMap<Animator, CBubble>();


    static final String TAG = "CBp_Mechanic_Base";


    protected void init(Context context, CBP_Component parent) {

        mContext = context;
        mParent  = parent;

        // Capture the local broadcast manager
        bManager = LocalBroadcastManager.getInstance(mContext);
        mParent.setOnTouchListener(this);
    }

    @Override
    public void onDestroy() {

        terminateQueue();

        for(int i1 = 0; i1 < SBubbles.length ; i1++) {
            if(SBubbles[i1] != null) {
                mParent.removeView((View) SBubbles[i1]);
                SBubbles[i1].onDestroy();
                SBubbles[i1] = null;
            }
        }
        if(SbubbleStumulus != null)
            mParent.removeView((View)SbubbleStumulus);

        SBubbles = null;
    }

    @Override
    public void onDraw(Canvas canvas) {

    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public void populateView(CBp_Data data) {

    }
    @Override
    public void doLayout(int width, int height, CBp_Data data) {

    }

    public void removeBubble(CBubble bubble) {
    }


    private void showStimulus(CBp_Data data) {

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        SbubbleStumulus = (CBubbleStimulus) View.inflate(mContext, R.layout.bubble_stimulus, null);

        // Set Color: pass in String e.g. "RED" - Cycle through the colors repetitively
        //
        SbubbleStumulus.setScale(0);
        SbubbleStumulus.setAlpha(1.0f);

        switch (data.stimulus_type) {

            case BP_CONST.REFERENCE:

                int[] shapeSet = BP_CONST.drawableMap.get(mParent.stimulus_data[data.dataset[data.stimulus_index]]);

                Drawable qDrawable = mParent.getResources().getDrawable(shapeSet[0], null);

                SbubbleStumulus.setContents(shapeSet[(int) (Math.random() * shapeSet.length)], null);
                break;

            case BP_CONST.TEXTDATA:
                SbubbleStumulus.setContents(0, mParent.stimulus_data[data.dataset[data.stimulus_index]]);
                break;
        }

        mParent.addView(SbubbleStumulus, layoutParams);
    }


    protected void runCommand(String command, Object target ) {

        CBubble bubble;
        long    delay = 0;

        switch(command) {

            case BP_CONST.POP_BUBBLE:

                bubble = (CBubble)target;
                delay  = bubble.pop();

                // stop listening to the bubble
                bubble.setOnClickListener(null);

                broadcastLocation(TCONST.GLANCEAT, bubble.getCenterPosition());

                post(BP_CONST.REMOVE_BUBBLE, bubble, delay);
                break;

            case BP_CONST.WIGGLE_BUBBLE:

                bubble = (CBubble)target;

                broadcastLocation(TCONST.GLANCEAT, bubble.getCenterPosition());

                CAnimatorUtil.wiggle(bubble, "horizontal", 0.10f, 70, 5);
                break;

            case BP_CONST.REMOVE_BUBBLE:

                bubble = (CBubble)target;

                removeBubble(bubble);

                mParent.removeView((View)target);
                break;

            case BP_CONST.SHOW_STIMULUS:

                if(!_stimulusShown) {

                    _stimulusShown = true;

                    showStimulus((CBp_Data) target);
                    post(BP_CONST.ZOOM_STIMULUS);
                }
                break;

            case BP_CONST.ZOOM_STIMULUS:

                if(!_stimulusAnimated) {

                    _stimulusAnimated = true;

                    float[] scale = new float[]{SbubbleStumulus.getHeight()};

                    // Persona - look at the stimulus
                    broadcastLocation(TCONST.GLANCEAT, new PointF(mParent.getWidth() / 2, mParent.getHeight() / 2));

                    SbubbleStumulus.setX((mParent.getWidth() - SbubbleStumulus.getWidth()) / 2);
                    SbubbleStumulus.setY((mParent.getHeight() - SbubbleStumulus.getHeight()) / 2);

                    Animator inflator = CAnimatorUtil.configZoomIn(SbubbleStumulus, 600, 0, new BounceInterpolator(), 0f, 3.0f);

                    inflator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationCancel(Animator arg0) {
                            //Functionality here
                        }

                        @Override
                        public void onAnimationStart(Animator arg0) {
                            //Functionality here
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {

                            post(BP_CONST.MOVE_STIMULUS, 400);
                        }

                        @Override
                        public void onAnimationRepeat(Animator arg0) {
                            //Functionality here
                        }
                    });

                    inflator.start();
                }
                break;

            case BP_CONST.MOVE_STIMULUS:

                if(!_stimulusMoved) {

                    _stimulusMoved = true;

                    float[] scale = new float[]{(BP_CONST.MARGIN_BOTTOM * .9f) / SbubbleStumulus.getHeight()};

                    float height = SbubbleStumulus.getHeight();
                    float scaledHeight = height * scale[0];

                    PointF wayPoints[] = new PointF[1];
                    PointF posFinal    = new PointF();

                    posFinal.x = SbubbleStumulus.getX();
                    posFinal.y = mParent.getHeight() - (scaledHeight + ((height - scaledHeight) / 2) + BP_CONST.STIM_PAD_BOTTOM);

                    wayPoints[0] = posFinal;

                    AnimatorSet inflator   = CAnimatorUtil.configZoomIn(SbubbleStumulus, 300, 0, new LinearInterpolator(), scale);
                    Animator    translator = CAnimatorUtil.configTranslate(SbubbleStumulus, 300, 0, wayPoints);

                    inflator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationCancel(Animator arg0) {
                            //Functionality here
                        }

                        @Override
                        public void onAnimationStart(Animator arg0) {
                            //Functionality here
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {

                            post(BP_CONST.SHOW_BUBBLES);
                        }

                        @Override
                        public void onAnimationRepeat(Animator arg0) {
                            //Functionality here
                        }
                    });

                    inflator.start();
                    translator.start();
                }
                break;

            case BP_CONST.CLEAR_CONTENT:

                AnimatorSet deflator           = new AnimatorSet();
                ArrayList<Animator> animations = new ArrayList<Animator>();

                for(int i1 = 0; i1 < SBubbles.length ; i1++) {
                    if(SBubbles[i1] != null)
                        animations.add(CAnimatorUtil.configZoomIn(SBubbles[i1], 600, 0, new AnticipateInterpolator(), 0f));
                }
                if(SbubbleStumulus != null)
                    animations.add(CAnimatorUtil.configZoomIn(SbubbleStumulus, 600, 0, new AnticipateInterpolator(), 0f));


                deflator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator arg0) {
                        //Functionality here
                    }

                    @Override
                    public void onAnimationStart(Animator arg0) {
                        //Functionality here
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        for(int i1 = 0; i1 < SBubbles.length ; i1++) {
                            if(SBubbles[i1] != null) {
                                mParent.removeView((View) SBubbles[i1]);
                                SBubbles[i1] = null;
                            }
                        }
                        if(SbubbleStumulus != null)
                            mParent.removeView((View)SbubbleStumulus);

                        SBubbles = null;
                    }

                    @Override
                    public void onAnimationRepeat(Animator arg0) {
                        //Functionality here
                    }
                });

                deflator.playTogether(animations);
                deflator.start();

                break;
        }

    }


    public class Queue implements Runnable {

        protected String _command;
        protected Object _target;

        public Queue(String command) {
            _command = command;
        }

        public Queue(String command, Object target) {
            _command = command;
            _target  = target;
        }


        @Override
        public void run() {

            try {
                queueMap.remove(this);

                runCommand(_command, _target);
            }
            catch(Exception e) {
                CErrorManager.logEvent(TAG, "Run Error:", e, false);
            }
        }
    }


    /**
     *  Disable the input queues permenantly in prep for destruction
     *  walks the queue chain to diaable scene queue
     *
     */
    private void terminateQueue() {

        // disable the input queue permenantly in prep for destruction
        //
        _qDisabled = true;
        flushQueue();
    }


    /**
     * Remove any pending scenegraph commands.
     *
     */
    private void flushQueue() {

        Iterator<?> tObjects = queueMap.entrySet().iterator();

        while(tObjects.hasNext() ) {
            Map.Entry entry = (Map.Entry) tObjects.next();

            mainHandler.removeCallbacks((Queue)(entry.getValue()));
        }
    }


    /**
     * Keep a mapping of pending messages so we can flush the queue if we want to terminate
     * the tutor before it finishes naturally.
     *
     * @param qCommand
     */
    private void enQueue(Queue qCommand) {
        enQueue(qCommand, 0);
    }
    private void enQueue(Queue qCommand, long delay) {

        if(!_qDisabled) {
            queueMap.put(qCommand, qCommand);

            if(delay > 0) {
                mainHandler.postDelayed(qCommand, delay);
            }
            else {
                mainHandler.post(qCommand);
            }
        }
    }


    /**
     * Post a command to the tutorgraph queue
     *
     * @param command
     */
    public void post(String command) {
        post(command, 0);
    }
    public void post(String command, long delay) {

        enQueue(new Queue(command), delay);
    }


    /**
     * Post a command and target to this scenegraph queue
     *
     * @param command
     */
    public void post(String command, Object target) {
        post(command, target, 0);
    }
    public void post(String command, Object target, long delay) {

        enQueue(new Queue(command, target), delay);
    }


    @Override
    public void onClick(View view) {

        CBubble bubble = (CBubble)view;

        //post(BP_CONST.POP_BUBBLE, bubble);
        //post(BP_CONST.WIGGLE_BUBBLE, bubble);
        post(BP_CONST.CLEAR_CONTENT, bubble);
    }


    protected void broadcastLocation(String Action, PointF touchPt) {

        if(_watchable) {
            mParent.getLocationOnScreen(_screenCoord);

            // Let the persona know where to look
            Intent msg = new Intent(Action);
            msg.putExtra(TCONST.SCREENPOINT, new float[]{touchPt.x + _screenCoord[0], (float) touchPt.y + _screenCoord[1]});

            bManager.sendBroadcast(msg);
        }
    }


    /**
     * Add Root vector to path
     *
     * @param touchPt
     */
    private void startTouch(PointF touchPt) {

        broadcastLocation(TCONST.LOOKATSTART, touchPt);
    }


    /**
     * Update the glyph path if motion is greater than tolerance - remove jitter
     *
     * @param touchPt
     */
    private void moveTouch(PointF touchPt) {

       broadcastLocation(TCONST.LOOKAT, touchPt);
    }


    /**
     * End the current glyph path
     * TODO: Manage debouncing
     *
     */
    private void endTouch(PointF touchPt) {

        broadcastLocation(TCONST.LOOKATEND, touchPt);
    }


    public boolean onTouch(View view, MotionEvent event) {
        PointF touchPt;
        long   delta;
        final int action = event.getAction();

        // TODO: switch back to setting onTouchListener
        if(_enabled) {
            mParent.onTouchEvent(event);

            touchPt = new PointF(event.getX(), event.getY());

            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    _prevTime = _time = System.nanoTime();
                    startTouch(touchPt);
                    break;
                case MotionEvent.ACTION_MOVE:
                    _time = System.nanoTime();
                    moveTouch(touchPt);
                    break;
                case MotionEvent.ACTION_UP:
                    _time = System.nanoTime();
                    endTouch(touchPt);
                    break;
            }
            delta = _time - _prevTime;
        }

        //Log.i(TAG, "Touch Time: " + _time + "  :  " + delta);
        return true;
    }


    /**
     *
     * @param valueRange
     * @return
     */
    protected float getRandInRange(float[] valueRange) {

        float range = valueRange[BP_CONST.MAX] - valueRange[BP_CONST.MIN];
        float rand  = valueRange[BP_CONST.MIN];

        // check if less than error tolerance.
        //
        if( range > 0.01) {
            rand = (float)(valueRange[BP_CONST.MIN] + Math.random() * range);
        }

        return rand;
    }


    /**
     *
     * @param valueRange
     * @return
     */
    protected int getRandInRange(int[] valueRange) {

        int range = valueRange[BP_CONST.MAX] - valueRange[BP_CONST.MIN];
        int rand  = valueRange[BP_CONST.MIN];

        // check if less than error tolerance.
        //
        if( range > 0) {
            rand = (int)(valueRange[BP_CONST.MIN] + Math.random() * range);
        }

        return rand;
    }


}
