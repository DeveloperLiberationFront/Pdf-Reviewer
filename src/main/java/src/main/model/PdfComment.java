package src.main.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class PdfComment {

	public static final int TITLE_LENGTH = 47;
	public static final String MUST_FIX = "Must Fix";
	public static final String SHOULD_FIX = "Should Fix";
	public static final String CONSIDER_FIX = "Consider Fixing";
	public static final String POSITIVE = "Positive";
	
	private List<String> tags;
	private String comment;
	
	public PdfComment(String string) {
		setTags(string);
		setComment(string);
	}
	
	public static List<PdfComment> getNegComments(List<String> comments) {
		List<PdfComment> retVal = new ArrayList<>();
		for(String comment : comments) {
			PdfComment pdfComment = new PdfComment(comment);
			if(!pdfComment.getTags().contains(POSITIVE)) {
				retVal.add(pdfComment);
			}
		}
		return retVal;
	}
	
	public static List<PdfComment> getComments(List<String> comments) {
		List<PdfComment> retVal = new ArrayList<>();
		for(String comment : comments) {
			retVal.add(new PdfComment(comment));
		}
		
		return retVal;
	}
	
	private void setComment(String s) {
		int tagsStartPos = s.indexOf("{");
		int tagsEndPos = s.indexOf("}");
		
		if(tagsStartPos != -1 && tagsEndPos != -1) {
			comment = (s.substring(0, tagsStartPos) + s.substring(tagsEndPos + 1, s.length()).trim()).trim();
		}
		else {
			comment = s.trim();
		}
	}
	
	private void setTags(String string) {
		tags = new ArrayList<>();
		
		if(string.contains("{") && string.contains("}")) {
			String tagList = string.substring(string.indexOf('{') + 1, string.indexOf('}'));
			String[] tagsStr = tagList.split(",");
			
			for(String tag : tagsStr) {
				tags.add(getTag(tag));
			}	
		}
	}
	
	public static String getTag(String t) {
		t = t.trim();
		if(t.equals("mf"))
			return MUST_FIX;
		else if(t.equals("sf"))
			return SHOULD_FIX;
		else if(t.equals("cf"))
			return CONSIDER_FIX;
		else if(t.equals("g") || t.equals("p"))
			return POSITIVE;
		else
			return t;
	}
	
	public String getComment() {
		return comment;
	}
	
	public String getTitle() {
		boolean tooLong = comment.length() > TITLE_LENGTH;
		int end = tooLong ? TITLE_LENGTH : comment.length();
		String ellipsis = tooLong? "..." : "";
		return comment.substring(0, end).trim() + ellipsis;
	}
	
	public List<String> getTags() {
		return tags;
	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		
		try {
			json.put("title", getTitle());
			json.put("tags", getTags());
			json.put("comment", getComment());
		} catch(JSONException e) {}
		
		return json;
	}
	
	@Override
	public String toString() {
		return getTitle() + " {" + getTags() + "}: " + getComment();
	}
}
