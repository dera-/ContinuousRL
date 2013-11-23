package sim;

import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Point;
import java.util.ArrayList;

import sim.obj.WallData;

public class WallCreate implements MouseListener, MouseMotionListener {
	private MainPanel Panel; //シミュレーター部 
	private Point Start=null;  //四角形作成の開始点の座標
	private boolean IN=false; //シミュレーター部内にマウスがあるかどうか
	
	/** コンストラクタ */
	public WallCreate(MainPanel con){
		Panel=con;
	}
	
	/** 壁の作成を開始するときに呼び出すメソッド */
	public void startEdit(){
		Panel.addMouseListener(this);
		Panel.addMouseMotionListener(this);
	}
	
	/** 壁の作成を終える時に呼び出すメソッド */
	public void endEdit(){
		Panel.removeMouseListener(this);
		Panel.removeMouseMotionListener(this);
	}
	

	@Override
	public void mouseDragged(MouseEvent arg0) {
		if(!IN)return;
		//左ドラッグ以外は遮断
		if(Start==null)return;
		int x=arg0.getX();
		int y=arg0.getY();
		Panel.rewriteLastElement(createWall(x,y));
		Panel.repaint();
	}

	@Override
	//ここでは使用しない
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	//ここでは使用しない
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	//ここでは使用しない
	public void mouseEntered(MouseEvent arg0) {
		IN=true;
	}

	@Override
	//ここでは使用しない
	public void mouseExited(MouseEvent arg0) {
		IN=false;
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		if(!IN)return;
		int btn=arg0.getButton();
		//左ボタンを押した時
		if(btn==MouseEvent.BUTTON1 && Start==null){
			int x=arg0.getX();
			int y=arg0.getY();
			Start=new Point(x,y);
			Panel.addWallElement(createWall(x,y));
			Panel.repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if(!IN)return;
		int btn=arg0.getButton();
		//左クリック時
		if(btn==MouseEvent.BUTTON1)Start=null;
		//右クリック時
		else if(btn==MouseEvent.BUTTON3){
			Panel.removeWallElement(arg0.getX(),arg0.getY());
			Panel.repaint();
		}
	}
	
	/** WallDataの作成 */
	private WallData createWall(int x,int y){
		int X,Y;
		int width = Math.abs(x-Start.x);
		int height = Math.abs(y-Start.y);
		if(Start.x <= x)X=Start.x;
		else X=x;
		if(Start.y <= y)Y=Start.y;
		else Y=y;
		return new WallData(X,Y,width,height);
	}
	
}
