import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonReader;

import enumerate.Action;

public class JSONUtility {
	public static String toJSON(TrainingData trainingData) {
		JsonObject dataobj = Json.createObjectBuilder()
				.add("winCount", trainingData.winCount)
				.add("matchCount", trainingData.matchCount)
				.add("ruleThreshold", trainingData.ruleThreshold)
				.add("maxScore", trainingData.maxScore)
				.add("maxInc", trainingData.maxInc)
				.add("incMod", trainingData.incMod)
				.add("groundRules", buildRules(trainingData.groundRules))
				.add("airRules", buildRules(trainingData.airRules))
				.build();
		
		StringWriter stringwriter = new StringWriter();
        JsonWriter jsonwriter = Json.createWriter(stringwriter);
        jsonwriter.writeObject(dataobj);
        jsonwriter.close();
		return stringwriter.getBuffer().toString();
	}
	
	private static JsonArray buildRules(ArrayList<Rule> rules) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		for(Rule r : rules)
			builder.add(buildRule(r));
		return builder.build();
	}
	
	private static JsonObject buildRule(Rule rule) {
		return Json.createObjectBuilder()
				.add("positionType", rule.positionType.name())
				.add("enemyAction", rule.enemyAction.name())
				.add("acts", buildActionDatas(rule.acts))
				.build();
	}
	
	private static JsonArray buildActionDatas(ArrayList<ActionData> actionDatas) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		for(ActionData d : actionDatas)
			builder.add(buildActionData(d));
		
		return builder.build();
	}
	
	private static JsonObject buildActionData(ActionData actionData) {
		return Json.createObjectBuilder()
				.add("action", actionData.action.name())
				.add("score", actionData.score)
				.build();
	}
	
	public static TrainingData fromJSON(String jsonString) {
		JsonReader jreader = Json.createReader(new StringReader(jsonString));
		JsonObject dataobj = jreader.readObject();
		jreader.close();

		TrainingData val = new TrainingData();
		val.winCount = dataobj.getInt("winCount");
		val.matchCount = dataobj.getInt("matchCount");
		val.ruleThreshold = (float)dataobj.getJsonNumber("ruleThreshold").doubleValue();
		val.maxScore = (float)dataobj.getJsonNumber("maxScore").doubleValue();
		val.maxInc = (float)dataobj.getJsonNumber("maxInc").doubleValue();
		val.incMod = (float)dataobj.getJsonNumber("incMod").doubleValue();
		val.groundRules = getRules(dataobj.getJsonArray("groundRules"));
		val.airRules = getRules(dataobj.getJsonArray("airRules"));
		
		return val;
	}
	
	private static ArrayList<Rule> getRules(JsonArray jRules){
		ArrayList<Rule> val = new ArrayList<Rule>();
		for(int i = 0 ; i < jRules.size() ; i++)
			val.add(getRule(jRules.getJsonObject(i)));
		return val;
	}
	
	private static Rule getRule(JsonObject jRule) {
		Rule val = new Rule();
		val.positionType = PositionType.valueOf(jRule.getString("positionType"));
		val.enemyAction = Action.valueOf(jRule.getString("enemyAction"));
		val.acts = getActionDatas(jRule.getJsonArray("acts"));
		return val;
	}
	
	private static ArrayList<ActionData> getActionDatas(JsonArray jActs){
		ArrayList<ActionData> val = new ArrayList<ActionData>();
		for(int i = 0 ; i < jActs.size() ; i++)
			val.add(getActionData(jActs.getJsonObject(i)));
		return val;
	}
	
	private static ActionData getActionData(JsonObject jAct) {
		Action act = Action.valueOf(jAct.getString("action"));
		float score = (float)jAct.getJsonNumber("score").doubleValue();
		return new ActionData(act, score);
	}
}
