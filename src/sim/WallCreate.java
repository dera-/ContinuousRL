package sim;

import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Point;
import java.util.ArrayList;

import sim.obj.WallData;

public class WallCreate implements MouseListener, MouseMotionListener {
	private MainPanel Panel; //�V�~�����[�^�[�� 
	private Point Start=null;  //�l�p�`�쐬�̊J�n�_�̍��W
	private boolean IN=false; //�V�~�����[�^�[�����Ƀ}�E�X�����邩�ǂ���
	
	/** �R���X�g���N�^ */
	public WallCreate(MainPanel con){
		Panel=con;
	}
	
	/** �ǂ̍쐬���J�n����Ƃ��ɌĂяo�����\�b�h */
	public void startEdit(){
		Panel.addMouseListener(this);
		Panel.addMouseMotionListener(this);
	}
	
	/** �ǂ̍쐬���I���鎞�ɌĂяo�����\�b�h */
	public void endEdit(){
		Panel.removeMouseListener(this);
		Panel.removeMouseMotionListener(this);
	}
	

	@Override
	public void mouseDragged(MouseEvent arg0) {
		if(!IN)return;
		//���h���b�O�ȊO�͎Ւf
		if(Start==null)return;
		int x=arg0.getX();
		int y=arg0.getY();
		Panel.rewriteLastElement(createWall(x,y));
		Panel.repaint();
	}

	@Override
	//�����ł͎g�p���Ȃ�
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	//�����ł͎g�p���Ȃ�
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	//�����ł͎g�p���Ȃ�
	public void mouseEntered(MouseEvent arg0) {
		IN=true;
	}

	@Override
	//�����ł͎g�p���Ȃ�
	public void mouseExited(MouseEvent arg0) {
		IN=false;
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		if(!IN)return;
		int btn=arg0.getButton();
		//���{�^������������
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
		//���N���b�N��
		if(btn==MouseEvent.BUTTON1)Start=null;
		//�E�N���b�N��
		else if(btn==MouseEvent.BUTTON3){
			Panel.removeWallElement(arg0.getX(),arg0.getY());
			Panel.repaint();
		}
	}
	
	/** WallData�̍쐬 */
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
