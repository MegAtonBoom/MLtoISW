package main;

public class AnalysisRow {



    //the open-source project of wich the data is
    private String dataset;

    //basically the walk-forward iteration
    private int trainingRelease;

    //% training data (training data/ total data avaiable at that iteration)
    private double trainingPercentage;

    //% of buggy classes on training set
    private double trainDefectivePerc;

    //% of buggy classes on testing set
    private double testDefectivePerc;

    //% used classifier
    private String classifier;

    //used balancing tecnique
    private String balancing;

    //used feature selection tecnique
    private String featureSelection;

    //used sensitivity tecnique
    private String sensitivity;

    //true positives- true negatives- false posditives- false negatives
    private int tp;
    private int tn;
    private int fp;
    private int fn;

    private double precision;
    private double recall;
    private double auc;
    private double kappa;

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public int getTrainingRelease() {
        return trainingRelease;
    }

    public void setTrainingRelease(int trainingRelease) {
        this.trainingRelease = trainingRelease;
    }

    public double getTrainingPercentage() {
        return trainingPercentage;
    }

    public void setTrainingPercentage(double trainingPercentage) {
        this.trainingPercentage = trainingPercentage;
    }


    public double getTrainDefectivePerc() {
        return trainDefectivePerc;
    }

    public void setTrainDefectivePerc(double trainDefectivePerc) {
        this.trainDefectivePerc = trainDefectivePerc;
    }

    public double getTestDefectivePerc() {
        return testDefectivePerc;
    }

    public void setTestDefectivePerc(double testDefectivePerc) {
        this.testDefectivePerc = testDefectivePerc;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getBalancing() {
        return balancing;
    }

    public void setBalancing(String balancing) {
        this.balancing = balancing;
    }

    public String getFeatureSelection() {
        return featureSelection;
    }

    public void setFeatureSelection(String featureSelection) {
        this.featureSelection = featureSelection;
    }

    public String getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(String sensitivity) {
        this.sensitivity = sensitivity;
    }

    public int getTp() {
        return tp;
    }

    public void setTp(int tp) {
        this.tp = tp;
    }

    public int getFp() {
        return fp;
    }

    public void setFp(int fp) {
        this.fp =fp;
    }

    public int getTn() {
        return tn;
    }

    public void setTn(int tn) {
        this.tn = tn;
    }

    public int getFn() {
        return fn;
    }

    public void setFn(int fn) {
        this.fn = fn;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        return recall;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public double getAuc() {
        return auc;
    }

    public void setAuc(double auc) {
        this.auc = auc;
    }

    public double getKappa() {
        return kappa;
    }

    public void setKappa(double kappa) {
        this.kappa = kappa;
    }

    // dataset, #TrainingRelease, %training (data on training / total data), %Defective in training,
    //%Defective in testing, classifier, balancing, Feature Selection, Sensitivity, TP, FP, TN, FN, Precision,
    //Recall, AUC, Kappa.

    public String[] getRow(){
        return new String[]{
                this.dataset,
                ""+this.trainingRelease,
                ""+this.trainingPercentage,
                ""+this.trainDefectivePerc,
                ""+this.testDefectivePerc,
                ""+this.classifier,
                ""+this.balancing,
                ""+this.featureSelection,
                ""+this.sensitivity,
                ""+this.tp, ""+this.fp, ""+this.tn, ""+this.fn,
                ""+this.precision,
                ""+this.recall,
                ""+this.auc,
                ""+this.kappa
        };
    }

    public static String[] getHeader(){
        return new String[]{
                "dataset",
                "training_release",
                "%training",
                "%defective_in_training",
                "%defective_inTtesting",
                "classifier",
                "balancing_tecn",
                "feature_sel_tecn",
                "sensitivity",
                "TP",
                "FP",
                "TN",
                "FN",
                "precision",
                "recall",
                "auc",
                "kappa"
        };
    }

}
