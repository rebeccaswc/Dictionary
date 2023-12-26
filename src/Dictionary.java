import java.net.*;
import java.net.http.*;
import java.util.*;
import javafx.application.*;
import javafx.collections.*;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.json.*;

public class Dictionary extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("中文博大精深");

        TextField searchField = new TextField();
        Image image = new Image("logo.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);
        Button searchButton = new Button("🔎 查字典囉 !");
        Button recordButton = new Button("📚️ 歷史紀錄");

        Label resultLabel = new Label();
        ListView<String> resultList = new ListView<>();
        ListView<String> historyList = new ListView<>();

        searchButton.setOnAction(event -> {
            String keyword = searchField.getText().trim();
            // 檢查輸入是否為中文
            if (!keyword.isEmpty() && keyword.matches("[\\u4E00-\\u9FA5]+")) {
                String apiUrl = "https://pedia.cloud.edu.tw/api/v2/List?keyword=" + keyword
                        + "&api_key=1fb7a4cc-67d2-4f59-bc39-f7bf4d992ada";

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(apiUrl))
                        .build();

                httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                        .thenApply(HttpResponse::body)
                        .thenAccept(response -> {
                            JSONArray jsonArray = new JSONArray(response);
                            ObservableList<String> items = FXCollections.observableArrayList();

                            Map<Integer, List<String>> wordMap = new TreeMap<>(); // 使用TreeMap排序

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String wordTitle = jsonObject.getString("WordTitle");
                                String wordDesc = jsonObject.getString("WordDesc");

                                if (wordTitle.contains(keyword)) {
                                    int length = wordTitle.length();
                                    wordMap.computeIfAbsent(length, k -> new ArrayList<>())
                                            .add(wordTitle + "： " + wordDesc);
                                }
                            }

                            for (List<String> itemList : wordMap.values()) {
                                for (String item : itemList) {
                                    items.add(item);
                                }
                            }

                            Platform.runLater(() -> {
                                resultList.setItems(items);
                                resultLabel.setText("共搜尋到 " + items.size() + " 個詞語");
                            });
                        })
                        .exceptionally(e -> {
                            e.printStackTrace();
                            return null;
                        });
            } else {
                resultLabel.setText("請輸入中文 > <！");
                resultList.getItems().clear(); // 清空结果列表
            }
        });

        resultList.setOnMouseClicked(event -> {
            String selectedItem = resultList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                int colonIndex = selectedItem.indexOf("：");
                if (colonIndex != -1) {
                    String term = selectedItem.substring(0, colonIndex);
                    fetchTermDetails(term);
                }
            }
        });

        

        resultList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        recordButton.setOnAction(event -> {
            // 建立新視窗顯示歷史紀錄
            Stage historyStage = new Stage();
            historyStage.setTitle("歷史紀錄");
        
            ListView<String> historyListView = new ListView<>();
            ObservableList<String> historyItems = FXCollections.observableArrayList(historyList.getItems());
            historyListView.setItems(historyItems);
        
            VBox historyVBox = new VBox(10);
            historyVBox.setAlignment(Pos.CENTER);
            historyVBox.getChildren().addAll(historyListView);
        
            Scene historyScene = new Scene(historyVBox, 300, 400);
            historyStage.setScene(historyScene);
            historyStage.show();
        });
        recordButton.setOnAction(event -> {
            // 建立新視窗顯示歷史紀錄
            Stage historyStage = new Stage();
            historyStage.setTitle("歷史紀錄");
        
            ListView<String> historyListView = new ListView<>();
            ObservableList<String> historyItems = FXCollections.observableArrayList(historyList.getItems());
            historyListView.setItems(historyItems);
        
            VBox historyVBox = new VBox(10);
            historyVBox.setAlignment(Pos.CENTER);
            historyVBox.getChildren().addAll(historyListView);
        
            Scene historyScene = new Scene(historyVBox, 300, 400);
            historyStage.setScene(historyScene);
            historyStage.show();
        });
                
        
        // 當點擊 resultList 時，將選定的詞語加入歷史紀錄
        resultList.setOnMouseClicked(event -> {
            String selectedItem = resultList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                int colonIndex = selectedItem.indexOf("：");
                if (colonIndex != -1) {
                    String term = selectedItem.substring(0, colonIndex);
                    fetchTermDetails(term);
                    
                    // 加入歷史紀錄
                    if (!historyList.getItems().contains(term)) {
                        historyList.getItems().add(term);
                    }                    
                }
            }
        });

        VBox vbox = new VBox(10);
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(searchButton, recordButton);
        vbox.setAlignment(Pos.CENTER);
        searchField.setMaxWidth(200);
        vbox.getChildren().addAll(imageView, searchField, buttonBox, resultLabel, resultList);
        Scene scene = new Scene(vbox, 800, 500);
        scene.getStylesheets().add("style.css");
        vbox.getStyleClass().add("background");
        searchButton.getStyleClass().add("button");
        recordButton.getStyleClass().add("button");
        resultList.getStyleClass().add("list-cell");
        resultList.getStyleClass().add("resultlist");
        historyList.getStyleClass().add("list-cell");

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void fetchTermDetails(String term) {
        String apiUrl = "https://pedia.cloud.edu.tw/api/v2/Detail?term=" + term +
                "&api_key=1fb7a4cc-67d2-4f59-bc39-f7bf4d992ada";

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(apiUrl))
                .build();

        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject conciseDict = jsonObject.getJSONObject("concise_dict");

                    if (conciseDict != null) {
                        JSONArray heteronyms = conciseDict.getJSONArray("heteronyms");
                        if (heteronyms.length() > 0) {
                            JSONObject firstHeteronym = heteronyms.getJSONObject(0);
                            JSONArray definitions = firstHeteronym.getJSONArray("definitions");

                            String synonym = "";
                            String antonym = "";

                            JSONObject revisedDict = jsonObject.getJSONObject("revised_dict");
                            JSONArray revisedHeteronyms = revisedDict.getJSONArray("heteronyms");

                            if (revisedHeteronyms.length() > 0) {
                                JSONObject revisedFirstHeteronym = revisedHeteronyms.getJSONObject(0);
                                synonym = revisedFirstHeteronym.optString("synonym");
                                antonym = revisedFirstHeteronym.optString("antonym");

                                if (synonym.isEmpty() && antonym.isEmpty()) {
                                    synonym = "無資料可查詢";
                                    antonym = "無資料可查詢";
                                }
                            } else {
                                synonym = "無資料可查詢";
                                antonym = "無資料可查詢";
                            }
                            StringBuilder definitionsStr = new StringBuilder();
                            for (int i = 0; i < definitions.length(); i++) {
                                JSONObject definition = definitions.getJSONObject(i);
                                String def = definition.optString("def");

                                int startIndex = def.indexOf("[例]");
                                if (startIndex != -1) {
                                    String exampleSentence = def.substring(startIndex + 3);
                                    definitionsStr.append("例句：").append(exampleSentence.trim())
                                            .append("\n");
                                }
                            }

                            final String finalSynonym = synonym;
                            final String finalAntonym = antonym;
                            final String finalDefinitionsStr = definitionsStr.toString();

                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Detail Information");
                                alert.setHeaderText(null);

                                alert.setContentText("同義： " + finalSynonym + "\n\n反義： " + finalAntonym +
                                        "\n\n" + finalDefinitionsStr + "\n");

                                alert.showAndWait();
                            });
                        }
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

}
