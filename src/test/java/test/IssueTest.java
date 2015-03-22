package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import edu.ncsu.dlf.model.PdfComment;
import edu.ncsu.dlf.model.Repo;
import edu.ncsu.dlf.model.PdfComment.Tag;

import org.junit.Before;
import org.junit.Test;

public class IssueTest {
    String aS;

    String bS;

    String cS;

    String dS;

    String eS;

    String fS;

    String gS;

    String hS;

    String iS;

    PdfComment a;

    PdfComment b;

    PdfComment c;

    PdfComment d;

    PdfComment e;

    PdfComment f;

    PdfComment g;

    PdfComment h;

    PdfComment i;

    private String jS;

    private PdfComment j;

    @Before
    public void setup() {
        aS = "{{tag 1, mf}} [[https://github.com/mpeterson2/Pdf-Test/issues/1]] This is an issue";
        bS = "{{tag 1, mf}}[[https://github.com/mpeterson2/Pdf-Test/issues/2]]This is an issue";
        cS = "This is an [[https://github.com/mpeterson2/Pdf-Test/issues/3]]issue{{tag 1, mf}} ";
        dS = "This is an [[https://github.com/mpeterson2/Pdf-Test/issues/4]]issue {{tag 1, mf}}";
        eS = "This is [[https://github.com/mpeterson2/Pdf-Test/issues/5]]{{tag 1, mf}} an issue";
        fS = "This is [[https://github.com/mpeterson2/Pdf-Test/issues/6]]{{tag 1, mf}}an issue";
        gS = "This is  [[https://github.com/mpeterson2/Pdf-Test/issues/7]] an issue";
        hS = "This is not an [[https://github.com/mpeterson2/Pdf-Test/issues/19]] issue";
        iS = "{{tag, p}} This is a positive issue";
        jS = "This is a really really really really long issue title with an incorrect must-fix tag [[mf]] ";

        a = new PdfComment(aS);
        b = new PdfComment(bS);
        c = new PdfComment(cS);
        d = new PdfComment(dS);
        e = new PdfComment(eS);
        f = new PdfComment(fS);
        g = new PdfComment(gS);
        h = new PdfComment(hS);
        i = new PdfComment(iS);
        j = new PdfComment(jS);
        j.setIssueNumber(8);        //simulating querying github for the issue count
    }   

    @Test
    public void testComment() {
        String aComment = a.getComment();
        assertEquals("This is an issue", aComment);
        assertEquals(aComment, b.getComment());
        assertEquals(aComment, c.getComment());
        assertEquals(aComment, d.getComment());
        assertEquals(aComment, e.getComment());
        assertEquals(aComment, f.getComment());
        assertEquals(aComment, g.getComment());
        assertEquals("This is not an issue", h.getComment());
        assertEquals("This is a really really really really long issue title with an incorrect must-fix tag", j.getComment());
    }

    @Test
    public void testTitle() {
        String aTitle = a.getTitle();
        assertEquals("This is an issue", aTitle);
        assertEquals(aTitle, b.getTitle());
        assertEquals(aTitle, c.getTitle());
        assertEquals(aTitle, d.getTitle());
        assertEquals(aTitle, e.getTitle());
        assertEquals(aTitle, f.getTitle());
        assertEquals(aTitle, g.getTitle());
        assertEquals("This is not an issue", h.getTitle());
        assertEquals("This is a really really really really long issu...", j.getTitle());
    }

    @Test
    public void testTags() {
        assertEquals(2, a.getTags().size());
        assertEquals(1, j.getTags().size());
        assertEquals(Tag.MUST_FIX, j.getTags().get(0));

        assertEquals(Tag.MUST_FIX, PdfComment.getTag("mf"));
        assertEquals(Tag.MUST_FIX, PdfComment.getTag("must-fix"));
        assertEquals(Tag.MUST_FIX, PdfComment.getTag("must fix"));
        assertEquals(Tag.MUST_FIX, PdfComment.getTag("mustfix"));
        assertEquals(Tag.MUST_FIX, PdfComment.getTag("mustFix"));
        assertEquals(Tag.SHOULD_FIX, PdfComment.getTag("sf"));
        assertEquals(Tag.SHOULD_FIX, PdfComment.getTag("should-fix"));
        assertEquals(Tag.SHOULD_FIX, PdfComment.getTag("should fix"));
        assertEquals(Tag.SHOULD_FIX, PdfComment.getTag("shouldfix"));
        assertEquals(Tag.SHOULD_FIX, PdfComment.getTag("shouldFix"));
        assertEquals(Tag.CONSIDER_FIX, PdfComment.getTag("cf"));
        assertEquals(Tag.CONSIDER_FIX, PdfComment.getTag("could-fix"));
        assertEquals(Tag.CONSIDER_FIX, PdfComment.getTag("could fix"));
        assertEquals(Tag.CONSIDER_FIX, PdfComment.getTag("couldFix"));
        assertEquals(Tag.CONSIDER_FIX, PdfComment.getTag("couldfix"));
        assertEquals(Tag.CUSTOM_TAG, PdfComment.getTag("Arbitrary Tag"));
    }

    @Test
    public void testPositiveTags() {
        assertTrue(i.getTags().contains(PdfComment.Tag.POSITIVE));

        assertEquals(Tag.POSITIVE, PdfComment.getTag("g"));
        assertEquals(Tag.POSITIVE, PdfComment.getTag("good"));
        assertEquals(Tag.POSITIVE, PdfComment.getTag("p"));
        assertEquals(Tag.POSITIVE, PdfComment.getTag("positive"));
        assertEquals(Tag.POSITIVE, PdfComment.getTag("plus"));
        assertEquals(Tag.POSITIVE, PdfComment.getTag("pos"));
        assertEquals(Tag.POSITIVE, PdfComment.getTag("+"));
    }

    @Test
    public void testGettingTagsFromStringList() {
        List<String> commentStrList = new ArrayList<>();
        commentStrList.add(aS);
        commentStrList.add(bS);
        commentStrList.add(cS);
        commentStrList.add(dS);
        commentStrList.add(eS);
        commentStrList.add(fS);
        commentStrList.add(gS);
        commentStrList.add(hS);
        commentStrList.add(iS);
        commentStrList.add(jS);

        List<PdfComment> allComments = convertStringsToPDFComments(commentStrList);

        boolean containsPositive = false;
        for (PdfComment com : allComments) {
            if (com.getTags().contains(Tag.POSITIVE))
                containsPositive = true;
        }
        assertTrue(containsPositive);

        List<PdfComment> negComments = PdfComment.getNegComments(commentStrList);

        for (PdfComment com : negComments) {
            assertFalse(com.getTags().contains("Positive"));
        }

    }

    private List<PdfComment> convertStringsToPDFComments(List<String> comments) {
        List<PdfComment> retVal = new ArrayList<>();
        for (String comment : comments) {
            retVal.add(new PdfComment(comment));
        }

        return retVal;
    }

    @Test
    public void testIssueNumber() {
        assertEquals(1, a.getIssueNumber());
        assertEquals(2, b.getIssueNumber());
        assertEquals(3, c.getIssueNumber());
        assertEquals(4, d.getIssueNumber());
        assertEquals(5, e.getIssueNumber());
        assertEquals(6, f.getIssueNumber());
        assertEquals(7, g.getIssueNumber());
        assertEquals(8, j.getIssueNumber());
        assertEquals(19, h.getIssueNumber());
        assertEquals(0, i.getIssueNumber());
    }

    @Test
    public void testContent() {
        Repo repo = new Repo("mpeterson2", "Pdf-Test");
        assertEquals("{{CUSTOM_TAG, MUST_FIX}} [[https://github.com/mpeterson2/Pdf-Test/issues/1]] This is an issue",
                a.getMessageWithLink(repo));
        assertEquals("{{CUSTOM_TAG, MUST_FIX}} [[https://github.com/mpeterson2/Pdf-Test/issues/2]] This is an issue",
                b.getMessageWithLink(repo));
        assertEquals("{{CUSTOM_TAG, MUST_FIX}} [[https://github.com/mpeterson2/Pdf-Test/issues/3]] This is an issue",
                c.getMessageWithLink(repo));
        assertEquals("{{CUSTOM_TAG, MUST_FIX}} [[https://github.com/mpeterson2/Pdf-Test/issues/4]] This is an issue",
                d.getMessageWithLink(repo));
        assertEquals("{{CUSTOM_TAG, MUST_FIX}} [[https://github.com/mpeterson2/Pdf-Test/issues/5]] This is an issue",
                e.getMessageWithLink(repo));
        assertEquals("{{CUSTOM_TAG, MUST_FIX}} [[https://github.com/mpeterson2/Pdf-Test/issues/6]] This is an issue",
                f.getMessageWithLink(repo));
        assertEquals("[[https://github.com/mpeterson2/Pdf-Test/issues/7]] This is an issue", g.getMessageWithLink(repo));
        assertEquals("[[https://github.com/mpeterson2/Pdf-Test/issues/19]] This is not an issue", h.getMessageWithLink(repo));
        assertEquals("{{CUSTOM_TAG, POSITIVE}} This is a positive issue", i.getMessageWithLink(repo));
        assertEquals("{{MUST_FIX}} [[https://github.com/mpeterson2/Pdf-Test/issues/8]] This is a really really really really long issue title with an incorrect must-fix tag", j.getMessageWithLink(repo));
        
    }
}
