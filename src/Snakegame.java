import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Snakegame extends JPanel implements ActionListener,KeyListener{
    private class Tile{
        int x;
        int y;
        Tile(int x,int y){
            this.x=x;
            this.y=y;

        }


    }
    int boardwidth;
    int boardheight;
    int tilesize=25;
    //food
    Tile food;
    //snake
    Tile snakehead;
    ArrayList<Tile> snakebody;


    Random random;
    //gamelogic
    Timer gameloop;
    int velocityX;
    int velocityY;
    boolean gameover=false;



    Snakegame(int boardwidth,int boardheight){
        this.boardwidth=boardwidth;
        this.boardheight=boardheight;
        setPreferredSize(new Dimension(this.boardwidth,this.boardheight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);

        snakehead = new Tile(5,5);
        snakebody = new ArrayList<Tile>();

        food = new Tile(10,10);
        random=new Random();
        placefood();
        velocityX=0;
        velocityY=0;


        gameloop= new Timer(100,this);
        gameloop.start();
        



    }
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }
    public void draw(Graphics g){
        //grid
        // for(int i=0; i < boardwidth/tilesize;i++){
        //     g.drawLine(i*tilesize,0,i*tilesize,boardheight);
        //     g.drawLine(0,i*tilesize,boardwidth,i*tilesize);
        // }
        //food
        g.setColor(Color.red);
        //g.fillRect(food.x*tilesize,food.y*tilesize,tilesize,tilesize);
        g.fill3DRect(food.x*tilesize,food.y*tilesize,tilesize,tilesize,true);

        //snake
        g.setColor(Color.green);
        // g.fillRect(snakehead.x*tilesize,snakehead.y*tilesize,tilesize,tilesize);
        g.fill3DRect(snakehead.x*tilesize,snakehead.y*tilesize,tilesize,tilesize,true);
        
        // snake body 
        for(int i=0 ; i<snakebody.size();i++){
            Tile snakepart = snakebody.get(i);
            // g.fillRect(snakepart.x*tilesize,snakepart.y*tilesize,tilesize,tilesize);
            g.fill3DRect(snakepart.x*tilesize,snakepart.y*tilesize,tilesize,tilesize,true);
        }
        //score
        g.setFont(new Font("Arial",Font.PLAIN,16));
        if(gameover){
            g.setColor(Color.red);
            g.drawString("gameover :"+ String.valueOf(snakebody.size()),tilesize-16,tilesize);
        }
        else{
            g.drawString("score"+ String.valueOf(snakebody.size()),tilesize-16,tilesize );
        }
    }
    public void placefood(){
        food.x=random.nextInt(boardwidth/tilesize);
        food.y=random.nextInt(boardheight/tilesize);

    }
    public boolean collision(Tile tile1,Tile tile2){
        return  tile1.x==tile2.x && tile1.y==tile2.y;
    }
    public void move(){
        if(collision(snakehead, food)){
            snakebody.add(new Tile(food.x, food.y));
            placefood();


        }
        //snakebody
        for(int i=snakebody.size()-1;i>=0;i--){
            Tile snakepart=snakebody.get(i);
            if(i==0){
                snakepart.x=snakehead.x;
                snakepart.y=snakehead.y;
            }
            else{
                Tile prevsnakepart= snakebody.get(i-1);
                snakepart.x=prevsnakepart.x;
                snakepart.y=prevsnakepart.y;
            }
        }


        //snake head
        snakehead.x+=velocityX;
        snakehead.y+=velocityY;
        // gameover
        for(int i=0;i< snakebody.size();i++){
            Tile snakepart=snakebody.get(i);
            //collide with snakehead
            if(collision(snakehead, snakepart)){
                gameover=true;
            }

        }
        if(snakehead.x*tilesize<0||snakehead.x*tilesize>boardwidth||
            snakehead.y*tilesize<0||snakehead.y>boardheight){
            gameover=true;
        }


    }
    @Override
    public void actionPerformed(ActionEvent e){
        move();
        repaint();
        if(gameover){
            gameloop.stop();
        }
            
    }
    @Override
    public void keyPressed(KeyEvent e){
        if(e.getKeyCode()==KeyEvent.VK_UP && velocityY !=1 ){
            velocityX=0;
            velocityY=-1;
        }
        else if(e.getKeyCode()==KeyEvent.VK_DOWN && velocityY !=-1){
            velocityX=0;
            velocityY=1;
        }
        else if(e.getKeyCode()==KeyEvent.VK_LEFT && velocityX != 1){
            velocityX=-1;
            velocityY=0;
        }
        else if(e.getKeyCode()==KeyEvent.VK_RIGHT && velocityX != -1){
            velocityX=1;
            velocityY=0;
        }

    }
    @Override
    public  void keyTyped(KeyEvent e){

    }
    @Override
    public void keyReleased(KeyEvent e){

    }



    
}
