package sim;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class GoalCreate implements MouseListener{
	private MainPanel Panel; //�V�~�����[�^�[��
	private boolean IN=false; //�V�~�����[�^�[�����Ƀ}�E�X�����邩�ǂ���

	/** �R���X�g���N�^ */
	public GoalCreate(MainPanel con){
		Panel=con;
	}
	
	/** �S�[���̐ݒu���J�n����Ƃ��ɌĂяo�����\�b�h */
	public void startEdit(){
		Panel.addMouseListener(this);
	}
	
	/** �S�[���̐ݒu���I���鎞�ɌĂяo�����\�b�h */
	public void endEdit(){
		Panel.removeMouseListener(this);
	}
	
	@Override
	//�����ł͎g�p���Ȃ�
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
	//�����ł͎g�p���Ȃ�
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(!IN)return;
		int btn=e.getButton();
		//�S�[���̐���
		if(btn==MouseEvent.BUTTON1){
			Panel.createGoal(e.getX(),e.getY());
		}
		//�S�[���̏���
		else if(btn==MouseEvent.BUTTON3){
			Panel.removeGoal();
		}
		Panel.repaint();
	}
	
	
	
}
