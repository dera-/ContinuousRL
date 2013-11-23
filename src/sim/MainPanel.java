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
	private Thread thr; /** �ʃX���b�h */
	private Image offscreenImg; //���C���[�W
	private Simulation Road;  //�V�~�����[�^�[�{��
	private boolean GUI=true;  //�w�K�ߒ����O���t�B�b�N�Ō��邩�ǂ���
	private boolean Continue=true;
	private JButton[] EventButtons; //MainFrame�̃{�^���z����󂯎��
	
	//�R���X�g���N�^
	public MainPanel(JButton[] buttons){
		setPreferredSize(new Dimension(Parameters.SimulatorWidth, Parameters.SimulatorHeight));  //�V�~�����[�^�[��ʂ̃T�C�Y�̐ݒ�
		setBackground(Color.white);
		Road = new Simulation();
		EventButtons = buttons;
	}
	
	//repaint���\�b�h���Ăяo�����\�b�h
	public void update(Graphics g){
		paint(g);
	}
	
	//�`����s�����\�b�h
	public void paint(Graphics g){
		//���C���[�W���쐬����
		if(offscreenImg==null){
			offscreenImg=createImage(Parameters.SimulatorWidth, Parameters.SimulatorHeight);
		}
		//���C���[�W��w�i�F�œh��Ԃ�
		Graphics offscreenG;
		offscreenG = offscreenImg.getGraphics();
		offscreenG.setColor(getBackground());
        offscreenG.fillRect(0, 0, Parameters.SimulatorWidth, Parameters.SimulatorHeight);
        //���C���[�W�ɃV�~�����[�V������ʂ̕`����s���B
        offscreenG.setColor(Color.black);
        draw(offscreenG);
        //���C���[�W��\�C���[�W�ɓ\��t����
        g.drawImage(offscreenImg,0,0,this);
	}
	
	/** ���ۂ̕`����s�� */
	private void draw(Graphics g){
		Road.draw(g);
	}
		
	//�ʃX���b�h���s������
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
	
	/** WallList�ɗv�f��ǉ����郁�\�b�h */
	public void addWallElement(WallData wall){
		Road.addWall(wall);
	}
	
	/** WallList�̍Ō�̗v�f�����������郁�\�b�h */
	public void rewriteLastElement(WallData wall){
		Road.rewriteLastElement(wall);
	}
	
	/** �������W���܂�ł���Ǘv�f��S�ď������\�b�h */
	public void removeWallElement(int x,int y){
		Road.removeWallElement(x,y);
	}
	
	/** �S�[�����������W�̏ꏊ�ɍ�� */
	public void createGoal(int x,int y){
		Road.createGoal(x,y);
	}
	
	/** �S�[�����������郁�\�b�h */
	public void removeGoal(){
		Road.removeGoal();
	}
	
	/** �t�B�[���hGUI�̒l�������̒l�ɕς��郁�\�b�h */
	public void changeGUI(boolean f){
		GUI=f;
	}
	
	/** �V�~�����[�V�������n�߂Ă������ǂ����̔�����s�����\�b�h */
	public boolean isPermitStart(){
		return Road.okStart();
	}
	
	/** �V�~�����[�V�������J�n���郁�\�b�h */
	public void startSimulate(){
		Continue=true;
		thr = new Thread(this);
		thr.start();
	}
	
	/** �V�~�����[�V�������~���郁�\�b�h  */
	public void stopSimulate(){
		Continue=false;
	}
	
	/** ���݊J���Ă���t�@�C������郁�\�b�h */
	public void fileClose(){
		Road.fileClose();
	}
	
	/** �{�^����S�Ďg�p�\�ɂ��� */
	private void usableButtons(){
		for(int i=0;i<EventButtons.length;i++){
			if(i==Parameters.Number_UI)continue;
			EventButtons[i].setEnabled(true);
			EventButtons[i].setText(Parameters.NonPushed[i]); 
		}
	}
	
}
