package cmu.xprize.sb_component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by jacky on 2016/6/22.
 */

public class CSb_Scoreboard extends android.support.percent.PercentRelativeLayout {
    private CSb_Coinbag bag_pennies;
    public CSb_Coinbag bag_dimes;
    private CSb_Coinbag bag_dollars;

    private CSb_Lollipop lollipop_pennies;
    public CSb_Lollipop lollipop_dimes;
    private CSb_Lollipop lollipop_dollars;


    private CSb_Lollipop[] lollipops;
    private CSb_Coinbag[] coinbags;
    private static float ratio = 302/210.0f;
    private int mScore;
    private int mColNum;
    private int widthScale;
    private boolean init = true;


    private static float DPTOPX;
    private Context context;

    private ImageView largeCoin;


    protected static float[][]    LAYOUT_CIRCLE = {
            {0.25f, 0.1f},
            {0.55f, 0.1f},
            {0.1f, 0.3f},
            {0.4f, 0.3f},
            {0.7f, 0.3f},
            {0.1f, 0.5f},
            {0.4f, 0.5f},
            {0.7f, 0.5f},
            {0.25f, 0.7f},
            {0.55f, 0.7f}
    };


    public CSb_Scoreboard(Context context) {
        super(context);
        init(context, null);
    }

    public CSb_Scoreboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CSb_Scoreboard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int widthSize = r - l;
        int heightSize = b - t;

        int bagWidth = (int)(widthSize * 40.0 / widthScale);
        int bagHeight = (int)(heightSize * 0.728);

        int lollipopWidth = (int)(widthSize * 100.0 / widthScale);
        int bagGap = (int)(widthSize * 50.0 / widthScale);
        int bagMarginLeft = (int)(widthSize * 30.0 / widthScale);
        int lollipopGap = bagGap;
        int lollipopMarginLeft = 0;

//        30 40 30 40 30 40 30

        for(int i = mColNum - 1; i >= 0; i--){
            coinbags[i].layout(bagMarginLeft,
                    heightSize - bagHeight,
                    bagMarginLeft + bagWidth,
                    heightSize);
            lollipops[i].layout(lollipopMarginLeft,
                    0, lollipopMarginLeft + lollipopWidth,
                    heightSize);
            bagMarginLeft += bagGap;
            lollipopMarginLeft += lollipopGap;
        }


//        bag_dollars.layout((int)(widthSize * 0.143),
//                            heightSize - bagHeight,
//                (int)(widthSize * 0.143) + bagWidth,
//                heightSize);
//
//        bag_dimes.layout((int)(widthSize * 0.381),
//                heightSize - bagHeight,
//                (int)(widthSize * 0.381) + bagWidth,
//                heightSize);
//
//        bag_pennies.layout((int)(widthSize * 0.619),
//                heightSize - bagHeight,
//                (int)(widthSize * 0.619) + bagWidth,
//                heightSize);
//
//        int lollipopWidth = (int)(widthSize * 0.476);
//        lollipop_dollars.layout(0, 0, lollipopWidth, heightSize);
//        lollipop_dimes.layout((int)(widthSize * 0.238), 0, (int)(widthSize * 0.238) + lollipopWidth, heightSize);
//        lollipop_pennies.layout((int)(widthSize * 0.476), 0, (int)(widthSize * 0.476) + lollipopWidth, heightSize);

    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;

        if(attrs != null) {

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CSb_Scoreboard,
                    0, 0);

            try {
                //inflate(getContext(), R.layout.sb_scoreboard, this);
                this.context = context;
                this.mScore = a.getInteger(R.styleable.CSb_Scoreboard_score, 0);
                this.mColNum = a.getInteger(R.styleable.CSb_Scoreboard_col_num, 3);
                int score = mScore;
                coinbags = new CSb_Coinbag[mColNum];
                lollipops = new CSb_Lollipop[mColNum];
                for(int i = 0; i < mColNum; i++){
                    coinbags[i] = new CSb_Coinbag(context);
                    lollipops[i] = new CSb_Lollipop(context);
                    coinbags[i].setImageResource(R.drawable.coin_bronze);
                    lollipops[i].setImageResoure(R.drawable.coin_bronze);
                    coinbags[i].setCoinNumber(score % 10);
                    score /= 10;
                    addView(coinbags[i]);
                    addView(lollipops[i]);
                    lollipops[i].setVisibility(INVISIBLE);
                }

                widthScale = 50 * (mColNum + 1);

//                bag_pennies = new CSb_Coinbag(context);
//                bag_dimes = new CSb_Coinbag(context);
//                bag_dollars = new CSb_Coinbag(context);
//
//                bag_pennies.setImageResource(R.drawable.coin_bronze);
//                bag_dimes.setImageResource(R.drawable.coin_silver);
//                bag_dollars.setImageResource(R.drawable.coin_gold);
//
//                lollipop_pennies = new CSb_Lollipop(context);
//                lollipop_dimes = new CSb_Lollipop(context);
//                lollipop_dollars = new CSb_Lollipop(context);
//
//                lollipop_pennies.setImageResoure(R.drawable.coin_bronze);
//                lollipop_dimes.setImageResoure(R.drawable.coin_silver);
//                lollipop_dollars.setImageResoure(R.drawable.coin_gold);

                largeCoin = new ImageView(context);
                largeCoin.setImageResource(R.drawable.coin_gold);
                largeCoin.setVisibility(INVISIBLE);
                largeCoin.setScaleType(ImageView.ScaleType.FIT_XY);



//                addView(bag_pennies);
//                addView(bag_dimes);
//                addView(bag_dollars);
//                addView(lollipop_pennies);
//                addView(lollipop_dimes);
//                addView(lollipop_dollars);
                addView(largeCoin);

//                bag_pennies.setVisibility(INVISIBLE);
//                bag_dollars.setVisibility(INVISIBLE);
//                bag_dimes.setVisibility(INVISIBLE);


//                lollipop_dimes.setVisibility(INVISIBLE);
//                lollipop_dollars.setVisibility(INVISIBLE);
//                lollipop_pennies.setVisibility(INVISIBLE);

            } finally {
                a.recycle();
            }
        }
    }

    public void increase() {
        mScore += 1;
        coinbags[0].increase();
        if(coinbags[0].getCoinNumber() == 10) {
            carryAnimation(0);
        }
    }

    public void decrease() {
        if(mScore == 0) return;
        if(coinbags[0].getCoinNumber() > 0) coinbags[0].decrease();
        else {
            if(mScore % 100 == 0)
                borrowAnimation(2);
            else
                borrowAnimation(1);
        }
//        borrowAnimation(bag_dollars);
        mScore -= 1;
    }

    private void setPosition() {
        int widthSize = getWidth();
        int widthScale = 50 * (mColNum + 1);
        int bagWidth = (int)(widthSize * 40.0 / widthScale);
        int lollipopWidth = (int)(widthSize * 100.0 / widthScale);
        int bagGap = (int)(widthSize * 50 / widthScale);
        int bagMarginLeft = (int)(widthSize * 30 / widthScale);
        int lollipopGap = bagGap;
        int lollipopMarginLeft = 0;

        for(int i = mColNum - 1; i >= 0; i--){
            coinbags[i].setX(bagMarginLeft);
            lollipops[i].setX(lollipopMarginLeft);
            bagMarginLeft += bagGap;
            lollipopMarginLeft += lollipopGap;
        }
    }

    private void carryAnimation(final int index) {
        if(index >= mColNum) return;

        final int width = getWidth();
        final int height = getHeight();

        setPosition();

        final CSb_Coinbag bag = coinbags[index];
        final CSb_Coinbag carryToBag = coinbags[index+1];
        final CSb_Lollipop lollipop = lollipops[index];

        bag.setVisibility(INVISIBLE);


        LayoutParams params_coin = (LayoutParams) largeCoin.getLayoutParams();
        params_coin.width = (int)(width * 100 / widthScale);
        params_coin.height = (int)(height / 302.0f * 100 );
        requestLayout();


//        final CSb_Coinbag carryToBag = bag == bag_pennies ? bag_dimes : bag_dollars;
//
//        final CSb_Lollipop lollipop = bag == bag_pennies? lollipop_pennies : lollipop_dimes;
        lollipop.setVisibility(VISIBLE);

        lollipop.animateToCircle();
        lollipop.postDelayed(new Runnable() {
            @Override
            public void run() {
//                if(bag == bag_pennies)
//                    largeCoin.setImageResource(R.drawable.coin_silver);
//                else
//                    largeCoin.setImageResource(R.drawable.coin_gold);
                largeCoin.setVisibility(VISIBLE);
                largeCoin.getDrawable().setAlpha(0);
                largeCoin.setX(lollipop.getX());
                largeCoin.setY(height/302.0f * 4);

                AnimatorSet animatorSet = new AnimatorSet();

                ValueAnimator alpha1 = ValueAnimator.ofInt(0, 255);
                alpha1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        largeCoin.getDrawable().setAlpha((Integer)animation.getAnimatedValue());
                    }
                });
                alpha1.setDuration(800);

                ValueAnimator alpha2 = ValueAnimator.ofFloat(1, 0);
                alpha2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        lollipop.setAlpha((Float)animation.getAnimatedValue());
                    }
                });
                alpha2.setDuration(800);

                ValueAnimator translateX = ValueAnimator.ofFloat(largeCoin.getX(),
                        carryToBag.getX() - width / 200.0f * 30 );
                translateX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        largeCoin.setX((Float) animation.getAnimatedValue());
                        lollipop.setX((Float) animation.getAnimatedValue());
                        requestLayout();
                    }
                });
                translateX.setDuration(1000);

                animatorSet.playTogether(alpha1, alpha2,translateX);

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        lollipop.setAlpha(1);
                        lollipop.setVisibility(INVISIBLE);

                        ValueAnimator scale = ValueAnimator.ofFloat(1, 0.2f);
                        scale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                LayoutParams params = (LayoutParams) largeCoin.getLayoutParams();
                                params.width = (int) (width * 100.0f / widthScale * (Float) animation.getAnimatedValue());
                                params.height = (int) (height / 302.0f * 100  * (Float) animation.getAnimatedValue());
                                requestLayout();
                            }
                        });
                        scale.setDuration(1000);

                        ValueAnimator translateX = ValueAnimator.ofFloat(largeCoin.getX(),
                                carryToBag.getX() + carryToBag.coins[0].getX());
                        translateX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                largeCoin.setX((Float) animation.getAnimatedValue());
                                requestLayout();
                            }
                        });
                        translateX.setDuration(1000);

                        ValueAnimator translateY2;
//                        if(carryToBag.getCoinNumber() > 0)
                            translateY2 = ValueAnimator.ofFloat(largeCoin.getY(),
                                    carryToBag.getY() +
                                            carryToBag.coins[carryToBag.getCoinNumber()].getY());
//                        else
//                            translateY2 = ValueAnimator.ofFloat(largeCoin.getY(),
//                                    carryToBag.getY() + carryToBag.coins[0].getY());

                        translateY2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                largeCoin.setY((Float) animation.getAnimatedValue());
                                requestLayout();
                            }
                        });
                        translateY2.setDuration(1000);


                        AnimatorSet set = new AnimatorSet();
                        set.playTogether(translateX, translateY2, scale);


                        set.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                largeCoin.setVisibility(INVISIBLE);
                                bag.setVisibility(VISIBLE);
                                bag.setCoinNumber(0);

                                carryToBag.increase();
                                if(carryToBag.getCoinNumber() == 10)
                                    carryAnimation(index+1);
//                                if (bag == bag_dimes)
//                                    bag_dollars.increase();
//                                else if (bag == bag_pennies) {
//                                    bag_dimes.increase();
//                                    if (bag_dimes.getCoinNumber() == 10)
//                                        carryAnimation(bag_dimes);
//                                }
                            }
                        });

                        set.start();
                    }
                });

                animatorSet.start();



            }
        }, 2500);

    }

    private void borrowAnimation(final int index){
        final int width = getWidth();
        final int height = getHeight();

        setPosition();
        final CSb_Coinbag bag = coinbags[index];
        final CSb_Coinbag lendToBag = coinbags[index-1];
        final CSb_Lollipop lollipop = lollipops[index-1];

        bag.decrease();

//        final CSb_Coinbag bagLandTo = bag == bag_dimes? bag_pennies : bag_dimes;
        lendToBag.setCoinNumber(10);
        lendToBag.setVisibility(INVISIBLE);

        LayoutParams params = (LayoutParams) largeCoin.getLayoutParams();
        params.width = (int) (width * 20 * widthScale);
        params.height = (int) (height / 302.0f * 20 );
        largeCoin.setX(bag.getX() + bag.coins[0].getX());
//        if(bag.getCoinNumber() > 0)
//            largeCoin.setY(bag.getY() - bag.coins[0].getY());
//        else
            largeCoin.setY(bag.getY() + bag.coins[bag.getCoinNumber()].getY());
        largeCoin.setVisibility(VISIBLE);

        lollipop.setAlpha(0);
        lollipop.setToCircle();
        lollipop.setVisibility(VISIBLE);
        lollipop.setToCircle();

        ValueAnimator translateX = ValueAnimator.ofFloat(largeCoin.getX(), bag.getX() - width * 30.0f / widthScale );
        translateX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                largeCoin.setX((Float) animation.getAnimatedValue());
                requestLayout();
            }
        });
        translateX.setDuration(1000);


        ValueAnimator translateY = ValueAnimator.ofFloat(largeCoin.getY(), height/302.0f * 4);
        translateY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                largeCoin.setY((Float) animation.getAnimatedValue());
                requestLayout();
            }
        });
        translateY.setDuration(1000);

        ValueAnimator scale = ValueAnimator.ofFloat(0.2f, 1);
        scale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LayoutParams params = (LayoutParams) largeCoin.getLayoutParams();
                params.width = (int) (width / 240.0f * 100  * (Float) animation.getAnimatedValue());
                params.height = (int) (height / 302.0f * 100  * (Float) animation.getAnimatedValue());

            }
        });
        scale.setDuration(1000);

        AnimatorSet set = new AnimatorSet();

        ValueAnimator translateX2 = ValueAnimator.ofFloat(bag.getX() - width * 30.0f / widthScale , lollipop.getX());
        translateX2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                largeCoin.setX((Float) animation.getAnimatedValue());
                lollipop.setX((Float) animation.getAnimatedValue());

            }
        });
        translateX2.setDuration(1000);


        ValueAnimator alpha1 = ValueAnimator.ofInt(255, 0);
        alpha1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                largeCoin.getDrawable().setAlpha((Integer)animation.getAnimatedValue());
            }
        });
        alpha1.setDuration(800);

        ValueAnimator alpha2 = ValueAnimator.ofFloat(0, 1);
        alpha2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                lollipop.setAlpha((Float)animation.getAnimatedValue());
            }
        });
        alpha2.setDuration(800);


//        largeCoin.setScaleType(ImageView.ScaleType.CENTER);
//        largeCoin.animate().scaleX(5).scaleY(5).translationYBy(0).setDuration(1000).start();

        set.playTogether(translateY, scale, translateX);
        set.playTogether(translateX2, alpha1, alpha2);
        set.play(translateX2).after(translateX);

        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                largeCoin.setVisibility(INVISIBLE);
                largeCoin.getDrawable().setAlpha(255);
                lollipop.animateToStick();
                lollipop.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        lollipop.setVisibility(INVISIBLE);
                        lendToBag.setVisibility(VISIBLE);
                        if (index > 1) {
                            borrowAnimation(index-1);
                        } else lendToBag.setCoinNumber(9);
                    }
                }, 2500);

            }
        });

        set.start();
    }
}

