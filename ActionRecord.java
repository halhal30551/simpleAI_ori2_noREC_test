import enumerate.Action;

public class ActionRecord {
	public Action action;
	public int startFrame;
	public int endFrame;
	public boolean isGroundRule;
	public int ruleID;
	public int actID;
	public int playerHP0;
	public int enemyHP0;
	public int playerHP1;
	public int enemyHP1;
	
	public ActionRecord unsaveClone() {
		ActionRecord val = new ActionRecord();
		val.action = this.action;
		val.startFrame = this.startFrame;
		val.endFrame = this.endFrame;
		val.isGroundRule = this.isGroundRule;
		val.ruleID = this.ruleID;
		val.actID = this.actID;
		val.playerHP0 = this.playerHP0;
		val.playerHP1 = this.playerHP1;
		val.enemyHP0 = this.enemyHP0;
		val.enemyHP1 = this.enemyHP1;		
		return val;
	}
	
	public boolean isInFrame(int frameNumber) {
		return frameNumber > 0 && frameNumber >= this.startFrame && frameNumber <= this.endFrame;
	}
}
