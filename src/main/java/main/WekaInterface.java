package main;

import com.opencsv.CSVWriter;
import utils.AttributeSelectionType;
import utils.ClassifierType;
import utils.SamplingType;
import utils.SensitivityType;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.supervised.instance.SMOTE;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;

public class WekaInterface {


    private String projectName;
    private String projectFolder;
    private String outputFolder;
    private String trainingString="training";
    private String testingString="testing";
    private String arff=".arff";
    private ClassifierType classifier;
    private SamplingType sampling;
    private SensitivityType sensitivity;
    private AttributeSelectionType attributeSelection;


    public WekaInterface(String projectFolder, String outputFolder, String projectName) throws IOException {
        this.projectFolder=projectFolder;
        this.outputFolder=outputFolder;
        this.projectName=projectName;
        outputRow(null, true);
    }


    public void executeRun() throws Exception {

        DataSource source1;
        DataSource source2;
        AnalysisRow row;
        int counter=5;
        int traingBuggy;
        int testBuggy;
        Evaluation eval;



        do {
            source1 = new DataSource(this.projectFolder+this.trainingString+counter+this.arff);
            Instances training = source1.getDataSet();
            source2 = new DataSource(this.projectFolder+this.testingString+counter+this.arff);
            Instances testing = source2.getDataSet();

            if((traingBuggy=getNumBuggy(training))==0) {
                counter++;
                continue;
            }
            testBuggy=getNumBuggy(testing);

            int numAttr = training.numAttributes();
            training.setClassIndex(numAttr - 1);
            testing.setClassIndex(numAttr - 1);

            if(this.sampling==SamplingType.SMOTE){
                SMOTE smote=new SMOTE();
                String[] opts = new String[]{ "-C", "0", "-K", "5", "-P", "100.0", "-S", "1" };
                smote.setOptions(opts);
                smote.setInputFormat(training);
                Instances newTraining= Filter.useFilter(training, smote);
                training=newTraining;
            }


            eval=getEvaluation(training, testing);


            row=getRow(eval, counter, training, testing, traingBuggy, testBuggy);
            outputRow(row, false);
            counter++;
        }while(new File(this.projectFolder+this.trainingString+counter+this.arff).isFile());

    }

    private AnalysisRow getRow(Evaluation eval, int iter, Instances train, Instances test, int trainBuggy, int testBuggy){

        double trainPerc=(double)train.numInstances()/(train.numInstances()+test.numInstances());
        double trainDefPerc=(double)trainBuggy/train.numInstances();
        double testDefPerc=(double)testBuggy/test.numInstances();
        int decimal=2;
        double auc=eval.areaUnderROC(1);
        double recall=eval.recall(1);
        double precision=eval.precision(1);
        double kappa=eval.kappa();

        AnalysisRow row= new AnalysisRow();

        row.setPrecision(getPercentage(precision, decimal+2));
        row.setAuc(getPercentage(auc, decimal+2));
        row.setRecall(getPercentage(recall, decimal+2));
        row.setKappa(getPercentage(kappa, decimal+2));
        row.setDataset(this.projectName);
        row.setTrainingRelease(iter);
        row.setTrainingPercentage(getPercentage(trainPerc,decimal));
        row.setTrainDefectivePerc(getPercentage(trainDefPerc,decimal));
        row.setTestDefectivePerc(getPercentage(testDefPerc,decimal));
        row.setClassifier(this.getClassifier().toString());
        row.setBalancing(this.sampling.toString());
        row.setSensitivity(this.sensitivity.toString());
        row.setFeatureSelection(this.attributeSelection.toString());

        row.setFp((int)eval.numFalsePositives(1));
        row.setFn((int)eval.numFalseNegatives(1));
        row.setTp((int)eval.numTruePositives(1));
        row.setTn((int)eval.numTrueNegatives(1));
        return row;
    }


    private double getPercentage(double perc, int cut){
        perc = perc * Math.pow((double)10, cut+2);
        perc = Math.floor(perc);
        perc = perc / Math.pow(10, cut);
        return perc;
    }

    private int getNumBuggy(Instances insts){
        int num=0;

        Enumeration<Instance> instEnum = insts.enumerateInstances();
        while (instEnum.hasMoreElements()) {
            Instance inst = instEnum.nextElement();
            if(inst.stringValue(insts.numAttributes()-1).equals("Yes")) num++;
        }

        return num;
    }

    private void outputRow(AnalysisRow row, boolean header) throws IOException {

        File file;

        file=new File(this.outputFolder+"\\output.csv");
        if(file.createNewFile()){  //skip
        }

        FileWriter outputfile = new FileWriter(file, true);

        CSVWriter writer = new CSVWriter(outputfile);

        if(header) writer.writeNext(AnalysisRow.getHeader());
        else writer.writeNext(row.getRow());

        writer.flush();
        writer.close();
    }

    //dummy
    private Evaluation getEvaluation(Instances training, Instances testing) throws Exception {
        Classifier classif;
        classif=getSamplingClassifier();
        Evaluation eval;

        if(this.attributeSelection!=AttributeSelectionType.NONE) {
            Instances[] inst=doAttrSelection(training, testing);
            training=inst[0];
            testing=inst[1];
        }

        classif.buildClassifier(training);

        if(this.sensitivity!=SensitivityType.NONE) eval=new Evaluation(testing, createCostMatrix());
        else eval=new Evaluation(testing);
        eval.evaluateModel(classif, testing);
        return eval;
    }

    private Classifier getSamplingClassifier() throws Exception {

        Classifier classif=getCorrectClassifier();
        Classifier nfc;
        FilteredClassifier fc = new FilteredClassifier();
        SpreadSubsample spreadSubsample = new SpreadSubsample();
        Resample resample = new Resample();
        String[] opts;
        switch(this.sampling){
            case NONE: break;
            case OVERSAMPLING :
                fc.setClassifier(classif);
                opts = new String[]{ "-B", "1.0", "-Z", "130.3" };
                resample.setOptions(opts);
                fc.setFilter(resample);
                nfc=getSensitivityClassifier(fc);
                return nfc;
            case UNDERSAMPLING:
                fc.setClassifier(classif);
                opts = new String[]{ "-M", "1.0"};
                spreadSubsample.setOptions(opts);
                fc.setFilter(spreadSubsample);
                nfc=getSensitivityClassifier(fc);
                return nfc;
            case SMOTE: break;
            default: break;
        }
        return getSensitivityClassifier(fc);
    }

    private Classifier getSensitivityClassifier(Classifier cf){
        CostSensitiveClassifier csf= new CostSensitiveClassifier();
        csf.setClassifier(cf);
        csf.setCostMatrix(createCostMatrix());
        switch(this.sensitivity){

            case SENSITIVELEARNING: csf.setMinimizeExpectedCost(false);
            return csf;

            case SENSITIVETHRESHOLD: csf.setMinimizeExpectedCost(true);
            return csf;

            default: return cf;
        }
    }

    private CostMatrix createCostMatrix() {
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0, 10.0);
        costMatrix.setCell(0, 1, 1.0);
        costMatrix.setCell(1, 1, 0.0);
        return costMatrix;
    }


    private Classifier getCorrectClassifier(){
        Classifier classif;
        switch(this.classifier){

            case RANDOMFOREST : classif= new RandomForest();
                break;

            case IBK: classif= new IBk();
                break;

            case NAIVEBAYES: classif= new NaiveBayes();
                break;

            default: return null;
        }
        return classif;
    }


    private Instances[] doAttrSelection(Instances training, Instances testing) throws Exception {

        AttributeSelection filter = new AttributeSelection();

        switch(this.attributeSelection){
            case BESTFIRST : BestFirst search = new BestFirst();
                filter.setSearch(search);
                break;
            case BACKWARDSEARCH:
                GreedyStepwise search1 = new GreedyStepwise();
                //set the algorithm to search backward
                search1.setSearchBackwards(true);
                filter.setSearch(search1);
                break;
            case FORWARDSEARCH:
                GreedyStepwise search2 = new GreedyStepwise();
                //set the algorithm to search backward
                search2.setSearchBackwards(false);
                filter.setSearch(search2);
                break;
            default: return new Instances[]{};
        }
        //create evaluator and search algorithm objects
        CfsSubsetEval eval = new CfsSubsetEval();


        //set the algorithm to search backward

        //set the filter to use the evaluator and search algorithm
        filter.setEvaluator(eval);


        //specify the dataset
        filter.setInputFormat(training);

        //apply
        Instances filteredTraining = Filter.useFilter(training, filter);
        filteredTraining.setClassIndex(filteredTraining.numAttributes()-1);
        Instances filteredTesting = Filter.useFilter(testing, filter);
        filteredTesting.setClassIndex(filteredTesting.numAttributes() - 1);
        return new Instances[]{filteredTraining,filteredTesting};

    }

    public ClassifierType getClassifier() {
        return classifier;
    }

    public void setClassifier(ClassifierType classifier) {
        this.classifier = classifier;
    }

    public SamplingType getSampling() {
        return sampling;
    }

    public void setSampling(SamplingType sampling) {
        this.sampling = sampling;
    }

    public SensitivityType getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(SensitivityType sensitivity) {
        this.sensitivity = sensitivity;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectFolder() {
        return projectFolder;
    }

    public void setProjectFolder(String projectFolder) {
        this.projectFolder = projectFolder;
    }

    public AttributeSelectionType getAttributeSelection() {
        return attributeSelection;
    }

    public void setAttributeSelection(AttributeSelectionType attributeSelection) {
        this.attributeSelection = attributeSelection;
    }

}
