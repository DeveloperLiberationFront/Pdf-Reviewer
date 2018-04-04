package edu.ncsu.dlf.model;

import java.awt.image.BufferedImage;
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

    // TODO: Add more tag types?
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
        tagMap.put("MUST_FIX", Tag.MUST_FIX);
        tagMap.put("sf", Tag.SHOULD_FIX);
        tagMap.put("should-fix", Tag.SHOULD_FIX);
        tagMap.put("should fix", Tag.SHOULD_FIX);
        tagMap.put("shouldfix", Tag.SHOULD_FIX);
        tagMap.put("shouldFix", Tag.SHOULD_FIX);
        tagMap.put("SHOULD_FIX", Tag.SHOULD_FIX);
        tagMap.put("cf", Tag.CONSIDER_FIX);
        tagMap.put("could-fix", Tag.CONSIDER_FIX);
        tagMap.put("could fix", Tag.CONSIDER_FIX);
        tagMap.put("couldfix", Tag.CONSIDER_FIX);
        tagMap.put("couldFix", Tag.CONSIDER_FIX);
        tagMap.put("considerfix", Tag.CONSIDER_FIX);
        tagMap.put("considerFix", Tag.CONSIDER_FIX);
        tagMap.put("CONSIDER_FIX", Tag.CONSIDER_FIX);

        tagMap.put("g", Tag.POSITIVE);
        tagMap.put("good", Tag.POSITIVE);
        tagMap.put("p", Tag.POSITIVE);
        tagMap.put("positive", Tag.POSITIVE);
        tagMap.put("pos", Tag.POSITIVE);
        tagMap.put("plus", Tag.POSITIVE);
        tagMap.put("+", Tag.POSITIVE);
        tagMap.put("POSITIVE", Tag.POSITIVE);
    }

    private List<Tag> tags;

    private String comment;

    private int issueNumber;

    private BufferedImage image;

    private int pageNumber;

    public PdfComment(String string) {
        string = setIssueNumberAndRepairBrokenTags(string);
        setTags(string);
        setComment(string);
    }

    public static List<PdfComment> getNegComments(List<String> comments) {
        List<PdfComment> retVal = new ArrayList<>();
        for (String comment : comments) {
            PdfComment pdfComment = new PdfComment(comment);
            if (!pdfComment.getTags().contains(Tag.POSITIVE)) {
                retVal.add(pdfComment);
            }
        }
        return retVal;
    }

    private void setComment(String comment) {
        int tagsStartPos = comment.indexOf("{{");
        int tagsEndPos = comment.indexOf("}}");
        if (tagsStartPos != -1 && tagsEndPos != -1) {
            String fHalf = comment.substring(0, tagsStartPos).trim();
            String sHalf = comment.substring(tagsEndPos + 2, comment.length()).trim();
            comment = fHalf + ' ' + sHalf;
        }

        int issueStartPos = comment.indexOf("[[");
        int issueEndPos = comment.indexOf("]]");
        if (issueStartPos != -1 && issueEndPos != -1) {
            String fHalf = comment.substring(0, issueStartPos).trim();
            String sHalf = comment.substring(issueEndPos + 2, comment.length()).trim();
            comment = fHalf + ' ' + sHalf;
        }

        this.comment = comment.trim();
    }

    //TODO: Figure out what issue number is supposed to be formatted like?
    private final String setIssueNumberAndRepairBrokenTags(String originalString) {

        int issueStartPos = originalString.indexOf("[[");
        int issueEndPos = originalString.indexOf("]]");
        if (issueStartPos != -1 && issueEndPos != -1) {
            String issueAreaStr = originalString.substring(issueStartPos, issueEndPos + 2);
            String issueStr = issueAreaStr.substring(issueAreaStr.lastIndexOf('/') + 1, issueAreaStr.indexOf("]]"));
            try {
                issueNumber = Integer.parseInt(issueStr);
            } catch (NumberFormatException e) {
                // Hmmm... Someone probably tried to tag something using [[]] instead of {{}}
                // We will try to repair it
                System.out.println("Broken string: " + originalString);
                String fixedString = issueAreaStr.replace("[[", "{{");
                fixedString = fixedString.replace("]]", "}}");
                originalString = originalString.replace(issueAreaStr, fixedString);
                System.out.println("Fixed string: " + originalString);
            }
        }
        return originalString;
    }

    public void setIssueNumber(int issueNumber) {
        this.issueNumber = issueNumber;
    }

    private void setTags(String str) {
        tags = new ArrayList<>();
        int startPos = str.indexOf("{{");
        int endPos = str.indexOf("}}");

        if (startPos != -1 && endPos != -1) {
            String areaStr = str.substring(startPos + 2, endPos);
            String[] tagsStr = areaStr.split(",");
            for (String tag : tagsStr)
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
        String ellipsis = tooLong ? "..." : "";
        return comment.substring(0, end).trim() + ellipsis;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public int getIssueNumber() {
        return issueNumber;
    }

    static String buildLink(Repo repo, int issueNumber) {
        return "https://github.com/" + repo.repoOwner + '/' + repo.repoName + "/issues/" + issueNumber;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {
            json.put("title", getTitle());
            json.put("tags", getTags());
            json.put("comment", getComment());
            json.put("issueNumber", getIssueNumber());
        } catch (JSONException e) {
        }

        return json;
    }

    @Override
    public String toString() {
        return "PdfComment [tags=" + tags + ", comment=" + comment + ", issueNumber=" + issueNumber + ", image="
                + image.getWidth() + 'x' + image.getHeight() + "px]";
    }

    public String getMessageWithLink(Repo repo) {
        String tagStr = "";
        if (!getTags().isEmpty()) {
            StringBuilder tagsBuilder = new StringBuilder("{{");
            for (Tag tag : getTags()) {
                tagsBuilder.append(tag).append(", ");
            }
            tagStr = tagsBuilder.toString();
            tagStr = tagStr.substring(0, tagStr.length() - 2) + "}} ";
        }

        String issueLinkStr = "";

        if (getIssueNumber() != 0) {
            issueLinkStr = "[[" + buildLink(repo, getIssueNumber()) + "]] ";
        }

        return (tagStr + issueLinkStr + getComment()).trim();
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNum) {
        this.pageNumber = pageNum;
    }
}
