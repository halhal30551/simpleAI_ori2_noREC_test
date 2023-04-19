import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.Date;

import enumerate.Action;
import enumerate.State;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;
import struct.MotionData;

public class SimpleTraining {
		
	public static final Action[] GroundActions = new Action[] {Action.STAND_D_DB_BA, Action.BACK_STEP, 
			Action.FORWARD_WALK, Action.DASH, Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, 
			Action.STAND_GUARD, Action.CROUCH_GUARD, Action.THROW_A, Action.THROW_B, 
			Action.STAND_A, Action.STAND_B, Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA, 
			Action.STAND_FB, Action.CROUCH_FA, Action.CROUCH_FB, Action.STAND_D_DF_FA, 
			Action.STAND_D_DF_FB, Action.STAND_F_D_DFA, Action.STAND_F_D_DFB, Action.STAND_D_DB_BB, Action.STAND_D_DF_FC};
	public static final Action[] AirActions = new Action[] {Action.AIR_GUARD, Action.AIR_A, 
			Action.AIR_B, Action.AIR_DA, Action.AIR_DB, Action.AIR_FA, Action.AIR_FB, 
			Action.AIR_UA, Action.AIR_UB, Action.AIR_D_DF_FA, Action.AIR_D_DF_FB, 
			Action.AIR_F_D_DFA, Action.AIR_F_D_DFB, Action.AIR_D_DB_BA, Action.AIR_D_DB_BB};
	public static final float ruleThreshold = 0.8f;
	
	private final String _dirname = "data/aiData/SimpleAI";
	private final float _maxscore = 10.0f;
	private final float _maxinc = 1.0f;
	private final float _incmod = 1.0f; //..default 1.0f
	private final float _maxdmg = 120.0f;
	private final int _framedelay = 15;
	
	private boolean _recorded;
	private String _playername;
	private ArrayList<MotionData> _motiondata;
	private ArrayList<Rule> _groundrules;
	private ArrayList<Rule> _airrules;
	private ArrayList<ActionRecord> _actcache;
	private ArrayList<ActionRecord> _records;
	private ActionRecord _currentact;
	private int _wincount = 0;
	private int _matchcount = 0;
		
	public SimpleTraining(GameData gameData, boolean playerID, boolean isRecorded) {
		_recorded = isRecorded;
		_playername = gameData.getCharacterName(playerID);
		_motiondata = gameData.getMotionData(playerID);
		_actcache = new ArrayList<ActionRecord>();
		_records = new ArrayList<ActionRecord>();
		_currentact = null;
		_wincount = 0;
		_matchcount = 0;
		initRules(_playername);
	}
	
	private void initRules(String charaName) {
		File dir = new File(_dirname);
		File file = new File(_dirname + "/" + getFileName(charaName));

		if(!dir.exists())
			dir.mkdir();

		if(!file.exists())
			initNewData(file.getPath());
		else
			initDataFromFile(file.getPath());
	}
	
	private void initNewData(String filePath) {		//新規データファイル作成
		_groundrules = new ArrayList<Rule>();
		_airrules = new ArrayList<Rule>();
		
		for(PositionType pt : PositionType.values()) {
			for(Action at : Action.values()) {
				Rule grule = new Rule();
				Rule arule = new Rule();
				
				grule.positionType = pt;
				grule.enemyAction = at;
				grule.acts = new ArrayList<ActionData>();
				for(Action gact : GroundActions) {
					ActionData tact = new ActionData(gact, _maxscore * 0.5f);
					MotionData tmot = _motiondata.get(Action.valueOf(gact.name()).ordinal());
					tact.setEnergyConsumed(Math.abs(tmot.getAttackStartAddEnergy()));
					tact.setFrameCount(tmot.frameNumber);
					grule.acts.add(tact);
				}
				
				arule.positionType = pt;
				arule.enemyAction = at;
				arule.acts = new ArrayList<ActionData>();
				for(Action aact : AirActions){
					ActionData tact = new ActionData(aact, _maxscore * 0.5f);
					MotionData tmot = _motiondata.get(Action.valueOf(aact.name()).ordinal());
					tact.setEnergyConsumed(Math.abs(tmot.getAttackStartAddEnergy()));
					tact.setFrameCount(tmot.frameNumber);
					arule.acts.add(tact);
				}
				
				_groundrules.add(grule);
				_airrules.add(arule);
			}
		}
		
		TrainingData trainingdata = new TrainingData();
		trainingdata.winCount = 0;
		trainingdata.matchCount = 0;
		trainingdata.ruleThreshold = ruleThreshold;
		trainingdata.maxScore = _maxscore;
		trainingdata.maxInc = _maxinc;
		trainingdata.incMod = _incmod;
		trainingdata.groundRules = new ArrayList<Rule>(_groundrules);
		trainingdata.airRules = new ArrayList<Rule>(_airrules);
		writeData(trainingdata, filePath);
		System.out.println("create new file");
	}
	
	private void initDataFromFile(String filePath) {		//ファイルからのデータ読み出し
		TrainingData trainingdata = readData(filePath);
		if(trainingdata == null) {
			initNewData(filePath);		//ファイル新規作成
		}else {
			_wincount = trainingdata.winCount;
			_matchcount = trainingdata.matchCount;
			_groundrules = new ArrayList<Rule>(trainingdata.groundRules);
			_airrules = new ArrayList<Rule>(trainingdata.airRules);
			
			for(Rule r : _groundrules) {
				for(ActionData a : r.acts) {
					MotionData tmot = _motiondata.get(Action.valueOf(a.action.name()).ordinal());
					a.setEnergyConsumed(Math.abs(tmot.getAttackStartAddEnergy()));
					a.setFrameCount(tmot.frameNumber);
				}
			}
			
			for(Rule r : _airrules) {
				for(ActionData a : r.acts) {
					MotionData tmot = _motiondata.get(Action.valueOf(a.action.name()).ordinal());
					a.setEnergyConsumed(Math.abs(tmot.getAttackStartAddEnergy()));
					a.setFrameCount(tmot.frameNumber);
				}
			}
		}	
		System.out.println("read from file"+filePath);
	}
	
	private void writeData(TrainingData trainingData, String filePath) {	//Data書き込み
		System.out.println("write Data to "+filePath);
		try {
			String stringdata = JSONUtility.toJSON(trainingData);
			FileWriter writer = new FileWriter(filePath);
			BufferedWriter buffer = new BufferedWriter(writer);
			buffer.write(stringdata);
			buffer.close();			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private TrainingData readData(String filePath) {		//データ読み出し
		try {
			FileReader reader = new FileReader(filePath);
			BufferedReader buffer = new BufferedReader(reader);
			String tempstring = "";
			String stringdata = "";

			try {
				while((tempstring = buffer.readLine()) != null) {
					stringdata += tempstring;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}			
			return JSONUtility.fromJSON(stringdata);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}		
	}
	
	private String getFileName(String charaName) {		//キャラクターとデータファイルの対応
		switch(charaName){
		case "ZEN":
			//return "training_01.data";
			return "original2_u_01.data";
		case "GARNET":
			//return "training_02.data";
			return "original2_u_02.data";
		case "LUD":
			//return "training_03.data";
			return "original2_u_03.data";
		default:
			return "";
		}
	}
	
	public Action selectAction(CharacterData player, CharacterData enemy, FrameData frameData) {
		ArrayList<Rule> rules = player.getState() == State.AIR ? _airrules : _groundrules;		//player状態がairならairRULE
		PositionType postype = Positioning.CalculatePosition(player.getCenterX(), player.getCenterY(), 
				enemy.getCenterX(), enemy.getCenterY());		//ポジションタイプ
		int rid = findRuleID(rules, postype, enemy.getAction());	//RuleID
		int aid = rules.get(rid).getBestActionID(player.getEnergy(), rid, rules, _matchcount);	//アクションID
		if(rid < 0 || aid < 0) {
			System.out.println("!! no action picked !!");		//両方マイナス、何も選択してない
			return Action.STAND;
		}
		
		if(_recorded) {		//レコーディング
			ActionRecord rec = new ActionRecord();
			rec.action = rules.get(rid).acts.get(aid).action;
			rec.startFrame = frameData.getFramesNumber();
			rec.endFrame = rec.startFrame + rules.get(rid).acts.get(aid).getFrameCount() + _framedelay;
			rec.isGroundRule = player.getState() != State.AIR;
			rec.ruleID = rid;
			rec.actID = aid;
			rec.playerHP0 = player.getHp();
			rec.enemyHP0 = enemy.getHp();
			_actcache.add(rec);
		}

		return rules.get(rid).acts.get(aid).action;
	}
	
	private int findRuleID(ArrayList<Rule> rules, PositionType posType, Action enemyAction) {	//ポジションと敵アクションに応じたインデックスを返す
		for(int i = 0 ; i < rules.size() ; i++) {
			if(rules.get(i).enemyAction == enemyAction && rules.get(i).positionType == posType) {
				return i;
			}
		}		
		return -1;
	}
	
	public void update(CharacterData player, CharacterData enemy, FrameData frameData) {//アップデート
		if(!_recorded)
			return;
		
		if(_currentact == null) {
			_currentact = findRecord(player.getAction(), _actcache);
			if(_currentact != null || isHitFrame(player, enemy, frameData))
				_actcache.clear();
		} else {
			if(canDoEvaluation(player, enemy, frameData)) {				
				if(_recorded && _currentact != null) {
					_currentact.playerHP1 = player.getHp();
					_currentact.enemyHP1 = enemy.getHp();
					_records.add(_currentact.unsaveClone());
					_currentact = null;
				}				
			}				
		}
	}
	
	public void endRound(boolean isWinning) {		//round終了時
		String filepath = _dirname + "/" + getFileName(_playername);//パス
		
		_matchcount++;
		if(isWinning)
			_wincount++;
		evaluateRecords(_records, isWinning);

		TrainingData trainingdata = new TrainingData();
		trainingdata.airRules = _airrules;
		trainingdata.groundRules = _groundrules;
		trainingdata.incMod = _incmod;
		trainingdata.matchCount = _matchcount;
		trainingdata.maxInc = _maxinc;
		trainingdata.maxScore = _maxscore;
		trainingdata.ruleThreshold = ruleThreshold;
		trainingdata.winCount = _wincount;		
		if (_recorded) {
			writeData(trainingdata, filepath);		//データ書き込み
		}
		
		
		//..reset for the next round
		_actcache = new ArrayList<ActionRecord>();
		_records = new ArrayList<ActionRecord>();
		_currentact = null;
		//..reset for the next round
	}
	
	private void evaluateRecords(ArrayList<ActionRecord> records, boolean isWin) {
		System.out.println("FINISHING...");
		ArrayList<ActionData> executedacts = new ArrayList<ActionData>();
		
		//..scores individual act
		for(ActionRecord r : records) {
			float inc = calculateScoreInc(r.playerHP0, r.playerHP1, r.enemyHP0, r.enemyHP1);	//HPからスコア算出
			ActionData actdata = r.isGroundRule ? _groundrules.get(r.ruleID).acts.get(r.actID) : 
				_airrules.get(r.ruleID).acts.get(r.actID);	//GROUND or AIR
			actdata.score += inc;					//既存スコアに足していく
			
			//..add win/lose reward/penalty
			if(!executedacts.contains(actdata)) {		//多分実行されてうる
				executedacts.add(actdata);
				actdata.score += (isWin ? 0.2f : -0.1f);		//勝っていれば+0.2, 負けていれば-0.1
				System.out.println("isWin ? 0.2f : -0.1f, isWin="+isWin);
			}
			//..add win/lose reward/penalty
		}
		//..scores individual act
				
		//..clamp scores
		for(ActionRecord r : records) {
			ActionData actdata = r.isGroundRule ? _groundrules.get(r.ruleID).acts.get(r.actID) : 
				_airrules.get(r.ruleID).acts.get(r.actID);
			if(actdata.score < 0)
				actdata.score = 0;
			else if(actdata.score > _maxscore)		//最大値超えたら矯正
				actdata.score = _maxscore;		//MAX更新
		}
		//..clamp scores
	}
	
//	private void evaluateRecords(ArrayList<ActionRecord> records) {
//		System.out.println("finishing . . .");
//		for(ActionRecord r : records) {
//			float inc = calculateScoreInc(r.playerHP0, r.playerHP1, r.enemyHP0, r.enemyHP1);
//			ActionData actdata = r.isGroundRule ? _groundrules.get(r.ruleID).acts.get(r.actID) : 
//				_airrules.get(r.ruleID).acts.get(r.actID);
//			actdata.score += inc;
//		}
//		
//		//..clamp scores
//		for(ActionRecord r : records) {
//			ActionData actdata = r.isGroundRule ? _groundrules.get(r.ruleID).acts.get(r.actID) : 
//				_airrules.get(r.ruleID).acts.get(r.actID);
//			if(actdata.score < 0)
//				actdata.score = 0;
//			else if(actdata.score > _maxscore)
//				actdata.score = _maxscore;
//		}
//	}
	
	private float calculateScoreInc(int pHP0, int pHP1, int eHP0, int eHP1) {		//スコア計算
		return calculateScoreInc(pHP0, pHP1, eHP0, eHP1, 1.0f);
	}
	
	private float calculateScoreInc(int pHP0, int pHP1, int eHP0, int eHP1, float incPercentage) {			//スコア計算	
		float val = ((float)(pHP1 - pHP0) / _maxdmg) - ((float)(eHP1 - eHP0) / _maxdmg);		//(HP最終値-HP初期値)/120		+なら勝ち-なら負け
		val *= (incPercentage * _incmod);		//×1？
		if(val < -_maxinc)
			return -_maxinc;		//-1未満なら-1を返す
		else if(val > _maxinc)
			return _maxinc;			//1より大きいなら1を返す
		else
			return val;		//-1～1はそのまま
	}
	
	private ActionRecord findRecord(Action act, ArrayList<ActionRecord> source) {
		for(ActionRecord r : source) {
			if(r.action == act)
				return r;
		}		
		return null;
	}
		
	private boolean canDoEvaluation(CharacterData player, CharacterData enemy, FrameData frame) {		
		if(frame.getFramesNumber() >= (_currentact.endFrame) || _currentact.isInFrame(player.getLastHitFrame()) || _currentact.isInFrame(enemy.getLastHitFrame()))
			return true;
		else
			return false;
	}
	
	private boolean isHitFrame(CharacterData player, CharacterData enemy, FrameData frame) {
		return frame.getFramesNumber() == player.getLastHitFrame() || frame.getFramesNumber() == enemy.getLastHitFrame();
	}
}
