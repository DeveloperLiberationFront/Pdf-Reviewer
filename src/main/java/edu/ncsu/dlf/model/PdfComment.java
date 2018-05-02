package edu.ncsu.dlf.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A representation of a PDAnnotation and GitHub issue in the system
 * @author Team19
 */
public class PdfComment {

    public static final int TITLE_LENGTH = 47;

    /**
     * Represent GitHub labels in the system
     * TODO: Add more tag types?
     */
    public enum Tag {
        MUST_FIX, SHOULD_FIX, CONSIDER_FIX, POSITIVE, CUSTOM_TAG
    }

    // Maps the Tag enum to specific string values
    private static Map<String, Tag> tagMap = new HashMap<String, PdfComment.Tag>();

    static {
        tagMap.put("mf", Tag.MUST_FIX);
        tagMap.put("MF", Tag.MUST_FIX);
        tagMap.put("must-fix", Tag.MUST_FIX);
        tagMap.put("must fix", Tag.MUST_FIX);
        tagMap.put("mustfix", Tag.MUST_FIX);
        tagMap.put("mustFix", Tag.MUST_FIX);
        tagMap.put("MUST_FIX", Tag.MUST_FIX);

        tagMap.put("sf", Tag.SHOULD_FIX);
        tagMap.put("SF", Tag.SHOULD_FIX);
        tagMap.put("should-fix", Tag.SHOULD_FIX);
        tagMap.put("should fix", Tag.SHOULD_FIX);
        tagMap.put("shouldfix", Tag.SHOULD_FIX);
        tagMap.put("shouldFix", Tag.SHOULD_FIX);
        tagMap.put("SHOULD_FIX", Tag.SHOULD_FIX);

        tagMap.put("cf", Tag.CONSIDER_FIX);
        tagMap.put("CF", Tag.CONSIDER_FIX);
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

    private String pageNumber;

    /**
     * Represents a PDF annotation that will be converted to GitHub issue.
     * @param string Comment string left by reviewer
     */
    public PdfComment(String string) {
        string = setIssueNumberAndRepairBrokenTags(string);
        setTags(string);
        setComment(string);
    }

    /**
     * Set comment field of the object
     * @param comment Comment string
     */
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

    /**
     * Set the issue number of the comment (if available) and repair broken tags
     * Tags should be {{}} not [[]]
     * @return Updated string to be used in PdfComment
     */
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

    /**
     * Set the issue number field
     */
    public void setIssueNumber(int issueNumber) {
        this.issueNumber = issueNumber;
    }

    /**
     * Set tags of PdfComment given the omment string
     */
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

    /**
     * Return tag value from String using tagMap
     */
    public static Tag getTag(String t) {
        t = t.trim();
        if (tagMap.containsKey(t))
            return tagMap.get(t);
        else
            return Tag.CUSTOM_TAG;
    }

    /**
     * Get comment string of class
     */
    public String getComment() {
        return comment;
    }

    /**
     * @return the first 50 char of the coment string
     */
    public String getTitle() {
        boolean tooLong = comment.length() > TITLE_LENGTH;
        int end = tooLong ? TITLE_LENGTH : comment.length();
        String ellipsis = tooLong ? "..." : "";
        return comment.substring(0, end).trim() + ellipsis;
    }

    /**
     * Get tags as a List
     */
    public List<Tag> getTags() {
        return tags;
    }

    /**
     * Get issue number of the comment
     */
    public int getIssueNumber() {
        return issueNumber;
    }

    /**
     * Build link to issue in repository on GitHub.
     * @param repo Repository on GitHub to host issues
     * @param issueNumber issue number of the comment on GitHub
     */
    static String buildLink(Repo repo, int issueNumber) {
        return "https://github.com/" + repo.repoOwner + '/' + repo.repoName + "/issues/" + issueNumber;
    }

    /**
     * @return PdfComment object as a JSON object
     */
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

    /**
     * @param repo GitHub repository to host issues
     * @return comment message with link to issue in GithUb
     */
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

    public String getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(String pageNum) {
        this.pageNumber = pageNum;
    }
}
