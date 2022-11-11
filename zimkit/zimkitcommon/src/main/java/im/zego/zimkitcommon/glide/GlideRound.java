package im.zego.zimkitcommon.glide;

public class GlideRound {
    private float leftTopRound;
    private float leftBottomRound;
    private float rightTopRound;
    private float rightBottomRound;

    public GlideRound(int round) {
        this.leftTopRound = round;
        this.leftBottomRound = round;
        this.rightTopRound = round;
        this.rightBottomRound = round;
    }

    public GlideRound(float leftTopRound, float leftBottomRound, float rightTopRound, float rightBottomRound) {
        this.leftTopRound = leftTopRound;
        this.leftBottomRound = leftBottomRound;
        this.rightTopRound = rightTopRound;
        this.rightBottomRound = rightBottomRound;
    }

    public float getLeftTopRound() {
        return leftTopRound;
    }

    public float getLeftBottomRound() {
        return leftBottomRound;
    }

    public float getRightTopRound() {
        return rightTopRound;
    }

    public float getRightBottomRound() {
        return rightBottomRound;
    }
}
