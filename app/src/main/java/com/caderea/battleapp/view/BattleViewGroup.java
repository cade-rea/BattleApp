package com.caderea.battleapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import com.caderea.battleapp.R;
import com.caderea.battleapp.queue.BattleQueue;
import com.caderea.battleapp.queue.QueueDrawer;

public class BattleViewGroup extends ViewGroup {

    private QueueDrawer p1queue;
    private QueueDrawer p2queue;
    private ProgressBar tickProgress;
    private BattleDrawArea battleDrawArea;
    private BattleInputArea battleInputArea;

    private int topBarHeight = 150;
    private int inputAreaHeight = 400;

    public BattleViewGroup(Context context) {
        this(context, null);
    }

    public BattleViewGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        p1queue = findViewById(R.id.player1QDrawer);
        p2queue = findViewById(R.id.player2QDrawer);
        tickProgress = findViewById(R.id.tickProgressBar);
        battleDrawArea = findViewById(R.id.battleDrawArea);
        battleInputArea = findViewById(R.id.battle_buttons);

    }

    public void updateTickProgress(int progress) {
        tickProgress.setProgress(progress);
    }

    public void setQueues(BattleQueue q1, BattleQueue q2) {
        p1queue.setQueue(q1);
        p2queue.setQueue(q2);
    }

    public void refreshQueues() {
        p1queue.update();
        p2queue.update();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int totalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int totalHeight = MeasureSpec.getSize(heightMeasureSpec);

        int oneFifthWidth = totalWidth / 5;
        int queueHeight  = totalHeight - topBarHeight - inputAreaHeight;
        int battleDrawWidth = totalWidth - (2 * oneFifthWidth);

        p1queue.measure(MeasureSpec.makeMeasureSpec(oneFifthWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(queueHeight, MeasureSpec.EXACTLY));
        p2queue.measure(MeasureSpec.makeMeasureSpec(oneFifthWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(queueHeight, MeasureSpec.EXACTLY));
        tickProgress.measure(MeasureSpec.makeMeasureSpec(totalWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(topBarHeight, MeasureSpec.EXACTLY));
        battleDrawArea.measure(MeasureSpec.makeMeasureSpec(battleDrawWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(queueHeight, MeasureSpec.EXACTLY));
        battleInputArea.measure(MeasureSpec.makeMeasureSpec(totalWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(inputAreaHeight, MeasureSpec.EXACTLY));

        setMeasuredDimension(totalWidth, totalHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;

        int oneFifthWidth = width / 5;

        int tickLeft = 0;
        int tickTop = 0;
        int tickRight = width;
        int tickBottom = topBarHeight;
        tickProgress.layout(tickLeft, tickTop, tickRight, tickBottom);

        int inputAreaLeft = left;
        int inputAreaTop = height - inputAreaHeight;
        int inputAreaRight = right;
        int inputAreaBottom = height;
        battleInputArea.layout(inputAreaLeft, inputAreaTop, inputAreaRight, inputAreaBottom);

        // queues 20% of each side
        int q1Left = 0;
        int q1Top = topBarHeight;
        int q1Right = q1Left + oneFifthWidth;
        int q1Bottom = inputAreaTop;
        p1queue.layout(q1Left, q1Top, q1Right, q1Bottom);

        int q2Left = right - oneFifthWidth;
        int q2Top = topBarHeight;
        int q2Right = right;
        int q2Bottom = inputAreaTop;
        p2queue.layout(q2Left, q2Top, q2Right, q2Bottom);

        int drawAreaLeft = q1Right + 1;
        int drawAreaTop = topBarHeight;
        int drawAreaRight = q2Left - 1;
        int drawAreaBottom = inputAreaTop;
        battleDrawArea.layout(drawAreaLeft, drawAreaTop, drawAreaRight, drawAreaBottom);

    }
}
