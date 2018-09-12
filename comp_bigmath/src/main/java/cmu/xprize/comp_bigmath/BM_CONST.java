package cmu.xprize.comp_bigmath;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 9/5/18.
 */

public class BM_CONST {

    // action map?
    static final String NEXTNODE = "NEXTNODE";

    // name of digit variables
    public static final String ONE_DIGIT = "one";
    public static final String TEN_DIGIT = "ten";
    public static final String HUN_DIGIT = "hun";

    public static final String TEN_CARRY_DIGIT = "ten_c";
    public static final String HUN_CARRY_DIGIT = "hun_c";

    public static final String ALL_DIGITS = "all";

    // name of row variables
    public static final String OPA_LOCATION = "opA";
    public static final String OPB_LOCATION = "opB";
    public static final String RESULT_LOCATION = "result";

    // class for just features
    public static final class FEATURES {

        // for choosing which type of problem
        public static final String FTR_IS_CARRY = "FTR_IS_CARRY";
        public static final String FTR_IS_BORROW = "FTR_IS_BORROW";

        // for correct vs wrong answers
        public static final String FTR_CORRECT = "FTR_CORRECT";
        public static final String FTR_WRONG = "FTR_WRONG";

        // for when the problem is finished, or has more to do
        public static final String FTR_PROBLEM_DONE = "FTR_PROBLEM_DONE";
        public static final String FTR_PROBLEM_HAS_MORE = "FTR_PROBLEM_HAS_MORE";

        // for when the data source has more problems or not
        public static final String FTR_MORE_PROBLEMS = "FTR_MORE_PROBLEMS";
        public static final String FTR_PROBLEMS_DONE = "FTR_PROBLEMS_DONE";
    }


    // this isn't used, it's just for me to keep track
    public enum NODE_MAP {
        INTRO, NEXT_PROBLEM, NEXT_DIGIT, USER_INPUT, CORRECT_FEEDBACK, WRONG_FEEDBACK, PROBLEM_CORRECT, NEXT_SCENE
    }

    // this isn't used, it's just for me to keep track
    public enum MODULE_MAP {
        INTRO, NEXTPROBLEM, NEXTDIGIT, USERINPUT, CORRECTFEEDBACK, WRONGFEEDBACK, PROBLEMCORRECT
    }

    // this isn't used, it's just for me to keep track
    public enum ACTION_MAP {
        SET_VERSION, SET_DATASOURCE, NEXTNODE, NEXTSCENE
    }


}
