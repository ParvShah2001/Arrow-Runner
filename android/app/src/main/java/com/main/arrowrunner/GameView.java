package com.main.arrowrunner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Ultra High-Performance 120 FPS Hardware Accelerated SurfaceView Game View.
 * Renders native vector graphics with sub-millisecond frame timing and zero Webview latency.
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    public interface GameStateListener {
        void onScoreChanged(int score, int coins, int combo, int lives);
        void onGameOver(int finalScore, int coinsEarned, int maxCombo, boolean isNewHigh);
    }

    public static class Entity {
        public float x, y;
        public int lane;
        public String type; // "orb", "hazard", "coin", "mega", "megacoin", "shield", "magnet"
        public float radius;
        public float rotation;

        public Entity(float x, float y, int lane, String type, float radius) {
            this.x = x;
            this.y = y;
            this.lane = lane;
            this.type = type;
            this.radius = radius;
            this.rotation = 0;
        }
    }

    public static class Particle {
        public float x, y, vx, vy, size;
        public int color;
        public int life, maxLife;

        public Particle(float x, float y, float vx, float vy, int color, float size, int maxLife) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.size = size;
            this.maxLife = maxLife;
            this.life = maxLife;
        }
    }

    public static class FloatingText {
        public float x, y;
        public String text;
        public int color;
        public int life;

        public FloatingText(float x, float y, String text, int color) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.color = color;
            this.life = 35;
        }
    }

    private Thread gameThread;
    private volatile boolean isPlaying;
    private final SurfaceHolder surfaceHolder;

    private final SkinManager skinManager;
    private final SoundManager soundManager;
    private GameStateListener stateListener;

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float canvasWidth = 1080f;
    private float canvasHeight = 1920f;
    private float[] laneX = new float[]{200f, 540f, 880f};

    // Game Variables
    private int currentLane = 1;
    private float playerX = 540f;
    private float targetPlayerX = 540f;
    private float playerY = 1600f;
    private float playerRadius = 45f;

    private boolean hasShield = false;
    private boolean hasMagnet = false;
    private int magnetTimer = 0;

    private int score = 0;
    private int runCoins = 0;
    private int lives = 3;
    private int comboCount = 0;
    private int comboMultiplier = 1;
    private int maxCombo = 1;

    private float baseSpeed = 12f;
    private float currentSpeed = 12f;
    private int spawnInterval = 50;
    private int frameCounter = 0;

    private final List<Entity> entities = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private final Random random = new Random();

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        skinManager = new SkinManager(context);
        soundManager = new SoundManager(context, skinManager);

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(3f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(40f);
        textPaint.setFakeBoldText(true);
    }

    public void setGameStateListener(GameStateListener listener) {
        this.stateListener = listener;
    }

    public SkinManager getSkinManager() {
        return skinManager;
    }

    public void startNewGame() {
        score = 0;
        runCoins = 0;
        lives = 3;
        comboCount = 0;
        comboMultiplier = 1;
        maxCombo = 1;
        currentSpeed = baseSpeed;
        spawnInterval = 50;
        frameCounter = 0;

        hasShield = false;
        hasMagnet = false;
        magnetTimer = 0;

        currentLane = 1;
        playerX = laneX[1];
        targetPlayerX = laneX[1];

        entities.clear();
        particles.clear();
        floatingTexts.clear();

        if (stateListener != null) {
            stateListener.onScoreChanged(score, runCoins, comboMultiplier, lives);
        }

        isPlaying = true;
    }

    public void pauseGame() {
        isPlaying = false;
    }

    public void resumeGame() {
        if (!isPlaying && surfaceHolder.getSurface().isValid()) {
            isPlaying = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        canvasWidth = getWidth();
        canvasHeight = getHeight();
        laneX = new float[]{ canvasWidth * 0.2f, canvasWidth * 0.5f, canvasWidth * 0.8f };
        playerY = canvasHeight - (canvasHeight * 0.15f);
        playerX = laneX[1];
        targetPlayerX = laneX[1];

        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            try {
                drawGame(canvas);
            } catch (Exception ignored) {
            } finally {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
        laneX = new float[]{ canvasWidth * 0.2f, canvasWidth * 0.5f, canvasWidth * 0.8f };
        playerY = canvasHeight - (canvasHeight * 0.15f);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pauseGame();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();

        while (isPlaying) {
            if (!surfaceHolder.getSurface().isValid()) {
                try { Thread.sleep(10); } catch (Exception ignored) {}
                continue;
            }

            try {
                long now = System.nanoTime();
                float dt = (now - lastTime) / 1000000000f;
                if (dt > 0.05f) dt = 0.05f;
                lastTime = now;

                updateGame(dt);

                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    try {
                        drawGame(canvas);
                    } finally {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private void updateGame(float dt) {
        // Player Lerp Movement
        playerX += (targetPlayerX - playerX) * Math.min(1f, 16f * dt);

        if (hasMagnet) {
            magnetTimer--;
            if (magnetTimer <= 0) hasMagnet = false;
        }

        // Spawning
        frameCounter++;
        if (frameCounter >= spawnInterval) {
            frameCounter = 0;
            spawnEntity();
        }

        // Update Entities
        Iterator<Entity> entityIterator = entities.iterator();
        while (entityIterator.hasNext()) {
            Entity ent = entityIterator.next();
            ent.y += currentSpeed * dt * 60f;
            ent.rotation += 2f;

            // Magnet Attraction
            if (hasMagnet && !"hazard".equals(ent.type)) {
                float dx = playerX - ent.x;
                float dy = playerY - ent.y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < 320f) {
                    ent.x += (dx / dist) * 16f;
                    ent.y += (dy / dist) * 16f;
                }
            }

            // Collision Check
            float dx = playerX - ent.x;
            float dy = playerY - ent.y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist < playerRadius + ent.radius) {
                if ("orb".equals(ent.type)) {
                    comboCount++;
                    comboMultiplier = Math.min(5, (comboCount / 5) + 1);
                    if (comboMultiplier > maxCombo) maxCombo = comboMultiplier;
                    score += 10 * comboMultiplier;

                    soundManager.playCollect();
                    createExplosion(ent.x, ent.y, 0xFF10B981, 14);
                    floatingTexts.add(new FloatingText(ent.x, ent.y, "+" + (10 * comboMultiplier), 0xFF10B981));
                }
                else if ("mega".equals(ent.type)) {
                    score += 50 * comboMultiplier;
                    soundManager.playCollect();
                    createExplosion(ent.x, ent.y, 0xFFF59E0B, 20);
                    floatingTexts.add(new FloatingText(ent.x, ent.y, "MEGA +" + (50 * comboMultiplier), 0xFFF59E0B));
                }
                else if ("coin".equals(ent.type)) {
                    runCoins += 1;
                    score += 5;
                    skinManager.addCoins(1);

                    soundManager.playCoin();
                    createExplosion(ent.x, ent.y, 0xFFF59E0B, 14);
                    floatingTexts.add(new FloatingText(ent.x, ent.y, "+1 🪙", 0xFFF59E0B));
                }
                else if ("megacoin".equals(ent.type)) {
                    runCoins += 5;
                    score += 25;
                    skinManager.addCoins(5);

                    soundManager.playCoin();
                    createExplosion(ent.x, ent.y, 0xFFF59E0B, 20);
                    floatingTexts.add(new FloatingText(ent.x, ent.y, "+5 🪙 BAG!", 0xFFF59E0B));
                }
                else if ("shield".equals(ent.type)) {
                    hasShield = true;
                    soundManager.playCollect();
                    createExplosion(ent.x, ent.y, 0xFF0288D1, 18);
                    floatingTexts.add(new FloatingText(ent.x, ent.y, "SHIELD ACTIVE!", 0xFF0288D1));
                }
                else if ("magnet".equals(ent.type)) {
                    hasMagnet = true;
                    magnetTimer = 660;
                    soundManager.playCollect();
                    createExplosion(ent.x, ent.y, 0xFF8B5CF6, 20);
                    floatingTexts.add(new FloatingText(ent.x, ent.y, "SUPER MAGNET!", 0xFF8B5CF6));
                }
                else if ("hazard".equals(ent.type)) {
                    if (hasShield) {
                        hasShield = false;
                        soundManager.playHit();
                        createExplosion(ent.x, ent.y, 0xFFEF4444, 20);
                        floatingTexts.add(new FloatingText(playerX, playerY - 30f, "SHIELD BROKEN!", 0xFFEF4444));
                    } else {
                        lives--;
                        comboCount = 0;
                        comboMultiplier = 1;
                        soundManager.playHit();
                        createExplosion(playerX, playerY, 0xFFEF4444, 26);
                        floatingTexts.add(new FloatingText(playerX, playerY - 30f, "DAMAGE! -1 LIFE", 0xFFEF4444));

                        if (lives <= 0) {
                            isPlaying = false;
                            boolean isNewHigh = skinManager.checkAndUpdateHighScore(score);
                            soundManager.playGameOver();
                            if (stateListener != null) {
                                post(() -> stateListener.onGameOver(score, runCoins, maxCombo, isNewHigh));
                            }
                            return;
                        }
                    }
                }

                if (stateListener != null) {
                    post(() -> stateListener.onScoreChanged(score, runCoins, comboMultiplier, lives));
                }

                entityIterator.remove();
                continue;
            }

            if (ent.y > canvasHeight + 60f) {
                if ("orb".equals(ent.type) || "mega".equals(ent.type)) {
                    comboCount = 0;
                    comboMultiplier = 1;
                    if (stateListener != null) {
                        post(() -> stateListener.onScoreChanged(score, runCoins, comboMultiplier, lives));
                    }
                }
                entityIterator.remove();
            }
        }

        // Particles
        if (particles.size() > 80) particles.subList(0, particles.size() - 80).clear();
        Iterator<Particle> particleIterator = particles.iterator();
        while (particleIterator.hasNext()) {
            Particle p = particleIterator.next();
            p.x += p.vx;
            p.y += p.vy;
            p.life--;
            if (p.life <= 0) particleIterator.remove();
        }

        // Floating Texts
        Iterator<FloatingText> textIterator = floatingTexts.iterator();
        while (textIterator.hasNext()) {
            FloatingText ft = textIterator.next();
            ft.y -= 2f;
            ft.life--;
            if (ft.life <= 0) textIterator.remove();
        }

        // Difficulty Progression
        currentSpeed = baseSpeed + (score / 80) * 0.4f;
        if (currentSpeed > 22f) currentSpeed = 22f;
        spawnInterval = Math.max(25, 50 - (score / 100) * 3);
    }

    private void spawnEntity() {
        int lane = random.nextInt(3);
        float rand = random.nextFloat();
        String type = "orb";
        float radius = 48f; // UNIFORM BOUNDING SIZE!

        if (rand < 0.45f) type = "orb";
        else if (rand < 0.72f) type = "hazard";
        else if (rand < 0.85f) type = "coin";
        else if (rand < 0.91f) { type = "mega"; radius = 58f; }
        else if (rand < 0.95f) { type = "megacoin"; radius = 58f; }
        else if (rand < 0.98f) type = "shield";
        else type = "magnet";

        entities.add(new Entity(laneX[lane], -60f, lane, type, radius));
    }

    private void createExplosion(float x, float y, int color, int count) {
        for (int i = 0; i < count; i++) {
            float angle = random.nextFloat() * (float) Math.PI * 2f;
            float speed = random.nextFloat() * 6f + 2f;
            particles.add(new Particle(
                    x, y,
                    (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed,
                    color,
                    random.nextFloat() * 5f + 3f,
                    random.nextInt(15) + 15
            ));
        }
    }

    private void drawGame(Canvas canvas) {
        boolean isLight = "light".equals(skinManager.getThemeMode());

        // Background
        bgPaint.setColor(isLight ? 0xFFF8FAFC : 0xFF090D16);
        canvas.drawRect(0, 0, canvasWidth, canvasHeight, bgPaint);

        // Lane Dividers
        gridPaint.setColor(isLight ? 0x3094A3B8 : 0x20FFFFFF);
        canvas.drawLine(laneX[0] + (laneX[1] - laneX[0]) / 2f, 0, laneX[0] + (laneX[1] - laneX[0]) / 2f, canvasHeight, gridPaint);
        canvas.drawLine(laneX[1] + (laneX[2] - laneX[1]) / 2f, 0, laneX[1] + (laneX[2] - laneX[1]) / 2f, canvasHeight, gridPaint);

        // Render Entities with High-Resolution Vector Renderer
        SkinManager.SkinItem hazardSkin = skinManager.getSkin("hazard", skinManager.getEquippedSkin("hazard"));
        SkinManager.SkinItem pointSkin = skinManager.getSkin("point", skinManager.getEquippedSkin("point"));
        SkinManager.SkinItem coinSkin = skinManager.getSkin("coin", skinManager.getEquippedSkin("coin"));

        for (Entity ent : entities) {
            if ("orb".equals(ent.type) || "mega".equals(ent.type)) {
                SkinRenderer.drawPoint(canvas, ent.x, ent.y, ent.radius, pointSkin);
            } else if ("coin".equals(ent.type) || "megacoin".equals(ent.type)) {
                SkinRenderer.drawCoin(canvas, ent.x, ent.y, ent.radius, coinSkin);
            } else if ("hazard".equals(ent.type)) {
                SkinRenderer.drawHazard(canvas, ent.x, ent.y, ent.radius, ent.rotation, hazardSkin);
            } else if ("shield".equals(ent.type) || "magnet".equals(ent.type)) {
                SkinRenderer.drawPoint(canvas, ent.x, ent.y, ent.radius, pointSkin);
            }
        }

        // Render Player Arrow
        SkinManager.SkinItem arrowSkin = skinManager.getSkin("arrow", skinManager.getEquippedSkin("arrow"));
        SkinRenderer.drawPlayerShip(canvas, playerX, playerY, playerRadius, arrowSkin, isLight, hasShield, hasMagnet);

        // Particles
        for (Particle p : particles) {
            particlePaint.setColor(p.color);
            particlePaint.setAlpha((int) ((p.life / (float) p.maxLife) * 255));
            canvas.drawCircle(p.x, p.y, p.size, particlePaint);
        }

        // Floating Score Texts
        for (FloatingText ft : floatingTexts) {
            textPaint.setColor(ft.color);
            textPaint.setAlpha((int) ((ft.life / 35f) * 255));
            canvas.drawText(ft.text, ft.x, ft.y, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isPlaying) return super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            if (touchX < canvasWidth / 2f) {
                if (currentLane > 0) {
                    currentLane--;
                    targetPlayerX = laneX[currentLane];
                    soundManager.playMove();
                }
            } else {
                if (currentLane < 2) {
                    currentLane++;
                    targetPlayerX = laneX[currentLane];
                    soundManager.playMove();
                }
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
}
