import aiinterface.AIInterface;
import aiinterface.CommandCenter;
import enumerate.Action;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;
import struct.Key;
import java.util.Date;
import java.util.ArrayList;

public class Simple_ori2_noREC_test implements AIInterface {

	//--grey-bg --mute --inverted-player 1 -n 50 --c1 ZEN --c2 ZEN --a1 SimpleReinforcement --a2 MCTS --disable-window --fastmode
	private boolean _playerid;
	private CommandCenter _cmd;
	private Key _key;
	private FrameData _framedata;
	private CharacterData _player;
	private CharacterData _enemy;
	private SimpleTraining _training;
	private ArrayList<String> roundData;
	private int countRound=0;
	private  String Command;
	
	@Override
	public int initialize(GameData gameData, boolean playerID) {
		_playerid = playerID;
		_cmd = new CommandCenter();
		_key = new Key();
		_training = new SimpleTraining(gameData, playerID, false);	//issRecorded=false
		this.roundData = new ArrayList<String>();
		Date now=new Date();
		roundData.add("start: "+now);
		System.out.println("round start");
		return 0;
	}
	
	@Override
	public void close() {
		for(int i=0; i<roundData.size(); i++) {
			System.out.println(roundData.get(i));
		}
	}

	@Override
	public void getInformation(FrameData frameData) {
		_framedata = frameData;
		_cmd.setFrameData(_framedata, _playerid);
		_player = _framedata.getCharacter(_playerid);
		_enemy = _framedata.getCharacter(!_playerid);
	}

	@Override
	public Key input() {
		return _key;
	}

	@Override
	public void processing() {			//in game loop
		if(!canProcess())
			return;

		boolean consesutive_command=false;
		_training.update(_player, _enemy, _framedata);	//データ更新
		if (_cmd.getSkillFlag()) {
			_key = _cmd.getSkillKey();	//コマンド入力済みの場合、それを実行する
		} else{
			_cmd.skillCancel();		//コマンドリセット
			Action selectedact = _training.selectAction(_player, _enemy, _framedata);
			
			if(selectedact.name()=="STAND_GUARD"||selectedact.name()=="CROUCH_GUARD"||
					selectedact.name()=="FORWARD_WALK"||selectedact.name()=="DASH"||selectedact.name()=="AIR_GUARD") {
				if(selectedact.name()==Command) {
					consesutive_command=true;		//連続性コマンド
					System.out.println("consesutive_command=true");
				}
			}
			if(!consesutive_command) {
				_key.empty();
			}

			_cmd.commandCall(selectedact.name());		//コマンド入力
			Command=selectedact.name();				//旧コマンド更新
			
			//_cmd.commandCall("FORWARD_WALK");
		}
	}

	@Override
	public void roundEnd(int p1Hp, int p2Hp, int frames) {		//ラウンド終了
		_training.endRound(_player.getHp() > _enemy.getHp());	//hp差をもとに学習
		System.out.println("iswin: " + (_player.getHp() > _enemy.getHp()) + " || HP1:" + p1Hp + " || HP2:" +p2Hp);
		
		Date now=new Date();
		countRound+=1;
		roundData.add("ROUND"+countRound);
		roundData.add("P1HP: "+p1Hp);
		roundData.add("P2HP: "+p2Hp);
		roundData.add("end : "+now);
	}
	
	public boolean canProcess() {
		return !_framedata.getEmptyFlag() && _framedata.getRemainingFramesNumber() > 0;
	}
}
