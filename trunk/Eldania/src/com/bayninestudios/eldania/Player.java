package com.bayninestudios.eldania;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;

public class Player
{
    private DrawModel playerModel;
    public int facing = 40;
    public Vector2 position;
    public float x, y, z;
    public float dx, dy;
    public boolean inCombat = false;
    private float MOVESPEED = 1.3f; // in tiles per second
    public int maxHealth = 30;
    public int curHealth = 20;
    private DrawModel healthBar;
    public long healProcTimer = 0; // in milliseconds
    private long walkFrameTimer = 0;
    private int walkFrame = 1;
    private final int WALKFRAMESPEED = 250;
    private final int HEALPROCINTERVAL = 1000;

    public int actionTimer = 2000;
    public final int ACTIONINTERVAL = 2000;

    private boolean moving = false;

    private long lastUpdate;

    public Player(Context context)
    {
        playerModel = new DrawModel(context, R.xml.player);
        this.x = 49.5f;
        this.y = 8.5f;
        this.z = 0f;
        lastUpdate = System.currentTimeMillis();
        healthBar = new DrawModel(context, R.xml.tile);
    }

    public void loadTextures(GL10 gl, Context context)
    {
        playerModel.loadTexture(gl, context, R.drawable.player);
        playerModel.specialTex();
    }

    public void draw(GL10 gl)
    {
        // gl.glColor4f(.1f, .1f, 1f, 1f);
        // playerModel.draw(gl,0f,0f,0f,facing);
        int frameFacing = facing;
        if (walkFrame == 0)
        {
            frameFacing = frameFacing - 32;
        }
        else if (walkFrame == 2)
        {
            frameFacing = frameFacing + 32;
        }
        playerModel.specialDraw(gl, frameFacing);
    }

    // TODO need to remove dashboard code from character class
    public void drawDash(GL10 gl)
    {
        Vector3 barScale = new Vector3();
        barScale.x = 1f;
        barScale.y = 0.1f;
        barScale.z = 1f;
        gl.glColor4f(.2f, .2f, .2f, 1f);
        healthBar.draw(gl, -4.7f, 2.6f, 0f, 0f, barScale);

        barScale.x = ((float) curHealth) / maxHealth;
        barScale.y = 0.1f;
        barScale.z = 1f;
        gl.glColor4f(.8f, 0f, 0f, 1f);
        healthBar.draw(gl, -4.7f, 2.6f, 0.1f, 0f, barScale);

        if (inCombat)
        {
            barScale.x = 1f;
            barScale.y = 0.1f;
            barScale.z = 1f;
            gl.glColor4f(.2f, .2f, .2f, 1f);
            healthBar.draw(gl, -4.7f, 2.4f, 0f, 0f, barScale);

            barScale.x = ((float) actionTimer) / ACTIONINTERVAL;
            barScale.y = 0.1f;
            barScale.z = 1f;
            gl.glColor4f(1f, 1f, 1f, 1f);
            healthBar.draw(gl, -4.7f, 2.4f, 0.1f, 0f, barScale);
        }
    }

    public void setFacing()
    {
        if ((dx > 0) && (dy == 0))
            facing = 40;
        else if ((dx < 0) && (dy == 0))
            facing = 56;
        else if ((dx == 0) && (dy < 0))
            facing = 48;
        else if ((dx == 0) && (dy > 0))
            facing = 32;
        // TODO: I need to do something with these diagonal moves
        // else if ((charDX > 0) && (charDY > 0))
        // facing = 135f;
        // else if ((charDX > 0) && (charDY < 0))
        // facing = 45f;
        // else if ((charDX < 0) && (charDY > 0))
        // facing = -135f;
        // else if ((charDX < 0) && (charDY < 0))
        // facing = -45f;
    }

    public void moveCharacter(int keyCode, boolean keyUp)
    {
        if (!inCombat)
        {
            float newMove = 1f;
            if (keyUp)
                newMove = newMove * -1f;
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
            {
                dx = dx - newMove;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
            {
                dx = dx + newMove;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP)
            {
                dy = dy + newMove;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
            {
                dy = dy - newMove;
            }
            setFacing();
        }
        else
        {
            dx = 0;
            dy = 0;
        }
    }

    public void update(Landscape landscape)
    {
        long curTime = System.currentTimeMillis();
        long timeDif = curTime - lastUpdate;
        float frameRate = timeDif / 1000f;
        if ((dx == 0) && (dy == 0))
        {
            walkFrame = 1;
            walkFrameTimer = 0;
            moving = false;
        }
        else
        {
            if (moving == false)
            {
                moving = true;
                walkFrame = 0;
            }

            float moveSpeed = MOVESPEED * frameRate;
            // move character
            float newCharX = x + dx * moveSpeed;
            float newCharY = y + dy * moveSpeed;

            if (landscape.checkPassable(newCharX, y))
            {
                x = newCharX;
            }
            if (landscape.checkPassable(x, newCharY))
            {
                y = newCharY;
            }

            walkFrameTimer = walkFrameTimer + timeDif;
            if (walkFrameTimer > WALKFRAMESPEED)
            {
                walkFrameTimer = walkFrameTimer - WALKFRAMESPEED;
                walkFrame++;
                if (walkFrame > 3)
                    walkFrame = 0;
            }
        }

        if (!inCombat)
        {
            // heal if needed
            if (curHealth < maxHealth)
            {
                healProcTimer = healProcTimer + timeDif;
                if (healProcTimer > HEALPROCINTERVAL)
                {
                    curHealth++;
                    healProcTimer = healProcTimer - HEALPROCINTERVAL;
                }
            }
        }
        lastUpdate = curTime;
    }
}