package src.main.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses comments in pdf to a more rich data type.
 *
 */
public class PdfComment {

	public static final int TITLE_LENGTH = 47;
	public static final String MUST_FIX = "Must Fix";
	public static final String SHOULD_FIX = "Should Fix";
	public static final String CONSIDER_FIX = "Consider Fixing";
	public static final String POSITIVE = "Positive";
	
	private List<String> tags;
	private String comment;
	private int issueNumber;
	
	public PdfComment(String string) {
		setTags(string);
		setIssueNumber(string);
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
		comment = s;
		
		int tagsStartPos = comment.indexOf('{');
		int tagsEndPos = comment.indexOf('}');
		if(tagsStartPos != -1 && tagsEndPos != -1) {
			String fHalf = comment.substring(0, tagsStartPos).trim();
			String sHalf = comment.substring(tagsEndPos + 1, comment.length()).trim();
			comment = fHalf + " " + sHalf;
		}
		
		int issueStartPos = comment.indexOf("[[");
		int issueEndPos = comment.indexOf("]]");
		if(issueStartPos != -1 && issueEndPos != -1) {
			String fHalf = comment.substring(0, issueStartPos).trim();
			String sHalf = comment.substring(issueEndPos + 2, comment.length()).trim();
			comment = fHalf + " " + sHalf;
		}
		
		comment = comment.trim();
	}
	
	public void setIssueNumber(String str) {
		int issueStartPos = str.indexOf("[[");
		int issueEndPos = str.indexOf("]]");
		if(issueStartPos != -1 && issueEndPos != -1) {
			String issueAreaStr = str.substring(issueStartPos + 1, issueEndPos);
			String issueStr = issueAreaStr.substring(issueAreaStr.lastIndexOf('/') + 1);
			issueNumber = Integer.parseInt(issueStr);
		}
	}
	
	public void setIssueNumber(int issueNumber) {
		this.issueNumber = issueNumber;
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
		if("mf".equals(t))
			return MUST_FIX;
		else if("sf".equals(t))
			return SHOULD_FIX;
		else if("cf".equals(t))
			return CONSIDER_FIX;
		else if("g".equals(t) || "p".equals(t))
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
	
	public int getIssueNumber() {
		return issueNumber;
	}
	
	public String getLink(String login, String repoName) {
		return "https://github.com/" + login + "/" + repoName + "/issues/" + getIssueNumber();
	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		
		try {
			json.put("title", getTitle());
			json.put("tags", getTags());
			json.put("comment", getComment());
			json.put("issueNumber", getIssueNumber());
		} catch(JSONException e) {}
		
		return json;
	}
	
	@Override
	public String toString() {
		return getTitle() + " {" + getTags() + "} " + "[" + getIssueNumber() + "]" + getComment();
	}
	
	public String getContents(String login, String repo) {
		String tagStr = "";
		if(!getTags().isEmpty()) {
			StringBuilder tagsBuilder = new StringBuilder("{");
			for(String tag : getTags()) {
				tagsBuilder.append(tag).append(", ");
			}
			tagStr = tagsBuilder.toString();
			tagStr = tagStr.substring(0, tagStr.length() - 2) + "} ";
		}
		
		String issueStr = "";
		
		if(getIssueNumber() != 0) {
			issueStr = "[[" + getLink(login, repo) + "]] ";
		}
		
		return (tagStr + issueStr + getComment()).trim();
	}
}
