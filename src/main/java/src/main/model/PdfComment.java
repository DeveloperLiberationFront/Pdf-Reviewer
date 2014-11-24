package src.main.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses comments in pdf to a more rich data type.
 *
 */
public class PdfComment {

	public static final int TITLE_LENGTH = 47;
	
	public enum Tag {
	    MUST_FIX, SHOULD_FIX, CONSIDER_FIX, POSITIVE, CUSTOM_TAG
	}
	
	private static Map<String, Tag> tagMap = new HashMap<String, PdfComment.Tag>();
	
	static {
	    tagMap.put("mf", Tag.MUST_FIX);
	    tagMap.put("must-fix", Tag.MUST_FIX);
	    tagMap.put("must fix", Tag.MUST_FIX);
	    tagMap.put("mustfix", Tag.MUST_FIX);
	    tagMap.put("mustFix", Tag.MUST_FIX);
	    tagMap.put("sf", Tag.SHOULD_FIX);
        tagMap.put("should-fix", Tag.SHOULD_FIX);
        tagMap.put("should fix", Tag.SHOULD_FIX);
        tagMap.put("shouldfix", Tag.SHOULD_FIX);
        tagMap.put("shouldFix", Tag.SHOULD_FIX);
        tagMap.put("cf", Tag.CONSIDER_FIX);
        tagMap.put("could-fix", Tag.CONSIDER_FIX);
        tagMap.put("could fix", Tag.CONSIDER_FIX);
        tagMap.put("couldfix", Tag.CONSIDER_FIX);
        tagMap.put("couldFix", Tag.CONSIDER_FIX);
        tagMap.put("considerfix", Tag.CONSIDER_FIX);
        tagMap.put("considerFix", Tag.CONSIDER_FIX);
        
        tagMap.put("g", Tag.POSITIVE);
        tagMap.put("good", Tag.POSITIVE);
        tagMap.put("p", Tag.POSITIVE);
        tagMap.put("positive", Tag.POSITIVE);
        tagMap.put("pos", Tag.POSITIVE);
        tagMap.put("plus", Tag.POSITIVE);
        tagMap.put("+", Tag.POSITIVE);
	}
	
	private List<Tag> tags;
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
			if(!pdfComment.getTags().contains(Tag.POSITIVE)) {
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
		
		int tagsStartPos = comment.indexOf("{{");
		int tagsEndPos = comment.indexOf("}}");
		if(tagsStartPos != -1 && tagsEndPos != -1) {
			String fHalf = comment.substring(0, tagsStartPos).trim();
			String sHalf = comment.substring(tagsEndPos + 2, comment.length()).trim();
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
	
	public final void setIssueNumber(String str) {
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
	
	private void setTags(String str) {
		tags = new ArrayList<>();
		int startPos = str.indexOf("{{");
		int endPos = str.indexOf("}}");
		
		if(startPos != -1 && endPos != -1) {
			String areaStr = str.substring(startPos + 2, endPos);
			String[] tagsStr = areaStr.split(",");
			for(String tag : tagsStr)
				tags.add(getTag(tag));
		}
	}
	
	public static Tag getTag(String t) {
		t = t.trim();
		if (tagMap.containsKey(t))
		    return tagMap.get(t);
		else
			return Tag.CUSTOM_TAG;
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
	
	public List<Tag> getTags() {
		return tags;
	}
	
	public int getIssueNumber() {
		return issueNumber;
	}
	
	public String buildLink(String repoOwner, String repoName) {
		return "https://github.com/" + repoOwner + "/" + repoName + "/issues/" + getIssueNumber();
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
	
	public String getMessageWithLink(String repoOwner, String repo) {
		String tagStr = "";
		if(!getTags().isEmpty()) {
			StringBuilder tagsBuilder = new StringBuilder("{{");
			for(Tag tag : getTags()) {
				tagsBuilder.append(tag).append(", ");
			}
			tagStr = tagsBuilder.toString();
			tagStr = tagStr.substring(0, tagStr.length() - 2) + "}} ";
		}
		
		String issueLinkStr = "";
		
		if(getIssueNumber() != 0) {
			issueLinkStr = "[[" + buildLink(repoOwner, repo) + "]] ";
		}
		
		return (tagStr + issueLinkStr + getComment()).trim();
	}
}
