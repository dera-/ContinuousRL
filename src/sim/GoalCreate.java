package sim;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class GoalCreate implements MouseListener{
	private MainPanel Panel; //シミュレーター部
	private boolean IN=false; //シミュレーター部内にマウスがあるかどうか

	/** コンストラクタ */
	public GoalCreate(MainPanel con){
		Panel=con;
	}
	
	/** ゴールの設置を開始するときに呼び出すメソッド */
	public void startEdit(){
		Panel.addMouseListener(this);
	}
	
	/** ゴールの設置を終える時に呼び出すメソッド */
	public void endEdit(){
		Panel.removeMouseListener(this);
	}
	
	@Override
	//ここでは使用しない
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {
		IN=true;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		IN=false;
	}

	@Override
	//ここでは使用しない
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(!IN)return;
		int btn=e.getButton();
		//ゴールの生成
		if(btn==MouseEvent.BUTTON1){
			Panel.createGoal(e.getX(),e.getY());
		}
		//ゴールの消去
		else if(btn==MouseEvent.BUTTON3){
			Panel.removeGoal();
		}
		Panel.repaint();
	}
	
	
	
}
