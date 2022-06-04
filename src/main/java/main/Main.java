package main;

import utils.AttributeSelectionType;
import utils.ClassifierType;
import utils.SamplingType;
import utils.SensitivityType;

public class Main {

    public static void main(String[] args) throws Exception {

        String folder="C:\\Users\\39320\\Desktop\\Corsi\\isw2\\data\\progetto\\";
        WekaInterface analyser = new WekaInterface(folder+"bk\\", folder+"output", "BOOKKEEPER");
        callForAnyClassifType(analyser);

        analyser.setProjectFolder(folder+"storm\\");
        analyser.setProjectName("STORM");
        callForAnyClassifType(analyser);

    }

    private static void callForAnyClassifType(WekaInterface analyser) throws Exception {
        analyser.setClassifier(ClassifierType.RANDOMFOREST);
        callerClassif(analyser);

        analyser.setClassifier(ClassifierType.NAIVEBAYES);
        callerClassif(analyser);

        analyser.setClassifier(ClassifierType.IBK);
        callerClassif(analyser);
    }

    private static void callerClassif(WekaInterface wi) throws Exception {
        wi.setAttributeSelection(AttributeSelectionType.NONE);
        callAfterAttrSel(wi);
        wi.setAttributeSelection(AttributeSelectionType.BESTFIRST);
        callAfterAttrSel(wi);
        wi.setAttributeSelection(AttributeSelectionType.BACKWARDSEARCH);
        callAfterAttrSel(wi);
        wi.setAttributeSelection(AttributeSelectionType.FORWARDSEARCH);
        callAfterAttrSel(wi);
    }

    private static void callAfterAttrSel(WekaInterface wi) throws Exception {
        wi.setSensitivity(SensitivityType.NONE);
        callerRest(wi);
        wi.setSensitivity(SensitivityType.SENSITIVELEARNING);
        callerRest(wi);
        wi.setSensitivity(SensitivityType.SENSITIVETHRESHOLD);
        callerRest(wi);
    }

    private static void callerRest(WekaInterface wi) throws Exception {
        wi.setSampling(SamplingType.NONE);
        wi.executeRun();

        wi.setSampling(SamplingType.UNDERSAMPLING);
        wi.executeRun();

        wi.setSampling(SamplingType.OVERSAMPLING);
        wi.executeRun();

        wi.setSampling(SamplingType.SMOTE);
        wi.executeRun();
    }
}
