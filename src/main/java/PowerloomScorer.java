/* Import statements for working with PowerLoom. */

import edu.isi.powerloom.*;
import edu.isi.powerloom.logic.*;
import edu.isi.stella.Module;
import edu.isi.stella.javalib.*;
import edu.isi.stella.Stella_Object;

import java.io.File;
import java.util.List;

public class PowerloomScorer extends Scorer {

    //private static String kbfileDefault = "business.plm";
    //private static String kbdirDefault = "kbs";
    //private static String workingModule = "BUSINESS";

    @Override
    public double getScore(Entity entity, Document doc) {
        return 0;
    }

    private static void loadVerbosely (String filename) {
        System.out.print("  Loading " + filename + " ...");
        PLI.load(filename, null);
        System.out.println("  done.");
    }

    @Override
    public void initializeScorer(Document document) {
        System.out.println("Testing PowerLoom Wrapper Class");
        PowerLoomUtils plu = new PowerLoomUtils();
        //plu.load("src/main/resources/powerloom_models/business.plm");
        //plu.changeModule("BUSINESS");
        // Note: query strings broken over multiple lines
        // for better formatting in book example section:
        plu.assertProposition(
                "(and (company c1)" +
                        "     (company-name c1 \"Moms Grocery\"))");
        plu.assertProposition(
                "(and (company c2)" +
                        "     (company-name c2 \"IBM\"))");
        plu.assertProposition(
                "(and (company c3)" +
                        "     (company-name c3 \"Apple\"))");
        List answers = plu.doQuery("all ?x (company ?x)");
        System.out.println(answers);
        answers = plu.doQuery(
                "all (?x ?name)" +
                        "    (and" +
                        "      (company ?x)" +
                        "      (company-name ?x ?name))");
        System.out.println(answers);
        plu.createRelation("CEO", 2);
        plu.assertProposition(
                "(CEO \"Apple\" \"SteveJobs\")");
        answers = plu.doQuery(
                "all (?x ?name ?ceo)" +
                        "    (and" +
                        "      (company-name ?x ?name)" +
                        "      (CEO ?name ?ceo))");
        System.out.println(answers);
    }
}
