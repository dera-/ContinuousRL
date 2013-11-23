package sim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.*;

import javax.swing.JButton;
import javax.swing.JPanel;

import sim.obj.WallData;

public class MainPanel extends JPanel implements Runnable{
	private Thread thr; /** 別スレッド */
	private Image offscreenImg; //裏イメージ
	private Simulation Road;  //シミュレーター本体
	private boolean GUI=true;  //学習過程をグラフィックで見るかどうか
	private boolean Continue=true;
	private JButton[] EventButtons; //MainFrameのボタン配列を受け取る
	
	//コンストラクタ
	public MainPanel(JButton[] buttons){
		setPreferredSize(new Dimension(Parameters.SimulatorWidth, Parameters.SimulatorHeight));  //シミュレーター画面のサイズの設定
		setBackground(Color.white);
		Road = new Simulation();
		EventButtons = buttons;
	}
	
	//repaintメソッドが呼び出すメソッド
	public void update(Graphics g){
		paint(g);
	}
	
	//描画を行うメソッド
	public void paint(Graphics g){
		//裏イメージを作成する
		if(offscreenImg==null){
			offscreenImg=createImage(Parameters.SimulatorWidth, Parameters.SimulatorHeight);
		}
		//裏イメージを背景色で塗りつぶす
		Graphics offscreenG;
		offscreenG = offscreenImg.getGraphics();
		offscreenG.setColor(getBackground());
        offscreenG.fillRect(0, 0, Parameters.SimulatorWidth, Parameters.SimulatorHeight);
        //裏イメージにシミュレーション画面の描画を行う。
        offscreenG.setColor(Color.black);
        draw(offscreenG);
        //裏イメージを表イメージに貼り付ける
        g.drawImage(offscreenImg,0,0,this);
	}
	
	/** 実際の描画を行う */
	private void draw(Graphics g){
		Road.draw(g);
	}
		
	//別スレッドが行う処理
	public void run(){
		Road.format();
		while(Continue){
			boolean flag=Road.running();
			if(flag){
				Road.fileClose();
				break;
			}
			if(!GUI)continue;
			repaint();
			try{
				Thread.sleep(Parameters.OneFrame);
			}catch(Exception ex){}
		}
		System.out.println("Simulation_End");
		thr=null;
		usableButtons();
	}
	
	/** WallListに要素を追加するメソッド */
	public void addWallElement(WallData wall){
		Road.addWall(wall);
	}
	
	/** WallListの最後の要素を書き換えるメソッド */
	public void rewriteLastElement(WallData wall){
		Road.rewriteLastElement(wall);
	}
	
	/** 引数座標を含んでいる壁要素を全て消すメソッド */
	public void removeWallElement(int x,int y){
		Road.removeWallElement(x,y);
	}
	
	/** ゴールを引数座標の場所に作る */
	public void createGoal(int x,int y){
		Road.createGoal(x,y);
	}
	
	/** ゴールを消去するメソッド */
	public void removeGoal(){
		Road.removeGoal();
	}
	
	/** フィールドGUIの値を引数の値に変えるメソッド */
	public void changeGUI(boolean f){
		GUI=f;
	}
	
	/** シミュレーションを始めていいかどうかの判定を行うメソッド */
	public boolean isPermitStart(){
		return Road.okStart();
	}
	
	/** シミュレーションを開始するメソッド */
	public void startSimulate(){
		Continue=true;
		thr = new Thread(this);
		thr.start();
	}
	
	/** シミュレーションを停止するメソッド  */
	public void stopSimulate(){
		Continue=false;
	}
	
	/** 現在開いているファイルを閉じるメソッド */
	public void fileClose(){
		Road.fileClose();
	}
	
	/** ボタンを全て使用可能にする */
	private void usableButtons(){
		for(int i=0;i<EventButtons.length;i++){
			if(i==Parameters.Number_UI)continue;
			EventButtons[i].setEnabled(true);
			EventButtons[i].setText(Parameters.NonPushed[i]); 
		}
	}
	
}
