package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.swing.*;
import java.io.*;
import java.util.*;

public class mainController {

        ReadFile readFile;
        Parse parser;
        Indexer indexer;
        static boolean dictionaryWithStemming = false;
        ObservableList<String> languagesBoxOptions = FXCollections.observableArrayList("English","French","Vietnamese","Rusian","Albanian","Polish","Latvian","Lithuanian","Tamil",
        "Indonesian","Arabic","Kirundi","German","Tigrigna","Slovenian","Malay","Cambodia","Spanish","Norwegian","Bengali","Japanse", "Amharic", "Ukrainian", "Czech", "Macedonian", "Chinese", "Italian", "Slovene",
                "Swedish", "Korean", "Danish", "Hungarian" , "Afrikaans", "Turkish", "Kazakh", "Georgian", "Hindi", "Bulgarian", "Hebrew", "Kinyarwanda", "Thai", "International", "Cambodian", "Tagalog"
        ,"Burmese","Urdu", "Spansih", "Swahili", "Belarusian","Persian","Slovak","Malagasy", "Azeri", "Cantonese" , "Portuguese", "Greek", "Russian", "Parentheses" );

        @FXML
        private TextField corpusPath;
        @FXML
        private TextField diskPath;
        @FXML
        private CheckBox stemmerCheckBox;
        @FXML
        private ChoiceBox languagesBox;

        /**
         * the function opening the searching window
         * if it's necessary to create inverted-index, readFile parser and index objects are created
         * @param actionEvent
         * @throws IOException
         */
        public void StartEngine(ActionEvent actionEvent) throws IOException {

            //checking if there is a dictionary in the system by checking termsInCorpus map
            //if(Indexer.termsCorpusMap == null) {

                //extracting corpusPath from the UI
                String pathToCorpus = corpusPath.getText();

                //extracting stemmer selection from the UI
                boolean stemmerSelection = stemmerCheckBox.isSelected();

                //extracting disk path from the UI and changing it according to stemmer checkbox selection
                String pathToDisk = diskPath.getText();

                //if the user didn't insert one of the paths - display error alert
                if(pathToCorpus.equals("") || pathToDisk.equals(""))
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    //alert.setTitle("Information Dialog");
                    alert.setHeaderText("Please check that you fill all the fields");
                    //alert.setContentText("s");
                    alert.showAndWait();
                    return;
                }
                //starting creating the inverted-index
                try {
                    //creating readFile with the corpus path
                    readFile = new ReadFile(pathToCorpus);
                }
                //if the folder of the corpus wasn't found - diaplay alert
                catch(NullPointerException e)
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    //alert.setTitle("Information Dialog");
                    alert.setHeaderText("The corpus wasn't found. Please enter your corpus' path again");
                    //alert.setContentText("s");
                    alert.showAndWait();
                    return;
                }

                //creating parser with path to stop words file (it's place is in the corpus' folder) and the stemmer selection
                //in order to know if to use stemming
                parser = new Parse( stemmerSelection);
                parser.LoadStopWordsList(pathToCorpus + "\\stop_words.txt");

                //creating index and sending to his constructor readfile object, parser object and disk path
                indexer = new Indexer(readFile, parser, pathToDisk);

                //starting thread that run the index's function play, which creates the inverted index
                Thread indexerThread = new Thread() {
                    public void run() {
                        try {
                            indexer.Play();
                        } catch (IOException e) {

                        }
                    }
                };

                 indexerThread.start();


                try { indexerThread.join(); }
                catch (InterruptedException e) { e.printStackTrace(); }

                //displaying statistics msg with the end of the creating of the inverted index
                if(!Indexer.hasException)
                {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("Just for you to know");
                    alert.setContentText("The number of documents that were indexed is: " + indexer.docsCorpusMap.size() +
                            ". The number of terms that were found is: " + Indexer.termsCorpusMap.size() + ". We found " +
                            "this data in: " + (System.nanoTime() - Indexer.startTime) / 1000000000.0 + " sec.");
                    alert.showAndWait();
                    Indexer.startTime = 0;
                }
                else
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    //alert.setTitle("Information Dialog");
                    alert.setHeaderText("Oops! the posting files folder wasn't found. Please try again");
                    //alert.setContentText("s");
                    alert.showAndWait();
                }
        }


        /**
         * file choose for choosing corpus path
         * @param event
         */
        public void BrowseCorpusPath(ActionEvent event) {
            JFileChooser chooser = new JFileChooser();
            //chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("Choose Corpus' Folder");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                corpusPath.setText("" + chooser.getSelectedFile());
        }

        /**
         * file choose for choosing disk path
         * @param event
         */
        public void browseDiskPath(ActionEvent event) {

            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("Choose Disk's Folder");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                diskPath.setText("" + chooser.getSelectedFile());
        }

        /**
         * restarting the system
         * @param event
         */
        public void Reset(ActionEvent event) {


                String pathToDisk = diskPath.getText();
                String postingFolderPath;
                String dictionaryFilePath;
                String corpusCitiesPath;
                String corpusDocsPath;
                String IDsPath;
                boolean stemmerSelection = stemmerCheckBox.isSelected();

                if(pathToDisk.equals(""))
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("please fill the disk path first");
                    alert.showAndWait();
                    return;
                }

                if(stemmerSelection) {
                    postingFolderPath = pathToDisk + "\\withStemming";
                    dictionaryFilePath = pathToDisk + "\\dictionaryWithStemming";
                    corpusCitiesPath = pathToDisk + "\\CorpusCitiesWithStemming";
                    corpusDocsPath = pathToDisk + "\\CorpusDocsWithStemming";
                    IDsPath = pathToDisk + "\\IDsDocsWithStemming";
                }
                else {
                    postingFolderPath = pathToDisk + "\\withoutStemming";
                    dictionaryFilePath = pathToDisk + "\\dictionaryWithoutStemming";
                    corpusCitiesPath = pathToDisk + "\\CorpusCitiesWithoutStemming";
                    corpusDocsPath = pathToDisk + "\\CorpusDocsWithoutStemming";
                    IDsPath = pathToDisk + "\\IDsDocsWithoutStemming";

                }

                //delete dictionary file
                File dictionaryFile = new File(dictionaryFilePath);
                if(!dictionaryFile.exists())
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("one of the files does not exists in the specified folder");
                    alert.showAndWait();
                    return;
                }
                File citiesFile = new File(corpusCitiesPath);
                if(!citiesFile.exists())
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("one of the files does not exists in the specified folder");
                    alert.showAndWait();
                    return;
                }
                File docsFile = new File(corpusDocsPath);
                if(!docsFile.exists())
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("one of the files does not exists in the specified folder");
                    alert.showAndWait();
                    return;
                }
                File IDsFile = new File(IDsPath);
                if(!IDsFile.exists())
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("one of the files does not exists in the specified folder");
                    alert.showAndWait();
                    return;
                }
                dictionaryFile.delete();
                citiesFile.delete();
                docsFile.delete();
                IDsFile.delete();

                //delete posting files
                File postingFilesDirectory = new File(postingFolderPath);
                if(postingFilesDirectory.listFiles() != null) {
                    for (File file : postingFilesDirectory.listFiles())
                        if (!file.isDirectory())
                            file.delete();
                }

                postingFilesDirectory.delete();

                //null in parameters on memory
                readFile = null;
                parser = null;
                indexer = null;
                corpusPath.setText("");
                diskPath.setText("");
                System.gc(); // calling the garbage collector
                Indexer.termsCorpusMap = null;
                Indexer.citiesInCorpus = new HashMap<>();
                Indexer.docsAndIDs = new HashMap<>();
                Indexer.NumberOfDocsInCorpus = 0;
                Indexer.citiesInAPI = new HashMap<>();
                Indexer.hasException = false;
                Parse.stopWordsList = new HashSet<>();
                Parse.monthsNames = new HashSet<>();



                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Reset engine was succeeded");
                alert.showAndWait();
            }

        /**
         * displaying the dictionary by term and frequency columns
         * @param event
         * @throws IOException
         */
        public void displayDictionary(ActionEvent event) throws IOException {


                if (Indexer.termsCorpusMap != null) {


                    TableColumn<TermDataInMap, String> termColumn = new TableColumn<>("Term");
                    termColumn.setMinWidth(300);
                    termColumn.setCellValueFactory(new PropertyValueFactory<>("term"));
                    TableColumn<TermDataInMap, Integer> freqCoulmn = new TableColumn<>("Frequency In Dictionary");
                    freqCoulmn.setMinWidth(300);
                    freqCoulmn.setCellValueFactory(new PropertyValueFactory<>("totalTf"));


                    TableView<TermDataInMap> table = new TableView<>();
                    table.setItems(getTermAndFrequency());
                    table.getColumns().addAll(termColumn, freqCoulmn);

                    VBox vbox = new VBox();

                    vbox.getChildren().addAll(table);

                    FXMLLoader fxmlLoader = new FXMLLoader();
                    Parent root1 = fxmlLoader.load(getClass().getResource("/dictionaryWindow.fxml").openStream());
                    Stage stage = new Stage();
                    //stage.initModality(Modality.APPLICATION_MODAL);
                    //stage.setTitle("displayDictionaryWindow");
                    stage.setScene(new Scene(vbox));

                    stage.show();

                }
                else
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    //alert.setTitle("Information Dialog");
                    alert.setHeaderText("We couldn't find your dictionary :(" );
                    //alert.setContentText("s");
                    alert.showAndWait();
                }
            }

        public ObservableList<TermDataInMap> getTermAndFrequency()
        {
            TreeMap<String, TermDataInMap> sortingTermsInCorpus = new TreeMap<>();
            sortingTermsInCorpus.putAll(Indexer.termsCorpusMap);
            ObservableList<TermDataInMap> frequencies = FXCollections.observableArrayList();

            for(String term: Indexer.termsCorpusMap.keySet()) {
                frequencies.add(Indexer.termsCorpusMap.get(term));
            }
            return frequencies;

        }

        private void LoadCorpusMap() throws FileNotFoundException {
            boolean stemmerSelection = stemmerCheckBox.isSelected();
            String pathToDisk = diskPath.getText();
            File dictionaryFile;
            if (stemmerSelection)
                dictionaryFile = new File(pathToDisk + "\\dictionaryWithStemming");
            else
                dictionaryFile = new File(pathToDisk + "\\dictionaryWithoutStemming");

            FileInputStream fileStreamer;
            fileStreamer = new FileInputStream(dictionaryFile);

            ObjectInputStream objectStreamer = null;
            try {
                objectStreamer = new ObjectInputStream(fileStreamer); }
                catch (IOException e) { e.printStackTrace(); }
            try {Indexer.termsCorpusMap = (Map<String, TermDataInMap>) objectStreamer.readObject();}
            catch (IOException e) { e.printStackTrace(); }
            catch (ClassNotFoundException e) { e.printStackTrace();}

            try
            {
                objectStreamer.close();
                fileStreamer.close();
            }
            catch (IOException e) { e.printStackTrace();}
        }


        private void LoadDocsMap() throws FileNotFoundException {
            boolean stemmerSelection = stemmerCheckBox.isSelected();
            String pathToDisk = diskPath.getText();
            File dictionaryFile;
            if (stemmerSelection)
                dictionaryFile = new File(pathToDisk + "\\CorpusDocsWithStemming");
            else
                dictionaryFile = new File(pathToDisk + "\\CorpusDocsWithoutStemming");

            FileInputStream fileStreamer;
            fileStreamer = new FileInputStream(dictionaryFile);

            ObjectInputStream objectStreamer = null;
            try {
                objectStreamer = new ObjectInputStream(fileStreamer); }
            catch (IOException e) { e.printStackTrace(); }
            try {Indexer.docsCorpusMap = (Map<String, DocTermDataInMap>) objectStreamer.readObject();}
            catch (IOException e) { e.printStackTrace(); }
            catch (ClassNotFoundException e) { e.printStackTrace();}

            try
            {
                objectStreamer.close();
                fileStreamer.close();
            }
            catch (IOException e) { e.printStackTrace();}
        }

        private void LoadCitiesMap() throws FileNotFoundException {
            boolean stemmerSelection = stemmerCheckBox.isSelected();
            String pathToDisk = diskPath.getText();
            File dictionaryFile;
            if (stemmerSelection)
                dictionaryFile = new File(pathToDisk + "\\CorpusCitiesWithStemming");
            else
                dictionaryFile = new File(pathToDisk + "\\CorpusCitiesWithoutStemming");

            FileInputStream fileStreamer;
            fileStreamer = new FileInputStream(dictionaryFile);


            ObjectInputStream objectStreamer = null;
            try {
                objectStreamer = new ObjectInputStream(fileStreamer); }
            catch (IOException e) { e.printStackTrace(); }

            try {Indexer.citiesInCorpus = (Map<String, CityInMap>) objectStreamer.readObject(); }
            catch (IOException e) { e.printStackTrace(); }
            catch (ClassNotFoundException e) { e.printStackTrace();}

            try
            {
                objectStreamer.close();
                fileStreamer.close();
            }
            catch (IOException e) { e.printStackTrace();}
        }

        private void LoadIDsMap() throws FileNotFoundException {
            boolean stemmerSelection = stemmerCheckBox.isSelected();
            String pathToDisk = diskPath.getText();
            File dictionaryFile;
            if (stemmerSelection)
                dictionaryFile = new File(pathToDisk + "\\IDsDocsWithStemming");
            else
                dictionaryFile = new File(pathToDisk + "\\IDsDocsWithoutStemming");

            FileInputStream fileStreamer;
            fileStreamer = new FileInputStream(dictionaryFile);

            ObjectInputStream objectStreamer = null;
            try {
                objectStreamer = new ObjectInputStream(fileStreamer); }
            catch (IOException e) { e.printStackTrace(); }

            try {Indexer.docsAndIDs = (Map<Integer, String>) objectStreamer.readObject();}
            catch (IOException e) { e.printStackTrace(); }
            catch (ClassNotFoundException e) { e.printStackTrace();}

            try
            {
                objectStreamer.close();
                fileStreamer.close();
            }
            catch (IOException e) { e.printStackTrace();}

        }

        /**
         * loading our 4 maps into the memory: termCorpusMap, docsCorpusMap, citiesCorpusMap and IDsAndDocsMaps
         * every one of the function that responsible for loading a specific map, uses an objectStreamer to do so
         */
        public void LoadDictionaryFromDisk(ActionEvent event)
        {
            try
            {
                LoadCorpusMap();
                LoadDocsMap();
                LoadCitiesMap();
                LoadIDsMap();
            }
            catch (FileNotFoundException e)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("One or more of the necessary dictionaries weren't found :(");
                alert.showAndWait();
                return;
            }
            boolean stemmerSelection = stemmerCheckBox.isSelected();
            if(stemmerSelection)
                dictionaryWithStemming = true;
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Loading the dictionaries was succeeded!");
            alert.showAndWait();

        }


    @FXML
    private void initChoiceBox()
    {
        languagesBox.setItems(languagesBoxOptions);
    }

    /**
     *opens a new window for writing the query after the inverted index and all the other relevant stuff are ready
     */
    public void OpenSearchWindow(ActionEvent actionEvent) throws IOException {

        if(Indexer.termsCorpusMap != null)
        {
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root1 = fxmlLoader.load(mainController.class.getResource("/engineWindow.fxml").openStream());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("engineWindow");
            stage.setScene(new Scene(root1, 650, 250));
            stage.show();
            boolean stemmerSelection = stemmerCheckBox.isSelected();
            engineController.setStemmerSelection(stemmerSelection);
            engineController.setPathToDisk(diskPath.getText());
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Please run the Indexer or load a dictionary before searching");
            alert.showAndWait();
            return;
        }
    }



}