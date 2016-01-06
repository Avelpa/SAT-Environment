/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.model.Polygon;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitry
 */
public class World {
    
    private ArrayList<Polygon> polygons;
    boolean mousePressed = false;
    Vector2 lastMousePos = null;
    Vector2 mousePos = null;
    Polygon movedPoly = null;
    
    private float frictionNormal   = 0.95f;
    private float frictionContact = 0.4f;
    
    public ArrayList<Vector2> potentialPolygon = new ArrayList();
    
    Robot robot;
    
    public World()
    {
        polygons = new ArrayList();
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void update()
    {
        System.out.println(potentialPolygon.size());
        float speed = 5;
//        if (Gdx.input.isKeyPressed(Input.Keys.S))
//        {
//            polygons.get(1).move(0, -speed);
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.A))
//        {
//            polygons.get(1).move(-speed, 0);
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.W))
//        {
//            polygons.get(1).move(0, speed);
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.D))
//        {
//            polygons.get(1).move(speed, 0);
//        }
        if (Gdx.input.isButtonPressed(0))
        {
            mousePos = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY());
            if (mousePressed)
            {
                Vector2 movement = mousePos.cpy().sub(lastMousePos);
                lastMousePos = mousePos;

                movedPoly = collidePoint(mousePos);
                if (movedPoly != null)
                    movedPoly.setVelocity(movement.x, movement.y);
            }
            else
            {
                mousePressed = true;
                lastMousePos = mousePos;
            }      
        }
        else if (Gdx.input.isButtonPressed(1))
        {
            if (!mousePressed)
            {
                mousePos = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY());
                potentialPolygon.add(mousePos);
                mousePressed = true;
            }
        }
        else
        {
            mousePressed = false;
            movedPoly = null;
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
        {
            if (potentialPolygon.size() >= 3)
            {
                polygons.add(new Polygon(potentialPolygon.toArray(new Vector2[potentialPolygon.size()]), frictionNormal));
                potentialPolygon.clear();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R))
        {
            if (polygons.size() > 0)
            {
                polygons.remove(polygons.size()-1);
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.BACKSPACE))
        {
            for (Polygon polygon: polygons)
            {
                polygon.goHome(1f/60);
            }
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.S))
        {
            for (Polygon polygon: polygons)
            {
                polygon.updateHome();
            }
        }
        
        for (Polygon polygon: polygons)
        {
            polygon.move();
        }
    }
    
    public Polygon collidePoint(Vector2 point)
    {
        if (Gdx.input.isTouched() && movedPoly != null)
            return movedPoly;
        Polygon collidedPolygon = null;
        
        Vector2[] normals;
        
        Vector2 proj;
        float dotProj;
        
        boolean collided;
        
        for (int i = polygons.size()-1; i >= 0; i --)
        {
            collided = true;
            
            normals = polygons.get(i).getNormals();
            for (Vector2 normal: normals)
            {
                proj = polygons.get(i).project(normal);
                dotProj = Polygon.projectVector(point, normal);
                
                if (proj.x > dotProj || proj.y < dotProj)
                {
                    collided = false;
                    break;
                }
            }
            if (collided)
            {
                return polygons.get(i);
            }
        }
        return null;
    }
    
    public ArrayList<Polygon> collidePolygons()
    {
        ArrayList<Polygon> collidedPolygons = new ArrayList();
        
        float minIntersect = Float.MAX_VALUE;
        Vector2 collidingAxis = null;
        
        Vector2[] normals1;
        Vector2[] normals2;
        
        Vector2 proj1;
        Vector2 proj2;
        
        boolean collidedFirst;
        boolean collidedSecond;
        
        if (polygons.size() > 0)
            polygons.get(0).setFriction(frictionNormal);
        for (int i = 0; i < polygons.size()-1; i ++)
        {
            collidedFirst = false;
            
            normals1 = polygons.get(i).getNormals();
            for (int j = i+1; j < polygons.size(); j ++)
            {
                collidedSecond = true;
                
                normals2 = polygons.get(j).getNormals();
                
                for (Vector2 normal: normals1)
                {
                    proj1 = polygons.get(i).project(normal);
                    proj2 = polygons.get(j).project(normal);
                    
                    // no collision
                    if (proj1.x > proj2.y || proj1.y < proj2.x)
                    {
                        collidedSecond = false;
                        if (!collidedPolygons.contains(polygons.get(j)))
                            polygons.get(j).setFriction(frictionNormal);
                        break;
                    }
                    else
                    {
//                        System.out.println(proj2.y-proj1.x);
                        if (Math.abs(proj2.y-proj1.x) < Math.abs(minIntersect))
                        {
                            minIntersect = proj2.y-proj1.x;
                            collidingAxis = normal;
                        }
                    }
                }
//                if (!collidedSecond)
//                    continue;
                
                for (Vector2 normal: normals2)
                {
                    proj1 = polygons.get(i).project(normal);
                    proj2 = polygons.get(j).project(normal);
                    
                    if (proj1.x > proj2.y || proj1.y < proj2.x)
                    {
                        collidedSecond = false;
                        if (!collidedPolygons.contains(polygons.get(j)))
                            polygons.get(j).setFriction(frictionNormal);
                        break;
                    }
                    else
                    {
//                        System.out.println(proj2.y-proj1.x);
                        if (Math.abs(proj2.y-proj1.x) < Math.abs(minIntersect))
                        {
                            minIntersect = proj2.y-proj1.x;
                            collidingAxis = normal;
                        }
                    }
                }
                
                if (collidedSecond)
                {
                    collidedFirst = true;
                    if (!collidedPolygons.contains(polygons.get(j)))
                    {
                        collidedPolygons.add(polygons.get(j));
                        polygons.get(j).setFriction(frictionContact);
                    }
                }
            }
            if (collidedFirst)
            {
                if (!collidedPolygons.contains(polygons.get(i)))
                {
                    collidedPolygons.add(polygons.get(i));
                    polygons.get(i).setFriction(frictionContact);
                }
            }
        }
        if (collidedPolygons.size() > 0)
        {
            System.out.println(minIntersect);
            System.out.println(collidingAxis);
            
//            collidedPolygons.get(1).setVelocity(-100, 0);
            collidedPolygons.get(1).setVelocity(collidingAxis.cpy().scl(1f/collidingAxis.len()).cpy().scl(minIntersect/2));
            collidedPolygons.get(1).move();
            
            collidedPolygons.get(0).setVelocity(collidingAxis.cpy().scl(1f/collidingAxis.len()).cpy().scl(-minIntersect/2));
            collidedPolygons.get(0).move();
            
//            robot.mouseMove(Gdx.input.getX()-(int)collidingAxis.cpy().scl(1f/collidingAxis.len()).cpy().scl(-minIntersect/2).x, Gdx.input.getY()-(int)collidingAxis.cpy().scl(1f/collidingAxis.len()).cpy().scl(-minIntersect/2).y);
//            robot.mouseMove(Gdx.input.getX(), Gdx.input.getY());
//            collidedPolygons.get(1).bump(collidingAxis.cpy().scl(1f/collidingAxis.len()).cpy().scl(minIntersect));
            
//            robot.mouseMove(MouseInfo.getPointerInfo().getLocation().x-(int)collidingAxis.cpy().scl(1f/collidingAxis.len()).cpy().scl(-minIntersect).x, MouseInfo.getPointerInfo().getLocation().y-(int)collidingAxis.cpy().scl(1f/collidingAxis.len()).cpy().scl(-minIntersect).y);
        }
        if (minIntersect == 0)
        {
            collidedPolygons.clear();
        }
//        System.out.println(MouseInfo.getPointerInfo().getLocation());
        
        return collidedPolygons;
    }
    
    public ArrayList<Polygon> getPolygons()
    {
        return polygons;
    }
    
}
