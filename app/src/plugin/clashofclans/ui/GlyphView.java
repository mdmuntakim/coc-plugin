package plugin.clashofclans.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * The icon set of the app. Every glyph is drawn with a Path on a 24x24 grid,
 * which keeps the APK small and means nothing is copied from the game.
 */
public final class GlyphView extends View {

    public static final int CREST = 0;
    public static final int TROPHY = 1;
    public static final int VILLAGE = 2;
    public static final int ARMY = 3;
    public static final int WAR = 4;
    public static final int TOOLS = 5;
    public static final int GEAR = 6;
    public static final int GEM = 7;
    public static final int ELIXIR = 8;
    public static final int STAR = 9;
    public static final int CLOCK = 10;
    public static final int TAG = 11;
    public static final int CHECK = 12;
    public static final int BOLT = 13;
    public static final int GOLD = 14;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private int kind = CREST;
    private int color = Palette.accent;

    public GlyphView(Context c) {
        this(c, null);
    }

    public GlyphView(Context c, AttributeSet attrs) {
        super(c, attrs);
        paint.setStyle(Paint.Style.FILL);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public GlyphView kind(int k) {
        this.kind = k;
        invalidate();
        return this;
    }

    public GlyphView color(int c) {
        this.color = c;
        invalidate();
        return this;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int fallback = Skin.dp(getContext(), 24);
        int w = resolveSize(fallback, widthMeasureSpec);
        int h = resolveSize(fallback, heightMeasureSpec);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float scale = Math.min(getWidth(), getHeight()) / 24f;
        canvas.save();
        canvas.translate((getWidth() - 24f * scale) / 2f, (getHeight() - 24f * scale) / 2f);
        canvas.scale(scale, scale);

        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);

        switch (kind) {
            case TROPHY: trophy(canvas); break;
            case VILLAGE: village(canvas); break;
            case ARMY: swords(canvas); break;
            case WAR: shield(canvas); break;
            case TOOLS: hammer(canvas); break;
            case GEAR: gear(canvas); break;
            case GEM: gem(canvas); break;
            case ELIXIR: elixir(canvas); break;
            case STAR: star(canvas, 12, 12.5f, 10f, 4.4f); break;
            case CLOCK: clock(canvas); break;
            case TAG: tag(canvas); break;
            case CHECK: check(canvas); break;
            case BOLT: bolt(canvas); break;
            case GOLD: coin(canvas); break;
            default: crest(canvas); break;
        }
        canvas.restore();
    }

    private void crest(Canvas canvas) {
        paint.setShader(new LinearGradient(0, 0, 0, 24,
                new int[]{Palette.accentLight, Palette.accent, Palette.accentDark},
                null, Shader.TileMode.CLAMP));
        path.reset();
        path.moveTo(12f, 1.6f);
        path.lineTo(21f, 4.7f);
        path.lineTo(21f, 12.2f);
        path.cubicTo(21f, 18.2f, 17.1f, 21.6f, 12f, 23.2f);
        path.cubicTo(6.9f, 21.6f, 3f, 18.2f, 3f, 12.2f);
        path.lineTo(3f, 4.7f);
        path.close();
        canvas.drawPath(path, paint);

        paint.setShader(null);
        paint.setColor(0xFF2A2013);
        path.reset();
        path.moveTo(12f, 3.6f);
        path.lineTo(19f, 6f);
        path.lineTo(19f, 12f);
        path.cubicTo(19f, 16.8f, 16.1f, 19.7f, 12f, 21f);
        path.cubicTo(7.9f, 19.7f, 5f, 16.8f, 5f, 12f);
        path.lineTo(5f, 6f);
        path.close();
        canvas.drawPath(path, paint);

        paint.setColor(Palette.accent);
        path.reset();
        path.moveTo(7.4f, 14.6f);
        path.lineTo(8.8f, 9.4f);
        path.lineTo(12f, 11.9f);
        path.lineTo(15.2f, 9.4f);
        path.lineTo(16.6f, 14.6f);
        path.close();
        canvas.drawPath(path, paint);
        rect.set(7.4f, 15.4f, 16.6f, 17.2f);
        canvas.drawRoundRect(rect, 0.7f, 0.7f, paint);
    }

    private void trophy(Canvas canvas) {
        paint.setColor(color);
        path.reset();
        path.moveTo(7.5f, 2.5f);
        path.lineTo(16.5f, 2.5f);
        path.lineTo(16.5f, 7f);
        path.cubicTo(16.5f, 11.2f, 14.6f, 13.4f, 12f, 13.4f);
        path.cubicTo(9.4f, 13.4f, 7.5f, 11.2f, 7.5f, 7f);
        path.close();
        canvas.drawPath(path, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.6f);
        rect.set(3.2f, 3.4f, 7.5f, 9.4f);
        canvas.drawArc(rect, 90, 180, false, paint);
        rect.set(16.5f, 3.4f, 20.8f, 9.4f);
        canvas.drawArc(rect, 270, 180, false, paint);

        paint.setStyle(Paint.Style.FILL);
        rect.set(10.6f, 13f, 13.4f, 17.4f);
        canvas.drawRect(rect, paint);
        rect.set(7.2f, 17.4f, 16.8f, 19.4f);
        canvas.drawRoundRect(rect, 0.8f, 0.8f, paint);
        rect.set(6.2f, 19.4f, 17.8f, 21.6f);
        canvas.drawRoundRect(rect, 0.8f, 0.8f, paint);
    }

    private void village(Canvas canvas) {
        paint.setColor(color);
        path.reset();
        path.moveTo(12f, 2.2f);
        path.lineTo(22f, 11f);
        path.lineTo(19.6f, 11f);
        path.lineTo(19.6f, 21f);
        path.lineTo(4.4f, 21f);
        path.lineTo(4.4f, 11f);
        path.lineTo(2f, 11f);
        path.close();
        canvas.drawPath(path, paint);

        paint.setColor(0xFF202730);
        rect.set(10f, 14.4f, 14f, 21f);
        canvas.drawRect(rect, paint);
        rect.set(6.2f, 13.2f, 8.9f, 16f);
        canvas.drawRect(rect, paint);
        rect.set(15.1f, 13.2f, 17.8f, 16f);
        canvas.drawRect(rect, paint);
    }

    private void swords(Canvas canvas) {
        canvas.save();
        canvas.rotate(45f, 12f, 12f);
        sword(canvas);
        canvas.restore();
        canvas.save();
        canvas.rotate(-45f, 12f, 12f);
        sword(canvas);
        canvas.restore();
    }

    private void sword(Canvas canvas) {
        paint.setColor(color);
        path.reset();
        path.moveTo(11.1f, 3.6f);
        path.lineTo(12.9f, 3.6f);
        path.lineTo(12.9f, 15f);
        path.lineTo(11.1f, 15f);
        path.close();
        canvas.drawPath(path, paint);
        path.reset();
        path.moveTo(11.1f, 3.6f);
        path.lineTo(12f, 0.8f);
        path.lineTo(12.9f, 3.6f);
        path.close();
        canvas.drawPath(path, paint);
        rect.set(8.4f, 14.6f, 15.6f, 16.4f);
        canvas.drawRoundRect(rect, 0.6f, 0.6f, paint);
        rect.set(11.2f, 16.4f, 12.8f, 20.4f);
        canvas.drawRect(rect, paint);
        canvas.drawCircle(12f, 21.2f, 1.3f, paint);
    }

    private void shield(Canvas canvas) {
        paint.setColor(color);
        path.reset();
        path.moveTo(12f, 1.8f);
        path.lineTo(21f, 4.8f);
        path.lineTo(21f, 12f);
        path.cubicTo(21f, 18f, 17f, 21.5f, 12f, 23f);
        path.cubicTo(7f, 21.5f, 3f, 18f, 3f, 12f);
        path.lineTo(3f, 4.8f);
        path.close();
        canvas.drawPath(path, paint);

        paint.setColor(0xFF202730);
        star(canvas, 12f, 11.4f, 5.4f, 2.3f);
    }

    private void hammer(Canvas canvas) {
        canvas.save();
        canvas.rotate(-32f, 12f, 12f);
        paint.setColor(color);
        rect.set(11f, 7.4f, 13f, 21.5f);
        canvas.drawRoundRect(rect, 0.8f, 0.8f, paint);
        paint.setColor(Skin.withAlpha(color, 235));
        rect.set(5.6f, 3.2f, 18.4f, 8.6f);
        canvas.drawRoundRect(rect, 1.4f, 1.4f, paint);
        paint.setColor(0xFF202730);
        rect.set(7.4f, 5f, 9.6f, 6.8f);
        canvas.drawRoundRect(rect, 0.4f, 0.4f, paint);
        rect.set(14.4f, 5f, 16.6f, 6.8f);
        canvas.drawRoundRect(rect, 0.4f, 0.4f, paint);
        canvas.restore();
    }

    private void gear(Canvas canvas) {
        paint.setColor(color);
        for (int i = 0; i < 8; i++) {
            canvas.save();
            canvas.rotate(i * 45f, 12f, 12f);
            rect.set(10.4f, 1.4f, 13.6f, 5.6f);
            canvas.drawRoundRect(rect, 0.7f, 0.7f, paint);
            canvas.restore();
        }
        paint.setColor(color);
        canvas.drawCircle(12f, 12f, 7.4f, paint);
        paint.setColor(0xFF202730);
        canvas.drawCircle(12f, 12f, 3.1f, paint);
    }

    private void gem(Canvas canvas) {
        paint.setColor(color);
        path.reset();
        path.moveTo(12f, 1.6f);
        path.lineTo(20.4f, 8.6f);
        path.lineTo(12f, 22.4f);
        path.lineTo(3.6f, 8.6f);
        path.close();
        canvas.drawPath(path, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0.9f);
        paint.setColor(0xFF202730);
        path.reset();
        path.moveTo(3.6f, 8.6f);
        path.lineTo(20.4f, 8.6f);
        path.moveTo(12f, 1.6f);
        path.lineTo(8.4f, 8.6f);
        path.lineTo(12f, 22.4f);
        path.lineTo(15.6f, 8.6f);
        path.lineTo(12f, 1.6f);
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void elixir(Canvas canvas) {
        paint.setColor(color);
        path.reset();
        path.moveTo(12f, 1.8f);
        path.cubicTo(12f, 1.8f, 4.6f, 10.2f, 4.6f, 15.2f);
        path.cubicTo(4.6f, 19.4f, 7.9f, 22.4f, 12f, 22.4f);
        path.cubicTo(16.1f, 22.4f, 19.4f, 19.4f, 19.4f, 15.2f);
        path.cubicTo(19.4f, 10.2f, 12f, 1.8f, 12f, 1.8f);
        path.close();
        canvas.drawPath(path, paint);
        paint.setColor(Skin.withAlpha(Color.WHITE, 90));
        canvas.drawCircle(9.4f, 16.2f, 1.9f, paint);
    }

    private void clock(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(color);
        canvas.drawCircle(12f, 12f, 8.6f, paint);
        path.reset();
        path.moveTo(12f, 6.6f);
        path.lineTo(12f, 12.4f);
        path.lineTo(16f, 14.6f);
        canvas.drawPath(path, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void tag(Canvas canvas) {
        paint.setColor(color);
        path.reset();
        path.moveTo(2.4f, 11.2f);
        path.lineTo(11.2f, 2.4f);
        path.lineTo(21.6f, 2.4f);
        path.lineTo(21.6f, 12.8f);
        path.lineTo(12.8f, 21.6f);
        path.close();
        canvas.drawPath(path, paint);
        paint.setColor(0xFF202730);
        canvas.drawCircle(17.2f, 6.8f, 2.1f, paint);
    }

    private void check(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.6f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(color);
        path.reset();
        path.moveTo(4f, 12.6f);
        path.lineTo(9.6f, 18.2f);
        path.lineTo(20f, 5.8f);
        canvas.drawPath(path, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void coin(Canvas canvas) {
        paint.setColor(color);
        canvas.drawCircle(12f, 12f, 9.6f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.4f);
        paint.setColor(0xFF202730);
        canvas.drawCircle(12f, 12f, 6.6f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF202730);
        star(canvas, 12f, 12f, 4.4f, 1.9f);
    }

    private void bolt(Canvas canvas) {
        paint.setColor(color);
        path.reset();
        path.moveTo(13.6f, 1.6f);
        path.lineTo(4.4f, 13.4f);
        path.lineTo(10.4f, 13.4f);
        path.lineTo(8.8f, 22.4f);
        path.lineTo(19.6f, 10f);
        path.lineTo(13.2f, 10f);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void star(Canvas canvas, float cx, float cy, float outer, float inner) {
        path.reset();
        for (int i = 0; i < 10; i++) {
            double angle = Math.toRadians(-90 + i * 36);
            float r = (i % 2 == 0) ? outer : inner;
            float x = (float) (cx + Math.cos(angle) * r);
            float y = (float) (cy + Math.sin(angle) * r);
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.close();
        canvas.drawPath(path, paint);
    }
}
