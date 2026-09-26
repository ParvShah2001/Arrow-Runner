package com.main.arrowrunner;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * High-Resolution Native Vector Skin Renderer.
 * Renders distinct 3D vector ships, hazards, points, and coins with zero resolution loss,
 * anti-aliased native Paint calls, and 100% uniform entity bounding box sizing.
 */
public class SkinRenderer {

    private static final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Path path = new Path();

    static {
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint.setStyle(Paint.Style.STROKE);
        innerPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Render Arrow Player Ship depending on equipped Skin ID.
     */
    public static void drawPlayerShip(Canvas canvas, float x, float y, float radius, SkinManager.SkinItem skin, boolean isLightMode, boolean hasShield, boolean hasMagnet) {
        canvas.save();
        canvas.translate(x, y);

        String id = skin.id;
        int primaryColor = skin.color;
        int coreColor = skin.coreColor;
        int bodyColor = isLightMode ? Color.parseColor("#0F172A") : Color.parseColor("#1E293B");

        // Magnet Aura
        if (hasMagnet) {
            strokePaint.setColor(0xFF8B5CF6);
            strokePaint.setStrokeWidth(3f);
            canvas.drawCircle(0, 0, 260f, strokePaint);
        }

        // Shield Aura
        if (hasShield) {
            strokePaint.setColor(0xFF0288D1);
            strokePaint.setStrokeWidth(6f);
            canvas.drawCircle(0, 0, radius + 14f, strokePaint);
        }

        strokePaint.setColor(primaryColor);
        strokePaint.setStrokeWidth(5f);
        fillPaint.setColor(bodyColor);

        path.reset();

        if ("arrow_1".equals(id)) { // Gold Vector Diamond Vessel
            path.moveTo(0, -radius - 8f);
            path.lineTo(radius + 6f, 0);
            path.lineTo(0, radius + 6f);
            path.lineTo(-radius - 6f, 0);
            path.close();
        } 
        else if ("arrow_2".equals(id)) { // Cyber Razor Blade
            path.moveTo(0, -radius - 6f);
            path.lineTo(radius + 8f, radius + 10f);
            path.lineTo(0, radius - 6f);
            path.lineTo(-radius - 8f, radius + 10f);
            path.close();
        }
        else if ("arrow_3".equals(id)) { // Quantum Pulsar Quad Wing
            path.moveTo(0, -radius - 5f);
            path.lineTo(12f, -4f);
            path.lineTo(radius + 8f, radius + 4f);
            path.lineTo(0, radius);
            path.lineTo(-radius - 8f, radius + 4f);
            path.lineTo(-12f, -4f);
            path.close();
        }
        else if ("arrow_4".equals(id)) { // Toxic Viper Trident
            path.moveTo(0, -radius - 10f);
            path.lineTo(10f, -2f);
            path.lineTo(radius + 6f, radius + 8f);
            path.lineTo(4f, radius + 2f);
            path.lineTo(0, radius - 4f);
            path.lineTo(-4f, radius + 2f);
            path.lineTo(-radius - 6f, radius + 8f);
            path.lineTo(-10f, -2f);
            path.close();
        }
        else if ("arrow_8".equals(id)) { // Matrix Hex Drone
            for (int i = 0; i < 6; i++) {
                double angle = Math.toRadians(i * 60);
                float px = (float) (Math.cos(angle) * (radius + 4f));
                float py = (float) (Math.sin(angle) * (radius + 4f));
                if (i == 0) path.moveTo(px, py);
                else path.lineTo(px, py);
            }
            path.close();
        }
        else { // Classic Dual-Wing Jet
            path.moveTo(0, -radius - 4f);
            path.lineTo(radius + 4f, radius + 6f);
            path.lineTo(0, radius - 2f);
            path.lineTo(-radius - 4f, radius + 6f);
            path.close();
        }

        canvas.drawPath(path, fillPaint);
        canvas.drawPath(path, strokePaint);

        // Cockpit Core Accent
        innerPaint.setColor(coreColor);
        canvas.drawCircle(0, 2f, 7f, innerPaint);

        canvas.restore();
    }

    /**
     * Render Hazard / Bomb depending on equipped Skin ID. UNIFORM SIZE = 48dp!
     */
    public static void drawHazard(Canvas canvas, float x, float y, float radius, float rotation, SkinManager.SkinItem skin) {
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate(rotation * 1.5f);

        int color = skin.color != 0 ? skin.color : 0xFFEF4444;
        fillPaint.setColor(color);
        strokePaint.setColor(Color.WHITE);
        strokePaint.setStrokeWidth(3f);

        path.reset();

        if ("bomb_5".equals(skin.id)) { // 12-Tooth Rotating Magma Sawblade
            int teeth = 12;
            for (int i = 0; i < teeth; i++) {
                double a1 = Math.toRadians((i * 360.0) / teeth);
                float x1 = (float) (Math.cos(a1) * radius);
                float y1 = (float) (Math.sin(a1) * radius);
                if (i == 0) path.moveTo(x1, y1);
                else path.lineTo(x1, y1);

                double a2 = Math.toRadians(((i + 0.5) * 360.0) / teeth);
                float x2 = (float) (Math.cos(a2) * (radius * 0.6f));
                float y2 = (float) (Math.sin(a2) * (radius * 0.6f));
                path.lineTo(x2, y2);
            }
            path.close();
            canvas.drawPath(path, fillPaint);
            canvas.drawPath(path, strokePaint);
        }
        else if ("bomb_7".equals(skin.id)) { // 3D Isometric Matrix Box
            RectF rect = new RectF(-radius + 4f, -radius + 4f, radius - 4f, radius - 4f);
            canvas.drawRoundRect(rect, 6f, 6f, fillPaint);
            canvas.drawRoundRect(rect, 6f, 6f, strokePaint);
        }
        else { // High-Contrast Danger Spike Mine
            int spikes = 8;
            for (int i = 0; i < spikes; i++) {
                double angle1 = Math.toRadians((i * 360.0) / spikes);
                float x1 = (float) (Math.cos(angle1) * radius);
                float y1 = (float) (Math.sin(angle1) * radius);
                if (i == 0) path.moveTo(x1, y1);
                else path.lineTo(x1, y1);

                double angle2 = Math.toRadians(((i + 0.5) * 360.0) / spikes);
                float x2 = (float) (Math.cos(angle2) * (radius * 0.5f));
                float y2 = (float) (Math.sin(angle2) * (radius * 0.5f));
                path.lineTo(x2, y2);
            }
            path.close();
            canvas.drawPath(path, fillPaint);
            canvas.drawPath(path, strokePaint);

            innerPaint.setColor(Color.WHITE);
            canvas.drawCircle(0, 0, radius * 0.25f, innerPaint);
        }

        canvas.restore();
    }

    /**
     * Render Points / Orb depending on equipped Skin ID. UNIFORM SIZE = 48dp!
     */
    public static void drawPoint(Canvas canvas, float x, float y, float radius, SkinManager.SkinItem skin) {
        canvas.save();
        canvas.translate(x, y);

        int color = skin.color != 0 ? skin.color : 0xFF10B981;
        fillPaint.setColor(color);
        strokePaint.setColor(Color.WHITE);
        strokePaint.setStrokeWidth(3f);

        path.reset();

        if ("orb_1".equals(skin.id)) { // 4-Point Cyan Lightning Spark
            for (int i = 0; i < 4; i++) {
                double a1 = Math.toRadians(i * 90);
                float x1 = (float) (Math.cos(a1) * radius);
                float y1 = (float) (Math.sin(a1) * radius);
                if (i == 0) path.moveTo(x1, y1);
                else path.lineTo(x1, y1);

                double a2 = Math.toRadians(i * 90 + 45);
                float x2 = (float) (Math.cos(a2) * (radius * 0.4f));
                float y2 = (float) (Math.sin(a2) * (radius * 0.4f));
                path.lineTo(x2, y2);
            }
            path.close();
            canvas.drawPath(path, fillPaint);
            canvas.drawPath(path, strokePaint);
        }
        else if ("orb_2".equals(skin.id)) { // 5-Point Shimmering Yellow Star
            for (int i = 0; i < 5; i++) {
                double a1 = Math.toRadians(18 + i * 72);
                float x1 = (float) (Math.cos(a1) * radius);
                float y1 = (float) (Math.sin(a1) * radius);
                if (i == 0) path.moveTo(x1, y1);
                else path.lineTo(x1, y1);

                double a2 = Math.toRadians(54 + i * 72);
                float x2 = (float) (Math.cos(a2) * (radius * 0.5f));
                float y2 = (float) (Math.sin(a2) * (radius * 0.5f));
                path.lineTo(x2, y2);
            }
            path.close();
            canvas.drawPath(path, fillPaint);
            canvas.drawPath(path, strokePaint);
        }
        else if ("orb_8".equals(skin.id)) { // 3D Cut Diamond Score Crystal
            path.moveTo(0, -radius);
            path.lineTo(radius, 0);
            path.lineTo(0, radius);
            path.lineTo(-radius, 0);
            path.close();
            canvas.drawPath(path, fillPaint);
            canvas.drawPath(path, strokePaint);
        }
        else { // Classic Emerald Pearl Sphere
            canvas.drawCircle(0, 0, radius, fillPaint);
            canvas.drawCircle(0, 0, radius, strokePaint);

            innerPaint.setColor(Color.WHITE);
            canvas.drawCircle(0, 0, radius * 0.4f, innerPaint);
        }

        canvas.restore();
    }

    /**
     * Render Coin depending on equipped Skin ID. UNIFORM SIZE = 48dp!
     */
    public static void drawCoin(Canvas canvas, float x, float y, float radius, SkinManager.SkinItem skin) {
        canvas.save();
        canvas.translate(x, y);

        int color = skin.color != 0 ? skin.color : 0xFFF59E0B;
        fillPaint.setColor(color);
        strokePaint.setColor(Color.WHITE);
        strokePaint.setStrokeWidth(3f);

        if ("coin_1".equals(skin.id)) { // Rectangular Hologram Credit Card
            RectF rect = new RectF(-radius + 2f, -12f, radius - 2f, 12f);
            canvas.drawRoundRect(rect, 4f, 4f, fillPaint);
            canvas.drawRoundRect(rect, 4f, 4f, strokePaint);
        }
        else { // High-Resolution Vector Gold Medal Disc
            canvas.drawCircle(0, 0, radius - 2f, fillPaint);
            canvas.drawCircle(0, 0, radius - 2f, strokePaint);

            innerPaint.setColor(Color.WHITE);
            canvas.drawCircle(0, 0, radius * 0.5f, innerPaint);
        }

        canvas.restore();
    }
}
